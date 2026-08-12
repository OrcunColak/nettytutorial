package com.colak.netty.udp.rpc.exception;

public abstract class RpcException extends Exception {
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(String message) {
        super(message);
    }
}
