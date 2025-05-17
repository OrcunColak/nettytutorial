package com.colak.netty;

import java.util.concurrent.TimeUnit;

public record FixedRateTimerParameters(String timerId, Runnable runnable, long delay, long period, TimeUnit timeUnit) {

    public FixedRateTimerParameters(String timerId, Runnable runnable, long delay, long period) {
        this(timerId, runnable, delay, period, TimeUnit.MILLISECONDS);
    }
}
