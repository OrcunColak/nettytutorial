package com.colak.netty.tcp.server;

import com.colak.netty.tcp.TcpManager;
import com.colak.netty.tcp.handler.TcpConnectionTracker;
import com.colak.netty.tcp.handler.TcpExceptionHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;

/// Build a Netty ServerBootstrap for TCP server connections.
@RequiredArgsConstructor
public class TcpServerBoostrapBuilder {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final TcpManager tcpManager;

    public ServerBootstrap build(TcpServerParameters parameters) {
        return new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<>() {
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
