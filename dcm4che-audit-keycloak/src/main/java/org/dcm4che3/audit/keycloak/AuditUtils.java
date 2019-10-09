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
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Oct 2018
 */
class AuditUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AuditUtils.class);

    enum AuditEventType {
        LOGIN(AuditMessages.EventID.UserAuthentication, AuditMessages.EventTypeCode.Login),
        LOGOUT(AuditMessages.EventID.UserAuthentication, AuditMessages.EventTypeCode.Logout),

        SU_LOGIN(AuditMessages.EventID.SecurityAlert, AuditMessages.EventTypeCode.EmergencyOverrideStarted),
        SU_LOGOUT(AuditMessages.EventID.SecurityAlert, AuditMessages.EventTypeCode.EmergencyOverrideStopped),
        UPDT_USER(AuditMessages.EventID.SecurityAlert, AuditMessages.EventTypeCode.UserSecurityAttributesChanged),
        ADMIN_EVT(AuditMessages.EventID.SecurityAlert, AuditMessages.EventTypeCode.SecurityConfiguration),
        ROLE_MAPPING(AuditMessages.EventID.SecurityAlert, AuditMessages.EventTypeCode.SecurityRolesChanged);
        
        final AuditMessages.EventID eventID;
        final EventTypeCode eventTypeCode;

        AuditEventType(AuditMessages.EventID eventID, EventTypeCode etc) {
            this.eventID = eventID;
            this.eventTypeCode = etc;
        }

        static AuditEventType forSuperUserAuth(Event event) {
            return isLogout(event) ? SU_LOGOUT : SU_LOGIN;
        }

        static AuditEventType forEvent(Event event) {
            return event.getType().name().startsWith("UPDATE_PASSWORD")
                    ? UPDT_USER
                    : isLogout(event) ? LOGOUT : LOGIN;
        }

        static AuditEventType forAdminEvent(AdminEvent adminEvent) {
            OperationType opType = adminEvent.getOperationType();
            ResourceType resourceType = adminEvent.getResourceType();

            return opType == OperationType.CREATE
                    && (resourceType == ResourceType.REALM_ROLE_MAPPING
                        || resourceType == ResourceType.CLIENT_ROLE_MAPPING)
                        ? ROLE_MAPPING
                        : opType == OperationType.UPDATE && resourceType == ResourceType.USER
                            ? UPDT_USER
                            : ADMIN_EVT;
        }

        static boolean isLogout(Event event) {
            return event.getType() == EventType.LOGOUT || event.getType() == EventType.LOGOUT_ERROR;
        }
    }

    static void emitAudit(AuditLogger auditLogger, AuditMessage auditMsg) {
        auditMsg.getAuditSourceIdentification().add(auditLogger.createAuditSourceIdentification());
        try {
            AuditLogger.SendStatus write = auditLogger.write(auditLogger.timeStamp(), auditMsg);
            System.out.println("log send status: " + write);
        } catch (Exception e) {
            LOG.warn("Failed to emit audit message", e);
        }
    }

    static void moveFailedFile(Path file, AuditLogger log, Exception e) {
        LOG.warn("Failed to process Audit Spool File {} of Audit Logger {} : {}",
                file, log.getCommonName(), e);
        try {
            Files.move(file, file.resolveSibling(file.getFileName().toString() + ".failed"));
        } catch (IOException e1) {
            LOG.warn("Failed to mark Audit Spool File {} of Audit Logger {} as failed : {}",
                    file, log.getCommonName(), e);
        }
    }

    static EventIdentificationBuilder eventID(
            AuditUtils.AuditEventType eventType, AuditLogger auditLogger, String outcome, String adminEventInfo) {
        return new EventIdentificationBuilder.Builder(
                eventType.eventID,
                AuditMessages.EventActionCode.Execute,
                auditLogger.timeStamp(),
                eventOutcomeIndicator(outcome))
                .outcomeDesc(adminEventInfo != null ? adminEventInfo + " " + outcome : outcome)
                .eventTypeCode(eventType.eventTypeCode)
                .build();
    }

    private static String eventOutcomeIndicator(String outcomeDesc) {
        return outcomeDesc != null
                ? AuditMessages.EventOutcomeIndicator.MinorFailure
                : AuditMessages.EventOutcomeIndicator.Success;
    }

    static ActiveParticipantBuilder[] activeParticipants(AuthInfo info, AuditLogger auditLogger) {
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
        return activeParticipants;
    }
}
