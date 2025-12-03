package com.npc2048.dns.repository;

import com.npc2048.dns.model.DnsQueryRecord;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * DNS查询记录Repository（响应式）
 * 继承ReactiveCrudRepository提供基础CRUD操作
 * @author yuelong.liang
 */
@Repository
public interface DnsQueryRecordRepository extends ReactiveCrudRepository<DnsQueryRecord, Long> {

    /**
     * 根据域名查询记录（响应式）
     * Spring Data R2DBC会自动实现此方法
     */
    Flux<DnsQueryRecord> findByDomain(String domain);

    /**
     * 查询缓存命中的记录
     */
    Flux<DnsQueryRecord> findByCacheHit(Boolean cacheHit);

    /**
     * 根据查询类型查询记录
     */
    Flux<DnsQueryRecord> findByQueryType(String queryType);
}
