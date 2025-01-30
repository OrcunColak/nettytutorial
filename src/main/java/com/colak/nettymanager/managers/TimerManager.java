package com.colak.nettymanager.managers;

import com.colak.nettymanager.FixedRateTimerParameters;
import com.colak.nettymanager.SingleShotTimerParameters;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TimerManager {

    private static final Logger logger = LoggerFactory.getLogger(TimerManager.class);

    private final EventLoopGroup workerGroup;

    private final ConcurrentMap<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    public TimerManager(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void scheduleFixedRateTimer(FixedRateTimerParameters parameters) {
        String timerId = parameters.timerId();
        if (timers.containsKey(timerId) && !timers.get(timerId).isCancelled()) {
            logger.info("Timer with ID {} is already running", timerId);
            return;
        }

        ScheduledFuture<?> scheduledFuture = workerGroup.scheduleAtFixedRate(parameters.runnable(), parameters.delay(),
                parameters.period(), parameters.timeUnit());
        timers.put(timerId, scheduledFuture);
        logger.info("Timer with ID {} started", timerId);

    }

    public void scheduleSingleShotTimer(SingleShotTimerParameters parameters) {
        workerGroup.schedule(parameters.runnable(), parameters.delay(), parameters.timeUnit());
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
