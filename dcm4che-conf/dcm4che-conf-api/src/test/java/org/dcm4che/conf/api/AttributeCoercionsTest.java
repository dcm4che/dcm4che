package org.dcm4che.conf.api;

import static org.junit.Assert.*;
import static org.dcm4che.net.Dimse.*;
import static org.dcm4che.net.TransferCapability.Role.*;

import org.dcm4che.data.UID;
import org.junit.Test;

public class AttributeCoercionsTest {

    private static final String URI = "file:///a.xsl";

    @Test
    public void testFindMatching() {
        AttributeCoercion ctFromAET1 = new AttributeCoercion(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET1", URI);
        AttributeCoercion anyFromAET2 = new AttributeCoercion(
                null, C_STORE_RQ, SCP, "AET2", URI);
        AttributeCoercion mrFromAny = new AttributeCoercion(
                UID.MRImageStorage, C_STORE_RQ, SCP, null, URI);
        AttributeCoercion any = new AttributeCoercion(
                null, C_STORE_RQ, SCP, null, URI);
        AttributeCoercions acs = new AttributeCoercions();
        acs.add(any);
        acs.add(ctFromAET1);
        acs.add(anyFromAET2);
        acs.add(mrFromAny);
        assertSame(ctFromAET1, acs.findMatching(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET1"));
        assertSame(anyFromAET2, acs.findMatching(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET2"));
        assertSame(mrFromAny, acs.findMatching(
                UID.MRImageStorage, C_STORE_RQ, SCP, "AET1"));
        assertSame(anyFromAET2, acs.findMatching(
                UID.MRImageStorage, C_STORE_RQ, SCP, "AET2"));
        assertSame(any, acs.findMatching(
                UID.CTImageStorage, C_STORE_RQ, SCP, "AET3"));
    }


}
