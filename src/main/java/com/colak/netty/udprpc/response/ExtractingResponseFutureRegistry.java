package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.handler.CorrelationKeyExtractor;

import java.util.concurrent.CompletableFuture;

public final class ExtractingResponseFutureRegistry<Req, Res, K> implements ResponseFutureRegistry<Res, K> {
    private final ResponseFutureRegistry<Res, K> delegate;
    private final CorrelationKeyExtractor<Req, K> requestKeyExtractor;
    private final CorrelationKeyExtractor<Res, K> responseKeyExtractor;

    public ExtractingResponseFutureRegistry(
            ResponseFutureRegistry<Res, K> delegate,
            CorrelationKeyExtractor<Req, K> requestKeyExtractor,
            CorrelationKeyExtractor<Res, K> responseKeyExtractor
    ) {
        this.delegate = delegate;
        this.requestKeyExtractor = requestKeyExtractor;
        this.responseKeyExtractor = responseKeyExtractor;
    }

    @Override
    public CompletableFuture<Res> register(K correlationKey) {
        return delegate.register(correlationKey);
    }

    @Override
    public void complete(K correlationKey, Res response) {
        delegate.complete(correlationKey, response);
    }

    @Override
    public void fail(K correlationKey, RpcException exception) {
        delegate.fail(correlationKey, exception);
    }

    @Override
    public void failAll(RpcException exception) {
        delegate.failAll(exception);
    }

    public CompletableFuture<Res> registerRequest(Req request) {
        K key = requestKeyExtractor.extract(request);
        if (key == null) {
            throw new IllegalArgumentException("Cannot extract correlation key from request");
        }
        return delegate.register(key);
    }

    public void failRequest(Req request, RpcException ex) {
        K key = requestKeyExtractor.extract(request);
        if (key != null) {
            delegate.fail(key, ex);
        }
    }

    public void completeFromResponse(Res response) {
        K key = responseKeyExtractor.extract(response);
        if (key != null) {
            delegate.complete(key, response);
        }
    }

    public void failFromResponse(Res response, RpcException ex) {
        K key = responseKeyExtractor.extract(response);
        if (key != null) {
            delegate.fail(key, ex);
        }
    }
}
