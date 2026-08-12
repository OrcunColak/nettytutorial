package com.colak.netty.udp.rpc.executors.callexecutor;

import com.colak.netty.udp.rpc.RpcCallParameters;
import com.colak.netty.udp.rpc.exception.RpcException;

public interface RpcCallExecutor {

    Object executeCall(Object request, RpcCallParameters params) throws RpcException,InterruptedException;
}
