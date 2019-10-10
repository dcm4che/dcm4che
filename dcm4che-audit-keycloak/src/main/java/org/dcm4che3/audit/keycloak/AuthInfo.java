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

import org.dcm4che3.util.StringUtils;
import org.keycloak.events.Event;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.AuthDetails;
import org.keycloak.models.KeycloakSession;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Oct 2019
 */
class AuthInfo {
    static final int USER_NAME = 0;
    static final int IP_ADDR = 1;
    static final int EVENT = 2;
    static final int RESOURCE_PATH = 3;
    static final int REPRESENTATION = 4;
    private final String[] fields;

    AuthInfo (Event event, KeycloakSession keycloakSession) {
        fields = new String[] {
                event.getDetails() != null
                        ? event.getDetails().get("username")
                        : event.getUserId() != null
                            ? keycloakSession.users().getUserById(event.getUserId(), keycloakSession.getContext().getRealm())
                                .getUsername()
                            : event.getIpAddress(),
                event.getIpAddress()
        };
    }

    AuthInfo (AdminEvent adminEvent, KeycloakSession keycloakSession) {
        AuthDetails authDetails = adminEvent.getAuthDetails();
        fields = new String[] {
                keycloakSession.users().getUserById(authDetails.getUserId(), keycloakSession.getContext().getRealm())
                        .getUsername(),
                authDetails.getIpAddress(),
                adminEvent.getOperationType() + " " + adminEvent.getResourceType(),
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
