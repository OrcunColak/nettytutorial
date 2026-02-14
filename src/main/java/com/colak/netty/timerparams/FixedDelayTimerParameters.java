package com.colak.netty.timerparams;

import lombok.Builder;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@Builder
public class FixedDelayTimerParameters {
    private final String timerId;
    private Runnable task;
    private long delay;
    private long period;
    private TimeUnit timeUnit;
}
