package com.colak.netty.udprpc.callexecutor;

import com.colak.netty.NettyManager;
import com.colak.netty.udprpc.exception.RpcTimeoutException;
import com.colak.netty.udprpc.response.CorrelationResponseRegistry;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRpcCallExecutorTest {

    @Mock
    private NettyManager nettyManager;

    @Mock
    private CorrelationResponseRegistry registry;

    @Mock
    private CorrelationStrategy correlationStrategy;

    @Test
    void testSuccess() throws Exception {
        Object request = "test-request";
        Object correlationKey = "correlation-key";


        var executor = new DefaultRpcCallExecutor(nettyManager, "test-channel", registry, correlationStrategy);

        when(correlationStrategy.fromRequest(request)).thenReturn(correlationKey);

        Object response = "test-response";
        CompletableFuture<Object> future = CompletableFuture.completedFuture(response);
        when(registry.registerRequest(correlationKey)).thenReturn(future);

        RpcCallParameters params = new RpcCallParameters(1, 1000L);
        Object result = executor.executeCall(request, params);

        assertEquals(response, result);

        // Verify that sendUdpMessage was called twice (2 retry attempts)
        verify(correlationStrategy).fromRequest(request);
        verify(registry).registerRequest(correlationKey);
        verify(nettyManager).sendUdpMessage("test-channel", request);
        verifyNoMoreInteractions(registry); // No failRequest on success
    }

    @Test
    void testTimeout() {
        Object request = "test-request";
        Object correlationKey = "correlation-key";
        CompletableFuture<Object> future = new CompletableFuture<>();

        var executor = new DefaultRpcCallExecutor(nettyManager, "test-channel", registry, correlationStrategy);

        when(correlationStrategy.fromRequest(request)).thenReturn(correlationKey);
        when(registry.registerRequest(correlationKey)).thenReturn(future);

        RpcCallParameters params = new RpcCallParameters(2, 100L);
        RpcTimeoutException exception = assertThrows(RpcTimeoutException.class, () -> executor.executeCall(request, params));

        assertEquals("No response after 2 attempts", exception.getMessage());

        // Verify that sendUdpMessage was called twice (2 retry attempts)
        verify(nettyManager, times(2)).sendUdpMessage("test-channel", request);
        verify(registry).registerRequest(correlationKey);
        verify(registry).failRequest(eq(correlationKey), any(RpcTimeoutException.class));
    }
}