package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/// K - key
/// Res - response
public abstract class AbstractRpcResponseInboundHandler<Key, Res> extends SimpleChannelInboundHandler<Res> {
    private final ResponseFutureRegistry<Key, Res> registry;

    protected AbstractRpcResponseInboundHandler(ResponseFutureRegistry<Key, Res> registry) {
        this.registry = registry;
    }

    // === Extension points ===
    protected abstract Key extractKey(Res response);

    protected abstract boolean isErrorResponse(Res response);

    protected RpcPeerException toPeerException(Res response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Res response) {
        Key key = extractKey(response);
        if (key == null) {
            return;
        }

        if (isErrorResponse(response)) {
            registry.fail(key, toPeerException(response));
        } else {
            registry.complete(key, response);
        }
    }
}
