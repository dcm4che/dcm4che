//
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

package org.dcm4che3.tool.dcmqrscp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.media.DicomDirReader;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.AssociationStateException;
import org.dcm4che3.net.Commands;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.DicomServiceException;

public class StgCmtSCPImpl extends AbstractDicomService {
    private final DicomDirReader dicomDirReader;
    private final Map<String,Connection> remoteConnections;
    private final boolean stgCmtOnSameAssoc;
    private final Executor executor;
    
    public StgCmtSCPImpl(DicomDirReader dicomDirReader, Map<String,Connection> remoteConnections, 
            boolean stgCmtOnSameAssoc, Executor executor) {
        super(UID.StorageCommitmentPushModelSOPClass);
        this.dicomDirReader = dicomDirReader;
        this.remoteConnections = remoteConnections;
        this.stgCmtOnSameAssoc = stgCmtOnSameAssoc;
        this.executor = executor;
    }

    @Override
    protected void onDimseRQ(Association as, PresentationContext pc,
            Dimse dimse, Attributes rq, Attributes actionInfo)
            throws IOException {
        if (dimse != Dimse.N_ACTION_RQ)
            throw new DicomServiceException(Status.UnrecognizedOperation);

        int actionTypeID = rq.getInt(Tag.ActionTypeID, 0);
        if (actionTypeID != 1)
            throw new DicomServiceException(Status.NoSuchActionType)
                    .setActionTypeID(actionTypeID);

        Attributes rsp = Commands.mkNActionRSP(rq, Status.Success);
        String callingAET = as.getCallingAET();
        String calledAET = as.getCalledAET();
        Connection remoteConnection = remoteConnections.get(callingAET);
        if (remoteConnection == null)
            throw new DicomServiceException(Status.ProcessingFailure, "Unknown Calling AET: " + callingAET);
        Attributes eventInfo = calculateStorageCommitmentResult(calledAET, actionInfo);
        try {
            as.writeDimseRSP(pc, rsp, null);
            executor.execute(new SendStgCmtResult(as, eventInfo, stgCmtOnSameAssoc, remoteConnection));
        } catch (AssociationStateException e) {
            DcmQRSCP.LOG.warn("{} << N-ACTION-RSP failed: {}", as, e.getMessage());
        }
    }
    
    private Attributes calculateStorageCommitmentResult(String calledAET,
            Attributes actionInfo) throws DicomServiceException {
        Sequence requestSeq = actionInfo.getSequence(Tag.ReferencedSOPSequence);
        int size = requestSeq.size();
        String[] sopIUIDs = new String[size];
        Attributes eventInfo = new Attributes(6);
        eventInfo.setString(Tag.RetrieveAETitle, VR.AE, calledAET);
        eventInfo.setString(Tag.StorageMediaFileSetID, VR.SH,
                dicomDirReader.getFileSetID());
        eventInfo.setString(Tag.StorageMediaFileSetUID, VR.SH,
                dicomDirReader.getFileSetUID());
        eventInfo.setString(Tag.TransactionUID, VR.UI,
                actionInfo.getString(Tag.TransactionUID));
        Sequence successSeq = eventInfo.newSequence(Tag.ReferencedSOPSequence,
                size);
        Sequence failedSeq = eventInfo.newSequence(Tag.FailedSOPSequence, size);
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(
                size * 4 / 3);
        for (int i = 0; i < sopIUIDs.length; i++) {
            Attributes item = requestSeq.get(i);
            map.put(sopIUIDs[i] = item.getString(Tag.ReferencedSOPInstanceUID),
                    item.getString(Tag.ReferencedSOPClassUID));
        }
        
        Map<String,Integer> instanceStatusMap = calculateMatches(map);
        
        for(Entry<String,Integer> entry : instanceStatusMap.entrySet()) {
            String iuid = entry.getKey();
            int status = entry.getValue();
            if(status == Status.Success) {
                successSeq.add(refSOP(iuid, map.get(iuid), status));
            } else {
                failedSeq.add(refSOP(iuid, map.get(iuid), status));
            }
        }
        
        if (failedSeq.isEmpty()) {
            eventInfo.remove(Tag.FailedSOPSequence);
        }
        
        return eventInfo;
    }
    
    protected Map<String,Integer> calculateMatches(Map<String,String> requestMap) throws DicomServiceException {
        Map<String, String> requestMapCopy = new HashMap<String, String>(requestMap);
        Map<String, Integer> instanceStatusMap = new HashMap<String, Integer>();
        
        String[] sopIUIDs = requestMap.keySet().toArray(new String[requestMap.keySet().size()]);
        
        DicomDirReader ddr = dicomDirReader;
        try {
            Attributes patRec = ddr.findPatientRecord();
            while (patRec != null) {
                Attributes studyRec = ddr.findStudyRecord(patRec);
                while (studyRec != null) {
                    Attributes seriesRec = ddr.findSeriesRecord(studyRec);
                    while (seriesRec != null) {
                        Attributes instRec = ddr.findLowerInstanceRecord(
                                seriesRec, true, sopIUIDs);
                        while (instRec != null) {
                            String iuid = instRec.getString(Tag.ReferencedSOPInstanceUIDInFile);
                            String cuid = requestMapCopy.remove(iuid);
                            if (cuid.equals(instRec.getString(Tag.ReferencedSOPClassUIDInFile))) {
                                instanceStatusMap.put(iuid, Status.Success);
                            }
                            else {
                                instanceStatusMap.put(iuid, Status.ClassInstanceConflict);
                            }
                               
                            instRec = ddr.findNextInstanceRecord(instRec, true,
                                    sopIUIDs);
                        }
                        seriesRec = ddr.findNextSeriesRecord(seriesRec);
                    }
                    studyRec = ddr.findNextStudyRecord(studyRec);
                }
                patRec = ddr.findNextPatientRecord(patRec);
            }
        } catch (IOException e) {
            DcmQRSCP.LOG.info("Failed to M-READ " + ddr.getFile(), e);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
        
        for (Map.Entry<String, String> entry : requestMapCopy.entrySet()) {
            instanceStatusMap.put(entry.getKey(), Status.NoSuchObjectInstance);
        }
        
        return instanceStatusMap;
    }
    
    private static Attributes refSOP(String iuid, String cuid, int failureReason) {
        Attributes attrs = new Attributes(3);
        attrs.setString(Tag.ReferencedSOPClassUID, VR.UI, cuid);
        attrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, iuid);
        if (failureReason != Status.Success)
            attrs.setInt(Tag.FailureReason, VR.US, failureReason);
        return attrs;
    }
    
}