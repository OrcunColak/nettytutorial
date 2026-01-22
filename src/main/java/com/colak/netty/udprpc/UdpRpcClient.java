package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udprpc.callexecutor.DefaultRpcCallExecutor;
import com.colak.netty.udprpc.callexecutor.RpcCallExecutor;
import com.colak.netty.udprpc.callexecutor.RpcCallParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcTransportException;
import com.colak.netty.udprpc.fireexecutor.DefaultFireAndForgetExecutor;
import com.colak.netty.udprpc.fireexecutor.FireAndForgetExecutor;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Objects;

@RequiredArgsConstructor
public final class UdpRpcClient<Key, Req, Res> {
    private final int maxAttempts;
    private final RpcCallExecutor<Req> rpcExecutor;
    private final FireAndForgetExecutor<Req> fireExecutor;

    private UdpRpcClient(Builder<Key, Req, Res> builder) {
        this.maxAttempts = builder.maxAttempts;
        this.rpcExecutor = new DefaultRpcCallExecutor<>(builder.nettyManager, builder.channelId, builder.registry,
                builder.correlationStrategy);
        this.fireExecutor = new DefaultFireAndForgetExecutor<>(builder.nettyManager, builder.channelId);
    }

    public static <Key, Req, Res> Builder<Key, Req, Res> builder() {
        return new Builder<>();
    }

    public static class Builder<Key, Req, Res> {
        private NettyManager nettyManager;
        private String channelId;
        private CorrelationResponseRegistry<Key> registry;
        private CorrelationStrategy<Key, Req, Res> correlationStrategy;
        private int maxAttempts = 3; // default

        private Builder() {
        }

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

        public Builder<Key, Req, Res> maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public UdpRpcClient<Key, Req, Res> build() {
            Objects.requireNonNull(nettyManager, "nettyManager is required");
            Objects.requireNonNull(channelId, "channelId is required");
            Objects.requireNonNull(registry, "registry is required");
            Objects.requireNonNull(correlationStrategy, "correlationStrategy is required");
            return new UdpRpcClient<>(this);
        }
    }

    /// Executes an RPC call and waits for a typed response
    public <T> T call(Req request, Duration timeout, Class<T> expectedType)
            throws RpcException, InterruptedException {
        RpcCallParameters callParams = RpcCallParameters.of(maxAttempts, timeout);
        Object result = rpcExecutor.executeCall(request, callParams);
        return castResult(result, expectedType);
    }

    /// Executes an RPC call without expecting a response type
    public void call(Req request, Duration timeout)
            throws RpcException, InterruptedException {
        RpcCallParameters callParams = RpcCallParameters.of(maxAttempts, timeout);
        rpcExecutor.executeCall(request, callParams);
        // Ignore the result
    }

    /// Executes an RPC call with custom retry and timeout parameters and waits for a typed response
    public <T> T call(Req request, BlockingRpcParameters params, Class<T> expectedType)
            throws RpcException, InterruptedException {
        RpcCallParameters callParams = RpcCallParameters.of(params.maxAttempts(), params.timeout());
        Object result = rpcExecutor.executeCall(request, callParams);
        return castResult(result, expectedType);
    }

    /// Executes an RPC call with custom retry and timeout parameters without expecting a response type
    public void call(Req request, BlockingRpcParameters params)
            throws RpcException, InterruptedException {
        RpcCallParameters callParams = RpcCallParameters.of(params.maxAttempts(), params.timeout());
        rpcExecutor.executeCall(request, callParams);
        // Ignore the result
    }

    /// Sends a request without waiting for a response (fire-and-forget)
    public void fire(Req request) {
        fireExecutor.fire(request);
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
