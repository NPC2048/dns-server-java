package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.google.common.collect.Maps;
import com.npc2048.dns.config.Constants;
import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.model.DnsQueryResult;
import com.npc2048.dns.model.UpstreamDnsConfig;
import com.npc2048.dns.network.NettyDnsServer;
import com.npc2048.dns.service.DnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    private final NettyDnsServer nettyDnsServer;

    /**
     * 获取 DNS 配置
     * GET /dns/config
     */
    @GetMapping("/config")
    public SaResult getConfig() {
        try {
            Map<String, Object> config = Maps.newHashMapWithExpectedSize(11);
            config.put(Constants.CONFIG_KEY_UPSTREAM_DNS, dnsConfig.getUpstreamDns());
            config.put(Constants.CONFIG_KEY_DEFAULT_TIMEOUT, dnsConfig.getDefaultTimeout());
            config.put(Constants.CONFIG_KEY_RETRY_COUNT, dnsConfig.getRetryCount());
            config.put(Constants.CONFIG_KEY_CACHE_MAX_SIZE, dnsConfig.getCacheMaxSize());
            config.put(Constants.CONFIG_KEY_CACHE_MAX_WEIGHT, dnsConfig.getCacheMaxWeight());
            config.put(Constants.CONFIG_KEY_CACHE_CAPACITY_MODE, dnsConfig.getCacheCapacityMode());
            config.put(Constants.CONFIG_KEY_CACHE_REBUILD_STRATEGY, dnsConfig.getCacheRebuildStrategy());
            config.put(Constants.CONFIG_KEY_CACHE_DEFAULT_TTL, dnsConfig.getCacheDefaultTtl());
            config.put(Constants.CONFIG_KEY_LISTEN_PORT, dnsConfig.getListenPort());
            config.put(Constants.CONFIG_KEY_CACHE_ENABLED, dnsConfig.getCacheEnabled());
            config.put(Constants.CONFIG_KEY_QUERY_LOG_ENABLED, dnsConfig.getQueryLogEnabled());
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
            if (config.containsKey(Constants.CONFIG_KEY_UPSTREAM_DNS)) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> upstreamList = (List<Map<String, Object>>) config.get(Constants.CONFIG_KEY_UPSTREAM_DNS);
                List<UpstreamDnsConfig> upstreamConfigs = upstreamList.stream()
                        .map(this::mapToUpstreamConfig)
                        .toList();
                dnsConfig.setUpstreamDns(upstreamConfigs);
            }

            // 更新其他配置
            if (config.containsKey(Constants.CONFIG_KEY_DEFAULT_TIMEOUT)) {
                dnsConfig.setDefaultTimeout((Integer) config.get(Constants.CONFIG_KEY_DEFAULT_TIMEOUT));
            }
            if (config.containsKey(Constants.CONFIG_KEY_RETRY_COUNT)) {
                dnsConfig.setRetryCount((Integer) config.get(Constants.CONFIG_KEY_RETRY_COUNT));
            }
            if (config.containsKey(Constants.CONFIG_KEY_CACHE_MAX_SIZE)) {
                dnsConfig.setCacheMaxSize((Integer) config.get(Constants.CONFIG_KEY_CACHE_MAX_SIZE));
            }
            if (config.containsKey(Constants.CONFIG_KEY_CACHE_MAX_WEIGHT)) {
                dnsConfig.setCacheMaxWeight(((Number) config.get(Constants.CONFIG_KEY_CACHE_MAX_WEIGHT)).longValue());
            }
            if (config.containsKey(Constants.CONFIG_KEY_CACHE_CAPACITY_MODE)) {
                dnsConfig.setCacheCapacityMode((String) config.get(Constants.CONFIG_KEY_CACHE_CAPACITY_MODE));
            }
            if (config.containsKey(Constants.CONFIG_KEY_CACHE_REBUILD_STRATEGY)) {
                dnsConfig.setCacheRebuildStrategy((String) config.get(Constants.CONFIG_KEY_CACHE_REBUILD_STRATEGY));
            }
            if (config.containsKey(Constants.CONFIG_KEY_CACHE_DEFAULT_TTL)) {
                dnsConfig.setCacheDefaultTtl((Integer) config.get(Constants.CONFIG_KEY_CACHE_DEFAULT_TTL));
            }
            if (config.containsKey(Constants.CONFIG_KEY_LISTEN_PORT)) {
                dnsConfig.setListenPort((Integer) config.get(Constants.CONFIG_KEY_LISTEN_PORT));
                nettyDnsServer.stopServer();
                nettyDnsServer.startServer();
            }
            if (config.containsKey(Constants.CONFIG_KEY_CACHE_ENABLED)) {
                dnsConfig.setCacheEnabled((Boolean) config.get(Constants.CONFIG_KEY_CACHE_ENABLED));
            }
            if (config.containsKey(Constants.CONFIG_KEY_QUERY_LOG_ENABLED)) {
                dnsConfig.setQueryLogEnabled((Boolean) config.get(Constants.CONFIG_KEY_QUERY_LOG_ENABLED));
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
            String domain = (String) request.get(Constants.CONFIG_KEY_DOMAIN);
            if (domain == null || domain.trim().isEmpty()) {
                return SaResult.error("域名不能为空");
            }

            // 调用 DnsService 查询
            DnsQueryResult result = dnsService.queryDomain(domain);
            return SaResult.data(result);
        } catch (Exception e) {
            log.error("DNS 查询失败", e);
            return SaResult.error("查询失败: " + e.getMessage());
        }
    }


    /**
     * 将 Map 转换为 UpstreamDnsConfig
     */
    private UpstreamDnsConfig mapToUpstreamConfig(Map<String, Object> map) {
        UpstreamDnsConfig.ProxyConfig proxyConfig = null;
        if (map.containsKey(Constants.CONFIG_KEY_PROXY_CONFIG) && map.get(Constants.CONFIG_KEY_PROXY_CONFIG) != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> proxyMap = (Map<String, Object>) map.get(Constants.CONFIG_KEY_PROXY_CONFIG);
            proxyConfig = UpstreamDnsConfig.ProxyConfig.builder()
                    .host((String) proxyMap.get(Constants.CONFIG_KEY_HOST))
                    .port((Integer) proxyMap.get(Constants.CONFIG_KEY_PORT))
                    .type((String) proxyMap.get(Constants.CONFIG_KEY_TYPE))
                    .username((String) proxyMap.get(Constants.CONFIG_KEY_USERNAME))
                    .password((String) proxyMap.get(Constants.CONFIG_KEY_PASSWORD))
                    .build();
        }

        return UpstreamDnsConfig.builder()
                .address((String) map.get(Constants.CONFIG_KEY_ADDRESS))
                .port((Integer) map.get(Constants.CONFIG_KEY_PORT))
                .timeout((Integer) map.get(Constants.CONFIG_KEY_TIMEOUT))
                .useProxy((Boolean) map.get(Constants.CONFIG_KEY_USE_PROXY))
                .proxyConfig(proxyConfig)
                .enabled(map.containsKey(Constants.CONFIG_KEY_ENABLED) ? (Boolean) map.get(Constants.CONFIG_KEY_ENABLED) : Constants.ENABLED)
                .priority(map.containsKey(Constants.CONFIG_KEY_PRIORITY) ? (Integer) map.get(Constants.CONFIG_KEY_PRIORITY) : Constants.DEFAULT_UPSTREAM_PRIORITY)
                .build();
    }
}