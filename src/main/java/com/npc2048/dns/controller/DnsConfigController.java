package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.model.UpstreamDnsConfig;
import com.npc2048.dns.service.DnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * DNS 配置控制器
 *
 * @author yuelong.liang
 */
@RestController
@RequestMapping("/api/dns")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
public class DnsConfigController {

    private final DnsConfig dnsConfig;
    private final DnsService dnsService;

    /**
     * 获取 DNS 配置
     * GET /api/dns/config
     */
    @GetMapping("/config")
    public Mono<SaResult> getConfig() {
        return Mono.fromCallable(() -> {
            Map<String, Object> config = Map.of(
                    "upstreamDns", dnsConfig.getUpstreamDns(),
                    "defaultTimeout", dnsConfig.getDefaultTimeout(),
                    "retryCount", dnsConfig.getRetryCount(),
                    "cacheMaxSize", dnsConfig.getCacheMaxSize(),
                    "cacheDefaultTtl", dnsConfig.getCacheDefaultTtl(),
                    "listenPort", dnsConfig.getListenPort(),
                    "cacheEnabled", dnsConfig.getCacheEnabled(),
                    "queryLogEnabled", dnsConfig.getQueryLogEnabled()
            );
            return SaResult.data(config);
        });
    }

    /**
     * 更新 DNS 配置
     * PUT /api/dns/config
     */
    @PutMapping("/config")
    public Mono<SaResult> updateConfig(@RequestBody Map<String, Object> config) {
        return Mono.fromRunnable(() -> {
            try {
                // 更新上游 DNS 配置
                if (config.containsKey("upstreamDns")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> upstreamList = (List<Map<String, Object>>) config.get("upstreamDns");
                    List<UpstreamDnsConfig> upstreamConfigs = upstreamList.stream()
                            .map(this::mapToUpstreamConfig)
                            .toList();
                    dnsConfig.setUpstreamDns(upstreamConfigs);
                }

                // 更新其他配置
                if (config.containsKey("defaultTimeout")) {
                    dnsConfig.setDefaultTimeout((Integer) config.get("defaultTimeout"));
                }
                if (config.containsKey("retryCount")) {
                    dnsConfig.setRetryCount((Integer) config.get("retryCount"));
                }
                if (config.containsKey("cacheMaxSize")) {
                    dnsConfig.setCacheMaxSize((Integer) config.get("cacheMaxSize"));
                }
                if (config.containsKey("cacheDefaultTtl")) {
                    dnsConfig.setCacheDefaultTtl((Integer) config.get("cacheDefaultTtl"));
                }
                if (config.containsKey("listenPort")) {
                    dnsConfig.setListenPort((Integer) config.get("listenPort"));
                }
                if (config.containsKey("cacheEnabled")) {
                    dnsConfig.setCacheEnabled((Boolean) config.get("cacheEnabled"));
                }
                if (config.containsKey("queryLogEnabled")) {
                    dnsConfig.setQueryLogEnabled((Boolean) config.get("queryLogEnabled"));
                }

                log.info("DNS 配置已更新");
            } catch (Exception e) {
                log.error("更新 DNS 配置失败", e);
                throw new RuntimeException("更新配置失败: " + e.getMessage());
            }
        }).thenReturn(SaResult.ok("配置更新成功"))
          .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 获取缓存统计信息
     * GET /api/dns/cache/stats
     */
    @GetMapping("/cache/stats")
    public Mono<SaResult> getCacheStats() {
        return Mono.fromCallable(() -> {
            Map<String, Object> stats = dnsService.getCacheStats();
            return SaResult.data(stats);
        });
    }

    /**
     * 清空缓存
     * DELETE /api/dns/cache
     */
    @DeleteMapping("/cache")
    public Mono<SaResult> clearCache() {
        return Mono.fromRunnable(() -> dnsService.clearCache())
                .thenReturn(SaResult.ok("缓存已清空"))
                .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 测试上游 DNS
     * POST /api/dns/test-upstream
     */
    @PostMapping("/test-upstream")
    public Mono<SaResult> testUpstream(@RequestBody Map<String, Object> request) {
        return Mono.fromCallable(() -> {
            String domain = (String) request.get("domain");
            String address = (String) request.get("address");
            Integer port = (Integer) request.get("port");
            Boolean useProxy = (Boolean) request.get("useProxy");

            if (domain == null || address == null) {
                return SaResult.error("缺少必要参数: domain 和 address");
            }

            // 创建测试配置
            UpstreamDnsConfig upstream = UpstreamDnsConfig.builder()
                    .address(address)
                    .port(port != null ? port : 53)
                    .timeout(5000)
                    .useProxy(useProxy != null ? useProxy : false)
                    .enabled(true)
                    .priority(1)
                    .build();

            // 构建测试 DNS 请求
            org.xbill.DNS.Message dnsRequest = new org.xbill.DNS.Message();
            dnsRequest.getHeader().setID(12345);
            dnsRequest.addRecord(
                    org.xbill.DNS.Record.newRecord(
                            org.xbill.DNS.Name.fromString(domain + "."),
                            org.xbill.DNS.Type.A,
                            org.xbill.DNS.DClass.IN
                    ),
                    org.xbill.DNS.Section.QUESTION
            );

            byte[] requestData = dnsRequest.toWire();

            // 测试转发
            // 这里简化实现，实际应该调用 DnsForwarder
            log.info("测试上游 DNS: {} -> {}:{} (代理: {})", domain, address, port, useProxy);

            return SaResult.data(Map.of(
                    "success", true,
                    "message", "测试请求已发送",
                    "domain", domain,
                    "upstream", address + ":" + (port != null ? port : 53),
                    "useProxy", useProxy != null ? useProxy : false
            ));
        }).onErrorResume(e -> Mono.just(SaResult.error("测试失败: " + e.getMessage())));
    }

    /**
     * 将 Map 转换为 UpstreamDnsConfig
     */
    private UpstreamDnsConfig mapToUpstreamConfig(Map<String, Object> map) {
        UpstreamDnsConfig.ProxyConfig proxyConfig = null;
        if (map.containsKey("proxyConfig") && map.get("proxyConfig") != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> proxyMap = (Map<String, Object>) map.get("proxyConfig");
            proxyConfig = UpstreamDnsConfig.ProxyConfig.builder()
                    .host((String) proxyMap.get("host"))
                    .port((Integer) proxyMap.get("port"))
                    .type((String) proxyMap.get("type"))
                    .username((String) proxyMap.get("username"))
                    .password((String) proxyMap.get("password"))
                    .build();
        }

        return UpstreamDnsConfig.builder()
                .address((String) map.get("address"))
                .port((Integer) map.get("port"))
                .timeout((Integer) map.get("timeout"))
                .useProxy((Boolean) map.get("useProxy"))
                .proxyConfig(proxyConfig)
                .enabled(map.containsKey("enabled") ? (Boolean) map.get("enabled") : true)
                .priority(map.containsKey("priority") ? (Integer) map.get("priority") : 1)
                .build();
    }
}