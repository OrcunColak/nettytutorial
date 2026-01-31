package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UdpRpcClientBuilder {
    // Required fields
    NettyManager nettyManager;
    String channelId;
    Integer port;

    // Optional fields with defaults
    final List<ChannelInboundHandler> inboundDecoders = new ArrayList<>();
    final List<ChannelInboundHandler> inboundHandlers = new ArrayList<>();
    final List<ChannelOutboundHandler> outboundEncoders = new ArrayList<>();
    RpcResponseInboundHandler responseHandler;
    CorrelationResponseRegistry registry;
    CorrelationStrategy correlationStrategy;
    int maxAttempts = 3; // default

    UdpRpcClientBuilder() {
    }

    public UdpRpcClientBuilder nettyManager(NettyManager nettyManager) {
        this.nettyManager = nettyManager;
        return this;
    }

    public UdpRpcClientBuilder channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public UdpRpcClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    public UdpRpcClientBuilder addInboundDecoder(ChannelInboundHandler decoder) {
        this.inboundDecoders.add(decoder);
        return this;
    }

    public UdpRpcClientBuilder addInboundDecoders(List<ChannelInboundHandler> decoders) {
        this.inboundDecoders.addAll(decoders);
        return this;
    }

    public UdpRpcClientBuilder addInboundHandler(ChannelInboundHandler handler) {
        this.inboundHandlers.add(handler);
        return this;
    }

    public UdpRpcClientBuilder addInboundHandlers(List<ChannelInboundHandler> handlers) {
        this.inboundHandlers.addAll(handlers);
        return this;
    }

    public UdpRpcClientBuilder addOutboundEncoder(ChannelOutboundHandler encoder) {
        this.outboundEncoders.add(encoder);
        return this;
    }

    public UdpRpcClientBuilder addOutboundEncoders(List<ChannelOutboundHandler> encoders) {
        this.outboundEncoders.addAll(encoders);
        return this;
    }

    public UdpRpcClientBuilder correlationStrategy(CorrelationStrategy correlationStrategy) {
        this.correlationStrategy = correlationStrategy;
        return this;
    }

    public UdpRpcClientBuilder maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public UdpRpcClient build() {
        // Provide defaults where possible
        if (nettyManager == null) {
            nettyManager = NettyManager.builder()
                    .build();
        }
        if (registry == null) {
            registry = new CorrelationResponseRegistry();
        }
        if (responseHandler == null) {
            responseHandler = new RpcResponseInboundHandler(registry, correlationStrategy);
        }
        validateRequiredFields();
        return new UdpRpcClient(this);
    }

    private void validateRequiredFields() {
        if (channelId == null || channelId.isBlank()) {
            throw new IllegalStateException("channelId must be provided");
        }
        Objects.requireNonNull(port, "port must be provided");
        if (inboundDecoders.isEmpty()) {
            throw new IllegalStateException("inboundDecoders must be provided");
        }
        Objects.requireNonNull(correlationStrategy, "correlationStrategy must be provided");
    }
}


