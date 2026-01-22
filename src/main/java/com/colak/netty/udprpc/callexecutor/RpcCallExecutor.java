package com.colak.netty.udprpc.callexecutor;

import com.colak.netty.udprpc.exception.RpcException;

public interface RpcCallExecutor {

    Object executeCall(Object request, RpcCallParameters params) throws RpcException,InterruptedException;
}
