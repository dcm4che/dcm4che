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

package org.dcm4che3.util;

import org.dcm4che3.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class TagUtils {

    private static char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String shortToHexString(int n) {
        char[] s = {
                HEX_DIGITS[(n >>> 12) & 0xF],
                HEX_DIGITS[(n >>> 8) & 0xF],
                HEX_DIGITS[(n >>> 4) & 0xF],
                HEX_DIGITS[(n >>> 0) & 0xF] };
        return new String(s);
    }

    public static String toHexString(int tag) {
        char[] s = {
                HEX_DIGITS[(tag >>> 28)],
                HEX_DIGITS[(tag >>> 24) & 0xF],
                HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF],
                HEX_DIGITS[(tag >>> 12) & 0xF],
                HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF],
                HEX_DIGITS[(tag >>> 0) & 0xF] };
        return new String(s);
    }

    public static String toHexString(byte[] b) {
        char[] s = new char[b.length << 1];
        for (int i = 0, j = 0; i < b.length; i++) {
            s[j++] = HEX_DIGITS[(b[i] >>> 4) & 0xF];
            s[j++] = HEX_DIGITS[b[i] & 0xF];
        }
        return new String(s);
    }

    public static String toString(int tag) {
        char[] s = {
                '(',
                HEX_DIGITS[(tag >>> 28)],
                HEX_DIGITS[(tag >>> 24) & 0xF],
                HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF],
                ',',
                HEX_DIGITS[(tag >>> 12) & 0xF],
                HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF],
                HEX_DIGITS[(tag >>> 0) & 0xF],
                ')'};
        return new String(s);
    }

    public static int groupNumber(int tag) {
        return tag >>> 16;
    }

    public static int elementNumber(int tag) {
        return tag & 0xFFFF;
    }

    public static boolean isGroupLength(int tag) {
        return elementNumber(tag) == 0;
    }

    public static boolean isPrivateCreator(int tag) {
        return (tag & 0x00010000) != 0
            && (tag & 0x0000FF00) == 0
            && (tag & 0x000000F0) != 0;
    }

    public static boolean isPrivateGroup(int tag) {
        return (tag & 0x00010000) != 0;
    }

    public static boolean isPrivateTag(int tag) {
        return (tag & 0x00010000) != 0
            && (tag & 0x0000FF00) != 0;
    }

    public static int toTag(int groupNumber, int elementNumber) {
        return groupNumber << 16 | elementNumber;
    }

    public static int toPrivateTag(int creatorTag, int elementNumber) {
        return (creatorTag & 0xffff0000) 
             | ((creatorTag & 0xff) << 8
             | (elementNumber & 0xff));
    }

    public static int creatorTagOf(int tag) {
        return (tag & 0xffff0000) | ((tag >>> 8) & 0xff);
    }

    public static int groupLengthTagOf(int tag) {
        return tag & 0xffff0000;
    }

    public static boolean isItem(int tag) {
        return tag == Tag.Item
            || tag == Tag.ItemDelimitationItem
            || tag == Tag.SequenceDelimitationItem;
    }

    public static boolean isFileMetaInformation(int tag) {
        return (tag & 0xffff0000) == 0x00020000;
    }

    public static int normalizeRepeatingGroup(int tag) {
        int gg000000 = tag & 0xffe00000;
        return (gg000000 == 0x50000000
             || gg000000 == 0x60000000)
                    ? tag & 0xffe0ffff
                    : tag;
    }

    public static int tmTagOf(int daTag) {
        switch (daTag) {
        case Tag.InstanceCreationDate:
            return Tag.InstanceCreationTime;
        case Tag.StudyDate:
            return Tag.StudyTime;
        case Tag.SeriesDate:
            return Tag.SeriesTime;
        case Tag.AcquisitionDate:
            return Tag.AcquisitionTime;
        case Tag.ContentDate:
            return Tag.ContentTime;
        case Tag.OverlayDate:
            return Tag.OverlayTime;
        case Tag.CurveDate:
            return Tag.CurveTime;
        case Tag.PatientBirthDate:
            return Tag.PatientBirthTime;
        case Tag.DateOfSecondaryCapture:
            return Tag.TimeOfSecondaryCapture;
        case Tag.DateOfLastCalibration:
            return Tag.TimeOfLastCalibration;
        case Tag.DateOfLastDetectorCalibration:
            return Tag.TimeOfLastDetectorCalibration;
        case Tag.ModifiedImageDate:
            return Tag.ModifiedImageTime;
        case Tag.StudyVerifiedDate:
            return Tag.StudyVerifiedTime;
        case Tag.StudyReadDate:
            return Tag.StudyReadTime;
        case Tag.ScheduledStudyStartDate:
            return Tag.ScheduledStudyStartTime;
        case Tag.ScheduledStudyStopDate:
            return Tag.ScheduledStudyStopTime;
        case Tag.StudyArrivalDate:
            return Tag.StudyArrivalTime;
        case Tag.StudyCompletionDate:
            return Tag.StudyCompletionTime;
        case Tag.ScheduledAdmissionDate:
            return Tag.ScheduledAdmissionTime;
        case Tag.ScheduledDischargeDate:
            return Tag.ScheduledDischargeTime;
        case Tag.AdmittingDate:
            return Tag.AdmittingTime;
        case Tag.DischargeDate:
            return Tag.DischargeTime;
        case Tag.ScheduledProcedureStepStartDate:
            return Tag.ScheduledProcedureStepStartTime;
        case Tag.ScheduledProcedureStepEndDate:
            return Tag.ScheduledProcedureStepEndTime;
        case Tag.PerformedProcedureStepStartDate:
            return Tag.PerformedProcedureStepStartTime;
        case Tag.PerformedProcedureStepEndDate:
            return Tag.PerformedProcedureStepEndTime;
        case Tag.IssueDateOfImagingServiceRequest:
            return Tag.IssueTimeOfImagingServiceRequest;
        case Tag.Date:
            return Tag.Time;
        case Tag.PresentationCreationDate:
            return Tag.PresentationCreationTime;
        case Tag.CreationDate:
            return Tag.CreationTime;
        case Tag.StructureSetDate:
            return Tag.StructureSetTime;
        case Tag.TreatmentControlPointDate:
            return Tag.TreatmentControlPointTime;
        case Tag.SafePositionExitDate:
            return Tag.SafePositionExitTime;
        case Tag.SafePositionReturnDate:
            return Tag.SafePositionReturnTime;
        case Tag.TreatmentDate:
            return Tag.TreatmentTime;
        case Tag.RTPlanDate:
            return Tag.RTPlanTime;
        case Tag.SourceStrengthReferenceDate:
            return Tag.SourceStrengthReferenceTime;
        case Tag.ReviewDate:
            return Tag.ReviewTime;
        case Tag.InterpretationRecordedDate:
            return Tag.InterpretationRecordedTime;
        case Tag.InterpretationTranscriptionDate:
            return Tag.InterpretationTranscriptionTime;
        case Tag.InterpretationApprovalDate:
            return Tag.InterpretationApprovalTime;
        }
        return 0;
    }

    public static int daTagOf(int tmTag) {
        switch (tmTag) {
        case Tag.InstanceCreationTime:
            return Tag.InstanceCreationDate;
        case Tag.StudyTime:
            return Tag.StudyDate;
        case Tag.SeriesTime:
            return Tag.SeriesDate;
        case Tag.AcquisitionTime:
            return Tag.AcquisitionDate;
        case Tag.ContentTime:
            return Tag.ContentDate;
        case Tag.OverlayTime:
            return Tag.OverlayDate;
        case Tag.CurveTime:
            return Tag.CurveDate;
        case Tag.PatientBirthTime:
            return Tag.PatientBirthDate;
        case Tag.TimeOfSecondaryCapture:
            return Tag.TimeOfSecondaryCapture;
        case Tag.TimeOfLastCalibration:
            return Tag.TimeOfLastCalibration;
        case Tag.TimeOfLastDetectorCalibration:
            return Tag.TimeOfLastDetectorCalibration;
        case Tag.ModifiedImageTime:
            return Tag.ModifiedImageDate;
        case Tag.StudyVerifiedTime:
            return Tag.StudyVerifiedDate;
        case Tag.StudyReadTime:
            return Tag.StudyReadDate;
        case Tag.ScheduledStudyStartTime:
            return Tag.ScheduledStudyStartDate;
        case Tag.ScheduledStudyStopTime:
            return Tag.ScheduledStudyStopDate;
        case Tag.StudyArrivalTime:
            return Tag.StudyArrivalDate;
        case Tag.StudyCompletionTime:
            return Tag.StudyCompletionDate;
        case Tag.ScheduledAdmissionTime:
            return Tag.ScheduledAdmissionDate;
        case Tag.ScheduledDischargeTime:
            return Tag.ScheduledDischargeDate;
        case Tag.AdmittingTime:
            return Tag.AdmittingDate;
        case Tag.DischargeTime:
            return Tag.DischargeDate;
        case Tag.ScheduledProcedureStepStartTime:
            return Tag.ScheduledProcedureStepStartDate;
        case Tag.ScheduledProcedureStepEndTime:
            return Tag.ScheduledProcedureStepEndDate;
        case Tag.PerformedProcedureStepStartTime:
            return Tag.PerformedProcedureStepStartDate;
        case Tag.PerformedProcedureStepEndTime:
            return Tag.PerformedProcedureStepEndDate;
        case Tag.IssueDateOfImagingServiceRequest:
            return Tag.IssueTimeOfImagingServiceRequest;
        case Tag.Time:
            return Tag.Date;
        case Tag.PresentationCreationTime:
            return Tag.PresentationCreationDate;
        case Tag.CreationTime:
            return Tag.CreationDate;
        case Tag.StructureSetTime:
            return Tag.StructureSetDate;
        case Tag.TreatmentControlPointTime:
            return Tag.TreatmentControlPointDate;
        case Tag.SafePositionExitTime:
            return Tag.SafePositionExitDate;
        case Tag.SafePositionReturnTime:
            return Tag.SafePositionReturnDate;
        case Tag.TreatmentTime:
            return Tag.TreatmentDate;
        case Tag.RTPlanTime:
            return Tag.RTPlanDate;
        case Tag.SourceStrengthReferenceTime:
            return Tag.SourceStrengthReferenceDate;
        case Tag.ReviewTime:
            return Tag.ReviewDate;
        case Tag.InterpretationRecordedTime:
            return Tag.InterpretationRecordedDate;
        case Tag.InterpretationTranscriptionTime:
            return Tag.InterpretationTranscriptionDate;
        case Tag.InterpretationApprovalTime:
            return Tag.InterpretationApprovalDate;
        }
        return 0;
    }
}

