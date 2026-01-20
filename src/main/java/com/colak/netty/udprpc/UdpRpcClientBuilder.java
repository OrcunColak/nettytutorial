package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.handler.DelegatingRpcInboundHandler;
import com.colak.netty.udprpc.handler.RpcResponseHandler;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class UdpRpcClientBuilder<CorrelationId, Request, Response> {
    private NettyManager nettyManager;
    private String channelId;
    private int port;
    private final List<ChannelOutboundHandler> outboundEncoders = new ArrayList<>();
    private final List<ChannelInboundHandler> inboundDecoders = new ArrayList<>();
    private final CorrelationResponseRegistry<CorrelationId, Response> registry = new CorrelationResponseRegistry<>();
    private CorrelationStrategy<CorrelationId, Request, Response> correlationStrategy;
    private RpcResponseHandler<CorrelationId, Request, Response> responseHandler;
    private Duration defaultTimeout = Duration.ofSeconds(5);
    private int defaultMaxAttempts = 3;

    public static <CorrelationId, Request, Response> UdpRpcClientBuilder<CorrelationId, Request, Response> create() {
        return new UdpRpcClientBuilder<>();
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> withNettyManager(NettyManager nettyManager) {
        this.nettyManager = nettyManager;
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> withChannel(String channelId, int port) {
        this.channelId = channelId;
        this.port = port;
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> addOutboundEncoder(ChannelOutboundHandler encoder) {
        this.outboundEncoders.add(encoder);
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> addInboundDecoder(ChannelInboundHandler decoder) {
        this.inboundDecoders.add(decoder);
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> withCorrelationStrategy(
            CorrelationStrategy<CorrelationId, Request, Response> strategy) {
        this.correlationStrategy = strategy;
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> withResponseHandler(
            RpcResponseHandler<CorrelationId, Request, Response> responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> withTimeout(Duration timeout) {
        this.defaultTimeout = timeout;
        return this;
    }

    public UdpRpcClientBuilder<CorrelationId, Request, Response> withMaxAttempts(int maxAttempts) {
        this.defaultMaxAttempts = maxAttempts;
        return this;
    }

    public SimpleRpcClient<CorrelationId, Request, Response> build() {
        validate();

        ChannelInboundHandler pipelineHandler = createPipelineHandler();

        // Build complete UdpServerParameters
        UdpServerParameters udpServerParameters = UdpServerParameters.builder()
                .channelId(channelId)
                .port(port)
                .inboundDecoders(inboundDecoders)
                .outboundEncoders(outboundEncoders)
                .inboundHandler(pipelineHandler)
                .build();

        nettyManager.addUdpServer(udpServerParameters);

        // Create sender
        var udpRpcClient = new UdpRpcClient<>(
                nettyManager,
                channelId,
                registry,
                correlationStrategy
        );
        return new SimpleRpcClient<>(udpRpcClient, defaultMaxAttempts, defaultTimeout);
    }

    private ChannelInboundHandler createPipelineHandler() {
        return new DelegatingRpcInboundHandler<>(
                registry,
                correlationStrategy,
                responseHandler);
    }

    private void validate() {
        if (nettyManager == null) {
            throw new IllegalStateException("NettyManager must be set");
        }
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalStateException("Channel ID must be set");
        }
        if (port <= 0) {
            throw new IllegalStateException("Port must be positive");
        }
        if (correlationStrategy == null) {
            throw new IllegalStateException("CorrelationStrategy must be set");
        }
        if (responseHandler == null) {
            throw new IllegalStateException("Handler must be set");
        }
    }
}