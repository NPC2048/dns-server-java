package com.npc2048.dns.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H2统计实体类
 * 对应 dns_statistics 表
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Entity
@Table(name = "dns_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsEntity {

    /**
     * 统计周期（主键）
     * 格式：'hourly:2025010115', 'daily:20250101'
     */
    @Id
    @Column(name = "period", length = 20)
    private String period;

    /**
     * 查询总数
     */
    @Column(name = "queries")
    private Integer queries;

    /**
     * 缓存命中数
     */
    @Column(name = "cache_hits")
    private Integer cacheHits;

    /**
     * 平均响应时间（毫秒）
     */
    @Column(name = "avg_response_time")
    private Integer avgResponseTime;
}