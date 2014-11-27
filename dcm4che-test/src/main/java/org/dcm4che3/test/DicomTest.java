package org.dcm4che3.test;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.hl7.MLLPConnection;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.junit.Before;
import org.junit.Rule;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class DicomTest {


    @Rule
    public DicomTestParameterInjectorRule parameterizer = new DicomTestParameterInjectorRule(this);

    @Before
    public void initConfig() {
        if (config == null) {
            config = new CommonDicomConfigurationWithHL7(
                    new SingleJsonFileConfigurationStorage("config.json"),
                    new ArrayList<Class<? extends DeviceExtension>>(),
                    new ArrayList<Class<? extends AEExtension>>(),
                    new ArrayList<Class<? extends HL7ApplicationExtension>>()
            );
        }
    }

    private DicomConfiguration config;

    /**
     * These are re-injected by the test runner before executing next test
     */
    private DicomParameters currentTestMethodParameters;
    private DicomParameters currentTestClassParameters;

    public void setCurrentTestMethodParameters(DicomParameters currentTestMethodParameters) {
        this.currentTestMethodParameters = currentTestMethodParameters;
    }

    public void setCurrentTestClassParameters(DicomParameters currentTestClassParameters) {
        this.currentTestClassParameters = currentTestClassParameters;
    }

    /**
     * Determines the source connection from the test parameters
     *
     * @return
     * @throws ConfigurationException
     */
    public Connection getSourceConnection() throws ConfigurationException {
        try {
            List<Connection> connections = getConfig().findDevice(dicomParams().sourceDeviceName()).getConnections();
            for (Connection connection : connections) {
                if (connection.getCommonName().equals(dicomParams().sourceConnectionCn()))
                    return connection;
            }
        } catch (Exception e) {
        }
        throw new RuntimeException("Source connection not found");
    }

    /**
     * Determines the target connection from the test parameters
     *
     * @return
     * @throws ConfigurationException
     */
    private Connection getTargetConnection() throws ConfigurationException {
        List<Connection> connections = getConfig().findDevice(dicomParams().targetDeviceName()).getConnections();
        for (Connection connection : connections) {
            if (connection.getCommonName().equals(dicomParams().targetConnectionCn()))
                return connection;
        }
        throw new RuntimeException("Target connection not found");
    }

    /**
     * Figures out the filename(s) that the tester specified in the parameters
     *
     * @return
     * @throws IOException
     */
    private List<String> testFileNameList() {
        // TODO: add support for alternatives, like specifying a whole directory, or just getting a file with the name equal to test method...
        if (dicomParams().files().length == 0)
            throw new RuntimeException("at least one file must be specified in the annotation!");
        return new ArrayList<String>(Arrays.asList(dicomParams().files()));
    }

    private byte[] readFile(String pathname) throws IOException {
        FileInputStream in = null;
        try {
            if (pathname.equals("-")) {
                in = new FileInputStream(FileDescriptor.in);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                StreamUtils.copy(in, buf);
                return buf.toByteArray();
            } else {
                File f = new File(pathname);
                in = new FileInputStream(f);
                byte[] b = new byte[(int) f.length()];
                StreamUtils.readFully(in, b, 0, b.length);
                return b;
            }
        } finally {
            SafeClose.close(in);
        }
    }


    @SuppressWarnings("unchecked")
    public void sendHL7() {

        Socket sock = null;
        Connection conn = null;

        try {
            conn = getSourceConnection();
            Connection remote = getTargetConnection();

            sock = conn.connect(remote);
            sock.setSoTimeout(conn.getResponseTimeout());
            MLLPConnection mllp = new MLLPConnection(sock);

            for (String filename : testFileNameList()) {
                mllp.writeMessage(readFile(filename));
                if (mllp.readMessage() == null)
                    throw new IOException("Connection closed by receiver");
            }

        } catch (Exception e) {
            throw new RuntimeException("Sending HL7 failed", e);
        } finally {
            if (conn != null && sock != null)
                conn.close(sock);
        }
    }


    public DicomConfiguration getConfig() {
        return config;
    }

    public DicomParameters dicomParams() {
        // TODO: merge class default params into test method params
        return currentTestMethodParameters;
    }


}
