package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.nio.ByteBuffer;

public class SafeBufferTest {

    @Test
    public void testSafeBuffer() {
        ByteBuffer buf = ByteBuffer.allocate(10);
        
        buf.put((byte) 1);
        buf.put((byte) 2);
        assertEquals(2, buf.position());
        
        SafeBuffer.flip(buf);
        assertEquals(0, buf.position());
        assertEquals(2, buf.limit());
        
        SafeBuffer.position(buf, 1);
        assertEquals(1, buf.position());
        
        SafeBuffer.mark(buf);
        SafeBuffer.position(buf, 2);
        SafeBuffer.reset(buf);
        assertEquals(1, buf.position());
        
        SafeBuffer.rewind(buf);
        assertEquals(0, buf.position());
        
        SafeBuffer.limit(buf, 5);
        assertEquals(5, buf.limit());
        
        SafeBuffer.clear(buf);
        assertEquals(0, buf.position());
        assertEquals(10, buf.limit());
    }
}
