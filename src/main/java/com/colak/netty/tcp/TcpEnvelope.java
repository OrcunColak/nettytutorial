package com.colak.netty.tcp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TcpEnvelope<T> {
    private final T payload;
    private final String connectionId;

    @Override
    public String toString() {
        return "TcpEnvelope{" +
               "payload=" + payload +
               ", connectionId='" + connectionId + '\'' +
               '}';
    }
}
