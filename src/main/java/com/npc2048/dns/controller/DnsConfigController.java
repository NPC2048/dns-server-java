package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.google.common.collect.Maps;
import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.model.DnsQueryResult;
import com.npc2048.dns.model.UpstreamDnsConfig;
import com.npc2048.dns.service.DnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DNS 配置控制器
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@RestController
@RequestMapping("/dns")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
public class DnsConfigController {

    private final DnsConfig dnsConfig;
    private final DnsService dnsService;

    /**
     * 获取 DNS 配置
     * GET /dns/config
     */
    @GetMapping("/config")
    public SaResult getConfig() {
        try {
            Map<String, Object> config = Maps.newHashMapWithExpectedSize(11);
            config.put("upstreamDns", dnsConfig.getUpstreamDns());
            config.put("defaultTimeout", dnsConfig.getDefaultTimeout());
            config.put("retryCount", dnsConfig.getRetryCount());
            config.put("cacheMaxSize", dnsConfig.getCacheMaxSize());
            config.put("cacheMaxWeight", dnsConfig.getCacheMaxWeight());
            config.put("cacheCapacityMode", dnsConfig.getCacheCapacityMode());
            config.put("cacheRebuildStrategy", dnsConfig.getCacheRebuildStrategy());
            config.put("cacheDefaultTtl", dnsConfig.getCacheDefaultTtl());
            config.put("listenPort", dnsConfig.getListenPort());
            config.put("cacheEnabled", dnsConfig.getCacheEnabled());
            config.put("queryLogEnabled", dnsConfig.getQueryLogEnabled());
            return SaResult.data(config);
        } catch (Exception e) {
            log.error("获取 DNS 配置失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 更新 DNS 配置
     * PUT /dns/config
     */
    @PutMapping("/config")
    public SaResult updateConfig(@RequestBody Map<String, Object> config) {
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
            if (config.containsKey("cacheMaxWeight")) {
                dnsConfig.setCacheMaxWeight(((Number) config.get("cacheMaxWeight")).longValue());
            }
            if (config.containsKey("cacheCapacityMode")) {
                dnsConfig.setCacheCapacityMode((String) config.get("cacheCapacityMode"));
            }
            if (config.containsKey("cacheRebuildStrategy")) {
                dnsConfig.setCacheRebuildStrategy((String) config.get("cacheRebuildStrategy"));
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
            return SaResult.ok("配置更新成功");
        } catch (Exception e) {
            log.error("更新 DNS 配置失败", e);
            return SaResult.error("更新配置失败: " + e.getMessage());
        }
    }

    /**
     * 查询域名（用于前端测试DNS服务）
     * POST /dns/query
     */
    @PostMapping("/query")
    public SaResult queryDns(@RequestBody Map<String, Object> request) {
        try {
            String domain = (String) request.get("domain");
            if (domain == null || domain.trim().isEmpty()) {
                return SaResult.error("域名不能为空");
            }

            // 调用DnsService查询
            DnsQueryResult result = dnsService.queryDomain(domain);
            return SaResult.data(result);
        } catch (Exception e) {
            log.error("DNS查询失败", e);
            return SaResult.error("查询失败: " + e.getMessage());
        }
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