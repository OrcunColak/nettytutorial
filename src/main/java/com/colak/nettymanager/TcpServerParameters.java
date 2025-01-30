package com.colak.nettymanager;

import io.netty.channel.ChannelInboundHandler;

import java.util.function.Supplier;

public record TcpServerParameters(String channelId, int port, Supplier<ChannelInboundHandler> inboundHandlerSupplier) {
}
