package com.colak.netty.tcp.handler;

import com.colak.netty.tcp.TcpEnvelope;
import com.colak.netty.tcp.TcpManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class TcpConnectionTracker extends ChannelInboundHandlerAdapter {
    public static AttributeKey<String> CONNECTION_ID_KEY = AttributeKey.valueOf("connectionId");

    private final TcpManager tcpManager;
    private final String stableClientId;

    public TcpConnectionTracker(TcpManager tcpManager) {
        this(tcpManager, null);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String connectionId = stableClientId != null ? stableClientId : UUID.randomUUID().toString();
        ctx.channel().attr(CONNECTION_ID_KEY).set(connectionId);
        if (ctx.channel() instanceof SocketChannel socketChannel) {
            tcpManager.addClientChannelIfAbsent(connectionId, socketChannel);
            log.info("TCP connection established: {}", connectionId);
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String connectionId = ctx.channel().attr(CONNECTION_ID_KEY).get();
        if (connectionId != null) {
            handleDisconnect(connectionId);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String connectionId = ctx.channel().attr(CONNECTION_ID_KEY).get();
        if (msg instanceof ByteBuf byteBuf && connectionId != null) {
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            byteBuf.release();
            TcpEnvelope<byte[]> envelope = new TcpEnvelope<>(bytes, connectionId);
            ctx.fireChannelRead(envelope);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void handleDisconnect(String connectionId) {
        tcpManager.removeClientChannel(connectionId);
        if (stableClientId != null) {
            tcpManager.scheduleReconnect(stableClientId);
        }
    }
}
