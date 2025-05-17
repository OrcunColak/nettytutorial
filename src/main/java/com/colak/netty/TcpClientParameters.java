package com.colak.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHandler;

public record TcpClientParameters(String channelId, String host, int port,
                                  SimpleChannelInboundHandler<ByteBuf> inboundHandler) {
}
