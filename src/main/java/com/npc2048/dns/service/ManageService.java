package com.npc2048.dns.service;

import cn.dev33.satoken.stp.StpUtil;
import com.npc2048.dns.config.AuthConfig;
import com.npc2048.dns.model.dto.AuthConfigUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 管理服务
 * 保持简单原则：
 * 1. 只有管理员才能修改配置
 * 2. 配置存储在内存中（第一版够用了）
 * 3. 后续可以加数据库持久化
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ManageService {

    private final AuthConfig authConfig;

    /**
     * 获取当前鉴权配置
     * 只有管理员才能查看
     */
    public Map<String, Object> getAuthConfig() {
        checkAdmin();

        return Map.of(
                "requireForDnsQueries", authConfig.isRequireForDnsQueries(),
                "adminUsers", authConfig.getAdminUsers()
        );
    }

    /**
     * 更新鉴权配置
     * 只有管理员才能修改
     */
    public void updateAuthConfig(AuthConfigUpdateRequest request) {
        checkAdmin();

        // 更新 DNS 查询鉴权配置
        if (request.getRequireForDnsQueries() != null) {
            boolean oldValue = authConfig.isRequireForDnsQueries();
            boolean newValue = request.getRequireForDnsQueries();
            authConfig.setRequireForDnsQueries(newValue);
            log.info("DNS查询鉴权配置已更新: {} -> {}", oldValue, newValue);
        }

        // 更新管理员用户列表
        if (request.getAdminUsers() != null && !request.getAdminUsers().isEmpty()) {
            authConfig.setAdminUsers(request.getAdminUsers());
            log.info("管理员用户列表已更新: {}", request.getAdminUsers());
        }
    }

    /**
     * 检查当前用户是否是管理员
     * 就这么简单，不要搞复杂
     */
    private void checkAdmin() {
        if (!StpUtil.isLogin()) {
            throw new RuntimeException("请先登录");
        }

        String username = StpUtil.getLoginIdAsString();
        if (!authConfig.isAdmin(username)) {
            throw new RuntimeException("需要管理员权限");
        }
    }
}