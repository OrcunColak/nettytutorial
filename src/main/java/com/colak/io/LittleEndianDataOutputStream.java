package com.colak.io;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class LittleEndianDataOutputStream {

    private ByteBuffer byteBuffer;

    public LittleEndianDataOutputStream(int initialCapacity) {
        // Create a ByteBuffer  and set the byte order to little-endian
        this.byteBuffer = ByteBuffer.allocate(initialCapacity)
                .order(ByteOrder.LITTLE_ENDIAN);
    }

    public void writeByte(byte value) {
        ensureCapacity(1);
        byteBuffer.put(value);
    }

    public void writeShort(short value) {
        ensureCapacity(2);
        byteBuffer.putShort(value);
    }

    public void writeInt(int value) {
        ensureCapacity(4);
        byteBuffer.putInt(value);
    }

    public void writeLong(long value) {
        ensureCapacity(8);
        byteBuffer.putLong(value);
    }

    public void writeFloat(float value) {
        ensureCapacity(4);
        byteBuffer.putFloat(value);
    }

    public void writeDouble(double value) {
        ensureCapacity(8);
        byteBuffer.putDouble(value);
    }

    public void writeStringWithNullTerminator(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);

        // Ensure space for the string + null terminator
        ensureCapacity(bytes.length + 1);
        byteBuffer.put(bytes);

        // Null terminator (0x00)
        byteBuffer.put((byte) 0);
    }

    public byte[] flushAndGetBuffer() {
        // Create a byte array with the written data
        byte[] result = new byte[byteBuffer.position()];

        // Switch the buffer from writing mode to reading mode
        byteBuffer.flip();

        // Read buffer into result
        byteBuffer.get(result);

        // Reset buffer so new writes start from the beginning
        byteBuffer.clear();

        return result;
    }

    public int getWrittenBytesCount() {
        return byteBuffer.position();
    }

    private void ensureCapacity(int additionalBytes) {

        if (byteBuffer.remaining() < additionalBytes) {
            // If we come here it means there is not enough space
            int currentCapacity = byteBuffer.capacity();
            int requiredCapacity = byteBuffer.position() + additionalBytes;

            // Start with double capacity
            int newCapacity = currentCapacity * 2;

            // Ensure newCapacity is always at least the required capacity
            while (newCapacity < requiredCapacity) {
                newCapacity *= 2;
            }

            ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity)
                    .order(ByteOrder.LITTLE_ENDIAN);

            // Switch the buffer from writing mode to reading mode
            byteBuffer.flip();

            // Copy the old buffer into the new one
            newBuffer.put(byteBuffer);

            byteBuffer = newBuffer;

        }
    }
}
