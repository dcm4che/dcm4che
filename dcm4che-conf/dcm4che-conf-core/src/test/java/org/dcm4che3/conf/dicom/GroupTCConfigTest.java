package org.dcm4che3.conf.dicom;


import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.core.SimpleStorageTest;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Roman K
 */
public class GroupTCConfigTest {


    @Test
    public void groupTCConfigTest() throws ConfigurationException {

        CommonDicomConfigurationWithHL7 config = SimpleStorageTest.createCommonDicomConfiguration();

        // persist groups
        TCConfiguration.persistDefaultTCGroups(config);

        // persist device with group tc config
        Device device = new Device("myDevice");

        // tc group config
        ApplicationEntity ae = new ApplicationEntity("myAE");
        TCGroupConfigAEExtension ext = new TCGroupConfigAEExtension();
        ext.getScpTCs().put(TCGroupConfigAEExtension.DefaultGroup.QUERY_RETRIEVE.name(), new TCGroupConfigAEExtension.TCGroupDetails());
        ext.getScuTCs().put(TCGroupConfigAEExtension.DefaultGroup.STORAGE.name(), new TCGroupConfigAEExtension.TCGroupDetails());
        ae.addAEExtension(ext);
        device.addApplicationEntity(ae);

        // normal tc config
        ApplicationEntity ae1 = new ApplicationEntity("myAltAE");
        ae1.addTransferCapability(new TransferCapability("aRegularTC", UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP, UID.ImplicitVRLittleEndian));
        device.addApplicationEntity(ae1);

        if (config.findDevice("myDevice")!=null)
            config.merge(device); else
            config.persist(device);


        // load device and check some tcs
        Device loadedDevice = config.findDevice("myDevice");
        ApplicationEntity myAE = loadedDevice.getApplicationEntity("myAE");

        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP));
        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.ModalityWorklistInformationModelFIND, TransferCapability.Role.SCP));
        Assert.assertNull(myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCP));

        Assert.assertNull(myAE.getTransferCapabilityFor(UID.ModalityWorklistInformationModelFIND, TransferCapability.Role.SCU));
        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCU));
        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.VideoEndoscopicImageStorage, TransferCapability.Role.SCU));

        ApplicationEntity myAltAE = loadedDevice.getApplicationEntity("myAltAE");

        Assert.assertNotNull(myAltAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP));
        Assert.assertEquals(myAltAE.getTransferCapabilities().size(), 1);


        // persist device back with exclusions

        TCGroupConfigAEExtension tcAEExt = loadedDevice.getApplicationEntity("myAE").getAEExtension(TCGroupConfigAEExtension.class);
        tcAEExt.getScpTCs().get(TCGroupConfigAEExtension.DefaultGroup.QUERY_RETRIEVE.name()).getExcludedTransferCapabilities().add(UID.StudyRootQueryRetrieveInformationModelFIND);
        tcAEExt.getScuTCs().get(TCGroupConfigAEExtension.DefaultGroup.STORAGE.name()).getExcludedTransferSyntaxes().add(UID.JPEGBaseline1);

        config.merge(loadedDevice);

        // load again
        loadedDevice = config.findDevice("myDevice");
        myAE = loadedDevice.getApplicationEntity("myAE");

        Assert.assertNull(myAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP));

        ArrayList<String> tss = new ArrayList<String>(Arrays.asList(myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCU).getTransferSyntaxes()));

        Assert.assertFalse(tss.contains(UID.JPEGBaseline1));
        Assert.assertTrue(tss.contains(UID.JPEGExtended24));
    }

}
