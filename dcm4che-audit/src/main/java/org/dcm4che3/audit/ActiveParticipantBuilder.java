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

package org.dcm4che3.audit;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since June 2016
 */

public class ActiveParticipantBuilder {
    private final String userID;
    private AuditMessages.UserIDTypeCode userIDTypeCode;
    private String userTypeCode;
    private final String napID;
    private String napTypeCode;
    private String altUserID;
    private String userName;
    private boolean requester;
    private AuditMessages.MediaType mediaType;
    private AuditMessages.RoleIDCode[] roleIDCode = {};

    public ActiveParticipantBuilder(String userID, String napID) {
        this.userID = userID;
        this.napID = napID;
        this.napTypeCode = napID != null
                            ? AuditMessages.isIP(napID)
                                ? AuditMessages.NetworkAccessPointTypeCode.IPAddress
                                : AuditMessages.NetworkAccessPointTypeCode.MachineName
                            : null;
    }

    public ActiveParticipantBuilder userIDTypeCode(AuditMessages.UserIDTypeCode val) {
        userIDTypeCode = val;
        userTypeCode = userTypeCode(val);
        return this;
    }

    public ActiveParticipantBuilder altUserID(String val) {
        altUserID = val;
        return this;
    }

    public ActiveParticipantBuilder userName(String val) {
        userName = val;
        return this;
    }

    public ActiveParticipantBuilder isRequester() {
      requester = true;
      return this;
    }

    public ActiveParticipantBuilder mediaType(AuditMessages.MediaType val) {
        mediaType = val;
        return this;
    }

    public ActiveParticipantBuilder roleIDCode(AuditMessages.RoleIDCode... val) {
        roleIDCode = val;
        return this;
    }

    public ActiveParticipant build() {
        ActiveParticipant ap = new ActiveParticipant();
        ap.setUserID(userID);
        ap.setUserIDTypeCode(userIDTypeCode);
        ap.setUserTypeCode(userTypeCode);
        ap.setAlternativeUserID(altUserID);
        ap.setUserName(userName);
        ap.setUserIsRequestor(requester);
        ap.setNetworkAccessPointID(napID);
        ap.setNetworkAccessPointTypeCode(napTypeCode);
        ap.setMediaType(mediaType);
        for (AuditMessages.RoleIDCode roleID : roleIDCode)
            ap.getRoleIDCode().add(roleID);
        return ap;
    }

    private static String userTypeCode(AuditMessages.UserIDTypeCode userIDTypeCode) {
        return userIDTypeCode == AuditMessages.UserIDTypeCode.NodeID || userIDTypeCode == AuditMessages.UserIDTypeCode.PersonID
                ? AuditMessages.UserTypeCode.Person
                : AuditMessages.UserTypeCode.Application;
    }
}

