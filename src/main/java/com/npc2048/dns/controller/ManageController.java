package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.model.dto.AuthConfigUpdateRequest;
import com.npc2048.dns.service.ManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 管理控制器
 * 只有管理员才能访问
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@RestController
@RequestMapping("/api/manage")
@RequiredArgsConstructor
@Slf4j
@SaCheckLogin
public class ManageController {

    private final ManageService manageService;

    /**
     * 获取鉴权配置
     * GET /api/manage/auth-config
     */
    @GetMapping("/auth-config")
    public Mono<SaResult> getAuthConfig() {
        return manageService.getAuthConfig()
                .map(config -> SaResult.data(config))
                .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 更新鉴权配置
     * PUT /api/manage/auth-config
     */
    @PutMapping("/auth-config")
    public Mono<SaResult> updateAuthConfig(@RequestBody AuthConfigUpdateRequest request) {
        return manageService.updateAuthConfig(request)
                .thenReturn(SaResult.ok("配置更新成功"))
                .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 健康检查（需要管理员权限）
     * GET /api/manage/health
     */
    @GetMapping("/health")
    public Mono<SaResult> health() {
        return Mono.just(SaResult.ok("管理服务正常"));
    }
}