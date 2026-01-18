package com.colak.netty.udprpc.response;

import com.colak.netty.udprpc.exception.RpcException;

import java.util.concurrent.CompletableFuture;

/// Key - correlation key
/// Res - response
public final class ExtractingResponseFutureRegistry<Key, Req, Res> implements ResponseFutureRegistry<Key, Res> {
    private final ResponseFutureRegistry<Key, Res> delegate;
    private final CorrelationStrategy<Key,Req, Res> correlation;

    public ExtractingResponseFutureRegistry(
            ResponseFutureRegistry<Key, Res> delegate,
            CorrelationStrategy<Key,Req, Res> correlation
    ) {
        this.delegate = delegate;
        this.correlation = correlation;
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

    public CompletableFuture<Res> registerRequest(Req request) {
        Key key = correlation.fromRequest(request);
        if (key == null) {
            throw new IllegalArgumentException("Cannot extract correlation key from request");
        }
        return register(key);
    }

    public void failRequest(Req request, RpcException ex) {
        Key key = correlation.fromRequest(request);
        if (key != null) {
            fail(key, ex);
        }
    }

    public void completeFromResponse(Res response) {
        Key key = correlation.fromResponse(response);
        if (key != null) {
            complete(key, response);
        }
    }

    public void failFromResponse(Res response, RpcException ex) {
        Key key = correlation.fromResponse(response);
        if (key != null) {
            fail(key, ex);
        }
    }
}
