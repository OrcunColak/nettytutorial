package com.colak.netty;

public interface ChannelSession {

    String getChannelId();

    boolean close();

    boolean isActive();

    boolean isInEventLoop();

    boolean sendMessage(Object message);
}
