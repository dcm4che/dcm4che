package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRJ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the org.dcm4che3.net.Association class
 */
public class AssociationTest {

    @Test(expected = BadSocketException.class)
    public void writeDimseRsp_pduEncoderThrowsException_performingRqCounterDecremented() throws IOException {
        Socket socket = new BadSocket();

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        Device device = new Device();
        device.setExecutor(executorService);

        Connection connection = new Connection();
        connection.setDevice(device);

        Association association = new Association(null, connection, socket);
        association.handle(new AAssociateAC());

        PresentationContext presentationContext = new PresentationContext(1, "as", "ts");
        Attributes cmd = new Attributes();
        cmd.setInt(Tag.CommandField, VR.US, 0x8021);
        cmd.setInt(Tag.Status, VR.US, Status.Success);

        int performingCount = association.getPerformingOperationCount();

        try {
            association.writeDimseRSP(presentationContext, cmd);
        } finally {
            Assert.assertEquals("Performing Operation count did not decrement",
                    performingCount - 1, association.getPerformingOperationCount());
            executorService.shutdownNow();
        }
    }

    private class BadSocket extends Socket {

        @Override public InputStream getInputStream() {
            return new DummyInputStream();
        }

        @Override public OutputStream getOutputStream() {
            return new BadOutputStream();
        }
    }

    private class DummyInputStream extends InputStream {

        Semaphore semaphore = new Semaphore(-1);

        @Override public int read() throws IOException {
            try {
                // Simulate the blocking read for nextPDU
                semaphore.acquire();
            } catch (InterruptedException e) {
            }
            return 0;
        }
    }

    private class BadOutputStream extends OutputStream {

        @Override public void write(int b) throws IOException {
            throw new BadSocketException();
        }
    }

    private class BadSocketException extends SocketException {
    }
}
