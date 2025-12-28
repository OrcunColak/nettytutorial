package com.colak.netty.managers;

import com.colak.netty.udpparams.UdpClientParameters;
import com.colak.netty.udpparams.UdpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UdpManager {

    private static final Logger logger = LoggerFactory.getLogger(UdpManager.class);

    private final EventLoopGroup workerGroup;

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public UdpManager(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public boolean addUdpServer(UdpServerParameters parameters) {
        boolean result = false;

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // inbound transformers (head → tail)
                            for (ChannelInboundHandler handler : parameters.getInboundEncoders()) {
                                pipeline.addLast(handler);
                            }

                            // terminal inbound handler
                            pipeline.addLast(parameters.getInboundHandler());

                            // outbound transformers (tail → head)
                            for (ChannelOutboundHandler handler : parameters.getOutboundEncoders()) {
                                pipeline.addLast(handler);
                            }
                        }
                    });

            Channel channel = bootstrap.bind(parameters.getPort()).sync().channel();
            channels.put(parameters.getChannelId(), channel);

            logger.info("UDP Server with ID {} started", parameters.getChannelId());
            result = true;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        return result;
    }


    public boolean addUdpClient(UdpClientParameters parameters) {
        boolean result = false;
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioDatagramChannel.class);

            // Only add the inbound handler if one is provided in parameters
            if (parameters.getInboundHandler() != null) {
                bootstrap.handler(parameters.getInboundHandler());
            } else {
                // No handler, just don't add any handler
                bootstrap.handler(new ChannelInboundHandlerAdapter() {
                    // Empty handler to prevent input handling
                });
            }

            if (parameters.isBroadcast()) {
                bootstrap.option(ChannelOption.SO_BROADCAST, true);
            }
            Channel channel = bootstrap
                    // Binds to any available port
                    .bind(0)
                    .sync()
                    .channel();

            channels.put(parameters.getChannelId(), channel);
            result = true;
            logger.info("UDP Client with ID {} started", parameters.getChannelId());
        } catch (InterruptedException exception) {
            // Restore interrupt status
            Thread.currentThread().interrupt();
        }
        return result;
    }

    // Send UDP message using an existing channel, target specified in the DatagramPacket
    public boolean sendUdpMessage(String channelId, Object message) {
        boolean channelExists = false;
        Channel channel = channels.get(channelId);
        if (channel instanceof NioDatagramChannel udpChannel) {
            channelExists = true;
            udpChannel.writeAndFlush(message)
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            logger.error("Failed to writeAndFlush to UDP channel: {}", channelId, future.cause());
                        }
                    });
        }
        return channelExists;
    }

    // Send UDP message using an existing channel, target specified in the DatagramPacket
    public boolean sendUdpMessageSync(String channelId, Object message) {
        boolean result = false;
        Channel channel = channels.get(channelId);
        if (channel instanceof NioDatagramChannel udpChannel) {
            try {
                ChannelFuture channelFuture = udpChannel.writeAndFlush(message).sync();
                result = channelFuture.isSuccess();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        return result;
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

    // Shuts down the server gracefully.
    // When this method is called, new channels should not be added
    public void shutdown() {
        channels.values().forEach(channel -> channel.close()
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        logger.error("Failed to close UDP channel", future.cause());
                    }
                }));
        channels.clear();
    }
}
