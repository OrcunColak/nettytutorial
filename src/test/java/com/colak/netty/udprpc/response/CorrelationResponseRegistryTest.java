package com.colak.netty.udprpc.response;

import com.colak.netty.UdpEnvelope;
import com.colak.netty.udprpc.SolarisKey;
import com.colak.netty.udprpc.SolarisMessage;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationResponseRegistryTest {

    @Test
    void shouldTimeoutWhenNoResponseArrives() {
        var registry = createRegistry();
        var correlationStrategy = createCorrelationStrategy();

        SolarisMessage request = new SolarisMessage((short) 1, (short) 2);
        request.setMessageNo((short) 3);
        request.setErrorNo((short) 0);

        UdpEnvelope<SolarisMessage> requestEnvelope = new UdpEnvelope<>(request, new InetSocketAddress("localhost", 1111));

        SolarisKey solarisKey = correlationStrategy.fromRequest(requestEnvelope);
        CompletableFuture<UdpEnvelope<SolarisMessage>> future = registry.registerRequest(solarisKey);

        assertThrows(TimeoutException.class, () -> future.get(50, TimeUnit.MILLISECONDS));
    }

    private CorrelationResponseRegistry<SolarisKey, UdpEnvelope<SolarisMessage>> createRegistry() {
        return new CorrelationResponseRegistry<>();
    }

    private CorrelationStrategy<SolarisKey, UdpEnvelope<SolarisMessage>, UdpEnvelope<SolarisMessage>> createCorrelationStrategy() {
        return new CorrelationStrategy<>() {
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
    }

}