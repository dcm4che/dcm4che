package org.dcm4che3.util;

import org.junit.Test;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import static org.junit.Assert.*;


public class SafeCloseTest {

    @Test
    public void testCloseCloseable() {
        class TestCloseable implements Closeable {
            boolean closed = false;
            @Override
            public void close() {
                closed = true;
            }
        }
        TestCloseable testCloseable = new TestCloseable();
        SafeClose.close(testCloseable);
        assertTrue(testCloseable.closed);
    }

    @Test
    public void testCloseCloseableNull() {
        SafeClose.close((Closeable) null);
    }

    @Test
    public void testCloseCloseableWithException() {
        Closeable testCloseable = () -> {
            throw new IOException();
        };
        SafeClose.close(testCloseable);
    }

    @Test
    public void testCloseSocket() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Socket socket = new Socket("localhost", serverSocket.getLocalPort());
            Socket clientSocket = serverSocket.accept();
            assertFalse(socket.isClosed());
            SafeClose.close(socket);
            assertTrue(socket.isClosed());
            SafeClose.close(clientSocket);
        }
    }

    @Test
    public void testCloseSocketNull() {
        SafeClose.close(null);
    }
}
