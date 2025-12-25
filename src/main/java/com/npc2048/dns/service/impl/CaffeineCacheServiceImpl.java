package com.npc2048.dns.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.npc2048.dns.common.util.DnsUtils;
import com.npc2048.dns.config.Constants;
import com.npc2048.dns.model.CacheEntry;
import com.npc2048.dns.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 Caffeine 的 DNS 缓存实现
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Slf4j
@Service
public class CaffeineCacheServiceImpl implements CacheService {

    private final Cache<String, CacheEntry> cache;
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    public CaffeineCacheServiceImpl() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(Constants.CACHE_MAX_SIZE)
                .recordStats()
                .expireAfter(new Expiry<String, CacheEntry>() {
                    @Override
                    public long expireAfterCreate(String key, CacheEntry value, long currentTime) {
                        // 将秒转为纳秒 (Caffeine 内部计时单位是纳秒)
                        // 建议设置一个兜底逻辑，比如 value.getTtl() 为空时默认 60s
                        return TimeUnit.SECONDS.toNanos(value.getTtl());
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheEntry value, long currentTime, @NonNegative long currentDuration) {
                        // 更新时如果不改变过期时间，返回 currentDuration
                        return TimeUnit.SECONDS.toNanos(value.getTtl());
                    }

                    @Override
                    public long expireAfterRead(String key, CacheEntry value, long currentTime, @NonNegative long currentDuration) {
                        // 读取时不延长寿命
                        return currentDuration;
                    }
                })
                .build();
    }

    @Override
    public byte[] get(String key) {
        CacheEntry value = cache.getIfPresent(key);
        if (value != null) {
            hits.incrementAndGet();
            log.debug("缓存命中: {}", key);
            return value.getData();
        } else {
            misses.incrementAndGet();
            log.debug("缓存未命中: {}", key);
        }
        return null;
    }

    @Override
    public int put(String key, byte[] data) {
        int ttl = DnsUtils.extractTtl(data);
        if (ttl > 0) {
            cache.put(key, new CacheEntry(data, ttl));
        }
        return ttl;
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

    @Override
    public Cache<String, CacheEntry> getCache() {
        return cache;
    }

}