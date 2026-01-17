package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

public interface ResponseFutureRegistry<KEY, RES> {

    CompletableFuture<RES> register(KEY key);

    void complete(KEY key, RES response);

    void fail(KEY key, RpcException exception);

    void failAll(RpcException exception);
}


