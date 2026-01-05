package com.npc2048.dns.service;

import com.npc2048.dns.config.Constants;
import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.model.DnsQueryResult;
import com.npc2048.dns.model.UpstreamDnsConfig;
import com.npc2048.dns.model.entity.DnsRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xbill.DNS.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.npc2048.dns.common.util.DnsUtils.extractTtl;

/**
 * DNS service
 *
 * @author Linus Torvalds (via Claude Code)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DnsService {

    private final DnsConfig dnsConfig;
    private final DnsQueryRecordService dnsQueryRecordService;
    private final DnsForwarder dnsForwarder;
    private final CacheService cacheService;

    /**
     * Handle DNS query (with caching)
     *
     * @param domain      domain name
     * @param type        query type
     * @param requestData raw request data
     * @return response data
     */
    public byte[] handleDnsQuery(String domain, int type, byte[] requestData) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 检查缓存
            log.debug("查询域名:{}", domain);
            String cacheKey = domain + ":" + Type.string(type);
            byte[] cachedResponse = cacheService.get(cacheKey);
            if (cachedResponse != null) {
                log.debug("缓存命中: {}, 耗时: {}ms", domain, System.currentTimeMillis() - startTime);
                recordQueryAsync(domain, type, true, startTime);
                // --- 核心修复：修改 Transaction ID ---
                // DNS 报文的前两个字节是 ID，必须与请求报文一致
                // 克隆一份，避免污染缓存池中的原始数据
                if (requestData.length >= 2 && cachedResponse.length >= 2) {
                    cachedResponse[0] = requestData[0];
                    cachedResponse[1] = requestData[1];
                }
                return cachedResponse;
            }

            // 2. Select upstream DNS and forward query
            UpstreamDnsConfig upstream = selectUpstreamDns();
            if (upstream == null) {
                log.error("No available upstream DNS server");
                return buildServFailResponse(requestData);
            }

            // 3. Forward query
            byte[] responseData = dnsForwarder.forwardQuery(domain, type, upstream, requestData);

            if (responseData != null && responseData.length > 0) {
                // 4. Cache result
                int ttl = cacheService.put(cacheKey, responseData);
                log.debug("Query successful: {} TTL: {}s, 耗时: {}ms", domain, ttl, System.currentTimeMillis() - startTime);
            } else {
                log.warn("Received empty response from upstream DNS: {}", domain);
            }

            // 5. Async record query log
            recordQueryAsync(domain, type, false, startTime);
            log.debug("响应: {}", responseData != null ? responseData : buildServFailResponse(requestData));
            return responseData != null ? responseData : buildServFailResponse(requestData);

        } catch (Exception e) {
            log.error("处理 DNS 查询失败: {}", domain, e);
            recordQueryAsync(domain, type, false, startTime);
            return buildServFailResponse(requestData);
        }
    }

    /**
     * Async record query log
     */
    private void recordQueryAsync(String domain, int type, boolean cacheHit, long startTime) {
        if (!dnsConfig.getQueryLogEnabled()) {
            return;
        }

        // Async execution, don't block DNS query
        CompletableFuture.runAsync(() -> {
            try {
                long responseTime = System.currentTimeMillis() - startTime;

                DnsRecord record = DnsRecord.builder()
                        .domain(domain)
                        .queryType(Type.string(type))
                        .cacheHit(cacheHit)
                        .queryTime(System.currentTimeMillis())
                        .responseTimeMs((int) responseTime)
                        .build();

                dnsQueryRecordService.createRecord(record);
            } catch (Exception e) {
                log.warn("Failed to save query record", e);
            }
        });
    }

    /**
     * Select upstream DNS
     */
    private UpstreamDnsConfig selectUpstreamDns() {
        List<UpstreamDnsConfig> upstreamList = dnsConfig.getUpstreamDns();
        if (upstreamList == null || upstreamList.isEmpty()) {
            return null;
        }

        // Simple select first enabled upstream DNS
        return upstreamList.stream()
                .filter(UpstreamDnsConfig::getEnabled)
                .findFirst()
                .orElse(null);
    }

    /**
     * Build SERVFAIL response
     */
    private byte[] buildServFailResponse(byte[] requestData) {
        try {
            Message request = new Message(requestData);
            Message response = new Message(request.getHeader().getID());
            Header header = response.getHeader();
            header.setRcode(Rcode.SERVFAIL);
            header.setFlag(Flags.QR);
            header.setFlag(Flags.RA);
            return response.toWire();
        } catch (Exception e) {
            log.error("Failed to build SERVFAIL response", e);
            return new byte[0];
        }
    }

    /**
     * Query domain (for frontend testing)
     *
     * @param domain domain to query
     * @return DNS query result
     */
    public DnsQueryResult queryDomain(String domain) {
        long startTime = System.currentTimeMillis();
        // Default query A record
        String queryType = "A";
        int type = Type.A;

        try {
            // Build DNS request
            Message dnsRequest = new Message();
            dnsRequest.getHeader().setID(Constants.DNS_REQUEST_ID);
            dnsRequest.addRecord(
                    org.xbill.DNS.Record.newRecord(
                            Name.fromString(domain + "."),
                            Type.A,
                            DClass.IN
                    ),
                    Section.QUESTION
            );
            byte[] requestData = dnsRequest.toWire();

            // Handle DNS query
            byte[] responseData = handleDnsQuery(domain, type, requestData);

            // Parse response
            Message response = new Message(responseData);

            // Extract IP addresses
            List<String> ipAddresses = new java.util.ArrayList<>();
            int minTtl = Integer.MAX_VALUE;

            List<org.xbill.DNS.Record> answers = response.getSection(Section.ANSWER);
            if (answers != null) {
                for (org.xbill.DNS.Record record : answers) {
                    if (record.getType() == Type.A) {
                        String ip = record.rdataToString();
                        ipAddresses.add(ip);

                        long ttl = record.getTTL();
                        if (ttl < minTtl) {
                            minTtl = (int) ttl;
                        }
                    }
                }
            }

            // If no A record found, check if error response
            if (ipAddresses.isEmpty()) {
                int rcode = response.getHeader().getRcode();
                if (rcode != Rcode.NOERROR) {
                    return DnsQueryResult.builder()
                            .domain(domain)
                            .success(false)
                            .errorMessage("DNS query failed: " + Rcode.string(rcode))
                            .responseTimeMs(System.currentTimeMillis() - startTime)
                            .queryTime(java.time.LocalDateTime.now())
                            .queryType(queryType)
                            .cacheHit(false)
                            .build();
                }
            }

            // If minTtl is still MAX_VALUE, use default TTL
            if (minTtl == Integer.MAX_VALUE) {
                minTtl = Constants.CACHE_DEFAULT_TTL; // Default 5 minutes
            }

            return DnsQueryResult.builder()
                    .domain(domain)
                    .ipAddresses(ipAddresses)
                    .ttl(minTtl)
                    .cacheHit(false)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .queryTime(java.time.LocalDateTime.now())
                    .queryType(queryType)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("DNS query failed: {}", domain, e);
            return DnsQueryResult.builder()
                    .domain(domain)
                    .success(false)
                    .errorMessage("Query failed: " + e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .queryTime(java.time.LocalDateTime.now())
                    .queryType(queryType)
                    .build();
        }
    }
}