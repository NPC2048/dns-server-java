package com.npc2048.dns.controller;

import com.npc2048.dns.model.DnsQueryRecord;
import com.npc2048.dns.service.DnsQueryRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * DNS查询记录Controller（响应式REST API）
 * 所有方法返回Mono或Flux，实现完全非阻塞
 *
 * @author yuelong.liang
 */
@Slf4j
@RestController
@RequestMapping("/api/dns-records")
@RequiredArgsConstructor
public class DnsQueryRecordController {

    private final DnsQueryRecordService service;

    /**
     * 创建新的DNS查询记录
     * POST /api/dns-records
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<DnsQueryRecord> createRecord(@Valid @RequestBody DnsQueryRecord record) {
        log.info("REST API: Creating new DNS query record for domain: {}", record.getDomain());
        return service.createRecord(record);
    }

    /**
     * 获取所有DNS查询记录
     * GET /api/dns-records
     */
    @GetMapping
    public Flux<DnsQueryRecord> getAllRecords() {
        log.info("REST API: Fetching all DNS query records");
        return service.getAllRecords();
    }

    /**
     * 根据ID获取单条记录
     * GET /api/dns-records/{id}
     */
    @GetMapping("/{id}")
    public Mono<DnsQueryRecord> getRecordById(@PathVariable Long id) {
        log.info("REST API: Fetching DNS query record by ID: {}", id);
        return service.getRecordById(id)
                .switchIfEmpty(Mono.error(new RecordNotFoundException("Record not found with ID: " + id)));
    }

    /**
     * 根据域名查询记录
     * GET /api/dns-records/domain/{domain}
     */
    @GetMapping("/domain/{domain}")
    public Flux<DnsQueryRecord> getRecordsByDomain(@PathVariable String domain) {
        log.info("REST API: Fetching DNS query records for domain: {}", domain);
        return service.getRecordsByDomain(domain);
    }

    /**
     * 根据缓存命中状态查询记���
     * GET /api/dns-records/cache-hit?value=true
     */
    @GetMapping("/cache-hit")
    public Flux<DnsQueryRecord> getRecordsByCacheHit(@RequestParam(defaultValue = "true") Boolean value) {
        log.info("REST API: Fetching DNS query records with cache hit: {}", value);
        return service.getRecordsByCacheHit(value);
    }

    /**
     * 更新DNS查询记录
     * PUT /api/dns-records/{id}
     */
    @PutMapping("/{id}")
    public Mono<DnsQueryRecord> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody DnsQueryRecord record) {
        log.info("REST API: Updating DNS query record with ID: {}", id);
        return service.updateRecord(id, record)
                .switchIfEmpty(Mono.error(new RecordNotFoundException("Record not found with ID: " + id)));
    }

    /**
     * ���除DNS查询记录
     * DELETE /api/dns-records/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteRecord(@PathVariable Long id) {
        log.info("REST API: Deleting DNS query record with ID: {}", id);
        return service.deleteRecord(id);
    }

    /**
     * 获取缓存命中率统计
     * GET /api/dns-records/stats/cache-hit-rate
     */
    @GetMapping("/stats/cache-hit-rate")
    public Mono<CacheHitRateResponse> getCacheHitRate() {
        log.info("REST API: Calculating cache hit rate");
        return service.calculateCacheHitRate()
                .map(rate -> new CacheHitRateResponse(rate));
    }

    /**
     * 服务器推送事件（SSE）- 实时推送新记录
     * GET /api/dns-records/stream
     * 演示响应式流的实时推送能力
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DnsQueryRecord> streamRecords() {
        log.info("REST API: Starting SSE stream for DNS query records");
        return service.getAllRecords()
                .delayElements(Duration.ofSeconds(1))  // 每秒推送一条，模拟实时效果
                .doOnNext(record -> log.debug("SSE: Pushing record {}", record.getId()))
                .doOnComplete(() -> log.info("SSE: Stream completed"));
    }

    /**
     * 缓存命中率响应DTO
     */
    public record CacheHitRateResponse(Double hitRate) {
    }

    /**
     * 记录未找到异常
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class RecordNotFoundException extends RuntimeException {
        public RecordNotFoundException(String message) {
            super(message);
        }
    }
}
