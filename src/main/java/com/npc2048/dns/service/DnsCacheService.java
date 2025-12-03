package com.npc2048.dns.service;

import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.model.CacheEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DNS 缓存服务
 *
 * @author yuelong.liang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DnsCacheService {

    private final DnsConfig dnsConfig;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 获取缓存条目
     *
     * @param domain 域名
     * @param type   查询类型
     * @return 缓存条目，如果不存在或已过期则返回 null
     */
    public Mono<CacheEntry> get(String domain, int type) {
        return Mono.fromCallable(() -> {
            String key = buildCacheKey(domain, type);
            CacheEntry entry = cache.get(key);

            if (entry == null) {
                return null;
            }

            if (entry.isExpired()) {
                // 异步移除过期缓存
                removeAsync(key);
                return null;
            }

            // 记录访问
            entry.recordAccess();
            return entry;
        });
    }

    /**
     * 保存缓存条目
     *
     * @param domain       域名
     * @param type         查询类型
     * @param responseData 响应数据
     * @param ttl          TTL（秒）
     * @return Mono<Void>
     */
    public Mono<Void> put(String domain, int type, String responseData, int ttl) {
        return Mono.fromRunnable(() -> {
            if (!dnsConfig.getCacheEnabled()) {
                return;
            }

            String key = buildCacheKey(domain, type);
            CacheEntry entry = CacheEntry.create(domain, org.xbill.DNS.Type.string(type), responseData, ttl);

            cache.put(key, entry);
            log.debug("缓存保存: {} 类型: {} TTL: {}s", domain, type, ttl);

            // 检查缓存大小，如果超过限制则淘汰最旧的条目
            if (cache.size() > dnsConfig.getCacheMaxSize()) {
                evictOldestEntry();
            }
        });
    }

    /**
     * 移除缓存条目
     *
     * @param domain 域名
     * @param type   查询类型
     * @return Mono<Void>
     */
    public Mono<Void> remove(String domain, int type) {
        return Mono.fromRunnable(() -> {
            String key = buildCacheKey(domain, type);
            cache.remove(key);
            log.debug("缓存移除: {} 类型: {}", domain, type);
        });
    }

    /**
     * 清空缓存
     *
     * @return Mono<Void>
     */
    public Mono<Void> clear() {
        return Mono.fromRunnable(() -> {
            cache.clear();
            log.info("DNS 缓存已清空");
        });
    }

    /**
     * 获取所有缓存条目
     *
     * @return 缓存条目流
     */
    public Flux<CacheEntry> getAll() {
        return Flux.fromIterable(cache.values())
                .filter(entry -> !entry.isExpired());
    }

    /**
     * 获取缓存统计信息
     *
     * @return 统计信息
     */
    public Mono<Map<String, Object>> getStats() {
        return Mono.fromCallable(() -> {
            long validCount = cache.values().stream()
                    .filter(entry -> !entry.isExpired())
                    .count();

            long expiredCount = cache.size() - validCount;

            return Map.of(
                    "totalSize", cache.size(),
                    "validCount", validCount,
                    "expiredCount", expiredCount,
                    "maxSize", dnsConfig.getCacheMaxSize(),
                    "usageRate", (double) cache.size() / dnsConfig.getCacheMaxSize()
            );
        });
    }

    /**
     * 清理过期缓存
     *
     * @return 清理的条目数
     */
    public Mono<Long> cleanupExpired() {
        return Mono.fromCallable(() -> {
            long before = cache.size();
            cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            long after = cache.size();
            long removed = before - after;

            if (removed > 0) {
                log.debug("清理了 {} 个过期缓存条目", removed);
            }

            return removed;
        });
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(String domain, int type) {
        return domain.toLowerCase() + ":" + type;
    }

    /**
     * 异步移除缓存条目
     */
    private void removeAsync(String key) {
        Mono.fromRunnable(() -> cache.remove(key))
                .subscribe();
    }

    /**
     * 淘汰最旧的缓存条目
     */
    private void evictOldestEntry() {
        cache.entrySet().stream()
                .min(Comparator.comparingLong(a -> a.getValue().getLastAccessTime()))
                .ifPresent(entry -> {
                    cache.remove(entry.getKey());
                    log.debug("淘汰缓存条目: {}", entry.getKey());
                });
    }
}