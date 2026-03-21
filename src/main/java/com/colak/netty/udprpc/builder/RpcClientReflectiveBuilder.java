package com.colak.netty.udprpc.builder;

import com.colak.netty.udprpc.UdpRpcClient;
import com.colak.netty.udprpc.annotations.RpcCorrelationStrategy;
import com.colak.netty.udprpc.annotations.RpcDecoder;
import com.colak.netty.udprpc.annotations.RpcEncoder;
import com.colak.netty.udprpc.annotations.RpcInboundHandler;
import com.colak.netty.udprpc.response.CorrelationStrategy;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RpcClientReflectiveBuilder {

    public static UdpRpcClient build(RpcClientReflectiveConfig config) throws Exception {
        Reflections reflections = new Reflections(config.getScanPackage(), Scanners.TypesAnnotated);
        String clientName = config.getRpcClientName() != null ? config.getRpcClientName() : "";

        // Outbound handlers (encoders)
        List<ChannelOutboundHandler> encoders = reflections.getTypesAnnotatedWith(RpcEncoder.class).stream()
                .filter(c -> c.getAnnotation(RpcEncoder.class).rpcClient().equals(clientName))
                .sorted(Comparator.comparingInt(c -> c.getAnnotation(RpcEncoder.class).order()))
                .map(clazz -> instantiate(clazz, ChannelOutboundHandler.class))
                .collect(Collectors.toList());

        // Inbound handlers (decoders)
        List<ChannelInboundHandler> decoders = reflections.getTypesAnnotatedWith(RpcDecoder.class).stream()
                .filter(c -> c.getAnnotation(RpcDecoder.class).rpcClient().equals(clientName))
                .sorted(Comparator.comparingInt(c -> c.getAnnotation(RpcDecoder.class).order()))
                .map(clazz -> instantiate(clazz, ChannelInboundHandler.class))
                .collect(Collectors.toList());

        // Single inbound handler
        ChannelInboundHandler inboundHandler = instantiateSingle(reflections, RpcInboundHandler.class, ChannelInboundHandler.class, clientName);

        // Single correlation strategy
        CorrelationStrategy correlation = instantiateSingle(reflections, RpcCorrelationStrategy.class, CorrelationStrategy.class, clientName);

        // Build the client
        UdpRpcClientBuilder builder = UdpRpcClient.builder()
                .threadNamePrefix(clientName.isEmpty() ? "rpc-client" : clientName + "-rpc-client")
                .channelId(config.getChannelId())
                .port(config.getPort())
                .addInboundDecoders(decoders)
                .addOutboundEncoders(encoders)
                .correlationStrategy(correlation)
                .addInboundHandler(inboundHandler);

        return builder.build();
    }

    // Generic instantiation
    private static <T> T instantiate(Class<?> clazz, Class<T> type) {
        try {
            return type.cast(clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate " + clazz.getName(), e);
        }
    }

    // Instantiate single annotated class with optional client name filter
    private static <T> T instantiateSingle(Reflections reflections,
                                           Class<? extends java.lang.annotation.Annotation> annotation,
                                           Class<T> type,
                                           String clientName) {
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(annotation).stream()
                .filter(c -> {
                    try {
                        return c.getMethod("rpcClient").invoke(null).equals(clientName);
                    } catch (NoSuchMethodException e) {
                        return true; // backward compatibility if annotation doesn't have rpcClient
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toSet());

        if (classes.isEmpty()) {
            throw new IllegalStateException("No class found with annotation: " + annotation.getSimpleName() +
                                            " for RPC client: " + clientName);
        }
        if (classes.size() > 1) {
            throw new IllegalStateException("Multiple classes found with annotation: " + annotation.getSimpleName() +
                                            " for RPC client: " + clientName);
        }
        return instantiate(classes.iterator().next(), type);
    }
}