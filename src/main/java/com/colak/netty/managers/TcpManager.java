package com.colak.netty.managers;

import com.colak.netty.TcpClientParameters;
import com.colak.netty.TcpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.UUID;
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
                        protected void initChannel(Channel channel) {
                            String clientId = UUID.randomUUID().toString(); // Generate a unique ID for each client
                            channel.pipeline()
                                    .addLast(parameters.handlerList().toArray(new ChannelHandlerAdapter[0]))
                                    // Add the exception handler
                                    .addLast(new ExceptionHandler(clientId))
                                    // User-provided handler
                                    .addLast(parameters.inboundHandlerSupplier().get());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

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
                    // We can pass more parameters if necessary
                    // .option(ChannelOption.SO_BACKLOG, backlog)
                    // .childOption(ChannelOption.SO_KEEPALIVE, false);

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

    // Exception handler class
    private class ExceptionHandler extends ChannelInboundHandlerAdapter {
        private final String channelId;

        public ExceptionHandler(String channelId) {
            this.channelId = channelId;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // Add the client channel to the map when it becomes active
            channels.put(channelId, ctx.channel());
            logger.info("Client connected: {} with ID: {}", ctx.channel().remoteAddress(), channelId);
            super.channelActive(ctx);
        }

        // Called when IOException or a ClosedChannelException occurs
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Log the exception
            logger.error("Exception in TCP communication with ID {}", channelId, cause);

            // Propagate the exception to the next handler in the pipeline
            ctx.fireExceptionCaught(cause);

            // Close the channel if an exception occurs
            ctx.close();
        }

        // Called when channel is closed
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // Remove the channel from the map when it becomes inactive
            channels.remove(channelId);
            logger.info("Channel with ID {} inactive: {}", channelId, ctx.channel().remoteAddress());
            super.channelInactive(ctx);
        }
    }

    public boolean sendTcpMessage(String channelId, byte[] message) {
        boolean channelExists = false;
        Channel channel = channels.get(channelId);
        if (channel instanceof NioServerSocketChannel || channel instanceof NioSocketChannel) {
            channelExists = true;
            ByteBuf byteBuf = Unpooled.wrappedBuffer(message);
            channel.writeAndFlush(byteBuf);
        }
        return channelExists;
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
        // Close all channels
        channels.values().forEach(Channel::close);
        channels.clear();
    }

}
