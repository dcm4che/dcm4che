/*
 * **** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.deident;

import org.dcm4che3.data.*;
import org.dcm4che3.dcmr.DeIdentificationMethod;
import org.dcm4che3.util.UIDUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Feb 2018
 */
public class DeIdentifier {

    private static final int[] X = {
            Tag.AcquisitionComments,
            Tag.AcquisitionContextSequence,
            Tag.AcquisitionProtocolDescription,
            Tag.ActualHumanPerformersSequence,
            Tag.AdditionalPatientHistory,
            Tag.AddressTrial,
            Tag.AdmissionID,
            Tag.AdmittingDiagnosesCodeSequence,
            Tag.AdmittingDiagnosesDescription,
            Tag.Allergies,
            Tag.Arbitrary,
            Tag.AuthorObserverSequence,
            Tag.BranchOfService,
            Tag.CommentsOnThePerformedProcedureStep,
            Tag.ConfidentialityConstraintOnPatientDataDescription,
            Tag.ConsultingPhysicianIdentificationSequence,
            Tag.ContentCreatorIdentificationCodeSequence,
            Tag.ContentSequence,
            Tag.ContributionDescription,
            Tag.CountryOfResidence,
            Tag.CurrentObserverTrial,
            Tag.CurrentPatientLocation,
            Tag.CustodialOrganizationSequence,
            Tag.Date, // Content Item Attribute
            Tag.DateTime, // Content Item Attribute
            Tag.DataSetTrailingPadding,
            Tag.DerivationDescription,
            Tag.DigitalSignatureUID,
            Tag.DigitalSignaturesSequence,
            Tag.DischargeDiagnosisCodeSequence,
            Tag.DischargeDiagnosisDescription,
            Tag.DistributionAddress,
            Tag.DistributionName,
            Tag.EthnicGroup,
            Tag.FrameComments,
            Tag.GraphicAnnotationSequence,
            Tag.HumanPerformerCodeSequence, // missing in Part 15
            Tag.HumanPerformerName,
            Tag.HumanPerformerOrganization,
            Tag.IconImageSequence,
            Tag.IdentifyingComments,
            Tag.ImageComments,
            Tag.ImagePresentationComments,
            Tag.ImagingServiceRequestComments,
            Tag.Impressions,
            Tag.InsurancePlanIdentification,
            Tag.IntendedRecipientsOfResultsIdentificationSequence,
            Tag.InterpretationApproverSequence,
            Tag.InterpretationAuthor,
            Tag.InterpretationDiagnosisDescription,
            Tag.InterpretationIDIssuer,
            Tag.InterpretationRecorder,
            Tag.InterpretationText,
            Tag.InterpretationTranscriber,
            Tag.IssuerOfAccessionNumberSequence, // missing in Part 15
            Tag.IssuerOfAdmissionID,
            Tag.IssuerOfAdmissionIDSequence, // missing in Part 15
            Tag.IssuerOfPatientID,
            Tag.IssuerOfPatientIDQualifiersSequence, // missing in Part 15
            Tag.IssuerOfServiceEpisodeID,
            Tag.MAC,
            Tag.MedicalAlerts,
            Tag.MedicalRecordLocator,
            Tag.MilitaryRank,
            Tag.ModifiedAttributesSequence,
            Tag.ModifiedImageDescription,
            Tag.ModifyingDeviceID,
            Tag.NameOfPhysiciansReadingStudy,
            Tag.NamesOfIntendedRecipientsOfResults,
            Tag.Occupation,
            Tag.OperatorIdentificationSequence,
            Tag.OrderCallbackPhoneNumber,
            Tag.OrderCallbackTelecomInformation,
            Tag.OrderEnteredBy,
            Tag.OrderEntererLocation,
            Tag.OriginalAttributesSequence,
            Tag.OtherPatientIDs,
            Tag.OtherPatientIDsSequence,
            Tag.OtherPatientNames,
            Tag.ParticipantSequence,
            Tag.PatientAddress,
            Tag.PatientComments,
            Tag.PatientState,
            Tag.PatientTransportArrangements,
            Tag.PatientAge,
            Tag.PatientBirthName,
            Tag.PatientBirthTime,
            Tag.PatientInstitutionResidence,
            Tag.PatientInsurancePlanCodeSequence,
            Tag.PatientMotherBirthName,
            Tag.PatientPrimaryLanguageCodeSequence,
            Tag.PatientPrimaryLanguageModifierCodeSequence,
            Tag.PatientReligiousPreference,
            Tag.PatientSize,
            Tag.PatientSizeCodeSequence, // missing in Part 15
            Tag.PatientTelecomInformation,
            Tag.PatientTelephoneNumbers,
            Tag.PatientWeight,
            Tag.PerformedLocation,
            Tag.PerformedProcedureStepDescription,
            Tag.PerformedProcedureStepID,
            Tag.PerformingPhysicianIdentificationSequence,
            Tag.PerformingPhysicianName,
            Tag.PersonAddress,
            Tag.PersonIdentificationCodeSequence,
            Tag.PersonName, // Content Item Attribute
            Tag.PersonTelecomInformation,
            Tag.PersonTelephoneNumbers,
            Tag.PhysicianApprovingInterpretation,
            Tag.PhysiciansReadingStudyIdentificationSequence,
            Tag.PhysiciansOfRecord,
            Tag.PhysiciansOfRecordIdentificationSequence,
            Tag.PreMedication,
            Tag.PregnancyStatus,
            Tag.ReasonForOmissionDescription,
            Tag.ReasonForTheImagingServiceRequest,
            Tag.ReasonForStudy,
            Tag.ReferencedDigitalSignatureSequence,
            Tag.ReferencedPatientAliasSequence,
            Tag.ReferencedPatientPhotoSequence,
            Tag.ReferencedPatientSequence,
            Tag.ReferencedSOPInstanceMACSequence,
            Tag.ReferringPhysicianAddress,
            Tag.ReferringPhysicianIdentificationSequence,
            Tag.ReferringPhysicianTelephoneNumbers,
            Tag.RegionOfResidence,
            Tag.RequestAttributesSequence,
            Tag.RequestedContrastAgent,
            Tag.RequestedProcedureComments,
            Tag.RequestedProcedureID,
            Tag.RequestedProcedureLocation,
            Tag.RequestingPhysician,
            Tag.RequestingPhysicianIdentificationSequence, // missing in Part 15
            Tag.RequestingService,
            Tag.RequestingServiceCodeSequence, // missing in Part 15
            Tag.ResponsibleOrganization,
            Tag.ResponsiblePerson,
            Tag.ResultsComments,
            Tag.ResultsDistributionListSequence,
            Tag.ResultsIDIssuer,
            Tag.ScheduledHumanPerformersSequence,
            Tag.ScheduledPatientInstitutionResidence,
            Tag.ScheduledPerformingPhysicianIdentificationSequence,
            Tag.ScheduledPerformingPhysicianName,
            Tag.ScheduledProcedureStepDescription,
            Tag.SeriesDescription,
            Tag.SeriesDescriptionCodeSequence, // missing in Part 15
            Tag.ServiceEpisodeDescription,
            Tag.ServiceEpisodeID,
            Tag.SmokingStatus,
            Tag.SpecialNeeds,
            Tag.StudyComments,
            Tag.StudyDescription,
            Tag.StudyIDIssuer,
            Tag.TelephoneNumberTrial,
            Tag.TextComments,
            Tag.TextString,
            Tag.TextValue, // Content Item Attribute
            Tag.Time, // Content Item Attribute
            Tag.TopicAuthor,
            Tag.TopicKeywords,
            Tag.TopicSubject,
            Tag.TopicTitle,
            Tag.VerbalSourceTrial,
            Tag.VerbalSourceIdentifierCodeSequenceTrial,
            Tag.VisitComments
    };

    private static final int[] X_INSTITUTION = {
            Tag.InstitutionAddress,
            Tag.InstitutionalDepartmentName,
            Tag.InstitutionalDepartmentTypeCodeSequence
    };

    private static final int[] X_DEVICE = {
            Tag.CassetteID,
            Tag.GantryID,
            Tag.GeneratorID,
            Tag.PerformedStationAETitle,
            Tag.PerformedStationGeographicLocationCodeSequence,
            Tag.PerformedStationName,
            Tag.PerformedStationNameCodeSequence,
            Tag.PlateID,
            Tag.ScheduledProcedureStepLocation,
            Tag.ScheduledStationAETitle,
            Tag.ScheduledStationGeographicLocationCodeSequence,
            Tag.ScheduledStationName,
            Tag.ScheduledStationNameCodeSequence,
            Tag.ScheduledStudyLocation,
            Tag.ScheduledStudyLocationAETitle,
            Tag.SourceSerialNumber,
    };

    private static final int[] X_DATES = {
            Tag.CurveDate,
            Tag.CurveTime,
            Tag.ExpectedCompletionDateTime,
            Tag.InstanceCoercionDateTime,
            Tag.InstanceCreationDate, // missing in Part 15
            Tag.InstanceCreationTime, // missing in Part 15
            Tag.LastMenstrualDate,
            Tag.ObservationDateTime,
            Tag.ObservationDateTrial,
            Tag.ObservationTimeTrial,
            Tag.OverlayDate,
            Tag.OverlayTime,
            Tag.PerformedProcedureStepEndDate,
            Tag.PerformedProcedureStepEndDateTime,
            Tag.PerformedProcedureStepEndTime,
            Tag.PerformedProcedureStepStartDate,
            Tag.PerformedProcedureStepStartDateTime,
            Tag.PerformedProcedureStepStartTime,
            Tag.ProcedureStepCancellationDateTime,
            Tag.ScheduledProcedureStepEndDate,
            Tag.ScheduledProcedureStepEndTime,
            Tag.ScheduledProcedureStepModificationDateTime,
            Tag.ScheduledProcedureStepStartDate,
            Tag.ScheduledProcedureStepStartDateTime,
            Tag.ScheduledProcedureStepStartTime,
            Tag.TimezoneOffsetFromUTC,
    };

    private static final int[] Z = {
            Tag.AccessionNumber,
            Tag.ConsultingPhysicianName,
            Tag.ContentCreatorName,
            Tag.FillerOrderNumberImagingServiceRequest,
            Tag.PatientID,
            Tag.PatientSexNeutered,
            Tag.PatientBirthDate,
            Tag.PatientName,
            Tag.PatientSex,
            Tag.PlacerOrderNumberImagingServiceRequest,
            Tag.ReferringPhysicianName,
            Tag.RequestedProcedureDescription,
            Tag.ReviewerName,
            Tag.StudyID,
            Tag.VerifyingObserverIdentificationCodeSequence,
    };

    private static final int[] Z_INSTITUTION = {
            Tag.InstitutionCodeSequence
    };

    private static final int[] Z_DATES = {
            Tag.AcquisitionDate,
            Tag.AcquisitionTime,
            Tag.AdmittingDate,
            Tag.AdmittingTime,
            Tag.SeriesDate,
            Tag.SeriesTime,
            Tag.StudyDate,
            Tag.StudyTime,
    };

    private static final int[] Z_UID = {
            Tag.ReferencedPerformedProcedureStepSequence,
            Tag.ReferencedStudySequence
    };

    private static final int[] D = {
            Tag.AcquisitionDeviceProcessingDescription,
            Tag.ContrastBolusAgent,
            Tag.DoseReferenceUID,
            Tag.OperatorsName,
            Tag.PersonName,
            Tag.ProtocolName,
            Tag.VerifyingObserverName,
            Tag.VerifyingOrganization
    };

    private static final int[] D_DEVICE = {
            Tag.DetectorID,
            Tag.DeviceSerialNumber,
            Tag.StationName,
    };

    private static final int[] D_INSTITUTION = {
            Tag.InstitutionName,
    };

    private static final int[] D_DATES = {
            Tag.AcquisitionDateTime,
            Tag.ContentDate,
            Tag.ContentTime,
            Tag.EndAcquisitionDateTime,
            Tag.StartAcquisitionDateTime,
            Tag.VerificationDateTime // missing in Part 15
    };

    private static final int[] U = {
            Tag.AffectedSOPInstanceUID,
            Tag.ConcatenationUID,
            Tag.DimensionOrganizationUID,
            Tag.FailedSOPInstanceUIDList,
            Tag.FiducialUID,
            Tag.FrameOfReferenceUID,
            Tag.InstanceCreatorUID,
            Tag.IrradiationEventUID,
            Tag.LargePaletteColorLookupTableUID,
            Tag.MediaStorageSOPInstanceUID,
            Tag.ObservationSubjectUIDTrial,
            Tag.ObservationUID,
            Tag.PaletteColorLookupTableUID,
            Tag.PresentationDisplayCollectionUID,
            Tag.PresentationSequenceCollectionUID,
            Tag.ReferencedFrameOfReferenceUID,
            Tag.ReferencedGeneralPurposeScheduledProcedureStepTransactionUID,
            Tag.ReferencedObservationUIDTrial,
            Tag.ReferencedSOPInstanceUID,
            Tag.ReferencedSOPInstanceUIDInFile,
            Tag.RelatedFrameOfReferenceUID,
            Tag.RequestedSOPInstanceUID,
            Tag.SeriesInstanceUID,
            Tag.SOPInstanceUID,
            Tag.StorageMediaFileSetUID,
            Tag.StudyInstanceUID,
            Tag.SynchronizationFrameOfReferenceUID,
            Tag.TargetUID,
            Tag.TemplateExtensionCreatorUID,
            Tag.TemplateExtensionOrganizationUID,
            Tag.TrackingUID,
            Tag.TransactionUID,
            Tag.UID,
    };

    private static final int[] U_DEVICE = {
            Tag.DeviceUID,
    };

    private static final String UNMODIFIED = "UNMODIFIED";
    private static final String REMOVED = "REMOVED";
    private static final String YES = "YES";

    private final EnumSet<Option> options;
    private final Attributes dummyValues = new Attributes();
    private int[] x = X;
    private int[] u = U;
    private int[] o;

    public enum Option {
        BasicApplicationConfidentialityProfile(DeIdentificationMethod.BasicApplicationConfidentialityProfile),
//        CleanPixelDataOption(DeIdentificationMethod.CleanPixelDataOption),
//        CleanRecognizableVisualFeaturesOption(DeIdentificationMethod.CleanRecognizableVisualFeaturesOption),
//        CleanGraphicsOption(DeIdentificationMethod.CleanGraphicsOption),
//        CleanStructuredContentOption(DeIdentificationMethod.CleanStructuredContentOption),
//        CleanDescriptorsOption(DeIdentificationMethod.CleanDescriptorsOption),
        RetainLongitudinalTemporalInformationFullDatesOption(
                DeIdentificationMethod.RetainLongitudinalTemporalInformationFullDatesOption),
//        RetainLongitudinalTemporalInformationModifiedDatesOption(
//                DeIdentificationMethod.RetainLongitudinalTemporalInformationModifiedDatesOption),
//        RetainPatientCharacteristicsOption(DeIdentificationMethod.RetainPatientCharacteristicsOption),
        RetainDeviceIdentityOption(DeIdentificationMethod.RetainDeviceIdentityOption),
        RetainInstitutionIdentityOption(DeIdentificationMethod.RetainInstitutionIdentityOption),
        RetainUIDsOption(DeIdentificationMethod.RetainUIDsOption),
//        RetainSafePrivateOption(DeIdentificationMethod.RetainSafePrivateOption),
        RetainPatientIDHashOption(DeIdentificationMethod.RetainPatientIDHashOption);

        private final Code code;

        Option(Code code) {
            this.code = code;
        }
    }

    public DeIdentifier(Option... options) {
        this.options = EnumSet.of(Option.BasicApplicationConfidentialityProfile, options);
        int[] z = Z;
        int[] d = D;
        if (!this.options.contains(Option.RetainDeviceIdentityOption)) {
            x = cat(x, X_DEVICE);
            d = cat(d, D_DEVICE);
            u = cat(u, U_DEVICE);
        }
        if (!this.options.contains(Option.RetainInstitutionIdentityOption)) {
            x = cat(x, X_INSTITUTION);
            z = cat(z, Z_INSTITUTION);
            d = cat(d, D_INSTITUTION);
        }
        if (!this.options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption)) {
            x = cat(x, X_DATES);
            z = cat(z, Z_DATES);
            d = cat(d, D_DATES);
        }
        if (!this.options.contains(Option.RetainUIDsOption)) {
            z = cat(z, Z_UID);
        }
        o = cat(z, d);
        Arrays.sort(x);
        Arrays.sort(u);
        Arrays.sort(o);
        initDummyValues(d);
    }

    public void setDummyValue(int tag, VR vr, String s) {
        dummyValues.setString(tag, vr, s);
    }

    public void deidentify(Attributes attrs) {
        IDWithIssuer pid = options.contains(Option.RetainPatientIDHashOption) ? IDWithIssuer.pidOf(attrs) : null;
        deidentifyItem(attrs);
        correct(attrs);
        if (pid != null) attrs.setString(Tag.PatientID, VR.LO, hash(pid));
        attrs.setString(Tag.PatientIdentityRemoved, VR.CS, YES);
        attrs.setString(Tag.LongitudinalTemporalInformationModified, VR.CS,
                options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption) ? UNMODIFIED : REMOVED);
        Sequence sq = attrs.ensureSequence(Tag.DeidentificationMethodCodeSequence, options.size());
        for (Option option : options) {
            sq.add(option.code.toItem());
        }
    }

    private static String hash(IDWithIssuer pid) {
        return UUID.nameUUIDFromBytes(pid.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }

    public String remapUID(String uid) {
        return options.contains(Option.RetainUIDsOption) ? uid : UIDUtils.remapUID(uid);
    }

    public boolean equalOptions(Option... options) {
        return EnumSet.of(Option.BasicApplicationConfidentialityProfile, options).equals(options);
    }

    private static int[] cat(int[] a, int[] b) {
        int[] dest = new int[a.length + b.length];
        System.arraycopy(a, 0, dest, 0, a.length);
        System.arraycopy(b, 0, dest, a.length, b.length);
        return dest;
    }

    private void initDummyValues(int[] d) {
        ElementDictionary dict = ElementDictionary.getStandardElementDictionary();
        for (int tag : d)
            initDummyValue(dict.vrOf(tag), tag);
        initDummyValue(VR.DA, Tag.SeriesDate);
        initDummyValue(VR.TM, Tag.SeriesTime);
    }

    private Object initDummyValue(VR vr, int tag) {
        return dummyValues.setString(tag, vr, dummyValueFor(vr));
    }

    private static String dummyValueFor(VR vr) {
        switch (vr) {
            case DA:
                return "19991111";
            case DT:
                return "19991111111111";
            case TM:
                return "111111";
            case IS:
            case DS:
                return "0";
        }
        return "REMOVED";
    }

    private void correct(Attributes attrs) {
        if (!options.contains(Option.RetainLongitudinalTemporalInformationFullDatesOption)
                && UID.PositronEmissionTomographyImageStorage.equals(attrs.getString(Tag.SOPClassUID))) {
            attrs.setString(Tag.SeriesDate, VR.DA, dummyValues.getString(Tag.SeriesDate));
            attrs.setString(Tag.SeriesTime, VR.TM, dummyValues.getString(Tag.SeriesTime));
        }
    }

    private void deidentifyItem(Attributes attrs) {
        attrs.removePrivateAttributes();
        attrs.removeCurveData();
        attrs.removeOverlayData();
        attrs.removeSelected(x);
        attrs.replaceSelected(dummyValues, o);
        if (!options.contains(Option.RetainUIDsOption))
            attrs.replaceUIDSelected(u);

        try {
            attrs.accept(new Attributes.Visitor() {
                @Override
                public boolean visit(Attributes attrs, int tag, VR vr, Object value) throws Exception {
                    if (value instanceof Sequence)
                        for (Attributes item : (Sequence) value)
                            deidentifyItem(item);
                    return true;
                }
            }, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
