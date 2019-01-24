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
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.models.KeycloakSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Oct 2018
 */
class AdminEventsAuditService {
    private static final Logger LOG = LoggerFactory.getLogger(AdminEventsAuditService.class);
    private static final String JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";

    static void spoolAuditMsg(AdminEvent adminEvent, AuditLogger auditLogger, KeycloakSession keycloakSession) {
        String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIR);
        Path dir = Paths.get(dataDir, "audit-auth-spool", auditLogger.getCommonName().replaceAll(" ", "_"));
        try {
            if (!Files.exists(dir))
                Files.createDirectories(dir);

            spoolAndAudit(dir, auditLogger, adminEvent, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Failed to spool and audit admin event {}: {} ",
                    adminEvent.getOperationType().name() + " " + adminEvent.getResourceType().name(), e);
        }
    }

    private static void spoolAndAudit(Path dir, AuditLogger auditLogger, AdminEvent adminEvent,
                                      KeycloakSession keycloakSession) throws IOException {
        AuthDetails authDetails = adminEvent.getAuthDetails();
        Path file = Files.createTempFile(dir, authDetails.getIpAddress() + "-" + authDetails.getUserId(), null);
        try {
            try (SpoolFileWriter writer = new SpoolFileWriter(
                    Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
                writer.writeLine(new AuthInfo(adminEvent, keycloakSession));
            }
            emitAudit(auditLogger, createAuditMsg(file, adminEvent, auditLogger));
            Files.delete(file);
        } catch (Exception e) {
            LOG.warn("Failed to process Audit Spool File {} of Audit Logger {} : {}",
                    file, auditLogger.getCommonName(), e);
            try {
                Files.move(file, file.resolveSibling(file.getFileName().toString() + ".failed"));
            } catch (IOException e1) {
                LOG.warn("Failed to mark Audit Spool File {} of Audit Logger {} as failed : {}",
                        file, auditLogger.getCommonName(), e);
            }
        }
    }

    private static AuditMessage createAuditMsg(Path file, AdminEvent adminEvent, AuditLogger auditLogger) {
        AuthInfo info = new AuthInfo(new SpoolFileReader(file).getMainInfo());
        AuditUtils.AuditEventType eventType = AuditUtils.AuditEventType.forAdminEvent(adminEvent);
        EventIdentificationBuilder eventIdentification = new EventIdentificationBuilder.Builder(
                eventType.eventID,
                AuditMessages.EventActionCode.Execute,
                auditLogger.timeStamp(),
                eventOutcomeIndicator(adminEvent.getError()))
                .outcomeDesc(info.getField(AuthInfo.EVENT))
                .eventTypeCode(eventType.eventTypeCode)
                .build();

        ActiveParticipantBuilder[] activeParticipants = new ActiveParticipantBuilder[2];
        String userName = info.getField(AuthInfo.USER_NAME);
        activeParticipants[0] = new ActiveParticipantBuilder.Builder(
                userName,
                info.getField(AuthInfo.IP_ADDR))
                .userIDTypeCode(AuditMessages.UserIDTypeCode.PersonID)
                .isRequester().build();
        activeParticipants[1] = new ActiveParticipantBuilder.Builder(
                auditLogger.getDevice().getDeviceName(),
                auditLogger.getConnections().get(0).getHostname())
                .userIDTypeCode(AuditMessages.UserIDTypeCode.DeviceName)
                .altUserID(AuditLogger.processID()).build();

        ParticipantObjectIdentificationBuilder poi = new ParticipantObjectIdentificationBuilder.Builder(
                auditLogger.getDevice().getDeviceName(),
                AuditMessages.ParticipantObjectIDTypeCode.DeviceName,
                AuditMessages.ParticipantObjectTypeCode.SystemObject,
                null)
                .detail(AuditMessages.createParticipantObjectDetail(
                        "Alert Description",
                        "Representation: " + info.getField(AuthInfo.REPRESENTATION)
                                + "\nResourcePath: " + info.getField(AuthInfo.RESOURCE_PATH)))
                .build();
        return AuditMessages.createMessage(eventIdentification, activeParticipants, poi);
    }

    private static String eventOutcomeIndicator(String outcomeDesc) {
        return outcomeDesc != null
                ? AuditMessages.EventOutcomeIndicator.MinorFailure
                : AuditMessages.EventOutcomeIndicator.Success;
    }

    private static void emitAudit(AuditLogger auditLogger, AuditMessage auditMsg) {
        auditMsg.getAuditSourceIdentification().add(auditLogger.createAuditSourceIdentification());
        try {
            AuditLogger.SendStatus write = auditLogger.write(auditLogger.timeStamp(), auditMsg);
            System.out.println("log send status: " + write);
        } catch (Exception e) {
            LOG.warn("Failed to emit audit message", e);
        }
    }

    static class AuthInfo {
        private static final int USER_NAME = 0;
        private static final int IP_ADDR = 1;
        private static final int EVENT = 2;
        private static final int RESOURCE_PATH = 3;
        private static final int REPRESENTATION = 4;
        private  final String[] fields;

        AuthInfo (AdminEvent adminEvent, KeycloakSession keycloakSession) {
            AuthDetails authDetails = adminEvent.getAuthDetails();
            fields = new String[] {
                    keycloakSession.users().getUserById(authDetails.getUserId(), keycloakSession.getContext().getRealm())
                            .getUsername(),
                    authDetails.getIpAddress(),
                    adminEvent.getOperationType().name() + " " + adminEvent.getResourceType().name(),
                    adminEvent.getResourcePath(),
                    adminEvent.getRepresentation()
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
