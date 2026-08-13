package com.colak.netty.udp;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.udp.client.UdpClientParameters;
import com.colak.netty.udp.server.UdpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
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
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // decoders
                            for (ChannelInboundHandler decoder : parameters.getInboundDecoders()) {
                                pipeline.addLast(decoder);
                            }
                            // handlers
                            for (ChannelInboundHandler handler : parameters.getInboundHandlers()) {
                                pipeline.addLast(handler);
                            }
                            // encoders
                            for (ChannelOutboundHandler encoder : parameters.getOutboundEncoders()) {
                                pipeline.addLast(encoder);
                            }
                        }
                    });

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
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class);
            // Only add the inbound handler if one is provided in parameters
            if (parameters.getInboundHandler() != null) {
                bootstrap.handler(parameters.getInboundHandler());
            } else {
                bootstrap.handler(new ChannelInboundHandlerAdapter() {
                    // Empty handler to prevent input handling
                });
            }
            if (parameters.isBroadcast()) {
                bootstrap.option(ChannelOption.SO_BROADCAST, true);
            }
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
