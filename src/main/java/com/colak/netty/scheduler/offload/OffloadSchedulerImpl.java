package com.colak.netty.scheduler.offload;

import com.colak.netty.core.OffloadScheduler;
import com.colak.netty.params.FixedDelayTimerParameters;
import com.colak.netty.params.SingleShotTimerParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OffloadSchedulerImpl implements OffloadScheduler {
    private final ScheduledThreadPoolExecutor executor;
    private final Map<String, ScheduledFuture<?>> registry = new ConcurrentHashMap<>();

    public OffloadSchedulerImpl(int threadCount, String threadNamePrefix) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be > 0");
        }

        this.executor = new ScheduledThreadPoolExecutor(threadCount,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName(threadNamePrefix + "-offload-" + thread.threadId());
                    thread.setDaemon(true);
                    return thread;
                });
        this.executor.setRemoveOnCancelPolicy(true);
    }

    @Override
    public ScheduledFuture<?> scheduleFixedDelay(FixedDelayTimerParameters params) {
        Objects.requireNonNull(params, "params must not be null");

        String timerId = params.getTimerId();
        if (registry.containsKey(timerId)) {
            throw new IllegalArgumentException("Timer already exists: " + timerId);
        }
        ScheduledFuture<?> future = executor.scheduleWithFixedDelay(params.getTask(), params.getInitialDelay(), params.getDelay(),
                params.getTimeUnit());
        registry.put(timerId, future);
        return future;
    }

    @Override
    public ScheduledFuture<?> scheduleSingleShot(SingleShotTimerParameters params) {
        Objects.requireNonNull(params, "params must not be null");
        String timerId = params.getTimerId();
        if (registry.containsKey(timerId)) {
            throw new IllegalArgumentException("Timer already exists: " + timerId);
        }

        Runnable wrapper = () -> {
            try {
                params.getTask().run();
            } finally {
                registry.remove(timerId);
            }
        };
        ScheduledFuture<?> future = executor.schedule(wrapper, params.getInitialDelay(), params.getTimeUnit());
        registry.put(timerId, future);
        return future;
    }

    /// Cancel by ID
    @Override
    public boolean cancel(String timerId, boolean mayInterruptIfRunning) {
        ScheduledFuture<?> future = registry.remove(timerId);
        if (future != null) {
            return future.cancel(mayInterruptIfRunning);
        }
        return false;
    }

    /// Bulk operations
    @Override
    public void cancelAll(boolean mayInterruptIfRunning) {
        registry.forEach((_, future) -> future.cancel(mayInterruptIfRunning));
        registry.clear();
    }

    @Override
    public int activeTimerCount() {
        return registry.size();
    }

    @Override
    public void shutdownAndWait() {
        shutdownAndWait(30, TimeUnit.SECONDS);
    }

    @Override
    public void shutdownAndWait(long timeout, TimeUnit unit) {
        try {
            cancelAll(true);
            executor.shutdown();
            if (!executor.awaitTermination(timeout, unit)) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            log.error("Error while shutting down offload scheduler", e);
        }
    }
}
