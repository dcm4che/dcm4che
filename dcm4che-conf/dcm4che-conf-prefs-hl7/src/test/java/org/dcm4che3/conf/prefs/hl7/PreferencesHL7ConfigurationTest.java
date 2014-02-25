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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.conf.prefs.hl7;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.prefs.PreferencesDicomConfiguration;
import org.dcm4che3.conf.prefs.hl7.PreferencesHL7Configuration;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.Connection.Protocol;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesHL7ConfigurationTest {

    private PreferencesDicomConfiguration config;
    private PreferencesHL7Configuration hl7Ext;

    @Before
    public void setUp() throws Exception {
        config = new PreferencesDicomConfiguration(Preferences.userRoot()
                .node("PreferencesHL7ConfigurationTest"));
        hl7Ext = new PreferencesHL7Configuration();
        config.addDicomConfigurationExtension(hl7Ext);
    }

    @After
    public void tearDown() throws Exception {
        config.purgeConfiguration();
    }

    @Test
    public void testRegisterApplicationName() throws Exception {
        hl7Ext.unregisterHL7Application("TEST-APP1^DCM4CHE");
        assertTrue(hl7Ext.registerHL7Application("TEST-APP1^DCM4CHE"));
        assertFalse(hl7Ext.registerHL7Application("TEST-APP1^DCM4CHE"));
        assertTrue(
                Arrays.asList(hl7Ext.listRegisteredHL7ApplicationNames())
                .contains("TEST-APP1^DCM4CHE"));
        hl7Ext.unregisterHL7Application("TEST-APP1^DCM4CHE");
        assertFalse(
                Arrays.asList(hl7Ext.listRegisteredHL7ApplicationNames())
                .contains("TEST-APP1^DCM4CHE"));
    }

    @Test
    public void testPersist() throws Exception {
        try {
            config.removeDevice("Test-Device-1");
        }  catch (ConfigurationNotFoundException e) {}
        Device device = createDevice("Test-Device-1", "TEST1^DCM4CHE");
        config.persist(device);
        HL7Application app = hl7Ext.findHL7Application("TEST1^DCM4CHE");
        assertEquals(2575, app.getConnections().get(0).getPort());
        assertEquals("TEST2^DCM4CHE", app.getAcceptedSendingApplications()[0]);
        assertEquals(7, app.getAcceptedMessageTypes().length);
        config.removeDevice("Test-Device-1");
    }

    private static Device createDevice(String name, String appName) throws Exception {
        Device device = new Device(name);
        HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
        device.addDeviceExtension(hl7Ext);
        Connection conn = createConn("host.dcm4che.org", 2575);
        device.addConnection(conn);
        HL7Application app = createHL7App(appName, conn);
        hl7Ext.addHL7Application(app);
        return device ;
    }

    private static Connection createConn(String hostname, int port) {
        Connection conn = new Connection();
        conn.setHostname(hostname);
        conn.setPort(port);
        conn.setProtocol(Protocol.HL7);
        return conn;
    }

    private static HL7Application createHL7App(String name, Connection conn) {
        HL7Application app = new HL7Application(name);
        app.addConnection(conn);
        app.setAcceptedSendingApplications("TEST2^DCM4CHE");
        app.setAcceptedMessageTypes(
                "ADT^A02",
                "ADT^A03",
                "ADT^A06",
                "ADT^A07",
                "ADT^A08",
                "ADT^A40",
                "ORM^O01");
        return app;
    }
}
