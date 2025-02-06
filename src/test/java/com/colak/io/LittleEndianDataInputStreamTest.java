package com.colak.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LittleEndianDataInputStreamTest {

    @Test
    public void testReadByte() throws IOException {
        byte[] data = new byte[]{0x01};
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        byte result = inputStream.readByte();
        assertEquals(0x01, result);
    }

    @Test
    public void testReadShort() throws IOException {
        byte[] data = new byte[]{0x01, 0x02};  // Little-endian representation of 0x0201
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        short result = inputStream.readShort();
        assertEquals(0x0201, result);
    }

    @Test
    public void testReadInt() throws IOException {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04};  // Little-endian representation of 0x04030201
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        int result = inputStream.readInt();
        assertEquals(0x04030201, result);
    }

    @Test
    public void testReadLong() throws IOException {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};  // Little-endian representation of 0x0807060504030201
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        long result = inputStream.readLong();
        assertEquals(0x0807060504030201L, result);
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        byte[] result = inputStream.readByteArray(3);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, result);
    }

    @Test
    public void testReadStringWithNullTerminator_Success() throws IOException {
        byte[] data = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x00}; // "Hello\0"
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        String result = inputStream.readNullTerminatedString();
        assertEquals("Hello", result);
    }

    @Test
    public void testReadStringWithNullTerminator_EmptyString() throws IOException {
        byte[] data = new byte[]{0x00}; // Empty string followed by null terminator
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        String result = inputStream.readNullTerminatedString();
        assertEquals("", result);
    }

    @Test
    public void testEnsureRemaining_InsufficientBytes() {
        byte[] data = new byte[]{0x01};
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        IOException thrown = assertThrows(IOException.class, inputStream::readInt);
        assertEquals("Insufficient bytes available to read", thrown.getMessage());
    }
}
