package com.colak.netty.udprpc;

public record SolarisKey(
        int protocolNo,
        int subType,
        int messageNo) {
}
