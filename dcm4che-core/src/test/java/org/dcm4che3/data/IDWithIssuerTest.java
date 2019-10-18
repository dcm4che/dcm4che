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
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che3.data;

import org.junit.Test;

import java.util.Set;

import static org.dcm4che3.data.Tag.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Mark Dechamps <mark.dechamps@gmail.com>
 */
public class IDWithIssuerTest {


    private static final String ID = "ID";
    private static final String NS = "NS";
    private static final String UID = "UID";
    private static final String ISO = "ISO";

    @Test
    public void rootId_is_not_added_when_matching_id_is_already_there() {
        Attributes rootWithMainId = createIdWithNS(NS);

        rootWithMainId.newSequence(OtherPatientIDsSequence, 0);
        Sequence other = rootWithMainId.getSequence(OtherPatientIDsSequence);

        Attributes otherPatientId = otherPatientIds(NS);
        other.add(otherPatientId);

        Set<IDWithIssuer> all = IDWithIssuer.pidsOf(rootWithMainId);
        assertEquals(1, all.size());
        assertEquals(IDWithIssuer.pidOf(otherPatientId), all.iterator().next());

    }

    @Test
    public void pidsOfTest_faking_duplicate_by_using_same_PID_but_different_issuer() {
        Attributes rootWithMainId = createIdWithNS(NS);
        rootWithMainId.newSequence(OtherPatientIDsSequence, 0);
        Sequence other = rootWithMainId.getSequence(OtherPatientIDsSequence);
        Attributes otherPatientId = otherPatientIds("other_ns");
        other.add(otherPatientId);

        Set<IDWithIssuer> all = IDWithIssuer.pidsOf(rootWithMainId);

        assertEquals("Same pid but for different issuer should not be removed!", all.size(), 2);
    }

    @Test
    public void no_main_id_returns_all() {
        Attributes rootWithMainId = createIdWithNS(NS);

        rootWithMainId.newSequence(OtherPatientIDsSequence, 0);
        Sequence other = rootWithMainId.getSequence(OtherPatientIDsSequence);

        Attributes otherPatientId = otherPatientIds("other_ns");
        other.add(otherPatientId);

        Set<IDWithIssuer> all = IDWithIssuer.pidsOf(rootWithMainId);

        assertEquals("Same pid but for different issuer should not be removed!", all.size(), 2);
    }


    private Attributes otherPatientIds(String ns) {
        Attributes otherPatientId = createIdWithNS(ns);
        otherPatientId.newSequence(IssuerOfPatientIDQualifiersSequence, 0);
        addUniversalIdentifierTo(otherPatientId);
        return otherPatientId;
    }

    private void addUniversalIdentifierTo(Attributes idWithNS) {
        Sequence uid = idWithNS.getSequence(IssuerOfPatientIDQualifiersSequence);
        Attributes uidAttributes = new Attributes();
        uidAttributes.setString(UniversalEntityID, VR.LO, UID);
        uidAttributes.setString(UniversalEntityIDType, VR.LO, ISO);
        uid.add(uidAttributes);
    }

    private Attributes createIdWithNS(String ns) {
        Attributes attributes = new Attributes();
        attributes.setString(PatientID, VR.LO, ID);
        attributes.setString(IssuerOfPatientID, VR.LO, ns);
        return attributes;
    }
}
