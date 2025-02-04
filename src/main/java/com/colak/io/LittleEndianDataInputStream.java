package com.colak.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LittleEndianDataInputStream {

    private ByteBuffer byteBuffer;

    public void setData(byte[] data) {
        // Create a ByteBuffer from the byte array and set the byte order to little-endian
        this.byteBuffer = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    
}
