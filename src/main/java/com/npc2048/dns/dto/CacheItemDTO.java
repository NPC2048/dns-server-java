package com.npc2048.dns.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存项DTO
 *
 * @author Claude Code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheItemDTO {
    /**
     * 缓存键（域名）
     */
    private String key;

    /**
     * 缓存数据
     */
    private byte[] data;

    /**
     * TTL值（秒）
     */
    private int ttl;

    /**
     * 创建时间戳
     */
    private long createdTime;

    /**
     * 过期时间戳
     */
    private long expireTime;
}