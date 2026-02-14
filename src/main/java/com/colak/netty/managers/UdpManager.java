package com.colak.netty.managers;

import com.colak.netty.ChannelSession;
import com.colak.netty.channels.UdpChannelSession;
import com.colak.netty.timerparams.FixedRateTimerParameters;
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
import io.netty.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Slf4j
public class UdpManager {
    private final EventLoopGroup workerGroup;
    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    public ChannelSession addUdpServer(UdpServerParameters parameters) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioDatagramChannel.class)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        // decoders
                        for (ChannelInboundHandler decoder : parameters.getInboundDecoders()) {
                            pipeline.addLast(decoder);
                        }

                        // handlers
                        for (ChannelInboundHandler handler : parameters.getInboundHandlers()) {
                            pipeline.addLast(handler);
                        }

                        // encoders
                        for (ChannelOutboundHandler encoder : parameters.getOutboundEncoders()) {
                            pipeline.addLast(encoder);
                        }
                    }
                });

        Channel channel = null;
        try {
            channel = bootstrap.bind(parameters.getPort()).sync().channel();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String channelId = parameters.getChannelId();
        channels.put(channelId, channel);
        ChannelSession channelSession = new UdpChannelSession(channelId, channel);

        log.info("UDP Server with ID {} started", channelId);
        return channelSession;

    }

    public void shutdown() {
    }
}
