package com.npc2048.dns.service;

import com.npc2048.dns.model.entity.QueryRecord;
import com.npc2048.dns.repository.h2.DnsQueryRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * DNS查询记录服务
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DnsQueryRecordService {

    private final DnsQueryRecordRepository repository;

    /**
     * 创建新的查询记录
     */
    public QueryRecord createRecord(QueryRecord record) {
        if (record.getQueryTime() == null) {
            record.setQueryTime(Instant.now().toEpochMilli());
        }
        log.debug("创建DNS查询记录: {}", record.getDomain());
        QueryRecord saved = repository.save(record);
        log.info("保存DNS查询记录成功，ID: {}", saved.getId());
        return saved;
    }

    /**
     * 分页查询所有记录
     */
    public Page<QueryRecord> getAllRecords(Pageable pageable) {
        log.debug("获取所有DNS查询记录");
        return repository.findAllByOrderByQueryTimeDesc(pageable);
    }

    /**
     * 根据ID查询单条记录
     */
    public QueryRecord getRecordById(Long id) {
        log.debug("根据ID查询DNS记录: {}", id);
        return repository.findById(id).orElse(null);
    }

    /**
     * 根据域名查询记录
     */
    public Page<QueryRecord> getRecordsByDomain(String domain, Pageable pageable) {
        log.debug("根据域名查询记录: {}", domain);
        return repository.findByDomainContainingOrderByQueryTimeDesc(domain, pageable);
    }

    /**
     * 根据缓存命中状态查询记录
     */
    public Page<QueryRecord> getRecordsByCacheHit(Boolean cacheHit, Pageable pageable) {
        log.debug("根据缓存命中状态查询记录: {}", cacheHit);
        return repository.findByCacheHit(cacheHit, pageable);
    }

    /**
     * 删除记录
     */
    public void deleteRecord(Long id) {
        log.debug("删除DNS查询记录: {}", id);
        repository.deleteById(id);
        log.info("删除DNS查询记录成功，ID: {}", id);
    }

    /**
     * 更新记录
     */
    public QueryRecord updateRecord(Long id, QueryRecord updatedRecord) {
        log.debug("更新DNS查询记录: {}", id);
        return repository.findById(id)
                .map(existing -> {
                    existing.setDomain(updatedRecord.getDomain());
                    existing.setQueryType(updatedRecord.getQueryType());
                    existing.setResponseIp(updatedRecord.getResponseIp());
                    existing.setCacheHit(updatedRecord.getCacheHit());
                    existing.setResponseTimeMs(updatedRecord.getResponseTimeMs());
                    return repository.save(existing);
                })
                .orElse(null);
    }

    /**
     * 统计缓存命中率
     */
    public double calculateCacheHitRate() {
        log.debug("计算缓存命中率");
        long totalCount = repository.count();
        long hitCount = repository.countByCacheHitTrue();

        if (totalCount == 0) {
            return 0.0;
        }

        double rate = (double) hitCount / totalCount;
        log.debug("缓存命中率: {}/{} = {}%", hitCount, totalCount, rate * 100);
        return rate;
    }

    /**
     * 根据查询类型查询记录
     */
    public List<QueryRecord> getRecordsByQueryType(String queryType) {
        log.debug("根据查询类型查询记录: {}", queryType);
        return repository.findByQueryType(queryType);
    }
}