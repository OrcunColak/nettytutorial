package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

/// Key - correlation key
/// Res - response
public interface ResponseFutureRegistry<Req, Res> {

    // === Sender side ===
    CompletableFuture<Res> registerRequest(Req request);

    void failRequest(Req request, RpcException exception);

    // === Inbound side ===
    void completeFromResponse(Res response);

    void failFromResponse(Res response, RpcException exception);
}




