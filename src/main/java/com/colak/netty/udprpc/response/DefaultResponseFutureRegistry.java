package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultResponseFutureRegistry<KEY, RES>
        implements ResponseFutureRegistry<KEY, RES> {

    private final ConcurrentMap<KEY, CompletableFuture<RES>> pending = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<RES> register(KEY key) {
        return pending.computeIfAbsent(key, k -> new CompletableFuture<>());
    }

    @Override
    public void complete(KEY key, RES response) {
        CompletableFuture<RES> future = pending.remove(key);
        if (future != null) {
            future.complete(response);
        }
    }

    @Override
    public void fail(KEY key, RpcException exception) {
        CompletableFuture<RES> future = pending.remove(key);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }

    @Override
    public void failAll(RpcException exception) {
        pending.forEach((k, f) -> f.completeExceptionally(exception));
        pending.clear();
    }
}