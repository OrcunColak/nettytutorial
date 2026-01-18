package com.colak.netty.udprpc;

import com.colak.netty.UdpEnvelope;
import com.colak.netty.udprpc.handler.AbstractRpcResponseInboundHandler;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;

public class SolarisRpcInboundHandler extends AbstractRpcResponseInboundHandler<SolarisKey, UdpEnvelope<SolarisMessage>> {

    protected SolarisRpcInboundHandler(ResponseFutureRegistry<SolarisKey, UdpEnvelope<SolarisMessage>> registry) {
        super(registry);
    }

    @Override
    protected SolarisKey extractKey(UdpEnvelope<SolarisMessage> envelope) {
        SolarisMessage message = envelope.getPayload();
        return new SolarisKey(message.getProtocolNo(), message.getSubType(), message.getMessageNo());
    }

    @Override
    protected boolean isErrorResponse(UdpEnvelope<SolarisMessage> envelope) {
        return false;
    }
}
