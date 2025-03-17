package com.colak.io;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class LittleEndianDataOutputStreamStringTest {


    @Test
    void testWriteNullTerminatedString() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream();
        stream.writeNullTerminatedString("Hello");

        byte[] buffer = stream.flushAndGetBuffer();
        byte[] expected = "Hello\0".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expected, buffer);
    }

}
