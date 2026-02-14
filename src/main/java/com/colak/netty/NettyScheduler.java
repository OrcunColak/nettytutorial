package com.colak.netty;

import com.colak.netty.timerparams.FixedRateTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;
import io.netty.util.concurrent.ScheduledFuture;

public interface NettyScheduler {

    ScheduledFuture<?> scheduleFixedRateTimer(FixedRateTimerParameters parameters);

    ScheduledFuture<?> scheduleSingleShotTimer(SingleShotTimerParameters parameters);

    boolean cancel(String timerId);

    void cancelAll();
}
