package com.colak.netty.streamingudprpc;

import com.colak.netty.NettyScheduler;
import com.colak.netty.udprpc.handler.RpcResponseInboundHandler;

import java.time.Duration;

public final class StreamContext<T> {
    private final StreamHandler<T> handler;
    private final RpcResponseInboundHandler responseHandler;
    private final StreamInactivityTracker tracker;

    private volatile boolean closed;
    private volatile boolean timedOut;

    public StreamContext(StreamHandler<T> handler,
                         RpcResponseInboundHandler responseHandler,
                         NettyScheduler scheduler,
                         Duration timeout) {

        this.handler = handler;
        this.responseHandler = responseHandler;

        this.tracker = new StreamInactivityTracker(
                scheduler,
                timeout,
                this::timeoutInternal
        );
    }

    public void start() {
        responseHandler.setStreamHandler(handler);
        tracker.start();
    }

    public void onMessage(Object msg) {
        if (closed || timedOut) {
            return;
        }

        tracker.recordActivity();
        handler.internalHandleMessage(msg);
    }

    public void close() {
        if (!closed && !timedOut) {
            closed = true;
            tracker.stop();
            responseHandler.unSetStreamHandler(handler);
            handler.onStreamClosed();
        }
    }

    private void timeoutInternal() {
        if (!closed && !timedOut) {
            timedOut = true;
            tracker.stop();
            responseHandler.unSetStreamHandler(handler);
            handler.onStreamTimeout();
        }
    }
}

