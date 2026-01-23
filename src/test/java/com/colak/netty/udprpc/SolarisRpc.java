package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.UdpEnvelope;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.time.Duration;

@Slf4j
public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";
        var correlationStrategy = correlationStrategy();

        UdpServerParameters remoteServer = UdpServerParameters
                .builder()
                .channelId("server-channel")
                .port(54321)
                .addInboundHandler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
                        DatagramPacket datagramPacket = new DatagramPacket(
                                packet.content().retainedDuplicate(),
                                packet.sender(),
                                packet.recipient()
                        );
                        ctx.writeAndFlush(datagramPacket);

                    }
                })
                .addOutboundEncoder(new SolarisEncoder())
                .build();
        nettyManager.addUdpServer(remoteServer);

        var rpcClient = UdpRpcClient.builder()
                .nettyManager(nettyManager)
                .channelId(channelId)
                .port(12345)
                .addInboundDecoder(new SolarisDecoder())
                .addOutboundEncoder(new SolarisEncoder())
                .correlationStrategy(correlationStrategy)
                .maxAttempts(1)
                .build();
        rpcClient.start();

        SolarisMessage message = new SolarisMessage((short) 1, (short) 2);
        message.setMessageNo((short) 3);
        message.setErrorNo((short) 0);

        try {
            UdpEnvelope<SolarisMessage> envelope = new UdpEnvelope<>(message, new InetSocketAddress("localhost", 54321));
            rpcClient.call(envelope, Duration.ofSeconds(10));
        } catch (RpcException | InterruptedException rpcException) {
            log.error("Exception from call", rpcException);
        }
        nettyManager.shutdown();
    }

    private static CorrelationStrategy correlationStrategy() {
        return new CorrelationStrategy() {
            @Override
            public SolarisKey fromRequest(Object request) {
                return new SolarisKey(1, 2, 3);
            }

            @Override
            public SolarisKey fromResponse(Object response) {
                return new SolarisKey(1, 2, 3);
            }
        };
    }
}