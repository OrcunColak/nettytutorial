package com.colak.netty.udprpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SolarisMessage {
    private final short protocolNo;

    private final short subType;

    private short messageNo;

    private short errorNo;
}
