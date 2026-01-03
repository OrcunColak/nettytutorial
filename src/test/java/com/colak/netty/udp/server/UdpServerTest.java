package com.colak.netty.udp.server;

import com.colak.netty.NettyManager;
import com.colak.netty.utils.EchoUdpServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class UdpServerTest {

    private static NettyManager nettyManager;

    @BeforeAll
    static void setUp() {
        nettyManager = NettyManager.newSingleThreadWorker();
    }

    @AfterAll
    static void tearDown() {
        nettyManager.shutdown();
    }

    @Test
    void testSendUdpMessage() throws Exception {
        // Create server on a random port
        EchoUdpServer echoUdpServer = new EchoUdpServer(nettyManager);
        echoUdpServer.start();

        // Create Java NIO packet and send it via UDP client
        int serverPort = echoUdpServer.getPort();
        java.net.DatagramPacket datagramPacket = createHelloPacket(serverPort);

        try (java.net.DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.send(datagramPacket);

            assertResponse(datagramSocket, serverPort);
        }
    }

    private void assertResponse(DatagramSocket datagramSocket, int serverPort) throws IOException {
        byte[] responseBuffer = new byte[1024];
        java.net.DatagramPacket responseDatagramPacket = new DatagramPacket(responseBuffer, responseBuffer.length);
        datagramSocket.receive(responseDatagramPacket);

        // Assert source port
        assertEquals(serverPort, responseDatagramPacket.getPort());

        // Assert message
        String response = new String(responseDatagramPacket.getData(), 0, responseDatagramPacket.getLength(),
                StandardCharsets.UTF_8);
        assertEquals("hello", response);
    }

    private DatagramPacket createHelloPacket(int serverPort) {
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", serverPort);

        byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(bytes, bytes.length, serverAddress);
    }
}
