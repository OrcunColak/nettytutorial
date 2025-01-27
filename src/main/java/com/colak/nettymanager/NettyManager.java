package com.colak.nettymanager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NettyManager {

    // For TCP
    private final EventLoopGroup bossGroup;

    // Shared between TCP and UDP
    private final EventLoopGroup workerGroup;

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public NettyManager() {
        this(1, 4);
    }

    public NettyManager(int bossThread, int workerThreads) {
        bossGroup = new MultiThreadIoEventLoopGroup(bossThread, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(bossThread, NioIoHandler.newFactory());
    }

    public boolean addTcpServer(TcpServerParameters parameters) {
        boolean result = false;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(parameters.handler());
                        }
                    });

            Channel channel = bootstrap.bind(parameters.port())
                    .sync()
                    .channel();
            channels.put(parameters.channelId(), channel);
            result = true;

        } catch (Exception exception) {

        }
        return result;
    }

    public boolean addTcpClient(TcpClientParameters parameters) {

    }
}
