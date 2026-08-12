package com.colak.netty.udp.rpc;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class RpcCallParameters {
    private int maxAttempts;
    private Duration timeout;

    public long getTimeoutMillis() {
        return timeout.toMillis();
    }
}
