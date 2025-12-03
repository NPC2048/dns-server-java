package com.npc2048.dns.model.dto;

import lombok.Data;

/**
 * 登录请求DTO
 * 就这么简单，不要搞复杂
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}