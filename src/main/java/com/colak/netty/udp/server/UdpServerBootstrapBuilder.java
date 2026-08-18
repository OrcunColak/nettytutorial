package com.colak.netty.udp.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UdpServerBootstrapBuilder {
    private final EventLoopGroup workerGroup;

    public Bootstrap build(UdpServerParameters parameters) {
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
        return bootstrap;
    }
}
