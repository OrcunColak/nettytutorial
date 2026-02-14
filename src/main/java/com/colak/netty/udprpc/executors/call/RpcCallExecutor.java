package com.colak.netty.udprpc.executors.call;

import com.colak.netty.udprpc.RpcCallParameters;
import com.colak.netty.udprpc.exception.RpcException;

public interface RpcCallExecutor {

    Object executeCall(Object request, RpcCallParameters params) throws RpcException,InterruptedException;
}
