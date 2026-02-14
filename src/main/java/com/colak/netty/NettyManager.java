package com.colak.netty;

import com.colak.netty.managers.UdpManager;
import com.colak.netty.scheduler.eventloop.NettyGlobalScheduler;
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

    private final NettyScheduler globalScheduler;
    private final UdpManager udpManager;
    private final boolean hasTcpSupport;

    // Package private constructor for builder
    NettyManager(NettyManagerBuilder builder) {
        this.bossGroup = builder.getBossThreads() > 0 ? createEventLoopGroup(builder.getBossThreads(),
                builder.getThreadNamePrefix() + "-boss") : null;
        this.hasTcpSupport = this.bossGroup != null;

        this.workerGroup = createEventLoopGroup(builder.getWorkerThreads(),
                builder.getThreadNamePrefix() + "-worker");

        this.globalScheduler = new NettyGlobalScheduler(workerGroup);
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


    public ChannelSession addUdpServer(UdpServerParameters parameters) {
        return udpManager.addUdpServer(parameters);
    }


    public void shutdown() {
        globalScheduler.cancelAll();
        udpManager.shutdown();

        if (hasTcpSupport) {
            Future<?> bossFuture = bossGroup.shutdownGracefully();
            bossFuture.syncUninterruptibly();
        }
        Future<?> workerGroupFuture = workerGroup.shutdownGracefully();
        workerGroupFuture.syncUninterruptibly();
    }
}
