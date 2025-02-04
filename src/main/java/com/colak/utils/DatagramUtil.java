package com.colak.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class DatagramUtil {

    public static java.net.DatagramPacket convertToJavaDatagramPacket(DatagramPacket nettyPacket) {
        // Extract ByteBuf and convert to byte array
        ByteBuf byteBuf = nettyPacket.content();
        byte[] data = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(data);

        // Extract sender's address and port
        InetSocketAddress sender = nettyPacket.sender();

        // Create and return Java DatagramPacket
        return new java.net.DatagramPacket(data, data.length, sender);
    }
}
