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
 * Portions created by the Initial Developer are Copyright (C) 2022
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
package org.dcm4che3.net.audit;

import java.io.File;
import java.util.Collections;

import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Marryat Ma
 */
public class AuditLoggerTest {

    private final AuditLogger newLogger = new AuditLogger();
    private final AuditLogger logger = new AuditLogger();
    private final Device mockDevice = createMockBuilder(Device.class)
            .withConstructor()
            .addMockedMethod("reconfigureConnections")
            .createNiceMock();

    @Before
    public void before() {
        logger.setDevice(mockDevice);
    }

    @Test
    public void reconfigure_UpdatesFacility_WhenCalled() {
        newLogger.setFacility(AuditLogger.Facility.audit);
        logger.setFacility(AuditLogger.Facility.auth);

        logger.reconfigure(newLogger);

        assertThat("facility", logger.getFacility(), is(AuditLogger.Facility.audit));
    }

    @Test
    public void reconfigure_UpdatesSuccessSeverity_WhenCalled() {
        newLogger.setSuccessSeverity(AuditLogger.Severity.notice);
        logger.setSuccessSeverity(AuditLogger.Severity.debug);

        logger.reconfigure(newLogger);

        assertThat("successSeverity", logger.getSuccessSeverity(), is(AuditLogger.Severity.notice));
    }

    @Test
    public void reconfigure_UpdatesMinorFailureSeverity_WhenCalled() {
        newLogger.setMinorFailureSeverity(AuditLogger.Severity.warning);
        logger.setMinorFailureSeverity(AuditLogger.Severity.info);

        logger.reconfigure(newLogger);

        assertThat("minorFailureSeverity", logger.getMinorFailureSeverity(), is(AuditLogger.Severity.warning));
    }

    @Test
    public void reconfigure_UpdatesSeriousFailureSeverity_WhenCalled() {
        newLogger.setSeriousFailureSeverity(AuditLogger.Severity.emerg);
        logger.setSeriousFailureSeverity(AuditLogger.Severity.crit);

        logger.reconfigure(newLogger);

        assertThat("seriousFailureSeverity", logger.getSeriousFailureSeverity(), is(AuditLogger.Severity.emerg));
    }

    @Test
    public void reconfigure_UpdatesMajorFailureSeverity_WhenCalled() {
        newLogger.setMajorFailureSeverity(AuditLogger.Severity.alert);
        logger.setMajorFailureSeverity(AuditLogger.Severity.err);

        logger.reconfigure(newLogger);

        assertThat("majorFailureSeverity", logger.getMajorFailureSeverity(), is(AuditLogger.Severity.alert));
    }

    @Test
    public void reconfigure_UpdatesApplicationName_WhenCalled() {
        newLogger.setApplicationName("NewApplicationName");
        logger.setApplicationName("OldApplicationName");

        logger.reconfigure(newLogger);

        assertThat("applicationName", logger.getApplicationName(), is("NewApplicationName"));
    }

    @Test
    public void reconfigure_UpdatesAuditSourceID_WhenCalled() {
        newLogger.setAuditSourceID("NewAuditSourceID");
        logger.setAuditSourceID("OldAuditSourceID");

        logger.reconfigure(newLogger);

        assertThat("auditSourceID", logger.getAuditSourceID(), is("NewAuditSourceID"));
    }

    @Test
    public void reconfigure_UpdatesAuditEnterpriseSiteID_WhenCalled() {
        newLogger.setAuditEnterpriseSiteID("NewAuditEnterpriseSiteID");
        logger.setAuditEnterpriseSiteID("OldAuditEnterpriseSiteID");

        logger.reconfigure(newLogger);

        assertThat("auditEnterpriseSiteID", logger.getAuditEnterpriseSiteID(), is("NewAuditEnterpriseSiteID"));
    }

    @Test
    public void reconfigure_UpdatesAuditSourceTypeCodes_WhenCalled() {
        newLogger.setAuditSourceTypeCodes("NewAuditSourceTypeCode1, NewAuditSourceTypeCode2");
        logger.setAuditSourceTypeCodes("OldAuditSourceTypeCode");

        logger.reconfigure(newLogger);

        assertThat("auditSourceTypeCodes", logger.getAuditSourceTypeCodes(), is(new String[]{"NewAuditSourceTypeCode1, NewAuditSourceTypeCode2"}));
    }

    @Test
    public void reconfigure_UpdatesMessageID_WhenCalled() {
        newLogger.setMessageID("NewMessageID");
        logger.setMessageID("OldMessageID");

        logger.reconfigure(newLogger);

        assertThat("messageID", logger.getMessageID(), is("NewMessageID"));
    }

    @Test
    public void reconfigure_UpdatesEncoding_WhenCalled() {
        newLogger.setEncoding("ISO-8859-6");
        logger.setEncoding("GB18030");

        logger.reconfigure(newLogger);

        assertThat("encoding", logger.getEncoding(), is("ISO-8859-6"));
    }

    @Test
    public void reconfigure_UpdatesSchemaURI_WhenCalled() {
        newLogger.setSchemaURI("NewSchemaURI");
        logger.setSchemaURI("OldSchemaURI");

        logger.reconfigure(newLogger);

        assertThat("schemaURI", logger.getSchemaURI(), is("NewSchemaURI"));
    }

    @Test
    public void reconfigure_UpdatesTimestampInUTC_WhenCalled() {
        newLogger.setTimestampInUTC(false);
        logger.setTimestampInUTC(true);

        logger.reconfigure(newLogger);

        assertThat("timestampInUTC", logger.isTimestampInUTC(), is(false));
    }

    @Test
    public void reconfigure_UpdatesIncludeBOM_WhenCalled() {
        newLogger.setIncludeBOM(true);
        logger.setIncludeBOM(false);

        logger.reconfigure(newLogger);

        assertThat("includeBOM", logger.isIncludeBOM(), is(true));
    }

    @Test
    public void reconfigure_UpdatesFormatXML_WhenCalled() {
        newLogger.setFormatXML(false);
        logger.setFormatXML(true);

        logger.reconfigure(newLogger);

        assertThat("formatXML", logger.isFormatXML(), is(false));
    }

    @Test
    public void reconfigure_UpdatesSupplement95_WhenCalled() {
        newLogger.setSupplement95(true);
        logger.setSupplement95(false);

        logger.reconfigure(newLogger);

        assertThat("supplement95", logger.isSupplement95(), is(true));
    }

    @Test
    public void reconfigure_UpdatesSpoolDirectory_WhenCalled() {
        final File newDir = new File("var/local/test");
        newLogger.setSpoolDirectory(newDir);
        logger.setSpoolDirectory(new File(""));

        logger.reconfigure(newLogger);

        assertThat("spoolDirectory", logger.getSpoolDirectory(), is(newDir));
    }

    @Test
    public void reconfigure_UpdatesSpoolFileNamePrefix_WhenCalled() {
        newLogger.setSpoolFileNamePrefix("PRE");
        logger.setSpoolFileNamePrefix("POST");

        logger.reconfigure(newLogger);

        assertThat("spoolFileNamePrefix", logger.getSpoolNameFilePrefix(), is("PRE"));
    }

    @Test
    public void reconfigure_UpdatesSpoolFileNameSuffix_WhenCalled() {
        newLogger.setSpoolFileNameSuffix("POST");
        logger.setSpoolFileNameSuffix("PRE");

        logger.reconfigure(newLogger);

        assertThat("spoolFileNameSuffix", logger.getSpoolFileNameSuffix(), is("POST"));
    }

    @Test
    public void reconfigure_UpdatesRetryInterval_WhenCalled() {
        newLogger.setRetryInterval(10);
        logger.setRetryInterval(1);

        logger.reconfigure(newLogger);

        assertThat("retryInterval", logger.getRetryInterval(), is(10));
    }

    @Test
    public void reconfigure_UpdatesAuditLoggerInstalled_WhenCalled() {
        newLogger.setAuditLoggerInstalled(false);

        mockDevice.setConnections(Collections.emptyList());
        mockDevice.setInstalled(true);
        logger.setAuditLoggerInstalled(true);

        logger.reconfigure(newLogger);

        assertThat("auditLoggerInstalled", logger.getAuditLoggerInstalled(), is(false));
    }

    @Test
    public void reconfigure_UpdatesAuditRecordRepositoryDevices_WhenCalled() {
        final Connection auditUDP = createConnection("audit-udp", 514, Connection.Protocol.SYSLOG_UDP);
        final Device oldARRDevice = createAuditRecordRepository(auditUDP, "syslog");
        newLogger.setAuditRecordRepositoryDevices(Collections.singletonList(oldARRDevice));

        final Connection auditTCP = createConnection("audit-tcp", 516, Connection.Protocol.SYSLOG_TLS);
        final Device newARRDevice = createAuditRecordRepository(auditTCP, "syslog2");
        logger.setAuditRecordRepositoryDevices(Collections.singletonList(newARRDevice));

        logger.reconfigure(newLogger);

        assertThat("auditRecordRepositoryDevices", logger.getAuditRecordRepositoryDevices().get(0), is(oldARRDevice));
    }

    @Test
    public void reconfigure_UpdatesAuditSuppressCriteriaList_WhenCalled() {
        final AuditSuppressCriteria newCriteria = new AuditSuppressCriteria();
        newCriteria.setCommonName("NewCommonName");
        newLogger.setAuditSuppressCriteriaList(Collections.singletonList(newCriteria));

        final AuditSuppressCriteria oldCriteria = new AuditSuppressCriteria();
        oldCriteria.setCommonName("OldCommentName");
        logger.setAuditSuppressCriteriaList(Collections.singletonList(oldCriteria));

        logger.reconfigure(newLogger);

        assertThat("auditSuppressCriteriaList", logger.getAuditSuppressCriteriaList().get(0), is(newCriteria));
    }

    @Test
    public void reconfigure_UpdatesDoIncludeInstanceUID_WhenCalled() {
        newLogger.setDoIncludeInstanceUID(true);
        logger.setDoIncludeInstanceUID(false);

        logger.reconfigure(newLogger);

        assertThat("doIncludeInstanceUID", logger.getDoIncludeInstanceUID(), is(true));
    }

    @Test
    public void reconfigure_UpdatesConnections_WhenCalled() {
        final Connection auditUDP = createConnection("audit-udp", 514, Connection.Protocol.SYSLOG_UDP);
        newLogger.setConnections(Collections.singletonList(auditUDP));

        final Connection auditTCP = createConnection("audit-tcp", 516, Connection.Protocol.SYSLOG_TLS);
        auditTCP.setDevice(logger.getDevice());
        logger.setConnections(Collections.singletonList(auditTCP));

        mockDevice.reconfigureConnections(logger.getConnections(), newLogger.getConnections());
        expectLastCall().andVoid();
        replay(mockDevice);

        logger.reconfigure(newLogger);

        verify(mockDevice);
    }

    private static Connection createConnection(final String connectionName, final int port, final Connection.Protocol protocol) {
        final Connection connection = new Connection(connectionName, "localhost", port);
        connection.setProtocol(protocol);
        return connection;
    }

    private static Device createAuditRecordRepository(final Connection connection, final String deviceName) {
        final AuditRecordRepository arr = new AuditRecordRepository();
        arr.addConnection(connection);

        final Device arrDevice = new Device(deviceName);
        arrDevice.addDeviceExtension(arr);
        arrDevice.addConnection(connection);
        return arrDevice;
    }
}