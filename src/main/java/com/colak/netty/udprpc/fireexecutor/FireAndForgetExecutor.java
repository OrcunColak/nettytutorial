package com.colak.netty.udprpc.fireexecutor;

public interface FireAndForgetExecutor {

    void fire(Object request);
}
