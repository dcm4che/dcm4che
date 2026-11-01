package org.dcm4che3.image;

import static org.junit.Assert.assertFalse;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

public class ICCProfileTest {

    @Test
    public void testInvalidICCProfileIsIgnored() {
        Attributes attrs = new Attributes(1);
        attrs.setBytes(Tag.ICCProfile, VR.OB, new byte[] { 1, 2, 3, 4 });
        assertFalse(ICCProfile.colorSpaceFactoryOf(attrs).getColorSpace(0).isPresent());
    }
}
