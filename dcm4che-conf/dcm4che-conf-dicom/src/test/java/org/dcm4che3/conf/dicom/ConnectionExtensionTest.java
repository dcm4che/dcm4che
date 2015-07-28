package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.extensions.CommonConnectionExtension;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.misc.DeepEquals;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.ConnectionExtension;
import org.dcm4che3.net.Device;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class ConnectionExtensionTest {


    @ConfigurableClass
    public static class MyConnectionExtension1 extends CommonConnectionExtension {

        @ConfigurableProperty
        private String myProperty;

        public String getMyProperty() {
            return myProperty;
        }

        public void setMyProperty(String myProperty) {
            this.myProperty = myProperty;
        }
    }

    @ConfigurableClass
    public static class MyConnectionExtension2 extends CommonConnectionExtension {

        @ConfigurableProperty
        private String myOtherProperty;

        public String getMyOtherProperty() {
            return myOtherProperty;
        }

        public void setMyOtherProperty(String myOtherProperty) {
            this.myOtherProperty = myOtherProperty;
        }
    }


    @Test
    public void test() throws ConfigurationException, IOException, GeneralSecurityException {
        System.setProperty("org.dcm4che.conf.filename", "target/config.json");

        CommonDicomConfigurationWithHL7 configurationWithHL7 = new DicomConfigurationBuilder(System.getProperties())
                .registerExtensionForBaseExtension(MyConnectionExtension1.class, ConnectionExtension.class)
                .registerExtensionForBaseExtension(MyConnectionExtension2.class, ConnectionExtension.class)
                .build();

        Device aDevice = new Device("aDevice");

        // 1 extension
        Connection conn = new Connection("myConn1", "local");
        MyConnectionExtension1 myConnectionExtension1 = new MyConnectionExtension1();
        myConnectionExtension1.setMyProperty("aValue1");
        conn.addExtension(myConnectionExtension1);

        aDevice.addConnection(conn);

        // 2 extension
        Connection conn2 = new Connection("myConn2", "remote");

        MyConnectionExtension1 connectionExtension21 = new MyConnectionExtension1();
        connectionExtension21.setMyProperty("myValue2");
        MyConnectionExtension2 connectionExtension22 = new MyConnectionExtension2();
        connectionExtension22.setMyOtherProperty("myValue3");

        conn2.addExtension(connectionExtension21);
        conn2.addExtension(connectionExtension22);

        aDevice.addConnection(conn2);


        // no extension
        aDevice.addConnection(new Connection("myConn3", "0.0.0.0"));

        configurationWithHL7.purgeConfiguration();
        configurationWithHL7.persist(aDevice);

        Device loaded = configurationWithHL7.findDevice("aDevice");

        Assert.assertTrue(DeepEquals.deepEquals(loaded, aDevice));

        // try reconfigure
        aDevice.reconfigure(loaded);
        Assert.assertTrue(DeepEquals.deepEquals(loaded, aDevice));

        // try reconfigure an empty device
        Device aDevice1 = new Device("aDevice");
        aDevice1.reconfigure(loaded);

        Assert.assertTrue(DeepEquals.deepEquals(loaded, aDevice1));

    }

}
