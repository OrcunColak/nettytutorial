package com.colak.netty.udpparams;


import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UdpServerParameters {
    private String channelId;
    private int port;
    private SimpleChannelInboundHandler<DatagramPacket> inboundHandler;
}
