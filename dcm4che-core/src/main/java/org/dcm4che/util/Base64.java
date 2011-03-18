package org.dcm4che.util;

import java.io.IOException;
import java.io.OutputStream;

public class Base64 {

    private static final char[] BASE64 = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

    private static final byte INV_BASE64[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest,
            int destPos) {
        if (srcPos < 0 || srcLen < 0 || srcLen > src.length - srcPos)
            throw new IndexOutOfBoundsException();
        int destLen = (srcLen * 4 / 3 + 3) & ~3;
        if (destPos < 0 || destLen > dest.length - destPos)
            throw new IndexOutOfBoundsException();
        byte b1, b2, b3;
        int n = srcLen / 3;
        int r = srcLen - 3 * n;
        while (n-- > 0) {
            dest[destPos++] = BASE64[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = BASE64[((b1 & 0x03) << 4)
                                   | (((b2 = src[srcPos++]) >>> 4) & 0x0F)];
            dest[destPos++] = BASE64[((b2 & 0x0F) << 2)
                                   | (((b3 = src[srcPos++]) >>> 6) & 0x03)];
            dest[destPos++] = BASE64[b3 & 0x3F];
        }
        if (r > 0)
            if (r == 1) {
                dest[destPos++] = BASE64[((b1 = src[srcPos]) >>> 2) & 0x3F];
                dest[destPos++] = BASE64[((b1 & 0x03) << 4)];
                dest[destPos++] = '=';
                dest[destPos++] = '=';
            } else {
                dest[destPos++] = BASE64[((b1 = src[srcPos++]) >>> 2) & 0x3F];
                dest[destPos++] = BASE64[((b1 & 0x03) << 4)
                                       | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
                dest[destPos++] = BASE64[(b2 & 0x0F) << 2];
                dest[destPos++] = '=';
            }
     }

    public static void decode(char[] ch, int off, int len, OutputStream out)
            throws IOException {
        byte b2, b3;
        while ((len -= 2) >= 0) {
            out.write((byte)((INV_BASE64[ch[off++]] << 2)
                     | ((b2 = INV_BASE64[ch[off++]]) >>> 4)));
            if ((len-- == 0) || ch[off] == '=')
                break;
            out.write((byte)((b2 << 4)
                     | ((b3 = INV_BASE64[ch[off++]]) >>> 2)));
            if ((len-- == 0) || ch[off] == '=')
                break;
            out.write((byte)((b3 << 6) | INV_BASE64[ch[off++]]));
        }
    }

}
