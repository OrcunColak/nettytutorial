package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class AbstractRpcResponseInboundHandler<Req, Res> extends SimpleChannelInboundHandler<Res> {
    private final ResponseFutureRegistry<Req, Res> registry;

    protected AbstractRpcResponseInboundHandler(ResponseFutureRegistry<Req, Res> registry) {
        this.registry = registry;
    }

    // === Extension points ===
    protected abstract boolean isErrorResponse(Res response);

    protected RpcPeerException toPeerException(Res response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Res response) {
        if (isErrorResponse(response)) {
            registry.failFromResponse(response, toPeerException(response));
        } else {
            registry.completeFromResponse(response);
        }
    }
}
