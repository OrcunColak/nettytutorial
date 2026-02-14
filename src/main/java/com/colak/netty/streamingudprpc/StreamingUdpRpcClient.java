package com.colak.netty.streamingudprpc;

import com.colak.netty.ChannelSession;
import com.colak.netty.udprpc.RpcCallParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.executors.call.RpcCallExecutor;
import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class StreamingUdpRpcClient {
    private final ChannelSession channelSession;
    private final RpcResponseInboundHandler responseHandler;
    private final RpcCallExecutor rpcExecutor;

    public <T> void startStream(Object startRequest, RpcCallParameters params, StreamHandler<T> handler)
            throws RpcException, InterruptedException {

        StreamContext<T> context = new StreamContext<>(
                handler,
                responseHandler,
                channelSession.getEventLoop(),
                Duration.ofSeconds(10)
        );

        context.start();

        try {
            rpcExecutor.executeCall(startRequest, params);
        } catch (Exception e) {
            context.close();
            throw e;
        }
    }

    public void stopStream(StreamHandler<?> handler) {
        StreamContext<?> ctx = responseHandler.getStreamContext();
        if (ctx != null) {
            ctx.close();
        }
    }
}