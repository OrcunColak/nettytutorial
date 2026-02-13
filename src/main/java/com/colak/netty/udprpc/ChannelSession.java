package com.colak.netty.udprpc;

public interface ChannelSession {

    String getChannelId();

    boolean close();

    boolean isActive();

    boolean sendMessage(Object message);

    // boolean sendMessageSync(Object message);
}
