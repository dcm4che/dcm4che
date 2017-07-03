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
 * Java(TM), hosted at https://github.com/dcm4che.
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

package org.dcm4che3.net;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Commands {

    public static final int NO_DATASET = 0x0101;
    private static int withDatasetType = 0x0000;

    public static Attributes mkCStoreRQ(int msgId, String cuid, String iuid,
            int priority)  {
       Attributes rq = mkRQ(msgId, 0x0001, withDatasetType);
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

    public static Attributes mkCStoreRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_STORE_RQ);
    }

    public static Attributes mkCFindRQ(int msgId, String cuid, int priority) {
       Attributes rq = mkRQ(msgId, 0x0020, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       return rq;
    }

    public static Attributes mkCFindRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_FIND_RQ);
    }

    public static Attributes mkCGetRQ(int msgId, String cuid, int priority) {
       Attributes rq = mkRQ(msgId, 0x0010, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       return rq;
    }

    public static Attributes mkCGetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_GET_RQ);
    }

    public static Attributes mkCMoveRQ(int msgId, String cuid, int priority,
            String destination) {
       Attributes rq = mkRQ(msgId, 0x0021, withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setInt(Tag.Priority, VR.US, priority);
       rq.setString(Tag.MoveDestination, VR.AE, destination);
       return rq;
    }

    public static Attributes mkCMoveRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_MOVE_RQ);
    }

    public static Attributes mkCCancelRQ(int msgId) {
        Attributes rq = new Attributes();
        rq.setInt(Tag.CommandField, VR.US, Dimse.C_CANCEL_RQ.commandField());
        rq.setInt(Tag.CommandDataSetType, VR.US, NO_DATASET);
        rq.setInt(Tag.MessageIDBeingRespondedTo, VR.US, msgId);
        return rq;
    }

    public static Attributes mkCEchoRQ(int msgId, String cuid) {
       Attributes rq = mkRQ(msgId, 0x0030, NO_DATASET);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       return rq;
    }

    public static Attributes mkEchoRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.C_ECHO_RQ);
    }

    public static Attributes mkNEventReportRQ(int msgId, String cuid,
            String iuid, int eventTypeID, Attributes data) {
       Attributes rq = mkRQ(msgId, 0x0100,
               data == null ? NO_DATASET : withDatasetType);
       rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
       rq.setInt(Tag.EventTypeID, VR.US, eventTypeID);
       return rq;
    }

    public static Attributes mkNEventReportRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_EVENT_REPORT_RQ);
    }

    public static Attributes mkNGetRQ(int msgId, String cuid, String iuid,
            int[] tags) {
       Attributes rq = mkRQ(msgId, 0x0110, NO_DATASET);
       rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
       if (tags != null)
           rq.setInt(Tag.AttributeIdentifierList, VR.AT, tags);
       return rq;
    }

    public static Attributes mkNGetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_GET_RQ);
    }

    public static Attributes mkNSetRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0120, withDatasetType);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNSetRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_SET_RQ);
    }

    public static Attributes mkNActionRQ(int msgId, String cuid,
            String iuid, int actionTypeID, Attributes data) {
       Attributes rq = mkRQ(msgId, 0x0130, 
               data == null ? NO_DATASET : withDatasetType);
       rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
       rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
       rq.setInt(Tag.ActionTypeID, VR.US, actionTypeID);
       return rq;
    }

    public static Attributes mkNActionRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_ACTION_RQ);
    }

    public static Attributes mkNCreateRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0140, withDatasetType);
        rq.setString(Tag.AffectedSOPClassUID, VR.UI, cuid);
        if (iuid != null)
            rq.setString(Tag.AffectedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }

    public static Attributes mkNCreateRSP(Attributes cmd, int status) {
        String iuid = cmd.getString(Tag.AffectedSOPInstanceUID);
        if (iuid == null)
            cmd.setString(Tag.AffectedSOPInstanceUID, VR.UI, UIDUtils.createUID());
        return mkRSP(cmd, status, Dimse.N_CREATE_RQ);
    }

    public static Attributes mkNDeleteRQ(int msgId, String cuid, String iuid) {
        Attributes rq = mkRQ(msgId, 0x0150, NO_DATASET);
        rq.setString(Tag.RequestedSOPClassUID, VR.UI, cuid);
        rq.setString(Tag.RequestedSOPInstanceUID, VR.UI, iuid);
        return rq;
    }    

    public static Attributes mkNDeleteRSP(Attributes cmd, int status) {
        return mkRSP(cmd, status, Dimse.N_DELETE_RQ);
    }

    private static Attributes mkRQ(int msgId, int cmdField, int datasetType) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.MessageID, VR.US, msgId);
        rsp.setInt(Tag.CommandField, VR.US, cmdField);
        rsp.setInt(Tag.CommandDataSetType, VR.US, datasetType);
        return rsp;
    }

    public static Attributes mkRSP(Attributes rq, int status, Dimse rqCmd) {
        Attributes rsp = new Attributes();
        rsp.setInt(Tag.CommandField, VR.US, rqCmd.commandFieldOfRSP());
        rsp.setInt(Tag.Status, VR.US, status);
        rsp.setInt(Tag.MessageIDBeingRespondedTo, VR.US,
                rq.getInt(Tag.MessageID, 0));
        rsp.setString(Tag.AffectedSOPClassUID, VR.UI,
                rq.getString(rqCmd.tagOfSOPClassUID()));
        int tagOfIUID = rqCmd.tagOfSOPInstanceUID();
        if (tagOfIUID != 0)
            rsp.setString(Tag.AffectedSOPInstanceUID, VR.UI,
                    rq.getString(tagOfIUID));
        return rsp;
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

}
