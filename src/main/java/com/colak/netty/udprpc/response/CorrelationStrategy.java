package com.colak.netty.udprpc.response;

public interface CorrelationStrategy<Key, Req, Res> {

    Key fromRequest(Req request);

    Key fromResponse(Res response);
}

