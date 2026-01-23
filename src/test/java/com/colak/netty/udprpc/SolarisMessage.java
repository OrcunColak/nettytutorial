package com.colak.netty.udprpc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SolarisMessage {
    private final int protocolNo;
    private final int subType;
    private int messageNo;
    private int errorNo;
}
