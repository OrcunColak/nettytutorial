package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.UdpEnvelope;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.DefaultResponseFutureRegistry;
import com.colak.netty.udprpc.response.ExtractingResponseFutureRegistry;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";
        var extractingRegistry = getRegistry();
        var handler = new SolarisRpcInboundHandler(extractingRegistry);

        UdpServerParameters udpServer = UdpServerParameters
                .builder()
                .channelId(channelId)
                .port(12345)
                .inboundHandler(handler)
                .addOutboundEncoder(new SolarisEncoder())
                .build();

        nettyManager.addUdpServer(udpServer);

        var udpBlockingRpcSender = new UdpBlockingRpcSender<>(nettyManager, extractingRegistry);
        SolarisMessage message = new SolarisMessage((short) 1, (short) 2);
        message.setMessageNo((short) 3);
        message.setErrorNo((short) 0);

        try {
            UdpEnvelope<SolarisMessage> envelope = new UdpEnvelope<>(message, new InetSocketAddress("localhost", 54321));
            UdpEnvelope<SolarisMessage> solarisMessage = udpBlockingRpcSender.sendAndAwait(channelId, envelope, BlockingRpcParameters.defaults());
            log.info("SolarisMessage {}", solarisMessage);
        } catch (RpcException | InterruptedException rpcException) {
            log.error("Exception from sendAndAwait", rpcException);
        }
        nettyManager.shutdown();
    }

    private static ExtractingResponseFutureRegistry<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> getRegistry() {
        ResponseFutureRegistry<SolarisKey, UdpEnvelope<SolarisMessage>> baseRegistry = new DefaultResponseFutureRegistry<>();

        CorrelationStrategy<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> keyExtractor = keyExtractor();

        return new ExtractingResponseFutureRegistry<>(baseRegistry, keyExtractor);
    }

    private static CorrelationStrategy<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> keyExtractor() {
        return new CorrelationStrategy<>() {
            @Override
            public SolarisKey fromRequest(UdpEnvelope<SolarisMessage> envelope) {
                SolarisMessage message = envelope.getPayload();
                return new SolarisKey(message.getProtocolNo(), message.getMessageNo(), message.getErrorNo());
            }

            @Override
            public SolarisKey fromResponse(UdpEnvelope<SolarisMessage> envelope) {
                SolarisMessage message = envelope.getPayload();
                return new SolarisKey(message.getProtocolNo(), message.getMessageNo(), message.getErrorNo());
            }
        };
    }
}

