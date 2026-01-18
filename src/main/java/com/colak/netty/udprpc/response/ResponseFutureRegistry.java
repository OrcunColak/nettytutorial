package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry<Res, K> {

    CompletableFuture<Res> register(K correlationKey);

    void complete(K correlationKey, Res response);

    void fail(K correlationKey, RpcException exception);

    void failAll(RpcException exception);
}



