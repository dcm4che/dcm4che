package org.dcm4che.media;

import java.util.Arrays;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;

public class RecordFactory {

    private static final int IN_USE = 0xffff;

    private static final int[] PATIENT_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.PatientName,
        Tag.PatientID,
    };

    private static final int[] STUDY_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.StudyDescription,
        Tag.StudyInstanceUID,
        Tag.StudyID,
    };

    private static final int[] SERIES_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.Modality,
        Tag.SeriesInstanceUID,
        Tag.SeriesNumber,
    };

    private static final int[] IMAGE_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.InstanceNumber,
    };

    private static final int[] RT_DOSE_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.InstanceNumber,
        Tag.DoseSummationType,
    };

    private static final int[] RT_STRUCTURE_SET_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.InstanceNumber,
        Tag.StructureSetLabel,
        Tag.StructureSetDate,
        Tag.StructureSetTime,
    };

    private static final int[] RT_PLAN_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.InstanceNumber,
        Tag.RTPlanLabel,
        Tag.RTPlanDate,
        Tag.RTPlanTime,
    };

    private static final int[] RT_TREAT_RECORD_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.InstanceNumber,
        Tag.TreatmentDate,
        Tag.TreatmentTime,
    };

    private static final int[] PRESENTATION_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ReferencedSeriesSequence,
        Tag.InstanceNumber,
        Tag.ContentLabel,
        Tag.ContentDescription,
        Tag.PresentationCreationDate,
        Tag.PresentationCreationTime,
        Tag.ContentCreatorName,
        Tag.BlendingSequence,
    };

    private static final int[] WAVEFORM_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.InstanceNumber
    };

    private static final int[] SR_DOCUMENT_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.InstanceNumber,
        Tag.VerificationDateTime,
        Tag.ConceptNameCodeSequence,
        Tag.CompletionFlag,
        Tag.VerificationFlag
    };

    private static final int[] KEY_OBJECT_DOC_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.InstanceNumber,
        Tag.ConceptNameCodeSequence
    };

    private static final int[] SPECTROSCOPY_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ImageType,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.ReferencedImageEvidenceSequence,
        Tag.InstanceNumber,
        Tag.NumberOfFrames,
        Tag.Rows,
        Tag.Columns,
        Tag.DataPointRows,
        Tag.DataPointColumns
    };

    private static final int[] RAW_DATA_KEYS = WAVEFORM_KEYS;

    private static final int[] REGISTRATION_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.InstanceNumber,
        Tag.ContentLabel,
        Tag.ContentDescription,
        Tag.ContentCreatorName,
    };

    private static final int[] FIDUCIAL_KEYS = REGISTRATION_KEYS;

    private static final int[] HANGING_PROTOCOL_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.HangingProtocolName,
        Tag.HangingProtocolDescription,
        Tag.HangingProtocolLevel,
        Tag.HangingProtocolCreator,
        Tag.HangingProtocolCreationDateTime,
        Tag.HangingProtocolDefinitionSequence,
        Tag.NumberOfPriorsReferenced,
        Tag.HangingProtocolUserIdentificationCodeSequence
    };

    private static final int[] ENCAP_DOC_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ContentDate,
        Tag.ContentTime,
        Tag.InstanceNumber,
        Tag.ConceptNameCodeSequence,
        Tag.DocumentTitle,
        Tag.MIMETypeOfEncapsulatedDocument
    };

    private static final int[] HL7_STRUC_DOC_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.HL7InstanceIdentifier,
        Tag.HL7DocumentEffectiveTime,
        Tag.HL7DocumentTypeCodeSequence,
        Tag.DocumentTitle
    };

    private static final int[] VALUE_MAP_KEYS = REGISTRATION_KEYS;

    private static final int[] STEREOMETRIC_KEYS = {
        Tag.SpecificCharacterSet,
    };

    private static final int[] PALETTE_KEYS = {
        Tag.SpecificCharacterSet,
        Tag.ContentLabel,
        Tag.ContentDescription,
    };

    private static final int[] PRIVATE_KEYS = STEREOMETRIC_KEYS;

    private final int[][] keysForRecordType = {
        PATIENT_KEYS,
        STUDY_KEYS,
        SERIES_KEYS,
        IMAGE_KEYS,
        RT_DOSE_KEYS,
        RT_STRUCTURE_SET_KEYS,
        RT_PLAN_KEYS,
        RT_TREAT_RECORD_KEYS,
        PRESENTATION_KEYS,
        WAVEFORM_KEYS,
        SR_DOCUMENT_KEYS,
        KEY_OBJECT_DOC_KEYS,
        SPECTROSCOPY_KEYS,
        RAW_DATA_KEYS,
        REGISTRATION_KEYS,
        FIDUCIAL_KEYS,
        HANGING_PROTOCOL_KEYS,
        ENCAP_DOC_KEYS,
        HL7_STRUC_DOC_KEYS,
        VALUE_MAP_KEYS,
        STEREOMETRIC_KEYS,
        PALETTE_KEYS,
        PRIVATE_KEYS
    };

    public void setRecordSelectionKeys(RecordType type, int[] keys) {
        int[] tmp = keys.clone();
        Arrays.sort(tmp);
        keysForRecordType[type.ordinal()] = tmp;
    }

    public Attributes createRecord(RecordType type, Attributes dataset,
            Attributes fmi, String[] fileIDs) {
        int[] keys = keysForRecordType[type.ordinal()];
        Attributes rec = new Attributes(
                keys.length + (fileIDs != null ? 8 : 4));
        rec.setInt(Tag.OffsetOfTheNextDirectoryRecord, VR.UL, 0);
        rec.setInt(Tag.RecordInUseFlag, VR.US, IN_USE);
        rec.setInt(Tag.OffsetOfReferencedLowerLevelDirectoryEntity, VR.UL, 0);
        rec.setString(Tag.DirectoryRecordType, VR.CS, type.code());
        if (fileIDs != null) {
            rec.setString(Tag.ReferencedFileID, VR.CS, fileIDs);
            rec.setString(Tag.ReferencedSOPClassUIDInFile, VR.UI,
                    fmi.getString(Tag.MediaStorageSOPClassUID, null));
            rec.setString(Tag.ReferencedSOPInstanceUIDInFile, VR.UI,
                    fmi.getString(Tag.MediaStorageSOPInstanceUID, null));
            rec.setString(Tag.ReferencedTransferSyntaxUIDInFile, VR.UI,
                    fmi.getString(Tag.TransferSyntaxUID, null));
        }
        rec.addSelected(dataset, keys);
        return rec ;
    }
}
