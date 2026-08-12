package com.colak.netty.udprpc;

import com.colak.netty.udp.rpc.exception.RpcPeerException;
import com.colak.netty.udp.rpc.handler.RpcResponseInboundHandler;
import com.colak.netty.udp.rpc.response.CorrelationStrategy;
import com.colak.netty.udp.rpc.response.ResponseFutureRegistry;
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
