package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CorrelationResponseRegistry<CorrelationId, Response>
        implements ResponseFutureRegistry<CorrelationId, Response> {

    private final ConcurrentMap<CorrelationId, CompletableFuture<Response>> pending = new ConcurrentHashMap<>();

    // ===== Sender side =====
    @Override
    public CompletableFuture<Response> registerRequest(CorrelationId correlationId) {
        return pending.computeIfAbsent(correlationId, _ -> new CompletableFuture<>());
    }

    @Override
    public void failRequest(CorrelationId correlationId, RpcException exception) {
        failByKey(correlationId, exception);
    }

    // ===== Inbound side =====
    @Override
    public void completeFromResponse(CorrelationId correlationId, Response response) {
        completeByKey(correlationId, response);
    }

    @Override
    public void failFromResponse(CorrelationId correlationId, RpcException exception) {
        failByKey(correlationId, exception);
    }

    // ===== Internal helpers =====
    private void completeByKey(CorrelationId correlationId, Response response) {
        CompletableFuture<Response> future = pending.remove(correlationId);
        if (future != null) {
            future.complete(response);
        }
    }

    private void failByKey(CorrelationId correlationId, RpcException exception) {
        CompletableFuture<Response> future = pending.remove(correlationId);
        if (future != null) {
            future.completeExceptionally(exception);
        }
    }
}