package com.colak.echoserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EchoServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public EchoServerHandler() {
        log.info("EchoServerHandler is created");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) {
        // Echo back the received message to the client
        // Retain the ByteBuf to increase the reference count
        msg.retain();

        // Echo the received ByteBuf back to the sender
        channelHandlerContext.writeAndFlush(msg);

        // You do not need to release the ByteBuf here since ctx.writeAndFlush(msg) will take care of it.
        // ReferenceCountUtil.release(msg); // Do not do this here
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Log the exception
        log.error("Exception caught: ", cause);

        // Close the channel or handle it based on the type of exception
        ctx.close();  // Optionally close the channel if the exception is severe
    }

}

