package com.npc2048.dns.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * [Sa-Token 权限认证] 全局配置类
 * 保持简单原则：
 * 1. 默认所有路径都不需要鉴权（保持向后兼容）
 * 2. 使用注解在需要的地方声明权限
 * 3. 排除登录和健康检查路径
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Configuration
@Slf4j
public class SaTokenConfigure {

    @Bean
    public SaReactorFilter saReactorFilter() {
        return new SaReactorFilter()
                // 设置拦截路径：所有路径
                .addInclude("/**")
                // 设置排除路径（这些路径不需要鉴权）
                .addExclude("/api/auth/health")
                .addExclude("/api/auth/login")
                // 设置认证函数：sa-token会自动处理注解
                .setAuth(obj -> {
                    // 什么都不做，让注解处理权限
                    // 保持简单：用注解声明权限，而不是复杂的路径匹配
                })
                // 设置异常处理函数
                .setError(e -> {
                    log.error("鉴权异常: {}", e.getMessage(), e);
                    return SaResult.error("鉴权失败: " + e.getMessage());
                });
    }

}
