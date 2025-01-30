package com.colak.nettymanager.managers;

import com.colak.nettymanager.TcpClientParameters;
import com.colak.nettymanager.TcpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TcpManager {

    private static final Logger logger = LoggerFactory.getLogger(TcpManager.class);

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public TcpManager(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
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
                                    .addLast(parameters.inboundHandler());
                        }
                    });

            Channel channel = bootstrap.bind(parameters.port())
                    .sync()
                    .channel();
            channels.put(parameters.channelId(), channel);
            result = true;
            logger.info("TCP Server with ID {} started", parameters.channelId());

        } catch (InterruptedException exception) {
            // Restore interrupt status
            Thread.currentThread().interrupt();
        }
        return result;
    }

    public boolean addTcpClient(TcpClientParameters parameters) {
        boolean resut = false;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline()
                                    .addLast(parameters.inboundHandler());
                        }
                    });

            Channel channel = bootstrap.connect(new InetSocketAddress(parameters.host(), parameters.port()))
                    .sync()
                    .channel();
            channels.put(parameters.channelId(), channel);
            resut = true;
            logger.info("TCP Client with ID {} started", parameters.channelId());
        } catch (InterruptedException exception) {
            // Restore interrupt status
            Thread.currentThread().interrupt();
        }
        return resut;
    }


    public boolean shutdownChannel(String channelId) {
        boolean result = false;

        Channel channel = channels.remove(channelId);
        if (channel != null) {
            try {
                // Close the channel and wait for it to complete
                channel.close().sync();
                result = true;
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        }
        return result;
    }

    public void shutdown() {
        channels.values().forEach(Channel::close);
        channels.clear();
    }

}
