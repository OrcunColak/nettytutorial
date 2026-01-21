package com.colak.netty.udprpc.exception;

import io.netty.channel.socket.DatagramPacket;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class RpcDecodingException extends RpcTransportException {
    private final DatagramPacket packet;

    public RpcDecodingException(String message, Throwable cause, DatagramPacket packet) {
        super(message,cause);
        this.packet = packet;
    }

    public DatagramPacket getPacket() {
        return packet;
    }

    public InetSocketAddress getSenderSocketAddress() {
        return packet.sender();
    }

    public InetAddress getSenderAddress() {
        return packet.sender().getAddress();
    }
}

