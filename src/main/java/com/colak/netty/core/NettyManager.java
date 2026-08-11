package com.colak.netty.core;

import com.colak.netty.managers.UdpManager;
import com.colak.netty.scheduler.eventloop.NettyGlobalScheduler;
import com.colak.netty.scheduler.offload.NullOffloadSchedulerImpl;
import com.colak.netty.scheduler.offload.OffloadSchedulerImpl;
import com.colak.netty.udpparams.UdpClientParameters;
import com.colak.netty.udpparams.UdpServerParameters;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;

import java.util.concurrent.ThreadFactory;

public class NettyManager {
    // For TCP
    private final EventLoopGroup bossGroup;
    // Shared between TCP and UDP
    private final EventLoopGroup workerGroup;
    private final TcpManager tcpManager;
    private final NettyScheduler nettyScheduler;
    private final UdpManager udpManager;
    private final OffloadScheduler offloadScheduler;
    private final boolean hasTcpSupport;

    /// Package private constructor for builder
    NettyManager(NettyManagerBuilder builder) {
        this.bossGroup = builder.getBossThreads() > 0 ?
                createEventLoopGroup(builder.getBossThreads(), builder.getThreadNamePrefix() + "-boss") : null;
        this.hasTcpSupport = this.bossGroup != null;

        this.workerGroup = createEventLoopGroup(builder.getWorkerThreads(), builder.getThreadNamePrefix() + "-worker");
        this.tcpManager = new TcpManager(bossGroup,workerGroup);
        this.nettyScheduler = new NettyGlobalScheduler(workerGroup);
        this.udpManager = new UdpManager(workerGroup);
        if (builder.hasOffloadSchedulerThreads()) {
            this.offloadScheduler = new OffloadSchedulerImpl(builder.getOffloadSchedulerThreads(), builder.getThreadNamePrefix());
        } else {
            this.offloadScheduler = new NullOffloadSchedulerImpl();
        }
    }

    public NettyScheduler getNettyScheduler() {
        return nettyScheduler;
    }

    public OffloadScheduler getOffloadScheduler() {
        return offloadScheduler;
    }

    public static NettyManagerBuilder builder() {
        return new NettyManagerBuilder();
    }

    private EventLoopGroup createEventLoopGroup(int threads, String threadNamePrefix) {
        if (threads <= 0) {
            return null;
        }
        ThreadFactory threadFactory = new DefaultThreadFactory(threadNamePrefix);
        return new MultiThreadIoEventLoopGroup(threads, threadFactory, NioIoHandler.newFactory());
    }


    public ChannelSession createUdpServer(UdpServerParameters parameters) {
        return udpManager.createUdpServer(parameters);
    }

    public ChannelSession createUdpClient(UdpClientParameters parameters) {
        return udpManager.createUdpClient(parameters);
    }

    public ChannelSession createTcpServer(TcpServerParameters parameters) {
        return tcpManager.createTcpServer(parameters);
    }

    public ChannelSession createTcpClient(TcpClientParameters parameters) {
        return tcpManager.createTcpClient(parameters);
    }

    public void validateTcpSupport() {
        if (!hasTcpSupport) {
            throw new IllegalStateException("TCP functionality not available : TCP support not enabled");
        }
    }

    /// Shuts down the server gracefully
    public void shutdown() {
        // Stop scheduling new timers
        offloadScheduler.shutdownAndWait();
        nettyScheduler.cancelAll();

        tcpManager.shutdown();
        udpManager.shutdown();

        Future<?> bossFuture = null;
        if (bossGroup != null) {
            bossFuture = bossGroup.shutdownGracefully();
        }
        Future<?> workerGroupFuture = workerGroup.shutdownGracefully();
        if (bossFuture != null) {
            bossFuture.syncUninterruptibly();
        }
        workerGroupFuture.syncUninterruptibly();
    }
}
