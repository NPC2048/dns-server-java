package com.npc2048.dns.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.npc2048.dns.model.CacheEntry;

/**
 * DNS缓存服务接口
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
public interface CacheService {

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存的数据，如果不存在或过期返回null
     */
    byte[] get(String key);

    /**
     * 存储缓存
     *
     * @param key  缓存键
     * @param data dns缓存数据
     * @return ttl
     */
    int put(String key, byte[] data);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     */
    void remove(String key);

    /**
     * 清空所有缓存
     */
    void clear();

    /**
     * 获取缓存大小
     *
     * @return 缓存条目数量
     */
    int size();

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计
     */
    CacheStats getStats();

    /**
     * 获取缓存
     */
    Cache<String, CacheEntry> getCache();

    /**
     * 缓存统计信息
     */
    record CacheStats(long hits, long misses, long size) {

        public double getHitRate() {
            long total = hits + misses;
            return total == 0 ? 0.0 : (double) hits / total;
        }
    }
}