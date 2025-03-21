package com.colak.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// A similar class exists in Guava
public class LittleEndianDataInputStream {

    private ByteBuffer byteBuffer;

    public LittleEndianDataInputStream() {
    }

    public LittleEndianDataInputStream(byte[] data) {
        setData(data);
    }

    public int getRemainingBytesCount() {
        return byteBuffer.remaining();
    }

    public void setPosition(int newPosition) {
        byteBuffer.position(newPosition);
    }

    public void setData(byte[] data) {
        // Create a ByteBuffer from the byte array and set the byte order to little-endian
        this.byteBuffer = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte readByte() throws IOException {
        ensureRemaining(1);
        return byteBuffer.get();
    }

    public short readUnsignedByte() throws IOException {
        ensureRemaining(1);
        return (short) (byteBuffer.get() & 0xFF);
    }

    public short readShort() throws IOException {
        ensureRemaining(2);
        return byteBuffer.getShort();
    }

    public int readInt() throws IOException {
        ensureRemaining(4);
        return byteBuffer.getInt();
    }

    public long readLong() throws IOException {
        ensureRemaining(8);
        return byteBuffer.getLong();
    }

    public float readFloat() throws IOException {
        ensureRemaining(4);
        return byteBuffer.getFloat();
    }

    public double readDouble() throws IOException {
        ensureRemaining(8);
        return byteBuffer.getDouble();
    }

    public byte[] readByteArray(int length) throws IOException {
        ensureRemaining(length);

        byte[] result = new byte[length];
        byteBuffer.get(result);
        return result;
    }

    public String readNullTerminatedString() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int byteRead = -1;

        // Read until null byte or end of stream
        while (byteBuffer.hasRemaining()) {
            byteRead = byteBuffer.get();

            // If we encounter the null terminator (0x00), stop reading
            if (byteRead == 0) {
                break;
            }

            // Append the character
            stringBuilder.append((char) byteRead);
        }

        // If we have not encountered the null terminator and the buffer is exhausted, throw an exception
        if (byteRead != 0) {
            throw new IOException("End of stream reached unexpectedly while reading string");
        }

        return stringBuilder.toString();
    }

    public String readNullTerminatedString(int fixedLength) throws IOException {
        int startPos = byteBuffer.position();

        // Read the null-terminated string using the existing method
        String result = readNullTerminatedString();

        // Calculate how many bytes were actually read (including null terminator)
        int bytesRead = byteBuffer.position() - startPos;

        // Move the position forward to ensure we consume exactly fixedLength + 1 bytes
        int remaining = (fixedLength + 1) - bytesRead;
        if (remaining > 0) {
            byteBuffer.position(byteBuffer.position() + remaining);
        }

        return result;
    }


    private void ensureRemaining(int bytes) throws IOException {
        if (byteBuffer.remaining() < bytes) {
            throw new IOException("Insufficient bytes available to read");
        }
    }
}
