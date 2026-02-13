package com.colak.netty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.net.InetSocketAddress;

@Getter
public class UdpEnvelope<T> {
    private final T payload;
    private final InetSocketAddress socketAddress;
    private final byte[] rawBytes;

    public UdpEnvelope(T payload, String ip, int port) {
        this(payload, new InetSocketAddress(ip, port));
    }

    public UdpEnvelope(T payload, InetSocketAddress socketAddress) {
        this(payload, socketAddress, null);
    }

    public UdpEnvelope(T payload, InetSocketAddress socketAddress, byte[] rawBytes) {
        this.payload = payload;
        this.socketAddress = socketAddress;
        this.rawBytes = rawBytes;
    }

    public InetAddress getAddress() {
        return socketAddress.getAddress();
    }

    @Override
    public String toString() {
        return "UdpEnvelope{" +
               "payload=" + payload.toString() +
               ", socketAddress=" + socketAddress +
               "}";
    }
}
