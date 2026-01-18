package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultResponseFutureRegistry<Res, K>
        implements ResponseFutureRegistry<Res, K> {

    private final ConcurrentMap<K, CompletableFuture<Res>> pending = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Res> register(K correlationKey) {
        return pending.computeIfAbsent(correlationKey, k -> new CompletableFuture<>());
    }

    @Override
    public void complete(K correlationKey, Res response) {
        CompletableFuture<Res> future = pending.remove(correlationKey);
        if (future != null) {
            future.complete(response);
        }
    }

    @Override
    public void fail(K correlationKey, RpcException exception) {
        CompletableFuture<Res> future = pending.remove(correlationKey);
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
