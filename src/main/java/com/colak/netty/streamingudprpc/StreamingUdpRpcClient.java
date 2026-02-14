package com.colak.netty.streamingudprpc;

import com.colak.netty.ChannelSession;
import com.colak.netty.NettyScheduler;
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

        NettyScheduler scheduler = channelSession.createNettyScheduler();

        StreamInactivityTracker tracker = new StreamInactivityTracker(scheduler, Duration.ofSeconds(10), handler::timeout);

        handler.setTracker(tracker);
        handler.setTerminationCallback(() -> {
            tracker.stop();
            stopStream(handler);
        });

        tracker.start();

        responseHandler.setStreamHandler(handler);
        // install first
        try {
            rpcExecutor.executeCall(startRequest, params);
        } catch (Exception e) {
            // rollback on failure
            responseHandler.unSetStreamHandler(handler);
            throw e;
        }
    }

    public void stopStream(StreamHandler<?> handler) {
        responseHandler.unSetStreamHandler(handler);
        handler.terminateStream();
    }
}