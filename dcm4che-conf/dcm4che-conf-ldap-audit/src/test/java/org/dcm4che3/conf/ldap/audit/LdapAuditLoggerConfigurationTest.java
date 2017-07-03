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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.EventID;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditLoggerDeviceExtension;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.audit.AuditSuppressCriteria;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapAuditLoggerConfigurationTest {

    private static File SPOOL_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

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
            config.removeDevice("TestAuditLoggerAndAuditRecordRepository", null);
        }  catch (ConfigurationNotFoundException e) {}
        config.persist(createLoggerDevice("TestAuditLoggerAndAuditRecordRepository", null),
                EnumSet.noneOf(DicomConfiguration.Option.class));
        validate(config.findDevice("TestAuditLoggerAndAuditRecordRepository"));
        config.removeDevice("TestAuditLoggerAndAuditRecordRepository", null);
    }

    @Test
    public void testPersistSeparated() throws Exception {
        try {
            config.removeDevice("TestAuditRecordRepository", null);
        }  catch (ConfigurationNotFoundException e) {}
        try {
            config.removeDevice("TestAuditLogger", null);
        }  catch (ConfigurationNotFoundException e) {}
        Device arrDevice = createARRDevice("TestAuditRecordRepository");
        config.persist(arrDevice, null);
        config.persist(createLoggerDevice("TestAuditLogger", arrDevice), null);
        validate(config.findDevice("TestAuditLogger"));
        config.removeDevice("TestAuditRecordRepository", null);
        config.removeDevice("TestAuditLogger", null);
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
        AuditLoggerDeviceExtension ext = new AuditLoggerDeviceExtension();
        device.addDeviceExtension(ext);
        AuditLogger logger = createAuditLogger(udp, tls, arrDevice);
        ext.addAuditLogger(logger);
        return device ;
    }

    private void addAuditRecordRepository(Device device, Connection udp,
            Connection tls) {
        AuditRecordRepository arr = new AuditRecordRepository();
        device.addDeviceExtension(arr);
        arr.addConnection(udp);
        arr.addConnection(tls);
    }

    private AuditLogger createAuditLogger(Connection udp, Connection tls,
            Device arrDevice) {
        AuditLogger logger = new AuditLogger("AuditLogger");
        logger.addConnection(udp);
        logger.addConnection(tls);
        logger.setAuditRecordRepositoryDevice(arrDevice);
        logger.setSchemaURI(AuditMessages.SCHEMA_URI);
        logger.setMessageID("MessageID");
        logger.setApplicationName("ApplicationName");
        logger.setAuditEnterpriseSiteID("AuditEnterpriseSiteID");
        logger.setAuditSourceID("AuditSourceID");
        logger.setAuditSourceTypeCodes("4", "5");
        logger.setEncoding("ISO-8859-1");
        logger.setFacility(AuditLogger.Facility.auth);
        logger.setSuccessSeverity(AuditLogger.Severity.info);
        logger.setMinorFailureSeverity(AuditLogger.Severity.notice);
        logger.setSeriousFailureSeverity(AuditLogger.Severity.warning);
        logger.setMajorFailureSeverity(AuditLogger.Severity.err);
        logger.setFormatXML(true);
        logger.setIncludeBOM(false);
        logger.setRetryInterval(300);
        logger.setSpoolDirectory(SPOOL_DIRECTORY);
        logger.setIncludeInstanceUID(true);
        logger.addAuditSuppressCriteria(createAuditSuppressCriteria());
        return logger;
    }

    private AuditSuppressCriteria createAuditSuppressCriteria() {
        AuditSuppressCriteria criteria = new AuditSuppressCriteria("AuditSuppressCriteria");
        criteria.setEventIDs(AuditMessages.EventID.ApplicationActivity);
        criteria.setEventActionCodes(AuditMessages.EventActionCode.Execute);
        criteria.setEventTypeCodes(AuditMessages.EventTypeCode.ApplicationStart, 
                AuditMessages.EventTypeCode.ApplicationStop);
        criteria.setEventOutcomeIndicators(AuditMessages.EventOutcomeIndicator.Success);
        criteria.setUserIDs("UserID");
        criteria.setAlternativeUserIDs("AltUserID");
        criteria.setNetworkAccessPointIDs("127.0.0.1");
        criteria.setUserRoleIDCodes(AuditMessages.RoleIDCode.ApplicationLauncher);
        criteria.setUserIsRequestor(true);
        return criteria;
    }

    private void validate(Device device) {
        AuditLoggerDeviceExtension ext = device.getDeviceExtension(AuditLoggerDeviceExtension.class);
        for (AuditLogger logger : ext.getAuditLoggers())
            validateLogger(logger);
    }

    private void validateLogger(AuditLogger logger) {
        assertNotNull(logger);
        assertEquals(2, logger.getConnections().size());
        assertEquals(AuditMessages.SCHEMA_URI, logger.getSchemaURI());
        assertEquals("MessageID", logger.getMessageID());
        assertEquals("ApplicationName", logger.getApplicationName());
        assertEquals("AuditEnterpriseSiteID", logger.getAuditEnterpriseSiteID());
        assertEquals("AuditSourceID", logger.getAuditSourceID());
        assertArrayEquals(new String[]{"4", "5"}, sort(logger.getAuditSourceTypeCodes()));
        assertEquals("ISO-8859-1", logger.getEncoding());
        assertEquals(AuditLogger.Facility.auth, logger.getFacility());
        assertEquals(AuditLogger.Severity.info, logger.getSuccessSeverity());
        assertEquals(AuditLogger.Severity.notice, logger.getMinorFailureSeverity());
        assertEquals(AuditLogger.Severity.warning, logger.getSeriousFailureSeverity());
        assertEquals(AuditLogger.Severity.err, logger.getMajorFailureSeverity());
        assertTrue(logger.isFormatXML());
        assertFalse(logger.isIncludeBOM());
        assertEquals(300, logger.getRetryInterval());
        assertEquals(SPOOL_DIRECTORY, logger.getSpoolDirectory());
        assertTrue(logger.isIncludeInstanceUID());
        validate(logger.getAuditSuppressCriteriaList());
        Device arrDevice = logger.getAuditRecordRepositoryDevice();
        assertNotNull(arrDevice);
        AuditRecordRepository arr = arrDevice.getDeviceExtension(AuditRecordRepository.class);
        assertNotNull(arr);
        assertEquals(2, arr.getConnections().size());
    }

    private void validate(List<AuditSuppressCriteria> criteriaList) {
        assertEquals(1, criteriaList.size());
        AuditSuppressCriteria criteria = criteriaList.get(0);
        assertEquals("AuditSuppressCriteria", criteria.getCommonName());
        EventID[] eventIDs = criteria.getEventIDs();
        assertEquals(1, eventIDs.length);
        assertEquals(AuditMessages.EventID.ApplicationActivity.getCsdCode(),
                eventIDs[0].getCsdCode());
        assertEquals(AuditMessages.EventID.ApplicationActivity.getCodeSystemName(),
                eventIDs[0].getCodeSystemName());
        assertEquals(AuditMessages.EventID.ApplicationActivity.getOriginalText(),
                eventIDs[0].getOriginalText());
        String[] eventActionCodes = criteria.getEventActionCodes();
        assertEquals(1, eventActionCodes.length);
        assertEquals(AuditMessages.EventActionCode.Execute, eventActionCodes[0]);
        EventTypeCode[] eventTypeCodes = criteria.getEventTypeCodes();
        assertEquals(2, eventTypeCodes.length);
        String[] eventOutcomeIndicators = criteria.getEventOutcomeIndicators();
        assertEquals(1, eventOutcomeIndicators.length);
        assertEquals(AuditMessages.EventOutcomeIndicator.Success, eventOutcomeIndicators[0]);
        String[] userIDs = criteria.getUserIDs();
        assertEquals(1, userIDs.length);
        assertEquals("UserID", userIDs[0]);
        String[] altUserIDs = criteria.getAlternativeUserIDs();
        assertEquals(1, altUserIDs.length);
        assertEquals("AltUserID", altUserIDs[0]);
        String[] networkAccessPointIDs = criteria.getNetworkAccessPointIDs();
        assertEquals(1, networkAccessPointIDs.length);
        assertEquals("127.0.0.1", networkAccessPointIDs[0]);
        RoleIDCode[] userRoleIDCodes = criteria.getUserRoleIDCodes();
        assertEquals(1, userRoleIDCodes.length);
        assertEquals(AuditMessages.RoleIDCode.ApplicationLauncher.getCsdCode(),
                userRoleIDCodes[0].getCsdCode());
        assertEquals(AuditMessages.RoleIDCode.ApplicationLauncher.getCodeSystemName(),
                userRoleIDCodes[0].getCodeSystemName());
        assertEquals(AuditMessages.RoleIDCode.ApplicationLauncher.getOriginalText(),
                userRoleIDCodes[0].getOriginalText());
        assertEquals(true, criteria.getUserIsRequestor());
    }

    private <T> T[] sort(T[] a) {
        Arrays.sort(a);
        return a;
    }

}
