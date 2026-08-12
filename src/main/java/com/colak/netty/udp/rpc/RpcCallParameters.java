package com.colak.netty.udp.rpc;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class RpcCallParameters {
    private final int maxAttempts;
    private final Duration timeout;

    /// Lombok will generate the builder, but we use this constructor to enforce validation
    private RpcCallParameters(int maxAttempts, Duration timeout) {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must be > 0");
        }
        this.maxAttempts = maxAttempts;
        this.timeout = timeout;
    }

    public long timeoutMillis() {
        return timeout.toMillis();
    }
}
