package com.colak.netty.udprpc.fireexecutor;

public interface FireAndForgetExecutor<Req> {

    void fire(Req request);
}
