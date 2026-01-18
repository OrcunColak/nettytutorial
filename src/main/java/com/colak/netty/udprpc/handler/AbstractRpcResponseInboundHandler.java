package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.exception.RpcTransportException;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/// ```
/// pipeline.addLast(new UdpResponseDecoder());
/// pipeline.addLast(
///     new AbstractRpcResponseInboundHandler<>(
///         responseFutureRegistry,
///         MyResponse::getCorrelationId
///     )
/// );
/// ```
public abstract class AbstractRpcResponseInboundHandler<Res, K> extends SimpleChannelInboundHandler<Res> {
    private final ResponseFutureRegistry<Res, K> registry;

    protected AbstractRpcResponseInboundHandler(ResponseFutureRegistry<Res, K> registry) {
        this.registry = registry;
    }

    // === Extension points ===
    protected abstract K extractKey(Res response);

    protected abstract boolean isErrorResponse(Res response);

    protected RpcPeerException toPeerException(Res response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===
    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, Res response) {
        K key = extractKey(response);
        if (key == null) {
            return;
        }

        if (isErrorResponse(response)) {
            registry.fail(key, toPeerException(response));
        } else {
            registry.complete(key, response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        registry.failAll(
                new RpcTransportException("Inbound handler failure", cause)
        );
        ctx.close();
    }
}
