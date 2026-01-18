package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcTimeoutException;
import com.colak.netty.udprpc.exception.RpcTransportException;
import com.colak.netty.udprpc.response.ExtractingResponseFutureRegistry;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public final class UdpBlockingRpcSender<Key, Req, Res> {
    private final NettyManager nettyManager;
    private final ExtractingResponseFutureRegistry<Key, Req, Res> registry;

    public Res sendAndAwait(String channelId, Req request, BlockingRpcParameters params)
            throws RpcException, InterruptedException {
        CompletableFuture<Res> future = registry.registerRequest(request);
        long timeoutMillis = params.timeoutMillis();
        try {
            for (int attempt = 0; attempt < params.maxAttempts(); attempt++) {
                nettyManager.sendUdpMessage(channelId, request);
                try {
                    return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
                } catch (TimeoutException ignored) {
                    // retry immediately
                }
            }
            RpcTimeoutException ex = new RpcTimeoutException("No response after " + params.maxAttempts() + " attempts");

            registry.failRequest(request, ex);
            throw ex;
        } catch (InterruptedException e) {
            RpcTransportException ex = new RpcTransportException("RPC interrupted", e);

            registry.failRequest(request, ex);
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            RpcException ex = mapExecutionException(e);
            registry.failRequest(request, ex);
            throw ex;
        }
    }

    private RpcException mapExecutionException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RpcException rpc) {
            return rpc;
        }
        return new RpcTransportException("RPC failed due to transport error", cause);
    }
}