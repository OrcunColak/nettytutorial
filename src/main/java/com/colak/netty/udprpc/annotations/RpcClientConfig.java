package com.colak.netty.udprpc.annotations;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcClientConfig {
    private String rpcClientName; // optional, can be null
    private int port;
    private String channelId;
    private String scanPackage;
}
