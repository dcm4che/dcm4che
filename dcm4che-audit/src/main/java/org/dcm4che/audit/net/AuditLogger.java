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

package org.dcm4che.audit.net;

import java.io.ByteArrayOutputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import org.dcm4che.audit.AuditMessage;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class AuditLogger {

    public enum Facility {
        kern,     // (0), -- kernel messages
        user,     // (1), -- user-level messages
        mail,     // (2), -- mail system messages
        daemon,   // (3), -- system daemons' messages
        auth,     // (4), -- authorization messages
        syslog,   // (5), -- messages generated internally by syslogd
        lpr,      // (6), -- line printer subsystem messages
        news,     // (7), -- network news subsystem messages
        uucp,     // (8), -- UUCP subsystem messages
        cron,     // (9), -- clock daemon messages
        authpriv, // (10),-- security/authorization messages
        ftp,      // (11),-- ftp daemon messages
        ntp,      // (12),-- NTP subsystem messages
        audit,    // (13),-- audit messages
        console,  // (14),-- console messages
        cron2,    // (15),-- clock daemon messages
        local0,   // (16),
        local1,   // (17),
        local2,   // (18),
        local3,   // (19),
        local4,   // (20),
        local5,   // (21),
        local6,   // (22),
        local7,   // (23)
    }

    public enum Severity {
        emerg,    // (0), -- emergency; system is unusable
        alert,    // (1), -- action must be taken immediately
        crit,     // (2), -- critical condition
        err,      // (3), -- error condition
        warning,  // (4), -- warning condition
        notice,   // (5), -- normal but significant condition
        info,     // (6), -- informational message
        debug,    // (7) -- debug-level messages
    }

    private static final Hashtable<String, AuditLogger> loggers =
            new Hashtable<String, AuditLogger>();

    private static String processID = processID();
    private static InetAddress localhost = localHost();
    private Facility facility = Facility.authpriv;
    private Severity successSeverity = Severity.notice;
    private Severity minorFailureSeverity = Severity.warning;
    private Severity seriousFailureSeverity = Severity.err;
    private Severity majorFailureSeverity = Severity.crit;
    private String applicationName;
    private String messageID = "DICOM+RFC3881";
    private GregorianCalendar timeStamp;
    private boolean timestampInUTC = false;
    private boolean prefixMessageWithBOM = true;

    private ByteArrayOutputStream buf = new ByteArrayOutputStream(512);

    private static String processID() {
        String s =  ManagementFactory.getRuntimeMXBean().getName();
        int atPos = s.indexOf('@');
        return atPos > 0 ? s.substring(0, atPos)
                : Integer.toString(new Random().nextInt() & 0x7fffffff);
    }

    private static InetAddress localHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static AuditLogger getLogger(String name) {
        AuditLogger logger = loggers.get(name);
        if (logger == null) {
            logger = new AuditLogger();
            loggers.put(name, logger);
        }
        return logger;
    }

    private Severity severityOf(AuditMessage msg) {
        String outcome = msg.getEventIdentification().getEventOutcomeIndicator();
        if (outcome == null || outcome.isEmpty())
            throw new IllegalArgumentException("Missing Event Outcome Indicator");
        if (outcome.length() == 1) {
            switch (outcome.charAt(0)) {
            case '0':
                return successSeverity;
            case '4':
                return minorFailureSeverity;
            case '8':
                return seriousFailureSeverity;
            }
        } else if (outcome.equals("12"))
            return majorFailureSeverity;
        throw new IllegalArgumentException("Invalid Event Outcome Indicator: "
            + outcome);
    }

    public Calendar timeStamp() {
        GregorianCalendar ts = timeStamp;
        if (ts == null) {
            timeStamp = ts = timestampInUTC 
                    ? new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.ENGLISH)
                    : new GregorianCalendar(Locale.ENGLISH);
        } else {
            ts.setTimeInMillis(System.currentTimeMillis());
        }
        return ts;
    }
}
