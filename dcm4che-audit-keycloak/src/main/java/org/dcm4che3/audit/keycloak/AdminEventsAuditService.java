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
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Oct 2018
 */
class AdminEventsAuditService {
    private static final String REPRESENTATION = "Representation: ";
    private static final String RESOURCE_PATH = "ResourcePath: ";
    private static final String REALM = "Realm: ";

    static void audit(AdminEvent adminEvent, AuditLogger auditLogger, KeycloakSession session) {
        AuditUtils.AuditEventType eventType = AuditUtils.AuditEventType.forAdminEvent(adminEvent);
        AuditMessage auditMsg = new AuditMessage();
        auditMsg.setEventIdentification(eventIdentification(eventType, adminEvent, auditLogger));
        auditMsg.getActiveParticipant().add(user(adminEvent, session));
        auditMsg.getActiveParticipant().add(device(auditLogger));
        auditMsg.getParticipantObjectIdentification().add(participant(adminEvent, session, auditLogger));
        AuditUtils.emitAudit(auditLogger, auditMsg);
    }

    private static EventIdentification eventIdentification(
            AuditUtils.AuditEventType eventType, AdminEvent adminEvent, AuditLogger auditLogger) {
        EventIdentification eventIdentification = new EventIdentification();
        eventIdentification.setEventID(eventType.eventID);
        eventIdentification.setEventActionCode(eventType.eventActionCode);
        eventIdentification.setEventDateTime(auditLogger.timeStamp());
        eventIdentification.getEventTypeCode().add(eventType.eventTypeCode);
        eventIdentification.setEventOutcomeIndicator(AuditUtils.eventOutcomeIndicator(adminEvent.getError()));
        eventIdentification.setEventOutcomeDescription(outcome(adminEvent));
        return eventIdentification;
    }

    private static String outcome(AdminEvent adminEvent) {
        String event = adminEvent.getOperationType().name().concat(" ").concat(adminEvent.getResourceType().name());
        return adminEvent.getError() == null
                ? event
                : event.concat(" ").concat(adminEvent.getError());
    }

    private static ActiveParticipant user(AdminEvent adminEvent, KeycloakSession session) {
        ActiveParticipant user = new ActiveParticipant();
        user.setUserID(username(adminEvent, session));
        user.setNetworkAccessPointID(adminEvent.getAuthDetails().getIpAddress());
        user.setUserIDTypeCode(AuditMessages.UserIDTypeCode.PersonID);
        user.setUserIsRequestor(true);
        return user;
    }

    private static String username(AdminEvent adminEvent, KeycloakSession session) {
        String userId = adminEvent.getAuthDetails().getUserId();
        UserModel userModel = session.users().getUserById(session.getContext().getRealm(), userId);
        return userModel.getUsername() == null
                ? userId
                : userModel.getUsername();
    }

    private static ActiveParticipant device(AuditLogger auditLogger) {
        ActiveParticipant device = new ActiveParticipant();
        device.setUserID(auditLogger.getDevice().getDeviceName());
        device.setAlternativeUserID(AuditLogger.processID());
        device.setNetworkAccessPointID(auditLogger.getConnections().get(0).getHostname());
        device.setUserIDTypeCode(AuditMessages.UserIDTypeCode.DeviceName);
        return device;
    }

    private static ParticipantObjectIdentification participant(
            AdminEvent adminEvent, KeycloakSession session, AuditLogger auditLogger) {
        ParticipantObjectIdentification participant = new ParticipantObjectIdentification();
        participant.setParticipantObjectID(auditLogger.getDevice().getDeviceName());
        participant.setParticipantObjectIDTypeCode(AuditMessages.ParticipantObjectIDTypeCode.DeviceName);
        participant.setParticipantObjectTypeCode(AuditMessages.ParticipantObjectTypeCode.SystemObject);
        participant.getParticipantObjectDetail()
                   .add(AuditMessages.createParticipantObjectDetail("Alert Description", alert(adminEvent, session)));
        return participant;
    }

    private static String alert(AdminEvent adminEvent, KeycloakSession session) {
        StringBuilder alert = new StringBuilder(RESOURCE_PATH);
        alert.append(adminEvent.getResourcePath()).append("\n");
        if (adminEvent.getRepresentation() != null)
            alert.append(REPRESENTATION).append(adminEvent.getRepresentation()).append("\n");
        if (!realmsMatch(adminEvent, session))
            alert.append(REALM).append(adminEvent.getAuthDetails().getRealmId());

        return alert.toString();
    }

    private static boolean realmsMatch(AdminEvent adminEvent, KeycloakSession session) {
        return adminEvent.getAuthDetails().getRealmId().equals(session.getContext().getRealm().getName());
    }
}
