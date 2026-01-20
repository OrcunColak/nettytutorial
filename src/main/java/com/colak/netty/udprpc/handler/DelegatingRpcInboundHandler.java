package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DelegatingRpcInboundHandler<CorrelationId, Request, Response> extends SimpleChannelInboundHandler<Response> {
    private final ResponseFutureRegistry<CorrelationId, Response> registry;
    private final CorrelationStrategy<CorrelationId, Request, Response> correlationStrategy;
    private final RpcResponseHandler<CorrelationId, Request, Response> userHandler;

    // Constructor with response class
    public DelegatingRpcInboundHandler(
            ResponseFutureRegistry<CorrelationId, Response> registry,
            CorrelationStrategy<CorrelationId, Request, Response> correlationStrategy,
            RpcResponseHandler<CorrelationId, Request, Response> userHandler) {
        super(null); // Pass null - accept all messages
        this.registry = registry;
        this.correlationStrategy = correlationStrategy;
        this.userHandler = userHandler;
    }


    // === Core logic ===
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Response response) {
        CorrelationId correlationId = correlationStrategy.fromResponse(response);
        if (userHandler.isErrorResponse(response)) {
            RpcException rpcException = userHandler.toPeerException(response);
            registry.failFromResponse(correlationId, rpcException);
        } else {
            registry.completeFromResponse(correlationId, response);
        }
    }
}