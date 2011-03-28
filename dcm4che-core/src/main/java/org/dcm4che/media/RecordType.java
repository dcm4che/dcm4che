package org.dcm4che.media;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;

public enum RecordType {
    PATIENT(Tag.PatientID, VR.UI),
    STUDY(Tag.StudyInstanceUID, VR.UI),
    SERIES(Tag.SeriesInstanceUID, VR.UI),
    IMAGE,
    OVERLAY,
    VOI_LUT,
    CURVE,
    STORED_PRINT,
    RT_DOSE,
    RT_STRUCTURE_SET,
    RT_PLAN,
    RT_TREAT_RECORD,
    PRESENTATION,
    WAVEFORM,
    SR_DOCUMENT,
    KEY_OBJECT_DOC,
    SPECTROSCOPY,
    RAW_DATA,
    REGISTRATION,
    FIDUCIAL,
    HANGING_PROTOCOL,
    ENCAP_DOC,
    HL7_STRUC_DOC,
    VALUE_MAP,
    STEREOMETRIC,
    PALETTE,
    SURFACE,
    MEASUREMENT,
    PLAN,
    STRUCT_DISPLAY,
    PRIVATE;

    private final int pkTag;
    private final VR pkVR;
    private final int pkTagInFile;

    RecordType() {
        this.pkTag = Tag.ReferencedSOPInstanceUIDInFile;
        this.pkVR = VR.UI;
        this.pkTagInFile = Tag.SOPInstanceUID;
    }

    RecordType(int pkTag, VR vr) {
        this.pkTagInFile = this.pkTag = pkTag;
        this.pkVR = vr;
    }

    public String code() {
        return name().replace('_', ' ');
    }

    public static RecordType forCode(String code) {
        try {
            return RecordType.valueOf(code.replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(code);
        }
    }

    public Attributes makePrimaryKey(Attributes attrs) {
        Attributes key = new Attributes(2);
        key.setString(Tag.DirectoryRecordType, VR.CS, code());
        key.setString(pkTag, pkVR, attrs.getString(pkTagInFile, null));
        return key;
    }
}
