package com.colak.netty.udp.rpc;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.core.NettyManager;
import com.colak.netty.udp.rpc.builder.UdpRpcClientBuilder;
import com.colak.netty.udp.rpc.exception.RpcException;
import com.colak.netty.udp.rpc.exception.RpcTransportException;
import com.colak.netty.udp.rpc.executors.callexecutor.DefaultRpcCallExecutor;
import com.colak.netty.udp.rpc.executors.callexecutor.RpcCallExecutor;
import com.colak.netty.udp.rpc.executors.fireexecutor.DefaultFireAndForgetExecutor;
import com.colak.netty.udp.rpc.executors.fireexecutor.FireAndForgetExecutor;
import com.colak.netty.udp.rpc.handler.RpcResponseInboundHandler;
import com.colak.netty.udp.rpc.managed.Managed;
import com.colak.netty.udp.rpc.response.CorrelationResponseRegistry;
import com.colak.netty.udp.rpc.response.CorrelationStrategy;
import com.colak.netty.udp.server.UdpServerParameters;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
public final class UdpRpcClient {
    private final Managed<NettyManager> nettyResource;
    private final String channelId;
    private final int port;
    private final List<ChannelInboundHandler> inboundDecoders;
    private final List<ChannelInboundHandler> inboundHandlers;
    private final List<ChannelOutboundHandler> outboundEncoders;
    private final CorrelationResponseRegistry registry;
    private final CorrelationStrategy correlationStrategy;
    private final RpcResponseInboundHandler rpcResponseHandler;
    private final int maxAttempts;

    private ChannelSession channelSession;
    /// Executors
    private RpcCallExecutor rpcExecutor;
    private FireAndForgetExecutor fireExecutor;

    public UdpRpcClient(UdpRpcClientBuilder builder) {
        this.nettyResource = builder.getNettyResource();
        this.channelId = builder.getChannelId();
        this.port = builder.getPort();
        this.inboundDecoders = List.copyOf(builder.getInboundDecoders());
        this.inboundHandlers = List.copyOf(builder.getInboundHandlers());
        this.outboundEncoders = List.copyOf(builder.getOutboundEncoders());
        this.registry = builder.getRegistry();
        this.correlationStrategy = builder.getCorrelationStrategy();
        this.maxAttempts = builder.getMaxAttempts();
        this.rpcResponseHandler = builder.getResponseHandler();
    }

    public boolean start() {
        UdpServerParameters rpcServerParameters = UdpServerParameters.builder()
                .channelId(channelId)
                .port(port)
                .addInboundDecoders(inboundDecoders)
                .addInboundHandler(rpcResponseHandler)
                .addInboundHandlers(inboundHandlers)
                .addOutboundEncoders(outboundEncoders)
                .build();
        NettyManager nettyManager = nettyResource.get();
        channelSession = nettyManager.createUdpServer(rpcServerParameters);

        rpcExecutor = new DefaultRpcCallExecutor(channelSession, registry, correlationStrategy);

        fireExecutor = new DefaultFireAndForgetExecutor(channelSession);

        return channelSession != null;
    }

    // public StreamingUdpRpcClient newStreamClient() {
    //     return new StreamingUdpRpcClient(channelSession, rpcResponseHandler, rpcExecutor);
    // }

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