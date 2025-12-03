package com.npc2048.dns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * DNS查询记录实体
 * 使用Spring Data R2DBC的@Table注解（非JPA的@Entity）
 *
 * @author yuelong.liang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("dns_query_records")
public class DnsQueryRecord {

    /**
     * 主键ID，自增
     */
    @Id
    private Long id;

    /**
     * 查询的域名
     */
    private String domain;

    /**
     * 查询类型（A, AAAA, CNAME等）
     */
    private String queryType;

    /**
     * 响应IP地址
     */
    private String responseIp;

    /**
     * 是否命中缓存
     */
    private Boolean cacheHit;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 响应时间（毫秒）
     */
    private Integer responseTimeMs;
}
