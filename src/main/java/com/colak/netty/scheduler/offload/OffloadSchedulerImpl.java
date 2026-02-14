package com.colak.netty.scheduler.offload;

import com.colak.netty.OffloadScheduler;
import com.colak.netty.timerparams.FixedDelayTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
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
            throw new IllegalArgumentException("threadCount must be greater than 0");
        }

        this.executor = new ScheduledThreadPoolExecutor(threadCount, r -> {
            Thread thread = new Thread(r, threadNamePrefix);
            thread.setName(threadNamePrefix + "-offload-" + thread.threadId());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public ScheduledFuture<?> scheduleFixedDelay(FixedDelayTimerParameters params) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleSingleShot(SingleShotTimerParameters params) {
        String timerId = params.getTimerId();
        if (registry.containsKey(timerId)) {
            throw new IllegalArgumentException("Timer with id " + timerId + " already exists");
        }

        Runnable wrapper = () -> {
            try {
                params.getTask().run();
            } finally {
                registry.remove(timerId);
            }
        };
        ScheduledFuture<?> future = executor.schedule(wrapper, params.getDelay(), params.getTimeUnit());
        registry.put(timerId, future);
        return future;
    }

    @Override
    public boolean cancel(String timerId, boolean mayInterruptIfRunning) {
        ScheduledFuture<?> future = registry.remove(timerId);
        if (future != null) {
            return future.cancel(mayInterruptIfRunning);
        }
        return false;
    }

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
    public void shutdownAndAwaitTermination() {
        shutdownAndAwaitTermination(30, TimeUnit.SECONDS);

    }

    @Override
    public boolean shutdownAndAwaitTermination(long timeout, TimeUnit unit) {
        try {
            cancelAll(true);
            executor.shutdown();
            if (!executor.awaitTermination(timeout, unit)) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            log.error("Error while shutting down offload scheduler", e);
        }
        return false;
    }
}
