package com.colak.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServerHandler2 extends SimpleChannelInboundHandler<String> {

    public EchoServerHandler2() {
        log.info("EchoServerHandler is created");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) {
        // Echo the received String back to the sender
        channelHandlerContext.writeAndFlush("Received : " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Log the exception
        log.error("Exception caught: ", cause);

        // Close the channel or handle it based on the type of exception
        ctx.close();  // Optionally close the channel if the exception is severe
    }

}

