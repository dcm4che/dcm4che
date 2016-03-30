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
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditLogger;
import org.keycloak.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Mar 2016
 */
public class AuditAuth {
    private static final Logger LOG = LoggerFactory.getLogger(AuditAuth.class);


    static void emitAuditMsg(Event event) {
        AuditLoggerFactory af = new AuditLoggerFactory();
        try {
            AuditLogger log = af.getAuditLogger();
            String userId = event.getUserId();
            AuditMessage msg = new AuditMessage();
            msg.setEventIdentification(AuditMessages.createEventIdentification(
                    AuditMessages.EventID.UserAuthentication, AuditMessages.EventActionCode.Execute,
                    log.timeStamp(), AuditMessages.EventOutcomeIndicator.Success, null));
            msg.getActiveParticipant().add(AuditMessages.createActiveParticipant(
                    buildAET(log.getDevice()), log.processID(), null, false,
                    log.getConnections().get(0).getHostname(), AuditMessages.NetworkAccessPointTypeCode.IPAddress, null));
            try {
                log.write(log.timeStamp(), msg);
            } catch (Exception e) {
                LOG.warn("Failed to emit audit message", e);
            }
        } catch (Exception e) {
            LOG.warn("Failed to get audit logger", e);
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

}
