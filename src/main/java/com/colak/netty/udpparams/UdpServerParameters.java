package com.colak.netty.udpparams;


import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public record UdpServerParameters(String channelId, int port, SimpleChannelInboundHandler<DatagramPacket> inboundHandler) {
}
