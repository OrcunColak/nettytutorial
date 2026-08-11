package com.colak.netty.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TcpExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String connectionId = ctx.channel().attr(TcpConnectionTracker.CONNECTION_ID_KEY).get();
        log.error("Unhandled exception on TCP connection: {}", connectionId, cause);
        ctx.close()
                .addListener(future -> {
                    if (!future.isSuccess()) {
                        log.error("Failed to close the TCP channel: {}", connectionId, future.cause());
                    }
                });
    }
}
