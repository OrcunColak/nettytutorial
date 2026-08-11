package com.colak.netty.scheduler.offload;

import com.colak.netty.core.OffloadScheduler;
import com.colak.netty.timerparams.FixedDelayTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NullOffloadSchedulerImpl implements OffloadScheduler {

    @Override
    public ScheduledFuture<?> scheduleFixedDelay(FixedDelayTimerParameters params) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleSingleShot(SingleShotTimerParameters params) {
        return null;
    }

    /// Cancel by ID
    @Override
    public boolean cancel(String timerId, boolean mayInterruptIfRunning) {
        return false;
    }

    /// Bulk operations
    @Override
    public void cancelAll(boolean mayInterruptIfRunning) {
    }

    @Override
    public int activeTimerCount() {
        return 0;
    }

    @Override
    public void shutdownAndWait() {
    }

    @Override
    public void shutdownAndWait(long timeout, TimeUnit unit) {
    }
}
