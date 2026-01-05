package com.npc2048.dns.service;

import com.npc2048.dns.model.entity.DnsConfig;
import com.npc2048.dns.repository.h2.ConfigEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 配置服务
 *
 * @author Linus Torvalds (通过 Claude Code)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DnsConfigService {

    private final ConfigEntityRepository repository;

    /**
     * 获取配置值
     */
    public String getConfig(String key) {
        return repository.findById(key)
                .map(DnsConfig::getValue)
                .orElse(null);
    }

    /**
     * 获取配置实体
     */
    public DnsConfig getConfigEntity(String key) {
        return repository.findById(key).orElse(null);
    }

    /**
     * 保存配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(String key, String value) {
        DnsConfig entity = repository.findById(key).orElse(null);
        if (entity == null) {
            entity = DnsConfig.builder()
                    .key(key)
                    .value(value)
                    .updatedTime(System.currentTimeMillis() / 1000)
                    .build();
        } else {
            entity.setValue(value);
            entity.setUpdatedTime(System.currentTimeMillis() / 1000);
        }
        repository.save(entity);
        log.info("配置已保存: {} = {}", key, value);
    }

    /**
     * 删除配置
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(String key) {
        repository.deleteById(key);
        log.info("配置已删除: {}", key);
    }
}