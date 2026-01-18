package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.handler.CorrelationKeyExtractor;

import java.util.concurrent.CompletableFuture;

public final class ExtractingResponseFutureRegistry<Key, Req, Res> implements ResponseFutureRegistry<Key, Res> {
    private final ResponseFutureRegistry<Key, Res> delegate;
    private final CorrelationKeyExtractor<Req, Key> requestKeyExtractor;
    private final CorrelationKeyExtractor<Res, Key> responseKeyExtractor;

    public ExtractingResponseFutureRegistry(
            ResponseFutureRegistry<Key, Res> delegate,
            CorrelationKeyExtractor<Req, Key> requestKeyExtractor,
            CorrelationKeyExtractor<Res, Key> responseKeyExtractor
    ) {
        this.delegate = delegate;
        this.requestKeyExtractor = requestKeyExtractor;
        this.responseKeyExtractor = responseKeyExtractor;
    }

    @Override
    public CompletableFuture<Res> register(Key correlationKey) {
        return delegate.register(correlationKey);
    }

    @Override
    public void complete(Key correlationKey, Res response) {
        delegate.complete(correlationKey, response);
    }

    @Override
    public void fail(Key correlationKey, RpcException exception) {
        delegate.fail(correlationKey, exception);
    }

    @Override
    public void failAll(RpcException exception) {
        delegate.failAll(exception);
    }

    public CompletableFuture<Res> registerRequest(Req request) {
        Key key = requestKeyExtractor.extract(request);
        if (key == null) {
            throw new IllegalArgumentException("Cannot extract correlation key from request");
        }
        return register(key);
    }

    public void failRequest(Req request, RpcException ex) {
        Key key = requestKeyExtractor.extract(request);
        if (key != null) {
            fail(key, ex);
        }
    }

    public void completeFromResponse(Res response) {
        Key key = responseKeyExtractor.extract(response);
        if (key != null) {
            complete(key, response);
        }
    }

    public void failFromResponse(Res response, RpcException ex) {
        Key key = responseKeyExtractor.extract(response);
        if (key != null) {
            fail(key, ex);
        }
    }
}
