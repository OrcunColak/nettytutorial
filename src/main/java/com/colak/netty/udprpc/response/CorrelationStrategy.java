package com.colak.netty.udprpc.response;

public interface CorrelationStrategy {

    Object fromRequest(Object request);

    Object fromResponse(Object response);

    default boolean isErrorResponse(Object response) {
        return false;
    }

    default Object toCompletionValue(Object response) {
        return response;
    }
}

