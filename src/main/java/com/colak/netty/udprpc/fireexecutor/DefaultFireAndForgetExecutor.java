package com.colak.netty.udprpc.fireexecutor;

import com.colak.netty.NettyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class DefaultFireAndForgetExecutor implements FireAndForgetExecutor {
    private final NettyManager nettyManager;
    private final String channelId;

    @Override
    public void fire(Object request) {
        try {
            nettyManager.sendUdpMessage(channelId, request);
        } catch (Exception e) {
            log.debug("Failed to send fire-and-forget message to channel '{}': {}",
                    channelId, e.getMessage(), e);
        }
    }
}

