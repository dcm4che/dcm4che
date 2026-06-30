package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.io.Closeable;
import java.net.Socket;

public class SafeCloseTest {

    private boolean closed = false;

    @Test
    public void testCloseCloseable() {
        Closeable c = () -> closed = true;
        SafeClose.close(c);
        assertTrue(closed);
    }

    @Test
    public void testCloseCloseableNull() {
        SafeClose.close((Closeable) null);
        // Should not throw exception
    }

    @Test
    public void testCloseCloseableWithException() {
        Closeable c = () -> {
            throw new IOException();
        };
        SafeClose.close(c);
        // Should not throw exception
    }

    @Test
    public void testCloseSocketNull() {
        SafeClose.close(null);
        // Should not throw exception
    }
}
