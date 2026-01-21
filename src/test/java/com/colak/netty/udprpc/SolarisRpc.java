package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.UdpEnvelope;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";
        var registry = getRegistry();
        var correlationStrategy = correlationStrategy();
        var handler = new SolarisRpcInboundHandler(registry, correlationStrategy);
        UdpServerParameters parameters = UdpServerParameters
                .builder()
                .channelId(channelId)
                .port(12345)
                .inboundHandler(handler)
                .addOutboundEncoder(new SolarisEncoder())
                .build();
        nettyManager.addUdpServer(parameters);

        var rpcClient = UdpRpcClient.<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>>builder()
                .nettyManager(nettyManager)
                .channelId(channelId)
                .registry(registry)
                .correlationStrategy(correlationStrategy)
                .build();

        SolarisMessage message = new SolarisMessage((short) 1, (short) 2);
        message.setMessageNo((short) 3);
        message.setErrorNo((short) 0);

        try {
            UdpEnvelope<SolarisMessage> envelope = new UdpEnvelope<>(message, new InetSocketAddress("localhost", 54321));
            rpcClient.call(envelope, BlockingRpcParameters.defaults());
        } catch (RpcException | InterruptedException rpcException) {
            log.error("Exception from call", rpcException);
        }
        nettyManager.shutdown();
    }

    private static CorrelationResponseRegistry<SolarisKey> getRegistry() {
        return new CorrelationResponseRegistry<>();
    }

    private static CorrelationStrategy<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> correlationStrategy() {
        return new CorrelationStrategy<>() {
            @Override
            public SolarisKey fromRequest(UdpEnvelope<SolarisMessage> envelope) {
                SolarisMessage message = envelope.getPayload();
                return toSolarisKey(message);
            }

            @Override
            public SolarisKey fromResponse(UdpEnvelope<SolarisMessage> envelope) {
                SolarisMessage message = envelope.getPayload();
                return toSolarisKey(message);
            }

            private SolarisKey toSolarisKey(SolarisMessage message) {
                return new SolarisKey(message.getProtocolNo(), message.getMessageNo(), message.getErrorNo());
            }
        };
    }
}