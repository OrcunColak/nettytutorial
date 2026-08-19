package com.colak.netty.tcp.client;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.core.NettyScheduler;
import com.colak.netty.scheduler.eventloop.NettyChannelScheduler;
import com.colak.netty.tcp.TcpManager;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TcpClientChannelSession implements ChannelSession {
    private final String channelId;
    private final TcpManager tcpManager;

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public EventLoop getEventLoop() {
        Channel channel = tcpManager.getClientChannel(channelId);
        if (channel == null) {
            throw new IllegalStateException("TCP client '" + channelId + "' is not connected");
        }
        return channel.eventLoop();
    }

    @Override
    public NettyScheduler createNettyScheduler() {
        Channel channel = tcpManager.getClientChannel(channelId);
        if (channel == null) {
            throw new IllegalStateException("TCP client '" + channelId + "' is not connected");
        }
        return new NettyChannelScheduler(channel);
    }

    @Override
    public boolean isActive() {
        Channel channel = tcpManager.getClientChannel(channelId);
        return channel != null && channel.isActive();
    }

    @Override
    @SuppressWarnings("resource")
    public boolean isInEventLoop() {
        Channel channel = tcpManager.getClientChannel(channelId);
        return channel != null && channel.eventLoop().inEventLoop();
    }

    @Override
    public boolean close() {
        tcpManager.stopReconnect(channelId); // prevents reconnect
        Channel channel = tcpManager.removeClientChannel(channelId);
        if (channel == null) {
            return false;
        }
        channel.close().addListener(future -> {
            if (!future.isSuccess()) {
                log.error("Failed to close TCP client: {}", channelId, future.cause());
            }
        });
        return true;
    }

    @Override
    public boolean sendMessage(Object message) {
        Channel channel = tcpManager.removeClientChannel(channelId);
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
