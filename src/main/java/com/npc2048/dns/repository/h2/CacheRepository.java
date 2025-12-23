package com.npc2048.dns.repository.h2;

import com.npc2048.dns.model.entity.CacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * H2缓存Repository
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Repository
public interface CacheRepository extends JpaRepository<CacheEntity, Long> {

}