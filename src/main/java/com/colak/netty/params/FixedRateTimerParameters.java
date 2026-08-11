package com.colak.netty.params;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
@Builder
public class FixedRateTimerParameters {
    private String timerId;
    private Runnable task;

    private long initialDelay;
    private long period;

    @Builder.Default
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /// Lombok will generate builder, but we use this constructor to enforce validation
    private FixedRateTimerParameters(String timerId, Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        this.timerId = Objects.requireNonNull(timerId, "timer id must not be null");
        this.task = Objects.requireNonNull(task, "task must not be null");
        this.timeUnit = Objects.requireNonNull(timeUnit, "time unit must not be null");

        if (timerId.isBlank()) {
            throw new IllegalArgumentException("timer id must not be blank");
        }
        if (initialDelay < 0) {
            throw new IllegalArgumentException("initial delay must be >= 0");
        }
        if (period <= 0L) {
            throw new IllegalArgumentException("period must be > 0");
        }
        this.initialDelay = initialDelay;
        this.period = period;
    }
}
