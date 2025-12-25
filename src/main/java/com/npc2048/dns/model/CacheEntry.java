package com.npc2048.dns.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yuelong.liang
 */
@Data
@AllArgsConstructor
public class CacheEntry {

    /**
     * 响应数据
     */
    private byte[] data;
    /**
     * ttl
     */
    private int ttl;

}
