import io.netty.channel.ChannelHanler;

public record UdpServerParameters(String channelId,int port,ChannelHandler handler) {
}
