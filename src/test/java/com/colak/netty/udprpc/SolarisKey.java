package com.colak.netty.udprpc;

public record SolarisKey(
        short protocolNo,
        short subType,
        short messageNo) {
}
