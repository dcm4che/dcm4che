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

import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
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
 * @since Mar 2016
 */
class AuditAuth {
    private static final Logger LOG = LoggerFactory.getLogger(AuditAuth.class);
    private static final String JBOSS_SERVER_DATA_DIR = "jboss.server.data.dir";

    static void spoolAuditMsg(Event event, AuditLogger log, KeycloakSession keycloakSession) {
        String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIR);
        Path dir = Paths.get(dataDir, "audit-auth-spool", log.getCommonName().replaceAll(" ", "_"));
        try {
            if (!Files.exists(dir))
                Files.createDirectories(dir);
            if (AuditUtils.AuditEventType.isLogout(event)
                    && Files.exists(dir.resolve(event.getSessionId()))) {
                sendAuditMessage(dir.resolve(event.getSessionId()), event, log, keycloakSession);
                return;
            }
            spoolAndAudit(dir, log, event, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Failed to spool and audit user auth event {}: {}", event.getType().name(), e);
        }
    }

    private static void spoolAndAudit(Path dir, AuditLogger log, Event event, KeycloakSession keycloakSession)
            throws IOException {
        Path file = event.getSessionId() != null && !Files.exists(dir.resolve(event.getSessionId()))
                    ? Files.createFile(dir.resolve(event.getSessionId()))
                    : Files.createTempFile(dir, event.getIpAddress() + "-" + event.getUserId(), null);
        try (SpoolFileWriter writer = new SpoolFileWriter(
                Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
            writer.writeLine(new AuthInfo(event, keycloakSession));
        }
        sendAuditMessage(file, event, log, keycloakSession);
    }

    private static void sendAuditMessage(Path file, Event event, AuditLogger auditLogger, KeycloakSession keycloakSession) {
        try {
            AuthInfo info = new AuthInfo(new SpoolFileReader(file).getMainInfo());
            ActiveParticipantBuilder[] activeParticipants = AuditUtils.activeParticipants(info, auditLogger);

            AuditUtils.AuditEventType eventType = AuditUtils.AuditEventType.forEvent(event);
            AuditUtils.emitAudit(auditLogger,
                    AuditMessages.createMessage(
                            AuditUtils.eventID(eventType, auditLogger, event.getError(), null),
                            activeParticipants));

            superUserAudit(event, auditLogger, keycloakSession, eventType, activeParticipants);
            if (event.getType() != EventType.LOGIN)
                Files.delete(file);
        } catch (Exception e) {
            AuditUtils.moveFailedFile(file, auditLogger, e);
        }
    }

    private static void superUserAudit(Event event, AuditLogger auditLogger, KeycloakSession keycloakSession,
                                       AuditUtils.AuditEventType eventType, ActiveParticipantBuilder[] activeParticipants) {
        if (event.getUserId() == null
                || eventType == AuditUtils.AuditEventType.UPDT_USER
                || !isSuperUser(event, keycloakSession))
            return;

        AuditUtils.emitAudit(auditLogger,
                AuditMessages.createMessage(
                        AuditUtils.eventID(AuditUtils.AuditEventType.forSuperUserAuth(event),
                                auditLogger,
                                event.getError(),
                                null),
                        activeParticipants));
    }

    private static boolean isSuperUser(Event event, KeycloakSession keycloakSession) {
        String suRole = System.getProperty("super-user-role");
        for (RoleModel roleMapping : keycloakSession.users()
                .getUserById(event.getUserId(), keycloakSession.getContext().getRealm())
                .getRoleMappings())
            if (roleMapping.getName().equals(suRole))
                return true;

        return false;
    }
}
