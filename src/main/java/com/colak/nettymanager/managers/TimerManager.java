package com.colak.nettymanager.managers;

import com.colak.nettymanager.SingleShotTimerParameters;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class TimerManager {

    private static final Logger logger = LoggerFactory.getLogger(TimerManager.class);

    private final EventLoopGroup workerGroup;

    private final ConcurrentMap<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    public TimerManager(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }


    public boolean scheduleFixedRateTimer(String timerId, Runnable runnable, long delay, long period) {
        return scheduleFixedRateTimer(timerId, runnable, delay, period, TimeUnit.MILLISECONDS);
    }

    public boolean scheduleFixedRateTimer(String timerId, Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        boolean result = false;
        if (timers.containsKey(timerId) && timers.get(timerId).isCancelled()) {
            logger.info("Timer with ID {} is already running", timerId);
        } else {
            ScheduledFuture<?> scheduledFuture = workerGroup.scheduleAtFixedRate(runnable, delay, period, timeUnit);
            timers.put(timerId, scheduledFuture);
            result = true;
            logger.info("Timer with ID {} started", timerId);
        }
        return result;
    }

    public void scheduleSingleShotTimer(SingleShotTimerParameters parameters) {
        workerGroup.schedule(parameters.runnable(), parameters.delay(), TimeUnit.MILLISECONDS);
    }

    public void scheduleSingleShotTimer(Runnable runnable, long delay, TimeUnit timeUnit) {
        workerGroup.schedule(runnable, delay, timeUnit);
    }

    public void stopTimer(String timerId) {
        ScheduledFuture<?> scheduledFuture = timers.get(timerId);
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            // false means do not interrupt if already running
            scheduledFuture.cancel(true);
            timers.remove(timerId);
            logger.info("Timer with ID {} stopped", timerId);
        }
    }

    // When this method is called, new timers should not be added
    public void shutdown() {
        timers.forEach((timerId, _) -> stopTimer(timerId));
        timers.clear();
        logger.info("All timers stopped");
    }
}
