package com.colak.netty.channels;

import com.colak.netty.ChannelSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class UdpChannelSession implements ChannelSession {
    private final String channelId;
    private final Channel channel;

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public boolean isInEventLoop() {
        return channel.eventLoop().inEventLoop();
    }

    @Override
    public boolean close() {
        if (!channel.isOpen()) {
            return false;
        }

        ChannelFuture future = channel.close();
        future.addListener(f -> {
            if (!f.isSuccess()) {
                log.error("Failed to close UDP channel {}", channelId, f.cause());
            }
        });
        // means "close initiated"
        return true;
    }

    @Override
    public boolean isActive() {
        return channel.isActive();
    }

    @Override
    public boolean sendMessage(Object message) {
        if (!channel.isActive()) {
            log.warn("UDP channel {} is not active", channelId);
            return false;
        }

        ChannelFuture channelFuture = channel.writeAndFlush(message);
        channelFuture.addListener(future -> {
            if (!future.isSuccess()) {
                log.error("Failed to send UDP channel {}", channelId, future.cause());
            }
        });
        // means accepted for sending
        return true;
    }

}
