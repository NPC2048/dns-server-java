package com.npc2048.dns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上游 DNS 服务器配置
 * 包含代理配置的最小方案
 *
 * @author yuelong.liang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstreamDnsConfig {

    /**
     * DNS 服务器地址，如 "8.8.8.8"
     */
    private String address;

    /**
     * DNS 服务器端口，默认 53
     */
    private Integer port;

    /**
     * 查询超时时间（毫秒）
     */
    private Integer timeout;

    /**
     * 是否启用代理
     */
    private Boolean useProxy;

    /**
     * 代理配置（当 useProxy 为 true 时使用）
     */
    private ProxyConfig proxyConfig;

    /**
     * 是否启用该上游 DNS
     */
    private Boolean enabled;

    /**
     * 优先级（数值越小优先级越高）
     */
    private Integer priority;

    /**
     * 代理配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProxyConfig {
        /**
         * 代理服务器地址
         */
        private String host;

        /**
         * 代理服务器端口
         */
        private Integer port;

        /**
         * 代理类型：HTTP, SOCKS5
         */
        private String type;

        /**
         * 用户名（可选）
         */
        private String username;

        /**
         * 密码（可选）
         */
        private String password;
    }
}