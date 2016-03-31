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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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
package org.dcm4che3.audit.keycloak;

import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.util.StringUtils;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Mar 2016
 */
public class AuditAuth {
    private static final Logger LOG = LoggerFactory.getLogger(AuditAuth.class);
    private static final String JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";

    static void spoolAuditMsg(Event event) {
        String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIR);
        Path dir = Paths.get(dataDir, "audit-auth-spool");
        Path file;
        try {
            if (!Files.exists(dir))
                Files.createDirectories(dir);
            if ((event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGOUT_ERROR)
                    && Files.exists(dir.resolve(event.getSessionId()))) {
                sendAuditMessage(dir.resolve(event.getSessionId()), event);
                return;
            }
            if (event.getType() == EventType.LOGIN_ERROR && event.getError() != null)
                file = Files.createTempFile(dir, event.getIpAddress(), null);
            else {
                if (event.getType() == EventType.LOGIN && Files.exists(dir.resolve(event.getSessionId())))
                    return;
                file = Files.createFile(dir.resolve(event.getSessionId()));
            }
            try (LineWriter writer = new LineWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND))) {
                writer.writeLine(new AuthInfo(event));
            }
            sendAuditMessage(file, event);
        } catch (Exception e) {
            LOG.warn("Failed to write to Audit Spool File - {} ", e);
        }
    }

    private static void sendAuditMessage(Path file, Event event) {
        AuditLoggerFactory af = new AuditLoggerFactory();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            AuthInfo info = new AuthInfo(reader.readLine());
            try {
                AuditLogger log = af.getAuditLogger();
                AuditMessage msg = new AuditMessage();
                msg.setEventIdentification(AuditMessages.createEventIdentification(
                    AuditMessages.EventID.UserAuthentication, AuditMessages.EventActionCode.Execute,
                    log.timeStamp(), event.getError() != null
                        ? AuditMessages.EventOutcomeIndicator.MinorFailure : AuditMessages.EventOutcomeIndicator.Success,
                    event.getError() != null ? event.getError() : null,
                    event.getType().equals(EventType.LOGIN) || event.getType().equals(EventType.LOGIN_ERROR)
                        ? AuditMessages.EventTypeCode.Login : AuditMessages.EventTypeCode.Logout));
                msg.getActiveParticipant().add(AuditMessages.createActiveParticipant(
                    info.getField(AuthInfo.USER_NAME), null, null, true, info.getField(AuthInfo.IP_ADDR),
                    AuditMessages.NetworkAccessPointTypeCode.IPAddress, null));
                msg.getActiveParticipant().add(AuditMessages.createActiveParticipant(
                        buildAET(log.getDevice()), log.processID(), null, false, log.getConnections().get(0).getHostname(),
                        AuditMessages.NetworkAccessPointTypeCode.IPAddress, null));
                try {
                    log.write(log.timeStamp(), msg);
                } catch (Exception e) {
                    LOG.warn("Failed to emit audit message", e);
                }
                if (event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGIN_ERROR)
                    Files.delete(file);
            } catch (Exception e) {
                LOG.warn("Failed to get audit logger", e);
            }
        } catch (Exception e) {
            LOG.warn("Failed to read audit spool file", e);
        }
    }

    static String buildAET(Device device) {
        String[] aets = device.getApplicationAETitles().toArray(new String[device.getApplicationAETitles().size()]);
        StringBuilder b = new StringBuilder();
        b.append(aets[0]);
        for (int i = 1; i < aets.length; i++)
            b.append(';').append(aets[i]);
        return b.toString();
    }

    static class LineWriter implements Closeable {
        private final BufferedWriter writer;

        public LineWriter(BufferedWriter writer) {
            this.writer = writer;
        }

        public void writeLine(Object o) throws IOException {
            writer.write(o.toString().replace('\r', '.').replace('\n', '.'));
            writer.newLine();
        }
        @Override
        public void close() throws IOException {
            writer.close();
        }
    }

    static class AuthInfo {
        private static final int USER_NAME = 0;
        private static final int IP_ADDR = 1;
        private  final String[] fields;

        AuthInfo (Event event) {
            fields = new String[] {
                    event.getDetails().get("username"),
                    event.getIpAddress()
            };
        }
        AuthInfo(String s) {
            fields = StringUtils.split(s, '\\');
        }

        String getField(int field) {
            return StringUtils.maskEmpty(fields[field], null);
        }

        @Override
        public String toString() {
            return StringUtils.concat(fields, '\\');
        }
    }
}
