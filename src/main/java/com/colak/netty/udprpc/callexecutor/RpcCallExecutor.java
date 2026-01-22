package com.colak.netty.udprpc.callexecutor;

import com.colak.netty.udprpc.exception.RpcException;

public interface RpcCallExecutor<Req> {

    Object executeCall(Req request, RpcCallParameters params) throws RpcException,InterruptedException;
}
