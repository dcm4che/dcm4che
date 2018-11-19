package org.dcm4che3.net;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AssociationTest {

    private static final String HOST = "localhost";
    private static final Integer PORT = 12456;

    private static final String CALLING_AET = "NICE_SCU";
    private static final String CALLED_AET = "NAUGHTY_SCP";

    private static Throwable uncaughtException;
    private static NaughtyScp naughtyScp;

    @BeforeClass
    public static void startNaughtyScp() throws IOException {
        naughtyScp = new NaughtyScp();
        naughtyScp.start();
    }

    @AfterClass
    public static void stopNaughtyScp() {
        naughtyScp.stop();
        if (uncaughtException != null) {
            uncaughtException.printStackTrace();
            fail("Caught Exception in NaughtyScp");
        }
    }

    @Test
    public void testAdditionalPresentationContextsInAssociateAccept() throws Exception {
        ApplicationEntity localAe = createLocalApplicationEntity();
        ApplicationEntity remoteAe = createRemoteApplicationEntity();
        
        // Request only one Presentation Context; NaughtyScp will accept two
        AAssociateRQ associateRequest = createAssociationRequest(
            new TransferCapability(UID.MRImageStorage, UID.MRImageStorage, Role.SCU, UID.ImplicitVRLittleEndian)
        );

        Association association = localAe.connect(remoteAe, associateRequest);
        association.release();
    }

    private ApplicationEntity createLocalApplicationEntity() throws IOException, GeneralSecurityException {
        Device device = new Device();
        Connection connection = new Connection();
        ApplicationEntity applicationEntity = new ApplicationEntity(CALLING_AET);
        device.addConnection(connection);
        device.addApplicationEntity(applicationEntity);
        applicationEntity.addConnection(connection);

        device.setExecutor(Executors.newSingleThreadExecutor());
        device.setScheduledExecutor(Executors.newSingleThreadScheduledExecutor());
        device.bindConnections();
        return applicationEntity;
    }

    private ApplicationEntity createRemoteApplicationEntity() {
        Device device = new Device();
        Connection connection = createRemoteConnection();
        ApplicationEntity applicationEntity = new ApplicationEntity(CALLED_AET);
        device.addConnection(connection);
        applicationEntity.addConnection(connection);

        return applicationEntity;
    }
    
    private Connection createRemoteConnection() {
        Connection connection = new Connection();
        connection.setHostname(HOST);
        connection.setPort(PORT);
        return connection;
    }

    private AAssociateRQ createAssociationRequest(TransferCapability... transferCapabilities) {
        AAssociateRQ associateRequest = new AAssociateRQ();
        for (TransferCapability transferCapability : transferCapabilities) {
            String sopClass = transferCapability.getSopClass();
            for (String transferSyntax : transferCapability.getTransferSyntaxes()) {
                associateRequest.addPresentationContextFor(sopClass, transferSyntax);
            }
        }
        return associateRequest;
    }

    private static class NaughtyScp {

        private final ServerSocket serverSocket;
        private byte[] associateAcceptWithAdditionalPresentationContext;
        private Thread thread;
        private boolean stopped = false;

        public NaughtyScp() throws IOException {
            serverSocket = new ServerSocket(PORT);
            associateAcceptWithAdditionalPresentationContext = getBytesFromResource("associateAcceptWithTwoPresentationContexts.bin");
        }

        private byte[] getBytesFromResource(String name) throws IOException {
            InputStream inputStream = AssociationTest.class.getResourceAsStream(name);
            return IOUtils.toByteArray(inputStream);
        }

        public void start() {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (!stopped) {
                            handleAssociateRequest();
                        }
                    } catch (InterruptedException exception) {
                        // expected, just set interrupted flag
                        Thread.currentThread().interrupt();
                    }
                }
            });

            thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    uncaughtException = throwable;
                }
            });

            thread.start();
        }

        private void handleAssociateRequest() throws InterruptedException {
            try {
                Socket socket = serverSocket.accept();
                OutputStream outputStream = socket.getOutputStream();
                Thread.sleep(500);
                outputStream.write(associateAcceptWithAdditionalPresentationContext);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        public void stop() {
            stopped = true;
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}
