package com.colak.netty;

import io.netty.channel.EventLoop;

public interface ChannelSession {

    String getChannelId();

    EventLoop getEventLoop();

    NettyScheduler createNettyScheduler();

    boolean close();

    boolean isActive();

    boolean isInEventLoop();

    boolean sendMessage(Object message);
}
