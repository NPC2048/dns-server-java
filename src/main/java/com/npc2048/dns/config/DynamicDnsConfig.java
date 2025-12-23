//package com.npc2048.dns.config;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.npc2048.dns.config.DnsConfig;
//import com.npc2048.dns.model.UpstreamDnsConfig;
//import com.npc2048.dns.service.ConfigService;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import reactor.core.publisher.Mono;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * 动态DNS配置类（从数据库加载）
// * 支持运行时修改配置并立即生效
// *
// * @author yuelong.liang
// */
//@Slf4j
//@Component
//public class DynamicDnsConfig extends DnsConfig {
//
//    private static final long CONFIG_CACHE_TTL = 300_000; // 5分钟缓存
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    private final ConfigService configService;
//
//    /**
//     * 不可变配置快照 - 使用原子引用确保线程安全
//     */
//    private final AtomicReference<DnsConfigSnapshot> configSnapshot = new AtomicReference<>();
//
//    /**
//     * 配置快照类 - 不可变对象
//     */
//    public record DnsConfigSnapshot(
//            int cacheMaxSize,
//            long cacheMaxWeight,
//            CacheCapacityMode cacheCapacityMode,
//            CacheRebuildStrategy cacheRebuildStrategy,
//            int cacheDefaultTtl,
//            int listenPort,
//            boolean cacheEnabled,
//            boolean queryLogEnabled,
//            List<UpstreamDnsConfig> upstreamDns,
//            int defaultTimeout,
//            int retryCount,
//            long loadTime
//    ) {
//        public boolean isExpired() {
//            return System.currentTimeMillis() - loadTime > CONFIG_CACHE_TTL;
//        }
//    }
//
//    public DynamicDnsConfig(ConfigService configService) {
//        this.configService = configService;
//    }
//
//    @PostConstruct
//    private void init() {
//        log.debug("初始化动态DNS配置");
//        // 初始化必须阻塞等待，确保首次访问前配置已加载
//        loadConfigFromDatabase().block();
//    }
//
//    private DnsConfigSnapshot getValidSnapshot() {
//        DnsConfigSnapshot snapshot = configSnapshot.get();
//        if (snapshot == null || snapshot.isExpired()) {
//            // 同步刷新确保一致性
//            loadConfigFromDatabase().block();
//            snapshot = configSnapshot.get();
//        }
//        return snapshot;
//    }
//
//    @Override
//    public synchronized String getUpstreamDns() {
//        try {
//            return objectMapper.writeValueAsString(getValidSnapshot().upstreamDns());
//        } catch (JsonProcessingException e) {
//            log.error("序列化上游DNS配置失败", e);
//            return "[]";
//        }
//    }
//
//    @Override
//    public synchronized void setUpstreamDns(String json) {
//        configService.saveConfig("upstreamDns", json)
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//        log.info("上游DNS配置已更新: {}", json);
//    }
//
//    @Override
//    public int getDefaultTimeout() {
//        return getValidSnapshot().defaultTimeout();
//    }
//
//    @Override
//    public int getRetryCount() {
//        return getValidSnapshot().retryCount();
//    }
//
//    @Override
//    public long getCacheMaxSize() {
//        return getValidSnapshot().cacheMaxSize();
//    }
//
//    @Override
//    public long getCacheMaxWeight() {
//        return getValidSnapshot().cacheMaxWeight();
//    }
//
//    @Override
//    public CacheCapacityMode getCacheCapacityMode() {
//        return getValidSnapshot().cacheCapacityMode();
//    }
//
//    @Override
//    public CacheRebuildStrategy getCacheRebuildStrategy() {
//        return getValidSnapshot().cacheRebuildStrategy();
//    }
//
//    @Override
//    public int getCacheDefaultTtl() {
//        return getValidSnapshot().cacheDefaultTtl();
//    }
//
//    @Override
//    public int getListenPort() {
//        return getValidSnapshot().listenPort();
//    }
//
//    @Override
//    public boolean isCacheEnabled() {
//        return getValidSnapshot().cacheEnabled();
//    }
//
//    @Override
//    public boolean isQueryLogEnabled() {
//        return getValidSnapshot().queryLogEnabled();
//    }
//
//    @Override
//    public synchronized void setDefaultTimeout(int timeout) {
//        configService.saveConfig("defaultTimeout", String.valueOf(timeout))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setRetryCount(int retryCount) {
//        configService.saveConfig("retryCount", String.valueOf(retryCount))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setCacheMaxSize(int maxSize) {
//        configService.saveConfig("cacheMaxSize", String.valueOf(maxSize))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setCacheMaxWeight(long maxWeight) {
//        configService.saveConfig("cacheMaxWeight", String.valueOf(maxWeight))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setCacheCapacityMode(CacheCapacityMode mode) {
//        configService.saveConfig("cacheCapacityMode", mode.toString())
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setCacheRebuildStrategy(CacheRebuildStrategy strategy) {
//        configService.saveConfig("cacheRebuildStrategy", strategy.toString())
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setCacheDefaultTtl(int ttl) {
//        configService.saveConfig("cacheDefaultTtl", String.valueOf(ttl))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setListenPort(int port) {
//        configService.saveConfig("listenPort", String.valueOf(port))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setCacheEnabled(boolean enabled) {
//        configService.saveConfig("cacheEnabled", String.valueOf(enabled))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    @Override
//    public synchronized void setQueryLogEnabled(boolean enabled) {
//        configService.saveConfig("queryLogEnabled", String.valueOf(enabled))
//            .then(Mono.defer(this::loadConfigFromDatabase))
//            .subscribe();
//    }
//
//    /**
//     * 刷新配置（强制从数据库重新加载）
//     */
//    @Override
//    public synchronized void refreshConfig() {
//        loadConfigFromDatabase().block();
//        log.info("DNS配置已刷新");
//    }
//
//    /**
//     * 从数据库加载配置 - 整体构建不可变快照
//     */
//    private Mono<Void> loadConfigFromDatabase() {
//        log.debug("从数据库加载配置...");
//
//        // 加载upstreamDns配置
//        Mono<List<UpstreamDnsConfig>> upstreamDnsMono = getConfigValue("upstreamDns")
//            .map(this::parseUpstreamDnsConfig);
//
//        // 加载所有其他配置
//        Mono<Integer> defaultTimeoutMono = getConfigValueAsInt("defaultTimeout");
//        Mono<Integer> retryCountMono = getConfigValueAsInt("retryCount");
//        Mono<Integer> cacheMaxSizeMono = getConfigValueAsInt("cacheMaxSize");
//        Mono<Long> cacheMaxWeightMono = getConfigValueAsLong("cacheMaxWeight");
//        Mono<String> cacheCapacityModeMono = getConfigValue("cacheCapacityMode");
//        Mono<String> cacheRebuildStrategyMono = getConfigValue("cacheRebuildStrategy");
//        Mono<Integer> cacheDefaultTtlMono = getConfigValueAsInt("cacheDefaultTtl");
//        Mono<Integer> listenPortMono = getConfigValueAsInt("listenPort");
//        Mono<Boolean> cacheEnabledMono = getConfigValueAsBoolean("cacheEnabled");
//        Mono<Boolean> queryLogEnabledMono = getConfigValueAsBoolean("queryLogEnabled");
//
//        return Mono.zip(
//                upstreamDnsMono.defaultIfEmpty(parseUpstreamDnsConfig("[]")),
//                defaultTimeoutMono.defaultIfEmpty(super.getDefaultTimeout()),
//                retryCountMono.defaultIfEmpty(super.getRetryCount()),
//                cacheMaxSizeMono.defaultIfEmpty((int) super.getCacheMaxSize()),
//                cacheMaxWeightMono.defaultIfEmpty(super.getCacheMaxWeight()),
//                cacheCapacityModeMono.defaultIfEmpty(super.getCacheCapacityMode().toString()),
//                cacheRebuildStrategyMono.defaultIfEmpty(super.getCacheRebuildStrategy().toString()),
//                cacheDefaultTtlMono.defaultIfEmpty(super.getCacheDefaultTtl()),
//                listenPortMono.defaultIfEmpty(super.getListenPort()),
//                cacheEnabledMono.defaultIfEmpty(super.isCacheEnabled()),
//                queryLogEnabledMono.defaultIfEmpty(super.isQueryLogEnabled())
//        ).doOnNext(tuple -> {
//            DnsConfigSnapshot newSnapshot = new DnsConfigSnapshot(
//                tuple.getT4(),  // cacheMaxSize
//                tuple.getT5(),  // cacheMaxWeight
//                CacheCapacityMode.valueOf(tuple.getT6()),
//                CacheRebuildStrategy.valueOf(tuple.getT7()),
//                tuple.getT8(),  // cacheDefaultTtl
//                tuple.getT9(),  // listenPort
//                tuple.getT10(), // cacheEnabled
//                tuple.getT11(), // queryLogEnabled
//                tuple.getT1(),  // upstreamDns
//                tuple.getT2(),  // defaultTimeout
//                tuple.getT3(),  // retryCount
//                System.currentTimeMillis()
//            );
//
//            // 原子性替换快照
//            configSnapshot.set(newSnapshot);
//            log.debug("配置已加载完成");
//        }).then();
//    }
//
//    /**
//     * 将配置键转换为数据库键
//     */
//    private String toDatabaseKey(String configKey) {
//        return "dns." + configKey;
//    }
//
//    /**
//     * 获取配置值（响应式）
//     */
//    private Mono<String> getConfigValue(String configKey) {
//        String dbKey = toDatabaseKey(configKey);
//        return configService.getConfig(dbKey)
//                .map(value -> {
//                    if (value == null) {
//                        // 返回父类默认值（通过getter）
//                        return switch (configKey) {
//                                // 特殊处理
//                                case "upstreamDns" -> null;
//                                case "defaultTimeout" -> String.valueOf(super.getDefaultTimeout());
//                                case "retryCount" -> String.valueOf(super.getRetryCount());
//                                case "cacheMaxSize" -> String.valueOf(super.getCacheMaxSize());
//                                case "cacheMaxWeight" -> String.valueOf(super.getCacheMaxWeight());
//                                case "cacheCapacityMode" -> super.getCacheCapacityMode().toString();
//                                case "cacheRebuildStrategy" -> super.getCacheRebuildStrategy().toString();
//                                case "cacheDefaultTtl" -> String.valueOf(super.getCacheDefaultTtl());
//                                case "listenPort" -> String.valueOf(super.getListenPort());
//                                case "cacheEnabled" -> String.valueOf(super.isCacheEnabled());
//                                case "queryLogEnabled" -> String.valueOf(super.isQueryLogEnabled());
//                                default -> null;
//                            };
//                    }
//                    return value;
//                })
//                .defaultIfEmpty(Mono.just(switch (configKey) {
//                                // 特殊处理
//                                case "upstreamDns" -> null;
//                                case "defaultTimeout" -> String.valueOf(super.getDefaultTimeout());
//                                case "retryCount" -> String.valueOf(super.getRetryCount());
//                                case "cacheMaxSize" -> String.valueOf(super.getCacheMaxSize());
//                                case "cacheMaxWeight" -> String.valueOf(super.getCacheMaxWeight());
//                                case "cacheCapacityMode" -> super.getCacheCapacityMode().toString();
//                                case "cacheRebuildStrategy" -> super.getCacheRebuildStrategy().toString();
//                                case "cacheDefaultTtl" -> String.valueOf(super.getCacheDefaultTtl());
//                                case "listenPort" -> String.valueOf(super.getListenPort());
//                                case "cacheEnabled" -> String.valueOf(super.isCacheEnabled());
//                                case "queryLogEnabled" -> String.valueOf(super.isQueryLogEnabled());
//                                default -> null;
//                            }));
//    }
//
//    /**
//     * 获取配置值（响应式，返回整数）
//     */
//    private Mono<Integer> getConfigValueAsInt(String key) {
//        return getConfigValue(key)
//                .flatMap(value -> {
//                    try {
//                        return Mono.just(Integer.parseInt(value));
//                    } catch (NumberFormatException e) {
//                        log.warn("配置 {} 的值 '{}' 不是有效的整数", key, value);
//                        return Mono.empty();
//                    }
//                })
//                .defaultIfEmpty(getDefaultIntValue(key));
//    }
//
//    private int getDefaultIntValue(String key) {
//        return switch (key) {
//            case "defaultTimeout" -> super.getDefaultTimeout();
//            case "retryCount" -> super.getRetryCount();
//            case "cacheMaxSize" -> (int) super.getCacheMaxSize();
//            case "cacheDefaultTtl" -> super.getCacheDefaultTtl();
//            case "listenPort" -> super.getListenPort();
//            default -> 0;
//        };
//    }
//
//    /**
//     * 获取配置值（响应式，返回长整数）
//     */
//    private Mono<Long> getConfigValueAsLong(String key) {
//        return getConfigValue(key)
//                .flatMap(value -> {
//                    try {
//                        return Mono.just(Long.parseLong(value));
//                    } catch (NumberFormatException e) {
//                        log.warn("配置 {} 的值 '{}' 不是有效的长整数", key, value);
//                        return Mono.empty();
//                    }
//                })
//                .defaultIfEmpty(getDefaultLongValue(key));
//    }
//
//    private long getDefaultLongValue(String key) {
//        return switch (key) {
//            case "cacheMaxWeight" -> super.getCacheMaxWeight();
//            default -> 0L;
//        };
//    }
//
//    /**
//     * 获取配置值（响应式，返回布尔值）
//     */
//    private Mono<Boolean> getConfigValueAsBoolean(String key) {
//        return getConfigValue(key)
//                .map(value -> Boolean.parseBoolean(value))
//                .defaultIfEmpty(getDefaultBooleanValue(key));
//    }
//
//    private boolean getDefaultBooleanValue(String key) {
//        return switch (key) {
//            case "cacheEnabled" -> super.isCacheEnabled();
//            case "queryLogEnabled" -> super.isQueryLogEnabled();
//            default -> false;
//        };
//    }
//
//    /**
//     * 解析上游DNS配置JSON
//     */
//    private List<UpstreamDnsConfig> parseUpstreamDnsConfig(String json) {
//        if (json == null || json.trim().isEmpty()) {
//            json = "[]";
//        }
//        try {
//            return objectMapper.readValue(json, new TypeReference<List<UpstreamDnsConfig>>() {});
//        } catch (JsonProcessingException e) {
//            log.error("解析上游DNS配置失败: {}", json, e);
//            // 返回默认配置而不是空列表
//            return List.of();
//        }
//    }
//}