package com.npc2048.dns.repository.h2;

import com.npc2048.dns.model.entity.StatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * H2统计Repository
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Repository
public interface StatsRepository extends JpaRepository<StatsEntity, String> {

}