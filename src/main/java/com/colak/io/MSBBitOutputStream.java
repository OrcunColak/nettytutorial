package com.colak.io;

import lombok.Getter;

// MSB (Most significant bit) bit stream writer
@Getter
public class MSBBitOutputStream {
    private final byte[] buffer;
    private int byteIndex;
    private int bitPosition;

    public MSBBitOutputStream(int size) {
        // Pre-allocate buffer
        this.buffer = new byte[size];
        this.byteIndex = 0;

        // Start at MSB
        this.bitPosition = 7;
    }

    public void writeBits(int value, int numBits) {
        if (numBits < 1 || numBits > 8) {
            throw new IllegalArgumentException("Number of bits must be between 1 and 8.");
        }

        for (int index = numBits - 1; index >= 0; index--) {
            // Ensure there is enough space before writing
            if (byteIndex >= buffer.length) {
                throw new IndexOutOfBoundsException("Buffer overflow: Too many bits written");
            }

            // Extract the bit
            int bit = (value >> index) & 1;
            // Place the bit
            buffer[byteIndex] |= (byte) (bit << bitPosition);
            bitPosition--;

            if (bitPosition < 0) {
                byteIndex++;
                bitPosition = 7; // Reset MSB
            }
        }
    }

    public int getSize() {
        // Return actual used size
        return byteIndex + (bitPosition < 7 ? 1 : 0);
    }
}
