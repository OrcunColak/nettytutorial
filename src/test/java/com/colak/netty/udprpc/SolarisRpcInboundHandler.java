package com.colak.netty.udprpc;

import com.colak.netty.UdpEnvelope;
import com.colak.netty.udprpc.exception.RpcPeerException;
import com.colak.netty.udprpc.handler.RpcResponseHandler;

public class SolarisRpcInboundHandler implements RpcResponseHandler<SolarisKey, UdpEnvelope<SolarisMessage>,UdpEnvelope<SolarisMessage>> {

    @Override
    public boolean isErrorResponse(UdpEnvelope<SolarisMessage> envelope) {
        return false;
    }

    @Override
    public RpcPeerException toPeerException(UdpEnvelope<SolarisMessage> envelope) {
        return null;
    }
}
