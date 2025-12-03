package com.npc2048.dns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DNS 缓存条目
 *
 * @author yuelong.liang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntry {

    /**
     * 域名
     */
    private String domain;

    /**
     * 查询类型（A, AAAA, CNAME 等）
     */
    private String queryType;

    /**
     * 响应数据（JSON 格式，包含所有记录）
     */
    private String responseData;

    /**
     * 过期时间戳（毫秒）
     */
    private Long expireTime;

    /**
     * TTL（秒）
     */
    private Integer ttl;

    /**
     * 创建时间戳（毫秒）
     */
    private Long createTime;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * 最后访问时间戳（毫秒）
     */
    private Long lastAccessTime;

    /**
     * 检查是否过期
     *
     * @return true 如果已过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    /**
     * 创建新的缓存条目
     *
     * @param domain       域名
     * @param queryType    查询类型
     * @param responseData 响应数据
     * @param ttl          TTL（秒）
     * @return 缓存条目
     */
    public static CacheEntry create(String domain, String queryType, String responseData, int ttl) {
        long now = System.currentTimeMillis();
        return CacheEntry.builder()
                .domain(domain)
                .queryType(queryType)
                .responseData(responseData)
                .ttl(ttl)
                .expireTime(now + ttl * 1000L)
                .createTime(now)
                .accessCount(0)
                .lastAccessTime(now)
                .build();
    }

    /**
     * 记录一次访问
     */
    public void recordAccess() {
        this.accessCount++;
        this.lastAccessTime = System.currentTimeMillis();
    }
}