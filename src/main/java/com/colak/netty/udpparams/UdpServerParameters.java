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

    private final List<ChannelInboundHandler> inboundDecoders;

    private final List<ChannelInboundHandler> inboundHandlers;

    private final List<ChannelOutboundHandler> outboundEncoders;

    private UdpServerParameters(Builder builder) {
        this.channelId = builder.channelId;
        this.port = builder.port;
        this.inboundDecoders = List.copyOf(builder.inboundDecoders);
        this.inboundHandlers = builder.inboundHandlers;
        this.outboundEncoders = List.copyOf(builder.outboundEncoders);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String channelId;
        private Integer port;

        private final List<ChannelInboundHandler> inboundDecoders = new ArrayList<>();
        private final List<ChannelInboundHandler> inboundHandlers = new ArrayList<>();
        private final List<ChannelOutboundHandler> outboundEncoders = new ArrayList<>();

        private Builder() {
        }

        public Builder channelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder addInboundDecoder(ChannelInboundHandler decoder) {
            this.inboundDecoders.add(decoder);
            return this;
        }

        public Builder addInboundDecoders(List<ChannelInboundHandler> decoders) {
            this.inboundDecoders.clear();
            this.inboundDecoders.addAll(decoders);
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


        public UdpServerParameters build() {
            if (channelId == null || channelId.isBlank()) {
                throw new IllegalStateException("channelId must be provided");
            }
            if (port == null) {
                throw new IllegalStateException("port must be provided");
            }
            if (inboundHandlers == null) {
                throw new IllegalStateException("inboundHandler must be provided");
            }
            return new UdpServerParameters(this);
        }
    }
}