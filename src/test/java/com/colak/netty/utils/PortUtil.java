package com.colak.netty.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class PortUtil {

    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(3000, 9000);
    }

    public static int findAvailableTcpPort(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (isTcpPortAvailable(port)) {
                return port;
            }
        }
        return -1; // No available TCP port found
    }

    public static int findAvailableUdpPort() {
        return findAvailableUdpPort(3000, 9000);
    }

    public static int findAvailableUdpPort(int startPort, int endPort) {
        for (int port = startPort; port <= endPort; port++) {
            if (isUdpPortAvailable(port)) {
                return port;
            }
        }
        return -1; // No available UDP port found
    }

    private static boolean isTcpPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isUdpPortAvailable(int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
