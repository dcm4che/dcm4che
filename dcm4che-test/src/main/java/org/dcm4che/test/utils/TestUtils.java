package org.dcm4che.test.utils;

import java.util.ArrayList;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.conf.AttributeFilter;
import org.dcm4chee.archive.conf.Entity;

public class TestUtils {

    public static void addTagToAttributesFilter(Entity entity, DicomConfiguration remoteConfig
            , int tagToAdd, VR vr) throws ConfigurationException {
        Device dev = remoteConfig.findDevice("dcm4chee-arc");
        ArchiveDeviceExtension arcDevExt = dev
                .getDeviceExtension(ArchiveDeviceExtension.class);
        AttributeFilter filter = arcDevExt.getAttributeFilter(Entity.Instance);
        filter.setCustomAttribute1(new ValueSelector(null, tagToAdd, vr, 0, new ItemPointer(Tag.ConceptNameCodeSequence, 0)));
        remoteConfig.merge(dev);
    }
}
