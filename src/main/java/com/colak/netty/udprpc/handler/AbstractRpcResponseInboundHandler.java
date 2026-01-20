package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class AbstractRpcResponseInboundHandler<CorrelationId, Request, Response>
        extends SimpleChannelInboundHandler<Response> {
    private final ResponseFutureRegistry<CorrelationId, Response> registry;
    private final CorrelationStrategy<CorrelationId, Request, Response> correlationStrategy;

    protected AbstractRpcResponseInboundHandler(ResponseFutureRegistry<CorrelationId, Response> registry,
                                                CorrelationStrategy<CorrelationId, Request, Response> correlationStrategy) {
        this.registry = registry;
        this.correlationStrategy = correlationStrategy;
    }

    // === Extension points ===
    protected abstract boolean isErrorResponse(Response response);

    protected RpcPeerException toPeerException(Response response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Response response) {
        CorrelationId correlationId = correlationStrategy.fromResponse(response);
        if (isErrorResponse(response)) {
            RpcException rpcException = toPeerException(response);
            registry.failFromResponse(correlationId, rpcException);
        } else {
            registry.completeFromResponse(correlationId, response);
        }
    }
}