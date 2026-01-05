package com.npc2048.dns.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H2查询记录实体类
 * 对应 dns_record 表
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Entity
@Table(name = "dns_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnsRecord {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 域名
     */
    @Column(name = "domain", length = 255)
    private String domain;

    /**
     * 查询类型（A, AAAA, CNAME等）
     */
    @Column(name = "query_type", length = 10)
    private String queryType;

    /**
     * 响应IP地址
     */
    @Column(name = "response_ip", length = 255)
    private String responseIp;

    /**
     * 是否命中缓存
     */
    @Column(name = "cache_hit")
    private Boolean cacheHit;

    /**
     * 查询时间戳（Unix时间戳，毫秒）
     */
    @Column(name = "query_time")
    private Long queryTime;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time_ms")
    private Integer responseTimeMs;
}