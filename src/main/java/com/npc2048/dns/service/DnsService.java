package com.npc2048.dns.service;

import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.model.CacheEntry;
import com.npc2048.dns.model.DnsQueryRecord;
import com.npc2048.dns.model.UpstreamDnsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DNS 服务
 *
 * @author yuelong.liang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DnsService {

    private final DnsConfig dnsConfig;
    private final DnsQueryRecordService dnsQueryRecordService;
    private final DnsCacheService dnsCacheService;
    private final DnsForwarder dnsForwarder;

    // 简单的内存缓存（后续会移到 DnsCacheService）
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 处理 DNS 查询
     *
     * @param domain     域名
     * @param type       查询类型
     * @param requestData 原始请求数据
     * @return 响应数据
     */
    public byte[] handleDnsQuery(String domain, int type, byte[] requestData) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        byte[] responseData = null;

        try {
            // 1. 检查缓存
            String cacheKey = buildCacheKey(domain, type);
            CacheEntry cacheEntry = cache.get(cacheKey);

            if (cacheEntry != null && !cacheEntry.isExpired()) {
                // 缓存命中
                cacheHit = true;
                cacheEntry.recordAccess();
                responseData = cacheEntry.getResponseData().getBytes();
                log.debug("缓存命中: {}", domain);
            } else {
                // 缓存未命中，转发到上游 DNS
                cacheHit = false;
                if (cacheEntry != null) {
                    // 移除过期缓存
                    cache.remove(cacheKey);
                }

                // 选择上游 DNS
                UpstreamDnsConfig upstream = selectUpstreamDns();
                if (upstream == null) {
                    log.error("没有可用的上游 DNS 服务器");
                    return buildServFailResponse(requestData);
                }

                // 转发查询
                responseData = dnsForwarder.forwardQuery(domain, type, upstream, requestData);

                // 缓存结果
                if (responseData != null && responseData.length > 0) {
                    cacheResult(domain, type, responseData);
                }
            }

            // 记录查询日志
            recordQueryLog(domain, type, cacheHit, startTime);

            return responseData != null ? responseData : buildServFailResponse(requestData);

        } catch (Exception e) {
            log.error("处理 DNS 查询失败: {}", domain, e);
            return buildServFailResponse(requestData);
        }
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String domain, int type) {
        return domain.toLowerCase() + ":" + type;
    }

    /**
     * 选择上游 DNS
     */
    private UpstreamDnsConfig selectUpstreamDns() {
        List<UpstreamDnsConfig> upstreamList = dnsConfig.getUpstreamDns();
        if (upstreamList == null || upstreamList.isEmpty()) {
            return null;
        }

        // 简单选择第一个启用的上游 DNS
        return upstreamList.stream()
                .filter(UpstreamDnsConfig::getEnabled)
                .findFirst()
                .orElse(null);
    }

    /**
     * 缓存结果
     */
    private void cacheResult(String domain, int type, byte[] responseData) {
        if (!dnsConfig.getCacheEnabled()) {
            return;
        }

        try {
            // 解析响应获取 TTL
            org.xbill.DNS.Message response = new org.xbill.DNS.Message(responseData);
            int ttl = extractTtlFromResponse(response);

            String cacheKey = buildCacheKey(domain, type);
            CacheEntry entry = CacheEntry.create(
                    domain,
                    org.xbill.DNS.Type.string(type),
                    new String(responseData),
                    ttl
            );

            cache.put(cacheKey, entry);

            // 简单的 LRU 淘汰
            if (cache.size() > dnsConfig.getCacheMaxSize()) {
                evictOldestEntry();
            }

        } catch (Exception e) {
            log.warn("缓存 DNS 结果失败: {}", domain, e);
        }
    }

    /**
     * 从响应中提取 TTL
     */
    private int extractTtlFromResponse(org.xbill.DNS.Message response) {
        try {
            org.xbill.DNS.Record[] answers = response.getSectionArray(org.xbill.DNS.Section.ANSWER);
            if (answers != null && answers.length > 0) {
                long ttl = answers[0].getTTL();
                // 确保 TTL 在 int 范围内
                if (ttl > Integer.MAX_VALUE) {
                    return Integer.MAX_VALUE;
                }
                return (int) ttl;
            }
        } catch (Exception e) {
            log.warn("提取 TTL 失败", e);
        }
        return dnsConfig.getCacheDefaultTtl();
    }

    /**
     * 淘汰最旧的缓存条目
     */
    private void evictOldestEntry() {
        cache.entrySet().stream()
                .min((a, b) -> Long.compare(
                        a.getValue().getLastAccessTime(),
                        b.getValue().getLastAccessTime()
                ))
                .ifPresent(entry -> cache.remove(entry.getKey()));
    }

    /**
     * 构建 SERVFAIL 响应
     */
    private byte[] buildServFailResponse(byte[] requestData) {
        try {
            org.xbill.DNS.Message request = new org.xbill.DNS.Message(requestData);
            org.xbill.DNS.Message response = new org.xbill.DNS.Message(request.getHeader().getID());
            response.getHeader().setRcode(org.xbill.DNS.Rcode.SERVFAIL);
            response.getHeader().setFlag(org.xbill.DNS.Flags.QR);
            response.getHeader().setFlag(org.xbill.DNS.Flags.RA);
            return response.toWire();
        } catch (Exception e) {
            log.error("构建 SERVFAIL 响应失败", e);
            return new byte[0];
        }
    }

    /**
     * 记录查询日志
     */
    private void recordQueryLog(String domain, int type, boolean cacheHit, long startTime) {
        if (!dnsConfig.getQueryLogEnabled()) {
            return;
        }

        long responseTime = System.currentTimeMillis() - startTime;

        DnsQueryRecord record = DnsQueryRecord.builder()
                .domain(domain)
                .queryType(org.xbill.DNS.Type.string(type))
                .cacheHit(cacheHit)
                .queryTime(LocalDateTime.now())
                .responseTimeMs((int) responseTime)
                .build();

        // 异步保存记录
        Mono.fromRunnable(() -> {
            try {
                dnsQueryRecordService.createRecord(record).subscribe();
            } catch (Exception e) {
                log.warn("保存查询记录失败", e);
            }
        }).subscribe();
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
                "size", cache.size(),
                "maxSize", dnsConfig.getCacheMaxSize(),
                "hitRate", calculateHitRate(),
                "entries", cache.values().stream()
                        .map(entry -> Map.of(
                                "domain", entry.getDomain(),
                                "type", entry.getQueryType(),
                                "ttl", entry.getTtl(),
                                "expireTime", entry.getExpireTime(),
                                "accessCount", entry.getAccessCount()
                        ))
                        .toList()
        );
    }

    /**
     * 计算命中率
     */
    private double calculateHitRate() {
        // 简化实现，实际应该从统计数据中计算
        return 0.0;
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        cache.clear();
        log.info("DNS 缓存已清空");
    }
}