package com.colak.nettymanager;

import java.util.concurrent.TimeUnit;

public record SingleShotTimerParameters(Runnable runnable, long delay, TimeUnit timeUnit) {

    public SingleShotTimerParameters(Runnable runnable, long delay) {
        this(runnable, delay, TimeUnit.MILLISECONDS);
    }
}
