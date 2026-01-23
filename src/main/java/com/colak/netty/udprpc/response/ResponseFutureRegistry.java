package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry {

    // === Sender side ===
    CompletableFuture<Object> registerRequest(Object key);

    void failRequest(Object key, RpcException exception);

    // === Inbound side ===
    void completeFromResponse(Object key, Object response);

    void failFromResponse(Object key, RpcException exception);
}




