package com.colak.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class LittleEndianDataInputStreamTest {

    private LittleEndianDataInputStream inputStream;

    @BeforeEach
    public void setUp() {
        inputStream = new LittleEndianDataInputStream();
    }

    @Test
    public void testReadByte() throws IOException {
        byte[] data = new byte[]{0x01};
        inputStream.setData(data);

        byte result = inputStream.readByte();
        assertEquals(0x01, result);
    }

    @Test
    public void testReadShort() throws IOException {
        byte[] data = new byte[]{0x01, 0x02};  // Little-endian representation of 0x0201
        inputStream.setData(data);

        short result = inputStream.readShort();
        assertEquals(0x0201, result);
    }

    @Test
    public void testReadInt() throws IOException {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04};  // Little-endian representation of 0x04030201
        inputStream.setData(data);

        int result = inputStream.readInt();
        assertEquals(0x04030201, result);
    }

    @Test
    public void testReadLong() throws IOException {
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};  // Little-endian representation of 0x0807060504030201
        inputStream.setData(data);

        long result = inputStream.readLong();
        assertEquals(0x0807060504030201L, result);
    }

    @Test
    public void testReadByteArray() throws IOException {
        byte[] data = new byte[]{0x01, 0x02, 0x03};
        inputStream.setData(data);

        byte[] result = inputStream.readByteArray(3);
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, result);
    }

    @Test
    public void testReadStringWithNullTerminator_Success() throws IOException {
        byte[] data = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x00}; // "Hello\0"
        inputStream.setData(data);

        String result = inputStream.readStringWithNullTerminator();
        assertEquals("Hello", result);
    }

    @Test
    public void testReadStringWithNullTerminator_EmptyString() throws IOException {
        byte[] data = new byte[]{0x00}; // Empty string followed by null terminator
        inputStream.setData(data);

        String result = inputStream.readStringWithNullTerminator();
        assertEquals("", result);
    }

    @Test
    public void testReadStringWithNullTerminator_NoNullTerminator() {
        byte[] data = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello" without null terminator
        inputStream.setData(data);

        IOException thrown = assertThrows(IOException.class, () -> inputStream.readStringWithNullTerminator());
        assertEquals("End of stream reached unexpectedly while reading string", thrown.getMessage());
    }

    @Test
    public void testReadTwoStringsWithNullTerminator() throws IOException {
        // Test data: Two strings with null terminators
        String inputString1 = "Hello";
        String inputString2 = "World";

        // Create byte array: "Hello\0World\0"
        byte[] data = (inputString1 + "\0" + inputString2 + "\0").getBytes(StandardCharsets.US_ASCII);

        LittleEndianDataInputStream dataInputStream = new LittleEndianDataInputStream();
        dataInputStream.setData(data);

        // Read first string: Expect "Hello"
        String result1 = dataInputStream.readStringWithNullTerminator();
        assertEquals(inputString1, result1, "The first string should be 'Hello'");

        // Read second string: Expect "World"
        String result2 = dataInputStream.readStringWithNullTerminator();
        assertEquals(inputString2, result2, "The second string should be 'World'");
    }

    @Test
    public void testReadTwoStrings_NoNullTerminatorOnSecondString() {
        // Test data: Two strings, with the second string not null-terminated
        String inputString1 = "Hello";
        String inputString2 = "World";  // Second string without a null terminator

        // Create byte array: "Hello\0World" (second string doesn't have a null terminator)
        byte[] data = (inputString1 + "\0" + inputString2).getBytes(StandardCharsets.US_ASCII);

        LittleEndianDataInputStream dataInputStream = new LittleEndianDataInputStream();
        dataInputStream.setData(data);

        try {
            // Read first string: Expect "Hello"
            String result1 = dataInputStream.readStringWithNullTerminator();
            assertEquals(inputString1, result1, "The first string should be 'Hello'");

            // Try to read second string: This should throw an exception because it's not null-terminated
            assertThrows(IOException.class, dataInputStream::readStringWithNullTerminator,
                    "An IOException should be thrown when the second string does not have a null terminator.");
        } catch (IOException e) {
            fail("IOException was thrown unexpectedly during the test: " + e.getMessage());
        }
    }

    @Test
    public void testEnsureRemaining_InsufficientBytes() {
        byte[] data = new byte[]{0x01};
        inputStream.setData(data);

        IOException thrown = assertThrows(IOException.class, () -> inputStream.readInt());
        assertEquals("Insufficient bytes available to read", thrown.getMessage());
    }
}
