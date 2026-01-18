package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// Key - correlation key
/// Res - response
public class DefaultResponseFutureRegistry<Key, Res>
        implements ResponseFutureRegistry<Key, Res> {

    private final ConcurrentMap<Key, CompletableFuture<Res>> pending = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Res> register(Key correlationKey) {
        return pending.computeIfAbsent(correlationKey, k -> new CompletableFuture<>());
    }

    @Override
    public void complete(Key correlationKey, Res response) {
        CompletableFuture<Res> future = pending.remove(correlationKey);
        if (future != null) {
            future.complete(response);
        }
    }

    @Override
    public void fail(Key correlationKey, RpcException exception) {
        CompletableFuture<Res> future = pending.remove(correlationKey);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }
}
