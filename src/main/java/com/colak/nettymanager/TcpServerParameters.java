package com.colak.nettymanager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;

public record TcpServerParameters(String channelId, int port, SimpleChannelInboundHandler<ByteBuf> handler) {
}
