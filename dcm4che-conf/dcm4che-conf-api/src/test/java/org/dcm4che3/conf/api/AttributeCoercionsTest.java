package org.dcm4che3.conf.api;

import static org.junit.Assert.*;
import static org.dcm4che3.net.Dimse.*;
import static org.dcm4che3.net.TransferCapability.Role.*;

import org.dcm4che3.data.UID;
import org.dcm4che3.conf.api.AttributeCoercion;
import org.dcm4che3.conf.api.AttributeCoercions;
import org.junit.Test;

public class AttributeCoercionsTest {

    private static final String URI = "file:///a.xsl";

    @Test
    public void testFindMatching() {
        AttributeCoercion ctFromAET1 = new AttributeCoercion(
                "Coerce CT from AET1",
                new String[]{UID.CTImageStorage}, 
                C_STORE_RQ, SCP,
                new String[]{"AET1"},
                URI);
        AttributeCoercion anyFromAET2 = new AttributeCoercion(
                "Coerce any from AET2",
                null, 
                C_STORE_RQ,
                SCP,
                new String[]{"AET2"},
                URI);
        AttributeCoercion mrFromAny = new AttributeCoercion(
                "Coerce MR from any",
                new String[]{UID.MRImageStorage},
                C_STORE_RQ, SCP,
                null,
                URI);
        AttributeCoercion any = new AttributeCoercion(
                "Coerce any from any",
                null,
                C_STORE_RQ,
                SCP,
                null,
                URI);
        AttributeCoercions acs = new AttributeCoercions();
        acs.add(any);
        acs.add(ctFromAET1);
        acs.add(anyFromAET2);
        acs.add(mrFromAny);
        assertSame(ctFromAET1, acs.findAttributeCoercion(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET1"));
        assertSame(anyFromAET2, acs.findAttributeCoercion(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET2"));
        assertSame(mrFromAny, acs.findAttributeCoercion(
                UID.MRImageStorage, C_STORE_RQ, SCP, "AET1"));
        assertSame(anyFromAET2, acs.findAttributeCoercion(
                UID.MRImageStorage, C_STORE_RQ, SCP, "AET2"));
        assertSame(any, acs.findAttributeCoercion(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET3"));
    }


}
