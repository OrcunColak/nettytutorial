package com.colak.netty.udp.rpc.executors.fireexecutor;

public interface FireAndForgetExecutor {

    void fire(Object request);
}
