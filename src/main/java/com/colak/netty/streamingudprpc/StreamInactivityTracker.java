package com.colak.netty.streamingudprpc;

import com.colak.netty.NettyScheduler;
import com.colak.netty.timerparams.SingleShotTimerParameters;
import io.netty.util.concurrent.ScheduledFuture;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class StreamInactivityTracker {
    private final NettyScheduler scheduler;
    private final Duration timeout;
    private final Runnable timeoutCallback;

    private ScheduledFuture<?> future;

    public StreamInactivityTracker(NettyScheduler scheduler,
                                   Duration timeout,
                                   Runnable timeoutCallback) {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.timeoutCallback = timeoutCallback;
    }

    /**
     * Call when stream starts.
     */
    public void start() {
        scheduleNew();
    }

    /**
     * Call on every incoming message.
     */
    public void recordActivity() {
        cancelCurrent();
        scheduleNew();
    }

    /**
     * Call when stream closes.
     */
    public void stop() {
        cancelCurrent();
    }

    private void scheduleNew() {
        SingleShotTimerParameters parameters = SingleShotTimerParameters.builder()
                .timerId("stream-timeout")
                .task(timeoutCallback)
                .delay(timeout.toMillis())
                .timeUnit(TimeUnit.MILLISECONDS)
                .build();
        future = scheduler.scheduleSingleShotTimer(parameters);
    }

    private void cancelCurrent() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }
}