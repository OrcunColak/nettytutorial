package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.handler.CorrelationKeyExtractor;
import com.colak.netty.udprpc.response.DefaultResponseFutureRegistry;
import com.colak.netty.udprpc.response.ExtractingResponseFutureRegistry;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;
import lombok.extern.slf4j.Slf4j;

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
            SolarisMessage solarisMessage = udpBlockingRpcSender.sendAndAwait(channelId, message, BlockingRpcParameters.defaults());
            log.info("SolarisMessage {}", solarisMessage);
        } catch (RpcException | InterruptedException rpcException) {
            log.error("Exception from sendAndAwait", rpcException);
        }
        nettyManager.shutdown();
    }

    private static ExtractingResponseFutureRegistry<
            SolarisKey, SolarisMessage, SolarisMessage> getRegistry() {

        ResponseFutureRegistry<SolarisKey, SolarisMessage> baseRegistry = new DefaultResponseFutureRegistry<>();

        CorrelationKeyExtractor<SolarisMessage, SolarisKey> keyExtractor =
                message -> new SolarisKey(
                        message.getProtocolNo(),
                        message.getSubType(),
                        message.getMessageNo()
                );

        return new ExtractingResponseFutureRegistry<>(
                baseRegistry,
                keyExtractor,
                keyExtractor
        );
    }
}

