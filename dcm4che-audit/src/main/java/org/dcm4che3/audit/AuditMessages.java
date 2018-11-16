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
 * Java(TM), hosted at https://github.com/dcm4che/dcm4che3.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.audit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Michael Backhaus <michael.backhaus@agfa.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 */
public class AuditMessages {

    private static final Pattern IP4 =
            Pattern.compile("\\d+(\\.\\d+){3}");
    private static final Pattern IP6 =
            Pattern.compile("[0-9a-fA-F]*(\\:[0-9a-fA-F]*){7}");
    public static final String SCHEMA_URI =
            "http://www.dcm4che.org/DICOM/audit-message.rnc";

    private static final ObjectFactory of = new ObjectFactory();
    private static JAXBContext jc;
    private static JAXBContext jc() throws JAXBException {
        JAXBContext jc = AuditMessages.jc;
        if (jc == null)
            AuditMessages.jc = jc = JAXBContext.newInstance("org.dcm4che3.audit", AuditMessage.class.getClassLoader());
        return jc;
    }

    public static boolean isIP(String s) {
        return IP4.matcher(s).matches() || IP6.matcher(s).matches();
    }

    /**
     * Enumerated Value: C = (create) if the receiver did not hold 
     * copies of the instances transferred 
     * 
     * R = (read) if the receiver already holds copies of the SOP 
     * Instances transferred, and has determined that no changes are 
     * needed to the copies held. 
     * 
     * U = (update) if the receiver is altering its held copies 
     * to reconcile differences between the held copies and the received copies. 
     * 
     * If the Audit Source is either not the receiver, or otherwise does not 
     * know whether or not the instances previously were held by the receiving 
     * node, then use “R” = (Read). 
     *
     */
    public static final class EventActionCode {
        public static final String Create = "C";
        public static final String Read = "R";
        public static final String Update = "U";
        public static final String Delete = "D";
        public static final String Execute = "E";
    }

    public static final class EventOutcomeIndicator {
        public static final String Success = "0";
        public static final String MinorFailure = "4";
        public static final String SeriousFailure = "8";
        public static final String MajorFailure = "12";
    }

    public static final class EventID extends org.dcm4che3.audit.EventID {

        public static final EventID ApplicationActivity =
                new EventID("110100", "DCM", "Application Activity");
        public static final EventID AuditLogUsed = 
                new EventID("110101", "DCM", "Audit Log Used");
        public static final EventID BeginTransferringDICOMInstances = 
                new EventID("110102", "DCM", "Begin Transferring DICOM Instances");
        public static final EventID DICOMInstancesAccessed = 
                new EventID("110103", "DCM", "DICOM Instances Accessed");
        public static final EventID DICOMInstancesTransferred = 
                new EventID("110104", "DCM", "DICOM Instances Transferred");
        public static final EventID DICOMStudyDeleted = 
                new EventID("110105", "DCM", "DICOM Study Deleted");
        public static final EventID Export =
                new EventID("110106", "DCM", "Export");
        public static final EventID Import =
                new EventID("110107", "DCM", "Import");
        public static final EventID NetworkEntry = 
                new EventID("110108", "DCM", "Network Entry");
        public static final EventID OrderRecord = 
                new EventID("110109", "DCM", "Order Record");
        public static final EventID PatientRecord = 
                new EventID("110110", "DCM", "Patient Record");
        public static final EventID ProcedureRecord = 
                new EventID("110111", "DCM", "Procedure Record");
        public static final EventID Query =
                new EventID("110112", "DCM", "Query");
        public static final EventID SecurityAlert = 
                new EventID("110113", "DCM", "Security Alert");
        public static final EventID UserAuthentication = 
                new EventID("110114", "DCM", "User Authentication");
        public static final EventID HealthServicesProvisionEvent = 
                new EventID("IHE0001", "IHE", "Health Services Provision Event");
        public static final EventID MedicationEvent = 
                new EventID("IHE0002", "IHE", "Medication Event");
        public static final EventID PatientCareResourceAssignment = 
                new EventID("IHE0003", "IHE", "Patient Care Resource Assignment");
        public static final EventID PatientCareEpisode = 
                new EventID("IHE0004", "IHE", "Patient Care Episode");
        public static final EventID PatientCareProtocol = 
                new EventID("IHE0005", "IHE", "Patient Care Protocol");

        EventID(String code, String codeSystemName, String originalText) {
            super.csdCode = code;
            super.codeSystemName = codeSystemName;
            super.originalText = originalText;
        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }
    }

    public static final class EventTypeCode
            extends org.dcm4che3.audit.EventTypeCode {

        public static final EventTypeCode ApplicationStart = 
                new EventTypeCode("110120", "DCM", "Application Start");
        public static final EventTypeCode ApplicationStop = 
                new EventTypeCode("110121", "DCM", "Application Stop");
        public static final EventTypeCode Login = 
                new EventTypeCode("110122", "DCM", "Login");
        public static final EventTypeCode Logout = 
                new EventTypeCode("110123", "DCM", "Logout");
        public static final EventTypeCode Attach = 
                new EventTypeCode("110124", "DCM", "Attach");
        public static final EventTypeCode Detach = 
                new EventTypeCode("110125", "DCM", "Detach");
        public static final EventTypeCode NodeAuthentication = 
                new EventTypeCode("110126", "DCM", "Node Authentication");
        public static final EventTypeCode EmergencyOverrideStarted = 
                new EventTypeCode("110127", "DCM", "Emergency Override Started");
        public static final EventTypeCode NetworkConfiguration = 
                new EventTypeCode("110128", "DCM", "Network Configuration");
        public static final EventTypeCode SecurityConfiguration = 
                new EventTypeCode("110129", "DCM", "Security Configuration");
        public static final EventTypeCode HardwareConfiguration = 
                new EventTypeCode("110130", "DCM", "Hardware Configuration");
        public static final EventTypeCode SoftwareConfiguration = 
                new EventTypeCode("110131", "DCM", "Software Configuration");
        public static final EventTypeCode UseOfRestrictedFunction = 
                new EventTypeCode("110132", "DCM", "Use of Restricted Function");
        public static final EventTypeCode AuditRecordingStopped = 
                new EventTypeCode("110133", "DCM", "Audit Recording Stopped");
        public static final EventTypeCode AuditRecordingStarted = 
                new EventTypeCode("110134", "DCM", "Audit Recording Started");
        public static final EventTypeCode ObjectSecurityAttributesChanged = 
                new EventTypeCode("110135", "DCM", "Object Security Attributes Changed");
        public static final EventTypeCode SecurityRolesChanged = 
                new EventTypeCode("110136", "DCM", "Security Roles Changed");
        public static final EventTypeCode UserSecurityAttributesChanged = 
                new EventTypeCode("110137", "DCM", "User security Attributes Changed");
        public static final EventTypeCode EmergencyOverrideStopped = 
                new EventTypeCode("110138", "DCM", "Emergency Override Stopped");
        public static final EventTypeCode RemoteServiceOperationStarted = 
                new EventTypeCode("110139", "DCM", "Remote Service Operation Started");
        public static final EventTypeCode RemoteServiceOperationStopped = 
                new EventTypeCode("110140", "DCM", "Remote Service Operation Stopped");
        public static final EventTypeCode LocalServiceOperationStarted = 
                new EventTypeCode("110141", "DCM", "Local Service Operation Started");
        public static final EventTypeCode LocalServiceOperationStopped = 
                new EventTypeCode("110142", "DCM", "Local Service Operation Stopped");
        //Defined in IHE IT Infrastructure (ITI)
        public static final EventTypeCode ITI_8_PatientIdentityFeed = 
            new EventTypeCode("ITI-8", "IHE Transactions", "Patient Identity Feed");
        public static final EventTypeCode ITI_9_PIXQuery = 
            new EventTypeCode("ITI-9", "IHE Transactions", "PIX Query");
        public static final EventTypeCode ITI_10_PIXUpdateNotification = 
            new EventTypeCode("ITI-10", "IHE Transactions", "PIX Update Notification");
        public static final EventTypeCode ITI_18_RegistryStoredQuery = 
            new EventTypeCode("ITI-18", "IHE Transactions", "Registry Stored Query");
        public static final EventTypeCode ITI_21_PatientDemographicsQuery = 
            new EventTypeCode("ITI-21", "IHE Transactions", "Patient Demographics Query");
        public static final EventTypeCode ITI_22_PatientDemographicsAndVisitQuery = 
            new EventTypeCode("ITI-22", "IHE Transactions", "Patient Demographics and Visit Query");
        public static final EventTypeCode ITI_38_CrossGatewayQuery = 
            new EventTypeCode("ITI-38", "IHE Transactions", "Cross Gateway Query");
        public static final EventTypeCode ITI_39_CrossGatewayRetrieve = 
            new EventTypeCode("ITI-39", "IHE Transactions", "Cross Gateway Retrieve");
        public static final EventTypeCode ITI_41_ProvideAndRegisterDocumentSetB = 
            new EventTypeCode("ITI-41", "IHE Transactions", "Provide and Register Document Set-b");
        public static final EventTypeCode ITI_42_RegisterDocumentSetB = 
            new EventTypeCode("ITI-42", "IHE Transactions", "Register Document Set-b");
        public static final EventTypeCode ITI_43_RetrieveDocumentSet = 
            new EventTypeCode("ITI-43", "IHE Transactions", "Retrieve Document Set");
        public static final EventTypeCode ITI_44_PatientIdentityFeed = 
            new EventTypeCode("ITI-44", "IHE Transactions", "Patient Identity Feed");
        public static final EventTypeCode ITI_45_PIXQuery = 
            new EventTypeCode("ITI-45", "IHE Transactions", "PIX Query");
        public static final EventTypeCode ITI_46_PIXUpdateNotification = 
            new EventTypeCode("ITI-46", "IHE Transactions", "PIX Update Notification");
        public static final EventTypeCode ITI_47_PatientDemographicsQuery = 
            new EventTypeCode("ITI-47", "IHE Transactions", "Patient Demographics Query");
        public static final EventTypeCode ITI_51_MultiPatientQuery = 
            new EventTypeCode("ITI-51", "IHE Transactions", "Multi-Patient Query");
        public static final EventTypeCode ITI_63_XCFFetch = 
            new EventTypeCode("ITI-63", "IHE Transactions", "XCF Fetch");
        public static final EventTypeCode CancelTask =
                new EventTypeCode("CANCEL", "99DCM4CHEE", "Cancel Task");
        public static final EventTypeCode RescheduleTask =
                new EventTypeCode("RESCHEDULE", "99DCM4CHEE", "Reschedule Task");
        public static final EventTypeCode DeleteTask =
                new EventTypeCode("DELETE", "99DCM4CHEE", "Delete Task");
        public static final EventTypeCode AssociationFailure =
                new EventTypeCode("ASSOCIATION-FAILURE", "99DCM4CHEE", "Association Failure");
        public static final EventTypeCode x0110 =
                new EventTypeCode("0110", "99DCM4CHEE", "Processing Failure");
        public static final EventTypeCode x0118 =
                new EventTypeCode("0118", "99DCM4CHEE", "No Such SOP Class");
        public static final EventTypeCode x0122 =
                new EventTypeCode("0122", "99DCM4CHEE", "SOP Class Not Supported");
        public static final EventTypeCode x0124 =
                new EventTypeCode("0124", "99DCM4CHEE", "Not Authorized");
        public static final EventTypeCode x0211 =
                new EventTypeCode("0211", "99DCM4CHEE", "Unrecognized Operation");
        public static final EventTypeCode x0212 =
                new EventTypeCode("0212", "99DCM4CHEE", "Mistyped Argument");
        public static final EventTypeCode A700 =
                new EventTypeCode("A700", "99DCM4CHEE", "Out Of Resources");
        public static final EventTypeCode A900 =
                new EventTypeCode("A900", "99DCM4CHEE", "Identifier Does Not Match SOP Class");
        public static final EventTypeCode A770 =
                new EventTypeCode("A770", "99DCM4CHEE", "Duplicate Rejection Note");
        public static final EventTypeCode A771 =
                new EventTypeCode("A771", "99DCM4CHEE", "Subsequent Occurrence of Rejected Object");
        public static final EventTypeCode A772 =
                new EventTypeCode("A772", "99DCM4CHEE", "Rejection Failed No Such Instance");
        public static final EventTypeCode A773 =
                new EventTypeCode("A773", "99DCM4CHEE", "Rejection Failed Class Instance Conflict");
        public static final EventTypeCode A774 =
                new EventTypeCode("A774", "99DCM4CHEE", "Rejection Failed Already Rejected");
        public static final EventTypeCode A775 =
                new EventTypeCode("A775", "99DCM4CHEE", "Rejection for Retention Policy Expired not allowed");
        public static final EventTypeCode A776 =
                new EventTypeCode("A776", "99DCM4CHEE", "Retention Policy of Study not yet expired");
        public static final EventTypeCode A777 =
                new EventTypeCode("A777", "99DCM4CHEE", "Patient ID missing in object");
        public static final EventTypeCode A778 =
                new EventTypeCode("A778", "99DCM4CHEE", "Conflicting Patient ID not accepted");
        public static final EventTypeCode C409 =
                new EventTypeCode("C409", "99DCM4CHEE", "Different Study Instance UID");

        public EventTypeCode(String code, String codeSystemName,
                String originalText) {
            super.csdCode = code;
            super.codeSystemName = codeSystemName;
            super.originalText = originalText;
        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public static final class AuditSourceTypeCode
            extends org.dcm4che3.audit.AuditSourceTypeCode {

        public static final AuditSourceTypeCode EndUserDisplayDevice =
                new AuditSourceTypeCode("1");
        public static final AuditSourceTypeCode DataAcquisitionDevice =
                new AuditSourceTypeCode("2");
        public static final AuditSourceTypeCode WebServerProcess =
                new AuditSourceTypeCode("3");
        public static final AuditSourceTypeCode ApplicationServerProcess =
                new AuditSourceTypeCode("4");
        public static final AuditSourceTypeCode DatabaseServerProcess =
                new AuditSourceTypeCode("5");
        public static final AuditSourceTypeCode SecurityServer =
                new AuditSourceTypeCode("6");
        public static final AuditSourceTypeCode NetworkComponent =
                new AuditSourceTypeCode("7");
        public static final AuditSourceTypeCode OperatingSoftware =
                new AuditSourceTypeCode("8");
        public static final AuditSourceTypeCode Other =
                new AuditSourceTypeCode("9");

        public AuditSourceTypeCode(String csdCode) {
            super.csdCode = csdCode;
        }

//        public AuditSourceTypeCode(String code, String codeSystemName,
//                String originalText) {
//            super.code = code;
//            super.codeSystemName = codeSystemName;
//            super.originalText = originalText;
//        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public static final class RoleIDCode extends org.dcm4che3.audit.RoleIDCode {

        public static final RoleIDCode Application = 
                new RoleIDCode("110150","DCM","Application");
        public static final RoleIDCode ApplicationLauncher = 
                new RoleIDCode("110151","DCM","Application Launcher");
        public static final RoleIDCode Destination = 
                new RoleIDCode("110152","DCM","Destination Role ID");
        public static final RoleIDCode Source = 
                new RoleIDCode("110153","DCM","Source Role ID");
        public static final RoleIDCode DestinationMedia = 
                new RoleIDCode("110154","DCM","Destination Media");
        public static final RoleIDCode SourceMedia = 
                new RoleIDCode("110155","DCM","Source Media");

        public RoleIDCode(String code, String codeSystemName,
                String originalText) {
            super.csdCode = code;
            super.codeSystemName = codeSystemName;
            super.originalText = originalText;
        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public static final class MediaType extends org.dcm4che3.audit.MediaType {

        public static final MediaType USBDiskEmulation =
                new MediaType("110030", "DCM", "USB Disk Emulation");
        public static final MediaType Email =
                new MediaType("110031", "DCM", "Email");
        public static final MediaType CD =
                new MediaType("110032", "DCM", "CD");
        public static final MediaType DVD =
                new MediaType("110033", "DCM", "DVD");
        public static final MediaType CompactFlash =
                new MediaType("110034", "DCM", "Compact Flash");
        public static final MediaType MultiMediaCard =
                new MediaType("110035", "DCM", "Multi-media Card");
        public static final MediaType SecureDigitalCard =
                new MediaType("110036", "DCM", "Secure Digital Card");
        public static final MediaType URI =
                new MediaType("110037", "DCM", "URI");
        public static final MediaType Film =
                new MediaType("110010", "DCM", "Film");
        public static final MediaType PaperDocument =
                new MediaType("110038", "DCM", "Paper Document");

        public MediaType(String code, String codeSystemName,
                String originalText) {
            super.csdCode = code;
            super.codeSystemName = codeSystemName;
            super.originalText = originalText;
        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public static final class NetworkAccessPointTypeCode {
        public static final String MachineName = "1";
        public static final String IPAddress = "2";
        public static final String TelephoneNumber = "3";
        public static final String EmailAddress = "4";
        public static final String URI = "5";
    }

    public static final class ParticipantObjectTypeCode {
        public static final String Person = "1";
        public static final String SystemObject = "2";
        public static final String Organization = "3";
        public static final String Other = "4";
    }

    public static final class UserTypeCode {
        public static final String Person = "1";
        public static final String Application = "2";
    }

    public static final class ParticipantObjectTypeCodeRole {
        public static final String Patient = "1";
        public static final String Location = "2";
        public static final String Report = "3";
        public static final String Resource = "4";
        public static final String MasterFile = "5";
        public static final String User = "6";
        public static final String List = "7";
        public static final String Doctor = "8";
        public static final String Subscriber = "9";
        public static final String Guarantor = "10";
        public static final String SecurityUserEntity = "11";
        public static final String SecurityUserGroup = "12";
        public static final String SecurityResource = "13";
        public static final String SecurityGranualarityDefinition = "14";
        public static final String Provider = "15";
        public static final String ReportDestination = "16";
        public static final String ReportLibrary = "17";
        public static final String Schedule = "18";
        public static final String Customer = "19";
        public static final String Job = "20";
        public static final String JobStream = "21";
        public static final String Table = "22";
        public static final String RoutingCriteria = "23";
        public static final String Query = "24";
    }

    public static final class ParticipantObjectDataLifeCycle {
        public static final String OriginationCreation = "1";
        public static final String ImportCopyFromOriginal  = "2";
        public static final String Amendment = "3";
        public static final String Verification = "4";
        public static final String Translation = "5";
        public static final String AccessUse = "6";
        public static final String DeIdentification = "7";
        public static final String AggregationSummarizationDerivation = "8";
        public static final String Report = "9";
        public static final String ExportCopyToTarget = "10";
        public static final String Disclosure = "11";
        public static final String ReceiptOfDisclosure = "12";
        public static final String Archiving = "13";
        public static final String LogicalDeletion = "14";
        public static final String PermanentErasurePhysicalDestruction  = "15";
    }

    public static final class ParticipantObjectIDTypeCode
            extends org.dcm4che3.audit.ParticipantObjectIDTypeCode {

        public static final ParticipantObjectIDTypeCode MedicalRecordNumber = 
                new ParticipantObjectIDTypeCode("1");
        public static final ParticipantObjectIDTypeCode PatientNumber =
                new ParticipantObjectIDTypeCode("2", "RFC-3881","Patient Number");
        public static final ParticipantObjectIDTypeCode EncounterNumber =
                new ParticipantObjectIDTypeCode("3");
        public static final ParticipantObjectIDTypeCode EnrolleeNumber =
                new ParticipantObjectIDTypeCode("4");
        public static final ParticipantObjectIDTypeCode SocialSecurityNumber = 
                new ParticipantObjectIDTypeCode("5");
        public static final ParticipantObjectIDTypeCode AccountNumber =
                new ParticipantObjectIDTypeCode("6");
        public static final ParticipantObjectIDTypeCode GuarantorNumber =
                new ParticipantObjectIDTypeCode("7");
        public static final ParticipantObjectIDTypeCode ReportName =
                new ParticipantObjectIDTypeCode("8");    
        public static final ParticipantObjectIDTypeCode ReportNumber =
                new ParticipantObjectIDTypeCode("9", "RFC-3881","Report Number");
        public static final ParticipantObjectIDTypeCode SearchCriteria =
                new ParticipantObjectIDTypeCode("10");
        public static final ParticipantObjectIDTypeCode UserIdentifier =
                new ParticipantObjectIDTypeCode("11");
        public static final ParticipantObjectIDTypeCode URI =
                new ParticipantObjectIDTypeCode("12", "RFC-3881", "URI");
        public static final ParticipantObjectIDTypeCode StudyInstanceUID = 
                new ParticipantObjectIDTypeCode("110180","DCM","Study Instance UID");
        public static final ParticipantObjectIDTypeCode SOPClassUID = 
                new ParticipantObjectIDTypeCode("110181","DCM","SOP Class UID");
        public static final ParticipantObjectIDTypeCode NodeID = 
                new ParticipantObjectIDTypeCode("110182","DCM","Node ID");
        public static final ParticipantObjectIDTypeCode ITI_PIXQuery = 
                new ParticipantObjectIDTypeCode("ITI-9","IHE Transactions","PIX Query");
        public static final ParticipantObjectIDTypeCode QIDO_QUERY =
                new ParticipantObjectIDTypeCode("QIDO","99DCM4CHEE","QIDO_Query");
        public static final ParticipantObjectIDTypeCode TASK =
                new ParticipantObjectIDTypeCode("TASK","99DCM4CHEE","Archive Task");
        public static final ParticipantObjectIDTypeCode TASKS =
                new ParticipantObjectIDTypeCode("TASKS","99DCM4CHEE","Archive Tasks");
        public static final ParticipantObjectIDTypeCode IHE_XDS_METADATA =
                new ParticipantObjectIDTypeCode("urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd",
                        "IHE XDS Metadata", "submission set classificationNode");
        public static final ParticipantObjectIDTypeCode DeviceName =
                new ParticipantObjectIDTypeCode("113877","DCM","Device Name");

        public ParticipantObjectIDTypeCode(String code) {
            super.csdCode = code;
        }

        public ParticipantObjectIDTypeCode(String code, String codeSystemName,
                String originalText) {
            super.csdCode = code;
            super.codeSystemName = codeSystemName;
            super.originalText = originalText;
        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public static final class UserIDTypeCode
            extends org.dcm4che3.audit.UserIDTypeCode {
        public static final UserIDTypeCode StationAETitle =
                new UserIDTypeCode("110119","DCM","Station AE Title");
        public static final UserIDTypeCode NodeID =
                new UserIDTypeCode("110182","DCM","Node ID");
        public static final UserIDTypeCode LocalUserID =
                new UserIDTypeCode("Cp1640-1","DCM","Local User ID");
        public static final UserIDTypeCode LocalGroupID =
                new UserIDTypeCode("Cp1640-2","DCM","Local Group ID");
        public static final UserIDTypeCode ApplicationFacility =
                new UserIDTypeCode("HL7APP","99DCM4CHEE","Application and Facility");
        public static final UserIDTypeCode DeviceName =
                new UserIDTypeCode("113877","DCM","Device Name");
        public static final UserIDTypeCode URI =
                new UserIDTypeCode("12", "RFC-3881", "URI");
        public static final UserIDTypeCode PersonID =
                new UserIDTypeCode("113871","DCM","Person ID");

        public UserIDTypeCode(String code) {
            super.csdCode = code;
        }

        public UserIDTypeCode(String code, String codeSystemName,
                                           String originalText) {
            super.csdCode = code;
            super.codeSystemName = codeSystemName;
            super.originalText = originalText;
        }

        @Override
        public void setCsdCode(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setOriginalText(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystem(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCodeSystemName(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public static EventIdentification toEventIdentification(EventIdentificationBuilder eventIdentificationBuilder) {
        EventIdentification ei = new EventIdentification();
        ei.setEventID(eventIdentificationBuilder.eventID);
        ei.setEventActionCode(eventIdentificationBuilder.eventActionCode);
        ei.setEventDateTime(eventIdentificationBuilder.eventDateTime);
        ei.setEventOutcomeIndicator(eventIdentificationBuilder.outcome);
        ei.setEventOutcomeDescription(eventIdentificationBuilder.outcomeDesc);
        for (org.dcm4che3.audit.EventTypeCode type : eventIdentificationBuilder.eventTypeCode)
            ei.getEventTypeCode().add(type);
        return ei;
    }

    private static ActiveParticipant toActiveParticipant(ActiveParticipantBuilder activeParticipantBuilder) {
        ActiveParticipant ap = new ActiveParticipant();
        ap.setUserID(activeParticipantBuilder.userID);
        ap.setUserIDTypeCode(activeParticipantBuilder.userIDTypeCode);
        ap.setUserTypeCode(activeParticipantBuilder.userTypeCode);
        ap.setAlternativeUserID(activeParticipantBuilder.altUserID);
        ap.setUserName(activeParticipantBuilder.userName);
        ap.setUserIsRequestor(activeParticipantBuilder.requester);
        ap.setNetworkAccessPointID(activeParticipantBuilder.napID);
        ap.setNetworkAccessPointTypeCode(activeParticipantBuilder.napTypeCode);
        ap.setMediaType(activeParticipantBuilder.mediaType);
        for (RoleIDCode roleID : activeParticipantBuilder.roleIDCode)
            ap.getRoleIDCode().add(roleID);
        return ap;
    }

   public static AuditSourceIdentification createAuditSourceIdentification(
            String siteID, String sourceID, AuditSourceTypeCode... types) {
        AuditSourceIdentification asi = new AuditSourceIdentification();
        asi.setAuditEnterpriseSiteID(siteID);
        asi.setAuditSourceID(sourceID);
        for (AuditSourceTypeCode type : types)
            asi.getAuditSourceTypeCode().add(type);
        return asi;
   }

    private static ParticipantObjectDescription toParticipantObjectDescription(ParticipantObjectDescriptionBuilder poDesc) {
        ParticipantObjectDescription pod = new ParticipantObjectDescription();
        for (String acc : poDesc.acc)
            pod.getAccession().add(AuditMessages.createAccession(acc));
        for (String mpps : poDesc.mpps)
            pod.getMPPS().add(AuditMessages.createMPPS(mpps));
        for (SOPClass sopC : poDesc.sopC)
            pod.getSOPClass().add(sopC);
        pod.setEncrypted(poDesc.encrypted);
        pod.setAnonymized(poDesc.anonymized);
        if (poDesc.pocsStudyUIDs.length > 1)
            pod.setParticipantObjectContainsStudy(
                    AuditMessages.createParticipantObjectContainsStudy(
                            AuditMessages.createStudyIDs(poDesc.pocsStudyUIDs)));
        return pod;
    }

    private static ParticipantObjectIdentification toParticipantObjectIdentification(
            ParticipantObjectIdentificationBuilder participantObjectIdentificationBuilder) {
        ParticipantObjectIdentification poi = new ParticipantObjectIdentification();
        poi.setParticipantObjectID(participantObjectIdentificationBuilder.id);
        poi.setParticipantObjectIDTypeCode(participantObjectIdentificationBuilder.idType);
        poi.setParticipantObjectName(participantObjectIdentificationBuilder.name);
        poi.setParticipantObjectQuery(participantObjectIdentificationBuilder.query);
        poi.setParticipantObjectTypeCode(participantObjectIdentificationBuilder.type);
        poi.setParticipantObjectTypeCodeRole(participantObjectIdentificationBuilder.role);
        poi.setParticipantObjectDataLifeCycle(participantObjectIdentificationBuilder.lifeCycle);
        poi.setParticipantObjectSensitivity(participantObjectIdentificationBuilder.sensitivity);
        if (participantObjectIdentificationBuilder.desc != null)
            poi.setParticipantObjectDescription(toParticipantObjectDescription(participantObjectIdentificationBuilder.desc));
        for (ParticipantObjectDetail participantObjectDetail : participantObjectIdentificationBuilder.detail)
                poi.getParticipantObjectDetail().add(participantObjectDetail);
        return poi;
    }

    public static ParticipantObjectDetail createParticipantObjectDetail(
            String type, String value) {
        if (value == null)
            return null;

        ParticipantObjectDetail detail = new ParticipantObjectDetail();
        detail.setType(type);
        detail.setValue(value.getBytes());
        return detail;
    }

    private static MPPS createMPPS(String uid) {
        MPPS mpps = new MPPS();
        mpps.setUID(uid);
        return mpps;
    }

    public static SOPClass createSOPClass(HashSet<String> instances, String uid, Integer numI) {
        SOPClass sopClass = new SOPClass();
        sopClass.setUID(uid);
        sopClass.setNumberOfInstances(numI);
        if (null != instances)
            for (String i : instances)
                sopClass.getInstance().add(createInstance(i));
        return sopClass;
    }

    private static Instance createInstance(String uid) {
        Instance inst = new Instance();
        inst.setUID(uid);
        return inst;
    }

    private static ParticipantObjectContainsStudy
            createParticipantObjectContainsStudy(org.dcm4che3.audit.StudyIDs... studyIDs) {
        ParticipantObjectContainsStudy study = new ParticipantObjectContainsStudy();
        for (org.dcm4che3.audit.StudyIDs studyID : studyIDs)
            study.getStudyIDs().add(studyID);
        return study;
    }

    private static Accession createAccession(String accessionNumber) {
        Accession accession = new Accession();
        accession.setNumber(accessionNumber);
        return accession;
    }

    private static StudyIDs[] createStudyIDs(String... studyUIDs) {
        Set<StudyIDs> set = new HashSet<>();
        for (String s : studyUIDs) {
            StudyIDs sID = new StudyIDs();
            sID.setUID(s);
            set.add(sID);
        }
        return set.toArray(new StudyIDs[set.size()]);
    }

    public static String alternativeUserIDForAETitle(String... aets) {
        if (aets.length == 0)
            return null;

        StringBuilder b = new StringBuilder();
        b.append("AETITLES=").append(aets[0]);
        for (int i = 1; i < aets.length; i++)
            b.append(';').append(aets[i]);
        return b.toString();
    }

    public static void toXML(AuditMessage message, OutputStream os)
            throws IOException {
        toXML(message, os, false, "UTF-8", SCHEMA_URI);
    }

    public static void toXML(AuditMessage message, OutputStream os,
            boolean format) throws IOException {
        toXML(message, os, format, "UTF-8", SCHEMA_URI);
    }

    public static void toXML(AuditMessage message, OutputStream os,
            boolean format, String encoding) throws IOException {
        toXML(message, os, format, encoding, SCHEMA_URI);
    }

    public static void toXML(AuditMessage message, OutputStream os,
            boolean format, String encoding, String schemaURI)
            throws IOException {
        try {
            Marshaller m = jc().createMarshaller();
            if (format)
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            if (schemaURI != null)
                m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION,
                        schemaURI);
            if (encoding != null)
                m.setProperty(Marshaller.JAXB_ENCODING, encoding);
            m.marshal(of.createAuditMessage(message), os );
        } catch( JAXBException jbe ){
            if (jbe.getLinkedException() instanceof IOException)
                throw (IOException) jbe.getLinkedException();
            throw new IllegalStateException(jbe);
        }
    }

    public static String toXML(AuditMessage message)
            throws IOException {
        return toXML(message, false, "UTF-8", SCHEMA_URI);
    }

    public static String toXML(AuditMessage message,
            boolean format) throws IOException {
        return toXML(message, format, "UTF-8", SCHEMA_URI);
    }

    public static String toXML(AuditMessage message,
            boolean format, String encoding) throws IOException {
        return toXML(message, format, encoding, SCHEMA_URI);
    }

    public static String toXML(AuditMessage message, 
            boolean format, String encoding, String schemaURL)
            throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(256);
        toXML(message, os, format, encoding, schemaURL);
        return os.toString(encoding);
    }

    public static AuditMessage fromXML(InputStream is)
            throws JAXBException {
        Unmarshaller u = jc().createUnmarshaller();
        @SuppressWarnings("unchecked")
        JAXBElement<AuditMessage> je =
                (JAXBElement<AuditMessage>) u.unmarshal(is);
        return je.getValue();
    }

    public static AuditMessage fromXML(Reader is)
            throws JAXBException {
        Unmarshaller u = jc().createUnmarshaller();
        @SuppressWarnings("unchecked")
        JAXBElement<AuditMessage> je =
                (JAXBElement<AuditMessage>) u.unmarshal(is);
        return je.getValue();
    }

    public static AuditMessage createMessage(
            EventIdentificationBuilder eventIdentificationBuilder, ActiveParticipantBuilder[] activeParticipantBuilders,
            ParticipantObjectIdentificationBuilder... participantObjectIdentificationBuilders) {
        AuditMessage msg = new AuditMessage();
        msg.setEventIdentification(toEventIdentification(eventIdentificationBuilder));
        for (ActiveParticipantBuilder activeParticipantBuilder : activeParticipantBuilders)
            if (activeParticipantBuilder != null)
                msg.getActiveParticipant().add(toActiveParticipant(activeParticipantBuilder));
        for (ParticipantObjectIdentificationBuilder participantObjectIdentificationBuilder : participantObjectIdentificationBuilders)
            msg.getParticipantObjectIdentification().add(toParticipantObjectIdentification(participantObjectIdentificationBuilder));
        return msg;
    }

    public static AuditMessages.UserIDTypeCode userIDTypeCode(String userID) {
        return AuditMessages.isIP(userID)
                ? AuditMessages.UserIDTypeCode.NodeID
                : AuditMessages.UserIDTypeCode.PersonID;
    }
}
