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

package org.dcm4che3.conf.ldap.audit;

import static org.junit.Assert.*;

import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditLoggerConfiguration;
import org.dcm4che3.conf.ldap.audit.LdapAuditRecordRepositoryConfiguration;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapAuditLoggerConfigurationTest {

    private LdapDicomConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new LdapDicomConfiguration();
        config.addDicomConfigurationExtension(new LdapAuditLoggerConfiguration());
        config.addDicomConfigurationExtension(new LdapAuditRecordRepositoryConfiguration());
    }

    @After
    public void tearDown() throws Exception {
        config.close();
    }

    @Test
    public void testPersistIntegrated() throws Exception {
        try {
            config.removeDevice("TestAuditLoggerAndAuditRecordRepository");
        }  catch (ConfigurationNotFoundException e) {}
        config.persist(createLoggerDevice("TestAuditLoggerAndAuditRecordRepository", null));
        validate(config.findDevice("TestAuditLoggerAndAuditRecordRepository"));
        config.removeDevice("TestAuditLoggerAndAuditRecordRepository");
    }

    @Test
    public void testPersistSeparated() throws Exception {
        try {
            config.removeDevice("TestAuditRecordRepository");
        }  catch (ConfigurationNotFoundException e) {}
        try {
            config.removeDevice("TestAuditLogger");
        }  catch (ConfigurationNotFoundException e) {}
        Device arrDevice = createARRDevice("TestAuditRecordRepository");
        config.persist(arrDevice);
        config.persist(createLoggerDevice("TestAuditLogger", arrDevice));
        validate(config.findDevice("TestAuditLogger"));
        config.removeDevice("TestAuditRecordRepository");
        config.removeDevice("TestAuditLogger");
    }

    private Device createARRDevice(String name) {
        Device device = new Device(name);
        Connection udp = new Connection("audit-udp", "host.dcm4che.org", 514);
        udp.setProtocol(Connection.Protocol.SYSLOG_UDP);
        Connection tls = new Connection("audit-tls", "host.dcm4che.org", 6514);
        tls.setProtocol(Connection.Protocol.SYSLOG_TLS);
        tls.setTlsCipherSuites("TLS_RSA_WITH_AES_128_CBC_SHA");
        device.addConnection(udp);
        device.addConnection(tls);
        addAuditRecordRepository(device, udp, tls);
        return device ;
    }

    private Device createLoggerDevice(String name, Device arrDevice) {
        Device device = new Device(name);
        Connection udp = new Connection("audit-udp", "host.dcm4che.org");
        udp.setProtocol(Connection.Protocol.SYSLOG_UDP);
        Connection tls = new Connection("audit-tls", "host.dcm4che.org");
        tls.setProtocol(Connection.Protocol.SYSLOG_TLS);
        tls.setTlsCipherSuites("TLS_RSA_WITH_AES_128_CBC_SHA");
        device.addConnection(udp);
        device.addConnection(tls);
        if (arrDevice == null) {
            udp.setPort(514);
            tls.setPort(6514);
            addAuditRecordRepository(device, udp, tls);
            arrDevice = device;
        }
        addAuditLogger(device, udp, tls, arrDevice);
        return device ;
    }

    private void addAuditRecordRepository(Device device, Connection udp,
            Connection tls) {
        AuditRecordRepository arr = new AuditRecordRepository();
        device.addDeviceExtension(arr);
        arr.addConnection(udp);
        arr.addConnection(tls);
    }

    private void addAuditLogger(Device device, Connection udp, Connection tls,
            Device arrDevice) {
        AuditLogger logger = new AuditLogger();
        device.addDeviceExtension(logger);
        logger.addConnection(udp);
        logger.addConnection(tls);
        logger.setAuditRecordRepositoryDevice(arrDevice);
        logger.setSchemaURI(AuditMessages.SCHEMA_URI);
    }

    private void validate(Device device) {
        AuditLogger logger = device.getDeviceExtension(AuditLogger.class);
        assertNotNull(logger);
        assertEquals(2, logger.getConnections().size());
        Device arrDevice = logger.getAuditRecordRepositoryDevice();
        assertNotNull(arrDevice);
        AuditRecordRepository arr = arrDevice.getDeviceExtension(AuditRecordRepository.class);
        assertNotNull(arr);
        assertEquals(2, arr.getConnections().size());
    }

}
