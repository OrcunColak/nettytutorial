package com.colak.netty.streamingudprpc;

import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.ScheduledFuture;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class StreamContext<T> {
    private final StreamHandler<T> handler;
    private final RpcResponseInboundHandler responseHandler;
    private final EventLoop eventLoop;
    private final Duration timeout;

    private ScheduledFuture<?> timeoutFuture;

    private boolean closed;
    private boolean timedOut;

    public StreamContext(StreamHandler<T> handler,
                         RpcResponseInboundHandler responseHandler,
                         EventLoop eventLoop,
                         Duration timeout) {

        this.handler = handler;
        this.responseHandler = responseHandler;
        this.eventLoop = eventLoop;
        this.timeout = timeout;

        handler.bindLifecycle(
                this::close,
                this::timeoutInternal
        );
    }

    public void start() {
        responseHandler.setStreamContext(this);
        scheduleTimeout();
    }

    public void onMessage(Object msg) {
        if (closed || timedOut) {
            return;
        }

        rescheduleTimeout();
        handler.internalHandleMessage(msg);
    }

    public void close() {
        if (!closed && !timedOut) {
            closed = true;
            cancelTimeout();
            responseHandler.clearStreamContext();
            handler.onStreamClosed();
        }
    }

    private void timeoutInternal() {
        if (!closed && !timedOut) {
            timedOut = true;
            cancelTimeout();
            responseHandler.clearStreamContext();
            handler.onStreamTimeout();
        }
    }

    // ===== Timeout logic =====

    private void scheduleTimeout() {
        timeoutFuture = eventLoop.schedule(this::timeoutInternal, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void rescheduleTimeout() {
        cancelTimeout();
        scheduleTimeout();
    }

    private void cancelTimeout() {
        if (timeoutFuture != null) {
            timeoutFuture.cancel(false);
            timeoutFuture = null;
        }
    }
}
