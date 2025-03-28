package com.colak.channelgroup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

// If you want to broadcast a message to all connected clients, you need to manage a group of channels manually using a ChannelGroup.
public class BroadcastServer {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new StringDecoder(),
                                    new StringEncoder(),
                                    new NettyServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(8080).sync();
            System.out.println("Netty server started, listening on port 8080");
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    // Netty server handler class
    private static class NettyServerHandler extends ChannelInboundHandlerAdapter {

        // Maintain a group of active channels (clients)
        private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            // Add new client to the channel group
            channels.add(ctx.channel());
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            // Remove client from the channel group when they disconnect
            channels.remove(ctx.channel());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                String message = (String) msg;
                System.out.println("Received message from client: " + message);

                // Broadcast message to all clients
                channels.writeAndFlush("Server received message: " + message + "\n");
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}