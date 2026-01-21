package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry<Key> {

    // === Sender side ===
    <T> CompletableFuture<T> registerRequest(Key key);

    void failRequest(Key key, RpcException exception);

    // === Inbound side ===
    <T> void completeFromResponse(Key key, T response);

    void failFromResponse(Key key, RpcException exception);
}




