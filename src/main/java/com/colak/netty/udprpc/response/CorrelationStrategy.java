package com.colak.netty.udprpc.response;

public interface CorrelationStrategy<CorrelationId, Request, Response> {

    CorrelationId fromRequest(Request request);

    CorrelationId fromResponse(Response response);
}

