package com.colak.netty.udp.rpc;

/// Interceptor called between a failed send attempt and the next retry.
/// Implementors may return a modified or cloned message object.
/// Must not change fields used for correlation (e.g. message ID) since the correlation key is extracted once before the
/// retry loop begins
@FunctionalInterface
public interface RetryInterceptor {

    /// No-op interceptor that returns the original message unchanged
    RetryInterceptor NOOP = (request, retryAttempt) -> request;

    /// Called after a send attempt failed and before the next retry.
    ///
    /// @param request      the original request message
    /// @param retryAttempt the retry number, 1-based (first retry = 1)
    /// @return the message to send on the next attempt; may be the same instance or a copy
    Object onRetry(Object request, int retryAttempt);
}
