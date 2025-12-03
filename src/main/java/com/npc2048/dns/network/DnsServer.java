package com.npc2048.dns.network;

import com.npc2048.dns.config.DnsConfig;
import com.npc2048.dns.service.DnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UDP DNS 服务器
 *
 * @author yuelong.liang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DnsServer implements CommandLineRunner {

    private final DnsConfig dnsConfig;
    private final DnsService dnsService;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private DatagramSocket socket;

    @Override
    public void run(String... args) {
        startServer();
    }

    /**
     * 启动 DNS 服务器
     */
    public void startServer() {
        if (running.get()) {
            log.warn("DNS 服务器已经在运行");
            return;
        }

        Mono.fromRunnable(() -> {
            try {
                int port = dnsConfig.getListenPort();
                socket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
                running.set(true);

                log.info("DNS 服务器启动，监听端口: {}", port);
                log.info("上游 DNS 配置: {}", dnsConfig.getUpstreamDns());

                // 使用响应式调度器处理请求
                while (running.get()) {
                    try {
                        byte[] buffer = new byte[512]; // DNS 报文最大长度
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        // 异步处理请求
                        handleRequestAsync(packet);
                    } catch (Exception e) {
                        if (running.get()) {
                            log.error("处理 DNS 请求时出错", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("启动 DNS 服务器失败", e);
                running.set(false);
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    /**
     * 异步处理 DNS 请求
     */
    private void handleRequestAsync(DatagramPacket packet) {
        Mono.fromCallable(() -> {
            try {
                // 解析 DNS 请求
                byte[] requestData = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, requestData, 0, packet.getLength());

                // 使用 dnsjava 解析请求
                org.xbill.DNS.Message request = new org.xbill.DNS.Message(requestData);
                org.xbill.DNS.Record question = request.getQuestion();

                if (question == null) {
                    log.warn("收到无效的 DNS 请求，没有查询问题");
                    return buildErrorResponse(request);
                }

                String domain = question.getName().toString(true);
                int type = question.getType();

                log.debug("收到 DNS 查询: {} 类型: {}", domain, org.xbill.DNS.Type.string(type));

                // 处理 DNS 查询
                byte[] responseData = dnsService.handleDnsQuery(domain, type, requestData);

                return responseData;
            } catch (Exception e) {
                log.error("处理 DNS 请求失败", e);
                return buildErrorResponseFromRaw(packet.getData());
            }
        }).subscribeOn(Schedulers.boundedElastic())
          .subscribe(responseData -> {
              try {
                  // 发送响应
                  DatagramPacket responsePacket = new DatagramPacket(
                          responseData, responseData.length,
                          packet.getAddress(), packet.getPort()
                  );
                  socket.send(responsePacket);
              } catch (Exception e) {
                  log.error("发送 DNS 响应失败", e);
              }
          });
    }

    /**
     * 构建错误响应
     */
    private byte[] buildErrorResponse(org.xbill.DNS.Message request) {
        try {
            org.xbill.DNS.Message response = new org.xbill.DNS.Message(request.getHeader().getID());
            response.getHeader().setRcode(org.xbill.DNS.Rcode.SERVFAIL);
            response.getHeader().setFlag(org.xbill.DNS.Flags.QR);
            response.getHeader().setFlag(org.xbill.DNS.Flags.RA);
            return response.toWire();
        } catch (Exception e) {
            log.error("构建错误响应失败", e);
            return new byte[0];
        }
    }

    /**
     * 从原始数据构建错误响应
     */
    private byte[] buildErrorResponseFromRaw(byte[] requestData) {
        try {
            // 提取请求 ID
            ByteBuffer buffer = ByteBuffer.wrap(requestData);
            short id = buffer.getShort();

            org.xbill.DNS.Message response = new org.xbill.DNS.Message(id);
            response.getHeader().setRcode(org.xbill.DNS.Rcode.SERVFAIL);
            response.getHeader().setFlag(org.xbill.DNS.Flags.QR);
            response.getHeader().setFlag(org.xbill.DNS.Flags.RA);
            return response.toWire();
        } catch (Exception e) {
            log.error("从原始数据构建错误响应失败", e);
            return new byte[0];
        }
    }

    /**
     * 停止 DNS 服务器
     */
    public void stopServer() {
        if (!running.get()) {
            log.warn("DNS 服务器未运行");
            return;
        }

        running.set(false);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        log.info("DNS 服务器已停止");
    }

    /**
     * 检查服务器是否在运行
     */
    public boolean isRunning() {
        return running.get();
    }
}