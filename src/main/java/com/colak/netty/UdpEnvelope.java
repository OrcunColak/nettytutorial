package com.colak.netty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@Getter
@RequiredArgsConstructor
public class UdpEnvelope<T> {
    private final T payload;
    private final InetSocketAddress socketAddress;

    public UdpEnvelope(T payload, String ip, int port) {
        this(payload, new InetSocketAddress(ip, port));
    }
}
