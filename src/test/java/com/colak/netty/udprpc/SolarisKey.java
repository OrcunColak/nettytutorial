package com.colak.netty.udprpc;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SolarisKey {
    private final short protocolNo;
    private final short subType;
    private final short messageNo;
}
