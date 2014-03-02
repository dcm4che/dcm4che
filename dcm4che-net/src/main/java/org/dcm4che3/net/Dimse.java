/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License")), you may not use this file except in compliance with
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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.net;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.TagUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public enum Dimse {
    C_STORE_RQ(0x0001, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageID, ":C-STORE-RQ[pcid="),
    C_STORE_RSP(0x8001, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":C-STORE-RSP[pcid="),
    C_GET_RQ(0x0010, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-GET-RQ[pcid="),
    C_GET_RSP(0x8010, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-GET-RSP[pcid="),
    C_FIND_RQ(0x0020, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-FIND-RQ[pcid="),
    C_FIND_RSP(0x8020, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-FIND-RSP[pcid="),
    C_MOVE_RQ(0x0021, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-MOVE-RQ[pcid="),
    C_MOVE_RSP(0x8021, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-MOVE-RSP[pcid="),
    C_ECHO_RQ(0x0030, Tag.AffectedSOPClassUID, 0,
            Tag.MessageID, ":C-ECHO-RQ[pcid="),
    C_ECHO_RSP(0x8030, Tag.AffectedSOPClassUID, 0,
            Tag.MessageIDBeingRespondedTo, ":C-ECHO-RSP[pcid="),
    N_EVENT_REPORT_RQ(0x0100, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageID, ":N-EVENT-REPORT-RQ[pcid="),
    N_EVENT_REPORT_RSP(0x8100, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-EVENT-REPORT-RSP[pcid="),
    N_GET_RQ(0x0110, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-GET-RQ[pcid="),
    N_GET_RSP(0x8110, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-GET-RSP[pcid="),
    N_SET_RQ(0x0120, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-SET-RQ[pcid="),
    N_SET_RSP(0x8120, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-SET-RSP[pcid="),
    N_ACTION_RQ(0x0130, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-ACTION-RQ[pcid="),
    N_ACTION_RSP(0x8130, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-ACTION-RSP[pcid="),
    N_CREATE_RQ(0x0140, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageID, ":N-CREATE-RQ[pcid="),
    N_CREATE_RSP(0x8140, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-CREATE-RSP[pcid="),
    N_DELETE_RQ(0x0150, Tag.RequestedSOPClassUID, Tag.RequestedSOPInstanceUID,
            Tag.MessageID, ":N-DELETE-RQ[pcid="),
    N_DELETE_RSP(0x8150, Tag.AffectedSOPClassUID, Tag.AffectedSOPInstanceUID,
            Tag.MessageIDBeingRespondedTo, ":N-DELETE-RSP[pcid="),
    C_CANCEL_RQ(0x0FFF, 0, 0, Tag.MessageIDBeingRespondedTo, ":C-CANCEL-RQ[pcid=");

    public static final Logger LOG = LoggerFactory.getLogger(Dimse.class);

    private final int commandField;
    private final int tagOfSOPClassUID;
    private final int tagOfSOPInstanceUID;
    private final int tagOfMessageID;
    private final String prompt;

    private Dimse(int cmdField, int tagOfSOPClassUID, int tagOfSOPInstanceUID,
            int tagOfMessageID, String prompt) {
        this.commandField = cmdField;
        this.tagOfSOPClassUID = tagOfSOPClassUID;
        this.tagOfSOPInstanceUID = tagOfSOPInstanceUID;
        this.tagOfMessageID = tagOfMessageID;
        this.prompt = prompt;
    }

    public int commandField() {
        return commandField;
    }

    public int tagOfSOPClassUID() {
        return tagOfSOPClassUID;
    }

    public int tagOfSOPInstanceUID() {
        return tagOfSOPInstanceUID;
    }

    public boolean isRSP() {
        return (commandField & 0x8000) != 0;
    }

    public boolean isRetrieveRQ() {
        return this == C_GET_RQ || this == C_MOVE_RQ;
    }

    public boolean isRetrieveRSP() {
        return this == C_GET_RSP || this == C_MOVE_RSP;
    }

    public boolean isCService() {
        return (commandField & 0x100) == 0;
    }

    public int commandFieldOfRSP() {
        return commandField | 0x8000;
    }

    public static Dimse valueOf(int commandField) {
        switch(commandField) {
        case 0x0001:
            return C_STORE_RQ;
        case 0x8001:
            return C_STORE_RSP;
        case 0x0010:
            return C_GET_RQ;
        case 0x8010:
            return C_GET_RSP;
        case 0x0020:
            return C_FIND_RQ;
        case 0x8020:
            return C_FIND_RSP;
        case 0x0021:
            return C_MOVE_RQ;
        case 0x8021:
            return C_MOVE_RSP;
        case 0x0030:
            return C_ECHO_RQ;
        case 0x8030:
            return C_ECHO_RSP;
        case 0x0100:
            return N_EVENT_REPORT_RQ;
        case 0x8100:
            return N_EVENT_REPORT_RSP;
        case 0x0110:
            return N_GET_RQ;
        case 0x8110:
            return N_GET_RSP;
        case 0x0120:
            return N_SET_RQ;
        case 0x8120:
            return N_SET_RSP;
        case 0x0130:
            return N_ACTION_RQ;
        case 0x8130:
            return N_ACTION_RSP;
        case 0x0140:
            return N_CREATE_RQ;
        case 0x8140:
            return N_CREATE_RSP;
        case 0x0150:
            return N_DELETE_RQ;
        case 0x8150:
            return N_DELETE_RSP;
        case 0x0FFF:
            return C_CANCEL_RQ;
        default:
            throw new IllegalArgumentException("commandField: " + commandField);
        }
    }

    public String toString(Attributes cmdAttrs, int pcid, String tsuid) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmdAttrs.getInt(tagOfMessageID, -1)).append(prompt).append(pcid);
        switch (this) {
        case C_STORE_RQ:
            promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
            promptMoveOriginatorTo(cmdAttrs, sb);
            break;
        case C_GET_RQ:
            promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
            promptAttributeIdentifierListTo(cmdAttrs, sb);
            break;
        case C_FIND_RQ:
        case C_MOVE_RQ:
            promptIntTo(cmdAttrs, ", prior=", Tag.Priority, sb);
            break;
        case C_GET_RSP:
        case C_MOVE_RSP:
            promptNumberOfSubOpsTo(cmdAttrs, sb);
            break;
        case N_EVENT_REPORT_RQ:
        case N_EVENT_REPORT_RSP:
            promptIntTo(cmdAttrs, ", eventID=", Tag.EventTypeID, sb);
            break;
        case N_ACTION_RQ:
        case N_ACTION_RSP:
            promptIntTo(cmdAttrs, ", actionID=", Tag.ActionTypeID, sb);
            break;
        }
        if (isRSP()) {
            sb.append(", status=")
              .append(Integer.toHexString(cmdAttrs.getInt(Tag.Status, -1)))
              .append('H');
            promptIntTo(cmdAttrs, ", errorID=", Tag.ErrorID, sb);
            promptStringTo(cmdAttrs, ", errorComment=", Tag.ErrorComment, sb);
            promptAttributeIdentifierListTo(cmdAttrs, sb);
        }
        promptUIDTo(cmdAttrs, "  cuid=", tagOfSOPClassUID, sb);
        promptUIDTo(cmdAttrs, "  iuid=", tagOfSOPInstanceUID, sb);
        promptUIDTo("  tsuid=", tsuid, sb);
        return sb.toString();
    }

    private static void promptIntTo(Attributes cmd, String name, int tag,
            StringBuilder sb) {
        int val = cmd.getInt(tag, 0);
        if (val != 0 || cmd.containsValue(tag))
            sb.append(name).append(val);
    }

    private static void promptStringTo(Attributes cmd, String name, int tag,
            StringBuilder sb) {
        String s = cmd.getString(tag, null);
        if (s != null)
            sb.append(name).append(s);
    }

    private static void promptUIDTo(Attributes cmd, String name, int tag,
            StringBuilder sb) {
        if (tag != 0) {
            String uid = cmd.getString(tag, null);
            if (uid != null)
                promptUIDTo(name, uid, sb);
        }
    }

    private static void promptUIDTo(String name, String uid, StringBuilder sb) {
        sb.append(StringUtils.LINE_SEPARATOR).append(name);
        UIDUtils.promptTo(uid, sb);
    }

    private static void promptMoveOriginatorTo(Attributes cmd, StringBuilder sb) {
        String aet = cmd.getString(Tag.MoveOriginatorApplicationEntityTitle,
                null);
        if (aet != null)
            sb.append(StringUtils.LINE_SEPARATOR)
              .append("  orig=")
              .append(aet)
              .append(" >> ")
              .append(cmd.getInt(Tag.MoveOriginatorMessageID, -1))
              .append(":C-MOVE-RQ");
    }

    private static void promptAttributeIdentifierListTo(Attributes cmd,
            StringBuilder sb) {
        int[] tags = cmd.getInts(Tag.AttributeIdentifierList);
        if (tags == null)
            return;

        sb.append(StringUtils.LINE_SEPARATOR).append("  tags=[");
        if (tags.length > 0) {
            for (int tag : tags)
                sb.append(TagUtils.toString(tag)).append(", ");
            sb.setLength(sb.length()-2);
        }
        sb.append(']');
    }

    private static void promptNumberOfSubOpsTo(Attributes cmd, StringBuilder sb) {
        promptIntTo(cmd, ", remaining=", Tag.NumberOfRemainingSuboperations, sb);
        promptIntTo(cmd, ", completed=", Tag.NumberOfCompletedSuboperations, sb);
        promptIntTo(cmd, ", failed=", Tag.NumberOfFailedSuboperations, sb);
        promptIntTo(cmd, ", warning=", Tag.NumberOfWarningSuboperations, sb);
    }
}
