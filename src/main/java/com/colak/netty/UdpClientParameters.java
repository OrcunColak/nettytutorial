package com.colak.netty;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UdpClientParameters {

    private final String channelId;

    // inboundHandler is optional for a UDP client that do not read any data
    private SimpleChannelInboundHandler<DatagramPacket> inboundHandler;

    // broadcast is optional
    private boolean broadcast;

    public UdpClientParameters(String channelId) {
        this.channelId = channelId;
    }
}