package com.colak.netty.tcp.client;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/// Pure scheduling helper for TCP client auto-reconnect logic
/// Does not own any shared state - operates on individual TcpClientState objects passed in
@RequiredArgsConstructor
@Slf4j
public class TcpClientReconnector {
    private final EventLoopGroup workerGroup;

    public boolean isMaxRetriesExceeded(TcpClientState state) {
        int currentAttempt = state.getReconnectAttemptCount();
        Integer maxRetries = state.getParameters().getMaxReconnectRetries();
        return maxRetries != null && currentAttempt >= maxRetries;
    }

    public void scheduleReconnect(TcpClientState state, Runnable reconnectAction) {
        long delayMs = computeReconnectDelayMs(state);
        state.incrementReconnectAttemptCount();

        log.info("Scheduling reconnect for TCP client {} in {} ms (attempt {})",
                state.getChannelId(), delayMs, state.getReconnectAttemptCount());

        ScheduledFuture<?> scheduledFuture = workerGroup.next()
                .schedule(reconnectAction, delayMs, TimeUnit.MILLISECONDS);
        scheduledFuture
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.error("Failed to schedule reconnect: {}", state.getChannelId(), future.cause());
                    }
                });
        state.setScheduledReconnect(scheduledFuture);
    }

    /// Called from user thread
    public void stopReconnect(TcpClientState state) {
        state.clearReconnectAttemptCount();
        state.cancelScheduledReconenct();
    }

    private long computeReconnectDelayMs(TcpClientState state) {
        var params = state.getParameters();
        int attempt = state.getReconnectAttemptCount();
        return Math.min(
                params.getReconnectInitialBackoffMs() * (long) attempt * params.getReconnectBackoffIncrementMs(),
                params.getReconnectMaxBackoffMs());
    }
}
