package com.colak.netty.udpparams;


import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class UdpServerParameters {

    private final String channelId;
    private final int port;

    /** Optional inbound transformers (decoders) */
    private final List<ChannelInboundHandler> inboundEncoders;

    /** Required terminal inbound handler */
    private final ChannelInboundHandler inboundHandler;

    /** Optional outbound transformers (encoders) */
    private final List<ChannelOutboundHandler> outboundEncoders;

    private UdpServerParameters(Builder builder) {
        this.channelId = builder.channelId;
        this.port = builder.port;
        this.inboundEncoders = List.copyOf(builder.inboundEncoders);
        this.inboundHandler = builder.inboundHandler;
        this.outboundEncoders = List.copyOf(builder.outboundEncoders);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String channelId;
        private Integer port;

        private final List<ChannelInboundHandler> inboundEncoders = new ArrayList<>();
        private ChannelInboundHandler inboundHandler;
        private final List<ChannelOutboundHandler> outboundEncoders = new ArrayList<>();

        private Builder() {}

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /** Optional inbound transformers (decoders) */
        public Builder addInboundEncoder(ChannelInboundHandler handler) {
            this.inboundEncoders.add(handler);
            return this;
        }

        /** Required final inbound handler */
        public Builder inboundHandler(ChannelInboundHandler handler) {
            this.inboundHandler = handler;
            return this;
        }

        /** Optional outbound transformers (encoders) */
        public Builder addOutboundEncoder(ChannelOutboundHandler handler) {
            this.outboundEncoders.add(handler);
            return this;
        }

        public UdpServerParameters build() {
            if (channelId == null || channelId.isBlank()) {
                throw new IllegalStateException("channelId must be provided");
            }
            if (port == null) {
                throw new IllegalStateException("port must be provided");
            }
            if (inboundHandler == null) {
                throw new IllegalStateException("inboundHandler must be provided");
            }
            return new UdpServerParameters(this);
        }
    }
}

