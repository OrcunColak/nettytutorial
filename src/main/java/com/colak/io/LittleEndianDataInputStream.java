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

    private void ensureRemaining(int bytes) throws IOException {
        if (byteBuffer.remaining() < bytes) {
            throw new IOException("Insufficient bytes available to read");
        }
    }
}
