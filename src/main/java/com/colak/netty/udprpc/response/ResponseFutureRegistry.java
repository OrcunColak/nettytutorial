package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry {

    // === Sender side ===
    <T> CompletableFuture<T> registerRequest(Object key);

    void failRequest(Object key, RpcException exception);

    // === Inbound side ===
    <T> void completeFromResponse(Object key, T response);

    void failFromResponse(Object key, RpcException exception);
}




