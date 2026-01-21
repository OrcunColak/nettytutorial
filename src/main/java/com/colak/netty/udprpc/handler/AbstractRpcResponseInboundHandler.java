package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class AbstractRpcResponseInboundHandler<Key, Req, Res>
        extends SimpleChannelInboundHandler<Res> {
    private final ResponseFutureRegistry<Key> registry;
    private final CorrelationStrategy<Key, Req, Res> correlationStrategy;

    protected AbstractRpcResponseInboundHandler(
            ResponseFutureRegistry<Key> registry,
            CorrelationStrategy<Key, Req, Res> correlationStrategy) {
        this.registry = registry;
        this.correlationStrategy = correlationStrategy;
    }

    public ResponseFutureRegistry<Key> getRegistry() {
        return registry;
    }

    // === Extension points ===
    protected abstract boolean isErrorResponse(Res response);

    protected abstract Object toCompletionValue(Res response);

    protected RpcPeerException toPeerException(Res response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Res response) {
        Key key = correlationStrategy.fromResponse(response);
        if (isErrorResponse(response)) {
            RpcException rpcPeerException = toPeerException(response);
            registry.failFromResponse(key, rpcPeerException);
        } else {
            Object completionValue = toCompletionValue(response);
            registry.completeFromResponse(key, completionValue);
        }
    }
}