package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry<CorrelationId, Response> {

    // === Sender side ===
    CompletableFuture<Response> registerRequest(CorrelationId correlationId);

    void failRequest(CorrelationId correlationId, RpcException exception);

    // === Inbound side ===
    void completeFromResponse(CorrelationId correlationId, Response response);

    void failFromResponse(CorrelationId correlationId, RpcException exception);
}




