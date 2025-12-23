package com.npc2048.dns.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * H2配置实体类
 * 对应 dns_config 表
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Entity
@Table(name = "dns_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {

    /**
     * 配置键（主键）
     */
    @Id
    @Column(name = "`key`", length = 100)
    private String key;

    /**
     * 配置值
     */
    @Column(name = "`value`", columnDefinition = "CLOB")
    private String value;

    /**
     * 更新时间戳（Unix时间戳，秒）
     */
    @Column(name = "updated_time")
    private Long updatedTime;
}