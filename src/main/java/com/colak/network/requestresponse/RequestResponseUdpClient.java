package com.colak.network.requestresponse;

import com.colak.network.udpsender.UdpParams;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RequestResponseUdpClient implements AutoCloseable {
    private final DatagramSocket datagramSocket;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final int originalSoTimeout; // Store original timeout

    public RequestResponseUdpClient() throws SocketException {
        this.datagramSocket = new DatagramSocket();
        this.originalSoTimeout = datagramSocket.getSoTimeout();
    }

    public RequestResponseUdpClient(int port) throws SocketException {
        this.datagramSocket = new DatagramSocket(port);
        this.originalSoTimeout = datagramSocket.getSoTimeout();
    }

    public RequestResponseUdpClient(InetSocketAddress address) throws SocketException {
        this.datagramSocket = new DatagramSocket(address);
        this.originalSoTimeout = datagramSocket.getSoTimeout();
    }

    public boolean sendWithAck(SendWithAckParams params) {
        validateSocket();
        boolean receivedValidAck = false;

        try {
            // Save current timeout
            int currentTimeout = datagramSocket.getSoTimeout();

            try {
                // Set temporary timeout for this operation
                datagramSocket.setSoTimeout(params.getResponseTimeoutMillis());

                // Send packet
                datagramSocket.send(params.toDatagramPacket());

                // Wait for acknowledgement
                receivedValidAck = waitForValidAck(params.getResponseValidator());
            } finally {
                // Restore original timeout
                datagramSocket.setSoTimeout(currentTimeout);
            }
        } catch(IOException exception) {
            handleSendError(params.getUdpParams(), exception);
        }

        return receivedValidAck;
    }

    private boolean waitForValidAck(ResponseValidator responseValidator) throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

        try {
            datagramSocket.receive(datagramPacket);
            return responseValidator.isValidResponse(datagramPacket);
        } catch (SocketTimeoutException e) {
            log.debug("Timeout waiting for ACK");
            return false;
        }
    }

    private void handleSendError(UdpParams params, IOException exception) {
        log.error("Error sending DatagramPacket to {}:{}",
                params.getDestinationHost(), params.getDestinationPort(), exception);

        if(params.isThrowOnIOException()) {
            throw new RuntimeException("Failed to send UDP packet", exception);
        }
    }

    private void validateSocket() {
        if(isClosed()) {
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

    // Helper method for testing
    public int getSoTimeout() throws SocketException {
        return datagramSocket.getSoTimeout();
    }
}

