package com.npc2048.dns.config;

import com.npc2048.dns.model.UpstreamDnsConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * DNS 服务器配置
 *
 * @author yuelong.liang
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dns")
public class DnsConfig {

    /**
     * 认证路径
     */
    private String authPath;

    /**
     * 上游 DNS 服务器配置列表
     */
    private List<UpstreamDnsConfig> upstreamDns = new ArrayList<>();

    /**
     * 默认上游 DNS 超时时间（毫秒）
     */
    private Integer defaultTimeout = Constants.DEFAULT_UPSTREAM_TIMEOUT;

    /**
     * 重试次数
     */
    private Integer retryCount = Constants.RETRY_COUNT;

    /**
     * 缓存最大条目数
     */
    private Integer cacheMaxSize = Constants.CACHE_MAX_SIZE;

    /**
     * 缓存最大权重（字符数）
     *  10M字符
     */
    private Long cacheMaxWeight = Constants.CACHE_MAX_WEIGHT;

    /**
     * 缓存容量模式：ENTRIES（条目数）或 WEIGHT（权重）
     */
    private String cacheCapacityMode = Constants.CACHE_CAPACITY_ENTRIES;

    /**
     * 缓存重建策略：CLEAR（清空）或 KEEP（保留）
     */
    private String cacheRebuildStrategy = Constants.CACHE_REBUILD_CLEAR;

    /**
     * 缓存默认 TTL（秒）
     */
    private Integer cacheDefaultTtl = Constants.CACHE_DEFAULT_TTL;

    /**
     * 监听端口
     */
    private Integer listenPort = Constants.DNS_SERVER_PORT;

    /**
     * 是否启用缓存
     */
    private Boolean cacheEnabled = Constants.CACHE_ENABLED;

    /**
     * 是否启用查询日志
     */
    private Boolean queryLogEnabled = Constants.QUERY_LOG_ENABLED;
}
