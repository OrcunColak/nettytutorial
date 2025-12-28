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
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;

public class NettyManager {

    private final EventLoopGroup bossGroup;

    private final EventLoopGroup workerGroup;

    private final TimerManager timerManager;
    private final TcpManager tcpManager;
    private final UdpManager udpManager;


    public NettyManager() {
        this(1, 4);
    }

    public NettyManager(int bossThread, int workerThreads) {
        this(new NettyManagerParameters(bossThread, workerThreads));
    }

    public NettyManager(NettyManagerParameters parameters) {
        if (parameters.executor() != null) {
            bossGroup = new MultiThreadIoEventLoopGroup(parameters.bossThread(), NioIoHandler.newFactory());
            workerGroup = new MultiThreadIoEventLoopGroup(parameters.workerThreads(), parameters.executor(), NioIoHandler.newFactory());
        } else {
            DefaultThreadFactory bossThreadFactory = new DefaultThreadFactory("netty-boss");
            bossGroup = new MultiThreadIoEventLoopGroup(parameters.bossThread(), bossThreadFactory, NioIoHandler.newFactory());

            // Create a custom thread factory with a specific name prefix
            DefaultThreadFactory workerThreadFactory = new DefaultThreadFactory("netty-worker");
            workerGroup = new MultiThreadIoEventLoopGroup(parameters.workerThreads(), workerThreadFactory, NioIoHandler.newFactory());
        }

        timerManager = new TimerManager(workerGroup);
        tcpManager = new TcpManager(bossGroup, workerGroup);
        udpManager = new UdpManager(workerGroup);
    }

    public boolean addTcpServer(TcpServerParameters parameters) {
        return tcpManager.addTcpServer(parameters);
    }

    public boolean addTcpClient(TcpClientParameters parameters) {
        return tcpManager.addTcpClient(parameters);
    }

    public boolean addUdpServer(UdpServerParameters parameters) {
        return udpManager.addUdpServer(parameters);
    }

    public boolean addUdpClient(UdpClientParameters parameters) {
        return udpManager.addUdpClient(parameters);
    }

    public boolean sendTcpMessage(String channelId, byte[] message) {
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
        return tcpManager.shutdownChannel(channelId);
    }

    public void shutdown() {
        timerManager.shutdown();
        udpManager.shutdown();
        tcpManager.shutdown();

        Future<?> bossFuture = bossGroup.shutdownGracefully();
        Future<?> workerGroupFuture = workerGroup.shutdownGracefully();

        bossFuture.syncUninterruptibly();
        workerGroupFuture.syncUninterruptibly();
    }

}
