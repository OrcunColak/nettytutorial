package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

/// Key - correlation key
/// Res - response
public interface ResponseFutureRegistry<Key, Res> {

    // === Sender side ===
    CompletableFuture<Res> registerRequest(Key key);

    void failRequest(Key key, RpcException exception);

    // === Inbound side ===
    void completeFromResponse(Key key, Res response);

    void failFromResponse(Key key, RpcException exception);
}




