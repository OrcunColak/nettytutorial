package com.colak.netty.udprpc;

import lombok.RequiredArgsConstructor;


public record SolarisKey(
        short protocolNo,
        short subType,
        short messageNo) {
}
