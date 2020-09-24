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
 * ***** END LICENSE BLOCK *****
 * This file is generated from Part 6 of the Standard Text Edition 2011.
 */
 
package org.dcm4che3.data;

import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class UID {

    private static final ResourceBundle rb = 
            ResourceBundle.getBundle("org.dcm4che3.data.UIDNames");

    public static String nameOf(String uid) {
        try {
            return rb.getString(uid);
        } catch (Exception e) {
            return "?";
        }
    }

    public static String forName(String keyword) {
        try {
            return (String) UID.class.getField(keyword).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException(keyword);
        }
    }


    /** Verification SOP Class, SOPClass */
    public static final String VerificationSOPClass = "1.2.840.10008.1.1";

    /** Implicit VR Little Endian, TransferSyntax */
    public static final String ImplicitVRLittleEndian = "1.2.840.10008.1.2";

    /** Explicit VR Little Endian, TransferSyntax */
    public static final String ExplicitVRLittleEndian = "1.2.840.10008.1.2.1";

    /** Deflated Explicit VR Little Endian, TransferSyntax */
    public static final String DeflatedExplicitVRLittleEndian = "1.2.840.10008.1.2.1.99";

    /** Explicit VR Big Endian (Retired), TransferSyntax */
    public static final String ExplicitVRBigEndianRetired = "1.2.840.10008.1.2.2";

    /** JPEG Baseline (Process 1), TransferSyntax */
    public static final String JPEGBaseline1 = "1.2.840.10008.1.2.4.50";

    /** JPEG Extended (Process 2 & 4), TransferSyntax */
    public static final String JPEGExtended24 = "1.2.840.10008.1.2.4.51";

    /** JPEG Extended (Process 3 & 5) (Retired), TransferSyntax */
    public static final String JPEGExtended35Retired = "1.2.840.10008.1.2.4.52";

    /** JPEG Spectral Selection, Non-Hierarchical (Process 6 & 8) (Retired), TransferSyntax */
    public static final String JPEGSpectralSelectionNonHierarchical68Retired = "1.2.840.10008.1.2.4.53";

    /** JPEG Spectral Selection, Non-Hierarchical (Process 7 & 9) (Retired), TransferSyntax */
    public static final String JPEGSpectralSelectionNonHierarchical79Retired = "1.2.840.10008.1.2.4.54";

    /** JPEG Full Progression, Non-Hierarchical (Process 10 & 12) (Retired), TransferSyntax */
    public static final String JPEGFullProgressionNonHierarchical1012Retired = "1.2.840.10008.1.2.4.55";

    /** JPEG Full Progression, Non-Hierarchical (Process 11 & 13) (Retired), TransferSyntax */
    public static final String JPEGFullProgressionNonHierarchical1113Retired = "1.2.840.10008.1.2.4.56";

    /** JPEG Lossless, Non-Hierarchical (Process 14), TransferSyntax */
    public static final String JPEGLosslessNonHierarchical14 = "1.2.840.10008.1.2.4.57";

    /** JPEG Lossless, Non-Hierarchical (Process 15) (Retired), TransferSyntax */
    public static final String JPEGLosslessNonHierarchical15Retired = "1.2.840.10008.1.2.4.58";

    /** JPEG Extended, Hierarchical (Process 16 & 18) (Retired), TransferSyntax */
    public static final String JPEGExtendedHierarchical1618Retired = "1.2.840.10008.1.2.4.59";

    /** JPEG Extended, Hierarchical (Process 17 & 19) (Retired), TransferSyntax */
    public static final String JPEGExtendedHierarchical1719Retired = "1.2.840.10008.1.2.4.60";

    /** JPEG Spectral Selection, Hierarchical (Process 20 & 22) (Retired), TransferSyntax */
    public static final String JPEGSpectralSelectionHierarchical2022Retired = "1.2.840.10008.1.2.4.61";

    /** JPEG Spectral Selection, Hierarchical (Process 21 & 23) (Retired), TransferSyntax */
    public static final String JPEGSpectralSelectionHierarchical2123Retired = "1.2.840.10008.1.2.4.62";

    /** JPEG Full Progression, Hierarchical (Process 24 & 26) (Retired), TransferSyntax */
    public static final String JPEGFullProgressionHierarchical2426Retired = "1.2.840.10008.1.2.4.63";

    /** JPEG Full Progression, Hierarchical (Process 25 & 27) (Retired), TransferSyntax */
    public static final String JPEGFullProgressionHierarchical2527Retired = "1.2.840.10008.1.2.4.64";

    /** JPEG Lossless, Hierarchical (Process 28) (Retired), TransferSyntax */
    public static final String JPEGLosslessHierarchical28Retired = "1.2.840.10008.1.2.4.65";

    /** JPEG Lossless, Hierarchical (Process 29) (Retired), TransferSyntax */
    public static final String JPEGLosslessHierarchical29Retired = "1.2.840.10008.1.2.4.66";

    /** JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]), TransferSyntax */
    public static final String JPEGLossless = "1.2.840.10008.1.2.4.70";

    /** JPEG-LS Lossless Image Compression, TransferSyntax */
    public static final String JPEGLSLossless = "1.2.840.10008.1.2.4.80";

    /** JPEG-LS Lossy (Near-Lossless) Image Compression, TransferSyntax */
    public static final String JPEGLSLossyNearLossless = "1.2.840.10008.1.2.4.81";

    /** JPEG 2000 Image Compression (Lossless Only), TransferSyntax */
    public static final String JPEG2000LosslessOnly = "1.2.840.10008.1.2.4.90";

    /** JPEG 2000 Image Compression, TransferSyntax */
    public static final String JPEG2000 = "1.2.840.10008.1.2.4.91";

    /** JPEG 2000 Part 2 Multi-component Image Compression (Lossless Only), TransferSyntax */
    public static final String JPEG2000Part2MultiComponentLosslessOnly = "1.2.840.10008.1.2.4.92";

    /** JPEG 2000 Part 2 Multi-component Image Compression, TransferSyntax */
    public static final String JPEG2000Part2MultiComponent = "1.2.840.10008.1.2.4.93";

    /** JPIP Referenced, TransferSyntax */
    public static final String JPIPReferenced = "1.2.840.10008.1.2.4.94";

    /** JPIP Referenced Deflate, TransferSyntax */
    public static final String JPIPReferencedDeflate = "1.2.840.10008.1.2.4.95";

    /** MPEG2 Main Profile / Main Level, TransferSyntax */
    public static final String MPEG2 = "1.2.840.10008.1.2.4.100";

    /** MPEG2 Main Profile / High Level, TransferSyntax */
    public static final String MPEG2MainProfileHighLevel = "1.2.840.10008.1.2.4.101";

    /** MPEG-4 AVC/H.264 High Profile / Level 4.1, TransferSyntax */
    public static final String MPEG4AVCH264HighProfileLevel41 = "1.2.840.10008.1.2.4.102";

    /** MPEG-4 AVC/H.264 BD-compatible High Profile / Level 4.1, TransferSyntax */
    public static final String MPEG4AVCH264BDCompatibleHighProfileLevel41 = "1.2.840.10008.1.2.4.103";

    /** MPEG-4 AVC/H.264 High Profile / Level 4.2 For 2D Video, TransferSyntax */
    public static final String MPEG4AVCH264HighProfileLevel42For2DVideo = "1.2.840.10008.1.2.4.104";

    /** MPEG-4 AVC/H.264 High Profile / Level 4.2 For 3D Video, TransferSyntax */
    public static final String MPEG4AVCH264HighProfileLevel42For3DVideo = "1.2.840.10008.1.2.4.105";

    /** MPEG-4 AVC/H.264 Stereo High Profile / Level 4.2, TransferSyntax */
    public static final String MPEG4AVCH264StereoHighProfileLevel42 = "1.2.840.10008.1.2.4.106";

    /** HEVC/H.265 Main Profile / Level 5.1, TransferSyntax */
    public static final String HEVCH265MainProfileLevel51 = "1.2.840.10008.1.2.4.107";

    /** HEVC/H.265 Main 10 Profile / Level 5.1, TransferSyntax */
    public static final String HEVCH265Main10ProfileLevel51 = "1.2.840.10008.1.2.4.108";

    /** RLE Lossless, TransferSyntax */
    public static final String RLELossless = "1.2.840.10008.1.2.5";

    /** RFC 2557 MIME encapsulation (Retired), TransferSyntax */
    public static final String RFC2557MIMEEncapsulationRetired = "1.2.840.10008.1.2.6.1";

    /** XML Encoding (Retired), TransferSyntax */
    public static final String XMLEncodingRetired = "1.2.840.10008.1.2.6.2";

    /** SMPTE ST 2110-20 Uncompressed Progressive Active Video, TransferSyntax */
    public static final String SMPTEST211020UncompressedProgressiveActiveVideo = "1.2.840.10008.1.2.7.1";

    /** SMPTE ST 2110-20 Uncompressed Interlaced Active Video, TransferSyntax */
    public static final String SMPTEST211020UncompressedInterlacedActiveVideo = "1.2.840.10008.1.2.7.2";

    /** SMPTE ST 2110-30 PCM Digital Audio, TransferSyntax */
    public static final String SMPTEST211030PCMDigitalAudio = "1.2.840.10008.1.2.7.3";

    /** Media Storage Directory Storage, SOPClass */
    public static final String MediaStorageDirectoryStorage = "1.2.840.10008.1.3.10";

    /** Talairach Brain Atlas Frame of Reference, WellKnownFrameOfReference */
    public static final String TalairachBrainAtlasFrameOfReference = "1.2.840.10008.1.4.1.1";

    /** SPM2 T1 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2T1FrameOfReference = "1.2.840.10008.1.4.1.2";

    /** SPM2 T2 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2T2FrameOfReference = "1.2.840.10008.1.4.1.3";

    /** SPM2 PD Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2PDFrameOfReference = "1.2.840.10008.1.4.1.4";

    /** SPM2 EPI Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2EPIFrameOfReference = "1.2.840.10008.1.4.1.5";

    /** SPM2 FIL T1 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2FILT1FrameOfReference = "1.2.840.10008.1.4.1.6";

    /** SPM2 PET Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2PETFrameOfReference = "1.2.840.10008.1.4.1.7";

    /** SPM2 TRANSM Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2TRANSMFrameOfReference = "1.2.840.10008.1.4.1.8";

    /** SPM2 SPECT Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2SPECTFrameOfReference = "1.2.840.10008.1.4.1.9";

    /** SPM2 GRAY Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2GRAYFrameOfReference = "1.2.840.10008.1.4.1.10";

    /** SPM2 WHITE Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2WHITEFrameOfReference = "1.2.840.10008.1.4.1.11";

    /** SPM2 CSF Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2CSFFrameOfReference = "1.2.840.10008.1.4.1.12";

    /** SPM2 BRAINMASK Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2BRAINMASKFrameOfReference = "1.2.840.10008.1.4.1.13";

    /** SPM2 AVG305T1 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2AVG305T1FrameOfReference = "1.2.840.10008.1.4.1.14";

    /** SPM2 AVG152T1 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2AVG152T1FrameOfReference = "1.2.840.10008.1.4.1.15";

    /** SPM2 AVG152T2 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2AVG152T2FrameOfReference = "1.2.840.10008.1.4.1.16";

    /** SPM2 AVG152PD Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2AVG152PDFrameOfReference = "1.2.840.10008.1.4.1.17";

    /** SPM2 SINGLESUBJT1 Frame of Reference, WellKnownFrameOfReference */
    public static final String SPM2SINGLESUBJT1FrameOfReference = "1.2.840.10008.1.4.1.18";

    /** ICBM 452 T1 Frame of Reference, WellKnownFrameOfReference */
    public static final String ICBM452T1FrameOfReference = "1.2.840.10008.1.4.2.1";

    /** ICBM Single Subject MRI Frame of Reference, WellKnownFrameOfReference */
    public static final String ICBMSingleSubjectMRIFrameOfReference = "1.2.840.10008.1.4.2.2";

    /** IEC 61217 Fixed Coordinate System Frame of Reference, WellKnownFrameOfReference */
    public static final String IEC61217FixedCoordinateSystemFrameOfReference = "1.2.840.10008.1.4.3.1";

    /** Standard Robotic-Arm Coordinate System Frame of Reference, WellKnownFrameOfReference */
    public static final String StandardRoboticArmCoordinateSystemFrameOfReference = "1.2.840.10008.1.4.3.2";

    /** Hot Iron Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String HotIronColorPaletteSOPInstance = "1.2.840.10008.1.5.1";

    /** PET Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String PETColorPaletteSOPInstance = "1.2.840.10008.1.5.2";

    /** Hot Metal Blue Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String HotMetalBlueColorPaletteSOPInstance = "1.2.840.10008.1.5.3";

    /** PET 20 Step Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String PET20StepColorPaletteSOPInstance = "1.2.840.10008.1.5.4";

    /** Spring Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String SpringColorPaletteSOPInstance = "1.2.840.10008.1.5.5";

    /** Summer Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String SummerColorPaletteSOPInstance = "1.2.840.10008.1.5.6";

    /** Fall Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String FallColorPaletteSOPInstance = "1.2.840.10008.1.5.7";

    /** Winter Color Palette SOP Instance, WellKnownSOPInstance */
    public static final String WinterColorPaletteSOPInstance = "1.2.840.10008.1.5.8";

    /** Basic Study Content Notification SOP Class (Retired), SOPClass */
    public static final String BasicStudyContentNotificationSOPClassRetired = "1.2.840.10008.1.9";

    /** Papyrus 3 Implicit VR Little Endian (Retired), TransferSyntax */
    public static final String Papyrus3ImplicitVRLittleEndianRetired = "1.2.840.10008.1.20";

    /** Storage Commitment Push Model SOP Class, SOPClass */
    public static final String StorageCommitmentPushModelSOPClass = "1.2.840.10008.1.20.1";

    /** Storage Commitment Push Model SOP Instance, WellKnownSOPInstance */
    public static final String StorageCommitmentPushModelSOPInstance = "1.2.840.10008.1.20.1.1";

    /** Storage Commitment Pull Model SOP Class (Retired), SOPClass */
    public static final String StorageCommitmentPullModelSOPClassRetired = "1.2.840.10008.1.20.2";

    /** Storage Commitment Pull Model SOP Instance (Retired), WellKnownSOPInstance */
    public static final String StorageCommitmentPullModelSOPInstanceRetired = "1.2.840.10008.1.20.2.1";

    /** Procedural Event Logging SOP Class, SOPClass */
    public static final String ProceduralEventLoggingSOPClass = "1.2.840.10008.1.40";

    /** Procedural Event Logging SOP Instance, WellKnownSOPInstance */
    public static final String ProceduralEventLoggingSOPInstance = "1.2.840.10008.1.40.1";

    /** Substance Administration Logging SOP Class, SOPClass */
    public static final String SubstanceAdministrationLoggingSOPClass = "1.2.840.10008.1.42";

    /** Substance Administration Logging SOP Instance, WellKnownSOPInstance */
    public static final String SubstanceAdministrationLoggingSOPInstance = "1.2.840.10008.1.42.1";

    /** DICOM UID Registry, DICOMUIDsAsACodingScheme */
    public static final String DICOMUIDRegistry = "1.2.840.10008.2.6.1";

    /** DICOM Controlled Terminology, CodingScheme */
    public static final String DICOMControlledTerminology = "1.2.840.10008.2.16.4";

    /** Adult Mouse Anatomy Ontology, CodingScheme */
    public static final String AdultMouseAnatomyOntology = "1.2.840.10008.2.16.5";

    /** Uberon Ontology, CodingScheme */
    public static final String UberonOntology = "1.2.840.10008.2.16.6";

    /** Integrated Taxonomic Information System (ITIS) Taxonomic Serial Number (TSN), CodingScheme */
    public static final String IntegratedTaxonomicInformationSystemITISTaxonomicSerialNumberTSN = "1.2.840.10008.2.16.7";

    /** Mouse Genome Initiative (MGI), CodingScheme */
    public static final String MouseGenomeInitiativeMGI = "1.2.840.10008.2.16.8";

    /** PubChem Compound CID, CodingScheme */
    public static final String PubChemCompoundCID = "1.2.840.10008.2.16.9";

    /** ICD-11, CodingScheme */
    public static final String ICD11 = "1.2.840.10008.2.16.10";

    /** New York University Melanoma Clinical Cooperative Group, CodingScheme */
    public static final String NewYorkUniversityMelanomaClinicalCooperativeGroup = "1.2.840.10008.2.16.11";

    /** Mayo Clinic Non-radiological Images Specific Body Structure Anatomical Surface Region Guide, CodingScheme */
    public static final String MayoClinicNonRadiologicalImagesSpecificBodyStructureAnatomicalSurfaceRegionGuide = "1.2.840.10008.2.16.12";

    /** Image Biomarker Standardisation Initiative, CodingScheme */
    public static final String ImageBiomarkerStandardisationInitiative = "1.2.840.10008.2.16.13";

    /** Radiomics Ontology, CodingScheme */
    public static final String RadiomicsOntology = "1.2.840.10008.2.16.14";

    /** RadElement, CodingScheme */
    public static final String RadElement = "1.2.840.10008.2.16.15";

    /** DICOM Application Context Name, ApplicationContextName */
    public static final String DICOMApplicationContextName = "1.2.840.10008.3.1.1.1";

    /** Detached Patient Management SOP Class (Retired), SOPClass */
    public static final String DetachedPatientManagementSOPClassRetired = "1.2.840.10008.3.1.2.1.1";

    /** Detached Patient Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String DetachedPatientManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.1.4";

    /** Detached Visit Management SOP Class (Retired), SOPClass */
    public static final String DetachedVisitManagementSOPClassRetired = "1.2.840.10008.3.1.2.2.1";

    /** Detached Study Management SOP Class (Retired), SOPClass */
    public static final String DetachedStudyManagementSOPClassRetired = "1.2.840.10008.3.1.2.3.1";

    /** Study Component Management SOP Class (Retired), SOPClass */
    public static final String StudyComponentManagementSOPClassRetired = "1.2.840.10008.3.1.2.3.2";

    /** Modality Performed Procedure Step SOP Class, SOPClass */
    public static final String ModalityPerformedProcedureStepSOPClass = "1.2.840.10008.3.1.2.3.3";

    /** Modality Performed Procedure Step Retrieve SOP Class, SOPClass */
    public static final String ModalityPerformedProcedureStepRetrieveSOPClass = "1.2.840.10008.3.1.2.3.4";

    /** Modality Performed Procedure Step Notification SOP Class, SOPClass */
    public static final String ModalityPerformedProcedureStepNotificationSOPClass = "1.2.840.10008.3.1.2.3.5";

    /** Detached Results Management SOP Class (Retired), SOPClass */
    public static final String DetachedResultsManagementSOPClassRetired = "1.2.840.10008.3.1.2.5.1";

    /** Detached Results Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String DetachedResultsManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.5.4";

    /** Detached Study Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String DetachedStudyManagementMetaSOPClassRetired = "1.2.840.10008.3.1.2.5.5";

    /** Detached Interpretation Management SOP Class (Retired), SOPClass */
    public static final String DetachedInterpretationManagementSOPClassRetired = "1.2.840.10008.3.1.2.6.1";

    /** Storage Service Class, ServiceClass */
    public static final String StorageServiceClass = "1.2.840.10008.4.2";

    /** Basic Film Session SOP Class, SOPClass */
    public static final String BasicFilmSessionSOPClass = "1.2.840.10008.5.1.1.1";

    /** Basic Film Box SOP Class, SOPClass */
    public static final String BasicFilmBoxSOPClass = "1.2.840.10008.5.1.1.2";

    /** Basic Grayscale Image Box SOP Class, SOPClass */
    public static final String BasicGrayscaleImageBoxSOPClass = "1.2.840.10008.5.1.1.4";

    /** Basic Color Image Box SOP Class, SOPClass */
    public static final String BasicColorImageBoxSOPClass = "1.2.840.10008.5.1.1.4.1";

    /** Referenced Image Box SOP Class (Retired), SOPClass */
    public static final String ReferencedImageBoxSOPClassRetired = "1.2.840.10008.5.1.1.4.2";

    /** Basic Grayscale Print Management Meta SOP Class, MetaSOPClass */
    public static final String BasicGrayscalePrintManagementMetaSOPClass = "1.2.840.10008.5.1.1.9";

    /** Referenced Grayscale Print Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String ReferencedGrayscalePrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.9.1";

    /** Print Job SOP Class, SOPClass */
    public static final String PrintJobSOPClass = "1.2.840.10008.5.1.1.14";

    /** Basic Annotation Box SOP Class, SOPClass */
    public static final String BasicAnnotationBoxSOPClass = "1.2.840.10008.5.1.1.15";

    /** Printer SOP Class, SOPClass */
    public static final String PrinterSOPClass = "1.2.840.10008.5.1.1.16";

    /** Printer Configuration Retrieval SOP Class, SOPClass */
    public static final String PrinterConfigurationRetrievalSOPClass = "1.2.840.10008.5.1.1.16.376";

    /** Printer SOP Instance, WellKnownPrinterSOPInstance */
    public static final String PrinterSOPInstance = "1.2.840.10008.5.1.1.17";

    /** Printer Configuration Retrieval SOP Instance, WellKnownPrinterSOPInstance */
    public static final String PrinterConfigurationRetrievalSOPInstance = "1.2.840.10008.5.1.1.17.376";

    /** Basic Color Print Management Meta SOP Class, MetaSOPClass */
    public static final String BasicColorPrintManagementMetaSOPClass = "1.2.840.10008.5.1.1.18";

    /** Referenced Color Print Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String ReferencedColorPrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.18.1";

    /** VOI LUT Box SOP Class, SOPClass */
    public static final String VOILUTBoxSOPClass = "1.2.840.10008.5.1.1.22";

    /** Presentation LUT SOP Class, SOPClass */
    public static final String PresentationLUTSOPClass = "1.2.840.10008.5.1.1.23";

    /** Image Overlay Box SOP Class (Retired), SOPClass */
    public static final String ImageOverlayBoxSOPClassRetired = "1.2.840.10008.5.1.1.24";

    /** Basic Print Image Overlay Box SOP Class (Retired), SOPClass */
    public static final String BasicPrintImageOverlayBoxSOPClassRetired = "1.2.840.10008.5.1.1.24.1";

    /** Print Queue SOP Instance (Retired), WellKnownPrintQueueSOPInstance */
    public static final String PrintQueueSOPInstanceRetired = "1.2.840.10008.5.1.1.25";

    /** Print Queue Management SOP Class (Retired), SOPClass */
    public static final String PrintQueueManagementSOPClassRetired = "1.2.840.10008.5.1.1.26";

    /** Stored Print Storage SOP Class (Retired), SOPClass */
    public static final String StoredPrintStorageSOPClassRetired = "1.2.840.10008.5.1.1.27";

    /** Hardcopy Grayscale Image Storage SOP Class (Retired), SOPClass */
    public static final String HardcopyGrayscaleImageStorageSOPClassRetired = "1.2.840.10008.5.1.1.29";

    /** Hardcopy Color Image Storage SOP Class (Retired), SOPClass */
    public static final String HardcopyColorImageStorageSOPClassRetired = "1.2.840.10008.5.1.1.30";

    /** Pull Print Request SOP Class (Retired), SOPClass */
    public static final String PullPrintRequestSOPClassRetired = "1.2.840.10008.5.1.1.31";

    /** Pull Stored Print Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String PullStoredPrintManagementMetaSOPClassRetired = "1.2.840.10008.5.1.1.32";

    /** Media Creation Management SOP Class UID, SOPClass */
    public static final String MediaCreationManagementSOPClassUID = "1.2.840.10008.5.1.1.33";

    /** Display System SOP Class, SOPClass */
    public static final String DisplaySystemSOPClass = "1.2.840.10008.5.1.1.40";

    /** Display System SOP Instance, WellKnownSOPInstance */
    public static final String DisplaySystemSOPInstance = "1.2.840.10008.5.1.1.40.1";

    /** Computed Radiography Image Storage, SOPClass */
    public static final String ComputedRadiographyImageStorage = "1.2.840.10008.5.1.4.1.1.1";

    /** Digital X-Ray Image Storage - For Presentation, SOPClass */
    public static final String DigitalXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.1";

    /** Digital X-Ray Image Storage - For Processing, SOPClass */
    public static final String DigitalXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.1.1";

    /** Digital Mammography X-Ray Image Storage - For Presentation, SOPClass */
    public static final String DigitalMammographyXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.2";

    /** Digital Mammography X-Ray Image Storage - For Processing, SOPClass */
    public static final String DigitalMammographyXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.2.1";

    /** Digital Intra-Oral X-Ray Image Storage - For Presentation, SOPClass */
    public static final String DigitalIntraOralXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.1.3";

    /** Digital Intra-Oral X-Ray Image Storage - For Processing, SOPClass */
    public static final String DigitalIntraOralXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.1.3.1";

    /** CT Image Storage, SOPClass */
    public static final String CTImageStorage = "1.2.840.10008.5.1.4.1.1.2";

    /** Enhanced CT Image Storage, SOPClass */
    public static final String EnhancedCTImageStorage = "1.2.840.10008.5.1.4.1.1.2.1";

    /** Legacy Converted Enhanced CT Image Storage, SOPClass */
    public static final String LegacyConvertedEnhancedCTImageStorage = "1.2.840.10008.5.1.4.1.1.2.2";

    /** Ultrasound Multi-frame Image Storage (Retired), SOPClass */
    public static final String UltrasoundMultiFrameImageStorageRetired = "1.2.840.10008.5.1.4.1.1.3";

    /** Ultrasound Multi-frame Image Storage, SOPClass */
    public static final String UltrasoundMultiFrameImageStorage = "1.2.840.10008.5.1.4.1.1.3.1";

    /** MR Image Storage, SOPClass */
    public static final String MRImageStorage = "1.2.840.10008.5.1.4.1.1.4";

    /** Enhanced MR Image Storage, SOPClass */
    public static final String EnhancedMRImageStorage = "1.2.840.10008.5.1.4.1.1.4.1";

    /** MR Spectroscopy Storage, SOPClass */
    public static final String MRSpectroscopyStorage = "1.2.840.10008.5.1.4.1.1.4.2";

    /** Enhanced MR Color Image Storage, SOPClass */
    public static final String EnhancedMRColorImageStorage = "1.2.840.10008.5.1.4.1.1.4.3";

    /** Legacy Converted Enhanced MR Image Storage, SOPClass */
    public static final String LegacyConvertedEnhancedMRImageStorage = "1.2.840.10008.5.1.4.1.1.4.4";

    /** Nuclear Medicine Image Storage (Retired), SOPClass */
    public static final String NuclearMedicineImageStorageRetired = "1.2.840.10008.5.1.4.1.1.5";

    /** Ultrasound Image Storage (Retired), SOPClass */
    public static final String UltrasoundImageStorageRetired = "1.2.840.10008.5.1.4.1.1.6";

    /** Ultrasound Image Storage, SOPClass */
    public static final String UltrasoundImageStorage = "1.2.840.10008.5.1.4.1.1.6.1";

    /** Enhanced US Volume Storage, SOPClass */
    public static final String EnhancedUSVolumeStorage = "1.2.840.10008.5.1.4.1.1.6.2";

    /** Secondary Capture Image Storage, SOPClass */
    public static final String SecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7";

    /** Multi-frame Single Bit Secondary Capture Image Storage, SOPClass */
    public static final String MultiFrameSingleBitSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.1";

    /** Multi-frame Grayscale Byte Secondary Capture Image Storage, SOPClass */
    public static final String MultiFrameGrayscaleByteSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.2";

    /** Multi-frame Grayscale Word Secondary Capture Image Storage, SOPClass */
    public static final String MultiFrameGrayscaleWordSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.3";

    /** Multi-frame True Color Secondary Capture Image Storage, SOPClass */
    public static final String MultiFrameTrueColorSecondaryCaptureImageStorage = "1.2.840.10008.5.1.4.1.1.7.4";

    /** Standalone Overlay Storage (Retired), SOPClass */
    public static final String StandaloneOverlayStorageRetired = "1.2.840.10008.5.1.4.1.1.8";

    /** Standalone Curve Storage (Retired), SOPClass */
    public static final String StandaloneCurveStorageRetired = "1.2.840.10008.5.1.4.1.1.9";

    /** Waveform Storage - Trial (Retired), SOPClass */
    public static final String WaveformStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.9.1";

    /** 12-lead ECG Waveform Storage, SOPClass */
    public static final String TwelveLeadECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.1";

    /** General ECG Waveform Storage, SOPClass */
    public static final String GeneralECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.2";

    /** Ambulatory ECG Waveform Storage, SOPClass */
    public static final String AmbulatoryECGWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.1.3";

    /** Hemodynamic Waveform Storage, SOPClass */
    public static final String HemodynamicWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.2.1";

    /** Cardiac Electrophysiology Waveform Storage, SOPClass */
    public static final String CardiacElectrophysiologyWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.3.1";

    /** Basic Voice Audio Waveform Storage, SOPClass */
    public static final String BasicVoiceAudioWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.4.1";

    /** General Audio Waveform Storage, SOPClass */
    public static final String GeneralAudioWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.4.2";

    /** Arterial Pulse Waveform Storage, SOPClass */
    public static final String ArterialPulseWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.5.1";

    /** Respiratory Waveform Storage, SOPClass */
    public static final String RespiratoryWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.6.1";

    /** Multi-channel Respiratory Waveform Storage, SOPClass */
    public static final String MultiChannelRespiratoryWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.6.2";

    /** Routine Scalp Electroencephalogram Waveform Storage, SOPClass */
    public static final String RoutineScalpElectroencephalogramWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.7.1";

    /** Electromyogram Waveform Storage, SOPClass */
    public static final String ElectromyogramWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.7.2";

    /** Electrooculogram Waveform Storage, SOPClass */
    public static final String ElectrooculogramWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.7.3";

    /** Sleep Electroencephalogram Waveform Storage, SOPClass */
    public static final String SleepElectroencephalogramWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.7.4";

    /** Body Position Waveform Storage, SOPClass */
    public static final String BodyPositionWaveformStorage = "1.2.840.10008.5.1.4.1.1.9.8.1";

    /** Standalone Modality LUT Storage (Retired), SOPClass */
    public static final String StandaloneModalityLUTStorageRetired = "1.2.840.10008.5.1.4.1.1.10";

    /** Standalone VOI LUT Storage (Retired), SOPClass */
    public static final String StandaloneVOILUTStorageRetired = "1.2.840.10008.5.1.4.1.1.11";

    /** Grayscale Softcopy Presentation State Storage, SOPClass */
    public static final String GrayscaleSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.1";

    /** Color Softcopy Presentation State Storage, SOPClass */
    public static final String ColorSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.2";

    /** Pseudo-Color Softcopy Presentation State Storage, SOPClass */
    public static final String PseudoColorSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.3";

    /** Blending Softcopy Presentation State Storage, SOPClass */
    public static final String BlendingSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.4";

    /** XA/XRF Grayscale Softcopy Presentation State Storage, SOPClass */
    public static final String XAXRFGrayscaleSoftcopyPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.5";

    /** Grayscale Planar MPR Volumetric Presentation State Storage, SOPClass */
    public static final String GrayscalePlanarMPRVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.6";

    /** Compositing Planar MPR Volumetric Presentation State Storage, SOPClass */
    public static final String CompositingPlanarMPRVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.7";

    /** Advanced Blending Presentation State Storage, SOPClass */
    public static final String AdvancedBlendingPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.8";

    /** Volume Rendering Volumetric Presentation State Storage, SOPClass */
    public static final String VolumeRenderingVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.9";

    /** Segmented Volume Rendering Volumetric Presentation State Storage, SOPClass */
    public static final String SegmentedVolumeRenderingVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.10";

    /** Multiple Volume Rendering Volumetric Presentation State Storage, SOPClass */
    public static final String MultipleVolumeRenderingVolumetricPresentationStateStorage = "1.2.840.10008.5.1.4.1.1.11.11";

    /** X-Ray Angiographic Image Storage, SOPClass */
    public static final String XRayAngiographicImageStorage = "1.2.840.10008.5.1.4.1.1.12.1";

    /** Enhanced XA Image Storage, SOPClass */
    public static final String EnhancedXAImageStorage = "1.2.840.10008.5.1.4.1.1.12.1.1";

    /** X-Ray Radiofluoroscopic Image Storage, SOPClass */
    public static final String XRayRadiofluoroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.12.2";

    /** Enhanced XRF Image Storage, SOPClass */
    public static final String EnhancedXRFImageStorage = "1.2.840.10008.5.1.4.1.1.12.2.1";

    /** X-Ray Angiographic Bi-Plane Image Storage (Retired), SOPClass */
    public static final String XRayAngiographicBiPlaneImageStorageRetired = "1.2.840.10008.5.1.4.1.1.12.3";

    /** Zeiss OPT File (Retired), SOPClass */
    public static final String ZeissOPTFileRetired = "1.2.840.10008.5.1.4.1.1.12.77";

    /** X-Ray 3D Angiographic Image Storage, SOPClass */
    public static final String XRay3DAngiographicImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.1";

    /** X-Ray 3D Craniofacial Image Storage, SOPClass */
    public static final String XRay3DCraniofacialImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.2";

    /** Breast Tomosynthesis Image Storage, SOPClass */
    public static final String BreastTomosynthesisImageStorage = "1.2.840.10008.5.1.4.1.1.13.1.3";

    /** Breast Projection X-Ray Image Storage - For Presentation, SOPClass */
    public static final String BreastProjectionXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.13.1.4";

    /** Breast Projection X-Ray Image Storage - For Processing, SOPClass */
    public static final String BreastProjectionXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.13.1.5";

    /** Intravascular Optical Coherence Tomography Image Storage - For Presentation, SOPClass */
    public static final String IntravascularOpticalCoherenceTomographyImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.14.1";

    /** Intravascular Optical Coherence Tomography Image Storage - For Processing, SOPClass */
    public static final String IntravascularOpticalCoherenceTomographyImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.14.2";

    /** Nuclear Medicine Image Storage, SOPClass */
    public static final String NuclearMedicineImageStorage = "1.2.840.10008.5.1.4.1.1.20";

    /** Parametric Map Storage, SOPClass */
    public static final String ParametricMapStorage = "1.2.840.10008.5.1.4.1.1.30";

    /** MR Image Storage Zero Padded (Retired), SOPClass */
    public static final String MRImageStorageZeroPaddedRetired = "1.2.840.10008.5.1.4.1.1.40";

    /** Raw Data Storage, SOPClass */
    public static final String RawDataStorage = "1.2.840.10008.5.1.4.1.1.66";

    /** Spatial Registration Storage, SOPClass */
    public static final String SpatialRegistrationStorage = "1.2.840.10008.5.1.4.1.1.66.1";

    /** Spatial Fiducials Storage, SOPClass */
    public static final String SpatialFiducialsStorage = "1.2.840.10008.5.1.4.1.1.66.2";

    /** Deformable Spatial Registration Storage, SOPClass */
    public static final String DeformableSpatialRegistrationStorage = "1.2.840.10008.5.1.4.1.1.66.3";

    /** Segmentation Storage, SOPClass */
    public static final String SegmentationStorage = "1.2.840.10008.5.1.4.1.1.66.4";

    /** Surface Segmentation Storage, SOPClass */
    public static final String SurfaceSegmentationStorage = "1.2.840.10008.5.1.4.1.1.66.5";

    /** Tractography Results Storage, SOPClass */
    public static final String TractographyResultsStorage = "1.2.840.10008.5.1.4.1.1.66.6";

    /** Real World Value Mapping Storage, SOPClass */
    public static final String RealWorldValueMappingStorage = "1.2.840.10008.5.1.4.1.1.67";

    /** Surface Scan Mesh Storage, SOPClass */
    public static final String SurfaceScanMeshStorage = "1.2.840.10008.5.1.4.1.1.68.1";

    /** Surface Scan Point Cloud Storage, SOPClass */
    public static final String SurfaceScanPointCloudStorage = "1.2.840.10008.5.1.4.1.1.68.2";

    /** VL Image Storage - Trial (Retired), SOPClass */
    public static final String VLImageStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.77.1";

    /** VL Multi-frame Image Storage - Trial (Retired), SOPClass */
    public static final String VLMultiFrameImageStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.77.2";

    /** VL Endoscopic Image Storage, SOPClass */
    public static final String VLEndoscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.1";

    /** Video Endoscopic Image Storage, SOPClass */
    public static final String VideoEndoscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.1.1";

    /** VL Microscopic Image Storage, SOPClass */
    public static final String VLMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.2";

    /** Video Microscopic Image Storage, SOPClass */
    public static final String VideoMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.2.1";

    /** VL Slide-Coordinates Microscopic Image Storage, SOPClass */
    public static final String VLSlideCoordinatesMicroscopicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.3";

    /** VL Photographic Image Storage, SOPClass */
    public static final String VLPhotographicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.4";

    /** Video Photographic Image Storage, SOPClass */
    public static final String VideoPhotographicImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.4.1";

    /** Ophthalmic Photography 8 Bit Image Storage, SOPClass */
    public static final String OphthalmicPhotography8BitImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.1";

    /** Ophthalmic Photography 16 Bit Image Storage, SOPClass */
    public static final String OphthalmicPhotography16BitImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.2";

    /** Stereometric Relationship Storage, SOPClass */
    public static final String StereometricRelationshipStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.3";

    /** Ophthalmic Tomography Image Storage, SOPClass */
    public static final String OphthalmicTomographyImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.4";

    /** Wide Field Ophthalmic Photography Stereographic Projection Image Storage, SOPClass */
    public static final String WideFieldOphthalmicPhotographyStereographicProjectionImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.5";

    /** Wide Field Ophthalmic Photography 3D Coordinates Image Storage, SOPClass */
    public static final String WideFieldOphthalmicPhotography3DCoordinatesImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.6";

    /** Ophthalmic Optical Coherence Tomography En Face Image Storage, SOPClass */
    public static final String OphthalmicOpticalCoherenceTomographyEnFaceImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.7";

    /** Ophthalmic Optical Coherence Tomography B-scan Volume Analysis Storage, SOPClass */
    public static final String OphthalmicOpticalCoherenceTomographyBScanVolumeAnalysisStorage = "1.2.840.10008.5.1.4.1.1.77.1.5.8";

    /** VL Whole Slide Microscopy Image Storage, SOPClass */
    public static final String VLWholeSlideMicroscopyImageStorage = "1.2.840.10008.5.1.4.1.1.77.1.6";

    /** Lensometry Measurements Storage, SOPClass */
    public static final String LensometryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.1";

    /** Autorefraction Measurements Storage, SOPClass */
    public static final String AutorefractionMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.2";

    /** Keratometry Measurements Storage, SOPClass */
    public static final String KeratometryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.3";

    /** Subjective Refraction Measurements Storage, SOPClass */
    public static final String SubjectiveRefractionMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.4";

    /** Visual Acuity Measurements Storage, SOPClass */
    public static final String VisualAcuityMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.5";

    /** Spectacle Prescription Report Storage, SOPClass */
    public static final String SpectaclePrescriptionReportStorage = "1.2.840.10008.5.1.4.1.1.78.6";

    /** Ophthalmic Axial Measurements Storage, SOPClass */
    public static final String OphthalmicAxialMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.78.7";

    /** Intraocular Lens Calculations Storage, SOPClass */
    public static final String IntraocularLensCalculationsStorage = "1.2.840.10008.5.1.4.1.1.78.8";

    /** Macular Grid Thickness and Volume Report Storage, SOPClass */
    public static final String MacularGridThicknessAndVolumeReportStorage = "1.2.840.10008.5.1.4.1.1.79.1";

    /** Ophthalmic Visual Field Static Perimetry Measurements Storage, SOPClass */
    public static final String OphthalmicVisualFieldStaticPerimetryMeasurementsStorage = "1.2.840.10008.5.1.4.1.1.80.1";

    /** Ophthalmic Thickness Map Storage, SOPClass */
    public static final String OphthalmicThicknessMapStorage = "1.2.840.10008.5.1.4.1.1.81.1";

    /** Corneal Topography Map Storage, SOPClass */
    public static final String CornealTopographyMapStorage = "1.2.840.10008.5.1.4.1.1.82.1";

    /** Text SR Storage - Trial (Retired), SOPClass */
    public static final String TextSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.1";

    /** Audio SR Storage - Trial (Retired), SOPClass */
    public static final String AudioSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.2";

    /** Detail SR Storage - Trial (Retired), SOPClass */
    public static final String DetailSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.3";

    /** Comprehensive SR Storage - Trial (Retired), SOPClass */
    public static final String ComprehensiveSRStorageTrialRetired = "1.2.840.10008.5.1.4.1.1.88.4";

    /** Basic Text SR Storage, SOPClass */
    public static final String BasicTextSRStorage = "1.2.840.10008.5.1.4.1.1.88.11";

    /** Enhanced SR Storage, SOPClass */
    public static final String EnhancedSRStorage = "1.2.840.10008.5.1.4.1.1.88.22";

    /** Comprehensive SR Storage, SOPClass */
    public static final String ComprehensiveSRStorage = "1.2.840.10008.5.1.4.1.1.88.33";

    /** Comprehensive 3D SR Storage, SOPClass */
    public static final String Comprehensive3DSRStorage = "1.2.840.10008.5.1.4.1.1.88.34";

    /** Extensible SR Storage, SOPClass */
    public static final String ExtensibleSRStorage = "1.2.840.10008.5.1.4.1.1.88.35";

    /** Procedure Log Storage, SOPClass */
    public static final String ProcedureLogStorage = "1.2.840.10008.5.1.4.1.1.88.40";

    /** Mammography CAD SR Storage, SOPClass */
    public static final String MammographyCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.50";

    /** Key Object Selection Document Storage, SOPClass */
    public static final String KeyObjectSelectionDocumentStorage = "1.2.840.10008.5.1.4.1.1.88.59";

    /** Chest CAD SR Storage, SOPClass */
    public static final String ChestCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.65";

    /** X-Ray Radiation Dose SR Storage, SOPClass */
    public static final String XRayRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.67";

    /** Radiopharmaceutical Radiation Dose SR Storage, SOPClass */
    public static final String RadiopharmaceuticalRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.68";

    /** Colon CAD SR Storage, SOPClass */
    public static final String ColonCADSRStorage = "1.2.840.10008.5.1.4.1.1.88.69";

    /** Implantation Plan SR Storage, SOPClass */
    public static final String ImplantationPlanSRStorage = "1.2.840.10008.5.1.4.1.1.88.70";

    /** Acquisition Context SR Storage, SOPClass */
    public static final String AcquisitionContextSRStorage = "1.2.840.10008.5.1.4.1.1.88.71";

    /** Simplified Adult Echo SR Storage, SOPClass */
    public static final String SimplifiedAdultEchoSRStorage = "1.2.840.10008.5.1.4.1.1.88.72";

    /** Patient Radiation Dose SR Storage, SOPClass */
    public static final String PatientRadiationDoseSRStorage = "1.2.840.10008.5.1.4.1.1.88.73";

    /** Planned Imaging Agent Administration SR Storage, SOPClass */
    public static final String PlannedImagingAgentAdministrationSRStorage = "1.2.840.10008.5.1.4.1.1.88.74";

    /** Performed Imaging Agent Administration SR Storage, SOPClass */
    public static final String PerformedImagingAgentAdministrationSRStorage = "1.2.840.10008.5.1.4.1.1.88.75";

    /** Content Assessment Results Storage, SOPClass */
    public static final String ContentAssessmentResultsStorage = "1.2.840.10008.5.1.4.1.1.90.1";

    /** Encapsulated PDF Storage, SOPClass */
    public static final String EncapsulatedPDFStorage = "1.2.840.10008.5.1.4.1.1.104.1";

    /** Encapsulated CDA Storage, SOPClass */
    public static final String EncapsulatedCDAStorage = "1.2.840.10008.5.1.4.1.1.104.2";

    /** Encapsulated STL Storage, SOPClass */
    public static final String EncapsulatedSTLStorage = "1.2.840.10008.5.1.4.1.1.104.3";

    /** Encapsulated OBJ Storage, SOPClass */
    public static final String EncapsulatedOBJStorage = "1.2.840.10008.5.1.4.1.1.104.4";

    /** Encapsulated MTL Storage, SOPClass */
    public static final String EncapsulatedMTLStorage = "1.2.840.10008.5.1.4.1.1.104.5";

    /** Positron Emission Tomography Image Storage, SOPClass */
    public static final String PositronEmissionTomographyImageStorage = "1.2.840.10008.5.1.4.1.1.128";

    /** Legacy Converted Enhanced PET Image Storage, SOPClass */
    public static final String LegacyConvertedEnhancedPETImageStorage = "1.2.840.10008.5.1.4.1.1.128.1";

    /** Standalone PET Curve Storage (Retired), SOPClass */
    public static final String StandalonePETCurveStorageRetired = "1.2.840.10008.5.1.4.1.1.129";

    /** Enhanced PET Image Storage, SOPClass */
    public static final String EnhancedPETImageStorage = "1.2.840.10008.5.1.4.1.1.130";

    /** Basic Structured Display Storage, SOPClass */
    public static final String BasicStructuredDisplayStorage = "1.2.840.10008.5.1.4.1.1.131";

    /** CT Defined Procedure Protocol Storage, SOPClass */
    public static final String CTDefinedProcedureProtocolStorage = "1.2.840.10008.5.1.4.1.1.200.1";

    /** CT Performed Procedure Protocol Storage, SOPClass */
    public static final String CTPerformedProcedureProtocolStorage = "1.2.840.10008.5.1.4.1.1.200.2";

    /** Protocol Approval Storage, SOPClass */
    public static final String ProtocolApprovalStorage = "1.2.840.10008.5.1.4.1.1.200.3";

    /** Protocol Approval Information Model - FIND, SOPClass */
    public static final String ProtocolApprovalInformationModelFIND = "1.2.840.10008.5.1.4.1.1.200.4";

    /** Protocol Approval Information Model - MOVE, SOPClass */
    public static final String ProtocolApprovalInformationModelMOVE = "1.2.840.10008.5.1.4.1.1.200.5";

    /** Protocol Approval Information Model - GET, SOPClass */
    public static final String ProtocolApprovalInformationModelGET = "1.2.840.10008.5.1.4.1.1.200.6";

    /** RT Image Storage, SOPClass */
    public static final String RTImageStorage = "1.2.840.10008.5.1.4.1.1.481.1";

    /** RT Dose Storage, SOPClass */
    public static final String RTDoseStorage = "1.2.840.10008.5.1.4.1.1.481.2";

    /** RT Structure Set Storage, SOPClass */
    public static final String RTStructureSetStorage = "1.2.840.10008.5.1.4.1.1.481.3";

    /** RT Beams Treatment Record Storage, SOPClass */
    public static final String RTBeamsTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.4";

    /** RT Plan Storage, SOPClass */
    public static final String RTPlanStorage = "1.2.840.10008.5.1.4.1.1.481.5";

    /** RT Brachy Treatment Record Storage, SOPClass */
    public static final String RTBrachyTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.6";

    /** RT Treatment Summary Record Storage, SOPClass */
    public static final String RTTreatmentSummaryRecordStorage = "1.2.840.10008.5.1.4.1.1.481.7";

    /** RT Ion Plan Storage, SOPClass */
    public static final String RTIonPlanStorage = "1.2.840.10008.5.1.4.1.1.481.8";

    /** RT Ion Beams Treatment Record Storage, SOPClass */
    public static final String RTIonBeamsTreatmentRecordStorage = "1.2.840.10008.5.1.4.1.1.481.9";

    /** RT Physician Intent Storage, SOPClass */
    public static final String RTPhysicianIntentStorage = "1.2.840.10008.5.1.4.1.1.481.10";

    /** RT Segment Annotation Storage, SOPClass */
    public static final String RTSegmentAnnotationStorage = "1.2.840.10008.5.1.4.1.1.481.11";

    /** RT Radiation Set Storage, SOPClass */
    public static final String RTRadiationSetStorage = "1.2.840.10008.5.1.4.1.1.481.12";

    /** C-Arm Photon-Electron Radiation Storage, SOPClass */
    public static final String CArmPhotonElectronRadiationStorage = "1.2.840.10008.5.1.4.1.1.481.13";

    /** Tomotherapeutic Radiation Storage, SOPClass */
    public static final String TomotherapeuticRadiationStorage = "1.2.840.10008.5.1.4.1.1.481.14";

    /** Robotic-Arm Radiation Storage, SOPClass */
    public static final String RoboticArmRadiationStorage = "1.2.840.10008.5.1.4.1.1.481.15";

    /** RT Radiation Record Set Storage, SOPClass */
    public static final String RTRadiationRecordSetStorage = "1.2.840.10008.5.1.4.1.1.481.16";

    /** RT Radiation Salvage Record Storage, SOPClass */
    public static final String RTRadiationSalvageRecordStorage = "1.2.840.10008.5.1.4.1.1.481.17";

    /** Tomotherapeutic Radiation Record Storage, SOPClass */
    public static final String TomotherapeuticRadiationRecordStorage = "1.2.840.10008.5.1.4.1.1.481.18";

    /** C-Arm Photon-Electron Radiation Record Storage, SOPClass */
    public static final String CArmPhotonElectronRadiationRecordStorage = "1.2.840.10008.5.1.4.1.1.481.19";

    /** Robotic Radiation Record Storage, SOPClass */
    public static final String RoboticRadiationRecordStorage = "1.2.840.10008.5.1.4.1.1.481.20";

    /** DICOS CT Image Storage, SOPClass */
    public static final String DICOSCTImageStorage = "1.2.840.10008.5.1.4.1.1.501.1";

    /** DICOS Digital X-Ray Image Storage - For Presentation, SOPClass */
    public static final String DICOSDigitalXRayImageStorageForPresentation = "1.2.840.10008.5.1.4.1.1.501.2.1";

    /** DICOS Digital X-Ray Image Storage - For Processing, SOPClass */
    public static final String DICOSDigitalXRayImageStorageForProcessing = "1.2.840.10008.5.1.4.1.1.501.2.2";

    /** DICOS Threat Detection Report Storage, SOPClass */
    public static final String DICOSThreatDetectionReportStorage = "1.2.840.10008.5.1.4.1.1.501.3";

    /** DICOS 2D AIT Storage, SOPClass */
    public static final String DICOS2DAITStorage = "1.2.840.10008.5.1.4.1.1.501.4";

    /** DICOS 3D AIT Storage, SOPClass */
    public static final String DICOS3DAITStorage = "1.2.840.10008.5.1.4.1.1.501.5";

    /** DICOS Quadrupole Resonance (QR) Storage, SOPClass */
    public static final String DICOSQuadrupoleResonanceQRStorage = "1.2.840.10008.5.1.4.1.1.501.6";

    /** Eddy Current Image Storage, SOPClass */
    public static final String EddyCurrentImageStorage = "1.2.840.10008.5.1.4.1.1.601.1";

    /** Eddy Current Multi-frame Image Storage, SOPClass */
    public static final String EddyCurrentMultiFrameImageStorage = "1.2.840.10008.5.1.4.1.1.601.2";

    /** Patient Root Query/Retrieve Information Model - FIND, SOPClass */
    public static final String PatientRootQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.1.2.1.1";

    /** Patient Root Query/Retrieve Information Model - MOVE, SOPClass */
    public static final String PatientRootQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.1.2.1.2";

    /** Patient Root Query/Retrieve Information Model - GET, SOPClass */
    public static final String PatientRootQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.1.2.1.3";

    /** Study Root Query/Retrieve Information Model - FIND, SOPClass */
    public static final String StudyRootQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.1.2.2.1";

    /** Study Root Query/Retrieve Information Model - MOVE, SOPClass */
    public static final String StudyRootQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.1.2.2.2";

    /** Study Root Query/Retrieve Information Model - GET, SOPClass */
    public static final String StudyRootQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.1.2.2.3";

    /** Patient/Study Only Query/Retrieve Information Model - FIND (Retired), SOPClass */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelFINDRetired = "1.2.840.10008.5.1.4.1.2.3.1";

    /** Patient/Study Only Query/Retrieve Information Model - MOVE (Retired), SOPClass */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelMOVERetired = "1.2.840.10008.5.1.4.1.2.3.2";

    /** Patient/Study Only Query/Retrieve Information Model - GET (Retired), SOPClass */
    public static final String PatientStudyOnlyQueryRetrieveInformationModelGETRetired = "1.2.840.10008.5.1.4.1.2.3.3";

    /** Composite Instance Root Retrieve - MOVE, SOPClass */
    public static final String CompositeInstanceRootRetrieveMOVE = "1.2.840.10008.5.1.4.1.2.4.2";

    /** Composite Instance Root Retrieve - GET, SOPClass */
    public static final String CompositeInstanceRootRetrieveGET = "1.2.840.10008.5.1.4.1.2.4.3";

    /** Composite Instance Retrieve Without Bulk Data - GET, SOPClass */
    public static final String CompositeInstanceRetrieveWithoutBulkDataGET = "1.2.840.10008.5.1.4.1.2.5.3";

    /** Defined Procedure Protocol Information Model - FIND, SOPClass */
    public static final String DefinedProcedureProtocolInformationModelFIND = "1.2.840.10008.5.1.4.20.1";

    /** Defined Procedure Protocol Information Model - MOVE, SOPClass */
    public static final String DefinedProcedureProtocolInformationModelMOVE = "1.2.840.10008.5.1.4.20.2";

    /** Defined Procedure Protocol Information Model - GET, SOPClass */
    public static final String DefinedProcedureProtocolInformationModelGET = "1.2.840.10008.5.1.4.20.3";

    /** Modality Worklist Information Model - FIND, SOPClass */
    public static final String ModalityWorklistInformationModelFIND = "1.2.840.10008.5.1.4.31";

    /** General Purpose Worklist Management Meta SOP Class (Retired), MetaSOPClass */
    public static final String GeneralPurposeWorklistManagementMetaSOPClassRetired = "1.2.840.10008.5.1.4.32";

    /** General Purpose Worklist Information Model - FIND (Retired), SOPClass */
    public static final String GeneralPurposeWorklistInformationModelFINDRetired = "1.2.840.10008.5.1.4.32.1";

    /** General Purpose Scheduled Procedure Step SOP Class (Retired), SOPClass */
    public static final String GeneralPurposeScheduledProcedureStepSOPClassRetired = "1.2.840.10008.5.1.4.32.2";

    /** General Purpose Performed Procedure Step SOP Class (Retired), SOPClass */
    public static final String GeneralPurposePerformedProcedureStepSOPClassRetired = "1.2.840.10008.5.1.4.32.3";

    /** Instance Availability Notification SOP Class, SOPClass */
    public static final String InstanceAvailabilityNotificationSOPClass = "1.2.840.10008.5.1.4.33";

    /** RT Beams Delivery Instruction Storage - Trial (Retired), SOPClass */
    public static final String RTBeamsDeliveryInstructionStorageTrialRetired = "1.2.840.10008.5.1.4.34.1";

    /** RT Conventional Machine Verification - Trial (Retired), SOPClass */
    public static final String RTConventionalMachineVerificationTrialRetired = "1.2.840.10008.5.1.4.34.2";

    /** RT Ion Machine Verification - Trial (Retired), SOPClass */
    public static final String RTIonMachineVerificationTrialRetired = "1.2.840.10008.5.1.4.34.3";

    /** Unified Worklist and Procedure Step Service Class - Trial (Retired), ServiceClass */
    public static final String UnifiedWorklistAndProcedureStepServiceClassTrialRetired = "1.2.840.10008.5.1.4.34.4";

    /** Unified Procedure Step - Push SOP Class - Trial (Retired), SOPClass */
    public static final String UnifiedProcedureStepPushSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.1";

    /** Unified Procedure Step - Watch SOP Class - Trial (Retired), SOPClass */
    public static final String UnifiedProcedureStepWatchSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.2";

    /** Unified Procedure Step - Pull SOP Class - Trial (Retired), SOPClass */
    public static final String UnifiedProcedureStepPullSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.3";

    /** Unified Procedure Step - Event SOP Class - Trial (Retired), SOPClass */
    public static final String UnifiedProcedureStepEventSOPClassTrialRetired = "1.2.840.10008.5.1.4.34.4.4";

    /** UPS Global Subscription SOP Instance, WellKnownSOPInstance */
    public static final String UPSGlobalSubscriptionSOPInstance = "1.2.840.10008.5.1.4.34.5";

    /** UPS Filtered Global Subscription SOP Instance, WellKnownSOPInstance */
    public static final String UPSFilteredGlobalSubscriptionSOPInstance = "1.2.840.10008.5.1.4.34.5.1";

    /** Unified Worklist and Procedure Step Service Class, ServiceClass */
    public static final String UnifiedWorklistAndProcedureStepServiceClass = "1.2.840.10008.5.1.4.34.6";

    /** Unified Procedure Step - Push SOP Class, SOPClass */
    public static final String UnifiedProcedureStepPushSOPClass = "1.2.840.10008.5.1.4.34.6.1";

    /** Unified Procedure Step - Watch SOP Class, SOPClass */
    public static final String UnifiedProcedureStepWatchSOPClass = "1.2.840.10008.5.1.4.34.6.2";

    /** Unified Procedure Step - Pull SOP Class, SOPClass */
    public static final String UnifiedProcedureStepPullSOPClass = "1.2.840.10008.5.1.4.34.6.3";

    /** Unified Procedure Step - Event SOP Class, SOPClass */
    public static final String UnifiedProcedureStepEventSOPClass = "1.2.840.10008.5.1.4.34.6.4";

    /** Unified Procedure Step - Query SOP Class, SOPClass */
    public static final String UnifiedProcedureStepQuerySOPClass = "1.2.840.10008.5.1.4.34.6.5";

    /** RT Beams Delivery Instruction Storage, SOPClass */
    public static final String RTBeamsDeliveryInstructionStorage = "1.2.840.10008.5.1.4.34.7";

    /** RT Conventional Machine Verification, SOPClass */
    public static final String RTConventionalMachineVerification = "1.2.840.10008.5.1.4.34.8";

    /** RT Ion Machine Verification, SOPClass */
    public static final String RTIonMachineVerification = "1.2.840.10008.5.1.4.34.9";

    /** RT Brachy Application Setup Delivery Instruction Storage, SOPClass */
    public static final String RTBrachyApplicationSetupDeliveryInstructionStorage = "1.2.840.10008.5.1.4.34.10";

    /** General Relevant Patient Information Query, SOPClass */
    public static final String GeneralRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.1";

    /** Breast Imaging Relevant Patient Information Query, SOPClass */
    public static final String BreastImagingRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.2";

    /** Cardiac Relevant Patient Information Query, SOPClass */
    public static final String CardiacRelevantPatientInformationQuery = "1.2.840.10008.5.1.4.37.3";

    /** Hanging Protocol Storage, SOPClass */
    public static final String HangingProtocolStorage = "1.2.840.10008.5.1.4.38.1";

    /** Hanging Protocol Information Model - FIND, SOPClass */
    public static final String HangingProtocolInformationModelFIND = "1.2.840.10008.5.1.4.38.2";

    /** Hanging Protocol Information Model - MOVE, SOPClass */
    public static final String HangingProtocolInformationModelMOVE = "1.2.840.10008.5.1.4.38.3";

    /** Hanging Protocol Information Model - GET, SOPClass */
    public static final String HangingProtocolInformationModelGET = "1.2.840.10008.5.1.4.38.4";

    /** Color Palette Storage, SOPClass */
    public static final String ColorPaletteStorage = "1.2.840.10008.5.1.4.39.1";

    /** Color Palette Query/Retrieve Information Model - FIND, SOPClass */
    public static final String ColorPaletteQueryRetrieveInformationModelFIND = "1.2.840.10008.5.1.4.39.2";

    /** Color Palette Query/Retrieve Information Model - MOVE, SOPClass */
    public static final String ColorPaletteQueryRetrieveInformationModelMOVE = "1.2.840.10008.5.1.4.39.3";

    /** Color Palette Query/Retrieve Information Model - GET, SOPClass */
    public static final String ColorPaletteQueryRetrieveInformationModelGET = "1.2.840.10008.5.1.4.39.4";

    /** Product Characteristics Query SOP Class, SOPClass */
    public static final String ProductCharacteristicsQuerySOPClass = "1.2.840.10008.5.1.4.41";

    /** Substance Approval Query SOP Class, SOPClass */
    public static final String SubstanceApprovalQuerySOPClass = "1.2.840.10008.5.1.4.42";

    /** Generic Implant Template Storage, SOPClass */
    public static final String GenericImplantTemplateStorage = "1.2.840.10008.5.1.4.43.1";

    /** Generic Implant Template Information Model - FIND, SOPClass */
    public static final String GenericImplantTemplateInformationModelFIND = "1.2.840.10008.5.1.4.43.2";

    /** Generic Implant Template Information Model - MOVE, SOPClass */
    public static final String GenericImplantTemplateInformationModelMOVE = "1.2.840.10008.5.1.4.43.3";

    /** Generic Implant Template Information Model - GET, SOPClass */
    public static final String GenericImplantTemplateInformationModelGET = "1.2.840.10008.5.1.4.43.4";

    /** Implant Assembly Template Storage, SOPClass */
    public static final String ImplantAssemblyTemplateStorage = "1.2.840.10008.5.1.4.44.1";

    /** Implant Assembly Template Information Model - FIND, SOPClass */
    public static final String ImplantAssemblyTemplateInformationModelFIND = "1.2.840.10008.5.1.4.44.2";

    /** Implant Assembly Template Information Model - MOVE, SOPClass */
    public static final String ImplantAssemblyTemplateInformationModelMOVE = "1.2.840.10008.5.1.4.44.3";

    /** Implant Assembly Template Information Model - GET, SOPClass */
    public static final String ImplantAssemblyTemplateInformationModelGET = "1.2.840.10008.5.1.4.44.4";

    /** Implant Template Group Storage, SOPClass */
    public static final String ImplantTemplateGroupStorage = "1.2.840.10008.5.1.4.45.1";

    /** Implant Template Group Information Model - FIND, SOPClass */
    public static final String ImplantTemplateGroupInformationModelFIND = "1.2.840.10008.5.1.4.45.2";

    /** Implant Template Group Information Model - MOVE, SOPClass */
    public static final String ImplantTemplateGroupInformationModelMOVE = "1.2.840.10008.5.1.4.45.3";

    /** Implant Template Group Information Model - GET, SOPClass */
    public static final String ImplantTemplateGroupInformationModelGET = "1.2.840.10008.5.1.4.45.4";

    /** Native DICOM Model, ApplicationHostingModel */
    public static final String NativeDICOMModel = "1.2.840.10008.7.1.1";

    /** Abstract Multi-Dimensional Image Model, ApplicationHostingModel */
    public static final String AbstractMultiDimensionalImageModel = "1.2.840.10008.7.1.2";

    /** DICOM Content Mapping Resource, MappingResource */
    public static final String DICOMContentMappingResource = "1.2.840.10008.8.1.1";

    /** Video Endoscopic Image Real-Time Communication, SOPClass */
    public static final String VideoEndoscopicImageRealTimeCommunication = "1.2.840.10008.10.1";

    /** Video Photographic Image Real-Time Communication, SOPClass */
    public static final String VideoPhotographicImageRealTimeCommunication = "1.2.840.10008.10.2";

    /** Audio Waveform Real-Time Communication, SOPClass */
    public static final String AudioWaveformRealTimeCommunication = "1.2.840.10008.10.3";

    /** Rendition Selection Document Real-Time Communication, SOPClass */
    public static final String RenditionSelectionDocumentRealTimeCommunication = "1.2.840.10008.10.4";

    /** dicomDeviceName, LDAPOID */
    public static final String dicomDeviceName = "1.2.840.10008.15.0.3.1";

    /** dicomDescription, LDAPOID */
    public static final String dicomDescription = "1.2.840.10008.15.0.3.2";

    /** dicomManufacturer, LDAPOID */
    public static final String dicomManufacturer = "1.2.840.10008.15.0.3.3";

    /** dicomManufacturerModelName, LDAPOID */
    public static final String dicomManufacturerModelName = "1.2.840.10008.15.0.3.4";

    /** dicomSoftwareVersion, LDAPOID */
    public static final String dicomSoftwareVersion = "1.2.840.10008.15.0.3.5";

    /** dicomVendorData, LDAPOID */
    public static final String dicomVendorData = "1.2.840.10008.15.0.3.6";

    /** dicomAETitle, LDAPOID */
    public static final String dicomAETitle = "1.2.840.10008.15.0.3.7";

    /** dicomNetworkConnectionReference, LDAPOID */
    public static final String dicomNetworkConnectionReference = "1.2.840.10008.15.0.3.8";

    /** dicomApplicationCluster, LDAPOID */
    public static final String dicomApplicationCluster = "1.2.840.10008.15.0.3.9";

    /** dicomAssociationInitiator, LDAPOID */
    public static final String dicomAssociationInitiator = "1.2.840.10008.15.0.3.10";

    /** dicomAssociationAcceptor, LDAPOID */
    public static final String dicomAssociationAcceptor = "1.2.840.10008.15.0.3.11";

    /** dicomHostname, LDAPOID */
    public static final String dicomHostname = "1.2.840.10008.15.0.3.12";

    /** dicomPort, LDAPOID */
    public static final String dicomPort = "1.2.840.10008.15.0.3.13";

    /** dicomSOPClass, LDAPOID */
    public static final String dicomSOPClass = "1.2.840.10008.15.0.3.14";

    /** dicomTransferRole, LDAPOID */
    public static final String dicomTransferRole = "1.2.840.10008.15.0.3.15";

    /** dicomTransferSyntax, LDAPOID */
    public static final String dicomTransferSyntax = "1.2.840.10008.15.0.3.16";

    /** dicomPrimaryDeviceType, LDAPOID */
    public static final String dicomPrimaryDeviceType = "1.2.840.10008.15.0.3.17";

    /** dicomRelatedDeviceReference, LDAPOID */
    public static final String dicomRelatedDeviceReference = "1.2.840.10008.15.0.3.18";

    /** dicomPreferredCalledAETitle, LDAPOID */
    public static final String dicomPreferredCalledAETitle = "1.2.840.10008.15.0.3.19";

    /** dicomTLSCyphersuite, LDAPOID */
    public static final String dicomTLSCyphersuite = "1.2.840.10008.15.0.3.20";

    /** dicomAuthorizedNodeCertificateReference, LDAPOID */
    public static final String dicomAuthorizedNodeCertificateReference = "1.2.840.10008.15.0.3.21";

    /** dicomThisNodeCertificateReference, LDAPOID */
    public static final String dicomThisNodeCertificateReference = "1.2.840.10008.15.0.3.22";

    /** dicomInstalled, LDAPOID */
    public static final String dicomInstalled = "1.2.840.10008.15.0.3.23";

    /** dicomStationName, LDAPOID */
    public static final String dicomStationName = "1.2.840.10008.15.0.3.24";

    /** dicomDeviceSerialNumber, LDAPOID */
    public static final String dicomDeviceSerialNumber = "1.2.840.10008.15.0.3.25";

    /** dicomInstitutionName, LDAPOID */
    public static final String dicomInstitutionName = "1.2.840.10008.15.0.3.26";

    /** dicomInstitutionAddress, LDAPOID */
    public static final String dicomInstitutionAddress = "1.2.840.10008.15.0.3.27";

    /** dicomInstitutionDepartmentName, LDAPOID */
    public static final String dicomInstitutionDepartmentName = "1.2.840.10008.15.0.3.28";

    /** dicomIssuerOfPatientID, LDAPOID */
    public static final String dicomIssuerOfPatientID = "1.2.840.10008.15.0.3.29";

    /** dicomPreferredCallingAETitle, LDAPOID */
    public static final String dicomPreferredCallingAETitle = "1.2.840.10008.15.0.3.30";

    /** dicomSupportedCharacterSet, LDAPOID */
    public static final String dicomSupportedCharacterSet = "1.2.840.10008.15.0.3.31";

    /** dicomConfigurationRoot, LDAPOID */
    public static final String dicomConfigurationRoot = "1.2.840.10008.15.0.4.1";

    /** dicomDevicesRoot, LDAPOID */
    public static final String dicomDevicesRoot = "1.2.840.10008.15.0.4.2";

    /** dicomUniqueAETitlesRegistryRoot, LDAPOID */
    public static final String dicomUniqueAETitlesRegistryRoot = "1.2.840.10008.15.0.4.3";

    /** dicomDevice, LDAPOID */
    public static final String dicomDevice = "1.2.840.10008.15.0.4.4";

    /** dicomNetworkAE, LDAPOID */
    public static final String dicomNetworkAE = "1.2.840.10008.15.0.4.5";

    /** dicomNetworkConnection, LDAPOID */
    public static final String dicomNetworkConnection = "1.2.840.10008.15.0.4.6";

    /** dicomUniqueAETitle, LDAPOID */
    public static final String dicomUniqueAETitle = "1.2.840.10008.15.0.4.7";

    /** dicomTransferCapability, LDAPOID */
    public static final String dicomTransferCapability = "1.2.840.10008.15.0.4.8";

    /** Universal Coordinated Time, SynchronizationFrameOfReference */
    public static final String UniversalCoordinatedTime = "1.2.840.10008.15.1.1";

    /** Private Agfa Basic Attribute Presentation State, SOPClass */
    public static final String PrivateAgfaBasicAttributePresentationState = "1.2.124.113532.3500.7";

    /** Private Agfa Arrival Transaction, SOPClass */
    public static final String PrivateAgfaArrivalTransaction = "1.2.124.113532.3500.8.1";

    /** Private Agfa Dictation Transaction, SOPClass */
    public static final String PrivateAgfaDictationTransaction = "1.2.124.113532.3500.8.2";

    /** Private Agfa Report Transcription Transaction, SOPClass */
    public static final String PrivateAgfaReportTranscriptionTransaction = "1.2.124.113532.3500.8.3";

    /** Private Agfa Report Approval Transaction, SOPClass */
    public static final String PrivateAgfaReportApprovalTransaction = "1.2.124.113532.3500.8.4";

    /** Private TomTec Annotation Storage, SOPClass */
    public static final String PrivateTomTecAnnotationStorage = "1.2.276.0.48.5.1.4.1.1.7";

    /** Private Toshiba US Image Storage, SOPClass */
    public static final String PrivateToshibaUSImageStorage = "1.2.392.200036.9116.7.8.1.1.1";

    /** Private Fuji CR Image Storage, SOPClass */
    public static final String PrivateFujiCRImageStorage = "1.2.392.200036.9125.1.1.2";

    /** Private GE Collage Storage, SOPClass */
    public static final String PrivateGECollageStorage = "1.2.528.1.1001.5.1.1.1";

    /** Private ERAD Practice Builder Report Text Storage, SOPClass */
    public static final String PrivateERADPracticeBuilderReportTextStorage = "1.2.826.0.1.3680043.293.1.0.1";

    /** Private ERAD Practice Builder Report Dictation Storage, SOPClass */
    public static final String PrivateERADPracticeBuilderReportDictationStorage = "1.2.826.0.1.3680043.293.1.0.2";

    /** Private Philips HP Live 3D 01 Storage, SOPClass */
    public static final String PrivatePhilipsHPLive3D01Storage = "1.2.840.113543.6.6.1.3.10001";

    /** Private Philips HP Live 3D 02 Storage, SOPClass */
    public static final String PrivatePhilipsHPLive3D02Storage = "1.2.840.113543.6.6.1.3.10002";

    /** Private GE 3D Model Storage, SOPClass */
    public static final String PrivateGE3DModelStorage = "1.2.840.113619.4.26";

    /** Private GE Dicom CT Image Info Object, SOPClass */
    public static final String PrivateGEDicomCTImageInfoObject = "1.2.840.113619.4.3";

    /** Private GE Dicom Display Image Info Object, SOPClass */
    public static final String PrivateGEDicomDisplayImageInfoObject = "1.2.840.113619.4.4";

    /** Private GE Dicom MR Image Info Object, SOPClass */
    public static final String PrivateGEDicomMRImageInfoObject = "1.2.840.113619.4.2";

    /** Private GE eNTEGRA Protocol or NM Genie Storage, SOPClass */
    public static final String PrivateGEeNTEGRAProtocolOrNMGenieStorage = "1.2.840.113619.4.27";

    /** Private GE PET Raw Data Storage, SOPClass */
    public static final String PrivateGEPETRawDataStorage = "1.2.840.113619.4.30";

    /** Private GE RT Plan Storage, SOPClass */
    public static final String PrivateGERTPlanStorage = "1.2.840.113619.4.5.249";

    /** Private PixelMed Legacy Converted Enhanced CT Image Storage, SOPClass */
    public static final String PrivatePixelMedLegacyConvertedEnhancedCTImageStorage = "1.3.6.1.4.1.5962.301.1";

    /** Private PixelMed Legacy Converted Enhanced MR Image Storage, SOPClass */
    public static final String PrivatePixelMedLegacyConvertedEnhancedMRImageStorage = "1.3.6.1.4.1.5962.301.2";

    /** Private PixelMed Legacy Converted Enhanced PET Image Storage, SOPClass */
    public static final String PrivatePixelMedLegacyConvertedEnhancedPETImageStorage = "1.3.6.1.4.1.5962.301.3";

    /** Private PixelMed Floating Point Image Storage, SOPClass */
    public static final String PrivatePixelMedFloatingPointImageStorage = "1.3.6.1.4.1.5962.301.9";

    /** Private Siemens CSA Non Image Storage, SOPClass */
    public static final String PrivateSiemensCSANonImageStorage = "1.3.12.2.1107.5.9.1";

    /** Private Siemens CT MR Volume Storage, SOPClass */
    public static final String PrivateSiemensCTMRVolumeStorage = "1.3.12.2.1107.5.99.3.10";

    /** Private Siemens AX Frame Sets Storage, SOPClass */
    public static final String PrivateSiemensAXFrameSetsStorage = "1.3.12.2.1107.5.99.3.11";

    /** Private Philips Specialised XA Storage, SOPClass */
    public static final String PrivatePhilipsSpecialisedXAStorage = "1.3.46.670589.2.3.1.1";

    /** Private Philips CX Image Storage, SOPClass */
    public static final String PrivatePhilipsCXImageStorage = "1.3.46.670589.2.4.1.1";

    /** Private Philips 3D Presentation State Storage, SOPClass */
    public static final String PrivatePhilips3DPresentationStateStorage = "1.3.46.670589.2.5.1.1";

    /** Private Philips VRML Storage, SOPClass */
    public static final String PrivatePhilipsVRMLStorage = "1.3.46.670589.2.8.1.1";

    /** Private Philips Volume Set Storage, SOPClass */
    public static final String PrivatePhilipsVolumeSetStorage = "1.3.46.670589.2.11.1.1";

    /** Private Philips Volume Storage (Retired), SOPClass */
    public static final String PrivatePhilipsVolumeStorageRetired = "1.3.46.670589.5.0.1";

    /** Private Philips Volume Storage, SOPClass */
    public static final String PrivatePhilipsVolumeStorage = "1.3.46.670589.5.0.1.1";

    /** Private Philips 3D Object Storage (Retired), SOPClass */
    public static final String PrivatePhilips3DObjectStorageRetired = "1.3.46.670589.5.0.2";

    /** Private Philips 3D Object Storage, SOPClass */
    public static final String PrivatePhilips3DObjectStorage = "1.3.46.670589.5.0.2.1";

    /** Private Philips Surface Storage (Retired), SOPClass */
    public static final String PrivatePhilipsSurfaceStorageRetired = "1.3.46.670589.5.0.3";

    /** Private Philips Surface Storage, SOPClass */
    public static final String PrivatePhilipsSurfaceStorage = "1.3.46.670589.5.0.3.1";

    /** Private Philips Composite Object Storage, SOPClass */
    public static final String PrivatePhilipsCompositeObjectStorage = "1.3.46.670589.5.0.4";

    /** Private Philips MR Cardio Profile Storage, SOPClass */
    public static final String PrivatePhilipsMRCardioProfileStorage = "1.3.46.670589.5.0.7";

    /** Private Philips MR Cardio Storage (Retired), SOPClass */
    public static final String PrivatePhilipsMRCardioStorageRetired = "1.3.46.670589.5.0.8";

    /** Private Philips MR Cardio Storage, SOPClass */
    public static final String PrivatePhilipsMRCardioStorage = "1.3.46.670589.5.0.8.1";

    /** Private Philips CT Synthetic Image Storage, SOPClass */
    public static final String PrivatePhilipsCTSyntheticImageStorage = "1.3.46.670589.5.0.9";

    /** Private Philips MR Synthetic Image Storage, SOPClass */
    public static final String PrivatePhilipsMRSyntheticImageStorage = "1.3.46.670589.5.0.10";

    /** Private Philips MR Cardio Analysis Storage (Retired), SOPClass */
    public static final String PrivatePhilipsMRCardioAnalysisStorageRetired = "1.3.46.670589.5.0.11";

    /** Private Philips MR Cardio Analysis Storage, SOPClass */
    public static final String PrivatePhilipsMRCardioAnalysisStorage = "1.3.46.670589.5.0.11.1";

    /** Private Philips CX Synthetic Image Storage, SOPClass */
    public static final String PrivatePhilipsCXSyntheticImageStorage = "1.3.46.670589.5.0.12";

    /** Private Philips Perfusion Storage, SOPClass */
    public static final String PrivatePhilipsPerfusionStorage = "1.3.46.670589.5.0.13";

    /** Private Philips Perfusion Image Storage, SOPClass */
    public static final String PrivatePhilipsPerfusionImageStorage = "1.3.46.670589.5.0.14";

    /** Private Philips X-Ray MF Storage, SOPClass */
    public static final String PrivatePhilipsXRayMFStorage = "1.3.46.670589.7.8.1618510091";

    /** Private Philips Live Run Storage, SOPClass */
    public static final String PrivatePhilipsLiveRunStorage = "1.3.46.670589.7.8.1618510092";

    /** Private Philips Run Storage, SOPClass */
    public static final String PrivatePhilipsRunStorage = "1.3.46.670589.7.8.16185100129";

    /** Private Philips Reconstruction Storage, SOPClass */
    public static final String PrivatePhilipsReconstructionStorage = "1.3.46.670589.7.8.16185100130";

    /** Private Philips MR Spectrum Storage, SOPClass */
    public static final String PrivatePhilipsMRSpectrumStorage = "1.3.46.670589.11.0.0.12.1";

    /** Private Philips MR Series Data Storage, SOPClass */
    public static final String PrivatePhilipsMRSeriesDataStorage = "1.3.46.670589.11.0.0.12.2";

    /** Private Philips MR Color Image Storage, SOPClass */
    public static final String PrivatePhilipsMRColorImageStorage = "1.3.46.670589.11.0.0.12.3";

    /** Private Philips MR Examcard Storage, SOPClass */
    public static final String PrivatePhilipsMRExamcardStorage = "1.3.46.670589.11.0.0.12.4";

    /** Private PMOD Multi-frame Image Storage, SOPClass */
    public static final String PrivatePMODMultiframeImageStorage = "2.16.840.1.114033.5.1.4.1.1.130";

}
