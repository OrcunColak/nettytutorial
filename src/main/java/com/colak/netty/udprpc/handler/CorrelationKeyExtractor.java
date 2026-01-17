package com.colak.netty.udprpc.handler;

@FunctionalInterface
public interface CorrelationKeyExtractor<RES, KEY> {
    KEY extract(RES response);
}

