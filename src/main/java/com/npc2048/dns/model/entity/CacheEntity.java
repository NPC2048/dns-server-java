package com.npc2048.dns.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H2缓存实体类
 * 对应 dns_cache 表
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Entity
@Table(name = "dns_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntity {

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
     * 响应数据（JSON格式）
     */
    @Column(name = "response_data", columnDefinition = "CLOB")
    private String responseData;

    /**
     * TTL（秒）
     */
    @Column(name = "ttl")
    private Integer ttl;

    /**
     * 过期时间戳（Unix时间戳，秒）
     */
    @Column(name = "expire_time")
    private Long expireTime;

    /**
     * 创建时间戳（Unix时间戳，秒）
     */
    @Column(name = "create_time")
    private Long createTime;

    /**
     * 访问次数
     */
    @Column(name = "access_count")
    private Integer accessCount;

    /**
     * 最后访问时间戳（Unix时间戳，秒）
     */
    @Column(name = "last_access_time")
    private Long lastAccessTime;

    /**
     * 检查是否过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() / 1000 > expireTime;
    }

    /**
     * 转换为内存缓存条目
     */
    public com.npc2048.dns.model.CacheEntry toCacheEntry() {
        return com.npc2048.dns.model.CacheEntry.builder()
                .domain(domain)
                .queryType(queryType)
                .responseData(responseData)
                .ttl(ttl)
                .expireTime(expireTime * 1000L)  // 转换为毫秒
                .createTime(createTime * 1000L)  // 转换为毫秒
                .accessCount(accessCount)
                .lastAccessTime(lastAccessTime != null ? lastAccessTime * 1000L : null)
                .build();
    }

    /**
     * 从内存缓存条目创建实体
     */
    public static CacheEntity fromCacheEntry(com.npc2048.dns.model.CacheEntry entry) {
        return CacheEntity.builder()
                .domain(entry.getDomain())
                .queryType(entry.getQueryType())
                .responseData(entry.getResponseData())
                .ttl(entry.getTtl())
                .expireTime(entry.getExpireTime() / 1000)  // 转换为秒
                .createTime(entry.getCreateTime() / 1000)  // 转换为秒
                .accessCount(entry.getAccessCount())
                .lastAccessTime(entry.getLastAccessTime() != null ? entry.getLastAccessTime() / 1000 : null)
                .build();
    }

    /**
     * 记录一次访问
     */
    public void recordAccess() {
        this.accessCount = (this.accessCount == null ? 0 : this.accessCount) + 1;
        this.lastAccessTime = System.currentTimeMillis() / 1000;
    }
}