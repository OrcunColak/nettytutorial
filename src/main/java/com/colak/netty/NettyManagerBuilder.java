package com.colak.netty;

public class NettyManagerBuilder {
    private int bossThreads = 0;    // means no TCP support
    private int workerThreads = 1;
    private String threadNamePrefix = "netty";

    NettyManagerBuilder() {
    }

    public NettyManagerBuilder bossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
        return this;
    }

    public NettyManagerBuilder workerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }

    public NettyManagerBuilder threadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
        return this;
    }

    public NettyManager build() {
        if (bossThreads < 0) {
            throw new IllegalStateException("bossThreads must be non-negative");
        }
        if (workerThreads <= 0) {
            throw new IllegalStateException("workerThreads must be positive");
        }
        return new NettyManager(this);
    }

    // Package private getters for NettyManager constructor
    int getBossThreads() {
        return bossThreads;
    }

    int getWorkerThreads() {
        return workerThreads;
    }

    String getThreadNamePrefix() {
        return threadNamePrefix;
    }
}
