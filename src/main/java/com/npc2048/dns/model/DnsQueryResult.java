package com.npc2048.dns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DNS查询结果 DTO
 * 用于前端测试DNS服务时返回的详细结果
 *
 * @author yuelong.liang
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DnsQueryResult {

    /**
     * 查询的域名
     */
    private String domain;

    /**
     * IP地址列表（A记录解析结果）
     */
    private List<String> ipAddresses;

    /**
     * TTL（生存时间，秒）
     */
    private Integer ttl;

    /**
     * 是否命中缓存
     */
    private Boolean cacheHit;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 查询时间
     */
    private LocalDateTime queryTime;

    /**
     * 查询类型（如 A, AAAA, CNAME 等）
     */
    private String queryType;

    /**
     * 是否查询成功
     */
    private Boolean success;

    /**
     * 错误信息（如果查询失败）
     */
    private String errorMessage;
}