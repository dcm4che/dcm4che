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
 * This file is generated from Part 6 and 7 of the Standard Text Edition 2011.
 */
 
package org.dcm4che3.data;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Tag {

    /** (0000,0000) VR=UL VM=1 Command Group Length */
    public static final int CommandGroupLength = 0x00000000;

    /** (0000,0001) VR=UL VM=1 Command Length to End (retired) */
    public static final int CommandLengthToEnd = 0x00000001;

    /** (0000,0002) VR=UI VM=1 Affected SOP Class UID */
    public static final int AffectedSOPClassUID = 0x00000002;

    /** (0000,0003) VR=UI VM=1 Requested SOP Class UID */
    public static final int RequestedSOPClassUID = 0x00000003;

    /** (0000,0010) VR=SH VM=1 Command Recognition Code (retired) */
    public static final int CommandRecognitionCode = 0x00000010;

    /** (0000,0100) VR=US VM=1 Command Field */
    public static final int CommandField = 0x00000100;

    /** (0000,0110) VR=US VM=1 Message ID */
    public static final int MessageID = 0x00000110;

    /** (0000,0120) VR=US VM=1 Message ID Being Responded To */
    public static final int MessageIDBeingRespondedTo = 0x00000120;

    /** (0000,0200) VR=AE VM=1 Initiator (retired) */
    public static final int Initiator = 0x00000200;

    /** (0000,0300) VR=AE VM=1 Receiver (retired) */
    public static final int Receiver = 0x00000300;

    /** (0000,0400) VR=AE VM=1 Find Location (retired) */
    public static final int FindLocation = 0x00000400;

    /** (0000,0600) VR=AE VM=1 Move Destination */
    public static final int MoveDestination = 0x00000600;

    /** (0000,0700) VR=US VM=1 Priority */
    public static final int Priority = 0x00000700;

    /** (0000,0800) VR=US VM=1 Command Data Set Type */
    public static final int CommandDataSetType = 0x00000800;

    /** (0000,0850) VR=US VM=1 Number of Matches (retired) */
    public static final int NumberOfMatches = 0x00000850;

    /** (0000,0860) VR=US VM=1 Response Sequence Number (retired) */
    public static final int ResponseSequenceNumber = 0x00000860;

    /** (0000,0900) VR=US VM=1 Status */
    public static final int Status = 0x00000900;

    /** (0000,0901) VR=AT VM=1-n Offending Element */
    public static final int OffendingElement = 0x00000901;

    /** (0000,0902) VR=LO VM=1 Error Comment */
    public static final int ErrorComment = 0x00000902;

    /** (0000,0903) VR=US VM=1 Error ID */
    public static final int ErrorID = 0x00000903;

    /** (0000,1000) VR=UI VM=1 Affected SOP Instance UID */
    public static final int AffectedSOPInstanceUID = 0x00001000;

    /** (0000,1001) VR=UI VM=1 Requested SOP Instance UID */
    public static final int RequestedSOPInstanceUID = 0x00001001;

    /** (0000,1002) VR=US VM=1 Event Type ID */
    public static final int EventTypeID = 0x00001002;

    /** (0000,1005) VR=AT VM=1-n Attribute Identifier List */
    public static final int AttributeIdentifierList = 0x00001005;

    /** (0000,1008) VR=US VM=1 Action Type ID */
    public static final int ActionTypeID = 0x00001008;

    /** (0000,1020) VR=US VM=1 Number of Remaining Sub-operations */
    public static final int NumberOfRemainingSuboperations = 0x00001020;

    /** (0000,1021) VR=US VM=1 Number of Completed Sub-operations */
    public static final int NumberOfCompletedSuboperations = 0x00001021;

    /** (0000,1022) VR=US VM=1 Number of Failed Sub-operations */
    public static final int NumberOfFailedSuboperations = 0x00001022;

    /** (0000,1023) VR=US VM=1 Number of Warning Sub-operations */
    public static final int NumberOfWarningSuboperations = 0x00001023;

    /** (0000,1030) VR=AE VM=1 Move Originator Application Entity Title */
    public static final int MoveOriginatorApplicationEntityTitle = 0x00001030;

    /** (0000,1031) VR=US VM=1 Move Originator Message ID */
    public static final int MoveOriginatorMessageID = 0x00001031;

    /** (0000,4000) VR=LT VM=1 Dialog Receiver (retired) */
    public static final int DialogReceiver = 0x00004000;

    /** (0000,4010) VR=LT VM=1 Terminal Type (retired) */
    public static final int TerminalType = 0x00004010;

    /** (0000,5010) VR=SH VM=1 Message Set ID (retired) */
    public static final int MessageSetID = 0x00005010;

    /** (0000,5020) VR=SH VM=1 End Message ID (retired) */
    public static final int EndMessageID = 0x00005020;

    /** (0000,5110) VR=LT VM=1 Display Format (retired) */
    public static final int DisplayFormat = 0x00005110;

    /** (0000,5120) VR=LT VM=1 Page Position ID (retired) */
    public static final int PagePositionID = 0x00005120;

    /** (0000,5130) VR=CS VM=1 Text Format ID (retired) */
    public static final int TextFormatID = 0x00005130;

    /** (0000,5140) VR=CS VM=1 Normal/Reverse (retired) */
    public static final int NormalReverse = 0x00005140;

    /** (0000,5150) VR=CS VM=1 Add Gray Scale (retired) */
    public static final int AddGrayScale = 0x00005150;

    /** (0000,5160) VR=CS VM=1 Borders (retired) */
    public static final int Borders = 0x00005160;

    /** (0000,5170) VR=IS VM=1 Copies (retired) */
    public static final int Copies = 0x00005170;

    /** (0000,5180) VR=CS VM=1 Command Magnification Type (retired) */
    public static final int CommandMagnificationType = 0x00005180;

    /** (0000,5190) VR=CS VM=1 Erase (retired) */
    public static final int Erase = 0x00005190;

    /** (0000,51A0) VR=CS VM=1 Print (retired) */
    public static final int Print = 0x000051A0;

    /** (0000,51B0) VR=US VM=1-n Overlays (retired) */
    public static final int Overlays = 0x000051B0;

    /** (0002,0000) VR=UL VM=1 File Meta Information Group Length */
    public static final int FileMetaInformationGroupLength = 0x00020000;

    /** (0002,0001) VR=OB VM=1 File Meta Information Version */
    public static final int FileMetaInformationVersion = 0x00020001;

    /** (0002,0002) VR=UI VM=1 Media Storage SOP Class UID */
    public static final int MediaStorageSOPClassUID = 0x00020002;

    /** (0002,0003) VR=UI VM=1 Media Storage SOP Instance UID */
    public static final int MediaStorageSOPInstanceUID = 0x00020003;

    /** (0002,0010) VR=UI VM=1 Transfer Syntax UID */
    public static final int TransferSyntaxUID = 0x00020010;

    /** (0002,0012) VR=UI VM=1 Implementation Class UID */
    public static final int ImplementationClassUID = 0x00020012;

    /** (0002,0013) VR=SH VM=1 Implementation Version Name */
    public static final int ImplementationVersionName = 0x00020013;

    /** (0002,0016) VR=AE VM=1 Source Application Entity Title */
    public static final int SourceApplicationEntityTitle = 0x00020016;

    /** (0002,0017) VR=AE VM=1 Sending Application Entity Title */
    public static final int SendingApplicationEntityTitle = 0x00020017;

    /** (0002,0018) VR=AE VM=1 Receiving Application Entity Title */
    public static final int ReceivingApplicationEntityTitle = 0x00020018;

    /** (0002,0026) VR=UR VM=1 Source Presentation Address */
    public static final int SourcePresentationAddress = 0x00020026;

    /** (0002,0027) VR=UR VM=1 Sending Presentation Address */
    public static final int SendingPresentationAddress = 0x00020027;

    /** (0002,0028) VR=UR VM=1 Receiving Presentation Address */
    public static final int ReceivingPresentationAddress = 0x00020028;

    /** (0002,0031) VR=OB VM=1 RTV Meta Information Version */
    public static final int RTVMetaInformationVersion = 0x00020031;

    /** (0002,0032) VR=UI VM=1 RTV Communication SOP Class UID */
    public static final int RTVCommunicationSOPClassUID = 0x00020032;

    /** (0002,0033) VR=UI VM=1 RTV Communication SOP Instance UID */
    public static final int RTVCommunicationSOPInstanceUID = 0x00020033;

    /** (0002,0035) VR=OB VM=1 RTV Source Identifier */
    public static final int RTVSourceIdentifier = 0x00020035;

    /** (0002,0036) VR=OB VM=1 RTV Flow Identifier */
    public static final int RTVFlowIdentifier = 0x00020036;

    /** (0002,0037) VR=UL VM=1 RTV Flow RTP Sampling Rate */
    public static final int RTVFlowRTPSamplingRate = 0x00020037;

    /** (0002,0038) VR=FD VM=1 RTV Flow Actual Frame Duration */
    public static final int RTVFlowActualFrameDuration = 0x00020038;

    /** (0002,0100) VR=UI VM=1 Private Information Creator UID */
    public static final int PrivateInformationCreatorUID = 0x00020100;

    /** (0002,0102) VR=OB VM=1 Private Information */
    public static final int PrivateInformation = 0x00020102;

    /** (0004,1130) VR=CS VM=1 File-set ID */
    public static final int FileSetID = 0x00041130;

    /** (0004,1141) VR=CS VM=1-8 File-set Descriptor File ID */
    public static final int FileSetDescriptorFileID = 0x00041141;

    /** (0004,1142) VR=CS VM=1 Specific Character Set of File-set Descriptor File */
    public static final int SpecificCharacterSetOfFileSetDescriptorFile = 0x00041142;

    /** (0004,1200) VR=UL VM=1 Offset of the First Directory Record of the Root Directory Entity */
    public static final int OffsetOfTheFirstDirectoryRecordOfTheRootDirectoryEntity = 0x00041200;

    /** (0004,1202) VR=UL VM=1 Offset of the Last Directory Record of the Root Directory Entity */
    public static final int OffsetOfTheLastDirectoryRecordOfTheRootDirectoryEntity = 0x00041202;

    /** (0004,1212) VR=US VM=1 File-set Consistency Flag */
    public static final int FileSetConsistencyFlag = 0x00041212;

    /** (0004,1220) VR=SQ VM=1 Directory Record Sequence */
    public static final int DirectoryRecordSequence = 0x00041220;

    /** (0004,1400) VR=UL VM=1 Offset of the Next Directory Record */
    public static final int OffsetOfTheNextDirectoryRecord = 0x00041400;

    /** (0004,1410) VR=US VM=1 Record In-use Flag */
    public static final int RecordInUseFlag = 0x00041410;

    /** (0004,1420) VR=UL VM=1 Offset of Referenced Lower-Level Directory Entity */
    public static final int OffsetOfReferencedLowerLevelDirectoryEntity = 0x00041420;

    /** (0004,1430) VR=CS VM=1 Directory Record Type */
    public static final int DirectoryRecordType = 0x00041430;

    /** (0004,1432) VR=UI VM=1 Private Record UID */
    public static final int PrivateRecordUID = 0x00041432;

    /** (0004,1500) VR=CS VM=1-8 Referenced File ID */
    public static final int ReferencedFileID = 0x00041500;

    /** (0004,1504) VR=UL VM=1 MRDR Directory Record Offset (retired) */
    public static final int MRDRDirectoryRecordOffset = 0x00041504;

    /** (0004,1510) VR=UI VM=1 Referenced SOP Class UID in File */
    public static final int ReferencedSOPClassUIDInFile = 0x00041510;

    /** (0004,1511) VR=UI VM=1 Referenced SOP Instance UID in File */
    public static final int ReferencedSOPInstanceUIDInFile = 0x00041511;

    /** (0004,1512) VR=UI VM=1 Referenced Transfer Syntax UID in File */
    public static final int ReferencedTransferSyntaxUIDInFile = 0x00041512;

    /** (0004,151A) VR=UI VM=1-n Referenced Related General SOP Class UID in File */
    public static final int ReferencedRelatedGeneralSOPClassUIDInFile = 0x0004151A;

    /** (0004,1600) VR=UL VM=1 Number of References (retired) */
    public static final int NumberOfReferences = 0x00041600;

    /** (0008,0001) VR=UL VM=1 Length to End (retired) */
    public static final int LengthToEnd = 0x00080001;

    /** (0008,0005) VR=CS VM=1-n Specific Character Set */
    public static final int SpecificCharacterSet = 0x00080005;

    /** (0008,0006) VR=SQ VM=1 Language Code Sequence */
    public static final int LanguageCodeSequence = 0x00080006;

    /** (0008,0008) VR=CS VM=2-n Image Type */
    public static final int ImageType = 0x00080008;

    /** (0008,0010) VR=SH VM=1 Recognition Code (retired) */
    public static final int RecognitionCode = 0x00080010;

    /** (0008,0012) VR=DA VM=1 Instance Creation Date */
    public static final int InstanceCreationDate = 0x00080012;

    /** (0008,0013) VR=TM VM=1 Instance Creation Time */
    public static final int InstanceCreationTime = 0x00080013;

    /** (0008,0014) VR=UI VM=1 Instance Creator UID */
    public static final int InstanceCreatorUID = 0x00080014;

    /** (0008,0015) VR=DT VM=1 Instance Coercion DateTime */
    public static final int InstanceCoercionDateTime = 0x00080015;

    /** (0008,0016) VR=UI VM=1 SOP Class UID */
    public static final int SOPClassUID = 0x00080016;

    /** (0008,0018) VR=UI VM=1 SOP Instance UID */
    public static final int SOPInstanceUID = 0x00080018;

    /** (0008,001A) VR=UI VM=1-n Related General SOP Class UID */
    public static final int RelatedGeneralSOPClassUID = 0x0008001A;

    /** (0008,001B) VR=UI VM=1 Original Specialized SOP Class UID */
    public static final int OriginalSpecializedSOPClassUID = 0x0008001B;

    /** (0008,0020) VR=DA VM=1 Study Date */
    public static final int StudyDate = 0x00080020;

    /** (0008,0021) VR=DA VM=1 Series Date */
    public static final int SeriesDate = 0x00080021;

    /** (0008,0022) VR=DA VM=1 Acquisition Date */
    public static final int AcquisitionDate = 0x00080022;

    /** (0008,0023) VR=DA VM=1 Content Date */
    public static final int ContentDate = 0x00080023;

    /** (0008,0024) VR=DA VM=1 Overlay Date (retired) */
    public static final int OverlayDate = 0x00080024;

    /** (0008,0025) VR=DA VM=1 Curve Date (retired) */
    public static final int CurveDate = 0x00080025;

    /** (0008,002A) VR=DT VM=1 Acquisition DateTime */
    public static final int AcquisitionDateTime = 0x0008002A;

    /** (0008,0030) VR=TM VM=1 Study Time */
    public static final int StudyTime = 0x00080030;

    /** (0008,0031) VR=TM VM=1 Series Time */
    public static final int SeriesTime = 0x00080031;

    /** (0008,0032) VR=TM VM=1 Acquisition Time */
    public static final int AcquisitionTime = 0x00080032;

    /** (0008,0033) VR=TM VM=1 Content Time */
    public static final int ContentTime = 0x00080033;

    /** (0008,0034) VR=TM VM=1 Overlay Time (retired) */
    public static final int OverlayTime = 0x00080034;

    /** (0008,0035) VR=TM VM=1 Curve Time (retired) */
    public static final int CurveTime = 0x00080035;

    /** (0008,0040) VR=US VM=1 Data Set Type (retired) */
    public static final int DataSetType = 0x00080040;

    /** (0008,0041) VR=LO VM=1 Data Set Subtype (retired) */
    public static final int DataSetSubtype = 0x00080041;

    /** (0008,0042) VR=CS VM=1 Nuclear Medicine Series Type (retired) */
    public static final int NuclearMedicineSeriesType = 0x00080042;

    /** (0008,0050) VR=SH VM=1 Accession Number */
    public static final int AccessionNumber = 0x00080050;

    /** (0008,0051) VR=SQ VM=1 Issuer of Accession Number Sequence */
    public static final int IssuerOfAccessionNumberSequence = 0x00080051;

    /** (0008,0052) VR=CS VM=1 Query/Retrieve Level */
    public static final int QueryRetrieveLevel = 0x00080052;

    /** (0008,0053) VR=CS VM=1 Query/Retrieve View */
    public static final int QueryRetrieveView = 0x00080053;

    /** (0008,0054) VR=AE VM=1-n Retrieve AE Title */
    public static final int RetrieveAETitle = 0x00080054;

    /** (0008,0055) VR=AE VM=1 Station  AE Title */
    public static final int StationAETitle = 0x00080055;

    /** (0008,0056) VR=CS VM=1 Instance Availability */
    public static final int InstanceAvailability = 0x00080056;

    /** (0008,0058) VR=UI VM=1-n Failed SOP Instance UID List */
    public static final int FailedSOPInstanceUIDList = 0x00080058;

    /** (0008,0060) VR=CS VM=1 Modality */
    public static final int Modality = 0x00080060;

    /** (0008,0061) VR=CS VM=1-n Modalities in Study */
    public static final int ModalitiesInStudy = 0x00080061;

    /** (0008,0062) VR=UI VM=1-n SOP Classes in Study */
    public static final int SOPClassesInStudy = 0x00080062;

    /** (0008,0063) VR=SQ VM=1 Anatomic Regions in Study Code Sequence */
    public static final int AnatomicRegionsInStudyCodeSequence = 0x00080063;

    /** (0008,0064) VR=CS VM=1 Conversion Type */
    public static final int ConversionType = 0x00080064;

    /** (0008,0068) VR=CS VM=1 Presentation Intent Type */
    public static final int PresentationIntentType = 0x00080068;

    /** (0008,0070) VR=LO VM=1 Manufacturer */
    public static final int Manufacturer = 0x00080070;

    /** (0008,0080) VR=LO VM=1 Institution Name */
    public static final int InstitutionName = 0x00080080;

    /** (0008,0081) VR=ST VM=1 Institution Address */
    public static final int InstitutionAddress = 0x00080081;

    /** (0008,0082) VR=SQ VM=1 Institution Code Sequence */
    public static final int InstitutionCodeSequence = 0x00080082;

    /** (0008,0090) VR=PN VM=1 Referring Physician's Name */
    public static final int ReferringPhysicianName = 0x00080090;

    /** (0008,0092) VR=ST VM=1 Referring Physician's Address */
    public static final int ReferringPhysicianAddress = 0x00080092;

    /** (0008,0094) VR=SH VM=1-n Referring Physician's Telephone Numbers */
    public static final int ReferringPhysicianTelephoneNumbers = 0x00080094;

    /** (0008,0096) VR=SQ VM=1 Referring Physician Identification Sequence */
    public static final int ReferringPhysicianIdentificationSequence = 0x00080096;

    /** (0008,009C) VR=PN VM=1-n Consulting Physician's Name */
    public static final int ConsultingPhysicianName = 0x0008009C;

    /** (0008,009D) VR=SQ VM=1 Consulting Physician Identification Sequence */
    public static final int ConsultingPhysicianIdentificationSequence = 0x0008009D;

    /** (0008,0100) VR=SH VM=1 Code Value */
    public static final int CodeValue = 0x00080100;

    /** (0008,0101) VR=LO VM=1 Extended Code Value */
    public static final int ExtendedCodeValue = 0x00080101;

    /** (0008,0102) VR=SH VM=1 Coding Scheme Designator */
    public static final int CodingSchemeDesignator = 0x00080102;

    /** (0008,0103) VR=SH VM=1 Coding Scheme Version */
    public static final int CodingSchemeVersion = 0x00080103;

    /** (0008,0104) VR=LO VM=1 Code Meaning */
    public static final int CodeMeaning = 0x00080104;

    /** (0008,0105) VR=CS VM=1 Mapping Resource */
    public static final int MappingResource = 0x00080105;

    /** (0008,0106) VR=DT VM=1 Context Group Version */
    public static final int ContextGroupVersion = 0x00080106;

    /** (0008,0107) VR=DT VM=1 Context Group Local Version */
    public static final int ContextGroupLocalVersion = 0x00080107;

    /** (0008,0108) VR=LT VM=1 Extended Code Meaning */
    public static final int ExtendedCodeMeaning = 0x00080108;

    /** (0008,0109) VR=SQ VM=1 Coding Scheme Resources Sequence */
    public static final int CodingSchemeResourcesSequence = 0x00080109;

    /** (0008,010A) VR=CS VM=1 Coding Scheme URL Type */
    public static final int CodingSchemeURLType = 0x0008010A;

    /** (0008,010B) VR=CS VM=1 Context Group Extension Flag */
    public static final int ContextGroupExtensionFlag = 0x0008010B;

    /** (0008,010C) VR=UI VM=1 Coding Scheme UID */
    public static final int CodingSchemeUID = 0x0008010C;

    /** (0008,010D) VR=UI VM=1 Context Group Extension Creator UID */
    public static final int ContextGroupExtensionCreatorUID = 0x0008010D;

    /** (0008,010E) VR=UR VM=1 Coding Scheme URL */
    public static final int CodingSchemeURL = 0x0008010E;

    /** (0008,010F) VR=CS VM=1 Context Identifier */
    public static final int ContextIdentifier = 0x0008010F;

    /** (0008,0110) VR=SQ VM=1 Coding Scheme Identification Sequence */
    public static final int CodingSchemeIdentificationSequence = 0x00080110;

    /** (0008,0112) VR=LO VM=1 Coding Scheme Registry */
    public static final int CodingSchemeRegistry = 0x00080112;

    /** (0008,0114) VR=ST VM=1 Coding Scheme External ID */
    public static final int CodingSchemeExternalID = 0x00080114;

    /** (0008,0115) VR=ST VM=1 Coding Scheme Name */
    public static final int CodingSchemeName = 0x00080115;

    /** (0008,0116) VR=ST VM=1 Coding Scheme Responsible Organization */
    public static final int CodingSchemeResponsibleOrganization = 0x00080116;

    /** (0008,0117) VR=UI VM=1 Context UID */
    public static final int ContextUID = 0x00080117;

    /** (0008,0118) VR=UI VM=1 Mapping Resource UID */
    public static final int MappingResourceUID = 0x00080118;

    /** (0008,0119) VR=UC VM=1 Long Code Value */
    public static final int LongCodeValue = 0x00080119;

    /** (0008,0120) VR=UR VM=1 URN Code Value */
    public static final int URNCodeValue = 0x00080120;

    /** (0008,0121) VR=SQ VM=1 Equivalent Code Sequence */
    public static final int EquivalentCodeSequence = 0x00080121;

    /** (0008,0122) VR=LO VM=1 Mapping Resource Name */
    public static final int MappingResourceName = 0x00080122;

    /** (0008,0123) VR=SQ VM=1 Context Group Identification Sequence */
    public static final int ContextGroupIdentificationSequence = 0x00080123;

    /** (0008,0124) VR=SQ VM=1 Mapping Resource Identification Sequence */
    public static final int MappingResourceIdentificationSequence = 0x00080124;

    /** (0008,0201) VR=SH VM=1 Timezone Offset From UTC */
    public static final int TimezoneOffsetFromUTC = 0x00080201;

    /** (0008,0220) VR=SQ VM=1 Responsible Group Code Sequence */
    public static final int ResponsibleGroupCodeSequence = 0x00080220;

    /** (0008,0221) VR=CS VM=1 Equipment Modality */
    public static final int EquipmentModality = 0x00080221;

    /** (0008,0222) VR=LO VM=1 Manufacturer's Related Model Group */
    public static final int ManufacturerRelatedModelGroup = 0x00080222;

    /** (0008,0300) VR=SQ VM=1 Private Data Element Characteristics Sequence */
    public static final int PrivateDataElementCharacteristicsSequence = 0x00080300;

    /** (0008,0301) VR=US VM=1 Private Group Reference */
    public static final int PrivateGroupReference = 0x00080301;

    /** (0008,0302) VR=LO VM=1 Private Creator Reference */
    public static final int PrivateCreatorReference = 0x00080302;

    /** (0008,0303) VR=CS VM=1 Block Identifying Information Status */
    public static final int BlockIdentifyingInformationStatus = 0x00080303;

    /** (0008,0304) VR=US VM=1-n Nonidentifying Private Elements */
    public static final int NonidentifyingPrivateElements = 0x00080304;

    /** (0008,0306) VR=US VM=1-n Identifying Private Elements */
    public static final int IdentifyingPrivateElements = 0x00080306;

    /** (0008,0305) VR=SQ VM=1 Deidentification Action Sequence */
    public static final int DeidentificationActionSequence = 0x00080305;

    /** (0008,0307) VR=CS VM=1 Deidentification Action */
    public static final int DeidentificationAction = 0x00080307;

    /** (0008,0308) VR=US VM=1 Private Data Element */
    public static final int PrivateDataElement = 0x00080308;

    /** (0008,0309) VR=UL VM=1-3 Private Data Element Value Multiplicity */
    public static final int PrivateDataElementValueMultiplicity = 0x00080309;

    /** (0008,030A) VR=CS VM=1 Private Data Element Value Representation */
    public static final int PrivateDataElementValueRepresentation = 0x0008030A;

    /** (0008,030B) VR=UL VM=1-2 Private Data Element Number of Items */
    public static final int PrivateDataElementNumberOfItems = 0x0008030B;

    /** (0008,030C) VR=UC VM=1 Private Data Element Name */
    public static final int PrivateDataElementName = 0x0008030C;

    /** (0008,030D) VR=UC VM=1 Private Data Element Keyword */
    public static final int PrivateDataElementKeyword = 0x0008030D;

    /** (0008,030E) VR=UT VM=1 Private Data Element Description */
    public static final int PrivateDataElementDescription = 0x0008030E;

    /** (0008,030F) VR=UT VM=1 Private Data Element Encoding */
    public static final int PrivateDataElementEncoding = 0x0008030F;

    /** (0008,0310) VR=SQ VM=1 Private Data Element Definition Sequence */
    public static final int PrivateDataElementDefinitionSequence = 0x00080310;

    /** (0008,1000) VR=AE VM=1 Network ID (retired) */
    public static final int NetworkID = 0x00081000;

    /** (0008,1010) VR=SH VM=1 Station Name */
    public static final int StationName = 0x00081010;

    /** (0008,1030) VR=LO VM=1 Study Description */
    public static final int StudyDescription = 0x00081030;

    /** (0008,1032) VR=SQ VM=1 Procedure Code Sequence */
    public static final int ProcedureCodeSequence = 0x00081032;

    /** (0008,103E) VR=LO VM=1 Series Description */
    public static final int SeriesDescription = 0x0008103E;

    /** (0008,103F) VR=SQ VM=1 Series Description Code Sequence */
    public static final int SeriesDescriptionCodeSequence = 0x0008103F;

    /** (0008,1040) VR=LO VM=1 Institutional Department Name */
    public static final int InstitutionalDepartmentName = 0x00081040;

    /** (0008,1041) VR=SQ VM=1 Institutional Department Type Code Sequence */
    public static final int InstitutionalDepartmentTypeCodeSequence = 0x00081041;

    /** (0008,1048) VR=PN VM=1-n Physician(s) of Record */
    public static final int PhysiciansOfRecord = 0x00081048;

    /** (0008,1049) VR=SQ VM=1 Physician(s) of Record Identification Sequence */
    public static final int PhysiciansOfRecordIdentificationSequence = 0x00081049;

    /** (0008,1050) VR=PN VM=1-n Performing Physician's Name */
    public static final int PerformingPhysicianName = 0x00081050;

    /** (0008,1052) VR=SQ VM=1 Performing Physician Identification Sequence */
    public static final int PerformingPhysicianIdentificationSequence = 0x00081052;

    /** (0008,1060) VR=PN VM=1-n Name of Physician(s) Reading Study */
    public static final int NameOfPhysiciansReadingStudy = 0x00081060;

    /** (0008,1062) VR=SQ VM=1 Physician(s) Reading Study Identification Sequence */
    public static final int PhysiciansReadingStudyIdentificationSequence = 0x00081062;

    /** (0008,1070) VR=PN VM=1-n Operators' Name */
    public static final int OperatorsName = 0x00081070;

    /** (0008,1072) VR=SQ VM=1 Operator Identification Sequence */
    public static final int OperatorIdentificationSequence = 0x00081072;

    /** (0008,1080) VR=LO VM=1-n Admitting Diagnoses Description */
    public static final int AdmittingDiagnosesDescription = 0x00081080;

    /** (0008,1084) VR=SQ VM=1 Admitting Diagnoses Code Sequence */
    public static final int AdmittingDiagnosesCodeSequence = 0x00081084;

    /** (0008,1090) VR=LO VM=1 Manufacturer's Model Name */
    public static final int ManufacturerModelName = 0x00081090;

    /** (0008,1100) VR=SQ VM=1 Referenced Results Sequence (retired) */
    public static final int ReferencedResultsSequence = 0x00081100;

    /** (0008,1110) VR=SQ VM=1 Referenced Study Sequence */
    public static final int ReferencedStudySequence = 0x00081110;

    /** (0008,1111) VR=SQ VM=1 Referenced Performed Procedure Step Sequence */
    public static final int ReferencedPerformedProcedureStepSequence = 0x00081111;

    /** (0008,1115) VR=SQ VM=1 Referenced Series Sequence */
    public static final int ReferencedSeriesSequence = 0x00081115;

    /** (0008,1120) VR=SQ VM=1 Referenced Patient Sequence */
    public static final int ReferencedPatientSequence = 0x00081120;

    /** (0008,1125) VR=SQ VM=1 Referenced Visit Sequence */
    public static final int ReferencedVisitSequence = 0x00081125;

    /** (0008,1130) VR=SQ VM=1 Referenced Overlay Sequence (retired) */
    public static final int ReferencedOverlaySequence = 0x00081130;

    /** (0008,1134) VR=SQ VM=1 Referenced Stereometric Instance Sequence */
    public static final int ReferencedStereometricInstanceSequence = 0x00081134;

    /** (0008,113A) VR=SQ VM=1 Referenced Waveform Sequence */
    public static final int ReferencedWaveformSequence = 0x0008113A;

    /** (0008,1140) VR=SQ VM=1 Referenced Image Sequence */
    public static final int ReferencedImageSequence = 0x00081140;

    /** (0008,1145) VR=SQ VM=1 Referenced Curve Sequence (retired) */
    public static final int ReferencedCurveSequence = 0x00081145;

    /** (0008,114A) VR=SQ VM=1 Referenced Instance Sequence */
    public static final int ReferencedInstanceSequence = 0x0008114A;

    /** (0008,114B) VR=SQ VM=1 Referenced Real World Value Mapping Instance Sequence */
    public static final int ReferencedRealWorldValueMappingInstanceSequence = 0x0008114B;

    /** (0008,1150) VR=UI VM=1 Referenced SOP Class UID */
    public static final int ReferencedSOPClassUID = 0x00081150;

    /** (0008,1155) VR=UI VM=1 Referenced SOP Instance UID */
    public static final int ReferencedSOPInstanceUID = 0x00081155;

    /** (0008,1156) VR=SQ VM=1 Definition Source Sequence */
    public static final int DefinitionSourceSequence = 0x00081156;

    /** (0008,115A) VR=UI VM=1-n SOP Classes Supported */
    public static final int SOPClassesSupported = 0x0008115A;

    /** (0008,1160) VR=IS VM=1-n Referenced Frame Number */
    public static final int ReferencedFrameNumber = 0x00081160;

    /** (0008,1161) VR=UL VM=1-n Simple Frame List */
    public static final int SimpleFrameList = 0x00081161;

    /** (0008,1162) VR=UL VM=3-3n Calculated Frame List */
    public static final int CalculatedFrameList = 0x00081162;

    /** (0008,1163) VR=FD VM=2 Time Range */
    public static final int TimeRange = 0x00081163;

    /** (0008,1164) VR=SQ VM=1 Frame Extraction Sequence */
    public static final int FrameExtractionSequence = 0x00081164;

    /** (0008,1167) VR=UI VM=1 Multi-frame Source SOP Instance UID */
    public static final int MultiFrameSourceSOPInstanceUID = 0x00081167;

    /** (0008,1190) VR=UR VM=1 Retrieve URL */
    public static final int RetrieveURL = 0x00081190;

    /** (0008,1195) VR=UI VM=1 Transaction UID */
    public static final int TransactionUID = 0x00081195;

    /** (0008,1196) VR=US VM=1 Warning Reason */
    public static final int WarningReason = 0x00081196;

    /** (0008,1197) VR=US VM=1 Failure Reason */
    public static final int FailureReason = 0x00081197;

    /** (0008,1198) VR=SQ VM=1 Failed SOP Sequence */
    public static final int FailedSOPSequence = 0x00081198;

    /** (0008,1199) VR=SQ VM=1 Referenced SOP Sequence */
    public static final int ReferencedSOPSequence = 0x00081199;

    /** (0008,119A) VR=SQ VM=1 Other Failures Sequence */
    public static final int OtherFailuresSequence = 0x0008119A;

    /** (0008,1200) VR=SQ VM=1 Studies Containing Other Referenced Instances Sequence */
    public static final int StudiesContainingOtherReferencedInstancesSequence = 0x00081200;

    /** (0008,1250) VR=SQ VM=1 Related Series Sequence */
    public static final int RelatedSeriesSequence = 0x00081250;

    /** (0008,2110) VR=CS VM=1 Lossy Image Compression (Retired) (retired) */
    public static final int LossyImageCompressionRetired = 0x00082110;

    /** (0008,2111) VR=ST VM=1 Derivation Description */
    public static final int DerivationDescription = 0x00082111;

    /** (0008,2112) VR=SQ VM=1 Source Image Sequence */
    public static final int SourceImageSequence = 0x00082112;

    /** (0008,2120) VR=SH VM=1 Stage Name */
    public static final int StageName = 0x00082120;

    /** (0008,2122) VR=IS VM=1 Stage Number */
    public static final int StageNumber = 0x00082122;

    /** (0008,2124) VR=IS VM=1 Number of Stages */
    public static final int NumberOfStages = 0x00082124;

    /** (0008,2127) VR=SH VM=1 View Name */
    public static final int ViewName = 0x00082127;

    /** (0008,2128) VR=IS VM=1 View Number */
    public static final int ViewNumber = 0x00082128;

    /** (0008,2129) VR=IS VM=1 Number of Event Timers */
    public static final int NumberOfEventTimers = 0x00082129;

    /** (0008,212A) VR=IS VM=1 Number of Views in Stage */
    public static final int NumberOfViewsInStage = 0x0008212A;

    /** (0008,2130) VR=DS VM=1-n Event Elapsed Time(s) */
    public static final int EventElapsedTimes = 0x00082130;

    /** (0008,2132) VR=LO VM=1-n Event Timer Name(s) */
    public static final int EventTimerNames = 0x00082132;

    /** (0008,2133) VR=SQ VM=1 Event Timer Sequence */
    public static final int EventTimerSequence = 0x00082133;

    /** (0008,2134) VR=FD VM=1 Event Time Offset */
    public static final int EventTimeOffset = 0x00082134;

    /** (0008,2135) VR=SQ VM=1 Event Code Sequence */
    public static final int EventCodeSequence = 0x00082135;

    /** (0008,2142) VR=IS VM=1 Start Trim */
    public static final int StartTrim = 0x00082142;

    /** (0008,2143) VR=IS VM=1 Stop Trim */
    public static final int StopTrim = 0x00082143;

    /** (0008,2144) VR=IS VM=1 Recommended Display Frame Rate */
    public static final int RecommendedDisplayFrameRate = 0x00082144;

    /** (0008,2200) VR=CS VM=1 Transducer Position (retired) */
    public static final int TransducerPosition = 0x00082200;

    /** (0008,2204) VR=CS VM=1 Transducer Orientation (retired) */
    public static final int TransducerOrientation = 0x00082204;

    /** (0008,2208) VR=CS VM=1 Anatomic Structure (retired) */
    public static final int AnatomicStructure = 0x00082208;

    /** (0008,2218) VR=SQ VM=1 Anatomic Region Sequence */
    public static final int AnatomicRegionSequence = 0x00082218;

    /** (0008,2220) VR=SQ VM=1 Anatomic Region Modifier Sequence */
    public static final int AnatomicRegionModifierSequence = 0x00082220;

    /** (0008,2228) VR=SQ VM=1 Primary Anatomic Structure Sequence */
    public static final int PrimaryAnatomicStructureSequence = 0x00082228;

    /** (0008,2229) VR=SQ VM=1 Anatomic Structure, Space or Region Sequence (retired) */
    public static final int AnatomicStructureSpaceOrRegionSequence = 0x00082229;

    /** (0008,2230) VR=SQ VM=1 Primary Anatomic Structure Modifier Sequence */
    public static final int PrimaryAnatomicStructureModifierSequence = 0x00082230;

    /** (0008,2240) VR=SQ VM=1 Transducer Position Sequence (retired) */
    public static final int TransducerPositionSequence = 0x00082240;

    /** (0008,2242) VR=SQ VM=1 Transducer Position Modifier Sequence (retired) */
    public static final int TransducerPositionModifierSequence = 0x00082242;

    /** (0008,2244) VR=SQ VM=1 Transducer Orientation Sequence (retired) */
    public static final int TransducerOrientationSequence = 0x00082244;

    /** (0008,2246) VR=SQ VM=1 Transducer Orientation Modifier Sequence (retired) */
    public static final int TransducerOrientationModifierSequence = 0x00082246;

    /** (0008,2251) VR=SQ VM=1 Anatomic Structure Space Or Region Code Sequence (Trial) (retired) */
    public static final int AnatomicStructureSpaceOrRegionCodeSequenceTrial = 0x00082251;

    /** (0008,2253) VR=SQ VM=1 Anatomic Portal Of Entrance Code Sequence (Trial) (retired) */
    public static final int AnatomicPortalOfEntranceCodeSequenceTrial = 0x00082253;

    /** (0008,2255) VR=SQ VM=1 Anatomic Approach Direction Code Sequence (Trial) (retired) */
    public static final int AnatomicApproachDirectionCodeSequenceTrial = 0x00082255;

    /** (0008,2256) VR=ST VM=1 Anatomic Perspective Description (Trial) (retired) */
    public static final int AnatomicPerspectiveDescriptionTrial = 0x00082256;

    /** (0008,2257) VR=SQ VM=1 Anatomic Perspective Code Sequence (Trial) (retired) */
    public static final int AnatomicPerspectiveCodeSequenceTrial = 0x00082257;

    /** (0008,2258) VR=ST VM=1 Anatomic Location Of Examining Instrument Description (Trial) (retired) */
    public static final int AnatomicLocationOfExaminingInstrumentDescriptionTrial = 0x00082258;

    /** (0008,2259) VR=SQ VM=1 Anatomic Location Of Examining Instrument Code Sequence (Trial) (retired) */
    public static final int AnatomicLocationOfExaminingInstrumentCodeSequenceTrial = 0x00082259;

    /** (0008,225A) VR=SQ VM=1 Anatomic Structure Space Or Region Modifier Code Sequence (Trial) (retired) */
    public static final int AnatomicStructureSpaceOrRegionModifierCodeSequenceTrial = 0x0008225A;

    /** (0008,225C) VR=SQ VM=1 On Axis Background Anatomic Structure Code Sequence (Trial) (retired) */
    public static final int OnAxisBackgroundAnatomicStructureCodeSequenceTrial = 0x0008225C;

    /** (0008,3001) VR=SQ VM=1 Alternate Representation Sequence */
    public static final int AlternateRepresentationSequence = 0x00083001;

    /** (0008,3002) VR=UI VM=1-n Available Transfer Syntax UID */
    public static final int AvailableTransferSyntaxUID = 0x00083002;

    /** (0008,3010) VR=UI VM=1-n Irradiation Event UID */
    public static final int IrradiationEventUID = 0x00083010;

    /** (0008,3011) VR=SQ VM=1 Source Irradiation Event Sequence */
    public static final int SourceIrradiationEventSequence = 0x00083011;

    /** (0008,3012) VR=UI VM=1 Radiopharmaceutical Administration Event UID */
    public static final int RadiopharmaceuticalAdministrationEventUID = 0x00083012;

    /** (0008,4000) VR=LT VM=1 Identifying Comments (retired) */
    public static final int IdentifyingComments = 0x00084000;

    /** (0008,9007) VR=CS VM=4 Frame Type */
    public static final int FrameType = 0x00089007;

    /** (0008,9092) VR=SQ VM=1 Referenced Image Evidence Sequence */
    public static final int ReferencedImageEvidenceSequence = 0x00089092;

    /** (0008,9121) VR=SQ VM=1 Referenced Raw Data Sequence */
    public static final int ReferencedRawDataSequence = 0x00089121;

    /** (0008,9123) VR=UI VM=1 Creator-Version UID */
    public static final int CreatorVersionUID = 0x00089123;

    /** (0008,9124) VR=SQ VM=1 Derivation Image Sequence */
    public static final int DerivationImageSequence = 0x00089124;

    /** (0008,9154) VR=SQ VM=1 Source Image Evidence Sequence */
    public static final int SourceImageEvidenceSequence = 0x00089154;

    /** (0008,9205) VR=CS VM=1 Pixel Presentation */
    public static final int PixelPresentation = 0x00089205;

    /** (0008,9206) VR=CS VM=1 Volumetric Properties */
    public static final int VolumetricProperties = 0x00089206;

    /** (0008,9207) VR=CS VM=1 Volume Based Calculation Technique */
    public static final int VolumeBasedCalculationTechnique = 0x00089207;

    /** (0008,9208) VR=CS VM=1 Complex Image Component */
    public static final int ComplexImageComponent = 0x00089208;

    /** (0008,9209) VR=CS VM=1 Acquisition Contrast */
    public static final int AcquisitionContrast = 0x00089209;

    /** (0008,9215) VR=SQ VM=1 Derivation Code Sequence */
    public static final int DerivationCodeSequence = 0x00089215;

    /** (0008,9237) VR=SQ VM=1 Referenced Presentation State Sequence */
    public static final int ReferencedPresentationStateSequence = 0x00089237;

    /** (0008,9410) VR=SQ VM=1 Referenced Other Plane Sequence */
    public static final int ReferencedOtherPlaneSequence = 0x00089410;

    /** (0008,9458) VR=SQ VM=1 Frame Display Sequence */
    public static final int FrameDisplaySequence = 0x00089458;

    /** (0008,9459) VR=FL VM=1 Recommended Display Frame Rate in Float */
    public static final int RecommendedDisplayFrameRateInFloat = 0x00089459;

    /** (0008,9460) VR=CS VM=1 Skip Frame Range Flag */
    public static final int SkipFrameRangeFlag = 0x00089460;

    /** (0010,0010) VR=PN VM=1 Patient's Name */
    public static final int PatientName = 0x00100010;

    /** (0010,0020) VR=LO VM=1 Patient ID */
    public static final int PatientID = 0x00100020;

    /** (0010,0021) VR=LO VM=1 Issuer of Patient ID */
    public static final int IssuerOfPatientID = 0x00100021;

    /** (0010,0022) VR=CS VM=1 Type of Patient ID */
    public static final int TypeOfPatientID = 0x00100022;

    /** (0010,0024) VR=SQ VM=1 Issuer of Patient ID Qualifiers Sequence */
    public static final int IssuerOfPatientIDQualifiersSequence = 0x00100024;

    /** (0010,0026) VR=SQ VM=1 Source Patient Group Identification Sequence */
    public static final int SourcePatientGroupIdentificationSequence = 0x00100026;

    /** (0010,0027) VR=SQ VM=1 Group of Patients Identification Sequence */
    public static final int GroupOfPatientsIdentificationSequence = 0x00100027;

    /** (0010,0028) VR=US VM=3 Subject Relative Position in Image */
    public static final int SubjectRelativePositionInImage = 0x00100028;

    /** (0010,0030) VR=DA VM=1 Patient's Birth Date */
    public static final int PatientBirthDate = 0x00100030;

    /** (0010,0032) VR=TM VM=1 Patient's Birth Time */
    public static final int PatientBirthTime = 0x00100032;

    /** (0010,0033) VR=LO VM=1 Patient's Birth Date in Alternative Calendar */
    public static final int PatientBirthDateInAlternativeCalendar = 0x00100033;

    /** (0010,0034) VR=LO VM=1 Patient's Death Date in Alternative Calendar */
    public static final int PatientDeathDateInAlternativeCalendar = 0x00100034;

    /** (0010,0035) VR=CS VM=1 Patient's Alternative Calendar */
    public static final int PatientAlternativeCalendar = 0x00100035;

    /** (0010,0040) VR=CS VM=1 Patient's Sex */
    public static final int PatientSex = 0x00100040;

    /** (0010,0050) VR=SQ VM=1 Patient's Insurance Plan Code Sequence */
    public static final int PatientInsurancePlanCodeSequence = 0x00100050;

    /** (0010,0101) VR=SQ VM=1 Patient's Primary Language Code Sequence */
    public static final int PatientPrimaryLanguageCodeSequence = 0x00100101;

    /** (0010,0102) VR=SQ VM=1 Patient's Primary Language Modifier Code Sequence */
    public static final int PatientPrimaryLanguageModifierCodeSequence = 0x00100102;

    /** (0010,0200) VR=CS VM=1 Quality Control Subject */
    public static final int QualityControlSubject = 0x00100200;

    /** (0010,0201) VR=SQ VM=1 Quality Control Subject Type Code Sequence */
    public static final int QualityControlSubjectTypeCodeSequence = 0x00100201;

    /** (0010,0212) VR=UC VM=1 Strain Description */
    public static final int StrainDescription = 0x00100212;

    /** (0010,0213) VR=LO VM=1 Strain Nomenclature */
    public static final int StrainNomenclature = 0x00100213;

    /** (0010,0214) VR=LO VM=1 Strain Stock Number */
    public static final int StrainStockNumber = 0x00100214;

    /** (0010,0215) VR=SQ VM=1 Strain Source Registry Code Sequence */
    public static final int StrainSourceRegistryCodeSequence = 0x00100215;

    /** (0010,0216) VR=SQ VM=1 Strain Stock Sequence */
    public static final int StrainStockSequence = 0x00100216;

    /** (0010,0217) VR=LO VM=1 Strain Source */
    public static final int StrainSource = 0x00100217;

    /** (0010,0218) VR=UT VM=1 Strain Additional Information */
    public static final int StrainAdditionalInformation = 0x00100218;

    /** (0010,0219) VR=SQ VM=1 Strain Code Sequence */
    public static final int StrainCodeSequence = 0x00100219;

    /** (0010,0221) VR=SQ VM=1 Genetic Modifications Sequence */
    public static final int GeneticModificationsSequence = 0x00100221;

    /** (0010,0222) VR=UC VM=1 Genetic Modifications Description */
    public static final int GeneticModificationsDescription = 0x00100222;

    /** (0010,0223) VR=LO VM=1 Genetic Modifications Nomenclature */
    public static final int GeneticModificationsNomenclature = 0x00100223;

    /** (0010,0229) VR=SQ VM=1 Genetic Modifications Code Sequence */
    public static final int GeneticModificationsCodeSequence = 0x00100229;

    /** (0010,1000) VR=LO VM=1-n Other Patient IDs (retired) */
    public static final int OtherPatientIDs = 0x00101000;

    /** (0010,1001) VR=PN VM=1-n Other Patient Names */
    public static final int OtherPatientNames = 0x00101001;

    /** (0010,1002) VR=SQ VM=1 Other Patient IDs Sequence */
    public static final int OtherPatientIDsSequence = 0x00101002;

    /** (0010,1005) VR=PN VM=1 Patient's Birth Name */
    public static final int PatientBirthName = 0x00101005;

    /** (0010,1010) VR=AS VM=1 Patient's Age */
    public static final int PatientAge = 0x00101010;

    /** (0010,1020) VR=DS VM=1 Patient's Size */
    public static final int PatientSize = 0x00101020;

    /** (0010,1021) VR=SQ VM=1 Patient's Size Code Sequence */
    public static final int PatientSizeCodeSequence = 0x00101021;

    /** (0010,1022) VR=DS VM=1 Patient's Body Mass Index */
    public static final int PatientBodyMassIndex = 0x00101022;

    /** (0010,1023) VR=DS VM=1 Measured AP Dimension */
    public static final int MeasuredAPDimension = 0x00101023;

    /** (0010,1024) VR=DS VM=1 Measured Lateral Dimension */
    public static final int MeasuredLateralDimension = 0x00101024;

    /** (0010,1030) VR=DS VM=1 Patient's Weight */
    public static final int PatientWeight = 0x00101030;

    /** (0010,1040) VR=LO VM=1 Patient's Address */
    public static final int PatientAddress = 0x00101040;

    /** (0010,1050) VR=LO VM=1-n Insurance Plan Identification (retired) */
    public static final int InsurancePlanIdentification = 0x00101050;

    /** (0010,1060) VR=PN VM=1 Patient's Mother's Birth Name */
    public static final int PatientMotherBirthName = 0x00101060;

    /** (0010,1080) VR=LO VM=1 Military Rank */
    public static final int MilitaryRank = 0x00101080;

    /** (0010,1081) VR=LO VM=1 Branch of Service */
    public static final int BranchOfService = 0x00101081;

    /** (0010,1090) VR=LO VM=1 Medical Record Locator (retired) */
    public static final int MedicalRecordLocator = 0x00101090;

    /** (0010,1100) VR=SQ VM=1 Referenced Patient Photo Sequence */
    public static final int ReferencedPatientPhotoSequence = 0x00101100;

    /** (0010,2000) VR=LO VM=1-n Medical Alerts */
    public static final int MedicalAlerts = 0x00102000;

    /** (0010,2110) VR=LO VM=1-n Allergies */
    public static final int Allergies = 0x00102110;

    /** (0010,2150) VR=LO VM=1 Country of Residence */
    public static final int CountryOfResidence = 0x00102150;

    /** (0010,2152) VR=LO VM=1 Region of Residence */
    public static final int RegionOfResidence = 0x00102152;

    /** (0010,2154) VR=SH VM=1-n Patient's Telephone Numbers */
    public static final int PatientTelephoneNumbers = 0x00102154;

    /** (0010,2155) VR=LT VM=1 Patient's Telecom Information */
    public static final int PatientTelecomInformation = 0x00102155;

    /** (0010,2160) VR=SH VM=1 Ethnic Group */
    public static final int EthnicGroup = 0x00102160;

    /** (0010,2180) VR=SH VM=1 Occupation */
    public static final int Occupation = 0x00102180;

    /** (0010,21A0) VR=CS VM=1 Smoking Status */
    public static final int SmokingStatus = 0x001021A0;

    /** (0010,21B0) VR=LT VM=1 Additional Patient History */
    public static final int AdditionalPatientHistory = 0x001021B0;

    /** (0010,21C0) VR=US VM=1 Pregnancy Status */
    public static final int PregnancyStatus = 0x001021C0;

    /** (0010,21D0) VR=DA VM=1 Last Menstrual Date */
    public static final int LastMenstrualDate = 0x001021D0;

    /** (0010,21F0) VR=LO VM=1 Patient's Religious Preference */
    public static final int PatientReligiousPreference = 0x001021F0;

    /** (0010,2201) VR=LO VM=1 Patient Species Description */
    public static final int PatientSpeciesDescription = 0x00102201;

    /** (0010,2202) VR=SQ VM=1 Patient Species Code Sequence */
    public static final int PatientSpeciesCodeSequence = 0x00102202;

    /** (0010,2203) VR=CS VM=1 Patient's Sex Neutered */
    public static final int PatientSexNeutered = 0x00102203;

    /** (0010,2210) VR=CS VM=1 Anatomical Orientation Type */
    public static final int AnatomicalOrientationType = 0x00102210;

    /** (0010,2292) VR=LO VM=1 Patient Breed Description */
    public static final int PatientBreedDescription = 0x00102292;

    /** (0010,2293) VR=SQ VM=1 Patient Breed Code Sequence */
    public static final int PatientBreedCodeSequence = 0x00102293;

    /** (0010,2294) VR=SQ VM=1 Breed Registration Sequence */
    public static final int BreedRegistrationSequence = 0x00102294;

    /** (0010,2295) VR=LO VM=1 Breed Registration Number */
    public static final int BreedRegistrationNumber = 0x00102295;

    /** (0010,2296) VR=SQ VM=1 Breed Registry Code Sequence */
    public static final int BreedRegistryCodeSequence = 0x00102296;

    /** (0010,2297) VR=PN VM=1 Responsible Person */
    public static final int ResponsiblePerson = 0x00102297;

    /** (0010,2298) VR=CS VM=1 Responsible Person Role */
    public static final int ResponsiblePersonRole = 0x00102298;

    /** (0010,2299) VR=LO VM=1 Responsible Organization */
    public static final int ResponsibleOrganization = 0x00102299;

    /** (0010,4000) VR=LT VM=1 Patient Comments */
    public static final int PatientComments = 0x00104000;

    /** (0010,9431) VR=FL VM=1 Examined Body Thickness */
    public static final int ExaminedBodyThickness = 0x00109431;

    /** (0012,0010) VR=LO VM=1 Clinical Trial Sponsor Name */
    public static final int ClinicalTrialSponsorName = 0x00120010;

    /** (0012,0020) VR=LO VM=1 Clinical Trial Protocol ID */
    public static final int ClinicalTrialProtocolID = 0x00120020;

    /** (0012,0021) VR=LO VM=1 Clinical Trial Protocol Name */
    public static final int ClinicalTrialProtocolName = 0x00120021;

    /** (0012,0030) VR=LO VM=1 Clinical Trial Site ID */
    public static final int ClinicalTrialSiteID = 0x00120030;

    /** (0012,0031) VR=LO VM=1 Clinical Trial Site Name */
    public static final int ClinicalTrialSiteName = 0x00120031;

    /** (0012,0040) VR=LO VM=1 Clinical Trial Subject ID */
    public static final int ClinicalTrialSubjectID = 0x00120040;

    /** (0012,0042) VR=LO VM=1 Clinical Trial Subject Reading ID */
    public static final int ClinicalTrialSubjectReadingID = 0x00120042;

    /** (0012,0050) VR=LO VM=1 Clinical Trial Time Point ID */
    public static final int ClinicalTrialTimePointID = 0x00120050;

    /** (0012,0051) VR=ST VM=1 Clinical Trial Time Point Description */
    public static final int ClinicalTrialTimePointDescription = 0x00120051;

    /** (0012,0052) VR=FD VM=1 Longitudinal Temporal Offset from Event */
    public static final int LongitudinalTemporalOffsetFromEvent = 0x00120052;

    /** (0012,0053) VR=CS VM=1 Longitudinal Temporal Event Type */
    public static final int LongitudinalTemporalEventType = 0x00120053;

    /** (0012,0060) VR=LO VM=1 Clinical Trial Coordinating Center Name */
    public static final int ClinicalTrialCoordinatingCenterName = 0x00120060;

    /** (0012,0062) VR=CS VM=1 Patient Identity Removed */
    public static final int PatientIdentityRemoved = 0x00120062;

    /** (0012,0063) VR=LO VM=1-n De-identification Method */
    public static final int DeidentificationMethod = 0x00120063;

    /** (0012,0064) VR=SQ VM=1 De-identification Method Code Sequence */
    public static final int DeidentificationMethodCodeSequence = 0x00120064;

    /** (0012,0071) VR=LO VM=1 Clinical Trial Series ID */
    public static final int ClinicalTrialSeriesID = 0x00120071;

    /** (0012,0072) VR=LO VM=1 Clinical Trial Series Description */
    public static final int ClinicalTrialSeriesDescription = 0x00120072;

    /** (0012,0081) VR=LO VM=1 Clinical Trial Protocol Ethics Committee Name */
    public static final int ClinicalTrialProtocolEthicsCommitteeName = 0x00120081;

    /** (0012,0082) VR=LO VM=1 Clinical Trial Protocol Ethics Committee Approval Number */
    public static final int ClinicalTrialProtocolEthicsCommitteeApprovalNumber = 0x00120082;

    /** (0012,0083) VR=SQ VM=1 Consent for Clinical Trial Use Sequence */
    public static final int ConsentForClinicalTrialUseSequence = 0x00120083;

    /** (0012,0084) VR=CS VM=1 Distribution Type */
    public static final int DistributionType = 0x00120084;

    /** (0012,0085) VR=CS VM=1 Consent for Distribution Flag */
    public static final int ConsentForDistributionFlag = 0x00120085;

    /** (0012,0086) VR=DA VM=1 Ethics Committee Approval Effectiveness Start Date */
    public static final int EthicsCommitteeApprovalEffectivenessStartDate = 0x00120086;

    /** (0012,0087) VR=DA VM=1 Ethics Committee Approval Effectiveness End Date */
    public static final int EthicsCommitteeApprovalEffectivenessEndDate = 0x00120087;

    /** (0014,0023) VR=ST VM=1 CAD File Format (retired) */
    public static final int CADFileFormat = 0x00140023;

    /** (0014,0024) VR=ST VM=1 Component Reference System (retired) */
    public static final int ComponentReferenceSystem = 0x00140024;

    /** (0014,0025) VR=ST VM=1 Component Manufacturing Procedure */
    public static final int ComponentManufacturingProcedure = 0x00140025;

    /** (0014,0028) VR=ST VM=1 Component Manufacturer */
    public static final int ComponentManufacturer = 0x00140028;

    /** (0014,0030) VR=DS VM=1-n Material Thickness */
    public static final int MaterialThickness = 0x00140030;

    /** (0014,0032) VR=DS VM=1-n Material Pipe Diameter */
    public static final int MaterialPipeDiameter = 0x00140032;

    /** (0014,0034) VR=DS VM=1-n Material Isolation Diameter */
    public static final int MaterialIsolationDiameter = 0x00140034;

    /** (0014,0042) VR=ST VM=1 Material Grade */
    public static final int MaterialGrade = 0x00140042;

    /** (0014,0044) VR=ST VM=1 Material Properties Description */
    public static final int MaterialPropertiesDescription = 0x00140044;

    /** (0014,0045) VR=ST VM=1 Material Properties File Format (Retired) (retired) */
    public static final int MaterialPropertiesFileFormatRetired = 0x00140045;

    /** (0014,0046) VR=LT VM=1 Material Notes */
    public static final int MaterialNotes = 0x00140046;

    /** (0014,0050) VR=CS VM=1 Component Shape */
    public static final int ComponentShape = 0x00140050;

    /** (0014,0052) VR=CS VM=1 Curvature Type */
    public static final int CurvatureType = 0x00140052;

    /** (0014,0054) VR=DS VM=1 Outer Diameter */
    public static final int OuterDiameter = 0x00140054;

    /** (0014,0056) VR=DS VM=1 Inner Diameter */
    public static final int InnerDiameter = 0x00140056;

    /** (0014,0100) VR=LO VM=1-n Component Welder IDs */
    public static final int ComponentWelderIDs = 0x00140100;

    /** (0014,0101) VR=CS VM=1 Secondary Approval Status */
    public static final int SecondaryApprovalStatus = 0x00140101;

    /** (0014,0102) VR=DA VM=1 Secondary Review Date */
    public static final int SecondaryReviewDate = 0x00140102;

    /** (0014,0103) VR=TM VM=1 Secondary Review Time */
    public static final int SecondaryReviewTime = 0x00140103;

    /** (0014,0104) VR=PN VM=1 Secondary Reviewer Name */
    public static final int SecondaryReviewerName = 0x00140104;

    /** (0014,0105) VR=ST VM=1 Repair ID */
    public static final int RepairID = 0x00140105;

    /** (0014,0106) VR=SQ VM=1 Multiple Component Approval Sequence */
    public static final int MultipleComponentApprovalSequence = 0x00140106;

    /** (0014,0107) VR=CS VM=1-n Other Approval Status */
    public static final int OtherApprovalStatus = 0x00140107;

    /** (0014,0108) VR=CS VM=1-n Other Secondary Approval Status */
    public static final int OtherSecondaryApprovalStatus = 0x00140108;

    /** (0014,1010) VR=ST VM=1 Actual Environmental Conditions */
    public static final int ActualEnvironmentalConditions = 0x00141010;

    /** (0014,1020) VR=DA VM=1 Expiry Date */
    public static final int ExpiryDate = 0x00141020;

    /** (0014,1040) VR=ST VM=1 Environmental Conditions */
    public static final int EnvironmentalConditions = 0x00141040;

    /** (0014,2002) VR=SQ VM=1 Evaluator Sequence */
    public static final int EvaluatorSequence = 0x00142002;

    /** (0014,2004) VR=IS VM=1 Evaluator Number */
    public static final int EvaluatorNumber = 0x00142004;

    /** (0014,2006) VR=PN VM=1 Evaluator Name */
    public static final int EvaluatorName = 0x00142006;

    /** (0014,2008) VR=IS VM=1 Evaluation Attempt */
    public static final int EvaluationAttempt = 0x00142008;

    /** (0014,2012) VR=SQ VM=1 Indication Sequence */
    public static final int IndicationSequence = 0x00142012;

    /** (0014,2014) VR=IS VM=1 Indication Number */
    public static final int IndicationNumber = 0x00142014;

    /** (0014,2016) VR=SH VM=1 Indication Label */
    public static final int IndicationLabel = 0x00142016;

    /** (0014,2018) VR=ST VM=1 Indication Description */
    public static final int IndicationDescription = 0x00142018;

    /** (0014,201A) VR=CS VM=1-n Indication Type */
    public static final int IndicationType = 0x0014201A;

    /** (0014,201C) VR=CS VM=1 Indication Disposition */
    public static final int IndicationDisposition = 0x0014201C;

    /** (0014,201E) VR=SQ VM=1 Indication ROI Sequence */
    public static final int IndicationROISequence = 0x0014201E;

    /** (0014,2030) VR=SQ VM=1 Indication Physical Property Sequence */
    public static final int IndicationPhysicalPropertySequence = 0x00142030;

    /** (0014,2032) VR=SH VM=1 Property Label */
    public static final int PropertyLabel = 0x00142032;

    /** (0014,2202) VR=IS VM=1 Coordinate System Number of Axes */
    public static final int CoordinateSystemNumberOfAxes = 0x00142202;

    /** (0014,2204) VR=SQ VM=1 Coordinate System Axes Sequence */
    public static final int CoordinateSystemAxesSequence = 0x00142204;

    /** (0014,2206) VR=ST VM=1 Coordinate System Axis Description */
    public static final int CoordinateSystemAxisDescription = 0x00142206;

    /** (0014,2208) VR=CS VM=1 Coordinate System Data Set Mapping */
    public static final int CoordinateSystemDataSetMapping = 0x00142208;

    /** (0014,220A) VR=IS VM=1 Coordinate System Axis Number */
    public static final int CoordinateSystemAxisNumber = 0x0014220A;

    /** (0014,220C) VR=CS VM=1 Coordinate System Axis Type */
    public static final int CoordinateSystemAxisType = 0x0014220C;

    /** (0014,220E) VR=CS VM=1 Coordinate System Axis Units */
    public static final int CoordinateSystemAxisUnits = 0x0014220E;

    /** (0014,2210) VR=OB VM=1 Coordinate System Axis Values */
    public static final int CoordinateSystemAxisValues = 0x00142210;

    /** (0014,2220) VR=SQ VM=1 Coordinate System Transform Sequence */
    public static final int CoordinateSystemTransformSequence = 0x00142220;

    /** (0014,2222) VR=ST VM=1 Transform Description */
    public static final int TransformDescription = 0x00142222;

    /** (0014,2224) VR=IS VM=1 Transform Number of Axes */
    public static final int TransformNumberOfAxes = 0x00142224;

    /** (0014,2226) VR=IS VM=1-n Transform Order of Axes */
    public static final int TransformOrderOfAxes = 0x00142226;

    /** (0014,2228) VR=CS VM=1 Transformed Axis Units */
    public static final int TransformedAxisUnits = 0x00142228;

    /** (0014,222A) VR=DS VM=1-n Coordinate System Transform Rotation and Scale Matrix */
    public static final int CoordinateSystemTransformRotationAndScaleMatrix = 0x0014222A;

    /** (0014,222C) VR=DS VM=1-n Coordinate System Transform Translation Matrix */
    public static final int CoordinateSystemTransformTranslationMatrix = 0x0014222C;

    /** (0014,3011) VR=DS VM=1 Internal Detector Frame Time */
    public static final int InternalDetectorFrameTime = 0x00143011;

    /** (0014,3012) VR=DS VM=1 Number of Frames Integrated */
    public static final int NumberOfFramesIntegrated = 0x00143012;

    /** (0014,3020) VR=SQ VM=1 Detector Temperature Sequence */
    public static final int DetectorTemperatureSequence = 0x00143020;

    /** (0014,3022) VR=ST VM=1 Sensor Name */
    public static final int SensorName = 0x00143022;

    /** (0014,3024) VR=DS VM=1 Horizontal Offset of Sensor */
    public static final int HorizontalOffsetOfSensor = 0x00143024;

    /** (0014,3026) VR=DS VM=1 Vertical Offset of Sensor */
    public static final int VerticalOffsetOfSensor = 0x00143026;

    /** (0014,3028) VR=DS VM=1 Sensor Temperature */
    public static final int SensorTemperature = 0x00143028;

    /** (0014,3040) VR=SQ VM=1 Dark Current Sequence */
    public static final int DarkCurrentSequence = 0x00143040;

    /** (0014,3050) VR=OB or OW VM=1 Dark Current Counts */
    public static final int DarkCurrentCounts = 0x00143050;

    /** (0014,3060) VR=SQ VM=1 Gain Correction Reference Sequence */
    public static final int GainCorrectionReferenceSequence = 0x00143060;

    /** (0014,3070) VR=OB or OW VM=1 Air Counts */
    public static final int AirCounts = 0x00143070;

    /** (0014,3071) VR=DS VM=1 KV Used in Gain Calibration */
    public static final int KVUsedInGainCalibration = 0x00143071;

    /** (0014,3072) VR=DS VM=1 MA Used in Gain Calibration */
    public static final int MAUsedInGainCalibration = 0x00143072;

    /** (0014,3073) VR=DS VM=1 Number of Frames Used for Integration */
    public static final int NumberOfFramesUsedForIntegration = 0x00143073;

    /** (0014,3074) VR=LO VM=1 Filter Material Used in Gain Calibration */
    public static final int FilterMaterialUsedInGainCalibration = 0x00143074;

    /** (0014,3075) VR=DS VM=1 Filter Thickness Used in Gain Calibration */
    public static final int FilterThicknessUsedInGainCalibration = 0x00143075;

    /** (0014,3076) VR=DA VM=1 Date of Gain Calibration */
    public static final int DateOfGainCalibration = 0x00143076;

    /** (0014,3077) VR=TM VM=1 Time of Gain Calibration */
    public static final int TimeOfGainCalibration = 0x00143077;

    /** (0014,3080) VR=OB VM=1 Bad Pixel Image */
    public static final int BadPixelImage = 0x00143080;

    /** (0014,3099) VR=LT VM=1 Calibration Notes */
    public static final int CalibrationNotes = 0x00143099;

    /** (0014,4002) VR=SQ VM=1 Pulser Equipment Sequence */
    public static final int PulserEquipmentSequence = 0x00144002;

    /** (0014,4004) VR=CS VM=1 Pulser Type */
    public static final int PulserType = 0x00144004;

    /** (0014,4006) VR=LT VM=1 Pulser Notes */
    public static final int PulserNotes = 0x00144006;

    /** (0014,4008) VR=SQ VM=1 Receiver Equipment Sequence */
    public static final int ReceiverEquipmentSequence = 0x00144008;

    /** (0014,400A) VR=CS VM=1 Amplifier Type */
    public static final int AmplifierType = 0x0014400A;

    /** (0014,400C) VR=LT VM=1 Receiver Notes */
    public static final int ReceiverNotes = 0x0014400C;

    /** (0014,400E) VR=SQ VM=1 Pre-Amplifier Equipment Sequence */
    public static final int PreAmplifierEquipmentSequence = 0x0014400E;

    /** (0014,400F) VR=LT VM=1 Pre-Amplifier Notes */
    public static final int PreAmplifierNotes = 0x0014400F;

    /** (0014,4010) VR=SQ VM=1 Transmit Transducer Sequence */
    public static final int TransmitTransducerSequence = 0x00144010;

    /** (0014,4011) VR=SQ VM=1 Receive Transducer Sequence */
    public static final int ReceiveTransducerSequence = 0x00144011;

    /** (0014,4012) VR=US VM=1 Number of Elements */
    public static final int NumberOfElements = 0x00144012;

    /** (0014,4013) VR=CS VM=1 Element Shape */
    public static final int ElementShape = 0x00144013;

    /** (0014,4014) VR=DS VM=1 Element Dimension A */
    public static final int ElementDimensionA = 0x00144014;

    /** (0014,4015) VR=DS VM=1 Element Dimension B */
    public static final int ElementDimensionB = 0x00144015;

    /** (0014,4016) VR=DS VM=1 Element Pitch A */
    public static final int ElementPitchA = 0x00144016;

    /** (0014,4017) VR=DS VM=1 Measured Beam Dimension A */
    public static final int MeasuredBeamDimensionA = 0x00144017;

    /** (0014,4018) VR=DS VM=1 Measured Beam Dimension B */
    public static final int MeasuredBeamDimensionB = 0x00144018;

    /** (0014,4019) VR=DS VM=1 Location of Measured Beam Diameter */
    public static final int LocationOfMeasuredBeamDiameter = 0x00144019;

    /** (0014,401A) VR=DS VM=1 Nominal Frequency */
    public static final int NominalFrequency = 0x0014401A;

    /** (0014,401B) VR=DS VM=1 Measured Center Frequency */
    public static final int MeasuredCenterFrequency = 0x0014401B;

    /** (0014,401C) VR=DS VM=1 Measured Bandwidth */
    public static final int MeasuredBandwidth = 0x0014401C;

    /** (0014,401D) VR=DS VM=1 Element Pitch B */
    public static final int ElementPitchB = 0x0014401D;

    /** (0014,4020) VR=SQ VM=1 Pulser Settings Sequence */
    public static final int PulserSettingsSequence = 0x00144020;

    /** (0014,4022) VR=DS VM=1 Pulse Width */
    public static final int PulseWidth = 0x00144022;

    /** (0014,4024) VR=DS VM=1 Excitation Frequency */
    public static final int ExcitationFrequency = 0x00144024;

    /** (0014,4026) VR=CS VM=1 Modulation Type */
    public static final int ModulationType = 0x00144026;

    /** (0014,4028) VR=DS VM=1 Damping */
    public static final int Damping = 0x00144028;

    /** (0014,4030) VR=SQ VM=1 Receiver Settings Sequence */
    public static final int ReceiverSettingsSequence = 0x00144030;

    /** (0014,4031) VR=DS VM=1 Acquired Soundpath Length */
    public static final int AcquiredSoundpathLength = 0x00144031;

    /** (0014,4032) VR=CS VM=1 Acquisition Compression Type */
    public static final int AcquisitionCompressionType = 0x00144032;

    /** (0014,4033) VR=IS VM=1 Acquisition Sample Size */
    public static final int AcquisitionSampleSize = 0x00144033;

    /** (0014,4034) VR=DS VM=1 Rectifier Smoothing */
    public static final int RectifierSmoothing = 0x00144034;

    /** (0014,4035) VR=SQ VM=1 DAC Sequence */
    public static final int DACSequence = 0x00144035;

    /** (0014,4036) VR=CS VM=1 DAC Type */
    public static final int DACType = 0x00144036;

    /** (0014,4038) VR=DS VM=1-n DAC Gain Points */
    public static final int DACGainPoints = 0x00144038;

    /** (0014,403A) VR=DS VM=1-n DAC Time Points */
    public static final int DACTimePoints = 0x0014403A;

    /** (0014,403C) VR=DS VM=1-n DAC Amplitude */
    public static final int DACAmplitude = 0x0014403C;

    /** (0014,4040) VR=SQ VM=1 Pre-Amplifier Settings Sequence */
    public static final int PreAmplifierSettingsSequence = 0x00144040;

    /** (0014,4050) VR=SQ VM=1 Transmit Transducer Settings Sequence */
    public static final int TransmitTransducerSettingsSequence = 0x00144050;

    /** (0014,4051) VR=SQ VM=1 Receive Transducer Settings Sequence */
    public static final int ReceiveTransducerSettingsSequence = 0x00144051;

    /** (0014,4052) VR=DS VM=1 Incident Angle */
    public static final int IncidentAngle = 0x00144052;

    /** (0014,4054) VR=ST VM=1 Coupling Technique */
    public static final int CouplingTechnique = 0x00144054;

    /** (0014,4056) VR=ST VM=1 Coupling Medium */
    public static final int CouplingMedium = 0x00144056;

    /** (0014,4057) VR=DS VM=1 Coupling Velocity */
    public static final int CouplingVelocity = 0x00144057;

    /** (0014,4058) VR=DS VM=1 Probe Center Location X */
    public static final int ProbeCenterLocationX = 0x00144058;

    /** (0014,4059) VR=DS VM=1 Probe Center Location Z */
    public static final int ProbeCenterLocationZ = 0x00144059;

    /** (0014,405A) VR=DS VM=1 Sound Path Length */
    public static final int SoundPathLength = 0x0014405A;

    /** (0014,405C) VR=ST VM=1 Delay Law Identifier */
    public static final int DelayLawIdentifier = 0x0014405C;

    /** (0014,4060) VR=SQ VM=1 Gate Settings Sequence */
    public static final int GateSettingsSequence = 0x00144060;

    /** (0014,4062) VR=DS VM=1 Gate Threshold */
    public static final int GateThreshold = 0x00144062;

    /** (0014,4064) VR=DS VM=1 Velocity of Sound */
    public static final int VelocityOfSound = 0x00144064;

    /** (0014,4070) VR=SQ VM=1 Calibration Settings Sequence */
    public static final int CalibrationSettingsSequence = 0x00144070;

    /** (0014,4072) VR=ST VM=1 Calibration Procedure */
    public static final int CalibrationProcedure = 0x00144072;

    /** (0014,4074) VR=SH VM=1 Procedure Version */
    public static final int ProcedureVersion = 0x00144074;

    /** (0014,4076) VR=DA VM=1 Procedure Creation Date */
    public static final int ProcedureCreationDate = 0x00144076;

    /** (0014,4078) VR=DA VM=1 Procedure Expiration Date */
    public static final int ProcedureExpirationDate = 0x00144078;

    /** (0014,407A) VR=DA VM=1 Procedure Last Modified Date */
    public static final int ProcedureLastModifiedDate = 0x0014407A;

    /** (0014,407C) VR=TM VM=1-n Calibration Time */
    public static final int CalibrationTime = 0x0014407C;

    /** (0014,407E) VR=DA VM=1-n Calibration Date */
    public static final int CalibrationDate = 0x0014407E;

    /** (0014,4080) VR=SQ VM=1 Probe Drive Equipment Sequence */
    public static final int ProbeDriveEquipmentSequence = 0x00144080;

    /** (0014,4081) VR=CS VM=1 Drive Type */
    public static final int DriveType = 0x00144081;

    /** (0014,4082) VR=LT VM=1 Probe Drive Notes */
    public static final int ProbeDriveNotes = 0x00144082;

    /** (0014,4083) VR=SQ VM=1 Drive Probe Sequence */
    public static final int DriveProbeSequence = 0x00144083;

    /** (0014,4084) VR=DS VM=1 Probe Inductance */
    public static final int ProbeInductance = 0x00144084;

    /** (0014,4085) VR=DS VM=1 Probe Resistance */
    public static final int ProbeResistance = 0x00144085;

    /** (0014,4086) VR=SQ VM=1 Receive Probe Sequence */
    public static final int ReceiveProbeSequence = 0x00144086;

    /** (0014,4087) VR=SQ VM=1 Probe Drive Settings Sequence */
    public static final int ProbeDriveSettingsSequence = 0x00144087;

    /** (0014,4088) VR=DS VM=1 Bridge Resistors */
    public static final int BridgeResistors = 0x00144088;

    /** (0014,4089) VR=DS VM=1 Probe Orientation Angle */
    public static final int ProbeOrientationAngle = 0x00144089;

    /** (0014,408B) VR=DS VM=1 User Selected Gain Y */
    public static final int UserSelectedGainY = 0x0014408B;

    /** (0014,408C) VR=DS VM=1 User Selected Phase */
    public static final int UserSelectedPhase = 0x0014408C;

    /** (0014,408D) VR=DS VM=1 User Selected Offset X */
    public static final int UserSelectedOffsetX = 0x0014408D;

    /** (0014,408E) VR=DS VM=1 User Selected Offset Y */
    public static final int UserSelectedOffsetY = 0x0014408E;

    /** (0014,4091) VR=SQ VM=1 Channel Settings Sequence */
    public static final int ChannelSettingsSequence = 0x00144091;

    /** (0014,4092) VR=DS VM=1 Channel Threshold */
    public static final int ChannelThreshold = 0x00144092;

    /** (0014,409A) VR=SQ VM=1 Scanner Settings Sequence */
    public static final int ScannerSettingsSequence = 0x0014409A;

    /** (0014,409B) VR=ST VM=1 Scan Procedure */
    public static final int ScanProcedure = 0x0014409B;

    /** (0014,409C) VR=DS VM=1 Translation Rate X */
    public static final int TranslationRateX = 0x0014409C;

    /** (0014,409D) VR=DS VM=1 Translation Rate Y */
    public static final int TranslationRateY = 0x0014409D;

    /** (0014,409F) VR=DS VM=1 Channel Overlap */
    public static final int ChannelOverlap = 0x0014409F;

    /** (0014,40A0) VR=LO VM=1 Image Quality Indicator Type */
    public static final int ImageQualityIndicatorType = 0x001440A0;

    /** (0014,40A1) VR=LO VM=1 Image Quality Indicator Material */
    public static final int ImageQualityIndicatorMaterial = 0x001440A1;

    /** (0014,40A2) VR=LO VM=1 Image Quality Indicator Size */
    public static final int ImageQualityIndicatorSize = 0x001440A2;

    /** (0014,5002) VR=IS VM=1 LINAC Energy */
    public static final int LINACEnergy = 0x00145002;

    /** (0014,5004) VR=IS VM=1 LINAC Output */
    public static final int LINACOutput = 0x00145004;

    /** (0014,5100) VR=US VM=1 Active Aperture */
    public static final int ActiveAperture = 0x00145100;

    /** (0014,5101) VR=DS VM=1 Total Aperture */
    public static final int TotalAperture = 0x00145101;

    /** (0014,5102) VR=DS VM=1 Aperture Elevation */
    public static final int ApertureElevation = 0x00145102;

    /** (0014,5103) VR=DS VM=1 Main Lobe Angle */
    public static final int MainLobeAngle = 0x00145103;

    /** (0014,5104) VR=DS VM=1 Main Roof Angle */
    public static final int MainRoofAngle = 0x00145104;

    /** (0014,5105) VR=CS VM=1 Connector Type */
    public static final int ConnectorType = 0x00145105;

    /** (0014,5106) VR=SH VM=1 Wedge Model Number */
    public static final int WedgeModelNumber = 0x00145106;

    /** (0014,5107) VR=DS VM=1 Wedge Angle Float */
    public static final int WedgeAngleFloat = 0x00145107;

    /** (0014,5108) VR=DS VM=1 Wedge Roof Angle */
    public static final int WedgeRoofAngle = 0x00145108;

    /** (0014,5109) VR=CS VM=1 Wedge Element 1 Position */
    public static final int WedgeElement1Position = 0x00145109;

    /** (0014,510A) VR=DS VM=1 Wedge Material Velocity */
    public static final int WedgeMaterialVelocity = 0x0014510A;

    /** (0014,510B) VR=SH VM=1 Wedge Material */
    public static final int WedgeMaterial = 0x0014510B;

    /** (0014,510C) VR=DS VM=1 Wedge Offset Z */
    public static final int WedgeOffsetZ = 0x0014510C;

    /** (0014,510D) VR=DS VM=1 Wedge Origin Offset X */
    public static final int WedgeOriginOffsetX = 0x0014510D;

    /** (0014,510E) VR=DS VM=1 Wedge Time Delay */
    public static final int WedgeTimeDelay = 0x0014510E;

    /** (0014,510F) VR=SH VM=1 Wedge Name */
    public static final int WedgeName = 0x0014510F;

    /** (0014,5110) VR=SH VM=1 Wedge Manufacturer Name */
    public static final int WedgeManufacturerName = 0x00145110;

    /** (0014,5111) VR=LO VM=1 Wedge Description */
    public static final int WedgeDescription = 0x00145111;

    /** (0014,5112) VR=DS VM=1 Nominal Beam Angle */
    public static final int NominalBeamAngle = 0x00145112;

    /** (0014,5113) VR=DS VM=1 Wedge Offset X */
    public static final int WedgeOffsetX = 0x00145113;

    /** (0014,5114) VR=DS VM=1 Wedge Offset Y */
    public static final int WedgeOffsetY = 0x00145114;

    /** (0014,5115) VR=DS VM=1 Wedge Total Length */
    public static final int WedgeTotalLength = 0x00145115;

    /** (0014,5116) VR=DS VM=1 Wedge In Contact Length */
    public static final int WedgeInContactLength = 0x00145116;

    /** (0014,5117) VR=DS VM=1 Wedge Front Gap */
    public static final int WedgeFrontGap = 0x00145117;

    /** (0014,5118) VR=DS VM=1 Wedge Total Height */
    public static final int WedgeTotalHeight = 0x00145118;

    /** (0014,5119) VR=DS VM=1 Wedge Front Height */
    public static final int WedgeFrontHeight = 0x00145119;

    /** (0014,511A) VR=DS VM=1 Wedge Rear Height */
    public static final int WedgeRearHeight = 0x0014511A;

    /** (0014,511B) VR=DS VM=1 Wedge Total Width */
    public static final int WedgeTotalWidth = 0x0014511B;

    /** (0014,511C) VR=DS VM=1 Wedge In Contact Width */
    public static final int WedgeInContactWidth = 0x0014511C;

    /** (0014,511D) VR=DS VM=1 Wedge Chamfer Height */
    public static final int WedgeChamferHeight = 0x0014511D;

    /** (0014,511E) VR=CS VM=1 Wedge Curve */
    public static final int WedgeCurve = 0x0014511E;

    /** (0014,511F) VR=DS VM=1 Radius Along the Wedge */
    public static final int RadiusAlongWedge = 0x0014511F;

    /** (0016,0001) VR=DS VM=1 White Point */
    public static final int WhitePoint = 0x00160001;

    /** (0016,0002) VR=DS VM=3 Primary Chromaticities */
    public static final int PrimaryChromaticities = 0x00160002;

    /** (0016,0003) VR=UT VM=1 Battery Level */
    public static final int BatteryLevel = 0x00160003;

    /** (0016,0004) VR=DS VM=1 Exposure Time in Seconds */
    public static final int ExposureTimeInSeconds = 0x00160004;

    /** (0016,0005) VR=DS VM=1 F-Number */
    public static final int FNumber = 0x00160005;

    /** (0016,0006) VR=IS VM=1 OECF Rows */
    public static final int OECFRows = 0x00160006;

    /** (0016,0007) VR=IS VM=1 OECF Columns */
    public static final int OECFColumns = 0x00160007;

    /** (0016,0008) VR=UC VM=1-n OECF Column Names */
    public static final int OECFColumnNames = 0x00160008;

    /** (0016,0009) VR=DS VM=1-n OECF Values */
    public static final int OECFValues = 0x00160009;

    /** (0016,000A) VR=IS VM=1 Spatial Frequency Response Rows */
    public static final int SpatialFrequencyResponseRows = 0x0016000A;

    /** (0016,000B) VR=IS VM=1 Spatial Frequency Response Columns */
    public static final int SpatialFrequencyResponseColumns = 0x0016000B;

    /** (0016,000C) VR=UC VM=1-n Spatial Frequency Response Column Names */
    public static final int SpatialFrequencyResponseColumnNames = 0x0016000C;

    /** (0016,000D) VR=DS VM=1-n Spatial Frequency Response Values */
    public static final int SpatialFrequencyResponseValues = 0x0016000D;

    /** (0016,000E) VR=IS VM=1 Color Filter Array Pattern Rows */
    public static final int ColorFilterArrayPatternRows = 0x0016000E;

    /** (0016,000F) VR=IS VM=1 Color Filter Array Pattern Columns */
    public static final int ColorFilterArrayPatternColumns = 0x0016000F;

    /** (0016,0010) VR=DS VM=1-n Color Filter Array Pattern Values */
    public static final int ColorFilterArrayPatternValues = 0x00160010;

    /** (0016,0011) VR=US VM=1 Flash Firing Status */
    public static final int FlashFiringStatus = 0x00160011;

    /** (0016,0012) VR=US VM=1 Flash Return Status */
    public static final int FlashReturnStatus = 0x00160012;

    /** (0016,0013) VR=US VM=1 Flash Mode */
    public static final int FlashMode = 0x00160013;

    /** (0016,0014) VR=US VM=1 Flash Function Present */
    public static final int FlashFunctionPresent = 0x00160014;

    /** (0016,0015) VR=US VM=1 Flash Red Eye Mode */
    public static final int FlashRedEyeMode = 0x00160015;

    /** (0016,0016) VR=US VM=1 Exposure Program */
    public static final int ExposureProgram = 0x00160016;

    /** (0016,0017) VR=UT VM=1 Spectral Sensitivity */
    public static final int SpectralSensitivity = 0x00160017;

    /** (0016,0018) VR=IS VM=1 Photographic Sensitivity */
    public static final int PhotographicSensitivity = 0x00160018;

    /** (0016,0019) VR=IS VM=1 Self Timer Mode */
    public static final int SelfTimerMode = 0x00160019;

    /** (0016,001A) VR=US VM=1 Sensitivity Type */
    public static final int SensitivityType = 0x0016001A;

    /** (0016,001B) VR=IS VM=1 Standard Output Sensitivity */
    public static final int StandardOutputSensitivity = 0x0016001B;

    /** (0016,001C) VR=IS VM=1 Recommended Exposure Index */
    public static final int RecommendedExposureIndex = 0x0016001C;

    /** (0016,001D) VR=IS VM=1 ISO Speed */
    public static final int ISOSpeed = 0x0016001D;

    /** (0016,001E) VR=IS VM=1 ISO Speed Latitude yyy */
    public static final int ISOSpeedLatitudeyyy = 0x0016001E;

    /** (0016,001F) VR=IS VM=1 ISO Speed Latitude zzz */
    public static final int ISOSpeedLatitudezzz = 0x0016001F;

    /** (0016,0020) VR=UT VM=1 EXIF Version */
    public static final int EXIFVersion = 0x00160020;

    /** (0016,0021) VR=DS VM=1 Shutter Speed Value */
    public static final int ShutterSpeedValue = 0x00160021;

    /** (0016,0022) VR=DS VM=1 Aperture Value */
    public static final int ApertureValue = 0x00160022;

    /** (0016,0023) VR=DS VM=1 Brightness Value */
    public static final int BrightnessValue = 0x00160023;

    /** (0016,0024) VR=DS VM=1 Exposure Bias Value */
    public static final int ExposureBiasValue = 0x00160024;

    /** (0016,0025) VR=DS VM=1 Max Aperture Value */
    public static final int MaxApertureValue = 0x00160025;

    /** (0016,0026) VR=DS VM=1 Subject Distance */
    public static final int SubjectDistance = 0x00160026;

    /** (0016,0027) VR=US VM=1 Metering Mode */
    public static final int MeteringMode = 0x00160027;

    /** (0016,0028) VR=US VM=1 Light Source */
    public static final int LightSource = 0x00160028;

    /** (0016,0029) VR=DS VM=1 Focal Length */
    public static final int FocalLength = 0x00160029;

    /** (0016,002A) VR=IS VM=2-4 Subject Area */
    public static final int SubjectArea = 0x0016002A;

    /** (0016,002B) VR=OB VM=1 Maker Note */
    public static final int MakerNote = 0x0016002B;

    /** (0016,0030) VR=DS VM=1 Temperature */
    public static final int Temperature = 0x00160030;

    /** (0016,0031) VR=DS VM=1 Humidity */
    public static final int Humidity = 0x00160031;

    /** (0016,0032) VR=DS VM=1 Pressure */
    public static final int Pressure = 0x00160032;

    /** (0016,0033) VR=DS VM=1 Water Depth */
    public static final int WaterDepth = 0x00160033;

    /** (0016,0034) VR=DS VM=1 Acceleration */
    public static final int Acceleration = 0x00160034;

    /** (0016,0035) VR=DS VM=1 Camera Elevation Angle */
    public static final int CameraElevationAngle = 0x00160035;

    /** (0016,0036) VR=DS VM=1-2 Flash Energy */
    public static final int FlashEnergy = 0x00160036;

    /** (0016,0037) VR=IS VM=2 Subject Location */
    public static final int SubjectLocation = 0x00160037;

    /** (0016,0038) VR=DS VM=1 Photographic Exposure Index */
    public static final int PhotographicExposureIndex = 0x00160038;

    /** (0016,0039) VR=US VM=1 Sensing Method */
    public static final int SensingMethod = 0x00160039;

    /** (0016,003A) VR=US VM=1 File Source */
    public static final int FileSource = 0x0016003A;

    /** (0016,003B) VR=US VM=1 Scene Type */
    public static final int SceneType = 0x0016003B;

    /** (0016,0041) VR=US VM=1 Custom Rendered */
    public static final int CustomRendered = 0x00160041;

    /** (0016,0042) VR=US VM=1 Exposure Mode */
    public static final int ExposureMode = 0x00160042;

    /** (0016,0043) VR=US VM=1 White Balance */
    public static final int WhiteBalance = 0x00160043;

    /** (0016,0044) VR=DS VM=1 Digital Zoom Ratio */
    public static final int DigitalZoomRatio = 0x00160044;

    /** (0016,0045) VR=IS VM=1 Focal Length In 35mm Film */
    public static final int FocalLengthIn35mmFilm = 0x00160045;

    /** (0016,0046) VR=US VM=1 Scene Capture Type */
    public static final int SceneCaptureType = 0x00160046;

    /** (0016,0047) VR=US VM=1 Gain Control */
    public static final int GainControl = 0x00160047;

    /** (0016,0048) VR=US VM=1 Contrast */
    public static final int Contrast = 0x00160048;

    /** (0016,0049) VR=US VM=1 Saturation */
    public static final int Saturation = 0x00160049;

    /** (0016,004A) VR=US VM=1 Sharpness */
    public static final int Sharpness = 0x0016004A;

    /** (0016,004B) VR=OB VM=1 Device Setting Description */
    public static final int DeviceSettingDescription = 0x0016004B;

    /** (0016,004C) VR=US VM=1 Subject Distance Range */
    public static final int SubjectDistanceRange = 0x0016004C;

    /** (0016,004D) VR=UT VM=1 Camera Owner Name */
    public static final int CameraOwnerName = 0x0016004D;

    /** (0016,004E) VR=DS VM=4 Lens Specification */
    public static final int LensSpecification = 0x0016004E;

    /** (0016,004F) VR=UT VM=1 Lens Make */
    public static final int LensMake = 0x0016004F;

    /** (0016,0050) VR=UT VM=1 Lens Model */
    public static final int LensModel = 0x00160050;

    /** (0016,0051) VR=UT VM=1 Lens Serial Number */
    public static final int LensSerialNumber = 0x00160051;

    /** (0016,0061) VR=CS VM=1 Interoperability Index */
    public static final int InteroperabilityIndex = 0x00160061;

    /** (0016,0062) VR=OB VM=1 Interoperability Version */
    public static final int InteroperabilityVersion = 0x00160062;

    /** (0016,0070) VR=OB VM=1 GPS Version ID */
    public static final int GPSVersionID = 0x00160070;

    /** (0016,0071) VR=CS VM=1 GPS Latitude Ref */
    public static final int GPSLatitudeRef = 0x00160071;

    /** (0016,0072) VR=DS VM=3 GPS Latitude */
    public static final int GPSLatitude = 0x00160072;

    /** (0016,0073) VR=CS VM=1 GPS Longitude Ref */
    public static final int GPSLongitudeRef = 0x00160073;

    /** (0016,0074) VR=DS VM=3 GPS Longitude */
    public static final int GPSLongitude = 0x00160074;

    /** (0016,0075) VR=US VM=1 GPS Altitude Ref */
    public static final int GPSAltitudeRef = 0x00160075;

    /** (0016,0076) VR=DS VM=1 GPS Altitude */
    public static final int GPSAltitude = 0x00160076;

    /** (0016,0077) VR=DT VM=1 GPS Time Stamp */
    public static final int GPSTimeStamp = 0x00160077;

    /** (0016,0078) VR=UT VM=1 GPS Satellites */
    public static final int GPSSatellites = 0x00160078;

    /** (0016,0079) VR=CS VM=1 GPS Status */
    public static final int GPSStatus = 0x00160079;

    /** (0016,007A) VR=CS VM=1 GPS Measure Mode */
    public static final int GPSMeasureMode = 0x0016007A;

    /** (0016,007B) VR=DS VM=1 GPS DOP */
    public static final int GPSDOP = 0x0016007B;

    /** (0016,007C) VR=CS VM=1 GPS Speed Ref */
    public static final int GPSSpeedRef = 0x0016007C;

    /** (0016,007D) VR=DS VM=1 GPS Speed */
    public static final int GPSSpeed = 0x0016007D;

    /** (0016,007E) VR=CS VM=1 GPS Track Ref */
    public static final int GPSTrackRef = 0x0016007E;

    /** (0016,007F) VR=DS VM=1 GPS Track */
    public static final int GPSTrack = 0x0016007F;

    /** (0016,0080) VR=CS VM=1 GPS Img Direction Ref */
    public static final int GPSImgDirectionRef = 0x00160080;

    /** (0016,0081) VR=DS VM=1 GPS Img Direction */
    public static final int GPSImgDirection = 0x00160081;

    /** (0016,0082) VR=UT VM=1 GPS Map Datum */
    public static final int GPSMapDatum = 0x00160082;

    /** (0016,0083) VR=CS VM=1 GPS Dest Latitude Ref */
    public static final int GPSDestLatitudeRef = 0x00160083;

    /** (0016,0084) VR=DS VM=3 GPS Dest Latitude */
    public static final int GPSDestLatitude = 0x00160084;

    /** (0016,0085) VR=CS VM=1 GPS Dest Longitude Ref */
    public static final int GPSDestLongitudeRef = 0x00160085;

    /** (0016,0086) VR=DS VM=3 GPS Dest Longitude */
    public static final int GPSDestLongitude = 0x00160086;

    /** (0016,0087) VR=CS VM=1 GPS Dest Bearing Ref */
    public static final int GPSDestBearingRef = 0x00160087;

    /** (0016,0088) VR=DS VM=1 GPS Dest Bearing */
    public static final int GPSDestBearing = 0x00160088;

    /** (0016,0089) VR=CS VM=1 GPS Dest Distance Ref */
    public static final int GPSDestDistanceRef = 0x00160089;

    /** (0016,008A) VR=DS VM=1 GPS Dest Distance */
    public static final int GPSDestDistance = 0x0016008A;

    /** (0016,008B) VR=OB VM=1 GPS Processing Method */
    public static final int GPSProcessingMethod = 0x0016008B;

    /** (0016,008C) VR=OB VM=1 GPS Area Information */
    public static final int GPSAreaInformation = 0x0016008C;

    /** (0016,008D) VR=DT VM=1 GPS Date Stamp */
    public static final int GPSDateStamp = 0x0016008D;

    /** (0016,008E) VR=IS VM=1 GPS Differential */
    public static final int GPSDifferential = 0x0016008E;

    /** (0018,0010) VR=LO VM=1 Contrast/Bolus Agent */
    public static final int ContrastBolusAgent = 0x00180010;

    /** (0018,0012) VR=SQ VM=1 Contrast/Bolus Agent Sequence */
    public static final int ContrastBolusAgentSequence = 0x00180012;

    /** (0018,0013) VR=FL VM=1 Contrast/Bolus T1 Relaxivity */
    public static final int ContrastBolusT1Relaxivity = 0x00180013;

    /** (0018,0014) VR=SQ VM=1 Contrast/Bolus Administration Route Sequence */
    public static final int ContrastBolusAdministrationRouteSequence = 0x00180014;

    /** (0018,0015) VR=CS VM=1 Body Part Examined */
    public static final int BodyPartExamined = 0x00180015;

    /** (0018,0020) VR=CS VM=1-n Scanning Sequence */
    public static final int ScanningSequence = 0x00180020;

    /** (0018,0021) VR=CS VM=1-n Sequence Variant */
    public static final int SequenceVariant = 0x00180021;

    /** (0018,0022) VR=CS VM=1-n Scan Options */
    public static final int ScanOptions = 0x00180022;

    /** (0018,0023) VR=CS VM=1 MR Acquisition Type */
    public static final int MRAcquisitionType = 0x00180023;

    /** (0018,0024) VR=SH VM=1 Sequence Name */
    public static final int SequenceName = 0x00180024;

    /** (0018,0025) VR=CS VM=1 Angio Flag */
    public static final int AngioFlag = 0x00180025;

    /** (0018,0026) VR=SQ VM=1 Intervention Drug Information Sequence */
    public static final int InterventionDrugInformationSequence = 0x00180026;

    /** (0018,0027) VR=TM VM=1 Intervention Drug Stop Time */
    public static final int InterventionDrugStopTime = 0x00180027;

    /** (0018,0028) VR=DS VM=1 Intervention Drug Dose */
    public static final int InterventionDrugDose = 0x00180028;

    /** (0018,0029) VR=SQ VM=1 Intervention Drug Code Sequence */
    public static final int InterventionDrugCodeSequence = 0x00180029;

    /** (0018,002A) VR=SQ VM=1 Additional Drug Sequence */
    public static final int AdditionalDrugSequence = 0x0018002A;

    /** (0018,0030) VR=LO VM=1-n Radionuclide (retired) */
    public static final int Radionuclide = 0x00180030;

    /** (0018,0031) VR=LO VM=1 Radiopharmaceutical */
    public static final int Radiopharmaceutical = 0x00180031;

    /** (0018,0032) VR=DS VM=1 Energy Window Centerline (retired) */
    public static final int EnergyWindowCenterline = 0x00180032;

    /** (0018,0033) VR=DS VM=1-n Energy Window Total Width (retired) */
    public static final int EnergyWindowTotalWidth = 0x00180033;

    /** (0018,0034) VR=LO VM=1 Intervention Drug Name */
    public static final int InterventionDrugName = 0x00180034;

    /** (0018,0035) VR=TM VM=1 Intervention Drug Start Time */
    public static final int InterventionDrugStartTime = 0x00180035;

    /** (0018,0036) VR=SQ VM=1 Intervention Sequence */
    public static final int InterventionSequence = 0x00180036;

    /** (0018,0037) VR=CS VM=1 Therapy Type (retired) */
    public static final int TherapyType = 0x00180037;

    /** (0018,0038) VR=CS VM=1 Intervention Status */
    public static final int InterventionStatus = 0x00180038;

    /** (0018,0039) VR=CS VM=1 Therapy Description (retired) */
    public static final int TherapyDescription = 0x00180039;

    /** (0018,003A) VR=ST VM=1 Intervention Description */
    public static final int InterventionDescription = 0x0018003A;

    /** (0018,0040) VR=IS VM=1 Cine Rate */
    public static final int CineRate = 0x00180040;

    /** (0018,0042) VR=CS VM=1 Initial Cine Run State */
    public static final int InitialCineRunState = 0x00180042;

    /** (0018,0050) VR=DS VM=1 Slice Thickness */
    public static final int SliceThickness = 0x00180050;

    /** (0018,0060) VR=DS VM=1 KVP */
    public static final int KVP = 0x00180060;

    /** (0018,0070) VR=IS VM=1 Counts Accumulated */
    public static final int CountsAccumulated = 0x00180070;

    /** (0018,0071) VR=CS VM=1 Acquisition Termination Condition */
    public static final int AcquisitionTerminationCondition = 0x00180071;

    /** (0018,0072) VR=DS VM=1 Effective Duration */
    public static final int EffectiveDuration = 0x00180072;

    /** (0018,0073) VR=CS VM=1 Acquisition Start Condition */
    public static final int AcquisitionStartCondition = 0x00180073;

    /** (0018,0074) VR=IS VM=1 Acquisition Start Condition Data */
    public static final int AcquisitionStartConditionData = 0x00180074;

    /** (0018,0075) VR=IS VM=1 Acquisition Termination Condition Data */
    public static final int AcquisitionTerminationConditionData = 0x00180075;

    /** (0018,0080) VR=DS VM=1 Repetition Time */
    public static final int RepetitionTime = 0x00180080;

    /** (0018,0081) VR=DS VM=1 Echo Time */
    public static final int EchoTime = 0x00180081;

    /** (0018,0082) VR=DS VM=1 Inversion Time */
    public static final int InversionTime = 0x00180082;

    /** (0018,0083) VR=DS VM=1 Number of Averages */
    public static final int NumberOfAverages = 0x00180083;

    /** (0018,0084) VR=DS VM=1 Imaging Frequency */
    public static final int ImagingFrequency = 0x00180084;

    /** (0018,0085) VR=SH VM=1 Imaged Nucleus */
    public static final int ImagedNucleus = 0x00180085;

    /** (0018,0086) VR=IS VM=1-n Echo Number(s) */
    public static final int EchoNumbers = 0x00180086;

    /** (0018,0087) VR=DS VM=1 Magnetic Field Strength */
    public static final int MagneticFieldStrength = 0x00180087;

    /** (0018,0088) VR=DS VM=1 Spacing Between Slices */
    public static final int SpacingBetweenSlices = 0x00180088;

    /** (0018,0089) VR=IS VM=1 Number of Phase Encoding Steps */
    public static final int NumberOfPhaseEncodingSteps = 0x00180089;

    /** (0018,0090) VR=DS VM=1 Data Collection Diameter */
    public static final int DataCollectionDiameter = 0x00180090;

    /** (0018,0091) VR=IS VM=1 Echo Train Length */
    public static final int EchoTrainLength = 0x00180091;

    /** (0018,0093) VR=DS VM=1 Percent Sampling */
    public static final int PercentSampling = 0x00180093;

    /** (0018,0094) VR=DS VM=1 Percent Phase Field of View */
    public static final int PercentPhaseFieldOfView = 0x00180094;

    /** (0018,0095) VR=DS VM=1 Pixel Bandwidth */
    public static final int PixelBandwidth = 0x00180095;

    /** (0018,1000) VR=LO VM=1 Device Serial Number */
    public static final int DeviceSerialNumber = 0x00181000;

    /** (0018,1002) VR=UI VM=1 Device UID */
    public static final int DeviceUID = 0x00181002;

    /** (0018,1003) VR=LO VM=1 Device ID */
    public static final int DeviceID = 0x00181003;

    /** (0018,1004) VR=LO VM=1 Plate ID */
    public static final int PlateID = 0x00181004;

    /** (0018,1005) VR=LO VM=1 Generator ID */
    public static final int GeneratorID = 0x00181005;

    /** (0018,1006) VR=LO VM=1 Grid ID */
    public static final int GridID = 0x00181006;

    /** (0018,1007) VR=LO VM=1 Cassette ID */
    public static final int CassetteID = 0x00181007;

    /** (0018,1008) VR=LO VM=1 Gantry ID */
    public static final int GantryID = 0x00181008;

    /** (0018,1009) VR=UT VM=1 Unique Device Identifier */
    public static final int UniqueDeviceIdentifier = 0x00181009;

    /** (0018,100A) VR=SQ VM=1 UDI Sequence */
    public static final int UDISequence = 0x0018100A;

    /** (0018,100B) VR=UI VM=1-n Manufacturer's Device Class UID */
    public static final int ManufacturerDeviceClassUID = 0x0018100B;

    /** (0018,1010) VR=LO VM=1 Secondary Capture Device ID */
    public static final int SecondaryCaptureDeviceID = 0x00181010;

    /** (0018,1011) VR=LO VM=1 Hardcopy Creation Device ID (retired) */
    public static final int HardcopyCreationDeviceID = 0x00181011;

    /** (0018,1012) VR=DA VM=1 Date of Secondary Capture */
    public static final int DateOfSecondaryCapture = 0x00181012;

    /** (0018,1014) VR=TM VM=1 Time of Secondary Capture */
    public static final int TimeOfSecondaryCapture = 0x00181014;

    /** (0018,1016) VR=LO VM=1 Secondary Capture Device Manufacturer */
    public static final int SecondaryCaptureDeviceManufacturer = 0x00181016;

    /** (0018,1017) VR=LO VM=1 Hardcopy Device Manufacturer (retired) */
    public static final int HardcopyDeviceManufacturer = 0x00181017;

    /** (0018,1018) VR=LO VM=1 Secondary Capture Device Manufacturer's Model Name */
    public static final int SecondaryCaptureDeviceManufacturerModelName = 0x00181018;

    /** (0018,1019) VR=LO VM=1-n Secondary Capture Device Software Versions */
    public static final int SecondaryCaptureDeviceSoftwareVersions = 0x00181019;

    /** (0018,101A) VR=LO VM=1-n Hardcopy Device Software Version (retired) */
    public static final int HardcopyDeviceSoftwareVersion = 0x0018101A;

    /** (0018,101B) VR=LO VM=1 Hardcopy Device Manufacturer's Model Name (retired) */
    public static final int HardcopyDeviceManufacturerModelName = 0x0018101B;

    /** (0018,1020) VR=LO VM=1-n Software Versions */
    public static final int SoftwareVersions = 0x00181020;

    /** (0018,1022) VR=SH VM=1 Video Image Format Acquired */
    public static final int VideoImageFormatAcquired = 0x00181022;

    /** (0018,1023) VR=LO VM=1 Digital Image Format Acquired */
    public static final int DigitalImageFormatAcquired = 0x00181023;

    /** (0018,1030) VR=LO VM=1 Protocol Name */
    public static final int ProtocolName = 0x00181030;

    /** (0018,1040) VR=LO VM=1 Contrast/Bolus Route */
    public static final int ContrastBolusRoute = 0x00181040;

    /** (0018,1041) VR=DS VM=1 Contrast/Bolus Volume */
    public static final int ContrastBolusVolume = 0x00181041;

    /** (0018,1042) VR=TM VM=1 Contrast/Bolus Start Time */
    public static final int ContrastBolusStartTime = 0x00181042;

    /** (0018,1043) VR=TM VM=1 Contrast/Bolus Stop Time */
    public static final int ContrastBolusStopTime = 0x00181043;

    /** (0018,1044) VR=DS VM=1 Contrast/Bolus Total Dose */
    public static final int ContrastBolusTotalDose = 0x00181044;

    /** (0018,1045) VR=IS VM=1 Syringe Counts */
    public static final int SyringeCounts = 0x00181045;

    /** (0018,1046) VR=DS VM=1-n Contrast Flow Rate */
    public static final int ContrastFlowRate = 0x00181046;

    /** (0018,1047) VR=DS VM=1-n Contrast Flow Duration */
    public static final int ContrastFlowDuration = 0x00181047;

    /** (0018,1048) VR=CS VM=1 Contrast/Bolus Ingredient */
    public static final int ContrastBolusIngredient = 0x00181048;

    /** (0018,1049) VR=DS VM=1 Contrast/Bolus Ingredient Concentration */
    public static final int ContrastBolusIngredientConcentration = 0x00181049;

    /** (0018,1050) VR=DS VM=1 Spatial Resolution */
    public static final int SpatialResolution = 0x00181050;

    /** (0018,1060) VR=DS VM=1 Trigger Time */
    public static final int TriggerTime = 0x00181060;

    /** (0018,1061) VR=LO VM=1 Trigger Source or Type */
    public static final int TriggerSourceOrType = 0x00181061;

    /** (0018,1062) VR=IS VM=1 Nominal Interval */
    public static final int NominalInterval = 0x00181062;

    /** (0018,1063) VR=DS VM=1 Frame Time */
    public static final int FrameTime = 0x00181063;

    /** (0018,1064) VR=LO VM=1 Cardiac Framing Type */
    public static final int CardiacFramingType = 0x00181064;

    /** (0018,1065) VR=DS VM=1-n Frame Time Vector */
    public static final int FrameTimeVector = 0x00181065;

    /** (0018,1066) VR=DS VM=1 Frame Delay */
    public static final int FrameDelay = 0x00181066;

    /** (0018,1067) VR=DS VM=1 Image Trigger Delay */
    public static final int ImageTriggerDelay = 0x00181067;

    /** (0018,1068) VR=DS VM=1 Multiplex Group Time Offset */
    public static final int MultiplexGroupTimeOffset = 0x00181068;

    /** (0018,1069) VR=DS VM=1 Trigger Time Offset */
    public static final int TriggerTimeOffset = 0x00181069;

    /** (0018,106A) VR=CS VM=1 Synchronization Trigger */
    public static final int SynchronizationTrigger = 0x0018106A;

    /** (0018,106C) VR=US VM=2 Synchronization Channel */
    public static final int SynchronizationChannel = 0x0018106C;

    /** (0018,106E) VR=UL VM=1 Trigger Sample Position */
    public static final int TriggerSamplePosition = 0x0018106E;

    /** (0018,1070) VR=LO VM=1 Radiopharmaceutical Route */
    public static final int RadiopharmaceuticalRoute = 0x00181070;

    /** (0018,1071) VR=DS VM=1 Radiopharmaceutical Volume */
    public static final int RadiopharmaceuticalVolume = 0x00181071;

    /** (0018,1072) VR=TM VM=1 Radiopharmaceutical Start Time */
    public static final int RadiopharmaceuticalStartTime = 0x00181072;

    /** (0018,1073) VR=TM VM=1 Radiopharmaceutical Stop Time */
    public static final int RadiopharmaceuticalStopTime = 0x00181073;

    /** (0018,1074) VR=DS VM=1 Radionuclide Total Dose */
    public static final int RadionuclideTotalDose = 0x00181074;

    /** (0018,1075) VR=DS VM=1 Radionuclide Half Life */
    public static final int RadionuclideHalfLife = 0x00181075;

    /** (0018,1076) VR=DS VM=1 Radionuclide Positron Fraction */
    public static final int RadionuclidePositronFraction = 0x00181076;

    /** (0018,1077) VR=DS VM=1 Radiopharmaceutical Specific Activity */
    public static final int RadiopharmaceuticalSpecificActivity = 0x00181077;

    /** (0018,1078) VR=DT VM=1 Radiopharmaceutical Start DateTime */
    public static final int RadiopharmaceuticalStartDateTime = 0x00181078;

    /** (0018,1079) VR=DT VM=1 Radiopharmaceutical Stop DateTime */
    public static final int RadiopharmaceuticalStopDateTime = 0x00181079;

    /** (0018,1080) VR=CS VM=1 Beat Rejection Flag */
    public static final int BeatRejectionFlag = 0x00181080;

    /** (0018,1081) VR=IS VM=1 Low R-R Value */
    public static final int LowRRValue = 0x00181081;

    /** (0018,1082) VR=IS VM=1 High R-R Value */
    public static final int HighRRValue = 0x00181082;

    /** (0018,1083) VR=IS VM=1 Intervals Acquired */
    public static final int IntervalsAcquired = 0x00181083;

    /** (0018,1084) VR=IS VM=1 Intervals Rejected */
    public static final int IntervalsRejected = 0x00181084;

    /** (0018,1085) VR=LO VM=1 PVC Rejection */
    public static final int PVCRejection = 0x00181085;

    /** (0018,1086) VR=IS VM=1 Skip Beats */
    public static final int SkipBeats = 0x00181086;

    /** (0018,1088) VR=IS VM=1 Heart Rate */
    public static final int HeartRate = 0x00181088;

    /** (0018,1090) VR=IS VM=1 Cardiac Number of Images */
    public static final int CardiacNumberOfImages = 0x00181090;

    /** (0018,1094) VR=IS VM=1 Trigger Window */
    public static final int TriggerWindow = 0x00181094;

    /** (0018,1100) VR=DS VM=1 Reconstruction Diameter */
    public static final int ReconstructionDiameter = 0x00181100;

    /** (0018,1110) VR=DS VM=1 Distance Source to Detector */
    public static final int DistanceSourceToDetector = 0x00181110;

    /** (0018,1111) VR=DS VM=1 Distance Source to Patient */
    public static final int DistanceSourceToPatient = 0x00181111;

    /** (0018,1114) VR=DS VM=1 Estimated Radiographic Magnification Factor */
    public static final int EstimatedRadiographicMagnificationFactor = 0x00181114;

    /** (0018,1120) VR=DS VM=1 Gantry/Detector Tilt */
    public static final int GantryDetectorTilt = 0x00181120;

    /** (0018,1121) VR=DS VM=1 Gantry/Detector Slew */
    public static final int GantryDetectorSlew = 0x00181121;

    /** (0018,1130) VR=DS VM=1 Table Height */
    public static final int TableHeight = 0x00181130;

    /** (0018,1131) VR=DS VM=1 Table Traverse */
    public static final int TableTraverse = 0x00181131;

    /** (0018,1134) VR=CS VM=1 Table Motion */
    public static final int TableMotion = 0x00181134;

    /** (0018,1135) VR=DS VM=1-n Table Vertical Increment */
    public static final int TableVerticalIncrement = 0x00181135;

    /** (0018,1136) VR=DS VM=1-n Table Lateral Increment */
    public static final int TableLateralIncrement = 0x00181136;

    /** (0018,1137) VR=DS VM=1-n Table Longitudinal Increment */
    public static final int TableLongitudinalIncrement = 0x00181137;

    /** (0018,1138) VR=DS VM=1 Table Angle */
    public static final int TableAngle = 0x00181138;

    /** (0018,113A) VR=CS VM=1 Table Type */
    public static final int TableType = 0x0018113A;

    /** (0018,1140) VR=CS VM=1 Rotation Direction */
    public static final int RotationDirection = 0x00181140;

    /** (0018,1141) VR=DS VM=1 Angular Position (retired) */
    public static final int AngularPosition = 0x00181141;

    /** (0018,1142) VR=DS VM=1-n Radial Position */
    public static final int RadialPosition = 0x00181142;

    /** (0018,1143) VR=DS VM=1 Scan Arc */
    public static final int ScanArc = 0x00181143;

    /** (0018,1144) VR=DS VM=1 Angular Step */
    public static final int AngularStep = 0x00181144;

    /** (0018,1145) VR=DS VM=1 Center of Rotation Offset */
    public static final int CenterOfRotationOffset = 0x00181145;

    /** (0018,1146) VR=DS VM=1-n Rotation Offset (retired) */
    public static final int RotationOffset = 0x00181146;

    /** (0018,1147) VR=CS VM=1 Field of View Shape */
    public static final int FieldOfViewShape = 0x00181147;

    /** (0018,1149) VR=IS VM=1-2 Field of View Dimension(s) */
    public static final int FieldOfViewDimensions = 0x00181149;

    /** (0018,1150) VR=IS VM=1 Exposure Time */
    public static final int ExposureTime = 0x00181150;

    /** (0018,1151) VR=IS VM=1 X-Ray Tube Current */
    public static final int XRayTubeCurrent = 0x00181151;

    /** (0018,1152) VR=IS VM=1 Exposure */
    public static final int Exposure = 0x00181152;

    /** (0018,1153) VR=IS VM=1 Exposure in µAs */
    public static final int ExposureInuAs = 0x00181153;

    /** (0018,1154) VR=DS VM=1 Average Pulse Width */
    public static final int AveragePulseWidth = 0x00181154;

    /** (0018,1155) VR=CS VM=1 Radiation Setting */
    public static final int RadiationSetting = 0x00181155;

    /** (0018,1156) VR=CS VM=1 Rectification Type */
    public static final int RectificationType = 0x00181156;

    /** (0018,115A) VR=CS VM=1 Radiation Mode */
    public static final int RadiationMode = 0x0018115A;

    /** (0018,115E) VR=DS VM=1 Image and Fluoroscopy Area Dose Product */
    public static final int ImageAndFluoroscopyAreaDoseProduct = 0x0018115E;

    /** (0018,1160) VR=SH VM=1 Filter Type */
    public static final int FilterType = 0x00181160;

    /** (0018,1161) VR=LO VM=1-n Type of Filters */
    public static final int TypeOfFilters = 0x00181161;

    /** (0018,1162) VR=DS VM=1 Intensifier Size */
    public static final int IntensifierSize = 0x00181162;

    /** (0018,1164) VR=DS VM=2 Imager Pixel Spacing */
    public static final int ImagerPixelSpacing = 0x00181164;

    /** (0018,1166) VR=CS VM=1-n Grid */
    public static final int Grid = 0x00181166;

    /** (0018,1170) VR=IS VM=1 Generator Power */
    public static final int GeneratorPower = 0x00181170;

    /** (0018,1180) VR=SH VM=1 Collimator/grid Name */
    public static final int CollimatorGridName = 0x00181180;

    /** (0018,1181) VR=CS VM=1 Collimator Type */
    public static final int CollimatorType = 0x00181181;

    /** (0018,1182) VR=IS VM=1-2 Focal Distance */
    public static final int FocalDistance = 0x00181182;

    /** (0018,1183) VR=DS VM=1-2 X Focus Center */
    public static final int XFocusCenter = 0x00181183;

    /** (0018,1184) VR=DS VM=1-2 Y Focus Center */
    public static final int YFocusCenter = 0x00181184;

    /** (0018,1190) VR=DS VM=1-n Focal Spot(s) */
    public static final int FocalSpots = 0x00181190;

    /** (0018,1191) VR=CS VM=1 Anode Target Material */
    public static final int AnodeTargetMaterial = 0x00181191;

    /** (0018,11A0) VR=DS VM=1 Body Part Thickness */
    public static final int BodyPartThickness = 0x001811A0;

    /** (0018,11A2) VR=DS VM=1 Compression Force */
    public static final int CompressionForce = 0x001811A2;

    /** (0018,11A3) VR=DS VM=1 Compression Pressure */
    public static final int CompressionPressure = 0x001811A3;

    /** (0018,11A4) VR=LO VM=1 Paddle Description */
    public static final int PaddleDescription = 0x001811A4;

    /** (0018,11A5) VR=DS VM=1 Compression Contact Area */
    public static final int CompressionContactArea = 0x001811A5;

    /** (0018,1200) VR=DA VM=1-n Date of Last Calibration */
    public static final int DateOfLastCalibration = 0x00181200;

    /** (0018,1201) VR=TM VM=1-n Time of Last Calibration */
    public static final int TimeOfLastCalibration = 0x00181201;

    /** (0018,1202) VR=DT VM=1 DateTime of Last Calibration */
    public static final int DateTimeOfLastCalibration = 0x00181202;

    /** (0018,1210) VR=SH VM=1-n Convolution Kernel */
    public static final int ConvolutionKernel = 0x00181210;

    /** (0018,1240) VR=IS VM=1-n Upper/Lower Pixel Values (retired) */
    public static final int UpperLowerPixelValues = 0x00181240;

    /** (0018,1242) VR=IS VM=1 Actual Frame Duration */
    public static final int ActualFrameDuration = 0x00181242;

    /** (0018,1243) VR=IS VM=1 Count Rate */
    public static final int CountRate = 0x00181243;

    /** (0018,1244) VR=US VM=1 Preferred Playback Sequencing */
    public static final int PreferredPlaybackSequencing = 0x00181244;

    /** (0018,1250) VR=SH VM=1 Receive Coil Name */
    public static final int ReceiveCoilName = 0x00181250;

    /** (0018,1251) VR=SH VM=1 Transmit Coil Name */
    public static final int TransmitCoilName = 0x00181251;

    /** (0018,1260) VR=SH VM=1 Plate Type */
    public static final int PlateType = 0x00181260;

    /** (0018,1261) VR=LO VM=1 Phosphor Type */
    public static final int PhosphorType = 0x00181261;

    /** (0018,1271) VR=FD VM=1 Water Equivalent Diameter */
    public static final int WaterEquivalentDiameter = 0x00181271;

    /** (0018,1272) VR=SQ VM=1 Water Equivalent Diameter Calculation Method Code Sequence */
    public static final int WaterEquivalentDiameterCalculationMethodCodeSequence = 0x00181272;

    /** (0018,1300) VR=DS VM=1 Scan Velocity */
    public static final int ScanVelocity = 0x00181300;

    /** (0018,1301) VR=CS VM=1-n Whole Body Technique */
    public static final int WholeBodyTechnique = 0x00181301;

    /** (0018,1302) VR=IS VM=1 Scan Length */
    public static final int ScanLength = 0x00181302;

    /** (0018,1310) VR=US VM=4 Acquisition Matrix */
    public static final int AcquisitionMatrix = 0x00181310;

    /** (0018,1312) VR=CS VM=1 In-plane Phase Encoding Direction */
    public static final int InPlanePhaseEncodingDirection = 0x00181312;

    /** (0018,1314) VR=DS VM=1 Flip Angle */
    public static final int FlipAngle = 0x00181314;

    /** (0018,1315) VR=CS VM=1 Variable Flip Angle Flag */
    public static final int VariableFlipAngleFlag = 0x00181315;

    /** (0018,1316) VR=DS VM=1 SAR */
    public static final int SAR = 0x00181316;

    /** (0018,1318) VR=DS VM=1 dB/dt */
    public static final int dBdt = 0x00181318;

    /** (0018,1320) VR=FL VM=1 B1rms */
    public static final int B1rms = 0x00181320;

    /** (0018,1400) VR=LO VM=1 Acquisition Device Processing Description */
    public static final int AcquisitionDeviceProcessingDescription = 0x00181400;

    /** (0018,1401) VR=LO VM=1 Acquisition Device Processing Code */
    public static final int AcquisitionDeviceProcessingCode = 0x00181401;

    /** (0018,1402) VR=CS VM=1 Cassette Orientation */
    public static final int CassetteOrientation = 0x00181402;

    /** (0018,1403) VR=CS VM=1 Cassette Size */
    public static final int CassetteSize = 0x00181403;

    /** (0018,1404) VR=US VM=1 Exposures on Plate */
    public static final int ExposuresOnPlate = 0x00181404;

    /** (0018,1405) VR=IS VM=1 Relative X-Ray Exposure */
    public static final int RelativeXRayExposure = 0x00181405;

    /** (0018,1411) VR=DS VM=1 Exposure Index */
    public static final int ExposureIndex = 0x00181411;

    /** (0018,1412) VR=DS VM=1 Target Exposure Index */
    public static final int TargetExposureIndex = 0x00181412;

    /** (0018,1413) VR=DS VM=1 Deviation Index */
    public static final int DeviationIndex = 0x00181413;

    /** (0018,1450) VR=DS VM=1 Column Angulation */
    public static final int ColumnAngulation = 0x00181450;

    /** (0018,1460) VR=DS VM=1 Tomo Layer Height */
    public static final int TomoLayerHeight = 0x00181460;

    /** (0018,1470) VR=DS VM=1 Tomo Angle */
    public static final int TomoAngle = 0x00181470;

    /** (0018,1480) VR=DS VM=1 Tomo Time */
    public static final int TomoTime = 0x00181480;

    /** (0018,1490) VR=CS VM=1 Tomo Type */
    public static final int TomoType = 0x00181490;

    /** (0018,1491) VR=CS VM=1 Tomo Class */
    public static final int TomoClass = 0x00181491;

    /** (0018,1495) VR=IS VM=1 Number of Tomosynthesis Source Images */
    public static final int NumberOfTomosynthesisSourceImages = 0x00181495;

    /** (0018,1500) VR=CS VM=1 Positioner Motion */
    public static final int PositionerMotion = 0x00181500;

    /** (0018,1508) VR=CS VM=1 Positioner Type */
    public static final int PositionerType = 0x00181508;

    /** (0018,1510) VR=DS VM=1 Positioner Primary Angle */
    public static final int PositionerPrimaryAngle = 0x00181510;

    /** (0018,1511) VR=DS VM=1 Positioner Secondary Angle */
    public static final int PositionerSecondaryAngle = 0x00181511;

    /** (0018,1520) VR=DS VM=1-n Positioner Primary Angle Increment */
    public static final int PositionerPrimaryAngleIncrement = 0x00181520;

    /** (0018,1521) VR=DS VM=1-n Positioner Secondary Angle Increment */
    public static final int PositionerSecondaryAngleIncrement = 0x00181521;

    /** (0018,1530) VR=DS VM=1 Detector Primary Angle */
    public static final int DetectorPrimaryAngle = 0x00181530;

    /** (0018,1531) VR=DS VM=1 Detector Secondary Angle */
    public static final int DetectorSecondaryAngle = 0x00181531;

    /** (0018,1600) VR=CS VM=1-3 Shutter Shape */
    public static final int ShutterShape = 0x00181600;

    /** (0018,1602) VR=IS VM=1 Shutter Left Vertical Edge */
    public static final int ShutterLeftVerticalEdge = 0x00181602;

    /** (0018,1604) VR=IS VM=1 Shutter Right Vertical Edge */
    public static final int ShutterRightVerticalEdge = 0x00181604;

    /** (0018,1606) VR=IS VM=1 Shutter Upper Horizontal Edge */
    public static final int ShutterUpperHorizontalEdge = 0x00181606;

    /** (0018,1608) VR=IS VM=1 Shutter Lower Horizontal Edge */
    public static final int ShutterLowerHorizontalEdge = 0x00181608;

    /** (0018,1610) VR=IS VM=2 Center of Circular Shutter */
    public static final int CenterOfCircularShutter = 0x00181610;

    /** (0018,1612) VR=IS VM=1 Radius of Circular Shutter */
    public static final int RadiusOfCircularShutter = 0x00181612;

    /** (0018,1620) VR=IS VM=2-2n Vertices of the Polygonal Shutter */
    public static final int VerticesOfThePolygonalShutter = 0x00181620;

    /** (0018,1622) VR=US VM=1 Shutter Presentation Value */
    public static final int ShutterPresentationValue = 0x00181622;

    /** (0018,1623) VR=US VM=1 Shutter Overlay Group */
    public static final int ShutterOverlayGroup = 0x00181623;

    /** (0018,1624) VR=US VM=3 Shutter Presentation Color CIELab Value */
    public static final int ShutterPresentationColorCIELabValue = 0x00181624;

    /** (0018,1630) VR=CS VM=1 Outline Shape Type */
    public static final int OutlineShapeType = 0x00181630;

    /** (0018,1631) VR=FD VM=1 Outline Left Vertical Edge */
    public static final int OutlineLeftVerticalEdge = 0x00181631;

    /** (0018,1632) VR=FD VM=1 Outline Right Vertical Edge */
    public static final int OutlineRightVerticalEdge = 0x00181632;

    /** (0018,1633) VR=FD VM=1 Outline Upper Horizontal Edge */
    public static final int OutlineUpperHorizontalEdge = 0x00181633;

    /** (0018,1634) VR=FD VM=1 Outline Lower Horizontal Edge */
    public static final int OutlineLowerHorizontalEdge = 0x00181634;

    /** (0018,1635) VR=FD VM=2 Center of Circular Outline */
    public static final int CenterOfCircularOutline = 0x00181635;

    /** (0018,1636) VR=FD VM=1 Diameter of Circular Outline */
    public static final int DiameterOfCircularOutline = 0x00181636;

    /** (0018,1637) VR=UL VM=1 Number of Polygonal Vertices */
    public static final int NumberOfPolygonalVertices = 0x00181637;

    /** (0018,1638) VR=OF VM=1 Vertices of the Polygonal Outline */
    public static final int VerticesOfThePolygonalOutline = 0x00181638;

    /** (0018,1700) VR=CS VM=1-3 Collimator Shape */
    public static final int CollimatorShape = 0x00181700;

    /** (0018,1702) VR=IS VM=1 Collimator Left Vertical Edge */
    public static final int CollimatorLeftVerticalEdge = 0x00181702;

    /** (0018,1704) VR=IS VM=1 Collimator Right Vertical Edge */
    public static final int CollimatorRightVerticalEdge = 0x00181704;

    /** (0018,1706) VR=IS VM=1 Collimator Upper Horizontal Edge */
    public static final int CollimatorUpperHorizontalEdge = 0x00181706;

    /** (0018,1708) VR=IS VM=1 Collimator Lower Horizontal Edge */
    public static final int CollimatorLowerHorizontalEdge = 0x00181708;

    /** (0018,1710) VR=IS VM=2 Center of Circular Collimator */
    public static final int CenterOfCircularCollimator = 0x00181710;

    /** (0018,1712) VR=IS VM=1 Radius of Circular Collimator */
    public static final int RadiusOfCircularCollimator = 0x00181712;

    /** (0018,1720) VR=IS VM=2-2n Vertices of the Polygonal Collimator */
    public static final int VerticesOfThePolygonalCollimator = 0x00181720;

    /** (0018,1800) VR=CS VM=1 Acquisition Time Synchronized */
    public static final int AcquisitionTimeSynchronized = 0x00181800;

    /** (0018,1801) VR=SH VM=1 Time Source */
    public static final int TimeSource = 0x00181801;

    /** (0018,1802) VR=CS VM=1 Time Distribution Protocol */
    public static final int TimeDistributionProtocol = 0x00181802;

    /** (0018,1803) VR=LO VM=1 NTP Source Address */
    public static final int NTPSourceAddress = 0x00181803;

    /** (0018,2001) VR=IS VM=1-n Page Number Vector */
    public static final int PageNumberVector = 0x00182001;

    /** (0018,2002) VR=SH VM=1-n Frame Label Vector */
    public static final int FrameLabelVector = 0x00182002;

    /** (0018,2003) VR=DS VM=1-n Frame Primary Angle Vector */
    public static final int FramePrimaryAngleVector = 0x00182003;

    /** (0018,2004) VR=DS VM=1-n Frame Secondary Angle Vector */
    public static final int FrameSecondaryAngleVector = 0x00182004;

    /** (0018,2005) VR=DS VM=1-n Slice Location Vector */
    public static final int SliceLocationVector = 0x00182005;

    /** (0018,2006) VR=SH VM=1-n Display Window Label Vector */
    public static final int DisplayWindowLabelVector = 0x00182006;

    /** (0018,2010) VR=DS VM=2 Nominal Scanned Pixel Spacing */
    public static final int NominalScannedPixelSpacing = 0x00182010;

    /** (0018,2020) VR=CS VM=1 Digitizing Device Transport Direction */
    public static final int DigitizingDeviceTransportDirection = 0x00182020;

    /** (0018,2030) VR=DS VM=1 Rotation of Scanned Film */
    public static final int RotationOfScannedFilm = 0x00182030;

    /** (0018,2041) VR=SQ VM=1 Biopsy Target Sequence */
    public static final int BiopsyTargetSequence = 0x00182041;

    /** (0018,2042) VR=UI VM=1 Target UID */
    public static final int TargetUID = 0x00182042;

    /** (0018,2043) VR=FL VM=2 Localizing Cursor Position */
    public static final int LocalizingCursorPosition = 0x00182043;

    /** (0018,2044) VR=FL VM=3 Calculated Target Position */
    public static final int CalculatedTargetPosition = 0x00182044;

    /** (0018,2045) VR=SH VM=1 Target Label */
    public static final int TargetLabel = 0x00182045;

    /** (0018,2046) VR=FL VM=1 Displayed Z Value */
    public static final int DisplayedZValue = 0x00182046;

    /** (0018,3100) VR=CS VM=1 IVUS Acquisition */
    public static final int IVUSAcquisition = 0x00183100;

    /** (0018,3101) VR=DS VM=1 IVUS Pullback Rate */
    public static final int IVUSPullbackRate = 0x00183101;

    /** (0018,3102) VR=DS VM=1 IVUS Gated Rate */
    public static final int IVUSGatedRate = 0x00183102;

    /** (0018,3103) VR=IS VM=1 IVUS Pullback Start Frame Number */
    public static final int IVUSPullbackStartFrameNumber = 0x00183103;

    /** (0018,3104) VR=IS VM=1 IVUS Pullback Stop Frame Number */
    public static final int IVUSPullbackStopFrameNumber = 0x00183104;

    /** (0018,3105) VR=IS VM=1-n Lesion Number */
    public static final int LesionNumber = 0x00183105;

    /** (0018,4000) VR=LT VM=1 Acquisition Comments (retired) */
    public static final int AcquisitionComments = 0x00184000;

    /** (0018,5000) VR=SH VM=1-n Output Power */
    public static final int OutputPower = 0x00185000;

    /** (0018,5010) VR=LO VM=1-n Transducer Data */
    public static final int TransducerData = 0x00185010;

    /** (0018,5012) VR=DS VM=1 Focus Depth */
    public static final int FocusDepth = 0x00185012;

    /** (0018,5020) VR=LO VM=1 Processing Function */
    public static final int ProcessingFunction = 0x00185020;

    /** (0018,5021) VR=LO VM=1 Postprocessing Function (retired) */
    public static final int PostprocessingFunction = 0x00185021;

    /** (0018,5022) VR=DS VM=1 Mechanical Index */
    public static final int MechanicalIndex = 0x00185022;

    /** (0018,5024) VR=DS VM=1 Bone Thermal Index */
    public static final int BoneThermalIndex = 0x00185024;

    /** (0018,5026) VR=DS VM=1 Cranial Thermal Index */
    public static final int CranialThermalIndex = 0x00185026;

    /** (0018,5027) VR=DS VM=1 Soft Tissue Thermal Index */
    public static final int SoftTissueThermalIndex = 0x00185027;

    /** (0018,5028) VR=DS VM=1 Soft Tissue-focus Thermal Index */
    public static final int SoftTissueFocusThermalIndex = 0x00185028;

    /** (0018,5029) VR=DS VM=1 Soft Tissue-surface Thermal Index */
    public static final int SoftTissueSurfaceThermalIndex = 0x00185029;

    /** (0018,5030) VR=DS VM=1 Dynamic Range (retired) */
    public static final int DynamicRange = 0x00185030;

    /** (0018,5040) VR=DS VM=1 Total Gain (retired) */
    public static final int TotalGain = 0x00185040;

    /** (0018,5050) VR=IS VM=1 Depth of Scan Field */
    public static final int DepthOfScanField = 0x00185050;

    /** (0018,5100) VR=CS VM=1 Patient Position */
    public static final int PatientPosition = 0x00185100;

    /** (0018,5101) VR=CS VM=1 View Position */
    public static final int ViewPosition = 0x00185101;

    /** (0018,5104) VR=SQ VM=1 Projection Eponymous Name Code Sequence */
    public static final int ProjectionEponymousNameCodeSequence = 0x00185104;

    /** (0018,5210) VR=DS VM=6 Image Transformation Matrix (retired) */
    public static final int ImageTransformationMatrix = 0x00185210;

    /** (0018,5212) VR=DS VM=3 Image Translation Vector (retired) */
    public static final int ImageTranslationVector = 0x00185212;

    /** (0018,6000) VR=DS VM=1 Sensitivity */
    public static final int Sensitivity = 0x00186000;

    /** (0018,6011) VR=SQ VM=1 Sequence of Ultrasound Regions */
    public static final int SequenceOfUltrasoundRegions = 0x00186011;

    /** (0018,6012) VR=US VM=1 Region Spatial Format */
    public static final int RegionSpatialFormat = 0x00186012;

    /** (0018,6014) VR=US VM=1 Region Data Type */
    public static final int RegionDataType = 0x00186014;

    /** (0018,6016) VR=UL VM=1 Region Flags */
    public static final int RegionFlags = 0x00186016;

    /** (0018,6018) VR=UL VM=1 Region Location Min X0 */
    public static final int RegionLocationMinX0 = 0x00186018;

    /** (0018,601A) VR=UL VM=1 Region Location Min Y0 */
    public static final int RegionLocationMinY0 = 0x0018601A;

    /** (0018,601C) VR=UL VM=1 Region Location Max X1 */
    public static final int RegionLocationMaxX1 = 0x0018601C;

    /** (0018,601E) VR=UL VM=1 Region Location Max Y1 */
    public static final int RegionLocationMaxY1 = 0x0018601E;

    /** (0018,6020) VR=SL VM=1 Reference Pixel X0 */
    public static final int ReferencePixelX0 = 0x00186020;

    /** (0018,6022) VR=SL VM=1 Reference Pixel Y0 */
    public static final int ReferencePixelY0 = 0x00186022;

    /** (0018,6024) VR=US VM=1 Physical Units X Direction */
    public static final int PhysicalUnitsXDirection = 0x00186024;

    /** (0018,6026) VR=US VM=1 Physical Units Y Direction */
    public static final int PhysicalUnitsYDirection = 0x00186026;

    /** (0018,6028) VR=FD VM=1 Reference Pixel Physical Value X */
    public static final int ReferencePixelPhysicalValueX = 0x00186028;

    /** (0018,602A) VR=FD VM=1 Reference Pixel Physical Value Y */
    public static final int ReferencePixelPhysicalValueY = 0x0018602A;

    /** (0018,602C) VR=FD VM=1 Physical Delta X */
    public static final int PhysicalDeltaX = 0x0018602C;

    /** (0018,602E) VR=FD VM=1 Physical Delta Y */
    public static final int PhysicalDeltaY = 0x0018602E;

    /** (0018,6030) VR=UL VM=1 Transducer Frequency */
    public static final int TransducerFrequency = 0x00186030;

    /** (0018,6031) VR=CS VM=1 Transducer Type */
    public static final int TransducerType = 0x00186031;

    /** (0018,6032) VR=UL VM=1 Pulse Repetition Frequency */
    public static final int PulseRepetitionFrequency = 0x00186032;

    /** (0018,6034) VR=FD VM=1 Doppler Correction Angle */
    public static final int DopplerCorrectionAngle = 0x00186034;

    /** (0018,6036) VR=FD VM=1 Steering Angle */
    public static final int SteeringAngle = 0x00186036;

    /** (0018,6038) VR=UL VM=1 Doppler Sample Volume X Position (Retired) (retired) */
    public static final int DopplerSampleVolumeXPositionRetired = 0x00186038;

    /** (0018,6039) VR=SL VM=1 Doppler Sample Volume X Position */
    public static final int DopplerSampleVolumeXPosition = 0x00186039;

    /** (0018,603A) VR=UL VM=1 Doppler Sample Volume Y Position (Retired) (retired) */
    public static final int DopplerSampleVolumeYPositionRetired = 0x0018603A;

    /** (0018,603B) VR=SL VM=1 Doppler Sample Volume Y Position */
    public static final int DopplerSampleVolumeYPosition = 0x0018603B;

    /** (0018,603C) VR=UL VM=1 TM-Line Position X0 (Retired) (retired) */
    public static final int TMLinePositionX0Retired = 0x0018603C;

    /** (0018,603D) VR=SL VM=1 TM-Line Position X0 */
    public static final int TMLinePositionX0 = 0x0018603D;

    /** (0018,603E) VR=UL VM=1 TM-Line Position Y0 (Retired) (retired) */
    public static final int TMLinePositionY0Retired = 0x0018603E;

    /** (0018,603F) VR=SL VM=1 TM-Line Position Y0 */
    public static final int TMLinePositionY0 = 0x0018603F;

    /** (0018,6040) VR=UL VM=1 TM-Line Position X1 (Retired) (retired) */
    public static final int TMLinePositionX1Retired = 0x00186040;

    /** (0018,6041) VR=SL VM=1 TM-Line Position X1 */
    public static final int TMLinePositionX1 = 0x00186041;

    /** (0018,6042) VR=UL VM=1 TM-Line Position Y1 (Retired) (retired) */
    public static final int TMLinePositionY1Retired = 0x00186042;

    /** (0018,6043) VR=SL VM=1 TM-Line Position Y1 */
    public static final int TMLinePositionY1 = 0x00186043;

    /** (0018,6044) VR=US VM=1 Pixel Component Organization */
    public static final int PixelComponentOrganization = 0x00186044;

    /** (0018,6046) VR=UL VM=1 Pixel Component Mask */
    public static final int PixelComponentMask = 0x00186046;

    /** (0018,6048) VR=UL VM=1 Pixel Component Range Start */
    public static final int PixelComponentRangeStart = 0x00186048;

    /** (0018,604A) VR=UL VM=1 Pixel Component Range Stop */
    public static final int PixelComponentRangeStop = 0x0018604A;

    /** (0018,604C) VR=US VM=1 Pixel Component Physical Units */
    public static final int PixelComponentPhysicalUnits = 0x0018604C;

    /** (0018,604E) VR=US VM=1 Pixel Component Data Type */
    public static final int PixelComponentDataType = 0x0018604E;

    /** (0018,6050) VR=UL VM=1 Number of Table Break Points */
    public static final int NumberOfTableBreakPoints = 0x00186050;

    /** (0018,6052) VR=UL VM=1-n Table of X Break Points */
    public static final int TableOfXBreakPoints = 0x00186052;

    /** (0018,6054) VR=FD VM=1-n Table of Y Break Points */
    public static final int TableOfYBreakPoints = 0x00186054;

    /** (0018,6056) VR=UL VM=1 Number of Table Entries */
    public static final int NumberOfTableEntries = 0x00186056;

    /** (0018,6058) VR=UL VM=1-n Table of Pixel Values */
    public static final int TableOfPixelValues = 0x00186058;

    /** (0018,605A) VR=FL VM=1-n Table of Parameter Values */
    public static final int TableOfParameterValues = 0x0018605A;

    /** (0018,6060) VR=FL VM=1-n R Wave Time Vector */
    public static final int RWaveTimeVector = 0x00186060;

    /** (0018,7000) VR=CS VM=1 Detector Conditions Nominal Flag */
    public static final int DetectorConditionsNominalFlag = 0x00187000;

    /** (0018,7001) VR=DS VM=1 Detector Temperature */
    public static final int DetectorTemperature = 0x00187001;

    /** (0018,7004) VR=CS VM=1 Detector Type */
    public static final int DetectorType = 0x00187004;

    /** (0018,7005) VR=CS VM=1 Detector Configuration */
    public static final int DetectorConfiguration = 0x00187005;

    /** (0018,7006) VR=LT VM=1 Detector Description */
    public static final int DetectorDescription = 0x00187006;

    /** (0018,7008) VR=LT VM=1 Detector Mode */
    public static final int DetectorMode = 0x00187008;

    /** (0018,700A) VR=SH VM=1 Detector ID */
    public static final int DetectorID = 0x0018700A;

    /** (0018,700C) VR=DA VM=1 Date of Last Detector Calibration */
    public static final int DateOfLastDetectorCalibration = 0x0018700C;

    /** (0018,700E) VR=TM VM=1 Time of Last Detector Calibration */
    public static final int TimeOfLastDetectorCalibration = 0x0018700E;

    /** (0018,7010) VR=IS VM=1 Exposures on Detector Since Last Calibration */
    public static final int ExposuresOnDetectorSinceLastCalibration = 0x00187010;

    /** (0018,7011) VR=IS VM=1 Exposures on Detector Since Manufactured */
    public static final int ExposuresOnDetectorSinceManufactured = 0x00187011;

    /** (0018,7012) VR=DS VM=1 Detector Time Since Last Exposure */
    public static final int DetectorTimeSinceLastExposure = 0x00187012;

    /** (0018,7014) VR=DS VM=1 Detector Active Time */
    public static final int DetectorActiveTime = 0x00187014;

    /** (0018,7016) VR=DS VM=1 Detector Activation Offset From Exposure */
    public static final int DetectorActivationOffsetFromExposure = 0x00187016;

    /** (0018,701A) VR=DS VM=2 Detector Binning */
    public static final int DetectorBinning = 0x0018701A;

    /** (0018,7020) VR=DS VM=2 Detector Element Physical Size */
    public static final int DetectorElementPhysicalSize = 0x00187020;

    /** (0018,7022) VR=DS VM=2 Detector Element Spacing */
    public static final int DetectorElementSpacing = 0x00187022;

    /** (0018,7024) VR=CS VM=1 Detector Active Shape */
    public static final int DetectorActiveShape = 0x00187024;

    /** (0018,7026) VR=DS VM=1-2 Detector Active Dimension(s) */
    public static final int DetectorActiveDimensions = 0x00187026;

    /** (0018,7028) VR=DS VM=2 Detector Active Origin */
    public static final int DetectorActiveOrigin = 0x00187028;

    /** (0018,702A) VR=LO VM=1 Detector Manufacturer Name */
    public static final int DetectorManufacturerName = 0x0018702A;

    /** (0018,702B) VR=LO VM=1 Detector Manufacturer's Model Name */
    public static final int DetectorManufacturerModelName = 0x0018702B;

    /** (0018,7030) VR=DS VM=2 Field of View Origin */
    public static final int FieldOfViewOrigin = 0x00187030;

    /** (0018,7032) VR=DS VM=1 Field of View Rotation */
    public static final int FieldOfViewRotation = 0x00187032;

    /** (0018,7034) VR=CS VM=1 Field of View Horizontal Flip */
    public static final int FieldOfViewHorizontalFlip = 0x00187034;

    /** (0018,7036) VR=FL VM=2 Pixel Data Area Origin Relative To FOV */
    public static final int PixelDataAreaOriginRelativeToFOV = 0x00187036;

    /** (0018,7038) VR=FL VM=1 Pixel Data Area Rotation Angle Relative To FOV */
    public static final int PixelDataAreaRotationAngleRelativeToFOV = 0x00187038;

    /** (0018,7040) VR=LT VM=1 Grid Absorbing Material */
    public static final int GridAbsorbingMaterial = 0x00187040;

    /** (0018,7041) VR=LT VM=1 Grid Spacing Material */
    public static final int GridSpacingMaterial = 0x00187041;

    /** (0018,7042) VR=DS VM=1 Grid Thickness */
    public static final int GridThickness = 0x00187042;

    /** (0018,7044) VR=DS VM=1 Grid Pitch */
    public static final int GridPitch = 0x00187044;

    /** (0018,7046) VR=IS VM=2 Grid Aspect Ratio */
    public static final int GridAspectRatio = 0x00187046;

    /** (0018,7048) VR=DS VM=1 Grid Period */
    public static final int GridPeriod = 0x00187048;

    /** (0018,704C) VR=DS VM=1 Grid Focal Distance */
    public static final int GridFocalDistance = 0x0018704C;

    /** (0018,7050) VR=CS VM=1-n Filter Material */
    public static final int FilterMaterial = 0x00187050;

    /** (0018,7052) VR=DS VM=1-n Filter Thickness Minimum */
    public static final int FilterThicknessMinimum = 0x00187052;

    /** (0018,7054) VR=DS VM=1-n Filter Thickness Maximum */
    public static final int FilterThicknessMaximum = 0x00187054;

    /** (0018,7056) VR=FL VM=1-n Filter Beam Path Length Minimum */
    public static final int FilterBeamPathLengthMinimum = 0x00187056;

    /** (0018,7058) VR=FL VM=1-n Filter Beam Path Length Maximum */
    public static final int FilterBeamPathLengthMaximum = 0x00187058;

    /** (0018,7060) VR=CS VM=1 Exposure Control Mode */
    public static final int ExposureControlMode = 0x00187060;

    /** (0018,7062) VR=LT VM=1 Exposure Control Mode Description */
    public static final int ExposureControlModeDescription = 0x00187062;

    /** (0018,7064) VR=CS VM=1 Exposure Status */
    public static final int ExposureStatus = 0x00187064;

    /** (0018,7065) VR=DS VM=1 Phototimer Setting */
    public static final int PhototimerSetting = 0x00187065;

    /** (0018,8150) VR=DS VM=1 Exposure Time in µS */
    public static final int ExposureTimeInuS = 0x00188150;

    /** (0018,8151) VR=DS VM=1 X-Ray Tube Current in µA */
    public static final int XRayTubeCurrentInuA = 0x00188151;

    /** (0018,9004) VR=CS VM=1 Content Qualification */
    public static final int ContentQualification = 0x00189004;

    /** (0018,9005) VR=SH VM=1 Pulse Sequence Name */
    public static final int PulseSequenceName = 0x00189005;

    /** (0018,9006) VR=SQ VM=1 MR Imaging Modifier Sequence */
    public static final int MRImagingModifierSequence = 0x00189006;

    /** (0018,9008) VR=CS VM=1 Echo Pulse Sequence */
    public static final int EchoPulseSequence = 0x00189008;

    /** (0018,9009) VR=CS VM=1 Inversion Recovery */
    public static final int InversionRecovery = 0x00189009;

    /** (0018,9010) VR=CS VM=1 Flow Compensation */
    public static final int FlowCompensation = 0x00189010;

    /** (0018,9011) VR=CS VM=1 Multiple Spin Echo */
    public static final int MultipleSpinEcho = 0x00189011;

    /** (0018,9012) VR=CS VM=1 Multi-planar Excitation */
    public static final int MultiPlanarExcitation = 0x00189012;

    /** (0018,9014) VR=CS VM=1 Phase Contrast */
    public static final int PhaseContrast = 0x00189014;

    /** (0018,9015) VR=CS VM=1 Time of Flight Contrast */
    public static final int TimeOfFlightContrast = 0x00189015;

    /** (0018,9016) VR=CS VM=1 Spoiling */
    public static final int Spoiling = 0x00189016;

    /** (0018,9017) VR=CS VM=1 Steady State Pulse Sequence */
    public static final int SteadyStatePulseSequence = 0x00189017;

    /** (0018,9018) VR=CS VM=1 Echo Planar Pulse Sequence */
    public static final int EchoPlanarPulseSequence = 0x00189018;

    /** (0018,9019) VR=FD VM=1 Tag Angle First Axis */
    public static final int TagAngleFirstAxis = 0x00189019;

    /** (0018,9020) VR=CS VM=1 Magnetization Transfer */
    public static final int MagnetizationTransfer = 0x00189020;

    /** (0018,9021) VR=CS VM=1 T2 Preparation */
    public static final int T2Preparation = 0x00189021;

    /** (0018,9022) VR=CS VM=1 Blood Signal Nulling */
    public static final int BloodSignalNulling = 0x00189022;

    /** (0018,9024) VR=CS VM=1 Saturation Recovery */
    public static final int SaturationRecovery = 0x00189024;

    /** (0018,9025) VR=CS VM=1 Spectrally Selected Suppression */
    public static final int SpectrallySelectedSuppression = 0x00189025;

    /** (0018,9026) VR=CS VM=1 Spectrally Selected Excitation */
    public static final int SpectrallySelectedExcitation = 0x00189026;

    /** (0018,9027) VR=CS VM=1 Spatial Pre-saturation */
    public static final int SpatialPresaturation = 0x00189027;

    /** (0018,9028) VR=CS VM=1 Tagging */
    public static final int Tagging = 0x00189028;

    /** (0018,9029) VR=CS VM=1 Oversampling Phase */
    public static final int OversamplingPhase = 0x00189029;

    /** (0018,9030) VR=FD VM=1 Tag Spacing First Dimension */
    public static final int TagSpacingFirstDimension = 0x00189030;

    /** (0018,9032) VR=CS VM=1 Geometry of k-Space Traversal */
    public static final int GeometryOfKSpaceTraversal = 0x00189032;

    /** (0018,9033) VR=CS VM=1 Segmented k-Space Traversal */
    public static final int SegmentedKSpaceTraversal = 0x00189033;

    /** (0018,9034) VR=CS VM=1 Rectilinear Phase Encode Reordering */
    public static final int RectilinearPhaseEncodeReordering = 0x00189034;

    /** (0018,9035) VR=FD VM=1 Tag Thickness */
    public static final int TagThickness = 0x00189035;

    /** (0018,9036) VR=CS VM=1 Partial Fourier Direction */
    public static final int PartialFourierDirection = 0x00189036;

    /** (0018,9037) VR=CS VM=1 Cardiac Synchronization Technique */
    public static final int CardiacSynchronizationTechnique = 0x00189037;

    /** (0018,9041) VR=LO VM=1 Receive Coil Manufacturer Name */
    public static final int ReceiveCoilManufacturerName = 0x00189041;

    /** (0018,9042) VR=SQ VM=1 MR Receive Coil Sequence */
    public static final int MRReceiveCoilSequence = 0x00189042;

    /** (0018,9043) VR=CS VM=1 Receive Coil Type */
    public static final int ReceiveCoilType = 0x00189043;

    /** (0018,9044) VR=CS VM=1 Quadrature Receive Coil */
    public static final int QuadratureReceiveCoil = 0x00189044;

    /** (0018,9045) VR=SQ VM=1 Multi-Coil Definition Sequence */
    public static final int MultiCoilDefinitionSequence = 0x00189045;

    /** (0018,9046) VR=LO VM=1 Multi-Coil Configuration */
    public static final int MultiCoilConfiguration = 0x00189046;

    /** (0018,9047) VR=SH VM=1 Multi-Coil Element Name */
    public static final int MultiCoilElementName = 0x00189047;

    /** (0018,9048) VR=CS VM=1 Multi-Coil Element Used */
    public static final int MultiCoilElementUsed = 0x00189048;

    /** (0018,9049) VR=SQ VM=1 MR Transmit Coil Sequence */
    public static final int MRTransmitCoilSequence = 0x00189049;

    /** (0018,9050) VR=LO VM=1 Transmit Coil Manufacturer Name */
    public static final int TransmitCoilManufacturerName = 0x00189050;

    /** (0018,9051) VR=CS VM=1 Transmit Coil Type */
    public static final int TransmitCoilType = 0x00189051;

    /** (0018,9052) VR=FD VM=1-2 Spectral Width */
    public static final int SpectralWidth = 0x00189052;

    /** (0018,9053) VR=FD VM=1-2 Chemical Shift Reference */
    public static final int ChemicalShiftReference = 0x00189053;

    /** (0018,9054) VR=CS VM=1 Volume Localization Technique */
    public static final int VolumeLocalizationTechnique = 0x00189054;

    /** (0018,9058) VR=US VM=1 MR Acquisition Frequency Encoding Steps */
    public static final int MRAcquisitionFrequencyEncodingSteps = 0x00189058;

    /** (0018,9059) VR=CS VM=1 De-coupling */
    public static final int Decoupling = 0x00189059;

    /** (0018,9060) VR=CS VM=1-2 De-coupled Nucleus */
    public static final int DecoupledNucleus = 0x00189060;

    /** (0018,9061) VR=FD VM=1-2 De-coupling Frequency */
    public static final int DecouplingFrequency = 0x00189061;

    /** (0018,9062) VR=CS VM=1 De-coupling Method */
    public static final int DecouplingMethod = 0x00189062;

    /** (0018,9063) VR=FD VM=1-2 De-coupling Chemical Shift Reference */
    public static final int DecouplingChemicalShiftReference = 0x00189063;

    /** (0018,9064) VR=CS VM=1 k-space Filtering */
    public static final int KSpaceFiltering = 0x00189064;

    /** (0018,9065) VR=CS VM=1-2 Time Domain Filtering */
    public static final int TimeDomainFiltering = 0x00189065;

    /** (0018,9066) VR=US VM=1-2 Number of Zero Fills */
    public static final int NumberOfZeroFills = 0x00189066;

    /** (0018,9067) VR=CS VM=1 Baseline Correction */
    public static final int BaselineCorrection = 0x00189067;

    /** (0018,9069) VR=FD VM=1 Parallel Reduction Factor In-plane */
    public static final int ParallelReductionFactorInPlane = 0x00189069;

    /** (0018,9070) VR=FD VM=1 Cardiac R-R Interval Specified */
    public static final int CardiacRRIntervalSpecified = 0x00189070;

    /** (0018,9073) VR=FD VM=1 Acquisition Duration */
    public static final int AcquisitionDuration = 0x00189073;

    /** (0018,9074) VR=DT VM=1 Frame Acquisition DateTime */
    public static final int FrameAcquisitionDateTime = 0x00189074;

    /** (0018,9075) VR=CS VM=1 Diffusion Directionality */
    public static final int DiffusionDirectionality = 0x00189075;

    /** (0018,9076) VR=SQ VM=1 Diffusion Gradient Direction Sequence */
    public static final int DiffusionGradientDirectionSequence = 0x00189076;

    /** (0018,9077) VR=CS VM=1 Parallel Acquisition */
    public static final int ParallelAcquisition = 0x00189077;

    /** (0018,9078) VR=CS VM=1 Parallel Acquisition Technique */
    public static final int ParallelAcquisitionTechnique = 0x00189078;

    /** (0018,9079) VR=FD VM=1-n Inversion Times */
    public static final int InversionTimes = 0x00189079;

    /** (0018,9080) VR=ST VM=1 Metabolite Map Description */
    public static final int MetaboliteMapDescription = 0x00189080;

    /** (0018,9081) VR=CS VM=1 Partial Fourier */
    public static final int PartialFourier = 0x00189081;

    /** (0018,9082) VR=FD VM=1 Effective Echo Time */
    public static final int EffectiveEchoTime = 0x00189082;

    /** (0018,9083) VR=SQ VM=1 Metabolite Map Code Sequence */
    public static final int MetaboliteMapCodeSequence = 0x00189083;

    /** (0018,9084) VR=SQ VM=1 Chemical Shift Sequence */
    public static final int ChemicalShiftSequence = 0x00189084;

    /** (0018,9085) VR=CS VM=1 Cardiac Signal Source */
    public static final int CardiacSignalSource = 0x00189085;

    /** (0018,9087) VR=FD VM=1 Diffusion b-value */
    public static final int DiffusionBValue = 0x00189087;

    /** (0018,9089) VR=FD VM=3 Diffusion Gradient Orientation */
    public static final int DiffusionGradientOrientation = 0x00189089;

    /** (0018,9090) VR=FD VM=3 Velocity Encoding Direction */
    public static final int VelocityEncodingDirection = 0x00189090;

    /** (0018,9091) VR=FD VM=1 Velocity Encoding Minimum Value */
    public static final int VelocityEncodingMinimumValue = 0x00189091;

    /** (0018,9092) VR=SQ VM=1 Velocity Encoding Acquisition Sequence */
    public static final int VelocityEncodingAcquisitionSequence = 0x00189092;

    /** (0018,9093) VR=US VM=1 Number of k-Space Trajectories */
    public static final int NumberOfKSpaceTrajectories = 0x00189093;

    /** (0018,9094) VR=CS VM=1 Coverage of k-Space */
    public static final int CoverageOfKSpace = 0x00189094;

    /** (0018,9095) VR=UL VM=1 Spectroscopy Acquisition Phase Rows */
    public static final int SpectroscopyAcquisitionPhaseRows = 0x00189095;

    /** (0018,9096) VR=FD VM=1 Parallel Reduction Factor In-plane (Retired) (retired) */
    public static final int ParallelReductionFactorInPlaneRetired = 0x00189096;

    /** (0018,9098) VR=FD VM=1-2 Transmitter Frequency */
    public static final int TransmitterFrequency = 0x00189098;

    /** (0018,9100) VR=CS VM=1-2 Resonant Nucleus */
    public static final int ResonantNucleus = 0x00189100;

    /** (0018,9101) VR=CS VM=1 Frequency Correction */
    public static final int FrequencyCorrection = 0x00189101;

    /** (0018,9103) VR=SQ VM=1 MR Spectroscopy FOV/Geometry Sequence */
    public static final int MRSpectroscopyFOVGeometrySequence = 0x00189103;

    /** (0018,9104) VR=FD VM=1 Slab Thickness */
    public static final int SlabThickness = 0x00189104;

    /** (0018,9105) VR=FD VM=3 Slab Orientation */
    public static final int SlabOrientation = 0x00189105;

    /** (0018,9106) VR=FD VM=3 Mid Slab Position */
    public static final int MidSlabPosition = 0x00189106;

    /** (0018,9107) VR=SQ VM=1 MR Spatial Saturation Sequence */
    public static final int MRSpatialSaturationSequence = 0x00189107;

    /** (0018,9112) VR=SQ VM=1 MR Timing and Related Parameters Sequence */
    public static final int MRTimingAndRelatedParametersSequence = 0x00189112;

    /** (0018,9114) VR=SQ VM=1 MR Echo Sequence */
    public static final int MREchoSequence = 0x00189114;

    /** (0018,9115) VR=SQ VM=1 MR Modifier Sequence */
    public static final int MRModifierSequence = 0x00189115;

    /** (0018,9117) VR=SQ VM=1 MR Diffusion Sequence */
    public static final int MRDiffusionSequence = 0x00189117;

    /** (0018,9118) VR=SQ VM=1 Cardiac Synchronization Sequence */
    public static final int CardiacSynchronizationSequence = 0x00189118;

    /** (0018,9119) VR=SQ VM=1 MR Averages Sequence */
    public static final int MRAveragesSequence = 0x00189119;

    /** (0018,9125) VR=SQ VM=1 MR FOV/Geometry Sequence */
    public static final int MRFOVGeometrySequence = 0x00189125;

    /** (0018,9126) VR=SQ VM=1 Volume Localization Sequence */
    public static final int VolumeLocalizationSequence = 0x00189126;

    /** (0018,9127) VR=UL VM=1 Spectroscopy Acquisition Data Columns */
    public static final int SpectroscopyAcquisitionDataColumns = 0x00189127;

    /** (0018,9147) VR=CS VM=1 Diffusion Anisotropy Type */
    public static final int DiffusionAnisotropyType = 0x00189147;

    /** (0018,9151) VR=DT VM=1 Frame Reference DateTime */
    public static final int FrameReferenceDateTime = 0x00189151;

    /** (0018,9152) VR=SQ VM=1 MR Metabolite Map Sequence */
    public static final int MRMetaboliteMapSequence = 0x00189152;

    /** (0018,9155) VR=FD VM=1 Parallel Reduction Factor out-of-plane */
    public static final int ParallelReductionFactorOutOfPlane = 0x00189155;

    /** (0018,9159) VR=UL VM=1 Spectroscopy Acquisition Out-of-plane Phase Steps */
    public static final int SpectroscopyAcquisitionOutOfPlanePhaseSteps = 0x00189159;

    /** (0018,9166) VR=CS VM=1 Bulk Motion Status (retired) */
    public static final int BulkMotionStatus = 0x00189166;

    /** (0018,9168) VR=FD VM=1 Parallel Reduction Factor Second In-plane */
    public static final int ParallelReductionFactorSecondInPlane = 0x00189168;

    /** (0018,9169) VR=CS VM=1 Cardiac Beat Rejection Technique */
    public static final int CardiacBeatRejectionTechnique = 0x00189169;

    /** (0018,9170) VR=CS VM=1 Respiratory Motion Compensation Technique */
    public static final int RespiratoryMotionCompensationTechnique = 0x00189170;

    /** (0018,9171) VR=CS VM=1 Respiratory Signal Source */
    public static final int RespiratorySignalSource = 0x00189171;

    /** (0018,9172) VR=CS VM=1 Bulk Motion Compensation Technique */
    public static final int BulkMotionCompensationTechnique = 0x00189172;

    /** (0018,9173) VR=CS VM=1 Bulk Motion Signal Source */
    public static final int BulkMotionSignalSource = 0x00189173;

    /** (0018,9174) VR=CS VM=1 Applicable Safety Standard Agency */
    public static final int ApplicableSafetyStandardAgency = 0x00189174;

    /** (0018,9175) VR=LO VM=1 Applicable Safety Standard Description */
    public static final int ApplicableSafetyStandardDescription = 0x00189175;

    /** (0018,9176) VR=SQ VM=1 Operating Mode Sequence */
    public static final int OperatingModeSequence = 0x00189176;

    /** (0018,9177) VR=CS VM=1 Operating Mode Type */
    public static final int OperatingModeType = 0x00189177;

    /** (0018,9178) VR=CS VM=1 Operating Mode */
    public static final int OperatingMode = 0x00189178;

    /** (0018,9179) VR=CS VM=1 Specific Absorption Rate Definition */
    public static final int SpecificAbsorptionRateDefinition = 0x00189179;

    /** (0018,9180) VR=CS VM=1 Gradient Output Type */
    public static final int GradientOutputType = 0x00189180;

    /** (0018,9181) VR=FD VM=1 Specific Absorption Rate Value */
    public static final int SpecificAbsorptionRateValue = 0x00189181;

    /** (0018,9182) VR=FD VM=1 Gradient Output */
    public static final int GradientOutput = 0x00189182;

    /** (0018,9183) VR=CS VM=1 Flow Compensation Direction */
    public static final int FlowCompensationDirection = 0x00189183;

    /** (0018,9184) VR=FD VM=1 Tagging Delay */
    public static final int TaggingDelay = 0x00189184;

    /** (0018,9185) VR=ST VM=1 Respiratory Motion Compensation Technique Description */
    public static final int RespiratoryMotionCompensationTechniqueDescription = 0x00189185;

    /** (0018,9186) VR=SH VM=1 Respiratory Signal Source ID */
    public static final int RespiratorySignalSourceID = 0x00189186;

    /** (0018,9195) VR=FD VM=1 Chemical Shift Minimum Integration Limit in Hz (retired) */
    public static final int ChemicalShiftMinimumIntegrationLimitInHz = 0x00189195;

    /** (0018,9196) VR=FD VM=1 Chemical Shift Maximum Integration Limit in Hz (retired) */
    public static final int ChemicalShiftMaximumIntegrationLimitInHz = 0x00189196;

    /** (0018,9197) VR=SQ VM=1 MR Velocity Encoding Sequence */
    public static final int MRVelocityEncodingSequence = 0x00189197;

    /** (0018,9198) VR=CS VM=1 First Order Phase Correction */
    public static final int FirstOrderPhaseCorrection = 0x00189198;

    /** (0018,9199) VR=CS VM=1 Water Referenced Phase Correction */
    public static final int WaterReferencedPhaseCorrection = 0x00189199;

    /** (0018,9200) VR=CS VM=1 MR Spectroscopy Acquisition Type */
    public static final int MRSpectroscopyAcquisitionType = 0x00189200;

    /** (0018,9214) VR=CS VM=1 Respiratory Cycle Position */
    public static final int RespiratoryCyclePosition = 0x00189214;

    /** (0018,9217) VR=FD VM=1 Velocity Encoding Maximum Value */
    public static final int VelocityEncodingMaximumValue = 0x00189217;

    /** (0018,9218) VR=FD VM=1 Tag Spacing Second Dimension */
    public static final int TagSpacingSecondDimension = 0x00189218;

    /** (0018,9219) VR=SS VM=1 Tag Angle Second Axis */
    public static final int TagAngleSecondAxis = 0x00189219;

    /** (0018,9220) VR=FD VM=1 Frame Acquisition Duration */
    public static final int FrameAcquisitionDuration = 0x00189220;

    /** (0018,9226) VR=SQ VM=1 MR Image Frame Type Sequence */
    public static final int MRImageFrameTypeSequence = 0x00189226;

    /** (0018,9227) VR=SQ VM=1 MR Spectroscopy Frame Type Sequence */
    public static final int MRSpectroscopyFrameTypeSequence = 0x00189227;

    /** (0018,9231) VR=US VM=1 MR Acquisition Phase Encoding Steps in-plane */
    public static final int MRAcquisitionPhaseEncodingStepsInPlane = 0x00189231;

    /** (0018,9232) VR=US VM=1 MR Acquisition Phase Encoding Steps out-of-plane */
    public static final int MRAcquisitionPhaseEncodingStepsOutOfPlane = 0x00189232;

    /** (0018,9234) VR=UL VM=1 Spectroscopy Acquisition Phase Columns */
    public static final int SpectroscopyAcquisitionPhaseColumns = 0x00189234;

    /** (0018,9236) VR=CS VM=1 Cardiac Cycle Position */
    public static final int CardiacCyclePosition = 0x00189236;

    /** (0018,9239) VR=SQ VM=1 Specific Absorption Rate Sequence */
    public static final int SpecificAbsorptionRateSequence = 0x00189239;

    /** (0018,9240) VR=US VM=1 RF Echo Train Length */
    public static final int RFEchoTrainLength = 0x00189240;

    /** (0018,9241) VR=US VM=1 Gradient Echo Train Length */
    public static final int GradientEchoTrainLength = 0x00189241;

    /** (0018,9250) VR=CS VM=1 Arterial Spin Labeling Contrast */
    public static final int ArterialSpinLabelingContrast = 0x00189250;

    /** (0018,9251) VR=SQ VM=1 MR Arterial Spin Labeling Sequence */
    public static final int MRArterialSpinLabelingSequence = 0x00189251;

    /** (0018,9252) VR=LO VM=1 ASL Technique Description */
    public static final int ASLTechniqueDescription = 0x00189252;

    /** (0018,9253) VR=US VM=1 ASL Slab Number */
    public static final int ASLSlabNumber = 0x00189253;

    /** (0018,9254) VR=FD VM=1 ASL Slab Thickness */
    public static final int ASLSlabThickness = 0x00189254;

    /** (0018,9255) VR=FD VM=3 ASL Slab Orientation */
    public static final int ASLSlabOrientation = 0x00189255;

    /** (0018,9256) VR=FD VM=3 ASL Mid Slab Position */
    public static final int ASLMidSlabPosition = 0x00189256;

    /** (0018,9257) VR=CS VM=1 ASL Context */
    public static final int ASLContext = 0x00189257;

    /** (0018,9258) VR=UL VM=1 ASL Pulse Train Duration */
    public static final int ASLPulseTrainDuration = 0x00189258;

    /** (0018,9259) VR=CS VM=1 ASL Crusher Flag */
    public static final int ASLCrusherFlag = 0x00189259;

    /** (0018,925A) VR=FD VM=1 ASL Crusher Flow Limit */
    public static final int ASLCrusherFlowLimit = 0x0018925A;

    /** (0018,925B) VR=LO VM=1 ASL Crusher Description */
    public static final int ASLCrusherDescription = 0x0018925B;

    /** (0018,925C) VR=CS VM=1 ASL Bolus Cut-off Flag */
    public static final int ASLBolusCutoffFlag = 0x0018925C;

    /** (0018,925D) VR=SQ VM=1 ASL Bolus Cut-off Timing Sequence */
    public static final int ASLBolusCutoffTimingSequence = 0x0018925D;

    /** (0018,925E) VR=LO VM=1 ASL Bolus Cut-off Technique */
    public static final int ASLBolusCutoffTechnique = 0x0018925E;

    /** (0018,925F) VR=UL VM=1 ASL Bolus Cut-off Delay Time */
    public static final int ASLBolusCutoffDelayTime = 0x0018925F;

    /** (0018,9260) VR=SQ VM=1 ASL Slab Sequence */
    public static final int ASLSlabSequence = 0x00189260;

    /** (0018,9295) VR=FD VM=1 Chemical Shift Minimum Integration Limit in ppm */
    public static final int ChemicalShiftMinimumIntegrationLimitInppm = 0x00189295;

    /** (0018,9296) VR=FD VM=1 Chemical Shift Maximum Integration Limit in ppm */
    public static final int ChemicalShiftMaximumIntegrationLimitInppm = 0x00189296;

    /** (0018,9297) VR=CS VM=1 Water Reference Acquisition */
    public static final int WaterReferenceAcquisition = 0x00189297;

    /** (0018,9298) VR=IS VM=1 Echo Peak Position */
    public static final int EchoPeakPosition = 0x00189298;

    /** (0018,9301) VR=SQ VM=1 CT Acquisition Type Sequence */
    public static final int CTAcquisitionTypeSequence = 0x00189301;

    /** (0018,9302) VR=CS VM=1 Acquisition Type */
    public static final int AcquisitionType = 0x00189302;

    /** (0018,9303) VR=FD VM=1 Tube Angle */
    public static final int TubeAngle = 0x00189303;

    /** (0018,9304) VR=SQ VM=1 CT Acquisition Details Sequence */
    public static final int CTAcquisitionDetailsSequence = 0x00189304;

    /** (0018,9305) VR=FD VM=1 Revolution Time */
    public static final int RevolutionTime = 0x00189305;

    /** (0018,9306) VR=FD VM=1 Single Collimation Width */
    public static final int SingleCollimationWidth = 0x00189306;

    /** (0018,9307) VR=FD VM=1 Total Collimation Width */
    public static final int TotalCollimationWidth = 0x00189307;

    /** (0018,9308) VR=SQ VM=1 CT Table Dynamics Sequence */
    public static final int CTTableDynamicsSequence = 0x00189308;

    /** (0018,9309) VR=FD VM=1 Table Speed */
    public static final int TableSpeed = 0x00189309;

    /** (0018,9310) VR=FD VM=1 Table Feed per Rotation */
    public static final int TableFeedPerRotation = 0x00189310;

    /** (0018,9311) VR=FD VM=1 Spiral Pitch Factor */
    public static final int SpiralPitchFactor = 0x00189311;

    /** (0018,9312) VR=SQ VM=1 CT Geometry Sequence */
    public static final int CTGeometrySequence = 0x00189312;

    /** (0018,9313) VR=FD VM=3 Data Collection Center (Patient) */
    public static final int DataCollectionCenterPatient = 0x00189313;

    /** (0018,9314) VR=SQ VM=1 CT Reconstruction Sequence */
    public static final int CTReconstructionSequence = 0x00189314;

    /** (0018,9315) VR=CS VM=1 Reconstruction Algorithm */
    public static final int ReconstructionAlgorithm = 0x00189315;

    /** (0018,9316) VR=CS VM=1 Convolution Kernel Group */
    public static final int ConvolutionKernelGroup = 0x00189316;

    /** (0018,9317) VR=FD VM=2 Reconstruction Field of View */
    public static final int ReconstructionFieldOfView = 0x00189317;

    /** (0018,9318) VR=FD VM=3 Reconstruction Target Center (Patient) */
    public static final int ReconstructionTargetCenterPatient = 0x00189318;

    /** (0018,9319) VR=FD VM=1 Reconstruction Angle */
    public static final int ReconstructionAngle = 0x00189319;

    /** (0018,9320) VR=SH VM=1 Image Filter */
    public static final int ImageFilter = 0x00189320;

    /** (0018,9321) VR=SQ VM=1 CT Exposure Sequence */
    public static final int CTExposureSequence = 0x00189321;

    /** (0018,9322) VR=FD VM=2 Reconstruction Pixel Spacing */
    public static final int ReconstructionPixelSpacing = 0x00189322;

    /** (0018,9323) VR=CS VM=1-n Exposure Modulation Type */
    public static final int ExposureModulationType = 0x00189323;

    /** (0018,9324) VR=FD VM=1 Estimated Dose Saving */
    public static final int EstimatedDoseSaving = 0x00189324;

    /** (0018,9325) VR=SQ VM=1 CT X-Ray Details Sequence */
    public static final int CTXRayDetailsSequence = 0x00189325;

    /** (0018,9326) VR=SQ VM=1 CT Position Sequence */
    public static final int CTPositionSequence = 0x00189326;

    /** (0018,9327) VR=FD VM=1 Table Position */
    public static final int TablePosition = 0x00189327;

    /** (0018,9328) VR=FD VM=1 Exposure Time in ms */
    public static final int ExposureTimeInms = 0x00189328;

    /** (0018,9329) VR=SQ VM=1 CT Image Frame Type Sequence */
    public static final int CTImageFrameTypeSequence = 0x00189329;

    /** (0018,9330) VR=FD VM=1 X-Ray Tube Current in mA */
    public static final int XRayTubeCurrentInmA = 0x00189330;

    /** (0018,9332) VR=FD VM=1 Exposure in mAs */
    public static final int ExposureInmAs = 0x00189332;

    /** (0018,9333) VR=CS VM=1 Constant Volume Flag */
    public static final int ConstantVolumeFlag = 0x00189333;

    /** (0018,9334) VR=CS VM=1 Fluoroscopy Flag */
    public static final int FluoroscopyFlag = 0x00189334;

    /** (0018,9335) VR=FD VM=1 Distance Source to Data Collection Center */
    public static final int DistanceSourceToDataCollectionCenter = 0x00189335;

    /** (0018,9337) VR=US VM=1 Contrast/Bolus Agent Number */
    public static final int ContrastBolusAgentNumber = 0x00189337;

    /** (0018,9338) VR=SQ VM=1 Contrast/Bolus Ingredient Code Sequence */
    public static final int ContrastBolusIngredientCodeSequence = 0x00189338;

    /** (0018,9340) VR=SQ VM=1 Contrast Administration Profile Sequence */
    public static final int ContrastAdministrationProfileSequence = 0x00189340;

    /** (0018,9341) VR=SQ VM=1 Contrast/Bolus Usage Sequence */
    public static final int ContrastBolusUsageSequence = 0x00189341;

    /** (0018,9342) VR=CS VM=1 Contrast/Bolus Agent Administered */
    public static final int ContrastBolusAgentAdministered = 0x00189342;

    /** (0018,9343) VR=CS VM=1 Contrast/Bolus Agent Detected */
    public static final int ContrastBolusAgentDetected = 0x00189343;

    /** (0018,9344) VR=CS VM=1 Contrast/Bolus Agent Phase */
    public static final int ContrastBolusAgentPhase = 0x00189344;

    /** (0018,9345) VR=FD VM=1 CTDIvol */
    public static final int CTDIvol = 0x00189345;

    /** (0018,9346) VR=SQ VM=1 CTDI Phantom Type Code Sequence */
    public static final int CTDIPhantomTypeCodeSequence = 0x00189346;

    /** (0018,9351) VR=FL VM=1 Calcium Scoring Mass Factor Patient */
    public static final int CalciumScoringMassFactorPatient = 0x00189351;

    /** (0018,9352) VR=FL VM=3 Calcium Scoring Mass Factor Device */
    public static final int CalciumScoringMassFactorDevice = 0x00189352;

    /** (0018,9353) VR=FL VM=1 Energy Weighting Factor */
    public static final int EnergyWeightingFactor = 0x00189353;

    /** (0018,9360) VR=SQ VM=1 CT Additional X-Ray Source Sequence */
    public static final int CTAdditionalXRaySourceSequence = 0x00189360;

    /** (0018,9361) VR=CS VM=1 Multi-energy CT Acquisition */
    public static final int MultienergyCTAcquisition = 0x00189361;

    /** (0018,9362) VR=SQ VM=1 Multi-energy CT Acquisition Sequence */
    public static final int MultienergyCTAcquisitionSequence = 0x00189362;

    /** (0018,9363) VR=SQ VM=1 Multi-energy CT Processing Sequence */
    public static final int MultienergyCTProcessingSequence = 0x00189363;

    /** (0018,9364) VR=SQ VM=1 Multi-energy CT Characteristics Sequence */
    public static final int MultienergyCTCharacteristicsSequence = 0x00189364;

    /** (0018,9365) VR=SQ VM=1 Multi-energy CT X-Ray Source Sequence */
    public static final int MultienergyCTXRaySourceSequence = 0x00189365;

    /** (0018,9366) VR=US VM=1 X-Ray Source Index */
    public static final int XRaySourceIndex = 0x00189366;

    /** (0018,9367) VR=UC VM=1 X-Ray Source ID */
    public static final int XRaySourceID = 0x00189367;

    /** (0018,9368) VR=CS VM=1 Multi-energy Source Technique */
    public static final int MultienergySourceTechnique = 0x00189368;

    /** (0018,9369) VR=DT VM=1 Source Start DateTime */
    public static final int SourceStartDateTime = 0x00189369;

    /** (0018,936A) VR=DT VM=1 Source End DateTime */
    public static final int SourceEndDateTime = 0x0018936A;

    /** (0018,936B) VR=US VM=1 Switching Phase Number */
    public static final int SwitchingPhaseNumber = 0x0018936B;

    /** (0018,936C) VR=DS VM=1 Switching Phase Nominal Duration */
    public static final int SwitchingPhaseNominalDuration = 0x0018936C;

    /** (0018,936D) VR=DS VM=1 Switching Phase Transition Duration */
    public static final int SwitchingPhaseTransitionDuration = 0x0018936D;

    /** (0018,936E) VR=DS VM=1 Effective Bin Energy */
    public static final int EffectiveBinEnergy = 0x0018936E;

    /** (0018,936F) VR=SQ VM=1 Multi-energy CT X-Ray Detector Sequence */
    public static final int MultienergyCTXRayDetectorSequence = 0x0018936F;

    /** (0018,9370) VR=US VM=1 X-Ray Detector Index */
    public static final int XRayDetectorIndex = 0x00189370;

    /** (0018,9371) VR=UC VM=1 X-Ray Detector ID */
    public static final int XRayDetectorID = 0x00189371;

    /** (0018,9372) VR=CS VM=1 Multi-energy Detector Type */
    public static final int MultienergyDetectorType = 0x00189372;

    /** (0018,9373) VR=ST VM=1 X-Ray Detector Label */
    public static final int XRayDetectorLabel = 0x00189373;

    /** (0018,9374) VR=DS VM=1 Nominal Max Energy */
    public static final int NominalMaxEnergy = 0x00189374;

    /** (0018,9375) VR=DS VM=1 Nominal Min Energy */
    public static final int NominalMinEnergy = 0x00189375;

    /** (0018,9376) VR=US VM=1-n Referenced X-Ray Detector Index */
    public static final int ReferencedXRayDetectorIndex = 0x00189376;

    /** (0018,9377) VR=US VM=1-n Referenced X-Ray Source Index */
    public static final int ReferencedXRaySourceIndex = 0x00189377;

    /** (0018,9378) VR=US VM=1-n Referenced Path Index */
    public static final int ReferencedPathIndex = 0x00189378;

    /** (0018,9379) VR=SQ VM=1 Multi-energy CT Path Sequence */
    public static final int MultienergyCTPathSequence = 0x00189379;

    /** (0018,937A) VR=US VM=1 Multi-energy CT Path Index */
    public static final int MultienergyCTPathIndex = 0x0018937A;

    /** (0018,937B) VR=UT VM=1 Multi-energy Acquisition Description */
    public static final int MultienergyAcquisitionDescription = 0x0018937B;

    /** (0018,937C) VR=FD VM=1 Monoenergetic Energy Equivalent */
    public static final int MonoenergeticEnergyEquivalent = 0x0018937C;

    /** (0018,937D) VR=SQ VM=1 Material Code Sequence */
    public static final int MaterialCodeSequence = 0x0018937D;

    /** (0018,937E) VR=CS VM=1 Decomposition Method */
    public static final int DecompositionMethod = 0x0018937E;

    /** (0018,937F) VR=UT VM=1 Decomposition Description */
    public static final int DecompositionDescription = 0x0018937F;

    /** (0018,9380) VR=SQ VM=1 Decomposition Algorithm Identification Sequence */
    public static final int DecompositionAlgorithmIdentificationSequence = 0x00189380;

    /** (0018,9381) VR=SQ VM=1 Decomposition Material Sequence */
    public static final int DecompositionMaterialSequence = 0x00189381;

    /** (0018,9382) VR=SQ VM=1 Material Attenuation Sequence */
    public static final int MaterialAttenuationSequence = 0x00189382;

    /** (0018,9383) VR=DS VM=1 Photon Energy */
    public static final int PhotonEnergy = 0x00189383;

    /** (0018,9384) VR=DS VM=1 X-Ray Mass Attenuation Coefficient */
    public static final int XRayMassAttenuationCoefficient = 0x00189384;

    /** (0018,9401) VR=SQ VM=1 Projection Pixel Calibration Sequence */
    public static final int ProjectionPixelCalibrationSequence = 0x00189401;

    /** (0018,9402) VR=FL VM=1 Distance Source to Isocenter */
    public static final int DistanceSourceToIsocenter = 0x00189402;

    /** (0018,9403) VR=FL VM=1 Distance Object to Table Top */
    public static final int DistanceObjectToTableTop = 0x00189403;

    /** (0018,9404) VR=FL VM=2 Object Pixel Spacing in Center of Beam */
    public static final int ObjectPixelSpacingInCenterOfBeam = 0x00189404;

    /** (0018,9405) VR=SQ VM=1 Positioner Position Sequence */
    public static final int PositionerPositionSequence = 0x00189405;

    /** (0018,9406) VR=SQ VM=1 Table Position Sequence */
    public static final int TablePositionSequence = 0x00189406;

    /** (0018,9407) VR=SQ VM=1 Collimator Shape Sequence */
    public static final int CollimatorShapeSequence = 0x00189407;

    /** (0018,9410) VR=CS VM=1 Planes in Acquisition */
    public static final int PlanesInAcquisition = 0x00189410;

    /** (0018,9412) VR=SQ VM=1 XA/XRF Frame Characteristics Sequence */
    public static final int XAXRFFrameCharacteristicsSequence = 0x00189412;

    /** (0018,9417) VR=SQ VM=1 Frame Acquisition Sequence */
    public static final int FrameAcquisitionSequence = 0x00189417;

    /** (0018,9420) VR=CS VM=1 X-Ray Receptor Type */
    public static final int XRayReceptorType = 0x00189420;

    /** (0018,9423) VR=LO VM=1 Acquisition Protocol Name */
    public static final int AcquisitionProtocolName = 0x00189423;

    /** (0018,9424) VR=LT VM=1 Acquisition Protocol Description */
    public static final int AcquisitionProtocolDescription = 0x00189424;

    /** (0018,9425) VR=CS VM=1 Contrast/Bolus Ingredient Opaque */
    public static final int ContrastBolusIngredientOpaque = 0x00189425;

    /** (0018,9426) VR=FL VM=1 Distance Receptor Plane to Detector Housing */
    public static final int DistanceReceptorPlaneToDetectorHousing = 0x00189426;

    /** (0018,9427) VR=CS VM=1 Intensifier Active Shape */
    public static final int IntensifierActiveShape = 0x00189427;

    /** (0018,9428) VR=FL VM=1-2 Intensifier Active Dimension(s) */
    public static final int IntensifierActiveDimensions = 0x00189428;

    /** (0018,9429) VR=FL VM=2 Physical Detector Size */
    public static final int PhysicalDetectorSize = 0x00189429;

    /** (0018,9430) VR=FL VM=2 Position of Isocenter Projection */
    public static final int PositionOfIsocenterProjection = 0x00189430;

    /** (0018,9432) VR=SQ VM=1 Field of View Sequence */
    public static final int FieldOfViewSequence = 0x00189432;

    /** (0018,9433) VR=LO VM=1 Field of View Description */
    public static final int FieldOfViewDescription = 0x00189433;

    /** (0018,9434) VR=SQ VM=1 Exposure Control Sensing Regions Sequence */
    public static final int ExposureControlSensingRegionsSequence = 0x00189434;

    /** (0018,9435) VR=CS VM=1 Exposure Control Sensing Region Shape */
    public static final int ExposureControlSensingRegionShape = 0x00189435;

    /** (0018,9436) VR=SS VM=1 Exposure Control Sensing Region Left Vertical Edge */
    public static final int ExposureControlSensingRegionLeftVerticalEdge = 0x00189436;

    /** (0018,9437) VR=SS VM=1 Exposure Control Sensing Region Right Vertical Edge */
    public static final int ExposureControlSensingRegionRightVerticalEdge = 0x00189437;

    /** (0018,9438) VR=SS VM=1 Exposure Control Sensing Region Upper Horizontal Edge */
    public static final int ExposureControlSensingRegionUpperHorizontalEdge = 0x00189438;

    /** (0018,9439) VR=SS VM=1 Exposure Control Sensing Region Lower Horizontal Edge */
    public static final int ExposureControlSensingRegionLowerHorizontalEdge = 0x00189439;

    /** (0018,9440) VR=SS VM=2 Center of Circular Exposure Control Sensing Region */
    public static final int CenterOfCircularExposureControlSensingRegion = 0x00189440;

    /** (0018,9441) VR=US VM=1 Radius of Circular Exposure Control Sensing Region */
    public static final int RadiusOfCircularExposureControlSensingRegion = 0x00189441;

    /** (0018,9442) VR=SS VM=2-n Vertices of the Polygonal Exposure Control Sensing Region */
    public static final int VerticesOfThePolygonalExposureControlSensingRegion = 0x00189442;

    /** (0018,9447) VR=FL VM=1 Column Angulation (Patient) */
    public static final int ColumnAngulationPatient = 0x00189447;

    /** (0018,9449) VR=FL VM=1 Beam Angle */
    public static final int BeamAngle = 0x00189449;

    /** (0018,9451) VR=SQ VM=1 Frame Detector Parameters Sequence */
    public static final int FrameDetectorParametersSequence = 0x00189451;

    /** (0018,9452) VR=FL VM=1 Calculated Anatomy Thickness */
    public static final int CalculatedAnatomyThickness = 0x00189452;

    /** (0018,9455) VR=SQ VM=1 Calibration Sequence */
    public static final int CalibrationSequence = 0x00189455;

    /** (0018,9456) VR=SQ VM=1 Object Thickness Sequence */
    public static final int ObjectThicknessSequence = 0x00189456;

    /** (0018,9457) VR=CS VM=1 Plane Identification */
    public static final int PlaneIdentification = 0x00189457;

    /** (0018,9461) VR=FL VM=1-2 Field of View Dimension(s) in Float */
    public static final int FieldOfViewDimensionsInFloat = 0x00189461;

    /** (0018,9462) VR=SQ VM=1 Isocenter Reference System Sequence */
    public static final int IsocenterReferenceSystemSequence = 0x00189462;

    /** (0018,9463) VR=FL VM=1 Positioner Isocenter Primary Angle */
    public static final int PositionerIsocenterPrimaryAngle = 0x00189463;

    /** (0018,9464) VR=FL VM=1 Positioner Isocenter Secondary Angle */
    public static final int PositionerIsocenterSecondaryAngle = 0x00189464;

    /** (0018,9465) VR=FL VM=1 Positioner Isocenter Detector Rotation Angle */
    public static final int PositionerIsocenterDetectorRotationAngle = 0x00189465;

    /** (0018,9466) VR=FL VM=1 Table X Position to Isocenter */
    public static final int TableXPositionToIsocenter = 0x00189466;

    /** (0018,9467) VR=FL VM=1 Table Y Position to Isocenter */
    public static final int TableYPositionToIsocenter = 0x00189467;

    /** (0018,9468) VR=FL VM=1 Table Z Position to Isocenter */
    public static final int TableZPositionToIsocenter = 0x00189468;

    /** (0018,9469) VR=FL VM=1 Table Horizontal Rotation Angle */
    public static final int TableHorizontalRotationAngle = 0x00189469;

    /** (0018,9470) VR=FL VM=1 Table Head Tilt Angle */
    public static final int TableHeadTiltAngle = 0x00189470;

    /** (0018,9471) VR=FL VM=1 Table Cradle Tilt Angle */
    public static final int TableCradleTiltAngle = 0x00189471;

    /** (0018,9472) VR=SQ VM=1 Frame Display Shutter Sequence */
    public static final int FrameDisplayShutterSequence = 0x00189472;

    /** (0018,9473) VR=FL VM=1 Acquired Image Area Dose Product */
    public static final int AcquiredImageAreaDoseProduct = 0x00189473;

    /** (0018,9474) VR=CS VM=1 C-arm Positioner Tabletop Relationship */
    public static final int CArmPositionerTabletopRelationship = 0x00189474;

    /** (0018,9476) VR=SQ VM=1 X-Ray Geometry Sequence */
    public static final int XRayGeometrySequence = 0x00189476;

    /** (0018,9477) VR=SQ VM=1 Irradiation Event Identification Sequence */
    public static final int IrradiationEventIdentificationSequence = 0x00189477;

    /** (0018,9504) VR=SQ VM=1 X-Ray 3D Frame Type Sequence */
    public static final int XRay3DFrameTypeSequence = 0x00189504;

    /** (0018,9506) VR=SQ VM=1 Contributing Sources Sequence */
    public static final int ContributingSourcesSequence = 0x00189506;

    /** (0018,9507) VR=SQ VM=1 X-Ray 3D Acquisition Sequence */
    public static final int XRay3DAcquisitionSequence = 0x00189507;

    /** (0018,9508) VR=FL VM=1 Primary Positioner Scan Arc */
    public static final int PrimaryPositionerScanArc = 0x00189508;

    /** (0018,9509) VR=FL VM=1 Secondary Positioner Scan Arc */
    public static final int SecondaryPositionerScanArc = 0x00189509;

    /** (0018,9510) VR=FL VM=1 Primary Positioner Scan Start Angle */
    public static final int PrimaryPositionerScanStartAngle = 0x00189510;

    /** (0018,9511) VR=FL VM=1 Secondary Positioner Scan Start Angle */
    public static final int SecondaryPositionerScanStartAngle = 0x00189511;

    /** (0018,9514) VR=FL VM=1 Primary Positioner Increment */
    public static final int PrimaryPositionerIncrement = 0x00189514;

    /** (0018,9515) VR=FL VM=1 Secondary Positioner Increment */
    public static final int SecondaryPositionerIncrement = 0x00189515;

    /** (0018,9516) VR=DT VM=1 Start Acquisition DateTime */
    public static final int StartAcquisitionDateTime = 0x00189516;

    /** (0018,9517) VR=DT VM=1 End Acquisition DateTime */
    public static final int EndAcquisitionDateTime = 0x00189517;

    /** (0018,9518) VR=SS VM=1 Primary Positioner Increment Sign */
    public static final int PrimaryPositionerIncrementSign = 0x00189518;

    /** (0018,9519) VR=SS VM=1 Secondary Positioner Increment Sign */
    public static final int SecondaryPositionerIncrementSign = 0x00189519;

    /** (0018,9524) VR=LO VM=1 Application Name */
    public static final int ApplicationName = 0x00189524;

    /** (0018,9525) VR=LO VM=1 Application Version */
    public static final int ApplicationVersion = 0x00189525;

    /** (0018,9526) VR=LO VM=1 Application Manufacturer */
    public static final int ApplicationManufacturer = 0x00189526;

    /** (0018,9527) VR=CS VM=1 Algorithm Type */
    public static final int AlgorithmType = 0x00189527;

    /** (0018,9528) VR=LO VM=1 Algorithm Description */
    public static final int AlgorithmDescription = 0x00189528;

    /** (0018,9530) VR=SQ VM=1 X-Ray 3D Reconstruction Sequence */
    public static final int XRay3DReconstructionSequence = 0x00189530;

    /** (0018,9531) VR=LO VM=1 Reconstruction Description */
    public static final int ReconstructionDescription = 0x00189531;

    /** (0018,9538) VR=SQ VM=1 Per Projection Acquisition Sequence */
    public static final int PerProjectionAcquisitionSequence = 0x00189538;

    /** (0018,9541) VR=SQ VM=1 Detector Position Sequence */
    public static final int DetectorPositionSequence = 0x00189541;

    /** (0018,9542) VR=SQ VM=1 X-Ray Acquisition Dose Sequence */
    public static final int XRayAcquisitionDoseSequence = 0x00189542;

    /** (0018,9543) VR=FD VM=1 X-Ray Source Isocenter Primary Angle */
    public static final int XRaySourceIsocenterPrimaryAngle = 0x00189543;

    /** (0018,9544) VR=FD VM=1 X-Ray Source Isocenter Secondary Angle */
    public static final int XRaySourceIsocenterSecondaryAngle = 0x00189544;

    /** (0018,9545) VR=FD VM=1 Breast Support Isocenter Primary Angle */
    public static final int BreastSupportIsocenterPrimaryAngle = 0x00189545;

    /** (0018,9546) VR=FD VM=1 Breast Support Isocenter Secondary Angle */
    public static final int BreastSupportIsocenterSecondaryAngle = 0x00189546;

    /** (0018,9547) VR=FD VM=1 Breast Support X Position to Isocenter */
    public static final int BreastSupportXPositionToIsocenter = 0x00189547;

    /** (0018,9548) VR=FD VM=1 Breast Support Y Position to Isocenter */
    public static final int BreastSupportYPositionToIsocenter = 0x00189548;

    /** (0018,9549) VR=FD VM=1 Breast Support Z Position to Isocenter */
    public static final int BreastSupportZPositionToIsocenter = 0x00189549;

    /** (0018,9550) VR=FD VM=1 Detector Isocenter Primary Angle */
    public static final int DetectorIsocenterPrimaryAngle = 0x00189550;

    /** (0018,9551) VR=FD VM=1 Detector Isocenter Secondary Angle */
    public static final int DetectorIsocenterSecondaryAngle = 0x00189551;

    /** (0018,9552) VR=FD VM=1 Detector X Position to Isocenter */
    public static final int DetectorXPositionToIsocenter = 0x00189552;

    /** (0018,9553) VR=FD VM=1 Detector Y Position to Isocenter */
    public static final int DetectorYPositionToIsocenter = 0x00189553;

    /** (0018,9554) VR=FD VM=1 Detector Z Position to Isocenter */
    public static final int DetectorZPositionToIsocenter = 0x00189554;

    /** (0018,9555) VR=SQ VM=1 X-Ray Grid Sequence */
    public static final int XRayGridSequence = 0x00189555;

    /** (0018,9556) VR=SQ VM=1 X-Ray Filter Sequence */
    public static final int XRayFilterSequence = 0x00189556;

    /** (0018,9557) VR=FD VM=3 Detector Active Area TLHC Position */
    public static final int DetectorActiveAreaTLHCPosition = 0x00189557;

    /** (0018,9558) VR=FD VM=6 Detector Active Area Orientation */
    public static final int DetectorActiveAreaOrientation = 0x00189558;

    /** (0018,9559) VR=CS VM=1 Positioner Primary Angle Direction */
    public static final int PositionerPrimaryAngleDirection = 0x00189559;

    /** (0018,9601) VR=SQ VM=1 Diffusion b-matrix Sequence */
    public static final int DiffusionBMatrixSequence = 0x00189601;

    /** (0018,9602) VR=FD VM=1 Diffusion b-value XX */
    public static final int DiffusionBValueXX = 0x00189602;

    /** (0018,9603) VR=FD VM=1 Diffusion b-value XY */
    public static final int DiffusionBValueXY = 0x00189603;

    /** (0018,9604) VR=FD VM=1 Diffusion b-value XZ */
    public static final int DiffusionBValueXZ = 0x00189604;

    /** (0018,9605) VR=FD VM=1 Diffusion b-value YY */
    public static final int DiffusionBValueYY = 0x00189605;

    /** (0018,9606) VR=FD VM=1 Diffusion b-value YZ */
    public static final int DiffusionBValueYZ = 0x00189606;

    /** (0018,9607) VR=FD VM=1 Diffusion b-value ZZ */
    public static final int DiffusionBValueZZ = 0x00189607;

    /** (0018,9621) VR=SQ VM=1 Functional MR Sequence */
    public static final int FunctionalMRSequence = 0x00189621;

    /** (0018,9622) VR=CS VM=1 Functional Settling Phase Frames Present */
    public static final int FunctionalSettlingPhaseFramesPresent = 0x00189622;

    /** (0018,9623) VR=DT VM=1 Functional Sync Pulse */
    public static final int FunctionalSyncPulse = 0x00189623;

    /** (0018,9624) VR=CS VM=1 Settling Phase Frame */
    public static final int SettlingPhaseFrame = 0x00189624;

    /** (0018,9701) VR=DT VM=1 Decay Correction DateTime */
    public static final int DecayCorrectionDateTime = 0x00189701;

    /** (0018,9715) VR=FD VM=1 Start Density Threshold */
    public static final int StartDensityThreshold = 0x00189715;

    /** (0018,9716) VR=FD VM=1 Start Relative Density Difference Threshold */
    public static final int StartRelativeDensityDifferenceThreshold = 0x00189716;

    /** (0018,9717) VR=FD VM=1 Start Cardiac Trigger Count Threshold */
    public static final int StartCardiacTriggerCountThreshold = 0x00189717;

    /** (0018,9718) VR=FD VM=1 Start Respiratory Trigger Count Threshold */
    public static final int StartRespiratoryTriggerCountThreshold = 0x00189718;

    /** (0018,9719) VR=FD VM=1 Termination Counts Threshold */
    public static final int TerminationCountsThreshold = 0x00189719;

    /** (0018,9720) VR=FD VM=1 Termination Density Threshold */
    public static final int TerminationDensityThreshold = 0x00189720;

    /** (0018,9721) VR=FD VM=1 Termination Relative Density Threshold */
    public static final int TerminationRelativeDensityThreshold = 0x00189721;

    /** (0018,9722) VR=FD VM=1 Termination Time Threshold */
    public static final int TerminationTimeThreshold = 0x00189722;

    /** (0018,9723) VR=FD VM=1 Termination Cardiac Trigger Count Threshold */
    public static final int TerminationCardiacTriggerCountThreshold = 0x00189723;

    /** (0018,9724) VR=FD VM=1 Termination Respiratory Trigger Count Threshold */
    public static final int TerminationRespiratoryTriggerCountThreshold = 0x00189724;

    /** (0018,9725) VR=CS VM=1 Detector Geometry */
    public static final int DetectorGeometry = 0x00189725;

    /** (0018,9726) VR=FD VM=1 Transverse Detector Separation */
    public static final int TransverseDetectorSeparation = 0x00189726;

    /** (0018,9727) VR=FD VM=1 Axial Detector Dimension */
    public static final int AxialDetectorDimension = 0x00189727;

    /** (0018,9729) VR=US VM=1 Radiopharmaceutical Agent Number */
    public static final int RadiopharmaceuticalAgentNumber = 0x00189729;

    /** (0018,9732) VR=SQ VM=1 PET Frame Acquisition Sequence */
    public static final int PETFrameAcquisitionSequence = 0x00189732;

    /** (0018,9733) VR=SQ VM=1 PET Detector Motion Details Sequence */
    public static final int PETDetectorMotionDetailsSequence = 0x00189733;

    /** (0018,9734) VR=SQ VM=1 PET Table Dynamics Sequence */
    public static final int PETTableDynamicsSequence = 0x00189734;

    /** (0018,9735) VR=SQ VM=1 PET Position Sequence */
    public static final int PETPositionSequence = 0x00189735;

    /** (0018,9736) VR=SQ VM=1 PET Frame Correction Factors Sequence */
    public static final int PETFrameCorrectionFactorsSequence = 0x00189736;

    /** (0018,9737) VR=SQ VM=1 Radiopharmaceutical Usage Sequence */
    public static final int RadiopharmaceuticalUsageSequence = 0x00189737;

    /** (0018,9738) VR=CS VM=1 Attenuation Correction Source */
    public static final int AttenuationCorrectionSource = 0x00189738;

    /** (0018,9739) VR=US VM=1 Number of Iterations */
    public static final int NumberOfIterations = 0x00189739;

    /** (0018,9740) VR=US VM=1 Number of Subsets */
    public static final int NumberOfSubsets = 0x00189740;

    /** (0018,9749) VR=SQ VM=1 PET Reconstruction Sequence */
    public static final int PETReconstructionSequence = 0x00189749;

    /** (0018,9751) VR=SQ VM=1 PET Frame Type Sequence */
    public static final int PETFrameTypeSequence = 0x00189751;

    /** (0018,9755) VR=CS VM=1 Time of Flight Information Used */
    public static final int TimeOfFlightInformationUsed = 0x00189755;

    /** (0018,9756) VR=CS VM=1 Reconstruction Type */
    public static final int ReconstructionType = 0x00189756;

    /** (0018,9758) VR=CS VM=1 Decay Corrected */
    public static final int DecayCorrected = 0x00189758;

    /** (0018,9759) VR=CS VM=1 Attenuation Corrected */
    public static final int AttenuationCorrected = 0x00189759;

    /** (0018,9760) VR=CS VM=1 Scatter Corrected */
    public static final int ScatterCorrected = 0x00189760;

    /** (0018,9761) VR=CS VM=1 Dead Time Corrected */
    public static final int DeadTimeCorrected = 0x00189761;

    /** (0018,9762) VR=CS VM=1 Gantry Motion Corrected */
    public static final int GantryMotionCorrected = 0x00189762;

    /** (0018,9763) VR=CS VM=1 Patient Motion Corrected */
    public static final int PatientMotionCorrected = 0x00189763;

    /** (0018,9764) VR=CS VM=1 Count Loss Normalization Corrected */
    public static final int CountLossNormalizationCorrected = 0x00189764;

    /** (0018,9765) VR=CS VM=1 Randoms Corrected */
    public static final int RandomsCorrected = 0x00189765;

    /** (0018,9766) VR=CS VM=1 Non-uniform Radial Sampling Corrected */
    public static final int NonUniformRadialSamplingCorrected = 0x00189766;

    /** (0018,9767) VR=CS VM=1 Sensitivity Calibrated */
    public static final int SensitivityCalibrated = 0x00189767;

    /** (0018,9768) VR=CS VM=1 Detector Normalization Correction */
    public static final int DetectorNormalizationCorrection = 0x00189768;

    /** (0018,9769) VR=CS VM=1 Iterative Reconstruction Method */
    public static final int IterativeReconstructionMethod = 0x00189769;

    /** (0018,9770) VR=CS VM=1 Attenuation Correction Temporal Relationship */
    public static final int AttenuationCorrectionTemporalRelationship = 0x00189770;

    /** (0018,9771) VR=SQ VM=1 Patient Physiological State Sequence */
    public static final int PatientPhysiologicalStateSequence = 0x00189771;

    /** (0018,9772) VR=SQ VM=1 Patient Physiological State Code Sequence */
    public static final int PatientPhysiologicalStateCodeSequence = 0x00189772;

    /** (0018,9801) VR=FD VM=1-n Depth(s) of Focus */
    public static final int DepthsOfFocus = 0x00189801;

    /** (0018,9803) VR=SQ VM=1 Excluded Intervals Sequence */
    public static final int ExcludedIntervalsSequence = 0x00189803;

    /** (0018,9804) VR=DT VM=1 Exclusion Start DateTime */
    public static final int ExclusionStartDateTime = 0x00189804;

    /** (0018,9805) VR=FD VM=1 Exclusion Duration */
    public static final int ExclusionDuration = 0x00189805;

    /** (0018,9806) VR=SQ VM=1 US Image Description Sequence */
    public static final int USImageDescriptionSequence = 0x00189806;

    /** (0018,9807) VR=SQ VM=1 Image Data Type Sequence */
    public static final int ImageDataTypeSequence = 0x00189807;

    /** (0018,9808) VR=CS VM=1 Data Type */
    public static final int DataType = 0x00189808;

    /** (0018,9809) VR=SQ VM=1 Transducer Scan Pattern Code Sequence */
    public static final int TransducerScanPatternCodeSequence = 0x00189809;

    /** (0018,980B) VR=CS VM=1 Aliased Data Type */
    public static final int AliasedDataType = 0x0018980B;

    /** (0018,980C) VR=CS VM=1 Position Measuring Device Used */
    public static final int PositionMeasuringDeviceUsed = 0x0018980C;

    /** (0018,980D) VR=SQ VM=1 Transducer Geometry Code Sequence */
    public static final int TransducerGeometryCodeSequence = 0x0018980D;

    /** (0018,980E) VR=SQ VM=1 Transducer Beam Steering Code Sequence */
    public static final int TransducerBeamSteeringCodeSequence = 0x0018980E;

    /** (0018,980F) VR=SQ VM=1 Transducer Application Code Sequence */
    public static final int TransducerApplicationCodeSequence = 0x0018980F;

    /** (0018,9810) VR=US or SS VM=1 Zero Velocity Pixel Value */
    public static final int ZeroVelocityPixelValue = 0x00189810;

    /** (0018,9900) VR=LO VM=1 Reference Location Label */
    public static final int ReferenceLocationLabel = 0x00189900;

    /** (0018,9901) VR=UT VM=1 Reference Location Description */
    public static final int ReferenceLocationDescription = 0x00189901;

    /** (0018,9902) VR=SQ VM=1 Reference Basis Code Sequence */
    public static final int ReferenceBasisCodeSequence = 0x00189902;

    /** (0018,9903) VR=SQ VM=1 Reference Geometry Code Sequence */
    public static final int ReferenceGeometryCodeSequence = 0x00189903;

    /** (0018,9904) VR=DS VM=1 Offset Distance */
    public static final int OffsetDistance = 0x00189904;

    /** (0018,9905) VR=CS VM=1 Offset Direction */
    public static final int OffsetDirection = 0x00189905;

    /** (0018,9906) VR=SQ VM=1 Potential Scheduled Protocol Code Sequence */
    public static final int PotentialScheduledProtocolCodeSequence = 0x00189906;

    /** (0018,9907) VR=SQ VM=1 Potential Requested Procedure Code Sequence */
    public static final int PotentialRequestedProcedureCodeSequence = 0x00189907;

    /** (0018,9908) VR=UC VM=1-n Potential Reasons for Procedure */
    public static final int PotentialReasonsForProcedure = 0x00189908;

    /** (0018,9909) VR=SQ VM=1 Potential Reasons for Procedure Code Sequence */
    public static final int PotentialReasonsForProcedureCodeSequence = 0x00189909;

    /** (0018,990A) VR=UC VM=1-n Potential Diagnostic Tasks */
    public static final int PotentialDiagnosticTasks = 0x0018990A;

    /** (0018,990B) VR=SQ VM=1 Contraindications Code Sequence */
    public static final int ContraindicationsCodeSequence = 0x0018990B;

    /** (0018,990C) VR=SQ VM=1 Referenced Defined Protocol Sequence */
    public static final int ReferencedDefinedProtocolSequence = 0x0018990C;

    /** (0018,990D) VR=SQ VM=1 Referenced Performed Protocol Sequence */
    public static final int ReferencedPerformedProtocolSequence = 0x0018990D;

    /** (0018,990E) VR=SQ VM=1 Predecessor Protocol Sequence */
    public static final int PredecessorProtocolSequence = 0x0018990E;

    /** (0018,990F) VR=UT VM=1 Protocol Planning Information */
    public static final int ProtocolPlanningInformation = 0x0018990F;

    /** (0018,9910) VR=UT VM=1 Protocol Design Rationale */
    public static final int ProtocolDesignRationale = 0x00189910;

    /** (0018,9911) VR=SQ VM=1 Patient Specification Sequence */
    public static final int PatientSpecificationSequence = 0x00189911;

    /** (0018,9912) VR=SQ VM=1 Model Specification Sequence */
    public static final int ModelSpecificationSequence = 0x00189912;

    /** (0018,9913) VR=SQ VM=1 Parameters Specification Sequence */
    public static final int ParametersSpecificationSequence = 0x00189913;

    /** (0018,9914) VR=SQ VM=1 Instruction Sequence */
    public static final int InstructionSequence = 0x00189914;

    /** (0018,9915) VR=US VM=1 Instruction Index */
    public static final int InstructionIndex = 0x00189915;

    /** (0018,9916) VR=LO VM=1 Instruction Text */
    public static final int InstructionText = 0x00189916;

    /** (0018,9917) VR=UT VM=1 Instruction Description */
    public static final int InstructionDescription = 0x00189917;

    /** (0018,9918) VR=CS VM=1 Instruction Performed Flag */
    public static final int InstructionPerformedFlag = 0x00189918;

    /** (0018,9919) VR=DT VM=1 Instruction Performed DateTime */
    public static final int InstructionPerformedDateTime = 0x00189919;

    /** (0018,991A) VR=UT VM=1 Instruction Performance Comment */
    public static final int InstructionPerformanceComment = 0x0018991A;

    /** (0018,991B) VR=SQ VM=1 Patient Positioning Instruction Sequence */
    public static final int PatientPositioningInstructionSequence = 0x0018991B;

    /** (0018,991C) VR=SQ VM=1 Positioning Method Code Sequence */
    public static final int PositioningMethodCodeSequence = 0x0018991C;

    /** (0018,991D) VR=SQ VM=1 Positioning Landmark Sequence */
    public static final int PositioningLandmarkSequence = 0x0018991D;

    /** (0018,991E) VR=UI VM=1 Target Frame of Reference UID */
    public static final int TargetFrameOfReferenceUID = 0x0018991E;

    /** (0018,991F) VR=SQ VM=1 Acquisition Protocol Element Specification Sequence */
    public static final int AcquisitionProtocolElementSpecificationSequence = 0x0018991F;

    /** (0018,9920) VR=SQ VM=1 Acquisition Protocol Element Sequence */
    public static final int AcquisitionProtocolElementSequence = 0x00189920;

    /** (0018,9921) VR=US VM=1 Protocol Element Number */
    public static final int ProtocolElementNumber = 0x00189921;

    /** (0018,9922) VR=LO VM=1 Protocol Element Name */
    public static final int ProtocolElementName = 0x00189922;

    /** (0018,9923) VR=UT VM=1 Protocol Element Characteristics Summary */
    public static final int ProtocolElementCharacteristicsSummary = 0x00189923;

    /** (0018,9924) VR=UT VM=1 Protocol Element Purpose */
    public static final int ProtocolElementPurpose = 0x00189924;

    /** (0018,9930) VR=CS VM=1 Acquisition Motion */
    public static final int AcquisitionMotion = 0x00189930;

    /** (0018,9931) VR=SQ VM=1 Acquisition Start Location Sequence */
    public static final int AcquisitionStartLocationSequence = 0x00189931;

    /** (0018,9932) VR=SQ VM=1 Acquisition End Location Sequence */
    public static final int AcquisitionEndLocationSequence = 0x00189932;

    /** (0018,9933) VR=SQ VM=1 Reconstruction Protocol Element Specification Sequence */
    public static final int ReconstructionProtocolElementSpecificationSequence = 0x00189933;

    /** (0018,9934) VR=SQ VM=1 Reconstruction Protocol Element Sequence */
    public static final int ReconstructionProtocolElementSequence = 0x00189934;

    /** (0018,9935) VR=SQ VM=1 Storage Protocol Element Specification Sequence */
    public static final int StorageProtocolElementSpecificationSequence = 0x00189935;

    /** (0018,9936) VR=SQ VM=1 Storage Protocol Element Sequence */
    public static final int StorageProtocolElementSequence = 0x00189936;

    /** (0018,9937) VR=LO VM=1 Requested Series Description */
    public static final int RequestedSeriesDescription = 0x00189937;

    /** (0018,9938) VR=US VM=1-n Source Acquisition Protocol Element Number */
    public static final int SourceAcquisitionProtocolElementNumber = 0x00189938;

    /** (0018,9939) VR=US VM=1-n Source Acquisition Beam Number */
    public static final int SourceAcquisitionBeamNumber = 0x00189939;

    /** (0018,993A) VR=US VM=1-n Source Reconstruction Protocol Element Number */
    public static final int SourceReconstructionProtocolElementNumber = 0x0018993A;

    /** (0018,993B) VR=SQ VM=1 Reconstruction Start Location Sequence */
    public static final int ReconstructionStartLocationSequence = 0x0018993B;

    /** (0018,993C) VR=SQ VM=1 Reconstruction End Location Sequence */
    public static final int ReconstructionEndLocationSequence = 0x0018993C;

    /** (0018,993D) VR=SQ VM=1 Reconstruction Algorithm Sequence */
    public static final int ReconstructionAlgorithmSequence = 0x0018993D;

    /** (0018,993E) VR=SQ VM=1 Reconstruction Target Center Location Sequence */
    public static final int ReconstructionTargetCenterLocationSequence = 0x0018993E;

    /** (0018,9941) VR=UT VM=1 Image Filter Description */
    public static final int ImageFilterDescription = 0x00189941;

    /** (0018,9942) VR=FD VM=1 CTDIvol Notification Trigger */
    public static final int CTDIvolNotificationTrigger = 0x00189942;

    /** (0018,9943) VR=FD VM=1 DLP Notification Trigger */
    public static final int DLPNotificationTrigger = 0x00189943;

    /** (0018,9944) VR=CS VM=1 Auto KVP Selection Type */
    public static final int AutoKVPSelectionType = 0x00189944;

    /** (0018,9945) VR=FD VM=1 Auto KVP Upper Bound */
    public static final int AutoKVPUpperBound = 0x00189945;

    /** (0018,9946) VR=FD VM=1 Auto KVP Lower Bound */
    public static final int AutoKVPLowerBound = 0x00189946;

    /** (0018,9947) VR=CS VM=1 Protocol Defined Patient Position */
    public static final int ProtocolDefinedPatientPosition = 0x00189947;

    /** (0018,A001) VR=SQ VM=1 Contributing Equipment Sequence */
    public static final int ContributingEquipmentSequence = 0x0018A001;

    /** (0018,A002) VR=DT VM=1 Contribution DateTime */
    public static final int ContributionDateTime = 0x0018A002;

    /** (0018,A003) VR=ST VM=1 Contribution Description */
    public static final int ContributionDescription = 0x0018A003;

    /** (0020,000D) VR=UI VM=1 Study Instance UID */
    public static final int StudyInstanceUID = 0x0020000D;

    /** (0020,000E) VR=UI VM=1 Series Instance UID */
    public static final int SeriesInstanceUID = 0x0020000E;

    /** (0020,0010) VR=SH VM=1 Study ID */
    public static final int StudyID = 0x00200010;

    /** (0020,0011) VR=IS VM=1 Series Number */
    public static final int SeriesNumber = 0x00200011;

    /** (0020,0012) VR=IS VM=1 Acquisition Number */
    public static final int AcquisitionNumber = 0x00200012;

    /** (0020,0013) VR=IS VM=1 Instance Number */
    public static final int InstanceNumber = 0x00200013;

    /** (0020,0014) VR=IS VM=1 Isotope Number (retired) */
    public static final int IsotopeNumber = 0x00200014;

    /** (0020,0015) VR=IS VM=1 Phase Number (retired) */
    public static final int PhaseNumber = 0x00200015;

    /** (0020,0016) VR=IS VM=1 Interval Number (retired) */
    public static final int IntervalNumber = 0x00200016;

    /** (0020,0017) VR=IS VM=1 Time Slot Number (retired) */
    public static final int TimeSlotNumber = 0x00200017;

    /** (0020,0018) VR=IS VM=1 Angle Number (retired) */
    public static final int AngleNumber = 0x00200018;

    /** (0020,0019) VR=IS VM=1 Item Number */
    public static final int ItemNumber = 0x00200019;

    /** (0020,0020) VR=CS VM=2 Patient Orientation */
    public static final int PatientOrientation = 0x00200020;

    /** (0020,0022) VR=IS VM=1 Overlay Number (retired) */
    public static final int OverlayNumber = 0x00200022;

    /** (0020,0024) VR=IS VM=1 Curve Number (retired) */
    public static final int CurveNumber = 0x00200024;

    /** (0020,0026) VR=IS VM=1 LUT Number (retired) */
    public static final int LUTNumber = 0x00200026;

    /** (0020,0030) VR=DS VM=3 Image Position (retired) */
    public static final int ImagePosition = 0x00200030;

    /** (0020,0032) VR=DS VM=3 Image Position (Patient) */
    public static final int ImagePositionPatient = 0x00200032;

    /** (0020,0035) VR=DS VM=6 Image Orientation (retired) */
    public static final int ImageOrientation = 0x00200035;

    /** (0020,0037) VR=DS VM=6 Image Orientation (Patient) */
    public static final int ImageOrientationPatient = 0x00200037;

    /** (0020,0050) VR=DS VM=1 Location (retired) */
    public static final int Location = 0x00200050;

    /** (0020,0052) VR=UI VM=1 Frame of Reference UID */
    public static final int FrameOfReferenceUID = 0x00200052;

    /** (0020,0060) VR=CS VM=1 Laterality */
    public static final int Laterality = 0x00200060;

    /** (0020,0062) VR=CS VM=1 Image Laterality */
    public static final int ImageLaterality = 0x00200062;

    /** (0020,0070) VR=LO VM=1 Image Geometry Type (retired) */
    public static final int ImageGeometryType = 0x00200070;

    /** (0020,0080) VR=CS VM=1-n Masking Image (retired) */
    public static final int MaskingImage = 0x00200080;

    /** (0020,00AA) VR=IS VM=1 Report Number (retired) */
    public static final int ReportNumber = 0x002000AA;

    /** (0020,0100) VR=IS VM=1 Temporal Position Identifier */
    public static final int TemporalPositionIdentifier = 0x00200100;

    /** (0020,0105) VR=IS VM=1 Number of Temporal Positions */
    public static final int NumberOfTemporalPositions = 0x00200105;

    /** (0020,0110) VR=DS VM=1 Temporal Resolution */
    public static final int TemporalResolution = 0x00200110;

    /** (0020,0200) VR=UI VM=1 Synchronization Frame of Reference UID */
    public static final int SynchronizationFrameOfReferenceUID = 0x00200200;

    /** (0020,0242) VR=UI VM=1 SOP Instance UID of Concatenation Source */
    public static final int SOPInstanceUIDOfConcatenationSource = 0x00200242;

    /** (0020,1000) VR=IS VM=1 Series in Study (retired) */
    public static final int SeriesInStudy = 0x00201000;

    /** (0020,1001) VR=IS VM=1 Acquisitions in Series (retired) */
    public static final int AcquisitionsInSeries = 0x00201001;

    /** (0020,1002) VR=IS VM=1 Images in Acquisition */
    public static final int ImagesInAcquisition = 0x00201002;

    /** (0020,1003) VR=IS VM=1 Images in Series (retired) */
    public static final int ImagesInSeries = 0x00201003;

    /** (0020,1004) VR=IS VM=1 Acquisitions in Study (retired) */
    public static final int AcquisitionsInStudy = 0x00201004;

    /** (0020,1005) VR=IS VM=1 Images in Study (retired) */
    public static final int ImagesInStudy = 0x00201005;

    /** (0020,1020) VR=LO VM=1-n Reference (retired) */
    public static final int Reference = 0x00201020;

    /** (0020,103F) VR=LO VM=1 Target Position Reference Indicator */
    public static final int TargetPositionReferenceIndicator = 0x0020103F;

    /** (0020,1040) VR=LO VM=1 Position Reference Indicator */
    public static final int PositionReferenceIndicator = 0x00201040;

    /** (0020,1041) VR=DS VM=1 Slice Location */
    public static final int SliceLocation = 0x00201041;

    /** (0020,1070) VR=IS VM=1-n Other Study Numbers (retired) */
    public static final int OtherStudyNumbers = 0x00201070;

    /** (0020,1200) VR=IS VM=1 Number of Patient Related Studies */
    public static final int NumberOfPatientRelatedStudies = 0x00201200;

    /** (0020,1202) VR=IS VM=1 Number of Patient Related Series */
    public static final int NumberOfPatientRelatedSeries = 0x00201202;

    /** (0020,1204) VR=IS VM=1 Number of Patient Related Instances */
    public static final int NumberOfPatientRelatedInstances = 0x00201204;

    /** (0020,1206) VR=IS VM=1 Number of Study Related Series */
    public static final int NumberOfStudyRelatedSeries = 0x00201206;

    /** (0020,1208) VR=IS VM=1 Number of Study Related Instances */
    public static final int NumberOfStudyRelatedInstances = 0x00201208;

    /** (0020,1209) VR=IS VM=1 Number of Series Related Instances */
    public static final int NumberOfSeriesRelatedInstances = 0x00201209;

    /** (0020,31xx) VR=CS VM=1-n Source Image IDs (retired) */
    public static final int SourceImageIDs = 0x00203100;

    /** (0020,3401) VR=CS VM=1 Modifying Device ID (retired) */
    public static final int ModifyingDeviceID = 0x00203401;

    /** (0020,3402) VR=CS VM=1 Modified Image ID (retired) */
    public static final int ModifiedImageID = 0x00203402;

    /** (0020,3403) VR=DA VM=1 Modified Image Date (retired) */
    public static final int ModifiedImageDate = 0x00203403;

    /** (0020,3404) VR=LO VM=1 Modifying Device Manufacturer (retired) */
    public static final int ModifyingDeviceManufacturer = 0x00203404;

    /** (0020,3405) VR=TM VM=1 Modified Image Time (retired) */
    public static final int ModifiedImageTime = 0x00203405;

    /** (0020,3406) VR=LO VM=1 Modified Image Description (retired) */
    public static final int ModifiedImageDescription = 0x00203406;

    /** (0020,4000) VR=LT VM=1 Image Comments */
    public static final int ImageComments = 0x00204000;

    /** (0020,5000) VR=AT VM=1-n Original Image Identification (retired) */
    public static final int OriginalImageIdentification = 0x00205000;

    /** (0020,5002) VR=LO VM=1-n Original Image Identification Nomenclature (retired) */
    public static final int OriginalImageIdentificationNomenclature = 0x00205002;

    /** (0020,9056) VR=SH VM=1 Stack ID */
    public static final int StackID = 0x00209056;

    /** (0020,9057) VR=UL VM=1 In-Stack Position Number */
    public static final int InStackPositionNumber = 0x00209057;

    /** (0020,9071) VR=SQ VM=1 Frame Anatomy Sequence */
    public static final int FrameAnatomySequence = 0x00209071;

    /** (0020,9072) VR=CS VM=1 Frame Laterality */
    public static final int FrameLaterality = 0x00209072;

    /** (0020,9111) VR=SQ VM=1 Frame Content Sequence */
    public static final int FrameContentSequence = 0x00209111;

    /** (0020,9113) VR=SQ VM=1 Plane Position Sequence */
    public static final int PlanePositionSequence = 0x00209113;

    /** (0020,9116) VR=SQ VM=1 Plane Orientation Sequence */
    public static final int PlaneOrientationSequence = 0x00209116;

    /** (0020,9128) VR=UL VM=1 Temporal Position Index */
    public static final int TemporalPositionIndex = 0x00209128;

    /** (0020,9153) VR=FD VM=1 Nominal Cardiac Trigger Delay Time */
    public static final int NominalCardiacTriggerDelayTime = 0x00209153;

    /** (0020,9154) VR=FL VM=1 Nominal Cardiac Trigger Time Prior To R-Peak */
    public static final int NominalCardiacTriggerTimePriorToRPeak = 0x00209154;

    /** (0020,9155) VR=FL VM=1 Actual Cardiac Trigger Time Prior To R-Peak */
    public static final int ActualCardiacTriggerTimePriorToRPeak = 0x00209155;

    /** (0020,9156) VR=US VM=1 Frame Acquisition Number */
    public static final int FrameAcquisitionNumber = 0x00209156;

    /** (0020,9157) VR=UL VM=1-n Dimension Index Values */
    public static final int DimensionIndexValues = 0x00209157;

    /** (0020,9158) VR=LT VM=1 Frame Comments */
    public static final int FrameComments = 0x00209158;

    /** (0020,9161) VR=UI VM=1 Concatenation UID */
    public static final int ConcatenationUID = 0x00209161;

    /** (0020,9162) VR=US VM=1 In-concatenation Number */
    public static final int InConcatenationNumber = 0x00209162;

    /** (0020,9163) VR=US VM=1 In-concatenation Total Number */
    public static final int InConcatenationTotalNumber = 0x00209163;

    /** (0020,9164) VR=UI VM=1 Dimension Organization UID */
    public static final int DimensionOrganizationUID = 0x00209164;

    /** (0020,9165) VR=AT VM=1 Dimension Index Pointer */
    public static final int DimensionIndexPointer = 0x00209165;

    /** (0020,9167) VR=AT VM=1 Functional Group Pointer */
    public static final int FunctionalGroupPointer = 0x00209167;

    /** (0020,9170) VR=SQ VM=1 Unassigned Shared Converted Attributes Sequence */
    public static final int UnassignedSharedConvertedAttributesSequence = 0x00209170;

    /** (0020,9171) VR=SQ VM=1 Unassigned Per-Frame Converted Attributes Sequence */
    public static final int UnassignedPerFrameConvertedAttributesSequence = 0x00209171;

    /** (0020,9172) VR=SQ VM=1 Conversion Source Attributes Sequence */
    public static final int ConversionSourceAttributesSequence = 0x00209172;

    /** (0020,9213) VR=LO VM=1 Dimension Index Private Creator */
    public static final int DimensionIndexPrivateCreator = 0x00209213;

    /** (0020,9221) VR=SQ VM=1 Dimension Organization Sequence */
    public static final int DimensionOrganizationSequence = 0x00209221;

    /** (0020,9222) VR=SQ VM=1 Dimension Index Sequence */
    public static final int DimensionIndexSequence = 0x00209222;

    /** (0020,9228) VR=UL VM=1 Concatenation Frame Offset Number */
    public static final int ConcatenationFrameOffsetNumber = 0x00209228;

    /** (0020,9238) VR=LO VM=1 Functional Group Private Creator */
    public static final int FunctionalGroupPrivateCreator = 0x00209238;

    /** (0020,9241) VR=FL VM=1 Nominal Percentage of Cardiac Phase */
    public static final int NominalPercentageOfCardiacPhase = 0x00209241;

    /** (0020,9245) VR=FL VM=1 Nominal Percentage of Respiratory Phase */
    public static final int NominalPercentageOfRespiratoryPhase = 0x00209245;

    /** (0020,9246) VR=FL VM=1 Starting Respiratory Amplitude */
    public static final int StartingRespiratoryAmplitude = 0x00209246;

    /** (0020,9247) VR=CS VM=1 Starting Respiratory Phase */
    public static final int StartingRespiratoryPhase = 0x00209247;

    /** (0020,9248) VR=FL VM=1 Ending Respiratory Amplitude */
    public static final int EndingRespiratoryAmplitude = 0x00209248;

    /** (0020,9249) VR=CS VM=1 Ending Respiratory Phase */
    public static final int EndingRespiratoryPhase = 0x00209249;

    /** (0020,9250) VR=CS VM=1 Respiratory Trigger Type */
    public static final int RespiratoryTriggerType = 0x00209250;

    /** (0020,9251) VR=FD VM=1 R-R Interval Time Nominal */
    public static final int RRIntervalTimeNominal = 0x00209251;

    /** (0020,9252) VR=FD VM=1 Actual Cardiac Trigger Delay Time */
    public static final int ActualCardiacTriggerDelayTime = 0x00209252;

    /** (0020,9253) VR=SQ VM=1 Respiratory Synchronization Sequence */
    public static final int RespiratorySynchronizationSequence = 0x00209253;

    /** (0020,9254) VR=FD VM=1 Respiratory Interval Time */
    public static final int RespiratoryIntervalTime = 0x00209254;

    /** (0020,9255) VR=FD VM=1 Nominal Respiratory Trigger Delay Time */
    public static final int NominalRespiratoryTriggerDelayTime = 0x00209255;

    /** (0020,9256) VR=FD VM=1 Respiratory Trigger Delay Threshold */
    public static final int RespiratoryTriggerDelayThreshold = 0x00209256;

    /** (0020,9257) VR=FD VM=1 Actual Respiratory Trigger Delay Time */
    public static final int ActualRespiratoryTriggerDelayTime = 0x00209257;

    /** (0020,9301) VR=FD VM=3 Image Position (Volume) */
    public static final int ImagePositionVolume = 0x00209301;

    /** (0020,9302) VR=FD VM=6 Image Orientation (Volume) */
    public static final int ImageOrientationVolume = 0x00209302;

    /** (0020,9307) VR=CS VM=1 Ultrasound Acquisition Geometry */
    public static final int UltrasoundAcquisitionGeometry = 0x00209307;

    /** (0020,9308) VR=FD VM=3 Apex Position */
    public static final int ApexPosition = 0x00209308;

    /** (0020,9309) VR=FD VM=16 Volume to Transducer Mapping Matrix */
    public static final int VolumeToTransducerMappingMatrix = 0x00209309;

    /** (0020,930A) VR=FD VM=16 Volume to Table Mapping Matrix */
    public static final int VolumeToTableMappingMatrix = 0x0020930A;

    /** (0020,930B) VR=CS VM=1 Volume to Transducer Relationship */
    public static final int VolumeToTransducerRelationship = 0x0020930B;

    /** (0020,930C) VR=CS VM=1 Patient Frame of Reference Source */
    public static final int PatientFrameOfReferenceSource = 0x0020930C;

    /** (0020,930D) VR=FD VM=1 Temporal Position Time Offset */
    public static final int TemporalPositionTimeOffset = 0x0020930D;

    /** (0020,930E) VR=SQ VM=1 Plane Position (Volume) Sequence */
    public static final int PlanePositionVolumeSequence = 0x0020930E;

    /** (0020,930F) VR=SQ VM=1 Plane Orientation (Volume) Sequence */
    public static final int PlaneOrientationVolumeSequence = 0x0020930F;

    /** (0020,9310) VR=SQ VM=1 Temporal Position Sequence */
    public static final int TemporalPositionSequence = 0x00209310;

    /** (0020,9311) VR=CS VM=1 Dimension Organization Type */
    public static final int DimensionOrganizationType = 0x00209311;

    /** (0020,9312) VR=UI VM=1 Volume Frame of Reference UID */
    public static final int VolumeFrameOfReferenceUID = 0x00209312;

    /** (0020,9313) VR=UI VM=1 Table Frame of Reference UID */
    public static final int TableFrameOfReferenceUID = 0x00209313;

    /** (0020,9421) VR=LO VM=1 Dimension Description Label */
    public static final int DimensionDescriptionLabel = 0x00209421;

    /** (0020,9450) VR=SQ VM=1 Patient Orientation in Frame Sequence */
    public static final int PatientOrientationInFrameSequence = 0x00209450;

    /** (0020,9453) VR=LO VM=1 Frame Label */
    public static final int FrameLabel = 0x00209453;

    /** (0020,9518) VR=US VM=1-n Acquisition Index */
    public static final int AcquisitionIndex = 0x00209518;

    /** (0020,9529) VR=SQ VM=1 Contributing SOP Instances Reference Sequence */
    public static final int ContributingSOPInstancesReferenceSequence = 0x00209529;

    /** (0020,9536) VR=US VM=1 Reconstruction Index */
    public static final int ReconstructionIndex = 0x00209536;

    /** (0022,0001) VR=US VM=1 Light Path Filter Pass-Through Wavelength */
    public static final int LightPathFilterPassThroughWavelength = 0x00220001;

    /** (0022,0002) VR=US VM=2 Light Path Filter Pass Band */
    public static final int LightPathFilterPassBand = 0x00220002;

    /** (0022,0003) VR=US VM=1 Image Path Filter Pass-Through Wavelength */
    public static final int ImagePathFilterPassThroughWavelength = 0x00220003;

    /** (0022,0004) VR=US VM=2 Image Path Filter Pass Band */
    public static final int ImagePathFilterPassBand = 0x00220004;

    /** (0022,0005) VR=CS VM=1 Patient Eye Movement Commanded */
    public static final int PatientEyeMovementCommanded = 0x00220005;

    /** (0022,0006) VR=SQ VM=1 Patient Eye Movement Command Code Sequence */
    public static final int PatientEyeMovementCommandCodeSequence = 0x00220006;

    /** (0022,0007) VR=FL VM=1 Spherical Lens Power */
    public static final int SphericalLensPower = 0x00220007;

    /** (0022,0008) VR=FL VM=1 Cylinder Lens Power */
    public static final int CylinderLensPower = 0x00220008;

    /** (0022,0009) VR=FL VM=1 Cylinder Axis */
    public static final int CylinderAxis = 0x00220009;

    /** (0022,000A) VR=FL VM=1 Emmetropic Magnification */
    public static final int EmmetropicMagnification = 0x0022000A;

    /** (0022,000B) VR=FL VM=1 Intra Ocular Pressure */
    public static final int IntraOcularPressure = 0x0022000B;

    /** (0022,000C) VR=FL VM=1 Horizontal Field of View */
    public static final int HorizontalFieldOfView = 0x0022000C;

    /** (0022,000D) VR=CS VM=1 Pupil Dilated */
    public static final int PupilDilated = 0x0022000D;

    /** (0022,000E) VR=FL VM=1 Degree of Dilation */
    public static final int DegreeOfDilation = 0x0022000E;

    /** (0022,0010) VR=FL VM=1 Stereo Baseline Angle */
    public static final int StereoBaselineAngle = 0x00220010;

    /** (0022,0011) VR=FL VM=1 Stereo Baseline Displacement */
    public static final int StereoBaselineDisplacement = 0x00220011;

    /** (0022,0012) VR=FL VM=1 Stereo Horizontal Pixel Offset */
    public static final int StereoHorizontalPixelOffset = 0x00220012;

    /** (0022,0013) VR=FL VM=1 Stereo Vertical Pixel Offset */
    public static final int StereoVerticalPixelOffset = 0x00220013;

    /** (0022,0014) VR=FL VM=1 Stereo Rotation */
    public static final int StereoRotation = 0x00220014;

    /** (0022,0015) VR=SQ VM=1 Acquisition Device Type Code Sequence */
    public static final int AcquisitionDeviceTypeCodeSequence = 0x00220015;

    /** (0022,0016) VR=SQ VM=1 Illumination Type Code Sequence */
    public static final int IlluminationTypeCodeSequence = 0x00220016;

    /** (0022,0017) VR=SQ VM=1 Light Path Filter Type Stack Code Sequence */
    public static final int LightPathFilterTypeStackCodeSequence = 0x00220017;

    /** (0022,0018) VR=SQ VM=1 Image Path Filter Type Stack Code Sequence */
    public static final int ImagePathFilterTypeStackCodeSequence = 0x00220018;

    /** (0022,0019) VR=SQ VM=1 Lenses Code Sequence */
    public static final int LensesCodeSequence = 0x00220019;

    /** (0022,001A) VR=SQ VM=1 Channel Description Code Sequence */
    public static final int ChannelDescriptionCodeSequence = 0x0022001A;

    /** (0022,001B) VR=SQ VM=1 Refractive State Sequence */
    public static final int RefractiveStateSequence = 0x0022001B;

    /** (0022,001C) VR=SQ VM=1 Mydriatic Agent Code Sequence */
    public static final int MydriaticAgentCodeSequence = 0x0022001C;

    /** (0022,001D) VR=SQ VM=1 Relative Image Position Code Sequence */
    public static final int RelativeImagePositionCodeSequence = 0x0022001D;

    /** (0022,001E) VR=FL VM=1 Camera Angle of View */
    public static final int CameraAngleOfView = 0x0022001E;

    /** (0022,0020) VR=SQ VM=1 Stereo Pairs Sequence */
    public static final int StereoPairsSequence = 0x00220020;

    /** (0022,0021) VR=SQ VM=1 Left Image Sequence */
    public static final int LeftImageSequence = 0x00220021;

    /** (0022,0022) VR=SQ VM=1 Right Image Sequence */
    public static final int RightImageSequence = 0x00220022;

    /** (0022,0028) VR=CS VM=1 Stereo Pairs Present */
    public static final int StereoPairsPresent = 0x00220028;

    /** (0022,0030) VR=FL VM=1 Axial Length of the Eye */
    public static final int AxialLengthOfTheEye = 0x00220030;

    /** (0022,0031) VR=SQ VM=1 Ophthalmic Frame Location Sequence */
    public static final int OphthalmicFrameLocationSequence = 0x00220031;

    /** (0022,0032) VR=FL VM=2-2n Reference Coordinates */
    public static final int ReferenceCoordinates = 0x00220032;

    /** (0022,0035) VR=FL VM=1 Depth Spatial Resolution */
    public static final int DepthSpatialResolution = 0x00220035;

    /** (0022,0036) VR=FL VM=1 Maximum Depth Distortion */
    public static final int MaximumDepthDistortion = 0x00220036;

    /** (0022,0037) VR=FL VM=1 Along-scan Spatial Resolution */
    public static final int AlongScanSpatialResolution = 0x00220037;

    /** (0022,0038) VR=FL VM=1 Maximum Along-scan Distortion */
    public static final int MaximumAlongScanDistortion = 0x00220038;

    /** (0022,0039) VR=CS VM=1 Ophthalmic Image Orientation */
    public static final int OphthalmicImageOrientation = 0x00220039;

    /** (0022,0041) VR=FL VM=1 Depth of Transverse Image */
    public static final int DepthOfTransverseImage = 0x00220041;

    /** (0022,0042) VR=SQ VM=1 Mydriatic Agent Concentration Units Sequence */
    public static final int MydriaticAgentConcentrationUnitsSequence = 0x00220042;

    /** (0022,0048) VR=FL VM=1 Across-scan Spatial Resolution */
    public static final int AcrossScanSpatialResolution = 0x00220048;

    /** (0022,0049) VR=FL VM=1 Maximum Across-scan Distortion */
    public static final int MaximumAcrossScanDistortion = 0x00220049;

    /** (0022,004E) VR=DS VM=1 Mydriatic Agent Concentration */
    public static final int MydriaticAgentConcentration = 0x0022004E;

    /** (0022,0055) VR=FL VM=1 Illumination Wave Length */
    public static final int IlluminationWaveLength = 0x00220055;

    /** (0022,0056) VR=FL VM=1 Illumination Power */
    public static final int IlluminationPower = 0x00220056;

    /** (0022,0057) VR=FL VM=1 Illumination Bandwidth */
    public static final int IlluminationBandwidth = 0x00220057;

    /** (0022,0058) VR=SQ VM=1 Mydriatic Agent Sequence */
    public static final int MydriaticAgentSequence = 0x00220058;

    /** (0022,1007) VR=SQ VM=1 Ophthalmic Axial Measurements Right Eye Sequence */
    public static final int OphthalmicAxialMeasurementsRightEyeSequence = 0x00221007;

    /** (0022,1008) VR=SQ VM=1 Ophthalmic Axial Measurements Left Eye Sequence */
    public static final int OphthalmicAxialMeasurementsLeftEyeSequence = 0x00221008;

    /** (0022,1009) VR=CS VM=1 Ophthalmic Axial Measurements Device Type */
    public static final int OphthalmicAxialMeasurementsDeviceType = 0x00221009;

    /** (0022,1010) VR=CS VM=1 Ophthalmic Axial Length Measurements Type */
    public static final int OphthalmicAxialLengthMeasurementsType = 0x00221010;

    /** (0022,1012) VR=SQ VM=1 Ophthalmic Axial Length Sequence */
    public static final int OphthalmicAxialLengthSequence = 0x00221012;

    /** (0022,1019) VR=FL VM=1 Ophthalmic Axial Length */
    public static final int OphthalmicAxialLength = 0x00221019;

    /** (0022,1024) VR=SQ VM=1 Lens Status Code Sequence */
    public static final int LensStatusCodeSequence = 0x00221024;

    /** (0022,1025) VR=SQ VM=1 Vitreous Status Code Sequence */
    public static final int VitreousStatusCodeSequence = 0x00221025;

    /** (0022,1028) VR=SQ VM=1 IOL Formula Code Sequence */
    public static final int IOLFormulaCodeSequence = 0x00221028;

    /** (0022,1029) VR=LO VM=1 IOL Formula Detail */
    public static final int IOLFormulaDetail = 0x00221029;

    /** (0022,1033) VR=FL VM=1 Keratometer Index */
    public static final int KeratometerIndex = 0x00221033;

    /** (0022,1035) VR=SQ VM=1 Source of Ophthalmic Axial Length Code Sequence */
    public static final int SourceOfOphthalmicAxialLengthCodeSequence = 0x00221035;

    /** (0022,1036) VR=SQ VM=1 Source of Corneal Size Data Code Sequence */
    public static final int SourceOfCornealSizeDataCodeSequence = 0x00221036;

    /** (0022,1037) VR=FL VM=1 Target Refraction */
    public static final int TargetRefraction = 0x00221037;

    /** (0022,1039) VR=CS VM=1 Refractive Procedure Occurred */
    public static final int RefractiveProcedureOccurred = 0x00221039;

    /** (0022,1040) VR=SQ VM=1 Refractive Surgery Type Code Sequence */
    public static final int RefractiveSurgeryTypeCodeSequence = 0x00221040;

    /** (0022,1044) VR=SQ VM=1 Ophthalmic Ultrasound Method Code Sequence */
    public static final int OphthalmicUltrasoundMethodCodeSequence = 0x00221044;

    /** (0022,1045) VR=SQ VM=1 Surgically Induced Astigmatism Sequence */
    public static final int SurgicallyInducedAstigmatismSequence = 0x00221045;

    /** (0022,1046) VR=CS VM=1 Type of Optical Correction */
    public static final int TypeOfOpticalCorrection = 0x00221046;

    /** (0022,1047) VR=SQ VM=1 Toric IOL Power Sequence */
    public static final int ToricIOLPowerSequence = 0x00221047;

    /** (0022,1048) VR=SQ VM=1 Predicted Toric Error Sequence */
    public static final int PredictedToricErrorSequence = 0x00221048;

    /** (0022,1049) VR=CS VM=1 Pre-Selected for Implantation */
    public static final int PreSelectedForImplantation = 0x00221049;

    /** (0022,104A) VR=SQ VM=1 Toric IOL Power for Exact Emmetropia Sequence */
    public static final int ToricIOLPowerForExactEmmetropiaSequence = 0x0022104A;

    /** (0022,104B) VR=SQ VM=1 Toric IOL Power for Exact Target Refraction Sequence */
    public static final int ToricIOLPowerForExactTargetRefractionSequence = 0x0022104B;

    /** (0022,1050) VR=SQ VM=1 Ophthalmic Axial Length Measurements Sequence */
    public static final int OphthalmicAxialLengthMeasurementsSequence = 0x00221050;

    /** (0022,1053) VR=FL VM=1 IOL Power */
    public static final int IOLPower = 0x00221053;

    /** (0022,1054) VR=FL VM=1 Predicted Refractive Error */
    public static final int PredictedRefractiveError = 0x00221054;

    /** (0022,1059) VR=FL VM=1 Ophthalmic Axial Length Velocity */
    public static final int OphthalmicAxialLengthVelocity = 0x00221059;

    /** (0022,1065) VR=LO VM=1 Lens Status Description */
    public static final int LensStatusDescription = 0x00221065;

    /** (0022,1066) VR=LO VM=1 Vitreous Status Description */
    public static final int VitreousStatusDescription = 0x00221066;

    /** (0022,1090) VR=SQ VM=1 IOL Power Sequence */
    public static final int IOLPowerSequence = 0x00221090;

    /** (0022,1092) VR=SQ VM=1 Lens Constant Sequence */
    public static final int LensConstantSequence = 0x00221092;

    /** (0022,1093) VR=LO VM=1 IOL Manufacturer */
    public static final int IOLManufacturer = 0x00221093;

    /** (0022,1094) VR=LO VM=1 Lens Constant Description (retired) */
    public static final int LensConstantDescription = 0x00221094;

    /** (0022,1095) VR=LO VM=1 Implant Name */
    public static final int ImplantName = 0x00221095;

    /** (0022,1096) VR=SQ VM=1 Keratometry Measurement Type Code Sequence */
    public static final int KeratometryMeasurementTypeCodeSequence = 0x00221096;

    /** (0022,1097) VR=LO VM=1 Implant Part Number */
    public static final int ImplantPartNumber = 0x00221097;

    /** (0022,1100) VR=SQ VM=1 Referenced Ophthalmic Axial Measurements Sequence */
    public static final int ReferencedOphthalmicAxialMeasurementsSequence = 0x00221100;

    /** (0022,1101) VR=SQ VM=1 Ophthalmic Axial Length Measurements Segment Name Code Sequence */
    public static final int OphthalmicAxialLengthMeasurementsSegmentNameCodeSequence = 0x00221101;

    /** (0022,1103) VR=SQ VM=1 Refractive Error Before Refractive Surgery Code Sequence */
    public static final int RefractiveErrorBeforeRefractiveSurgeryCodeSequence = 0x00221103;

    /** (0022,1121) VR=FL VM=1 IOL Power For Exact Emmetropia */
    public static final int IOLPowerForExactEmmetropia = 0x00221121;

    /** (0022,1122) VR=FL VM=1 IOL Power For Exact Target Refraction */
    public static final int IOLPowerForExactTargetRefraction = 0x00221122;

    /** (0022,1125) VR=SQ VM=1 Anterior Chamber Depth Definition Code Sequence */
    public static final int AnteriorChamberDepthDefinitionCodeSequence = 0x00221125;

    /** (0022,1127) VR=SQ VM=1 Lens Thickness Sequence */
    public static final int LensThicknessSequence = 0x00221127;

    /** (0022,1128) VR=SQ VM=1 Anterior Chamber Depth Sequence */
    public static final int AnteriorChamberDepthSequence = 0x00221128;

    /** (0022,112A) VR=SQ VM=1 Calculation Comment Sequence */
    public static final int CalculationCommentSequence = 0x0022112A;

    /** (0022,112B) VR=CS VM=1 Calculation Comment Type */
    public static final int CalculationCommentType = 0x0022112B;

    /** (0022,112C) VR=LT VM=1 Calculation Comment */
    public static final int CalculationComment = 0x0022112C;

    /** (0022,1130) VR=FL VM=1 Lens Thickness */
    public static final int LensThickness = 0x00221130;

    /** (0022,1131) VR=FL VM=1 Anterior Chamber Depth */
    public static final int AnteriorChamberDepth = 0x00221131;

    /** (0022,1132) VR=SQ VM=1 Source of Lens Thickness Data Code Sequence */
    public static final int SourceOfLensThicknessDataCodeSequence = 0x00221132;

    /** (0022,1133) VR=SQ VM=1 Source of Anterior Chamber Depth Data Code Sequence */
    public static final int SourceOfAnteriorChamberDepthDataCodeSequence = 0x00221133;

    /** (0022,1134) VR=SQ VM=1 Source of Refractive Measurements Sequence */
    public static final int SourceOfRefractiveMeasurementsSequence = 0x00221134;

    /** (0022,1135) VR=SQ VM=1 Source of Refractive Measurements Code Sequence */
    public static final int SourceOfRefractiveMeasurementsCodeSequence = 0x00221135;

    /** (0022,1140) VR=CS VM=1 Ophthalmic Axial Length Measurement Modified */
    public static final int OphthalmicAxialLengthMeasurementModified = 0x00221140;

    /** (0022,1150) VR=SQ VM=1 Ophthalmic Axial Length Data Source Code Sequence */
    public static final int OphthalmicAxialLengthDataSourceCodeSequence = 0x00221150;

    /** (0022,1153) VR=SQ VM=1 Ophthalmic Axial Length Acquisition Method Code Sequence (retired) */
    public static final int OphthalmicAxialLengthAcquisitionMethodCodeSequence = 0x00221153;

    /** (0022,1155) VR=FL VM=1 Signal to Noise Ratio */
    public static final int SignalToNoiseRatio = 0x00221155;

    /** (0022,1159) VR=LO VM=1 Ophthalmic Axial Length Data Source Description */
    public static final int OphthalmicAxialLengthDataSourceDescription = 0x00221159;

    /** (0022,1210) VR=SQ VM=1 Ophthalmic Axial Length Measurements Total Length Sequence */
    public static final int OphthalmicAxialLengthMeasurementsTotalLengthSequence = 0x00221210;

    /** (0022,1211) VR=SQ VM=1 Ophthalmic Axial Length Measurements Segmental Length Sequence */
    public static final int OphthalmicAxialLengthMeasurementsSegmentalLengthSequence = 0x00221211;

    /** (0022,1212) VR=SQ VM=1 Ophthalmic Axial Length Measurements Length Summation Sequence */
    public static final int OphthalmicAxialLengthMeasurementsLengthSummationSequence = 0x00221212;

    /** (0022,1220) VR=SQ VM=1 Ultrasound Ophthalmic Axial Length Measurements Sequence */
    public static final int UltrasoundOphthalmicAxialLengthMeasurementsSequence = 0x00221220;

    /** (0022,1225) VR=SQ VM=1 Optical Ophthalmic Axial Length Measurements Sequence */
    public static final int OpticalOphthalmicAxialLengthMeasurementsSequence = 0x00221225;

    /** (0022,1230) VR=SQ VM=1 Ultrasound Selected Ophthalmic Axial Length Sequence */
    public static final int UltrasoundSelectedOphthalmicAxialLengthSequence = 0x00221230;

    /** (0022,1250) VR=SQ VM=1 Ophthalmic Axial Length Selection Method Code Sequence */
    public static final int OphthalmicAxialLengthSelectionMethodCodeSequence = 0x00221250;

    /** (0022,1255) VR=SQ VM=1 Optical Selected Ophthalmic Axial Length Sequence */
    public static final int OpticalSelectedOphthalmicAxialLengthSequence = 0x00221255;

    /** (0022,1257) VR=SQ VM=1 Selected Segmental Ophthalmic Axial Length Sequence */
    public static final int SelectedSegmentalOphthalmicAxialLengthSequence = 0x00221257;

    /** (0022,1260) VR=SQ VM=1 Selected Total Ophthalmic Axial Length Sequence */
    public static final int SelectedTotalOphthalmicAxialLengthSequence = 0x00221260;

    /** (0022,1262) VR=SQ VM=1 Ophthalmic Axial Length Quality Metric Sequence */
    public static final int OphthalmicAxialLengthQualityMetricSequence = 0x00221262;

    /** (0022,1265) VR=SQ VM=1 Ophthalmic Axial Length Quality Metric Type Code Sequence (retired) */
    public static final int OphthalmicAxialLengthQualityMetricTypeCodeSequence = 0x00221265;

    /** (0022,1273) VR=LO VM=1 Ophthalmic Axial Length Quality Metric Type Description (retired) */
    public static final int OphthalmicAxialLengthQualityMetricTypeDescription = 0x00221273;

    /** (0022,1300) VR=SQ VM=1 Intraocular Lens Calculations Right Eye Sequence */
    public static final int IntraocularLensCalculationsRightEyeSequence = 0x00221300;

    /** (0022,1310) VR=SQ VM=1 Intraocular Lens Calculations Left Eye Sequence */
    public static final int IntraocularLensCalculationsLeftEyeSequence = 0x00221310;

    /** (0022,1330) VR=SQ VM=1 Referenced Ophthalmic Axial Length Measurement QC Image Sequence */
    public static final int ReferencedOphthalmicAxialLengthMeasurementQCImageSequence = 0x00221330;

    /** (0022,1415) VR=CS VM=1 Ophthalmic Mapping Device Type */
    public static final int OphthalmicMappingDeviceType = 0x00221415;

    /** (0022,1420) VR=SQ VM=1 Acquisition Method Code Sequence */
    public static final int AcquisitionMethodCodeSequence = 0x00221420;

    /** (0022,1423) VR=SQ VM=1 Acquisition Method Algorithm Sequence */
    public static final int AcquisitionMethodAlgorithmSequence = 0x00221423;

    /** (0022,1436) VR=SQ VM=1 Ophthalmic Thickness Map Type Code Sequence */
    public static final int OphthalmicThicknessMapTypeCodeSequence = 0x00221436;

    /** (0022,1443) VR=SQ VM=1 Ophthalmic Thickness Mapping Normals Sequence */
    public static final int OphthalmicThicknessMappingNormalsSequence = 0x00221443;

    /** (0022,1445) VR=SQ VM=1 Retinal Thickness Definition Code Sequence */
    public static final int RetinalThicknessDefinitionCodeSequence = 0x00221445;

    /** (0022,1450) VR=SQ VM=1 Pixel Value Mapping to Coded Concept Sequence */
    public static final int PixelValueMappingToCodedConceptSequence = 0x00221450;

    /** (0022,1452) VR=US or SS VM=1 Mapped Pixel Value */
    public static final int MappedPixelValue = 0x00221452;

    /** (0022,1454) VR=LO VM=1 Pixel Value Mapping Explanation */
    public static final int PixelValueMappingExplanation = 0x00221454;

    /** (0022,1458) VR=SQ VM=1 Ophthalmic Thickness Map Quality Threshold Sequence */
    public static final int OphthalmicThicknessMapQualityThresholdSequence = 0x00221458;

    /** (0022,1460) VR=FL VM=1 Ophthalmic Thickness Map Threshold Quality Rating */
    public static final int OphthalmicThicknessMapThresholdQualityRating = 0x00221460;

    /** (0022,1463) VR=FL VM=2 Anatomic Structure Reference Point */
    public static final int AnatomicStructureReferencePoint = 0x00221463;

    /** (0022,1465) VR=SQ VM=1 Registration to Localizer Sequence */
    public static final int RegistrationToLocalizerSequence = 0x00221465;

    /** (0022,1466) VR=CS VM=1 Registered Localizer Units */
    public static final int RegisteredLocalizerUnits = 0x00221466;

    /** (0022,1467) VR=FL VM=2 Registered Localizer Top Left Hand Corner */
    public static final int RegisteredLocalizerTopLeftHandCorner = 0x00221467;

    /** (0022,1468) VR=FL VM=2 Registered Localizer Bottom Right Hand Corner */
    public static final int RegisteredLocalizerBottomRightHandCorner = 0x00221468;

    /** (0022,1470) VR=SQ VM=1 Ophthalmic Thickness Map Quality Rating Sequence */
    public static final int OphthalmicThicknessMapQualityRatingSequence = 0x00221470;

    /** (0022,1472) VR=SQ VM=1 Relevant OPT Attributes Sequence */
    public static final int RelevantOPTAttributesSequence = 0x00221472;

    /** (0022,1512) VR=SQ VM=1 Transformation Method Code Sequence */
    public static final int TransformationMethodCodeSequence = 0x00221512;

    /** (0022,1513) VR=SQ VM=1 Transformation Algorithm Sequence */
    public static final int TransformationAlgorithmSequence = 0x00221513;

    /** (0022,1515) VR=CS VM=1 Ophthalmic Axial Length Method */
    public static final int OphthalmicAxialLengthMethod = 0x00221515;

    /** (0022,1517) VR=FL VM=1 Ophthalmic FOV */
    public static final int OphthalmicFOV = 0x00221517;

    /** (0022,1518) VR=SQ VM=1 Two Dimensional to Three Dimensional Map Sequence */
    public static final int TwoDimensionalToThreeDimensionalMapSequence = 0x00221518;

    /** (0022,1525) VR=SQ VM=1 Wide Field Ophthalmic Photography Quality Rating Sequence */
    public static final int WideFieldOphthalmicPhotographyQualityRatingSequence = 0x00221525;

    /** (0022,1526) VR=SQ VM=1 Wide Field Ophthalmic Photography Quality Threshold Sequence */
    public static final int WideFieldOphthalmicPhotographyQualityThresholdSequence = 0x00221526;

    /** (0022,1527) VR=FL VM=1 Wide Field Ophthalmic Photography Threshold Quality Rating */
    public static final int WideFieldOphthalmicPhotographyThresholdQualityRating = 0x00221527;

    /** (0022,1528) VR=FL VM=1 X Coordinates Center Pixel View Angle */
    public static final int XCoordinatesCenterPixelViewAngle = 0x00221528;

    /** (0022,1529) VR=FL VM=1 Y Coordinates Center Pixel View Angle */
    public static final int YCoordinatesCenterPixelViewAngle = 0x00221529;

    /** (0022,1530) VR=UL VM=1 Number of Map Points */
    public static final int NumberOfMapPoints = 0x00221530;

    /** (0022,1531) VR=OF VM=1 Two Dimensional to Three Dimensional Map Data */
    public static final int TwoDimensionalToThreeDimensionalMapData = 0x00221531;

    /** (0022,1612) VR=SQ VM=1 Derivation Algorithm Sequence */
    public static final int DerivationAlgorithmSequence = 0x00221612;

    /** (0022,1615) VR=SQ VM=1 Ophthalmic Image Type Code Sequence */
    public static final int OphthalmicImageTypeCodeSequence = 0x00221615;

    /** (0022,1616) VR=LO VM=1 Ophthalmic Image Type Description */
    public static final int OphthalmicImageTypeDescription = 0x00221616;

    /** (0022,1618) VR=SQ VM=1 Scan Pattern Type Code Sequence */
    public static final int ScanPatternTypeCodeSequence = 0x00221618;

    /** (0022,1620) VR=SQ VM=1 Referenced Surface Mesh Identification Sequence */
    public static final int ReferencedSurfaceMeshIdentificationSequence = 0x00221620;

    /** (0022,1622) VR=CS VM=1 Ophthalmic Volumetric Properties Flag */
    public static final int OphthalmicVolumetricPropertiesFlag = 0x00221622;

    /** (0022,1624) VR=FL VM=1 Ophthalmic Anatomic Reference Point X-Coordinate */
    public static final int OphthalmicAnatomicReferencePointXCoordinate = 0x00221624;

    /** (0022,1626) VR=FL VM=1 Ophthalmic Anatomic Reference Point Y-Coordinate */
    public static final int OphthalmicAnatomicReferencePointYCoordinate = 0x00221626;

    /** (0022,1628) VR=SQ VM=1 Ophthalmic En Face Image Quality Rating Sequence */
    public static final int OphthalmicEnFaceImageQualityRatingSequence = 0x00221628;

    /** (0022,1630) VR=DS VM=1 Quality Threshold */
    public static final int QualityThreshold = 0x00221630;

    /** (0022,1640) VR=SQ VM=1 OCT B-scan Analysis Acquisition Parameters Sequence */
    public static final int OCTBscanAnalysisAcquisitionParametersSequence = 0x00221640;

    /** (0022,1642) VR=UL VM=1 Number of B-scans Per Frame */
    public static final int NumberofBscansPerFrame = 0x00221642;

    /** (0022,1643) VR=FL VM=1 B-scan Slab Thickness */
    public static final int BscanSlabThickness = 0x00221643;

    /** (0022,1644) VR=FL VM=1 Distance Between B-scan Slabs */
    public static final int DistanceBetweenBscanSlabs = 0x00221644;

    /** (0022,1645) VR=FL VM=1 B-scan Cycle Time */
    public static final int BscanCycleTime = 0x00221645;

    /** (0022,1646) VR=FL VM=1-n B-scan Cycle Time Vector */
    public static final int BscanCycleTimeVector = 0x00221646;

    /** (0022,1649) VR=FL VM=1 A-scan Rate */
    public static final int AscanRate = 0x00221649;

    /** (0022,1650) VR=FL VM=1 B-scan Rate */
    public static final int BscanRate = 0x00221650;

    /** (0022,1658) VR=UL VM=1 Surface Mesh Z-Pixel Offset */
    public static final int SurfaceMeshZPixelOffset = 0x00221658;

    /** (0024,0010) VR=FL VM=1 Visual Field Horizontal Extent */
    public static final int VisualFieldHorizontalExtent = 0x00240010;

    /** (0024,0011) VR=FL VM=1 Visual Field Vertical Extent */
    public static final int VisualFieldVerticalExtent = 0x00240011;

    /** (0024,0012) VR=CS VM=1 Visual Field Shape */
    public static final int VisualFieldShape = 0x00240012;

    /** (0024,0016) VR=SQ VM=1 Screening Test Mode Code Sequence */
    public static final int ScreeningTestModeCodeSequence = 0x00240016;

    /** (0024,0018) VR=FL VM=1 Maximum Stimulus Luminance */
    public static final int MaximumStimulusLuminance = 0x00240018;

    /** (0024,0020) VR=FL VM=1 Background Luminance */
    public static final int BackgroundLuminance = 0x00240020;

    /** (0024,0021) VR=SQ VM=1 Stimulus Color Code Sequence */
    public static final int StimulusColorCodeSequence = 0x00240021;

    /** (0024,0024) VR=SQ VM=1 Background Illumination Color Code Sequence */
    public static final int BackgroundIlluminationColorCodeSequence = 0x00240024;

    /** (0024,0025) VR=FL VM=1 Stimulus Area */
    public static final int StimulusArea = 0x00240025;

    /** (0024,0028) VR=FL VM=1 Stimulus Presentation Time */
    public static final int StimulusPresentationTime = 0x00240028;

    /** (0024,0032) VR=SQ VM=1 Fixation Sequence */
    public static final int FixationSequence = 0x00240032;

    /** (0024,0033) VR=SQ VM=1 Fixation Monitoring Code Sequence */
    public static final int FixationMonitoringCodeSequence = 0x00240033;

    /** (0024,0034) VR=SQ VM=1 Visual Field Catch Trial Sequence */
    public static final int VisualFieldCatchTrialSequence = 0x00240034;

    /** (0024,0035) VR=US VM=1 Fixation Checked Quantity */
    public static final int FixationCheckedQuantity = 0x00240035;

    /** (0024,0036) VR=US VM=1 Patient Not Properly Fixated Quantity */
    public static final int PatientNotProperlyFixatedQuantity = 0x00240036;

    /** (0024,0037) VR=CS VM=1 Presented Visual Stimuli Data Flag */
    public static final int PresentedVisualStimuliDataFlag = 0x00240037;

    /** (0024,0038) VR=US VM=1 Number of Visual Stimuli */
    public static final int NumberOfVisualStimuli = 0x00240038;

    /** (0024,0039) VR=CS VM=1 Excessive Fixation Losses Data Flag */
    public static final int ExcessiveFixationLossesDataFlag = 0x00240039;

    /** (0024,0040) VR=CS VM=1 Excessive Fixation Losses */
    public static final int ExcessiveFixationLosses = 0x00240040;

    /** (0024,0042) VR=US VM=1 Stimuli Retesting Quantity */
    public static final int StimuliRetestingQuantity = 0x00240042;

    /** (0024,0044) VR=LT VM=1 Comments on Patient's Performance of Visual Field */
    public static final int CommentsOnPatientPerformanceOfVisualField = 0x00240044;

    /** (0024,0045) VR=CS VM=1 False Negatives Estimate Flag */
    public static final int FalseNegativesEstimateFlag = 0x00240045;

    /** (0024,0046) VR=FL VM=1 False Negatives Estimate */
    public static final int FalseNegativesEstimate = 0x00240046;

    /** (0024,0048) VR=US VM=1 Negative Catch Trials Quantity */
    public static final int NegativeCatchTrialsQuantity = 0x00240048;

    /** (0024,0050) VR=US VM=1 False Negatives Quantity */
    public static final int FalseNegativesQuantity = 0x00240050;

    /** (0024,0051) VR=CS VM=1 Excessive False Negatives Data Flag */
    public static final int ExcessiveFalseNegativesDataFlag = 0x00240051;

    /** (0024,0052) VR=CS VM=1 Excessive False Negatives */
    public static final int ExcessiveFalseNegatives = 0x00240052;

    /** (0024,0053) VR=CS VM=1 False Positives Estimate Flag */
    public static final int FalsePositivesEstimateFlag = 0x00240053;

    /** (0024,0054) VR=FL VM=1 False Positives Estimate */
    public static final int FalsePositivesEstimate = 0x00240054;

    /** (0024,0055) VR=CS VM=1 Catch Trials Data Flag */
    public static final int CatchTrialsDataFlag = 0x00240055;

    /** (0024,0056) VR=US VM=1 Positive Catch Trials Quantity */
    public static final int PositiveCatchTrialsQuantity = 0x00240056;

    /** (0024,0057) VR=CS VM=1 Test Point Normals Data Flag */
    public static final int TestPointNormalsDataFlag = 0x00240057;

    /** (0024,0058) VR=SQ VM=1 Test Point Normals Sequence */
    public static final int TestPointNormalsSequence = 0x00240058;

    /** (0024,0059) VR=CS VM=1 Global Deviation Probability Normals Flag */
    public static final int GlobalDeviationProbabilityNormalsFlag = 0x00240059;

    /** (0024,0060) VR=US VM=1 False Positives Quantity */
    public static final int FalsePositivesQuantity = 0x00240060;

    /** (0024,0061) VR=CS VM=1 Excessive False Positives Data Flag */
    public static final int ExcessiveFalsePositivesDataFlag = 0x00240061;

    /** (0024,0062) VR=CS VM=1 Excessive False Positives */
    public static final int ExcessiveFalsePositives = 0x00240062;

    /** (0024,0063) VR=CS VM=1 Visual Field Test Normals Flag */
    public static final int VisualFieldTestNormalsFlag = 0x00240063;

    /** (0024,0064) VR=SQ VM=1 Results Normals Sequence */
    public static final int ResultsNormalsSequence = 0x00240064;

    /** (0024,0065) VR=SQ VM=1 Age Corrected Sensitivity Deviation Algorithm Sequence */
    public static final int AgeCorrectedSensitivityDeviationAlgorithmSequence = 0x00240065;

    /** (0024,0066) VR=FL VM=1 Global Deviation From Normal */
    public static final int GlobalDeviationFromNormal = 0x00240066;

    /** (0024,0067) VR=SQ VM=1 Generalized Defect Sensitivity Deviation Algorithm Sequence */
    public static final int GeneralizedDefectSensitivityDeviationAlgorithmSequence = 0x00240067;

    /** (0024,0068) VR=FL VM=1 Localized Deviation From Normal */
    public static final int LocalizedDeviationFromNormal = 0x00240068;

    /** (0024,0069) VR=LO VM=1 Patient Reliability Indicator */
    public static final int PatientReliabilityIndicator = 0x00240069;

    /** (0024,0070) VR=FL VM=1 Visual Field Mean Sensitivity */
    public static final int VisualFieldMeanSensitivity = 0x00240070;

    /** (0024,0071) VR=FL VM=1 Global Deviation Probability */
    public static final int GlobalDeviationProbability = 0x00240071;

    /** (0024,0072) VR=CS VM=1 Local Deviation Probability Normals Flag */
    public static final int LocalDeviationProbabilityNormalsFlag = 0x00240072;

    /** (0024,0073) VR=FL VM=1 Localized Deviation Probability */
    public static final int LocalizedDeviationProbability = 0x00240073;

    /** (0024,0074) VR=CS VM=1 Short Term Fluctuation Calculated */
    public static final int ShortTermFluctuationCalculated = 0x00240074;

    /** (0024,0075) VR=FL VM=1 Short Term Fluctuation */
    public static final int ShortTermFluctuation = 0x00240075;

    /** (0024,0076) VR=CS VM=1 Short Term Fluctuation Probability Calculated */
    public static final int ShortTermFluctuationProbabilityCalculated = 0x00240076;

    /** (0024,0077) VR=FL VM=1 Short Term Fluctuation Probability */
    public static final int ShortTermFluctuationProbability = 0x00240077;

    /** (0024,0078) VR=CS VM=1 Corrected Localized Deviation From Normal Calculated */
    public static final int CorrectedLocalizedDeviationFromNormalCalculated = 0x00240078;

    /** (0024,0079) VR=FL VM=1 Corrected Localized Deviation From Normal */
    public static final int CorrectedLocalizedDeviationFromNormal = 0x00240079;

    /** (0024,0080) VR=CS VM=1 Corrected Localized Deviation From Normal Probability Calculated */
    public static final int CorrectedLocalizedDeviationFromNormalProbabilityCalculated = 0x00240080;

    /** (0024,0081) VR=FL VM=1 Corrected Localized Deviation From Normal Probability */
    public static final int CorrectedLocalizedDeviationFromNormalProbability = 0x00240081;

    /** (0024,0083) VR=SQ VM=1 Global Deviation Probability Sequence */
    public static final int GlobalDeviationProbabilitySequence = 0x00240083;

    /** (0024,0085) VR=SQ VM=1 Localized Deviation Probability Sequence */
    public static final int LocalizedDeviationProbabilitySequence = 0x00240085;

    /** (0024,0086) VR=CS VM=1 Foveal Sensitivity Measured */
    public static final int FovealSensitivityMeasured = 0x00240086;

    /** (0024,0087) VR=FL VM=1 Foveal Sensitivity */
    public static final int FovealSensitivity = 0x00240087;

    /** (0024,0088) VR=FL VM=1 Visual Field Test Duration */
    public static final int VisualFieldTestDuration = 0x00240088;

    /** (0024,0089) VR=SQ VM=1 Visual Field Test Point Sequence */
    public static final int VisualFieldTestPointSequence = 0x00240089;

    /** (0024,0090) VR=FL VM=1 Visual Field Test Point X-Coordinate */
    public static final int VisualFieldTestPointXCoordinate = 0x00240090;

    /** (0024,0091) VR=FL VM=1 Visual Field Test Point Y-Coordinate */
    public static final int VisualFieldTestPointYCoordinate = 0x00240091;

    /** (0024,0092) VR=FL VM=1 Age Corrected Sensitivity Deviation Value */
    public static final int AgeCorrectedSensitivityDeviationValue = 0x00240092;

    /** (0024,0093) VR=CS VM=1 Stimulus Results */
    public static final int StimulusResults = 0x00240093;

    /** (0024,0094) VR=FL VM=1 Sensitivity Value */
    public static final int SensitivityValue = 0x00240094;

    /** (0024,0095) VR=CS VM=1 Retest Stimulus Seen */
    public static final int RetestStimulusSeen = 0x00240095;

    /** (0024,0096) VR=FL VM=1 Retest Sensitivity Value */
    public static final int RetestSensitivityValue = 0x00240096;

    /** (0024,0097) VR=SQ VM=1 Visual Field Test Point Normals Sequence */
    public static final int VisualFieldTestPointNormalsSequence = 0x00240097;

    /** (0024,0098) VR=FL VM=1 Quantified Defect */
    public static final int QuantifiedDefect = 0x00240098;

    /** (0024,0100) VR=FL VM=1 Age Corrected Sensitivity Deviation Probability Value */
    public static final int AgeCorrectedSensitivityDeviationProbabilityValue = 0x00240100;

    /** (0024,0102) VR=CS VM=1 Generalized Defect Corrected Sensitivity Deviation Flag */
    public static final int GeneralizedDefectCorrectedSensitivityDeviationFlag = 0x00240102;

    /** (0024,0103) VR=FL VM=1 Generalized Defect Corrected Sensitivity Deviation Value */
    public static final int GeneralizedDefectCorrectedSensitivityDeviationValue = 0x00240103;

    /** (0024,0104) VR=FL VM=1 Generalized Defect Corrected Sensitivity Deviation Probability Value */
    public static final int GeneralizedDefectCorrectedSensitivityDeviationProbabilityValue = 0x00240104;

    /** (0024,0105) VR=FL VM=1 Minimum Sensitivity Value */
    public static final int MinimumSensitivityValue = 0x00240105;

    /** (0024,0106) VR=CS VM=1 Blind Spot Localized */
    public static final int BlindSpotLocalized = 0x00240106;

    /** (0024,0107) VR=FL VM=1 Blind Spot X-Coordinate */
    public static final int BlindSpotXCoordinate = 0x00240107;

    /** (0024,0108) VR=FL VM=1 Blind Spot Y-Coordinate */
    public static final int BlindSpotYCoordinate = 0x00240108;

    /** (0024,0110) VR=SQ VM=1 Visual Acuity Measurement Sequence */
    public static final int VisualAcuityMeasurementSequence = 0x00240110;

    /** (0024,0112) VR=SQ VM=1 Refractive Parameters Used on Patient Sequence */
    public static final int RefractiveParametersUsedOnPatientSequence = 0x00240112;

    /** (0024,0113) VR=CS VM=1 Measurement Laterality */
    public static final int MeasurementLaterality = 0x00240113;

    /** (0024,0114) VR=SQ VM=1 Ophthalmic Patient Clinical Information Left Eye Sequence */
    public static final int OphthalmicPatientClinicalInformationLeftEyeSequence = 0x00240114;

    /** (0024,0115) VR=SQ VM=1 Ophthalmic Patient Clinical Information Right Eye Sequence */
    public static final int OphthalmicPatientClinicalInformationRightEyeSequence = 0x00240115;

    /** (0024,0117) VR=CS VM=1 Foveal Point Normative Data Flag */
    public static final int FovealPointNormativeDataFlag = 0x00240117;

    /** (0024,0118) VR=FL VM=1 Foveal Point Probability Value */
    public static final int FovealPointProbabilityValue = 0x00240118;

    /** (0024,0120) VR=CS VM=1 Screening Baseline Measured */
    public static final int ScreeningBaselineMeasured = 0x00240120;

    /** (0024,0122) VR=SQ VM=1 Screening Baseline Measured Sequence */
    public static final int ScreeningBaselineMeasuredSequence = 0x00240122;

    /** (0024,0124) VR=CS VM=1 Screening Baseline Type */
    public static final int ScreeningBaselineType = 0x00240124;

    /** (0024,0126) VR=FL VM=1 Screening Baseline Value */
    public static final int ScreeningBaselineValue = 0x00240126;

    /** (0024,0202) VR=LO VM=1 Algorithm Source */
    public static final int AlgorithmSource = 0x00240202;

    /** (0024,0306) VR=LO VM=1 Data Set Name */
    public static final int DataSetName = 0x00240306;

    /** (0024,0307) VR=LO VM=1 Data Set Version */
    public static final int DataSetVersion = 0x00240307;

    /** (0024,0308) VR=LO VM=1 Data Set Source */
    public static final int DataSetSource = 0x00240308;

    /** (0024,0309) VR=LO VM=1 Data Set Description */
    public static final int DataSetDescription = 0x00240309;

    /** (0024,0317) VR=SQ VM=1 Visual Field Test Reliability Global Index Sequence */
    public static final int VisualFieldTestReliabilityGlobalIndexSequence = 0x00240317;

    /** (0024,0320) VR=SQ VM=1 Visual Field Global Results Index Sequence */
    public static final int VisualFieldGlobalResultsIndexSequence = 0x00240320;

    /** (0024,0325) VR=SQ VM=1 Data Observation Sequence */
    public static final int DataObservationSequence = 0x00240325;

    /** (0024,0338) VR=CS VM=1 Index Normals Flag */
    public static final int IndexNormalsFlag = 0x00240338;

    /** (0024,0341) VR=FL VM=1 Index Probability */
    public static final int IndexProbability = 0x00240341;

    /** (0024,0344) VR=SQ VM=1 Index Probability Sequence */
    public static final int IndexProbabilitySequence = 0x00240344;

    /** (0028,0002) VR=US VM=1 Samples per Pixel */
    public static final int SamplesPerPixel = 0x00280002;

    /** (0028,0003) VR=US VM=1 Samples per Pixel Used */
    public static final int SamplesPerPixelUsed = 0x00280003;

    /** (0028,0004) VR=CS VM=1 Photometric Interpretation */
    public static final int PhotometricInterpretation = 0x00280004;

    /** (0028,0005) VR=US VM=1 Image Dimensions (retired) */
    public static final int ImageDimensions = 0x00280005;

    /** (0028,0006) VR=US VM=1 Planar Configuration */
    public static final int PlanarConfiguration = 0x00280006;

    /** (0028,0008) VR=IS VM=1 Number of Frames */
    public static final int NumberOfFrames = 0x00280008;

    /** (0028,0009) VR=AT VM=1-n Frame Increment Pointer */
    public static final int FrameIncrementPointer = 0x00280009;

    /** (0028,000A) VR=AT VM=1-n Frame Dimension Pointer */
    public static final int FrameDimensionPointer = 0x0028000A;

    /** (0028,0010) VR=US VM=1 Rows */
    public static final int Rows = 0x00280010;

    /** (0028,0011) VR=US VM=1 Columns */
    public static final int Columns = 0x00280011;

    /** (0028,0012) VR=US VM=1 Planes (retired) */
    public static final int Planes = 0x00280012;

    /** (0028,0014) VR=US VM=1 Ultrasound Color Data Present */
    public static final int UltrasoundColorDataPresent = 0x00280014;

    /** (0028,0030) VR=DS VM=2 Pixel Spacing */
    public static final int PixelSpacing = 0x00280030;

    /** (0028,0031) VR=DS VM=2 Zoom Factor */
    public static final int ZoomFactor = 0x00280031;

    /** (0028,0032) VR=DS VM=2 Zoom Center */
    public static final int ZoomCenter = 0x00280032;

    /** (0028,0034) VR=IS VM=2 Pixel Aspect Ratio */
    public static final int PixelAspectRatio = 0x00280034;

    /** (0028,0040) VR=CS VM=1 Image Format (retired) */
    public static final int ImageFormat = 0x00280040;

    /** (0028,0050) VR=LO VM=1-n Manipulated Image (retired) */
    public static final int ManipulatedImage = 0x00280050;

    /** (0028,0051) VR=CS VM=1-n Corrected Image */
    public static final int CorrectedImage = 0x00280051;

    /** (0028,005F) VR=LO VM=1 Compression Recognition Code (retired) */
    public static final int CompressionRecognitionCode = 0x0028005F;

    /** (0028,0060) VR=CS VM=1 Compression Code (retired) */
    public static final int CompressionCode = 0x00280060;

    /** (0028,0061) VR=SH VM=1 Compression Originator (retired) */
    public static final int CompressionOriginator = 0x00280061;

    /** (0028,0062) VR=LO VM=1 Compression Label (retired) */
    public static final int CompressionLabel = 0x00280062;

    /** (0028,0063) VR=SH VM=1 Compression Description (retired) */
    public static final int CompressionDescription = 0x00280063;

    /** (0028,0065) VR=CS VM=1-n Compression Sequence (retired) */
    public static final int CompressionSequence = 0x00280065;

    /** (0028,0066) VR=AT VM=1-n Compression Step Pointers (retired) */
    public static final int CompressionStepPointers = 0x00280066;

    /** (0028,0068) VR=US VM=1 Repeat Interval (retired) */
    public static final int RepeatInterval = 0x00280068;

    /** (0028,0069) VR=US VM=1 Bits Grouped (retired) */
    public static final int BitsGrouped = 0x00280069;

    /** (0028,0070) VR=US VM=1-n Perimeter Table (retired) */
    public static final int PerimeterTable = 0x00280070;

    /** (0028,0071) VR=US or SS VM=1 Perimeter Value (retired) */
    public static final int PerimeterValue = 0x00280071;

    /** (0028,0080) VR=US VM=1 Predictor Rows (retired) */
    public static final int PredictorRows = 0x00280080;

    /** (0028,0081) VR=US VM=1 Predictor Columns (retired) */
    public static final int PredictorColumns = 0x00280081;

    /** (0028,0082) VR=US VM=1-n Predictor Constants (retired) */
    public static final int PredictorConstants = 0x00280082;

    /** (0028,0090) VR=CS VM=1 Blocked Pixels (retired) */
    public static final int BlockedPixels = 0x00280090;

    /** (0028,0091) VR=US VM=1 Block Rows (retired) */
    public static final int BlockRows = 0x00280091;

    /** (0028,0092) VR=US VM=1 Block Columns (retired) */
    public static final int BlockColumns = 0x00280092;

    /** (0028,0093) VR=US VM=1 Row Overlap (retired) */
    public static final int RowOverlap = 0x00280093;

    /** (0028,0094) VR=US VM=1 Column Overlap (retired) */
    public static final int ColumnOverlap = 0x00280094;

    /** (0028,0100) VR=US VM=1 Bits Allocated */
    public static final int BitsAllocated = 0x00280100;

    /** (0028,0101) VR=US VM=1 Bits Stored */
    public static final int BitsStored = 0x00280101;

    /** (0028,0102) VR=US VM=1 High Bit */
    public static final int HighBit = 0x00280102;

    /** (0028,0103) VR=US VM=1 Pixel Representation */
    public static final int PixelRepresentation = 0x00280103;

    /** (0028,0104) VR=US or SS VM=1 Smallest Valid Pixel Value (retired) */
    public static final int SmallestValidPixelValue = 0x00280104;

    /** (0028,0105) VR=US or SS VM=1 Largest Valid Pixel Value (retired) */
    public static final int LargestValidPixelValue = 0x00280105;

    /** (0028,0106) VR=US or SS VM=1 Smallest Image Pixel Value */
    public static final int SmallestImagePixelValue = 0x00280106;

    /** (0028,0107) VR=US or SS VM=1 Largest Image Pixel Value */
    public static final int LargestImagePixelValue = 0x00280107;

    /** (0028,0108) VR=US or SS VM=1 Smallest Pixel Value in Series */
    public static final int SmallestPixelValueInSeries = 0x00280108;

    /** (0028,0109) VR=US or SS VM=1 Largest Pixel Value in Series */
    public static final int LargestPixelValueInSeries = 0x00280109;

    /** (0028,0110) VR=US or SS VM=1 Smallest Image Pixel Value in Plane (retired) */
    public static final int SmallestImagePixelValueInPlane = 0x00280110;

    /** (0028,0111) VR=US or SS VM=1 Largest Image Pixel Value in Plane (retired) */
    public static final int LargestImagePixelValueInPlane = 0x00280111;

    /** (0028,0120) VR=US or SS VM=1 Pixel Padding Value */
    public static final int PixelPaddingValue = 0x00280120;

    /** (0028,0121) VR=US or SS VM=1 Pixel Padding Range Limit */
    public static final int PixelPaddingRangeLimit = 0x00280121;

    /** (0028,0122) VR=FL VM=1 Float Pixel Padding Value */
    public static final int FloatPixelPaddingValue = 0x00280122;

    /** (0028,0123) VR=FD VM=1 Double Float Pixel Padding Value */
    public static final int DoubleFloatPixelPaddingValue = 0x00280123;

    /** (0028,0124) VR=FL VM=1 Float Pixel Padding Range Limit */
    public static final int FloatPixelPaddingRangeLimit = 0x00280124;

    /** (0028,0125) VR=FD VM=1 Double Float Pixel Padding Range Limit */
    public static final int DoubleFloatPixelPaddingRangeLimit = 0x00280125;

    /** (0028,0200) VR=US VM=1 Image Location (retired) */
    public static final int ImageLocation = 0x00280200;

    /** (0028,0300) VR=CS VM=1 Quality Control Image */
    public static final int QualityControlImage = 0x00280300;

    /** (0028,0301) VR=CS VM=1 Burned In Annotation */
    public static final int BurnedInAnnotation = 0x00280301;

    /** (0028,0302) VR=CS VM=1 Recognizable Visual Features */
    public static final int RecognizableVisualFeatures = 0x00280302;

    /** (0028,0303) VR=CS VM=1 Longitudinal Temporal Information Modified */
    public static final int LongitudinalTemporalInformationModified = 0x00280303;

    /** (0028,0304) VR=UI VM=1 Referenced Color Palette Instance UID */
    public static final int ReferencedColorPaletteInstanceUID = 0x00280304;

    /** (0028,0400) VR=LO VM=1 Transform Label (retired) */
    public static final int TransformLabel = 0x00280400;

    /** (0028,0401) VR=LO VM=1 Transform Version Number (retired) */
    public static final int TransformVersionNumber = 0x00280401;

    /** (0028,0402) VR=US VM=1 Number of Transform Steps (retired) */
    public static final int NumberOfTransformSteps = 0x00280402;

    /** (0028,0403) VR=LO VM=1-n Sequence of Compressed Data (retired) */
    public static final int SequenceOfCompressedData = 0x00280403;

    /** (0028,0404) VR=AT VM=1-n Details of Coefficients (retired) */
    public static final int DetailsOfCoefficients = 0x00280404;

    /** (0028,04x0) VR=US VM=1 Rows For Nth Order Coefficients (retired) */
    public static final int RowsForNthOrderCoefficients = 0x00280400;

    /** (0028,04x1) VR=US VM=1 Columns For Nth Order Coefficients (retired) */
    public static final int ColumnsForNthOrderCoefficients = 0x00280401;

    /** (0028,04x2) VR=LO VM=1-n Coefficient Coding (retired) */
    public static final int CoefficientCoding = 0x00280402;

    /** (0028,04x3) VR=AT VM=1-n Coefficient Coding Pointers (retired) */
    public static final int CoefficientCodingPointers = 0x00280403;

    /** (0028,0700) VR=LO VM=1 DCT Label (retired) */
    public static final int DCTLabel = 0x00280700;

    /** (0028,0701) VR=CS VM=1-n Data Block Description (retired) */
    public static final int DataBlockDescription = 0x00280701;

    /** (0028,0702) VR=AT VM=1-n Data Block (retired) */
    public static final int DataBlock = 0x00280702;

    /** (0028,0710) VR=US VM=1 Normalization Factor Format (retired) */
    public static final int NormalizationFactorFormat = 0x00280710;

    /** (0028,0720) VR=US VM=1 Zonal Map Number Format (retired) */
    public static final int ZonalMapNumberFormat = 0x00280720;

    /** (0028,0721) VR=AT VM=1-n Zonal Map Location (retired) */
    public static final int ZonalMapLocation = 0x00280721;

    /** (0028,0722) VR=US VM=1 Zonal Map Format (retired) */
    public static final int ZonalMapFormat = 0x00280722;

    /** (0028,0730) VR=US VM=1 Adaptive Map Format (retired) */
    public static final int AdaptiveMapFormat = 0x00280730;

    /** (0028,0740) VR=US VM=1 Code Number Format (retired) */
    public static final int CodeNumberFormat = 0x00280740;

    /** (0028,08x0) VR=CS VM=1-n Code Label (retired) */
    public static final int CodeLabel = 0x00280800;

    /** (0028,08x2) VR=US VM=1 Number of Tables (retired) */
    public static final int NumberOfTables = 0x00280802;

    /** (0028,08x3) VR=AT VM=1-n Code Table Location (retired) */
    public static final int CodeTableLocation = 0x00280803;

    /** (0028,08x4) VR=US VM=1 Bits For Code Word (retired) */
    public static final int BitsForCodeWord = 0x00280804;

    /** (0028,08x8) VR=AT VM=1-n Image Data Location (retired) */
    public static final int ImageDataLocation = 0x00280808;

    /** (0028,0A02) VR=CS VM=1 Pixel Spacing Calibration Type */
    public static final int PixelSpacingCalibrationType = 0x00280A02;

    /** (0028,0A04) VR=LO VM=1 Pixel Spacing Calibration Description */
    public static final int PixelSpacingCalibrationDescription = 0x00280A04;

    /** (0028,1040) VR=CS VM=1 Pixel Intensity Relationship */
    public static final int PixelIntensityRelationship = 0x00281040;

    /** (0028,1041) VR=SS VM=1 Pixel Intensity Relationship Sign */
    public static final int PixelIntensityRelationshipSign = 0x00281041;

    /** (0028,1050) VR=DS VM=1-n Window Center */
    public static final int WindowCenter = 0x00281050;

    /** (0028,1051) VR=DS VM=1-n Window Width */
    public static final int WindowWidth = 0x00281051;

    /** (0028,1052) VR=DS VM=1 Rescale Intercept */
    public static final int RescaleIntercept = 0x00281052;

    /** (0028,1053) VR=DS VM=1 Rescale Slope */
    public static final int RescaleSlope = 0x00281053;

    /** (0028,1054) VR=LO VM=1 Rescale Type */
    public static final int RescaleType = 0x00281054;

    /** (0028,1055) VR=LO VM=1-n Window Center & Width Explanation */
    public static final int WindowCenterWidthExplanation = 0x00281055;

    /** (0028,1056) VR=CS VM=1 VOI LUT Function */
    public static final int VOILUTFunction = 0x00281056;

    /** (0028,1080) VR=CS VM=1 Gray Scale (retired) */
    public static final int GrayScale = 0x00281080;

    /** (0028,1090) VR=CS VM=1 Recommended Viewing Mode */
    public static final int RecommendedViewingMode = 0x00281090;

    /** (0028,1100) VR=US or SS VM=3 Gray Lookup Table Descriptor (retired) */
    public static final int GrayLookupTableDescriptor = 0x00281100;

    /** (0028,1101) VR=US or SS VM=3 Red Palette Color Lookup Table Descriptor */
    public static final int RedPaletteColorLookupTableDescriptor = 0x00281101;

    /** (0028,1102) VR=US or SS VM=3 Green Palette Color Lookup Table Descriptor */
    public static final int GreenPaletteColorLookupTableDescriptor = 0x00281102;

    /** (0028,1103) VR=US or SS VM=3 Blue Palette Color Lookup Table Descriptor */
    public static final int BluePaletteColorLookupTableDescriptor = 0x00281103;

    /** (0028,1104) VR=US VM=3 Alpha Palette Color Lookup Table Descriptor */
    public static final int AlphaPaletteColorLookupTableDescriptor = 0x00281104;

    /** (0028,1111) VR=US or SS VM=4 Large Red Palette Color Lookup Table Descriptor (retired) */
    public static final int LargeRedPaletteColorLookupTableDescriptor = 0x00281111;

    /** (0028,1112) VR=US or SS VM=4 Large Green Palette Color Lookup Table Descriptor (retired) */
    public static final int LargeGreenPaletteColorLookupTableDescriptor = 0x00281112;

    /** (0028,1113) VR=US or SS VM=4 Large Blue Palette Color Lookup Table Descriptor (retired) */
    public static final int LargeBluePaletteColorLookupTableDescriptor = 0x00281113;

    /** (0028,1199) VR=UI VM=1 Palette Color Lookup Table UID */
    public static final int PaletteColorLookupTableUID = 0x00281199;

    /** (0028,1200) VR=US or SS or OW VM=1-n or 1 Gray Lookup Table Data (retired) */
    public static final int GrayLookupTableData = 0x00281200;

    /** (0028,1201) VR=OW VM=1 Red Palette Color Lookup Table Data */
    public static final int RedPaletteColorLookupTableData = 0x00281201;

    /** (0028,1202) VR=OW VM=1 Green Palette Color Lookup Table Data */
    public static final int GreenPaletteColorLookupTableData = 0x00281202;

    /** (0028,1203) VR=OW VM=1 Blue Palette Color Lookup Table Data */
    public static final int BluePaletteColorLookupTableData = 0x00281203;

    /** (0028,1204) VR=OW VM=1 Alpha Palette Color Lookup Table Data */
    public static final int AlphaPaletteColorLookupTableData = 0x00281204;

    /** (0028,1211) VR=OW VM=1 Large Red Palette Color Lookup Table Data (retired) */
    public static final int LargeRedPaletteColorLookupTableData = 0x00281211;

    /** (0028,1212) VR=OW VM=1 Large Green Palette Color Lookup Table Data (retired) */
    public static final int LargeGreenPaletteColorLookupTableData = 0x00281212;

    /** (0028,1213) VR=OW VM=1 Large Blue Palette Color Lookup Table Data (retired) */
    public static final int LargeBluePaletteColorLookupTableData = 0x00281213;

    /** (0028,1214) VR=UI VM=1 Large Palette Color Lookup Table UID (retired) */
    public static final int LargePaletteColorLookupTableUID = 0x00281214;

    /** (0028,1221) VR=OW VM=1 Segmented Red Palette Color Lookup Table Data */
    public static final int SegmentedRedPaletteColorLookupTableData = 0x00281221;

    /** (0028,1222) VR=OW VM=1 Segmented Green Palette Color Lookup Table Data */
    public static final int SegmentedGreenPaletteColorLookupTableData = 0x00281222;

    /** (0028,1223) VR=OW VM=1 Segmented Blue Palette Color Lookup Table Data */
    public static final int SegmentedBluePaletteColorLookupTableData = 0x00281223;

    /** (0028,1224) VR=OW VM=1 Segmented Alpha Palette Color Lookup Table Data */
    public static final int SegmentedAlphaPaletteColorLookupTableData = 0x00281224;

    /** (0028,1230) VR=SQ VM=1 Stored Value Color Range Sequence */
    public static final int StoredValueColorRangeSequence = 0x00281230;

    /** (0028,1231) VR=FD VM=1 Minimum Stored Value Mapped */
    public static final int MinimumStoredValueMapped = 0x00281231;

    /** (0028,1232) VR=FD VM=1 Maximum Stored Value Mapped */
    public static final int MaximumStoredValueMapped = 0x00281232;

    /** (0028,1300) VR=CS VM=1 Breast Implant Present */
    public static final int BreastImplantPresent = 0x00281300;

    /** (0028,1350) VR=CS VM=1 Partial View */
    public static final int PartialView = 0x00281350;

    /** (0028,1351) VR=ST VM=1 Partial View Description */
    public static final int PartialViewDescription = 0x00281351;

    /** (0028,1352) VR=SQ VM=1 Partial View Code Sequence */
    public static final int PartialViewCodeSequence = 0x00281352;

    /** (0028,135A) VR=CS VM=1 Spatial Locations Preserved */
    public static final int SpatialLocationsPreserved = 0x0028135A;

    /** (0028,1401) VR=SQ VM=1 Data Frame Assignment Sequence */
    public static final int DataFrameAssignmentSequence = 0x00281401;

    /** (0028,1402) VR=CS VM=1 Data Path Assignment */
    public static final int DataPathAssignment = 0x00281402;

    /** (0028,1403) VR=US VM=1 Bits Mapped to Color Lookup Table */
    public static final int BitsMappedToColorLookupTable = 0x00281403;

    /** (0028,1404) VR=SQ VM=1 Blending LUT 1 Sequence */
    public static final int BlendingLUT1Sequence = 0x00281404;

    /** (0028,1405) VR=CS VM=1 Blending LUT 1 Transfer Function */
    public static final int BlendingLUT1TransferFunction = 0x00281405;

    /** (0028,1406) VR=FD VM=1 Blending Weight Constant */
    public static final int BlendingWeightConstant = 0x00281406;

    /** (0028,1407) VR=US VM=3 Blending Lookup Table Descriptor */
    public static final int BlendingLookupTableDescriptor = 0x00281407;

    /** (0028,1408) VR=OW VM=1 Blending Lookup Table Data */
    public static final int BlendingLookupTableData = 0x00281408;

    /** (0028,140B) VR=SQ VM=1 Enhanced Palette Color Lookup Table Sequence */
    public static final int EnhancedPaletteColorLookupTableSequence = 0x0028140B;

    /** (0028,140C) VR=SQ VM=1 Blending LUT 2 Sequence */
    public static final int BlendingLUT2Sequence = 0x0028140C;

    /** (0028,140D) VR=CS VM=1 Blending LUT 2 Transfer Function */
    public static final int BlendingLUT2TransferFunction = 0x0028140D;

    /** (0028,140E) VR=CS VM=1 Data Path ID */
    public static final int DataPathID = 0x0028140E;

    /** (0028,140F) VR=CS VM=1 RGB LUT Transfer Function */
    public static final int RGBLUTTransferFunction = 0x0028140F;

    /** (0028,1410) VR=CS VM=1 Alpha LUT Transfer Function */
    public static final int AlphaLUTTransferFunction = 0x00281410;

    /** (0028,2000) VR=OB VM=1 ICC Profile */
    public static final int ICCProfile = 0x00282000;

    /** (0028,2002) VR=CS VM=1 Color Space */
    public static final int ColorSpace = 0x00282002;

    /** (0028,2110) VR=CS VM=1 Lossy Image Compression */
    public static final int LossyImageCompression = 0x00282110;

    /** (0028,2112) VR=DS VM=1-n Lossy Image Compression Ratio */
    public static final int LossyImageCompressionRatio = 0x00282112;

    /** (0028,2114) VR=CS VM=1-n Lossy Image Compression Method */
    public static final int LossyImageCompressionMethod = 0x00282114;

    /** (0028,3000) VR=SQ VM=1 Modality LUT Sequence */
    public static final int ModalityLUTSequence = 0x00283000;

    /** (0028,3002) VR=US or SS VM=3 LUT Descriptor */
    public static final int LUTDescriptor = 0x00283002;

    /** (0028,3003) VR=LO VM=1 LUT Explanation */
    public static final int LUTExplanation = 0x00283003;

    /** (0028,3004) VR=LO VM=1 Modality LUT Type */
    public static final int ModalityLUTType = 0x00283004;

    /** (0028,3006) VR=US or OW VM=1-n or 1 LUT Data */
    public static final int LUTData = 0x00283006;

    /** (0028,3010) VR=SQ VM=1 VOI LUT Sequence */
    public static final int VOILUTSequence = 0x00283010;

    /** (0028,3110) VR=SQ VM=1 Softcopy VOI LUT Sequence */
    public static final int SoftcopyVOILUTSequence = 0x00283110;

    /** (0028,4000) VR=LT VM=1 Image Presentation Comments (retired) */
    public static final int ImagePresentationComments = 0x00284000;

    /** (0028,5000) VR=SQ VM=1 Bi-Plane Acquisition Sequence (retired) */
    public static final int BiPlaneAcquisitionSequence = 0x00285000;

    /** (0028,6010) VR=US VM=1 Representative Frame Number */
    public static final int RepresentativeFrameNumber = 0x00286010;

    /** (0028,6020) VR=US VM=1-n Frame Numbers of Interest (FOI) */
    public static final int FrameNumbersOfInterest = 0x00286020;

    /** (0028,6022) VR=LO VM=1-n Frame of Interest Description */
    public static final int FrameOfInterestDescription = 0x00286022;

    /** (0028,6023) VR=CS VM=1-n Frame of Interest Type */
    public static final int FrameOfInterestType = 0x00286023;

    /** (0028,6030) VR=US VM=1-n Mask Pointer(s) (retired) */
    public static final int MaskPointers = 0x00286030;

    /** (0028,6040) VR=US VM=1-n R Wave Pointer */
    public static final int RWavePointer = 0x00286040;

    /** (0028,6100) VR=SQ VM=1 Mask Subtraction Sequence */
    public static final int MaskSubtractionSequence = 0x00286100;

    /** (0028,6101) VR=CS VM=1 Mask Operation */
    public static final int MaskOperation = 0x00286101;

    /** (0028,6102) VR=US VM=2-2n Applicable Frame Range */
    public static final int ApplicableFrameRange = 0x00286102;

    /** (0028,6110) VR=US VM=1-n Mask Frame Numbers */
    public static final int MaskFrameNumbers = 0x00286110;

    /** (0028,6112) VR=US VM=1 Contrast Frame Averaging */
    public static final int ContrastFrameAveraging = 0x00286112;

    /** (0028,6114) VR=FL VM=2 Mask Sub-pixel Shift */
    public static final int MaskSubPixelShift = 0x00286114;

    /** (0028,6120) VR=SS VM=1 TID Offset */
    public static final int TIDOffset = 0x00286120;

    /** (0028,6190) VR=ST VM=1 Mask Operation Explanation */
    public static final int MaskOperationExplanation = 0x00286190;

    /** (0028,7000) VR=SQ VM=1 Equipment Administrator Sequence */
    public static final int EquipmentAdministratorSequence = 0x00287000;

    /** (0028,7001) VR=US VM=1 Number of Display Subsystems */
    public static final int NumberOfDisplaySubsystems = 0x00287001;

    /** (0028,7002) VR=US VM=1 Current Configuration ID */
    public static final int CurrentConfigurationID = 0x00287002;

    /** (0028,7003) VR=US VM=1 Display Subsystem ID */
    public static final int DisplaySubsystemID = 0x00287003;

    /** (0028,7004) VR=SH VM=1 Display Subsystem Name */
    public static final int DisplaySubsystemName = 0x00287004;

    /** (0028,7005) VR=LO VM=1 Display Subsystem Description */
    public static final int DisplaySubsystemDescription = 0x00287005;

    /** (0028,7006) VR=CS VM=1 System Status */
    public static final int SystemStatus = 0x00287006;

    /** (0028,7007) VR=LO VM=1 System Status Comment */
    public static final int SystemStatusComment = 0x00287007;

    /** (0028,7008) VR=SQ VM=1 Target Luminance Characteristics Sequence */
    public static final int TargetLuminanceCharacteristicsSequence = 0x00287008;

    /** (0028,7009) VR=US VM=1 Luminance Characteristics ID */
    public static final int LuminanceCharacteristicsID = 0x00287009;

    /** (0028,700A) VR=SQ VM=1 Display Subsystem Configuration Sequence */
    public static final int DisplaySubsystemConfigurationSequence = 0x0028700A;

    /** (0028,700B) VR=US VM=1 Configuration ID */
    public static final int ConfigurationID = 0x0028700B;

    /** (0028,700C) VR=SH VM=1 Configuration Name */
    public static final int ConfigurationName = 0x0028700C;

    /** (0028,700D) VR=LO VM=1 Configuration Description */
    public static final int ConfigurationDescription = 0x0028700D;

    /** (0028,700E) VR=US VM=1 Referenced Target Luminance Characteristics ID */
    public static final int ReferencedTargetLuminanceCharacteristicsID = 0x0028700E;

    /** (0028,700F) VR=SQ VM=1 QA Results Sequence */
    public static final int QAResultsSequence = 0x0028700F;

    /** (0028,7010) VR=SQ VM=1 Display Subsystem QA Results Sequence */
    public static final int DisplaySubsystemQAResultsSequence = 0x00287010;

    /** (0028,7011) VR=SQ VM=1 Configuration QA Results Sequence */
    public static final int ConfigurationQAResultsSequence = 0x00287011;

    /** (0028,7012) VR=SQ VM=1 Measurement Equipment Sequence */
    public static final int MeasurementEquipmentSequence = 0x00287012;

    /** (0028,7013) VR=CS VM=1-n Measurement Functions */
    public static final int MeasurementFunctions = 0x00287013;

    /** (0028,7014) VR=CS VM=1 Measurement Equipment Type */
    public static final int MeasurementEquipmentType = 0x00287014;

    /** (0028,7015) VR=SQ VM=1 Visual Evaluation Result Sequence */
    public static final int VisualEvaluationResultSequence = 0x00287015;

    /** (0028,7016) VR=SQ VM=1 Display Calibration Result Sequence */
    public static final int DisplayCalibrationResultSequence = 0x00287016;

    /** (0028,7017) VR=US VM=1 DDL Value */
    public static final int DDLValue = 0x00287017;

    /** (0028,7018) VR=FL VM=2 CIExy White Point */
    public static final int CIExyWhitePoint = 0x00287018;

    /** (0028,7019) VR=CS VM=1 Display Function Type */
    public static final int DisplayFunctionType = 0x00287019;

    /** (0028,701A) VR=FL VM=1 Gamma Value */
    public static final int GammaValue = 0x0028701A;

    /** (0028,701B) VR=US VM=1 Number of Luminance Points */
    public static final int NumberOfLuminancePoints = 0x0028701B;

    /** (0028,701C) VR=SQ VM=1 Luminance Response Sequence */
    public static final int LuminanceResponseSequence = 0x0028701C;

    /** (0028,701D) VR=FL VM=1 Target Minimum Luminance */
    public static final int TargetMinimumLuminance = 0x0028701D;

    /** (0028,701E) VR=FL VM=1 Target Maximum Luminance */
    public static final int TargetMaximumLuminance = 0x0028701E;

    /** (0028,701F) VR=FL VM=1 Luminance Value */
    public static final int LuminanceValue = 0x0028701F;

    /** (0028,7020) VR=LO VM=1 Luminance Response Description */
    public static final int LuminanceResponseDescription = 0x00287020;

    /** (0028,7021) VR=CS VM=1 White Point Flag */
    public static final int WhitePointFlag = 0x00287021;

    /** (0028,7022) VR=SQ VM=1 Display Device Type Code Sequence */
    public static final int DisplayDeviceTypeCodeSequence = 0x00287022;

    /** (0028,7023) VR=SQ VM=1 Display Subsystem Sequence */
    public static final int DisplaySubsystemSequence = 0x00287023;

    /** (0028,7024) VR=SQ VM=1 Luminance Result Sequence */
    public static final int LuminanceResultSequence = 0x00287024;

    /** (0028,7025) VR=CS VM=1 Ambient Light Value Source */
    public static final int AmbientLightValueSource = 0x00287025;

    /** (0028,7026) VR=CS VM=1-n Measured Characteristics */
    public static final int MeasuredCharacteristics = 0x00287026;

    /** (0028,7027) VR=SQ VM=1 Luminance Uniformity Result Sequence */
    public static final int LuminanceUniformityResultSequence = 0x00287027;

    /** (0028,7028) VR=SQ VM=1 Visual Evaluation Test Sequence */
    public static final int VisualEvaluationTestSequence = 0x00287028;

    /** (0028,7029) VR=CS VM=1 Test Result */
    public static final int TestResult = 0x00287029;

    /** (0028,702A) VR=LO VM=1 Test Result Comment */
    public static final int TestResultComment = 0x0028702A;

    /** (0028,702B) VR=CS VM=1 Test Image Validation */
    public static final int TestImageValidation = 0x0028702B;

    /** (0028,702C) VR=SQ VM=1 Test Pattern Code Sequence */
    public static final int TestPatternCodeSequence = 0x0028702C;

    /** (0028,702D) VR=SQ VM=1 Measurement Pattern Code Sequence */
    public static final int MeasurementPatternCodeSequence = 0x0028702D;

    /** (0028,702E) VR=SQ VM=1 Visual Evaluation Method Code Sequence */
    public static final int VisualEvaluationMethodCodeSequence = 0x0028702E;

    /** (0028,7FE0) VR=UR VM=1 Pixel Data Provider URL */
    public static final int PixelDataProviderURL = 0x00287FE0;

    /** (0028,9001) VR=UL VM=1 Data Point Rows */
    public static final int DataPointRows = 0x00289001;

    /** (0028,9002) VR=UL VM=1 Data Point Columns */
    public static final int DataPointColumns = 0x00289002;

    /** (0028,9003) VR=CS VM=1 Signal Domain Columns */
    public static final int SignalDomainColumns = 0x00289003;

    /** (0028,9099) VR=US VM=1 Largest Monochrome Pixel Value (retired) */
    public static final int LargestMonochromePixelValue = 0x00289099;

    /** (0028,9108) VR=CS VM=1 Data Representation */
    public static final int DataRepresentation = 0x00289108;

    /** (0028,9110) VR=SQ VM=1 Pixel Measures Sequence */
    public static final int PixelMeasuresSequence = 0x00289110;

    /** (0028,9132) VR=SQ VM=1 Frame VOI LUT Sequence */
    public static final int FrameVOILUTSequence = 0x00289132;

    /** (0028,9145) VR=SQ VM=1 Pixel Value Transformation Sequence */
    public static final int PixelValueTransformationSequence = 0x00289145;

    /** (0028,9235) VR=CS VM=1 Signal Domain Rows */
    public static final int SignalDomainRows = 0x00289235;

    /** (0028,9411) VR=FL VM=1 Display Filter Percentage */
    public static final int DisplayFilterPercentage = 0x00289411;

    /** (0028,9415) VR=SQ VM=1 Frame Pixel Shift Sequence */
    public static final int FramePixelShiftSequence = 0x00289415;

    /** (0028,9416) VR=US VM=1 Subtraction Item ID */
    public static final int SubtractionItemID = 0x00289416;

    /** (0028,9422) VR=SQ VM=1 Pixel Intensity Relationship LUT Sequence */
    public static final int PixelIntensityRelationshipLUTSequence = 0x00289422;

    /** (0028,9443) VR=SQ VM=1 Frame Pixel Data Properties Sequence */
    public static final int FramePixelDataPropertiesSequence = 0x00289443;

    /** (0028,9444) VR=CS VM=1 Geometrical Properties */
    public static final int GeometricalProperties = 0x00289444;

    /** (0028,9445) VR=FL VM=1 Geometric Maximum Distortion */
    public static final int GeometricMaximumDistortion = 0x00289445;

    /** (0028,9446) VR=CS VM=1-n Image Processing Applied */
    public static final int ImageProcessingApplied = 0x00289446;

    /** (0028,9454) VR=CS VM=1 Mask Selection Mode */
    public static final int MaskSelectionMode = 0x00289454;

    /** (0028,9474) VR=CS VM=1 LUT Function */
    public static final int LUTFunction = 0x00289474;

    /** (0028,9478) VR=FL VM=1 Mask Visibility Percentage */
    public static final int MaskVisibilityPercentage = 0x00289478;

    /** (0028,9501) VR=SQ VM=1 Pixel Shift Sequence */
    public static final int PixelShiftSequence = 0x00289501;

    /** (0028,9502) VR=SQ VM=1 Region Pixel Shift Sequence */
    public static final int RegionPixelShiftSequence = 0x00289502;

    /** (0028,9503) VR=SS VM=2-2n Vertices of the Region */
    public static final int VerticesOfTheRegion = 0x00289503;

    /** (0028,9505) VR=SQ VM=1 Multi-frame Presentation Sequence */
    public static final int MultiFramePresentationSequence = 0x00289505;

    /** (0028,9506) VR=US VM=2-2n Pixel Shift Frame Range */
    public static final int PixelShiftFrameRange = 0x00289506;

    /** (0028,9507) VR=US VM=2-2n LUT Frame Range */
    public static final int LUTFrameRange = 0x00289507;

    /** (0028,9520) VR=DS VM=16 Image to Equipment Mapping Matrix */
    public static final int ImageToEquipmentMappingMatrix = 0x00289520;

    /** (0028,9537) VR=CS VM=1 Equipment Coordinate System Identification */
    public static final int EquipmentCoordinateSystemIdentification = 0x00289537;

    /** (0032,000A) VR=CS VM=1 Study Status ID (retired) */
    public static final int StudyStatusID = 0x0032000A;

    /** (0032,000C) VR=CS VM=1 Study Priority ID (retired) */
    public static final int StudyPriorityID = 0x0032000C;

    /** (0032,0012) VR=LO VM=1 Study ID Issuer (retired) */
    public static final int StudyIDIssuer = 0x00320012;

    /** (0032,0032) VR=DA VM=1 Study Verified Date (retired) */
    public static final int StudyVerifiedDate = 0x00320032;

    /** (0032,0033) VR=TM VM=1 Study Verified Time (retired) */
    public static final int StudyVerifiedTime = 0x00320033;

    /** (0032,0034) VR=DA VM=1 Study Read Date (retired) */
    public static final int StudyReadDate = 0x00320034;

    /** (0032,0035) VR=TM VM=1 Study Read Time (retired) */
    public static final int StudyReadTime = 0x00320035;

    /** (0032,1000) VR=DA VM=1 Scheduled Study Start Date (retired) */
    public static final int ScheduledStudyStartDate = 0x00321000;

    /** (0032,1001) VR=TM VM=1 Scheduled Study Start Time (retired) */
    public static final int ScheduledStudyStartTime = 0x00321001;

    /** (0032,1010) VR=DA VM=1 Scheduled Study Stop Date (retired) */
    public static final int ScheduledStudyStopDate = 0x00321010;

    /** (0032,1011) VR=TM VM=1 Scheduled Study Stop Time (retired) */
    public static final int ScheduledStudyStopTime = 0x00321011;

    /** (0032,1020) VR=LO VM=1 Scheduled Study Location (retired) */
    public static final int ScheduledStudyLocation = 0x00321020;

    /** (0032,1021) VR=AE VM=1-n Scheduled Study Location AE Title (retired) */
    public static final int ScheduledStudyLocationAETitle = 0x00321021;

    /** (0032,1030) VR=LO VM=1 Reason for Study (retired) */
    public static final int ReasonForStudy = 0x00321030;

    /** (0032,1031) VR=SQ VM=1 Requesting Physician Identification Sequence */
    public static final int RequestingPhysicianIdentificationSequence = 0x00321031;

    /** (0032,1032) VR=PN VM=1 Requesting Physician */
    public static final int RequestingPhysician = 0x00321032;

    /** (0032,1033) VR=LO VM=1 Requesting Service */
    public static final int RequestingService = 0x00321033;

    /** (0032,1034) VR=SQ VM=1 Requesting Service Code Sequence */
    public static final int RequestingServiceCodeSequence = 0x00321034;

    /** (0032,1040) VR=DA VM=1 Study Arrival Date (retired) */
    public static final int StudyArrivalDate = 0x00321040;

    /** (0032,1041) VR=TM VM=1 Study Arrival Time (retired) */
    public static final int StudyArrivalTime = 0x00321041;

    /** (0032,1050) VR=DA VM=1 Study Completion Date (retired) */
    public static final int StudyCompletionDate = 0x00321050;

    /** (0032,1051) VR=TM VM=1 Study Completion Time (retired) */
    public static final int StudyCompletionTime = 0x00321051;

    /** (0032,1055) VR=CS VM=1 Study Component Status ID (retired) */
    public static final int StudyComponentStatusID = 0x00321055;

    /** (0032,1060) VR=LO VM=1 Requested Procedure Description */
    public static final int RequestedProcedureDescription = 0x00321060;

    /** (0032,1064) VR=SQ VM=1 Requested Procedure Code Sequence */
    public static final int RequestedProcedureCodeSequence = 0x00321064;

    /** (0032,1066) VR=UT VM=1 Reason for Visit */
    public static final int ReasonForVisit = 0x00321066;

    /** (0032,1067) VR=SQ VM=1 Reason for Visit Code Sequence */
    public static final int ReasonForVisitCodeSequence = 0x00321067;

    /** (0032,1070) VR=LO VM=1 Requested Contrast Agent */
    public static final int RequestedContrastAgent = 0x00321070;

    /** (0032,4000) VR=LT VM=1 Study Comments (retired) */
    public static final int StudyComments = 0x00324000;

    /** (0034,0001) VR=SQ VM=1 Flow Identifier Sequence */
    public static final int FlowIdentifierSequence = 0x00340001;

    /** (0034,0002) VR=OB VM=1 Flow Identifier */
    public static final int FlowIdentifier = 0x00340002;

    /** (0034,0003) VR=UI VM=1 Flow Transfer Syntax UID */
    public static final int FlowTransferSyntaxUID = 0x00340003;

    /** (0034,0004) VR=UL VM=1 Flow RTP Sampling Rate */
    public static final int FlowRTPSamplingRate = 0x00340004;

    /** (0034,0005) VR=OB VM=1 Source Identifier */
    public static final int SourceIdentifier = 0x00340005;

    /** (0034,0007) VR=OB VM=1 Frame Origin Timestamp */
    public static final int FrameOriginTimestamp = 0x00340007;

    /** (0034,0008) VR=CS VM=1 Includes Imaging Subject */
    public static final int IncludesImagingSubject = 0x00340008;

    /** (0034,0009) VR=SQ VM=1 Frame Usefulness Group Sequence */
    public static final int FrameUsefulnessGroupSequence = 0x00340009;

    /** (0034,000A) VR=SQ VM=1 Real-Time Bulk Data Flow Sequence */
    public static final int RealTimeBulkDataFlowSequence = 0x0034000A;

    /** (0034,000B) VR=SQ VM=1 Camera Position Group Sequence */
    public static final int CameraPositionGroupSequence = 0x0034000B;

    /** (0034,000C) VR=CS VM=1 Includes Information */
    public static final int IncludesInformation = 0x0034000C;

    /** (0034,000D) VR=SQ VM=1 Time of Frame Group Sequence */
    public static final int TimeOfFrameGroupSequence = 0x0034000D;

    /** (0038,0004) VR=SQ VM=1 Referenced Patient Alias Sequence */
    public static final int ReferencedPatientAliasSequence = 0x00380004;

    /** (0038,0008) VR=CS VM=1 Visit Status ID */
    public static final int VisitStatusID = 0x00380008;

    /** (0038,0010) VR=LO VM=1 Admission ID */
    public static final int AdmissionID = 0x00380010;

    /** (0038,0011) VR=LO VM=1 Issuer of Admission ID (retired) */
    public static final int IssuerOfAdmissionID = 0x00380011;

    /** (0038,0014) VR=SQ VM=1 Issuer of Admission ID Sequence */
    public static final int IssuerOfAdmissionIDSequence = 0x00380014;

    /** (0038,0016) VR=LO VM=1 Route of Admissions */
    public static final int RouteOfAdmissions = 0x00380016;

    /** (0038,001A) VR=DA VM=1 Scheduled Admission Date (retired) */
    public static final int ScheduledAdmissionDate = 0x0038001A;

    /** (0038,001B) VR=TM VM=1 Scheduled Admission Time (retired) */
    public static final int ScheduledAdmissionTime = 0x0038001B;

    /** (0038,001C) VR=DA VM=1 Scheduled Discharge Date (retired) */
    public static final int ScheduledDischargeDate = 0x0038001C;

    /** (0038,001D) VR=TM VM=1 Scheduled Discharge Time (retired) */
    public static final int ScheduledDischargeTime = 0x0038001D;

    /** (0038,001E) VR=LO VM=1 Scheduled Patient Institution Residence (retired) */
    public static final int ScheduledPatientInstitutionResidence = 0x0038001E;

    /** (0038,0020) VR=DA VM=1 Admitting Date */
    public static final int AdmittingDate = 0x00380020;

    /** (0038,0021) VR=TM VM=1 Admitting Time */
    public static final int AdmittingTime = 0x00380021;

    /** (0038,0030) VR=DA VM=1 Discharge Date (retired) */
    public static final int DischargeDate = 0x00380030;

    /** (0038,0032) VR=TM VM=1 Discharge Time (retired) */
    public static final int DischargeTime = 0x00380032;

    /** (0038,0040) VR=LO VM=1 Discharge Diagnosis Description (retired) */
    public static final int DischargeDiagnosisDescription = 0x00380040;

    /** (0038,0044) VR=SQ VM=1 Discharge Diagnosis Code Sequence (retired) */
    public static final int DischargeDiagnosisCodeSequence = 0x00380044;

    /** (0038,0050) VR=LO VM=1 Special Needs */
    public static final int SpecialNeeds = 0x00380050;

    /** (0038,0060) VR=LO VM=1 Service Episode ID */
    public static final int ServiceEpisodeID = 0x00380060;

    /** (0038,0061) VR=LO VM=1 Issuer of Service Episode ID (retired) */
    public static final int IssuerOfServiceEpisodeID = 0x00380061;

    /** (0038,0062) VR=LO VM=1 Service Episode Description */
    public static final int ServiceEpisodeDescription = 0x00380062;

    /** (0038,0064) VR=SQ VM=1 Issuer of Service Episode ID Sequence */
    public static final int IssuerOfServiceEpisodeIDSequence = 0x00380064;

    /** (0038,0100) VR=SQ VM=1 Pertinent Documents Sequence */
    public static final int PertinentDocumentsSequence = 0x00380100;

    /** (0038,0101) VR=SQ VM=1 Pertinent Resources Sequence */
    public static final int PertinentResourcesSequence = 0x00380101;

    /** (0038,0102) VR=LO VM=1 Resource Description */
    public static final int ResourceDescription = 0x00380102;

    /** (0038,0300) VR=LO VM=1 Current Patient Location */
    public static final int CurrentPatientLocation = 0x00380300;

    /** (0038,0400) VR=LO VM=1 Patient's Institution Residence */
    public static final int PatientInstitutionResidence = 0x00380400;

    /** (0038,0500) VR=LO VM=1 Patient State */
    public static final int PatientState = 0x00380500;

    /** (0038,0502) VR=SQ VM=1 Patient Clinical Trial Participation Sequence */
    public static final int PatientClinicalTrialParticipationSequence = 0x00380502;

    /** (0038,4000) VR=LT VM=1 Visit Comments */
    public static final int VisitComments = 0x00384000;

    /** (003A,0004) VR=CS VM=1 Waveform Originality */
    public static final int WaveformOriginality = 0x003A0004;

    /** (003A,0005) VR=US VM=1 Number of Waveform Channels */
    public static final int NumberOfWaveformChannels = 0x003A0005;

    /** (003A,0010) VR=UL VM=1 Number of Waveform Samples */
    public static final int NumberOfWaveformSamples = 0x003A0010;

    /** (003A,001A) VR=DS VM=1 Sampling Frequency */
    public static final int SamplingFrequency = 0x003A001A;

    /** (003A,0020) VR=SH VM=1 Multiplex Group Label */
    public static final int MultiplexGroupLabel = 0x003A0020;

    /** (003A,0200) VR=SQ VM=1 Channel Definition Sequence */
    public static final int ChannelDefinitionSequence = 0x003A0200;

    /** (003A,0202) VR=IS VM=1 Waveform Channel Number */
    public static final int WaveformChannelNumber = 0x003A0202;

    /** (003A,0203) VR=SH VM=1 Channel Label */
    public static final int ChannelLabel = 0x003A0203;

    /** (003A,0205) VR=CS VM=1-n Channel Status */
    public static final int ChannelStatus = 0x003A0205;

    /** (003A,0208) VR=SQ VM=1 Channel Source Sequence */
    public static final int ChannelSourceSequence = 0x003A0208;

    /** (003A,0209) VR=SQ VM=1 Channel Source Modifiers Sequence */
    public static final int ChannelSourceModifiersSequence = 0x003A0209;

    /** (003A,020A) VR=SQ VM=1 Source Waveform Sequence */
    public static final int SourceWaveformSequence = 0x003A020A;

    /** (003A,020C) VR=LO VM=1 Channel Derivation Description */
    public static final int ChannelDerivationDescription = 0x003A020C;

    /** (003A,0210) VR=DS VM=1 Channel Sensitivity */
    public static final int ChannelSensitivity = 0x003A0210;

    /** (003A,0211) VR=SQ VM=1 Channel Sensitivity Units Sequence */
    public static final int ChannelSensitivityUnitsSequence = 0x003A0211;

    /** (003A,0212) VR=DS VM=1 Channel Sensitivity Correction Factor */
    public static final int ChannelSensitivityCorrectionFactor = 0x003A0212;

    /** (003A,0213) VR=DS VM=1 Channel Baseline */
    public static final int ChannelBaseline = 0x003A0213;

    /** (003A,0214) VR=DS VM=1 Channel Time Skew */
    public static final int ChannelTimeSkew = 0x003A0214;

    /** (003A,0215) VR=DS VM=1 Channel Sample Skew */
    public static final int ChannelSampleSkew = 0x003A0215;

    /** (003A,0218) VR=DS VM=1 Channel Offset */
    public static final int ChannelOffset = 0x003A0218;

    /** (003A,021A) VR=US VM=1 Waveform Bits Stored */
    public static final int WaveformBitsStored = 0x003A021A;

    /** (003A,0220) VR=DS VM=1 Filter Low Frequency */
    public static final int FilterLowFrequency = 0x003A0220;

    /** (003A,0221) VR=DS VM=1 Filter High Frequency */
    public static final int FilterHighFrequency = 0x003A0221;

    /** (003A,0222) VR=DS VM=1 Notch Filter Frequency */
    public static final int NotchFilterFrequency = 0x003A0222;

    /** (003A,0223) VR=DS VM=1 Notch Filter Bandwidth */
    public static final int NotchFilterBandwidth = 0x003A0223;

    /** (003A,0230) VR=FL VM=1 Waveform Data Display Scale */
    public static final int WaveformDataDisplayScale = 0x003A0230;

    /** (003A,0231) VR=US VM=3 Waveform Display Background CIELab Value */
    public static final int WaveformDisplayBackgroundCIELabValue = 0x003A0231;

    /** (003A,0240) VR=SQ VM=1 Waveform Presentation Group Sequence */
    public static final int WaveformPresentationGroupSequence = 0x003A0240;

    /** (003A,0241) VR=US VM=1 Presentation Group Number */
    public static final int PresentationGroupNumber = 0x003A0241;

    /** (003A,0242) VR=SQ VM=1 Channel Display Sequence */
    public static final int ChannelDisplaySequence = 0x003A0242;

    /** (003A,0244) VR=US VM=3 Channel Recommended Display CIELab Value */
    public static final int ChannelRecommendedDisplayCIELabValue = 0x003A0244;

    /** (003A,0245) VR=FL VM=1 Channel Position */
    public static final int ChannelPosition = 0x003A0245;

    /** (003A,0246) VR=CS VM=1 Display Shading Flag */
    public static final int DisplayShadingFlag = 0x003A0246;

    /** (003A,0247) VR=FL VM=1 Fractional Channel Display Scale */
    public static final int FractionalChannelDisplayScale = 0x003A0247;

    /** (003A,0248) VR=FL VM=1 Absolute Channel Display Scale */
    public static final int AbsoluteChannelDisplayScale = 0x003A0248;

    /** (003A,0300) VR=SQ VM=1 Multiplexed Audio Channels Description Code Sequence */
    public static final int MultiplexedAudioChannelsDescriptionCodeSequence = 0x003A0300;

    /** (003A,0301) VR=IS VM=1 Channel Identification Code */
    public static final int ChannelIdentificationCode = 0x003A0301;

    /** (003A,0302) VR=CS VM=1 Channel Mode */
    public static final int ChannelMode = 0x003A0302;

    /** (003A,0310) VR=UI VM=1 Multiplex Group UID */
    public static final int MultiplexGroupUID = 0x003A0310;

    /** (003A,0311) VR=DS VM=1 Powerline Frequency */
    public static final int PowerlineFrequency = 0x003A0311;

    /** (003A,0312) VR=SQ VM=1 Channel Impedance Sequence */
    public static final int ChannelImpedanceSequence = 0x003A0312;

    /** (003A,0313) VR=DS VM=1 Impedance Value */
    public static final int ImpedanceValue = 0x003A0313;

    /** (003A,0314) VR=DT VM=1 Impedance Measurement DateTime */
    public static final int ImpedanceMeasurementDateTime = 0x003A0314;

    /** (003A,0315) VR=DS VM=1 Impedance Measurement Frequency */
    public static final int ImpedanceMeasurementFrequency = 0x003A0315;

    /** (003A,0316) VR=CS VM=1 Impedance Measurement Current Type */
    public static final int ImpedanceMeasurementCurrentType = 0x003A0316;

    /** (0040,0001) VR=AE VM=1-n Scheduled Station AE Title */
    public static final int ScheduledStationAETitle = 0x00400001;

    /** (0040,0002) VR=DA VM=1 Scheduled Procedure Step Start Date */
    public static final int ScheduledProcedureStepStartDate = 0x00400002;

    /** (0040,0003) VR=TM VM=1 Scheduled Procedure Step Start Time */
    public static final int ScheduledProcedureStepStartTime = 0x00400003;

    /** (0040,0004) VR=DA VM=1 Scheduled Procedure Step End Date */
    public static final int ScheduledProcedureStepEndDate = 0x00400004;

    /** (0040,0005) VR=TM VM=1 Scheduled Procedure Step End Time */
    public static final int ScheduledProcedureStepEndTime = 0x00400005;

    /** (0040,0006) VR=PN VM=1 Scheduled Performing Physician's Name */
    public static final int ScheduledPerformingPhysicianName = 0x00400006;

    /** (0040,0007) VR=LO VM=1 Scheduled Procedure Step Description */
    public static final int ScheduledProcedureStepDescription = 0x00400007;

    /** (0040,0008) VR=SQ VM=1 Scheduled Protocol Code Sequence */
    public static final int ScheduledProtocolCodeSequence = 0x00400008;

    /** (0040,0009) VR=SH VM=1 Scheduled Procedure Step ID */
    public static final int ScheduledProcedureStepID = 0x00400009;

    /** (0040,000A) VR=SQ VM=1 Stage Code Sequence */
    public static final int StageCodeSequence = 0x0040000A;

    /** (0040,000B) VR=SQ VM=1 Scheduled Performing Physician Identification Sequence */
    public static final int ScheduledPerformingPhysicianIdentificationSequence = 0x0040000B;

    /** (0040,0010) VR=SH VM=1-n Scheduled Station Name */
    public static final int ScheduledStationName = 0x00400010;

    /** (0040,0011) VR=SH VM=1 Scheduled Procedure Step Location */
    public static final int ScheduledProcedureStepLocation = 0x00400011;

    /** (0040,0012) VR=LO VM=1 Pre-Medication */
    public static final int PreMedication = 0x00400012;

    /** (0040,0020) VR=CS VM=1 Scheduled Procedure Step Status */
    public static final int ScheduledProcedureStepStatus = 0x00400020;

    /** (0040,0026) VR=SQ VM=1 Order Placer Identifier Sequence */
    public static final int OrderPlacerIdentifierSequence = 0x00400026;

    /** (0040,0027) VR=SQ VM=1 Order Filler Identifier Sequence */
    public static final int OrderFillerIdentifierSequence = 0x00400027;

    /** (0040,0031) VR=UT VM=1 Local Namespace Entity ID */
    public static final int LocalNamespaceEntityID = 0x00400031;

    /** (0040,0032) VR=UT VM=1 Universal Entity ID */
    public static final int UniversalEntityID = 0x00400032;

    /** (0040,0033) VR=CS VM=1 Universal Entity ID Type */
    public static final int UniversalEntityIDType = 0x00400033;

    /** (0040,0035) VR=CS VM=1 Identifier Type Code */
    public static final int IdentifierTypeCode = 0x00400035;

    /** (0040,0036) VR=SQ VM=1 Assigning Facility Sequence */
    public static final int AssigningFacilitySequence = 0x00400036;

    /** (0040,0039) VR=SQ VM=1 Assigning Jurisdiction Code Sequence */
    public static final int AssigningJurisdictionCodeSequence = 0x00400039;

    /** (0040,003A) VR=SQ VM=1 Assigning Agency or Department Code Sequence */
    public static final int AssigningAgencyOrDepartmentCodeSequence = 0x0040003A;

    /** (0040,0100) VR=SQ VM=1 Scheduled Procedure Step Sequence */
    public static final int ScheduledProcedureStepSequence = 0x00400100;

    /** (0040,0220) VR=SQ VM=1 Referenced Non-Image Composite SOP Instance Sequence */
    public static final int ReferencedNonImageCompositeSOPInstanceSequence = 0x00400220;

    /** (0040,0241) VR=AE VM=1 Performed Station AE Title */
    public static final int PerformedStationAETitle = 0x00400241;

    /** (0040,0242) VR=SH VM=1 Performed Station Name */
    public static final int PerformedStationName = 0x00400242;

    /** (0040,0243) VR=SH VM=1 Performed Location */
    public static final int PerformedLocation = 0x00400243;

    /** (0040,0244) VR=DA VM=1 Performed Procedure Step Start Date */
    public static final int PerformedProcedureStepStartDate = 0x00400244;

    /** (0040,0245) VR=TM VM=1 Performed Procedure Step Start Time */
    public static final int PerformedProcedureStepStartTime = 0x00400245;

    /** (0040,0250) VR=DA VM=1 Performed Procedure Step End Date */
    public static final int PerformedProcedureStepEndDate = 0x00400250;

    /** (0040,0251) VR=TM VM=1 Performed Procedure Step End Time */
    public static final int PerformedProcedureStepEndTime = 0x00400251;

    /** (0040,0252) VR=CS VM=1 Performed Procedure Step Status */
    public static final int PerformedProcedureStepStatus = 0x00400252;

    /** (0040,0253) VR=SH VM=1 Performed Procedure Step ID */
    public static final int PerformedProcedureStepID = 0x00400253;

    /** (0040,0254) VR=LO VM=1 Performed Procedure Step Description */
    public static final int PerformedProcedureStepDescription = 0x00400254;

    /** (0040,0255) VR=LO VM=1 Performed Procedure Type Description */
    public static final int PerformedProcedureTypeDescription = 0x00400255;

    /** (0040,0260) VR=SQ VM=1 Performed Protocol Code Sequence */
    public static final int PerformedProtocolCodeSequence = 0x00400260;

    /** (0040,0261) VR=CS VM=1 Performed Protocol Type */
    public static final int PerformedProtocolType = 0x00400261;

    /** (0040,0270) VR=SQ VM=1 Scheduled Step Attributes Sequence */
    public static final int ScheduledStepAttributesSequence = 0x00400270;

    /** (0040,0275) VR=SQ VM=1 Request Attributes Sequence */
    public static final int RequestAttributesSequence = 0x00400275;

    /** (0040,0280) VR=ST VM=1 Comments on the Performed Procedure Step */
    public static final int CommentsOnThePerformedProcedureStep = 0x00400280;

    /** (0040,0281) VR=SQ VM=1 Performed Procedure Step Discontinuation Reason Code Sequence */
    public static final int PerformedProcedureStepDiscontinuationReasonCodeSequence = 0x00400281;

    /** (0040,0293) VR=SQ VM=1 Quantity Sequence */
    public static final int QuantitySequence = 0x00400293;

    /** (0040,0294) VR=DS VM=1 Quantity */
    public static final int Quantity = 0x00400294;

    /** (0040,0295) VR=SQ VM=1 Measuring Units Sequence */
    public static final int MeasuringUnitsSequence = 0x00400295;

    /** (0040,0296) VR=SQ VM=1 Billing Item Sequence */
    public static final int BillingItemSequence = 0x00400296;

    /** (0040,0300) VR=US VM=1 Total Time of Fluoroscopy (retired) */
    public static final int TotalTimeOfFluoroscopy = 0x00400300;

    /** (0040,0301) VR=US VM=1 Total Number of Exposures (retired) */
    public static final int TotalNumberOfExposures = 0x00400301;

    /** (0040,0302) VR=US VM=1 Entrance Dose */
    public static final int EntranceDose = 0x00400302;

    /** (0040,0303) VR=US VM=1-2 Exposed Area */
    public static final int ExposedArea = 0x00400303;

    /** (0040,0306) VR=DS VM=1 Distance Source to Entrance */
    public static final int DistanceSourceToEntrance = 0x00400306;

    /** (0040,0307) VR=DS VM=1 Distance Source to Support (retired) */
    public static final int DistanceSourceToSupport = 0x00400307;

    /** (0040,030E) VR=SQ VM=1 Exposure Dose Sequence (retired) */
    public static final int ExposureDoseSequence = 0x0040030E;

    /** (0040,0310) VR=ST VM=1 Comments on Radiation Dose */
    public static final int CommentsOnRadiationDose = 0x00400310;

    /** (0040,0312) VR=DS VM=1 X-Ray Output */
    public static final int XRayOutput = 0x00400312;

    /** (0040,0314) VR=DS VM=1 Half Value Layer */
    public static final int HalfValueLayer = 0x00400314;

    /** (0040,0316) VR=DS VM=1 Organ Dose */
    public static final int OrganDose = 0x00400316;

    /** (0040,0318) VR=CS VM=1 Organ Exposed */
    public static final int OrganExposed = 0x00400318;

    /** (0040,0320) VR=SQ VM=1 Billing Procedure Step Sequence */
    public static final int BillingProcedureStepSequence = 0x00400320;

    /** (0040,0321) VR=SQ VM=1 Film Consumption Sequence */
    public static final int FilmConsumptionSequence = 0x00400321;

    /** (0040,0324) VR=SQ VM=1 Billing Supplies and Devices Sequence */
    public static final int BillingSuppliesAndDevicesSequence = 0x00400324;

    /** (0040,0330) VR=SQ VM=1 Referenced Procedure Step Sequence (retired) */
    public static final int ReferencedProcedureStepSequence = 0x00400330;

    /** (0040,0340) VR=SQ VM=1 Performed Series Sequence */
    public static final int PerformedSeriesSequence = 0x00400340;

    /** (0040,0400) VR=LT VM=1 Comments on the Scheduled Procedure Step */
    public static final int CommentsOnTheScheduledProcedureStep = 0x00400400;

    /** (0040,0440) VR=SQ VM=1 Protocol Context Sequence */
    public static final int ProtocolContextSequence = 0x00400440;

    /** (0040,0441) VR=SQ VM=1 Content Item Modifier Sequence */
    public static final int ContentItemModifierSequence = 0x00400441;

    /** (0040,0500) VR=SQ VM=1 Scheduled Specimen Sequence */
    public static final int ScheduledSpecimenSequence = 0x00400500;

    /** (0040,050A) VR=LO VM=1 Specimen Accession Number (retired) */
    public static final int SpecimenAccessionNumber = 0x0040050A;

    /** (0040,0512) VR=LO VM=1 Container Identifier */
    public static final int ContainerIdentifier = 0x00400512;

    /** (0040,0513) VR=SQ VM=1 Issuer of the Container Identifier Sequence */
    public static final int IssuerOfTheContainerIdentifierSequence = 0x00400513;

    /** (0040,0515) VR=SQ VM=1 Alternate Container Identifier Sequence */
    public static final int AlternateContainerIdentifierSequence = 0x00400515;

    /** (0040,0518) VR=SQ VM=1 Container Type Code Sequence */
    public static final int ContainerTypeCodeSequence = 0x00400518;

    /** (0040,051A) VR=LO VM=1 Container Description */
    public static final int ContainerDescription = 0x0040051A;

    /** (0040,0520) VR=SQ VM=1 Container Component Sequence */
    public static final int ContainerComponentSequence = 0x00400520;

    /** (0040,0550) VR=SQ VM=1 Specimen Sequence (retired) */
    public static final int SpecimenSequence = 0x00400550;

    /** (0040,0551) VR=LO VM=1 Specimen Identifier */
    public static final int SpecimenIdentifier = 0x00400551;

    /** (0040,0552) VR=SQ VM=1 Specimen Description Sequence (Trial) (retired) */
    public static final int SpecimenDescriptionSequenceTrial = 0x00400552;

    /** (0040,0553) VR=ST VM=1 Specimen Description (Trial) (retired) */
    public static final int SpecimenDescriptionTrial = 0x00400553;

    /** (0040,0554) VR=UI VM=1 Specimen UID */
    public static final int SpecimenUID = 0x00400554;

    /** (0040,0555) VR=SQ VM=1 Acquisition Context Sequence */
    public static final int AcquisitionContextSequence = 0x00400555;

    /** (0040,0556) VR=ST VM=1 Acquisition Context Description */
    public static final int AcquisitionContextDescription = 0x00400556;

    /** (0040,059A) VR=SQ VM=1 Specimen Type Code Sequence */
    public static final int SpecimenTypeCodeSequence = 0x0040059A;

    /** (0040,0560) VR=SQ VM=1 Specimen Description Sequence */
    public static final int SpecimenDescriptionSequence = 0x00400560;

    /** (0040,0562) VR=SQ VM=1 Issuer of the Specimen Identifier Sequence */
    public static final int IssuerOfTheSpecimenIdentifierSequence = 0x00400562;

    /** (0040,0600) VR=LO VM=1 Specimen Short Description */
    public static final int SpecimenShortDescription = 0x00400600;

    /** (0040,0602) VR=UT VM=1 Specimen Detailed Description */
    public static final int SpecimenDetailedDescription = 0x00400602;

    /** (0040,0610) VR=SQ VM=1 Specimen Preparation Sequence */
    public static final int SpecimenPreparationSequence = 0x00400610;

    /** (0040,0612) VR=SQ VM=1 Specimen Preparation Step Content Item Sequence */
    public static final int SpecimenPreparationStepContentItemSequence = 0x00400612;

    /** (0040,0620) VR=SQ VM=1 Specimen Localization Content Item Sequence */
    public static final int SpecimenLocalizationContentItemSequence = 0x00400620;

    /** (0040,06FA) VR=LO VM=1 Slide Identifier (retired) */
    public static final int SlideIdentifier = 0x004006FA;

    /** (0040,0710) VR=SQ VM=1 Whole Slide Microscopy Image Frame Type Sequence */
    public static final int WholeSlideMicroscopyImageFrameTypeSequence = 0x00400710;

    /** (0040,071A) VR=SQ VM=1 Image Center Point Coordinates Sequence */
    public static final int ImageCenterPointCoordinatesSequence = 0x0040071A;

    /** (0040,072A) VR=DS VM=1 X Offset in Slide Coordinate System */
    public static final int XOffsetInSlideCoordinateSystem = 0x0040072A;

    /** (0040,073A) VR=DS VM=1 Y Offset in Slide Coordinate System */
    public static final int YOffsetInSlideCoordinateSystem = 0x0040073A;

    /** (0040,074A) VR=DS VM=1 Z Offset in Slide Coordinate System */
    public static final int ZOffsetInSlideCoordinateSystem = 0x0040074A;

    /** (0040,08D8) VR=SQ VM=1 Pixel Spacing Sequence (retired) */
    public static final int PixelSpacingSequence = 0x004008D8;

    /** (0040,08DA) VR=SQ VM=1 Coordinate System Axis Code Sequence (retired) */
    public static final int CoordinateSystemAxisCodeSequence = 0x004008DA;

    /** (0040,08EA) VR=SQ VM=1 Measurement Units Code Sequence */
    public static final int MeasurementUnitsCodeSequence = 0x004008EA;

    /** (0040,09F8) VR=SQ VM=1 Vital Stain Code Sequence (Trial) (retired) */
    public static final int VitalStainCodeSequenceTrial = 0x004009F8;

    /** (0040,1001) VR=SH VM=1 Requested Procedure ID */
    public static final int RequestedProcedureID = 0x00401001;

    /** (0040,1002) VR=LO VM=1 Reason for the Requested Procedure */
    public static final int ReasonForTheRequestedProcedure = 0x00401002;

    /** (0040,1003) VR=SH VM=1 Requested Procedure Priority */
    public static final int RequestedProcedurePriority = 0x00401003;

    /** (0040,1004) VR=LO VM=1 Patient Transport Arrangements */
    public static final int PatientTransportArrangements = 0x00401004;

    /** (0040,1005) VR=LO VM=1 Requested Procedure Location */
    public static final int RequestedProcedureLocation = 0x00401005;

    /** (0040,1006) VR=SH VM=1 Placer Order Number / Procedure (retired) */
    public static final int PlacerOrderNumberProcedure = 0x00401006;

    /** (0040,1007) VR=SH VM=1 Filler Order Number / Procedure (retired) */
    public static final int FillerOrderNumberProcedure = 0x00401007;

    /** (0040,1008) VR=LO VM=1 Confidentiality Code */
    public static final int ConfidentialityCode = 0x00401008;

    /** (0040,1009) VR=SH VM=1 Reporting Priority */
    public static final int ReportingPriority = 0x00401009;

    /** (0040,100A) VR=SQ VM=1 Reason for Requested Procedure Code Sequence */
    public static final int ReasonForRequestedProcedureCodeSequence = 0x0040100A;

    /** (0040,1010) VR=PN VM=1-n Names of Intended Recipients of Results */
    public static final int NamesOfIntendedRecipientsOfResults = 0x00401010;

    /** (0040,1011) VR=SQ VM=1 Intended Recipients of Results Identification Sequence */
    public static final int IntendedRecipientsOfResultsIdentificationSequence = 0x00401011;

    /** (0040,1012) VR=SQ VM=1 Reason For Performed Procedure Code Sequence */
    public static final int ReasonForPerformedProcedureCodeSequence = 0x00401012;

    /** (0040,1060) VR=LO VM=1 Requested Procedure Description (Trial) (retired) */
    public static final int RequestedProcedureDescriptionTrial = 0x00401060;

    /** (0040,1101) VR=SQ VM=1 Person Identification Code Sequence */
    public static final int PersonIdentificationCodeSequence = 0x00401101;

    /** (0040,1102) VR=ST VM=1 Person's Address */
    public static final int PersonAddress = 0x00401102;

    /** (0040,1103) VR=LO VM=1-n Person's Telephone Numbers */
    public static final int PersonTelephoneNumbers = 0x00401103;

    /** (0040,1104) VR=LT VM=1 Person's Telecom Information */
    public static final int PersonTelecomInformation = 0x00401104;

    /** (0040,1400) VR=LT VM=1 Requested Procedure Comments */
    public static final int RequestedProcedureComments = 0x00401400;

    /** (0040,2001) VR=LO VM=1 Reason for the Imaging Service Request (retired) */
    public static final int ReasonForTheImagingServiceRequest = 0x00402001;

    /** (0040,2004) VR=DA VM=1 Issue Date of Imaging Service Request */
    public static final int IssueDateOfImagingServiceRequest = 0x00402004;

    /** (0040,2005) VR=TM VM=1 Issue Time of Imaging Service Request */
    public static final int IssueTimeOfImagingServiceRequest = 0x00402005;

    /** (0040,2006) VR=SH VM=1 Placer Order Number / Imaging Service Request (Retired) (retired) */
    public static final int PlacerOrderNumberImagingServiceRequestRetired = 0x00402006;

    /** (0040,2007) VR=SH VM=1 Filler Order Number / Imaging Service Request (Retired) (retired) */
    public static final int FillerOrderNumberImagingServiceRequestRetired = 0x00402007;

    /** (0040,2008) VR=PN VM=1 Order Entered By */
    public static final int OrderEnteredBy = 0x00402008;

    /** (0040,2009) VR=SH VM=1 Order Enterer's Location */
    public static final int OrderEntererLocation = 0x00402009;

    /** (0040,2010) VR=SH VM=1 Order Callback Phone Number */
    public static final int OrderCallbackPhoneNumber = 0x00402010;

    /** (0040,2011) VR=LT VM=1 Order Callback Telecom Information */
    public static final int OrderCallbackTelecomInformation = 0x00402011;

    /** (0040,2016) VR=LO VM=1 Placer Order Number / Imaging Service Request */
    public static final int PlacerOrderNumberImagingServiceRequest = 0x00402016;

    /** (0040,2017) VR=LO VM=1 Filler Order Number / Imaging Service Request */
    public static final int FillerOrderNumberImagingServiceRequest = 0x00402017;

    /** (0040,2400) VR=LT VM=1 Imaging Service Request Comments */
    public static final int ImagingServiceRequestComments = 0x00402400;

    /** (0040,3001) VR=LO VM=1 Confidentiality Constraint on Patient Data Description */
    public static final int ConfidentialityConstraintOnPatientDataDescription = 0x00403001;

    /** (0040,4001) VR=CS VM=1 General Purpose Scheduled Procedure Step Status (retired) */
    public static final int GeneralPurposeScheduledProcedureStepStatus = 0x00404001;

    /** (0040,4002) VR=CS VM=1 General Purpose Performed Procedure Step Status (retired) */
    public static final int GeneralPurposePerformedProcedureStepStatus = 0x00404002;

    /** (0040,4003) VR=CS VM=1 General Purpose Scheduled Procedure Step Priority (retired) */
    public static final int GeneralPurposeScheduledProcedureStepPriority = 0x00404003;

    /** (0040,4004) VR=SQ VM=1 Scheduled Processing Applications Code Sequence (retired) */
    public static final int ScheduledProcessingApplicationsCodeSequence = 0x00404004;

    /** (0040,4005) VR=DT VM=1 Scheduled Procedure Step Start DateTime */
    public static final int ScheduledProcedureStepStartDateTime = 0x00404005;

    /** (0040,4006) VR=CS VM=1 Multiple Copies Flag (retired) */
    public static final int MultipleCopiesFlag = 0x00404006;

    /** (0040,4007) VR=SQ VM=1 Performed Processing Applications Code Sequence (retired) */
    public static final int PerformedProcessingApplicationsCodeSequence = 0x00404007;

    /** (0040,4008) VR=DT VM=1 Scheduled Procedure Step Expiration DateTime */
    public static final int ScheduledProcedureStepExpirationDateTime = 0x00404008;

    /** (0040,4009) VR=SQ VM=1 Human Performer Code Sequence */
    public static final int HumanPerformerCodeSequence = 0x00404009;

    /** (0040,4010) VR=DT VM=1 Scheduled Procedure Step Modification DateTime */
    public static final int ScheduledProcedureStepModificationDateTime = 0x00404010;

    /** (0040,4011) VR=DT VM=1 Expected Completion DateTime */
    public static final int ExpectedCompletionDateTime = 0x00404011;

    /** (0040,4015) VR=SQ VM=1 Resulting General Purpose Performed Procedure Steps Sequence (retired) */
    public static final int ResultingGeneralPurposePerformedProcedureStepsSequence = 0x00404015;

    /** (0040,4016) VR=SQ VM=1 Referenced General Purpose Scheduled Procedure Step Sequence (retired) */
    public static final int ReferencedGeneralPurposeScheduledProcedureStepSequence = 0x00404016;

    /** (0040,4018) VR=SQ VM=1 Scheduled Workitem Code Sequence */
    public static final int ScheduledWorkitemCodeSequence = 0x00404018;

    /** (0040,4019) VR=SQ VM=1 Performed Workitem Code Sequence */
    public static final int PerformedWorkitemCodeSequence = 0x00404019;

    /** (0040,4020) VR=CS VM=1 Input Availability Flag (retired) */
    public static final int InputAvailabilityFlag = 0x00404020;

    /** (0040,4021) VR=SQ VM=1 Input Information Sequence */
    public static final int InputInformationSequence = 0x00404021;

    /** (0040,4022) VR=SQ VM=1 Relevant Information Sequence (retired) */
    public static final int RelevantInformationSequence = 0x00404022;

    /** (0040,4023) VR=UI VM=1 Referenced General Purpose Scheduled Procedure Step Transaction UID (retired) */
    public static final int ReferencedGeneralPurposeScheduledProcedureStepTransactionUID = 0x00404023;

    /** (0040,4025) VR=SQ VM=1 Scheduled Station Name Code Sequence */
    public static final int ScheduledStationNameCodeSequence = 0x00404025;

    /** (0040,4026) VR=SQ VM=1 Scheduled Station Class Code Sequence */
    public static final int ScheduledStationClassCodeSequence = 0x00404026;

    /** (0040,4027) VR=SQ VM=1 Scheduled Station Geographic Location Code Sequence */
    public static final int ScheduledStationGeographicLocationCodeSequence = 0x00404027;

    /** (0040,4028) VR=SQ VM=1 Performed Station Name Code Sequence */
    public static final int PerformedStationNameCodeSequence = 0x00404028;

    /** (0040,4029) VR=SQ VM=1 Performed Station Class Code Sequence */
    public static final int PerformedStationClassCodeSequence = 0x00404029;

    /** (0040,4030) VR=SQ VM=1 Performed Station Geographic Location Code Sequence */
    public static final int PerformedStationGeographicLocationCodeSequence = 0x00404030;

    /** (0040,4031) VR=SQ VM=1 Requested Subsequent Workitem Code Sequence (retired) */
    public static final int RequestedSubsequentWorkitemCodeSequence = 0x00404031;

    /** (0040,4032) VR=SQ VM=1 Non-DICOM Output Code Sequence (retired) */
    public static final int NonDICOMOutputCodeSequence = 0x00404032;

    /** (0040,4033) VR=SQ VM=1 Output Information Sequence */
    public static final int OutputInformationSequence = 0x00404033;

    /** (0040,4034) VR=SQ VM=1 Scheduled Human Performers Sequence */
    public static final int ScheduledHumanPerformersSequence = 0x00404034;

    /** (0040,4035) VR=SQ VM=1 Actual Human Performers Sequence */
    public static final int ActualHumanPerformersSequence = 0x00404035;

    /** (0040,4036) VR=LO VM=1 Human Performer's Organization */
    public static final int HumanPerformerOrganization = 0x00404036;

    /** (0040,4037) VR=PN VM=1 Human Performer's Name */
    public static final int HumanPerformerName = 0x00404037;

    /** (0040,4040) VR=CS VM=1 Raw Data Handling */
    public static final int RawDataHandling = 0x00404040;

    /** (0040,4041) VR=CS VM=1 Input Readiness State */
    public static final int InputReadinessState = 0x00404041;

    /** (0040,4050) VR=DT VM=1 Performed Procedure Step Start DateTime */
    public static final int PerformedProcedureStepStartDateTime = 0x00404050;

    /** (0040,4051) VR=DT VM=1 Performed Procedure Step End DateTime */
    public static final int PerformedProcedureStepEndDateTime = 0x00404051;

    /** (0040,4052) VR=DT VM=1 Procedure Step Cancellation DateTime */
    public static final int ProcedureStepCancellationDateTime = 0x00404052;

    /** (0040,4070) VR=SQ VM=1 Output Destination Sequence */
    public static final int OutputDestinationSequence = 0x00404070;

    /** (0040,4071) VR=SQ VM=1 DICOM Storage Sequence */
    public static final int DICOMStorageSequence = 0x00404071;

    /** (0040,4072) VR=SQ VM=1 STOW-RS Storage Sequence */
    public static final int STOWRSStorageSequence = 0x00404072;

    /** (0040,4073) VR=UR VM=1 Storage URL */
    public static final int StorageURL = 0x00404073;

    /** (0040,4074) VR=SQ VM=1 XDS Storage Sequence */
    public static final int XDSStorageSequence = 0x00404074;

    /** (0040,8302) VR=DS VM=1 Entrance Dose in mGy */
    public static final int EntranceDoseInmGy = 0x00408302;

    /** (0040,8303) VR=CS VM=1 Entrance Dose Derivation */
    public static final int EntranceDoseDerivation = 0x00408303;

    /** (0040,9092) VR=SQ VM=1 Parametric Map Frame Type Sequence */
    public static final int ParametricMapFrameTypeSequence = 0x00409092;

    /** (0040,9094) VR=SQ VM=1 Referenced Image Real World Value Mapping Sequence */
    public static final int ReferencedImageRealWorldValueMappingSequence = 0x00409094;

    /** (0040,9096) VR=SQ VM=1 Real World Value Mapping Sequence */
    public static final int RealWorldValueMappingSequence = 0x00409096;

    /** (0040,9098) VR=SQ VM=1 Pixel Value Mapping Code Sequence */
    public static final int PixelValueMappingCodeSequence = 0x00409098;

    /** (0040,9210) VR=SH VM=1 LUT Label */
    public static final int LUTLabel = 0x00409210;

    /** (0040,9211) VR=US or SS VM=1 Real World Value Last Value Mapped */
    public static final int RealWorldValueLastValueMapped = 0x00409211;

    /** (0040,9212) VR=FD VM=1-n Real World Value LUT Data */
    public static final int RealWorldValueLUTData = 0x00409212;

    /** (0040,9213) VR=FD VM=1 Double Float Real World Value Last Value Mapped */
    public static final int DoubleFloatRealWorldValueLastValueMapped = 0x00409213;

    /** (0040,9214) VR=FD VM=1 Double Float Real World Value First Value Mapped */
    public static final int DoubleFloatRealWorldValueFirstValueMapped = 0x00409214;

    /** (0040,9216) VR=US or SS VM=1 Real World Value First Value Mapped */
    public static final int RealWorldValueFirstValueMapped = 0x00409216;

    /** (0040,9220) VR=SQ VM=1 Quantity Definition Sequence */
    public static final int QuantityDefinitionSequence = 0x00409220;

    /** (0040,9224) VR=FD VM=1 Real World Value Intercept */
    public static final int RealWorldValueIntercept = 0x00409224;

    /** (0040,9225) VR=FD VM=1 Real World Value Slope */
    public static final int RealWorldValueSlope = 0x00409225;

    /** (0040,A007) VR=CS VM=1 Findings Flag (Trial) (retired) */
    public static final int FindingsFlagTrial = 0x0040A007;

    /** (0040,A010) VR=CS VM=1 Relationship Type */
    public static final int RelationshipType = 0x0040A010;

    /** (0040,A020) VR=SQ VM=1 Findings Sequence (Trial) (retired) */
    public static final int FindingsSequenceTrial = 0x0040A020;

    /** (0040,A021) VR=UI VM=1 Findings Group UID (Trial) (retired) */
    public static final int FindingsGroupUIDTrial = 0x0040A021;

    /** (0040,A022) VR=UI VM=1 Referenced Findings Group UID (Trial) (retired) */
    public static final int ReferencedFindingsGroupUIDTrial = 0x0040A022;

    /** (0040,A023) VR=DA VM=1 Findings Group Recording Date (Trial) (retired) */
    public static final int FindingsGroupRecordingDateTrial = 0x0040A023;

    /** (0040,A024) VR=TM VM=1 Findings Group Recording Time (Trial) (retired) */
    public static final int FindingsGroupRecordingTimeTrial = 0x0040A024;

    /** (0040,A026) VR=SQ VM=1 Findings Source Category Code Sequence (Trial) (retired) */
    public static final int FindingsSourceCategoryCodeSequenceTrial = 0x0040A026;

    /** (0040,A027) VR=LO VM=1 Verifying Organization */
    public static final int VerifyingOrganization = 0x0040A027;

    /** (0040,A028) VR=SQ VM=1 Documenting Organization Identifier Code Sequence (Trial) (retired) */
    public static final int DocumentingOrganizationIdentifierCodeSequenceTrial = 0x0040A028;

    /** (0040,A030) VR=DT VM=1 Verification DateTime */
    public static final int VerificationDateTime = 0x0040A030;

    /** (0040,A032) VR=DT VM=1 Observation DateTime */
    public static final int ObservationDateTime = 0x0040A032;

    /** (0040,A040) VR=CS VM=1 Value Type */
    public static final int ValueType = 0x0040A040;

    /** (0040,A043) VR=SQ VM=1 Concept Name Code Sequence */
    public static final int ConceptNameCodeSequence = 0x0040A043;

    /** (0040,A047) VR=LO VM=1 Measurement Precision Description (Trial) (retired) */
    public static final int MeasurementPrecisionDescriptionTrial = 0x0040A047;

    /** (0040,A050) VR=CS VM=1 Continuity Of Content */
    public static final int ContinuityOfContent = 0x0040A050;

    /** (0040,A057) VR=CS VM=1-n Urgency or Priority Alerts (Trial) (retired) */
    public static final int UrgencyOrPriorityAlertsTrial = 0x0040A057;

    /** (0040,A060) VR=LO VM=1 Sequencing Indicator (Trial) (retired) */
    public static final int SequencingIndicatorTrial = 0x0040A060;

    /** (0040,A066) VR=SQ VM=1 Document Identifier Code Sequence (Trial) (retired) */
    public static final int DocumentIdentifierCodeSequenceTrial = 0x0040A066;

    /** (0040,A067) VR=PN VM=1 Document Author (Trial) (retired) */
    public static final int DocumentAuthorTrial = 0x0040A067;

    /** (0040,A068) VR=SQ VM=1 Document Author Identifier Code Sequence (Trial) (retired) */
    public static final int DocumentAuthorIdentifierCodeSequenceTrial = 0x0040A068;

    /** (0040,A070) VR=SQ VM=1 Identifier Code Sequence (Trial) (retired) */
    public static final int IdentifierCodeSequenceTrial = 0x0040A070;

    /** (0040,A073) VR=SQ VM=1 Verifying Observer Sequence */
    public static final int VerifyingObserverSequence = 0x0040A073;

    /** (0040,A074) VR=OB VM=1 Object Binary Identifier (Trial) (retired) */
    public static final int ObjectBinaryIdentifierTrial = 0x0040A074;

    /** (0040,A075) VR=PN VM=1 Verifying Observer Name */
    public static final int VerifyingObserverName = 0x0040A075;

    /** (0040,A076) VR=SQ VM=1 Documenting Observer Identifier Code Sequence (Trial) (retired) */
    public static final int DocumentingObserverIdentifierCodeSequenceTrial = 0x0040A076;

    /** (0040,A078) VR=SQ VM=1 Author Observer Sequence */
    public static final int AuthorObserverSequence = 0x0040A078;

    /** (0040,A07A) VR=SQ VM=1 Participant Sequence */
    public static final int ParticipantSequence = 0x0040A07A;

    /** (0040,A07C) VR=SQ VM=1 Custodial Organization Sequence */
    public static final int CustodialOrganizationSequence = 0x0040A07C;

    /** (0040,A080) VR=CS VM=1 Participation Type */
    public static final int ParticipationType = 0x0040A080;

    /** (0040,A082) VR=DT VM=1 Participation DateTime */
    public static final int ParticipationDateTime = 0x0040A082;

    /** (0040,A084) VR=CS VM=1 Observer Type */
    public static final int ObserverType = 0x0040A084;

    /** (0040,A085) VR=SQ VM=1 Procedure Identifier Code Sequence (Trial) (retired) */
    public static final int ProcedureIdentifierCodeSequenceTrial = 0x0040A085;

    /** (0040,A088) VR=SQ VM=1 Verifying Observer Identification Code Sequence */
    public static final int VerifyingObserverIdentificationCodeSequence = 0x0040A088;

    /** (0040,A089) VR=OB VM=1 Object Directory Binary Identifier (Trial) (retired) */
    public static final int ObjectDirectoryBinaryIdentifierTrial = 0x0040A089;

    /** (0040,A090) VR=SQ VM=1 Equivalent CDA Document Sequence (retired) */
    public static final int EquivalentCDADocumentSequence = 0x0040A090;

    /** (0040,A0B0) VR=US VM=2-2n Referenced Waveform Channels */
    public static final int ReferencedWaveformChannels = 0x0040A0B0;

    /** (0040,A110) VR=DA VM=1 Date of Document or Verbal Transaction (Trial) (retired) */
    public static final int DateOfDocumentOrVerbalTransactionTrial = 0x0040A110;

    /** (0040,A112) VR=TM VM=1 Time of Document Creation or Verbal Transaction (Trial) (retired) */
    public static final int TimeOfDocumentCreationOrVerbalTransactionTrial = 0x0040A112;

    /** (0040,A120) VR=DT VM=1 DateTime */
    public static final int DateTime = 0x0040A120;

    /** (0040,A121) VR=DA VM=1 Date */
    public static final int Date = 0x0040A121;

    /** (0040,A122) VR=TM VM=1 Time */
    public static final int Time = 0x0040A122;

    /** (0040,A123) VR=PN VM=1 Person Name */
    public static final int PersonName = 0x0040A123;

    /** (0040,A124) VR=UI VM=1 UID */
    public static final int UID = 0x0040A124;

    /** (0040,A125) VR=CS VM=2 Report Status ID (Trial) (retired) */
    public static final int ReportStatusIDTrial = 0x0040A125;

    /** (0040,A130) VR=CS VM=1 Temporal Range Type */
    public static final int TemporalRangeType = 0x0040A130;

    /** (0040,A132) VR=UL VM=1-n Referenced Sample Positions */
    public static final int ReferencedSamplePositions = 0x0040A132;

    /** (0040,A136) VR=US VM=1-n Referenced Frame Numbers (retired) */
    public static final int ReferencedFrameNumbers = 0x0040A136;

    /** (0040,A138) VR=DS VM=1-n Referenced Time Offsets */
    public static final int ReferencedTimeOffsets = 0x0040A138;

    /** (0040,A13A) VR=DT VM=1-n Referenced DateTime */
    public static final int ReferencedDateTime = 0x0040A13A;

    /** (0040,A160) VR=UT VM=1 Text Value */
    public static final int TextValue = 0x0040A160;

    /** (0040,A161) VR=FD VM=1-n Floating Point Value */
    public static final int FloatingPointValue = 0x0040A161;

    /** (0040,A162) VR=SL VM=1-n Rational Numerator Value */
    public static final int RationalNumeratorValue = 0x0040A162;

    /** (0040,A163) VR=UL VM=1-n Rational Denominator Value */
    public static final int RationalDenominatorValue = 0x0040A163;

    /** (0040,A167) VR=SQ VM=1 Observation Category Code Sequence (Trial) (retired) */
    public static final int ObservationCategoryCodeSequenceTrial = 0x0040A167;

    /** (0040,A168) VR=SQ VM=1 Concept Code Sequence */
    public static final int ConceptCodeSequence = 0x0040A168;

    /** (0040,A16A) VR=ST VM=1 Bibliographic Citation (Trial) (retired) */
    public static final int BibliographicCitationTrial = 0x0040A16A;

    /** (0040,A170) VR=SQ VM=1 Purpose of Reference Code Sequence */
    public static final int PurposeOfReferenceCodeSequence = 0x0040A170;

    /** (0040,A171) VR=UI VM=1 Observation UID */
    public static final int ObservationUID = 0x0040A171;

    /** (0040,A172) VR=UI VM=1 Referenced Observation UID (Trial) (retired) */
    public static final int ReferencedObservationUIDTrial = 0x0040A172;

    /** (0040,A173) VR=CS VM=1 Referenced Observation Class (Trial) (retired) */
    public static final int ReferencedObservationClassTrial = 0x0040A173;

    /** (0040,A174) VR=CS VM=1 Referenced Object Observation Class (Trial) (retired) */
    public static final int ReferencedObjectObservationClassTrial = 0x0040A174;

    /** (0040,A180) VR=US VM=1 Annotation Group Number */
    public static final int AnnotationGroupNumber = 0x0040A180;

    /** (0040,A192) VR=DA VM=1 Observation Date (Trial) (retired) */
    public static final int ObservationDateTrial = 0x0040A192;

    /** (0040,A193) VR=TM VM=1 Observation Time (Trial) (retired) */
    public static final int ObservationTimeTrial = 0x0040A193;

    /** (0040,A194) VR=CS VM=1 Measurement Automation (Trial) (retired) */
    public static final int MeasurementAutomationTrial = 0x0040A194;

    /** (0040,A195) VR=SQ VM=1 Modifier Code Sequence */
    public static final int ModifierCodeSequence = 0x0040A195;

    /** (0040,A224) VR=ST VM=1 Identification Description (Trial) (retired) */
    public static final int IdentificationDescriptionTrial = 0x0040A224;

    /** (0040,A290) VR=CS VM=1 Coordinates Set Geometric Type (Trial) (retired) */
    public static final int CoordinatesSetGeometricTypeTrial = 0x0040A290;

    /** (0040,A296) VR=SQ VM=1 Algorithm Code Sequence (Trial) (retired) */
    public static final int AlgorithmCodeSequenceTrial = 0x0040A296;

    /** (0040,A297) VR=ST VM=1 Algorithm Description (Trial) (retired) */
    public static final int AlgorithmDescriptionTrial = 0x0040A297;

    /** (0040,A29A) VR=SL VM=2-2n Pixel Coordinates Set (Trial) (retired) */
    public static final int PixelCoordinatesSetTrial = 0x0040A29A;

    /** (0040,A300) VR=SQ VM=1 Measured Value Sequence */
    public static final int MeasuredValueSequence = 0x0040A300;

    /** (0040,A301) VR=SQ VM=1 Numeric Value Qualifier Code Sequence */
    public static final int NumericValueQualifierCodeSequence = 0x0040A301;

    /** (0040,A307) VR=PN VM=1 Current Observer (Trial) (retired) */
    public static final int CurrentObserverTrial = 0x0040A307;

    /** (0040,A30A) VR=DS VM=1-n Numeric Value */
    public static final int NumericValue = 0x0040A30A;

    /** (0040,A313) VR=SQ VM=1 Referenced Accession Sequence (Trial) (retired) */
    public static final int ReferencedAccessionSequenceTrial = 0x0040A313;

    /** (0040,A33A) VR=ST VM=1 Report Status Comment (Trial) (retired) */
    public static final int ReportStatusCommentTrial = 0x0040A33A;

    /** (0040,A340) VR=SQ VM=1 Procedure Context Sequence (Trial) (retired) */
    public static final int ProcedureContextSequenceTrial = 0x0040A340;

    /** (0040,A352) VR=PN VM=1 Verbal Source (Trial) (retired) */
    public static final int VerbalSourceTrial = 0x0040A352;

    /** (0040,A353) VR=ST VM=1 Address (Trial) (retired) */
    public static final int AddressTrial = 0x0040A353;

    /** (0040,A354) VR=LO VM=1 Telephone Number (Trial) (retired) */
    public static final int TelephoneNumberTrial = 0x0040A354;

    /** (0040,A358) VR=SQ VM=1 Verbal Source Identifier Code Sequence (Trial) (retired) */
    public static final int VerbalSourceIdentifierCodeSequenceTrial = 0x0040A358;

    /** (0040,A360) VR=SQ VM=1 Predecessor Documents Sequence */
    public static final int PredecessorDocumentsSequence = 0x0040A360;

    /** (0040,A370) VR=SQ VM=1 Referenced Request Sequence */
    public static final int ReferencedRequestSequence = 0x0040A370;

    /** (0040,A372) VR=SQ VM=1 Performed Procedure Code Sequence */
    public static final int PerformedProcedureCodeSequence = 0x0040A372;

    /** (0040,A375) VR=SQ VM=1 Current Requested Procedure Evidence Sequence */
    public static final int CurrentRequestedProcedureEvidenceSequence = 0x0040A375;

    /** (0040,A380) VR=SQ VM=1 Report Detail Sequence (Trial) (retired) */
    public static final int ReportDetailSequenceTrial = 0x0040A380;

    /** (0040,A385) VR=SQ VM=1 Pertinent Other Evidence Sequence */
    public static final int PertinentOtherEvidenceSequence = 0x0040A385;

    /** (0040,A390) VR=SQ VM=1 HL7 Structured Document Reference Sequence */
    public static final int HL7StructuredDocumentReferenceSequence = 0x0040A390;

    /** (0040,A402) VR=UI VM=1 Observation Subject UID (Trial) (retired) */
    public static final int ObservationSubjectUIDTrial = 0x0040A402;

    /** (0040,A403) VR=CS VM=1 Observation Subject Class (Trial) (retired) */
    public static final int ObservationSubjectClassTrial = 0x0040A403;

    /** (0040,A404) VR=SQ VM=1 Observation Subject Type Code Sequence (Trial) (retired) */
    public static final int ObservationSubjectTypeCodeSequenceTrial = 0x0040A404;

    /** (0040,A491) VR=CS VM=1 Completion Flag */
    public static final int CompletionFlag = 0x0040A491;

    /** (0040,A492) VR=LO VM=1 Completion Flag Description */
    public static final int CompletionFlagDescription = 0x0040A492;

    /** (0040,A493) VR=CS VM=1 Verification Flag */
    public static final int VerificationFlag = 0x0040A493;

    /** (0040,A494) VR=CS VM=1 Archive Requested */
    public static final int ArchiveRequested = 0x0040A494;

    /** (0040,A496) VR=CS VM=1 Preliminary Flag */
    public static final int PreliminaryFlag = 0x0040A496;

    /** (0040,A504) VR=SQ VM=1 Content Template Sequence */
    public static final int ContentTemplateSequence = 0x0040A504;

    /** (0040,A525) VR=SQ VM=1 Identical Documents Sequence */
    public static final int IdenticalDocumentsSequence = 0x0040A525;

    /** (0040,A600) VR=CS VM=1 Observation Subject Context Flag (Trial) (retired) */
    public static final int ObservationSubjectContextFlagTrial = 0x0040A600;

    /** (0040,A601) VR=CS VM=1 Observer Context Flag (Trial) (retired) */
    public static final int ObserverContextFlagTrial = 0x0040A601;

    /** (0040,A603) VR=CS VM=1 Procedure Context Flag (Trial) (retired) */
    public static final int ProcedureContextFlagTrial = 0x0040A603;

    /** (0040,A730) VR=SQ VM=1 Content Sequence */
    public static final int ContentSequence = 0x0040A730;

    /** (0040,A731) VR=SQ VM=1 Relationship Sequence (Trial) (retired) */
    public static final int RelationshipSequenceTrial = 0x0040A731;

    /** (0040,A732) VR=SQ VM=1 Relationship Type Code Sequence (Trial) (retired) */
    public static final int RelationshipTypeCodeSequenceTrial = 0x0040A732;

    /** (0040,A744) VR=SQ VM=1 Language Code Sequence (Trial) (retired) */
    public static final int LanguageCodeSequenceTrial = 0x0040A744;

    /** (0040,A992) VR=ST VM=1 Uniform Resource Locator (Trial) (retired) */
    public static final int UniformResourceLocatorTrial = 0x0040A992;

    /** (0040,B020) VR=SQ VM=1 Waveform Annotation Sequence */
    public static final int WaveformAnnotationSequence = 0x0040B020;

    /** (0040,DB00) VR=CS VM=1 Template Identifier */
    public static final int TemplateIdentifier = 0x0040DB00;

    /** (0040,DB06) VR=DT VM=1 Template Version (retired) */
    public static final int TemplateVersion = 0x0040DB06;

    /** (0040,DB07) VR=DT VM=1 Template Local Version (retired) */
    public static final int TemplateLocalVersion = 0x0040DB07;

    /** (0040,DB0B) VR=CS VM=1 Template Extension Flag (retired) */
    public static final int TemplateExtensionFlag = 0x0040DB0B;

    /** (0040,DB0C) VR=UI VM=1 Template Extension Organization UID (retired) */
    public static final int TemplateExtensionOrganizationUID = 0x0040DB0C;

    /** (0040,DB0D) VR=UI VM=1 Template Extension Creator UID (retired) */
    public static final int TemplateExtensionCreatorUID = 0x0040DB0D;

    /** (0040,DB73) VR=UL VM=1-n Referenced Content Item Identifier */
    public static final int ReferencedContentItemIdentifier = 0x0040DB73;

    /** (0040,E001) VR=ST VM=1 HL7 Instance Identifier */
    public static final int HL7InstanceIdentifier = 0x0040E001;

    /** (0040,E004) VR=DT VM=1 HL7 Document Effective Time */
    public static final int HL7DocumentEffectiveTime = 0x0040E004;

    /** (0040,E006) VR=SQ VM=1 HL7 Document Type Code Sequence */
    public static final int HL7DocumentTypeCodeSequence = 0x0040E006;

    /** (0040,E008) VR=SQ VM=1 Document Class Code Sequence */
    public static final int DocumentClassCodeSequence = 0x0040E008;

    /** (0040,E010) VR=UR VM=1 Retrieve URI */
    public static final int RetrieveURI = 0x0040E010;

    /** (0040,E011) VR=UI VM=1 Retrieve Location UID */
    public static final int RetrieveLocationUID = 0x0040E011;

    /** (0040,E020) VR=CS VM=1 Type of Instances */
    public static final int TypeOfInstances = 0x0040E020;

    /** (0040,E021) VR=SQ VM=1 DICOM Retrieval Sequence */
    public static final int DICOMRetrievalSequence = 0x0040E021;

    /** (0040,E022) VR=SQ VM=1 DICOM Media Retrieval Sequence */
    public static final int DICOMMediaRetrievalSequence = 0x0040E022;

    /** (0040,E023) VR=SQ VM=1 WADO Retrieval Sequence */
    public static final int WADORetrievalSequence = 0x0040E023;

    /** (0040,E024) VR=SQ VM=1 XDS Retrieval Sequence */
    public static final int XDSRetrievalSequence = 0x0040E024;

    /** (0040,E025) VR=SQ VM=1 WADO-RS Retrieval Sequence */
    public static final int WADORSRetrievalSequence = 0x0040E025;

    /** (0040,E030) VR=UI VM=1 Repository Unique ID */
    public static final int RepositoryUniqueID = 0x0040E030;

    /** (0040,E031) VR=UI VM=1 Home Community ID */
    public static final int HomeCommunityID = 0x0040E031;

    /** (0042,0010) VR=ST VM=1 Document Title */
    public static final int DocumentTitle = 0x00420010;

    /** (0042,0011) VR=OB VM=1 Encapsulated Document */
    public static final int EncapsulatedDocument = 0x00420011;

    /** (0042,0012) VR=LO VM=1 MIME Type of Encapsulated Document */
    public static final int MIMETypeOfEncapsulatedDocument = 0x00420012;

    /** (0042,0013) VR=SQ VM=1 Source Instance Sequence */
    public static final int SourceInstanceSequence = 0x00420013;

    /** (0042,0014) VR=LO VM=1-n List of MIME Types */
    public static final int ListOfMIMETypes = 0x00420014;

    /** (0042,0015) VR=UL VM=1 Encapsulated Document Length */
    public static final int EncapsulatedDocumentLength = 0x00420015;

    /** (0044,0001) VR=ST VM=1 Product Package Identifier */
    public static final int ProductPackageIdentifier = 0x00440001;

    /** (0044,0002) VR=CS VM=1 Substance Administration Approval */
    public static final int SubstanceAdministrationApproval = 0x00440002;

    /** (0044,0003) VR=LT VM=1 Approval Status Further Description */
    public static final int ApprovalStatusFurtherDescription = 0x00440003;

    /** (0044,0004) VR=DT VM=1 Approval Status DateTime */
    public static final int ApprovalStatusDateTime = 0x00440004;

    /** (0044,0007) VR=SQ VM=1 Product Type Code Sequence */
    public static final int ProductTypeCodeSequence = 0x00440007;

    /** (0044,0008) VR=LO VM=1-n Product Name */
    public static final int ProductName = 0x00440008;

    /** (0044,0009) VR=LT VM=1 Product Description */
    public static final int ProductDescription = 0x00440009;

    /** (0044,000A) VR=LO VM=1 Product Lot Identifier */
    public static final int ProductLotIdentifier = 0x0044000A;

    /** (0044,000B) VR=DT VM=1 Product Expiration DateTime */
    public static final int ProductExpirationDateTime = 0x0044000B;

    /** (0044,0010) VR=DT VM=1 Substance Administration DateTime */
    public static final int SubstanceAdministrationDateTime = 0x00440010;

    /** (0044,0011) VR=LO VM=1 Substance Administration Notes */
    public static final int SubstanceAdministrationNotes = 0x00440011;

    /** (0044,0012) VR=LO VM=1 Substance Administration Device ID */
    public static final int SubstanceAdministrationDeviceID = 0x00440012;

    /** (0044,0013) VR=SQ VM=1 Product Parameter Sequence */
    public static final int ProductParameterSequence = 0x00440013;

    /** (0044,0019) VR=SQ VM=1 Substance Administration Parameter Sequence */
    public static final int SubstanceAdministrationParameterSequence = 0x00440019;

    /** (0044,0100) VR=SQ VM=1 Approval Sequence */
    public static final int ApprovalSequence = 0x00440100;

    /** (0044,0101) VR=SQ VM=1 Assertion Code Sequence */
    public static final int AssertionCodeSequence = 0x00440101;

    /** (0044,0102) VR=UI VM=1 Assertion UID */
    public static final int AssertionUID = 0x00440102;

    /** (0044,0103) VR=SQ VM=1 Asserter Identification Sequence */
    public static final int AsserterIdentificationSequence = 0x00440103;

    /** (0044,0104) VR=DT VM=1 Assertion DateTime */
    public static final int AssertionDateTime = 0x00440104;

    /** (0044,0105) VR=DT VM=1 Assertion Expiration DateTime */
    public static final int AssertionExpirationDateTime = 0x00440105;

    /** (0044,0106) VR=UT VM=1 Assertion Comments */
    public static final int AssertionComments = 0x00440106;

    /** (0044,0107) VR=SQ VM=1 Related Assertion Sequence */
    public static final int RelatedAssertionSequence = 0x00440107;

    /** (0044,0108) VR=UI VM=1 Referenced Assertion UID */
    public static final int ReferencedAssertionUID = 0x00440108;

    /** (0044,0109) VR=SQ VM=1 Approval Subject Sequence */
    public static final int ApprovalSubjectSequence = 0x00440109;

    /** (0044,010A) VR=SQ VM=1 Organizational Role Code Sequence */
    public static final int OrganizationalRoleCodeSequence = 0x0044010A;

    /** (0046,0012) VR=LO VM=1 Lens Description */
    public static final int LensDescription = 0x00460012;

    /** (0046,0014) VR=SQ VM=1 Right Lens Sequence */
    public static final int RightLensSequence = 0x00460014;

    /** (0046,0015) VR=SQ VM=1 Left Lens Sequence */
    public static final int LeftLensSequence = 0x00460015;

    /** (0046,0016) VR=SQ VM=1 Unspecified Laterality Lens Sequence */
    public static final int UnspecifiedLateralityLensSequence = 0x00460016;

    /** (0046,0018) VR=SQ VM=1 Cylinder Sequence */
    public static final int CylinderSequence = 0x00460018;

    /** (0046,0028) VR=SQ VM=1 Prism Sequence */
    public static final int PrismSequence = 0x00460028;

    /** (0046,0030) VR=FD VM=1 Horizontal Prism Power */
    public static final int HorizontalPrismPower = 0x00460030;

    /** (0046,0032) VR=CS VM=1 Horizontal Prism Base */
    public static final int HorizontalPrismBase = 0x00460032;

    /** (0046,0034) VR=FD VM=1 Vertical Prism Power */
    public static final int VerticalPrismPower = 0x00460034;

    /** (0046,0036) VR=CS VM=1 Vertical Prism Base */
    public static final int VerticalPrismBase = 0x00460036;

    /** (0046,0038) VR=CS VM=1 Lens Segment Type */
    public static final int LensSegmentType = 0x00460038;

    /** (0046,0040) VR=FD VM=1 Optical Transmittance */
    public static final int OpticalTransmittance = 0x00460040;

    /** (0046,0042) VR=FD VM=1 Channel Width */
    public static final int ChannelWidth = 0x00460042;

    /** (0046,0044) VR=FD VM=1 Pupil Size */
    public static final int PupilSize = 0x00460044;

    /** (0046,0046) VR=FD VM=1 Corneal Size */
    public static final int CornealSize = 0x00460046;

    /** (0046,0047) VR=SQ VM=1 Corneal Size Sequence */
    public static final int CornealSizeSequence = 0x00460047;

    /** (0046,0050) VR=SQ VM=1 Autorefraction Right Eye Sequence */
    public static final int AutorefractionRightEyeSequence = 0x00460050;

    /** (0046,0052) VR=SQ VM=1 Autorefraction Left Eye Sequence */
    public static final int AutorefractionLeftEyeSequence = 0x00460052;

    /** (0046,0060) VR=FD VM=1 Distance Pupillary Distance */
    public static final int DistancePupillaryDistance = 0x00460060;

    /** (0046,0062) VR=FD VM=1 Near Pupillary Distance */
    public static final int NearPupillaryDistance = 0x00460062;

    /** (0046,0063) VR=FD VM=1 Intermediate Pupillary Distance */
    public static final int IntermediatePupillaryDistance = 0x00460063;

    /** (0046,0064) VR=FD VM=1 Other Pupillary Distance */
    public static final int OtherPupillaryDistance = 0x00460064;

    /** (0046,0070) VR=SQ VM=1 Keratometry Right Eye Sequence */
    public static final int KeratometryRightEyeSequence = 0x00460070;

    /** (0046,0071) VR=SQ VM=1 Keratometry Left Eye Sequence */
    public static final int KeratometryLeftEyeSequence = 0x00460071;

    /** (0046,0074) VR=SQ VM=1 Steep Keratometric Axis Sequence */
    public static final int SteepKeratometricAxisSequence = 0x00460074;

    /** (0046,0075) VR=FD VM=1 Radius of Curvature */
    public static final int RadiusOfCurvature = 0x00460075;

    /** (0046,0076) VR=FD VM=1 Keratometric Power */
    public static final int KeratometricPower = 0x00460076;

    /** (0046,0077) VR=FD VM=1 Keratometric Axis */
    public static final int KeratometricAxis = 0x00460077;

    /** (0046,0080) VR=SQ VM=1 Flat Keratometric Axis Sequence */
    public static final int FlatKeratometricAxisSequence = 0x00460080;

    /** (0046,0092) VR=CS VM=1 Background Color */
    public static final int BackgroundColor = 0x00460092;

    /** (0046,0094) VR=CS VM=1 Optotype */
    public static final int Optotype = 0x00460094;

    /** (0046,0095) VR=CS VM=1 Optotype Presentation */
    public static final int OptotypePresentation = 0x00460095;

    /** (0046,0097) VR=SQ VM=1 Subjective Refraction Right Eye Sequence */
    public static final int SubjectiveRefractionRightEyeSequence = 0x00460097;

    /** (0046,0098) VR=SQ VM=1 Subjective Refraction Left Eye Sequence */
    public static final int SubjectiveRefractionLeftEyeSequence = 0x00460098;

    /** (0046,0100) VR=SQ VM=1 Add Near Sequence */
    public static final int AddNearSequence = 0x00460100;

    /** (0046,0101) VR=SQ VM=1 Add Intermediate Sequence */
    public static final int AddIntermediateSequence = 0x00460101;

    /** (0046,0102) VR=SQ VM=1 Add Other Sequence */
    public static final int AddOtherSequence = 0x00460102;

    /** (0046,0104) VR=FD VM=1 Add Power */
    public static final int AddPower = 0x00460104;

    /** (0046,0106) VR=FD VM=1 Viewing Distance */
    public static final int ViewingDistance = 0x00460106;

    /** (0046,0110) VR=SQ VM=1 Cornea Measurements Sequence */
    public static final int CorneaMeasurementsSequence = 0x00460110;

    /** (0046,0111) VR=SQ VM=1 Source of Cornea Measurement Data Code Sequence */
    public static final int SourceOfCorneaMeasurementDataCodeSequence = 0x00460111;

    /** (0046,0112) VR=SQ VM=1 Steep Corneal Axis Sequence */
    public static final int SteepCornealAxisSequence = 0x00460112;

    /** (0046,0113) VR=SQ VM=1 Flat Corneal Axis Sequence */
    public static final int FlatCornealAxisSequence = 0x00460113;

    /** (0046,0114) VR=FD VM=1 Corneal Power */
    public static final int CornealPower = 0x00460114;

    /** (0046,0115) VR=FD VM=1 Corneal Axis */
    public static final int CornealAxis = 0x00460115;

    /** (0046,0116) VR=SQ VM=1 Cornea Measurement Method Code Sequence */
    public static final int CorneaMeasurementMethodCodeSequence = 0x00460116;

    /** (0046,0117) VR=FL VM=1 Refractive Index of Cornea */
    public static final int RefractiveIndexOfCornea = 0x00460117;

    /** (0046,0118) VR=FL VM=1 Refractive Index of Aqueous Humor */
    public static final int RefractiveIndexOfAqueousHumor = 0x00460118;

    /** (0046,0121) VR=SQ VM=1 Visual Acuity Type Code Sequence */
    public static final int VisualAcuityTypeCodeSequence = 0x00460121;

    /** (0046,0122) VR=SQ VM=1 Visual Acuity Right Eye Sequence */
    public static final int VisualAcuityRightEyeSequence = 0x00460122;

    /** (0046,0123) VR=SQ VM=1 Visual Acuity Left Eye Sequence */
    public static final int VisualAcuityLeftEyeSequence = 0x00460123;

    /** (0046,0124) VR=SQ VM=1 Visual Acuity Both Eyes Open Sequence */
    public static final int VisualAcuityBothEyesOpenSequence = 0x00460124;

    /** (0046,0125) VR=CS VM=1 Viewing Distance Type */
    public static final int ViewingDistanceType = 0x00460125;

    /** (0046,0135) VR=SS VM=2 Visual Acuity Modifiers */
    public static final int VisualAcuityModifiers = 0x00460135;

    /** (0046,0137) VR=FD VM=1 Decimal Visual Acuity */
    public static final int DecimalVisualAcuity = 0x00460137;

    /** (0046,0139) VR=LO VM=1 Optotype Detailed Definition */
    public static final int OptotypeDetailedDefinition = 0x00460139;

    /** (0046,0145) VR=SQ VM=1 Referenced Refractive Measurements Sequence */
    public static final int ReferencedRefractiveMeasurementsSequence = 0x00460145;

    /** (0046,0146) VR=FD VM=1 Sphere Power */
    public static final int SpherePower = 0x00460146;

    /** (0046,0147) VR=FD VM=1 Cylinder Power */
    public static final int CylinderPower = 0x00460147;

    /** (0046,0201) VR=CS VM=1 Corneal Topography Surface */
    public static final int CornealTopographySurface = 0x00460201;

    /** (0046,0202) VR=FL VM=2 Corneal Vertex Location */
    public static final int CornealVertexLocation = 0x00460202;

    /** (0046,0203) VR=FL VM=1 Pupil Centroid X-Coordinate */
    public static final int PupilCentroidXCoordinate = 0x00460203;

    /** (0046,0204) VR=FL VM=1 Pupil Centroid Y-Coordinate */
    public static final int PupilCentroidYCoordinate = 0x00460204;

    /** (0046,0205) VR=FL VM=1 Equivalent Pupil Radius */
    public static final int EquivalentPupilRadius = 0x00460205;

    /** (0046,0207) VR=SQ VM=1 Corneal Topography Map Type Code Sequence */
    public static final int CornealTopographyMapTypeCodeSequence = 0x00460207;

    /** (0046,0208) VR=IS VM=2-2n Vertices of the Outline of Pupil */
    public static final int VerticesOfTheOutlineOfPupil = 0x00460208;

    /** (0046,0210) VR=SQ VM=1 Corneal Topography Mapping Normals Sequence */
    public static final int CornealTopographyMappingNormalsSequence = 0x00460210;

    /** (0046,0211) VR=SQ VM=1 Maximum Corneal Curvature Sequence */
    public static final int MaximumCornealCurvatureSequence = 0x00460211;

    /** (0046,0212) VR=FL VM=1 Maximum Corneal Curvature */
    public static final int MaximumCornealCurvature = 0x00460212;

    /** (0046,0213) VR=FL VM=2 Maximum Corneal Curvature Location */
    public static final int MaximumCornealCurvatureLocation = 0x00460213;

    /** (0046,0215) VR=SQ VM=1 Minimum Keratometric Sequence */
    public static final int MinimumKeratometricSequence = 0x00460215;

    /** (0046,0218) VR=SQ VM=1 Simulated Keratometric Cylinder Sequence */
    public static final int SimulatedKeratometricCylinderSequence = 0x00460218;

    /** (0046,0220) VR=FL VM=1 Average Corneal Power */
    public static final int AverageCornealPower = 0x00460220;

    /** (0046,0224) VR=FL VM=1 Corneal I-S Value */
    public static final int CornealISValue = 0x00460224;

    /** (0046,0227) VR=FL VM=1 Analyzed Area */
    public static final int AnalyzedArea = 0x00460227;

    /** (0046,0230) VR=FL VM=1 Surface Regularity Index */
    public static final int SurfaceRegularityIndex = 0x00460230;

    /** (0046,0232) VR=FL VM=1 Surface Asymmetry Index */
    public static final int SurfaceAsymmetryIndex = 0x00460232;

    /** (0046,0234) VR=FL VM=1 Corneal Eccentricity Index */
    public static final int CornealEccentricityIndex = 0x00460234;

    /** (0046,0236) VR=FL VM=1 Keratoconus Prediction Index */
    public static final int KeratoconusPredictionIndex = 0x00460236;

    /** (0046,0238) VR=FL VM=1 Decimal Potential Visual Acuity */
    public static final int DecimalPotentialVisualAcuity = 0x00460238;

    /** (0046,0242) VR=CS VM=1 Corneal Topography Map Quality Evaluation */
    public static final int CornealTopographyMapQualityEvaluation = 0x00460242;

    /** (0046,0244) VR=SQ VM=1 Source Image Corneal Processed Data Sequence */
    public static final int SourceImageCornealProcessedDataSequence = 0x00460244;

    /** (0046,0247) VR=FL VM=3 Corneal Point Location */
    public static final int CornealPointLocation = 0x00460247;

    /** (0046,0248) VR=CS VM=1 Corneal Point Estimated */
    public static final int CornealPointEstimated = 0x00460248;

    /** (0046,0249) VR=FL VM=1 Axial Power */
    public static final int AxialPower = 0x00460249;

    /** (0046,0250) VR=FL VM=1 Tangential Power */
    public static final int TangentialPower = 0x00460250;

    /** (0046,0251) VR=FL VM=1 Refractive Power */
    public static final int RefractivePower = 0x00460251;

    /** (0046,0252) VR=FL VM=1 Relative Elevation */
    public static final int RelativeElevation = 0x00460252;

    /** (0046,0253) VR=FL VM=1 Corneal Wavefront */
    public static final int CornealWavefront = 0x00460253;

    /** (0048,0001) VR=FL VM=1 Imaged Volume Width */
    public static final int ImagedVolumeWidth = 0x00480001;

    /** (0048,0002) VR=FL VM=1 Imaged Volume Height */
    public static final int ImagedVolumeHeight = 0x00480002;

    /** (0048,0003) VR=FL VM=1 Imaged Volume Depth */
    public static final int ImagedVolumeDepth = 0x00480003;

    /** (0048,0006) VR=UL VM=1 Total Pixel Matrix Columns */
    public static final int TotalPixelMatrixColumns = 0x00480006;

    /** (0048,0007) VR=UL VM=1 Total Pixel Matrix Rows */
    public static final int TotalPixelMatrixRows = 0x00480007;

    /** (0048,0008) VR=SQ VM=1 Total Pixel Matrix Origin Sequence */
    public static final int TotalPixelMatrixOriginSequence = 0x00480008;

    /** (0048,0010) VR=CS VM=1 Specimen Label in Image */
    public static final int SpecimenLabelInImage = 0x00480010;

    /** (0048,0011) VR=CS VM=1 Focus Method */
    public static final int FocusMethod = 0x00480011;

    /** (0048,0012) VR=CS VM=1 Extended Depth of Field */
    public static final int ExtendedDepthOfField = 0x00480012;

    /** (0048,0013) VR=US VM=1 Number of Focal Planes */
    public static final int NumberOfFocalPlanes = 0x00480013;

    /** (0048,0014) VR=FL VM=1 Distance Between Focal Planes */
    public static final int DistanceBetweenFocalPlanes = 0x00480014;

    /** (0048,0015) VR=US VM=3 Recommended Absent Pixel CIELab Value */
    public static final int RecommendedAbsentPixelCIELabValue = 0x00480015;

    /** (0048,0100) VR=SQ VM=1 Illuminator Type Code Sequence */
    public static final int IlluminatorTypeCodeSequence = 0x00480100;

    /** (0048,0102) VR=DS VM=6 Image Orientation (Slide) */
    public static final int ImageOrientationSlide = 0x00480102;

    /** (0048,0105) VR=SQ VM=1 Optical Path Sequence */
    public static final int OpticalPathSequence = 0x00480105;

    /** (0048,0106) VR=SH VM=1 Optical Path Identifier */
    public static final int OpticalPathIdentifier = 0x00480106;

    /** (0048,0107) VR=ST VM=1 Optical Path Description */
    public static final int OpticalPathDescription = 0x00480107;

    /** (0048,0108) VR=SQ VM=1 Illumination Color Code Sequence */
    public static final int IlluminationColorCodeSequence = 0x00480108;

    /** (0048,0110) VR=SQ VM=1 Specimen Reference Sequence */
    public static final int SpecimenReferenceSequence = 0x00480110;

    /** (0048,0111) VR=DS VM=1 Condenser Lens Power */
    public static final int CondenserLensPower = 0x00480111;

    /** (0048,0112) VR=DS VM=1 Objective Lens Power */
    public static final int ObjectiveLensPower = 0x00480112;

    /** (0048,0113) VR=DS VM=1 Objective Lens Numerical Aperture */
    public static final int ObjectiveLensNumericalAperture = 0x00480113;

    /** (0048,0120) VR=SQ VM=1 Palette Color Lookup Table Sequence */
    public static final int PaletteColorLookupTableSequence = 0x00480120;

    /** (0048,0200) VR=SQ VM=1 Referenced Image Navigation Sequence */
    public static final int ReferencedImageNavigationSequence = 0x00480200;

    /** (0048,0201) VR=US VM=2 Top Left Hand Corner of Localizer Area */
    public static final int TopLeftHandCornerOfLocalizerArea = 0x00480201;

    /** (0048,0202) VR=US VM=2 Bottom Right Hand Corner of Localizer Area */
    public static final int BottomRightHandCornerOfLocalizerArea = 0x00480202;

    /** (0048,0207) VR=SQ VM=1 Optical Path Identification Sequence */
    public static final int OpticalPathIdentificationSequence = 0x00480207;

    /** (0048,021A) VR=SQ VM=1 Plane Position (Slide) Sequence */
    public static final int PlanePositionSlideSequence = 0x0048021A;

    /** (0048,021E) VR=SL VM=1 Column Position In Total Image Pixel Matrix */
    public static final int ColumnPositionInTotalImagePixelMatrix = 0x0048021E;

    /** (0048,021F) VR=SL VM=1 Row Position In Total Image Pixel Matrix */
    public static final int RowPositionInTotalImagePixelMatrix = 0x0048021F;

    /** (0048,0301) VR=CS VM=1 Pixel Origin Interpretation */
    public static final int PixelOriginInterpretation = 0x00480301;

    /** (0048,0302) VR=UL VM=1 Number of Optical Paths */
    public static final int NumberOfOpticalPaths = 0x00480302;

    /** (0048,0303) VR=UL VM=1 Total Pixel Matrix Focal Planes */
    public static final int TotalPixelMatrixFocalPlanes = 0x00480303;

    /** (0050,0004) VR=CS VM=1 Calibration Image */
    public static final int CalibrationImage = 0x00500004;

    /** (0050,0010) VR=SQ VM=1 Device Sequence */
    public static final int DeviceSequence = 0x00500010;

    /** (0050,0012) VR=SQ VM=1 Container Component Type Code Sequence */
    public static final int ContainerComponentTypeCodeSequence = 0x00500012;

    /** (0050,0013) VR=FD VM=1 Container Component Thickness */
    public static final int ContainerComponentThickness = 0x00500013;

    /** (0050,0014) VR=DS VM=1 Device Length */
    public static final int DeviceLength = 0x00500014;

    /** (0050,0015) VR=FD VM=1 Container Component Width */
    public static final int ContainerComponentWidth = 0x00500015;

    /** (0050,0016) VR=DS VM=1 Device Diameter */
    public static final int DeviceDiameter = 0x00500016;

    /** (0050,0017) VR=CS VM=1 Device Diameter Units */
    public static final int DeviceDiameterUnits = 0x00500017;

    /** (0050,0018) VR=DS VM=1 Device Volume */
    public static final int DeviceVolume = 0x00500018;

    /** (0050,0019) VR=DS VM=1 Inter-Marker Distance */
    public static final int InterMarkerDistance = 0x00500019;

    /** (0050,001A) VR=CS VM=1 Container Component Material */
    public static final int ContainerComponentMaterial = 0x0050001A;

    /** (0050,001B) VR=LO VM=1 Container Component ID */
    public static final int ContainerComponentID = 0x0050001B;

    /** (0050,001C) VR=FD VM=1 Container Component Length */
    public static final int ContainerComponentLength = 0x0050001C;

    /** (0050,001D) VR=FD VM=1 Container Component Diameter */
    public static final int ContainerComponentDiameter = 0x0050001D;

    /** (0050,001E) VR=LO VM=1 Container Component Description */
    public static final int ContainerComponentDescription = 0x0050001E;

    /** (0050,0020) VR=LO VM=1 Device Description */
    public static final int DeviceDescription = 0x00500020;

    /** (0050,0021) VR=ST VM=1 Long Device Description */
    public static final int LongDeviceDescription = 0x00500021;

    /** (0052,0001) VR=FL VM=1 Contrast/Bolus Ingredient Percent by Volume */
    public static final int ContrastBolusIngredientPercentByVolume = 0x00520001;

    /** (0052,0002) VR=FD VM=1 OCT Focal Distance */
    public static final int OCTFocalDistance = 0x00520002;

    /** (0052,0003) VR=FD VM=1 Beam Spot Size */
    public static final int BeamSpotSize = 0x00520003;

    /** (0052,0004) VR=FD VM=1 Effective Refractive Index */
    public static final int EffectiveRefractiveIndex = 0x00520004;

    /** (0052,0006) VR=CS VM=1 OCT Acquisition Domain */
    public static final int OCTAcquisitionDomain = 0x00520006;

    /** (0052,0007) VR=FD VM=1 OCT Optical Center Wavelength */
    public static final int OCTOpticalCenterWavelength = 0x00520007;

    /** (0052,0008) VR=FD VM=1 Axial Resolution */
    public static final int AxialResolution = 0x00520008;

    /** (0052,0009) VR=FD VM=1 Ranging Depth */
    public static final int RangingDepth = 0x00520009;

    /** (0052,0011) VR=FD VM=1 A-line Rate */
    public static final int ALineRate = 0x00520011;

    /** (0052,0012) VR=US VM=1 A-lines Per Frame */
    public static final int ALinesPerFrame = 0x00520012;

    /** (0052,0013) VR=FD VM=1 Catheter Rotational Rate */
    public static final int CatheterRotationalRate = 0x00520013;

    /** (0052,0014) VR=FD VM=1 A-line Pixel Spacing */
    public static final int ALinePixelSpacing = 0x00520014;

    /** (0052,0016) VR=SQ VM=1 Mode of Percutaneous Access Sequence */
    public static final int ModeOfPercutaneousAccessSequence = 0x00520016;

    /** (0052,0025) VR=SQ VM=1 Intravascular OCT Frame Type Sequence */
    public static final int IntravascularOCTFrameTypeSequence = 0x00520025;

    /** (0052,0026) VR=CS VM=1 OCT Z Offset Applied */
    public static final int OCTZOffsetApplied = 0x00520026;

    /** (0052,0027) VR=SQ VM=1 Intravascular Frame Content Sequence */
    public static final int IntravascularFrameContentSequence = 0x00520027;

    /** (0052,0028) VR=FD VM=1 Intravascular Longitudinal Distance */
    public static final int IntravascularLongitudinalDistance = 0x00520028;

    /** (0052,0029) VR=SQ VM=1 Intravascular OCT Frame Content Sequence */
    public static final int IntravascularOCTFrameContentSequence = 0x00520029;

    /** (0052,0030) VR=SS VM=1 OCT Z Offset Correction */
    public static final int OCTZOffsetCorrection = 0x00520030;

    /** (0052,0031) VR=CS VM=1 Catheter Direction of Rotation */
    public static final int CatheterDirectionOfRotation = 0x00520031;

    /** (0052,0033) VR=FD VM=1 Seam Line Location */
    public static final int SeamLineLocation = 0x00520033;

    /** (0052,0034) VR=FD VM=1 First A-line Location */
    public static final int FirstALineLocation = 0x00520034;

    /** (0052,0036) VR=US VM=1 Seam Line Index */
    public static final int SeamLineIndex = 0x00520036;

    /** (0052,0038) VR=US VM=1 Number of Padded A-lines */
    public static final int NumberOfPaddedALines = 0x00520038;

    /** (0052,0039) VR=CS VM=1 Interpolation Type */
    public static final int InterpolationType = 0x00520039;

    /** (0052,003A) VR=CS VM=1 Refractive Index Applied */
    public static final int RefractiveIndexApplied = 0x0052003A;

    /** (0054,0010) VR=US VM=1-n Energy Window Vector */
    public static final int EnergyWindowVector = 0x00540010;

    /** (0054,0011) VR=US VM=1 Number of Energy Windows */
    public static final int NumberOfEnergyWindows = 0x00540011;

    /** (0054,0012) VR=SQ VM=1 Energy Window Information Sequence */
    public static final int EnergyWindowInformationSequence = 0x00540012;

    /** (0054,0013) VR=SQ VM=1 Energy Window Range Sequence */
    public static final int EnergyWindowRangeSequence = 0x00540013;

    /** (0054,0014) VR=DS VM=1 Energy Window Lower Limit */
    public static final int EnergyWindowLowerLimit = 0x00540014;

    /** (0054,0015) VR=DS VM=1 Energy Window Upper Limit */
    public static final int EnergyWindowUpperLimit = 0x00540015;

    /** (0054,0016) VR=SQ VM=1 Radiopharmaceutical Information Sequence */
    public static final int RadiopharmaceuticalInformationSequence = 0x00540016;

    /** (0054,0017) VR=IS VM=1 Residual Syringe Counts */
    public static final int ResidualSyringeCounts = 0x00540017;

    /** (0054,0018) VR=SH VM=1 Energy Window Name */
    public static final int EnergyWindowName = 0x00540018;

    /** (0054,0020) VR=US VM=1-n Detector Vector */
    public static final int DetectorVector = 0x00540020;

    /** (0054,0021) VR=US VM=1 Number of Detectors */
    public static final int NumberOfDetectors = 0x00540021;

    /** (0054,0022) VR=SQ VM=1 Detector Information Sequence */
    public static final int DetectorInformationSequence = 0x00540022;

    /** (0054,0030) VR=US VM=1-n Phase Vector */
    public static final int PhaseVector = 0x00540030;

    /** (0054,0031) VR=US VM=1 Number of Phases */
    public static final int NumberOfPhases = 0x00540031;

    /** (0054,0032) VR=SQ VM=1 Phase Information Sequence */
    public static final int PhaseInformationSequence = 0x00540032;

    /** (0054,0033) VR=US VM=1 Number of Frames in Phase */
    public static final int NumberOfFramesInPhase = 0x00540033;

    /** (0054,0036) VR=IS VM=1 Phase Delay */
    public static final int PhaseDelay = 0x00540036;

    /** (0054,0038) VR=IS VM=1 Pause Between Frames */
    public static final int PauseBetweenFrames = 0x00540038;

    /** (0054,0039) VR=CS VM=1 Phase Description */
    public static final int PhaseDescription = 0x00540039;

    /** (0054,0050) VR=US VM=1-n Rotation Vector */
    public static final int RotationVector = 0x00540050;

    /** (0054,0051) VR=US VM=1 Number of Rotations */
    public static final int NumberOfRotations = 0x00540051;

    /** (0054,0052) VR=SQ VM=1 Rotation Information Sequence */
    public static final int RotationInformationSequence = 0x00540052;

    /** (0054,0053) VR=US VM=1 Number of Frames in Rotation */
    public static final int NumberOfFramesInRotation = 0x00540053;

    /** (0054,0060) VR=US VM=1-n R-R Interval Vector */
    public static final int RRIntervalVector = 0x00540060;

    /** (0054,0061) VR=US VM=1 Number of R-R Intervals */
    public static final int NumberOfRRIntervals = 0x00540061;

    /** (0054,0062) VR=SQ VM=1 Gated Information Sequence */
    public static final int GatedInformationSequence = 0x00540062;

    /** (0054,0063) VR=SQ VM=1 Data Information Sequence */
    public static final int DataInformationSequence = 0x00540063;

    /** (0054,0070) VR=US VM=1-n Time Slot Vector */
    public static final int TimeSlotVector = 0x00540070;

    /** (0054,0071) VR=US VM=1 Number of Time Slots */
    public static final int NumberOfTimeSlots = 0x00540071;

    /** (0054,0072) VR=SQ VM=1 Time Slot Information Sequence */
    public static final int TimeSlotInformationSequence = 0x00540072;

    /** (0054,0073) VR=DS VM=1 Time Slot Time */
    public static final int TimeSlotTime = 0x00540073;

    /** (0054,0080) VR=US VM=1-n Slice Vector */
    public static final int SliceVector = 0x00540080;

    /** (0054,0081) VR=US VM=1 Number of Slices */
    public static final int NumberOfSlices = 0x00540081;

    /** (0054,0090) VR=US VM=1-n Angular View Vector */
    public static final int AngularViewVector = 0x00540090;

    /** (0054,0100) VR=US VM=1-n Time Slice Vector */
    public static final int TimeSliceVector = 0x00540100;

    /** (0054,0101) VR=US VM=1 Number of Time Slices */
    public static final int NumberOfTimeSlices = 0x00540101;

    /** (0054,0200) VR=DS VM=1 Start Angle */
    public static final int StartAngle = 0x00540200;

    /** (0054,0202) VR=CS VM=1 Type of Detector Motion */
    public static final int TypeOfDetectorMotion = 0x00540202;

    /** (0054,0210) VR=IS VM=1-n Trigger Vector */
    public static final int TriggerVector = 0x00540210;

    /** (0054,0211) VR=US VM=1 Number of Triggers in Phase */
    public static final int NumberOfTriggersInPhase = 0x00540211;

    /** (0054,0220) VR=SQ VM=1 View Code Sequence */
    public static final int ViewCodeSequence = 0x00540220;

    /** (0054,0222) VR=SQ VM=1 View Modifier Code Sequence */
    public static final int ViewModifierCodeSequence = 0x00540222;

    /** (0054,0300) VR=SQ VM=1 Radionuclide Code Sequence */
    public static final int RadionuclideCodeSequence = 0x00540300;

    /** (0054,0302) VR=SQ VM=1 Administration Route Code Sequence */
    public static final int AdministrationRouteCodeSequence = 0x00540302;

    /** (0054,0304) VR=SQ VM=1 Radiopharmaceutical Code Sequence */
    public static final int RadiopharmaceuticalCodeSequence = 0x00540304;

    /** (0054,0306) VR=SQ VM=1 Calibration Data Sequence */
    public static final int CalibrationDataSequence = 0x00540306;

    /** (0054,0308) VR=US VM=1 Energy Window Number */
    public static final int EnergyWindowNumber = 0x00540308;

    /** (0054,0400) VR=SH VM=1 Image ID */
    public static final int ImageID = 0x00540400;

    /** (0054,0410) VR=SQ VM=1 Patient Orientation Code Sequence */
    public static final int PatientOrientationCodeSequence = 0x00540410;

    /** (0054,0412) VR=SQ VM=1 Patient Orientation Modifier Code Sequence */
    public static final int PatientOrientationModifierCodeSequence = 0x00540412;

    /** (0054,0414) VR=SQ VM=1 Patient Gantry Relationship Code Sequence */
    public static final int PatientGantryRelationshipCodeSequence = 0x00540414;

    /** (0054,0500) VR=CS VM=1 Slice Progression Direction */
    public static final int SliceProgressionDirection = 0x00540500;

    /** (0054,0501) VR=CS VM=1 Scan Progression Direction */
    public static final int ScanProgressionDirection = 0x00540501;

    /** (0054,1000) VR=CS VM=2 Series Type */
    public static final int SeriesType = 0x00541000;

    /** (0054,1001) VR=CS VM=1 Units */
    public static final int Units = 0x00541001;

    /** (0054,1002) VR=CS VM=1 Counts Source */
    public static final int CountsSource = 0x00541002;

    /** (0054,1004) VR=CS VM=1 Reprojection Method */
    public static final int ReprojectionMethod = 0x00541004;

    /** (0054,1006) VR=CS VM=1 SUV Type */
    public static final int SUVType = 0x00541006;

    /** (0054,1100) VR=CS VM=1 Randoms Correction Method */
    public static final int RandomsCorrectionMethod = 0x00541100;

    /** (0054,1101) VR=LO VM=1 Attenuation Correction Method */
    public static final int AttenuationCorrectionMethod = 0x00541101;

    /** (0054,1102) VR=CS VM=1 Decay Correction */
    public static final int DecayCorrection = 0x00541102;

    /** (0054,1103) VR=LO VM=1 Reconstruction Method */
    public static final int ReconstructionMethod = 0x00541103;

    /** (0054,1104) VR=LO VM=1 Detector Lines of Response Used */
    public static final int DetectorLinesOfResponseUsed = 0x00541104;

    /** (0054,1105) VR=LO VM=1 Scatter Correction Method */
    public static final int ScatterCorrectionMethod = 0x00541105;

    /** (0054,1200) VR=DS VM=1 Axial Acceptance */
    public static final int AxialAcceptance = 0x00541200;

    /** (0054,1201) VR=IS VM=2 Axial Mash */
    public static final int AxialMash = 0x00541201;

    /** (0054,1202) VR=IS VM=1 Transverse Mash */
    public static final int TransverseMash = 0x00541202;

    /** (0054,1203) VR=DS VM=2 Detector Element Size */
    public static final int DetectorElementSize = 0x00541203;

    /** (0054,1210) VR=DS VM=1 Coincidence Window Width */
    public static final int CoincidenceWindowWidth = 0x00541210;

    /** (0054,1220) VR=CS VM=1-n Secondary Counts Type */
    public static final int SecondaryCountsType = 0x00541220;

    /** (0054,1300) VR=DS VM=1 Frame Reference Time */
    public static final int FrameReferenceTime = 0x00541300;

    /** (0054,1310) VR=IS VM=1 Primary (Prompts) Counts Accumulated */
    public static final int PrimaryPromptsCountsAccumulated = 0x00541310;

    /** (0054,1311) VR=IS VM=1-n Secondary Counts Accumulated */
    public static final int SecondaryCountsAccumulated = 0x00541311;

    /** (0054,1320) VR=DS VM=1 Slice Sensitivity Factor */
    public static final int SliceSensitivityFactor = 0x00541320;

    /** (0054,1321) VR=DS VM=1 Decay Factor */
    public static final int DecayFactor = 0x00541321;

    /** (0054,1322) VR=DS VM=1 Dose Calibration Factor */
    public static final int DoseCalibrationFactor = 0x00541322;

    /** (0054,1323) VR=DS VM=1 Scatter Fraction Factor */
    public static final int ScatterFractionFactor = 0x00541323;

    /** (0054,1324) VR=DS VM=1 Dead Time Factor */
    public static final int DeadTimeFactor = 0x00541324;

    /** (0054,1330) VR=US VM=1 Image Index */
    public static final int ImageIndex = 0x00541330;

    /** (0054,1400) VR=CS VM=1-n Counts Included (retired) */
    public static final int CountsIncluded = 0x00541400;

    /** (0054,1401) VR=CS VM=1 Dead Time Correction Flag (retired) */
    public static final int DeadTimeCorrectionFlag = 0x00541401;

    /** (0060,3000) VR=SQ VM=1 Histogram Sequence */
    public static final int HistogramSequence = 0x00603000;

    /** (0060,3002) VR=US VM=1 Histogram Number of Bins */
    public static final int HistogramNumberOfBins = 0x00603002;

    /** (0060,3004) VR=US or SS VM=1 Histogram First Bin Value */
    public static final int HistogramFirstBinValue = 0x00603004;

    /** (0060,3006) VR=US or SS VM=1 Histogram Last Bin Value */
    public static final int HistogramLastBinValue = 0x00603006;

    /** (0060,3008) VR=US VM=1 Histogram Bin Width */
    public static final int HistogramBinWidth = 0x00603008;

    /** (0060,3010) VR=LO VM=1 Histogram Explanation */
    public static final int HistogramExplanation = 0x00603010;

    /** (0060,3020) VR=UL VM=1-n Histogram Data */
    public static final int HistogramData = 0x00603020;

    /** (0062,0001) VR=CS VM=1 Segmentation Type */
    public static final int SegmentationType = 0x00620001;

    /** (0062,0002) VR=SQ VM=1 Segment Sequence */
    public static final int SegmentSequence = 0x00620002;

    /** (0062,0003) VR=SQ VM=1 Segmented Property Category Code Sequence */
    public static final int SegmentedPropertyCategoryCodeSequence = 0x00620003;

    /** (0062,0004) VR=US VM=1 Segment Number */
    public static final int SegmentNumber = 0x00620004;

    /** (0062,0005) VR=LO VM=1 Segment Label */
    public static final int SegmentLabel = 0x00620005;

    /** (0062,0006) VR=ST VM=1 Segment Description */
    public static final int SegmentDescription = 0x00620006;

    /** (0062,0007) VR=SQ VM=1 Segmentation Algorithm Identification Sequence */
    public static final int SegmentationAlgorithmIdentificationSequence = 0x00620007;

    /** (0062,0008) VR=CS VM=1 Segment Algorithm Type */
    public static final int SegmentAlgorithmType = 0x00620008;

    /** (0062,0009) VR=LO VM=1 Segment Algorithm Name */
    public static final int SegmentAlgorithmName = 0x00620009;

    /** (0062,000A) VR=SQ VM=1 Segment Identification Sequence */
    public static final int SegmentIdentificationSequence = 0x0062000A;

    /** (0062,000B) VR=US VM=1-n Referenced Segment Number */
    public static final int ReferencedSegmentNumber = 0x0062000B;

    /** (0062,000C) VR=US VM=1 Recommended Display Grayscale Value */
    public static final int RecommendedDisplayGrayscaleValue = 0x0062000C;

    /** (0062,000D) VR=US VM=3 Recommended Display CIELab Value */
    public static final int RecommendedDisplayCIELabValue = 0x0062000D;

    /** (0062,000E) VR=US VM=1 Maximum Fractional Value */
    public static final int MaximumFractionalValue = 0x0062000E;

    /** (0062,000F) VR=SQ VM=1 Segmented Property Type Code Sequence */
    public static final int SegmentedPropertyTypeCodeSequence = 0x0062000F;

    /** (0062,0010) VR=CS VM=1 Segmentation Fractional Type */
    public static final int SegmentationFractionalType = 0x00620010;

    /** (0062,0011) VR=SQ VM=1 Segmented Property Type Modifier Code Sequence */
    public static final int SegmentedPropertyTypeModifierCodeSequence = 0x00620011;

    /** (0062,0012) VR=SQ VM=1 Used Segments Sequence */
    public static final int UsedSegmentsSequence = 0x00620012;

    /** (0062,0013) VR=CS VM=1 Segments Overlap */
    public static final int SegmentsOverlap = 0x00620013;

    /** (0062,0020) VR=UT VM=1 Tracking ID */
    public static final int TrackingID = 0x00620020;

    /** (0062,0021) VR=UI VM=1 Tracking UID */
    public static final int TrackingUID = 0x00620021;

    /** (0064,0002) VR=SQ VM=1 Deformable Registration Sequence */
    public static final int DeformableRegistrationSequence = 0x00640002;

    /** (0064,0003) VR=UI VM=1 Source Frame of Reference UID */
    public static final int SourceFrameOfReferenceUID = 0x00640003;

    /** (0064,0005) VR=SQ VM=1 Deformable Registration Grid Sequence */
    public static final int DeformableRegistrationGridSequence = 0x00640005;

    /** (0064,0007) VR=UL VM=3 Grid Dimensions */
    public static final int GridDimensions = 0x00640007;

    /** (0064,0008) VR=FD VM=3 Grid Resolution */
    public static final int GridResolution = 0x00640008;

    /** (0064,0009) VR=OF VM=1 Vector Grid Data */
    public static final int VectorGridData = 0x00640009;

    /** (0064,000F) VR=SQ VM=1 Pre Deformation Matrix Registration Sequence */
    public static final int PreDeformationMatrixRegistrationSequence = 0x0064000F;

    /** (0064,0010) VR=SQ VM=1 Post Deformation Matrix Registration Sequence */
    public static final int PostDeformationMatrixRegistrationSequence = 0x00640010;

    /** (0066,0001) VR=UL VM=1 Number of Surfaces */
    public static final int NumberOfSurfaces = 0x00660001;

    /** (0066,0002) VR=SQ VM=1 Surface Sequence */
    public static final int SurfaceSequence = 0x00660002;

    /** (0066,0003) VR=UL VM=1 Surface Number */
    public static final int SurfaceNumber = 0x00660003;

    /** (0066,0004) VR=LT VM=1 Surface Comments */
    public static final int SurfaceComments = 0x00660004;

    /** (0066,0009) VR=CS VM=1 Surface Processing */
    public static final int SurfaceProcessing = 0x00660009;

    /** (0066,000A) VR=FL VM=1 Surface Processing Ratio */
    public static final int SurfaceProcessingRatio = 0x0066000A;

    /** (0066,000B) VR=LO VM=1 Surface Processing Description */
    public static final int SurfaceProcessingDescription = 0x0066000B;

    /** (0066,000C) VR=FL VM=1 Recommended Presentation Opacity */
    public static final int RecommendedPresentationOpacity = 0x0066000C;

    /** (0066,000D) VR=CS VM=1 Recommended Presentation Type */
    public static final int RecommendedPresentationType = 0x0066000D;

    /** (0066,000E) VR=CS VM=1 Finite Volume */
    public static final int FiniteVolume = 0x0066000E;

    /** (0066,0010) VR=CS VM=1 Manifold */
    public static final int Manifold = 0x00660010;

    /** (0066,0011) VR=SQ VM=1 Surface Points Sequence */
    public static final int SurfacePointsSequence = 0x00660011;

    /** (0066,0012) VR=SQ VM=1 Surface Points Normals Sequence */
    public static final int SurfacePointsNormalsSequence = 0x00660012;

    /** (0066,0013) VR=SQ VM=1 Surface Mesh Primitives Sequence */
    public static final int SurfaceMeshPrimitivesSequence = 0x00660013;

    /** (0066,0015) VR=UL VM=1 Number of Surface Points */
    public static final int NumberOfSurfacePoints = 0x00660015;

    /** (0066,0016) VR=OF VM=1 Point Coordinates Data */
    public static final int PointCoordinatesData = 0x00660016;

    /** (0066,0017) VR=FL VM=3 Point Position Accuracy */
    public static final int PointPositionAccuracy = 0x00660017;

    /** (0066,0018) VR=FL VM=1 Mean Point Distance */
    public static final int MeanPointDistance = 0x00660018;

    /** (0066,0019) VR=FL VM=1 Maximum Point Distance */
    public static final int MaximumPointDistance = 0x00660019;

    /** (0066,001A) VR=FL VM=6 Points Bounding Box Coordinates */
    public static final int PointsBoundingBoxCoordinates = 0x0066001A;

    /** (0066,001B) VR=FL VM=3 Axis of Rotation */
    public static final int AxisOfRotation = 0x0066001B;

    /** (0066,001C) VR=FL VM=3 Center of Rotation */
    public static final int CenterOfRotation = 0x0066001C;

    /** (0066,001E) VR=UL VM=1 Number of Vectors */
    public static final int NumberOfVectors = 0x0066001E;

    /** (0066,001F) VR=US VM=1 Vector Dimensionality */
    public static final int VectorDimensionality = 0x0066001F;

    /** (0066,0020) VR=FL VM=1-n Vector Accuracy */
    public static final int VectorAccuracy = 0x00660020;

    /** (0066,0021) VR=OF VM=1 Vector Coordinate Data */
    public static final int VectorCoordinateData = 0x00660021;

    /** (0066,0023) VR=OW VM=1 Triangle Point Index List (retired) */
    public static final int TrianglePointIndexList = 0x00660023;

    /** (0066,0024) VR=OW VM=1 Edge Point Index List (retired) */
    public static final int EdgePointIndexList = 0x00660024;

    /** (0066,0025) VR=OW VM=1 Vertex Point Index List (retired) */
    public static final int VertexPointIndexList = 0x00660025;

    /** (0066,0026) VR=SQ VM=1 Triangle Strip Sequence */
    public static final int TriangleStripSequence = 0x00660026;

    /** (0066,0027) VR=SQ VM=1 Triangle Fan Sequence */
    public static final int TriangleFanSequence = 0x00660027;

    /** (0066,0028) VR=SQ VM=1 Line Sequence */
    public static final int LineSequence = 0x00660028;

    /** (0066,0029) VR=OW VM=1 Primitive Point Index List (retired) */
    public static final int PrimitivePointIndexList = 0x00660029;

    /** (0066,002A) VR=UL VM=1 Surface Count */
    public static final int SurfaceCount = 0x0066002A;

    /** (0066,002B) VR=SQ VM=1 Referenced Surface Sequence */
    public static final int ReferencedSurfaceSequence = 0x0066002B;

    /** (0066,002C) VR=UL VM=1 Referenced Surface Number */
    public static final int ReferencedSurfaceNumber = 0x0066002C;

    /** (0066,002D) VR=SQ VM=1 Segment Surface Generation Algorithm Identification Sequence */
    public static final int SegmentSurfaceGenerationAlgorithmIdentificationSequence = 0x0066002D;

    /** (0066,002E) VR=SQ VM=1 Segment Surface Source Instance Sequence */
    public static final int SegmentSurfaceSourceInstanceSequence = 0x0066002E;

    /** (0066,002F) VR=SQ VM=1 Algorithm Family Code Sequence */
    public static final int AlgorithmFamilyCodeSequence = 0x0066002F;

    /** (0066,0030) VR=SQ VM=1 Algorithm Name Code Sequence */
    public static final int AlgorithmNameCodeSequence = 0x00660030;

    /** (0066,0031) VR=LO VM=1 Algorithm Version */
    public static final int AlgorithmVersion = 0x00660031;

    /** (0066,0032) VR=LT VM=1 Algorithm Parameters */
    public static final int AlgorithmParameters = 0x00660032;

    /** (0066,0034) VR=SQ VM=1 Facet Sequence */
    public static final int FacetSequence = 0x00660034;

    /** (0066,0035) VR=SQ VM=1 Surface Processing Algorithm Identification Sequence */
    public static final int SurfaceProcessingAlgorithmIdentificationSequence = 0x00660035;

    /** (0066,0036) VR=LO VM=1 Algorithm Name */
    public static final int AlgorithmName = 0x00660036;

    /** (0066,0037) VR=FL VM=1 Recommended Point Radius */
    public static final int RecommendedPointRadius = 0x00660037;

    /** (0066,0038) VR=FL VM=1 Recommended Line Thickness */
    public static final int RecommendedLineThickness = 0x00660038;

    /** (0066,0040) VR=OL VM=1 Long Primitive Point Index List */
    public static final int LongPrimitivePointIndexList = 0x00660040;

    /** (0066,0041) VR=OL VM=1 Long Triangle Point Index List */
    public static final int LongTrianglePointIndexList = 0x00660041;

    /** (0066,0042) VR=OL VM=1 Long Edge Point Index List */
    public static final int LongEdgePointIndexList = 0x00660042;

    /** (0066,0043) VR=OL VM=1 Long Vertex Point Index List */
    public static final int LongVertexPointIndexList = 0x00660043;

    /** (0066,0101) VR=SQ VM=1 Track Set Sequence */
    public static final int TrackSetSequence = 0x00660101;

    /** (0066,0102) VR=SQ VM=1 Track Sequence */
    public static final int TrackSequence = 0x00660102;

    /** (0066,0103) VR=OW VM=1 Recommended Display CIELab Value List */
    public static final int RecommendedDisplayCIELabValueList = 0x00660103;

    /** (0066,0104) VR=SQ VM=1 Tracking Algorithm Identification Sequence */
    public static final int TrackingAlgorithmIdentificationSequence = 0x00660104;

    /** (0066,0105) VR=UL VM=1 Track Set Number */
    public static final int TrackSetNumber = 0x00660105;

    /** (0066,0106) VR=LO VM=1 Track Set Label */
    public static final int TrackSetLabel = 0x00660106;

    /** (0066,0107) VR=UT VM=1 Track Set Description */
    public static final int TrackSetDescription = 0x00660107;

    /** (0066,0108) VR=SQ VM=1 Track Set Anatomical Type Code Sequence */
    public static final int TrackSetAnatomicalTypeCodeSequence = 0x00660108;

    /** (0066,0121) VR=SQ VM=1 Measurements Sequence */
    public static final int MeasurementsSequence = 0x00660121;

    /** (0066,0124) VR=SQ VM=1 Track Set Statistics Sequence */
    public static final int TrackSetStatisticsSequence = 0x00660124;

    /** (0066,0125) VR=OF VM=1 Floating Point Values */
    public static final int FloatingPointValues = 0x00660125;

    /** (0066,0129) VR=OL VM=1 Track Point Index List */
    public static final int TrackPointIndexList = 0x00660129;

    /** (0066,0130) VR=SQ VM=1 Track Statistics Sequence */
    public static final int TrackStatisticsSequence = 0x00660130;

    /** (0066,0132) VR=SQ VM=1 Measurement Values Sequence */
    public static final int MeasurementValuesSequence = 0x00660132;

    /** (0066,0133) VR=SQ VM=1 Diffusion Acquisition Code Sequence */
    public static final int DiffusionAcquisitionCodeSequence = 0x00660133;

    /** (0066,0134) VR=SQ VM=1 Diffusion Model Code Sequence */
    public static final int DiffusionModelCodeSequence = 0x00660134;

    /** (0068,6210) VR=LO VM=1 Implant Size */
    public static final int ImplantSize = 0x00686210;

    /** (0068,6221) VR=LO VM=1 Implant Template Version */
    public static final int ImplantTemplateVersion = 0x00686221;

    /** (0068,6222) VR=SQ VM=1 Replaced Implant Template Sequence */
    public static final int ReplacedImplantTemplateSequence = 0x00686222;

    /** (0068,6223) VR=CS VM=1 Implant Type */
    public static final int ImplantType = 0x00686223;

    /** (0068,6224) VR=SQ VM=1 Derivation Implant Template Sequence */
    public static final int DerivationImplantTemplateSequence = 0x00686224;

    /** (0068,6225) VR=SQ VM=1 Original Implant Template Sequence */
    public static final int OriginalImplantTemplateSequence = 0x00686225;

    /** (0068,6226) VR=DT VM=1 Effective DateTime */
    public static final int EffectiveDateTime = 0x00686226;

    /** (0068,6230) VR=SQ VM=1 Implant Target Anatomy Sequence */
    public static final int ImplantTargetAnatomySequence = 0x00686230;

    /** (0068,6260) VR=SQ VM=1 Information From Manufacturer Sequence */
    public static final int InformationFromManufacturerSequence = 0x00686260;

    /** (0068,6265) VR=SQ VM=1 Notification From Manufacturer Sequence */
    public static final int NotificationFromManufacturerSequence = 0x00686265;

    /** (0068,6270) VR=DT VM=1 Information Issue DateTime */
    public static final int InformationIssueDateTime = 0x00686270;

    /** (0068,6280) VR=ST VM=1 Information Summary */
    public static final int InformationSummary = 0x00686280;

    /** (0068,62A0) VR=SQ VM=1 Implant Regulatory Disapproval Code Sequence */
    public static final int ImplantRegulatoryDisapprovalCodeSequence = 0x006862A0;

    /** (0068,62A5) VR=FD VM=1 Overall Template Spatial Tolerance */
    public static final int OverallTemplateSpatialTolerance = 0x006862A5;

    /** (0068,62C0) VR=SQ VM=1 HPGL Document Sequence */
    public static final int HPGLDocumentSequence = 0x006862C0;

    /** (0068,62D0) VR=US VM=1 HPGL Document ID */
    public static final int HPGLDocumentID = 0x006862D0;

    /** (0068,62D5) VR=LO VM=1 HPGL Document Label */
    public static final int HPGLDocumentLabel = 0x006862D5;

    /** (0068,62E0) VR=SQ VM=1 View Orientation Code Sequence */
    public static final int ViewOrientationCodeSequence = 0x006862E0;

    /** (0068,62F0) VR=SQ VM=1 View Orientation Modifier Code Sequence */
    public static final int ViewOrientationModifierCodeSequence = 0x006862F0;

    /** (0068,62F2) VR=FD VM=1 HPGL Document Scaling */
    public static final int HPGLDocumentScaling = 0x006862F2;

    /** (0068,6300) VR=OB VM=1 HPGL Document */
    public static final int HPGLDocument = 0x00686300;

    /** (0068,6310) VR=US VM=1 HPGL Contour Pen Number */
    public static final int HPGLContourPenNumber = 0x00686310;

    /** (0068,6320) VR=SQ VM=1 HPGL Pen Sequence */
    public static final int HPGLPenSequence = 0x00686320;

    /** (0068,6330) VR=US VM=1 HPGL Pen Number */
    public static final int HPGLPenNumber = 0x00686330;

    /** (0068,6340) VR=LO VM=1 HPGL Pen Label */
    public static final int HPGLPenLabel = 0x00686340;

    /** (0068,6345) VR=ST VM=1 HPGL Pen Description */
    public static final int HPGLPenDescription = 0x00686345;

    /** (0068,6346) VR=FD VM=2 Recommended Rotation Point */
    public static final int RecommendedRotationPoint = 0x00686346;

    /** (0068,6347) VR=FD VM=4 Bounding Rectangle */
    public static final int BoundingRectangle = 0x00686347;

    /** (0068,6350) VR=US VM=1-n Implant Template 3D Model Surface Number */
    public static final int ImplantTemplate3DModelSurfaceNumber = 0x00686350;

    /** (0068,6360) VR=SQ VM=1 Surface Model Description Sequence */
    public static final int SurfaceModelDescriptionSequence = 0x00686360;

    /** (0068,6380) VR=LO VM=1 Surface Model Label */
    public static final int SurfaceModelLabel = 0x00686380;

    /** (0068,6390) VR=FD VM=1 Surface Model Scaling Factor */
    public static final int SurfaceModelScalingFactor = 0x00686390;

    /** (0068,63A0) VR=SQ VM=1 Materials Code Sequence */
    public static final int MaterialsCodeSequence = 0x006863A0;

    /** (0068,63A4) VR=SQ VM=1 Coating Materials Code Sequence */
    public static final int CoatingMaterialsCodeSequence = 0x006863A4;

    /** (0068,63A8) VR=SQ VM=1 Implant Type Code Sequence */
    public static final int ImplantTypeCodeSequence = 0x006863A8;

    /** (0068,63AC) VR=SQ VM=1 Fixation Method Code Sequence */
    public static final int FixationMethodCodeSequence = 0x006863AC;

    /** (0068,63B0) VR=SQ VM=1 Mating Feature Sets Sequence */
    public static final int MatingFeatureSetsSequence = 0x006863B0;

    /** (0068,63C0) VR=US VM=1 Mating Feature Set ID */
    public static final int MatingFeatureSetID = 0x006863C0;

    /** (0068,63D0) VR=LO VM=1 Mating Feature Set Label */
    public static final int MatingFeatureSetLabel = 0x006863D0;

    /** (0068,63E0) VR=SQ VM=1 Mating Feature Sequence */
    public static final int MatingFeatureSequence = 0x006863E0;

    /** (0068,63F0) VR=US VM=1 Mating Feature ID */
    public static final int MatingFeatureID = 0x006863F0;

    /** (0068,6400) VR=SQ VM=1 Mating Feature Degree of Freedom Sequence */
    public static final int MatingFeatureDegreeOfFreedomSequence = 0x00686400;

    /** (0068,6410) VR=US VM=1 Degree of Freedom ID */
    public static final int DegreeOfFreedomID = 0x00686410;

    /** (0068,6420) VR=CS VM=1 Degree of Freedom Type */
    public static final int DegreeOfFreedomType = 0x00686420;

    /** (0068,6430) VR=SQ VM=1 2D Mating Feature Coordinates Sequence */
    public static final int TwoDMatingFeatureCoordinatesSequence = 0x00686430;

    /** (0068,6440) VR=US VM=1 Referenced HPGL Document ID */
    public static final int ReferencedHPGLDocumentID = 0x00686440;

    /** (0068,6450) VR=FD VM=2 2D Mating Point */
    public static final int TwoDMatingPoint = 0x00686450;

    /** (0068,6460) VR=FD VM=4 2D Mating Axes */
    public static final int TwoDMatingAxes = 0x00686460;

    /** (0068,6470) VR=SQ VM=1 2D Degree of Freedom Sequence */
    public static final int TwoDDegreeOfFreedomSequence = 0x00686470;

    /** (0068,6490) VR=FD VM=3 3D Degree of Freedom Axis */
    public static final int ThreeDDegreeOfFreedomAxis = 0x00686490;

    /** (0068,64A0) VR=FD VM=2 Range of Freedom */
    public static final int RangeOfFreedom = 0x006864A0;

    /** (0068,64C0) VR=FD VM=3 3D Mating Point */
    public static final int ThreeDMatingPoint = 0x006864C0;

    /** (0068,64D0) VR=FD VM=9 3D Mating Axes */
    public static final int ThreeDMatingAxes = 0x006864D0;

    /** (0068,64F0) VR=FD VM=3 2D Degree of Freedom Axis */
    public static final int TwoDDegreeOfFreedomAxis = 0x006864F0;

    /** (0068,6500) VR=SQ VM=1 Planning Landmark Point Sequence */
    public static final int PlanningLandmarkPointSequence = 0x00686500;

    /** (0068,6510) VR=SQ VM=1 Planning Landmark Line Sequence */
    public static final int PlanningLandmarkLineSequence = 0x00686510;

    /** (0068,6520) VR=SQ VM=1 Planning Landmark Plane Sequence */
    public static final int PlanningLandmarkPlaneSequence = 0x00686520;

    /** (0068,6530) VR=US VM=1 Planning Landmark ID */
    public static final int PlanningLandmarkID = 0x00686530;

    /** (0068,6540) VR=LO VM=1 Planning Landmark Description */
    public static final int PlanningLandmarkDescription = 0x00686540;

    /** (0068,6545) VR=SQ VM=1 Planning Landmark Identification Code Sequence */
    public static final int PlanningLandmarkIdentificationCodeSequence = 0x00686545;

    /** (0068,6550) VR=SQ VM=1 2D Point Coordinates Sequence */
    public static final int TwoDPointCoordinatesSequence = 0x00686550;

    /** (0068,6560) VR=FD VM=2 2D Point Coordinates */
    public static final int TwoDPointCoordinates = 0x00686560;

    /** (0068,6590) VR=FD VM=3 3D Point Coordinates */
    public static final int ThreeDPointCoordinates = 0x00686590;

    /** (0068,65A0) VR=SQ VM=1 2D Line Coordinates Sequence */
    public static final int TwoDLineCoordinatesSequence = 0x006865A0;

    /** (0068,65B0) VR=FD VM=4 2D Line Coordinates */
    public static final int TwoDLineCoordinates = 0x006865B0;

    /** (0068,65D0) VR=FD VM=6 3D Line Coordinates */
    public static final int ThreeDLineCoordinates = 0x006865D0;

    /** (0068,65E0) VR=SQ VM=1 2D Plane Coordinates Sequence */
    public static final int TwoDPlaneCoordinatesSequence = 0x006865E0;

    /** (0068,65F0) VR=FD VM=4 2D Plane Intersection */
    public static final int TwoDPlaneIntersection = 0x006865F0;

    /** (0068,6610) VR=FD VM=3 3D Plane Origin */
    public static final int ThreeDPlaneOrigin = 0x00686610;

    /** (0068,6620) VR=FD VM=3 3D Plane Normal */
    public static final int ThreeDPlaneNormal = 0x00686620;

    /** (0068,7001) VR=CS VM=1 Model Modification */
    public static final int ModelModification = 0x00687001;

    /** (0068,7002) VR=CS VM=1 Model Mirroring */
    public static final int ModelMirroring = 0x00687002;

    /** (0068,7003) VR=SQ VM=1 Model Usage Code Sequence */
    public static final int ModelUsageCodeSequence = 0x00687003;

    /** (0068,7004) VR=UI VM=1 Model Group UID */
    public static final int ModelGroupUID = 0x00687004;

    /** (0068,7005) VR=UR VM=1 Relative URI Reference Within Encapsulated Document */
    public static final int RelativeURIReferenceWithinEncapsulatedDocument = 0x00687005;

    /** (0070,0001) VR=SQ VM=1 Graphic Annotation Sequence */
    public static final int GraphicAnnotationSequence = 0x00700001;

    /** (0070,0002) VR=CS VM=1 Graphic Layer */
    public static final int GraphicLayer = 0x00700002;

    /** (0070,0003) VR=CS VM=1 Bounding Box Annotation Units */
    public static final int BoundingBoxAnnotationUnits = 0x00700003;

    /** (0070,0004) VR=CS VM=1 Anchor Point Annotation Units */
    public static final int AnchorPointAnnotationUnits = 0x00700004;

    /** (0070,0005) VR=CS VM=1 Graphic Annotation Units */
    public static final int GraphicAnnotationUnits = 0x00700005;

    /** (0070,0006) VR=ST VM=1 Unformatted Text Value */
    public static final int UnformattedTextValue = 0x00700006;

    /** (0070,0008) VR=SQ VM=1 Text Object Sequence */
    public static final int TextObjectSequence = 0x00700008;

    /** (0070,0009) VR=SQ VM=1 Graphic Object Sequence */
    public static final int GraphicObjectSequence = 0x00700009;

    /** (0070,0010) VR=FL VM=2 Bounding Box Top Left Hand Corner */
    public static final int BoundingBoxTopLeftHandCorner = 0x00700010;

    /** (0070,0011) VR=FL VM=2 Bounding Box Bottom Right Hand Corner */
    public static final int BoundingBoxBottomRightHandCorner = 0x00700011;

    /** (0070,0012) VR=CS VM=1 Bounding Box Text Horizontal Justification */
    public static final int BoundingBoxTextHorizontalJustification = 0x00700012;

    /** (0070,0014) VR=FL VM=2 Anchor Point */
    public static final int AnchorPoint = 0x00700014;

    /** (0070,0015) VR=CS VM=1 Anchor Point Visibility */
    public static final int AnchorPointVisibility = 0x00700015;

    /** (0070,0020) VR=US VM=1 Graphic Dimensions */
    public static final int GraphicDimensions = 0x00700020;

    /** (0070,0021) VR=US VM=1 Number of Graphic Points */
    public static final int NumberOfGraphicPoints = 0x00700021;

    /** (0070,0022) VR=FL VM=2-n Graphic Data */
    public static final int GraphicData = 0x00700022;

    /** (0070,0023) VR=CS VM=1 Graphic Type */
    public static final int GraphicType = 0x00700023;

    /** (0070,0024) VR=CS VM=1 Graphic Filled */
    public static final int GraphicFilled = 0x00700024;

    /** (0070,0040) VR=IS VM=1 Image Rotation (Retired) (retired) */
    public static final int ImageRotationRetired = 0x00700040;

    /** (0070,0041) VR=CS VM=1 Image Horizontal Flip */
    public static final int ImageHorizontalFlip = 0x00700041;

    /** (0070,0042) VR=US VM=1 Image Rotation */
    public static final int ImageRotation = 0x00700042;

    /** (0070,0050) VR=US VM=2 Displayed Area Top Left Hand Corner (Trial) (retired) */
    public static final int DisplayedAreaTopLeftHandCornerTrial = 0x00700050;

    /** (0070,0051) VR=US VM=2 Displayed Area Bottom Right Hand Corner (Trial) (retired) */
    public static final int DisplayedAreaBottomRightHandCornerTrial = 0x00700051;

    /** (0070,0052) VR=SL VM=2 Displayed Area Top Left Hand Corner */
    public static final int DisplayedAreaTopLeftHandCorner = 0x00700052;

    /** (0070,0053) VR=SL VM=2 Displayed Area Bottom Right Hand Corner */
    public static final int DisplayedAreaBottomRightHandCorner = 0x00700053;

    /** (0070,005A) VR=SQ VM=1 Displayed Area Selection Sequence */
    public static final int DisplayedAreaSelectionSequence = 0x0070005A;

    /** (0070,0060) VR=SQ VM=1 Graphic Layer Sequence */
    public static final int GraphicLayerSequence = 0x00700060;

    /** (0070,0062) VR=IS VM=1 Graphic Layer Order */
    public static final int GraphicLayerOrder = 0x00700062;

    /** (0070,0066) VR=US VM=1 Graphic Layer Recommended Display Grayscale Value */
    public static final int GraphicLayerRecommendedDisplayGrayscaleValue = 0x00700066;

    /** (0070,0067) VR=US VM=3 Graphic Layer Recommended Display RGB Value (retired) */
    public static final int GraphicLayerRecommendedDisplayRGBValue = 0x00700067;

    /** (0070,0068) VR=LO VM=1 Graphic Layer Description */
    public static final int GraphicLayerDescription = 0x00700068;

    /** (0070,0080) VR=CS VM=1 Content Label */
    public static final int ContentLabel = 0x00700080;

    /** (0070,0081) VR=LO VM=1 Content Description */
    public static final int ContentDescription = 0x00700081;

    /** (0070,0082) VR=DA VM=1 Presentation Creation Date */
    public static final int PresentationCreationDate = 0x00700082;

    /** (0070,0083) VR=TM VM=1 Presentation Creation Time */
    public static final int PresentationCreationTime = 0x00700083;

    /** (0070,0084) VR=PN VM=1 Content Creator's Name */
    public static final int ContentCreatorName = 0x00700084;

    /** (0070,0086) VR=SQ VM=1 Content Creator's Identification Code Sequence */
    public static final int ContentCreatorIdentificationCodeSequence = 0x00700086;

    /** (0070,0087) VR=SQ VM=1 Alternate Content Description Sequence */
    public static final int AlternateContentDescriptionSequence = 0x00700087;

    /** (0070,0100) VR=CS VM=1 Presentation Size Mode */
    public static final int PresentationSizeMode = 0x00700100;

    /** (0070,0101) VR=DS VM=2 Presentation Pixel Spacing */
    public static final int PresentationPixelSpacing = 0x00700101;

    /** (0070,0102) VR=IS VM=2 Presentation Pixel Aspect Ratio */
    public static final int PresentationPixelAspectRatio = 0x00700102;

    /** (0070,0103) VR=FL VM=1 Presentation Pixel Magnification Ratio */
    public static final int PresentationPixelMagnificationRatio = 0x00700103;

    /** (0070,0207) VR=LO VM=1 Graphic Group Label */
    public static final int GraphicGroupLabel = 0x00700207;

    /** (0070,0208) VR=ST VM=1 Graphic Group Description */
    public static final int GraphicGroupDescription = 0x00700208;

    /** (0070,0209) VR=SQ VM=1 Compound Graphic Sequence */
    public static final int CompoundGraphicSequence = 0x00700209;

    /** (0070,0226) VR=UL VM=1 Compound Graphic Instance ID */
    public static final int CompoundGraphicInstanceID = 0x00700226;

    /** (0070,0227) VR=LO VM=1 Font Name */
    public static final int FontName = 0x00700227;

    /** (0070,0228) VR=CS VM=1 Font Name Type */
    public static final int FontNameType = 0x00700228;

    /** (0070,0229) VR=LO VM=1 CSS Font Name */
    public static final int CSSFontName = 0x00700229;

    /** (0070,0230) VR=FD VM=1 Rotation Angle */
    public static final int RotationAngle = 0x00700230;

    /** (0070,0231) VR=SQ VM=1 Text Style Sequence */
    public static final int TextStyleSequence = 0x00700231;

    /** (0070,0232) VR=SQ VM=1 Line Style Sequence */
    public static final int LineStyleSequence = 0x00700232;

    /** (0070,0233) VR=SQ VM=1 Fill Style Sequence */
    public static final int FillStyleSequence = 0x00700233;

    /** (0070,0234) VR=SQ VM=1 Graphic Group Sequence */
    public static final int GraphicGroupSequence = 0x00700234;

    /** (0070,0241) VR=US VM=3 Text Color CIELab Value */
    public static final int TextColorCIELabValue = 0x00700241;

    /** (0070,0242) VR=CS VM=1 Horizontal Alignment */
    public static final int HorizontalAlignment = 0x00700242;

    /** (0070,0243) VR=CS VM=1 Vertical Alignment */
    public static final int VerticalAlignment = 0x00700243;

    /** (0070,0244) VR=CS VM=1 Shadow Style */
    public static final int ShadowStyle = 0x00700244;

    /** (0070,0245) VR=FL VM=1 Shadow Offset X */
    public static final int ShadowOffsetX = 0x00700245;

    /** (0070,0246) VR=FL VM=1 Shadow Offset Y */
    public static final int ShadowOffsetY = 0x00700246;

    /** (0070,0247) VR=US VM=3 Shadow Color CIELab Value */
    public static final int ShadowColorCIELabValue = 0x00700247;

    /** (0070,0248) VR=CS VM=1 Underlined */
    public static final int Underlined = 0x00700248;

    /** (0070,0249) VR=CS VM=1 Bold */
    public static final int Bold = 0x00700249;

    /** (0070,0250) VR=CS VM=1 Italic */
    public static final int Italic = 0x00700250;

    /** (0070,0251) VR=US VM=3 Pattern On Color CIELab Value */
    public static final int PatternOnColorCIELabValue = 0x00700251;

    /** (0070,0252) VR=US VM=3 Pattern Off Color CIELab Value */
    public static final int PatternOffColorCIELabValue = 0x00700252;

    /** (0070,0253) VR=FL VM=1 Line Thickness */
    public static final int LineThickness = 0x00700253;

    /** (0070,0254) VR=CS VM=1 Line Dashing Style */
    public static final int LineDashingStyle = 0x00700254;

    /** (0070,0255) VR=UL VM=1 Line Pattern */
    public static final int LinePattern = 0x00700255;

    /** (0070,0256) VR=OB VM=1 Fill Pattern */
    public static final int FillPattern = 0x00700256;

    /** (0070,0257) VR=CS VM=1 Fill Mode */
    public static final int FillMode = 0x00700257;

    /** (0070,0258) VR=FL VM=1 Shadow Opacity */
    public static final int ShadowOpacity = 0x00700258;

    /** (0070,0261) VR=FL VM=1 Gap Length */
    public static final int GapLength = 0x00700261;

    /** (0070,0262) VR=FL VM=1 Diameter of Visibility */
    public static final int DiameterOfVisibility = 0x00700262;

    /** (0070,0273) VR=FL VM=2 Rotation Point */
    public static final int RotationPoint = 0x00700273;

    /** (0070,0274) VR=CS VM=1 Tick Alignment */
    public static final int TickAlignment = 0x00700274;

    /** (0070,0278) VR=CS VM=1 Show Tick Label */
    public static final int ShowTickLabel = 0x00700278;

    /** (0070,0279) VR=CS VM=1 Tick Label Alignment */
    public static final int TickLabelAlignment = 0x00700279;

    /** (0070,0282) VR=CS VM=1 Compound Graphic Units */
    public static final int CompoundGraphicUnits = 0x00700282;

    /** (0070,0284) VR=FL VM=1 Pattern On Opacity */
    public static final int PatternOnOpacity = 0x00700284;

    /** (0070,0285) VR=FL VM=1 Pattern Off Opacity */
    public static final int PatternOffOpacity = 0x00700285;

    /** (0070,0287) VR=SQ VM=1 Major Ticks Sequence */
    public static final int MajorTicksSequence = 0x00700287;

    /** (0070,0288) VR=FL VM=1 Tick Position */
    public static final int TickPosition = 0x00700288;

    /** (0070,0289) VR=SH VM=1 Tick Label */
    public static final int TickLabel = 0x00700289;

    /** (0070,0294) VR=CS VM=1 Compound Graphic Type */
    public static final int CompoundGraphicType = 0x00700294;

    /** (0070,0295) VR=UL VM=1 Graphic Group ID */
    public static final int GraphicGroupID = 0x00700295;

    /** (0070,0306) VR=CS VM=1 Shape Type */
    public static final int ShapeType = 0x00700306;

    /** (0070,0308) VR=SQ VM=1 Registration Sequence */
    public static final int RegistrationSequence = 0x00700308;

    /** (0070,0309) VR=SQ VM=1 Matrix Registration Sequence */
    public static final int MatrixRegistrationSequence = 0x00700309;

    /** (0070,030A) VR=SQ VM=1 Matrix Sequence */
    public static final int MatrixSequence = 0x0070030A;

    /** (0070,030B) VR=FD VM=16 Frame of Reference to Displayed Coordinate System Transformation Matrix */
    public static final int FrameOfReferenceToDisplayedCoordinateSystemTransformationMatrix = 0x0070030B;

    /** (0070,030C) VR=CS VM=1 Frame of Reference Transformation Matrix Type */
    public static final int FrameOfReferenceTransformationMatrixType = 0x0070030C;

    /** (0070,030D) VR=SQ VM=1 Registration Type Code Sequence */
    public static final int RegistrationTypeCodeSequence = 0x0070030D;

    /** (0070,030F) VR=ST VM=1 Fiducial Description */
    public static final int FiducialDescription = 0x0070030F;

    /** (0070,0310) VR=SH VM=1 Fiducial Identifier */
    public static final int FiducialIdentifier = 0x00700310;

    /** (0070,0311) VR=SQ VM=1 Fiducial Identifier Code Sequence */
    public static final int FiducialIdentifierCodeSequence = 0x00700311;

    /** (0070,0312) VR=FD VM=1 Contour Uncertainty Radius */
    public static final int ContourUncertaintyRadius = 0x00700312;

    /** (0070,0314) VR=SQ VM=1 Used Fiducials Sequence */
    public static final int UsedFiducialsSequence = 0x00700314;

    /** (0070,0318) VR=SQ VM=1 Graphic Coordinates Data Sequence */
    public static final int GraphicCoordinatesDataSequence = 0x00700318;

    /** (0070,031A) VR=UI VM=1 Fiducial UID */
    public static final int FiducialUID = 0x0070031A;

    /** (0070,031B) VR=UI VM=1 Referenced Fiducial UID */
    public static final int ReferencedFiducialUID = 0x0070031B;

    /** (0070,031C) VR=SQ VM=1 Fiducial Set Sequence */
    public static final int FiducialSetSequence = 0x0070031C;

    /** (0070,031E) VR=SQ VM=1 Fiducial Sequence */
    public static final int FiducialSequence = 0x0070031E;

    /** (0070,031F) VR=SQ VM=1 Fiducials Property Category Code Sequence */
    public static final int FiducialsPropertyCategoryCodeSequence = 0x0070031F;

    /** (0070,0401) VR=US VM=3 Graphic Layer Recommended Display CIELab Value */
    public static final int GraphicLayerRecommendedDisplayCIELabValue = 0x00700401;

    /** (0070,0402) VR=SQ VM=1 Blending Sequence */
    public static final int BlendingSequence = 0x00700402;

    /** (0070,0403) VR=FL VM=1 Relative Opacity */
    public static final int RelativeOpacity = 0x00700403;

    /** (0070,0404) VR=SQ VM=1 Referenced Spatial Registration Sequence */
    public static final int ReferencedSpatialRegistrationSequence = 0x00700404;

    /** (0070,0405) VR=CS VM=1 Blending Position */
    public static final int BlendingPosition = 0x00700405;

    /** (0070,1101) VR=UI VM=1 Presentation Display Collection UID */
    public static final int PresentationDisplayCollectionUID = 0x00701101;

    /** (0070,1102) VR=UI VM=1 Presentation Sequence Collection UID */
    public static final int PresentationSequenceCollectionUID = 0x00701102;

    /** (0070,1103) VR=US VM=1 Presentation Sequence Position Index */
    public static final int PresentationSequencePositionIndex = 0x00701103;

    /** (0070,1104) VR=SQ VM=1 Rendered Image Reference Sequence */
    public static final int RenderedImageReferenceSequence = 0x00701104;

    /** (0070,1201) VR=SQ VM=1 Volumetric Presentation State Input Sequence */
    public static final int VolumetricPresentationStateInputSequence = 0x00701201;

    /** (0070,1202) VR=CS VM=1 Presentation Input Type */
    public static final int PresentationInputType = 0x00701202;

    /** (0070,1203) VR=US VM=1 Input Sequence Position Index */
    public static final int InputSequencePositionIndex = 0x00701203;

    /** (0070,1204) VR=CS VM=1 Crop */
    public static final int Crop = 0x00701204;

    /** (0070,1205) VR=US VM=1-n Cropping Specification Index */
    public static final int CroppingSpecificationIndex = 0x00701205;

    /** (0070,1206) VR=CS VM=1 Compositing Method (retired) */
    public static final int CompositingMethod = 0x00701206;

    /** (0070,1207) VR=US VM=1 Volumetric Presentation Input Number */
    public static final int VolumetricPresentationInputNumber = 0x00701207;

    /** (0070,1208) VR=CS VM=1 Image Volume Geometry */
    public static final int ImageVolumeGeometry = 0x00701208;

    /** (0070,1209) VR=UI VM=1 Volumetric Presentation Input Set UID */
    public static final int VolumetricPresentationInputSetUID = 0x00701209;

    /** (0070,120A) VR=SQ VM=1 Volumetric Presentation Input Set Sequence */
    public static final int VolumetricPresentationInputSetSequence = 0x0070120A;

    /** (0070,120B) VR=CS VM=1 Global Crop */
    public static final int GlobalCrop = 0x0070120B;

    /** (0070,120C) VR=US VM=1-n Global Cropping Specification Index */
    public static final int GlobalCroppingSpecificationIndex = 0x0070120C;

    /** (0070,120D) VR=CS VM=1 Rendering Method */
    public static final int RenderingMethod = 0x0070120D;

    /** (0070,1301) VR=SQ VM=1 Volume Cropping Sequence */
    public static final int VolumeCroppingSequence = 0x00701301;

    /** (0070,1302) VR=CS VM=1 Volume Cropping Method */
    public static final int VolumeCroppingMethod = 0x00701302;

    /** (0070,1303) VR=FD VM=6 Bounding Box Crop */
    public static final int BoundingBoxCrop = 0x00701303;

    /** (0070,1304) VR=SQ VM=1 Oblique Cropping Plane Sequence */
    public static final int ObliqueCroppingPlaneSequence = 0x00701304;

    /** (0070,1305) VR=FD VM=4 Plane */
    public static final int Plane = 0x00701305;

    /** (0070,1306) VR=FD VM=3 Plane Normal */
    public static final int PlaneNormal = 0x00701306;

    /** (0070,1309) VR=US VM=1 Cropping Specification Number */
    public static final int CroppingSpecificationNumber = 0x00701309;

    /** (0070,1501) VR=CS VM=1 Multi-Planar Reconstruction Style */
    public static final int MultiPlanarReconstructionStyle = 0x00701501;

    /** (0070,1502) VR=CS VM=1 MPR Thickness Type */
    public static final int MPRThicknessType = 0x00701502;

    /** (0070,1503) VR=FD VM=1 MPR Slab Thickness */
    public static final int MPRSlabThickness = 0x00701503;

    /** (0070,1505) VR=FD VM=3 MPR Top Left Hand Corner */
    public static final int MPRTopLeftHandCorner = 0x00701505;

    /** (0070,1507) VR=FD VM=3 MPR View Width Direction */
    public static final int MPRViewWidthDirection = 0x00701507;

    /** (0070,1508) VR=FD VM=1 MPR View Width */
    public static final int MPRViewWidth = 0x00701508;

    /** (0070,150C) VR=UL VM=1 Number of Volumetric Curve Points */
    public static final int NumberOfVolumetricCurvePoints = 0x0070150C;

    /** (0070,150D) VR=OD VM=1 Volumetric Curve Points */
    public static final int VolumetricCurvePoints = 0x0070150D;

    /** (0070,1511) VR=FD VM=3 MPR View Height Direction */
    public static final int MPRViewHeightDirection = 0x00701511;

    /** (0070,1512) VR=FD VM=1 MPR View Height */
    public static final int MPRViewHeight = 0x00701512;

    /** (0070,1602) VR=CS VM=1 Render Projection */
    public static final int RenderProjection = 0x00701602;

    /** (0070,1603) VR=FD VM=3 Viewpoint Position */
    public static final int ViewpointPosition = 0x00701603;

    /** (0070,1604) VR=FD VM=3 Viewpoint LookAt Point */
    public static final int ViewpointLookAtPoint = 0x00701604;

    /** (0070,1605) VR=FD VM=3 Viewpoint Up Direction */
    public static final int ViewpointUpDirection = 0x00701605;

    /** (0070,1606) VR=FD VM=6 Render Field of View */
    public static final int RenderFieldOfView = 0x00701606;

    /** (0070,1607) VR=FD VM=1 Sampling Step Size */
    public static final int SamplingStepSize = 0x00701607;

    /** (0070,1701) VR=CS VM=1 Shading Style */
    public static final int ShadingStyle = 0x00701701;

    /** (0070,1702) VR=FD VM=1 Ambient Reflection Intensity */
    public static final int AmbientReflectionIntensity = 0x00701702;

    /** (0070,1703) VR=FD VM=3 Light Direction */
    public static final int LightDirection = 0x00701703;

    /** (0070,1704) VR=FD VM=1 Diffuse Reflection Intensity */
    public static final int DiffuseReflectionIntensity = 0x00701704;

    /** (0070,1705) VR=FD VM=1 Specular Reflection Intensity */
    public static final int SpecularReflectionIntensity = 0x00701705;

    /** (0070,1706) VR=FD VM=1 Shininess */
    public static final int Shininess = 0x00701706;

    /** (0070,1801) VR=SQ VM=1 Presentation State Classification Component Sequence */
    public static final int PresentationStateClassificationComponentSequence = 0x00701801;

    /** (0070,1802) VR=CS VM=1 Component Type */
    public static final int ComponentType = 0x00701802;

    /** (0070,1803) VR=SQ VM=1 Component Input Sequence */
    public static final int ComponentInputSequence = 0x00701803;

    /** (0070,1804) VR=US VM=1 Volumetric Presentation Input Index */
    public static final int VolumetricPresentationInputIndex = 0x00701804;

    /** (0070,1805) VR=SQ VM=1 Presentation State Compositor Component Sequence */
    public static final int PresentationStateCompositorComponentSequence = 0x00701805;

    /** (0070,1806) VR=SQ VM=1 Weighting Transfer Function Sequence */
    public static final int WeightingTransferFunctionSequence = 0x00701806;

    /** (0070,1807) VR=US VM=3 Weighting Lookup Table Descriptor */
    public static final int WeightingLookupTableDescriptor = 0x00701807;

    /** (0070,1808) VR=OB VM=1 Weighting Lookup Table Data */
    public static final int WeightingLookupTableData = 0x00701808;

    /** (0070,1901) VR=SQ VM=1 Volumetric Annotation Sequence */
    public static final int VolumetricAnnotationSequence = 0x00701901;

    /** (0070,1903) VR=SQ VM=1 Referenced Structured Context Sequence */
    public static final int ReferencedStructuredContextSequence = 0x00701903;

    /** (0070,1904) VR=UI VM=1 Referenced Content Item */
    public static final int ReferencedContentItem = 0x00701904;

    /** (0070,1905) VR=SQ VM=1 Volumetric Presentation Input Annotation Sequence */
    public static final int VolumetricPresentationInputAnnotationSequence = 0x00701905;

    /** (0070,1907) VR=CS VM=1 Annotation Clipping */
    public static final int AnnotationClipping = 0x00701907;

    /** (0070,1A01) VR=CS VM=1 Presentation Animation Style */
    public static final int PresentationAnimationStyle = 0x00701A01;

    /** (0070,1A03) VR=FD VM=1 Recommended Animation Rate */
    public static final int RecommendedAnimationRate = 0x00701A03;

    /** (0070,1A04) VR=SQ VM=1 Animation Curve Sequence */
    public static final int AnimationCurveSequence = 0x00701A04;

    /** (0070,1A05) VR=FD VM=1 Animation Step Size */
    public static final int AnimationStepSize = 0x00701A05;

    /** (0070,1A06) VR=FD VM=1 Swivel Range */
    public static final int SwivelRange = 0x00701A06;

    /** (0070,1A07) VR=OD VM=1 Volumetric Curve Up Directions */
    public static final int VolumetricCurveUpDirections = 0x00701A07;

    /** (0070,1A08) VR=SQ VM=1 Volume Stream Sequence */
    public static final int VolumeStreamSequence = 0x00701A08;

    /** (0070,1A09) VR=LO VM=1 RGBA Transfer Function Description */
    public static final int RGBATransferFunctionDescription = 0x00701A09;

    /** (0070,1B01) VR=SQ VM=1 Advanced Blending Sequence */
    public static final int AdvancedBlendingSequence = 0x00701B01;

    /** (0070,1B02) VR=US VM=1 Blending Input Number */
    public static final int BlendingInputNumber = 0x00701B02;

    /** (0070,1B03) VR=SQ VM=1 Blending Display Input Sequence */
    public static final int BlendingDisplayInputSequence = 0x00701B03;

    /** (0070,1B04) VR=SQ VM=1 Blending Display Sequence */
    public static final int BlendingDisplaySequence = 0x00701B04;

    /** (0070,1B06) VR=CS VM=1 Blending Mode */
    public static final int BlendingMode = 0x00701B06;

    /** (0070,1B07) VR=CS VM=1 Time Series Blending */
    public static final int TimeSeriesBlending = 0x00701B07;

    /** (0070,1B08) VR=CS VM=1 Geometry for Display */
    public static final int GeometryForDisplay = 0x00701B08;

    /** (0070,1B11) VR=SQ VM=1 Threshold Sequence */
    public static final int ThresholdSequence = 0x00701B11;

    /** (0070,1B12) VR=SQ VM=1 Threshold Value Sequence */
    public static final int ThresholdValueSequence = 0x00701B12;

    /** (0070,1B13) VR=CS VM=1 Threshold Type */
    public static final int ThresholdType = 0x00701B13;

    /** (0070,1B14) VR=FD VM=1 Threshold Value */
    public static final int ThresholdValue = 0x00701B14;

    /** (0072,0002) VR=SH VM=1 Hanging Protocol Name */
    public static final int HangingProtocolName = 0x00720002;

    /** (0072,0004) VR=LO VM=1 Hanging Protocol Description */
    public static final int HangingProtocolDescription = 0x00720004;

    /** (0072,0006) VR=CS VM=1 Hanging Protocol Level */
    public static final int HangingProtocolLevel = 0x00720006;

    /** (0072,0008) VR=LO VM=1 Hanging Protocol Creator */
    public static final int HangingProtocolCreator = 0x00720008;

    /** (0072,000A) VR=DT VM=1 Hanging Protocol Creation DateTime */
    public static final int HangingProtocolCreationDateTime = 0x0072000A;

    /** (0072,000C) VR=SQ VM=1 Hanging Protocol Definition Sequence */
    public static final int HangingProtocolDefinitionSequence = 0x0072000C;

    /** (0072,000E) VR=SQ VM=1 Hanging Protocol User Identification Code Sequence */
    public static final int HangingProtocolUserIdentificationCodeSequence = 0x0072000E;

    /** (0072,0010) VR=LO VM=1 Hanging Protocol User Group Name */
    public static final int HangingProtocolUserGroupName = 0x00720010;

    /** (0072,0012) VR=SQ VM=1 Source Hanging Protocol Sequence */
    public static final int SourceHangingProtocolSequence = 0x00720012;

    /** (0072,0014) VR=US VM=1 Number of Priors Referenced */
    public static final int NumberOfPriorsReferenced = 0x00720014;

    /** (0072,0020) VR=SQ VM=1 Image Sets Sequence */
    public static final int ImageSetsSequence = 0x00720020;

    /** (0072,0022) VR=SQ VM=1 Image Set Selector Sequence */
    public static final int ImageSetSelectorSequence = 0x00720022;

    /** (0072,0024) VR=CS VM=1 Image Set Selector Usage Flag */
    public static final int ImageSetSelectorUsageFlag = 0x00720024;

    /** (0072,0026) VR=AT VM=1 Selector Attribute */
    public static final int SelectorAttribute = 0x00720026;

    /** (0072,0028) VR=US VM=1 Selector Value Number */
    public static final int SelectorValueNumber = 0x00720028;

    /** (0072,0030) VR=SQ VM=1 Time Based Image Sets Sequence */
    public static final int TimeBasedImageSetsSequence = 0x00720030;

    /** (0072,0032) VR=US VM=1 Image Set Number */
    public static final int ImageSetNumber = 0x00720032;

    /** (0072,0034) VR=CS VM=1 Image Set Selector Category */
    public static final int ImageSetSelectorCategory = 0x00720034;

    /** (0072,0038) VR=US VM=2 Relative Time */
    public static final int RelativeTime = 0x00720038;

    /** (0072,003A) VR=CS VM=1 Relative Time Units */
    public static final int RelativeTimeUnits = 0x0072003A;

    /** (0072,003C) VR=SS VM=2 Abstract Prior Value */
    public static final int AbstractPriorValue = 0x0072003C;

    /** (0072,003E) VR=SQ VM=1 Abstract Prior Code Sequence */
    public static final int AbstractPriorCodeSequence = 0x0072003E;

    /** (0072,0040) VR=LO VM=1 Image Set Label */
    public static final int ImageSetLabel = 0x00720040;

    /** (0072,0050) VR=CS VM=1 Selector Attribute VR */
    public static final int SelectorAttributeVR = 0x00720050;

    /** (0072,0052) VR=AT VM=1-n Selector Sequence Pointer */
    public static final int SelectorSequencePointer = 0x00720052;

    /** (0072,0054) VR=LO VM=1-n Selector Sequence Pointer Private Creator */
    public static final int SelectorSequencePointerPrivateCreator = 0x00720054;

    /** (0072,0056) VR=LO VM=1 Selector Attribute Private Creator */
    public static final int SelectorAttributePrivateCreator = 0x00720056;

    /** (0072,005E) VR=AE VM=1-n Selector AE Value */
    public static final int SelectorAEValue = 0x0072005E;

    /** (0072,005F) VR=AS VM=1-n Selector AS Value */
    public static final int SelectorASValue = 0x0072005F;

    /** (0072,0060) VR=AT VM=1-n Selector AT Value */
    public static final int SelectorATValue = 0x00720060;

    /** (0072,0061) VR=DA VM=1-n Selector DA Value */
    public static final int SelectorDAValue = 0x00720061;

    /** (0072,0062) VR=CS VM=1-n Selector CS Value */
    public static final int SelectorCSValue = 0x00720062;

    /** (0072,0063) VR=DT VM=1-n Selector DT Value */
    public static final int SelectorDTValue = 0x00720063;

    /** (0072,0064) VR=IS VM=1-n Selector IS Value */
    public static final int SelectorISValue = 0x00720064;

    /** (0072,0065) VR=OB VM=1 Selector OB Value */
    public static final int SelectorOBValue = 0x00720065;

    /** (0072,0066) VR=LO VM=1-n Selector LO Value */
    public static final int SelectorLOValue = 0x00720066;

    /** (0072,0067) VR=OF VM=1 Selector OF Value */
    public static final int SelectorOFValue = 0x00720067;

    /** (0072,0068) VR=LT VM=1 Selector LT Value */
    public static final int SelectorLTValue = 0x00720068;

    /** (0072,0069) VR=OW VM=1 Selector OW Value */
    public static final int SelectorOWValue = 0x00720069;

    /** (0072,006A) VR=PN VM=1-n Selector PN Value */
    public static final int SelectorPNValue = 0x0072006A;

    /** (0072,006B) VR=TM VM=1-n Selector TM Value */
    public static final int SelectorTMValue = 0x0072006B;

    /** (0072,006C) VR=SH VM=1-n Selector SH Value */
    public static final int SelectorSHValue = 0x0072006C;

    /** (0072,006D) VR=UN VM=1 Selector UN Value */
    public static final int SelectorUNValue = 0x0072006D;

    /** (0072,006E) VR=ST VM=1 Selector ST Value */
    public static final int SelectorSTValue = 0x0072006E;

    /** (0072,006F) VR=UC VM=1-n Selector UC Value */
    public static final int SelectorUCValue = 0x0072006F;

    /** (0072,0070) VR=UT VM=1 Selector UT Value */
    public static final int SelectorUTValue = 0x00720070;

    /** (0072,0071) VR=UR VM=1 Selector UR Value */
    public static final int SelectorURValue = 0x00720071;

    /** (0072,0072) VR=DS VM=1-n Selector DS Value */
    public static final int SelectorDSValue = 0x00720072;

    /** (0072,0073) VR=OD VM=1 Selector OD Value */
    public static final int SelectorODValue = 0x00720073;

    /** (0072,0074) VR=FD VM=1-n Selector FD Value */
    public static final int SelectorFDValue = 0x00720074;

    /** (0072,0075) VR=OL VM=1 Selector OL Value */
    public static final int SelectorOLValue = 0x00720075;

    /** (0072,0076) VR=FL VM=1-n Selector FL Value */
    public static final int SelectorFLValue = 0x00720076;

    /** (0072,0078) VR=UL VM=1-n Selector UL Value */
    public static final int SelectorULValue = 0x00720078;

    /** (0072,007A) VR=US VM=1-n Selector US Value */
    public static final int SelectorUSValue = 0x0072007A;

    /** (0072,007C) VR=SL VM=1-n Selector SL Value */
    public static final int SelectorSLValue = 0x0072007C;

    /** (0072,007E) VR=SS VM=1-n Selector SS Value */
    public static final int SelectorSSValue = 0x0072007E;

    /** (0072,007F) VR=UI VM=1-n Selector UI Value */
    public static final int SelectorUIValue = 0x0072007F;

    /** (0072,0080) VR=SQ VM=1 Selector Code Sequence Value */
    public static final int SelectorCodeSequenceValue = 0x00720080;

    /** (0072,0100) VR=US VM=1 Number of Screens */
    public static final int NumberOfScreens = 0x00720100;

    /** (0072,0102) VR=SQ VM=1 Nominal Screen Definition Sequence */
    public static final int NominalScreenDefinitionSequence = 0x00720102;

    /** (0072,0104) VR=US VM=1 Number of Vertical Pixels */
    public static final int NumberOfVerticalPixels = 0x00720104;

    /** (0072,0106) VR=US VM=1 Number of Horizontal Pixels */
    public static final int NumberOfHorizontalPixels = 0x00720106;

    /** (0072,0108) VR=FD VM=4 Display Environment Spatial Position */
    public static final int DisplayEnvironmentSpatialPosition = 0x00720108;

    /** (0072,010A) VR=US VM=1 Screen Minimum Grayscale Bit Depth */
    public static final int ScreenMinimumGrayscaleBitDepth = 0x0072010A;

    /** (0072,010C) VR=US VM=1 Screen Minimum Color Bit Depth */
    public static final int ScreenMinimumColorBitDepth = 0x0072010C;

    /** (0072,010E) VR=US VM=1 Application Maximum Repaint Time */
    public static final int ApplicationMaximumRepaintTime = 0x0072010E;

    /** (0072,0200) VR=SQ VM=1 Display Sets Sequence */
    public static final int DisplaySetsSequence = 0x00720200;

    /** (0072,0202) VR=US VM=1 Display Set Number */
    public static final int DisplaySetNumber = 0x00720202;

    /** (0072,0203) VR=LO VM=1 Display Set Label */
    public static final int DisplaySetLabel = 0x00720203;

    /** (0072,0204) VR=US VM=1 Display Set Presentation Group */
    public static final int DisplaySetPresentationGroup = 0x00720204;

    /** (0072,0206) VR=LO VM=1 Display Set Presentation Group Description */
    public static final int DisplaySetPresentationGroupDescription = 0x00720206;

    /** (0072,0208) VR=CS VM=1 Partial Data Display Handling */
    public static final int PartialDataDisplayHandling = 0x00720208;

    /** (0072,0210) VR=SQ VM=1 Synchronized Scrolling Sequence */
    public static final int SynchronizedScrollingSequence = 0x00720210;

    /** (0072,0212) VR=US VM=2-n Display Set Scrolling Group */
    public static final int DisplaySetScrollingGroup = 0x00720212;

    /** (0072,0214) VR=SQ VM=1 Navigation Indicator Sequence */
    public static final int NavigationIndicatorSequence = 0x00720214;

    /** (0072,0216) VR=US VM=1 Navigation Display Set */
    public static final int NavigationDisplaySet = 0x00720216;

    /** (0072,0218) VR=US VM=1-n Reference Display Sets */
    public static final int ReferenceDisplaySets = 0x00720218;

    /** (0072,0300) VR=SQ VM=1 Image Boxes Sequence */
    public static final int ImageBoxesSequence = 0x00720300;

    /** (0072,0302) VR=US VM=1 Image Box Number */
    public static final int ImageBoxNumber = 0x00720302;

    /** (0072,0304) VR=CS VM=1 Image Box Layout Type */
    public static final int ImageBoxLayoutType = 0x00720304;

    /** (0072,0306) VR=US VM=1 Image Box Tile Horizontal Dimension */
    public static final int ImageBoxTileHorizontalDimension = 0x00720306;

    /** (0072,0308) VR=US VM=1 Image Box Tile Vertical Dimension */
    public static final int ImageBoxTileVerticalDimension = 0x00720308;

    /** (0072,0310) VR=CS VM=1 Image Box Scroll Direction */
    public static final int ImageBoxScrollDirection = 0x00720310;

    /** (0072,0312) VR=CS VM=1 Image Box Small Scroll Type */
    public static final int ImageBoxSmallScrollType = 0x00720312;

    /** (0072,0314) VR=US VM=1 Image Box Small Scroll Amount */
    public static final int ImageBoxSmallScrollAmount = 0x00720314;

    /** (0072,0316) VR=CS VM=1 Image Box Large Scroll Type */
    public static final int ImageBoxLargeScrollType = 0x00720316;

    /** (0072,0318) VR=US VM=1 Image Box Large Scroll Amount */
    public static final int ImageBoxLargeScrollAmount = 0x00720318;

    /** (0072,0320) VR=US VM=1 Image Box Overlap Priority */
    public static final int ImageBoxOverlapPriority = 0x00720320;

    /** (0072,0330) VR=FD VM=1 Cine Relative to Real-Time */
    public static final int CineRelativeToRealTime = 0x00720330;

    /** (0072,0400) VR=SQ VM=1 Filter Operations Sequence */
    public static final int FilterOperationsSequence = 0x00720400;

    /** (0072,0402) VR=CS VM=1 Filter-by Category */
    public static final int FilterByCategory = 0x00720402;

    /** (0072,0404) VR=CS VM=1 Filter-by Attribute Presence */
    public static final int FilterByAttributePresence = 0x00720404;

    /** (0072,0406) VR=CS VM=1 Filter-by Operator */
    public static final int FilterByOperator = 0x00720406;

    /** (0072,0420) VR=US VM=3 Structured Display Background CIELab Value */
    public static final int StructuredDisplayBackgroundCIELabValue = 0x00720420;

    /** (0072,0421) VR=US VM=3 Empty Image Box CIELab Value */
    public static final int EmptyImageBoxCIELabValue = 0x00720421;

    /** (0072,0422) VR=SQ VM=1 Structured Display Image Box Sequence */
    public static final int StructuredDisplayImageBoxSequence = 0x00720422;

    /** (0072,0424) VR=SQ VM=1 Structured Display Text Box Sequence */
    public static final int StructuredDisplayTextBoxSequence = 0x00720424;

    /** (0072,0427) VR=SQ VM=1 Referenced First Frame Sequence */
    public static final int ReferencedFirstFrameSequence = 0x00720427;

    /** (0072,0430) VR=SQ VM=1 Image Box Synchronization Sequence */
    public static final int ImageBoxSynchronizationSequence = 0x00720430;

    /** (0072,0432) VR=US VM=2-n Synchronized Image Box List */
    public static final int SynchronizedImageBoxList = 0x00720432;

    /** (0072,0434) VR=CS VM=1 Type of Synchronization */
    public static final int TypeOfSynchronization = 0x00720434;

    /** (0072,0500) VR=CS VM=1 Blending Operation Type */
    public static final int BlendingOperationType = 0x00720500;

    /** (0072,0510) VR=CS VM=1 Reformatting Operation Type */
    public static final int ReformattingOperationType = 0x00720510;

    /** (0072,0512) VR=FD VM=1 Reformatting Thickness */
    public static final int ReformattingThickness = 0x00720512;

    /** (0072,0514) VR=FD VM=1 Reformatting Interval */
    public static final int ReformattingInterval = 0x00720514;

    /** (0072,0516) VR=CS VM=1 Reformatting Operation Initial View Direction */
    public static final int ReformattingOperationInitialViewDirection = 0x00720516;

    /** (0072,0520) VR=CS VM=1-n 3D Rendering Type */
    public static final int ThreeDRenderingType = 0x00720520;

    /** (0072,0600) VR=SQ VM=1 Sorting Operations Sequence */
    public static final int SortingOperationsSequence = 0x00720600;

    /** (0072,0602) VR=CS VM=1 Sort-by Category */
    public static final int SortByCategory = 0x00720602;

    /** (0072,0604) VR=CS VM=1 Sorting Direction */
    public static final int SortingDirection = 0x00720604;

    /** (0072,0700) VR=CS VM=2 Display Set Patient Orientation */
    public static final int DisplaySetPatientOrientation = 0x00720700;

    /** (0072,0702) VR=CS VM=1 VOI Type */
    public static final int VOIType = 0x00720702;

    /** (0072,0704) VR=CS VM=1 Pseudo-Color Type */
    public static final int PseudoColorType = 0x00720704;

    /** (0072,0705) VR=SQ VM=1 Pseudo-Color Palette Instance Reference Sequence */
    public static final int PseudoColorPaletteInstanceReferenceSequence = 0x00720705;

    /** (0072,0706) VR=CS VM=1 Show Grayscale Inverted */
    public static final int ShowGrayscaleInverted = 0x00720706;

    /** (0072,0710) VR=CS VM=1 Show Image True Size Flag */
    public static final int ShowImageTrueSizeFlag = 0x00720710;

    /** (0072,0712) VR=CS VM=1 Show Graphic Annotation Flag */
    public static final int ShowGraphicAnnotationFlag = 0x00720712;

    /** (0072,0714) VR=CS VM=1 Show Patient Demographics Flag */
    public static final int ShowPatientDemographicsFlag = 0x00720714;

    /** (0072,0716) VR=CS VM=1 Show Acquisition Techniques Flag */
    public static final int ShowAcquisitionTechniquesFlag = 0x00720716;

    /** (0072,0717) VR=CS VM=1 Display Set Horizontal Justification */
    public static final int DisplaySetHorizontalJustification = 0x00720717;

    /** (0072,0718) VR=CS VM=1 Display Set Vertical Justification */
    public static final int DisplaySetVerticalJustification = 0x00720718;

    /** (0074,0120) VR=FD VM=1 Continuation Start Meterset */
    public static final int ContinuationStartMeterset = 0x00740120;

    /** (0074,0121) VR=FD VM=1 Continuation End Meterset */
    public static final int ContinuationEndMeterset = 0x00740121;

    /** (0074,1000) VR=CS VM=1 Procedure Step State */
    public static final int ProcedureStepState = 0x00741000;

    /** (0074,1002) VR=SQ VM=1 Procedure Step Progress Information Sequence */
    public static final int ProcedureStepProgressInformationSequence = 0x00741002;

    /** (0074,1004) VR=DS VM=1 Procedure Step Progress */
    public static final int ProcedureStepProgress = 0x00741004;

    /** (0074,1006) VR=ST VM=1 Procedure Step Progress Description */
    public static final int ProcedureStepProgressDescription = 0x00741006;

    /** (0074,1007) VR=SQ VM=1 Procedure Step Progress Parameters Sequence */
    public static final int ProcedureStepProgressParametersSequence = 0x00741007;

    /** (0074,1008) VR=SQ VM=1 Procedure Step Communications URI Sequence */
    public static final int ProcedureStepCommunicationsURISequence = 0x00741008;

    /** (0074,100A) VR=UR VM=1 Contact URI */
    public static final int ContactURI = 0x0074100A;

    /** (0074,100C) VR=LO VM=1 Contact Display Name */
    public static final int ContactDisplayName = 0x0074100C;

    /** (0074,100E) VR=SQ VM=1 Procedure Step Discontinuation Reason Code Sequence */
    public static final int ProcedureStepDiscontinuationReasonCodeSequence = 0x0074100E;

    /** (0074,1020) VR=SQ VM=1 Beam Task Sequence */
    public static final int BeamTaskSequence = 0x00741020;

    /** (0074,1022) VR=CS VM=1 Beam Task Type */
    public static final int BeamTaskType = 0x00741022;

    /** (0074,1024) VR=IS VM=1 Beam Order Index (Trial) (retired) */
    public static final int BeamOrderIndexTrial = 0x00741024;

    /** (0074,1025) VR=CS VM=1 Autosequence Flag */
    public static final int AutosequenceFlag = 0x00741025;

    /** (0074,1026) VR=FD VM=1 Table Top Vertical Adjusted Position */
    public static final int TableTopVerticalAdjustedPosition = 0x00741026;

    /** (0074,1027) VR=FD VM=1 Table Top Longitudinal Adjusted Position */
    public static final int TableTopLongitudinalAdjustedPosition = 0x00741027;

    /** (0074,1028) VR=FD VM=1 Table Top Lateral Adjusted Position */
    public static final int TableTopLateralAdjustedPosition = 0x00741028;

    /** (0074,102A) VR=FD VM=1 Patient Support Adjusted Angle */
    public static final int PatientSupportAdjustedAngle = 0x0074102A;

    /** (0074,102B) VR=FD VM=1 Table Top Eccentric Adjusted Angle */
    public static final int TableTopEccentricAdjustedAngle = 0x0074102B;

    /** (0074,102C) VR=FD VM=1 Table Top Pitch Adjusted Angle */
    public static final int TableTopPitchAdjustedAngle = 0x0074102C;

    /** (0074,102D) VR=FD VM=1 Table Top Roll Adjusted Angle */
    public static final int TableTopRollAdjustedAngle = 0x0074102D;

    /** (0074,1030) VR=SQ VM=1 Delivery Verification Image Sequence */
    public static final int DeliveryVerificationImageSequence = 0x00741030;

    /** (0074,1032) VR=CS VM=1 Verification Image Timing */
    public static final int VerificationImageTiming = 0x00741032;

    /** (0074,1034) VR=CS VM=1 Double Exposure Flag */
    public static final int DoubleExposureFlag = 0x00741034;

    /** (0074,1036) VR=CS VM=1 Double Exposure Ordering */
    public static final int DoubleExposureOrdering = 0x00741036;

    /** (0074,1038) VR=DS VM=1 Double Exposure Meterset (Trial) (retired) */
    public static final int DoubleExposureMetersetTrial = 0x00741038;

    /** (0074,103A) VR=DS VM=4 Double Exposure Field Delta (Trial) (retired) */
    public static final int DoubleExposureFieldDeltaTrial = 0x0074103A;

    /** (0074,1040) VR=SQ VM=1 Related Reference RT Image Sequence */
    public static final int RelatedReferenceRTImageSequence = 0x00741040;

    /** (0074,1042) VR=SQ VM=1 General Machine Verification Sequence */
    public static final int GeneralMachineVerificationSequence = 0x00741042;

    /** (0074,1044) VR=SQ VM=1 Conventional Machine Verification Sequence */
    public static final int ConventionalMachineVerificationSequence = 0x00741044;

    /** (0074,1046) VR=SQ VM=1 Ion Machine Verification Sequence */
    public static final int IonMachineVerificationSequence = 0x00741046;

    /** (0074,1048) VR=SQ VM=1 Failed Attributes Sequence */
    public static final int FailedAttributesSequence = 0x00741048;

    /** (0074,104A) VR=SQ VM=1 Overridden Attributes Sequence */
    public static final int OverriddenAttributesSequence = 0x0074104A;

    /** (0074,104C) VR=SQ VM=1 Conventional Control Point Verification Sequence */
    public static final int ConventionalControlPointVerificationSequence = 0x0074104C;

    /** (0074,104E) VR=SQ VM=1 Ion Control Point Verification Sequence */
    public static final int IonControlPointVerificationSequence = 0x0074104E;

    /** (0074,1050) VR=SQ VM=1 Attribute Occurrence Sequence */
    public static final int AttributeOccurrenceSequence = 0x00741050;

    /** (0074,1052) VR=AT VM=1 Attribute Occurrence Pointer */
    public static final int AttributeOccurrencePointer = 0x00741052;

    /** (0074,1054) VR=UL VM=1 Attribute Item Selector */
    public static final int AttributeItemSelector = 0x00741054;

    /** (0074,1056) VR=LO VM=1 Attribute Occurrence Private Creator */
    public static final int AttributeOccurrencePrivateCreator = 0x00741056;

    /** (0074,1057) VR=IS VM=1-n Selector Sequence Pointer Items */
    public static final int SelectorSequencePointerItems = 0x00741057;

    /** (0074,1200) VR=CS VM=1 Scheduled Procedure Step Priority */
    public static final int ScheduledProcedureStepPriority = 0x00741200;

    /** (0074,1202) VR=LO VM=1 Worklist Label */
    public static final int WorklistLabel = 0x00741202;

    /** (0074,1204) VR=LO VM=1 Procedure Step Label */
    public static final int ProcedureStepLabel = 0x00741204;

    /** (0074,1210) VR=SQ VM=1 Scheduled Processing Parameters Sequence */
    public static final int ScheduledProcessingParametersSequence = 0x00741210;

    /** (0074,1212) VR=SQ VM=1 Performed Processing Parameters Sequence */
    public static final int PerformedProcessingParametersSequence = 0x00741212;

    /** (0074,1216) VR=SQ VM=1 Unified Procedure Step Performed Procedure Sequence */
    public static final int UnifiedProcedureStepPerformedProcedureSequence = 0x00741216;

    /** (0074,1220) VR=SQ VM=1 Related Procedure Step Sequence (retired) */
    public static final int RelatedProcedureStepSequence = 0x00741220;

    /** (0074,1222) VR=LO VM=1 Procedure Step Relationship Type (retired) */
    public static final int ProcedureStepRelationshipType = 0x00741222;

    /** (0074,1224) VR=SQ VM=1 Replaced Procedure Step Sequence */
    public static final int ReplacedProcedureStepSequence = 0x00741224;

    /** (0074,1230) VR=LO VM=1 Deletion Lock */
    public static final int DeletionLock = 0x00741230;

    /** (0074,1234) VR=AE VM=1 Receiving AE */
    public static final int ReceivingAE = 0x00741234;

    /** (0074,1236) VR=AE VM=1 Requesting AE */
    public static final int RequestingAE = 0x00741236;

    /** (0074,1238) VR=LT VM=1 Reason for Cancellation */
    public static final int ReasonForCancellation = 0x00741238;

    /** (0074,1242) VR=CS VM=1 SCP Status */
    public static final int SCPStatus = 0x00741242;

    /** (0074,1244) VR=CS VM=1 Subscription List Status */
    public static final int SubscriptionListStatus = 0x00741244;

    /** (0074,1246) VR=CS VM=1 Unified Procedure Step List Status */
    public static final int UnifiedProcedureStepListStatus = 0x00741246;

    /** (0074,1324) VR=UL VM=1 Beam Order Index */
    public static final int BeamOrderIndex = 0x00741324;

    /** (0074,1338) VR=FD VM=1 Double Exposure Meterset */
    public static final int DoubleExposureMeterset = 0x00741338;

    /** (0074,133A) VR=FD VM=4 Double Exposure Field Delta */
    public static final int DoubleExposureFieldDelta = 0x0074133A;

    /** (0074,1401) VR=SQ VM=1 Brachy Task Sequence */
    public static final int BrachyTaskSequence = 0x00741401;

    /** (0074,1402) VR=DS VM=1 Continuation Start Total Reference Air Kerma */
    public static final int ContinuationStartTotalReferenceAirKerma = 0x00741402;

    /** (0074,1403) VR=DS VM=1 Continuation End Total Reference Air Kerma */
    public static final int ContinuationEndTotalReferenceAirKerma = 0x00741403;

    /** (0074,1404) VR=IS VM=1 Continuation Pulse Number */
    public static final int ContinuationPulseNumber = 0x00741404;

    /** (0074,1405) VR=SQ VM=1 Channel Delivery Order Sequence */
    public static final int ChannelDeliveryOrderSequence = 0x00741405;

    /** (0074,1406) VR=IS VM=1 Referenced Channel Number */
    public static final int ReferencedChannelNumber = 0x00741406;

    /** (0074,1407) VR=DS VM=1 Start Cumulative Time Weight */
    public static final int StartCumulativeTimeWeight = 0x00741407;

    /** (0074,1408) VR=DS VM=1 End Cumulative Time Weight */
    public static final int EndCumulativeTimeWeight = 0x00741408;

    /** (0074,1409) VR=SQ VM=1 Omitted Channel Sequence */
    public static final int OmittedChannelSequence = 0x00741409;

    /** (0074,140A) VR=CS VM=1 Reason for Channel Omission */
    public static final int ReasonForChannelOmission = 0x0074140A;

    /** (0074,140B) VR=LO VM=1 Reason for Channel Omission Description */
    public static final int ReasonForChannelOmissionDescription = 0x0074140B;

    /** (0074,140C) VR=IS VM=1 Channel Delivery Order Index */
    public static final int ChannelDeliveryOrderIndex = 0x0074140C;

    /** (0074,140D) VR=SQ VM=1 Channel Delivery Continuation Sequence */
    public static final int ChannelDeliveryContinuationSequence = 0x0074140D;

    /** (0074,140E) VR=SQ VM=1 Omitted Application Setup Sequence */
    public static final int OmittedApplicationSetupSequence = 0x0074140E;

    /** (0076,0001) VR=LO VM=1 Implant Assembly Template Name */
    public static final int ImplantAssemblyTemplateName = 0x00760001;

    /** (0076,0003) VR=LO VM=1 Implant Assembly Template Issuer */
    public static final int ImplantAssemblyTemplateIssuer = 0x00760003;

    /** (0076,0006) VR=LO VM=1 Implant Assembly Template Version */
    public static final int ImplantAssemblyTemplateVersion = 0x00760006;

    /** (0076,0008) VR=SQ VM=1 Replaced Implant Assembly Template Sequence */
    public static final int ReplacedImplantAssemblyTemplateSequence = 0x00760008;

    /** (0076,000A) VR=CS VM=1 Implant Assembly Template Type */
    public static final int ImplantAssemblyTemplateType = 0x0076000A;

    /** (0076,000C) VR=SQ VM=1 Original Implant Assembly Template Sequence */
    public static final int OriginalImplantAssemblyTemplateSequence = 0x0076000C;

    /** (0076,000E) VR=SQ VM=1 Derivation Implant Assembly Template Sequence */
    public static final int DerivationImplantAssemblyTemplateSequence = 0x0076000E;

    /** (0076,0010) VR=SQ VM=1 Implant Assembly Template Target Anatomy Sequence */
    public static final int ImplantAssemblyTemplateTargetAnatomySequence = 0x00760010;

    /** (0076,0020) VR=SQ VM=1 Procedure Type Code Sequence */
    public static final int ProcedureTypeCodeSequence = 0x00760020;

    /** (0076,0030) VR=LO VM=1 Surgical Technique */
    public static final int SurgicalTechnique = 0x00760030;

    /** (0076,0032) VR=SQ VM=1 Component Types Sequence */
    public static final int ComponentTypesSequence = 0x00760032;

    /** (0076,0034) VR=SQ VM=1 Component Type Code Sequence */
    public static final int ComponentTypeCodeSequence = 0x00760034;

    /** (0076,0036) VR=CS VM=1 Exclusive Component Type */
    public static final int ExclusiveComponentType = 0x00760036;

    /** (0076,0038) VR=CS VM=1 Mandatory Component Type */
    public static final int MandatoryComponentType = 0x00760038;

    /** (0076,0040) VR=SQ VM=1 Component Sequence */
    public static final int ComponentSequence = 0x00760040;

    /** (0076,0055) VR=US VM=1 Component ID */
    public static final int ComponentID = 0x00760055;

    /** (0076,0060) VR=SQ VM=1 Component Assembly Sequence */
    public static final int ComponentAssemblySequence = 0x00760060;

    /** (0076,0070) VR=US VM=1 Component 1 Referenced ID */
    public static final int Component1ReferencedID = 0x00760070;

    /** (0076,0080) VR=US VM=1 Component 1 Referenced Mating Feature Set ID */
    public static final int Component1ReferencedMatingFeatureSetID = 0x00760080;

    /** (0076,0090) VR=US VM=1 Component 1 Referenced Mating Feature ID */
    public static final int Component1ReferencedMatingFeatureID = 0x00760090;

    /** (0076,00A0) VR=US VM=1 Component 2 Referenced ID */
    public static final int Component2ReferencedID = 0x007600A0;

    /** (0076,00B0) VR=US VM=1 Component 2 Referenced Mating Feature Set ID */
    public static final int Component2ReferencedMatingFeatureSetID = 0x007600B0;

    /** (0076,00C0) VR=US VM=1 Component 2 Referenced Mating Feature ID */
    public static final int Component2ReferencedMatingFeatureID = 0x007600C0;

    /** (0078,0001) VR=LO VM=1 Implant Template Group Name */
    public static final int ImplantTemplateGroupName = 0x00780001;

    /** (0078,0010) VR=ST VM=1 Implant Template Group Description */
    public static final int ImplantTemplateGroupDescription = 0x00780010;

    /** (0078,0020) VR=LO VM=1 Implant Template Group Issuer */
    public static final int ImplantTemplateGroupIssuer = 0x00780020;

    /** (0078,0024) VR=LO VM=1 Implant Template Group Version */
    public static final int ImplantTemplateGroupVersion = 0x00780024;

    /** (0078,0026) VR=SQ VM=1 Replaced Implant Template Group Sequence */
    public static final int ReplacedImplantTemplateGroupSequence = 0x00780026;

    /** (0078,0028) VR=SQ VM=1 Implant Template Group Target Anatomy Sequence */
    public static final int ImplantTemplateGroupTargetAnatomySequence = 0x00780028;

    /** (0078,002A) VR=SQ VM=1 Implant Template Group Members Sequence */
    public static final int ImplantTemplateGroupMembersSequence = 0x0078002A;

    /** (0078,002E) VR=US VM=1 Implant Template Group Member ID */
    public static final int ImplantTemplateGroupMemberID = 0x0078002E;

    /** (0078,0050) VR=FD VM=3 3D Implant Template Group Member Matching Point */
    public static final int ThreeDImplantTemplateGroupMemberMatchingPoint = 0x00780050;

    /** (0078,0060) VR=FD VM=9 3D Implant Template Group Member Matching Axes */
    public static final int ThreeDImplantTemplateGroupMemberMatchingAxes = 0x00780060;

    /** (0078,0070) VR=SQ VM=1 Implant Template Group Member Matching 2D Coordinates Sequence */
    public static final int ImplantTemplateGroupMemberMatching2DCoordinatesSequence = 0x00780070;

    /** (0078,0090) VR=FD VM=2 2D Implant Template Group Member Matching Point */
    public static final int TwoDImplantTemplateGroupMemberMatchingPoint = 0x00780090;

    /** (0078,00A0) VR=FD VM=4 2D Implant Template Group Member Matching Axes */
    public static final int TwoDImplantTemplateGroupMemberMatchingAxes = 0x007800A0;

    /** (0078,00B0) VR=SQ VM=1 Implant Template Group Variation Dimension Sequence */
    public static final int ImplantTemplateGroupVariationDimensionSequence = 0x007800B0;

    /** (0078,00B2) VR=LO VM=1 Implant Template Group Variation Dimension Name */
    public static final int ImplantTemplateGroupVariationDimensionName = 0x007800B2;

    /** (0078,00B4) VR=SQ VM=1 Implant Template Group Variation Dimension Rank Sequence */
    public static final int ImplantTemplateGroupVariationDimensionRankSequence = 0x007800B4;

    /** (0078,00B6) VR=US VM=1 Referenced Implant Template Group Member ID */
    public static final int ReferencedImplantTemplateGroupMemberID = 0x007800B6;

    /** (0078,00B8) VR=US VM=1 Implant Template Group Variation Dimension Rank */
    public static final int ImplantTemplateGroupVariationDimensionRank = 0x007800B8;

    /** (0080,0001) VR=SQ VM=1 Surface Scan Acquisition Type Code Sequence */
    public static final int SurfaceScanAcquisitionTypeCodeSequence = 0x00800001;

    /** (0080,0002) VR=SQ VM=1 Surface Scan Mode Code Sequence */
    public static final int SurfaceScanModeCodeSequence = 0x00800002;

    /** (0080,0003) VR=SQ VM=1 Registration Method Code Sequence */
    public static final int RegistrationMethodCodeSequence = 0x00800003;

    /** (0080,0004) VR=FD VM=1 Shot Duration Time */
    public static final int ShotDurationTime = 0x00800004;

    /** (0080,0005) VR=FD VM=1 Shot Offset Time */
    public static final int ShotOffsetTime = 0x00800005;

    /** (0080,0006) VR=US VM=1-n Surface Point Presentation Value Data */
    public static final int SurfacePointPresentationValueData = 0x00800006;

    /** (0080,0007) VR=US VM=3-3n Surface Point Color CIELab Value Data */
    public static final int SurfacePointColorCIELabValueData = 0x00800007;

    /** (0080,0008) VR=SQ VM=1 UV Mapping Sequence */
    public static final int UVMappingSequence = 0x00800008;

    /** (0080,0009) VR=SH VM=1 Texture Label */
    public static final int TextureLabel = 0x00800009;

    /** (0080,0010) VR=OF VM=1 U Value Data */
    public static final int UValueData = 0x00800010;

    /** (0080,0011) VR=OF VM=1 V Value Data */
    public static final int VValueData = 0x00800011;

    /** (0080,0012) VR=SQ VM=1 Referenced Texture Sequence */
    public static final int ReferencedTextureSequence = 0x00800012;

    /** (0080,0013) VR=SQ VM=1 Referenced Surface Data Sequence */
    public static final int ReferencedSurfaceDataSequence = 0x00800013;

    /** (0082,0001) VR=CS VM=1 Assessment Summary */
    public static final int AssessmentSummary = 0x00820001;

    /** (0082,0003) VR=UT VM=1 Assessment Summary Description */
    public static final int AssessmentSummaryDescription = 0x00820003;

    /** (0082,0004) VR=SQ VM=1 Assessed SOP Instance Sequence */
    public static final int AssessedSOPInstanceSequence = 0x00820004;

    /** (0082,0005) VR=SQ VM=1 Referenced Comparison SOP Instance Sequence */
    public static final int ReferencedComparisonSOPInstanceSequence = 0x00820005;

    /** (0082,0006) VR=UL VM=1 Number of Assessment Observations */
    public static final int NumberOfAssessmentObservations = 0x00820006;

    /** (0082,0007) VR=SQ VM=1 Assessment Observations Sequence */
    public static final int AssessmentObservationsSequence = 0x00820007;

    /** (0082,0008) VR=CS VM=1 Observation Significance */
    public static final int ObservationSignificance = 0x00820008;

    /** (0082,000A) VR=UT VM=1 Observation Description */
    public static final int ObservationDescription = 0x0082000A;

    /** (0082,000C) VR=SQ VM=1 Structured Constraint Observation Sequence */
    public static final int StructuredConstraintObservationSequence = 0x0082000C;

    /** (0082,0010) VR=SQ VM=1 Assessed Attribute Value Sequence */
    public static final int AssessedAttributeValueSequence = 0x00820010;

    /** (0082,0016) VR=LO VM=1 Assessment Set ID */
    public static final int AssessmentSetID = 0x00820016;

    /** (0082,0017) VR=SQ VM=1 Assessment Requester Sequence */
    public static final int AssessmentRequesterSequence = 0x00820017;

    /** (0082,0018) VR=LO VM=1 Selector Attribute Name */
    public static final int SelectorAttributeName = 0x00820018;

    /** (0082,0019) VR=LO VM=1 Selector Attribute Keyword */
    public static final int SelectorAttributeKeyword = 0x00820019;

    /** (0082,0021) VR=SQ VM=1 Assessment Type Code Sequence */
    public static final int AssessmentTypeCodeSequence = 0x00820021;

    /** (0082,0022) VR=SQ VM=1 Observation Basis Code Sequence */
    public static final int ObservationBasisCodeSequence = 0x00820022;

    /** (0082,0023) VR=LO VM=1 Assessment Label */
    public static final int AssessmentLabel = 0x00820023;

    /** (0082,0032) VR=CS VM=1 Constraint Type */
    public static final int ConstraintType = 0x00820032;

    /** (0082,0033) VR=UT VM=1 Specification Selection Guidance */
    public static final int SpecificationSelectionGuidance = 0x00820033;

    /** (0082,0034) VR=SQ VM=1 Constraint Value Sequence */
    public static final int ConstraintValueSequence = 0x00820034;

    /** (0082,0035) VR=SQ VM=1 Recommended Default Value Sequence */
    public static final int RecommendedDefaultValueSequence = 0x00820035;

    /** (0082,0036) VR=CS VM=1 Constraint Violation Significance */
    public static final int ConstraintViolationSignificance = 0x00820036;

    /** (0082,0037) VR=UT VM=1 Constraint Violation Condition */
    public static final int ConstraintViolationCondition = 0x00820037;

    /** (0082,0038) VR=CS VM=1 Modifiable Constraint Flag */
    public static final int ModifiableConstraintFlag = 0x00820038;

    /** (0088,0130) VR=SH VM=1 Storage Media File-set ID */
    public static final int StorageMediaFileSetID = 0x00880130;

    /** (0088,0140) VR=UI VM=1 Storage Media File-set UID */
    public static final int StorageMediaFileSetUID = 0x00880140;

    /** (0088,0200) VR=SQ VM=1 Icon Image Sequence */
    public static final int IconImageSequence = 0x00880200;

    /** (0088,0904) VR=LO VM=1 Topic Title (retired) */
    public static final int TopicTitle = 0x00880904;

    /** (0088,0906) VR=ST VM=1 Topic Subject (retired) */
    public static final int TopicSubject = 0x00880906;

    /** (0088,0910) VR=LO VM=1 Topic Author (retired) */
    public static final int TopicAuthor = 0x00880910;

    /** (0088,0912) VR=LO VM=1-32 Topic Keywords (retired) */
    public static final int TopicKeywords = 0x00880912;

    /** (0100,0410) VR=CS VM=1 SOP Instance Status */
    public static final int SOPInstanceStatus = 0x01000410;

    /** (0100,0420) VR=DT VM=1 SOP Authorization DateTime */
    public static final int SOPAuthorizationDateTime = 0x01000420;

    /** (0100,0424) VR=LT VM=1 SOP Authorization Comment */
    public static final int SOPAuthorizationComment = 0x01000424;

    /** (0100,0426) VR=LO VM=1 Authorization Equipment Certification Number */
    public static final int AuthorizationEquipmentCertificationNumber = 0x01000426;

    /** (0400,0005) VR=US VM=1 MAC ID Number */
    public static final int MACIDNumber = 0x04000005;

    /** (0400,0010) VR=UI VM=1 MAC Calculation Transfer Syntax UID */
    public static final int MACCalculationTransferSyntaxUID = 0x04000010;

    /** (0400,0015) VR=CS VM=1 MAC Algorithm */
    public static final int MACAlgorithm = 0x04000015;

    /** (0400,0020) VR=AT VM=1-n Data Elements Signed */
    public static final int DataElementsSigned = 0x04000020;

    /** (0400,0100) VR=UI VM=1 Digital Signature UID */
    public static final int DigitalSignatureUID = 0x04000100;

    /** (0400,0105) VR=DT VM=1 Digital Signature DateTime */
    public static final int DigitalSignatureDateTime = 0x04000105;

    /** (0400,0110) VR=CS VM=1 Certificate Type */
    public static final int CertificateType = 0x04000110;

    /** (0400,0115) VR=OB VM=1 Certificate of Signer */
    public static final int CertificateOfSigner = 0x04000115;

    /** (0400,0120) VR=OB VM=1 Signature */
    public static final int Signature = 0x04000120;

    /** (0400,0305) VR=CS VM=1 Certified Timestamp Type */
    public static final int CertifiedTimestampType = 0x04000305;

    /** (0400,0310) VR=OB VM=1 Certified Timestamp */
    public static final int CertifiedTimestamp = 0x04000310;

    /** (0400,0401) VR=SQ VM=1 Digital Signature Purpose Code Sequence */
    public static final int DigitalSignaturePurposeCodeSequence = 0x04000401;

    /** (0400,0402) VR=SQ VM=1 Referenced Digital Signature Sequence */
    public static final int ReferencedDigitalSignatureSequence = 0x04000402;

    /** (0400,0403) VR=SQ VM=1 Referenced SOP Instance MAC Sequence */
    public static final int ReferencedSOPInstanceMACSequence = 0x04000403;

    /** (0400,0404) VR=OB VM=1 MAC */
    public static final int MAC = 0x04000404;

    /** (0400,0500) VR=SQ VM=1 Encrypted Attributes Sequence */
    public static final int EncryptedAttributesSequence = 0x04000500;

    /** (0400,0510) VR=UI VM=1 Encrypted Content Transfer Syntax UID */
    public static final int EncryptedContentTransferSyntaxUID = 0x04000510;

    /** (0400,0520) VR=OB VM=1 Encrypted Content */
    public static final int EncryptedContent = 0x04000520;

    /** (0400,0550) VR=SQ VM=1 Modified Attributes Sequence */
    public static final int ModifiedAttributesSequence = 0x04000550;

    /** (0400,0551) VR=SQ VM=1 Nonconforming Modified Attributes Sequence */
    public static final int NonconformingModifiedAttributesSequence = 0x04000551;

    /** (0400,0552) VR=OB VM=1 Nonconforming Data Element Value */
    public static final int NonconformingDataElementValue = 0x04000552;

    /** (0400,0561) VR=SQ VM=1 Original Attributes Sequence */
    public static final int OriginalAttributesSequence = 0x04000561;

    /** (0400,0562) VR=DT VM=1 Attribute Modification DateTime */
    public static final int AttributeModificationDateTime = 0x04000562;

    /** (0400,0563) VR=LO VM=1 Modifying System */
    public static final int ModifyingSystem = 0x04000563;

    /** (0400,0564) VR=LO VM=1 Source of Previous Values */
    public static final int SourceOfPreviousValues = 0x04000564;

    /** (0400,0565) VR=CS VM=1 Reason for the Attribute Modification */
    public static final int ReasonForTheAttributeModification = 0x04000565;

    /** (0400,0600) VR=CS VM=1 Instance Origin Status */
    public static final int InstanceOriginStatus = 0x04000600;

    /** (1000,xxx0) VR=US VM=3 Escape Triplet (retired) */
    public static final int EscapeTriplet = 0x10000000;

    /** (1000,xxx1) VR=US VM=3 Run Length Triplet (retired) */
    public static final int RunLengthTriplet = 0x10000001;

    /** (1000,xxx2) VR=US VM=1 Huffman Table Size (retired) */
    public static final int HuffmanTableSize = 0x10000002;

    /** (1000,xxx3) VR=US VM=3 Huffman Table Triplet (retired) */
    public static final int HuffmanTableTriplet = 0x10000003;

    /** (1000,xxx4) VR=US VM=1 Shift Table Size (retired) */
    public static final int ShiftTableSize = 0x10000004;

    /** (1000,xxx5) VR=US VM=3 Shift Table Triplet (retired) */
    public static final int ShiftTableTriplet = 0x10000005;

    /** (1010,xxxx) VR=US VM=1-n Zonal Map (retired) */
    public static final int ZonalMap = 0x10100000;

    /** (2000,0010) VR=IS VM=1 Number of Copies */
    public static final int NumberOfCopies = 0x20000010;

    /** (2000,001E) VR=SQ VM=1 Printer Configuration Sequence */
    public static final int PrinterConfigurationSequence = 0x2000001E;

    /** (2000,0020) VR=CS VM=1 Print Priority */
    public static final int PrintPriority = 0x20000020;

    /** (2000,0030) VR=CS VM=1 Medium Type */
    public static final int MediumType = 0x20000030;

    /** (2000,0040) VR=CS VM=1 Film Destination */
    public static final int FilmDestination = 0x20000040;

    /** (2000,0050) VR=LO VM=1 Film Session Label */
    public static final int FilmSessionLabel = 0x20000050;

    /** (2000,0060) VR=IS VM=1 Memory Allocation */
    public static final int MemoryAllocation = 0x20000060;

    /** (2000,0061) VR=IS VM=1 Maximum Memory Allocation */
    public static final int MaximumMemoryAllocation = 0x20000061;

    /** (2000,0062) VR=CS VM=1 Color Image Printing Flag (retired) */
    public static final int ColorImagePrintingFlag = 0x20000062;

    /** (2000,0063) VR=CS VM=1 Collation Flag (retired) */
    public static final int CollationFlag = 0x20000063;

    /** (2000,0065) VR=CS VM=1 Annotation Flag (retired) */
    public static final int AnnotationFlag = 0x20000065;

    /** (2000,0067) VR=CS VM=1 Image Overlay Flag (retired) */
    public static final int ImageOverlayFlag = 0x20000067;

    /** (2000,0069) VR=CS VM=1 Presentation LUT Flag (retired) */
    public static final int PresentationLUTFlag = 0x20000069;

    /** (2000,006A) VR=CS VM=1 Image Box Presentation LUT Flag (retired) */
    public static final int ImageBoxPresentationLUTFlag = 0x2000006A;

    /** (2000,00A0) VR=US VM=1 Memory Bit Depth */
    public static final int MemoryBitDepth = 0x200000A0;

    /** (2000,00A1) VR=US VM=1 Printing Bit Depth */
    public static final int PrintingBitDepth = 0x200000A1;

    /** (2000,00A2) VR=SQ VM=1 Media Installed Sequence */
    public static final int MediaInstalledSequence = 0x200000A2;

    /** (2000,00A4) VR=SQ VM=1 Other Media Available Sequence */
    public static final int OtherMediaAvailableSequence = 0x200000A4;

    /** (2000,00A8) VR=SQ VM=1 Supported Image Display Formats Sequence */
    public static final int SupportedImageDisplayFormatsSequence = 0x200000A8;

    /** (2000,0500) VR=SQ VM=1 Referenced Film Box Sequence */
    public static final int ReferencedFilmBoxSequence = 0x20000500;

    /** (2000,0510) VR=SQ VM=1 Referenced Stored Print Sequence (retired) */
    public static final int ReferencedStoredPrintSequence = 0x20000510;

    /** (2010,0010) VR=ST VM=1 Image Display Format */
    public static final int ImageDisplayFormat = 0x20100010;

    /** (2010,0030) VR=CS VM=1 Annotation Display Format ID */
    public static final int AnnotationDisplayFormatID = 0x20100030;

    /** (2010,0040) VR=CS VM=1 Film Orientation */
    public static final int FilmOrientation = 0x20100040;

    /** (2010,0050) VR=CS VM=1 Film Size ID */
    public static final int FilmSizeID = 0x20100050;

    /** (2010,0052) VR=CS VM=1 Printer Resolution ID */
    public static final int PrinterResolutionID = 0x20100052;

    /** (2010,0054) VR=CS VM=1 Default Printer Resolution ID */
    public static final int DefaultPrinterResolutionID = 0x20100054;

    /** (2010,0060) VR=CS VM=1 Magnification Type */
    public static final int MagnificationType = 0x20100060;

    /** (2010,0080) VR=CS VM=1 Smoothing Type */
    public static final int SmoothingType = 0x20100080;

    /** (2010,00A6) VR=CS VM=1 Default Magnification Type */
    public static final int DefaultMagnificationType = 0x201000A6;

    /** (2010,00A7) VR=CS VM=1-n Other Magnification Types Available */
    public static final int OtherMagnificationTypesAvailable = 0x201000A7;

    /** (2010,00A8) VR=CS VM=1 Default Smoothing Type */
    public static final int DefaultSmoothingType = 0x201000A8;

    /** (2010,00A9) VR=CS VM=1-n Other Smoothing Types Available */
    public static final int OtherSmoothingTypesAvailable = 0x201000A9;

    /** (2010,0100) VR=CS VM=1 Border Density */
    public static final int BorderDensity = 0x20100100;

    /** (2010,0110) VR=CS VM=1 Empty Image Density */
    public static final int EmptyImageDensity = 0x20100110;

    /** (2010,0120) VR=US VM=1 Min Density */
    public static final int MinDensity = 0x20100120;

    /** (2010,0130) VR=US VM=1 Max Density */
    public static final int MaxDensity = 0x20100130;

    /** (2010,0140) VR=CS VM=1 Trim */
    public static final int Trim = 0x20100140;

    /** (2010,0150) VR=ST VM=1 Configuration Information */
    public static final int ConfigurationInformation = 0x20100150;

    /** (2010,0152) VR=LT VM=1 Configuration Information Description */
    public static final int ConfigurationInformationDescription = 0x20100152;

    /** (2010,0154) VR=IS VM=1 Maximum Collated Films */
    public static final int MaximumCollatedFilms = 0x20100154;

    /** (2010,015E) VR=US VM=1 Illumination */
    public static final int Illumination = 0x2010015E;

    /** (2010,0160) VR=US VM=1 Reflected Ambient Light */
    public static final int ReflectedAmbientLight = 0x20100160;

    /** (2010,0376) VR=DS VM=2 Printer Pixel Spacing */
    public static final int PrinterPixelSpacing = 0x20100376;

    /** (2010,0500) VR=SQ VM=1 Referenced Film Session Sequence */
    public static final int ReferencedFilmSessionSequence = 0x20100500;

    /** (2010,0510) VR=SQ VM=1 Referenced Image Box Sequence */
    public static final int ReferencedImageBoxSequence = 0x20100510;

    /** (2010,0520) VR=SQ VM=1 Referenced Basic Annotation Box Sequence */
    public static final int ReferencedBasicAnnotationBoxSequence = 0x20100520;

    /** (2020,0010) VR=US VM=1 Image Box Position */
    public static final int ImageBoxPosition = 0x20200010;

    /** (2020,0020) VR=CS VM=1 Polarity */
    public static final int Polarity = 0x20200020;

    /** (2020,0030) VR=DS VM=1 Requested Image Size */
    public static final int RequestedImageSize = 0x20200030;

    /** (2020,0040) VR=CS VM=1 Requested Decimate/Crop Behavior */
    public static final int RequestedDecimateCropBehavior = 0x20200040;

    /** (2020,0050) VR=CS VM=1 Requested Resolution ID */
    public static final int RequestedResolutionID = 0x20200050;

    /** (2020,00A0) VR=CS VM=1 Requested Image Size Flag */
    public static final int RequestedImageSizeFlag = 0x202000A0;

    /** (2020,00A2) VR=CS VM=1 Decimate/Crop Result */
    public static final int DecimateCropResult = 0x202000A2;

    /** (2020,0110) VR=SQ VM=1 Basic Grayscale Image Sequence */
    public static final int BasicGrayscaleImageSequence = 0x20200110;

    /** (2020,0111) VR=SQ VM=1 Basic Color Image Sequence */
    public static final int BasicColorImageSequence = 0x20200111;

    /** (2020,0130) VR=SQ VM=1 Referenced Image Overlay Box Sequence (retired) */
    public static final int ReferencedImageOverlayBoxSequence = 0x20200130;

    /** (2020,0140) VR=SQ VM=1 Referenced VOI LUT Box Sequence (retired) */
    public static final int ReferencedVOILUTBoxSequence = 0x20200140;

    /** (2030,0010) VR=US VM=1 Annotation Position */
    public static final int AnnotationPosition = 0x20300010;

    /** (2030,0020) VR=LO VM=1 Text String */
    public static final int TextString = 0x20300020;

    /** (2040,0010) VR=SQ VM=1 Referenced Overlay Plane Sequence (retired) */
    public static final int ReferencedOverlayPlaneSequence = 0x20400010;

    /** (2040,0011) VR=US VM=1-99 Referenced Overlay Plane Groups (retired) */
    public static final int ReferencedOverlayPlaneGroups = 0x20400011;

    /** (2040,0020) VR=SQ VM=1 Overlay Pixel Data Sequence (retired) */
    public static final int OverlayPixelDataSequence = 0x20400020;

    /** (2040,0060) VR=CS VM=1 Overlay Magnification Type (retired) */
    public static final int OverlayMagnificationType = 0x20400060;

    /** (2040,0070) VR=CS VM=1 Overlay Smoothing Type (retired) */
    public static final int OverlaySmoothingType = 0x20400070;

    /** (2040,0072) VR=CS VM=1 Overlay or Image Magnification (retired) */
    public static final int OverlayOrImageMagnification = 0x20400072;

    /** (2040,0074) VR=US VM=1 Magnify to Number of Columns (retired) */
    public static final int MagnifyToNumberOfColumns = 0x20400074;

    /** (2040,0080) VR=CS VM=1 Overlay Foreground Density (retired) */
    public static final int OverlayForegroundDensity = 0x20400080;

    /** (2040,0082) VR=CS VM=1 Overlay Background Density (retired) */
    public static final int OverlayBackgroundDensity = 0x20400082;

    /** (2040,0090) VR=CS VM=1 Overlay Mode (retired) */
    public static final int OverlayMode = 0x20400090;

    /** (2040,0100) VR=CS VM=1 Threshold Density (retired) */
    public static final int ThresholdDensity = 0x20400100;

    /** (2040,0500) VR=SQ VM=1 Referenced Image Box Sequence (Retired) (retired) */
    public static final int ReferencedImageBoxSequenceRetired = 0x20400500;

    /** (2050,0010) VR=SQ VM=1 Presentation LUT Sequence */
    public static final int PresentationLUTSequence = 0x20500010;

    /** (2050,0020) VR=CS VM=1 Presentation LUT Shape */
    public static final int PresentationLUTShape = 0x20500020;

    /** (2050,0500) VR=SQ VM=1 Referenced Presentation LUT Sequence */
    public static final int ReferencedPresentationLUTSequence = 0x20500500;

    /** (2100,0010) VR=SH VM=1 Print Job ID (retired) */
    public static final int PrintJobID = 0x21000010;

    /** (2100,0020) VR=CS VM=1 Execution Status */
    public static final int ExecutionStatus = 0x21000020;

    /** (2100,0030) VR=CS VM=1 Execution Status Info */
    public static final int ExecutionStatusInfo = 0x21000030;

    /** (2100,0040) VR=DA VM=1 Creation Date */
    public static final int CreationDate = 0x21000040;

    /** (2100,0050) VR=TM VM=1 Creation Time */
    public static final int CreationTime = 0x21000050;

    /** (2100,0070) VR=AE VM=1 Originator */
    public static final int Originator = 0x21000070;

    /** (2100,0140) VR=AE VM=1 Destination AE */
    public static final int DestinationAE = 0x21000140;

    /** (2100,0160) VR=SH VM=1 Owner ID */
    public static final int OwnerID = 0x21000160;

    /** (2100,0170) VR=IS VM=1 Number of Films */
    public static final int NumberOfFilms = 0x21000170;

    /** (2100,0500) VR=SQ VM=1 Referenced Print Job Sequence (Pull Stored Print) (retired) */
    public static final int ReferencedPrintJobSequencePullStoredPrint = 0x21000500;

    /** (2110,0010) VR=CS VM=1 Printer Status */
    public static final int PrinterStatus = 0x21100010;

    /** (2110,0020) VR=CS VM=1 Printer Status Info */
    public static final int PrinterStatusInfo = 0x21100020;

    /** (2110,0030) VR=LO VM=1 Printer Name */
    public static final int PrinterName = 0x21100030;

    /** (2110,0099) VR=SH VM=1 Print Queue ID (retired) */
    public static final int PrintQueueID = 0x21100099;

    /** (2120,0010) VR=CS VM=1 Queue Status (retired) */
    public static final int QueueStatus = 0x21200010;

    /** (2120,0050) VR=SQ VM=1 Print Job Description Sequence (retired) */
    public static final int PrintJobDescriptionSequence = 0x21200050;

    /** (2120,0070) VR=SQ VM=1 Referenced Print Job Sequence (retired) */
    public static final int ReferencedPrintJobSequence = 0x21200070;

    /** (2130,0010) VR=SQ VM=1 Print Management Capabilities Sequence (retired) */
    public static final int PrintManagementCapabilitiesSequence = 0x21300010;

    /** (2130,0015) VR=SQ VM=1 Printer Characteristics Sequence (retired) */
    public static final int PrinterCharacteristicsSequence = 0x21300015;

    /** (2130,0030) VR=SQ VM=1 Film Box Content Sequence (retired) */
    public static final int FilmBoxContentSequence = 0x21300030;

    /** (2130,0040) VR=SQ VM=1 Image Box Content Sequence (retired) */
    public static final int ImageBoxContentSequence = 0x21300040;

    /** (2130,0050) VR=SQ VM=1 Annotation Content Sequence (retired) */
    public static final int AnnotationContentSequence = 0x21300050;

    /** (2130,0060) VR=SQ VM=1 Image Overlay Box Content Sequence (retired) */
    public static final int ImageOverlayBoxContentSequence = 0x21300060;

    /** (2130,0080) VR=SQ VM=1 Presentation LUT Content Sequence (retired) */
    public static final int PresentationLUTContentSequence = 0x21300080;

    /** (2130,00A0) VR=SQ VM=1 Proposed Study Sequence (retired) */
    public static final int ProposedStudySequence = 0x213000A0;

    /** (2130,00C0) VR=SQ VM=1 Original Image Sequence (retired) */
    public static final int OriginalImageSequence = 0x213000C0;

    /** (2200,0001) VR=CS VM=1 Label Using Information Extracted From Instances */
    public static final int LabelUsingInformationExtractedFromInstances = 0x22000001;

    /** (2200,0002) VR=UT VM=1 Label Text */
    public static final int LabelText = 0x22000002;

    /** (2200,0003) VR=CS VM=1 Label Style Selection */
    public static final int LabelStyleSelection = 0x22000003;

    /** (2200,0004) VR=LT VM=1 Media Disposition */
    public static final int MediaDisposition = 0x22000004;

    /** (2200,0005) VR=LT VM=1 Barcode Value */
    public static final int BarcodeValue = 0x22000005;

    /** (2200,0006) VR=CS VM=1 Barcode Symbology */
    public static final int BarcodeSymbology = 0x22000006;

    /** (2200,0007) VR=CS VM=1 Allow Media Splitting */
    public static final int AllowMediaSplitting = 0x22000007;

    /** (2200,0008) VR=CS VM=1 Include Non-DICOM Objects */
    public static final int IncludeNonDICOMObjects = 0x22000008;

    /** (2200,0009) VR=CS VM=1 Include Display Application */
    public static final int IncludeDisplayApplication = 0x22000009;

    /** (2200,000A) VR=CS VM=1 Preserve Composite Instances After Media Creation */
    public static final int PreserveCompositeInstancesAfterMediaCreation = 0x2200000A;

    /** (2200,000B) VR=US VM=1 Total Number of Pieces of Media Created */
    public static final int TotalNumberOfPiecesOfMediaCreated = 0x2200000B;

    /** (2200,000C) VR=LO VM=1 Requested Media Application Profile */
    public static final int RequestedMediaApplicationProfile = 0x2200000C;

    /** (2200,000D) VR=SQ VM=1 Referenced Storage Media Sequence */
    public static final int ReferencedStorageMediaSequence = 0x2200000D;

    /** (2200,000E) VR=AT VM=1-n Failure Attributes */
    public static final int FailureAttributes = 0x2200000E;

    /** (2200,000F) VR=CS VM=1 Allow Lossy Compression */
    public static final int AllowLossyCompression = 0x2200000F;

    /** (2200,0020) VR=CS VM=1 Request Priority */
    public static final int RequestPriority = 0x22000020;

    /** (3002,0002) VR=SH VM=1 RT Image Label */
    public static final int RTImageLabel = 0x30020002;

    /** (3002,0003) VR=LO VM=1 RT Image Name */
    public static final int RTImageName = 0x30020003;

    /** (3002,0004) VR=ST VM=1 RT Image Description */
    public static final int RTImageDescription = 0x30020004;

    /** (3002,000A) VR=CS VM=1 Reported Values Origin */
    public static final int ReportedValuesOrigin = 0x3002000A;

    /** (3002,000C) VR=CS VM=1 RT Image Plane */
    public static final int RTImagePlane = 0x3002000C;

    /** (3002,000D) VR=DS VM=3 X-Ray Image Receptor Translation */
    public static final int XRayImageReceptorTranslation = 0x3002000D;

    /** (3002,000E) VR=DS VM=1 X-Ray Image Receptor Angle */
    public static final int XRayImageReceptorAngle = 0x3002000E;

    /** (3002,0010) VR=DS VM=6 RT Image Orientation */
    public static final int RTImageOrientation = 0x30020010;

    /** (3002,0011) VR=DS VM=2 Image Plane Pixel Spacing */
    public static final int ImagePlanePixelSpacing = 0x30020011;

    /** (3002,0012) VR=DS VM=2 RT Image Position */
    public static final int RTImagePosition = 0x30020012;

    /** (3002,0020) VR=SH VM=1 Radiation Machine Name */
    public static final int RadiationMachineName = 0x30020020;

    /** (3002,0022) VR=DS VM=1 Radiation Machine SAD */
    public static final int RadiationMachineSAD = 0x30020022;

    /** (3002,0024) VR=DS VM=1 Radiation Machine SSD */
    public static final int RadiationMachineSSD = 0x30020024;

    /** (3002,0026) VR=DS VM=1 RT Image SID */
    public static final int RTImageSID = 0x30020026;

    /** (3002,0028) VR=DS VM=1 Source to Reference Object Distance */
    public static final int SourceToReferenceObjectDistance = 0x30020028;

    /** (3002,0029) VR=IS VM=1 Fraction Number */
    public static final int FractionNumber = 0x30020029;

    /** (3002,0030) VR=SQ VM=1 Exposure Sequence */
    public static final int ExposureSequence = 0x30020030;

    /** (3002,0032) VR=DS VM=1 Meterset Exposure */
    public static final int MetersetExposure = 0x30020032;

    /** (3002,0034) VR=DS VM=4 Diaphragm Position */
    public static final int DiaphragmPosition = 0x30020034;

    /** (3002,0040) VR=SQ VM=1 Fluence Map Sequence */
    public static final int FluenceMapSequence = 0x30020040;

    /** (3002,0041) VR=CS VM=1 Fluence Data Source */
    public static final int FluenceDataSource = 0x30020041;

    /** (3002,0042) VR=DS VM=1 Fluence Data Scale */
    public static final int FluenceDataScale = 0x30020042;

    /** (3002,0050) VR=SQ VM=1 Primary Fluence Mode Sequence */
    public static final int PrimaryFluenceModeSequence = 0x30020050;

    /** (3002,0051) VR=CS VM=1 Fluence Mode */
    public static final int FluenceMode = 0x30020051;

    /** (3002,0052) VR=SH VM=1 Fluence Mode ID */
    public static final int FluenceModeID = 0x30020052;

    /** (3004,0001) VR=CS VM=1 DVH Type */
    public static final int DVHType = 0x30040001;

    /** (3004,0002) VR=CS VM=1 Dose Units */
    public static final int DoseUnits = 0x30040002;

    /** (3004,0004) VR=CS VM=1 Dose Type */
    public static final int DoseType = 0x30040004;

    /** (3004,0005) VR=CS VM=1 Spatial Transform of Dose */
    public static final int SpatialTransformOfDose = 0x30040005;

    /** (3004,0006) VR=LO VM=1 Dose Comment */
    public static final int DoseComment = 0x30040006;

    /** (3004,0008) VR=DS VM=3 Normalization Point */
    public static final int NormalizationPoint = 0x30040008;

    /** (3004,000A) VR=CS VM=1 Dose Summation Type */
    public static final int DoseSummationType = 0x3004000A;

    /** (3004,000C) VR=DS VM=2-n Grid Frame Offset Vector */
    public static final int GridFrameOffsetVector = 0x3004000C;

    /** (3004,000E) VR=DS VM=1 Dose Grid Scaling */
    public static final int DoseGridScaling = 0x3004000E;

    /** (3004,0010) VR=SQ VM=1 RT Dose ROI Sequence */
    public static final int RTDoseROISequence = 0x30040010;

    /** (3004,0012) VR=DS VM=1 Dose Value */
    public static final int DoseValue = 0x30040012;

    /** (3004,0014) VR=CS VM=1-3 Tissue Heterogeneity Correction */
    public static final int TissueHeterogeneityCorrection = 0x30040014;

    /** (3004,0040) VR=DS VM=3 DVH Normalization Point */
    public static final int DVHNormalizationPoint = 0x30040040;

    /** (3004,0042) VR=DS VM=1 DVH Normalization Dose Value */
    public static final int DVHNormalizationDoseValue = 0x30040042;

    /** (3004,0050) VR=SQ VM=1 DVH Sequence */
    public static final int DVHSequence = 0x30040050;

    /** (3004,0052) VR=DS VM=1 DVH Dose Scaling */
    public static final int DVHDoseScaling = 0x30040052;

    /** (3004,0054) VR=CS VM=1 DVH Volume Units */
    public static final int DVHVolumeUnits = 0x30040054;

    /** (3004,0056) VR=IS VM=1 DVH Number of Bins */
    public static final int DVHNumberOfBins = 0x30040056;

    /** (3004,0058) VR=DS VM=2-2n DVH Data */
    public static final int DVHData = 0x30040058;

    /** (3004,0060) VR=SQ VM=1 DVH Referenced ROI Sequence */
    public static final int DVHReferencedROISequence = 0x30040060;

    /** (3004,0062) VR=CS VM=1 DVH ROI Contribution Type */
    public static final int DVHROIContributionType = 0x30040062;

    /** (3004,0070) VR=DS VM=1 DVH Minimum Dose */
    public static final int DVHMinimumDose = 0x30040070;

    /** (3004,0072) VR=DS VM=1 DVH Maximum Dose */
    public static final int DVHMaximumDose = 0x30040072;

    /** (3004,0074) VR=DS VM=1 DVH Mean Dose */
    public static final int DVHMeanDose = 0x30040074;

    /** (3006,0002) VR=SH VM=1 Structure Set Label */
    public static final int StructureSetLabel = 0x30060002;

    /** (3006,0004) VR=LO VM=1 Structure Set Name */
    public static final int StructureSetName = 0x30060004;

    /** (3006,0006) VR=ST VM=1 Structure Set Description */
    public static final int StructureSetDescription = 0x30060006;

    /** (3006,0008) VR=DA VM=1 Structure Set Date */
    public static final int StructureSetDate = 0x30060008;

    /** (3006,0009) VR=TM VM=1 Structure Set Time */
    public static final int StructureSetTime = 0x30060009;

    /** (3006,0010) VR=SQ VM=1 Referenced Frame of Reference Sequence */
    public static final int ReferencedFrameOfReferenceSequence = 0x30060010;

    /** (3006,0012) VR=SQ VM=1 RT Referenced Study Sequence */
    public static final int RTReferencedStudySequence = 0x30060012;

    /** (3006,0014) VR=SQ VM=1 RT Referenced Series Sequence */
    public static final int RTReferencedSeriesSequence = 0x30060014;

    /** (3006,0016) VR=SQ VM=1 Contour Image Sequence */
    public static final int ContourImageSequence = 0x30060016;

    /** (3006,0018) VR=SQ VM=1 Predecessor Structure Set Sequence */
    public static final int PredecessorStructureSetSequence = 0x30060018;

    /** (3006,0020) VR=SQ VM=1 Structure Set ROI Sequence */
    public static final int StructureSetROISequence = 0x30060020;

    /** (3006,0022) VR=IS VM=1 ROI Number */
    public static final int ROINumber = 0x30060022;

    /** (3006,0024) VR=UI VM=1 Referenced Frame of Reference UID */
    public static final int ReferencedFrameOfReferenceUID = 0x30060024;

    /** (3006,0026) VR=LO VM=1 ROI Name */
    public static final int ROIName = 0x30060026;

    /** (3006,0028) VR=ST VM=1 ROI Description */
    public static final int ROIDescription = 0x30060028;

    /** (3006,002A) VR=IS VM=3 ROI Display Color */
    public static final int ROIDisplayColor = 0x3006002A;

    /** (3006,002C) VR=DS VM=1 ROI Volume */
    public static final int ROIVolume = 0x3006002C;

    /** (3006,0030) VR=SQ VM=1 RT Related ROI Sequence */
    public static final int RTRelatedROISequence = 0x30060030;

    /** (3006,0033) VR=CS VM=1 RT ROI Relationship */
    public static final int RTROIRelationship = 0x30060033;

    /** (3006,0036) VR=CS VM=1 ROI Generation Algorithm */
    public static final int ROIGenerationAlgorithm = 0x30060036;

    /** (3006,0037) VR=SQ VM=1 ROI Derivation Algorithm Identification Sequence */
    public static final int ROIDerivationAlgorithmIdentificationSequence = 0x30060037;

    /** (3006,0038) VR=LO VM=1 ROI Generation Description */
    public static final int ROIGenerationDescription = 0x30060038;

    /** (3006,0039) VR=SQ VM=1 ROI Contour Sequence */
    public static final int ROIContourSequence = 0x30060039;

    /** (3006,0040) VR=SQ VM=1 Contour Sequence */
    public static final int ContourSequence = 0x30060040;

    /** (3006,0042) VR=CS VM=1 Contour Geometric Type */
    public static final int ContourGeometricType = 0x30060042;

    /** (3006,0044) VR=DS VM=1 Contour Slab Thickness */
    public static final int ContourSlabThickness = 0x30060044;

    /** (3006,0045) VR=DS VM=3 Contour Offset Vector */
    public static final int ContourOffsetVector = 0x30060045;

    /** (3006,0046) VR=IS VM=1 Number of Contour Points */
    public static final int NumberOfContourPoints = 0x30060046;

    /** (3006,0048) VR=IS VM=1 Contour Number */
    public static final int ContourNumber = 0x30060048;

    /** (3006,0049) VR=IS VM=1-n Attached Contours */
    public static final int AttachedContours = 0x30060049;

    /** (3006,0050) VR=DS VM=3-3n Contour Data */
    public static final int ContourData = 0x30060050;

    /** (3006,0080) VR=SQ VM=1 RT ROI Observations Sequence */
    public static final int RTROIObservationsSequence = 0x30060080;

    /** (3006,0082) VR=IS VM=1 Observation Number */
    public static final int ObservationNumber = 0x30060082;

    /** (3006,0084) VR=IS VM=1 Referenced ROI Number */
    public static final int ReferencedROINumber = 0x30060084;

    /** (3006,0085) VR=SH VM=1 ROI Observation Label */
    public static final int ROIObservationLabel = 0x30060085;

    /** (3006,0086) VR=SQ VM=1 RT ROI Identification Code Sequence */
    public static final int RTROIIdentificationCodeSequence = 0x30060086;

    /** (3006,0088) VR=ST VM=1 ROI Observation Description */
    public static final int ROIObservationDescription = 0x30060088;

    /** (3006,00A0) VR=SQ VM=1 Related RT ROI Observations Sequence */
    public static final int RelatedRTROIObservationsSequence = 0x300600A0;

    /** (3006,00A4) VR=CS VM=1 RT ROI Interpreted Type */
    public static final int RTROIInterpretedType = 0x300600A4;

    /** (3006,00A6) VR=PN VM=1 ROI Interpreter */
    public static final int ROIInterpreter = 0x300600A6;

    /** (3006,00B0) VR=SQ VM=1 ROI Physical Properties Sequence */
    public static final int ROIPhysicalPropertiesSequence = 0x300600B0;

    /** (3006,00B2) VR=CS VM=1 ROI Physical Property */
    public static final int ROIPhysicalProperty = 0x300600B2;

    /** (3006,00B4) VR=DS VM=1 ROI Physical Property Value */
    public static final int ROIPhysicalPropertyValue = 0x300600B4;

    /** (3006,00B6) VR=SQ VM=1 ROI Elemental Composition Sequence */
    public static final int ROIElementalCompositionSequence = 0x300600B6;

    /** (3006,00B7) VR=US VM=1 ROI Elemental Composition Atomic Number */
    public static final int ROIElementalCompositionAtomicNumber = 0x300600B7;

    /** (3006,00B8) VR=FL VM=1 ROI Elemental Composition Atomic Mass Fraction */
    public static final int ROIElementalCompositionAtomicMassFraction = 0x300600B8;

    /** (3006,00B9) VR=SQ VM=1 Additional RT ROI Identification Code Sequence (retired) */
    public static final int AdditionalRTROIIdentificationCodeSequence = 0x300600B9;

    /** (3006,00C0) VR=SQ VM=1 Frame of Reference Relationship Sequence (retired) */
    public static final int FrameOfReferenceRelationshipSequence = 0x300600C0;

    /** (3006,00C2) VR=UI VM=1 Related Frame of Reference UID (retired) */
    public static final int RelatedFrameOfReferenceUID = 0x300600C2;

    /** (3006,00C4) VR=CS VM=1 Frame of Reference Transformation Type (retired) */
    public static final int FrameOfReferenceTransformationType = 0x300600C4;

    /** (3006,00C6) VR=DS VM=16 Frame of Reference Transformation Matrix */
    public static final int FrameOfReferenceTransformationMatrix = 0x300600C6;

    /** (3006,00C8) VR=LO VM=1 Frame of Reference Transformation Comment */
    public static final int FrameOfReferenceTransformationComment = 0x300600C8;

    /** (3006,00C9) VR=SQ VM=1 Patient Location Coordinates Sequence */
    public static final int PatientLocationCoordinatesSequence = 0x300600C9;

    /** (3006,00CA) VR=SQ VM=1 Patient Location Coordinates Code Sequence */
    public static final int PatientLocationCoordinatesCodeSequence = 0x300600CA;

    /** (3006,00CB) VR=SQ VM=1 Patient Support Position Sequence */
    public static final int PatientSupportPositionSequence = 0x300600CB;

    /** (3008,0010) VR=SQ VM=1 Measured Dose Reference Sequence */
    public static final int MeasuredDoseReferenceSequence = 0x30080010;

    /** (3008,0012) VR=ST VM=1 Measured Dose Description */
    public static final int MeasuredDoseDescription = 0x30080012;

    /** (3008,0014) VR=CS VM=1 Measured Dose Type */
    public static final int MeasuredDoseType = 0x30080014;

    /** (3008,0016) VR=DS VM=1 Measured Dose Value */
    public static final int MeasuredDoseValue = 0x30080016;

    /** (3008,0020) VR=SQ VM=1 Treatment Session Beam Sequence */
    public static final int TreatmentSessionBeamSequence = 0x30080020;

    /** (3008,0021) VR=SQ VM=1 Treatment Session Ion Beam Sequence */
    public static final int TreatmentSessionIonBeamSequence = 0x30080021;

    /** (3008,0022) VR=IS VM=1 Current Fraction Number */
    public static final int CurrentFractionNumber = 0x30080022;

    /** (3008,0024) VR=DA VM=1 Treatment Control Point Date */
    public static final int TreatmentControlPointDate = 0x30080024;

    /** (3008,0025) VR=TM VM=1 Treatment Control Point Time */
    public static final int TreatmentControlPointTime = 0x30080025;

    /** (3008,002A) VR=CS VM=1 Treatment Termination Status */
    public static final int TreatmentTerminationStatus = 0x3008002A;

    /** (3008,002B) VR=SH VM=1 Treatment Termination Code */
    public static final int TreatmentTerminationCode = 0x3008002B;

    /** (3008,002C) VR=CS VM=1 Treatment Verification Status */
    public static final int TreatmentVerificationStatus = 0x3008002C;

    /** (3008,0030) VR=SQ VM=1 Referenced Treatment Record Sequence */
    public static final int ReferencedTreatmentRecordSequence = 0x30080030;

    /** (3008,0032) VR=DS VM=1 Specified Primary Meterset */
    public static final int SpecifiedPrimaryMeterset = 0x30080032;

    /** (3008,0033) VR=DS VM=1 Specified Secondary Meterset */
    public static final int SpecifiedSecondaryMeterset = 0x30080033;

    /** (3008,0036) VR=DS VM=1 Delivered Primary Meterset */
    public static final int DeliveredPrimaryMeterset = 0x30080036;

    /** (3008,0037) VR=DS VM=1 Delivered Secondary Meterset */
    public static final int DeliveredSecondaryMeterset = 0x30080037;

    /** (3008,003A) VR=DS VM=1 Specified Treatment Time */
    public static final int SpecifiedTreatmentTime = 0x3008003A;

    /** (3008,003B) VR=DS VM=1 Delivered Treatment Time */
    public static final int DeliveredTreatmentTime = 0x3008003B;

    /** (3008,0040) VR=SQ VM=1 Control Point Delivery Sequence */
    public static final int ControlPointDeliverySequence = 0x30080040;

    /** (3008,0041) VR=SQ VM=1 Ion Control Point Delivery Sequence */
    public static final int IonControlPointDeliverySequence = 0x30080041;

    /** (3008,0042) VR=DS VM=1 Specified Meterset */
    public static final int SpecifiedMeterset = 0x30080042;

    /** (3008,0044) VR=DS VM=1 Delivered Meterset */
    public static final int DeliveredMeterset = 0x30080044;

    /** (3008,0045) VR=FL VM=1 Meterset Rate Set */
    public static final int MetersetRateSet = 0x30080045;

    /** (3008,0046) VR=FL VM=1 Meterset Rate Delivered */
    public static final int MetersetRateDelivered = 0x30080046;

    /** (3008,0047) VR=FL VM=1-n Scan Spot Metersets Delivered */
    public static final int ScanSpotMetersetsDelivered = 0x30080047;

    /** (3008,0048) VR=DS VM=1 Dose Rate Delivered */
    public static final int DoseRateDelivered = 0x30080048;

    /** (3008,0050) VR=SQ VM=1 Treatment Summary Calculated Dose Reference Sequence */
    public static final int TreatmentSummaryCalculatedDoseReferenceSequence = 0x30080050;

    /** (3008,0052) VR=DS VM=1 Cumulative Dose to Dose Reference */
    public static final int CumulativeDoseToDoseReference = 0x30080052;

    /** (3008,0054) VR=DA VM=1 First Treatment Date */
    public static final int FirstTreatmentDate = 0x30080054;

    /** (3008,0056) VR=DA VM=1 Most Recent Treatment Date */
    public static final int MostRecentTreatmentDate = 0x30080056;

    /** (3008,005A) VR=IS VM=1 Number of Fractions Delivered */
    public static final int NumberOfFractionsDelivered = 0x3008005A;

    /** (3008,0060) VR=SQ VM=1 Override Sequence */
    public static final int OverrideSequence = 0x30080060;

    /** (3008,0061) VR=AT VM=1 Parameter Sequence Pointer */
    public static final int ParameterSequencePointer = 0x30080061;

    /** (3008,0062) VR=AT VM=1 Override Parameter Pointer */
    public static final int OverrideParameterPointer = 0x30080062;

    /** (3008,0063) VR=IS VM=1 Parameter Item Index */
    public static final int ParameterItemIndex = 0x30080063;

    /** (3008,0064) VR=IS VM=1 Measured Dose Reference Number */
    public static final int MeasuredDoseReferenceNumber = 0x30080064;

    /** (3008,0065) VR=AT VM=1 Parameter Pointer */
    public static final int ParameterPointer = 0x30080065;

    /** (3008,0066) VR=ST VM=1 Override Reason */
    public static final int OverrideReason = 0x30080066;

    /** (3008,0067) VR=US VM=1 Parameter Value Number */
    public static final int ParameterValueNumber = 0x30080067;

    /** (3008,0068) VR=SQ VM=1 Corrected Parameter Sequence */
    public static final int CorrectedParameterSequence = 0x30080068;

    /** (3008,006A) VR=FL VM=1 Correction Value */
    public static final int CorrectionValue = 0x3008006A;

    /** (3008,0070) VR=SQ VM=1 Calculated Dose Reference Sequence */
    public static final int CalculatedDoseReferenceSequence = 0x30080070;

    /** (3008,0072) VR=IS VM=1 Calculated Dose Reference Number */
    public static final int CalculatedDoseReferenceNumber = 0x30080072;

    /** (3008,0074) VR=ST VM=1 Calculated Dose Reference Description */
    public static final int CalculatedDoseReferenceDescription = 0x30080074;

    /** (3008,0076) VR=DS VM=1 Calculated Dose Reference Dose Value */
    public static final int CalculatedDoseReferenceDoseValue = 0x30080076;

    /** (3008,0078) VR=DS VM=1 Start Meterset */
    public static final int StartMeterset = 0x30080078;

    /** (3008,007A) VR=DS VM=1 End Meterset */
    public static final int EndMeterset = 0x3008007A;

    /** (3008,0080) VR=SQ VM=1 Referenced Measured Dose Reference Sequence */
    public static final int ReferencedMeasuredDoseReferenceSequence = 0x30080080;

    /** (3008,0082) VR=IS VM=1 Referenced Measured Dose Reference Number */
    public static final int ReferencedMeasuredDoseReferenceNumber = 0x30080082;

    /** (3008,0090) VR=SQ VM=1 Referenced Calculated Dose Reference Sequence */
    public static final int ReferencedCalculatedDoseReferenceSequence = 0x30080090;

    /** (3008,0092) VR=IS VM=1 Referenced Calculated Dose Reference Number */
    public static final int ReferencedCalculatedDoseReferenceNumber = 0x30080092;

    /** (3008,00A0) VR=SQ VM=1 Beam Limiting Device Leaf Pairs Sequence */
    public static final int BeamLimitingDeviceLeafPairsSequence = 0x300800A0;

    /** (3008,00B0) VR=SQ VM=1 Recorded Wedge Sequence */
    public static final int RecordedWedgeSequence = 0x300800B0;

    /** (3008,00C0) VR=SQ VM=1 Recorded Compensator Sequence */
    public static final int RecordedCompensatorSequence = 0x300800C0;

    /** (3008,00D0) VR=SQ VM=1 Recorded Block Sequence */
    public static final int RecordedBlockSequence = 0x300800D0;

    /** (3008,00E0) VR=SQ VM=1 Treatment Summary Measured Dose Reference Sequence */
    public static final int TreatmentSummaryMeasuredDoseReferenceSequence = 0x300800E0;

    /** (3008,00F0) VR=SQ VM=1 Recorded Snout Sequence */
    public static final int RecordedSnoutSequence = 0x300800F0;

    /** (3008,00F2) VR=SQ VM=1 Recorded Range Shifter Sequence */
    public static final int RecordedRangeShifterSequence = 0x300800F2;

    /** (3008,00F4) VR=SQ VM=1 Recorded Lateral Spreading Device Sequence */
    public static final int RecordedLateralSpreadingDeviceSequence = 0x300800F4;

    /** (3008,00F6) VR=SQ VM=1 Recorded Range Modulator Sequence */
    public static final int RecordedRangeModulatorSequence = 0x300800F6;

    /** (3008,0100) VR=SQ VM=1 Recorded Source Sequence */
    public static final int RecordedSourceSequence = 0x30080100;

    /** (3008,0105) VR=LO VM=1 Source Serial Number */
    public static final int SourceSerialNumber = 0x30080105;

    /** (3008,0110) VR=SQ VM=1 Treatment Session Application Setup Sequence */
    public static final int TreatmentSessionApplicationSetupSequence = 0x30080110;

    /** (3008,0116) VR=CS VM=1 Application Setup Check */
    public static final int ApplicationSetupCheck = 0x30080116;

    /** (3008,0120) VR=SQ VM=1 Recorded Brachy Accessory Device Sequence */
    public static final int RecordedBrachyAccessoryDeviceSequence = 0x30080120;

    /** (3008,0122) VR=IS VM=1 Referenced Brachy Accessory Device Number */
    public static final int ReferencedBrachyAccessoryDeviceNumber = 0x30080122;

    /** (3008,0130) VR=SQ VM=1 Recorded Channel Sequence */
    public static final int RecordedChannelSequence = 0x30080130;

    /** (3008,0132) VR=DS VM=1 Specified Channel Total Time */
    public static final int SpecifiedChannelTotalTime = 0x30080132;

    /** (3008,0134) VR=DS VM=1 Delivered Channel Total Time */
    public static final int DeliveredChannelTotalTime = 0x30080134;

    /** (3008,0136) VR=IS VM=1 Specified Number of Pulses */
    public static final int SpecifiedNumberOfPulses = 0x30080136;

    /** (3008,0138) VR=IS VM=1 Delivered Number of Pulses */
    public static final int DeliveredNumberOfPulses = 0x30080138;

    /** (3008,013A) VR=DS VM=1 Specified Pulse Repetition Interval */
    public static final int SpecifiedPulseRepetitionInterval = 0x3008013A;

    /** (3008,013C) VR=DS VM=1 Delivered Pulse Repetition Interval */
    public static final int DeliveredPulseRepetitionInterval = 0x3008013C;

    /** (3008,0140) VR=SQ VM=1 Recorded Source Applicator Sequence */
    public static final int RecordedSourceApplicatorSequence = 0x30080140;

    /** (3008,0142) VR=IS VM=1 Referenced Source Applicator Number */
    public static final int ReferencedSourceApplicatorNumber = 0x30080142;

    /** (3008,0150) VR=SQ VM=1 Recorded Channel Shield Sequence */
    public static final int RecordedChannelShieldSequence = 0x30080150;

    /** (3008,0152) VR=IS VM=1 Referenced Channel Shield Number */
    public static final int ReferencedChannelShieldNumber = 0x30080152;

    /** (3008,0160) VR=SQ VM=1 Brachy Control Point Delivered Sequence */
    public static final int BrachyControlPointDeliveredSequence = 0x30080160;

    /** (3008,0162) VR=DA VM=1 Safe Position Exit Date */
    public static final int SafePositionExitDate = 0x30080162;

    /** (3008,0164) VR=TM VM=1 Safe Position Exit Time */
    public static final int SafePositionExitTime = 0x30080164;

    /** (3008,0166) VR=DA VM=1 Safe Position Return Date */
    public static final int SafePositionReturnDate = 0x30080166;

    /** (3008,0168) VR=TM VM=1 Safe Position Return Time */
    public static final int SafePositionReturnTime = 0x30080168;

    /** (3008,0171) VR=SQ VM=1 Pulse Specific Brachy Control Point Delivered Sequence */
    public static final int PulseSpecificBrachyControlPointDeliveredSequence = 0x30080171;

    /** (3008,0172) VR=US VM=1 Pulse Number */
    public static final int PulseNumber = 0x30080172;

    /** (3008,0173) VR=SQ VM=1 Brachy Pulse Control Point Delivered Sequence */
    public static final int BrachyPulseControlPointDeliveredSequence = 0x30080173;

    /** (3008,0200) VR=CS VM=1 Current Treatment Status */
    public static final int CurrentTreatmentStatus = 0x30080200;

    /** (3008,0202) VR=ST VM=1 Treatment Status Comment */
    public static final int TreatmentStatusComment = 0x30080202;

    /** (3008,0220) VR=SQ VM=1 Fraction Group Summary Sequence */
    public static final int FractionGroupSummarySequence = 0x30080220;

    /** (3008,0223) VR=IS VM=1 Referenced Fraction Number */
    public static final int ReferencedFractionNumber = 0x30080223;

    /** (3008,0224) VR=CS VM=1 Fraction Group Type */
    public static final int FractionGroupType = 0x30080224;

    /** (3008,0230) VR=CS VM=1 Beam Stopper Position */
    public static final int BeamStopperPosition = 0x30080230;

    /** (3008,0240) VR=SQ VM=1 Fraction Status Summary Sequence */
    public static final int FractionStatusSummarySequence = 0x30080240;

    /** (3008,0250) VR=DA VM=1 Treatment Date */
    public static final int TreatmentDate = 0x30080250;

    /** (3008,0251) VR=TM VM=1 Treatment Time */
    public static final int TreatmentTime = 0x30080251;

    /** (300A,0002) VR=SH VM=1 RT Plan Label */
    public static final int RTPlanLabel = 0x300A0002;

    /** (300A,0003) VR=LO VM=1 RT Plan Name */
    public static final int RTPlanName = 0x300A0003;

    /** (300A,0004) VR=ST VM=1 RT Plan Description */
    public static final int RTPlanDescription = 0x300A0004;

    /** (300A,0006) VR=DA VM=1 RT Plan Date */
    public static final int RTPlanDate = 0x300A0006;

    /** (300A,0007) VR=TM VM=1 RT Plan Time */
    public static final int RTPlanTime = 0x300A0007;

    /** (300A,0009) VR=LO VM=1-n Treatment Protocols */
    public static final int TreatmentProtocols = 0x300A0009;

    /** (300A,000A) VR=CS VM=1 Plan Intent */
    public static final int PlanIntent = 0x300A000A;

    /** (300A,000B) VR=LO VM=1-n Treatment Sites */
    public static final int TreatmentSites = 0x300A000B;

    /** (300A,000C) VR=CS VM=1 RT Plan Geometry */
    public static final int RTPlanGeometry = 0x300A000C;

    /** (300A,000E) VR=ST VM=1 Prescription Description */
    public static final int PrescriptionDescription = 0x300A000E;

    /** (300A,0010) VR=SQ VM=1 Dose Reference Sequence */
    public static final int DoseReferenceSequence = 0x300A0010;

    /** (300A,0012) VR=IS VM=1 Dose Reference Number */
    public static final int DoseReferenceNumber = 0x300A0012;

    /** (300A,0013) VR=UI VM=1 Dose Reference UID */
    public static final int DoseReferenceUID = 0x300A0013;

    /** (300A,0014) VR=CS VM=1 Dose Reference Structure Type */
    public static final int DoseReferenceStructureType = 0x300A0014;

    /** (300A,0015) VR=CS VM=1 Nominal Beam Energy Unit */
    public static final int NominalBeamEnergyUnit = 0x300A0015;

    /** (300A,0016) VR=LO VM=1 Dose Reference Description */
    public static final int DoseReferenceDescription = 0x300A0016;

    /** (300A,0018) VR=DS VM=3 Dose Reference Point Coordinates */
    public static final int DoseReferencePointCoordinates = 0x300A0018;

    /** (300A,001A) VR=DS VM=1 Nominal Prior Dose */
    public static final int NominalPriorDose = 0x300A001A;

    /** (300A,0020) VR=CS VM=1 Dose Reference Type */
    public static final int DoseReferenceType = 0x300A0020;

    /** (300A,0021) VR=DS VM=1 Constraint Weight */
    public static final int ConstraintWeight = 0x300A0021;

    /** (300A,0022) VR=DS VM=1 Delivery Warning Dose */
    public static final int DeliveryWarningDose = 0x300A0022;

    /** (300A,0023) VR=DS VM=1 Delivery Maximum Dose */
    public static final int DeliveryMaximumDose = 0x300A0023;

    /** (300A,0025) VR=DS VM=1 Target Minimum Dose */
    public static final int TargetMinimumDose = 0x300A0025;

    /** (300A,0026) VR=DS VM=1 Target Prescription Dose */
    public static final int TargetPrescriptionDose = 0x300A0026;

    /** (300A,0027) VR=DS VM=1 Target Maximum Dose */
    public static final int TargetMaximumDose = 0x300A0027;

    /** (300A,0028) VR=DS VM=1 Target Underdose Volume Fraction */
    public static final int TargetUnderdoseVolumeFraction = 0x300A0028;

    /** (300A,002A) VR=DS VM=1 Organ at Risk Full-volume Dose */
    public static final int OrganAtRiskFullVolumeDose = 0x300A002A;

    /** (300A,002B) VR=DS VM=1 Organ at Risk Limit Dose */
    public static final int OrganAtRiskLimitDose = 0x300A002B;

    /** (300A,002C) VR=DS VM=1 Organ at Risk Maximum Dose */
    public static final int OrganAtRiskMaximumDose = 0x300A002C;

    /** (300A,002D) VR=DS VM=1 Organ at Risk Overdose Volume Fraction */
    public static final int OrganAtRiskOverdoseVolumeFraction = 0x300A002D;

    /** (300A,0040) VR=SQ VM=1 Tolerance Table Sequence */
    public static final int ToleranceTableSequence = 0x300A0040;

    /** (300A,0042) VR=IS VM=1 Tolerance Table Number */
    public static final int ToleranceTableNumber = 0x300A0042;

    /** (300A,0043) VR=SH VM=1 Tolerance Table Label */
    public static final int ToleranceTableLabel = 0x300A0043;

    /** (300A,0044) VR=DS VM=1 Gantry Angle Tolerance */
    public static final int GantryAngleTolerance = 0x300A0044;

    /** (300A,0046) VR=DS VM=1 Beam Limiting Device Angle Tolerance */
    public static final int BeamLimitingDeviceAngleTolerance = 0x300A0046;

    /** (300A,0048) VR=SQ VM=1 Beam Limiting Device Tolerance Sequence */
    public static final int BeamLimitingDeviceToleranceSequence = 0x300A0048;

    /** (300A,004A) VR=DS VM=1 Beam Limiting Device Position Tolerance */
    public static final int BeamLimitingDevicePositionTolerance = 0x300A004A;

    /** (300A,004B) VR=FL VM=1 Snout Position Tolerance */
    public static final int SnoutPositionTolerance = 0x300A004B;

    /** (300A,004C) VR=DS VM=1 Patient Support Angle Tolerance */
    public static final int PatientSupportAngleTolerance = 0x300A004C;

    /** (300A,004E) VR=DS VM=1 Table Top Eccentric Angle Tolerance */
    public static final int TableTopEccentricAngleTolerance = 0x300A004E;

    /** (300A,004F) VR=FL VM=1 Table Top Pitch Angle Tolerance */
    public static final int TableTopPitchAngleTolerance = 0x300A004F;

    /** (300A,0050) VR=FL VM=1 Table Top Roll Angle Tolerance */
    public static final int TableTopRollAngleTolerance = 0x300A0050;

    /** (300A,0051) VR=DS VM=1 Table Top Vertical Position Tolerance */
    public static final int TableTopVerticalPositionTolerance = 0x300A0051;

    /** (300A,0052) VR=DS VM=1 Table Top Longitudinal Position Tolerance */
    public static final int TableTopLongitudinalPositionTolerance = 0x300A0052;

    /** (300A,0053) VR=DS VM=1 Table Top Lateral Position Tolerance */
    public static final int TableTopLateralPositionTolerance = 0x300A0053;

    /** (300A,0055) VR=CS VM=1 RT Plan Relationship */
    public static final int RTPlanRelationship = 0x300A0055;

    /** (300A,0070) VR=SQ VM=1 Fraction Group Sequence */
    public static final int FractionGroupSequence = 0x300A0070;

    /** (300A,0071) VR=IS VM=1 Fraction Group Number */
    public static final int FractionGroupNumber = 0x300A0071;

    /** (300A,0072) VR=LO VM=1 Fraction Group Description */
    public static final int FractionGroupDescription = 0x300A0072;

    /** (300A,0078) VR=IS VM=1 Number of Fractions Planned */
    public static final int NumberOfFractionsPlanned = 0x300A0078;

    /** (300A,0079) VR=IS VM=1 Number of Fraction Pattern Digits Per Day */
    public static final int NumberOfFractionPatternDigitsPerDay = 0x300A0079;

    /** (300A,007A) VR=IS VM=1 Repeat Fraction Cycle Length */
    public static final int RepeatFractionCycleLength = 0x300A007A;

    /** (300A,007B) VR=LT VM=1 Fraction Pattern */
    public static final int FractionPattern = 0x300A007B;

    /** (300A,0080) VR=IS VM=1 Number of Beams */
    public static final int NumberOfBeams = 0x300A0080;

    /** (300A,0082) VR=DS VM=3 Beam Dose Specification Point (retired) */
    public static final int BeamDoseSpecificationPoint = 0x300A0082;

    /** (300A,0083) VR=UI VM=1 Referenced Dose Reference UID */
    public static final int ReferencedDoseReferenceUID = 0x300A0083;

    /** (300A,0084) VR=DS VM=1 Beam Dose */
    public static final int BeamDose = 0x300A0084;

    /** (300A,0086) VR=DS VM=1 Beam Meterset */
    public static final int BeamMeterset = 0x300A0086;

    /** (300A,0088) VR=FL VM=1 Beam Dose Point Depth */
    public static final int BeamDosePointDepth = 0x300A0088;

    /** (300A,0089) VR=FL VM=1 Beam Dose Point Equivalent Depth */
    public static final int BeamDosePointEquivalentDepth = 0x300A0089;

    /** (300A,008A) VR=FL VM=1 Beam Dose Point SSD */
    public static final int BeamDosePointSSD = 0x300A008A;

    /** (300A,008B) VR=CS VM=1 Beam Dose Meaning */
    public static final int BeamDoseMeaning = 0x300A008B;

    /** (300A,008C) VR=SQ VM=1 Beam Dose Verification Control Point Sequence */
    public static final int BeamDoseVerificationControlPointSequence = 0x300A008C;

    /** (300A,008D) VR=FL VM=1 Average Beam Dose Point Depth (retired) */
    public static final int AverageBeamDosePointDepth = 0x300A008D;

    /** (300A,008E) VR=FL VM=1 Average Beam Dose Point Equivalent Depth (retired) */
    public static final int AverageBeamDosePointEquivalentDepth = 0x300A008E;

    /** (300A,008F) VR=FL VM=1 Average Beam Dose Point SSD (retired) */
    public static final int AverageBeamDosePointSSD = 0x300A008F;

    /** (300A,0090) VR=CS VM=1 Beam Dose Type */
    public static final int BeamDoseType = 0x300A0090;

    /** (300A,0091) VR=DS VM=1 Alternate Beam Dose */
    public static final int AlternateBeamDose = 0x300A0091;

    /** (300A,0092) VR=CS VM=1 Alternate Beam Dose Type */
    public static final int AlternateBeamDoseType = 0x300A0092;

    /** (300A,0093) VR=CS VM=1 Depth Value Averaging Flag */
    public static final int DepthValueAveragingFlag = 0x300A0093;

    /** (300A,0094) VR=DS VM=1 Beam Dose Point Source to External Contour Distance */
    public static final int BeamDosePointSourceToExternalContourDistance = 0x300A0094;

    /** (300A,00A0) VR=IS VM=1 Number of Brachy Application Setups */
    public static final int NumberOfBrachyApplicationSetups = 0x300A00A0;

    /** (300A,00A2) VR=DS VM=3 Brachy Application Setup Dose Specification Point */
    public static final int BrachyApplicationSetupDoseSpecificationPoint = 0x300A00A2;

    /** (300A,00A4) VR=DS VM=1 Brachy Application Setup Dose */
    public static final int BrachyApplicationSetupDose = 0x300A00A4;

    /** (300A,00B0) VR=SQ VM=1 Beam Sequence */
    public static final int BeamSequence = 0x300A00B0;

    /** (300A,00B2) VR=SH VM=1 Treatment Machine Name */
    public static final int TreatmentMachineName = 0x300A00B2;

    /** (300A,00B3) VR=CS VM=1 Primary Dosimeter Unit */
    public static final int PrimaryDosimeterUnit = 0x300A00B3;

    /** (300A,00B4) VR=DS VM=1 Source-Axis Distance */
    public static final int SourceAxisDistance = 0x300A00B4;

    /** (300A,00B6) VR=SQ VM=1 Beam Limiting Device Sequence */
    public static final int BeamLimitingDeviceSequence = 0x300A00B6;

    /** (300A,00B8) VR=CS VM=1 RT Beam Limiting Device Type */
    public static final int RTBeamLimitingDeviceType = 0x300A00B8;

    /** (300A,00BA) VR=DS VM=1 Source to Beam Limiting Device Distance */
    public static final int SourceToBeamLimitingDeviceDistance = 0x300A00BA;

    /** (300A,00BB) VR=FL VM=1 Isocenter to Beam Limiting Device Distance */
    public static final int IsocenterToBeamLimitingDeviceDistance = 0x300A00BB;

    /** (300A,00BC) VR=IS VM=1 Number of Leaf/Jaw Pairs */
    public static final int NumberOfLeafJawPairs = 0x300A00BC;

    /** (300A,00BE) VR=DS VM=3-n Leaf Position Boundaries */
    public static final int LeafPositionBoundaries = 0x300A00BE;

    /** (300A,00C0) VR=IS VM=1 Beam Number */
    public static final int BeamNumber = 0x300A00C0;

    /** (300A,00C2) VR=LO VM=1 Beam Name */
    public static final int BeamName = 0x300A00C2;

    /** (300A,00C3) VR=ST VM=1 Beam Description */
    public static final int BeamDescription = 0x300A00C3;

    /** (300A,00C4) VR=CS VM=1 Beam Type */
    public static final int BeamType = 0x300A00C4;

    /** (300A,00C5) VR=FD VM=1 Beam Delivery Duration Limit */
    public static final int BeamDeliveryDurationLimit = 0x300A00C5;

    /** (300A,00C6) VR=CS VM=1 Radiation Type */
    public static final int RadiationType = 0x300A00C6;

    /** (300A,00C7) VR=CS VM=1 High-Dose Technique Type */
    public static final int HighDoseTechniqueType = 0x300A00C7;

    /** (300A,00C8) VR=IS VM=1 Reference Image Number */
    public static final int ReferenceImageNumber = 0x300A00C8;

    /** (300A,00CA) VR=SQ VM=1 Planned Verification Image Sequence */
    public static final int PlannedVerificationImageSequence = 0x300A00CA;

    /** (300A,00CC) VR=LO VM=1-n Imaging Device-Specific Acquisition Parameters */
    public static final int ImagingDeviceSpecificAcquisitionParameters = 0x300A00CC;

    /** (300A,00CE) VR=CS VM=1 Treatment Delivery Type */
    public static final int TreatmentDeliveryType = 0x300A00CE;

    /** (300A,00D0) VR=IS VM=1 Number of Wedges */
    public static final int NumberOfWedges = 0x300A00D0;

    /** (300A,00D1) VR=SQ VM=1 Wedge Sequence */
    public static final int WedgeSequence = 0x300A00D1;

    /** (300A,00D2) VR=IS VM=1 Wedge Number */
    public static final int WedgeNumber = 0x300A00D2;

    /** (300A,00D3) VR=CS VM=1 Wedge Type */
    public static final int WedgeType = 0x300A00D3;

    /** (300A,00D4) VR=SH VM=1 Wedge ID */
    public static final int WedgeID = 0x300A00D4;

    /** (300A,00D5) VR=IS VM=1 Wedge Angle */
    public static final int WedgeAngle = 0x300A00D5;

    /** (300A,00D6) VR=DS VM=1 Wedge Factor */
    public static final int WedgeFactor = 0x300A00D6;

    /** (300A,00D7) VR=FL VM=1 Total Wedge Tray Water-Equivalent Thickness */
    public static final int TotalWedgeTrayWaterEquivalentThickness = 0x300A00D7;

    /** (300A,00D8) VR=DS VM=1 Wedge Orientation */
    public static final int WedgeOrientation = 0x300A00D8;

    /** (300A,00D9) VR=FL VM=1 Isocenter to Wedge Tray Distance */
    public static final int IsocenterToWedgeTrayDistance = 0x300A00D9;

    /** (300A,00DA) VR=DS VM=1 Source to Wedge Tray Distance */
    public static final int SourceToWedgeTrayDistance = 0x300A00DA;

    /** (300A,00DB) VR=FL VM=1 Wedge Thin Edge Position */
    public static final int WedgeThinEdgePosition = 0x300A00DB;

    /** (300A,00DC) VR=SH VM=1 Bolus ID */
    public static final int BolusID = 0x300A00DC;

    /** (300A,00DD) VR=ST VM=1 Bolus Description */
    public static final int BolusDescription = 0x300A00DD;

    /** (300A,00DE) VR=DS VM=1 Effective Wedge Angle */
    public static final int EffectiveWedgeAngle = 0x300A00DE;

    /** (300A,00E0) VR=IS VM=1 Number of Compensators */
    public static final int NumberOfCompensators = 0x300A00E0;

    /** (300A,00E1) VR=SH VM=1 Material ID */
    public static final int MaterialID = 0x300A00E1;

    /** (300A,00E2) VR=DS VM=1 Total Compensator Tray Factor */
    public static final int TotalCompensatorTrayFactor = 0x300A00E2;

    /** (300A,00E3) VR=SQ VM=1 Compensator Sequence */
    public static final int CompensatorSequence = 0x300A00E3;

    /** (300A,00E4) VR=IS VM=1 Compensator Number */
    public static final int CompensatorNumber = 0x300A00E4;

    /** (300A,00E5) VR=SH VM=1 Compensator ID */
    public static final int CompensatorID = 0x300A00E5;

    /** (300A,00E6) VR=DS VM=1 Source to Compensator Tray Distance */
    public static final int SourceToCompensatorTrayDistance = 0x300A00E6;

    /** (300A,00E7) VR=IS VM=1 Compensator Rows */
    public static final int CompensatorRows = 0x300A00E7;

    /** (300A,00E8) VR=IS VM=1 Compensator Columns */
    public static final int CompensatorColumns = 0x300A00E8;

    /** (300A,00E9) VR=DS VM=2 Compensator Pixel Spacing */
    public static final int CompensatorPixelSpacing = 0x300A00E9;

    /** (300A,00EA) VR=DS VM=2 Compensator Position */
    public static final int CompensatorPosition = 0x300A00EA;

    /** (300A,00EB) VR=DS VM=1-n Compensator Transmission Data */
    public static final int CompensatorTransmissionData = 0x300A00EB;

    /** (300A,00EC) VR=DS VM=1-n Compensator Thickness Data */
    public static final int CompensatorThicknessData = 0x300A00EC;

    /** (300A,00ED) VR=IS VM=1 Number of Boli */
    public static final int NumberOfBoli = 0x300A00ED;

    /** (300A,00EE) VR=CS VM=1 Compensator Type */
    public static final int CompensatorType = 0x300A00EE;

    /** (300A,00EF) VR=SH VM=1 Compensator Tray ID */
    public static final int CompensatorTrayID = 0x300A00EF;

    /** (300A,00F0) VR=IS VM=1 Number of Blocks */
    public static final int NumberOfBlocks = 0x300A00F0;

    /** (300A,00F2) VR=DS VM=1 Total Block Tray Factor */
    public static final int TotalBlockTrayFactor = 0x300A00F2;

    /** (300A,00F3) VR=FL VM=1 Total Block Tray Water-Equivalent Thickness */
    public static final int TotalBlockTrayWaterEquivalentThickness = 0x300A00F3;

    /** (300A,00F4) VR=SQ VM=1 Block Sequence */
    public static final int BlockSequence = 0x300A00F4;

    /** (300A,00F5) VR=SH VM=1 Block Tray ID */
    public static final int BlockTrayID = 0x300A00F5;

    /** (300A,00F6) VR=DS VM=1 Source to Block Tray Distance */
    public static final int SourceToBlockTrayDistance = 0x300A00F6;

    /** (300A,00F7) VR=FL VM=1 Isocenter to Block Tray Distance */
    public static final int IsocenterToBlockTrayDistance = 0x300A00F7;

    /** (300A,00F8) VR=CS VM=1 Block Type */
    public static final int BlockType = 0x300A00F8;

    /** (300A,00F9) VR=LO VM=1 Accessory Code */
    public static final int AccessoryCode = 0x300A00F9;

    /** (300A,00FA) VR=CS VM=1 Block Divergence */
    public static final int BlockDivergence = 0x300A00FA;

    /** (300A,00FB) VR=CS VM=1 Block Mounting Position */
    public static final int BlockMountingPosition = 0x300A00FB;

    /** (300A,00FC) VR=IS VM=1 Block Number */
    public static final int BlockNumber = 0x300A00FC;

    /** (300A,00FE) VR=LO VM=1 Block Name */
    public static final int BlockName = 0x300A00FE;

    /** (300A,0100) VR=DS VM=1 Block Thickness */
    public static final int BlockThickness = 0x300A0100;

    /** (300A,0102) VR=DS VM=1 Block Transmission */
    public static final int BlockTransmission = 0x300A0102;

    /** (300A,0104) VR=IS VM=1 Block Number of Points */
    public static final int BlockNumberOfPoints = 0x300A0104;

    /** (300A,0106) VR=DS VM=2-2n Block Data */
    public static final int BlockData = 0x300A0106;

    /** (300A,0107) VR=SQ VM=1 Applicator Sequence */
    public static final int ApplicatorSequence = 0x300A0107;

    /** (300A,0108) VR=SH VM=1 Applicator ID */
    public static final int ApplicatorID = 0x300A0108;

    /** (300A,0109) VR=CS VM=1 Applicator Type */
    public static final int ApplicatorType = 0x300A0109;

    /** (300A,010A) VR=LO VM=1 Applicator Description */
    public static final int ApplicatorDescription = 0x300A010A;

    /** (300A,010C) VR=DS VM=1 Cumulative Dose Reference Coefficient */
    public static final int CumulativeDoseReferenceCoefficient = 0x300A010C;

    /** (300A,010E) VR=DS VM=1 Final Cumulative Meterset Weight */
    public static final int FinalCumulativeMetersetWeight = 0x300A010E;

    /** (300A,0110) VR=IS VM=1 Number of Control Points */
    public static final int NumberOfControlPoints = 0x300A0110;

    /** (300A,0111) VR=SQ VM=1 Control Point Sequence */
    public static final int ControlPointSequence = 0x300A0111;

    /** (300A,0112) VR=IS VM=1 Control Point Index */
    public static final int ControlPointIndex = 0x300A0112;

    /** (300A,0114) VR=DS VM=1 Nominal Beam Energy */
    public static final int NominalBeamEnergy = 0x300A0114;

    /** (300A,0115) VR=DS VM=1 Dose Rate Set */
    public static final int DoseRateSet = 0x300A0115;

    /** (300A,0116) VR=SQ VM=1 Wedge Position Sequence */
    public static final int WedgePositionSequence = 0x300A0116;

    /** (300A,0118) VR=CS VM=1 Wedge Position */
    public static final int WedgePosition = 0x300A0118;

    /** (300A,011A) VR=SQ VM=1 Beam Limiting Device Position Sequence */
    public static final int BeamLimitingDevicePositionSequence = 0x300A011A;

    /** (300A,011C) VR=DS VM=2-2n Leaf/Jaw Positions */
    public static final int LeafJawPositions = 0x300A011C;

    /** (300A,011E) VR=DS VM=1 Gantry Angle */
    public static final int GantryAngle = 0x300A011E;

    /** (300A,011F) VR=CS VM=1 Gantry Rotation Direction */
    public static final int GantryRotationDirection = 0x300A011F;

    /** (300A,0120) VR=DS VM=1 Beam Limiting Device Angle */
    public static final int BeamLimitingDeviceAngle = 0x300A0120;

    /** (300A,0121) VR=CS VM=1 Beam Limiting Device Rotation Direction */
    public static final int BeamLimitingDeviceRotationDirection = 0x300A0121;

    /** (300A,0122) VR=DS VM=1 Patient Support Angle */
    public static final int PatientSupportAngle = 0x300A0122;

    /** (300A,0123) VR=CS VM=1 Patient Support Rotation Direction */
    public static final int PatientSupportRotationDirection = 0x300A0123;

    /** (300A,0124) VR=DS VM=1 Table Top Eccentric Axis Distance */
    public static final int TableTopEccentricAxisDistance = 0x300A0124;

    /** (300A,0125) VR=DS VM=1 Table Top Eccentric Angle */
    public static final int TableTopEccentricAngle = 0x300A0125;

    /** (300A,0126) VR=CS VM=1 Table Top Eccentric Rotation Direction */
    public static final int TableTopEccentricRotationDirection = 0x300A0126;

    /** (300A,0128) VR=DS VM=1 Table Top Vertical Position */
    public static final int TableTopVerticalPosition = 0x300A0128;

    /** (300A,0129) VR=DS VM=1 Table Top Longitudinal Position */
    public static final int TableTopLongitudinalPosition = 0x300A0129;

    /** (300A,012A) VR=DS VM=1 Table Top Lateral Position */
    public static final int TableTopLateralPosition = 0x300A012A;

    /** (300A,012C) VR=DS VM=3 Isocenter Position */
    public static final int IsocenterPosition = 0x300A012C;

    /** (300A,012E) VR=DS VM=3 Surface Entry Point */
    public static final int SurfaceEntryPoint = 0x300A012E;

    /** (300A,0130) VR=DS VM=1 Source to Surface Distance */
    public static final int SourceToSurfaceDistance = 0x300A0130;

    /** (300A,0131) VR=FL VM=1 Average Beam Dose Point Source to External Contour Distance */
    public static final int AverageBeamDosePointSourceToExternalContourDistance = 0x300A0131;

    /** (300A,0132) VR=FL VM=1 Source to External Contour Distance */
    public static final int SourceToExternalContourDistance = 0x300A0132;

    /** (300A,0133) VR=FL VM=3 External Contour Entry Point */
    public static final int ExternalContourEntryPoint = 0x300A0133;

    /** (300A,0134) VR=DS VM=1 Cumulative Meterset Weight */
    public static final int CumulativeMetersetWeight = 0x300A0134;

    /** (300A,0140) VR=FL VM=1 Table Top Pitch Angle */
    public static final int TableTopPitchAngle = 0x300A0140;

    /** (300A,0142) VR=CS VM=1 Table Top Pitch Rotation Direction */
    public static final int TableTopPitchRotationDirection = 0x300A0142;

    /** (300A,0144) VR=FL VM=1 Table Top Roll Angle */
    public static final int TableTopRollAngle = 0x300A0144;

    /** (300A,0146) VR=CS VM=1 Table Top Roll Rotation Direction */
    public static final int TableTopRollRotationDirection = 0x300A0146;

    /** (300A,0148) VR=FL VM=1 Head Fixation Angle */
    public static final int HeadFixationAngle = 0x300A0148;

    /** (300A,014A) VR=FL VM=1 Gantry Pitch Angle */
    public static final int GantryPitchAngle = 0x300A014A;

    /** (300A,014C) VR=CS VM=1 Gantry Pitch Rotation Direction */
    public static final int GantryPitchRotationDirection = 0x300A014C;

    /** (300A,014E) VR=FL VM=1 Gantry Pitch Angle Tolerance */
    public static final int GantryPitchAngleTolerance = 0x300A014E;

    /** (300A,0150) VR=CS VM=1 Fixation Eye */
    public static final int FixationEye = 0x300A0150;

    /** (300A,0151) VR=DS VM=1 Chair Head Frame Position */
    public static final int ChairHeadFramePosition = 0x300A0151;

    /** (300A,0152) VR=DS VM=1 Head Fixation Angle Tolerance */
    public static final int HeadFixationAngleTolerance = 0x300A0152;

    /** (300A,0153) VR=DS VM=1 Chair Head Frame Position Tolerance */
    public static final int ChairHeadFramePositionTolerance = 0x300A0153;

    /** (300A,0154) VR=DS VM=1 Fixation Light Azimuthal Angle Tolerance */
    public static final int FixationLightAzimuthalAngleTolerance = 0x300A0154;

    /** (300A,0155) VR=DS VM=1 Fixation Light Polar Angle Tolerance */
    public static final int FixationLightPolarAngleTolerance = 0x300A0155;

    /** (300A,0180) VR=SQ VM=1 Patient Setup Sequence */
    public static final int PatientSetupSequence = 0x300A0180;

    /** (300A,0182) VR=IS VM=1 Patient Setup Number */
    public static final int PatientSetupNumber = 0x300A0182;

    /** (300A,0183) VR=LO VM=1 Patient Setup Label */
    public static final int PatientSetupLabel = 0x300A0183;

    /** (300A,0184) VR=LO VM=1 Patient Additional Position */
    public static final int PatientAdditionalPosition = 0x300A0184;

    /** (300A,0190) VR=SQ VM=1 Fixation Device Sequence */
    public static final int FixationDeviceSequence = 0x300A0190;

    /** (300A,0192) VR=CS VM=1 Fixation Device Type */
    public static final int FixationDeviceType = 0x300A0192;

    /** (300A,0194) VR=SH VM=1 Fixation Device Label */
    public static final int FixationDeviceLabel = 0x300A0194;

    /** (300A,0196) VR=ST VM=1 Fixation Device Description */
    public static final int FixationDeviceDescription = 0x300A0196;

    /** (300A,0198) VR=SH VM=1 Fixation Device Position */
    public static final int FixationDevicePosition = 0x300A0198;

    /** (300A,0199) VR=FL VM=1 Fixation Device Pitch Angle */
    public static final int FixationDevicePitchAngle = 0x300A0199;

    /** (300A,019A) VR=FL VM=1 Fixation Device Roll Angle */
    public static final int FixationDeviceRollAngle = 0x300A019A;

    /** (300A,01A0) VR=SQ VM=1 Shielding Device Sequence */
    public static final int ShieldingDeviceSequence = 0x300A01A0;

    /** (300A,01A2) VR=CS VM=1 Shielding Device Type */
    public static final int ShieldingDeviceType = 0x300A01A2;

    /** (300A,01A4) VR=SH VM=1 Shielding Device Label */
    public static final int ShieldingDeviceLabel = 0x300A01A4;

    /** (300A,01A6) VR=ST VM=1 Shielding Device Description */
    public static final int ShieldingDeviceDescription = 0x300A01A6;

    /** (300A,01A8) VR=SH VM=1 Shielding Device Position */
    public static final int ShieldingDevicePosition = 0x300A01A8;

    /** (300A,01B0) VR=CS VM=1 Setup Technique */
    public static final int SetupTechnique = 0x300A01B0;

    /** (300A,01B2) VR=ST VM=1 Setup Technique Description */
    public static final int SetupTechniqueDescription = 0x300A01B2;

    /** (300A,01B4) VR=SQ VM=1 Setup Device Sequence */
    public static final int SetupDeviceSequence = 0x300A01B4;

    /** (300A,01B6) VR=CS VM=1 Setup Device Type */
    public static final int SetupDeviceType = 0x300A01B6;

    /** (300A,01B8) VR=SH VM=1 Setup Device Label */
    public static final int SetupDeviceLabel = 0x300A01B8;

    /** (300A,01BA) VR=ST VM=1 Setup Device Description */
    public static final int SetupDeviceDescription = 0x300A01BA;

    /** (300A,01BC) VR=DS VM=1 Setup Device Parameter */
    public static final int SetupDeviceParameter = 0x300A01BC;

    /** (300A,01D0) VR=ST VM=1 Setup Reference Description */
    public static final int SetupReferenceDescription = 0x300A01D0;

    /** (300A,01D2) VR=DS VM=1 Table Top Vertical Setup Displacement */
    public static final int TableTopVerticalSetupDisplacement = 0x300A01D2;

    /** (300A,01D4) VR=DS VM=1 Table Top Longitudinal Setup Displacement */
    public static final int TableTopLongitudinalSetupDisplacement = 0x300A01D4;

    /** (300A,01D6) VR=DS VM=1 Table Top Lateral Setup Displacement */
    public static final int TableTopLateralSetupDisplacement = 0x300A01D6;

    /** (300A,0200) VR=CS VM=1 Brachy Treatment Technique */
    public static final int BrachyTreatmentTechnique = 0x300A0200;

    /** (300A,0202) VR=CS VM=1 Brachy Treatment Type */
    public static final int BrachyTreatmentType = 0x300A0202;

    /** (300A,0206) VR=SQ VM=1 Treatment Machine Sequence */
    public static final int TreatmentMachineSequence = 0x300A0206;

    /** (300A,0210) VR=SQ VM=1 Source Sequence */
    public static final int SourceSequence = 0x300A0210;

    /** (300A,0212) VR=IS VM=1 Source Number */
    public static final int SourceNumber = 0x300A0212;

    /** (300A,0214) VR=CS VM=1 Source Type */
    public static final int SourceType = 0x300A0214;

    /** (300A,0216) VR=LO VM=1 Source Manufacturer */
    public static final int SourceManufacturer = 0x300A0216;

    /** (300A,0218) VR=DS VM=1 Active Source Diameter */
    public static final int ActiveSourceDiameter = 0x300A0218;

    /** (300A,021A) VR=DS VM=1 Active Source Length */
    public static final int ActiveSourceLength = 0x300A021A;

    /** (300A,021B) VR=SH VM=1 Source Model ID */
    public static final int SourceModelID = 0x300A021B;

    /** (300A,021C) VR=LO VM=1 Source Description */
    public static final int SourceDescription = 0x300A021C;

    /** (300A,0222) VR=DS VM=1 Source Encapsulation Nominal Thickness */
    public static final int SourceEncapsulationNominalThickness = 0x300A0222;

    /** (300A,0224) VR=DS VM=1 Source Encapsulation Nominal Transmission */
    public static final int SourceEncapsulationNominalTransmission = 0x300A0224;

    /** (300A,0226) VR=LO VM=1 Source Isotope Name */
    public static final int SourceIsotopeName = 0x300A0226;

    /** (300A,0228) VR=DS VM=1 Source Isotope Half Life */
    public static final int SourceIsotopeHalfLife = 0x300A0228;

    /** (300A,0229) VR=CS VM=1 Source Strength Units */
    public static final int SourceStrengthUnits = 0x300A0229;

    /** (300A,022A) VR=DS VM=1 Reference Air Kerma Rate */
    public static final int ReferenceAirKermaRate = 0x300A022A;

    /** (300A,022B) VR=DS VM=1 Source Strength */
    public static final int SourceStrength = 0x300A022B;

    /** (300A,022C) VR=DA VM=1 Source Strength Reference Date */
    public static final int SourceStrengthReferenceDate = 0x300A022C;

    /** (300A,022E) VR=TM VM=1 Source Strength Reference Time */
    public static final int SourceStrengthReferenceTime = 0x300A022E;

    /** (300A,0230) VR=SQ VM=1 Application Setup Sequence */
    public static final int ApplicationSetupSequence = 0x300A0230;

    /** (300A,0232) VR=CS VM=1 Application Setup Type */
    public static final int ApplicationSetupType = 0x300A0232;

    /** (300A,0234) VR=IS VM=1 Application Setup Number */
    public static final int ApplicationSetupNumber = 0x300A0234;

    /** (300A,0236) VR=LO VM=1 Application Setup Name */
    public static final int ApplicationSetupName = 0x300A0236;

    /** (300A,0238) VR=LO VM=1 Application Setup Manufacturer */
    public static final int ApplicationSetupManufacturer = 0x300A0238;

    /** (300A,0240) VR=IS VM=1 Template Number */
    public static final int TemplateNumber = 0x300A0240;

    /** (300A,0242) VR=SH VM=1 Template Type */
    public static final int TemplateType = 0x300A0242;

    /** (300A,0244) VR=LO VM=1 Template Name */
    public static final int TemplateName = 0x300A0244;

    /** (300A,0250) VR=DS VM=1 Total Reference Air Kerma */
    public static final int TotalReferenceAirKerma = 0x300A0250;

    /** (300A,0260) VR=SQ VM=1 Brachy Accessory Device Sequence */
    public static final int BrachyAccessoryDeviceSequence = 0x300A0260;

    /** (300A,0262) VR=IS VM=1 Brachy Accessory Device Number */
    public static final int BrachyAccessoryDeviceNumber = 0x300A0262;

    /** (300A,0263) VR=SH VM=1 Brachy Accessory Device ID */
    public static final int BrachyAccessoryDeviceID = 0x300A0263;

    /** (300A,0264) VR=CS VM=1 Brachy Accessory Device Type */
    public static final int BrachyAccessoryDeviceType = 0x300A0264;

    /** (300A,0266) VR=LO VM=1 Brachy Accessory Device Name */
    public static final int BrachyAccessoryDeviceName = 0x300A0266;

    /** (300A,026A) VR=DS VM=1 Brachy Accessory Device Nominal Thickness */
    public static final int BrachyAccessoryDeviceNominalThickness = 0x300A026A;

    /** (300A,026C) VR=DS VM=1 Brachy Accessory Device Nominal Transmission */
    public static final int BrachyAccessoryDeviceNominalTransmission = 0x300A026C;

    /** (300A,0271) VR=DS VM=1 Channel Effective Length */
    public static final int ChannelEffectiveLength = 0x300A0271;

    /** (300A,0272) VR=DS VM=1 Channel Inner Length */
    public static final int ChannelInnerLength = 0x300A0272;

    /** (300A,0273) VR=SH VM=1 Afterloader Channel ID */
    public static final int AfterloaderChannelID = 0x300A0273;

    /** (300A,0274) VR=DS VM=1 Source Applicator Tip Length */
    public static final int SourceApplicatorTipLength = 0x300A0274;

    /** (300A,0280) VR=SQ VM=1 Channel Sequence */
    public static final int ChannelSequence = 0x300A0280;

    /** (300A,0282) VR=IS VM=1 Channel Number */
    public static final int ChannelNumber = 0x300A0282;

    /** (300A,0284) VR=DS VM=1 Channel Length */
    public static final int ChannelLength = 0x300A0284;

    /** (300A,0286) VR=DS VM=1 Channel Total Time */
    public static final int ChannelTotalTime = 0x300A0286;

    /** (300A,0288) VR=CS VM=1 Source Movement Type */
    public static final int SourceMovementType = 0x300A0288;

    /** (300A,028A) VR=IS VM=1 Number of Pulses */
    public static final int NumberOfPulses = 0x300A028A;

    /** (300A,028C) VR=DS VM=1 Pulse Repetition Interval */
    public static final int PulseRepetitionInterval = 0x300A028C;

    /** (300A,0290) VR=IS VM=1 Source Applicator Number */
    public static final int SourceApplicatorNumber = 0x300A0290;

    /** (300A,0291) VR=SH VM=1 Source Applicator ID */
    public static final int SourceApplicatorID = 0x300A0291;

    /** (300A,0292) VR=CS VM=1 Source Applicator Type */
    public static final int SourceApplicatorType = 0x300A0292;

    /** (300A,0294) VR=LO VM=1 Source Applicator Name */
    public static final int SourceApplicatorName = 0x300A0294;

    /** (300A,0296) VR=DS VM=1 Source Applicator Length */
    public static final int SourceApplicatorLength = 0x300A0296;

    /** (300A,0298) VR=LO VM=1 Source Applicator Manufacturer */
    public static final int SourceApplicatorManufacturer = 0x300A0298;

    /** (300A,029C) VR=DS VM=1 Source Applicator Wall Nominal Thickness */
    public static final int SourceApplicatorWallNominalThickness = 0x300A029C;

    /** (300A,029E) VR=DS VM=1 Source Applicator Wall Nominal Transmission */
    public static final int SourceApplicatorWallNominalTransmission = 0x300A029E;

    /** (300A,02A0) VR=DS VM=1 Source Applicator Step Size */
    public static final int SourceApplicatorStepSize = 0x300A02A0;

    /** (300A,02A2) VR=IS VM=1 Transfer Tube Number */
    public static final int TransferTubeNumber = 0x300A02A2;

    /** (300A,02A4) VR=DS VM=1 Transfer Tube Length */
    public static final int TransferTubeLength = 0x300A02A4;

    /** (300A,02B0) VR=SQ VM=1 Channel Shield Sequence */
    public static final int ChannelShieldSequence = 0x300A02B0;

    /** (300A,02B2) VR=IS VM=1 Channel Shield Number */
    public static final int ChannelShieldNumber = 0x300A02B2;

    /** (300A,02B3) VR=SH VM=1 Channel Shield ID */
    public static final int ChannelShieldID = 0x300A02B3;

    /** (300A,02B4) VR=LO VM=1 Channel Shield Name */
    public static final int ChannelShieldName = 0x300A02B4;

    /** (300A,02B8) VR=DS VM=1 Channel Shield Nominal Thickness */
    public static final int ChannelShieldNominalThickness = 0x300A02B8;

    /** (300A,02BA) VR=DS VM=1 Channel Shield Nominal Transmission */
    public static final int ChannelShieldNominalTransmission = 0x300A02BA;

    /** (300A,02C8) VR=DS VM=1 Final Cumulative Time Weight */
    public static final int FinalCumulativeTimeWeight = 0x300A02C8;

    /** (300A,02D0) VR=SQ VM=1 Brachy Control Point Sequence */
    public static final int BrachyControlPointSequence = 0x300A02D0;

    /** (300A,02D2) VR=DS VM=1 Control Point Relative Position */
    public static final int ControlPointRelativePosition = 0x300A02D2;

    /** (300A,02D4) VR=DS VM=3 Control Point 3D Position */
    public static final int ControlPoint3DPosition = 0x300A02D4;

    /** (300A,02D6) VR=DS VM=1 Cumulative Time Weight */
    public static final int CumulativeTimeWeight = 0x300A02D6;

    /** (300A,02E0) VR=CS VM=1 Compensator Divergence */
    public static final int CompensatorDivergence = 0x300A02E0;

    /** (300A,02E1) VR=CS VM=1 Compensator Mounting Position */
    public static final int CompensatorMountingPosition = 0x300A02E1;

    /** (300A,02E2) VR=DS VM=1-n Source to Compensator Distance */
    public static final int SourceToCompensatorDistance = 0x300A02E2;

    /** (300A,02E3) VR=FL VM=1 Total Compensator Tray Water-Equivalent Thickness */
    public static final int TotalCompensatorTrayWaterEquivalentThickness = 0x300A02E3;

    /** (300A,02E4) VR=FL VM=1 Isocenter to Compensator Tray Distance */
    public static final int IsocenterToCompensatorTrayDistance = 0x300A02E4;

    /** (300A,02E5) VR=FL VM=1 Compensator Column Offset */
    public static final int CompensatorColumnOffset = 0x300A02E5;

    /** (300A,02E6) VR=FL VM=1-n Isocenter to Compensator Distances */
    public static final int IsocenterToCompensatorDistances = 0x300A02E6;

    /** (300A,02E7) VR=FL VM=1 Compensator Relative Stopping Power Ratio */
    public static final int CompensatorRelativeStoppingPowerRatio = 0x300A02E7;

    /** (300A,02E8) VR=FL VM=1 Compensator Milling Tool Diameter */
    public static final int CompensatorMillingToolDiameter = 0x300A02E8;

    /** (300A,02EA) VR=SQ VM=1 Ion Range Compensator Sequence */
    public static final int IonRangeCompensatorSequence = 0x300A02EA;

    /** (300A,02EB) VR=LT VM=1 Compensator Description */
    public static final int CompensatorDescription = 0x300A02EB;

    /** (300A,0302) VR=IS VM=1 Radiation Mass Number */
    public static final int RadiationMassNumber = 0x300A0302;

    /** (300A,0304) VR=IS VM=1 Radiation Atomic Number */
    public static final int RadiationAtomicNumber = 0x300A0304;

    /** (300A,0306) VR=SS VM=1 Radiation Charge State */
    public static final int RadiationChargeState = 0x300A0306;

    /** (300A,0308) VR=CS VM=1 Scan Mode */
    public static final int ScanMode = 0x300A0308;

    /** (300A,0309) VR=CS VM=1 Modulated Scan Mode Type */
    public static final int ModulatedScanModeType = 0x300A0309;

    /** (300A,030A) VR=FL VM=2 Virtual Source-Axis Distances */
    public static final int VirtualSourceAxisDistances = 0x300A030A;

    /** (300A,030C) VR=SQ VM=1 Snout Sequence */
    public static final int SnoutSequence = 0x300A030C;

    /** (300A,030D) VR=FL VM=1 Snout Position */
    public static final int SnoutPosition = 0x300A030D;

    /** (300A,030F) VR=SH VM=1 Snout ID */
    public static final int SnoutID = 0x300A030F;

    /** (300A,0312) VR=IS VM=1 Number of Range Shifters */
    public static final int NumberOfRangeShifters = 0x300A0312;

    /** (300A,0314) VR=SQ VM=1 Range Shifter Sequence */
    public static final int RangeShifterSequence = 0x300A0314;

    /** (300A,0316) VR=IS VM=1 Range Shifter Number */
    public static final int RangeShifterNumber = 0x300A0316;

    /** (300A,0318) VR=SH VM=1 Range Shifter ID */
    public static final int RangeShifterID = 0x300A0318;

    /** (300A,0320) VR=CS VM=1 Range Shifter Type */
    public static final int RangeShifterType = 0x300A0320;

    /** (300A,0322) VR=LO VM=1 Range Shifter Description */
    public static final int RangeShifterDescription = 0x300A0322;

    /** (300A,0330) VR=IS VM=1 Number of Lateral Spreading Devices */
    public static final int NumberOfLateralSpreadingDevices = 0x300A0330;

    /** (300A,0332) VR=SQ VM=1 Lateral Spreading Device Sequence */
    public static final int LateralSpreadingDeviceSequence = 0x300A0332;

    /** (300A,0334) VR=IS VM=1 Lateral Spreading Device Number */
    public static final int LateralSpreadingDeviceNumber = 0x300A0334;

    /** (300A,0336) VR=SH VM=1 Lateral Spreading Device ID */
    public static final int LateralSpreadingDeviceID = 0x300A0336;

    /** (300A,0338) VR=CS VM=1 Lateral Spreading Device Type */
    public static final int LateralSpreadingDeviceType = 0x300A0338;

    /** (300A,033A) VR=LO VM=1 Lateral Spreading Device Description */
    public static final int LateralSpreadingDeviceDescription = 0x300A033A;

    /** (300A,033C) VR=FL VM=1 Lateral Spreading Device Water Equivalent Thickness */
    public static final int LateralSpreadingDeviceWaterEquivalentThickness = 0x300A033C;

    /** (300A,0340) VR=IS VM=1 Number of Range Modulators */
    public static final int NumberOfRangeModulators = 0x300A0340;

    /** (300A,0342) VR=SQ VM=1 Range Modulator Sequence */
    public static final int RangeModulatorSequence = 0x300A0342;

    /** (300A,0344) VR=IS VM=1 Range Modulator Number */
    public static final int RangeModulatorNumber = 0x300A0344;

    /** (300A,0346) VR=SH VM=1 Range Modulator ID */
    public static final int RangeModulatorID = 0x300A0346;

    /** (300A,0348) VR=CS VM=1 Range Modulator Type */
    public static final int RangeModulatorType = 0x300A0348;

    /** (300A,034A) VR=LO VM=1 Range Modulator Description */
    public static final int RangeModulatorDescription = 0x300A034A;

    /** (300A,034C) VR=SH VM=1 Beam Current Modulation ID */
    public static final int BeamCurrentModulationID = 0x300A034C;

    /** (300A,0350) VR=CS VM=1 Patient Support Type */
    public static final int PatientSupportType = 0x300A0350;

    /** (300A,0352) VR=SH VM=1 Patient Support ID */
    public static final int PatientSupportID = 0x300A0352;

    /** (300A,0354) VR=LO VM=1 Patient Support Accessory Code */
    public static final int PatientSupportAccessoryCode = 0x300A0354;

    /** (300A,0355) VR=LO VM=1 Tray Accessory Code */
    public static final int TrayAccessoryCode = 0x300A0355;

    /** (300A,0356) VR=FL VM=1 Fixation Light Azimuthal Angle */
    public static final int FixationLightAzimuthalAngle = 0x300A0356;

    /** (300A,0358) VR=FL VM=1 Fixation Light Polar Angle */
    public static final int FixationLightPolarAngle = 0x300A0358;

    /** (300A,035A) VR=FL VM=1 Meterset Rate */
    public static final int MetersetRate = 0x300A035A;

    /** (300A,0360) VR=SQ VM=1 Range Shifter Settings Sequence */
    public static final int RangeShifterSettingsSequence = 0x300A0360;

    /** (300A,0362) VR=LO VM=1 Range Shifter Setting */
    public static final int RangeShifterSetting = 0x300A0362;

    /** (300A,0364) VR=FL VM=1 Isocenter to Range Shifter Distance */
    public static final int IsocenterToRangeShifterDistance = 0x300A0364;

    /** (300A,0366) VR=FL VM=1 Range Shifter Water Equivalent Thickness */
    public static final int RangeShifterWaterEquivalentThickness = 0x300A0366;

    /** (300A,0370) VR=SQ VM=1 Lateral Spreading Device Settings Sequence */
    public static final int LateralSpreadingDeviceSettingsSequence = 0x300A0370;

    /** (300A,0372) VR=LO VM=1 Lateral Spreading Device Setting */
    public static final int LateralSpreadingDeviceSetting = 0x300A0372;

    /** (300A,0374) VR=FL VM=1 Isocenter to Lateral Spreading Device Distance */
    public static final int IsocenterToLateralSpreadingDeviceDistance = 0x300A0374;

    /** (300A,0380) VR=SQ VM=1 Range Modulator Settings Sequence */
    public static final int RangeModulatorSettingsSequence = 0x300A0380;

    /** (300A,0382) VR=FL VM=1 Range Modulator Gating Start Value */
    public static final int RangeModulatorGatingStartValue = 0x300A0382;

    /** (300A,0384) VR=FL VM=1 Range Modulator Gating Stop Value */
    public static final int RangeModulatorGatingStopValue = 0x300A0384;

    /** (300A,0386) VR=FL VM=1 Range Modulator Gating Start Water Equivalent Thickness */
    public static final int RangeModulatorGatingStartWaterEquivalentThickness = 0x300A0386;

    /** (300A,0388) VR=FL VM=1 Range Modulator Gating Stop Water Equivalent Thickness */
    public static final int RangeModulatorGatingStopWaterEquivalentThickness = 0x300A0388;

    /** (300A,038A) VR=FL VM=1 Isocenter to Range Modulator Distance */
    public static final int IsocenterToRangeModulatorDistance = 0x300A038A;

    /** (300A,038F) VR=FL VM=1-n Scan Spot Time Offset */
    public static final int ScanSpotTimeOffset = 0x300A038F;

    /** (300A,0390) VR=SH VM=1 Scan Spot Tune ID */
    public static final int ScanSpotTuneID = 0x300A0390;

    /** (300A,0391) VR=IS VM=1-n Scan Spot Prescribed Indices */
    public static final int ScanSpotPrescribedIndices = 0x300A0391;

    /** (300A,0392) VR=IS VM=1 Number of Scan Spot Positions */
    public static final int NumberOfScanSpotPositions = 0x300A0392;

    /** (300A,0393) VR=CS VM=1 Scan Spot Reordered */
    public static final int ScanSpotReordered = 0x300A0393;

    /** (300A,0394) VR=FL VM=1-n Scan Spot Position Map */
    public static final int ScanSpotPositionMap = 0x300A0394;

    /** (300A,0395) VR=CS VM=1 Scan Spot Reordering Allowed */
    public static final int ScanSpotReorderingAllowed = 0x300A0395;

    /** (300A,0396) VR=FL VM=1-n Scan Spot Meterset Weights */
    public static final int ScanSpotMetersetWeights = 0x300A0396;

    /** (300A,0398) VR=FL VM=2 Scanning Spot Size */
    public static final int ScanningSpotSize = 0x300A0398;

    /** (300A,0399) VR=FL VM=2-2N Scan Spot Sizes Delivered */
    public static final int ScanSpotSizesDelivered = 0x300A0399;

    /** (300A,039A) VR=IS VM=1 Number of Paintings */
    public static final int NumberOfPaintings = 0x300A039A;

    /** (300A,03A0) VR=SQ VM=1 Ion Tolerance Table Sequence */
    public static final int IonToleranceTableSequence = 0x300A03A0;

    /** (300A,03A2) VR=SQ VM=1 Ion Beam Sequence */
    public static final int IonBeamSequence = 0x300A03A2;

    /** (300A,03A4) VR=SQ VM=1 Ion Beam Limiting Device Sequence */
    public static final int IonBeamLimitingDeviceSequence = 0x300A03A4;

    /** (300A,03A6) VR=SQ VM=1 Ion Block Sequence */
    public static final int IonBlockSequence = 0x300A03A6;

    /** (300A,03A8) VR=SQ VM=1 Ion Control Point Sequence */
    public static final int IonControlPointSequence = 0x300A03A8;

    /** (300A,03AA) VR=SQ VM=1 Ion Wedge Sequence */
    public static final int IonWedgeSequence = 0x300A03AA;

    /** (300A,03AC) VR=SQ VM=1 Ion Wedge Position Sequence */
    public static final int IonWedgePositionSequence = 0x300A03AC;

    /** (300A,0401) VR=SQ VM=1 Referenced Setup Image Sequence */
    public static final int ReferencedSetupImageSequence = 0x300A0401;

    /** (300A,0402) VR=ST VM=1 Setup Image Comment */
    public static final int SetupImageComment = 0x300A0402;

    /** (300A,0410) VR=SQ VM=1 Motion Synchronization Sequence */
    public static final int MotionSynchronizationSequence = 0x300A0410;

    /** (300A,0412) VR=FL VM=3 Control Point Orientation */
    public static final int ControlPointOrientation = 0x300A0412;

    /** (300A,0420) VR=SQ VM=1 General Accessory Sequence */
    public static final int GeneralAccessorySequence = 0x300A0420;

    /** (300A,0421) VR=SH VM=1 General Accessory ID */
    public static final int GeneralAccessoryID = 0x300A0421;

    /** (300A,0422) VR=ST VM=1 General Accessory Description */
    public static final int GeneralAccessoryDescription = 0x300A0422;

    /** (300A,0423) VR=CS VM=1 General Accessory Type */
    public static final int GeneralAccessoryType = 0x300A0423;

    /** (300A,0424) VR=IS VM=1 General Accessory Number */
    public static final int GeneralAccessoryNumber = 0x300A0424;

    /** (300A,0425) VR=FL VM=1 Source to General Accessory Distance */
    public static final int SourceToGeneralAccessoryDistance = 0x300A0425;

    /** (300A,0426) VR=DS VM=1 Isocenter to General Accessory Distance */
    public static final int IsocenterToGeneralAccessoryDistance = 0x300A0426;

    /** (300A,0431) VR=SQ VM=1 Applicator Geometry Sequence */
    public static final int ApplicatorGeometrySequence = 0x300A0431;

    /** (300A,0432) VR=CS VM=1 Applicator Aperture Shape */
    public static final int ApplicatorApertureShape = 0x300A0432;

    /** (300A,0433) VR=FL VM=1 Applicator Opening */
    public static final int ApplicatorOpening = 0x300A0433;

    /** (300A,0434) VR=FL VM=1 Applicator Opening X */
    public static final int ApplicatorOpeningX = 0x300A0434;

    /** (300A,0435) VR=FL VM=1 Applicator Opening Y */
    public static final int ApplicatorOpeningY = 0x300A0435;

    /** (300A,0436) VR=FL VM=1 Source to Applicator Mounting Position Distance */
    public static final int SourceToApplicatorMountingPositionDistance = 0x300A0436;

    /** (300A,0440) VR=IS VM=1 Number of Block Slab Items */
    public static final int NumberOfBlockSlabItems = 0x300A0440;

    /** (300A,0441) VR=SQ VM=1 Block Slab Sequence */
    public static final int BlockSlabSequence = 0x300A0441;

    /** (300A,0442) VR=DS VM=1 Block Slab Thickness */
    public static final int BlockSlabThickness = 0x300A0442;

    /** (300A,0443) VR=US VM=1 Block Slab Number */
    public static final int BlockSlabNumber = 0x300A0443;

    /** (300A,0450) VR=SQ VM=1 Device Motion Control Sequence */
    public static final int DeviceMotionControlSequence = 0x300A0450;

    /** (300A,0451) VR=CS VM=1 Device Motion Execution Mode */
    public static final int DeviceMotionExecutionMode = 0x300A0451;

    /** (300A,0452) VR=CS VM=1 Device Motion Observation Mode */
    public static final int DeviceMotionObservationMode = 0x300A0452;

    /** (300A,0453) VR=SQ VM=1 Device Motion Parameter Code Sequence */
    public static final int DeviceMotionParameterCodeSequence = 0x300A0453;

    /** (300A,0501) VR=FL VM=1 Distal Depth Fraction */
    public static final int DistalDepthFraction = 0x300A0501;

    /** (300A,0502) VR=FL VM=1 Distal Depth */
    public static final int DistalDepth = 0x300A0502;

    /** (300A,0503) VR=FL VM=2 Nominal Range Modulation Fractions */
    public static final int NominalRangeModulationFractions = 0x300A0503;

    /** (300A,0504) VR=FL VM=2 Nominal Range Modulated Region Depths */
    public static final int NominalRangeModulatedRegionDepths = 0x300A0504;

    /** (300A,0505) VR=SQ VM=1 Depth Dose Parameters Sequence */
    public static final int DepthDoseParametersSequence = 0x300A0505;

    /** (300A,0506) VR=SQ VM=1 Delivered Depth Dose Parameters Sequence */
    public static final int DeliveredDepthDoseParametersSequence = 0x300A0506;

    /** (300A,0507) VR=FL VM=1 Delivered Distal Depth Fraction */
    public static final int DeliveredDistalDepthFraction = 0x300A0507;

    /** (300A,0508) VR=FL VM=1 Delivered Distal Depth */
    public static final int DeliveredDistalDepth = 0x300A0508;

    /** (300A,0509) VR=FL VM=2 Delivered Nominal Range Modulation Fractions */
    public static final int DeliveredNominalRangeModulationFractions = 0x300A0509;

    /** (300A,0510) VR=FL VM=2 Delivered Nominal Range Modulated Region Depths */
    public static final int DeliveredNominalRangeModulatedRegionDepths = 0x300A0510;

    /** (300A,0511) VR=CS VM=1 Delivered Reference Dose Definition */
    public static final int DeliveredReferenceDoseDefinition = 0x300A0511;

    /** (300A,0512) VR=CS VM=1 Reference Dose Definition */
    public static final int ReferenceDoseDefinition = 0x300A0512;

    /** (300A,0600) VR=US VM=1 RT Control Point Index */
    public static final int RTControlPointIndex = 0x300A0600;

    /** (300A,0601) VR=US VM=1 Radiation Generation Mode Index */
    public static final int RadiationGenerationModeIndex = 0x300A0601;

    /** (300A,0602) VR=US VM=1 Referenced Defined Device Index */
    public static final int ReferencedDefinedDeviceIndex = 0x300A0602;

    /** (300A,0603) VR=US VM=1 Radiation Dose Identification Index */
    public static final int RadiationDoseIdentificationIndex = 0x300A0603;

    /** (300A,0604) VR=US VM=1 Number of RT Control Points */
    public static final int NumberOfRTControlPoints = 0x300A0604;

    /** (300A,0605) VR=US VM=1 Referenced Radiation Generation Mode Index */
    public static final int ReferencedRadiationGenerationModeIndex = 0x300A0605;

    /** (300A,0606) VR=US VM=1 Treatment Position Index */
    public static final int TreatmentPositionIndex = 0x300A0606;

    /** (300A,0607) VR=US VM=1 Referenced Device Index */
    public static final int ReferencedDeviceIndex = 0x300A0607;

    /** (300A,0608) VR=LO VM=1 Treatment Position Group Label */
    public static final int TreatmentPositionGroupLabel = 0x300A0608;

    /** (300A,0609) VR=UI VM=1 Treatment Position Group UID */
    public static final int TreatmentPositionGroupUID = 0x300A0609;

    /** (300A,060A) VR=SQ VM=1 Treatment Position Group Sequence */
    public static final int TreatmentPositionGroupSequence = 0x300A060A;

    /** (300A,060B) VR=US VM=1 Referenced Treatment Position Index */
    public static final int ReferencedTreatmentPositionIndex = 0x300A060B;

    /** (300A,060C) VR=US VM=1 Referenced Radiation Dose Identification Index */
    public static final int ReferencedRadiationDoseIdentificationIndex = 0x300A060C;

    /** (300A,060D) VR=FD VM=1 RT Accessory Holder Water-Equivalent Thickness */
    public static final int RTAccessoryHolderWaterEquivalentThickness = 0x300A060D;

    /** (300A,060E) VR=US VM=1 Referenced RT Accessory Holder Device Index */
    public static final int ReferencedRTAccessoryHolderDeviceIndex = 0x300A060E;

    /** (300A,060F) VR=CS VM=1 RT Accessory Holder Slot Existence Flag */
    public static final int RTAccessoryHolderSlotExistenceFlag = 0x300A060F;

    /** (300A,0610) VR=SQ VM=1 RT Accessory Holder Slot Sequence */
    public static final int RTAccessoryHolderSlotSequence = 0x300A0610;

    /** (300A,0611) VR=LO VM=1 RT Accessory Holder Slot ID */
    public static final int RTAccessoryHolderSlotID = 0x300A0611;

    /** (300A,0612) VR=FD VM=1 RT Accessory Holder Slot Distance */
    public static final int RTAccessoryHolderSlotDistance = 0x300A0612;

    /** (300A,0613) VR=FD VM=1 RT Accessory Slot Distance */
    public static final int RTAccessorySlotDistance = 0x300A0613;

    /** (300A,0614) VR=SQ VM=1 RT Accessory Holder Definition Sequence */
    public static final int RTAccessoryHolderDefinitionSequence = 0x300A0614;

    /** (300A,0615) VR=LO VM=1 RT Accessory Device Slot ID */
    public static final int RTAccessoryDeviceSlotID = 0x300A0615;

    /** (300A,0616) VR=SQ VM=1 RT Radiation Sequence */
    public static final int RTRadiationSequence = 0x300A0616;

    /** (300A,0617) VR=SQ VM=1 Radiation Dose Sequence */
    public static final int RadiationDoseSequence = 0x300A0617;

    /** (300A,0618) VR=SQ VM=1 Radiation Dose Identification Sequence */
    public static final int RadiationDoseIdentificationSequence = 0x300A0618;

    /** (300A,0619) VR=LO VM=1 Radiation Dose Identification Label */
    public static final int RadiationDoseIdentificationLabel = 0x300A0619;

    /** (300A,061A) VR=CS VM=1 Reference Dose Type */
    public static final int ReferenceDoseType = 0x300A061A;

    /** (300A,061B) VR=CS VM=1 Primary Dose Value Indicator */
    public static final int PrimaryDoseValueIndicator = 0x300A061B;

    /** (300A,061C) VR=SQ VM=1 Dose Values Sequence */
    public static final int DoseValuesSequence = 0x300A061C;

    /** (300A,061D) VR=CS VM=1-n Dose Value Purpose */
    public static final int DoseValuePurpose = 0x300A061D;

    /** (300A,061E) VR=FD VM=3 Reference Dose Point Coordinates */
    public static final int ReferenceDosePointCoordinates = 0x300A061E;

    /** (300A,061F) VR=SQ VM=1 Radiation Dose Values Parameters Sequence */
    public static final int RadiationDoseValuesParametersSequence = 0x300A061F;

    /** (300A,0620) VR=SQ VM=1 Meterset to Dose Mapping Sequence */
    public static final int MetersetToDoseMappingSequence = 0x300A0620;

    /** (300A,0621) VR=SQ VM=1 Expected In-Vivo Measurement Values Sequence */
    public static final int ExpectedInVivoMeasurementValuesSequence = 0x300A0621;

    /** (300A,0622) VR=US VM=1 Expected In-Vivo Measurement Value Index */
    public static final int ExpectedInVivoMeasurementValueIndex = 0x300A0622;

    /** (300A,0623) VR=LO VM=1 Radiation Dose In-Vivo Measurement Label */
    public static final int RadiationDoseInVivoMeasurementLabel = 0x300A0623;

    /** (300A,0624) VR=FD VM=2 Radiation Dose Central Axis Displacement */
    public static final int RadiationDoseCentralAxisDisplacement = 0x300A0624;

    /** (300A,0625) VR=FD VM=1 Radiation Dose Value */
    public static final int RadiationDoseValue = 0x300A0625;

    /** (300A,0626) VR=FD VM=1 Radiation Dose Source to Skin Distance */
    public static final int RadiationDoseSourceToSkinDistance = 0x300A0626;

    /** (300A,0627) VR=FD VM=3 Radiation Dose Measurement Point Coordinates */
    public static final int RadiationDoseMeasurementPointCoordinates = 0x300A0627;

    /** (300A,0628) VR=FD VM=1 Radiation Dose Source to External Contour Distance */
    public static final int RadiationDoseSourceToExternalContourDistance = 0x300A0628;

    /** (300A,0629) VR=SQ VM=1 RT Tolerance Set Sequence */
    public static final int RTToleranceSetSequence = 0x300A0629;

    /** (300A,062A) VR=LO VM=1 RT Tolerance Set Label */
    public static final int RTToleranceSetLabel = 0x300A062A;

    /** (300A,062B) VR=SQ VM=1 Attribute Tolerance Values Sequence */
    public static final int AttributeToleranceValuesSequence = 0x300A062B;

    /** (300A,062C) VR=FD VM=1 Tolerance Value */
    public static final int ToleranceValue = 0x300A062C;

    /** (300A,062D) VR=SQ VM=1 Patient Support Position Tolerance Sequence */
    public static final int PatientSupportPositionToleranceSequence = 0x300A062D;

    /** (300A,062E) VR=FD VM=1 Treatment Time Limit */
    public static final int TreatmentTimeLimit = 0x300A062E;

    /** (300A,062F) VR=SQ VM=1 C-Arm Photon-Electron Control Point Sequence */
    public static final int CArmPhotonElectronControlPointSequence = 0x300A062F;

    /** (300A,0630) VR=SQ VM=1 Referenced RT Radiation Sequence */
    public static final int ReferencedRTRadiationSequence = 0x300A0630;

    /** (300A,0631) VR=SQ VM=1 Referenced RT Instance Sequence */
    public static final int ReferencedRTInstanceSequence = 0x300A0631;

    /** (300A,0632) VR=SQ VM=1 Referenced RT Patient Setup Sequence */
    public static final int ReferencedRTPatientSetupSequence = 0x300A0632;

    /** (300A,0634) VR=FD VM=1 Source to Patient Surface Distance */
    public static final int SourceToPatientSurfaceDistance = 0x300A0634;

    /** (300A,0635) VR=SQ VM=1 Treatment Machine Special Mode Code Sequence */
    public static final int TreatmentMachineSpecialModeCodeSequence = 0x300A0635;

    /** (300A,0636) VR=US VM=1 Intended Number of Fractions */
    public static final int IntendedNumberOfFractions = 0x300A0636;

    /** (300A,0637) VR=CS VM=1 RT Radiation Set Intent */
    public static final int RTRadiationSetIntent = 0x300A0637;

    /** (300A,0638) VR=CS VM=1 RT Radiation Physical and Geometric Content Detail Flag */
    public static final int RTRadiationPhysicalAndGeometricContentDetailFlag = 0x300A0638;

    /** (300A,0639) VR=CS VM=1 RT Record Flag */
    public static final int RTRecordFlag = 0x300A0639;

    /** (300A,063A) VR=SQ VM=1 Treatment Device Identification Sequence */
    public static final int TreatmentDeviceIdentificationSequence = 0x300A063A;

    /** (300A,063B) VR=SQ VM=1 Referenced RT Physician Intent Sequence */
    public static final int ReferencedRTPhysicianIntentSequence = 0x300A063B;

    /** (300A,063C) VR=FD VM=1 Cumulative Meterset */
    public static final int CumulativeMeterset = 0x300A063C;

    /** (300A,063D) VR=FD VM=1 Delivery Rate */
    public static final int DeliveryRate = 0x300A063D;

    /** (300A,063E) VR=SQ VM=1 Delivery Rate Unit Sequence */
    public static final int DeliveryRateUnitSequence = 0x300A063E;

    /** (300A,063F) VR=SQ VM=1 Treatment Position Sequence */
    public static final int TreatmentPositionSequence = 0x300A063F;

    /** (300A,0640) VR=FD VM=1 Radiation Source-Axis Distance */
    public static final int RadiationSourceAxisDistance = 0x300A0640;

    /** (300A,0641) VR=US VM=1 Number of RT Beam Limiting Devices */
    public static final int NumberOfRTBeamLimitingDevices = 0x300A0641;

    /** (300A,0642) VR=FD VM=1 RT Beam Limiting Device Proximal Distance */
    public static final int RTBeamLimitingDeviceProximalDistance = 0x300A0642;

    /** (300A,0643) VR=FD VM=1 RT Beam Limiting Device Distal Distance */
    public static final int RTBeamLimitingDeviceDistalDistance = 0x300A0643;

    /** (300A,0644) VR=SQ VM=1 Parallel RT Beam Delimiter Device Orientation Label Code Sequence */
    public static final int ParallelRTBeamDelimiterDeviceOrientationLabelCodeSequence = 0x300A0644;

    /** (300A,0645) VR=FD VM=1 Beam Modifier Orientation Angle */
    public static final int BeamModifierOrientationAngle = 0x300A0645;

    /** (300A,0646) VR=SQ VM=1 Fixed RT Beam Delimiter Device Sequence */
    public static final int FixedRTBeamDelimiterDeviceSequence = 0x300A0646;

    /** (300A,0647) VR=SQ VM=1 Parallel RT Beam Delimiter Device Sequence */
    public static final int ParallelRTBeamDelimiterDeviceSequence = 0x300A0647;

    /** (300A,0648) VR=US VM=1 Number of Parallel RT Beam Delimiters */
    public static final int NumberOfParallelRTBeamDelimiters = 0x300A0648;

    /** (300A,0649) VR=FD VM=2-n Parallel RT Beam Delimiter Boundaries */
    public static final int ParallelRTBeamDelimiterBoundaries = 0x300A0649;

    /** (300A,064A) VR=FD VM=2-n Parallel RT Beam Delimiter Positions */
    public static final int ParallelRTBeamDelimiterPositions = 0x300A064A;

    /** (300A,064B) VR=FD VM=2 RT Beam Limiting Device Offset */
    public static final int RTBeamLimitingDeviceOffset = 0x300A064B;

    /** (300A,064C) VR=SQ VM=1 RT Beam Delimiter Geometry Sequence */
    public static final int RTBeamDelimiterGeometrySequence = 0x300A064C;

    /** (300A,064D) VR=SQ VM=1 RT Beam Limiting Device Definition Sequence */
    public static final int RTBeamLimitingDeviceDefinitionSequence = 0x300A064D;

    /** (300A,064E) VR=CS VM=1 Parallel RT Beam Delimiter Opening Mode */
    public static final int ParallelRTBeamDelimiterOpeningMode = 0x300A064E;

    /** (300A,064F) VR=CS VM=1-n Parallel RT Beam Delimiter Leaf Mounting Side */
    public static final int ParallelRTBeamDelimiterLeafMountingSide = 0x300A064F;

    /** (300A,0650) VR=UI VM=1 Patient Setup UID */
    public static final int PatientSetupUID = 0x300A0650;

    /** (300A,0651) VR=SQ VM=1 Wedge Definition Sequence */
    public static final int WedgeDefinitionSequence = 0x300A0651;

    /** (300A,0652) VR=FD VM=1 Radiation Beam Wedge Angle */
    public static final int RadiationBeamWedgeAngle = 0x300A0652;

    /** (300A,0653) VR=FD VM=1 Radiation Beam Wedge Thin Edge Distance */
    public static final int RadiationBeamWedgeThinEdgeDistance = 0x300A0653;

    /** (300A,0654) VR=FD VM=1 Radiation Beam Effective Wedge Angle */
    public static final int RadiationBeamEffectiveWedgeAngle = 0x300A0654;

    /** (300A,0655) VR=US VM=1 Number of Wedge Positions */
    public static final int NumberOfWedgePositions = 0x300A0655;

    /** (300A,0656) VR=SQ VM=1 RT Beam Limiting Device Opening Sequence */
    public static final int RTBeamLimitingDeviceOpeningSequence = 0x300A0656;

    /** (300A,0657) VR=US VM=1 Number of RT Beam Limiting Device Openings */
    public static final int NumberOfRTBeamLimitingDeviceOpenings = 0x300A0657;

    /** (300A,0658) VR=SQ VM=1 Radiation Dosimeter Unit Sequence */
    public static final int RadiationDosimeterUnitSequence = 0x300A0658;

    /** (300A,0659) VR=SQ VM=1 RT Device Distance Reference Location Code Sequence */
    public static final int RTDeviceDistanceReferenceLocationCodeSequence = 0x300A0659;

    /** (300A,065A) VR=SQ VM=1 Radiation Device Configuration and Commissioning Key Sequence */
    public static final int RadiationDeviceConfigurationAndCommissioningKeySequence = 0x300A065A;

    /** (300A,065B) VR=SQ VM=1 Patient Support Position Parameter Sequence */
    public static final int PatientSupportPositionParameterSequence = 0x300A065B;

    /** (300A,065C) VR=CS VM=1 Patient Support Position Specification Method */
    public static final int PatientSupportPositionSpecificationMethod = 0x300A065C;

    /** (300A,065D) VR=SQ VM=1 Patient Support Position Device Parameter Sequence */
    public static final int PatientSupportPositionDeviceParameterSequence = 0x300A065D;

    /** (300A,065E) VR=US VM=1 Device Order Index */
    public static final int DeviceOrderIndex = 0x300A065E;

    /** (300A,065F) VR=US VM=1 Patient Support Position Parameter Order Index */
    public static final int PatientSupportPositionParameterOrderIndex = 0x300A065F;

    /** (300A,0660) VR=SQ VM=1 Patient Support Position Device Tolerance Sequence */
    public static final int PatientSupportPositionDeviceToleranceSequence = 0x300A0660;

    /** (300A,0661) VR=US VM=1 Patient Support Position Tolerance Order Index */
    public static final int PatientSupportPositionToleranceOrderIndex = 0x300A0661;

    /** (300A,0662) VR=SQ VM=1 Compensator Definition Sequence */
    public static final int CompensatorDefinitionSequence = 0x300A0662;

    /** (300A,0663) VR=CS VM=1 Compensator Map Orientation */
    public static final int CompensatorMapOrientation = 0x300A0663;

    /** (300A,0664) VR=OF VM=1 Compensator Proximal Thickness Map */
    public static final int CompensatorProximalThicknessMap = 0x300A0664;

    /** (300A,0665) VR=OF VM=1 Compensator Distal Thickness Map */
    public static final int CompensatorDistalThicknessMap = 0x300A0665;

    /** (300A,0666) VR=FD VM=1 Compensator Base Plane Offset */
    public static final int CompensatorBasePlaneOffset = 0x300A0666;

    /** (300A,0667) VR=SQ VM=1 Compensator Shape Fabrication Code Sequence */
    public static final int CompensatorShapeFabricationCodeSequence = 0x300A0667;

    /** (300A,0668) VR=SQ VM=1 Compensator Shape Sequence */
    public static final int CompensatorShapeSequence = 0x300A0668;

    /** (300A,0669) VR=FD VM=1 Radiation Beam Compensator Milling Tool Diameter */
    public static final int RadiationBeamCompensatorMillingToolDiameter = 0x300A0669;

    /** (300A,066A) VR=SQ VM=1 Block Definition Sequence */
    public static final int BlockDefinitionSequence = 0x300A066A;

    /** (300A,066B) VR=OF VM=1 Block Edge Data */
    public static final int BlockEdgeData = 0x300A066B;

    /** (300A,066C) VR=CS VM=1 Block Orientation */
    public static final int BlockOrientation = 0x300A066C;

    /** (300A,066D) VR=FD VM=1 Radiation Beam Block Thickness */
    public static final int RadiationBeamBlockThickness = 0x300A066D;

    /** (300A,066E) VR=FD VM=1 Radiation Beam Block Slab Thickness */
    public static final int RadiationBeamBlockSlabThickness = 0x300A066E;

    /** (300A,066F) VR=SQ VM=1 Block Edge Data Sequence */
    public static final int BlockEdgeDataSequence = 0x300A066F;

    /** (300A,0670) VR=US VM=1 Number of RT Accessory Holders */
    public static final int NumberOfRTAccessoryHolders = 0x300A0670;

    /** (300A,0671) VR=SQ VM=1 General Accessory Definition Sequence */
    public static final int GeneralAccessoryDefinitionSequence = 0x300A0671;

    /** (300A,0672) VR=US VM=1 Number of General Accessories */
    public static final int NumberOfGeneralAccessories = 0x300A0672;

    /** (300A,0673) VR=SQ VM=1 Bolus Definition Sequence */
    public static final int BolusDefinitionSequence = 0x300A0673;

    /** (300A,0674) VR=US VM=1 Number of Boluses */
    public static final int NumberOfBoluses = 0x300A0674;

    /** (300A,0675) VR=UI VM=1 Equipment Frame of Reference UID */
    public static final int EquipmentFrameOfReferenceUID = 0x300A0675;

    /** (300A,0676) VR=ST VM=1 Equipment Frame of Reference Description */
    public static final int EquipmentFrameOfReferenceDescription = 0x300A0676;

    /** (300A,0677) VR=SQ VM=1 Equipment Reference Point Coordinates Sequence */
    public static final int EquipmentReferencePointCoordinatesSequence = 0x300A0677;

    /** (300A,0678) VR=SQ VM=1 Equipment Reference Point Code Sequence */
    public static final int EquipmentReferencePointCodeSequence = 0x300A0678;

    /** (300A,0679) VR=FD VM=1 RT Beam Limiting Device Angle */
    public static final int RTBeamLimitingDeviceAngle = 0x300A0679;

    /** (300A,067A) VR=FD VM=1 Source Roll Angle */
    public static final int SourceRollAngle = 0x300A067A;

    /** (300A,067B) VR=SQ VM=1 Radiation GenerationMode Sequence */
    public static final int RadiationGenerationModeSequence = 0x300A067B;

    /** (300A,067C) VR=SH VM=1 Radiation GenerationMode Label */
    public static final int RadiationGenerationModeLabel = 0x300A067C;

    /** (300A,067D) VR=ST VM=1 Radiation GenerationMode Description */
    public static final int RadiationGenerationModeDescription = 0x300A067D;

    /** (300A,067E) VR=SQ VM=1 Radiation GenerationMode Machine Code Sequence */
    public static final int RadiationGenerationModeMachineCodeSequence = 0x300A067E;

    /** (300A,067F) VR=SQ VM=1 Radiation Type Code Sequence */
    public static final int RadiationTypeCodeSequence = 0x300A067F;

    /** (300A,0680) VR=DS VM=1 Nominal Energy */
    public static final int NominalEnergy = 0x300A0680;

    /** (300A,0681) VR=DS VM=1 Minimum Nominal Energy */
    public static final int MinimumNominalEnergy = 0x300A0681;

    /** (300A,0682) VR=DS VM=1 Maximum Nominal Energy */
    public static final int MaximumNominalEnergy = 0x300A0682;

    /** (300A,0683) VR=SQ VM=1 Radiation Fluence Modifier Code Sequence */
    public static final int RadiationFluenceModifierCodeSequence = 0x300A0683;

    /** (300A,0684) VR=SQ VM=1 Energy Unit Code Sequence */
    public static final int EnergyUnitCodeSequence = 0x300A0684;

    /** (300A,0685) VR=US VM=1 Number of Radiation GenerationModes */
    public static final int NumberOfRadiationGenerationModes = 0x300A0685;

    /** (300A,0686) VR=SQ VM=1 Patient Support Devices Sequence */
    public static final int PatientSupportDevicesSequence = 0x300A0686;

    /** (300A,0687) VR=US VM=1 Number of Patient Support Devices */
    public static final int NumberOfPatientSupportDevices = 0x300A0687;

    /** (300A,0688) VR=FD VM=1 RT Beam Modifier Definition Distance */
    public static final int RTBeamModifierDefinitionDistance = 0x300A0688;

    /** (300A,0689) VR=SQ VM=1 Beam Area Limit Sequence */
    public static final int BeamAreaLimitSequence = 0x300A0689;

    /** (300A,068A) VR=SQ VM=1 Referenced RT Prescription Sequence */
    public static final int ReferencedRTPrescriptionSequence = 0x300A068A;

    /** (300A,0700) VR=UI VM=1 Treatment Session UID */
    public static final int TreatmentSessionUID = 0x300A0700;

    /** (300A,0701) VR=CS VM=1 RT Radiation Usage */
    public static final int RTRadiationUsage = 0x300A0701;

    /** (300A,0702) VR=SQ VM=1 Referenced RT Radiation Set Sequence */
    public static final int ReferencedRTRadiationSetSequence = 0x300A0702;

    /** (300A,0703) VR=SQ VM=1 Referenced RT Radiation Record Sequence */
    public static final int ReferencedRTRadiationRecordSequence = 0x300A0703;

    /** (300A,0704) VR=US VM=1 RT Radiation Set Delivery Number */
    public static final int RTRadiationSetDeliveryNumber = 0x300A0704;

    /** (300A,0705) VR=US VM=1 Clinical Fraction Number */
    public static final int ClinicalFractionNumber = 0x300A0705;

    /** (300A,0706) VR=CS VM=1 RT Treatment Fraction Completion Status */
    public static final int RTTreatmentFractionCompletionStatus = 0x300A0706;

    /** (300A,0707) VR=CS VM=1 RT Radiation Set Usage */
    public static final int RTRadiationSetUsage = 0x300A0707;

    /** (300A,0708) VR=CS VM=1 Treatment Delivery Continuation Flag */
    public static final int TreatmentDeliveryContinuationFlag = 0x300A0708;

    /** (300A,0709) VR=CS VM=1 Treatment Record Content Origin */
    public static final int TreatmentRecordContentOrigin = 0x300A0709;

    /** (300A,0714) VR=CS VM=1 RT Treatment Termination Status */
    public static final int RTTreatmentTerminationStatus = 0x300A0714;

    /** (300A,0715) VR=SQ VM=1 RT Treatment Termination Reason Code Sequence */
    public static final int RTTreatmentTerminationReasonCodeSequence = 0x300A0715;

    /** (300A,0716) VR=SQ VM=1 Machine-Specific Treatment Termination Code Sequence */
    public static final int MachineSpecificTreatmentTerminationCodeSequence = 0x300A0716;

    /** (300A,0722) VR=SQ VM=1 RT Radiation Salvage Record Control Point Sequence */
    public static final int RTRadiationSalvageRecordControlPointSequence = 0x300A0722;

    /** (300A,0723) VR=CS VM=1 Starting Meterset Value Known Flag */
    public static final int StartingMetersetValueKnownFlag = 0x300A0723;

    /** (300A,0730) VR=ST VM=1 Treatment Termination Description */
    public static final int TreatmentTerminationDescription = 0x300A0730;

    /** (300A,0731) VR=SQ VM=1 Treatment Tolerance Violation Sequence */
    public static final int TreatmentToleranceViolationSequence = 0x300A0731;

    /** (300A,0732) VR=CS VM=1 Treatment Tolerance Violation Category */
    public static final int TreatmentToleranceViolationCategory = 0x300A0732;

    /** (300A,0733) VR=SQ VM=1 Treatment Tolerance Violation Attribute Sequence */
    public static final int TreatmentToleranceViolationAttributeSequence = 0x300A0733;

    /** (300A,0734) VR=ST VM=1 Treatment Tolerance Violation Description */
    public static final int TreatmentToleranceViolationDescription = 0x300A0734;

    /** (300A,0735) VR=ST VM=1 Treatment Tolerance Violation Identification */
    public static final int TreatmentToleranceViolationIdentification = 0x300A0735;

    /** (300A,0736) VR=DT VM=1 Treatment Tolerance Violation DateTime */
    public static final int TreatmentToleranceViolationDateTime = 0x300A0736;

    /** (300A,073A) VR=DT VM=1 Recorded RT Control Point DateTime */
    public static final int RecordedRTControlPointDateTime = 0x300A073A;

    /** (300A,073B) VR=US VM=1 Referenced Radiation RT Control Point Index */
    public static final int ReferencedRadiationRTControlPointIndex = 0x300A073B;

    /** (300A,073E) VR=SQ VM=1 Alternate Value Sequence */
    public static final int AlternateValueSequence = 0x300A073E;

    /** (300A,073F) VR=SQ VM=1 Confirmation Sequence */
    public static final int ConfirmationSequence = 0x300A073F;

    /** (300A,0740) VR=SQ VM=1 Interlock Sequence */
    public static final int InterlockSequence = 0x300A0740;

    /** (300A,0741) VR=DT VM=1 Interlock DateTime */
    public static final int InterlockDateTime = 0x300A0741;

    /** (300A,0742) VR=ST VM=1 Interlock Description */
    public static final int InterlockDescription = 0x300A0742;

    /** (300A,0743) VR=SQ VM=1 Interlock Originating Device Sequence */
    public static final int InterlockOriginatingDeviceSequence = 0x300A0743;

    /** (300A,0744) VR=SQ VM=1 Interlock Code Sequence */
    public static final int InterlockCodeSequence = 0x300A0744;

    /** (300A,0745) VR=SQ VM=1 Interlock Resolution Code Sequence */
    public static final int InterlockResolutionCodeSequence = 0x300A0745;

    /** (300A,0746) VR=SQ VM=1 Interlock Resolution User Sequence */
    public static final int InterlockResolutionUserSequence = 0x300A0746;

    /** (300A,0760) VR=DT VM=1 Override DateTime */
    public static final int OverrideDateTime = 0x300A0760;

    /** (300A,0761) VR=SQ VM=1 Treatment Tolerance Violation Type Code Sequence */
    public static final int TreatmentToleranceViolationTypeCodeSequence = 0x300A0761;

    /** (300A,0762) VR=SQ VM=1 Treatment Tolerance Violation Cause Code Sequence */
    public static final int TreatmentToleranceViolationCauseCodeSequence = 0x300A0762;

    /** (300A,0772) VR=SQ VM=1 Measured Meterset to Dose Mapping Sequence */
    public static final int MeasuredMetersetToDoseMappingSequence = 0x300A0772;

    /** (300A,0773) VR=US VM=1 Referenced Expected In-Vivo Measurement Value Index */
    public static final int ReferencedExpectedInVivoMeasurementValueIndex = 0x300A0773;

    /** (300A,0774) VR=SQ VM=1 Dose Measurement Device Code Sequence */
    public static final int DoseMeasurementDeviceCodeSequence = 0x300A0774;

    /** (300A,0780) VR=SQ VM=1 Additional Parameter Recording Instance Sequence */
    public static final int AdditionalParameterRecordingInstanceSequence = 0x300A0780;

    /** (300A,0783) VR=ST VM=1 Interlock Origin Description */
    public static final int InterlockOriginDescription = 0x300A0783;

    /** (300C,0002) VR=SQ VM=1 Referenced RT Plan Sequence */
    public static final int ReferencedRTPlanSequence = 0x300C0002;

    /** (300C,0004) VR=SQ VM=1 Referenced Beam Sequence */
    public static final int ReferencedBeamSequence = 0x300C0004;

    /** (300C,0006) VR=IS VM=1 Referenced Beam Number */
    public static final int ReferencedBeamNumber = 0x300C0006;

    /** (300C,0007) VR=IS VM=1 Referenced Reference Image Number */
    public static final int ReferencedReferenceImageNumber = 0x300C0007;

    /** (300C,0008) VR=DS VM=1 Start Cumulative Meterset Weight */
    public static final int StartCumulativeMetersetWeight = 0x300C0008;

    /** (300C,0009) VR=DS VM=1 End Cumulative Meterset Weight */
    public static final int EndCumulativeMetersetWeight = 0x300C0009;

    /** (300C,000A) VR=SQ VM=1 Referenced Brachy Application Setup Sequence */
    public static final int ReferencedBrachyApplicationSetupSequence = 0x300C000A;

    /** (300C,000C) VR=IS VM=1 Referenced Brachy Application Setup Number */
    public static final int ReferencedBrachyApplicationSetupNumber = 0x300C000C;

    /** (300C,000E) VR=IS VM=1 Referenced Source Number */
    public static final int ReferencedSourceNumber = 0x300C000E;

    /** (300C,0020) VR=SQ VM=1 Referenced Fraction Group Sequence */
    public static final int ReferencedFractionGroupSequence = 0x300C0020;

    /** (300C,0022) VR=IS VM=1 Referenced Fraction Group Number */
    public static final int ReferencedFractionGroupNumber = 0x300C0022;

    /** (300C,0040) VR=SQ VM=1 Referenced Verification Image Sequence */
    public static final int ReferencedVerificationImageSequence = 0x300C0040;

    /** (300C,0042) VR=SQ VM=1 Referenced Reference Image Sequence */
    public static final int ReferencedReferenceImageSequence = 0x300C0042;

    /** (300C,0050) VR=SQ VM=1 Referenced Dose Reference Sequence */
    public static final int ReferencedDoseReferenceSequence = 0x300C0050;

    /** (300C,0051) VR=IS VM=1 Referenced Dose Reference Number */
    public static final int ReferencedDoseReferenceNumber = 0x300C0051;

    /** (300C,0055) VR=SQ VM=1 Brachy Referenced Dose Reference Sequence */
    public static final int BrachyReferencedDoseReferenceSequence = 0x300C0055;

    /** (300C,0060) VR=SQ VM=1 Referenced Structure Set Sequence */
    public static final int ReferencedStructureSetSequence = 0x300C0060;

    /** (300C,006A) VR=IS VM=1 Referenced Patient Setup Number */
    public static final int ReferencedPatientSetupNumber = 0x300C006A;

    /** (300C,0080) VR=SQ VM=1 Referenced Dose Sequence */
    public static final int ReferencedDoseSequence = 0x300C0080;

    /** (300C,00A0) VR=IS VM=1 Referenced Tolerance Table Number */
    public static final int ReferencedToleranceTableNumber = 0x300C00A0;

    /** (300C,00B0) VR=SQ VM=1 Referenced Bolus Sequence */
    public static final int ReferencedBolusSequence = 0x300C00B0;

    /** (300C,00C0) VR=IS VM=1 Referenced Wedge Number */
    public static final int ReferencedWedgeNumber = 0x300C00C0;

    /** (300C,00D0) VR=IS VM=1 Referenced Compensator Number */
    public static final int ReferencedCompensatorNumber = 0x300C00D0;

    /** (300C,00E0) VR=IS VM=1 Referenced Block Number */
    public static final int ReferencedBlockNumber = 0x300C00E0;

    /** (300C,00F0) VR=IS VM=1 Referenced Control Point Index */
    public static final int ReferencedControlPointIndex = 0x300C00F0;

    /** (300C,00F2) VR=SQ VM=1 Referenced Control Point Sequence */
    public static final int ReferencedControlPointSequence = 0x300C00F2;

    /** (300C,00F4) VR=IS VM=1 Referenced Start Control Point Index */
    public static final int ReferencedStartControlPointIndex = 0x300C00F4;

    /** (300C,00F6) VR=IS VM=1 Referenced Stop Control Point Index */
    public static final int ReferencedStopControlPointIndex = 0x300C00F6;

    /** (300C,0100) VR=IS VM=1 Referenced Range Shifter Number */
    public static final int ReferencedRangeShifterNumber = 0x300C0100;

    /** (300C,0102) VR=IS VM=1 Referenced Lateral Spreading Device Number */
    public static final int ReferencedLateralSpreadingDeviceNumber = 0x300C0102;

    /** (300C,0104) VR=IS VM=1 Referenced Range Modulator Number */
    public static final int ReferencedRangeModulatorNumber = 0x300C0104;

    /** (300C,0111) VR=SQ VM=1 Omitted Beam Task Sequence */
    public static final int OmittedBeamTaskSequence = 0x300C0111;

    /** (300C,0112) VR=CS VM=1 Reason for Omission */
    public static final int ReasonForOmission = 0x300C0112;

    /** (300C,0113) VR=LO VM=1 Reason for Omission Description */
    public static final int ReasonForOmissionDescription = 0x300C0113;

    /** (300E,0002) VR=CS VM=1 Approval Status */
    public static final int ApprovalStatus = 0x300E0002;

    /** (300E,0004) VR=DA VM=1 Review Date */
    public static final int ReviewDate = 0x300E0004;

    /** (300E,0005) VR=TM VM=1 Review Time */
    public static final int ReviewTime = 0x300E0005;

    /** (300E,0008) VR=PN VM=1 Reviewer Name */
    public static final int ReviewerName = 0x300E0008;

    /** (3010,0001) VR=SQ VM=1 Radiobiological Dose Effect Sequence */
    public static final int RadiobiologicalDoseEffectSequence = 0x30100001;

    /** (3010,0002) VR=CS VM=1 Radiobiological Dose Effect Flag */
    public static final int RadiobiologicalDoseEffectFlag = 0x30100002;

    /** (3010,0003) VR=SQ VM=1 Effective Dose Calculation Method Category Code Sequence */
    public static final int EffectiveDoseCalculationMethodCategoryCodeSequence = 0x30100003;

    /** (3010,0004) VR=SQ VM=1 Effective Dose Calculation Method Code Sequence */
    public static final int EffectiveDoseCalculationMethodCodeSequence = 0x30100004;

    /** (3010,0005) VR=LO VM=1 Effective Dose Calculation Method Description */
    public static final int EffectiveDoseCalculationMethodDescription = 0x30100005;

    /** (3010,0006) VR=UI VM=1 Conceptual Volume UID */
    public static final int ConceptualVolumeUID = 0x30100006;

    /** (3010,0007) VR=SQ VM=1 Originating SOP Instance Reference Sequence */
    public static final int OriginatingSOPInstanceReferenceSequence = 0x30100007;

    /** (3010,0008) VR=SQ VM=1 Conceptual Volume Constituent Sequence */
    public static final int ConceptualVolumeConstituentSequence = 0x30100008;

    /** (3010,0009) VR=SQ VM=1 Equivalent Conceptual Volume Instance Reference Sequence */
    public static final int EquivalentConceptualVolumeInstanceReferenceSequence = 0x30100009;

    /** (3010,000A) VR=SQ VM=1 Equivalent Conceptual Volumes Sequence */
    public static final int EquivalentConceptualVolumesSequence = 0x3010000A;

    /** (3010,000B) VR=UI VM=1 Referenced Conceptual Volume UID */
    public static final int ReferencedConceptualVolumeUID = 0x3010000B;

    /** (3010,000C) VR=UT VM=1 Conceptual Volume Combination Expression */
    public static final int ConceptualVolumeCombinationExpression = 0x3010000C;

    /** (3010,000D) VR=US VM=1 Conceptual Volume Constituent Index */
    public static final int ConceptualVolumeConstituentIndex = 0x3010000D;

    /** (3010,000E) VR=CS VM=1 Conceptual Volume Combination Flag */
    public static final int ConceptualVolumeCombinationFlag = 0x3010000E;

    /** (3010,000F) VR=ST VM=1 Conceptual Volume Combination Description */
    public static final int ConceptualVolumeCombinationDescription = 0x3010000F;

    /** (3010,0010) VR=CS VM=1 Conceptual Volume Segmentation Defined Flag */
    public static final int ConceptualVolumeSegmentationDefinedFlag = 0x30100010;

    /** (3010,0011) VR=SQ VM=1 Conceptual Volume Segmentation Reference Sequence */
    public static final int ConceptualVolumeSegmentationReferenceSequence = 0x30100011;

    /** (3010,0012) VR=SQ VM=1 Conceptual Volume Constituent Segmentation Reference Sequence */
    public static final int ConceptualVolumeConstituentSegmentationReferenceSequence = 0x30100012;

    /** (3010,0013) VR=UI VM=1 Constituent Conceptual Volume UID */
    public static final int ConstituentConceptualVolumeUID = 0x30100013;

    /** (3010,0014) VR=SQ VM=1 Derivation Conceptual Volume Sequence */
    public static final int DerivationConceptualVolumeSequence = 0x30100014;

    /** (3010,0015) VR=UI VM=1 Source Conceptual Volume UID */
    public static final int SourceConceptualVolumeUID = 0x30100015;

    /** (3010,0016) VR=SQ VM=1 Conceptual Volume Derivation Algorithm Sequence */
    public static final int ConceptualVolumeDerivationAlgorithmSequence = 0x30100016;

    /** (3010,0017) VR=ST VM=1 Conceptual Volume Description */
    public static final int ConceptualVolumeDescription = 0x30100017;

    /** (3010,0018) VR=SQ VM=1 Source Conceptual Volume Sequence */
    public static final int SourceConceptualVolumeSequence = 0x30100018;

    /** (3010,0019) VR=SQ VM=1 Author Identification Sequence */
    public static final int AuthorIdentificationSequence = 0x30100019;

    /** (3010,001A) VR=LO VM=1 Manufacturer's Model Version */
    public static final int ManufacturerModelVersion = 0x3010001A;

    /** (3010,001B) VR=UC VM=1 Device Alternate Identifier */
    public static final int DeviceAlternateIdentifier = 0x3010001B;

    /** (3010,001C) VR=CS VM=1 Device Alternate Identifier Type */
    public static final int DeviceAlternateIdentifierType = 0x3010001C;

    /** (3010,001D) VR=LT VM=1 Device Alternate Identifier Format */
    public static final int DeviceAlternateIdentifierFormat = 0x3010001D;

    /** (3010,001E) VR=LO VM=1 Segmentation Creation Template Label */
    public static final int SegmentationCreationTemplateLabel = 0x3010001E;

    /** (3010,001F) VR=UI VM=1 Segmentation Template UID */
    public static final int SegmentationTemplateUID = 0x3010001F;

    /** (3010,0020) VR=US VM=1 Referenced Segment Reference Index */
    public static final int ReferencedSegmentReferenceIndex = 0x30100020;

    /** (3010,0021) VR=SQ VM=1 Segment Reference Sequence */
    public static final int SegmentReferenceSequence = 0x30100021;

    /** (3010,0022) VR=US VM=1 Segment Reference Index */
    public static final int SegmentReferenceIndex = 0x30100022;

    /** (3010,0023) VR=SQ VM=1 Direct Segment Reference Sequence */
    public static final int DirectSegmentReferenceSequence = 0x30100023;

    /** (3010,0024) VR=SQ VM=1 Combination Segment Reference Sequence */
    public static final int CombinationSegmentReferenceSequence = 0x30100024;

    /** (3010,0025) VR=SQ VM=1 Conceptual Volume Sequence */
    public static final int ConceptualVolumeSequence = 0x30100025;

    /** (3010,0026) VR=SQ VM=1 Segmented RT Accessory Device Sequence */
    public static final int SegmentedRTAccessoryDeviceSequence = 0x30100026;

    /** (3010,0027) VR=SQ VM=1 Segment Characteristics Sequence */
    public static final int SegmentCharacteristicsSequence = 0x30100027;

    /** (3010,0028) VR=SQ VM=1 Related Segment Characteristics Sequence */
    public static final int RelatedSegmentCharacteristicsSequence = 0x30100028;

    /** (3010,0029) VR=US VM=1 Segment Characteristics Precedence */
    public static final int SegmentCharacteristicsPrecedence = 0x30100029;

    /** (3010,002A) VR=SQ VM=1 RT Segment Annotation Sequence */
    public static final int RTSegmentAnnotationSequence = 0x3010002A;

    /** (3010,002B) VR=SQ VM=1 Segment Annotation Category Code Sequence */
    public static final int SegmentAnnotationCategoryCodeSequence = 0x3010002B;

    /** (3010,002C) VR=SQ VM=1 Segment Annotation Type Code Sequence */
    public static final int SegmentAnnotationTypeCodeSequence = 0x3010002C;

    /** (3010,002D) VR=LO VM=1 Device Label */
    public static final int DeviceLabel = 0x3010002D;

    /** (3010,002E) VR=SQ VM=1 Device Type Code Sequence */
    public static final int DeviceTypeCodeSequence = 0x3010002E;

    /** (3010,002F) VR=SQ VM=1 Segment Annotation Type Modifier Code Sequence */
    public static final int SegmentAnnotationTypeModifierCodeSequence = 0x3010002F;

    /** (3010,0030) VR=SQ VM=1 Patient Equipment Relationship Code Sequence */
    public static final int PatientEquipmentRelationshipCodeSequence = 0x30100030;

    /** (3010,0031) VR=UI VM=1 Referenced Fiducials UID */
    public static final int ReferencedFiducialsUID = 0x30100031;

    /** (3010,0032) VR=SQ VM=1 Patient Treatment Orientation Sequence */
    public static final int PatientTreatmentOrientationSequence = 0x30100032;

    /** (3010,0033) VR=SH VM=1 User Content Label */
    public static final int UserContentLabel = 0x30100033;

    /** (3010,0034) VR=LO VM=1 User Content Long Label */
    public static final int UserContentLongLabel = 0x30100034;

    /** (3010,0035) VR=SH VM=1 Entity Label */
    public static final int EntityLabel = 0x30100035;

    /** (3010,0036) VR=LO VM=1 Entity Name */
    public static final int EntityName = 0x30100036;

    /** (3010,0037) VR=ST VM=1 Entity Description */
    public static final int EntityDescription = 0x30100037;

    /** (3010,0038) VR=LO VM=1 Entity Long Label */
    public static final int EntityLongLabel = 0x30100038;

    /** (3010,0039) VR=US VM=1 Device Index */
    public static final int DeviceIndex = 0x30100039;

    /** (3010,003A) VR=US VM=1 RT Treatment Phase Index */
    public static final int RTTreatmentPhaseIndex = 0x3010003A;

    /** (3010,003B) VR=UI VM=1 RT Treatment Phase UID */
    public static final int RTTreatmentPhaseUID = 0x3010003B;

    /** (3010,003C) VR=US VM=1 RT Prescription Index */
    public static final int RTPrescriptionIndex = 0x3010003C;

    /** (3010,003D) VR=US VM=1 RT Segment Annotation Index */
    public static final int RTSegmentAnnotationIndex = 0x3010003D;

    /** (3010,003E) VR=US VM=1 Basis RT Treatment Phase Index */
    public static final int BasisRTTreatmentPhaseIndex = 0x3010003E;

    /** (3010,003F) VR=US VM=1 Related RT Treatment Phase Index */
    public static final int RelatedRTTreatmentPhaseIndex = 0x3010003F;

    /** (3010,0040) VR=US VM=1 Referenced RT Treatment Phase Index */
    public static final int ReferencedRTTreatmentPhaseIndex = 0x30100040;

    /** (3010,0041) VR=US VM=1 Referenced RT Prescription Index */
    public static final int ReferencedRTPrescriptionIndex = 0x30100041;

    /** (3010,0042) VR=US VM=1 Referenced Parent RT Prescription Index */
    public static final int ReferencedParentRTPrescriptionIndex = 0x30100042;

    /** (3010,0043) VR=ST VM=1 Manufacturer's Device Identifier */
    public static final int ManufacturerDeviceIdentifier = 0x30100043;

    /** (3010,0044) VR=SQ VM=1 Instance-Level Referenced Performed Procedure Step Sequence */
    public static final int InstanceLevelReferencedPerformedProcedureStepSequence = 0x30100044;

    /** (3010,0045) VR=CS VM=1 RT Treatment Phase Intent Presence Flag */
    public static final int RTTreatmentPhaseIntentPresenceFlag = 0x30100045;

    /** (3010,0046) VR=CS VM=1 Radiotherapy Treatment Type */
    public static final int RadiotherapyTreatmentType = 0x30100046;

    /** (3010,0047) VR=CS VM=1-n Teletherapy Radiation Type */
    public static final int TeletherapyRadiationType = 0x30100047;

    /** (3010,0048) VR=CS VM=1-n Brachytherapy Source Type */
    public static final int BrachytherapySourceType = 0x30100048;

    /** (3010,0049) VR=SQ VM=1 Referenced RT Treatment Phase Sequence */
    public static final int ReferencedRTTreatmentPhaseSequence = 0x30100049;

    /** (3010,004A) VR=SQ VM=1 Referenced Direct Segment Instance Sequence */
    public static final int ReferencedDirectSegmentInstanceSequence = 0x3010004A;

    /** (3010,004B) VR=SQ VM=1 Intended RT Treatment Phase Sequence */
    public static final int IntendedRTTreatmentPhaseSequence = 0x3010004B;

    /** (3010,004C) VR=DA VM=1 Intended Phase Start Date */
    public static final int IntendedPhaseStartDate = 0x3010004C;

    /** (3010,004D) VR=DA VM=1 Intended Phase End Date */
    public static final int IntendedPhaseEndDate = 0x3010004D;

    /** (3010,004E) VR=SQ VM=1 RT Treatment Phase Interval Sequence */
    public static final int RTTreatmentPhaseIntervalSequence = 0x3010004E;

    /** (3010,004F) VR=CS VM=1 Temporal Relationship Interval Anchor */
    public static final int TemporalRelationshipIntervalAnchor = 0x3010004F;

    /** (3010,0050) VR=FD VM=1 Minimum Number of Interval Days */
    public static final int MinimumNumberOfIntervalDays = 0x30100050;

    /** (3010,0051) VR=FD VM=1 Maximum Number of Interval Days */
    public static final int MaximumNumberOfIntervalDays = 0x30100051;

    /** (3010,0052) VR=UI VM=1-n Pertinent SOP Classes in Study */
    public static final int PertinentSOPClassesInStudy = 0x30100052;

    /** (3010,0053) VR=UI VM=1-n Pertinent SOP Classes in Series */
    public static final int PertinentSOPClassesInSeries = 0x30100053;

    /** (3010,0054) VR=LO VM=1 RT Prescription Label */
    public static final int RTPrescriptionLabel = 0x30100054;

    /** (3010,0055) VR=SQ VM=1 RT Physician Intent Predecessor Sequence */
    public static final int RTPhysicianIntentPredecessorSequence = 0x30100055;

    /** (3010,0056) VR=LO VM=1 RT Treatment Approach Label */
    public static final int RTTreatmentApproachLabel = 0x30100056;

    /** (3010,0057) VR=SQ VM=1 RT Physician Intent Sequence */
    public static final int RTPhysicianIntentSequence = 0x30100057;

    /** (3010,0058) VR=US VM=1 RT Physician Intent Index */
    public static final int RTPhysicianIntentIndex = 0x30100058;

    /** (3010,0059) VR=CS VM=1 RT Treatment Intent Type */
    public static final int RTTreatmentIntentType = 0x30100059;

    /** (3010,005A) VR=UT VM=1 RT Physician Intent Narrative */
    public static final int RTPhysicianIntentNarrative = 0x3010005A;

    /** (3010,005B) VR=SQ VM=1 RT Protocol Code Sequence */
    public static final int RTProtocolCodeSequence = 0x3010005B;

    /** (3010,005C) VR=ST VM=1 Reason for Superseding */
    public static final int ReasonForSuperseding = 0x3010005C;

    /** (3010,005D) VR=SQ VM=1 RT Diagnosis Code Sequence */
    public static final int RTDiagnosisCodeSequence = 0x3010005D;

    /** (3010,005E) VR=US VM=1 Referenced RT Physician Intent Index */
    public static final int ReferencedRTPhysicianIntentIndex = 0x3010005E;

    /** (3010,005F) VR=SQ VM=1 RT Physician Intent Input Instance Sequence */
    public static final int RTPhysicianIntentInputInstanceSequence = 0x3010005F;

    /** (3010,0060) VR=SQ VM=1 RT Anatomic Prescription Sequence */
    public static final int RTAnatomicPrescriptionSequence = 0x30100060;

    /** (3010,0061) VR=UT VM=1 Prior Treatment Dose Description */
    public static final int PriorTreatmentDoseDescription = 0x30100061;

    /** (3010,0062) VR=SQ VM=1 Prior Treatment Reference Sequence */
    public static final int PriorTreatmentReferenceSequence = 0x30100062;

    /** (3010,0063) VR=CS VM=1 Dosimetric Objective Evaluation Scope */
    public static final int DosimetricObjectiveEvaluationScope = 0x30100063;

    /** (3010,0064) VR=SQ VM=1 Therapeutic Role Category Code Sequence */
    public static final int TherapeuticRoleCategoryCodeSequence = 0x30100064;

    /** (3010,0065) VR=SQ VM=1 Therapeutic Role Type Code Sequence */
    public static final int TherapeuticRoleTypeCodeSequence = 0x30100065;

    /** (3010,0066) VR=US VM=1 Conceptual Volume Optimization Precedence */
    public static final int ConceptualVolumeOptimizationPrecedence = 0x30100066;

    /** (3010,0067) VR=SQ VM=1 Conceptual Volume Category Code Sequence */
    public static final int ConceptualVolumeCategoryCodeSequence = 0x30100067;

    /** (3010,0068) VR=CS VM=1 Conceptual Volume Blocking Constraint */
    public static final int ConceptualVolumeBlockingConstraint = 0x30100068;

    /** (3010,0069) VR=SQ VM=1 Conceptual Volume Type Code Sequence */
    public static final int ConceptualVolumeTypeCodeSequence = 0x30100069;

    /** (3010,006A) VR=SQ VM=1 Conceptual Volume Type Modifier Code Sequence */
    public static final int ConceptualVolumeTypeModifierCodeSequence = 0x3010006A;

    /** (3010,006B) VR=SQ VM=1 RT Prescription Sequence */
    public static final int RTPrescriptionSequence = 0x3010006B;

    /** (3010,006C) VR=SQ VM=1 Dosimetric Objective Sequence */
    public static final int DosimetricObjectiveSequence = 0x3010006C;

    /** (3010,006D) VR=SQ VM=1 Dosimetric Objective Type Code Sequence */
    public static final int DosimetricObjectiveTypeCodeSequence = 0x3010006D;

    /** (3010,006E) VR=UI VM=1 Dosimetric Objective UID */
    public static final int DosimetricObjectiveUID = 0x3010006E;

    /** (3010,006F) VR=UI VM=1 Referenced Dosimetric Objective UID */
    public static final int ReferencedDosimetricObjectiveUID = 0x3010006F;

    /** (3010,0070) VR=SQ VM=1 Dosimetric Objective Parameter Sequence */
    public static final int DosimetricObjectiveParameterSequence = 0x30100070;

    /** (3010,0071) VR=SQ VM=1 Referenced Dosimetric Objectives Sequence */
    public static final int ReferencedDosimetricObjectivesSequence = 0x30100071;

    /** (3010,0073) VR=CS VM=1 Absolute Dosimetric Objective Flag */
    public static final int AbsoluteDosimetricObjectiveFlag = 0x30100073;

    /** (3010,0074) VR=FD VM=1 Dosimetric Objective Weight */
    public static final int DosimetricObjectiveWeight = 0x30100074;

    /** (3010,0075) VR=CS VM=1 Dosimetric Objective Purpose */
    public static final int DosimetricObjectivePurpose = 0x30100075;

    /** (3010,0076) VR=SQ VM=1 Planning Input Information Sequence */
    public static final int PlanningInputInformationSequence = 0x30100076;

    /** (3010,0077) VR=LO VM=1 Treatment Site */
    public static final int TreatmentSite = 0x30100077;

    /** (3010,0078) VR=SQ VM=1 Treatment Site Code Sequence */
    public static final int TreatmentSiteCodeSequence = 0x30100078;

    /** (3010,0079) VR=SQ VM=1 Fraction Pattern Sequence */
    public static final int FractionPatternSequence = 0x30100079;

    /** (3010,007A) VR=UT VM=1 Treatment Technique Notes */
    public static final int TreatmentTechniqueNotes = 0x3010007A;

    /** (3010,007B) VR=UT VM=1 Prescription Notes */
    public static final int PrescriptionNotes = 0x3010007B;

    /** (3010,007C) VR=IS VM=1 Number of Interval Fractions */
    public static final int NumberOfIntervalFractions = 0x3010007C;

    /** (3010,007D) VR=US VM=1 Number of Fractions */
    public static final int NumberOfFractions = 0x3010007D;

    /** (3010,007E) VR=US VM=1 Intended Delivery Duration */
    public static final int IntendedDeliveryDuration = 0x3010007E;

    /** (3010,007F) VR=UT VM=1 Fractionation Notes */
    public static final int FractionationNotes = 0x3010007F;

    /** (3010,0080) VR=SQ VM=1 RT Treatment Technique Code Sequence */
    public static final int RTTreatmentTechniqueCodeSequence = 0x30100080;

    /** (3010,0081) VR=SQ VM=1 Prescription Notes Sequence */
    public static final int PrescriptionNotesSequence = 0x30100081;

    /** (3010,0082) VR=SQ VM=1 Fraction-Based Relationship Sequence */
    public static final int FractionBasedRelationshipSequence = 0x30100082;

    /** (3010,0083) VR=CS VM=1 Fraction-Based Relationship Interval Anchor */
    public static final int FractionBasedRelationshipIntervalAnchor = 0x30100083;

    /** (3010,0084) VR=FD VM=1 Minimum Hours between Fractions */
    public static final int MinimumHoursBetweenFractions = 0x30100084;

    /** (3010,0085) VR=TM VM=1-n Intended Fraction Start Time */
    public static final int IntendedFractionStartTime = 0x30100085;

    /** (3010,0086) VR=LT VM=1 Intended Start Day of Week */
    public static final int IntendedStartDayOfWeek = 0x30100086;

    /** (3010,0087) VR=SQ VM=1 Weekday Fraction Pattern Sequence */
    public static final int WeekdayFractionPatternSequence = 0x30100087;

    /** (3010,0088) VR=SQ VM=1 Delivery Time Structure Code Sequence */
    public static final int DeliveryTimeStructureCodeSequence = 0x30100088;

    /** (3010,0089) VR=SQ VM=1 Treatment Site Modifier Code Sequence */
    public static final int TreatmentSiteModifierCodeSequence = 0x30100089;

    /** (3010,0090) VR=CS VM=1 Robotic Base Location Indicator */
    public static final int RoboticBaseLocationIndicator = 0x30100090;

    /** (3010,0091) VR=SQ VM=1 Robotic Path Node Set Code Sequence */
    public static final int RoboticPathNodeSetCodeSequence = 0x30100091;

    /** (3010,0092) VR=UL VM=1 Robotic Node Identifier */
    public static final int RoboticNodeIdentifier = 0x30100092;

    /** (3010,0093) VR=FD VM=3 RT Treatment Source Coordinates */
    public static final int RTTreatmentSourceCoordinates = 0x30100093;

    /** (3010,0094) VR=FD VM=1 Radiation Source Coordinate SystemYaw Angle */
    public static final int RadiationSourceCoordinateSystemYawAngle = 0x30100094;

    /** (3010,0095) VR=FD VM=1 Radiation Source Coordinate SystemRoll Angle */
    public static final int RadiationSourceCoordinateSystemRollAngle = 0x30100095;

    /** (3010,0096) VR=FD VM=1 Radiation Source Coordinate System Pitch Angle */
    public static final int RadiationSourceCoordinateSystemPitchAngle = 0x30100096;

    /** (3010,0097) VR=SQ VM=1 Robotic Path Control Point Sequence */
    public static final int RoboticPathControlPointSequence = 0x30100097;

    /** (3010,0098) VR=SQ VM=1 Tomotherapeutic Control Point Sequence */
    public static final int TomotherapeuticControlPointSequence = 0x30100098;

    /** (3010,0099) VR=FD VM=1-n Tomotherapeutic Leaf Open Durations */
    public static final int TomotherapeuticLeafOpenDurations = 0x30100099;

    /** (3010,009A) VR=FD VM=1-n Tomotherapeutic Leaf Initial Closed Durations */
    public static final int TomotherapeuticLeafInitialClosedDurations = 0x3010009A;

    /** (4000,0010) VR=LT VM=1 Arbitrary (retired) */
    public static final int Arbitrary = 0x40000010;

    /** (4000,4000) VR=LT VM=1 Text Comments (retired) */
    public static final int TextComments = 0x40004000;

    /** (4008,0040) VR=SH VM=1 Results ID (retired) */
    public static final int ResultsID = 0x40080040;

    /** (4008,0042) VR=LO VM=1 Results ID Issuer (retired) */
    public static final int ResultsIDIssuer = 0x40080042;

    /** (4008,0050) VR=SQ VM=1 Referenced Interpretation Sequence (retired) */
    public static final int ReferencedInterpretationSequence = 0x40080050;

    /** (4008,00FF) VR=CS VM=1 Report Production Status (Trial) (retired) */
    public static final int ReportProductionStatusTrial = 0x400800FF;

    /** (4008,0100) VR=DA VM=1 Interpretation Recorded Date (retired) */
    public static final int InterpretationRecordedDate = 0x40080100;

    /** (4008,0101) VR=TM VM=1 Interpretation Recorded Time (retired) */
    public static final int InterpretationRecordedTime = 0x40080101;

    /** (4008,0102) VR=PN VM=1 Interpretation Recorder (retired) */
    public static final int InterpretationRecorder = 0x40080102;

    /** (4008,0103) VR=LO VM=1 Reference to Recorded Sound (retired) */
    public static final int ReferenceToRecordedSound = 0x40080103;

    /** (4008,0108) VR=DA VM=1 Interpretation Transcription Date (retired) */
    public static final int InterpretationTranscriptionDate = 0x40080108;

    /** (4008,0109) VR=TM VM=1 Interpretation Transcription Time (retired) */
    public static final int InterpretationTranscriptionTime = 0x40080109;

    /** (4008,010A) VR=PN VM=1 Interpretation Transcriber (retired) */
    public static final int InterpretationTranscriber = 0x4008010A;

    /** (4008,010B) VR=ST VM=1 Interpretation Text (retired) */
    public static final int InterpretationText = 0x4008010B;

    /** (4008,010C) VR=PN VM=1 Interpretation Author (retired) */
    public static final int InterpretationAuthor = 0x4008010C;

    /** (4008,0111) VR=SQ VM=1 Interpretation Approver Sequence (retired) */
    public static final int InterpretationApproverSequence = 0x40080111;

    /** (4008,0112) VR=DA VM=1 Interpretation Approval Date (retired) */
    public static final int InterpretationApprovalDate = 0x40080112;

    /** (4008,0113) VR=TM VM=1 Interpretation Approval Time (retired) */
    public static final int InterpretationApprovalTime = 0x40080113;

    /** (4008,0114) VR=PN VM=1 Physician Approving Interpretation (retired) */
    public static final int PhysicianApprovingInterpretation = 0x40080114;

    /** (4008,0115) VR=LT VM=1 Interpretation Diagnosis Description (retired) */
    public static final int InterpretationDiagnosisDescription = 0x40080115;

    /** (4008,0117) VR=SQ VM=1 Interpretation Diagnosis Code Sequence (retired) */
    public static final int InterpretationDiagnosisCodeSequence = 0x40080117;

    /** (4008,0118) VR=SQ VM=1 Results Distribution List Sequence (retired) */
    public static final int ResultsDistributionListSequence = 0x40080118;

    /** (4008,0119) VR=PN VM=1 Distribution Name (retired) */
    public static final int DistributionName = 0x40080119;

    /** (4008,011A) VR=LO VM=1 Distribution Address (retired) */
    public static final int DistributionAddress = 0x4008011A;

    /** (4008,0200) VR=SH VM=1 Interpretation ID (retired) */
    public static final int InterpretationID = 0x40080200;

    /** (4008,0202) VR=LO VM=1 Interpretation ID Issuer (retired) */
    public static final int InterpretationIDIssuer = 0x40080202;

    /** (4008,0210) VR=CS VM=1 Interpretation Type ID (retired) */
    public static final int InterpretationTypeID = 0x40080210;

    /** (4008,0212) VR=CS VM=1 Interpretation Status ID (retired) */
    public static final int InterpretationStatusID = 0x40080212;

    /** (4008,0300) VR=ST VM=1 Impressions (retired) */
    public static final int Impressions = 0x40080300;

    /** (4008,4000) VR=ST VM=1 Results Comments (retired) */
    public static final int ResultsComments = 0x40084000;

    /** (4010,0001) VR=CS VM=1 Low Energy Detectors */
    public static final int LowEnergyDetectors = 0x40100001;

    /** (4010,0002) VR=CS VM=1 High Energy Detectors */
    public static final int HighEnergyDetectors = 0x40100002;

    /** (4010,0004) VR=SQ VM=1 Detector Geometry Sequence */
    public static final int DetectorGeometrySequence = 0x40100004;

    /** (4010,1001) VR=SQ VM=1 Threat ROI Voxel Sequence */
    public static final int ThreatROIVoxelSequence = 0x40101001;

    /** (4010,1004) VR=FL VM=3 Threat ROI Base */
    public static final int ThreatROIBase = 0x40101004;

    /** (4010,1005) VR=FL VM=3 Threat ROI Extents */
    public static final int ThreatROIExtents = 0x40101005;

    /** (4010,1006) VR=OB VM=1 Threat ROI Bitmap */
    public static final int ThreatROIBitmap = 0x40101006;

    /** (4010,1007) VR=SH VM=1 Route Segment ID */
    public static final int RouteSegmentID = 0x40101007;

    /** (4010,1008) VR=CS VM=1 Gantry Type */
    public static final int GantryType = 0x40101008;

    /** (4010,1009) VR=CS VM=1 OOI Owner Type */
    public static final int OOIOwnerType = 0x40101009;

    /** (4010,100A) VR=SQ VM=1 Route Segment Sequence */
    public static final int RouteSegmentSequence = 0x4010100A;

    /** (4010,1010) VR=US VM=1 Potential Threat Object ID */
    public static final int PotentialThreatObjectID = 0x40101010;

    /** (4010,1011) VR=SQ VM=1 Threat Sequence */
    public static final int ThreatSequence = 0x40101011;

    /** (4010,1012) VR=CS VM=1 Threat Category */
    public static final int ThreatCategory = 0x40101012;

    /** (4010,1013) VR=LT VM=1 Threat Category Description */
    public static final int ThreatCategoryDescription = 0x40101013;

    /** (4010,1014) VR=CS VM=1 ATD Ability Assessment */
    public static final int ATDAbilityAssessment = 0x40101014;

    /** (4010,1015) VR=CS VM=1 ATD Assessment Flag */
    public static final int ATDAssessmentFlag = 0x40101015;

    /** (4010,1016) VR=FL VM=1 ATD Assessment Probability */
    public static final int ATDAssessmentProbability = 0x40101016;

    /** (4010,1017) VR=FL VM=1 Mass */
    public static final int Mass = 0x40101017;

    /** (4010,1018) VR=FL VM=1 Density */
    public static final int Density = 0x40101018;

    /** (4010,1019) VR=FL VM=1 Z Effective */
    public static final int ZEffective = 0x40101019;

    /** (4010,101A) VR=SH VM=1 Boarding Pass ID */
    public static final int BoardingPassID = 0x4010101A;

    /** (4010,101B) VR=FL VM=3 Center of Mass */
    public static final int CenterOfMass = 0x4010101B;

    /** (4010,101C) VR=FL VM=3 Center of PTO */
    public static final int CenterOfPTO = 0x4010101C;

    /** (4010,101D) VR=FL VM=6-n Bounding Polygon */
    public static final int BoundingPolygon = 0x4010101D;

    /** (4010,101E) VR=SH VM=1 Route Segment Start Location ID */
    public static final int RouteSegmentStartLocationID = 0x4010101E;

    /** (4010,101F) VR=SH VM=1 Route Segment End Location ID */
    public static final int RouteSegmentEndLocationID = 0x4010101F;

    /** (4010,1020) VR=CS VM=1 Route Segment Location ID Type */
    public static final int RouteSegmentLocationIDType = 0x40101020;

    /** (4010,1021) VR=CS VM=1-n Abort Reason */
    public static final int AbortReason = 0x40101021;

    /** (4010,1023) VR=FL VM=1 Volume of PTO */
    public static final int VolumeOfPTO = 0x40101023;

    /** (4010,1024) VR=CS VM=1 Abort Flag */
    public static final int AbortFlag = 0x40101024;

    /** (4010,1025) VR=DT VM=1 Route Segment Start Time */
    public static final int RouteSegmentStartTime = 0x40101025;

    /** (4010,1026) VR=DT VM=1 Route Segment End Time */
    public static final int RouteSegmentEndTime = 0x40101026;

    /** (4010,1027) VR=CS VM=1 TDR Type */
    public static final int TDRType = 0x40101027;

    /** (4010,1028) VR=CS VM=1 International Route Segment */
    public static final int InternationalRouteSegment = 0x40101028;

    /** (4010,1029) VR=LO VM=1-n Threat Detection Algorithm and Version */
    public static final int ThreatDetectionAlgorithmandVersion = 0x40101029;

    /** (4010,102A) VR=SH VM=1 Assigned Location */
    public static final int AssignedLocation = 0x4010102A;

    /** (4010,102B) VR=DT VM=1 Alarm Decision Time */
    public static final int AlarmDecisionTime = 0x4010102B;

    /** (4010,1031) VR=CS VM=1 Alarm Decision */
    public static final int AlarmDecision = 0x40101031;

    /** (4010,1033) VR=US VM=1 Number of Total Objects */
    public static final int NumberOfTotalObjects = 0x40101033;

    /** (4010,1034) VR=US VM=1 Number of Alarm Objects */
    public static final int NumberOfAlarmObjects = 0x40101034;

    /** (4010,1037) VR=SQ VM=1 PTO Representation Sequence */
    public static final int PTORepresentationSequence = 0x40101037;

    /** (4010,1038) VR=SQ VM=1 ATD Assessment Sequence */
    public static final int ATDAssessmentSequence = 0x40101038;

    /** (4010,1039) VR=CS VM=1 TIP Type */
    public static final int TIPType = 0x40101039;

    /** (4010,103A) VR=CS VM=1 DICOS Version */
    public static final int DICOSVersion = 0x4010103A;

    /** (4010,1041) VR=DT VM=1 OOI Owner Creation Time */
    public static final int OOIOwnerCreationTime = 0x40101041;

    /** (4010,1042) VR=CS VM=1 OOI Type */
    public static final int OOIType = 0x40101042;

    /** (4010,1043) VR=FL VM=3 OOI Size */
    public static final int OOISize = 0x40101043;

    /** (4010,1044) VR=CS VM=1 Acquisition Status */
    public static final int AcquisitionStatus = 0x40101044;

    /** (4010,1045) VR=SQ VM=1 Basis Materials Code Sequence */
    public static final int BasisMaterialsCodeSequence = 0x40101045;

    /** (4010,1046) VR=CS VM=1 Phantom Type */
    public static final int PhantomType = 0x40101046;

    /** (4010,1047) VR=SQ VM=1 OOI Owner Sequence */
    public static final int OOIOwnerSequence = 0x40101047;

    /** (4010,1048) VR=CS VM=1 Scan Type */
    public static final int ScanType = 0x40101048;

    /** (4010,1051) VR=LO VM=1 Itinerary ID */
    public static final int ItineraryID = 0x40101051;

    /** (4010,1052) VR=SH VM=1 Itinerary ID Type */
    public static final int ItineraryIDType = 0x40101052;

    /** (4010,1053) VR=LO VM=1 Itinerary ID Assigning Authority */
    public static final int ItineraryIDAssigningAuthority = 0x40101053;

    /** (4010,1054) VR=SH VM=1 Route ID */
    public static final int RouteID = 0x40101054;

    /** (4010,1055) VR=SH VM=1 Route ID Assigning Authority */
    public static final int RouteIDAssigningAuthority = 0x40101055;

    /** (4010,1056) VR=CS VM=1 Inbound Arrival Type */
    public static final int InboundArrivalType = 0x40101056;

    /** (4010,1058) VR=SH VM=1 Carrier ID */
    public static final int CarrierID = 0x40101058;

    /** (4010,1059) VR=CS VM=1 Carrier ID Assigning Authority */
    public static final int CarrierIDAssigningAuthority = 0x40101059;

    /** (4010,1060) VR=FL VM=3 Source Orientation */
    public static final int SourceOrientation = 0x40101060;

    /** (4010,1061) VR=FL VM=3 Source Position */
    public static final int SourcePosition = 0x40101061;

    /** (4010,1062) VR=FL VM=1 Belt Height */
    public static final int BeltHeight = 0x40101062;

    /** (4010,1064) VR=SQ VM=1 Algorithm Routing Code Sequence */
    public static final int AlgorithmRoutingCodeSequence = 0x40101064;

    /** (4010,1067) VR=CS VM=1 Transport Classification */
    public static final int TransportClassification = 0x40101067;

    /** (4010,1068) VR=LT VM=1 OOI Type Descriptor */
    public static final int OOITypeDescriptor = 0x40101068;

    /** (4010,1069) VR=FL VM=1 Total Processing Time */
    public static final int TotalProcessingTime = 0x40101069;

    /** (4010,106C) VR=OB VM=1 Detector Calibration Data */
    public static final int DetectorCalibrationData = 0x4010106C;

    /** (4010,106D) VR=CS VM=1 Additional Screening Performed */
    public static final int AdditionalScreeningPerformed = 0x4010106D;

    /** (4010,106E) VR=CS VM=1 Additional Inspection Selection Criteria */
    public static final int AdditionalInspectionSelectionCriteria = 0x4010106E;

    /** (4010,106F) VR=SQ VM=1 Additional Inspection Method Sequence */
    public static final int AdditionalInspectionMethodSequence = 0x4010106F;

    /** (4010,1070) VR=CS VM=1 AIT Device Type */
    public static final int AITDeviceType = 0x40101070;

    /** (4010,1071) VR=SQ VM=1 QR Measurements Sequence */
    public static final int QRMeasurementsSequence = 0x40101071;

    /** (4010,1072) VR=SQ VM=1 Target Material Sequence */
    public static final int TargetMaterialSequence = 0x40101072;

    /** (4010,1073) VR=FD VM=1 SNR Threshold */
    public static final int SNRThreshold = 0x40101073;

    /** (4010,1075) VR=DS VM=1 Image Scale Representation */
    public static final int ImageScaleRepresentation = 0x40101075;

    /** (4010,1076) VR=SQ VM=1 Referenced PTO Sequence */
    public static final int ReferencedPTOSequence = 0x40101076;

    /** (4010,1077) VR=SQ VM=1 Referenced TDR Instance Sequence */
    public static final int ReferencedTDRInstanceSequence = 0x40101077;

    /** (4010,1078) VR=ST VM=1 PTO Location Description */
    public static final int PTOLocationDescription = 0x40101078;

    /** (4010,1079) VR=SQ VM=1 Anomaly Locator Indicator Sequence */
    public static final int AnomalyLocatorIndicatorSequence = 0x40101079;

    /** (4010,107A) VR=FL VM=3 Anomaly Locator Indicator */
    public static final int AnomalyLocatorIndicator = 0x4010107A;

    /** (4010,107B) VR=SQ VM=1 PTO Region Sequence */
    public static final int PTORegionSequence = 0x4010107B;

    /** (4010,107C) VR=CS VM=1 Inspection Selection Criteria */
    public static final int InspectionSelectionCriteria = 0x4010107C;

    /** (4010,107D) VR=SQ VM=1 Secondary Inspection Method Sequence */
    public static final int SecondaryInspectionMethodSequence = 0x4010107D;

    /** (4010,107E) VR=DS VM=6 PRCS to RCS Orientation */
    public static final int PRCSToRCSOrientation = 0x4010107E;

    /** (4FFE,0001) VR=SQ VM=1 MAC Parameters Sequence */
    public static final int MACParametersSequence = 0x4FFE0001;

    /** (50xx,0005) VR=US VM=1 Curve Dimensions (retired) */
    public static final int CurveDimensions = 0x50000005;

    /** (50xx,0010) VR=US VM=1 Number of Points (retired) */
    public static final int NumberOfPoints = 0x50000010;

    /** (50xx,0020) VR=CS VM=1 Type of Data (retired) */
    public static final int TypeOfData = 0x50000020;

    /** (50xx,0022) VR=LO VM=1 Curve Description (retired) */
    public static final int CurveDescription = 0x50000022;

    /** (50xx,0030) VR=SH VM=1-n Axis Units (retired) */
    public static final int AxisUnits = 0x50000030;

    /** (50xx,0040) VR=SH VM=1-n Axis Labels (retired) */
    public static final int AxisLabels = 0x50000040;

    /** (50xx,0103) VR=US VM=1 Data Value Representation (retired) */
    public static final int DataValueRepresentation = 0x50000103;

    /** (50xx,0104) VR=US VM=1-n Minimum Coordinate Value (retired) */
    public static final int MinimumCoordinateValue = 0x50000104;

    /** (50xx,0105) VR=US VM=1-n Maximum Coordinate Value (retired) */
    public static final int MaximumCoordinateValue = 0x50000105;

    /** (50xx,0106) VR=SH VM=1-n Curve Range (retired) */
    public static final int CurveRange = 0x50000106;

    /** (50xx,0110) VR=US VM=1-n Curve Data Descriptor (retired) */
    public static final int CurveDataDescriptor = 0x50000110;

    /** (50xx,0112) VR=US VM=1-n Coordinate Start Value (retired) */
    public static final int CoordinateStartValue = 0x50000112;

    /** (50xx,0114) VR=US VM=1-n Coordinate Step Value (retired) */
    public static final int CoordinateStepValue = 0x50000114;

    /** (50xx,1001) VR=CS VM=1 Curve Activation Layer (retired) */
    public static final int CurveActivationLayer = 0x50001001;

    /** (50xx,2000) VR=US VM=1 Audio Type (retired) */
    public static final int AudioType = 0x50002000;

    /** (50xx,2002) VR=US VM=1 Audio Sample Format (retired) */
    public static final int AudioSampleFormat = 0x50002002;

    /** (50xx,2004) VR=US VM=1 Number of Channels (retired) */
    public static final int NumberOfChannels = 0x50002004;

    /** (50xx,2006) VR=UL VM=1 Number of Samples (retired) */
    public static final int NumberOfSamples = 0x50002006;

    /** (50xx,2008) VR=UL VM=1 Sample Rate (retired) */
    public static final int SampleRate = 0x50002008;

    /** (50xx,200A) VR=UL VM=1 Total Time (retired) */
    public static final int TotalTime = 0x5000200A;

    /** (50xx,200C) VR=OB or OW VM=1 Audio Sample Data (retired) */
    public static final int AudioSampleData = 0x5000200C;

    /** (50xx,200E) VR=LT VM=1 Audio Comments (retired) */
    public static final int AudioComments = 0x5000200E;

    /** (50xx,2500) VR=LO VM=1 Curve Label (retired) */
    public static final int CurveLabel = 0x50002500;

    /** (50xx,2600) VR=SQ VM=1 Curve Referenced Overlay Sequence (retired) */
    public static final int CurveReferencedOverlaySequence = 0x50002600;

    /** (50xx,2610) VR=US VM=1 Curve Referenced Overlay Group (retired) */
    public static final int CurveReferencedOverlayGroup = 0x50002610;

    /** (50xx,3000) VR=OB or OW VM=1 Curve Data (retired) */
    public static final int CurveData = 0x50003000;

    /** (5200,9229) VR=SQ VM=1 Shared Functional Groups Sequence */
    public static final int SharedFunctionalGroupsSequence = 0x52009229;

    /** (5200,9230) VR=SQ VM=1 Per-frame Functional Groups Sequence */
    public static final int PerFrameFunctionalGroupsSequence = 0x52009230;

    /** (5400,0100) VR=SQ VM=1 Waveform Sequence */
    public static final int WaveformSequence = 0x54000100;

    /** (5400,0110) VR=OB or OW VM=1 Channel Minimum Value */
    public static final int ChannelMinimumValue = 0x54000110;

    /** (5400,0112) VR=OB or OW VM=1 Channel Maximum Value */
    public static final int ChannelMaximumValue = 0x54000112;

    /** (5400,1004) VR=US VM=1 Waveform Bits Allocated */
    public static final int WaveformBitsAllocated = 0x54001004;

    /** (5400,1006) VR=CS VM=1 Waveform Sample Interpretation */
    public static final int WaveformSampleInterpretation = 0x54001006;

    /** (5400,100A) VR=OB or OW VM=1 Waveform Padding Value */
    public static final int WaveformPaddingValue = 0x5400100A;

    /** (5400,1010) VR=OB or OW VM=1 Waveform Data */
    public static final int WaveformData = 0x54001010;

    /** (5600,0010) VR=OF VM=1 First Order Phase Correction Angle */
    public static final int FirstOrderPhaseCorrectionAngle = 0x56000010;

    /** (5600,0020) VR=OF VM=1 Spectroscopy Data */
    public static final int SpectroscopyData = 0x56000020;

    /** (60xx,0010) VR=US VM=1 Overlay Rows */
    public static final int OverlayRows = 0x60000010;

    /** (60xx,0011) VR=US VM=1 Overlay Columns */
    public static final int OverlayColumns = 0x60000011;

    /** (60xx,0012) VR=US VM=1 Overlay Planes (retired) */
    public static final int OverlayPlanes = 0x60000012;

    /** (60xx,0015) VR=IS VM=1 Number of Frames in Overlay */
    public static final int NumberOfFramesInOverlay = 0x60000015;

    /** (60xx,0022) VR=LO VM=1 Overlay Description */
    public static final int OverlayDescription = 0x60000022;

    /** (60xx,0040) VR=CS VM=1 Overlay Type */
    public static final int OverlayType = 0x60000040;

    /** (60xx,0045) VR=LO VM=1 Overlay Subtype */
    public static final int OverlaySubtype = 0x60000045;

    /** (60xx,0050) VR=SS VM=2 Overlay Origin */
    public static final int OverlayOrigin = 0x60000050;

    /** (60xx,0051) VR=US VM=1 Image Frame Origin */
    public static final int ImageFrameOrigin = 0x60000051;

    /** (60xx,0052) VR=US VM=1 Overlay Plane Origin (retired) */
    public static final int OverlayPlaneOrigin = 0x60000052;

    /** (60xx,0060) VR=CS VM=1 Overlay Compression Code (retired) */
    public static final int OverlayCompressionCode = 0x60000060;

    /** (60xx,0061) VR=SH VM=1 Overlay Compression Originator (retired) */
    public static final int OverlayCompressionOriginator = 0x60000061;

    /** (60xx,0062) VR=SH VM=1 Overlay Compression Label (retired) */
    public static final int OverlayCompressionLabel = 0x60000062;

    /** (60xx,0063) VR=CS VM=1 Overlay Compression Description (retired) */
    public static final int OverlayCompressionDescription = 0x60000063;

    /** (60xx,0066) VR=AT VM=1-n Overlay Compression Step Pointers (retired) */
    public static final int OverlayCompressionStepPointers = 0x60000066;

    /** (60xx,0068) VR=US VM=1 Overlay Repeat Interval (retired) */
    public static final int OverlayRepeatInterval = 0x60000068;

    /** (60xx,0069) VR=US VM=1 Overlay Bits Grouped (retired) */
    public static final int OverlayBitsGrouped = 0x60000069;

    /** (60xx,0100) VR=US VM=1 Overlay Bits Allocated */
    public static final int OverlayBitsAllocated = 0x60000100;

    /** (60xx,0102) VR=US VM=1 Overlay Bit Position */
    public static final int OverlayBitPosition = 0x60000102;

    /** (60xx,0110) VR=CS VM=1 Overlay Format (retired) */
    public static final int OverlayFormat = 0x60000110;

    /** (60xx,0200) VR=US VM=1 Overlay Location (retired) */
    public static final int OverlayLocation = 0x60000200;

    /** (60xx,0800) VR=CS VM=1-n Overlay Code Label (retired) */
    public static final int OverlayCodeLabel = 0x60000800;

    /** (60xx,0802) VR=US VM=1 Overlay Number of Tables (retired) */
    public static final int OverlayNumberOfTables = 0x60000802;

    /** (60xx,0803) VR=AT VM=1-n Overlay Code Table Location (retired) */
    public static final int OverlayCodeTableLocation = 0x60000803;

    /** (60xx,0804) VR=US VM=1 Overlay Bits For Code Word (retired) */
    public static final int OverlayBitsForCodeWord = 0x60000804;

    /** (60xx,1001) VR=CS VM=1 Overlay Activation Layer */
    public static final int OverlayActivationLayer = 0x60001001;

    /** (60xx,1100) VR=US VM=1 Overlay Descriptor - Gray (retired) */
    public static final int OverlayDescriptorGray = 0x60001100;

    /** (60xx,1101) VR=US VM=1 Overlay Descriptor - Red (retired) */
    public static final int OverlayDescriptorRed = 0x60001101;

    /** (60xx,1102) VR=US VM=1 Overlay Descriptor - Green (retired) */
    public static final int OverlayDescriptorGreen = 0x60001102;

    /** (60xx,1103) VR=US VM=1 Overlay Descriptor - Blue (retired) */
    public static final int OverlayDescriptorBlue = 0x60001103;

    /** (60xx,1200) VR=US VM=1-n Overlays - Gray (retired) */
    public static final int OverlaysGray = 0x60001200;

    /** (60xx,1201) VR=US VM=1-n Overlays - Red (retired) */
    public static final int OverlaysRed = 0x60001201;

    /** (60xx,1202) VR=US VM=1-n Overlays - Green (retired) */
    public static final int OverlaysGreen = 0x60001202;

    /** (60xx,1203) VR=US VM=1-n Overlays - Blue (retired) */
    public static final int OverlaysBlue = 0x60001203;

    /** (60xx,1301) VR=IS VM=1 ROI Area */
    public static final int ROIArea = 0x60001301;

    /** (60xx,1302) VR=DS VM=1 ROI Mean */
    public static final int ROIMean = 0x60001302;

    /** (60xx,1303) VR=DS VM=1 ROI Standard Deviation */
    public static final int ROIStandardDeviation = 0x60001303;

    /** (60xx,1500) VR=LO VM=1 Overlay Label */
    public static final int OverlayLabel = 0x60001500;

    /** (60xx,3000) VR=OB or OW VM=1 Overlay Data */
    public static final int OverlayData = 0x60003000;

    /** (60xx,4000) VR=LT VM=1 Overlay Comments (retired) */
    public static final int OverlayComments = 0x60004000;

    /** (7FE0,0001) VR=OV VM=1 Extended Offset Table */
    public static final int ExtendedOffsetTable = 0x7FE00001;

    /** (7FE0,0002) VR=OV VM=1 Extended Offset Table Lengths */
    public static final int ExtendedOffsetTableLengths = 0x7FE00002;

    /** (7FE0,0008) VR=OF VM=1 Float Pixel Data */
    public static final int FloatPixelData = 0x7FE00008;

    /** (7FE0,0009) VR=OD VM=1 Double Float Pixel Data */
    public static final int DoubleFloatPixelData = 0x7FE00009;

    /** (7FE0,0010) VR=OB or OW VM=1 Pixel Data */
    public static final int PixelData = 0x7FE00010;

    /** (7FE0,0020) VR=OW VM=1 Coefficients SDVN (retired) */
    public static final int CoefficientsSDVN = 0x7FE00020;

    /** (7FE0,0030) VR=OW VM=1 Coefficients SDHN (retired) */
    public static final int CoefficientsSDHN = 0x7FE00030;

    /** (7FE0,0040) VR=OW VM=1 Coefficients SDDN (retired) */
    public static final int CoefficientsSDDN = 0x7FE00040;

    /** (7Fxx,0010) VR=OB or OW VM=1 Variable Pixel Data (retired) */
    public static final int VariablePixelData = 0x7F000010;

    /** (7Fxx,0011) VR=US VM=1 Variable Next Data Group (retired) */
    public static final int VariableNextDataGroup = 0x7F000011;

    /** (7Fxx,0020) VR=OW VM=1 Variable Coefficients SDVN (retired) */
    public static final int VariableCoefficientsSDVN = 0x7F000020;

    /** (7Fxx,0030) VR=OW VM=1 Variable Coefficients SDHN (retired) */
    public static final int VariableCoefficientsSDHN = 0x7F000030;

    /** (7Fxx,0040) VR=OW VM=1 Variable Coefficients SDDN (retired) */
    public static final int VariableCoefficientsSDDN = 0x7F000040;

    /** (FFFA,FFFA) VR=SQ VM=1 Digital Signatures Sequence */
    public static final int DigitalSignaturesSequence = 0xFFFAFFFA;

    /** (FFFC,FFFC) VR=OB VM=1 Data Set Trailing Padding */
    public static final int DataSetTrailingPadding = 0xFFFCFFFC;

    /** (FFFE,E000) VR= VM=1 Item */
    public static final int Item = 0xFFFEE000;

    /** (FFFE,E00D) VR= VM=1 Item Delimitation Item */
    public static final int ItemDelimitationItem = 0xFFFEE00D;

    /** (FFFE,E0DD) VR= VM=1 Sequence Delimitation Item */
    public static final int SequenceDelimitationItem = 0xFFFEE0DD;

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
