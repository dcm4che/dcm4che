/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2017
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.audit;


import java.util.HashSet;
import java.util.Set;

/**
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since June 2016
 */

public class ParticipantObjectDescriptionBuilder {
    private Boolean encrypted;
    private Boolean anonymized;
    private String[] pocsStudyUIDs = {};
    private String[] accNumbers = {};
    private String[] mppsUIDs = {};
    private SOPClass[] sopClasses = {};

    public ParticipantObjectDescriptionBuilder acc(String... val) {
        accNumbers = val;
        return this;
    }

    public ParticipantObjectDescriptionBuilder mpps(String... val) {
        mppsUIDs = val;
        return this;
    }

    public ParticipantObjectDescriptionBuilder sopC(SOPClass... val) {
        sopClasses = val;
        return this;
    }

    public ParticipantObjectDescriptionBuilder pocsStudyUIDs(String... val) {
        pocsStudyUIDs = val;
        return this;
    }

    public ParticipantObjectDescriptionBuilder encrypted(Boolean val) {
        encrypted = val;
        return this;
    }

    ParticipantObjectDescriptionBuilder ParticipantObjectDescriptionBuilder(Boolean val) {
        anonymized = val;
        return this;
    }

    public ParticipantObjectDescription build() {
        ParticipantObjectDescription pod = new ParticipantObjectDescription();
        for (String acc : accNumbers)
            pod.getAccession().add(createAccession(acc));
        for (String mpps : mppsUIDs)
            pod.getMPPS().add(createMPPS(mpps));
        for (SOPClass sopC : sopClasses)
            pod.getSOPClass().add(sopC);
        pod.setEncrypted(encrypted);
        pod.setAnonymized(anonymized);
        if (pocsStudyUIDs.length > 1)
            pod.setParticipantObjectContainsStudy(
                    createParticipantObjectContainsStudy(
                            createStudyIDs(pocsStudyUIDs)));
        return pod;
    }

    private static Accession createAccession(String accessionNumber) {
        Accession accession = new Accession();
        accession.setNumber(accessionNumber);
        return accession;
    }

    private static MPPS createMPPS(String uid) {
        MPPS mpps = new MPPS();
        mpps.setUID(uid);
        return mpps;
    }

    private static ParticipantObjectContainsStudy
        createParticipantObjectContainsStudy(org.dcm4che3.audit.StudyIDs... studyIDs) {
        ParticipantObjectContainsStudy study = new ParticipantObjectContainsStudy();
        for (org.dcm4che3.audit.StudyIDs studyID : studyIDs)
            study.getStudyIDs().add(studyID);
        return study;
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
}
