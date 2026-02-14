package com.colak.netty.udprpc.executors.fireexecutor;

import com.colak.netty.ChannelSession;
import com.colak.netty.NettyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class DefaultFireAndForgetExecutor implements FireAndForgetExecutor {
    private final ChannelSession channelSession;

    @Override
    public void fire(Object request) {
        try {
            channelSession.sendMessage(request);
        } catch (Exception e) {
            log.debug("Failed to send fire-and-forget message to channel '{}': {}",
                    channelSession.getChannelId(), e.getMessage(), e);
        }
    }
}

