package com.colak.netty;

import com.colak.netty.timerparams.FixedDelayTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface OffloadScheduler {

    ScheduledFuture<?> scheduleFixedDelay(FixedDelayTimerParameters params);

    ScheduledFuture<?> scheduleSingleShot(SingleShotTimerParameters params);

    boolean cancel(String timerId, boolean mayInterruptIfRunning);

    void cancelAll(boolean mayInterruptIfRunning);

    int activeTimerCount();

    void shutdownAndAwaitTermination();

    boolean shutdownAndAwaitTermination(long timeout, TimeUnit unit);
}
