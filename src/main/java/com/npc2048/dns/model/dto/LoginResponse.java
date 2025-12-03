package com.npc2048.dns.model.dto;

import lombok.Data;

/**
 * 登录响应DTO
 * 返回token和用户信息
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Data
public class LoginResponse {
    private String token;
    private String username;
    private boolean isAdmin;

    public LoginResponse(String token, String username, boolean isAdmin) {
        this.token = token;
        this.username = username;
        this.isAdmin = isAdmin;
    }
}