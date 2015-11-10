/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.dicom;


import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

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
        ext.getScpTCs().put(TCGroupConfigAEExtension.DefaultGroup.QUERY.name(), new TCGroupConfigAEExtension.TCGroupDetails());
        ext.getScpTCs().put(TCGroupConfigAEExtension.DefaultGroup.RETRIEVE.name(), new TCGroupConfigAEExtension.TCGroupDetails());
        ext.getScuTCs().put(TCGroupConfigAEExtension.DefaultGroup.STORAGE.name(), new TCGroupConfigAEExtension.TCGroupDetails());
        ae.addAEExtension(ext);
        device.addApplicationEntity(ae);

        // normal tc config
        ApplicationEntity ae1 = new ApplicationEntity("myAltAE");
        ae1.addTransferCapability(new TransferCapability("aRegularTC", UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP, UID.ImplicitVRLittleEndian));
        device.addApplicationEntity(ae1);

        config.merge(device);

        // load device and check some tcs
        Device loadedDevice = config.findDevice("myDevice");
        ApplicationEntity myAE = loadedDevice.getApplicationEntity("myAE");

        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP));
        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelGET, TransferCapability.Role.SCP));
        Assert.assertNull(myAE.getTransferCapabilityFor(UID.ModalityWorklistInformationModelFIND, TransferCapability.Role.SCP));
        Assert.assertNull(myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCP));

        Assert.assertNull(myAE.getTransferCapabilityFor(UID.ModalityWorklistInformationModelFIND, TransferCapability.Role.SCU));
        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCU));
        Assert.assertNotNull(myAE.getTransferCapabilityFor(UID.VideoEndoscopicImageStorage, TransferCapability.Role.SCU));

        ApplicationEntity myAltAE = loadedDevice.getApplicationEntity("myAltAE");

        Assert.assertNotNull(myAltAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP));
        Assert.assertEquals(myAltAE.getTransferCapabilities().size(), 1);


        // persist device back with exclusions

        TCGroupConfigAEExtension tcAEExt = loadedDevice.getApplicationEntity("myAE").getAEExtension(TCGroupConfigAEExtension.class);
        tcAEExt.getScpTCs().get(TCGroupConfigAEExtension.DefaultGroup.QUERY.name()).getExcludedTransferCapabilities().add(UID.StudyRootQueryRetrieveInformationModelFIND);
        tcAEExt.getScpTCs().get(TCGroupConfigAEExtension.DefaultGroup.RETRIEVE.name()).getExcludedTransferCapabilities().add(UID.StudyRootQueryRetrieveInformationModelFIND);
        tcAEExt.getScuTCs().get(TCGroupConfigAEExtension.DefaultGroup.STORAGE.name()).getExcludedTransferSyntaxes().add(UID.JPEGBaseline1);

        config.merge(loadedDevice);

        // load again
        loadedDevice = config.findDevice("myDevice");
        myAE = loadedDevice.getApplicationEntity("myAE");

        Assert.assertNull(myAE.getTransferCapabilityFor(UID.StudyRootQueryRetrieveInformationModelFIND, TransferCapability.Role.SCP));

        ArrayList<String> tss = new ArrayList<String>(Arrays.asList(myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCU).getTransferSyntaxes()));

        Assert.assertFalse(tss.contains(UID.JPEGBaseline1));
        Assert.assertTrue(tss.contains(UID.JPEGExtended24));


        // try whitelisting
        tcAEExt = loadedDevice.getApplicationEntity("myAE").getAEExtension(TCGroupConfigAEExtension.class);
        TCGroupConfigAEExtension.TCGroupDetails tcGroupDetails = new TCGroupConfigAEExtension.TCGroupDetails();
        tcAEExt.getScpTCs().put(TCGroupConfigAEExtension.DefaultGroup.STORAGE.name(), tcGroupDetails);

        tcGroupDetails.getExcludedTransferSyntaxes().clear();
        tcGroupDetails.getWhitelistedTransferSyntaxes().add(UID.ImplicitVRLittleEndian);

        config.merge(loadedDevice);

        loadedDevice = config.findDevice("myDevice");
        myAE = loadedDevice.getApplicationEntity("myAE");
        TransferCapability loadedTCs = myAE.getTransferCapabilityFor(UID.ComputedRadiographyImageStorage, TransferCapability.Role.SCP);

        Assert.assertTrue(loadedTCs.containsTransferSyntax(UID.ImplicitVRLittleEndian));
        Assert.assertFalse(loadedTCs.containsTransferSyntax(UID.JPEGExtended24));
        Assert.assertFalse(loadedTCs.containsTransferSyntax(UID.ExplicitVRLittleEndian));
        Assert.assertFalse(loadedTCs.containsTransferSyntax(UID.DeflatedExplicitVRLittleEndian));
        Assert.assertFalse(loadedTCs.containsTransferSyntax(UID.ExplicitVRBigEndianRetired));

    }

}
