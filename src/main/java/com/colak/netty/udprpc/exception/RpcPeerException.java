package com.colak.netty.udprpc.exception;

/// The remote peer explicitly responded with an error.
/// Your registry or inbound handler decides what that cause represents.
public final class RpcPeerException extends RpcException {

    private final Object errorPayload;

    public RpcPeerException(String message, Object errorPayload) {
        super(message);
        this.errorPayload = errorPayload;
    }

    public RpcPeerException(String message, Object errorPayload, Throwable cause) {
        super(message, cause);
        this.errorPayload = errorPayload;
    }

    public Object getErrorPayload() {
        return errorPayload;
    }
}

