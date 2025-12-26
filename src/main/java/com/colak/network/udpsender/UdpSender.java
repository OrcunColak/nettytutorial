package com.colak.network.udpsender;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class UdpSender implements AutoCloseable {
    private final DatagramSocket datagramSocket;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final int originalSoTimeout; // Store original timeout

    public UdpSender() throws SocketException {
        this.datagramSocket = new DatagramSocket();
        this.originalSoTimeout = datagramSocket.getSoTimeout();
    }

    public UdpSender(int port) throws SocketException {
        this.datagramSocket = new DatagramSocket(port);
        this.originalSoTimeout = datagramSocket.getSoTimeout();
    }

    public UdpSender(InetSocketAddress address) throws SocketException {
        this.datagramSocket = new DatagramSocket(address);
        this.originalSoTimeout = datagramSocket.getSoTimeout();
    }

    // Simple send method (fire and forget)
    public void send(UdpParams params) {
        validateSocket();
        try {
            datagramSocket.send(params.toDatagramPacket());
        } catch (IOException exception) {
            handleSendError(params, exception);
        }
    }

    private void handleSendError(UdpParams params, IOException exception) {
        log.error("Error sending DatagramPacket to {}:{}",
                params.getDestinationHost(), params.getDestinationPort(), exception);

        if (params.isThrowOnIOException()) {
            throw new RuntimeException("Failed to send UDP packet", exception);
        }
    }

    private void validateSocket() {
        if (isClosed()) {
            throw new IllegalStateException("UdpCommunicator is closed");
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            datagramSocket.close();
        }
    }

    public boolean isClosed() {
        return closed.get();
    }

    public int getLocalPort() {
        return datagramSocket.getLocalPort();
    }

    public InetAddress getLocalAddress() {
        return datagramSocket.getLocalAddress();
    }
}
