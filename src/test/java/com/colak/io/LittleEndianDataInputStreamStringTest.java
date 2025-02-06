package com.colak.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class LittleEndianDataInputStreamStringTest {


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
    public void testReadStringWithNullTerminator_NoNullTerminator() {
        byte[] data = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello" without null terminator
        LittleEndianDataInputStream inputStream = new LittleEndianDataInputStream(data);

        IOException thrown = assertThrows(IOException.class, inputStream::readNullTerminatedString);
        assertEquals("End of stream reached unexpectedly while reading string", thrown.getMessage());
    }

    @Test
    public void testReadTwoStringsWithNullTerminator() throws IOException {
        // Test data: Two strings with null terminators
        String inputString1 = "Hello";
        String inputString2 = "World";

        // Create byte array: "Hello\0World\0"
        byte[] data = (inputString1 + "\0" + inputString2 + "\0").getBytes(StandardCharsets.US_ASCII);

        LittleEndianDataInputStream dataInputStream = new LittleEndianDataInputStream(data);

        // Read first string: Expect "Hello"
        String result1 = dataInputStream.readNullTerminatedString();
        assertEquals(inputString1, result1, "The first string should be 'Hello'");

        // Read second string: Expect "World"
        String result2 = dataInputStream.readNullTerminatedString();
        assertEquals(inputString2, result2, "The second string should be 'World'");
    }

    @Test
    public void testReadTwoStrings_NoNullTerminatorOnSecondString() {
        // Test data: Two strings, with the second string not null-terminated
        String inputString1 = "Hello";
        String inputString2 = "World";  // Second string without a null terminator

        // Create byte array: "Hello\0World" (second string doesn't have a null terminator)
        byte[] data = (inputString1 + "\0" + inputString2).getBytes(StandardCharsets.US_ASCII);

        LittleEndianDataInputStream dataInputStream = new LittleEndianDataInputStream(data);

        try {
            // Read first string: Expect "Hello"
            String result1 = dataInputStream.readNullTerminatedString();
            assertEquals(inputString1, result1, "The first string should be 'Hello'");

            // Try to read second string: This should throw an exception because it's not null-terminated
            assertThrows(IOException.class, dataInputStream::readNullTerminatedString,
                    "An IOException should be thrown when the second string does not have a null terminator.");
        } catch (IOException e) {
            fail("IOException was thrown unexpectedly during the test: " + e.getMessage());
        }
    }

}
