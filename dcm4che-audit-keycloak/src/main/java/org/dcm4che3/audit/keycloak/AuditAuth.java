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

import org.dcm4che3.audit.ActiveParticipant;
import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.EventIdentification;
import org.dcm4che3.net.audit.AuditLogger;
import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;


/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Mar 2016
 */
class AuditAuth {

    static void audit(Event event, AuditLogger auditLogger, KeycloakSession session, String suRole) {
        String userId = event.getUserId();
        UserModel userModel = session.users().getUserById(session.getContext().getRealm(), userId);
        String username =  event.getDetails() == null
                            ? userId
                            : event.getDetails().get("username") == null
                                ? userModel.getUsername()
                                : event.getDetails().get("username");

        AuditMessage auditMsg = createAuditMsg(event, username, auditLogger);
        AuditUtils.emitAudit(auditLogger, auditMsg);

        if (!oneOfUserAuthEvents(event)
                || username == null
                || userModel.getRoleMappingsStream().noneMatch(roleModel -> roleModel.getName().equals(suRole)))
            return;

        AuditUtils.AuditEventType eventType = AuditUtils.AuditEventType.forSuperUserAuth(event);
        auditMsg.setEventIdentification(eventIdentification(eventType, event, auditLogger));
        AuditUtils.emitAudit(auditLogger, auditMsg);
    }

    private static AuditMessage createAuditMsg(Event event, String username, AuditLogger auditLogger) {
        AuditUtils.AuditEventType eventType = AuditUtils.AuditEventType.forEvent(event);
        AuditMessage msg = new AuditMessage();
        msg.setEventIdentification(eventIdentification(eventType, event, auditLogger));
        msg.getActiveParticipant().add(user(event, username));
        msg.getActiveParticipant().add(device(auditLogger));
        return msg;
    }

    private static EventIdentification eventIdentification(
            AuditUtils.AuditEventType eventType, Event event, AuditLogger auditLogger) {
        EventIdentification eventIdentification = new EventIdentification();
        eventIdentification.setEventID(eventType.eventID);
        eventIdentification.setEventActionCode(eventType.eventActionCode);
        eventIdentification.setEventDateTime(auditLogger.timeStamp());
        eventIdentification.getEventTypeCode().add(eventType.eventTypeCode);
        eventIdentification.setEventOutcomeIndicator(AuditUtils.eventOutcomeIndicator(event.getError()));
        eventIdentification.setEventOutcomeDescription(event.getError());
        return eventIdentification;
    }

    private static ActiveParticipant user(Event event, String username) {
        ActiveParticipant user = new ActiveParticipant();
        user.setUserID(username);
        user.setNetworkAccessPointID(event.getIpAddress());
        user.setUserIDTypeCode(AuditMessages.UserIDTypeCode.PersonID);
        user.setUserIsRequestor(true);
        return user;
    }

    private static ActiveParticipant device(AuditLogger auditLogger) {
        ActiveParticipant device = new ActiveParticipant();
        device.setUserID(auditLogger.getDevice().getDeviceName());
        device.setAlternativeUserID(AuditLogger.processID());
        device.setNetworkAccessPointID(auditLogger.getConnections().get(0).getHostname());
        device.setUserIDTypeCode(AuditMessages.UserIDTypeCode.DeviceName);
        return device;
    }

    static boolean oneOfUserAuthEvents(Event event) {
        switch (event.getType()) {
            case LOGIN:
            case LOGIN_ERROR:
            case LOGOUT:
            case LOGOUT_ERROR:
                return true;
        }
        return false;
    }
}
