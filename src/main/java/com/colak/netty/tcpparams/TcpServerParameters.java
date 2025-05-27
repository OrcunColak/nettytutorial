package com.colak.netty.tcpparams;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInboundHandler;

import java.util.List;
import java.util.function.Supplier;

public record TcpServerParameters(String channelId,
                                  int port,
                                  Supplier<ChannelInboundHandler> inboundHandlerSupplier,
                                  List<ChannelHandlerAdapter> handlerList) {

    public TcpServerParameters(String channelId, int port, Supplier<ChannelInboundHandler> inboundHandlerSupplier) {
        this(channelId, port, inboundHandlerSupplier, List.of());
    }
}
