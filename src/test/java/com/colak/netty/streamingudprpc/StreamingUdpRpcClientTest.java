package com.colak.netty.streamingudprpc;

import com.colak.netty.NettyManager;
import com.colak.netty.udpparams.UdpServerParameters;
import com.colak.netty.udprpc.RpcCallParameters;
import com.colak.netty.udprpc.UdpRpcClient;
import com.colak.netty.udprpc.exception.RpcException;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.DatagramPacketDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Slf4j
class StreamingUdpRpcClientTest {

    private static class FixedTargetDatagramEncoder extends MessageToMessageEncoder<String> {

        private final InetSocketAddress target;

        public FixedTargetDatagramEncoder(int port) {
            target = new InetSocketAddress("127.0.0.1", port);
        }

        @Override
        protected void encode(ChannelHandlerContext ctx,
                              String msg,
                              List<Object> out) {

            ByteBuf buf = Unpooled.copiedBuffer(msg, StandardCharsets.UTF_8);
            out.add(new DatagramPacket(buf, target));
        }
    }

    private static class MyCorrelationStrategy implements CorrelationStrategy {
        @Override
        public Object fromRequest(Object request) {
            return request;
        }

        @Override
        public Object fromResponse(Object response) {
            return response;
        }
    }

    @Test
    void startStream() throws RpcException, InterruptedException {
        int udpPort = 54321;
        int rpcPort = 12345;
        NettyManager nettyManager = NettyManager.builder()
                .build();

        List<ChannelInboundHandler> decoders = List.of(
                new DatagramPacketDecoder(new StringDecoder(StandardCharsets.UTF_8))
        );
        List<ChannelOutboundHandler> encoders = List.of(new StringEncoder(StandardCharsets.UTF_8), new FixedTargetDatagramEncoder(rpcPort));
        UdpServerParameters udpServerParameters = UdpServerParameters.builder()
                .channelId("udp-server")
                .port(udpPort)
                .addInboundDecoders(decoders)
                .addOutboundEncoders(encoders)
                .addInboundHandler(new SimpleChannelInboundHandler<String>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, String string) {
                        ctx.pipeline().writeAndFlush("test");

                    }
                })
                .build();
        nettyManager.addUdpServer(udpServerParameters);

        List<ChannelOutboundHandler> rpcEncoders = List.of(new StringEncoder(StandardCharsets.UTF_8), new FixedTargetDatagramEncoder(udpPort));

        UdpRpcClient udpRpcClient = UdpRpcClient.builder()
                .channelId("test-channel")
                .port(rpcPort)
                .addInboundDecoders(decoders)
                .addOutboundEncoders(rpcEncoders)
                .correlationStrategy(new MyCorrelationStrategy())
                .build();

        StreamHandler<String> handler = new StreamHandler<>(String.class) {
            @Override
            protected boolean onHandleMessage(String message) {
                return false;
            }

            @Override
            protected void onStreamClosed() {

            }

            @Override
            protected void onStreamTimeout() {
                log.info("onStreamTimeout");
            }
        };
        udpRpcClient.start();
        StreamingUdpRpcClient streamClient = udpRpcClient.newStreamClient();

        RpcCallParameters parameters = RpcCallParameters.builder()
                .maxAttempts(1)
                .timeout(Duration.ofSeconds(2))
                .build();
        String result = streamClient.startStream("test", parameters, Duration.ofSeconds(10), handler, String.class);
        log.info("result: {}", result);

        Thread.sleep(Duration.ofSeconds(20));
    }
}