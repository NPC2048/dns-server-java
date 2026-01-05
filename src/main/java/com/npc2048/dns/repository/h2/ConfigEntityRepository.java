package com.npc2048.dns.repository.h2;

import com.npc2048.dns.model.entity.DnsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Linus Torvalds (通过 Claude Code)
 */
@Repository
public interface ConfigEntityRepository extends JpaRepository<DnsConfig, String> {
}