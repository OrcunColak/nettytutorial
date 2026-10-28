package com.colak.netty.udp;

import lombok.Getter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/// Use this class to wrap a message as Udp
@Getter
public class UdpEnvelope<T> {
    private final T payload;
    private final InetSocketAddress socketAddress;
    private final byte[] rawBytes;
    private Map<String, Object> attributes;

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

    /// Ip Address
    public InetAddress getAddress() {
        return socketAddress.getAddress();
    }

    public <V> V getAttribute(String key, Class<V> type) {
        if (attributes == null) {
            return null;
        }
        return type.cast(attributes.get(key));
    }

    public void setAttributes(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(key, value);
    }

    @Override
    public String toString() {
        return "UdpEnvelope{" +
               "payload=" + payload.toString() +
               ", socketAddress=" + socketAddress +
               "}";
    }
}
