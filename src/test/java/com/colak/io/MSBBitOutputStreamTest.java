package com.colak.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MSBBitOutputStreamTest {

    @Test
    void testWriteBits() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Writing 5 bits: 10101
        stream.writeBits(0b10101, 5);
        assertEquals(1, stream.getSize());

        // Compare as unsigned int
        byte[] buffer = stream.getBuffer();
        assertEquals(168, Byte.toUnsignedInt(buffer[0]));

        // Writing another 3 bits: 110
        stream.writeBits(0b110, 3);
        assertEquals(1, stream.getSize());

        // Compare as unsigned int
        assertEquals(174, Byte.toUnsignedInt(buffer[0]));
    }

    @Test
    void testWriteEightOnesIndividually() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Writing 8 individual bits: 1 one at a time
        for (int i = 0; i < 8; i++) {
            stream.writeBits(1, 1);
        }

        assertEquals(1, stream.getSize()); // Should use exactly 1 byte
        assertEquals(255, Byte.toUnsignedInt(stream.getBuffer()[0])); // Ensure all bits are set to 1
    }

    @Test
    void testWriteAlternatingOnesAndZeros() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Writing 8 alternating bits: 1 0 1 0 1 0 1 0 (0b10101010)
        stream.writeBits(1, 1);
        stream.writeBits(0, 1);
        stream.writeBits(1, 1);
        stream.writeBits(0, 1);
        stream.writeBits(1, 1);
        stream.writeBits(0, 1);
        stream.writeBits(1, 1);
        stream.writeBits(0, 1);

        assertEquals(1, stream.getSize()); // Should use exactly 1 byte
        assertEquals(170, Byte.toUnsignedInt(stream.getBuffer()[0])); // 0b10101010 = 170
    }

    @Test
    void testWriteFourOnesAndFourZeros() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Writing 4 ones: 1 1 1 1
        stream.writeBits(1, 1);
        stream.writeBits(1, 1);
        stream.writeBits(1, 1);
        stream.writeBits(1, 1);

        // Writing 4 zeros: 0 0 0 0
        stream.writeBits(0, 1);
        stream.writeBits(0, 1);
        stream.writeBits(0, 1);
        stream.writeBits(0, 1);

        assertEquals(1, stream.getSize()); // Should use exactly 1 byte
        assertEquals(240, Byte.toUnsignedInt(stream.getBuffer()[0])); // 0b11110000 = 240
    }

    @Test
    void testWriteFourZerosAndOneOne() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Writing 4 zeros: 0 0 0 0
        stream.writeBits(0, 1);
        stream.writeBits(0, 1);
        stream.writeBits(0, 1);
        stream.writeBits(0, 1);

        // Writing 1 one: 1
        stream.writeBits(1, 1);

        assertEquals(1, stream.getSize()); // Should use exactly 1 byte
        assertEquals(8, Byte.toUnsignedInt(stream.getBuffer()[0])); // 0b00001000 = 8
    }


    @Test
    void testWriteMultipleBytes() {
        MSBBitOutputStream stream = new MSBBitOutputStream(2);

        // Writing 8 bits: 11001100
        stream.writeBits(0b11001100, 8);
        assertEquals(1, stream.getSize());
        byte[] buffer = stream.getBuffer();
        assertEquals(204, Byte.toUnsignedInt(buffer[0]));

        // Writing another 8 bits: 10101010
        stream.writeBits(0b10101010, 8);
        assertEquals(2, stream.getSize());
        assertEquals(170, Byte.toUnsignedInt(buffer[1]));
    }

    @Test
    void testBitPositionWrapsCorrectly() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Writing 8 bits: 11110000
        stream.writeBits(0b11110000, 8);
        assertEquals(1, stream.getSize());
        byte[] buffer = stream.getBuffer();
        assertEquals(240, Byte.toUnsignedInt(buffer[0]));
    }

    @Test
    void testIllegalBitCount() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        assertThrows(IllegalArgumentException.class, () -> stream.writeBits(0b1, 0));
        assertThrows(IllegalArgumentException.class, () -> stream.writeBits(0b1, 9));
    }

    @Test
    void testBufferOverflow() {
        MSBBitOutputStream stream = new MSBBitOutputStream(1);

        // Fill the buffer
        stream.writeBits(0b11111111, 8);

        // Next write should overflow
        assertThrows(IndexOutOfBoundsException.class, () -> stream.writeBits(0b1, 1));
    }
}
