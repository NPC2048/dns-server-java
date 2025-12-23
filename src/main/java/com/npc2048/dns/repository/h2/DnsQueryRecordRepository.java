package com.npc2048.dns.repository.h2;

import com.npc2048.dns.model.entity.QueryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DNS 查询记录的数据库访问层
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Repository
public interface DnsQueryRecordRepository extends JpaRepository<QueryRecord, Long> {

    /**
     * 根据域名查询记录
     */
    List<QueryRecord> findByDomain(String domain);

    /**
     * 查询缓存命中的记录
     */
    Page<QueryRecord> findByCacheHit(Boolean cacheHit, Pageable pageable);

    /**
     * 根据查询类型查询记录
     */
    List<QueryRecord> findByQueryType(String queryType);

    /**
     * 分页查询所有记录
     */
    Page<QueryRecord> findAllByOrderByQueryTimeDesc(Pageable pageable);

    /**
     * 查找指定域名的查询记录，并分页
     */
    Page<QueryRecord> findByDomainContainingOrderByQueryTimeDesc(String domain, Pageable pageable);

    /**
     * 统计查询总数
     */
    long count();

    /**
     * 查找缓存命中的记录数
     */
    long countByCacheHitTrue();

    /**
     * 根据查询类型分页查询记录
     */
    @Query("SELECT r FROM QueryRecord r WHERE r.queryType = :queryType ORDER BY r.queryTime DESC")
    Page<QueryRecord> findByQueryTypeOrderByQueryTimeDesc(@Param("queryType") String queryType, Pageable pageable);
}