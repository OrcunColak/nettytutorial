package com.colak.netty;

import com.colak.netty.managers.TcpManager;
import com.colak.netty.managers.TimerManager;
import com.colak.netty.managers.UdpManager;
import com.colak.netty.tcpparams.TcpClientParameters;
import com.colak.netty.tcpparams.TcpServerParameters;
import com.colak.netty.timerparams.FixedRateTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;
import com.colak.netty.udpparams.UdpClientParameters;
import com.colak.netty.udpparams.UdpServerParameters;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NettyManager {
    // For TCP
    private final EventLoopGroup bossGroup;
    // Shared between TCP and UDP
    private final EventLoopGroup workerGroup;

    private final TimerManager timerManager;
    private final TcpManager tcpManager;
    private final UdpManager udpManager;
    private final boolean hasTcpSupport;

    // Package private constructor for builder
    NettyManager(NettyManagerBuilder builder) {
        this.bossGroup = builder.getBossThreads() > 0 ? createEventLoopGroup(builder.getBossThreads(),
                builder.getThreadNamePrefix() + "-boss") : null;
        this.hasTcpSupport = this.bossGroup != null;

        this.workerGroup = createEventLoopGroup(builder.getWorkerThreads(),
                builder.getThreadNamePrefix() + "-worker");

        this.timerManager = new TimerManager(workerGroup);
        this.tcpManager = new TcpManager(bossGroup, workerGroup);
        this.udpManager = new UdpManager(workerGroup);
    }

    public static NettyManagerBuilder builder() {
        return new NettyManagerBuilder();
    }

    private EventLoopGroup createEventLoopGroup(int threads, String threadNamePrefix) {
        if (threads < 0) {
            return null;
        }
        ThreadFactory threadFactory = new DefaultThreadFactory(threadNamePrefix);
        return new MultiThreadIoEventLoopGroup(threads, threadFactory, NioIoHandler.newFactory());
    }

    public boolean addTcpServer(TcpServerParameters parameters) {
        validateTcpSupport();
        return tcpManager.addTcpServer(parameters);
    }

    public boolean addTcpClient(TcpClientParameters parameters) {
        validateTcpSupport();
        return tcpManager.addTcpClient(parameters);
    }

    public boolean addUdpServer(UdpServerParameters parameters) {
        return udpManager.addUdpServer(parameters);
    }

    public boolean addUdpClient(UdpClientParameters parameters) {
        return udpManager.addUdpClient(parameters);
    }

    public boolean sendTcpMessage(String channelId, byte[] message) {
        validateTcpSupport();
        return tcpManager.sendTcpMessage(channelId, message);
    }

    public boolean sendUdpMessage(String channelId, Object message) {
        return udpManager.sendUdpMessage(channelId, message);
    }

    public boolean sendUdpMessageSync(String channelId, Object message) {
        return udpManager.sendUdpMessageSync(channelId, message);
    }

    public void scheduleFixedRateTimer(FixedRateTimerParameters parameters) {
        timerManager.scheduleFixedRateTimer(parameters);
    }

    public void scheduleSingleShotTimer(SingleShotTimerParameters parameters) {
        timerManager.scheduleSingleShotTimer(parameters);
    }

    public void stopTimer(String timerId) {
        timerManager.stopTimer(timerId);
    }

    public void stopAllTimers() {
        timerManager.shutdown();
    }

    public boolean shutdownUdpChannel(String channelId) {
        return udpManager.shutdownChannel(channelId);
    }

    public boolean shutdownTcpChannel(String channelId) {
        validateTcpSupport();
        return tcpManager.shutdownChannel(channelId);
    }

    private void validateTcpSupport() {
        if (!hasTcpSupport) {
            throw new IllegalStateException("TCP functionality not available.: bossThreads was negative");
        }
    }

    public void shutdown() {
        timerManager.shutdown();
        udpManager.shutdown();
        tcpManager.shutdown();

        if (hasTcpSupport) {
            Future<?> bossFuture = bossGroup.shutdownGracefully();
            bossFuture.syncUninterruptibly();
        }
        Future<?> workerGroupFuture = workerGroup.shutdownGracefully();
        workerGroupFuture.syncUninterruptibly();
    }
}
