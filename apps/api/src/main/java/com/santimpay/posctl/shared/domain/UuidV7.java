package com.santimpay.posctl.shared.domain;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Minimal UUIDv7 (time-ordered) generator — better B-tree index locality than v4 and safe to expose.
 * Layout per RFC 9562: 48-bit Unix ms timestamp, 4-bit version (7), 12+62 bits randomness.
 */
public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {}

    public static UUID generate() {
        long now = System.currentTimeMillis();
        byte[] bytes = new byte[16];

        // 48-bit timestamp
        bytes[0] = (byte) (now >>> 40);
        bytes[1] = (byte) (now >>> 32);
        bytes[2] = (byte) (now >>> 24);
        bytes[3] = (byte) (now >>> 16);
        bytes[4] = (byte) (now >>> 8);
        bytes[5] = (byte) now;

        // randomness
        byte[] rand = new byte[10];
        RANDOM.nextBytes(rand);
        System.arraycopy(rand, 0, bytes, 6, 10);

        // version 7 in the high nibble of byte 6
        bytes[6] = (byte) ((bytes[6] & 0x0F) | 0x70);
        // variant (10xx) in the high bits of byte 8
        bytes[8] = (byte) ((bytes[8] & 0x3F) | 0x80);

        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xFF);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xFF);
        }
        return new UUID(msb, lsb);
    }
}
