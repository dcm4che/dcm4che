package org.dcm4che3.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        device.setAssociationMonitor(new AssociationMonitor() {
            @Override public void onAssociationEstablished(Association as) {
            }

            @Override public void onAssociationFailed(Association as, Throwable e) {
            }

            @Override public void onAssociationRejected(Association as, AAssociateRJ aarj) {
            }

            @Override public void onAssociationAccepted(Association as) {
            }
        });

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
            executorService.shutdown();
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

        @Override public int read() throws IOException {
            try {
                // Just a short sleep to simulate the blocking read for nextPDU
                Thread.sleep(5000);
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
