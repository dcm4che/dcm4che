<?xml version="1.0" encoding="UTF-8"?>
<!-- ***** BEGIN LICENSE BLOCK *****
   - Version: MPL 1.1/GPL 2.0/LGPL 2.1
   -
   - The contents of this file are subject to the Mozilla Public License Version
   - 1.1 (the "License"); you may not use this file except in compliance with
   - the License. You may obtain a copy of the License at
   - http://www.mozilla.org/MPL/
   -
   - Software distributed under the License is distributed on an "AS IS" basis,
   - WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
   - for the specific language governing rights and limitations under the
   - License.
   -
   - The Original Code is part of dcm4che, an implementation of DICOM(TM) in
   - Java(TM), hosted at https://github.com/gunterze/dcm4che.
   -
   - The Initial Developer of the Original Code is
   - J4Care.
   - Portions created by the Initial Developer are Copyright (C) 2016
   - the Initial Developer. All Rights Reserved.
   -
   - Contributor(s):
   - Gunter Zeilinger <gunterze@gmail.com>
   -
   - Alternatively, the contents of this file may be used under the terms of
   - either the GNU General Public License Version 2 or later (the "GPL"), or
   - the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
   - in which case the provisions of the GPL or the LGPL are applicable instead
   - of those above. If you wish to allow use of your version of this file only
   - under the terms of either the GPL or the LGPL, and not to allow others to
   - use your version of this file under the terms of the MPL, indicate your
   - decision by deleting the provisions above and replace them with the notice
   - and other provisions required by the GPL or the LGPL. If you do not delete
   - the provisions above, a recipient may use your version of this file under
   - the terms of any one of the MPL, the GPL or the LGPL.
   -
   - ***** END LICENSE BLOCK *****  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template name="uidsEx">
    <uid value="1.2.124.113532.3500.7" keyword="PrivateAgfaBasicAttributePresentationState" type="SOPClass">Private Agfa Basic Attribute Presentation State</uid>
    <uid value="1.2.124.113532.3500.8.1" keyword="PrivateAgfaArrivalTransaction" type="SOPClass">Private Agfa Arrival Transaction</uid>
    <uid value="1.2.124.113532.3500.8.2" keyword="PrivateAgfaDictationTransaction" type="SOPClass">Private Agfa Dictation Transaction</uid>
    <uid value="1.2.124.113532.3500.8.3" keyword="PrivateAgfaReportTranscriptionTransaction" type="SOPClass">Private Agfa Report Transcription Transaction</uid>
    <uid value="1.2.124.113532.3500.8.4" keyword="PrivateAgfaReportApprovalTransaction" type="SOPClass">Private Agfa Report Approval Transaction</uid>
    <uid value="1.2.276.0.48.5.1.4.1.1.7" keyword="PrivateTomTecAnnotationStorage" type="SOPClass">Private TomTec Annotation Storage</uid>
    <uid value="1.2.392.200036.9116.7.8.1.1.1" keyword="PrivateToshibaUSImageStorage" type="SOPClass">Private Toshiba US Image Storage</uid>
    <uid value="1.2.392.200036.9125.1.1.2" keyword="PrivateFujiCRImageStorage" type="SOPClass">Private Fuji CR Image Storage</uid>
    <uid value="1.2.528.1.1001.5.1.1.1" keyword="PrivateGECollageStorage" type="SOPClass">Private GE Collage Storage</uid>
    <uid value="1.2.826.0.1.3680043.293.1.0.1" keyword="PrivateERADPracticeBuilderReportTextStorage" type="SOPClass">Private ERAD Practice Builder Report Text Storage</uid>
    <uid value="1.2.826.0.1.3680043.293.1.0.2" keyword="PrivateERADPracticeBuilderReportDictationStorage" type="SOPClass">Private ERAD Practice Builder Report Dictation Storage</uid>
    <uid value="1.2.840.113543.6.6.1.3.10001" keyword="PrivatePhilipsHPLive3D01Storage" type="SOPClass">Private Philips HP Live 3D 01 Storage</uid>
    <uid value="1.2.840.113543.6.6.1.3.10002" keyword="PrivatePhilipsHPLive3D02Storage" type="SOPClass">Private Philips HP Live 3D 02 Storage</uid>
    <uid value="1.2.840.113619.4.26" keyword="PrivateGE3DModelStorage" type="SOPClass">Private GE 3D Model Storage</uid>
    <uid value="1.2.840.113619.4.3" keyword="PrivateGEDicomCTImageInfoObject" type="SOPClass">Private GE Dicom CT Image Info Object</uid>
    <uid value="1.2.840.113619.4.4" keyword="PrivateGEDicomDisplayImageInfoObject" type="SOPClass">Private GE Dicom Display Image Info Object</uid>
    <uid value="1.2.840.113619.4.2" keyword="PrivateGEDicomMRImageInfoObject" type="SOPClass">Private GE Dicom MR Image Info Object</uid>
    <uid value="1.2.840.113619.4.27" keyword="PrivateGEeNTEGRAProtocolOrNMGenieStorage" type="SOPClass">Private GE eNTEGRA Protocol or NM Genie Storage</uid>
    <uid value="1.2.840.113619.4.30" keyword="PrivateGEPETRawDataStorage" type="SOPClass">Private GE PET Raw Data Storage</uid>
    <uid value="1.2.840.113619.4.5.249" keyword="PrivateGERTPlanStorage" type="SOPClass">Private GE RT Plan Storage</uid>
    <uid value="1.3.6.1.4.1.5962.301.1" keyword="PrivatePixelMedLegacyConvertedEnhancedCTImageStorage" type="SOPClass">Private PixelMed Legacy Converted Enhanced CT Image Storage</uid>
    <uid value="1.3.6.1.4.1.5962.301.2" keyword="PrivatePixelMedLegacyConvertedEnhancedMRImageStorage" type="SOPClass">Private PixelMed Legacy Converted Enhanced MR Image Storage</uid>
    <uid value="1.3.6.1.4.1.5962.301.3" keyword="PrivatePixelMedLegacyConvertedEnhancedPETImageStorage" type="SOPClass">Private PixelMed Legacy Converted Enhanced PET Image Storage</uid>
    <uid value="1.3.6.1.4.1.5962.301.9" keyword="PrivatePixelMedFloatingPointImageStorage" type="SOPClass">Private PixelMed Floating Point Image Storage</uid>
    <uid value="1.3.12.2.1107.5.9.1" keyword="PrivateSiemensCSANonImageStorage" type="SOPClass">Private Siemens CSA Non Image Storage</uid>
    <uid value="1.3.12.2.1107.5.99.3.10" keyword="PrivateSiemensCTMRVolumeStorage" type="SOPClass">Private Siemens CT MR Volume Storage</uid>
    <uid value="1.3.12.2.1107.5.99.3.11" keyword="PrivateSiemensAXFrameSetsStorage" type="SOPClass">Private Siemens AX Frame Sets Storage</uid>
    <uid value="1.3.46.670589.2.3.1.1" keyword="PrivatePhilipsSpecialisedXAStorage" type="SOPClass">Private Philips Specialised XA Storage</uid>
    <uid value="1.3.46.670589.2.4.1.1" keyword="PrivatePhilipsCXImageStorage" type="SOPClass">Private Philips CX Image Storage</uid>
    <uid value="1.3.46.670589.2.5.1.1" keyword="PrivatePhilips3DPresentationStateStorage" type="SOPClass">Private Philips 3D Presentation State Storage</uid>
    <uid value="1.3.46.670589.2.8.1.1" keyword="PrivatePhilipsVRMLStorage" type="SOPClass">Private Philips VRML Storage</uid>
    <uid value="1.3.46.670589.2.11.1.1" keyword="PrivatePhilipsVolumeSetStorage" type="SOPClass">Private Philips Volume Set Storage</uid>
    <uid value="1.3.46.670589.5.0.1" keyword="PrivatePhilipsVolumeStorageRetired" type="SOPClass">Private Philips Volume Storage (Retired)</uid>
    <uid value="1.3.46.670589.5.0.1.1" keyword="PrivatePhilipsVolumeStorage" type="SOPClass">Private Philips Volume Storage</uid>
    <uid value="1.3.46.670589.5.0.2" keyword="PrivatePhilips3DObjectStorageRetired" type="SOPClass">Private Philips 3D Object Storage (Retired)</uid>
    <uid value="1.3.46.670589.5.0.2.1" keyword="PrivatePhilips3DObjectStorage" type="SOPClass">Private Philips 3D Object Storage</uid>
    <uid value="1.3.46.670589.5.0.3" keyword="PrivatePhilipsSurfaceStorageRetired" type="SOPClass">Private Philips Surface Storage (Retired)</uid>
    <uid value="1.3.46.670589.5.0.3.1" keyword="PrivatePhilipsSurfaceStorage" type="SOPClass">Private Philips Surface Storage</uid>
    <uid value="1.3.46.670589.5.0.4" keyword="PrivatePhilipsCompositeObjectStorage" type="SOPClass">Private Philips Composite Object Storage</uid>
    <uid value="1.3.46.670589.5.0.7" keyword="PrivatePhilipsMRCardioProfileStorage" type="SOPClass">Private Philips MR Cardio Profile Storage</uid>
    <uid value="1.3.46.670589.5.0.8" keyword="PrivatePhilipsMRCardioStorageRetired" type="SOPClass">Private Philips MR Cardio Storage (Retired)</uid>
    <uid value="1.3.46.670589.5.0.8.1" keyword="PrivatePhilipsMRCardioStorage" type="SOPClass">Private Philips MR Cardio Storage</uid>
    <uid value="1.3.46.670589.5.0.9" keyword="PrivatePhilipsCTSyntheticImageStorage" type="SOPClass">Private Philips CT Synthetic Image Storage</uid>
    <uid value="1.3.46.670589.5.0.10" keyword="PrivatePhilipsMRSyntheticImageStorage" type="SOPClass">Private Philips MR Synthetic Image Storage</uid>
    <uid value="1.3.46.670589.5.0.11" keyword="PrivatePhilipsMRCardioAnalysisStorageRetired" type="SOPClass">Private Philips MR Cardio Analysis Storage (Retired)</uid>
    <uid value="1.3.46.670589.5.0.11.1" keyword="PrivatePhilipsMRCardioAnalysisStorage" type="SOPClass">Private Philips MR Cardio Analysis Storage</uid>
    <uid value="1.3.46.670589.5.0.12" keyword="PrivatePhilipsCXSyntheticImageStorage" type="SOPClass">Private Philips CX Synthetic Image Storage</uid>
    <uid value="1.3.46.670589.5.0.13" keyword="PrivatePhilipsPerfusionStorage" type="SOPClass">Private Philips Perfusion Storage</uid>
    <uid value="1.3.46.670589.5.0.14" keyword="PrivatePhilipsPerfusionImageStorage" type="SOPClass">Private Philips Perfusion Image Storage</uid>
    <uid value="1.3.46.670589.7.8.1618510091" keyword="PrivatePhilipsXRayMFStorage" type="SOPClass">Private Philips X-Ray MF Storage</uid>
    <uid value="1.3.46.670589.7.8.1618510092" keyword="PrivatePhilipsLiveRunStorage" type="SOPClass">Private Philips Live Run Storage</uid>
    <uid value="1.3.46.670589.7.8.16185100129" keyword="PrivatePhilipsRunStorage" type="SOPClass">Private Philips Run Storage</uid>
    <uid value="1.3.46.670589.7.8.16185100130" keyword="PrivatePhilipsReconstructionStorage" type="SOPClass">Private Philips Reconstruction Storage</uid>
    <uid value="1.3.46.670589.11.0.0.12.1" keyword="PrivatePhilipsMRSpectrumStorage" type="SOPClass">Private Philips MR Spectrum Storage</uid>
    <uid value="1.3.46.670589.11.0.0.12.2" keyword="PrivatePhilipsMRSeriesDataStorage" type="SOPClass">Private Philips MR Series Data Storage</uid>
    <uid value="1.3.46.670589.11.0.0.12.3" keyword="PrivatePhilipsMRColorImageStorage" type="SOPClass">Private Philips MR Color Image Storage</uid>
    <uid value="1.3.46.670589.11.0.0.12.4" keyword="PrivatePhilipsMRExamcardStorage" type="SOPClass">Private Philips MR Examcard Storage</uid>
    <uid value="2.16.840.1.114033.5.1.4.1.1.130" keyword="PrivatePMODMultiframeImageStorage" type="SOPClass">Private PMOD Multi-frame Image Storage</uid>
  </xsl:template>

</xsl:stylesheet>