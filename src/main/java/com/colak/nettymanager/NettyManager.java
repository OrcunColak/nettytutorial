package com.colak.nettymanager;

import com.colak.nettymanager.managers.TcpManager;
import com.colak.nettymanager.managers.TimerManager;
import com.colak.nettymanager.managers.UdpManager;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;

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
        bossGroup = new MultiThreadIoEventLoopGroup(bossThread, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(workerThreads, NioIoHandler.newFactory());

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

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
