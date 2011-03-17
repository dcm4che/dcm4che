package org.dcm4che.util;

import java.io.OutputStream;

public class Base64 {

    private static final char[] BASE64 = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

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
        switch (r) {
        case 1:
            dest[destPos++] = BASE64[((b1 = src[srcPos]) >>> 2) & 0x3F];
            dest[destPos++] = BASE64[((b1 & 0x03) << 4)];
            dest[destPos++] = '=';
            dest[destPos++] = '=';
            break;
        case 2:
            dest[destPos++] = BASE64[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = BASE64[((b1 & 0x03) << 4)
                                   | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
            dest[destPos++] = BASE64[(b2 & 0x0F) << 2];
            dest[destPos++] = '=';
            break;
        }
     }

    public static void decode(char[] ch, int off, int len, char[] carry,
            int carryLen, OutputStream bout) {
        // TODO Auto-generated method stub
        
    }

}
