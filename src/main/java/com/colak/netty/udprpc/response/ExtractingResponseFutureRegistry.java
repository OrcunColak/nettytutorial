package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.handler.CorrelationKeyExtractor;

import java.util.concurrent.CompletableFuture;

public final class ExtractingResponseFutureRegistry<REQ, RES, KEY> {
    private final ResponseFutureRegistry<KEY, RES> delegate;
    private final CorrelationKeyExtractor<REQ, KEY> requestKeyExtractor;
    private final CorrelationKeyExtractor<RES, KEY> responseKeyExtractor;

    public ExtractingResponseFutureRegistry(
            ResponseFutureRegistry<KEY, RES> delegate,
            CorrelationKeyExtractor<REQ, KEY> requestKeyExtractor,
            CorrelationKeyExtractor<RES, KEY> responseKeyExtractor
    ) {
        this.delegate = delegate;
        this.requestKeyExtractor = requestKeyExtractor;
        this.responseKeyExtractor = responseKeyExtractor;
    }

    // === Sender side ===
    public CompletableFuture<RES> register(REQ request) {
        KEY key = requestKeyExtractor.extract(request);
        if (key == null) {
            throw new IllegalArgumentException("Cannot extract correlation key from request");
        }
        return delegate.register(key);
    }

    public void fail(REQ request, RpcException ex) {
        KEY key = requestKeyExtractor.extract(request);
        if (key != null) {
            delegate.fail(key, ex);
        }
    }

    // === Inbound handler side ===
    public void completeFromResponse(RES response) {
        KEY key = responseKeyExtractor.extract(response);
        if (key != null) {
            delegate.complete(key, response);
        }
    }

    public void failFromResponse(RES response, RpcException ex) {
        KEY key = responseKeyExtractor.extract(response);
        if (key != null) {
            delegate.fail(key, ex);
        }
    }

    // === Channel-level failures ===
    public void failAll(RpcException ex) {
        delegate.failAll(ex);
    }
}
