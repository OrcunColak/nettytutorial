package com.colak.netty.udprpc.exception;

/// Exception thrown when RPC transport layer operations fail such as message serialization/deserialization errors
public class RpcTransportException extends RpcException {

    public RpcTransportException(String message) {
        super(message);
    }

    public RpcTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
