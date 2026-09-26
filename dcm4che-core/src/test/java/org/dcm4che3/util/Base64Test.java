package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Base64Test {

    @Test
    public void testEncode() {
        byte[] src = "Hello".getBytes();
        char[] dest = new char[8];
        Base64.encode(src, 0, src.length, dest, 0);
        assertEquals("SGVsbG8=", new String(dest));
    }

    @Test
    public void testDecode() throws IOException {
        char[] src = "SGVsbG8=".toCharArray();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Base64.decode(src, 0, src.length, out);
        assertEquals("Hello", out.toString());
    }

    @Test
    public void testEncodeDecodeEmpty() throws IOException {
        byte[] src = new byte[0];
        char[] dest = new char[0];
        Base64.encode(src, 0, 0, dest, 0);
        assertEquals("", new String(dest));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Base64.decode(dest, 0, 0, out);
        assertEquals("", out.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testEncodeIndexOutOfBounds() {
        Base64.encode(new byte[5], 0, 6, new char[8], 0);
    }
}
