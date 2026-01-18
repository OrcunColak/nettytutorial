package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry<Key, Res> {

    CompletableFuture<Res> register(Key correlationKey);

    void complete(Key correlationKey, Res response);

    void fail(Key correlationKey, RpcException exception);

    void failAll(RpcException exception);
}



