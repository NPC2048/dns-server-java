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
    private Integer defaultTimeout = 5000;

    /**
     * 重试次数
     */
    private Integer retryCount = 3;

    /**
     * 缓存最大条目数
     */
    private Integer cacheMaxSize = 10000;

    /**
     * 缓存最大权重（字符数）
     *  10M字符
     */
    private Long cacheMaxWeight = 10485760L;

    /**
     * 缓存容量模式：ENTRIES（条目数）或 WEIGHT（权重）
     */
    private String cacheCapacityMode = "ENTRIES";

    /**
     * 缓存重建策略：CLEAR（清空）或 KEEP（保留）
     */
    private String cacheRebuildStrategy = "CLEAR";

    /**
     * 缓存默认 TTL（秒）
     */
    private Integer cacheDefaultTtl = 300;

    /**
     * 监听端口
     */
    private Integer listenPort = 53;

    /**
     * 是否启用缓存
     */
    private Boolean cacheEnabled = true;

    /**
     * 是否启用查询日志
     */
    private Boolean queryLogEnabled = true;
}
