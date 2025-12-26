package com.colak.channelgroupexamples;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class TelnetClient {


    private final String host;
    private final int port;

    static void main() throws InterruptedException {
        new TelnetClient("127.0.0.1", 8080).run();
    }

    public TelnetClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws InterruptedException {
        EventLoopGroup group = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new TelnetClientHandler());
                        }
                    });

            // Connect to the server
            Channel channel = bootstrap.connect(host, port).sync().channel();
            System.out.println("Connected to Telnet Server. Type messages to send:");

            // Read user input and send messages
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
                channel.writeAndFlush(message + "\r\n");
            }

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    private static class TelnetClientHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            System.out.println("Server Response: " + msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }


}