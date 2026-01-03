package com.colak.echoserverexample;

import com.colak.netty.NettyManager;
import com.colak.netty.tcpparams.TcpServerParameters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.List;
import java.util.Scanner;

public class EchoServer {

    private final int port;
    private final NettyManager nettyManager;

    public static void main(String[] args) {
        int port = 8080; // Default port
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        EchoServer echoServer = new EchoServer(port);
        echoServer.start();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Press Enter to exit...");

        // Wait for user to press Enter
        scanner.nextLine();

        echoServer.shutdown();
    }

    public EchoServer(int port) {
        this.port = port;
        this.nettyManager = NettyManager.newSingleThreadWorker();
    }

    public void start() {
        TcpServerParameters parameters = new TcpServerParameters("echoServerChannel",
                port,
                EchoServerHandler2::new,
                // Add two ChannelHandlerAdapters
                // StringDecoder for reading ByteBuf as string
                // StringEncoder for echoing back String as ByteBuf
                List.of(new StringDecoder(), new StringEncoder()));
        nettyManager.addTcpServer(parameters);
    }

    public void shutdown() {
        nettyManager.shutdown();
    }


}
