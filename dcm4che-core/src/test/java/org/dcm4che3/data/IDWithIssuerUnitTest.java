package org.dcm4che3.data;

import org.junit.Test;

import java.util.Set;

import static org.dcm4che3.data.Tag.*;
import static org.junit.Assert.assertEquals;

public class IDWithIssuerUnitTest {


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
        assertEquals(all.size(), 1);
        assertEquals(all.iterator().next(), IDWithIssuer.pidOf(otherPatientId));

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