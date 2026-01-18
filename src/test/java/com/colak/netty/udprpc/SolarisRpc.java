package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.response.DefaultResponseFutureRegistry;
import com.colak.netty.udprpc.response.ExtractingResponseFutureRegistry;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;

public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";

        DefaultResponseFutureRegistry<SolarisKey, SolarisMessage> defaultRegistry = new DefaultResponseFutureRegistry<>();
        ResponseFutureRegistry<SolarisKey, SolarisMessage> registry =
                new ExtractingResponseFutureRegistry<SolarisKey, SolarisMessage>(
                new DefaultResponseFutureRegistry<SolarisKey, SolarisMessage>());
        SolarisRpcInboundHandler handler = new SolarisRpcInboundHandler();
        UdpServerParameters udpServer = UdpServerParameters
                .builder()
                .channelId(channelId)
                .port(12345)
                .addInboundDecoder(handler)
                .build();
        nettyManager.addUdpServer(udpServer);
    }
}
