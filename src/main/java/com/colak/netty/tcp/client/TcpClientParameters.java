package com.colak.netty.tcp.client;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class TcpClientParameters {
    private final String channelId;
    private final String host;
    private final int port;
    private final List<ChannelInboundHandler> inboundDecoders;
    private final List<ChannelInboundHandler> inboundHandlers;
    private final List<ChannelOutboundHandler> outboundEncoders;
    private final long connectTimeoutMs;
    private final boolean autoReconnect;
    private final long reconnectInitialBackoffMs;
    private final long reconnectBackoffIncrementsMs;
    private final long reconnectMaxBackoffMs;
    private final Integer reconnectMaxRetries;

    private TcpClientParameters(Builder builder) {
        this.channelId = builder.channelId;
        this.host = builder.host;
        this.port = builder.port;
        this.inboundDecoders = List.copyOf(builder.inboundDecoders);
        this.inboundHandlers = List.copyOf(builder.inboundHandlers);
        this.outboundEncoders = List.copyOf(builder.outboundEncoders);
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.autoReconnect = builder.autoReconnect;
        this.reconnectInitialBackoffMs = builder.reconnectInitialBackoff.toMillis();
        this.reconnectBackoffIncrementsMs = builder.reconnectBackoffIncrements.toMillis();
        this.reconnectMaxBackoffMs = builder.reconnectMaxBackoff.toMillis();
        this.reconnectMaxRetries = builder.reconnectMaxRetries;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String channelId;
        private String host;
        private Integer port;
        private final List<ChannelInboundHandler> inboundDecoders = new ArrayList<>();
        private final List<ChannelInboundHandler> inboundHandlers = new ArrayList<>();
        private final List<ChannelOutboundHandler> outboundEncoders = new ArrayList<>();
        private long connectTimeoutMs = Duration.ofSeconds(5).toMillis();
        private boolean autoReconnect = false;
        private Duration reconnectInitialBackoff = Duration.ofSeconds(1);
        private Duration reconnectBackoffIncrements = Duration.ofSeconds(1);
        private Duration reconnectMaxBackoff = Duration.ofSeconds(30);
        private Integer reconnectMaxRetries = null;

        private Builder() {
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder addInboundDecoder(ChannelInboundHandler handler) {
            this.inboundDecoders.add(handler);
            return this;
        }

        public Builder addInboundDecoders(List<ChannelInboundHandler> handlers) {
            this.inboundDecoders.addAll(handlers);
            return this;
        }

        public Builder addInboundHandler(ChannelInboundHandler handler) {
            this.inboundHandlers.add(handler);
            return this;
        }

        public Builder addInboundHandlers(List<ChannelInboundHandler> handlers) {
            this.inboundHandlers.addAll(handlers);
            return this;
        }

        public Builder addOutboundEncoder(ChannelOutboundHandler handler) {
            this.outboundEncoders.add(handler);
            return this;
        }

        public Builder addOutboundEncoders(List<ChannelOutboundHandler> handlers) {
            this.outboundEncoders.addAll(handlers);
            return this;
        }

        public Builder connectTimeoutMs(long connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder autoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        public Builder reconnectInitialBackoff(Duration reconnectInitialBackoff) {
            this.reconnectInitialBackoff = reconnectInitialBackoff;
            return this;
        }

        public Builder reconnectBackoffIncrements(Duration reconnectBackoffIncrements) {
            this.reconnectBackoffIncrements = reconnectBackoffIncrements;
            return this;
        }

        public Builder reconnectMaxBackoff(Duration reconnectMaxBackoff) {
            this.reconnectMaxBackoff = reconnectMaxBackoff;
            return this;
        }

        public Builder reconnectMaxRetries(Integer reconnectMaxRetries) {
            this.reconnectMaxRetries = reconnectMaxRetries;
            return this;
        }

        public TcpClientParameters build() {
            if (channelId == null || channelId.isBlank()) {
                throw new IllegalArgumentException("channelId must be provided");
            }
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("host must be provided");
            }
            Objects.requireNonNull(port, "port must be provided");
            if (inboundHandlers.isEmpty()) {
                throw new IllegalArgumentException("at least one inboundHandler must be provided");
            }
            return new TcpClientParameters(this);
        }
    }
}
