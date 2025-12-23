package com.npc2048.dns.network;

import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.service.DnsService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioDatagramChannel;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Netty-based UDP DNS server
 *
 * @author Linus Torvalds (via Claude Code)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NettyDnsServer implements CommandLineRunner {

    private final DnsConfig dnsConfig;
    private final DnsService dnsService;

    private EventLoopGroup workerGroup;
    private Channel channel;

    @Override
    public void run(@NonNull String... args) {
        startServer();
    }

    /**
     * 启动 DNS 服务器
     */
    public void startServer() {
        workerGroup = new MultiThreadIoEventLoopGroup(4, NioIoHandler.newFactory());

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, false)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加DNS编解码器（使用dnsjava的Netty集成）
                            pipeline.addLast(new DnsServerHandler(dnsService));
                        }
                    });

            channel = bootstrap.bind(dnsConfig.getListenPort()).sync().channel();
            log.info("Netty DNS服务器启动成功，监听端口: {}", dnsConfig.getListenPort());
            log.info("上游 DNS 配置: {}", dnsConfig.getUpstreamDns());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("DNS服务器启动被中断", e);
        }
    }

    /**
     * 停止 DNS 服务器
     */
    @PreDestroy
    public void stopServer() {
        if (channel != null) {
            channel.close().awaitUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty DNS服务器已停止");
    }

    /**
     * 检查服务器是否在运行
     */
    public boolean isRunning() {
        return channel != null && channel.isActive();
    }
}