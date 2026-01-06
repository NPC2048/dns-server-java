package com.npc2048.dns.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 缓存分页查询结果
 *
 * @author Claude Code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CachePageDTO {
    /**
     * 缓存项列表
     */
    private List<CacheItemDTO> items;

    /**
     * 总数量
     */
    private long total;

    /**
     * 当前页码
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;
}