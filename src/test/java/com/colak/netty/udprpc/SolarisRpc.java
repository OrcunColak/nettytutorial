package com.colak.netty.udprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;

public class SolarisRpc {

    static void main() {
        NettyManager nettyManager = NettyManager.newSingleThreadWorker();
        String channelId = "solaris-channel";
        UdpServerParameters udpServer = UdpServerParameters
                .builder()
                .channelId(channelId)
                .port(12345)
                .addInboundDecoder(null)
                .build();
        nettyManager.addUdpServer(udpServer);
    }
}
