package com.colak.netty.udprpc.exception;

/// Your registry or inbound handler decides what that cause represents.
public class RpcTransportException extends RpcException {
    public RpcTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
