package com.colak.netty.udp;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.udp.client.UdpClientBootstrapBuilder;
import com.colak.netty.udp.client.UdpClientParameters;
import com.colak.netty.udp.server.UdpServerBootstrapBuilder;
import com.colak.netty.udp.server.UdpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Slf4j
public class UdpManager {
    private final EventLoopGroup workerGroup;
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public ChannelSession createUdpServer(UdpServerParameters parameters) {
        try {
            Bootstrap bootstrap = new UdpServerBootstrapBuilder(workerGroup)
                    .build(parameters);

            Channel channel = bootstrap.bind(parameters.getPort()).sync().channel();
            String channelId = parameters.getChannelId();
            channels.put(channelId, channel);

            ChannelSession channelSession = new UdpChannelSession(channelId, channel, this);
            log.info("UDP Server with ID {} started", channelId);
            return channelSession;
        } catch (InterruptedException e) {
            log.error("Failed to add UDP Server", e);
            throw new RuntimeException(e);
        }
    }

    public ChannelSession createUdpClient(UdpClientParameters parameters) {
        try {
            Bootstrap bootstrap = new UdpClientBootstrapBuilder(workerGroup)
                    .build(parameters);

            Channel channel = bootstrap.bind(0).sync().channel();
            String channelId = parameters.getChannelId();
            channels.put(channelId, channel);

            ChannelSession channelSession = new UdpChannelSession(channelId, channel, this);
            log.info("UDP client added with channel ID: {}", channelId);
            return channelSession;
        } catch (InterruptedException e) {
            log.error("Failed to add UDP client", e);
            throw new RuntimeException(e);
        }
    }

    public void removeChannel(String channelId) {
        channels.remove(channelId);
    }

    public void shutdown() {
        channels.values().forEach(channel -> {
            channel.close().addListener(future -> {
                if (!future.isSuccess()) {
                    log.error("Failed to close UDP Server", future.cause());
                }
            });
        });
        channels.clear();
    }
}
