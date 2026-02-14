package com.colak.netty.udprpc.executors.fire;

public interface FireAndForgetExecutor {

    void fire(Object request);
}
