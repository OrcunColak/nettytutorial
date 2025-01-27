import io.netty.channel.SimpleChannelInboundHanler;
import io.netty.channel.socket.DatagramPacket;

public record UdpClientParameters(String channelId,SimpleChannelInboundHanler<DatagramPacket> inboundHandler) {
}
