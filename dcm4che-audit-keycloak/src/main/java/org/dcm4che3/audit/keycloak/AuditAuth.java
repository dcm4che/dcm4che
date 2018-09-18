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
            spoolAndAudit(dir, log, event, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Failed to write to Audit Spool File - {} ", e);
        }
    }

    private static void spoolAndAudit(Path dir, AuditLogger log, Event event, KeycloakSession keycloakSession) {
        try {
            Path file = event.getSessionId() != null && !Files.exists(dir.resolve(event.getSessionId()))
                        ? Files.createFile(dir.resolve(event.getSessionId()))
                        : Files.createTempFile(dir, event.getIpAddress() + "-" + event.getUserId(), null);
            try (LineWriter writer = new LineWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
                writer.writeLine(new AuthInfo(event, keycloakSession));
            }
            sendAuditMessage(file, event, log, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Audit Login Exception: {}" + e);
        }
    }

    private static boolean isLogout(Event event) {
        return event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGOUT_ERROR;
    }

    private static boolean isUpdatePassword(Event event) {
        return event.getType() == EventType.UPDATE_PASSWORD || event.getType() == EventType.UPDATE_PASSWORD_ERROR;
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

        if (isUpdatePassword(event)) {
            emitAudit(log,
                    eventIDBuilder(log, event.getError(), AuditUtils.EventType.UPDT_PSWD),
                    activeParticipants);
        }
        else {
            emitAudit(log,
                    eventIDBuilder(log, event.getError(), AuditUtils.EventType.forUserAuth(event)),
                    activeParticipants);

            if (event.getUserId() != null
                    && userRoles(userName, keycloakSession).contains(System.getProperty("super-user-role")))
                emitAudit(log,
                        eventIDBuilder(log, event.getError(), AuditUtils.EventType.forSuperUserAuth(event)),
                        activeParticipants);
        }

        if (event.getType() != EventType.LOGIN)
            Files.delete(file);
    }

    private static void emitAudit(
            AuditLogger log, EventIdentificationBuilder eventID, ActiveParticipantBuilder[] activeParticipants) {
        AuditMessage msg = AuditMessages.createMessage(eventID, activeParticipants);
        msg.getAuditSourceIdentification().add(log.createAuditSourceIdentification());
        try {
            AuditLogger.SendStatus write = log.write(log.timeStamp(), msg);
            System.out.println("log send status: " + write);
        } catch (Exception e) {
            LOG.warn("Failed to emit audit message", e);
        }
    }

    private static EventIdentificationBuilder eventIDBuilder(
            AuditLogger log, String outcome, AuditUtils.EventType eventType) {
        return new EventIdentificationBuilder.Builder(
                eventType.eventID, eventType.eventActionCode, log.timeStamp(), eventOutcomeIndicator(outcome))
                .outcomeDesc(outcome)
                .eventTypeCode(eventType.eventTypeCode).build();
    }

    private static List<String> userRoles(String userName, KeycloakSession keycloakSession) {
        List<String> userRoles = new ArrayList<>();
        for (RoleModel roleMapping : keycloakSession.users()
                                      .getUserByUsername(userName, keycloakSession.getContext().getRealm())
                                      .getRoleMappings())
            userRoles.add(roleMapping.getName());
        return userRoles;
    }

    private static String eventOutcomeIndicator(String outcomeDesc) {
        return outcomeDesc != null ? AuditMessages.EventOutcomeIndicator.MinorFailure : AuditMessages.EventOutcomeIndicator.Success;
    }

    static class AuditUtils {
        enum EventClass {
            USER_AUTHENTICATE, SECURITY_ALERT
        }

        enum EventType {
            LOGIN(EventClass.USER_AUTHENTICATE, AuditMessages.EventID.UserAuthentication, AuditMessages.EventActionCode.Execute,
                    AuditMessages.EventTypeCode.Login),
            LOGOUT(EventClass.USER_AUTHENTICATE, AuditMessages.EventID.UserAuthentication, AuditMessages.EventActionCode.Execute,
                    AuditMessages.EventTypeCode.Logout),
            SU_LOGIN(EventClass.SECURITY_ALERT, AuditMessages.EventID.SecurityAlert, AuditMessages.EventActionCode.Execute,
                    AuditMessages.EventTypeCode.EmergencyOverrideStarted),
            SU_LOGOUT(EventClass.SECURITY_ALERT, AuditMessages.EventID.SecurityAlert, AuditMessages.EventActionCode.Execute,
                    AuditMessages.EventTypeCode.EmergencyOverrideStopped),
            UPDT_PSWD(EventClass.SECURITY_ALERT, AuditMessages.EventID.SecurityAlert, AuditMessages.EventActionCode.Update,
                    AuditMessages.EventTypeCode.UserSecurityAttributesChanged);

            final EventClass eventClass;
            final AuditMessages.EventID eventID;
            final String eventActionCode;
            final EventTypeCode eventTypeCode;

            EventType(EventClass eventClass, AuditMessages.EventID eventID, String eventActionCode, EventTypeCode etc) {
                this.eventClass = eventClass;
                this.eventID = eventID;
                this.eventActionCode = eventActionCode;
                this.eventTypeCode = etc;
            }
            
            static EventType forUserAuth(Event event) {
                return isLogout(event) ? LOGOUT : LOGIN;
            }

            static EventType forSuperUserAuth(Event event) {
                return isLogout(event) ? SU_LOGOUT : SU_LOGIN;
            }
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

        LineReader(Path p) {
            try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                this.mainInfo = reader.readLine();
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

        AuthInfo (Event event, KeycloakSession keycloakSession) {
            fields = new String[] {
                    event.getDetails() != null
                        ? event.getDetails().get("username")
                        : keycloakSession.users().getUserById(event.getUserId(), keycloakSession.getContext().getRealm())
                            .getUsername(),
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
