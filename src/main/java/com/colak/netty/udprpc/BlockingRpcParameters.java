package com.colak.netty.udprpc;

import java.time.Duration;

public record BlockingRpcParameters(
        int maxAttempts,
        Duration timeout
) {
    public static BlockingRpcParameters defaults() {
        return new BlockingRpcParameters(3, Duration.ofSeconds(2));
    }

    public long timeoutMillis() {
        return timeout.toMillis();
    }
}


