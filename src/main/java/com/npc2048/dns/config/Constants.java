package com.npc2048.dns.config;

/**
 * DNS 服务器配置常量类
 * <p>
 * 集中管理所有配置相关的魔法值，便于维护和修改
 *
 * @author npc2048
 * @since 1.0.0
 */
public final class Constants {

    /**
     * 私有构造函数，防止实例化
     */
    private Constants() {
    }

    // ==================== 网络配置 ====================

    /**
     * HTTP 服务器端口
     */
    public static final int HTTP_SERVER_PORT = 5381;

    /**
     * DNS 服务器监听端口
     */
    public static final int DNS_SERVER_PORT = 5354;

    /**
     * 默认 DNS 端口
     */
    public static final int DEFAULT_DNS_PORT = 53;

    /**
     * UDP 缓冲区大小
     */
    public static final int UDP_BUFFER_SIZE = 512;

    /**
     * 最大端口值
     */
    public static final int MAX_PORT_VALUE = 65535;

    // ==================== 超时配置 ====================

    /**
     * 默认上游 DNS 超时时间（毫秒）
     */
    public static final int DEFAULT_UPSTREAM_TIMEOUT = 5000;

    /**
     * 缓存默认 TTL（秒）
     */
    public static final int CACHE_DEFAULT_TTL = 300;

    /**
     * Sa-Token 超时时间（秒）
     */
    public static final int SA_TOKEN_TIMEOUT = 2592000;

    /**
     * Sa-Token 活动超时（秒，-1 表示禁用）
     */
    public static final int SA_TOKEN_ACTIVE_TIMEOUT = -1;

    // ==================== 缓存配置 ====================

    /**
     * 缓存最大条目数
     */
    public static final int CACHE_MAX_SIZE = 10000;

    /**
     * 缓存最大权重（字符数，约 10MB）
     */
    public static final long CACHE_MAX_WEIGHT = 10485760L;

    /**
     * 缓存容量模式 - 按条目数
     */
    public static final String CACHE_CAPACITY_ENTRIES = "ENTRIES";

    /**
     * 缓存容量模式 - 按权重
     */
    public static final String CACHE_CAPACITY_WEIGHT = "WEIGHT";

    /**
     * 缓存重建策略 - 清空重建
     */
    public static final String CACHE_REBUILD_CLEAR = "CLEAR";

    /**
     * 缓存重建策略 - 后台重建
     */
    public static final String CACHE_REBUILD_BACKGROUND = "BACKGROUND";

    // ==================== 重试和连接配置 ====================

    /**
     * 重试次数
     */
    public static final int RETRY_COUNT = 3;

    // ==================== 上游 DNS 配置 ====================

    /**
     * Google DNS 服务器
     */
    public static final String GOOGLE_DNS = "8.8.8.8";

    /**
     * Google DNS 备用服务器
     */
    public static final String GOOGLE_DNS_BACKUP = "8.8.4.4";

    /**
     * 114 DNS 服务器
     */
    public static final String DNS_114 = "114.114.114.114";

    /**
     * 默认上游 DNS 优先级
     */
    public static final int DEFAULT_UPSTREAM_PRIORITY = 1;

    /**
     * 启用状态
     */
    public static final boolean ENABLED = true;

    /**
     * 禁用状态
     */
    public static final boolean DISABLED = false;

    // ==================== 日志和统计配置 ====================

    /**
     * 启用缓存
     */
    public static final boolean CACHE_ENABLED = true;

    /**
     * 启用查询日志
     */
    public static final boolean QUERY_LOG_ENABLED = true;

    /**
     * DNS 查询不需要鉴权
     */
    public static final boolean DNS_QUERY_NO_AUTH = false;

    // ==================== 数据库和初始化配置 ====================

    /**
     * JDBC fetch size
     */
    public static final int JDBC_FETCH_SIZE = 100;

    /**
     * H2 数据库 busy timeout
     */
    public static final int H2_BUSY_TIMEOUT = 5000;

    /**
     * 数据库字段长度
     */
    public static final int DB_FIELD_LENGTH = 100;

    // ==================== 其他魔法值 ====================

    /**
     * DNS 请求 ID
     */
    public static final int DNS_REQUEST_ID = 12345;

    // ==================== 配置键名 ====================

    /**
     * 上游 DNS 配置键
     */
    public static final String CONFIG_KEY_UPSTREAM_DNS = "upstreamDns";

    /**
     * 默认超时配置键
     */
    public static final String CONFIG_KEY_DEFAULT_TIMEOUT = "defaultTimeout";

    /**
     * 重试次数配置键
     */
    public static final String CONFIG_KEY_RETRY_COUNT = "retryCount";

    /**
     * 缓存最大大小配置键
     */
    public static final String CONFIG_KEY_CACHE_MAX_SIZE = "cacheMaxSize";

    /**
     * 缓存最大权重配置键
     */
    public static final String CONFIG_KEY_CACHE_MAX_WEIGHT = "cacheMaxWeight";

    /**
     * 缓存容量模式配置键
     */
    public static final String CONFIG_KEY_CACHE_CAPACITY_MODE = "cacheCapacityMode";

    /**
     * 缓存重建策略配置键
     */
    public static final String CONFIG_KEY_CACHE_REBUILD_STRATEGY = "cacheRebuildStrategy";

    /**
     * 缓存默认 TTL 配置键
     */
    public static final String CONFIG_KEY_CACHE_DEFAULT_TTL = "cacheDefaultTtl";

    /**
     * 监听端口配置键
     */
    public static final String CONFIG_KEY_LISTEN_PORT = "listenPort";

    /**
     * 缓存启用配置键
     */
    public static final String CONFIG_KEY_CACHE_ENABLED = "cacheEnabled";

    /**
     * 查询日志启用配置键
     */
    public static final String CONFIG_KEY_QUERY_LOG_ENABLED = "queryLogEnabled";

    /**
     * 域名配置键
     */
    public static final String CONFIG_KEY_DOMAIN = "domain";

    /**
     * 代理配置键
     */
    public static final String CONFIG_KEY_PROXY_CONFIG = "proxyConfig";

    /**
     * 地址配置键
     */
    public static final String CONFIG_KEY_ADDRESS = "address";

    /**
     * 端口配置键
     */
    public static final String CONFIG_KEY_PORT = "port";

    /**
     * 超时配置键
     */
    public static final String CONFIG_KEY_TIMEOUT = "timeout";

    /**
     * 使用代理配置键
     */
    public static final String CONFIG_KEY_USE_PROXY = "useProxy";

    /**
     * 用户名配置键
     */
    public static final String CONFIG_KEY_USERNAME = "username";

    /**
     * 密码配置键
     */
    public static final String CONFIG_KEY_PASSWORD = "password";

    /**
     * 类型配置键
     */
    public static final String CONFIG_KEY_TYPE = "type";

    /**
     * 主机配置键
     */
    public static final String CONFIG_KEY_HOST = "host";

    /**
     * 启用配置键
     */
    public static final String CONFIG_KEY_ENABLED = "enabled";

    /**
     * 优先级配置键
     */
    public static final String CONFIG_KEY_PRIORITY = "priority";
}