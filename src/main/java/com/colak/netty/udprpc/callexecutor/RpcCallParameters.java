package com.colak.netty.udprpc.callexecutor;

import java.time.Duration;

public record RpcCallParameters(int attemptLimit, long timeoutMillis) {

    public static RpcCallParameters of ( int attemptLimit, Duration timeout){
        return new RpcCallParameters(attemptLimit, timeout.toMillis());
    }
}
