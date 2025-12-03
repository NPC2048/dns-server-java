package com.npc2048.dns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 统计数据 DTO
 *
 * @author yuelong.liang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 总查询数
     */
    private Long totalQueries;

    /**
     * 缓存命中数
     */
    private Long cacheHits;

    /**
     * 缓存未命中数
     */
    private Long cacheMisses;

    /**
     * 缓存命中率
     */
    private Double cacheHitRate;

    /**
     * 平均响应时间（毫秒）
     */
    private Double avgResponseTime;

    /**
     * 当前 QPS（每秒查询数）
     */
    private Double currentQps;

    /**
     * 缓存大小
     */
    private Integer cacheSize;

    /**
     * 缓存使用率（百分比）
     */
    private Double cacheUsage;

    /**
     * Top N 域名
     */
    private List<DomainCount> topDomains;

    /**
     * 查询类型分布
     */
    private Map<String, Long> queryTypes;

    /**
     * 上游 DNS 状态
     */
    private List<UpstreamStatus> upstreamStatus;

    /**
     * 域名计数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainCount {
        private String domain;
        private Long count;
    }

    /**
     * 上游 DNS 状态
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpstreamStatus {
        private String address;
        private Integer port;
        private Boolean enabled;
        private Long successCount;
        private Long failureCount;
        private Double successRate;
        private Double avgResponseTime;
    }
}