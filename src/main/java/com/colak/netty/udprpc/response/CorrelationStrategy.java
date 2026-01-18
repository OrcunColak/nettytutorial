package com.colak.netty.udprpc.response;

public interface CorrelationStrategy<K, Req, Res> {

    K fromRequest(Req request);

    K fromResponse(Res response);
}

