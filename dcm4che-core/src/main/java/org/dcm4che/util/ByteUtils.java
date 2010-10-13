package org.dcm4che.util;

public class ByteUtils {

    public static int bytesToVR(byte[] bytes, int off) {
        return bytesToUShortBE(bytes, off);
    }

    public static int bytesToUShort(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToUShortBE(bytes, off)
                         : bytesToUShortLE(bytes, off);
    }

    public static int bytesToUShortBE(byte[] bytes, int off) {
        return ((bytes[off] & 255) << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToUShortLE(byte[] bytes, int off) {
        return ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToShortBE(byte[] bytes, int off) {
        return (bytes[off] << 8) + (bytes[off + 1] & 255);
    }

    public static int bytesToShortLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 8)  + (bytes[off] & 255);
    }

    public static int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(bytes, off) : bytesToIntLE(bytes, off);
    }

    public static int bytesToIntBE(byte[] bytes, int off) {
        return (bytes[off] << 24) + ((bytes[off + 1] & 255) << 16)
                + ((bytes[off + 2] & 255) << 8) + (bytes[off + 3] & 255);
    }

    public static int bytesToIntLE(byte[] bytes, int off) {
        return (bytes[off + 3] << 24) + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8) + (bytes[off] & 255);
    }

    public static int bytesToTag(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToTagBE(bytes, off) : bytesToTagLE(bytes, off);
    }

    public static int bytesToTagBE(byte[] bytes, int off) {
        return bytesToIntBE(bytes, off);
    }

    public static int bytesToTagLE(byte[] bytes, int off) {
        return (bytes[off + 1] << 24) + ((bytes[off] & 255) << 16)
                + ((bytes[off + 3] & 255) << 8) + (bytes[off + 2] & 255);
    }

    public static float bytesToFloatBE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntBE(bytes, off));
    }

    public static float bytesToFloatLE(byte[] bytes, int off) {
        return Float.intBitsToFloat(bytesToIntLE(bytes, off));
    }

    public static double bytesToDoubleBE(byte[] bytes, int off) {
        long l = ((long) bytes[off] << 56)
                + ((long) (bytes[off + 1] & 255) << 48)
                + ((long) (bytes[off + 2] & 255) << 40)
                + ((long) (bytes[off + 3] & 255) << 32)
                + ((long) (bytes[off + 4] & 255) << 24)
                + ((bytes[off + 5] & 255) << 16)
                + ((bytes[off + 6] & 255) << 8)
                + (bytes[off + 7] & 255);
        return Double.longBitsToDouble(l);
    }

    public static double bytesToDoubleLE(byte[] bytes, int off) {
        long l = ((long) bytes[off + 7] << 56)
                + ((long) (bytes[off + 6] & 255) << 48)
                + ((long) (bytes[off + 5] & 255) << 40)
                + ((long) (bytes[off + 4] & 255) << 32)
                + ((long) (bytes[off + 3] & 255) << 24)
                + ((bytes[off + 2] & 255) << 16)
                + ((bytes[off + 1] & 255) << 8)
                + (bytes[off] & 255);
        return Double.longBitsToDouble(l);
    }

    public static byte[] shortToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte) (i >> 8);
        bytes[off + 1] = (byte) i;
        return bytes;
    }

    public static byte[] shortToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (i >> 8);
        bytes[off] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytesBE(int i, byte[] bytes, int off) {
        bytes[off] = (byte) (i >> 24);
        bytes[off + 1] = (byte) (i >> 16);
        bytes[off + 2] = (byte) (i >> 8);
        bytes[off + 3] = (byte) i;
        return bytes;
    }

    public static byte[] intToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 3] = (byte) (i >> 24);
        bytes[off + 2] = (byte) (i >> 16);
        bytes[off + 1] = (byte) (i >> 8);
        bytes[off] = (byte) i;
        return bytes;
    }

    public static byte[] tagToBytesBE(int i, byte[] bytes, int off) {
        return intToBytesBE(i, bytes, off);
    }

    public static byte[] tagToBytesLE(int i, byte[] bytes, int off) {
        bytes[off + 1] = (byte) (i >> 24);
        bytes[off] = (byte) (i >> 16);
        bytes[off + 3] = (byte) (i >> 8);
        bytes[off + 2] = (byte) i;
        return bytes;
    }

    public static byte[] floatToBytesBE(float f, byte[] bytes, int off) {
        return intToBytesBE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] floatToBytesLE(float f, byte[] bytes, int off) {
        return intToBytesLE(Float.floatToIntBits(f), bytes, off);
    }

    public static byte[] doubleToBytesBE(double d, byte[] bytes, int off) {
        long l = Double.doubleToLongBits(d);
        bytes[off] = (byte) (l >> 56);
        bytes[off + 1] = (byte) (l >> 48);
        bytes[off + 2] = (byte) (l >> 40);
        bytes[off + 3] = (byte) (l >> 32);
        bytes[off + 4] = (byte) (l >> 24);
        bytes[off + 5] = (byte) (l >> 16);
        bytes[off + 6] = (byte) (l >> 8);
        bytes[off + 7] = (byte) l;
        return bytes;
    }

    public static byte[] doubleToBytesLE(double d, byte[] bytes, int off) {
        long l = Double.doubleToLongBits(d);
        bytes[off + 7] = (byte) (l >> 56);
        bytes[off + 6] = (byte) (l >> 48);
        bytes[off + 5] = (byte) (l >> 40);
        bytes[off + 4] = (byte) (l >> 32);
        bytes[off + 3] = (byte) (l >> 24);
        bytes[off + 2] = (byte) (l >> 16);
        bytes[off + 1] = (byte) (l >> 8);
        bytes[off] = (byte) l;
        return bytes;
    }

    public static byte[] swapShorts(byte[] bytes) {
        int len = bytes.length;
        checkLength(len, 2);
        for (int off = 0; off < len; off += 2)
            swap(bytes, off, off+1);
        return bytes;
    }

    public static byte[] swapInts(byte[] bytes) {
        int len = bytes.length;
        checkLength(len, 4);
        for (int off = 0; off < len; off += 4) {
            swap(bytes, off, off+3);
            swap(bytes, off+1, off+2);
        }
        return bytes;
    }

    public static byte[] swapLongs(byte[] bytes) {
        int len = bytes.length;
        checkLength(len, 8);
        for (int off = 0; off < len; off += 8) {
            swap(bytes, off, off+7);
            swap(bytes, off+1, off+6);
            swap(bytes, off+2, off+5);
            swap(bytes, off+3, off+4);
        }
        return bytes;
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    private static void swap(byte[] bytes, int a, int b) {
        byte t = bytes[a];
        bytes[a] = bytes[b];
        bytes[b] = t;
    }

}
