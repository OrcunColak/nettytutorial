package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.handler.CorrelationKeyExtractor;
import com.colak.netty.udprpc.response.DefaultResponseFutureRegistry;
import com.colak.netty.udprpc.response.ExtractingResponseFutureRegistry;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;

public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";

        ExtractingResponseFutureRegistry<SolarisMessage, SolarisMessage, SolarisKey>
                extractingRegistry = getRegistry();

        SolarisRpcInboundHandler handler =
                new SolarisRpcInboundHandler(extractingRegistry);

        UdpServerParameters udpServer = UdpServerParameters
                .builder()
                .channelId(channelId)
                .port(12345)
                .addInboundDecoder(handler)
                .build();

        nettyManager.addUdpServer(udpServer);
    }

    private static ExtractingResponseFutureRegistry<
            SolarisMessage, SolarisMessage, SolarisKey> getRegistry() {

        ResponseFutureRegistry<SolarisMessage, SolarisKey> baseRegistry =
                new DefaultResponseFutureRegistry<>();

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

