package com.colak.netty.params;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
public class SingleShotTimerParameters {
    private String timerId;
    private Runnable task;
    private long initialDelay;

    @Builder.Default
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private SingleShotTimerParameters(String timerId, Runnable task, long initialDelay, TimeUnit timeUnit) {
        this.timerId = Objects.requireNonNull(timerId, "timer id must not be null");
        this.task = Objects.requireNonNull(task, "task must not be null");
        this.timeUnit = Objects.requireNonNull(timeUnit, "time unit must not be null");

        if (timerId.isBlank()) {
            throw new IllegalArgumentException("timer id must not be blank");
        }
        if (initialDelay < 0) {
            throw new IllegalArgumentException("initial delay must be >= 0");
        }
        this.initialDelay = initialDelay;
    }
}
