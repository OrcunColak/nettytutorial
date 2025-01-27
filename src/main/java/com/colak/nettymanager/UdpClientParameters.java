package com.colak.nettymanager;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public record UdpClientParameters(String channelId, SimpleChannelInboundHandler<DatagramPacket> inboundHandler) {
}
