
import io.netty.buffer.ByteBuf;
import io.netty.channel.SimpleChannelInboundHanler;

public record TcpClientParameters(String channelId,String host,int port,SimpleChannelInboundHanler<ByteBuf> inboundHandler) {
}
