package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CorrelationResponseRegistry implements ResponseFutureRegistry {
    private final ConcurrentMap<Object, CompletableFuture<Object>> pending = new ConcurrentHashMap<>();

    // ===== Sender side =====
    @Override
    public CompletableFuture<Object> registerRequest(Object key) {
        return pending.computeIfAbsent(key, _ -> new CompletableFuture<>());
    }

    @Override
    public void failRequest(Object key, RpcException exception) {
        failByKey(key, exception);
    }

    // ===== Inbound side =====
    @Override
    public void completeFromResponse(Object key, Object response) {
        completeByKey(key, response);
    }

    @Override
    public void failFromResponse(Object key, RpcException exception) {
        failByKey(key, exception);
    }

    // ===== Internal helpers =====
    private void completeByKey(Object key, Object response) {
        CompletableFuture<Object> future = pending.remove(key);
        if (future != null) {
            future.complete(response);
        }
    }

    private void failByKey(Object key, RpcException exception) {
        CompletableFuture<Object> future = pending.remove(key);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }
}