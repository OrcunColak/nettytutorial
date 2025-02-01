package com.colak.echoserver;

import com.colak.nettymanager.NettyManager;
import com.colak.nettymanager.NettyManagerParameters;
import com.colak.nettymanager.TcpServerParameters;

import java.util.Scanner;

public class EchoServer {

    private final int port;
    private final NettyManager nettyManager;

    public EchoServer(int port) {
        this.port = port;
        NettyManagerParameters parameters = new NettyManagerParameters();
        this.nettyManager = new NettyManager(parameters);
    }

    public void start() {
        TcpServerParameters parameters = new TcpServerParameters("echoServerChannel",
                port,
                EchoServerHandler::new);
        nettyManager.addTcpServer(parameters);
    }

    public void shutdown() {
        nettyManager.shutdown();
    }

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
}
