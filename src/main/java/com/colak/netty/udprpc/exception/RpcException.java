package com.colak.netty.udprpc.exception;

public abstract class RpcException extends Exception {
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(String message) {
        super(message);
    }
}
