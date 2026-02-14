package com.colak.netty.udprpc.handler;

import com.colak.netty.streamingudprpc.StreamContext;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class RpcResponseInboundHandler extends SimpleChannelInboundHandler<Object> {
    private final ResponseFutureRegistry registry;
    private final CorrelationStrategy correlationStrategy;

    private final AtomicReference<StreamContext<?>> streamContextRef = new AtomicReference<>();

    protected RpcPeerException toPeerException(Object response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // Use channel.eventLoop().execute(() -> handler.setStreamHandler(streamHandler));
    public void setStreamContext(StreamContext<?> context) {
        streamContextRef.set(context);
    }

    /*
     * Clear active stream context.
     * Should be called from the EventLoop.
     */
    public void clearStreamContext() {
        streamContextRef.set(null);
    }

    public StreamContext<?> getStreamContext() {
        return streamContextRef.get();
    }

    // === Core logic ===
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object response) {
        var context = streamContextRef.get();
        if (context != null) {
            if (context.onMessage(response)) {
                return;
            }
        }
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