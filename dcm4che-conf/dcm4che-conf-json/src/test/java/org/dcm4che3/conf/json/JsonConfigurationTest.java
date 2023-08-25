/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.json;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.EventID;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.json.audit.JsonAuditLoggerConfiguration;
import org.dcm4che3.conf.json.audit.JsonAuditRecordRepositoryConfiguration;
import org.dcm4che3.conf.json.hl7.JsonHL7Configuration;
import org.dcm4che3.conf.json.imageio.JsonImageReaderConfiguration;
import org.dcm4che3.conf.json.imageio.JsonImageWriterConfiguration;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.UID;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageWriterFactory;
import org.dcm4che3.net.*;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.net.audit.AuditLoggerDeviceExtension;
import org.dcm4che3.net.audit.AuditRecordRepository;
import org.dcm4che3.net.audit.AuditSuppressCriteria;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.net.imageio.ImageReaderExtension;
import org.dcm4che3.net.imageio.ImageWriterExtension;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Nov 2015
 */
public class JsonConfigurationTest {

    static final String HL7_DEFAULT_CHARACTER_SET = "8859/1";
    static final String[] HL7_MESSAGE_TYPES = {
            "ADT^A02",
            "ADT^A03",
            "ADT^A06",
            "ADT^A07",
            "ADT^A08",
            "ADT^A40",
            "ORM^O01"
    };

    static final String[] HL7_ACCEPTED_SENDING_APPLICATIONS = {
            "DCM4CHEE^J4CARE",
            "MAS1TLN^TALLINN"
    };

    static final EventID[] AUDIT_LOGGER_EVENT_IDS = {
            AuditMessages.EventID.HealthServicesProvisionEvent,
            AuditMessages.EventID.MedicationEvent,
    };

    static final EventTypeCode[] AUDIT_LOGGER_EVENT_TYPE_CODES = {
            AuditMessages.EventTypeCode.ApplicationStart,
            AuditMessages.EventTypeCode.ApplicationStop
    };

    static final String[] AUDIT_LOGGER_EVENT_ACTION_CODES = {
            AuditMessages.EventActionCode.Create,
            AuditMessages.EventActionCode.Delete
    };

    static final String[] AUDIT_LOGGER_EVENT_OUTCOME_INDICATORS = {
            AuditMessages.EventOutcomeIndicator.MajorFailure,
            AuditMessages.EventOutcomeIndicator.MinorFailure
    };

    static final String[] AUDIT_LOGGER_USER_IDS = {
            "4",
            "2",
            "0"
    };

    static final String[] AUDIT_LOGGER_ALTERNATIVE_USER_IDS = {
            "XYZ",
            "XYZ",
            "XYZ"
    };

    static final RoleIDCode[] AUDIT_LOGGER_ROLE_ID_CODES = {
            AuditMessages.RoleIDCode.Application,
            AuditMessages.RoleIDCode.ApplicationLauncher
    };

    static final String[] AUDIT_LOGGER_NETWORK_ACCESS_POINT_IDS = {
            AuditMessages.NetworkAccessPointTypeCode.EmailAddress,
            AuditMessages.NetworkAccessPointTypeCode.IPAddress
    };

    @Test
    public void testWriteTo() throws Exception {
        StringWriter writer = new StringWriter();
        try ( JsonGenerator gen = Json.createGenerator(writer)) {
            JsonConfiguration config = new JsonConfiguration();
            config.addJsonConfigurationExtension(new JsonAuditLoggerConfiguration());
            config.addJsonConfigurationExtension(new JsonAuditRecordRepositoryConfiguration());
            config.addJsonConfigurationExtension(new JsonImageReaderConfiguration());
            config.addJsonConfigurationExtension(new JsonImageWriterConfiguration());
            config.addJsonConfigurationExtension(new JsonHL7Configuration());
            config.writeTo(createDevice("Test-Device-1", "TEST-AET1"), gen, true);
        }
        Path path = Paths.get("src/test/data/device.json");
//        try (BufferedWriter w = Files.newBufferedWriter(path, Charset.forName("UTF-8"))) {
//            w.write(writer.toString());
//        }
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            assertEquals(reader.readLine(), writer.toString());
        }
    }

    @Test
    public void testWriteARRTo() throws Exception {
        StringWriter writer = new StringWriter();
        try (JsonGenerator gen = Json.createGenerator(writer)) {
            JsonConfiguration config = new JsonConfiguration();
            config.addJsonConfigurationExtension(new JsonAuditRecordRepositoryConfiguration());
            config.writeTo(createARRDevice("TestAuditRecordRepository"), gen, true);
        }
        Path path = Paths.get("src/test/data/arrdevice.json");
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            assertEquals(reader.readLine(), writer.toString());
        }
    }

    @Test
    public void testLoadARR() throws Exception {
        Device device = loadARR();
        AuditRecordRepository arr = device.getDeviceExtension(AuditRecordRepository.class);
        assertNotNull(arr);
        List<Connection> conns = arr.getConnections();
        assertEquals(2, conns.size());
    }

    private static Device loadARR() throws IOException, ConfigurationException {
        Path path = Paths.get("src/test/data/arrdevice.json");
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            JsonConfiguration config = new JsonConfiguration();
            config.addJsonConfigurationExtension(new JsonAuditRecordRepositoryConfiguration());
            return config.loadDeviceFrom(Json.createParser(reader), null);
        }
    }

    private static final ConfigurationDelegate configDelegate = new ConfigurationDelegate() {
        @Override
        public Device findDevice(String name) throws ConfigurationException {
            if (!name.equals("TestAuditRecordRepository"))
                throw new ConfigurationNotFoundException("Unknown Device: " + name);
            try {
                return loadARR();
            } catch (IOException e) {
                throw new ConfigurationException(e);
            }
        }

    };

    @Test
    public void testLoadDevice() throws Exception {
        Device device = null;
        Path path = Paths.get("src/test/data/device.json");
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("UTF-8"))) {
            JsonConfiguration config = new JsonConfiguration();
            config.addJsonConfigurationExtension(new JsonAuditLoggerConfiguration());
            config.addJsonConfigurationExtension(new JsonAuditRecordRepositoryConfiguration());
            config.addJsonConfigurationExtension(new JsonImageReaderConfiguration());
            config.addJsonConfigurationExtension(new JsonImageWriterConfiguration());
            config.addJsonConfigurationExtension(new JsonHL7Configuration());
            device = config.loadDeviceFrom(Json.createParser(reader), configDelegate);
        }
        assertEquals("Test-Device-1", device.getDeviceName());
        List<Connection> conns = device.listConnections();
        assertEquals(3, conns.size());
        Connection conn = conns.get(0);
        assertEquals("host.dcm4che.org", conn.getHostname());
        assertEquals(11112, conn.getPort());
        Collection<ApplicationEntity> aes = device.getApplicationEntities();
        assertEquals(1, aes.size());
        ApplicationEntity ae = aes.iterator().next();
        assertEquals("TEST-AET1", ae.getAETitle());
        assertTrue(ae.isAssociationAcceptor());
        assertFalse(ae.isAssociationInitiator());
        List<Connection> aeconns = ae.getConnections();
        assertEquals(1, aeconns.size());
        assertSame(conn, aeconns.get(0));
        assertEquals(3, ae.getTransferCapabilities().size());
        TransferCapability echoSCP = ae.getTransferCapabilityFor(UID.Verification, TransferCapability.Role.SCP);
        assertNotNull(echoSCP);
        assertArrayEquals(new String[]{ UID.ImplicitVRLittleEndian }, echoSCP.getTransferSyntaxes());
        assertNull(echoSCP.getCommonName());
        assertNull(echoSCP.getQueryOptions());
        assertNull(echoSCP.getStorageOptions());
        TransferCapability ctSCP = ae.getTransferCapabilityFor(UID.CTImageStorage, TransferCapability.Role.SCP);
        assertNotNull(ctSCP);
        StorageOptions storageOptions = ctSCP.getStorageOptions();
        assertNotNull(storageOptions);
        assertEquals(StorageOptions.LevelOfSupport.LEVEL_2, storageOptions.getLevelOfSupport());
        assertEquals(StorageOptions.DigitalSignatureSupport.LEVEL_1, storageOptions.getDigitalSignatureSupport());
        assertEquals(StorageOptions.ElementCoercion.YES, storageOptions.getElementCoercion());
        TransferCapability findSCP = ae.getTransferCapabilityFor(
                UID.StudyRootQueryRetrieveInformationModelFind, TransferCapability.Role.SCP);
        assertNotNull(findSCP);
        assertEquals(EnumSet.of(QueryOption.RELATIONAL), findSCP.getQueryOptions());
        assertImageReaderExtension(device.getDeviceExtension(ImageReaderExtension.class));
        assertImageWriterExtension(device.getDeviceExtension(ImageWriterExtension.class));
        assertAuditLoggerDeviceExtension(device.getDeviceExtension(AuditLoggerDeviceExtension.class));
        assertHL7DeviceExtension(device.getDeviceExtension(HL7DeviceExtension.class));
    }

    private void assertAuditLoggerDeviceExtension(AuditLoggerDeviceExtension auditLoggerExt) {
        assertNotNull(auditLoggerExt);
        Collection<AuditLogger> auditLoggers = auditLoggerExt.getAuditLoggers();
        AuditLogger auditLogger = auditLoggers.iterator().next();
        assertNotNull(auditLogger.getAuditRecordRepositoryDevice());
        List<Connection> conns = auditLogger.getConnections();
        assertEquals(1, conns.size());
        assertEquals("SourceID", auditLogger.getAuditSourceID());
        assertEquals("EnterpriseID", auditLogger.getAuditEnterpriseSiteID());
        assertEquals("[4]", Arrays.toString(auditLogger.getAuditSourceTypeCodes()));
        assertEquals(AuditLogger.Facility.authpriv.toString(), auditLogger.getFacility().toString());
        assertEquals(AuditLogger.Severity.notice.toString(), auditLogger.getSuccessSeverity().toString());
        assertEquals(AuditLogger.Severity.warning.toString(), auditLogger.getMinorFailureSeverity().toString());
        assertEquals(AuditLogger.Severity.err.toString(), auditLogger.getSeriousFailureSeverity().toString());
        assertEquals(AuditLogger.Severity.crit.toString(), auditLogger.getMajorFailureSeverity().toString());
        assertEquals("IHE+RFC-3881", auditLogger.getMessageID());
        assertEquals("UTF-8", auditLogger.getEncoding());
        assertEquals(true, auditLogger.isIncludeBOM());
        assertEquals(false, auditLogger.isTimestampInUTC());
        assertEquals(false, auditLogger.isFormatXML());
        assertEquals(false, auditLogger.isIncludeInstanceUID());
        assertEquals(0, auditLogger.getRetryInterval());
        assertSuppressCriteria(auditLogger.getAuditSuppressCriteriaList());
    }

    private void assertSuppressCriteria(List<AuditSuppressCriteria> auditSuppressCriteriaList) {
        for (AuditSuppressCriteria asc : auditSuppressCriteriaList) {
            assertEquals("cn", asc.getCommonName());
            assertArrayEquals(eventIDsToStringArray(AUDIT_LOGGER_EVENT_IDS), asc.getEventIDsAsStringArray());
            assertArrayEquals(eventCodesToStringArray(AUDIT_LOGGER_EVENT_TYPE_CODES), asc.getEventTypeCodesAsStringArray());
            assertArrayEquals(AUDIT_LOGGER_EVENT_ACTION_CODES, asc.getEventActionCodes());
            assertArrayEquals(AUDIT_LOGGER_EVENT_OUTCOME_INDICATORS, asc.getEventOutcomeIndicators());
            assertArrayEquals(AUDIT_LOGGER_USER_IDS, asc.getUserIDs());
            assertArrayEquals(AUDIT_LOGGER_ALTERNATIVE_USER_IDS, asc.getAlternativeUserIDs());
            assertArrayEquals(roleIDCodesToStringArray(AUDIT_LOGGER_ROLE_ID_CODES), asc.getUserRoleIDCodesAsStringArray());
            assertArrayEquals(AUDIT_LOGGER_NETWORK_ACCESS_POINT_IDS, asc.getNetworkAccessPointIDs());
            assertEquals(true, asc.getUserIsRequestor());
        }
    }

    private static String[] eventIDsToStringArray(EventID... a) {
        String[] ss = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            ss[i] = new Code(
                    a[i].getCsdCode(),
                    a[i].getCodeSystemName(),
                    null,
                    a[i].getOriginalText())
                    .toString();
        }
        return ss;
    }

    private static String[] eventCodesToStringArray(EventTypeCode... a) {
        String[] ss = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            ss[i] = new Code(
                    a[i].getCsdCode(),
                    a[i].getCodeSystemName(),
                    null,
                    a[i].getOriginalText())
                    .toString();
        }
        return ss;
    }

    private static String[] roleIDCodesToStringArray(RoleIDCode... a) {
        String[] ss = new String[a.length];
        for (int i = 0; i < a.length; i++) {
            ss[i] = new Code(
                    a[i].getCsdCode(),
                    a[i].getCodeSystemName(),
                    null,
                    a[i].getOriginalText())
                    .toString();
        }
        return ss;
    }

    private void assertImageWriterExtension(ImageWriterExtension ext) {
        assertNotNull(ext);
        ImageWriterFactory factory = ext.getImageWriterFactory();
        assertNotNull(factory);
        Set<Map.Entry<String, ImageWriterFactory.ImageWriterParam>> expectedEntries =
                ImageWriterFactory.getDefault().getEntries();
        assertEquals(expectedEntries.size(), factory.getEntries().size());
        for (Map.Entry<String, ImageWriterFactory.ImageWriterParam> expected : expectedEntries) {
            assertEquals(expected.getValue(), factory.get(expected.getKey()));
        }
    }

    private void assertImageReaderExtension(ImageReaderExtension ext) {
        assertNotNull(ext);
        ImageReaderFactory factory = ext.getImageReaderFactory();
        assertNotNull(factory);
        Set<Map.Entry<String, ImageReaderFactory.ImageReaderParam>> expectedEntries =
                ImageReaderFactory.getDefault().getEntries();
        assertEquals(expectedEntries.size(), factory.getEntries().size());
        for (Map.Entry<String, ImageReaderFactory.ImageReaderParam> expected : expectedEntries) {
            assertEquals(expected.getValue(), factory.get(expected.getKey()));
        }
    }

    private static Device createDevice(String name, String aet) throws Exception {
        Device device = new Device(name);
        Connection conn = createConn("host.dcm4che.org", 11112);
        device.addConnection(conn);
        ApplicationEntity ae = createAE(aet, conn);
        device.addApplicationEntity(ae);
        device.addDeviceExtension(new ImageReaderExtension(ImageReaderFactory.getDefault()));
        device.addDeviceExtension(new ImageWriterExtension(ImageWriterFactory.getDefault()));
        addAuditLoggerDeviceExtension(device, createARRDevice("TestAuditRecordRepository"));
        addHL7DeviceExtension(device);
        return device ;
    }

    private static Connection createConn(String hostname, int port) {
        Connection conn = new Connection();
        conn.setHostname(hostname);
        conn.setPort(port);
        return conn;
    }

    private static TransferCapability echoSCP() {
        return new TransferCapability(null, UID.Verification, TransferCapability.Role.SCP,
                UID.ImplicitVRLittleEndian);
    }

    private static final TransferCapability ctSCP() {
        TransferCapability tc = new TransferCapability(null, UID.CTImageStorage, TransferCapability.Role.SCP,
                UID.ImplicitVRLittleEndian, UID.ExplicitVRLittleEndian);
        tc.setStorageOptions(STORAGE_OPTIONS);
        return tc;
    }

    private static final TransferCapability findSCP() {
        TransferCapability tc = new TransferCapability(null,
                UID.StudyRootQueryRetrieveInformationModelFind, TransferCapability.Role.SCP,
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

    private static Device createARRDevice(String name) {
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

    private static void addAuditRecordRepository(Device device, Connection udp, Connection tls) {
        AuditRecordRepository arr = new AuditRecordRepository();
        device.addDeviceExtension(arr);
        arr.addConnection(udp);
        arr.addConnection(tls);
    }

    private static void addAuditLoggerDeviceExtension(Device device, Device arrDevice) {
        Connection auditUDP = new Connection("audit-udp", "localhost");
        auditUDP.setProtocol(Connection.Protocol.SYSLOG_UDP);
        device.addConnection(auditUDP);
        AuditLoggerDeviceExtension ext = new AuditLoggerDeviceExtension();
        device.addDeviceExtension(ext);
        AuditLogger auditLogger = new AuditLogger("Audit Logger");
        auditLogger.addConnection(auditUDP);
        auditLogger.setAuditSourceID("SourceID");
        auditLogger.setAuditEnterpriseSiteID("EnterpriseID");
        auditLogger.setAuditSourceTypeCodes("4");
        auditLogger.setApplicationName("applicationName");
        auditLogger.setAuditRecordRepositoryDevice(arrDevice);
        auditLogger.setAuditSuppressCriteriaList(createSuppressCriteriaList());
        ext.addAuditLogger(auditLogger);
    }

    private static List<AuditSuppressCriteria> createSuppressCriteriaList() {
        AuditSuppressCriteria asc = new AuditSuppressCriteria("cn");
        asc.setEventIDs(AUDIT_LOGGER_EVENT_IDS);
        asc.setEventTypeCodes(AUDIT_LOGGER_EVENT_TYPE_CODES);
        asc.setEventActionCodes(AUDIT_LOGGER_EVENT_ACTION_CODES);
        asc.setEventOutcomeIndicators(AUDIT_LOGGER_EVENT_OUTCOME_INDICATORS);
        asc.setUserIDs(AUDIT_LOGGER_USER_IDS);
        asc.setAlternativeUserIDs(AUDIT_LOGGER_ALTERNATIVE_USER_IDS);
        asc.setUserRoleIDCodes(AUDIT_LOGGER_ROLE_ID_CODES);
        asc.setNetworkAccessPointIDs(AUDIT_LOGGER_NETWORK_ACCESS_POINT_IDS);
        asc.setUserIsRequestor(true);
        return Collections.singletonList(asc);
    }

    private static void addHL7DeviceExtension(Device device) {
        Connection hl7 = new Connection("hl7", "localhost", 2575);
        hl7.setBindAddress("0.0.0.0");
        hl7.setClientBindAddress("0.0.0.0");
        hl7.setProtocol(Connection.Protocol.HL7);
        device.addConnection(hl7);

        HL7DeviceExtension ext = new HL7DeviceExtension();
        device.addDeviceExtension(ext);
        HL7Application hl7App = new HL7Application("*");
        ext.addHL7Application(hl7App);
        hl7App.addConnection(hl7);
        hl7App.setHL7DefaultCharacterSet(HL7_DEFAULT_CHARACTER_SET);
        hl7App.setAcceptedMessageTypes(HL7_MESSAGE_TYPES);
        hl7App.setAcceptedSendingApplications(HL7_ACCEPTED_SENDING_APPLICATIONS);
    }

    private void assertHL7DeviceExtension(HL7DeviceExtension ext) {
        assertNotNull(ext);
        Collection<HL7Application> hl7Apps = ext.getHL7Applications();
        assertEquals(1, hl7Apps.size());
        HL7Application hl7App = hl7Apps.iterator().next();
        assertEquals("*", hl7App.getApplicationName());
        assertEquals(1, hl7App.getConnections().size());
        assertEquals(HL7_DEFAULT_CHARACTER_SET, hl7App.getHL7DefaultCharacterSet());
        assertArrayEquals(HL7_MESSAGE_TYPES, hl7App.getAcceptedMessageTypes());
        assertArrayEquals(HL7_ACCEPTED_SENDING_APPLICATIONS, hl7App.getAcceptedSendingApplications());
    }

}