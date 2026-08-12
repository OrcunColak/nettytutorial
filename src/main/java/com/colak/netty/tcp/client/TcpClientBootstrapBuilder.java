package com.colak.netty.tcp.client;

import com.colak.netty.tcp.TcpManager;
import com.colak.netty.tcp.handler.TcpConnectionTracker;
import com.colak.netty.tcp.handler.TcpExceptionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.RequiredArgsConstructor;

/// Build a Netty Boostrap for TCP client connection
@RequiredArgsConstructor
public class TcpClientBootstrapBuilder {
    private final EventLoopGroup eventLoopGroup;
    private final TcpManager tcpManager;

    public Bootstrap build(TcpClientParameters parameters) {
        return new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) parameters.getConnectTimeoutMs())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        for (ChannelInboundHandler handler : parameters.getInboundDecoders()) {
                            pipeline.addLast(handler);
                        }
                        TcpConnectionTracker tcpConnectionTracker = new TcpConnectionTracker(tcpManager,
                                parameters.getChannelId());
                        pipeline.addLast(tcpConnectionTracker);
                        for (ChannelInboundHandler handler : parameters.getInboundHandlers()) {
                            pipeline.addLast(handler);
                        }
                        for (ChannelOutboundHandler handler : parameters.getOutboundEncoders()) {
                            pipeline.addLast(handler);
                        }
                        TcpExceptionHandler tcpExceptionHandler = new TcpExceptionHandler();
                        pipeline.addLast(tcpExceptionHandler);

                    }
                });
    }
}
