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
package org.dcm4che3.xdsi;

public interface XDSConstants {

    public static final String XDS_ERR_MISSING_DOCUMENT = "XDSMissingDocument";
    public static final String XDS_ERR_MISSING_DOCUMENT_METADATA = "XDSMissingDocumentMetadata";
    public static final String XDS_ERR_REG_NOT_AVAIL = "XDSRegistryNotAvailable";
    public static final String XDS_ERR_REGISTRY_ERROR = "XDSRegistryError";
    public static final String XDS_ERR_REPOSITORY_ERROR = "XDSRepositoryError";
    public static final String XDS_ERR_REGISTRY_DUPLICATE_UNIQUE_ID_IN_MSG = "XDSRegistryDuplicateUniqueIdInMessage";
    public static final String XDS_ERR_REPOSITORY_DUPLICATE_UNIQUE_ID_IN_MSG = "XDSRepositoryDuplicateUniqueIdInMessage";
    public static final String XDS_ERR_DUPLICATE_UNIQUE_ID_IN_REGISTRY = "XDSDuplicateUniqueIdInRegistry";
    public static final String XDS_ERR_NON_IDENTICAL_HASH = "XDSNonIdenticalHash";
    public static final String XDS_ERR_NON_IDENTICAL_SIZE = "XDSNonIdenticalSize";
    public static final String XDS_ERR_REGISTRY_BUSY = "XDSRegistryBusy";
    public static final String XDS_ERR_REPOSITORY_BUSY = "XDSRepositoryBusy";
    public static final String XDS_ERR_REGISTRY_OUT_OF_RESOURCES = "XDSRegistryOutOfResources";
    public static final String XDS_ERR_REPOSITORY_OUT_OF_RESOURCES = "XDSRepositoryOutOfResources";
    public static final String XDS_ERR_REGISTRY_METADATA_ERROR = "XDSRegistryMetadataError";
    public static final String XDS_ERR_REPOSITORY_METADATA_ERROR = "XDSRepositoryMetadataError";
    public static final String XDS_ERR_TOO_MANY_RESULTS = "XDSTooManyResults";
    public static final String XDS_ERR_EXTRA_METADATA_NOT_SAVED = "XDSExtraMetadataNotSaved";
    public static final String XDS_ERR_UNKNOWN_PATID = "XDSUnknownPatientId";
    public static final String XDS_ERR_PATID_DOESNOT_MATCH = "XDSPatientIdDoesNotMatch";
    public static final String XDS_ERR_UNKNOWN_STORED_QUERY_ID = "XDSUnknownStoredQuery";
    public static final String XDS_ERR_STORED_QUERY_MISSING_PARAM = "XDSStoredQueryMissingParam";
    public static final String XDS_ERR_STORED_QUERY_PARAM_NUMBER = "XDSStoredQueryParamNumber";
    public static final String XDS_ERR_REGISTRY_DEPRECATED_DOC_ERROR = "XDSRegistryDeprecatedDocumentError";
    public static final String XDS_ERR_UNKNOWN_REPOSITORY_ID = "XDSUnknownRepositoryId";
    public static final String XDS_ERR_DOCUMENT_UNIQUE_ID_ERROR = "XDSDocumentUniqueIdError";
    public static final String XDS_ERR_RESULT_NOT_SINGLE_PATIENT = "XDSResultNotSinglePatient";
    public static final String XDS_ERR_PARTIAL_FOLDER_CONTENT_NOT_PROCESSED = "PartialFolderContentNotProcessed";
    public static final String XDS_ERR_PARTIAL_REPLACE_CONTENT_NOT_PROCESSED = "PartialReplaceContentNotProcessed";
    public static final String XDS_ERR_UNKNOWN_COMMUNITY = "XDSUnknownCommunity";
    public static final String XDS_ERR_MISSING_HOME_COMMUNITY_ID = "XDSMissingHomeCommunityId";
    public static final String XDS_ERR_UNAVAILABLE_COMMUNITY = "XDSUnavailableCommunity";

    //Association Types
    public static final String HAS_MEMBER = "urn:oasis:names:tc:ebxml-regrep:AssociationType:HasMember";
    public static final String RELATED_TO = "urn:oasis:names:tc:ebxml-regrep:AssociationType:relatedTo";
    //UUID Document Relationships
    public static final String RPLC = "urn:ihe:iti:2007:AssociationType:RPLC";
    public static final String APND = "urn:ihe:iti:2007:AssociationType:APND";
    public static final String XFRM = "urn:ihe:iti:2007:AssociationType:XFRM";
    public static final String XFRM_RPLC = "urn:ihe:iti:2007:AssociationType:XFRM_RPLC";
    public static final String SIGNS = "urn:ihe:iti:2007:AssociationType:signs";

    //Response stati 
    public static final String XDS_STATUS_SUCCESS = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success";
    public static final String XDS_STATUS_FAILURE = "urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Failure";
    public static final String XDS_STATUS_PARTIAL_SUCCESS = "urn:ihe:iti:2007:ResponseStatusType:PartialSuccess";
    public static final String XDS_ERR_SEVERITY_WARNING = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Warning";
    public static final String XDS_ERR_SEVERITY_ERROR = "urn:oasis:names:tc:ebxml-regrep:ErrorSeverityType:Error";


    //RegistryObject types
    public static final String CLASSIFICATION = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Classification";
    public static final String EXTERNAL_IDENTIFIER = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:ExternalIdentifier";
    public static final String ASSOCIATION = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:Association";

    //RegistryObject stati
    public static final String STATUS_SUBMITTED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Submitted";
    public static final String STATUS_APPROVED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Approved";
    public static final String STATUS_DEPRECATED = "urn:oasis:names:tc:ebxml-regrep:StatusType:Deprecated";

    //UUID SubmissionSet
    public static final String UUID_XDSSubmissionSet = "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd";
    public static final String UUID_XDSSubmissionSet_autor = "urn:uuid:a7058bb9-b4e4-4307-ba5b-e3f0ab85e12d";
    public static final String UUID_XDSSubmissionSet_contentTypeCode = "urn:uuid:aa543740-bdda-424e-8c96-df4873be8500";
    public static final String UUID_XDSSubmissionSet_patientId = "urn:uuid:6b5aea1a-874d-4603-a4bc-96a0a7b38446";
    public static final String UUID_XDSSubmissionSet_sourceId = "urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832";
    public static final String UUID_XDSSubmissionSet_uniqueId = "urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8";
    public static final String UUID_XDSSubmissionSet_limitedMetadata = "urn:uuid:5003a9db-8d8d-49e6-bf0c-990e34ac7707";
    
    // UUID XDSDocumentEntry
    public static final String UUID_XDSDocumentEntry = "urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1";
    public static final String UUID_XDSDocumentEntry_author = "urn:uuid:93606bcf-9494-43ec-9b4e-a7748d1a838d";
    public static final String UUID_XDSDocumentEntry_classCode = "urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a";
    public static final String UUID_XDSDocumentEntry_confidentialityCode = "urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f";
    public static final String UUID_XDSDocumentEntry_eventCodeList = "urn:uuid:2c6b8cb7-8b2a-4051-b291-b1ae6a575ef4";
    public static final String UUID_XDSDocumentEntry_formatCode = "urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d";
    public static final String UUID_XDSDocumentEntry_healthCareFacilityTypeCode = "urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1";
    public static final String UUID_XDSDocumentEntry_patientId = "urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427";
    public static final String UUID_XDSDocumentEntry_practiceSettingCode = "urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead";
    public static final String UUID_XDSDocumentEntry_typeCode = "urn:uuid:f0306f51-975f-434e-a61c-c59651d33983";
    public static final String UUID_XDSDocumentEntry_uniqueId = "urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab";
    public static final String UUID_XDSDocumentEntry_limitedMetadata = "urn:uuid:ab9b591b-83ab-4d03-8f5d-f93b1fb92e85";
    
    // UUID XDSFolder
    public static final String UUID_XDSFolder = "urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2";
    public static final String UUID_XDSFolder_codeList = "urn:uuid:1ba97051-7806-41a8-a48b-8fce7af683c5";
    public static final String UUID_XDSFolder_patientId = "urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a";
    public static final String UUID_XDSFolder_uniqueId = "urn:uuid:75df8f67-9973-4fbe-a900-df66cefecc5a";
    public static final String UUID_XDSFolder_limitedMetadata = "urn:uuid:2c144a76-29a9-4b7c-af54-b25409fe7d03";
        
    // UUID other
    public static final String UUID_XDSDocumentEntryStub = "urn:uuid:10aa1a4b-715a-4120-bfd0-9760414112c8";
    public static final String UUID_Association_Documentation = "urn:uuid:abd807a3-4432-4053-87b4-fd82c643d1f3";
    
    //Stored Query
    public static final String XDS_FindDocuments = "urn:uuid:14d4debf-8f97-4251-9a74-a90016b0af0d";      
    public static final String XDS_FindDocumentsByReferenceId = "urn:uuid:12941a89-e02e-4be5-967cce4bfc8fe492";      
    public static final String XDS_FindSubmissionSets = "urn:uuid:f26abbcb-ac74-4422-8a30-edb644bbc1a9";         
    public static final String XDS_FindFolders = "urn:uuid:958f3006-baad-4929-a4de-ff1114824431";        
    public static final String XDS_GetAll = "urn:uuid:10b545ea-725c-446d-9b95-8aeb444eddf3";     
    public static final String XDS_GetDocuments = "urn:uuid:5c4f972b-d56b-40ac-a5fc-c8ca9b40b9d4";       
    public static final String XDS_GetFolders = "urn:uuid:5737b14c-8a1a-4539-b659-e03a34a5e1e4";         
    public static final String XDS_GetAssociations = "urn:uuid:a7ae438b-4bc2-4642-93e9-be891f7bb155";            
    public static final String XDS_GetDocumentsAndAssociations = "urn:uuid:bab9529a-4a10-40b3-a01f-f68a615d247a";        
    public static final String XDS_GetSubmissionSets = "urn:uuid:51224314-5390-4169-9b91-b1980040715a";          
    public static final String XDS_GetSubmissionSetAndContents = "urn:uuid:e8e3cb2c-e39c-46b9-99e4-c12f57260b83";        
    public static final String XDS_GetFolderAndContents = "urn:uuid:b909a503-523d-4517-8acf-8e5834dfc4c7";       
    public static final String XDS_GetFoldersForDocument = "urn:uuid:10cae35a-c7f9-4cf5-b61e-fc3278ffb578";      
    public static final String XDS_GetRelatedDocuments = "urn:uuid:d90e5407-b356-4d91-a89f-873917b4b0e6";    
    //Stored Query Parameter
    //FindDocuments
    public static final String QRY_DOCUMENT_ENTRY_PATIENT_ID = "$XDSDocumentEntryPatientId";
    public static final String QRY_DOCUMENT_ENTRY_CLASS_CODE = "$XDSDocumentEntryClassCode";
    public static final String QRY_DOCUMENT_ENTRY_TYPE_CODE = "$XDSDocumentEntryTypeCode";
    public static final String QRY_DOCUMENT_ENTRY_PRACTICE_SETTING_CODE = "$XDSDocumentEntryPracticeSettingCode";
    public static final String QRY_DOCUMENT_ENTRY_CREATION_TIME_FROM = "$XDSDocumentEntryCreationTimeFrom";
    public static final String QRY_DOCUMENT_ENTRY_CREATION_TIME_TO = "$XDSDocumentEntryCreationTimeTo";
    public static final String QRY_DOCUMENT_ENTRY_SERVICE_START_TIME_FROM = "$XDSDocumentEntryServiceStartTimeFrom";
    public static final String QRY_DOCUMENT_ENTRY_SERVICE_START_TIME_TO = "$XDSDocumentEntryServiceStartTimeTo";
    public static final String QRY_DOCUMENT_ENTRY_SERVICE_STOP_TIME_FROM = "$XDSDocumentEntryServiceStopTimeFrom";
    public static final String QRY_DOCUMENT_ENTRY_SERVICE_STOP_TIME_TO = "$XDSDocumentEntryServiceStopTimeTo";
    public static final String QRY_DOCUMENT_ENTRY_HEALTHCARE_FACILITY_TYPE_CODE = "$XDSDocumentEntryHealthcareFacilityTypeCode";
    public static final String QRY_DOCUMENT_ENTRY_EVENT_CODE_LIST = "$XDSDocumentEntryEventCodeList";
    public static final String QRY_DOCUMENT_ENTRY_CONFIDENTIALITY_CODE = "$XDSDocumentEntryConfidentialityCode";
    public static final String QRY_DOCUMENT_ENTRY_AUTHOR_PERSON = "$XDSDocumentEntryAuthorPerson";
    public static final String QRY_DOCUMENT_ENTRY_FORMAT_CODE = "$XDSDocumentEntryFormatCode";
    public static final String QRY_DOCUMENT_ENTRY_STATUS = "$XDSDocumentEntryStatus";
    public static final String QRY_DOCUMENT_ENTRY_REFERENCED_ID_LIST = "$XDSDocumentEntryReferenceIdList";
    //FindSubmissionSet
    public static final String QRY_SUBMISSIONSET_PATIENT_ID = "$XDSSubmissionSetPatientId";
    public static final String QRY_SUBMISSIONSET_SOURCE_ID = "$XDSSubmissionSetSourceId";
    public static final String QRY_SUBMISSIONSET_SUBMISSION_TIME_FROM = "$XDSSubmissionSetSubmissionTimeFrom";
    public static final String QRY_SUBMISSIONSET_SUBMISSION_TIME_TO = "$XDSSubmissionSetSubmissionTimeTo";
    public static final String QRY_SUBMISSIONSET_AUTHOR_PERSON = "$XDSSubmissionSetAuthorPerson";
    public static final String QRY_SUBMISSIONSET_CONTENT_TYPE = "$XDSSubmissionSetContentType";
    public static final String QRY_SUBMISSIONSET_STATUS = "$XDSSubmissionSetStatus";
    //FindFolders
    public static final String QRY_FOLDER_PATIENT_ID = "$XDSFolderPatientId";
    public static final String QRY_FOLDER_LAST_UPDATE_TIME_FROM = "$XDSFolderLastUpdateTimeFrom";
    public static final String QRY_FOLDER_LAST_UPDATE_TIME_TO = "$XDSFolderLastUpdateTimeTo";
    public static final String QRY_FOLDER_CODE_LIST = "$XDSFolderCodeList";
    public static final String QRY_FOLDER_STATUS = "$XDSFolderStatus";
    //GetSubmissionSet
    public static final String QRY_SUBMISSIONSET_UNIQUE_ID = "$XDSSubmissionSetUniqueId";
    public static final String QRY_SUBMISSIONSET_ENTRY_UUID = "$XDSSubmissionSetEntryUUID";
    //GetDocuments
    public static final String QRY_DOCUMENT_ENTRY_UUID = "$XDSDocumentEntryEntryUUID";
    public static final String QRY_DOCUMENT_UNIQUE_ID = "$XDSDocumentEntryUniqueId";
    //GetFolder
    public static final String QRY_FOLDER_ENTRY_UUID = "$XDSFolderEntryUUID";
    public static final String QRY_FOLDER_UNIQUE_ID = "$XDSFolderUniqueId";
    //GetAssociations
    public static final String QRY_UUID = "$uuid";
    //GetAll
    public static final String QRY_PATIENT_ID = "$patientId";

    public static final String QRY_ASSOCIATION_TYPES = "$AssociationTypes";
    public static final String QRY_HOME_COMMUNITY_ID = "$homeCommunityId";

    //Stored Query queryTypes
    public static final String QUERY_RETURN_TYPE_LEAF = "LeafClass";
    public static final String QUERY_RETURN_TYPE_OBJREF = "ObjectRef";

    //Slot names
    public static final String SLOT_NAME_AUTHOR_PERSON = "authorPerson";
    public static final String SLOT_NAME_CREATION_TIME = "creationTime";
    public static final String SLOT_NAME_SERVICE_START_TIME = "serviceStartTime";
    public static final String SLOT_NAME_SERVICE_STOP_TIME = "serviceStopTime";
    public static final String SLOT_NAME_SUBMISSION_TIME = "submissionTime";
    public static final String SLOT_NAME_SUBMISSIONSET_STATUS = "SubmissionSetStatus";
    public static final String SLOT_NAME_LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String SLOT_NAME_REPOSITORY_UNIQUE_ID = "repositoryUniqueId";
    public static final String SLOT_NAME_SIZE = "size";
    public static final String SLOT_NAME_HASH = "hash";
    public static final String SLOT_NAME_SOURCE_PATIENT_ID = "sourcePatientId";
    public static final String SLOT_NAME_SOURCE_PATIENT_INFO = "sourcePatientInfo";
    public static final String SLOT_NAME_LANGUAGE_CODE = "languageCode";
    public static final String SLOT_NAME_INTENDED_RECIPIENT = "intendedRecipient";
    public static final String SLOT_NAME_LEGAL_AUTHENTICATOR = "legalAuthenticator";
    public static final String SLOT_NAME_REFERENCE_ID_LIST = "urn:ihe:iti:xds:2013:referenceIdList";
    //Other
    public static final String WS_ADDRESSING_NS = "http://www.w3.org/2005/08/addressing";
    public static final String WS_ADDRESSING_ANONYMOUS = "http://www.w3.org/2005/08/addressing/anonymous";

    //CXi identifier Type Codes
    public static final String CXI_TYPE_UNIQUQ_ID = "urn:ihe:iti:xds:2013:uniqueId";
    public static final String CXI_TYPE_ACCESSION = "urn:ihe:iti:xds:2013:accession";
    public static final String CXI_TYPE_REFERRAL = "urn:ihe:iti:xds:2013:referral";
    public static final String CXI_TYPE_ORDER = "urn:ihe:iti:xds:2013:order";
    public static final String CXI_TYPE_WORKFLOW_INSTANCE_ID = "urn:ihe:iti:xdw:2013:workflowInstanceId";
    public static final String CXI_TYPE_STUDY_INSTANCE_UID = "urn:ihe:iti:xds:2016:studyInstanceUID";
    public static final String CXI_TYPE_ENCOUNTER_ID = "urn:ihe:iti:xds:2015:encounterId";
}
