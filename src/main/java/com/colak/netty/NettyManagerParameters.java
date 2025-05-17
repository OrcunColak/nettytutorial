package com.colak.netty;

import java.util.concurrent.Executor;

public record NettyManagerParameters(int bossThread, int workerThreads, Executor executor) {

    public NettyManagerParameters(int bossThread, int workerThreads) {
        this(bossThread, workerThreads, null);
    }

    public NettyManagerParameters() {
        this(1, 4, null);
    }
}
