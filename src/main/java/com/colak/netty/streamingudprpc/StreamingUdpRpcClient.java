package com.colak.netty.streamingudprpc;

import com.colak.netty.udprpc.UdpRpcClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StreamingUdpRpcClient {
    private final UdpRpcClient rpcClient;

    public void startStream(Object message, StreamHandler<?> handler) {
        // inboundHandler.setStreamHandler(handler);
        //
        // rpcClient.call(message,
    }
}
