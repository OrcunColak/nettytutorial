package com.colak.netty.udprpc.handler;

import com.colak.netty.udprpc.exception.RpcPeerException;

// Interface that users must implement
public interface RpcResponseHandler<CorrelationId, Request,Response> {
    boolean isErrorResponse(Response response);
    RpcPeerException toPeerException(Response response);
}