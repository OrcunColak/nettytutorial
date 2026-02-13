package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RpcResponseInboundHandler extends SimpleChannelInboundHandler<Object> {
    private final ResponseFutureRegistry registry;
    private final CorrelationStrategy correlationStrategy;

    protected RpcPeerException toPeerException(Object response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // === Core logic ===
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object response) {
        Object key = correlationStrategy.fromResponse(response);
        if (key != null) {
            if (correlationStrategy.isErrorResponse(response)) {
                RpcException rpcPeerException = toPeerException(response);
                registry.failFromResponse(key, rpcPeerException);
            } else {
                Object completionValue = correlationStrategy.toCompletionValue(response);
                registry.completeFromResponse(key, completionValue);
            }
        }
        // Fire to next handler
        ctx.fireChannelRead(ReferenceCountUtil.retain(response));
    }
}