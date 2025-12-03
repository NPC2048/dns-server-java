package com.npc2048.dns.model.dto;

import lombok.Data;

/**
 * 鉴权配置更新请求
 * 就这么两个字段，不要搞复杂
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Data
public class AuthConfigUpdateRequest {
    /**
     * DNS查询是否需要鉴权
     */
    private Boolean requireForDnsQueries;

    /**
     * 管理员用户列表
     */
    private java.util.List<String> adminUsers;
}