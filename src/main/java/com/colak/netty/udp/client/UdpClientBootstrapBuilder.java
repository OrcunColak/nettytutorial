package com.colak.netty.udp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UdpClientBootstrapBuilder {
    private final EventLoopGroup workerGroup;

    public Bootstrap build(UdpClientParameters parameters) {
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
        return bootstrap;
    }
}
