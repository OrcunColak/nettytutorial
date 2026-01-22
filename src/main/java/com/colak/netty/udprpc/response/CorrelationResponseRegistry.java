package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CorrelationResponseRegistry implements ResponseFutureRegistry {

    private final ConcurrentMap<Object, CompletableFuture<?>> pending = new ConcurrentHashMap<>();

    // ===== Sender side =====
    @Override
    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<T> registerRequest(Object key) {
        return (CompletableFuture<T>) pending.computeIfAbsent(key, _ -> new CompletableFuture<>());
    }

    @Override
    public void failRequest(Object key, RpcException exception) {
        failByKey(key, exception);
    }

    // ===== Inbound side =====
    @Override
    public <T> void completeFromResponse(Object key, T response) {
        completeByKey(key, response);
    }

    @Override
    public void failFromResponse(Object key, RpcException exception) {
        failByKey(key, exception);
    }

    // ===== Internal helpers =====
    @SuppressWarnings("unchecked")
    private <T> void completeByKey(Object key, T response) {
        CompletableFuture<T> future = (CompletableFuture<T>) pending.remove(key);
        if (future != null) {
            future.complete(response);
        }
    }

    private void failByKey(Object key, RpcException exception) {
        CompletableFuture<?> future = pending.remove(key);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }
}