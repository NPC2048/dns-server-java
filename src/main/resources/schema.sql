-- DNS缓存表（核心性能表）
CREATE TABLE IF NOT EXISTS dns_cache (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    domain TEXT NOT NULL,
    query_type TEXT NOT NULL,  -- 'A', 'AAAA', 'CNAME'
    response_data TEXT NOT NULL,  -- JSON格式
    ttl INTEGER NOT NULL,  -- 秒
    expire_time INTEGER NOT NULL,  -- Unix时间戳（秒）
    create_time INTEGER NOT NULL,
    access_count INTEGER DEFAULT 0,
    last_access_time INTEGER,
    -- 复合唯一约束
    UNIQUE(domain, query_type)
);

-- 缓存表索引
CREATE INDEX IF NOT EXISTS idx_cache_expire ON dns_cache(expire_time);
CREATE INDEX IF NOT EXISTS idx_cache_domain ON dns_cache(domain);

-- DNS查询记录表（历史数据）
CREATE TABLE IF NOT EXISTS dns_queries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    domain TEXT NOT NULL,
    query_type TEXT NOT NULL,
    response_ip TEXT,
    cache_hit BOOLEAN NOT NULL,
    query_time INTEGER NOT NULL,  -- Unix时间戳（毫秒）
    response_time_ms INTEGER
);

-- 查询记录表索引
CREATE INDEX IF NOT EXISTS idx_queries_time ON dns_queries(query_time);
CREATE INDEX IF NOT EXISTS idx_queries_domain ON dns_queries(domain);

-- DNS配置表（热配置）
CREATE TABLE IF NOT EXISTS dns_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_time INTEGER NOT NULL
);

-- 初始配置数据
INSERT OR IGNORE INTO dns_config (key, value, updated_time) VALUES
    ('upstream_dns', '[{"address":"8.8.8.8","port":53,"timeout":5000,"useProxy":false,"enabled":true,"priority":1},{"address":"8.8.4.4","port":53,"timeout":5000,"useProxy":false,"enabled":true,"priority":2},{"address":"114.114.114.114","port":53,"timeout":5000,"useProxy":false,"enabled":true,"priority":3}]', strftime('%s', 'now')),
    ('cache_size', '10000', strftime('%s', 'now')),
    ('cache_max_weight', '10485760', strftime('%s', 'now')),
    ('cache_capacity_mode', 'ENTRIES', strftime('%s', 'now')),
    ('cache_rebuild_strategy', 'CLEAR', strftime('%s', 'now')),
    ('cache_ttl_default', '300', strftime('%s', 'now')),
    ('listen_port', '5354', strftime('%s', 'now')),
    ('default_timeout', '5000', strftime('%s', 'now')),
    ('retry_count', '3', strftime('%s', 'now')),
    ('cache_enabled', 'true', strftime('%s', 'now')),
    ('query_log_enabled', 'true', strftime('%s', 'now'));

-- DNS统计表（聚合数据）
CREATE TABLE IF NOT EXISTS dns_statistics (
    period TEXT PRIMARY KEY,  -- 'hourly:2025010115', 'daily:20250101'
    queries INTEGER DEFAULT 0,
    cache_hits INTEGER DEFAULT 0,
    avg_response_time INTEGER DEFAULT 0
);

-- 启用WAL模式（Write-Ahead Logging）提升并发性能
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;
PRAGMA busy_timeout = 5000;