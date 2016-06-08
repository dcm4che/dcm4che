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

import org.dcm4che3.audit.*;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.util.StringUtils;

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
import java.util.HashSet;


/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Mar 2016
 */
public class AuditAuth {
    private static final Logger LOG = LoggerFactory.getLogger(AuditAuth.class);
    private static final String JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";

    static void spoolAuditMsg(Event event, AuditLogger log) {
        String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIR);
        Path dir = Paths.get(dataDir, "audit-auth-spool");
        Path file;
        try {
            if (!Files.exists(dir))
                Files.createDirectories(dir);
            if ((event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGOUT_ERROR)
                    && Files.exists(dir.resolve(event.getSessionId()))) {
                sendAuditMessage(dir.resolve(event.getSessionId()), event, log);
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
            sendAuditMessage(file, event, log);
        } catch (Exception e) {
            LOG.warn("Failed to write to Audit Spool File - {} ", e);
        }
    }

    private static EventIdentification getEI(AuditLogger log, Event event) {
        String outcome = event.getError() != null
                ? AuditMessages.EventOutcomeIndicator.MinorFailure : AuditMessages.EventOutcomeIndicator.Success;
        EventTypeCode etc = event.getType().equals(EventType.LOGIN) || event.getType().equals(EventType.LOGIN_ERROR)
                ? AuditMessages.EventTypeCode.Login : AuditMessages.EventTypeCode.Logout;
        BuildEventIdentification ei = new BuildEventIdentification.Builder(AuditMessages.EventID.UserAuthentication,
                AuditMessages.EventActionCode.Execute, log.timeStamp(), outcome).outcomeDesc(event.getError())
                .eventTypeCode(etc).build();
        return AuditMessages.getEI(ei);
    }

    private static void sendAuditMessage(Path file, Event event, AuditLogger log) throws IOException{
        AuthInfo info = new AuthInfo(new LineReader(file).getMainInfo());
        BuildActiveParticipant ap1 = new BuildActiveParticipant.Builder(
                info.getField(AuthInfo.USER_NAME), info.getField(AuthInfo.IP_ADDR)).requester(true).build();
        BuildActiveParticipant ap2 = new BuildActiveParticipant.Builder(
                AuditMessages.getAET(log.getDevice().getApplicationAETitles().toArray(
                    new String[log.getDevice().getApplicationAETitles().size()])),
                log.getConnections().get(0).getHostname()).altUserID(log.processID()).requester(false).build();
        AuditMessage msg = AuditMessages.createMessage(getEI(log, event), AuditMessages.getApList(ap1, ap2), null);
        msg.getAuditSourceIdentification().add(log.createAuditSourceIdentification());
        try {
            log.write(log.timeStamp(), msg);
        } catch (Exception e) {
            LOG.warn("Failed to emit audit message", e);
        }
        if (event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGIN_ERROR)
            Files.delete(file);
    }

    static class LineWriter implements Closeable {
        private final BufferedWriter writer;

        LineWriter(BufferedWriter writer) {
            this.writer = writer;
        }

        void writeLine(Object o) throws IOException {
            writer.write(o.toString().replace('\r', '.').replace('\n', '.'));
            writer.newLine();
        }
        @Override
        public void close() throws IOException {
            writer.close();
        }
    }

    static class LineReader {
        private static final Logger LOG = LoggerFactory.getLogger(LineReader.class);
        private String mainInfo;

        LineReader(Path p) throws IOException {
            try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                this.mainInfo = reader.readLine();
                reader.close();
            } catch (Exception e) {
                LOG.warn("Failed to read audit spool file", e);
            }
        }
        String getMainInfo() {
            return mainInfo;
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
