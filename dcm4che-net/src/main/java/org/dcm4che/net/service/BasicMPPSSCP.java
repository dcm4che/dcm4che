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

package org.dcm4che.net.service;

import java.io.IOException;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationStateException;
import org.dcm4che.net.Commands;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.util.AttributesValidator;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicMPPSSCP extends DicomService {

    public static final String IN_PROGRESS = "IN PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String DISCONTINUED = "DISCONTINUED";
    public static final String MAY_NO_LONGER_BE_UPDATED =
            "Performed Procedure Step Object may no longer be updated";
    public static final int MAY_NO_LONGER_BE_UPDATED_ERROR_ID = 0xA710;
    private static final int[] NSET_NOT_ALLOWED = {
        Tag.Modality,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.IssuerOfPatientIDQualifiersSequence,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.StudyID,
        Tag.AdmissionID,
        Tag.IssuerOfAdmissionIDSequence,
        Tag.ServiceEpisodeID,
        Tag.ServiceEpisodeDescription,
        Tag.IssuerOfServiceEpisodeIDSequence,
        Tag.PerformedStationAETitle,
        Tag.PerformedStationName,
        Tag.PerformedLocation,
        Tag.PerformedProcedureStepStartDate,
        Tag.PerformedProcedureStepStartTime,
        Tag.PerformedProcedureStepID,
    };

    public BasicMPPSSCP() {
        super(UID.ModalityPerformedProcedureStepSOPClass);
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc, Dimse dimse,
            Attributes rq, Attributes rqAttrs) throws IOException {
        switch (dimse) {
        case N_CREATE_RQ:
            onNCreateRQ(as, pc, rq, rqAttrs);
            break;
        case N_SET_RQ:
            onNSetRQ(as, pc, rq, rqAttrs);
            break;
        default:
            throw new DicomServiceException(Status.UnrecognizedOperation);
        }
    }

    protected void onNCreateRQ(Association as, PresentationContext pc,
            Attributes rq, Attributes rqAttrs) throws IOException {
        checkNCreateRQ(rqAttrs);
        Attributes rsp = Commands.mkNCreateRSP(rq, Status.Success);
        Attributes rspAttrs = create(as, rq, rqAttrs, rsp);
        try {
            as.writeDimseRSP(pc, rsp, rspAttrs);
        } catch (AssociationStateException e) {
            LOG.warn("{} << N-CREATE-RSP failed: {}", as, e.getMessage());
        }
    }

    protected Attributes create(Association as, Attributes rq,
            Attributes rqAttrs, Attributes rsp) throws DicomServiceException {
        return null;
    }

    protected void onNSetRQ(Association as, PresentationContext pc,
            Attributes rq, Attributes rqAttrs) throws IOException {
        checkNSetRQ(rqAttrs);
        Attributes rsp = Commands.mkNSetRSP(rq, Status.Success);
        Attributes rspAttrs = set(as, rq, rqAttrs, rsp);
        try {
            as.writeDimseRSP(pc, rsp, rspAttrs);
        } catch (AssociationStateException e) {
            LOG.warn("{} << N-SET-RSP failed: {}", as, e.getMessage());
        }
    }

    protected Attributes set(Association as, Attributes rq, Attributes rqAttrs,
            Attributes rsp) throws DicomServiceException {
        return null;
    }

    private void checkNCreateRQ(Attributes rqAttrs) throws DicomServiceException {
        AttributesValidator validator = new AttributesValidator(rqAttrs);
        Sequence ssaSeq = validator.getType1Sequence(
                Tag.ScheduledStepAttributesSequence,
                Integer.MAX_VALUE);
        if (ssaSeq != null) {
            for (Attributes ssa : ssaSeq) {
                if (!ssa.containsValue(Tag.StudyInstanceUID)) {
                    validator.addInvalidAttribueValue(Tag.ScheduledStepAttributesSequence);
                    break;
                }
            }
        }
        validator.getType1String(Tag.PerformedProcedureStepID, 0, 1);
        validator.getType1String(Tag.PerformedStationAETitle, 0, 1);
        validator.getType1String(Tag.PerformedProcedureStepStartDate, 0, 1);
        validator.getType1String(Tag.PerformedProcedureStepStartTime, 0, 1);
        validator.getType1String(Tag.PerformedProcedureStepStatus, 0, 1, IN_PROGRESS);
        if (validator.hasMissingAttributes())
            throw new DicomServiceException(Status.MissingAttribute)
                .setAttributeIdentifierList(validator.getMissingAttributes());
        if (validator.hasMissingAttributeValues())
            throw new DicomServiceException(Status.MissingAttributeValue)
                .setDataset(validator.getMissingAttributeValues());
        if (validator.hasInvalidAttributeValues())
            throw new DicomServiceException(Status.InvalidAttributeValue)
                .setDataset(validator.getInvalidAttributeValues());
    }

    private void checkNSetRQ(Attributes rqAttrs) throws DicomServiceException {
        Attributes notAllowed = new Attributes(rqAttrs, NSET_NOT_ALLOWED);
        if (!notAllowed.isEmpty())
            throw new DicomServiceException(Status.NoSuchAttribute)
                .setAttributeIdentifierList(notAllowed.tags());
        AttributesValidator validator = new AttributesValidator(rqAttrs);
        validator.getType3String(Tag.PerformedProcedureStepStatus, 0, 1, 
                IN_PROGRESS, COMPLETED, DISCONTINUED);
        if (validator.hasInvalidAttributeValues())
            throw new DicomServiceException(Status.InvalidAttributeValue)
                .setDataset(validator.getInvalidAttributeValues());
    }

}
