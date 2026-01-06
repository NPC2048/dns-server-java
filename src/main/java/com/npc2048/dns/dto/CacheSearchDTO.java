package com.npc2048.dns.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缓存搜索条件
 *
 * @author Claude Code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheSearchDTO {
    /**
     * 最小TTL（秒）
     */
    private Integer minTtl;

    /**
     * 最大TTL（秒）
     */
    private Integer maxTtl;

    /**
     * 搜索关键词
     */
    private String keyword;
}