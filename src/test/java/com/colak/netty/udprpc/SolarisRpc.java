package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.UdpEnvelope;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.time.Duration;

@Slf4j
public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";
        var registry = getRegistry();
        var correlationStrategy = correlationStrategy();
        var handler = new SolarisRpcInboundHandler(registry, correlationStrategy);
        UdpServerParameters rpcServer = UdpServerParameters
                .builder()
                .channelId(channelId)
                .port(12345)
                .addInboundDecoder(new RpcResponseInboundHandler(registry, correlationStrategy))
                .inboundHandler(handler)
                .addOutboundEncoder(new SolarisEncoder())
                .build();
        nettyManager.addUdpServer(rpcServer);

        UdpServerParameters remoteServer = UdpServerParameters
                .builder()
                .channelId("server-channel")
                .port(54321)
                .inboundHandler(new SimpleChannelInboundHandler<DatagramPacket>() {
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
                .registry(registry)
                .correlationStrategy(correlationStrategy)
                .maxAttempts(1)
                .build();

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

    private static CorrelationResponseRegistry getRegistry() {
        return new CorrelationResponseRegistry();
    }

    private static CorrelationStrategy correlationStrategy() {
        return new CorrelationStrategy() {
            @Override
            public SolarisKey fromRequest(Object request) {
                return new SolarisKey((short) 1, (short) 2, (short) 3);
                // if (request instanceof UdpEnvelope<?> envelope) {
                //     if (envelope.getPayload() instanceof SolarisMessage solarisMessage) {
                //         return toSolarisKey(solarisMessage);
                //     }
                // }
                // throw new IllegalArgumentException("Invalid request");
            }

            @Override
            public SolarisKey fromResponse(Object response) {
                return new SolarisKey((short) 1, (short) 2, (short) 3);
                // if (response instanceof UdpEnvelope<?> envelope) {
                //     if (envelope.getPayload() instanceof SolarisMessage solarisMessage) {
                //         return toSolarisKey(solarisMessage);
                //     }
                // }
                // throw new IllegalArgumentException("Invalid request");
            }

            private SolarisKey toSolarisKey(SolarisMessage message) {
                return new SolarisKey(message.getProtocolNo(), message.getMessageNo(), message.getErrorNo());
            }
        };
    }
}