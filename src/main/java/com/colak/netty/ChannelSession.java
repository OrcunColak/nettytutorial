package com.colak.netty;

public interface ChannelSession {

    String getChannelId();

    NettyScheduler createNettyScheduler();

    boolean close();

    boolean isActive();

    boolean isInEventLoop();

    boolean sendMessage(Object message);
}
