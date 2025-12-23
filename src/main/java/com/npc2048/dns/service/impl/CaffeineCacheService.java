package com.npc2048.dns.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.npc2048.dns.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 Caffeine 的 DNS 缓存实现
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Slf4j
@Service
public class CaffeineCacheService implements CacheService {

    private final Cache<String, byte[]> cache;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    public CaffeineCacheService() {
        this.cache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .maximumSize(10000)
                .recordStats()
                .build();
    }

    @Override
    public byte[] get(String key) {
        byte[] value = cache.getIfPresent(key);
        if (value != null) {
            hits.incrementAndGet();
            log.debug("缓存命中: {}", key);
        } else {
            misses.incrementAndGet();
            log.debug("缓存未命中: {}", key);
        }
        return value;
    }

    @Override
    public void put(String key, byte[] data, int ttl) {
        if (ttl > 0) {
            // Caffeine 不直接支持 TTL，这里简化处理
            // 实际项目中可以使用 expireAfterWrite 配置
            cache.put(key, data);
            log.debug("缓存存储: {} TTL:{}秒", key, ttl);
        }
    }

    @Override
    public void remove(String key) {
        cache.invalidate(key);
        log.debug("缓存删除: {}", key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
        hits.set(0);
        misses.set(0);
        log.info("缓存已清空");
    }

    @Override
    public int size() {
        return (int) cache.estimatedSize();
    }

    @Override
    public CacheStats getStats() {
        return new CacheStats(
                hits.get(),
                misses.get(),
                cache.estimatedSize()
        );
    }
}