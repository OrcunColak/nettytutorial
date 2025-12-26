package com.colak.network.udpsender;

import lombok.Getter;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

@Getter
public class UdpParams {
    private final String destinationHost;
    private final int destinationPort;
    private final byte[] data;
    private final boolean throwOnIOException;

    protected UdpParams(Builder builder) {
        this.destinationHost = builder.destinationHost;
        this.destinationPort = builder.destinationPort;
        this.data = builder.data;
        this.throwOnIOException = builder.throwOnIOException;
    }

    public DatagramPacket toDatagramPacket() {
        InetSocketAddress address = new InetSocketAddress(destinationHost, destinationPort);
        return new DatagramPacket(data, data.length, address);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String destinationHost;
        private int destinationPort;
        private byte[] data;
        private boolean throwOnIOException = false;

        public Builder destinationHost(String destinationHost) {
            this.destinationHost = destinationHost;
            return this;
        }

        public Builder destinationPort(int destinationPort) {
            this.destinationPort = destinationPort;
            return this;
        }

        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        public Builder throwOnIOException(boolean throwOnIOException) {
            this.throwOnIOException = throwOnIOException;
            return this;
        }

        public UdpParams build() {
            if (destinationHost == null || data == null) {
                throw new IllegalArgumentException("Destination host and data must be provided");
            }
            return new UdpParams(this);
        }
    }
}
