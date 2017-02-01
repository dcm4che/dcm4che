package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.DicomConfigOptions;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.normalization.DefaultsAndNullFilterDecorator;
import org.dcm4che3.conf.core.storage.InMemoryConfiguration;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

public class ReferenceResolutionTest {


    @Test
    public void testReferenceResolution() throws Exception {


        Configuration storage = new ReferenceIndexingDecorator(
                new InMemoryConfiguration(),
                new HashMap<String, Path>()
        );

        CommonDicomConfigurationWithHL7 dicomConfiguration = new CommonDicomConfigurationWithHL7(
                storage,
                new HashMap<Class, List<Class>>()
        );

        Device dev = new Device("dev");
        ApplicationEntity ae1 = new ApplicationEntity("AE1");
        dev.setDefaultAE(ae1);

        dicomConfiguration.persist(dev);

        // should fail, referenced ae not persisted
        try {
            dicomConfiguration.findDevice("dev");
            Assert.fail();
        } catch (Exception ignored) {
        }

        DicomConfigOptions options = new DicomConfigOptions();
        options.setIgnoreUnresolvedReferences(true);

        // should work
        Device loadedWithNoAeRef = dicomConfiguration.findDevice("dev", options);
        Assert.assertNull(loadedWithNoAeRef.getDefaultAE());


        // now lets add it for real
        dev.addApplicationEntity(ae1);
        dicomConfiguration.merge(dev);

        // both should work
        Device loaded1 = dicomConfiguration.findDevice("dev");
        Device loaded2 = dicomConfiguration.findDevice("dev", options);

        Assert.assertEquals("AE1", loaded1.getDefaultAE().getAETitle());
        Assert.assertEquals("AE1", loaded2.getDefaultAE().getAETitle());


    }
}
