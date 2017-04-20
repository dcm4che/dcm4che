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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che3.
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

package org.dcm4che3.net.audit;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.dcm4che3.audit.*;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.*;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
@LDAP(objectClasses = "dcmAuditLogger")
@ConfigurableClass
public class AuditLogger extends DeviceExtension {

    private static final String DICOM_PRIMARY_DEVICE_TYPE = "dicomPrimaryDeviceType";

    private static final String DEVICE_NAME_IN_FILENAME_SEPARATOR = "-._";

    private static Disruptor<AuditMessageEvent> disruptor;

    public enum SendStatus {
        SENT, QUEUED, SUPPRESSED
    }

    private static final long serialVersionUID = 1595714214186063103L;

    private static final int MSG_PROMPT_LEN = 8192;

    private static Logger LOG = LoggerFactory.getLogger(AuditLogger.class);

    public enum Facility {
        kern,            // (0) -- kernel messages
        user,            // (1) -- user-level messages
        mail,            // (2) -- mail system messages
        daemon,          // (3) -- system daemons' messages
        auth,            // (4) -- authorization messages
        syslog,          // (5) -- messages generated internally by syslogd
        lpr,             // (6) -- line printer subsystem messages
        news,            // (7) -- network news subsystem messages
        uucp,            // (8) -- UUCP subsystem messages
        cron,            // (9) -- clock daemon messages
        authpriv,        // (10)-- security/authorization messages
        ftp,             // (11)-- ftp daemon messages
        ntp,             // (12)-- NTP subsystem messages
        audit,           // (13)-- audit messages
        console,         // (14)-- console messages
        cron2,           // (15)-- clock daemon messages
        local0,          // (16)
        local1,          // (17)
        local2,          // (18)
        local3,          // (19)
        local4,          // (20)
        local5,          // (21)
        local6,          // (22)
        local7,          // (23)
    }

    public enum Severity {
        emerg,           // (0)  -- emergency; system is unusable
        alert,           // (1)  -- action must be taken immediately
        crit,            // (2)  -- critical condition
        err,             // (3)  -- error condition
        warning,         // (4)  -- warning condition
        notice,          // (5)  -- normal but significant condition
        info,            // (6)  -- informational message
        debug            // (7)  -- debug-level messages
    }

    public static final String MESSAGE_ID = "IHE+RFC-3881";

    private static final int[] DIGITS_0X = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };
    private static final int[] DIGITS_X0 = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };
    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final char SYSLOG_VERSION = '1';
    private static final InetAddress localHost = localHost();
    private static final String processID = processID();
    private static final Comparator<File> FILE_COMPARATOR = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            long diff = o1.lastModified() - o2.lastModified();
            return diff < 0 ? -1 : diff > 0 ? 1 : 0;
        }
    };
    private static volatile AuditLogger defaultLogger;

    @ConfigurableProperty(name = "dcmAuditRecordRepositoryDeviceReference",
            label = "ARR Device",
            description = "Devices that correspond to Audit Record Repositories to which audit messages are sent",
            tags = ConfigurableProperty.Tag.PRIMARY,
            collectionOfReferences = true)
    private List<Device> auditRecordRepositoryDevices = new ArrayList<Device>();

    @ConfigurableProperty(
            name = "dcmAuditFacility",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "10")
    private Facility facility = Facility.authpriv;

    @ConfigurableProperty(
            name = "dcmAuditSuccessSeverity",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "5"
    )
    private Severity successSeverity = Severity.notice;

    @ConfigurableProperty(
            name = "dcmAuditMinorFailureSeverity",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "4"
    )
    private Severity minorFailureSeverity = Severity.warning;

    @ConfigurableProperty(
            name = "dcmAuditSeriousFailureSeverity",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "3"
    )
    private Severity seriousFailureSeverity = Severity.err;

    @ConfigurableProperty(
            name = "dcmAuditMajorFailureSeverity",
            enumRepresentation = ConfigurableProperty.EnumRepresentation.ORDINAL,
            defaultValue = "2"
    )
    private Severity majorFailureSeverity = Severity.crit;


    @ConfigurableProperty(name = "dcmAuditApplicationName")
    private String applicationName;

    @ConfigurableProperty(name = "dcmAuditSourceID")
    private String auditSourceID;

    @ConfigurableProperty(name = "dcmAuditEnterpriseSiteID")
    private String auditEnterpriseSiteID;

    @ConfigurableProperty(name = "dcmAuditSourceTypeCode")
    private String[] auditSourceTypeCodes = {};

    @ConfigurableProperty(name = "dcmAuditMessageID", defaultValue = MESSAGE_ID)
    private String messageID = MESSAGE_ID;

    @ConfigurableProperty(name = "dcmAuditMessageEncoding", defaultValue = "UTF-8")
    private String encoding = "UTF-8";

    @ConfigurableProperty(name = "dcmAuditMessageSchemaURI", defaultValue =AuditMessages.SCHEMA_URI )
    private String schemaURI = AuditMessages.SCHEMA_URI;

    @ConfigurableProperty(name = "dcmAuditTimestampInUTC", defaultValue = "false")
    private boolean timestampInUTC = false;

    @ConfigurableProperty(name = "dcmAuditMessageBOM", defaultValue = "true")
    private boolean includeBOM = true;

    @ConfigurableProperty(name = "dcmAuditMessageFormatXML", defaultValue = "false")
    private boolean formatXML;

    @ConfigurableProperty(name = "dcmAuditMessageSupplement95Schema", defaultValue = "false")
    private boolean supplement95;

    @ConfigurableProperty(name = "dicomInstalled")
    private Boolean auditLoggerInstalled;

    @ConfigurableProperty(name = "dcmAuditIncludeInstanceUID")
    private Boolean doIncludeInstanceUID = false;

    @ConfigurableProperty(name = "dcmAuditLoggerSpoolDirectoryURI")
    private String spoolDirectoryURI;

    private File spoolDirectory;

    private String spoolFileNamePrefix = "audit";
    private String spoolFileNameSuffix = ".log";

    @ConfigurableProperty(name = "dcmAuditLoggerRetryInterval", defaultValue = "0")
    private int retryInterval;

    @LDAP(
            noContainerNode = true,
            distinguishingField = "cn"
    )
    @ConfigurableProperty(name = "dcmAuditSuppressCriteria")
    private final List<AuditSuppressCriteria> suppressAuditMessageFilters =
            new ArrayList<AuditSuppressCriteria>(0);

    @ConfigurableProperty(name = "dicomNetworkConnectionReference",
            label = "Connections",
            description = "Connections that can be used to send audit messages",
            tags = ConfigurableProperty.Tag.PRIMARY,
            collectionOfReferences = true)
    private List<Connection> connections = new ArrayList<Connection>(1);

    private transient MessageBuilder builder;
    private transient Map<String,ActiveConnection> activeConnection = new HashMap<String, ActiveConnection>();
    private transient ScheduledFuture<?> retryTimer;
    private transient Exception lastException;
    private transient long lastSentTimeInMillis;
    private transient final FilenameFilter FILENAME_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith(spoolFileNamePrefix) && name.endsWith(spoolFileNameSuffix);
        }
    };


    public List<AuditSuppressCriteria> getSuppressAuditMessageFilters() {
        return suppressAuditMessageFilters;
    }

    public void setSuppressAuditMessageFilters(List<AuditSuppressCriteria> suppressAuditMessageFilters) {
        this.suppressAuditMessageFilters.clear();
        for (AuditSuppressCriteria filter : suppressAuditMessageFilters) this.suppressAuditMessageFilters.add(filter);
    }

    public final List<Device> getAuditRecordRepositoryDevices() {
        return auditRecordRepositoryDevices;
    }

    public List<String> getAuditRecordRepositoryDeviceNames() {
        if (auditRecordRepositoryDevices == null)
            throw new IllegalStateException("AuditRecordRepositoryDevice not initalized");
        List<String> names = new ArrayList<String>(auditRecordRepositoryDevices.size());
        for (Device d : auditRecordRepositoryDevices) {
        	names.add(d.getDeviceName());
        }
        return names;
    }

    public void setAuditRecordRepositoryDevices(List<Device> arrDevices) {
    	for (ActiveConnection c : activeConnection.values())
    		SafeClose.close(c);
        activeConnection.clear();
        if(arrDevices==null){
            this.auditRecordRepositoryDevices.clear();
        }else{
            this.auditRecordRepositoryDevices = arrDevices;
        }
    }
    
    public void addAuditRecordRepositoryDevice(Device device) {
    	auditRecordRepositoryDevices.add(device);
    }

    public boolean removeAuditRecordRepositoryDevice(Device device) {
    	return auditRecordRepositoryDevices.remove(device);
    }

    public final Facility getFacility() {
        return facility;
    }

    public final void setFacility(Facility facility) {
        if (facility == null)
            throw new NullPointerException();
        this.facility = facility;
    }

    public final Severity getSuccessSeverity() {
        return successSeverity;
    }

    public final void setSuccessSeverity(Severity severity) {
        if (severity == null)
            throw new NullPointerException();
        this.successSeverity = severity;
    }

    public final Severity getMinorFailureSeverity() {
        return minorFailureSeverity;
    }

    public final void setMinorFailureSeverity(Severity severity) {
        if (severity == null)
            throw new NullPointerException();
        this.minorFailureSeverity = severity;
    }

    public final Severity getSeriousFailureSeverity() {
        return seriousFailureSeverity;
    }

    public final void setSeriousFailureSeverity(Severity severity) {
        if (severity == null)
            throw new NullPointerException();
        this.seriousFailureSeverity = severity;
    }

    public final Severity getMajorFailureSeverity() {
        return majorFailureSeverity;
    }

    public final void setMajorFailureSeverity(Severity severity) {
        if (severity == null)
            throw new NullPointerException();
        this.majorFailureSeverity = severity;
    }

    public final String getApplicationName() {
        return applicationName;
    }

    private String applicationName() {
        return applicationName != null
                ? applicationName
                : auditSourceID();
    }

    public final void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public final String getAuditSourceID() {
        return auditSourceID;
    }

    public final void setAuditSourceID(String auditSourceID) {
        this.auditSourceID = auditSourceID;
    }

    private String auditSourceID() {
        return auditSourceID != null
                ? auditSourceID
                : getDevice().getDeviceName();
    }

    public final String getAuditEnterpriseSiteID() {
        return auditEnterpriseSiteID;
    }

    public final void setAuditEnterpriseSiteID(String auditEnterpriseSiteID) {
        this.auditEnterpriseSiteID = auditEnterpriseSiteID;
    }

    public String[] getAuditSourceTypeCodes() {
        return auditSourceTypeCodes;
    }

    public void setAuditSourceTypeCodes(String... auditSourceTypeCode) {
        this.auditSourceTypeCodes = auditSourceTypeCode;
    }

    public ActiveParticipant createActiveParticipant(
            boolean requestor, RoleIDCode... roleIDs) {

        Collection<String> aets = device.getApplicationAETitles();

        return createActiveParticipant(requestor,
                processID(),
                AuditMessages.alternativeUserIDForAETitle(
                        aets.toArray(new String[aets.size()])),
                applicationName(),
                localHost().getHostName(),
                roleIDs);
    }

    public ActiveParticipant createActiveParticipant(
            boolean requestor,
            String userID,
            String alternativeUserID,
            String userName,
            String hostName,
            RoleIDCode... roleIDs) {
        ActiveParticipant ap = new ActiveParticipant();
        ap.setUserID(userID);
        ap.setAlternativeUserID(alternativeUserID);
        ap.setUserName(userName);
        ap.setUserIsRequestor(requestor);
        ap.setNetworkAccessPointID(hostName);
        ap.setNetworkAccessPointTypeCode(AuditMessages.isIP(hostName)
                ? AuditMessages.NetworkAccessPointTypeCode.IPAddress
                : AuditMessages.NetworkAccessPointTypeCode.MachineName);
        for (RoleIDCode roleID : roleIDs)
            ap.getRoleIDCode().add(roleID);
        return ap;
    }

    public AuditSourceIdentification createAuditSourceIdentification() {
        AuditSourceIdentification asi = new AuditSourceIdentification();
        asi.setAuditSourceID(auditSourceID());
        if (auditEnterpriseSiteID != null) {
            if (auditEnterpriseSiteID.equals("dicomInstitutionName")) {
                String[] institutionNames = getDevice().getInstitutionNames();
                if (institutionNames.length > 0)
                    asi.setAuditEnterpriseSiteID(institutionNames[0]);
            } else
                asi.setAuditEnterpriseSiteID(auditEnterpriseSiteID);
        }
        for (String code : auditSourceTypeCodes) {
            if (code.equals(DICOM_PRIMARY_DEVICE_TYPE)) {
                for (String type : device.getPrimaryDeviceTypes()) {
                    AuditSourceTypeCode astc = new AuditSourceTypeCode();
                    astc.setCode(type);
                    astc.setCodeSystemName("DCM");
                    asi.getAuditSourceTypeCode().add(astc);
                }
            } else {
                AuditSourceTypeCode astc = new AuditSourceTypeCode();
                astc.setCode(code);
                asi.getAuditSourceTypeCode().add(astc);
            }
        }
        return asi;
    }

    public final String getMessageID() {
        return messageID;
    }

    public final void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public final String getEncoding() {
        return encoding;
    }

    public final void setEncoding(String encoding) {
        if (!Charset.isSupported(encoding))
            throw new IllegalArgumentException(
                    "Charset not supported: " + encoding);
        this.encoding = encoding;
    }

    public final String getSchemaURI() {
        return schemaURI;
    }

    public final void setSchemaURI(String schemaURI) {
        this.schemaURI = schemaURI;
    }

    public final boolean isTimestampInUTC() {
        return timestampInUTC;
    }

    public final void setTimestampInUTC(boolean timestampInUTC) {
        this.timestampInUTC = timestampInUTC;
    }

    public final boolean isIncludeBOM() {
        return includeBOM;
    }

    public final void setIncludeBOM(boolean includeBOM) {
        this.includeBOM = includeBOM;
    }

    public final boolean isFormatXML() {
        return formatXML;
    }

    public final void setFormatXML(boolean formatXML) {
        this.formatXML = formatXML;
    }

    public boolean isSupplement95() {
        return supplement95;
    }

    public void setSupplement95(boolean sup95) {
        this.supplement95 = sup95;
    }

    public boolean isInstalled() {
        return device != null && device.isInstalled()
                && (auditLoggerInstalled == null || auditLoggerInstalled.booleanValue());
    }

    public final Boolean getAuditLoggerInstalled() {
        return auditLoggerInstalled;
    }

    public void setAuditLoggerInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue()
                && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.auditLoggerInstalled = installed;
    }


    public Boolean isIncludeInstanceUID() {
        return doIncludeInstanceUID;
    }

    public Boolean getDoIncludeInstanceUID() {
        return doIncludeInstanceUID;
    }

    public void setDoIncludeInstanceUID(Boolean doIncludeInstanceUID) {
        this.doIncludeInstanceUID = doIncludeInstanceUID;
    }

    /**
     * Get spool directory into which messages failed to sent to the record
     * repository are stored for later re-send.
     *
     * @return The directory in which the messages failed to sent are stored,
     * or {@code null} if the default temporary-file directory is to
     * be used
     */
    public File getSpoolDirectory() {
        return spoolDirectory;
    }

    /**
     * Set spool directory into which messages failed sent to the record
     * repository are stored for later re-send.
     *
     * @param directory The directory in which the messages failed to sent are
     *                  stored, or {@code null} if the default temporary-file
     *                  directory is to be used
     */
    public void setSpoolDirectory(File directory) {
        this.spoolDirectory = directory;
    }

    public String getSpoolDirectoryURI() {
        return spoolDirectory != null ? spoolDirectory.toURI().toString() : null;
    }

    public void setSpoolDirectoryURI(String uri) {
        this.spoolDirectory = uri != null ? new File(URI.create(uri)) : null;
    }

    public String getSpoolNameFilePrefix() {
        return spoolFileNamePrefix;
    }

    public void setSpoolFileNamePrefix(String prefix) {
        if (prefix.length() < 3)
            throw new IllegalArgumentException("Spool file name prefix too short");
        this.spoolFileNamePrefix = prefix;
    }

    public String getSpoolFileNameSuffix() {
        return spoolFileNameSuffix;
    }

    public void setSpoolFileNameSuffix(String suffix) {
        if (suffix.isEmpty())
            throw new IllegalArgumentException("Spool file name suffix cannot be empty");
        this.spoolFileNameSuffix = suffix;
    }

    /**
     * Get interval in seconds to retry to sent messages which could not be
     * sent to the record repository or {@code 0} if messages failed to sent
     * are not spooled for later re-send.
     *
     * @return interval retry interval in seconds or {@code 0}
     * @see #write(Calendar, AuditMessage)
     */
    public int getRetryInterval() {
        return retryInterval;
    }

    /**
     * Set interval in seconds to retry to sent messages which could not be
     * sent to the record repository or {@code 0} if messages failed to sent
     * are not spooled for later re-send.
     *
     * @param interval retry interval in seconds or {@code 0}
     * @see #write(Calendar, AuditMessage)
     */
    public void setRetryInterval(int interval) {
        this.retryInterval = interval;
    }

    public void addConnection(Connection conn) {
        if (!conn.getProtocol().isSyslog())
            throw new IllegalArgumentException(
                    "Audit Logger does not support protocol " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " +
                    device.getDeviceName());
        connections.add(conn);
    }

    @Override
    public void verifyNotUsed(Connection conn) {
        if (connections.contains(conn))
            throw new IllegalStateException(conn + " used by Audit Logger");
    }

    public boolean removeConnection(Connection conn) {
        return connections.remove(conn);
    }


    public void setConnections(List<Connection> connections) {
        this.connections.clear();
        for (Connection connection : connections) addConnection(connection);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public List<AuditSuppressCriteria> getAuditSuppressCriteriaList() {
        return suppressAuditMessageFilters;
    }

    public AuditSuppressCriteria findAuditSuppressCriteriaByCommonName(String cn) {
        for (AuditSuppressCriteria criteria : suppressAuditMessageFilters) {
            if (criteria.getCommonName().equals(cn))
                return criteria;
        }
        return null;
    }

    public void setAuditSuppressCriteriaList(List<AuditSuppressCriteria> filters) {
        this.suppressAuditMessageFilters.clear();
        this.suppressAuditMessageFilters.addAll(filters);
    }

    public void addAuditSuppressCriteria(AuditSuppressCriteria criteria) {
        this.suppressAuditMessageFilters.add(criteria);
    }

    public void clearAllAuditSuppressCriteria() {
        this.suppressAuditMessageFilters.clear();
    }

    /**
     * Test if the Event Identification and the Active ActiveParticipant of an
     * Audit Message matches one of the {@code AuditSuppressCriteria}
     *
     * @param msg Audit Message to test
     * @return {@code true} the specified audit message will be suppressed;
     * otherwise {@code false}
     */
    public boolean isAuditMessageSuppressed(AuditMessage msg) {
        for (AuditSuppressCriteria criteria : suppressAuditMessageFilters) {
            if (criteria.match(msg))
                return true;
        }
        return false;
    }

    @Override
    public void reconfigure(DeviceExtension from) {
        reconfigure((AuditLogger) from);
    }

    private void reconfigure(AuditLogger from) {
        setFacility(from.facility);
        setSuccessSeverity(from.successSeverity);
        setMinorFailureSeverity(from.minorFailureSeverity);
        setSeriousFailureSeverity(from.seriousFailureSeverity);
        setMajorFailureSeverity(from.majorFailureSeverity);
        setApplicationName(from.applicationName);
        setAuditSourceID(from.auditSourceID);
        setAuditEnterpriseSiteID(from.auditEnterpriseSiteID);
        setAuditSourceTypeCodes(from.auditSourceTypeCodes);
        setMessageID(from.messageID);
        setEncoding(from.encoding);
        setSchemaURI(from.schemaURI);
        setTimestampInUTC(from.timestampInUTC);
        setIncludeBOM(from.includeBOM);
        setFormatXML(from.formatXML);
        setSupplement95(from.isSupplement95());
        setSpoolDirectory(from.spoolDirectory);
        setSpoolFileNamePrefix(from.spoolFileNamePrefix);
        setSpoolFileNameSuffix(from.spoolFileNameSuffix);
        setRetryInterval(from.retryInterval);
        setAuditLoggerInstalled(from.auditLoggerInstalled);
        setAuditRecordRepositoryDevices(from.auditRecordRepositoryDevices);
        setAuditSuppressCriteriaList(from.suppressAuditMessageFilters);
        device.reconfigureConnections(connections, from.connections);

        closeActiveConnection();
    }

    public Calendar timeStamp() {
        return timestampInUTC
                ? new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)
                : new GregorianCalendar(Locale.ENGLISH);
    }

    /**
     * Send Audit Message by Syslog Protocol to Audit Record Repository, if the
     * message does not match any configured {@code AuditSuppressCriteria}. If
     * an I/O error occurs sending the message to the {@code AuditRecordRepository}
     * and if a {@code RetryInterval) is configured, the message will be spooled
     * into the configured {@code SpoolDirectory} for later re-send and the
     * method returns {@code false}. If no {@code RetryInterval} is configured,
     * the method throws an {@code IOException) if an I/O error occurs sending
     * the message.
     * <p/>
     * Attention: sending via UDP without getting an I/O error does not ensure
     * that the Audit Record Repository actually received the message!
     *
     * @param timeStamp included in Syslog Header
     * @param msg       Audit Message
     * @return {@code SendStatus.SUPPRESSED} if the message was suppressed;
     * {@code SendStatus.SENT} if the message was successfully emitted;
     * {@code SendStatus.QUEUED} if the message was spooled for later re-send
     * @throws IllegalStateException           if there is no {@code AuditRecordRepository} associated with
     *                                         this {@code AuditLogger}
     * @throws IncompatibleConnectionException if no {@code Connection) of this {@code AuditLogger} is compatible
     *                                         with any {@code Connection) of the associated {@code AuditRecordRepository}
     * @throws GeneralSecurityException        if the {@link  SSLContext} could not get intialized from configured
     *                                         private key and public certificates
     * @throws IOException                     if an I/O error occurs sending the message to the {@code AuditRecordRepository}
     *                                         or on spooling the message to the file system
     */
    public SendStatus write(Calendar timeStamp, AuditMessage msg)
            throws IncompatibleConnectionException, GeneralSecurityException, IOException {
        if (isAuditMessageSuppressed(msg))
            return SendStatus.SUPPRESSED;

        return sendMessage(builder().createMessage(timeStamp, msg));
    }

    public void writeAsync(Calendar timeStamp, AuditMessage msg)
            throws IncompatibleConnectionException, GeneralSecurityException, IOException, InsufficientCapacityException {

        if (isAuditMessageSuppressed(msg))
            return;

        RingBuffer<AuditMessageEvent> ringBuffer = getDisruptor(this).getRingBuffer();

        long sequence = ringBuffer.next();  // Grab the next sequence
        try
        {
            AuditMessageEvent msgenrtry = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            msgenrtry.setLogger(this);  // Fill with data
            msgenrtry.setMessage(msg);
        }
        finally
        {
            ringBuffer.publish(sequence);
        }
    }

    public SendStatus write(Calendar timeStamp, Severity severity,
                            byte[] data, int off, int len)
            throws IncompatibleConnectionException, GeneralSecurityException, IOException {
        return sendMessage(
                builder().createMessage(timeStamp, severity, data, off, len));
    }

    private MessageBuilder builder() {
        if (builder == null)
            builder = new MessageBuilder();

        return builder;
    }

    private SendStatus sendMessage(DatagramPacket msg) throws IncompatibleConnectionException,
            GeneralSecurityException, IOException {
        String deviceName;
        SendStatus status = SendStatus.SENT;
        for (Device arrDev : auditRecordRepositoryDevices) {
        	deviceName = arrDev.getDeviceName();
	        if (getNumberOfQueuedMessages(deviceName) > 0) {
	            spoolMessage(deviceName, msg);
	        } else {
	            try {
	                activeConnection(arrDev).sendMessage(msg);
	                lastSentTimeInMillis = System.currentTimeMillis();
	            } catch (IOException e) {
	                lastException = e;
	                if (retryInterval > 0) {
	                    LOG.info("Failed to send audit message:", e);
	                    spoolMessage(deviceName, msg);
	                    scheduleRetry();
	                    status = SendStatus.QUEUED;
	                } else {
	                    throw e;
	                }
	            }
	        }
        }
        return status;
    }

    private synchronized void scheduleRetry() {
        if (retryTimer != null || retryInterval <= 0) {
            return;
        }

        LOG.debug("Scheduled retry in {} s", retryInterval);
        retryTimer = getDevice().schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        synchronized (AuditLogger.this) {
                            retryTimer = null;
                        }
                        sendQueuedMessages();
                    }
                },
                retryInterval, TimeUnit.SECONDS);
    }

    private void spoolMessage(String deviceName, DatagramPacket msg) throws IOException {
        if (spoolDirectory != null)
            spoolDirectory.mkdirs();

        File f = null;
        try {
            f = File.createTempFile(spoolFileNamePrefix+DEVICE_NAME_IN_FILENAME_SEPARATOR+deviceName+DEVICE_NAME_IN_FILENAME_SEPARATOR, spoolFileNameSuffix, spoolDirectory);
            if (spoolDirectory == null)
                spoolDirectory = f.getParentFile();

            LOG.info("Spool audit message to {}", f);
            FileOutputStream out = new FileOutputStream(f);
            try {
                out.write(msg.getData(), msg.getOffset(), msg.getLength());
            } finally {
                SafeClose.close(out);
            }
            f = null;
        } catch (IOException e) {
            throw new IOException("Failed to spool audit message for device "+deviceName, e);
        } finally {
            if (f != null)
                f.delete();
        }
    }

    public void sendQueuedMessages() {
        File dir = spoolDirectory;
        if (dir == null)
            return;
        boolean failed = false;
        for (final Device arrDev : this.auditRecordRepositoryDevices) {
	        try {
	        	FilenameFilter fnFilter = new FilenameFilter() {
	        		String prefix = spoolFileNamePrefix+DEVICE_NAME_IN_FILENAME_SEPARATOR+arrDev.getDeviceName()+DEVICE_NAME_IN_FILENAME_SEPARATOR;
	                @Override
	                public boolean accept(File dir, String name) {
	                    return name.startsWith(prefix) 
	                    		&& name.endsWith(spoolFileNameSuffix);
	                }
	            };
	            File[] queuedMessages = dir.listFiles(fnFilter);
	            byte[] b = null;
	            while (queuedMessages != null && queuedMessages.length > 0) {
	                Arrays.sort(queuedMessages, FILE_COMPARATOR);
	                for (File file : queuedMessages) {
	                    LOG.debug("Read audit message from {}", file);
	                    int len = (int) file.length();
	                    if (b == null || b.length < len)
	                        b = new byte[len];
	                    try {
	                        FileInputStream in = new FileInputStream(file);
	                        try {
	                            StreamUtils.readFully(in, b, 0, len);
	                        } finally {
	                            SafeClose.close(in);
	                        }
	                    } catch (IOException e) {
	                        LOG.warn("Failed to read audit message from {}", file, e);
	                        File dest = new File(file.getParent(), file.getName() + ".err");
	                        file.renameTo(dest);
	                        continue;
	                    }
                    	activeConnection(arrDev).sendMessage(new DatagramPacket(b, 0, len));
	                    lastSentTimeInMillis = System.currentTimeMillis();
	                    if (file.delete())
	                        LOG.debug("Delete spool file {}", file);
	                    else
	                        LOG.warn("Failed to delete spool file {}", file);
	                }
	                queuedMessages = dir.listFiles(FILENAME_FILTER);
	            }
	        } catch (Exception e) {
	            lastException = e;
	            LOG.info("Failed to send audit message:", e);
	            failed = true;
	        }
        }
        if (failed)
        	scheduleRetry();
        synchronized (this) {
            notify();
        }
    }

	public Exception getLastException() {
        return lastException;
    }

    public long getLastSentTimeInMillis() {
        return lastSentTimeInMillis;
    }

    public int getNumberOfQueuedMessages() {
    	int tot = 0;
    	for (Device d : this.auditRecordRepositoryDevices)
    		tot += getNumberOfQueuedMessages(d.getDeviceName());
    	return tot;
    }
    
    public int getNumberOfQueuedMessages(final String deviceName) {
        try {
        	FilenameFilter fnFilter = new FilenameFilter() {
        		String prefix = spoolFileNamePrefix+DEVICE_NAME_IN_FILENAME_SEPARATOR+deviceName+DEVICE_NAME_IN_FILENAME_SEPARATOR;
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(prefix) 
                    		&& name.endsWith(spoolFileNameSuffix);
                }
            };

            return spoolDirectory.list(fnFilter).length;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public File[] getQueuedMessages() {
        try {
            return spoolDirectory.listFiles(FILENAME_FILTER);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public synchronized void waitForNoQueuedMessages(long timeout)
            throws InterruptedException {
    	int count;
    	for (Device arrDev : this.auditRecordRepositoryDevices) {
    		while ( (count = getNumberOfQueuedMessages(arrDev.getDeviceName())) > 0) {
    			LOG.debug("Wait for {} queued Audit Messages for AuditRepository {}!", count, arrDev.getDeviceName());
    			wait(timeout);
    		}
    	}
    }

    public synchronized void closeActiveConnection() {
        for (Map.Entry<String, ActiveConnection> entry : activeConnection.entrySet()) {
            try {
                entry.getValue().close();
            } catch (IOException e) {
            	LOG.error("Failed to close active connection to {}", entry.getKey(), e);
                throw new AssertionError(e);
            }
            this.activeConnection.clear();
        }
    }

    private synchronized ActiveConnection activeConnection(Device arrDev)
            throws IncompatibleConnectionException {
        ActiveConnection activeConnection = this.activeConnection.get(arrDev.getDeviceName());
        if (activeConnection != null)
            return activeConnection;

        AuditRecordRepository arr = arrDev.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null)
            throw new IllegalStateException("AuditRecordRepositoryDevice "
                    + arrDev.getDeviceName()
                    + " does not provide Audit Record Repository");

        for (Connection remoteConn : arr.getConnections())
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : connections)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn)) {
                        activeConnection = conn.getProtocol().isTCP()
                                        ? new TCPConnection(conn, remoteConn)
                                        : new UDPConnection(conn, remoteConn);
                        this.activeConnection.put(arrDev.getDeviceName(), activeConnection);
                        return activeConnection;
                    }
        throw new IncompatibleConnectionException(
                "No compatible connection to " + arr + " available on " + this);
    }

	public static String processID() {
        String s = ManagementFactory.getRuntimeMXBean().getName();
        int atPos = s.indexOf('@');
        return atPos > 0 ? s.substring(0, atPos)
                : Integer.toString(new Random().nextInt() & 0x7fffffff);
    }

    public static InetAddress localHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private Severity severityOf(AuditMessage msg) {
        String eventOutcomeIndicator = msg.getEventIdentification()
                .getEventOutcomeIndicator();
        if (eventOutcomeIndicator.length() == 1)
            switch (eventOutcomeIndicator.charAt(0)) {
                case '0':
                    return successSeverity;
                case '4':
                    return minorFailureSeverity;
                case '8':
                    return seriousFailureSeverity;
            }
        else if (eventOutcomeIndicator.equals("12"))
            return majorFailureSeverity;

        throw new IllegalArgumentException(
                "Illegal eventOutcomeIndicator: " + eventOutcomeIndicator);
    }

    private int prival(Severity severity) {
        return (facility.ordinal() << 3) | severity.ordinal();
    }

    public static AuditLogger getDefaultLogger() {
        return defaultLogger;
    }

    public static void setDefaultLogger(AuditLogger defaultLogger) {
        AuditLogger.defaultLogger = defaultLogger;
    }

    private class MessageBuilder extends ByteArrayOutputStream {

        DatagramPacket createMessage(Calendar timeStamp, AuditMessage msg) {
            try {
                reset();
                writeHeader(severityOf(msg), timeStamp);
                if (!supplement95) {
                    AuditMessages.toXML(msg, builder, formatXML, encoding, schemaURI);
                } else {
                    AuditMessages.toSupplement95XML(msg, builder, formatXML, encoding, schemaURI);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new DatagramPacket(buf, 0, count);
        }

        DatagramPacket createMessage(Calendar timeStamp, Severity severity,
                                     byte[] data, int off, int len) {
            try {
                reset();
                writeHeader(severity, timeStamp);
                write(data, off, len);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new DatagramPacket(buf, 0, count);
        }

        void writeHeader(Severity severity, Calendar timeStamp)
                throws IOException {
            write('<');
            writeInt(prival(severity));
            write('>');
            write(SYSLOG_VERSION);
            write(' ');
            write(timeStamp);
            write(' ');
            if (localHost != null)
                write(localHost.getCanonicalHostName().getBytes(encoding));
            else
                write('-');
            write(' ');
            write(applicationName().getBytes(encoding));
            write(' ');
            write(processID.getBytes(encoding));
            write(' ');
            if (messageID != null)
                write(messageID.getBytes(encoding));
            else
                write('-');
            write(' ');
            write('-');
            write(' ');
            if (includeBOM && encoding.equals("UTF-8"))
                write(BOM);
        }

        void writeInt(int i) {
            if (i >= 100)
                writeNNN(i);
            else if (i >= 10)
                writeNN(i);
            else
                writeN(i);
        }

        void write(Calendar timeStamp) {
            writeNNNN(timeStamp.get(Calendar.YEAR));
            write('-');
            writeNN(timeStamp.get(Calendar.MONTH) + 1);
            write('-');
            writeNN(timeStamp.get(Calendar.DAY_OF_MONTH));
            write('T');
            writeNN(timeStamp.get(Calendar.HOUR_OF_DAY));
            write(':');
            writeNN(timeStamp.get(Calendar.MINUTE));
            write(':');
            writeNN(timeStamp.get(Calendar.SECOND));
            write('.');
            writeNNN(timeStamp.get(Calendar.MILLISECOND));
            int tzOffset = timeStamp.get(Calendar.ZONE_OFFSET)
                    + timeStamp.get(Calendar.DST_OFFSET);
            if (tzOffset == 0)
                write('Z');
            else {
                tzOffset /= 60000;
                if (tzOffset > 0)
                    write('+');
                else {
                    write('-');
                    tzOffset = -tzOffset;
                }
                writeNN(tzOffset / 60);
                write(':');
                writeNN(tzOffset % 60);
            }
        }

        void writeNNNN(int i) {
            writeNN(i / 100);
            writeNN(i % 100);
        }

        void writeNNN(int i) {
            writeN(i / 100);
            writeNN(i % 100);
        }

        void writeNN(int i) {
            write(DIGITS_X0[i]);
            write(DIGITS_0X[i]);
        }

        void writeN(int i) {
            write(DIGITS_0X[i]);
        }

    }

    private static String toString(DatagramPacket packet) {
        try {
            int len = packet.getLength();
            boolean truncate = len > MSG_PROMPT_LEN;
            String s = new String(packet.getData(), 0,
                    truncate ? MSG_PROMPT_LEN : len, "UTF-8");
            if (truncate)
                s += "...";
            return s;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private abstract class ActiveConnection implements Closeable {
        final Connection conn;
        final Connection remoteConn;

        ActiveConnection(Connection conn, Connection remoteConn) {
            this.conn = conn;
            this.remoteConn = remoteConn;
        }

        abstract void sendMessage(DatagramPacket msg) throws IOException,
                IncompatibleConnectionException, GeneralSecurityException;

    }

    private class UDPConnection extends ActiveConnection {
        DatagramSocket ds;

        UDPConnection(Connection conn, Connection remoteConn) {
            super(conn, remoteConn);
        }

        @Override
        void sendMessage(DatagramPacket msg) throws IOException {

            if (ds == null)
                ds = conn.createDatagramSocket();

            InetSocketAddress endPoint = remoteConn.getEndPoint();
            LOG.info("Send audit message to {}", endPoint);
            if (LOG.isDebugEnabled())
                LOG.debug(AuditLogger.toString(msg));
            msg.setSocketAddress(endPoint);
            ds.send(msg);
        }

        @Override
        public void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

    }

    private class TCPConnection extends ActiveConnection {
        Socket sock;
        OutputStream out;
        ScheduledFuture<?> idleTimer;

        TCPConnection(Connection conn, Connection remoteConn) {
            super(conn, remoteConn);
        }

        void connect() throws IOException,
                IncompatibleConnectionException, GeneralSecurityException {
            if (sock == null) {
                sock = conn.connect(remoteConn);
                out = sock.getOutputStream();
            }
        }

        @Override
        synchronized void sendMessage(DatagramPacket packet) throws IOException,
                IncompatibleConnectionException, GeneralSecurityException {
            stopIdleTimer();
            connect();
            try {
                trySendMessage(packet);
            } catch (IOException e) {
                LOG.info("Failed to send audit message to {} - reconnect",
                        sock, e);
                close();
                connect();
                trySendMessage(packet);
            }
            startIdleTimer();
        }

        void trySendMessage(DatagramPacket packet) throws IOException {
            LOG.info("Send audit message to {}", sock);
            if (LOG.isDebugEnabled())
                LOG.debug(AuditLogger.toString(packet));
            out.write(Integer.toString(packet.getLength()).getBytes(encoding));
            out.write(' ');
            out.write(packet.getData(), packet.getOffset(), packet.getLength());
            out.flush();
        }

        private void startIdleTimer() {
            int idleTimeout = conn.getIdleTimeout();
            if (idleTimeout > 0) {
                LOG.debug("Start Idle timeout of {} ms for {}", idleTimeout, sock);
                try {
                    idleTimer = conn.getDevice().schedule(
                            new Runnable() {
                                @Override
                                public void run() {
                                    onIdleTimerExpired();
                                }
                            },
                            idleTimeout,
                            TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    LOG.warn("Failed to start Idle timeout", e);
                }
            }
        }

        private void stopIdleTimer() {
            if (idleTimer != null) {
                LOG.debug("Stop Idle timer for {}", sock);
                idleTimer.cancel(false);
                idleTimer = null;
            }
        }

        @Override
        public synchronized void close() {
            stopIdleTimer();
            closeSocket();
        }

        private void closeSocket() {
            if (sock != null)
                conn.close(sock);
            sock = null;
            out = null;
        }

        private void onIdleTimerExpired() {
            ScheduledFuture<?> expiredIdleTimer = idleTimer;
            synchronized (this) {
                if (expiredIdleTimer != idleTimer) {
                    LOG.debug("Detect restart of Idle timer for {}", sock);
                } else {
                    LOG.info("Idle timeout for {} expired", sock);
                    idleTimer = null;
                    closeSocket();
                }
            }
        }

    }

    public static Disruptor<AuditMessageEvent> getDisruptor(AuditLogger logger) {
        if (disruptor == null)
            disruptor = initializeDisruptor(logger);

        return disruptor;
    }

    private static Disruptor<AuditMessageEvent> initializeDisruptor(AuditLogger logger) {
        // Executor that will be used to construct new threads for consumers
        Executor executor = Executors.newCachedThreadPool();

        // The factory for the event
        AuditMessageEventFactory factory = new AuditMessageEventFactory();

        // Specify the size of the ring buffer, must be power of 2.
        int bufferSize = 8;

        Disruptor<AuditMessageEvent> disruptorInstance = new Disruptor<AuditMessageEvent>(factory, bufferSize, executor);

        // Connect the handler
        disruptorInstance.handleEventsWith(new AuditMessageEventHandler());

        // Start the Disruptor, starts all threads running
        disruptorInstance.start();

        return disruptorInstance;
    }
}
