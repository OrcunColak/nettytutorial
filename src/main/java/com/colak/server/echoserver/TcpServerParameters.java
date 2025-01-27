import io.netty.channel.ChannelHandler;

public record TcpServerParameters(String channelId,int port,ChannelHanler<ByteBuf> handler) {
}
