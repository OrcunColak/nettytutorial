package com.colak.netty.udprpc.callexecutor;

import com.colak.netty.NettyManager;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcTimeoutException;
import com.colak.netty.udprpc.exception.RpcTransportException;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public final class DefaultRpcCallExecutor implements RpcCallExecutor {
    private final NettyManager nettyManager;
    private final String channelId;
    private final CorrelationResponseRegistry registry;
    private final CorrelationStrategy correlationStrategy;

    @Override
    public Object executeCall(Object request, RpcCallParameters params) throws RpcException, InterruptedException {
        Object key = correlationStrategy.fromRequest(request);
        CompletableFuture<?> future = registerRequest(key);
        try {
            return executeWithRetries(request, params, future, key);
        } catch (InterruptedException e) {
            RpcTransportException rpcException = new RpcTransportException("RPC interrupted", e);

            failRequest(key, rpcException);
            Thread.currentThread().interrupt();
            // rethrow original InterruptedException
            throw e;
        } catch (ExecutionException e) {
            RpcException rpcException = mapExecutionException(e);

            failRequest(key, rpcException);
            throw rpcException;
        } catch (Exception e) {
            RpcTransportException rpcException = new RpcTransportException("RPC failed", e);

            failRequest(key, rpcException);
            throw rpcException;
        }
    }

    private Object executeWithRetries(Object request, RpcCallParameters params,
                                      CompletableFuture<?> future, Object key)
            throws RpcTimeoutException, ExecutionException, InterruptedException {
        int attemptLimit = params.attemptLimit();
        long timeoutMillis = params.timeoutMillis();
        for (int attempt = 0; attempt < attemptLimit; attempt++) {
            nettyManager.sendUdpMessage(channelId, request);
            try {
                return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ignored) {
                // retry immediately
            }
        }
        RpcTimeoutException rpcException = new RpcTimeoutException("No response after " + attemptLimit + " attempts");

        registry.failRequest(key, rpcException);
        throw rpcException;
    }

    private CompletableFuture<?> registerRequest(Object key) {
        return registry.registerRequest(key);
    }

    private void failRequest(Object key, RpcException rpcException) {
        registry.failRequest(key, rpcException);
    }

    private RpcException mapExecutionException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RpcException rpc) {
            return rpc;
        }
        return new RpcTransportException("RPC failed due to transport error", cause);
    }
}