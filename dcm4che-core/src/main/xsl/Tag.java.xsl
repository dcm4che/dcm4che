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
   - Java(TM), hosted at https://github.com/dcm4che.
   -
   - The Initial Developer of the Original Code is
   - Agfa Healthcare.
   - Portions created by the Initial Developer are Copyright (C) 2011
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

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text"></xsl:output>
  <xsl:template match="/elements">
    <xsl:text>/* ***** BEGIN LICENSE BLOCK *****
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
 * This file is generated from Part 6 and 7 of the Standard Text Edition 2011.
 */
 
package org.dcm4che3.data;

/**
 * @author Gunter Zeilinger &lt;gunterze@gmail.com&gt;
 */
public class Tag {
</xsl:text>
    <xsl:apply-templates select="//el[@keyword!='']" />
    <xsl:text>
    /** (0008,0012) VR=DA VM=1 Instance Creation Date
     *  (0008,0013) VR=TM VM=1 Instance Creation Time */
    public static final long InstanceCreationDateAndTime = 0x0008001200080013L;

    /** (0008,0020) VR=DA VM=1 Study Date
     *  (0008,0030) VR=TM VM=1 Study Time */
    public static final long StudyDateAndTime = 0x0008002000080030L;

    /** (0008,0021) VR=DA VM=1 Series Date
     *  (0008,0031) VR=TM VM=1 Series Time */
    public static final long SeriesDateAndTime = 0x0008002100080031L;

    /** (0008,0022) VR=DA VM=1 Acquisition Date
     *  (0008,0032) VR=TM VM=1 Acquisition Time */
    public static final long AcquisitionDateAndTime = 0x0008002200080032L;

    /** (0008,0023) VR=DA VM=1 Content Date
     *  (0008,0033) VR=TM VM=1 Content Time */
    public static final long ContentDateAndTime = 0x0008002300080033L;

    /** (0008,0024) VR=DA VM=1 Overlay Date (retired)
     *  (0008,0034) VR=TM VM=1 Overlay Time (retired) */
    public static final long OverlayDateAndTime = 0x0008002400080034L;

    /** (0008,0025) VR=DA VM=1 Curve Date (retired)
     *  (0008,0035) VR=TM VM=1 Curve Time (retired) */
    public static final long CurveDateAndTime = 0x0008002500080035L;

    /** (0010,0030) VR=DA VM=1 Patient's Birth Date
     *  (0010,0032) VR=TM VM=1 Patient's Birth Time */
    public static final long PatientBirthDateAndTime = 0x0010003000100032L;

    /** (0018,1012) VR=DA VM=1 Date of Secondary Capture
     *  (0018,1014) VR=TM VM=1 Time of Secondary Capture */
    public static final long DateAndTimeOfSecondaryCapture = 0x0018101200181014L;

    /** (0018,1200) VR=DA VM=1-n Date of Last Calibration
     *  (0018,1201) VR=TM VM=1-n Time of Last Calibration */
    public static final long DateAndTimeOfLastCalibration = 0x0018120000181201L;

    /** (0018,700C) VR=DA VM=1 Date of Last Detector Calibration
     *  (0018,700E) VR=TM VM=1 Time of Last Detector Calibration */
    public static final long DateAndTimeOfLastDetectorCalibration = 0x0018700C0018700EL;

    /** (0020,3403) VR=DA VM=1 Modified Image Date (retired)
     *  (0020,3405) VR=TM VM=1 Modified Image Time (retired) */
    public static final long ModifiedImageDateAndTime = 0x0020340300203405L;

    /** (0032,0032) VR=DA VM=1 Study Verified Date (retired)
     *  (0032,0033) VR=TM VM=1 Study Verified Time (retired) */
    public static final long StudyVerifiedDateAndTime = 0x0032003200320033L;

    /** (0032,0034) VR=DA VM=1 Study Read Date (retired)
     *  (0032,0035) VR=TM VM=1 Study Read Time (retired) */
    public static final long StudyReadDateAndTime = 0x0032003400320035L;

    /** (0032,1000) VR=DA VM=1 Scheduled Study Start Date (retired)
     *  (0032,1001) VR=TM VM=1 Scheduled Study Start Time (retired) */
    public static final long ScheduledStudyStartDateAndTime = 0x0032100000321001L;

    /** (0032,1010) VR=DA VM=1 Scheduled Study Stop Date (retired)
     *  (0032,1011) VR=TM VM=1 Scheduled Study Stop Time (retired) */
    public static final long ScheduledStudyStopDateAndTime = 0x0032101000321011L;

    /** (0032,1040) VR=DA VM=1 Study Arrival Date (retired)
     *  (0032,1041) VR=TM VM=1 Study Arrival Time (retired) */
    public static final long StudyArrivalDateAndTime = 0x0032104000321041L;

    /** (0032,1050) VR=DA VM=1 Study Completion Date (retired)
     *  (0032,1051) VR=TM VM=1 Study Completion Time (retired) */
    public static final long StudyCompletionDateAndTime = 0x0032105000321051L;

    /** (0038,001A) VR=DA VM=1 Scheduled Admission Date (retired)
     *  (0038,001B) VR=TM VM=1 Scheduled Admission Time (retired) */
    public static final long ScheduledAdmissionDateAndTime = 0x0038001A0038001BL;

    /** (0038,001C) VR=DA VM=1 Scheduled Discharge Date (retired)
     *  (0038,001D) VR=TM VM=1 Scheduled Discharge Time (retired) */
    public static final long ScheduledDischargeDateAndTime = 0x0038001C0038001DL;

    /** (0038,0020) VR=DA VM=1 Admitting Date
     *  (0038,0021) VR=TM VM=1 Admitting Time */
    public static final long AdmittingDateAndTime = 0x0038002000380021L;

    /** (0038,0030) VR=DA VM=1 Discharge Date (retired)
     *  (0038,0032) VR=TM VM=1 Discharge Time (retired) */
    public static final long DischargeDateAndTime = 0x0038003000380032L;

    /** (0040,0002) VR=DA VM=1 Scheduled Procedure Step Start Date
     *  (0040,0003) VR=TM VM=1 Scheduled Procedure Step Start Time */
    public static final long ScheduledProcedureStepStartDateAndTime = 0x0040000200400003L;

    /** (0040,0004) VR=DA VM=1 Scheduled Procedure Step End Date
     *  (0040,0005) VR=TM VM=1 Scheduled Procedure Step End Time */
    public static final long ScheduledProcedureStepEndDateAndTime = 0x0040000400400005L;

    /** (0040,0244) VR=DA VM=1 Performed Procedure Step Start Date
     *  (0040,0245) VR=TM VM=1 Performed Procedure Step Start Time */
    public static final long PerformedProcedureStepStartDateAndTime = 0x0040024400400245L;

    /** (0040,0250) VR=DA VM=1 Performed Procedure Step End Date
     *  (0040,0251) VR=TM VM=1 Performed Procedure Step End Time */
    public static final long PerformedProcedureStepEndDateAndTime = 0x0040025000400251L;

    /** (0040,2004) VR=DA VM=1 Issue Date of Imaging Service Request
     *  (0040,2005) VR=TM VM=1 Issue Time of Imaging Service Request */
    public static final long IssueDateAndTimeOfImagingServiceRequest = 0x0040200400402005L;

    /** (0040,A121) VR=DA VM=1 Date
     *  (0040,A122) VR=TM VM=1 Time */
    public static final long DateAndTime = 0x0040A1210040A122L;

    /** (0070,0082) VR=DA VM=1 Presentation Creation Date
     *  (0070,0083) VR=TM VM=1 Presentation Creation Time */
    public static final long PresentationCreationDateAndTime = 0x0070008200700083L;

    /** (2100,0040) VR=DA VM=1 Creation Date
     *  (2100,0050) VR=TM VM=1 Creation Time */
    public static final long CreationDateAndTime = 0x2100004021000050L;

    /** (3006,0008) VR=DA VM=1 Structure Set Date
     *  (3006,0009) VR=TM VM=1 Structure Set Time */
    public static final long StructureSetDateAndTime = 0x3006000830060009L;

    /** (3008,0024) VR=DA VM=1 Treatment Control Point Date
     *  (3008,0025) VR=TM VM=1 Treatment Control Point Time */
    public static final long TreatmentControlPointDateAndTime = 0x3008002430080025L;

    /** (3008,0162) VR=DA VM=1 Safe Position Exit Date
     *  (3008,0164) VR=TM VM=1 Safe Position Exit Time */
    public static final long SafePositionExitDateAndTime = 0x3008016230080164L;

    /** (3008,0166) VR=DA VM=1 Safe Position Return Date
     *  (3008,0168) VR=TM VM=1 Safe Position Return Time */
    public static final long SafePositionReturnDateAndTime = 0x3008016630080168L;

    /** (3008,0250) VR=DA VM=1 Treatment Date
     *  (3008,0251) VR=TM VM=1 Treatment Time */
    public static final long TreatmentDateAndTime = 0x3008025030080251L;

    /** (300A,0006) VR=DA VM=1 RT Plan Date
     *  (300A,0007) VR=TM VM=1 RT Plan Time */
    public static final long RTPlanDateAndTime = 0x300A0006300A0007L;

    /** (300A,022C) VR=DA VM=1 Source Strength Reference Date
     *  (300A,022E) VR=TM VM=1 Source Strength Reference Time */
    public static final long SourceStrengthReferenceDateAndTime = 0x300A022C300A022EL;

    /** (300E,0004) VR=DA VM=1 Review Date
     *  (300E,0005) VR=TM VM=1 Review Time */
    public static final long ReviewDateAndTime = 0x300E0004300E0005L;

    /** (4008,0100) VR=DA VM=1 Interpretation Recorded Date (retired)
     *  (4008,0101) VR=TM VM=1 Interpretation Recorded Time (retired) */
    public static final long InterpretationRecordedDateAndTime = 0x4008010040080101L;

    /** (4008,0108) VR=DA VM=1 Interpretation Transcription Date (retired)
     *  (4008,0109) VR=TM VM=1 Interpretation Transcription Time (retired) */
    public static final long InterpretationTranscriptionDateAndTime = 0x4008010840080109L;

    /** (4008,0112) VR=DA VM=1 Interpretation Approval Date (retired)
     *  (4008,0113) VR=TM VM=1 Interpretation Approval Time (retired) */
    public static final long InterpretationApprovalDateAndTime = 0x4008011240080113L;
}
</xsl:text>
  </xsl:template>
  <xsl:template match="el">
    <xsl:text>
    /** (</xsl:text>
    <xsl:value-of select="substring(@tag,1,4)" />
    <xsl:text>,</xsl:text>
    <xsl:value-of select="substring(@tag,5,4)" />
    <xsl:text>) VR=</xsl:text>
    <xsl:value-of select="@vr" />
    <xsl:text> VM=</xsl:text>
    <xsl:value-of select="@vm" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="text()" />
    <xsl:if test="@retired='true'">
      <xsl:text> (retired)</xsl:text>
    </xsl:if>
    <xsl:text> */
    public static final int </xsl:text>
    <xsl:value-of select="@keyword" />
    <xsl:text> = 0x</xsl:text>
    <xsl:value-of select="translate(@tag,'x','0')" />
    <xsl:text>;
</xsl:text>
  </xsl:template>
</xsl:stylesheet>