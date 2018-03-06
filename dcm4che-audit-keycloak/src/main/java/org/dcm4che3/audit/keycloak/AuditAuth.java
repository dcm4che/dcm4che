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
package org.dcm4che3.audit.keycloak;

import org.dcm4che3.audit.*;
import org.dcm4che3.net.audit.AuditLogger;
import org.dcm4che3.util.StringUtils;

import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
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
import java.util.ArrayList;
import java.util.List;


/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Mar 2016
 */
public class AuditAuth {
    private static final Logger LOG = LoggerFactory.getLogger(AuditAuth.class);
    private static final String JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";

    static void spoolAuditMsg(Event event, AuditLogger log, KeycloakSession keycloakSession) {
        String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIR);
        Path dir = Paths.get(dataDir, "audit-auth-spool", log.getCommonName().replaceAll(" ", "_"));
        try {
            if (!Files.exists(dir))
                Files.createDirectories(dir);
            if (isLogout(event) && Files.exists(dir.resolve(event.getSessionId()))) {
                sendAuditMessage(dir.resolve(event.getSessionId()), event, log, keycloakSession);
                return;
            }
            auditLogin(dir, log, event, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Failed to write to Audit Spool File - {} ", e);
        }
    }

    private static void auditLogin(Path dir, AuditLogger log, Event event, KeycloakSession keycloakSession) {
        try {
            Path file = event.getType() == EventType.LOGIN_ERROR
                    ? Files.createTempFile(dir, event.getIpAddress(), null)
                    : event.getType() == EventType.LOGIN && !Files.exists(dir.resolve(event.getSessionId()))
                        ? Files.createFile(dir.resolve(event.getSessionId()))
                        : null;

            if (file == null)
                return;

            try (LineWriter writer = new LineWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
                writer.writeLine(new AuthInfo(event));
            }
            sendAuditMessage(file, event, log, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Audit Login Exception: {}" + e);
        }
    }

    private static boolean isLogout(Event event) {
        return event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGOUT_ERROR;
    }

    private static EventIdentificationBuilder userAuthenticateEventIDBuilder(AuditLogger log, Event event) {
        String outcome = event.getError();
        return new EventIdentificationBuilder.Builder(
                AuditMessages.EventID.UserAuthentication,
                AuditMessages.EventActionCode.Execute,
                log.timeStamp(),
                outcome != null ? AuditMessages.EventOutcomeIndicator.MinorFailure : AuditMessages.EventOutcomeIndicator.Success)
                .outcomeDesc(outcome)
                .eventTypeCode(isLogout(event) ? AuditMessages.EventTypeCode.Logout : AuditMessages.EventTypeCode.Login)
                .build();
    }

    private static EventIdentificationBuilder securityAlertEventIDBuilder(AuditLogger log, Event event) {
        String outcome = event.getError();
        return new EventIdentificationBuilder.Builder(
                AuditMessages.EventID.SecurityAlert,
                AuditMessages.EventActionCode.Execute,
                log.timeStamp(),
                outcome != null ? AuditMessages.EventOutcomeIndicator.MinorFailure : AuditMessages.EventOutcomeIndicator.Success)
                .outcomeDesc(outcome)
                .eventTypeCode(isLogout(event)
                        ? AuditMessages.EventTypeCode.EmergencyOverrideStopped : AuditMessages.EventTypeCode.EmergencyOverrideStarted)
                .build();
    }

    private static void sendAuditMessage(Path file, Event event, AuditLogger log, KeycloakSession keycloakSession)
            throws IOException{
        AuthInfo info = new AuthInfo(new LineReader(file).getMainInfo());

        ActiveParticipantBuilder[] activeParticipants = new ActiveParticipantBuilder[2];
        String userName = info.getField(AuthInfo.USER_NAME);
        activeParticipants[0] = new ActiveParticipantBuilder.Builder(
                userName,
                info.getField(AuthInfo.IP_ADDR))
                .userIDTypeCode(AuditMessages.UserIDTypeCode.PersonID)
                .requester(true).build();
        activeParticipants[1] = new ActiveParticipantBuilder.Builder(
                log.getDevice().getDeviceName(),
                log.getConnections().get(0).getHostname())
                .userIDTypeCode(AuditMessages.UserIDTypeCode.DeviceName)
                .altUserID(AuditLogger.processID()).build();

        AuditMessage msgUserAuthenticate = AuditMessages.createMessage(userAuthenticateEventIDBuilder(log, event), activeParticipants);
        msgUserAuthenticate.getAuditSourceIdentification().add(log.createAuditSourceIdentification());
        emitAudit(log, msgUserAuthenticate);

        if (userRoles(userName, keycloakSession).contains(System.getProperty("super-user-role"))) {
            AuditMessage msgSecurityAlert = AuditMessages.createMessage(securityAlertEventIDBuilder(log, event), activeParticipants);
            msgSecurityAlert.getAuditSourceIdentification().add(log.createAuditSourceIdentification());
            emitAudit(log, msgSecurityAlert);
        }

        if (event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGIN_ERROR)
            Files.delete(file);
    }

    private static List<String> userRoles(String userName, KeycloakSession keycloakSession) {
        List<String> userRoles = new ArrayList<>();
        for (RoleModel roleMapping : keycloakSession.users()
                                      .getUserByUsername(userName, keycloakSession.getContext().getRealm())
                                      .getRoleMappings())
            userRoles.add(roleMapping.getName());
        return userRoles;
    }

    private static void emitAudit(AuditLogger log, AuditMessage msg) {
        try {
            AuditLogger.SendStatus write = log.write(log.timeStamp(), msg);
            System.out.println("log send status: " + write);
        } catch (Exception e) {
            LOG.warn("Failed to emit audit message", e);
        }
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
