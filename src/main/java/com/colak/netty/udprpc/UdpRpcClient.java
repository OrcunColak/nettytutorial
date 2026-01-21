package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcTimeoutException;
import com.colak.netty.udprpc.exception.RpcTransportException;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public final class UdpRpcClient<Key, Req, Res> {
    private final NettyManager nettyManager;
    private final String channelId;
    private final CorrelationResponseRegistry<Key> registry;
    private final CorrelationStrategy<Key, Req, Res> correlationStrategy;

    private UdpRpcClient(Builder<Key, Req, Res> builder) {
        this.nettyManager = builder.nettyManager;
        this.channelId = builder.channelId;
        this.registry = builder.registry;
        this.correlationStrategy = builder.correlationStrategy;
    }

    public static <Key, Req, Res> Builder<Key, Req, Res> builder() {
        return new Builder<>();
    }

    public static class Builder<Key, Req, Res> {
        private NettyManager nettyManager;
        private String channelId;
        private CorrelationResponseRegistry<Key> registry;
        private CorrelationStrategy<Key, Req, Res> correlationStrategy;


        public Builder<Key, Req, Res> nettyManager(NettyManager nettyManager) {
            this.nettyManager = nettyManager;
            return this;
        }

        public Builder<Key, Req, Res> channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder<Key, Req, Res> registry(CorrelationResponseRegistry<Key> registry) {
            this.registry = registry;
            return this;
        }

        public Builder<Key, Req, Res> correlationStrategy(CorrelationStrategy<Key, Req, Res> correlationStrategy) {
            this.correlationStrategy = correlationStrategy;
            return this;
        }

        public UdpRpcClient<Key, Req, Res> build() {
            Objects.requireNonNull(nettyManager, "nettyManager is required");
            Objects.requireNonNull(channelId, "channelId is required");
            Objects.requireNonNull(registry, "registry is required");
            Objects.requireNonNull(correlationStrategy, "correlationStrategy is required");
            return new UdpRpcClient<>(nettyManager, channelId, registry, correlationStrategy);
        }
    }

    public <T> T call(Req request, BlockingRpcParameters params, Class<T> expectedType)
            throws RpcException, InterruptedException {
        Object result = executeCall(request, params);
        return castResult(result, expectedType);
    }

    public void call(Req request, BlockingRpcParameters params)
            throws RpcException, InterruptedException {
        executeCall(request, params);
        // Ignore the result
    }

    public Object executeCall(Req request, BlockingRpcParameters params)
            throws RpcException, InterruptedException {
        Key key = correlationStrategy.fromRequest(request);
        CompletableFuture<?> future = registry.registerRequest(key);
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

            registry.failRequest(key, ex);
            throw ex;
        } catch (InterruptedException e) {
            RpcTransportException ex = new RpcTransportException("RPC interrupted", e);

            registry.failRequest(key, ex);
            Thread.currentThread().interrupt();
            throw e;
        } catch (ExecutionException e) {
            RpcException ex = mapExecutionException(e);
            registry.failRequest(key, ex);
            throw ex;
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

    private RpcException mapExecutionException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RpcException rpc) {
            return rpc;
        }
        return new RpcTransportException("RPC failed due to transport error", cause);
    }
}