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
public abstract class AbstractRpcResponseInboundHandler<RES, KEY>
        extends SimpleChannelInboundHandler<RES> {

    private final ResponseFutureRegistry<KEY, RES> registry;

    protected AbstractRpcResponseInboundHandler(ResponseFutureRegistry<KEY, RES> registry) {
        this.registry = registry;
    }

    // === Extension points ===

    /**
     * Extract correlation key from response
     */
    protected abstract KEY extractKey(RES response);

    /**
     * Whether this response represents a remote error
     */
    protected abstract boolean isErrorResponse(RES response);

    /**
     * Convert error response into RpcPeerException
     */
    protected RpcPeerException toPeerException(RES response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===

    @Override
    protected final void channelRead0(ChannelHandlerContext ctx, RES response) {
        KEY key = extractKey(response);
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
