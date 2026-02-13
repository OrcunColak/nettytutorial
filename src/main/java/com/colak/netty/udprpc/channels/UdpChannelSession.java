package com.colak.netty.udprpc.channels;

import com.colak.netty.udprpc.ChannelSession;
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

    // @Override
    // public boolean sendMessageSync(Object message) {
    //     if (channel.eventLoop().inEventLoop()) {
    //         throw new IllegalStateException("Blocking sendMessageSync in event loop");
    //     }
    //
    //     boolean result = false;
    //     try {
    //         ChannelFuture channelFuture = channel.writeAndFlush(message).sync();
    //         result = channelFuture.isSuccess();
    //     } catch (InterruptedException exception) {
    //         Thread.currentThread().interrupt();
    //         log.error("Interrupted while sending UDP message {}", channelId, exception);
    //     }
    //     return result;
    // }
}
