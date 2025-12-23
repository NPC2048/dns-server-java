-- H2数据库表结构初始化脚本
-- 用于DNS服务器应用

-- DNS查询记录表
CREATE TABLE IF NOT EXISTS dns_queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    domain VARCHAR(255) NOT NULL,
    query_type VARCHAR(10) NOT NULL,
    response_ip VARCHAR(45),
    cache_hit BOOLEAN NOT NULL,
    query_time BIGINT NOT NULL,
    response_time_ms INTEGER
);

-- DNS配置表
CREATE TABLE IF NOT EXISTS dns_config (
    `key` VARCHAR(255) PRIMARY KEY,
    `value` TEXT NOT NULL,
    updated_time BIGINT NOT NULL
);

-- DNS缓存表
CREATE TABLE IF NOT EXISTS dns_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    domain VARCHAR(255) NOT NULL,
    query_type VARCHAR(10) NOT NULL,
    response_data TEXT NOT NULL,
    ttl INTEGER NOT NULL,
    expire_time BIGINT NOT NULL,
    create_time BIGINT NOT NULL,
    access_count INTEGER NOT NULL DEFAULT 0,
    last_access_time BIGINT
);

-- DNS统计表
CREATE TABLE IF NOT EXISTS dns_statistics (
    period VARCHAR(50) PRIMARY KEY,
    queries INTEGER NOT NULL DEFAULT 0,
    cache_hits INTEGER NOT NULL DEFAULT 0,
    avg_response_time INTEGER NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_dns_queries_domain ON dns_queries(domain);
CREATE INDEX IF NOT EXISTS idx_dns_queries_query_time ON dns_queries(query_time);
CREATE INDEX IF NOT EXISTS idx_dns_cache_domain ON dns_cache(domain);
CREATE INDEX IF NOT EXISTS idx_dns_cache_expire_time ON dns_cache(expire_time);