package com.colak.netty.tcp;

import com.colak.netty.core.ChannelSession;
import com.colak.netty.tcp.client.TcpClientBootstrapBuilder;
import com.colak.netty.tcp.client.TcpClientChannelSession;
import com.colak.netty.tcp.client.TcpClientParameters;
import com.colak.netty.tcp.client.TcpClientReconnector;
import com.colak.netty.tcp.client.TcpClientState;
import com.colak.netty.tcp.server.TcpServerBoostrapBuilder;
import com.colak.netty.tcp.server.TcpServerChannelSession;
import com.colak.netty.tcp.server.TcpServerParameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TcpManager {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final TcpClientReconnector reconnector;
    private final ConcurrentMap<String, ServerSocketChannel> serverChannels = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Boolean> pendingCreations = new ConcurrentHashMap<>();
    /// Active SocketChannel registry: both server-accepted child connections
    /// and direct client connections (user-provided channelId keys)
    private final ConcurrentMap<String, SocketChannel> connections = new ConcurrentHashMap<>();
    /// Consolidated client lifecycle state
    private final ConcurrentMap<String, TcpClientState> clientStates = new ConcurrentHashMap<>();

    public TcpManager(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.reconnector = new TcpClientReconnector(workerGroup);
    }

    public void addClientChannelIfAbsent(String channelId, SocketChannel socketChannel) {
        connections.putIfAbsent(channelId, socketChannel);
    }

    public SocketChannel removeClientChannel(String channelId) {
        return connections.remove(channelId);
    }

    public void removeServerChannel(String channelId) {
        serverChannels.remove(channelId);
    }

    public SocketChannel getClientChannel(String channelId) {
        return connections.get(channelId);
    }

    public Collection<String> getConnectionIds() {
        return connections.keySet();
    }

    /// Stops reconnect attempts and removes the client from lifecycle tracking
    /// Called from TcpClientChannelSession.close()
    public void stopReconnect(String channelId) {
        TcpClientState state = clientStates.remove(channelId);
        if (state != null) {
            reconnector.stopReconnect(state);
        }
    }

    public ChannelSession createTcpServer(TcpServerParameters parameters) {
        String channelId = parameters.getChannelId();
        int port = parameters.getPort();
        try {
            if (pendingCreations.putIfAbsent(channelId, Boolean.TRUE)) {
                throw new IllegalStateException("Tcp Server '" + channelId + "' is already being created");
            }

            if (serverChannels.containsKey(channelId)) {
                log.warn("TCP Server {} already exists, returning existing session", channelId);
                return new TcpServerChannelSession(channelId, serverChannels.get(channelId), this);
            }

            ServerBootstrap bootstrap = new TcpServerBoostrapBuilder(bossGroup, workerGroup, this)
                    .build(parameters);
            ServerSocketChannel channel = (ServerSocketChannel) bootstrap.bind(port)
                    .sync()
                    .channel();
            serverChannels.put(channelId, channel);
            log.info("TCP Server {} started on port {}", channelId, port);
            return new TcpServerChannelSession(channelId, channel, this);
        } catch (InterruptedException e) {
            log.error("Failed to start TCP Server on port {}", channelId, e);
            throw new RuntimeException(e);
        } finally {
            pendingCreations.remove(channelId);
        }
    }

    public ChannelSession createTcpClient(TcpClientParameters parameters) {
        String channelId = parameters.getChannelId();
        int port = parameters.getPort();
        try {
            if (pendingCreations.putIfAbsent(channelId, Boolean.TRUE)) {
                throw new IllegalStateException("Tcp Server '" + channelId + "' is already being created");
            }
            if (clientStates.containsKey(channelId)) {
                log.warn("TCP Client {} already exists, returning existing session", channelId);
                return new TcpClientChannelSession(channelId, connections.get(channelId), this);
            }
            TcpClientState clientState = new TcpClientState(channelId, parameters);
            clientStates.put(channelId, clientState);
            try {
                performInitialConnect(parameters);
            } catch (InterruptedException e) {
                log.error("Failed to connect TCP Client with Id '{}': {} ", channelId, port, e);
                if (parameters.isAutoReconnect()) {
                    scheduleReconnect(channelId);
                } else {
                    clientStates.remove(channelId);
                    throw new RuntimeException(e);
                }
            }
            return new TcpClientChannelSession(channelId, connections.get(channelId), this);
        } finally {
            pendingCreations.remove(channelId);
        }
    }

    private void performInitialConnect(TcpClientParameters parameters) throws InterruptedException {
        Bootstrap bootstrap = new TcpClientBootstrapBuilder(workerGroup, this)
                .build(parameters);
        String channelId = parameters.getChannelId();
        String host = parameters.getHost();
        int port = parameters.getPort();
        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);
        SocketChannel channel = (SocketChannel) bootstrap.connect(remoteAddress)
                .sync()
                .channel();
        connections.putIfAbsent(channelId, channel);
        log.info("TCP Client {} connected to {}:{}", channelId, host, port);
    }

    public void scheduleReconnect(String channelId) {
        TcpClientState state = clientStates.remove(channelId);
        if (state == null || !state.getParameters().isAutoReconnect()) {
            return;
        }
        if (reconnector.isMaxRetriesExceeded(state)) {
            log.warn("TCP Client {} has been exceeded max retries", channelId);
            clientStates.remove(channelId);
            return;
        }
        reconnector.scheduleReconnect(state, () -> attemptReconnect(channelId));
    }

    private void attemptReconnect(String channelId) {
        TcpClientState state = clientStates.get(channelId);
        if (state == null) {
            log.warn("Reconnect cancelled for TCP Client {} (client removed))", channelId);
            return;
        }

        Bootstrap bootstrap = new TcpClientBootstrapBuilder(workerGroup, this)
                .build(state.getParameters());
        InetSocketAddress remoteAddress = new InetSocketAddress(state.getParameters().getHost(),
                state.getParameters().getPort());
        bootstrap.connect(remoteAddress).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                if (clientStates.get(channelId) == null) {
                    log.info("Discarding late reconnect for TCP Client {} (session already closed", channelId);
                    future.channel().close().addListener((ChannelFuture closeFuture) -> {
                        if (!closeFuture.isSuccess()) {
                            log.error("Failed to discard late reconnect for {}", channelId, closeFuture.cause());
                        }
                    });
                    return;
                }
                // TcpConenctionTracker.channelActive will add to connection map
                // a moment later on the same event loop thread, so no putIfAbsent needed.
                state.clearReconnectAttemptCount();
                log.info("TCP Client {} reconnected successfully", channelId);
            } else {
                log.error("Reconnect attempt failed for TCP Client {}", channelId, future.cause());
                scheduleReconnect(channelId);
            }
        });
    }

    public void shutdown() {
        clientStates.values().forEach(reconnector::stopReconnect);
        clientStates.clear();

        connections.values().forEach(channel -> channel.close()
                .addListener((ChannelFuture future) -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to close TCP connection ", future.cause());
                    }
                }));
        connections.clear();

        serverChannels.values().forEach(channel -> channel.close()
                .addListener((ChannelFuture future) -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to close TCP server channel", future.cause());
                    }
                }));
        serverChannels.clear();
    }
}
