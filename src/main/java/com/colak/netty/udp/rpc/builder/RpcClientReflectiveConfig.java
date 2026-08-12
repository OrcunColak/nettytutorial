package com.colak.netty.udp.rpc.builder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcClientReflectiveConfig {
    private String rpcClientName; // optional, can be null
    private int port;
    private String channelId;
    private String scanPackage;
}
