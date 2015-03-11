package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.core.SimpleStorageTest;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roman K
 */
@RunWith(JUnit4.class)
public class DicomConfigurationTest {

    @Test
    public void renameAETest() throws ConfigurationException {

        CommonDicomConfigurationWithHL7 config = SimpleStorageTest.createCommonDicomConfiguration();


        // create device
        String aeRenameTestDevice = "AERenameTestDevice";

        Device testDevice = createDevice(aeRenameTestDevice);

        config.removeDevice(aeRenameTestDevice);
        config.persist(testDevice);

        // replace connection
        testDevice.getApplicationEntity("aet1").setAETitle("aet2");
        config.merge(testDevice);

        // see if there is only aet2
        Device deviceLoaded = config.findDevice(aeRenameTestDevice);

        Assert.assertEquals("There must stay only 1 ae", 1, deviceLoaded.getApplicationEntities().size());

        Assert.assertEquals("The new aet must have 1 connection", 1, deviceLoaded.getApplicationEntity("aet2").getConnections().size());

    }

    private Device createDevice(String aeRenameTestDevice) {
        Device testDevice = new Device(aeRenameTestDevice);
        Connection connection = new Connection();
        connection.setProtocol(Connection.Protocol.DICOM);
        connection.setCommonName("myConn");
        connection.setHostname("localhost");

        ApplicationEntity ae = new ApplicationEntity();
        List<Connection> list = new ArrayList<Connection>();
        list.add(connection);

        testDevice.addConnection(connection);
        ae.setConnections(list);
        ae.setAETitle("aet1");
        testDevice.addApplicationEntity(ae);
        return testDevice;
    }
}
