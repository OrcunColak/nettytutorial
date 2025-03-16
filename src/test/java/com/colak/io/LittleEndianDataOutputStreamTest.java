package com.colak.io;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LittleEndianDataOutputStreamTest {

    @Test
    void testWriteByte() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeByte((byte) 0x12);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(new byte[]{0x12}, buffer);
    }

    @Test
    void testWriteUnsignedByte() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);

        stream.writeUnsignedByte((short) 255);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(new byte[]{(byte) 0xFF}, buffer);
    }

    @Test
    void testWriteShort() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeShort((short) 0x1234);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(new byte[]{0x34, 0x12}, buffer);  // Little-endian order
    }

    @Test
    void testWriteInt() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeInt(0x12345678);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(new byte[]{0x78, 0x56, 0x34, 0x12}, buffer);
    }

    @Test
    void testWriteLong() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeLong(0x0123456789ABCDEFL);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(new byte[]{
                (byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89,
                (byte) 0x67, (byte) 0x45, (byte) 0x23, (byte) 0x01
        }, buffer);
    }

    @Test
    void testWriteFloat() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeFloat(1.0f);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(1.0f).array(), buffer);
    }

    @Test
    void testWriteDouble() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeDouble(1.0);

        byte[] buffer = stream.flushAndGetBuffer();
        assertArrayEquals(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putDouble(1.0).array(), buffer);
    }

    @Test
    void testWriteNullTerminatedString() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(20);
        stream.writeNullTerminatedString("Hello");

        byte[] buffer = stream.flushAndGetBuffer();
        byte[] expected = "Hello\0".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expected, buffer);
    }

    @Test
    void testEnsureCapacityExpansion() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(4);
        stream.writeInt(0x12345678);  // 4 bytes (fits exactly)
        stream.writeByte((byte) 0x99); // Should trigger buffer expansion

        byte[] buffer = stream.flushAndGetBuffer();

        assertArrayEquals(new byte[]{0x78, 0x56, 0x34, 0x12, (byte) 0x99}, buffer);
    }

    @Test
    void testGetWrittenBytesCount() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeByte((byte) 0x12);
        stream.writeShort((short) 0x3456);
        stream.writeInt(0x789ABCDE);

        assertEquals(7, stream.getWrittenBytesCount());
    }

    @Test
    void testFlushAndGetBufferResetsForNewWrites() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream(10);
        stream.writeByte((byte) 0x12);
        byte[] firstFlush = stream.flushAndGetBuffer();

        assertArrayEquals(new byte[]{0x12}, firstFlush);
        assertEquals(0, stream.getWrittenBytesCount()); // Buffer should be reset

        stream.writeByte((byte) 0x34);
        byte[] secondFlush = stream.flushAndGetBuffer();

        assertArrayEquals(new byte[]{0x34}, secondFlush);
    }
}
