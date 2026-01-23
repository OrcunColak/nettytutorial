package com.colak.netty.udprpc;

import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;

public class SolarisRpcInboundHandler extends RpcResponseInboundHandler {

    protected SolarisRpcInboundHandler(ResponseFutureRegistry registry,
                                       CorrelationStrategy correlationStrategy) {
        super(registry, correlationStrategy);
    }

    @Override
    public RpcPeerException toPeerException(Object response) {
        return null;
    }

    protected void channelRead0(ChannelHandlerContext ctx, Object response) {
        System.out.println("response = " + response);
    }
}
