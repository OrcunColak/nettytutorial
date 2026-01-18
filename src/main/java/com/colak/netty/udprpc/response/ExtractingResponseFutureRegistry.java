package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/// Key - correlation key
/// Res - response
public final class ExtractingResponseFutureRegistry<Key, Req, Res>
        implements ResponseFutureRegistry<Req, Res> {

    private final ConcurrentMap<Key, CompletableFuture<Res>> pending = new ConcurrentHashMap<>();

    private final CorrelationStrategy<Key, Req, Res> correlationStrategy;

    public ExtractingResponseFutureRegistry(CorrelationStrategy<Key, Req, Res> correlationStrategy) {
        this.correlationStrategy = correlationStrategy;
    }

    // ===== Sender side =====

    @Override
    public CompletableFuture<Res> registerRequest(Req request) {
        Key key = correlationStrategy.fromRequest(request);
        if (key == null) {
            throw new IllegalArgumentException("Cannot extract correlation key from request");
        }
        return pending.computeIfAbsent(key, k -> new CompletableFuture<>());
    }

    @Override
    public void failRequest(Req request, RpcException exception) {
        Key key = correlationStrategy.fromRequest(request);
        if (key != null) {
            failByKey(key, exception);
        }
    }

    // ===== Inbound side =====

    @Override
    public void completeFromResponse(Res response) {
        Key key = correlationStrategy.fromResponse(response);
        if (key != null) {
            completeByKey(key, response);
        }
    }

    @Override
    public void failFromResponse(Res response, RpcException exception) {
        Key key = correlationStrategy.fromResponse(response);
        if (key != null) {
            failByKey(key, exception);
        }
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
