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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

package org.dcm4che.conf.ldap;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import org.dcm4che.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che.conf.api.ConfigurationNotFoundException;
import org.dcm4che.data.UID;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapDicomConfigurationTest {

    private LdapDicomConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new LdapDicomConfiguration(
                LdapDicomConfiguration.authenticate(
                                LdapDicomConfiguration.env("ldap://localhost:389"),
                                "cn=admin,dc=nodomain", "admin"),
                        "dc=nodomain");
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
        config.unregisterAETitle("TEST-AET1");
    }

    @Test
    public void testPersist() throws Exception {
        try {
            config.removeDevice("Test-Device-1");
        }  catch (ConfigurationNotFoundException e) {}
        Device device = createDevice("Test-Device-1", "TEST-AET1");
        config.persist(device);
        ApplicationEntity ae = config.findApplicationEntity("TEST-AET1");
        assertFalse(ae.isAssociationInitiator());
        assertTrue(ae.isAssociationAcceptor());
        assertTrue(ae.getConnections().get(0).isServer());
        Collection<TransferCapability> tcs = ae.getTransferCapabilities();
        Iterator<TransferCapability> tciter = tcs.iterator();
        assertTrue(tciter.hasNext());
        TransferCapability tc = tciter.next();
        assertEquals(UID.VerificationSOPClass, tc.getSopClass());
        assertEquals(TransferCapability.Role.SCP, tc.getRole());
        assertArrayEquals(new String[] { UID.ImplicitVRLittleEndian }, tc.getTransferSyntaxes());
        assertFalse(tciter.hasNext());
        try {
            config.persist(createDevice("Test-Device-1", "TEST-AET1"));
            fail("ConfigurationAlreadyExistsException expected");
        } catch (ConfigurationAlreadyExistsException e) {}
        config.removeDevice("Test-Device-1");
    }

    @Test
    public void testMerge() throws Exception {
        try {
            config.removeDevice("Test-Device-1");
        }  catch (ConfigurationNotFoundException e) {}
        Device device = createDevice("Test-Device-1", "TEST-AET1");
        config.persist(device);
        modifyDevice(device);
        config.merge(device);
        ApplicationEntity ae2 = config.findApplicationEntity("TEST-AET2");
        ApplicationEntity ae = ae2.getDevice().getApplicationEntity("TEST-AET1");
        assertTrue(ae.isAssociationInitiator());
        assertFalse(ae.isAssociationAcceptor());
        assertFalse(ae.getConnections().get(0).isServer());
        Collection<TransferCapability> tcs = ae.getTransferCapabilities();
        Iterator<TransferCapability> tciter = tcs.iterator();
        assertTrue(tciter.hasNext());
        TransferCapability tc = tciter.next();
        assertEquals(UID.VerificationSOPClass, tc.getSopClass());
        assertEquals(TransferCapability.Role.SCU, tc.getRole());
        assertArrayEquals(new String[] { UID.ImplicitVRLittleEndian }, tc.getTransferSyntaxes());
        assertFalse(tciter.hasNext());
        config.removeDevice("Test-Device-1");
    }

    private static Device createDevice(String name, String aet) throws Exception {
        Device device = new Device(name);
        Connection conn = createConn();
        device.addConnection(conn);
        ApplicationEntity ae = createAE(aet, conn);
        device.addApplicationEntity(ae);
        return device ;
    }

    private static Connection createConn() {
        Connection conn = new Connection();
        conn.setHostname("host.dcm4che.org");
        conn.setPort(11112);
        return conn;
    }

    private static final TransferCapability ECHO_SCP = new TransferCapability(null, 
            UID.VerificationSOPClass,
            TransferCapability.Role.SCP,
            UID.ImplicitVRLittleEndian);

    private static final TransferCapability ECHO_SCU = new TransferCapability(null, 
            UID.VerificationSOPClass,
            TransferCapability.Role.SCU,
            UID.ImplicitVRLittleEndian);

    private static ApplicationEntity createAE(String aet, Connection conn) {
        ApplicationEntity ae = new ApplicationEntity(aet);
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        ae.addTransferCapability(ECHO_SCP);
        return ae;
    }

    private static void modifyDevice(Device device) throws Exception  {
        ApplicationEntity ae = device.getApplicationEntity("TEST-AET1");
        ae.getConnections().get(0).setPort(-1);
        ae.setAssociationInitiator(true);
        ae.setAssociationAcceptor(false);
        ae.removeTransferCapability(ECHO_SCP);
        ae.addTransferCapability(ECHO_SCU);
        Connection conn = createConn();
        device.addConnection(conn);
        ApplicationEntity ae2 = createAE("TEST-AET2", conn);
        device.addApplicationEntity(ae2);
    }

}
