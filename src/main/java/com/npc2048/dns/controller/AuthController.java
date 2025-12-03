package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.model.dto.LoginRequest;
import com.npc2048.dns.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 认证控制器
 * 就这么几个API，不要搞复杂
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Mono<SaResult> login(@RequestBody LoginRequest request) {
        return authService.login(request)
                .map(SaResult::data)
                .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @SaCheckLogin
    public Mono<SaResult> logout() {
        return authService.logout()
                .thenReturn(SaResult.ok("登出成功"))
                .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/current
     */
    @GetMapping("/current")
    @SaCheckLogin
    public Mono<SaResult> getCurrentUser() {
        return authService.getCurrentUser()
                .map(SaResult::data)
                .onErrorResume(e -> Mono.just(SaResult.error(e.getMessage())));
    }

    /**
     * 健康检查（无需鉴权）
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public Mono<SaResult> health() {
        return Mono.just(SaResult.ok("认证服务正常"));
    }
}