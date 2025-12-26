package com.colak.network.requestresponse;

import java.io.IOException;
import java.net.DatagramPacket;

@FunctionalInterface
public interface ResponseValidator {
    boolean isValidResponse(DatagramPacket datagramPacket) throws IOException;
}
