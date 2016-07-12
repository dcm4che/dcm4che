package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.util.PathFollower;
import org.dcm4che3.net.Connection;
import org.junit.Assert;
import org.junit.Test;

import java.util.Deque;

/**
 * Created by aprvf on 27.06.2016.
 */
public class FollowerTest {

    @Test
    public void testSimple() throws Exception {


        Deque<ConfigProperty> configProperties = PathFollower.traceProperties(
                DicomConfigurationRoot.class,
                new Path("dicomConfigurationRoot", "dicomDevicesRoot", "dcm4chee-arc", "dicomConnection", 2)
        );

        Assert.assertEquals(Connection.class, configProperties.getLast().getRawClass());
    }

    @Test
    public void testWrong() throws Exception {


        try {
            PathFollower.traceProperties(
                    DicomConfigurationRoot.class,
                    new Path("dicomConfigurationRoot", 1, "dcm4chee-arc", "dicomConnection", 2)
            );
        } catch (Exception e) {
        }

        try {
            PathFollower.traceProperties(
                    DicomConfigurationRoot.class,
                    new Path("dicomConfigurationRoot", "dicomDevicesRoot", "dcm4chee-arc", "dicomConnection", "sff")
            );
        } catch (Exception e) {
        }

    }



}
