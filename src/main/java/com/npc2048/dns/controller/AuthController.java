package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.model.dto.LoginRequest;
import com.npc2048.dns.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

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
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public SaResult login(HttpServletRequest request, @RequestBody LoginRequest loginRequest) {
        try {
            return SaResult.data(authService.login(request, loginRequest));
        } catch (Exception e) {
            log.error("登录失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @SaCheckLogin
    public SaResult logout() {
        try {
            authService.logout();
            return SaResult.ok("登出成功");
        } catch (Exception e) {
            log.error("登出失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * GET /api/auth/current
     */
    @GetMapping("/current")
    @SaCheckLogin
    public SaResult getCurrentUser() {
        try {
            return SaResult.data(authService.getCurrentUser());
        } catch (Exception e) {
            log.error("获取当前用户失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 健康检查（无需鉴权）
     * GET /api/auth/health
     */
    @GetMapping("/health")
    public SaResult health() {
        return SaResult.ok("认证服务正常");
    }
}