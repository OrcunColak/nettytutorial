package com.colak.netty.tcp.client;

import io.netty.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/// Consolidates all per-client lifecycle state.
/// Does not hold SocketChannel - that is kept in the connections map in TcpManager
/// so there is only one authoritative source of truth
@RequiredArgsConstructor
public class TcpClientState {
    private final String channelId;
    private final TcpClientParameters parameters;
    private final AtomicInteger reconnectAttemptCount = new AtomicInteger();
    private volatile ScheduledFuture<?> scheduledReconnect;

    public String getChannelId() {
        return channelId;
    }

    public TcpClientParameters getParameters() {
        return parameters;
    }

    public int getReconnectAttemptCount() {
        return reconnectAttemptCount.get();
    }

    public void incrementReconnectAttemptCount() {
        reconnectAttemptCount.incrementAndGet();
    }

    /// Calle from netty or user thread
    public void clearReconnectAttemptCount() {
        reconnectAttemptCount.set(0);
    }

    public ScheduledFuture<?> getScheduledReconnect() {
        return scheduledReconnect;
    }

    public void setScheduledReconnect(ScheduledFuture<?> scheduledReconnect) {
        // Cancel any state reconnect before overwriting
        ScheduledFuture<?> exiting = this.scheduledReconnect;
        if (exiting != null) {
            exiting.cancel(false);
        }
        this.scheduledReconnect = scheduledReconnect;
    }

    public void cancelScheduledReconnect() {
        ScheduledFuture<?> exiting = this.scheduledReconnect;
        if (exiting != null) {
            exiting.cancel(false);
        }
        this.scheduledReconnect = null;
    }
}
