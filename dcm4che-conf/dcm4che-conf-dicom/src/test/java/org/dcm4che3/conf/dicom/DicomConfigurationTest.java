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
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableClassExtension;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.configclasses.SomeDeviceExtension;
import org.dcm4che3.net.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

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

    @Test
    public void listDevices_accepts_single_primaryDeviceType() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device originalDevice = createDeviceWithArchiveExtension("DEVICE", "Default", "Other", "Storage", DeviceType.ARCHIVE, DeviceType.PRINT);
        config.persist(originalDevice);

        Device[] devices = config.listDevices(DeviceType.ARCHIVE);
        Assert.assertEquals("Method listDevices should have retrieved exactly 1 Device", 1, devices.length);

        Device device = devices[0];

        Assert.assertTrue("Device retrieved from listDevices does not match the expected original Device", deepCompare(originalDevice, device));
        Assert.assertTrue("Device retrieved does not contain expected ARCHIVE DeviceType", Arrays.asList(device.getPrimaryDeviceTypes()).contains(DeviceType.ARCHIVE.toString()));
        Assert.assertTrue("Device retrieved does not contain expected PRINT DeviceType", Arrays.asList(device.getPrimaryDeviceTypes()).contains(DeviceType.PRINT.toString()));
        Assert.assertTrue("Device retrieved does not contain expected ExternalArchiveAEExtension values", device.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));
    }

    @Test
    public void listDevices_accepts_multiple_primaryDeviceTypes() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device archiveDevice = createDeviceWithArchiveExtension("DEVICE1", "Default1", "Other1", "Storage1", DeviceType.ARCHIVE);
        Device printDevice = createDeviceWithArchiveExtension("DEVICE2", "Default2", "Other2", "Storage2", DeviceType.PRINT);
        Device printAndArchiveDevice = createDeviceWithArchiveExtension("DEVICE3", "Default3", "Other3", "Storage3", DeviceType.PRINT, DeviceType.ARCHIVE);
        config.persist(archiveDevice);
        config.persist(printDevice);
        config.persist(printAndArchiveDevice);

        Device[] devices = config.listDevices(new DeviceType[] {DeviceType.ARCHIVE, DeviceType.PRINT, null, DeviceType.ARCHIVE} );
        Assert.assertEquals("Method listDevices should have retrieved exactly 3 Devices", 3, devices.length);

        Device listedArchiveDevice = null;
        Device listedPrintDevice = null;
        Device listedPrintAndArchiveDevice = null;

        for (Device device : devices) {
            List<String> deviceTypes = Arrays.asList(device.getPrimaryDeviceTypes());
            if (deviceTypes.contains(DeviceType.ARCHIVE.toString())) {
                if (deviceTypes.contains(DeviceType.PRINT.toString())) {
                    listedPrintAndArchiveDevice = device;
                } else {
                    listedArchiveDevice = device;
                }
            } else {
                listedPrintDevice = device;
            }
        }

        Assert.assertTrue("Device of type ARCHIVE retrieved from listDevices does not match the expected original Device",deepCompare(listedArchiveDevice, archiveDevice));
        Assert.assertTrue("Device of type ARCHIVE retrieved does not contain expected ARCHIVE DeviceType", Arrays.asList(listedArchiveDevice.getPrimaryDeviceTypes()).contains(DeviceType.ARCHIVE.toString()));
        Assert.assertTrue("Device of type ARCHIVE retrieved does not contain expected ExternalArchiveAEExtension values", listedArchiveDevice.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));

        Assert.assertTrue("Device of type PRINT retrieved from listDevices does not match the expected original Device",deepCompare(listedPrintDevice, printDevice));
        Assert.assertTrue("Device of type PRINT retrieved does not contain expected PRINT DeviceType", Arrays.asList(listedPrintDevice.getPrimaryDeviceTypes()).contains(DeviceType.PRINT.toString()));
        Assert.assertTrue("Device of type PRINT retrieved does not contain expected ExternalArchiveAEExtension values", listedPrintDevice.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));

        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved from listDevices does not match the expected original Device",deepCompare(listedPrintAndArchiveDevice, printAndArchiveDevice));
        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved does not contain expected PRINT DeviceType", Arrays.asList(listedPrintAndArchiveDevice.getPrimaryDeviceTypes()).contains(DeviceType.PRINT.toString()));
        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved does not contain expected ARCHIVE DeviceType", Arrays.asList(listedPrintAndArchiveDevice.getPrimaryDeviceTypes()).contains(DeviceType.ARCHIVE.toString()));
        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved does not contain expected ExternalArchiveAEExtension values", listedPrintAndArchiveDevice.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));
    }

    @Test
    public void listDevices_accepts_null_primaryDeviceType() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device originalDevice = createDeviceWithArchiveExtension("DEVICE", "Default", "Other", "Storage", DeviceType.ARCHIVE, DeviceType.PRINT);
        config.persist(originalDevice);

        DeviceType deviceType = null;
        Device[] devices = config.listDevices(deviceType);

        Assert.assertTrue("listDevices should accept null DeviceType and return an empty array", devices != null && devices.length == 0);
    }

    @Test
    public void listDevices_accepts_null_primaryDeviceTypes() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device originalDevice = createDeviceWithArchiveExtension("DEVICE", "Default", "Other", "Storage", DeviceType.ARCHIVE, DeviceType.PRINT);
        config.persist(originalDevice);

        DeviceType[] deviceTypes = null;
        Device[] devices = config.listDevices(deviceTypes);

        Assert.assertTrue("listDevices should accept null DeviceType array and return an empty array", devices != null && devices.length == 0);
    }

    @Test
    public void listDevices_accepts_empty_primaryDeviceTypes() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device originalDevice = createDeviceWithArchiveExtension("DEVICE", "Default", "Other", "Storage", DeviceType.ARCHIVE, DeviceType.PRINT);
        config.persist(originalDevice);

        DeviceType[] deviceTypes = new DeviceType[0];
        Device[] devices = config.listDevices(deviceTypes);

        Assert.assertTrue("listDevices should accept empty DeviceType array and return an empty array", devices != null && devices.length == 0);
    }

    @Test
    public void listDevices_produces_equivalent_device_objects_as_findDevice() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device originalDevice = createDeviceWithArchiveExtension("DEVICE", "Default", "Other", "Storage", DeviceType.ARCHIVE, DeviceType.PRINT);
        config.persist(originalDevice);

        Device foundDevice = config.findDevice(originalDevice.getDeviceName());

        Device[] listedDevices = config.listDevices(DeviceType.ARCHIVE);
        Assert.assertEquals("Method listDevices should have retrieved exactly 1 Device", 1, listedDevices.length);

        Device listedDevice = listedDevices[0];
        Assert.assertTrue("Device retrieved from listDevices does not match the expected Device retrieved from findDevice", deepCompare(foundDevice, listedDevice));

        listedDevices = config.listDevices(new DeviceType[] {DeviceType.ARCHIVE} );
        Assert.assertEquals("Method listDevices should have retrieved exactly 1 Device", 1, listedDevices.length);

        listedDevice = listedDevices[0];
        Assert.assertTrue("Device retrieved from listDevices does not match the expected Device retrieved from findDevice", deepCompare(foundDevice, listedDevice));
    }

    @Test
    public void listAllDevices_returns_all_devices() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device archiveDevice = createDeviceWithArchiveExtension("DEVICE1", "Default1", "Other1", "Storage1", DeviceType.ARCHIVE);
        Device printDevice = createDeviceWithArchiveExtension("DEVICE2", "Default2", "Other2", "Storage2", DeviceType.PRINT);
        Device printAndArchiveDevice = createDeviceWithArchiveExtension("DEVICE3", "Default3", "Other3", "Storage3", DeviceType.PRINT, DeviceType.ARCHIVE);
        config.persist(archiveDevice);
        config.persist(printDevice);
        config.persist(printAndArchiveDevice);

        Device[] devices = config.listAllDevices();
        Assert.assertEquals("Method listDevices should have retrieved exactly 3 Devices", 3, devices.length);

        Device listedArchiveDevice = null;
        Device listedPrintDevice = null;
        Device listedPrintAndArchiveDevice = null;

        for (Device device : devices) {
            List<String> deviceTypes = Arrays.asList(device.getPrimaryDeviceTypes());
            if (deviceTypes.contains(DeviceType.ARCHIVE.toString())) {
                if (deviceTypes.contains(DeviceType.PRINT.toString())) {
                    listedPrintAndArchiveDevice = device;
                } else {
                    listedArchiveDevice = device;
                }
            } else {
                listedPrintDevice = device;
            }
        }

        Assert.assertTrue("Device of type ARCHIVE retrieved from listAllDevices does not match the expected original Device",deepCompare(listedArchiveDevice, archiveDevice));
        Assert.assertTrue("Device of type ARCHIVE retrieved from listAllDevices does not contain expected ARCHIVE DeviceType", Arrays.asList(listedArchiveDevice.getPrimaryDeviceTypes()).contains(DeviceType.ARCHIVE.toString()));
        Assert.assertTrue("Device of type ARCHIVE retrieved from listAllDevices does not contain expected ExternalArchiveAEExtension values", listedArchiveDevice.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));

        Assert.assertTrue("Device of type PRINT retrieved from listAllDevices does not match the expected original Device",deepCompare(listedPrintDevice, printDevice));
        Assert.assertTrue("Device of type PRINT retrieved from listAllDevices does not contain expected PRINT DeviceType", Arrays.asList(listedPrintDevice.getPrimaryDeviceTypes()).contains(DeviceType.PRINT.toString()));
        Assert.assertTrue("Device of type PRINT retrieved from listAllDevices does not contain expected ExternalArchiveAEExtension values", listedPrintDevice.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));

        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved from listAllDevices does not match the expected original Device",deepCompare(listedPrintAndArchiveDevice, printAndArchiveDevice));
        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved from listAllDevices does not contain expected PRINT DeviceType", Arrays.asList(listedPrintAndArchiveDevice.getPrimaryDeviceTypes()).contains(DeviceType.PRINT.toString()));
        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved from listAllDevices does not contain expected ARCHIVE DeviceType", Arrays.asList(listedPrintAndArchiveDevice.getPrimaryDeviceTypes()).contains(DeviceType.ARCHIVE.toString()));
        Assert.assertTrue("Device of type PRINT and ARCHIVE retrieved from listAllDevices does not contain expected ExternalArchiveAEExtension values", listedPrintAndArchiveDevice.getApplicationEntities().stream().anyMatch(aet -> {
            ExternalArchiveAEExtension extension = aet.getAEExtension(ExternalArchiveAEExtension.class);
            return extension != null && extension.isDefaultForStorage();
        }));
    }

    @Test
    public void listAllDevices_produces_equivalent_device_objects_as_findDevice() {
        CommonDicomConfiguration config = SimpleStorageTest.createCommonDicomConfiguration();
        config.purgeConfiguration();

        Device originalDevice = createDeviceWithArchiveExtension("DEVICE", "Default", "Other", "Storage", DeviceType.ARCHIVE, DeviceType.PRINT);
        config.persist(originalDevice);

        Device[] listedDevices = config.listAllDevices();
        Assert.assertEquals("Method listAllDevices should have retrieved exactly 1 Device", 1, listedDevices.length);

        Device listedDevice = listedDevices[0];
        Device foundDevice = config.findDevice(originalDevice.getDeviceName());

        Assert.assertTrue("Device retrieved from listAllDevices does not match the expected Device retrieved from findDevice", deepCompare(foundDevice, listedDevice));
    }

    @Test
    public void testSearchByUUID() throws ConfigurationException {
        CommonDicomConfigurationWithHL7 config = SimpleStorageTest.createCommonDicomConfiguration();

        config.purgeConfiguration();

        Device device = new Device("ABC");
        ApplicationEntity ae1 = new ApplicationEntity("myAE1");
        ApplicationEntity ae2 = new ApplicationEntity("myAE2");


        String uuid1 = ae1.getUuid();
        String uuid2 = ae2.getUuid();

        device.addApplicationEntity(ae1);
        device.addApplicationEntity(ae2);
        config.persist(device);

        Device device2 = new Device("CDE");
        ApplicationEntity ae3 = new ApplicationEntity("myAE3");

//        String devUUID = device2.getUuid();

        String uuid3 = ae3.getUuid();

        device2.addApplicationEntity(ae3);
        config.persist(device2);

        Assert.assertEquals("myAE1", config.findApplicationEntityByUUID(uuid1).getAETitle());
        Assert.assertEquals("myAE2", config.findApplicationEntityByUUID(uuid2).getAETitle());
        Assert.assertEquals("myAE3", config.findApplicationEntityByUUID(uuid3).getAETitle());

//        Assert.assertEquals("CDE", config.findDeviceByUUID(devUUID).getDeviceName());


        try {
            config.findApplicationEntityByUUID("nonexistent");
            Assert.fail("An AE should have not been found");
        } catch (ConfigurationNotFoundException e) {
            // noop
        }
    }

    @Test
    public void testByAnyUUIDSearch() {

        CommonDicomConfigurationWithHL7 config = SimpleStorageTest.createCommonDicomConfiguration();

        config.purgeConfiguration();

        Device device = new Device("ABC3");

        String createdDeviceUUid = device.getUuid();

        ApplicationEntity ae1 = new ApplicationEntity("myAE1");
        ApplicationEntity ae2 = new ApplicationEntity("myAE2");

        String uuid1 = ae1.getUuid();
        String uuid2 = ae2.getUuid();

        device.addApplicationEntity(ae1);
        device.addApplicationEntity(ae2);
        config.persist(device);

        config.persist(new Device("ABC1"));
        config.persist(new Device("ABC2"));
        config.persist(new Device("ABC4"));

        String foundDeviceUUID1 = (String) config.getConfigurationStorage().search(DicomPath.DeviceUUIDByAnyUUID.set("UUID", uuid1).path()).next();
        String foundDeviceUUID2 = (String) config.getConfigurationStorage().search(DicomPath.DeviceUUIDByAnyUUID.set("UUID", uuid2).path()).next();

        Assert.assertEquals(createdDeviceUUid, foundDeviceUUID1);
        Assert.assertEquals(createdDeviceUUid, foundDeviceUUID2);
    }


    @ConfigurableClass
    public static class AEExtensionWithReferences extends AEExtension {


        @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.Reference)
        private ApplicationEntity anotherAERef;

        @ConfigurableProperty(type = ConfigurableProperty.ConfigurablePropertyType.Reference)
        private Device deviceRef;

        public AEExtensionWithReferences() {
        }

        public AEExtensionWithReferences(ApplicationEntity anotherAERef) {
            this.anotherAERef = anotherAERef;
        }

        public AEExtensionWithReferences(ApplicationEntity anotherAERef, Device deviceRef) {
            this.anotherAERef = anotherAERef;
            this.deviceRef = deviceRef;
        }

        public ApplicationEntity getAnotherAERef() {
            return anotherAERef;
        }

        public void setAnotherAERef(ApplicationEntity anotherAERef) {
            this.anotherAERef = anotherAERef;
        }

        public Device getDeviceRef() {
            return deviceRef;
        }

        public void setDeviceRef(Device deviceRef) {
            this.deviceRef = deviceRef;
        }

    }


    @Test
    public void testAEReference() throws ConfigurationException {

        CommonDicomConfigurationWithHL7 commonDicomConfiguration = SimpleStorageTest.createCommonDicomConfiguration();

        commonDicomConfiguration.purgeConfiguration();

        Device oneDeviceWithAE = new Device("oneDeviceWithAE");
        ApplicationEntity myAE = new ApplicationEntity("myAE");
        oneDeviceWithAE.addApplicationEntity(myAE);

        SomeDeviceExtension someDeviceExtension = new SomeDeviceExtension();
        someDeviceExtension.setReferencedEntity(myAE);
        oneDeviceWithAE.addDeviceExtension(someDeviceExtension);

        Device anotherDeviceWithRef = new Device("anotherDeviceWithRef");
        SomeDeviceExtension ext = new SomeDeviceExtension();
        ext.setReferencedEntity(myAE);
        anotherDeviceWithRef.addDeviceExtension(ext);


        commonDicomConfiguration.persist(anotherDeviceWithRef);
        commonDicomConfiguration.persist(oneDeviceWithAE);

        Device loaded = commonDicomConfiguration.findDevice("anotherDeviceWithRef");

        Device loadedWithSelfRef = commonDicomConfiguration.findDevice("oneDeviceWithAE");

        ApplicationEntity referencedEntity = loaded.getDeviceExtension(SomeDeviceExtension.class).getReferencedEntity();
        Assert.assertEquals(referencedEntity.getAETitle(), "myAE");

        ApplicationEntity referencedEntity1 = loadedWithSelfRef.getDeviceExtension(SomeDeviceExtension.class).getReferencedEntity();
        Assert.assertEquals(referencedEntity1.getAETitle(), "myAE");


    }

    @Test
    public void testAECrossRef() {
        CommonDicomConfigurationWithHL7 config = prepareTestConfigWithRefs();

        Device theCoreDevice = new Device("TheCoreDevice");
        ApplicationEntity ae1 = new ApplicationEntity("theFirstAE");
        ApplicationEntity ae2 = new ApplicationEntity("theSecondAE");

        ae1.addAEExtension(new AEExtensionWithReferences(ae2));
        ae2.addAEExtension(new AEExtensionWithReferences(ae1));

        theCoreDevice.addApplicationEntity(ae1);
        theCoreDevice.addApplicationEntity(ae2);

        config.persist(theCoreDevice);

        Device loadedDevice = config.findDevice("TheCoreDevice");

        Assert.assertEquals(
                "TheCoreDevice",
                loadedDevice
                        .getApplicationEntity("theFirstAE")
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getDevice()
                        .getDeviceName()
        );
        Assert.assertEquals(
                "TheCoreDevice",
                loadedDevice
                        .getApplicationEntity("theSecondAE")
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getDevice()
                        .getDeviceName()
        );
    }

    private CommonDicomConfigurationWithHL7 prepareTestConfigWithRefs() {
        List<ConfigurableClassExtension> list = new ArrayList<ConfigurableClassExtension>();
        list.add(new AEExtensionWithReferences());

        CommonDicomConfigurationWithHL7 config = SimpleStorageTest.createCommonDicomConfiguration(list);

        config.purgeConfiguration();
        return config;
    }

    @Test
    public void testAEselfRef() {
        CommonDicomConfigurationWithHL7 config = prepareTestConfigWithRefs();

        Device theCoreDevice = new Device("TheCoreDevice");
        ApplicationEntity ae1 = new ApplicationEntity("theFirstAE");
        ae1.addAEExtension(new AEExtensionWithReferences(ae1));
        theCoreDevice.addApplicationEntity(ae1);
        config.persist(theCoreDevice);

        Device loadedDevice = config.findDevice("TheCoreDevice");

        Assert.assertEquals(
                "TheCoreDevice",
                loadedDevice
                        .getApplicationEntity("theFirstAE")
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getDevice()
                        .getDeviceName()
        );
    }

    @Test
    public void testLongRefChain() {
        CommonDicomConfigurationWithHL7 config = prepareTestConfigWithRefs();

        Device theCoreDevice = new Device("TheCoreDevice");
        Device theSecondDevice = new Device("TheSecondDevice");
        ApplicationEntity ae5 = new ApplicationEntity("theThirdAE");
        theSecondDevice.setDefaultAE(ae5);
        theSecondDevice.addApplicationEntity(ae5);
        config.persist(theSecondDevice);

        ApplicationEntity ae1 = new ApplicationEntity("theFirstAE");
        ApplicationEntity ae2 = new ApplicationEntity("theSecondAE");
        ApplicationEntity ae3 = new ApplicationEntity("theThirdAE");

        ae1.addAEExtension(new AEExtensionWithReferences(ae2));
        ae2.addAEExtension(new AEExtensionWithReferences(ae3));
        ae3.addAEExtension(new AEExtensionWithReferences(ae1,theSecondDevice));


        theCoreDevice.addApplicationEntity(ae1);
        theCoreDevice.addApplicationEntity(ae2);
        theCoreDevice.addApplicationEntity(ae3);
        config.persist(theCoreDevice);

        Device loadedDevice = config.findDevice("TheCoreDevice");

        Assert.assertEquals(
                "TheCoreDevice",
                loadedDevice
                        .getApplicationEntity("theFirstAE")
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getDevice()
                        .getDeviceName()

        );

        Assert.assertEquals(
                "TheSecondDevice",
                loadedDevice
                        .getApplicationEntity("theFirstAE")
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getAnotherAERef()
                        .getAEExtension(AEExtensionWithReferences.class)
                        .getDeviceRef()
                        .getDefaultAE()
                        .getDevice()
                        .getDeviceName()
        );
    }

    private Device createDeviceWithArchiveExtension(String deviceName, String defaultTitle, String otherTitle, String storageTitle, DeviceType... deviceTypes) {
        ApplicationEntity defaultApplicationEntity = new ApplicationEntity();
        defaultApplicationEntity.setAETitle(defaultTitle);

        ApplicationEntity otherApplicationEntity = new ApplicationEntity();
        otherApplicationEntity.setAETitle(otherTitle);

        ApplicationEntity storageApplicationEntity = new ApplicationEntity();
        storageApplicationEntity.setAETitle(storageTitle);

        ExternalArchiveAEExtension storageArchiveExtension = new ExternalArchiveAEExtension();
        storageArchiveExtension.setDefaultForStorage(true);
        storageApplicationEntity.addAEExtension(storageArchiveExtension);

        Device testDevice = new Device();
        testDevice.setDeviceName(deviceName);
        testDevice.setInstalled(true);
        testDevice.setPrimaryDeviceTypes(Arrays.stream(deviceTypes).map(DeviceType::toString).toArray(String[]::new));
        testDevice.setDefaultAE(defaultApplicationEntity);
        testDevice.addApplicationEntity(defaultApplicationEntity);
        testDevice.addApplicationEntity(otherApplicationEntity);
        testDevice.addApplicationEntity(storageApplicationEntity);
        return testDevice;
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

    private boolean deepCompare(Serializable o1, Serializable o2) {
        try {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ObjectOutputStream oos1 = new ObjectOutputStream(baos1);
            oos1.writeObject(o1);
            oos1.close();

            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            ObjectOutputStream oos2 = new ObjectOutputStream(baos2);
            oos2.writeObject(o2);
            oos2.close();

            return Arrays.equals(baos1.toByteArray(), baos2.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
