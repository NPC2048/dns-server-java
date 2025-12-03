package com.npc2048.dns.service;

import com.npc2048.dns.model.DnsQueryRecord;
import com.npc2048.dns.repository.DnsQueryRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * DNS查询记录服务（响应式实现）
 *
 * @author yuelong.liang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DnsQueryRecordService {

    private final DnsQueryRecordRepository repository;

    /**
     * 创建新的查询记录（响应式）
     */
    public Mono<DnsQueryRecord> createRecord(DnsQueryRecord record) {
        if (record.getQueryTime() == null) {
            record.setQueryTime(LocalDateTime.now());
        }
        log.debug("Creating DNS query record: {}", record.getDomain());
        return repository.save(record)
                .doOnSuccess(saved -> log.info("Saved DNS query record with ID: {}", saved.getId()));
    }

    /**
     * 查询所有记录（响应式流）
     */
    public Flux<DnsQueryRecord> getAllRecords() {
        log.debug("Fetching all DNS query records");
        return repository.findAll();
    }

    /**
     * 根据ID查询单条记录
     */
    public Mono<DnsQueryRecord> getRecordById(Long id) {
        log.debug("Fetching DNS query record by ID: {}", id);
        return repository.findById(id)
                .doOnNext(record -> log.debug("Found record: {}", record.getDomain()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("DNS query record not found with ID: {}", id);
                    return Mono.empty();
                }));
    }

    /**
     * 根据域名查询记录
     */
    public Flux<DnsQueryRecord> getRecordsByDomain(String domain) {
        log.debug("Fetching DNS query records for domain: {}", domain);
        return repository.findByDomain(domain);
    }

    /**
     * 根据缓存命中状态查询记录
     */
    public Flux<DnsQueryRecord> getRecordsByCacheHit(Boolean cacheHit) {
        log.debug("Fetching DNS query records with cache hit: {}", cacheHit);
        return repository.findByCacheHit(cacheHit);
    }

    /**
     * 删除记录
     */
    public Mono<Void> deleteRecord(Long id) {
        log.debug("Deleting DNS query record with ID: {}", id);
        return repository.deleteById(id)
                .doOnSuccess(v -> log.info("Deleted DNS query record with ID: {}", id));
    }

    /**
     * 更新记录
     */
    public Mono<DnsQueryRecord> updateRecord(Long id, DnsQueryRecord updatedRecord) {
        log.debug("Updating DNS query record with ID: {}", id);
        return repository.findById(id)
                .flatMap(existing -> {
                    existing.setDomain(updatedRecord.getDomain());
                    existing.setQueryType(updatedRecord.getQueryType());
                    existing.setResponseIp(updatedRecord.getResponseIp());
                    existing.setCacheHit(updatedRecord.getCacheHit());
                    existing.setResponseTimeMs(updatedRecord.getResponseTimeMs());
                    return repository.save(existing);
                })
                .doOnSuccess(saved -> log.info("Updated DNS query record with ID: {}", saved.getId()));
    }

    /**
     * 统计缓存命中率（响应式计算）
     */
    public Mono<Double> calculateCacheHitRate() {
        log.debug("Calculating cache hit rate");
        Mono<Long> totalCount = repository.count();
        Mono<Long> hitCount = repository.findByCacheHit(true).count();

        return Mono.zip(totalCount, hitCount)
                .map(tuple -> {
                    long total = tuple.getT1();
                    long hits = tuple.getT2();
                    if (total == 0) return 0.0;
                    double rate = (double) hits / total * 100;
                    log.debug("Cache hit rate: {}/{} = {}%", hits, total, rate);
                    return rate;
                });
    }
}
