package com.colak.netty.udp.streaming;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.udp.rpc.RpcCallParameters;
import com.colak.netty.udp.rpc.exception.RpcException;
import com.colak.netty.udp.rpc.exception.RpcTransportException;
import com.colak.netty.udp.rpc.executors.callexecutor.RpcCallExecutor;
import com.colak.netty.udp.rpc.handler.RpcResponseInboundHandler;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class StreamingUdpRpcClient {
    private final ChannelSession channelSession;
    private final RpcResponseInboundHandler responseHandler;
    private final RpcCallExecutor rpcExecutor;

    public <T> T startStream(Object startRequest,
                             RpcCallParameters params,
                             Duration streamInactivity,
                             StreamHandler<T> handler,
                             Class<T> expectedType)
            throws RpcException, InterruptedException {

        StreamContext<T> context = new StreamContext<>(handler, responseHandler, channelSession.getEventLoop(),
                streamInactivity);
        context.start();

        try {
            Object result = rpcExecutor.executeCall(startRequest, params);
            return castResult(result, expectedType);
        } catch (Exception e) {
            context.close();
            throw e;
        }
    }

    public void stopStream() {
        StreamContext<?> ctx = responseHandler.getStreamContext();
        if (ctx != null) {
            ctx.close();
        }
    }

    private <T> T castResult(Object result, Class<T> expectedType) throws RpcTransportException {
        if (!expectedType.isInstance(result)) {
            String message = String.format("Type mismatch: expected %s but got %s (%s)",
                    expectedType.getName(),
                    result.getClass().getName(),
                    result);
            throw new RpcTransportException(message);
        }
        return expectedType.cast(result);
    }
}