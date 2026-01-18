package com.colak.netty.udprpc;

import com.colak.netty.UdpEnvelope;
import com.colak.netty.udprpc.handler.AbstractRpcResponseInboundHandler;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;

public class SolarisRpcInboundHandler extends AbstractRpcResponseInboundHandler<UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> {

    protected SolarisRpcInboundHandler(ResponseFutureRegistry<UdpEnvelope<SolarisMessage>,
                UdpEnvelope<SolarisMessage>> registry) {
        super(registry);
    }

    @Override
    protected boolean isErrorResponse(UdpEnvelope<SolarisMessage> envelope) {
        return false;
    }
}
