package com.colak.netty.udprpc;

import com.colak.netty.udprpc.handler.AbstractRpcResponseInboundHandler;
import com.colak.netty.udprpc.response.ResponseFutureRegistry;

public class SolarisRpcInboundHandler extends AbstractRpcResponseInboundHandler<SolarisMessage,SolarisKey> {

    protected SolarisRpcInboundHandler(ResponseFutureRegistry<SolarisKey,SolarisMessage> registry) {
        super(registry);
    }

    @Override
    protected SolarisKey extractKey(SolarisMessage message) {
        return new SolarisKey(message.getProtocolNo(), message.getSubType(), message.getMessageNo());
    }

    @Override
    protected boolean isErrorResponse(SolarisMessage response) {
        return false;
    }
}
