/* ***** BEGIN LICENSE BLOCK *****
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
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che.net;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.util.StringUtils;
import org.dcm4che.util.TagUtils;
import org.dcm4che.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Commands {

    public static final int C_STORE_RQ = 0x0001;
    public static final int C_STORE_RSP = 0x8001;
    public static final int C_GET_RQ = 0x0010;
    public static final int C_GET_RSP = 0x8010;
    public static final int C_FIND_RQ = 0x0020;
    public static final int C_FIND_RSP = 0x8020;
    public static final int C_MOVE_RQ = 0x0021;
    public static final int C_MOVE_RSP = 0x8021;
    public static final int C_ECHO_RQ = 0x0030;
    public static final int C_ECHO_RSP = 0x8030;
    public static final int N_EVENT_REPORT_RQ = 0x0100;
    public static final int N_EVENT_REPORT_RSP = 0x8100;
    public static final int N_GET_RQ = 0x0110;
    public static final int N_GET_RSP = 0x8110;
    public static final int N_SET_RQ = 0x0120;
    public static final int N_SET_RSP = 0x8120;
    public static final int N_ACTION_RQ = 0x0130;
    public static final int N_ACTION_RSP = 0x8130;
    public static final int N_CREATE_RQ = 0x0140;
    public static final int N_CREATE_RSP = 0x8140;
    public static final int N_DELETE_RQ = 0x0150;
    public static final int N_DELETE_RSP = 0x8150;
    public static final int C_CANCEL_RQ = 0x0FFF;
    private static final int RSP = 0x8000;

    public static final int NO_DATASET = 0x0101;
    private static int withDatasetType = 0x0000;

    public static boolean isRSP(int cmdField) {
        return (cmdField & RSP) != 0;
    }

    public static boolean isCancelRQ(Attributes cmd) {
        return cmd.getInt(Tag.CommandField, 0) == C_CANCEL_RQ;
    }

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid,
            int priority)  {
       Attributes rq = mkRQ(msgId, C_STORE_RQ, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       return rq;
    }

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid,
            int priority, String moveOriginatorAET, int moveOriginatorMsgId) {
       Attributes rq = mkCStoreRQ(msgId, cuid, iuid, priority);
       rq.setString(Tag.MoveOriginatorApplicationEntityTitle, VR.AE,
               moveOriginatorAET);
       rq.setInt(Tag.MoveOriginatorMessageID, VR.US, moveOriginatorMsgId);
       return rq;
    }

    public static Attributes mkCFindRQ(int msgId, String cuid, int priority) {
       Attributes rq = mkRQ(msgId, C_FIND_RQ, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       return rq;
    }

    public static Attributes mkCGetRQ(int msgId, String cuid, int priority) {
       Attributes rq = mkRQ(msgId, C_GET_RQ, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       return rq;
    }

    public static Attributes mkCMoveRQ(int msgId, String cuid, int priority,
            String destination) {
       Attributes rq = mkRQ(msgId, C_MOVE_RQ, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       rq.setString(Tag.MoveDestination, VR.AE, destination);
       return rq;
    }

    public static Attributes mkCCancelRQ(int msgId) {
        Attributes rq = new Attributes();
        rq.setInt(Tag.CommandField, VR.US, C_CANCEL_RQ);
        rq.setInt(Tag.CommandDataSetType, VR.US, NO_DATASET);
        rq.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rq;
    }

    public static Attributes mkCEchoRQ(int msgId, String cuid) {
       Attributes rq = mkRQ(msgId, C_ECHO_RQ, NO_DATASET);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       return rq;
    }

    public static Attributes mkNEventReportRQ(int msgId, String cuid,
            String iuid, int eventTypeID, Attributes data) {
       Attributes rq = mkRQ(msgId, N_EVENT_REPORT_RQ, 
               data == null ? NO_DATASET : withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
       rq.setInt(Tag.EventTypeID, VR.US, eventTypeID);
       return rq;
    }

    public static Attributes mkNGetRQ(int msgId, String cuid, String iuid,
            int[] tags) {
       Attributes rq = mkRQ(msgId, N_GET_RQ, NO_DATASET);
       rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
       if (tags != null)
           rq.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
       return rq;
    }

    public static Attributes mkNSetRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, N_SET_RQ, withDatasetType);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNActionRQ(int msgId, String cuid,
            String iuid, int actionTypeID, Attributes data) {
       Attributes rq = mkRQ(msgId, N_ACTION_RQ, 
               data == null ? NO_DATASET : withDatasetType);
       rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
       rq.setInt(Tag.ActionTypeID, VR.US, actionTypeID);
       return rq;
    }

    public static Attributes mkNCreateRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, N_CREATE_RQ, withDatasetType);
        if (cuid != null)
            rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNDeleteRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, N_DELETE_RQ, NO_DATASET);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }    

    private static Attributes mkRQ(int msgId, int cmdfield, int datasetType) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.MessageID, VR.US, msgId);
        rsp.setInt(Tag.CommandField, VR.US, cmdfield);
        rsp.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        return rsp;
    }

    public static Attributes mkRSP(Attributes rq, int status) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.CommandField, VR.US, rspFieldFor(rq));
        rsp.setInt(Tag.Status, VR.US, status);
        rsp.setInt(Tag.MessageIDBeingRespondedTo, VR.US,
                rq.getInt(Tag.MessageID, 0));
        return rsp;
    }

    public static int rspFieldFor(Attributes rq) {
        return rq.getInt(Tag.CommandField, 0) | RSP;
    }

    public static void includeUIDsinRSP(Attributes rq, Attributes rsp) {
        String cuid = rq.getString(Tag.AffectedSOPClassUID, null);
        if (cuid == null)
            cuid = rq.getString(Tag.RequestedSOPClassUID, null);
        rsp.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID, null);
        if (iuid == null)
            iuid = rq.getString(Tag.RequestedSOPInstanceUID, null);
        if (iuid != null) {
            rsp.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        }
    }

    public static void initNumberOfSuboperations(Attributes rsp,
            int remaining) {
        rsp.setInt(Tag.NumberOfRemainingSuboperations, VR.US, remaining);
        rsp.setInt(Tag.NumberOfCompletedSuboperations, VR.US, 0);
        rsp.setInt(Tag.NumberOfFailedSuboperations, VR.US, 0);
        rsp.setInt(Tag.NumberOfWarningSuboperations, VR.US, 0);
    }

    public static void incNumberOfSuboperations(int tag, Attributes rsp) {
        synchronized (rsp) {
            rsp.setInt(tag, VR.US, rsp.getInt(tag, 0) + 1);
            rsp.setInt(Tag.NumberOfRemainingSuboperations, VR.US, 
                    rsp.getInt(Tag.NumberOfRemainingSuboperations, 1) - 1);
        }
    }

    public static int getWithDatasetType() {
        return withDatasetType;
    }

    public static void setWithDatasetType(int withDatasetType) {
        if (withDatasetType == NO_DATASET
                || (withDatasetType & 0xffff0000) != 0)
            throw new IllegalArgumentException("withDatasetType: " 
                    + Integer.toHexString(withDatasetType) + "H");
        Commands.withDatasetType = withDatasetType;
    }

    public static boolean hasDataset(Attributes cmd) {
        return cmd.getInt(Tag.CommandDataSetType, 0) != NO_DATASET;
    }

    public static boolean isPending(int status) {
        return (status & Status.Pending) == Status.Pending;
    }

    public static StringBuilder promptTo(Attributes cmd, int pcid,
            String tsuid, StringBuilder sb) {
        int cmdfield = cmd.getInt(Tag.CommandField, -1);
        switch (cmdfield) {
        case C_STORE_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":C-STORE-RQ[pcid=", pcid, sb);
            promptIntTo(cmd, ", prior=", Tag.Priority, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.AffectedSOPInstanceUID, sb);
            promptMoveOriginatorTo(cmd, sb);
            break;
        case C_GET_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":C-GET-RQ[pcid=", pcid, sb);
            promptIntTo(cmd, ", prior=", Tag.Priority, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            break;
        case C_FIND_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":C-FIND-RQ[pcid=", pcid, sb);
            promptIntTo(cmd, ", prior=", Tag.Priority, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            break;
        case C_MOVE_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":C-MOVE-RQ[pcid=", pcid, sb);
            promptIntTo(cmd, ", prior=", Tag.Priority, sb);
            promptStringTo(cmd, ", dest=", Tag.MoveDestination, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            break;
        case C_ECHO_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":C-ECHO-RQ[pcid=", pcid, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            break;
        case N_EVENT_REPORT_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":N-EVENT-REPORT-RQ[pcid=", pcid, sb);
            promptIntTo(cmd, ", eventID=", Tag.EventTypeID, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.AffectedSOPInstanceUID, sb);
            break;
        case N_GET_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":N-GET-RQ[pcid=", pcid, sb);
            promptAttributeIdentifierListTo(cmd, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.AffectedSOPInstanceUID, sb);
            break;
        case N_SET_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":N-SET-RQ[pcid=", pcid, sb);
            promptUIDTo(cmd, "  cuid=", Tag.RequestedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.RequestedSOPInstanceUID, sb);
            break;
        case N_ACTION_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":N-ACTION-RQ[pcid=", pcid, sb);
            promptIntTo(cmd, ", actionID=", Tag.ActionTypeID, sb);
            promptUIDTo(cmd, "  cuid=", Tag.RequestedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.RequestedSOPInstanceUID, sb);
            break;
        case N_CREATE_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":N-CREATE-RQ[pcid=", pcid, sb);
            promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.AffectedSOPClassUID, sb);
            break;
        case N_DELETE_RQ:
            promptHeaderTo(cmd, Tag.MessageID, ":N-DELETE-RQ[pcid=", pcid, sb);
            promptUIDTo(cmd, "  cuid=", Tag.RequestedSOPClassUID, sb);
            promptUIDTo(cmd, "  iuid=", Tag.RequestedSOPInstanceUID, sb);
            break;
        case C_CANCEL_RQ:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":C-CANCEL-RQ[pcid=", pcid, sb);
            break;
        case C_STORE_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":C-STORE-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        case C_GET_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":C-GET-RSP[pcid=", pcid, sb);
            promptNumberOfSubOpsTo(cmd, sb);
            promptStatusTo(cmd, sb);
            break;
        case C_FIND_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":C-FIND-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        case C_MOVE_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":C-MOVE-RSP[pcid=", pcid, sb);
            promptNumberOfSubOpsTo(cmd, sb);
            promptStatusTo(cmd, sb);
            break;
        case C_ECHO_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":C-ECHO-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        case N_EVENT_REPORT_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":N-EVENT-REPORT-RSP[pcid=", pcid, sb);
            promptIntTo(cmd, ", eventID=", Tag.EventTypeID, sb);
            promptStatusTo(cmd, sb);
            break;
        case N_GET_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":N-GET-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        case N_SET_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":N-SET-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        case N_ACTION_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":N-ACTION-RSP[pcid=", pcid, sb);
            promptIntTo(cmd, ", actionID=", Tag.ActionTypeID, sb);
            promptStatusTo(cmd, sb);
            break;
        case N_CREATE_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":N-CREATE-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        case N_DELETE_RSP:
            promptHeaderTo(cmd, Tag.MessageIDBeingRespondedTo,
                    ":N-DELETE-RSP[pcid=", pcid, sb);
            promptStatusTo(cmd, sb);
            break;
        default:
            throw new IllegalArgumentException("CommandField:" + cmdfield);
        }
        if (hasDataset(cmd))
            promptUIDTo("  ts=", tsuid, sb);
        sb.append(']');
        return sb;
    }

    private static void promptHeaderTo(Attributes cmd, int tag, String msg,
            int pcid, StringBuilder sb) {
        sb.append(cmd.getInt(tag, -1)).append(msg).append(pcid);
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
        promptUIDTo(name, cmd.getString(tag, null), sb);
    }

    private static void promptUIDTo(String name, String uid,
            StringBuilder sb) {
        if (uid != null)
            UIDUtils.promptTo(uid,
                    sb.append(StringUtils.LINE_SEPARATOR).append(name));
    }

    private static void promptMoveOriginatorTo(Attributes cmd,
            StringBuilder sb) {
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

    private static void promptStatusTo(Attributes cmd, StringBuilder sb) {
        sb.append(", status=")
          .append(Integer.toHexString(cmd.getInt(Tag.Status, -1)))
          .append('H');
        promptIntTo(cmd, ", errorID=", Tag.ErrorID, sb);
        promptStringTo(cmd, ", errorComment=", Tag.ErrorComment, sb);
        promptUIDTo(cmd, "  cuid=", Tag.AffectedSOPClassUID, sb);
        promptUIDTo(cmd, "  iuid=", Tag.AffectedSOPInstanceUID, sb);
    }
}
