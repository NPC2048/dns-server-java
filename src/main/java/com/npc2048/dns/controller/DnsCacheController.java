package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.service.CacheService;
import com.npc2048.dns.service.CacheService.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * DNS 缓存控制器
 *
 * @author yuelong.liang
 */
@Slf4j
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@SaCheckLogin
public class DnsCacheController {

    private final CacheService cacheService;

    @GetMapping("/detail")
    public SaResult detail() {
        CacheService.CacheStats stats = cacheService.getStats();
        return SaResult.data(stats);
    }

    /**
     * 获取所有缓存键
     */
    @GetMapping("/keys")
    public SaResult getAllKeys() {
        Set<String> keys = cacheService.getCache().asMap().keySet();
        return SaResult.data(keys);
    }

    /**
     * 获取特定缓存项详情
     */
    @GetMapping("/item/{key}")
    public SaResult getCacheItem(@PathVariable String key) {
        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();
        com.npc2048.dns.model.CacheEntry entry = cache.getIfPresent(key);
        if (entry != null) {
            Map<String, Object> item = new HashMap<>();
            item.put("key", key);
            item.put("data", entry.getData());
            item.put("ttl", entry.getTtl());
            item.put("createdTime", System.currentTimeMillis());
            item.put("expireTime", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(entry.getTtl()));
            return SaResult.data(item);
        }
        return SaResult.error("Cache item not found");
    }

    /**
     * 分页查询缓存
     */
    @GetMapping("/page")
    public SaResult getCachePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 1 || size < 1 || size > 100) {
            return SaResult.error("Invalid pagination parameters");
        }

        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();
        Map<String, com.npc2048.dns.model.CacheEntry> cacheMap = cache.asMap();
        List<Map<String, Object>> items = new ArrayList<>();

        cacheMap.entrySet().stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .forEach(entry -> {
                    com.npc2048.dns.model.CacheEntry value = entry.getValue();
                    Map<String, Object> item = new HashMap<>();
                    item.put("key", entry.getKey());
                    item.put("data", value.getData());
                    item.put("ttl", value.getTtl());
                    item.put("createdTime", System.currentTimeMillis());
                    item.put("expireTime", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(value.getTtl()));
                    items.add(item);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", cacheMap.size());
        result.put("page", page);
        result.put("size", size);

        return SaResult.data(result);
    }

    /**
     * 搜索缓存
     */
    @GetMapping("/search")
    public SaResult searchCache(
            @RequestParam(required = false) Integer minTtl,
            @RequestParam(required = false) Integer maxTtl,
            @RequestParam(required = false) String keyword) {

        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();
        Map<String, com.npc2048.dns.model.CacheEntry> cacheMap = cache.asMap();
        List<Map<String, Object>> results = new ArrayList<>();

        cacheMap.forEach((key, value) -> {
            // TTL过滤
            if (minTtl != null && value.getTtl() < minTtl) return;
            if (maxTtl != null && value.getTtl() > maxTtl) return;

            // 关键词过滤
            if (keyword != null && !key.contains(keyword)) return;

            Map<String, Object> item = new HashMap<>();
            item.put("key", key);
            item.put("data", value.getData());
            item.put("ttl", value.getTtl());
            item.put("createdTime", System.currentTimeMillis());
            item.put("expireTime", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(value.getTtl()));
            results.add(item);
        });

        return SaResult.data(results);
    }

    /**
     * 按TTL范围查询缓存
     */
    @GetMapping("/ttl-range")
    public SaResult findByTtlRange(
            @RequestParam int minTtl,
            @RequestParam int maxTtl) {

        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();
        Map<String, com.npc2048.dns.model.CacheEntry> cacheMap = cache.asMap();
        List<Map<String, Object>> results = new ArrayList<>();

        cacheMap.forEach((key, value) -> {
            if (value.getTtl() >= minTtl && value.getTtl() <= maxTtl) {
                Map<String, Object> item = new HashMap<>();
                item.put("key", key);
                item.put("data", value.getData());
                item.put("ttl", value.getTtl());
                item.put("createdTime", System.currentTimeMillis());
                item.put("expireTime", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(value.getTtl()));
                results.add(item);
            }
        });

        return SaResult.data(results);
    }

    /**
     * 获取缓存详细信息
     */
    @GetMapping("/details")
    public SaResult getCacheDetails() {
        CacheStats stats = cacheService.getStats();
        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();

        Map<String, Object> details = new HashMap<>();
        details.put("stats", Map.of(
                "hits", stats.hits(),
                "misses", stats.misses(),
                "size", stats.size(),
                "hitRate", stats.getHitRate()
        ));
        details.put("cacheSize", cacheService.size());
        details.put("evictionCount", cache.stats().evictionCount());
        details.put("loadSuccessCount", cache.stats().loadSuccessCount());
        details.put("loadFailureCount", cache.stats().loadFailureCount());

        return SaResult.data(details);
    }

    /**
     * 检查缓存是否存在
     */
    @GetMapping("/contains/{key}")
    public SaResult containsKey(@PathVariable String key) {
        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();
        boolean contains = cache.getIfPresent(key) != null;
        return SaResult.data(Map.of("contains", contains));
    }

    /**
     * 批量获取缓存项
     */
    @PostMapping("/batch-get")
    public SaResult batchGet(@RequestBody List<String> keys) {
        com.github.benmanes.caffeine.cache.Cache<String, com.npc2048.dns.model.CacheEntry> cache = cacheService.getCache();
        List<Map<String, Object>> results = new ArrayList<>();

        keys.forEach(key -> {
            com.npc2048.dns.model.CacheEntry entry = cache.getIfPresent(key);
            if (entry != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("key", key);
                item.put("data", entry.getData());
                item.put("ttl", entry.getTtl());
                item.put("createdTime", System.currentTimeMillis());
                item.put("expireTime", System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(entry.getTtl()));
                results.add(item);
            }
        });

        return SaResult.data(results);
    }

}
