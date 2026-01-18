package com.colak.netty.udprpc;

import com.colak.netty.UdpEnvelope;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class SolarisEncoder extends MessageToMessageEncoder<UdpEnvelope<SolarisMessage>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UdpEnvelope<SolarisMessage> envelope, List<Object> out) {
        // 1. Serialize
        byte[] bytes = new  byte[1024];

        // 2. Allocate ByteBuf
        ByteBuf buf = ctx.alloc().buffer(bytes.length);
        buf.writeBytes(bytes);

        // 3. Wrap as DatagramPacket
        DatagramPacket packet = new DatagramPacket(buf, envelope.getSocketAddress());

        // 4. Pass to Netty
        out.add(packet);
    }
}
