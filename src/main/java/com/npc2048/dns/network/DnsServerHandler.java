package com.npc2048.dns.network;

import com.npc2048.dns.service.DnsService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xbill.DNS.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Netty DNS request handler
 *
 * @author Linus Torvalds (via Claude Code)
 */
@Slf4j
@RequiredArgsConstructor
public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private final DnsService dnsService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
        try {
            // 读取请求数据
            ByteBuf content = packet.content();
            byte[] requestData = new byte[content.readableBytes()];
            content.readBytes(requestData);

            // 解析 DNS 请求
            Message dnsQuery = new Message(requestData);
            org.xbill.DNS.Record question = dnsQuery.getQuestion();

            if (question == null) {
                log.warn("Invalid DNS request: no question found");
                sendErrorResponse(ctx, packet, dnsQuery.getHeader().getID());
                return;
            }

            String domain = question.getName().toString(true);
            int type = question.getType();

            log.debug("Received DNS query: {} type: {}", domain, Type.string(type));

            // Handle DNS query (synchronous call)
            byte[] responseData = dnsService.handleDnsQuery(domain, type, requestData);

            // 发送响应
            if (responseData != null && responseData.length > 0) {
                sendResponse(ctx, packet, responseData);
            } else {
                log.warn("DNS query returned empty response: {}", domain);
                sendErrorResponse(ctx, packet, dnsQuery.getHeader().getID());
            }

        } catch (Exception e) {
            log.error("Failed to handle DNS request", e);
            sendErrorResponse(ctx, packet, 0);
        }
    }

    /**
     * Send DNS response
     */
    private void sendResponse(ChannelHandlerContext ctx, DatagramPacket packet, byte[] responseData) {
        ByteBuf buf = ctx.alloc().buffer(responseData.length);
        buf.writeBytes(responseData);

        DatagramPacket response = new DatagramPacket(buf, packet.sender());
        ctx.writeAndFlush(response).addListener(future -> {
            if (!future.isSuccess()) {
                log.error("Failed to send DNS response", future.cause());
            }
        });
    }

    /**
     * Send error response
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, DatagramPacket packet, int id) {
        try {
            Message response = new Message(id);
            Header header = response.getHeader();
            header.setRcode(Rcode.SERVFAIL);
            header.setFlag(Flags.QR);
            header.setFlag(Flags.RA);

            byte[] errorData = response.toWire();
            sendResponse(ctx, packet, errorData);
        } catch (Exception e) {
            log.error("Failed to build error response", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("DNS handler exception", cause);
        ctx.close();
    }
}