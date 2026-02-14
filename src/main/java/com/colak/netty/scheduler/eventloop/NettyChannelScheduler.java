package com.colak.netty.scheduler.eventloop;

import com.colak.netty.NettyScheduler;
import com.colak.netty.timerparams.FixedRateTimerParameters;
import com.colak.netty.timerparams.SingleShotTimerParameters;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequiredArgsConstructor
@Slf4j
public class NettyChannelScheduler implements NettyScheduler {
    private final EventLoop eventLoop;
    private final ConcurrentMap<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    @Override
    public ScheduledFuture<?> scheduleFixedRateTimer(FixedRateTimerParameters parameters) {
        String timerId = parameters.getTimerId();
        if (timers.containsKey(timerId)) {
            log.info("Timer with ID {} is already running", timerId);
            return null;
        }

        ScheduledFuture<?> scheduledFuture = eventLoop.scheduleAtFixedRate(parameters.getTask(), parameters.getDelay(),
                parameters.getPeriod(), parameters.getTimeUnit());
        timers.put(timerId, scheduledFuture);
        log.info("Timer with ID {} started", timerId);
        return scheduledFuture;
    }

    @Override
    public ScheduledFuture<?> scheduleSingleShotTimer(SingleShotTimerParameters parameters) {
        ScheduledFuture<?> scheduledFuture = eventLoop.schedule(parameters.getTask(), parameters.getDelay(), parameters.getTimeUnit());
        scheduledFuture
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to scheduleSingleShotTimer", future.cause());
                    }
                });
        return scheduledFuture;
    }

    @Override
    public boolean cancel(String timerId) {
        boolean result = false;
        ScheduledFuture<?> scheduledFuture = timers.remove(timerId);
        if (scheduledFuture != null) {
            // false means do not interrupt if already running
            result = scheduledFuture.cancel(false);
            log.info("Timer with ID {} stopped", timerId);
        } else {
            log.info("No running timer found with with ID {}", timerId);
        }
        return result;
    }

    @Override
    public void cancelAll() {
        timers.forEach((timerId, scheduledFuture) -> cancel(timerId));
        timers.clear();
        log.info("All timers stopped");
    }
}
