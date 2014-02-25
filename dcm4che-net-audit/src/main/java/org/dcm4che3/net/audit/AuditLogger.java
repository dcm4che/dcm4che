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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.dcm4che3.audit.ActiveParticipant;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditSourceIdentification;
import org.dcm4che3.audit.AuditSourceTypeCode;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.AuditMessages.RoleIDCode;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.util.SafeClose;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 */
public class AuditLogger extends DeviceExtension {

    private static final long serialVersionUID = 1595714214186063103L;

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

    public static final String MESSAGE_ID = "DICOM+RFC3881";

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
    private static final byte[] BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
    private static final char SYSLOG_VERSION = '1';
    private static final InetAddress localHost = localHost();
    private static final String processID = processID();

    private static volatile AuditLogger defaultLogger;
    
    private Device arrDevice;
    private Facility facility = Facility.authpriv;
    private Severity successSeverity = Severity.notice;
    private Severity minorFailureSeverity = Severity.warning;
    private Severity seriousFailureSeverity = Severity.err;
    private Severity majorFailureSeverity = Severity.crit;
    private String applicationName;
    private String auditSourceID;
    private String auditEnterpriseSiteID;
    private String[] auditSourceTypeCodes = {};
    private String messageID = MESSAGE_ID;
    private String encoding = "UTF-8";
    private String schemaURI = AuditMessages.SCHEMA_URI;
    private boolean timestampInUTC = false;
    private boolean includeBOM = true;
    private boolean formatXML;
    private Boolean installed;
    private Boolean includeInstanceUID = false;

    private final List<Connection> conns = new ArrayList<Connection>(1);

    private transient ActiveConnection activeConnection;

    public final Device getAuditRecordRepositoryDevice() {
        return arrDevice;
    }

    public String getAuditRecordRepositoryDeviceName() {
        if (arrDevice == null)
            throw new IllegalStateException("AuditRecordRepositoryDevice not initalized");
        return arrDevice.getDeviceName();
    }

    public void setAuditRecordRepositoryDevice(Device arrDevice) {
        SafeClose.close(activeConnection);
        activeConnection = null;
        this.arrDevice = arrDevice;
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
        ActiveParticipant ap = new ActiveParticipant();
        ap.setUserID(processID());
        Collection<String> aets = device.getApplicationAETitles();
        ap.setAlternativeUserID(
                AuditMessages.alternativeUserIDForAETitle(
                        aets.toArray(new String[aets.size()])));
        ap.setUserName(applicationName());
        ap.setUserIsRequestor(requestor);
        String hostName = localHost().getHostName();
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
            if (code.equals("dicomPrimaryDeviceType")) {
                for (String type : device.getPrimaryDeviceTypes()) {
                    AuditSourceTypeCode astc = new AuditSourceTypeCode();
                    astc.setCode(type);
                    astc.setCodeSystemName("DCM");
                    asi.getAuditSourceTypeCode().add(astc);
                }
            } else {
                AuditSourceTypeCode astc = new AuditSourceTypeCode();
                astc.setCode(code);
                asi.getAuditSourceTypeCode().add(astc );
            }
        }
        return asi ;
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

    public boolean isInstalled() {
        return device != null && device.isInstalled() 
                && (installed == null || installed.booleanValue());
    }

    public final Boolean getInstalled() {
        return installed;
    }

    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue()
                && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    public Boolean isIncludeInstanceUID() {
        return includeInstanceUID;
    }

    public void setIncludeInstanceUID(Boolean includeInstanceUID) {
        this.includeInstanceUID = includeInstanceUID;
    }

    public void addConnection(Connection conn) {
        if (!conn.getProtocol().isSyslog())
            throw new IllegalArgumentException(
                    "Audit Logger does not support protocol " + conn.getProtocol());
        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by " + 
                    device.getDeviceName());
        conns.add(conn);
    }

    @Override
    public void verifyNotUsed(Connection conn) {
        if (conns.contains(conn))
            throw new IllegalStateException(conn + " used by Audit Logger");
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    @Override
    public void reconfigure(DeviceExtension from)  {
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
        setInstalled(from.installed);
        setAuditRecordRepositoryDevice(from.arrDevice);
        device.reconfigureConnections(conns, from.conns);
    }

    public Calendar timeStamp() {
        return timestampInUTC 
            ? new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)
            : new GregorianCalendar(Locale.ENGLISH);
    }

    public void write(Calendar timeStamp, AuditMessage message)
            throws IncompatibleConnectionException, GeneralSecurityException {
        connection().send(timeStamp, message);
    }

    private ActiveConnection connection()
            throws IncompatibleConnectionException {
        ActiveConnection activeConnection = this.activeConnection;
        if (activeConnection != null)
            return activeConnection;

        Device arrDev = this.arrDevice;
        if (arrDevice == null)
            throw new IllegalStateException("No AuditRecordRepositoryDevice initalized");
        AuditRecordRepository arr = arrDev.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null)
            throw new IllegalStateException("AuditRecordRepositoryDevice "
                    + arrDevice.getDeviceName()
                    + " does not provide Audit Record Repository");
        for (Connection remoteConn : arr.getConnections())
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn)) {
                        return (this.activeConnection = 
                                conn.getProtocol().isTCP()
                                ? new TCPConnection(conn, remoteConn)
                                : new UDPConnection(conn, remoteConn));
                     }
        throw new IncompatibleConnectionException(
                "No compatible connection to " + arr + " available on " + this);
    }


    public static String processID() {
        String s =  ManagementFactory.getRuntimeMXBean().getName();
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
            switch(eventOutcomeIndicator.charAt(0)) {
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

    private abstract class ActiveConnection extends ByteArrayOutputStream {
        final Connection conn;
        final Connection remoteConn;
        ActiveConnection(Connection conn, Connection remoteConn) {
            this.conn = conn;
            this.remoteConn = remoteConn;
        }

        abstract void connect() throws IOException,
                IncompatibleConnectionException, GeneralSecurityException;

        abstract void sendMessage() throws IOException;

        void send(Calendar timeStamp, AuditMessage msg)
                throws IncompatibleConnectionException, GeneralSecurityException {
            reset();
            try {
                writeHeader(severityOf(msg), timeStamp);
                AuditMessages.toXML(msg, this, formatXML, encoding, schemaURI);
            } catch (IOException e) {
                throw (AssertionError) new AssertionError("Unexpected exception: " + e).initCause(e);
            }
            try {
                connect();
                sendMessage();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

        private void writeInt(int i) {
            if (i >= 100)
                writeNNN(i);
            else if (i >= 10)
                writeNN(i);
            else
                writeN(i);
        }

        private void write(Calendar timeStamp) {
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

    private class UDPConnection extends ActiveConnection {
        DatagramSocket ds;
        UDPConnection(Connection conn, Connection remoteConn) {
            super(conn, remoteConn);
        }

        @Override
        void connect() throws IOException {
            if (ds == null)
                this.ds = conn.createDatagramSocket();
        }

        @Override
        void sendMessage() throws IOException {
            ds.send(new DatagramPacket(buf, count, remoteConn.getEndPoint()));
        }

        @Override
        public void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }
    }

    private class TCPConnection extends ActiveConnection  {
        Socket sock;
        OutputStream out;
        TCPConnection(Connection conn, Connection remoteConn) {
            super(conn, remoteConn);
        }

        @Override
        void connect() throws IOException,
            IncompatibleConnectionException, GeneralSecurityException {
            if (sock == null) {
                sock = conn.connect(remoteConn);
                out = sock.getOutputStream();
            }
        }

        @Override
        void sendMessage() throws IOException {
            try {
                out.write(Integer.toString(count).getBytes(encoding));
                out.write(' ');
                out.write(buf, 0, count);
                out.flush();
            } catch (IOException e) {
                close();
                throw e;
            }
        }


        @Override
        public void close() {
            SafeClose.close(out);
            SafeClose.close(sock);
            sock = null;
            out = null;
        }
    }

}
