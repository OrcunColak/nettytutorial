package com.colak.netty.udprpc;

import com.colak.netty.UdpEnvelope;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import com.colak.netty.udprpc.response.ExtractingResponseFutureRegistry;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolarisRpcInboundHandlerTest {

    private final InetSocketAddress destinationAddress = new InetSocketAddress("localhost", 1111);
    private final InetSocketAddress replyAddress = new InetSocketAddress("localhost", 2222);

    @Test
    void testResponse() throws Exception {
        var registry = createRegistry();

        SolarisRpcInboundHandler handler = new SolarisRpcInboundHandler(registry);

        EmbeddedChannel channel = new EmbeddedChannel(handler);

        SolarisMessage request = new SolarisMessage((short) 1, (short) 2);
        request.setMessageNo((short) 3);
        request.setErrorNo((short) 0);

        UdpEnvelope<SolarisMessage> requestEnvelope = new UdpEnvelope<>(request, destinationAddress);

        // register RPC future (this is what sendAndAwait does internally)
        CompletableFuture<UdpEnvelope<SolarisMessage>> future = registry.registerRequest(requestEnvelope);

        // when – simulate response arriving from the network
        SolarisMessage response = new SolarisMessage((short) 1, (short) 2);
        response.setMessageNo((short) 3);
        response.setErrorNo((short) 0);

        UdpEnvelope<SolarisMessage> responseEnvelope = new UdpEnvelope<>(response, replyAddress);

        channel.writeInbound(responseEnvelope);

        // then
        UdpEnvelope<SolarisMessage> result = future.get(1, TimeUnit.SECONDS);

        assertEquals((short) 3, result.getPayload().getMessageNo());
        assertEquals((short) 0, result.getPayload().getErrorNo());
    }



    private ExtractingResponseFutureRegistry<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> createRegistry() {
        CorrelationStrategy<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> strategy = new CorrelationStrategy<>() {
            @Override
            public SolarisKey fromRequest(UdpEnvelope<SolarisMessage> envelope) {
                return toKey(envelope.getPayload());
            }

            @Override
            public SolarisKey fromResponse(UdpEnvelope<SolarisMessage> envelope) {
                return toKey(envelope.getPayload());
            }

            private SolarisKey toKey(SolarisMessage message) {
                return new SolarisKey(
                        message.getProtocolNo(),
                        message.getMessageNo(),
                        message.getErrorNo()
                );
            }
        };
        return new ExtractingResponseFutureRegistry<>(strategy);
    }
}