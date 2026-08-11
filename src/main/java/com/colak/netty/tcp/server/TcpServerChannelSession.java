package com.colak.netty.tcp.server;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.core.NettyScheduler;
import com.colak.netty.scheduler.eventloop.NettyChannelScheduler;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;


@RequiredArgsConstructor
@Slf4j
public class TcpServerChannelSession implements ChannelSession {
    private final String channelId;
    private final ServerSocketChannel channel;
    private final TcpManager tcpManager;

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public EventLoop getEventLoop() {
        return channel.eventLoop();
    }

    @Override
    public NettyScheduler createNettyScheduler() {
        return new NettyChannelScheduler(channel);
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    @SuppressWarnings("resource")
    public boolean isInEventLoop() {
        return channel.eventLoop().inEventLoop();
    }

    public Collection<String> getConnectionIds() {
        return tcpManager.getConnectionIds();
    }

    @Override
    public boolean close() {
        if (!channel.isOpen()) {
            return false;
        }
        channel.close()
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to close TCP server channel: {}", channelId, future.cause());
                    }
                });
        tcpManager.removeServerChannel(channelId);
        return true;
    }

    public boolean closeClientConnection(String connectionId) {
        SocketChannel channel = tcpManager.removeClientChannelId(connectionId);
        if (channel != null) {
            try {
                channel.close().sync();
                return true;
            } catch (InterruptedException exception) {
                log.error("Failed to close TCP client connection: {}", connectionId, exception);
                Thread.currentThread().interrupt();
            }
        } else {
            log.error("No TCP client connection found with connectionId: {} to close", connectionId);
        }
        return false;
    }

    @Override
    public boolean sendMessage(Object message) {
        log.warn("Cannot send message on TCP server channel: {}", channelId);
        return false;
    }
}
