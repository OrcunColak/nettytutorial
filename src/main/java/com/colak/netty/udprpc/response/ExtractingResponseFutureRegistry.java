package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// Key - correlation key
/// Res - response
// Key - correlation key

/// Res - response
public final class ExtractingResponseFutureRegistry<Key, Res>
        implements ResponseFutureRegistry<Key, Res> {

    private final ConcurrentMap<Key, CompletableFuture<Res>> pending = new ConcurrentHashMap<>();

    // ===== Sender side =====
    @Override
    public CompletableFuture<Res> registerRequest(Key key) {
        return pending.computeIfAbsent(key, _ -> new CompletableFuture<>());
    }

    @Override
    public void failRequest(Key key, RpcException exception) {
        failByKey(key, exception);
    }

    // ===== Inbound side =====
    @Override
    public void completeFromResponse(Key key, Res response) {
        completeByKey(key, response);
    }

    @Override
    public void failFromResponse(Key key, RpcException exception) {
        failByKey(key, exception);
    }

    // ===== Internal helpers =====
    private void completeByKey(Key key, Res response) {
        CompletableFuture<Res> future = pending.remove(key);
        if (future != null) {
            future.complete(response);
        }
    }

    private void failByKey(Key key, RpcException exception) {
        CompletableFuture<Res> future = pending.remove(key);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }
}