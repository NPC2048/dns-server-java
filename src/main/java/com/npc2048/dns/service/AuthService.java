package com.npc2048.dns.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.npc2048.dns.config.AuthConfig;
import com.npc2048.dns.model.dto.LoginRequest;
import com.npc2048.dns.model.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务
 * 保持简单原则：
 * 1. 硬编码用户，不要搞数据库
 * 2. 简单的用户名密码验证
 * 3. 使用sa-token管理会话
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AuthConfig authConfig;

    /**
     * 硬编码用户，第一版够用了
     * 格式：username -> password
     */
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        // 默认管理员用户
        USERS.put("admin", "admin123");
        // 默认普通用户
        USERS.put("user", "user123");
    }

    /**
     * 用户登录
     * 就这么简单，不要搞复杂
     */
    public LoginResponse login(HttpServletRequest request, LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 验证用户名密码
        if (!USERS.containsKey(username) || !USERS.get(username).equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 使用sa-token登录
        StpUtil.login(username);

        // 获取token
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("tokenInfo: {}", tokenInfo);

        // 检查是否是管理员
        boolean isAdmin = authConfig.isAdmin(username);

        log.info("用户登录成功: {}, 管理员: {}", username, isAdmin);
        return new LoginResponse(tokenInfo.getTokenValue(), username, isAdmin);
    }

    /**
     * 用户登出
     */
    public void logout() {
        StpUtil.logout();
        log.info("用户登出");
    }

    /**
     * 获取当前用户信息
     */
    public LoginResponse getCurrentUser() {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("用户未登录");
        }

        String username = StpUtil.getLoginIdAsString();
        String token = StpUtil.getTokenValue();
        boolean isAdmin = authConfig.isAdmin(username);

        return new LoginResponse(token, username, isAdmin);
    }
}