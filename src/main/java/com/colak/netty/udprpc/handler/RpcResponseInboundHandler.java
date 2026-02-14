package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.streamingudprpc.StreamHandler;
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
    private AtomicReference<StreamHandler<?>> streamHandlerAtomicReference = new AtomicReference<>();

    protected RpcPeerException toPeerException(Object response) {
        return new RpcPeerException("Peer returned error", response);
    }

    // Use channel.eventLoop().execute(() -> handler.setStreamHandler(streamHandler));
    public void setStreamHandler(StreamHandler<?> streamHandler) {
        streamHandlerAtomicReference.set(streamHandler);
    }

    public void unSetStreamHandler(StreamHandler<?> streamHandler) {
        streamHandlerAtomicReference.set(null);
    }

    // === Core logic ===
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object response) {
        var streamHandler = streamHandlerAtomicReference.get();
        if (streamHandler != null) {
            streamHandler.internalHandleMessage(response);
        } else {
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
        }
        // Fire to next handler
        ctx.fireChannelRead(ReferenceCountUtil.retain(response));
    }
}