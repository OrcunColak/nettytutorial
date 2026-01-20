package com.colak.netty.udprpc;

import com.colak.netty.udprpc.exception.RpcException;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@RequiredArgsConstructor
public class SimpleRpcClient<CorrelationId, Request, Response> {
    private final UdpRpcClient<CorrelationId, Request, Response> sender;
    private final BlockingRpcParameters blockingRpcParameters;

    public SimpleRpcClient(UdpRpcClient<CorrelationId, Request, Response> sender, int defaultMaxAttempts, Duration defaultTimeout) {
        this.sender = sender;
        this.blockingRpcParameters = new BlockingRpcParameters(defaultMaxAttempts, defaultTimeout);
    }

    public Response send(Request request) throws RpcException, InterruptedException {
        return sender.sendAndAwait(request, blockingRpcParameters);
    }
}
