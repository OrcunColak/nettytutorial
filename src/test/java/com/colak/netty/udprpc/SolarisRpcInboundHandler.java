package com.colak.netty.udprpc;

import com.colak.netty.UdpEnvelope;
import com.colak.netty.udprpc.handler.AbstractRpcResponseInboundHandler;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;

public class SolarisRpcInboundHandler extends AbstractRpcResponseInboundHandler<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> {

    protected SolarisRpcInboundHandler(ResponseFutureRegistry<SolarisKey, UdpEnvelope<SolarisMessage>> registry,
                                       CorrelationStrategy<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> correlationStrategy) {
        super(registry, correlationStrategy);
    }

    @Override
    protected boolean isErrorResponse(UdpEnvelope<SolarisMessage> envelope) {
        return false;
    }
}
