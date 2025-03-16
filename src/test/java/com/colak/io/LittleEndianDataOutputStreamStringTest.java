package com.colak.io;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LittleEndianDataOutputStreamStringTest {


    @Test
    void testWriteNullTerminatedString() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream();
        stream.writeNullTerminatedString("Hello");

        byte[] buffer = stream.flushAndGetBuffer();
        byte[] expected = "Hello\0".getBytes(StandardCharsets.US_ASCII);

        assertArrayEquals(expected, buffer);
    }

    @Test
    void shouldWriteNullTerminatedStringWithPadding() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream();

        stream.writeNullTerminatedString("Test", 10);
        byte[] result = stream.flushAndGetBuffer();

        // Expected output: "Test" (ASCII: 84, 101, 115, 116) + null (0) + padding (6 nulls)
        byte[] expected = {84, 101, 115, 116, 0, 0, 0, 0, 0, 0, 0};

        assertArrayEquals(expected, result);
    }

    @Test
    void shouldThrowExceptionWhenStringTooLong() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> stream.writeNullTerminatedString("ThisIsTooLong", 5));
        assertTrue(exception.getMessage().contains("String is too long for the fixed length"));
    }

    @Test
    void shouldWriteExactSizeStringWithOnlyOneNullTerminator() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream();

        stream.writeNullTerminatedString("Hello", 6);
        byte[] result = stream.flushAndGetBuffer();

        // Expected output: "Hello" + null terminator, exactly 7 bytes
        byte[] expected = {72, 101, 108, 108, 111, 0, 0}; // "Hello\0"

        assertArrayEquals(expected, result);
    }

    @Test
    void shouldWriteOnlyNullTerminatorWhenEmptyString() {
        LittleEndianDataOutputStream stream = new LittleEndianDataOutputStream();

        stream.writeNullTerminatedString("", 5);
        byte[] result = stream.flushAndGetBuffer();

        // Expected output: null terminator + 5 null padding
        byte[] expected = {0, 0, 0, 0, 0, 0};

        assertArrayEquals(expected, result);
    }


}
