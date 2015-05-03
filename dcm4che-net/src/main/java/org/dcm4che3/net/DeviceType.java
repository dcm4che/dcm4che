package org.dcm4che3.net;

/**
 * Represents a type of device.
 * This list is composed of code values
 * (0008,0100) for Context ID 30 in PS3.16
 *
 * @author Roman K
 */
public enum DeviceType {

    /**
     * Cardiac Electrophys
     */
    EPS,
    /**
     * Computed Radiography
     */
    CR,
    /**
     * Computed Tomography
     */
    CT,
    /**
     * Digital Radiography
     */
    DX,
    /**
     * Electrocardiography
     */
    ECG,
    /**
     * Endoscopy
     */
    ES,
    /**
     * External-camera Photography
     */
    XC,
    /**
     * General Microscopy
     */
    GM,
    /**
     * Hemodynamic Waveform
     */
    HD,
    /**
     * Intra-oral Radiography
     */
    IO,
    /**
     * Intravascular Ultrasound
     */
    IVUS,
    /**
     * Magnetic Resonance
     */
    MR,
    /**
     * Mammography
     */
    MG,
    /**
     * Nuclear Medicine
     */
    NM,
    /**
     * Ophthalmic Photography
     */
    OP,
    /**
     * Panoramic X-Ray
     */
    PX,
    /**
     * Positron emission tomography
     */
    PT,
    /**
     * Radiofluoroscopy
     */
    RF,
    /**
     * Radiographic imaging
     */
    RG,
    /**
     * Radiotherapy Image
     */
    RTIMAGE,
    /**
     * Slide Microscopy
     */
    SM,
    /**
     * Ultrasound
     */
    US,
    /**
     * X-Ray Angiog
     */
    XA,
    /**
     * Archive
     */
    ARCHIVE,
    /**
     * Computation Server
     */
    COMP,
    /**
     * Computer Assisted Detection/Diagnosis
     */
    CAD,
    /**
     * Department System Scheduler
     */
    DSS,
    /**
     * Film Digitizer
     */
    FILMD,
    /**
     * Media Creation Device
     */
    MCD,
    /**
     * Hard Copy Print Server
     */
    PRINT,
    /**
     * Image Capture
     */
    CAPTURE,
    /**
     * Procedure Logging
     */
    LOG,
    /**
     * Radiation Therapy Device
     */
    RT,
    /**
     * Workstation
     */
    WSD
}
