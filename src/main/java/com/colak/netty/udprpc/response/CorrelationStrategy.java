package com.colak.netty.udprpc.response;

public interface CorrelationStrategy {

    Object fromRequest(Object request);

    Object fromResponse(Object response);
}

