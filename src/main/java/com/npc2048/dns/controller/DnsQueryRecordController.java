package com.npc2048.dns.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.util.SaResult;
import com.npc2048.dns.model.entity.QueryRecord;
import com.npc2048.dns.service.DnsQueryRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DNS查询记录Controller
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Slf4j
@RestController
@RequestMapping("/api/dns-records")
@RequiredArgsConstructor
@SaCheckLogin
public class DnsQueryRecordController {

    private final DnsQueryRecordService service;

    /**
     * 创建新的DNS查询记录
     * POST /api/dns-records
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaResult createRecord(@Valid @RequestBody QueryRecord record) {
        try {
            log.info("创建DNS查询记录: domain={}", record.getDomain());
            QueryRecord saved = service.createRecord(record);
            return SaResult.data(saved);
        } catch (Exception e) {
            log.error("创建DNS查询记录失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 分页获取所有DNS查询记录
     * GET /api/dns-records?page=0&size=20
     */
    @GetMapping
    public SaResult getAllRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("获取DNS查询记录列表: page={}, size={}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<QueryRecord> records = service.getAllRecords(pageable);

            return SaResult.data(Map.of(
                "content", records.getContent(),
                "totalElements", records.getTotalElements(),
                "totalPages", records.getTotalPages(),
                "currentPage", page,
                "pageSize", size
            ));
        } catch (Exception e) {
            log.error("获取DNS查询记录失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取单条记录
     * GET /api/dns-records/{id}
     */
    @GetMapping("/{id}")
    public SaResult getRecordById(@PathVariable Long id) {
        try {
            log.info("获取DNS查询记录: id={}", id);
            QueryRecord record = service.getRecordById(id);
            if (record == null) {
                return SaResult.error("记录不存在，ID: " + id);
            }
            return SaResult.data(record);
        } catch (Exception e) {
            log.error("获取DNS查询记录失败: id={}", id, e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 根据域名查询记录
     * GET /api/dns-records/domain/{domain}?page=0&size=20
     */
    @GetMapping("/domain/{domain}")
    public SaResult getRecordsByDomain(
            @PathVariable String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("根据域名查询记录: domain={}", domain);
            Pageable pageable = PageRequest.of(page, size);
            Page<QueryRecord> records = service.getRecordsByDomain(domain, pageable);

            return SaResult.data(Map.of(
                "content", records.getContent(),
                "totalElements", records.getTotalElements(),
                "totalPages", records.getTotalPages(),
                "currentPage", page,
                "pageSize", size
            ));
        } catch (Exception e) {
            log.error("根据域名查询记录失败: domain={}", domain, e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 根据缓存命中状态查询记录
     * GET /api/dns-records/cache-hit?value=true&page=0&size=20
     */
    @GetMapping("/cache-hit")
    public SaResult getRecordsByCacheHit(
            @RequestParam Boolean value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("根据缓存命中状态查询记录: cacheHit={}", value);
            Pageable pageable = PageRequest.of(page, size);
            Page<QueryRecord> records = service.getRecordsByCacheHit(value, pageable);

            return SaResult.data(Map.of(
                "content", records.getContent(),
                "totalElements", records.getTotalElements(),
                "totalPages", records.getTotalPages(),
                "currentPage", page,
                "pageSize", size
            ));
        } catch (Exception e) {
            log.error("根据缓存命中状态查询记录失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 获取缓存命中率统计
     * GET /api/dns-records/stats/cache-hit-rate
     */
    @GetMapping("/stats/cache-hit-rate")
    public SaResult getCacheHitRate() {
        try {
            log.info("计算缓存命中率");
            double hitRate = service.calculateCacheHitRate();
            return SaResult.data(Map.of(
                "cacheHitRate", hitRate,
                "description", String.format("%.2f%%", hitRate * 100)
            ));
        } catch (Exception e) {
            log.error("计算缓存命中率失败", e);
            return SaResult.error(e.getMessage());
        }
    }

    /**
     * 删除DNS查询记录
     * DELETE /api/dns-records/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public SaResult deleteRecord(@PathVariable Long id) {
        try {
            log.info("删除DNS查询记录: id={}", id);
            service.deleteRecord(id);
            return SaResult.ok("记录删除成功");
        } catch (Exception e) {
            log.error("删除DNS查询记录失败: id={}", id, e);
            return SaResult.error(e.getMessage());
        }
    }
}