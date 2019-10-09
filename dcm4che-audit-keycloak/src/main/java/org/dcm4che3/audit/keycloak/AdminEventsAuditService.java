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
        try {
            Path file = createFile(auditLogger, adminEvent);
            spoolAndAudit(file, auditLogger, adminEvent, keycloakSession);
        } catch (Exception e) {
            LOG.warn("Failed to spool and audit admin event {}: {} ",
                    adminEvent.getOperationType().name() + " " + adminEvent.getResourceType().name(), e);
        }
    }

    private static Path createFile(AuditLogger auditLogger, AdminEvent adminEvent) throws Exception {
        String dataDir = System.getProperty(JBOSS_SERVER_DATA_DIR);
        Path dir = Paths.get(dataDir,
                "audit-auth-spool",
                auditLogger.getCommonName().replaceAll(" ", "_"));

        if (!Files.exists(dir))
            Files.createDirectories(dir);

        AuthDetails authDetails = adminEvent.getAuthDetails();
        return Files.createTempFile(dir, authDetails.getIpAddress() + "-" + authDetails.getUserId(), null);
    }

    private static void spoolAndAudit(Path file, AuditLogger auditLogger, AdminEvent adminEvent,
                                      KeycloakSession keycloakSession) throws IOException {
        try (SpoolFileWriter writer = new SpoolFileWriter(
                Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.APPEND))) {
            writer.writeLine(new AuthInfo(adminEvent, keycloakSession));
        }

        try {
            AuditUtils.emitAudit(auditLogger, createAuditMsg(file, adminEvent, auditLogger));
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
        EventIdentificationBuilder eventIdentification = AuditUtils.eventID(
                eventType, auditLogger, adminEvent.getError(), info.getField(AuthInfo.EVENT));

        ActiveParticipantBuilder[] activeParticipants = AuditUtils.activeParticipants(info, auditLogger);

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
}
