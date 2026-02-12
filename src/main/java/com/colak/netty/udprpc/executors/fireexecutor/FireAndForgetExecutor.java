package com.colak.netty.udprpc.executors.fireexecutor;

public interface FireAndForgetExecutor {

    void fire(Object request);
}
