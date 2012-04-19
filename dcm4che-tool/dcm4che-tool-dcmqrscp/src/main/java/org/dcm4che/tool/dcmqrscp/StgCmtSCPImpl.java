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

package org.dcm4che.tool.dcmqrscp;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationStateException;
import org.dcm4che.net.Commands;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.Status;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.pdu.RoleSelection;
import org.dcm4che.net.service.DicomService;
import org.dcm4che.net.service.DicomServiceException;

public class StgCmtSCPImpl extends DicomService {

    public StgCmtSCPImpl() {
        super(UID.StorageCommitmentPushModelSOPClass);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse,
            Attributes rq, Attributes actionInfo) throws IOException {
        if (dimse != Dimse.N_ACTION_RQ)
            throw new DicomServiceException(Status.UnrecognizedOperation);

        int actionTypeID = rq.getInt(Tag.ActionTypeID, 0);
        if (actionTypeID != 1)
            throw new DicomServiceException(Status.NoSuchActionType)
                        .setActionTypeID(actionTypeID);

        Attributes rsp = Commands.mkNActionRSP(rq, Status.Success);
        String callingAET = as.getCallingAET();
        String calledAET = as.getCalledAET();
        DcmQRSCP qrscp = DcmQRSCP.deviceOf(as);
        if (qrscp.getRemoteConnection(callingAET) == null)
            throw new DicomServiceException(Status.ProcessingFailure,
                    "Unknown Calling AET: " + callingAET);
        Attributes eventInfo =
                qrscp.calculateStorageCommitmentResult(calledAET, actionInfo);
        try {
            as.writeDimseRSP(pc, rsp, null);
            qrscp.execute(new SendStgCmtResult(as, eventInfo));
        } catch (AssociationStateException e) {
            LOG.warn("{} << N-ACTION-RSP failed: {}", as, e.getMessage());
        }
    }

    private static class SendStgCmtResult implements Runnable {

        private final DcmQRSCP qrscp;
        private final Association as;
        private final Attributes eventInfo;

        SendStgCmtResult(Association as, Attributes eventInfo) {
            this.qrscp = DcmQRSCP.deviceOf(as);
            this.as = as;
            this.eventInfo = eventInfo;
        }

        @Override
        public void run() {
            if (qrscp.isStgCmtOnSameAssoc()) {
                try {
                    neventReport(as);
                    return;
                } catch (Exception e) {
                    LOG.info("Failed to return Storage Commitment Result in same Association:", e);
                }
            }
            try {
                Association diffAssoc = as.getApplicationEntity().connect(
                        as.getConnection(),
                        qrscp.getRemoteConnection(as.getRemoteAET()),
                        makeAAssociateRQ());
                neventReport(diffAssoc);
                diffAssoc.release();
            } catch (Exception e) {
                LOG.error("Failed to return Storage Commitment Result in new Association:", e);
            }
        }

        private void neventReport(Association as)
                throws IOException, InterruptedException {
            as.neventReport(UID.StorageCommitmentPushModelSOPClass,
                    UID.StorageCommitmentPushModelSOPInstance, 
                    eventTypeId(eventInfo), eventInfo, null).next();
        }
    
        private int eventTypeId(Attributes eventInfo) {
            return eventInfo.containsValue(Tag.FailedSOPSequence) ? 2 : 1;
        }
    
        private AAssociateRQ makeAAssociateRQ() {
            AAssociateRQ aarq = new AAssociateRQ();
            aarq.setCallingAET(as.getLocalAET());
            aarq.setCalledAET(as.getRemoteAET());
            ApplicationEntity ae = as.getApplicationEntity();
            TransferCapability tc = ae.getTransferCapabilityFor(
                    UID.StorageCommitmentPushModelSOPClass, TransferCapability.Role.SCP);
            aarq.addPresentationContext(
                            new PresentationContext(
                                    1,
                                    UID.StorageCommitmentPushModelSOPClass,
                                    tc.getTransferSyntaxes()));
            aarq.addRoleSelection(
                    new RoleSelection(UID.StorageCommitmentPushModelSOPClass, false, true));
            return aarq;
        }

    }
}
