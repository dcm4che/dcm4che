/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.conf.ldap;

import org.dcm4che3.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.*;
import org.dcm4che3.net.TransferCapability.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapDicomConfigurationTest {

    private LdapDicomConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new LdapDicomConfiguration();
    }

    @After
    public void tearDown() throws Exception {
        config.close();
    }

    @Test
    public void testRegisterAETitle() throws Exception {
        config.unregisterAETitle("TEST-AET1");
        assertTrue(config.registerAETitle("TEST-AET1"));
        assertFalse(config.registerAETitle("TEST-AET1"));
        assertTrue(
                Arrays.asList(config.listRegisteredAETitles())
                .contains("TEST-AET1"));
        config.unregisterAETitle("TEST-AET1");
        assertFalse(
                Arrays.asList(config.listRegisteredAETitles())
                .contains("TEST-AET1"));
    }

    @Test
    public void testPersist() throws Exception {
        try {
            config.removeDevice("Test-Device-1", null);
        }  catch (ConfigurationNotFoundException e) {}
        Device device = createDevice("Test-Device-1", "TEST-AET1");
        config.persist(device, null);
        ApplicationEntity ae = config.findApplicationEntity("TEST-AET1");
        assertFalse(ae.isAssociationInitiator());
        assertTrue(ae.isAssociationAcceptor());
        assertTrue(ae.getConnections().get(0).isServer());
        TransferCapability echoSCP = ae.getTransferCapabilityFor(
                UID.Verification, TransferCapability.Role.SCP);
        assertNotNull(echoSCP);
        assertArrayEquals(new String[] { UID.ImplicitVRLittleEndian }, echoSCP.getTransferSyntaxes());
        TransferCapability ctSCP = ae.getTransferCapabilityFor(
                UID.CTImageStorage, TransferCapability.Role.SCP);
        assertNotNull(ctSCP);
        assertArrayEquals(new String[] { UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian },
                sort(ctSCP.getTransferSyntaxes()));
        assertNull(ctSCP.getStorageOptions());
        TransferCapability findSCP = ae.getTransferCapabilityFor(
                UID.StudyRootQueryRetrieveInformationModelFind, TransferCapability.Role.SCP);
        assertNotNull(findSCP);
        assertArrayEquals(new String[] { UID.ImplicitVRLittleEndian }, findSCP.getTransferSyntaxes());
        assertEquals(EnumSet.of(QueryOption.RELATIONAL), findSCP.getQueryOptions());
        assertEquals(1, config.listDeviceInfos(deviceInfo("Test-Device-1")).length);
        try {
            config.persist(createDevice("Test-Device-1", "TEST-AET1"), null);
            fail("ConfigurationAlreadyExistsException expected");
        } catch (ConfigurationAlreadyExistsException e) {}
        config.removeDevice("Test-Device-1", null);
    }

    private DeviceInfo deviceInfo(String deviceName) {
        DeviceInfo keys =  new DeviceInfo();
        keys.setDeviceName(deviceName);
        return keys;
    }

    private static String[] sort(String[] a) {
        Arrays.sort(a);
        return a;
    }

    @Test
    public void testMerge() throws Exception {
        try {
            config.removeDevice("Test-Device-1", null);
        }  catch (ConfigurationNotFoundException e) {}
        Device device = createDevice("Test-Device-1", "TEST-AET1");
        config.persist(device, null);
        modifyDevice(device);
        config.merge(device, null);
        ApplicationEntity ae2 = config.findApplicationEntity("TEST-AET2");
        ApplicationEntity ae = ae2.getDevice().getApplicationEntity("TEST-AET1");
        assertTrue(ae.isAssociationInitiator());
        assertFalse(ae.isAssociationAcceptor());
        assertFalse(ae.getConnections().get(0).isServer());
        TransferCapability echoSCP = ae.getTransferCapabilityFor(
                UID.Verification, TransferCapability.Role.SCP);
        assertNull(echoSCP);
        TransferCapability echoSCU = ae.getTransferCapabilityFor(
                UID.Verification, TransferCapability.Role.SCU);
        assertNotNull(echoSCU);
        TransferCapability ctSCP = ae.getTransferCapabilityFor(
                UID.CTImageStorage, TransferCapability.Role.SCP);
        assertEquals(STORAGE_OPTIONS, ctSCP.getStorageOptions());
        TransferCapability findSCP = ae.getTransferCapabilityFor(
                UID.StudyRootQueryRetrieveInformationModelFind, TransferCapability.Role.SCP);
        assertEquals(EnumSet.of(QueryOption.RELATIONAL, QueryOption.DATETIME),
                findSCP.getQueryOptions());
        config.removeDevice("Test-Device-1", null);
    }

    private static Device createDevice(String name, String aet) throws Exception {
        Device device = new Device(name);
        Connection conn = createConn("host.dcm4che.org", 11112);
        device.addConnection(conn);
        ApplicationEntity ae = createAE(aet, conn);
        device.addApplicationEntity(ae);
        return device ;
    }

    private static Connection createConn(String hostname, int port) {
        Connection conn = new Connection();
        conn.setHostname(hostname);
        conn.setPort(port);
        return conn;
    }

    private static TransferCapability echoSCP() {
        return new TransferCapability(null, UID.Verification, Role.SCP,
                UID.ImplicitVRLittleEndian);
    }

    private static final TransferCapability ctSCP() {
        return new TransferCapability(null, UID.CTImageStorage, Role.SCP, 
                UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian);
    }
 
    private static final TransferCapability findSCP() {
        TransferCapability tc = new TransferCapability(null, 
                UID.StudyRootQueryRetrieveInformationModelFind, Role.SCP,
                UID.ImplicitVRLittleEndian);
        tc.setQueryOptions(EnumSet.of(QueryOption.RELATIONAL));
        return tc;
    }

    private static final StorageOptions STORAGE_OPTIONS = new StorageOptions(
            StorageOptions.LevelOfSupport.LEVEL_2,
            StorageOptions.DigitalSignatureSupport.LEVEL_1,
            StorageOptions.ElementCoercion.YES);

    private static ApplicationEntity createAE(String aet, Connection conn) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.setAssociationInitiator(false);
        ae.addConnection(conn);
        ae.addTransferCapability(echoSCP());
        ae.addTransferCapability(ctSCP());
        ae.addTransferCapability(findSCP());
        return ae;
    }

    private static void modifyDevice(Device device) throws Exception  {
        ApplicationEntity ae = device.getApplicationEntity("TEST-AET1");
        ae.getConnections().get(0).setPort(Connection.NOT_LISTENING);
        ae.setAssociationInitiator(true);
        ae.setAssociationAcceptor(false);
        for (TransferCapability tc : ae.getTransferCapabilities()) {
            String sopClass = tc.getSopClass();
            if (sopClass.equals(UID.Verification))
                tc.setRole(TransferCapability.Role.SCU);
            else if (sopClass.equals(UID.CTImageStorage))
                tc.setStorageOptions(STORAGE_OPTIONS);
            else
                tc.getQueryOptions().add(QueryOption.DATETIME);
        }
        Connection conn = createConn("host.dcm4che.org", 11114);
        device.addConnection(conn);
        ApplicationEntity ae2 = createAE("TEST-AET2", conn);
        device.addApplicationEntity(ae2);
    }

 }
