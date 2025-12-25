package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
