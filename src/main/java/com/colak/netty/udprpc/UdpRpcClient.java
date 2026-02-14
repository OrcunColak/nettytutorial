package com.colak.netty.udprpc;

import com.colak.netty.ChannelSession;
import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.executors.call.DefaultRpcCallExecutor;
import com.colak.netty.udprpc.executors.call.RpcCallExecutor;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.exception.RpcTransportException;
import com.colak.netty.udprpc.executors.fire.DefaultFireAndForgetExecutor;
import com.colak.netty.udprpc.executors.fire.FireAndForgetExecutor;
import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;
import com.colak.netty.udprpc.managednetty.Managed;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
public final class UdpRpcClient {
    private final Managed<NettyManager> nettyResource;
    private final NettyManager nettyManager;
    private final String channelId;
    private final int port;
    private final List<ChannelInboundHandler> inboundDecoders;
    private final List<ChannelInboundHandler> inboundHandlers;
    private final List<ChannelOutboundHandler> outboundEncoders;
    private final RpcResponseInboundHandler rpcResponseHandler;
    private final CorrelationResponseRegistry registry;
    private final CorrelationStrategy correlationStrategy;
    private final int maxAttempts;

    // Executors
    private ChannelSession channelSession;
    private RpcCallExecutor rpcExecutor;
    private FireAndForgetExecutor fireExecutor;

    UdpRpcClient(UdpRpcClientBuilder builder) {
        this.nettyResource = builder.nettyResource;
        this.nettyManager = builder.nettyResource.get();
        this.channelId = builder.channelId;
        this.port = builder.port;
        this.inboundDecoders = builder.inboundDecoders;
        this.inboundHandlers = builder.inboundHandlers;
        this.outboundEncoders = builder.outboundEncoders;
        this.rpcResponseHandler = builder.responseHandler;
        this.registry = builder.registry;
        this.correlationStrategy = builder.correlationStrategy;
        this.maxAttempts = builder.maxAttempts;
    }

    public ChannelSession start() {
        UdpServerParameters rpcServerParameters = UdpServerParameters.builder()
                .channelId(channelId)
                .port(port)
                .addInboundDecoders(inboundDecoders)
                .addInboundHandler(rpcResponseHandler)
                .addInboundHandlers(inboundHandlers)
                .addOutboundEncoders(outboundEncoders)
                .build();
        channelSession = nettyManager.addUdpServer(rpcServerParameters);

        rpcExecutor = new DefaultRpcCallExecutor(channelSession, registry, correlationStrategy);

        fireExecutor = new DefaultFireAndForgetExecutor(channelSession);

        return channelSession;
    }

    public void stop() {
        channelSession.close();
        nettyResource.close();
    }

    public static UdpRpcClientBuilder builder() {
        return new UdpRpcClientBuilder();
    }

    /// Executes an RPC call without expecting a response type
    public void call(Object request, Duration timeout)
            throws RpcException, InterruptedException {
        RpcCallParameters callParams = RpcCallParameters.builder()
                .maxAttempts(maxAttempts)
                .timeout(timeout)
                .build();
        rpcExecutor.executeCall(request, callParams);
        // Ignore the result
    }

    /// Executes an RPC call and waits for a typed response
    public <T> T callForObject(Object request, Duration timeout, Class<T> expectedType)
            throws RpcException, InterruptedException {
        RpcCallParameters callParams = RpcCallParameters.builder()
                .maxAttempts(maxAttempts)
                .timeout(timeout)
                .build();
        Object result = rpcExecutor.executeCall(request, callParams);
        return castResult(result, expectedType);
    }

    /// Executes an RPC call with custom retry and timeout parameters without expecting a response type
    public void call(Object request, RpcCallParameters params)
            throws RpcException, InterruptedException {
        rpcExecutor.executeCall(request, params);
        // Ignore the result
    }

    /// Executes an RPC call with custom retry and timeout parameters and waits for a typed response
    public <T> T callForObject(Object request, RpcCallParameters params, Class<T> expectedType)
            throws RpcException, InterruptedException {
        Object result = rpcExecutor.executeCall(request, params);
        return castResult(result, expectedType);
    }

    /// Sends a request without waiting for a response (fire-and-forget)
    public void fire(Object request) {
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