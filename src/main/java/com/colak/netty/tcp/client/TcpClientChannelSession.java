package com.colak.netty.tcp.client;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.core.NettyScheduler;
import com.colak.netty.scheduler.eventloop.NettyChannelScheduler;
import com.colak.netty.tcp.TcpManager;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TcpClientChannelSession implements ChannelSession {
    private final String channelId;
    private final SocketChannel channel;
    private final TcpManager tcpManager;

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public EventLoop getEventLoop() {
        return channel != null ? channel.eventLoop() : null;
    }

    @Override
    public NettyScheduler createNettyScheduler() {
        return channel != null ? new NettyChannelScheduler(channel) : null;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    @Override
    @SuppressWarnings("resource")
    public boolean isInEventLoop() {
        return channel != null && channel.eventLoop().inEventLoop();
    }

    @Override
    public boolean sendMessage(Object message) {
        if (channel == null || !channel.isActive()) {
            log.warn("TCP client channel {} is not active", channelId);
            return false;
        }
        channel.writeAndFlush(message)
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to writeAndFlush to TCP client channel {}", channelId, future.cause());
                    }
                });
        return true;
    }
}
