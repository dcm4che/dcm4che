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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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


import org.dcm4che3.audit.AuditMessage;
import org.dcm4che3.audit.ParticipantObjectDescription;
import org.dcm4che3.audit.AuditMessages;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class AuditMessageTest {

    @Test
    public void testDICOMInstancesTransferred() throws Exception {
        AuditMessage msg = new AuditMessage();
        msg.setEventIdentification(AuditMessages.createEventIdentification(
                AuditMessages.EventID.DICOMInstancesTransferred,
                AuditMessages.EventActionCode.Create,
                null,
                AuditMessages.EventOutcomeIndicator.Success,
                null));
        msg.getActiveParticipant().add(
                AuditMessages.createActiveParticipant(
                    "123",
                    AuditMessages.alternativeUserIDForAETitle("AEFOO"),
                    null,
                    false, 
                    "192.168.1.2",
                    AuditMessages.NetworkAccessPointTypeCode.IPAddress,
                    null,
                    AuditMessages.RoleIDCode.Source));
        msg.getActiveParticipant().add(
                AuditMessages.createActiveParticipant(
                    "67562",
                    AuditMessages.alternativeUserIDForAETitle("AEPACS"),
                    null,
                    false, 
                    "192.168.1.5",
                    AuditMessages.NetworkAccessPointTypeCode.IPAddress,
                    null,
                    AuditMessages.RoleIDCode.Destination));
        msg.getActiveParticipant().add(
                AuditMessages.createActiveParticipant(
                    "smitty@readingroom.hospital.org",
                    "smith@nema",
                    "Dr. Smith",
                    true, 
                    "192.168.1.2",
                    AuditMessages.NetworkAccessPointTypeCode.IPAddress,
                    null,
                    AuditMessages.RoleIDCode.Source));
        msg.getAuditSourceIdentification().add(
                AuditMessages.createAuditSourceIdentification(
                    "Hospital",
                    "ReadingRoom",
                    AuditMessages.AuditSourceTypeCode.EndUserDisplayDevice));
        ParticipantObjectDescription pod = new ParticipantObjectDescription();
        pod.getMPPS().add(AuditMessages.createMPPS("1.2.840.10008.1.2.3.4.5"));
        pod.getAccession().add(AuditMessages.createAccession("12341234"));
        pod.getSOPClass().add(AuditMessages.createSOPClass(
                "1.2.840.10008.5.1.4.1.1.2", 1500));
        pod.getSOPClass().add(AuditMessages.createSOPClass(
                "1.2.840.10008.5.1.4.1.1.11.1", 3));
        msg.getParticipantObjectIdentification().add(
                AuditMessages.createParticipantObjectIdentification(
                        "1.2.840.10008.2.3.4.5.6.7.78.8",
                        AuditMessages.ParticipantObjectIDTypeCode.StudyInstanceUID,
                        null,
                        null,
                        AuditMessages.ParticipantObjectTypeCode.SystemObject,
                        AuditMessages.ParticipantObjectTypeCodeRole.Resource,
                        AuditMessages.ParticipantObjectDataLifeCycle.OriginationCreation,
                        null,
                        pod));
        msg.getParticipantObjectIdentification().add(
                AuditMessages.createParticipantObjectIdentification(
                        "ptid12345",
                        AuditMessages.ParticipantObjectIDTypeCode.PatientNumber,
                        "John Doe",
                        null,
                        AuditMessages.ParticipantObjectTypeCode.Person,
                        AuditMessages.ParticipantObjectTypeCodeRole.Patient,
                        null,
                        null,
                        null));
        AuditMessages.toXML(msg, System.out, true);
    }

}
