package com.colak.netty.core;

import com.colak.netty.timerparams.FixedDelayTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface OffloadScheduler {

    ScheduledFuture<?> scheduleFixedDelay(FixedDelayTimerParameters params);

    ScheduledFuture<?> scheduleSingleShot(SingleShotTimerParameters params);

    /// Cancel by ID
    boolean cancel(String timerId, boolean mayInterruptIfRunning);

    /// Bulk operations
    void cancelAll(boolean mayInterruptIfRunning);

    int activeTimerCount();

    void shutdownAndWait();

    void shutdownAndWait(long timeout, TimeUnit unit);
}
