package com.colak.netty.managers;

import com.colak.netty.udpparams.UdpClientParameters;
import com.colak.netty.udpparams.UdpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
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
                    .handler(parameters.inboundHandler());

            Channel channel = bootstrap.bind(parameters.port()).sync().channel();
            channels.put(parameters.channelId(), channel);
            result = true;
            logger.info("UDP Server with ID {} started", parameters.channelId());
        } catch (InterruptedException exception) {
            // Restore interrupt status
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
    public boolean sendUdpMessage(String channelId, DatagramPacket message) {
        boolean channelExists = false;
        Channel channel = channels.get(channelId);
        if (channel instanceof NioDatagramChannel udpChannel) {
            channelExists = true;
            udpChannel.writeAndFlush(message);
        }
        return channelExists;
    }

    // Send UDP message using an existing channel, target specified in the DatagramPacket
    public boolean sendUdpMessageSync(String channelId, DatagramPacket message) {
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
        channels.values().forEach(Channel::close);
        channels.clear();

    }
}
