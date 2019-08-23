package org.dcm4che3.data;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.dcm4che3.data.Tag.*;
import static org.junit.Assert.assertEquals;

public class OtherPatientIdCleanerUnitTest {


    private static final String ID = "ID";
    private static final String NS = "NS";
    private static final String UID = "UID";
    private static final String ISO = "ISO";

    @Test
    public void duplicates_should_be_removed() {
        Attributes rootWithMainId = createIdWithNS(NS);

        rootWithMainId.newSequence(OtherPatientIDsSequence, 0);
        Sequence other = rootWithMainId.getSequence(OtherPatientIDsSequence);

        Attributes otherPatientId = otherPatientIds(NS);
        other.add(otherPatientId);

        IDWithIssuer root = IDWithIssuer.pidOf(rootWithMainId);
        Set<IDWithIssuer> all = new HashSet<>();
        all.add(IDWithIssuer.pidOf(otherPatientId));
        all.add(root);

        assertEquals(all.size(), 2);

        OtherPatientIdCleaner cleaner = new OtherPatientIdCleaner(all, root);
        all = cleaner.filterOutDuplicates();

        assertEquals(all.size(), 1);
        assertEquals(all.iterator().next(), IDWithIssuer.pidOf(otherPatientId));

    }
    @Test
    public void duplicates_should_be_removed_by_PidsOf_call() {
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

        IDWithIssuer root = IDWithIssuer.pidOf(rootWithMainId);
        Set<IDWithIssuer> all = new HashSet<>();
        all.add(IDWithIssuer.pidOf(otherPatientId));
        all.add(root);

        OtherPatientIdCleaner cleaner = new OtherPatientIdCleaner(all, root);
        all = cleaner.filterOutDuplicates();
        assertEquals("Same pid but for different issuer should not be removed!",all.size(), 2);
    }

    @Test
    public void no_main_id_returns_all() {
        Attributes rootWithMainId = createIdWithNS(NS);

        rootWithMainId.newSequence(OtherPatientIDsSequence, 0);
        Sequence other = rootWithMainId.getSequence(OtherPatientIDsSequence);

        Attributes otherPatientId = otherPatientIds("other_ns");
        other.add(otherPatientId);

        Set<IDWithIssuer> all = new HashSet<>();
        all.add(IDWithIssuer.pidOf(otherPatientId));
        all.add(IDWithIssuer.pidOf(rootWithMainId));

        assertEquals(all.size(), 2);
        OtherPatientIdCleaner cleaner = new OtherPatientIdCleaner(all, null);
        all = cleaner.filterOutDuplicates();

        assertEquals("Same pid but for different issuer should not be removed!",all.size(), 2);
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