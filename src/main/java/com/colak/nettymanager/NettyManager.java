package com.colak.nettymanager;

import com.colak.nettymanager.managers.TimerManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class NettyManager {

    private static final Logger logger = LoggerFactory.getLogger(NettyManager.class);

    // For TCP
    private final EventLoopGroup bossGroup;

    // Shared between TCP and UDP
    private final EventLoopGroup workerGroup;

    private final ConcurrentMap<String, Channel> channels = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    private final TimerManager timerManager;

    public NettyManager() {
        this(1, 4);
    }

    public NettyManager(int bossThread, int workerThreads) {
        bossGroup = new MultiThreadIoEventLoopGroup(bossThread, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(workerThreads, NioIoHandler.newFactory());
        timerManager = new TimerManager(workerGroup);
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

    public boolean startTimer(String timerId, Runnable runnable, long delay, long period) {
        return timerManager.scheduleFixedRateTimer(timerId, runnable, delay, period);
    }

    public boolean startTimer(String timerId, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return timerManager.scheduleFixedRateTimer(timerId, runnable, delay, period, timeUnit);
    }

    public void scheduleSingleShotTimer(SingleShotTimerParameters parameters) {
        timerManager.scheduleSingleShotTimer(parameters);
    }

    public void scheduleSingleShotTimer(Runnable runnable, long delay, TimeUnit timeUnit) {
        timerManager.scheduleSingleShotTimer(runnable, delay, timeUnit);
    }

    public void stopTimer(String timerId) {
        timerManager.stopTimer(timerId);
    }

    // When this method is called, new timers should not be added
    public void stopAllTimers() {
        timerManager.shutdown();
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

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
