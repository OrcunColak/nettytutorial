package com.colak.netty.udp.rpc.response;

import com.colak.netty.udp.rpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

/// Key - correlation key
public interface ResponseFutureRegistry {

    /// === Sender side ===
    CompletableFuture<Object> registerRequest(Object key);

    void failRequest(Object key, RpcException exception);

    /// === Inbound side ===
    void completeFromResponse(Object key, Object response);

    void failFromResponse(Object key, RpcException exception);
}




