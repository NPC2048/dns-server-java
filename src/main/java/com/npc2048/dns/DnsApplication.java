package com.npc2048.dns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author yuelong.liang
 */
@SpringBootApplication
@EnableConfigurationProperties
public class DnsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DnsApplication.class, args);
    }

}
