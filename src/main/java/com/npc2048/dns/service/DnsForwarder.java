package com.npc2048.dns.service;

import com.npc2048.dns.model.UpstreamDnsConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * DNS 转发器
 *
 * @author yuelong.liang
 */
@Slf4j
@Component
public class DnsForwarder {

    /**
     * 转发 DNS 查询到上游服务器
     *
     * @param domain     域名
     * @param type       查询类型
     * @param upstream   上游 DNS 配置
     * @param requestData 原始请求数据
     * @return 响应数据
     */
    public byte[] forwardQuery(String domain, int type, UpstreamDnsConfig upstream, byte[] requestData) {
        if (upstream == null) {
            log.error("上游 DNS 配置为空");
            return null;
        }

        try {
            log.debug("转发查询到上游 DNS: {}:{} (代理: {})",
                    upstream.getAddress(), upstream.getPort(), upstream.getUseProxy());

            byte[] responseData;

            if (Boolean.TRUE.equals(upstream.getUseProxy()) && upstream.getProxyConfig() != null) {
                // 通过代理转发
                responseData = forwardThroughProxy(domain, type, upstream, requestData);
            } else {
                // 直连转发
                responseData = forwardDirect(domain, type, upstream, requestData);
            }

            if (responseData != null && responseData.length > 0) {
                log.debug("从上游 DNS 收到响应: {} 字节", responseData.length);
            } else {
                log.warn("从上游 DNS 收到空响应");
            }

            return responseData;

        } catch (Exception e) {
            log.error("转发 DNS 查询失败: {} -> {}:{}", domain, upstream.getAddress(), upstream.getPort(), e);
            return null;
        }
    }

    /**
     * 直连转发
     */
    private byte[] forwardDirect(String domain, int type, UpstreamDnsConfig upstream, byte[] requestData) {
        DatagramSocket socket = null;
        try {
            // 创建 UDP socket
            socket = new DatagramSocket();
            socket.setSoTimeout(upstream.getTimeout() != null ? upstream.getTimeout() : 5000);

            // 准备请求数据包
            InetAddress address = InetAddress.getByName(upstream.getAddress());
            int port = upstream.getPort() != null ? upstream.getPort() : 53;
            DatagramPacket requestPacket = new DatagramPacket(
                    requestData, requestData.length,
                    address, port
            );

            // 发送请求
            socket.send(requestPacket);

            // 接收响应
            byte[] buffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // 提取响应数据
            byte[] responseData = new byte[responsePacket.getLength()];
            System.arraycopy(responsePacket.getData(), 0, responseData, 0, responsePacket.getLength());

            return responseData;

        } catch (Exception e) {
            log.error("直连转发失败: {} -> {}:{}", domain, upstream.getAddress(), upstream.getPort(), e);
            return null;
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    /**
     * 通过代理转发
     */
    private byte[] forwardThroughProxy(String domain, int type, UpstreamDnsConfig upstream, byte[] requestData) {
        UpstreamDnsConfig.ProxyConfig proxyConfig = upstream.getProxyConfig();
        if (proxyConfig == null) {
            log.warn("代理配置为空，使用直连");
            return forwardDirect(domain, type, upstream, requestData);
        }

        try {
            // 根据代理类型创建代理
            Proxy proxy = createProxy(proxyConfig);
            if (proxy == null) {
                log.warn("不支持的代理类型: {}，使用直连", proxyConfig.getType());
                return forwardDirect(domain, type, upstream, requestData);
            }

            // 注意：Java 的 Proxy 类主要用于 TCP，DNS 是 UDP 协议
            // 这里简化处理，实际 DNS over TCP 可以通过代理，但 UDP 需要 SOCKS5 代理支持
            log.warn("DNS over UDP 通过代理转发需要特殊处理，当前简化实现可能不工作");
            log.warn("代理配置: {}://{}:{}", proxyConfig.getType(), proxyConfig.getHost(), proxyConfig.getPort());

            // 简化实现：对于 HTTP 代理，可以尝试 DNS over HTTPS 或 TCP
            // 这里先返回 null，后续可以扩展
            return null;

        } catch (Exception e) {
            log.error("代理转发失败: {} -> {}:{} 代理: {}:{}",
                    domain, upstream.getAddress(), upstream.getPort(),
                    proxyConfig.getHost(), proxyConfig.getPort(), e);
            return null;
        }
    }

    /**
     * 创建代理
     */
    private Proxy createProxy(UpstreamDnsConfig.ProxyConfig proxyConfig) {
        try {
            String type = proxyConfig.getType().toUpperCase();
            String host = proxyConfig.getHost();
            int port = proxyConfig.getPort() != null ? proxyConfig.getPort() : 0;

            if (port <= 0 || port > 65535) {
                log.warn("代理端口无效: {}", port);
                return null;
            }

            SocketAddress address = new InetSocketAddress(host, port);

            switch (type) {
                case "HTTP":
                    return new Proxy(Proxy.Type.HTTP, address);
                case "SOCKS":
                case "SOCKS5":
                    return new Proxy(Proxy.Type.SOCKS, address);
                default:
                    log.warn("不支持的代理类型: {}", type);
                    return null;
            }
        } catch (Exception e) {
            log.error("创建代理失败", e);
            return null;
        }
    }

    /**
     * 构建错误响应
     */
    private byte[] buildErrorResponse(byte[] requestData) {
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
            log.error("构建错误响应失败", e);
            return new byte[0];
        }
    }
}