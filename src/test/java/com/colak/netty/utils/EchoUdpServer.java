package com.colak.netty.utils;

import com.colak.netty.NettyManager;
import com.colak.netty.UdpServerParameters;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
@RequiredArgsConstructor
public class EchoUdpServer {

    private final NettyManager nettyManager;

    private int port;

    public void start() {
        String channelId = "echoServer";
        port = PortUtil.findAvailableUdpPort();

        // Create echo handler for UDP server
        SimpleChannelInboundHandler<DatagramPacket> echoHandler = new SimpleChannelInboundHandler<>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
                // Create echo packet
                DatagramPacket echoPacket = new DatagramPacket(
                        datagramPacket.content().retain(),
                        datagramPacket.sender());
                ctx.writeAndFlush(echoPacket);
            }
        };

        // Create UDP server
        boolean result = nettyManager.addUdpServer(new UdpServerParameters(channelId, port, echoHandler));
        assertTrue(result, "Echo server did not start successfully");
    }
}
