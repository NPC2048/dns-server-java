package com.npc2048.dns.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DNS鉴权配置
 *
 * 保持简单原则：
 * 1. requireForDnsQueries: DNS查询是否需要鉴权（默认false，保持向后兼容）
 * 2. adminUsers: 管理员用户列表（硬编码，不要搞复杂）
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Data
@Component
@ConfigurationProperties(prefix = "dns.auth")
public class AuthConfig {

    /**
     * DNS查询是否需要鉴权
     * 默认false：不破坏现有系统
     * 只有显式配置为true时才启用
     */
    private boolean requireForDnsQueries = false;

    /**
     * 管理员用户列表
     * 硬编码几个用户，不要搞数据库
     * 第一版够用了
     */
    private List<String> adminUsers = List.of("admin");

    /**
     * 检查用户是否是管理员
     * 就这么简单，不要搞复杂的权限系统
     */
    public boolean isAdmin(String username) {
        return adminUsers.contains(username);
    }
}