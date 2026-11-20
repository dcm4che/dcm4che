package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.Assert.*;


public class ICCProfileTest {

    private static final byte[] ICC_PROFILE_BYTES;

    static {
        try (InputStream is = ICCProfile.class.getResourceAsStream("sRGB.icc")) {
            if (is == null) {
                throw new RuntimeException("sRGB.icc not found");
            }
            byte[] buf = new byte[8192];
            int n;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            ICC_PROFILE_BYTES = baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testIsPresentIn() {
        Attributes attrs = new Attributes();
        assertFalse(ICCProfile.isPresentIn(attrs));

        attrs.setBytes(Tag.ICCProfile, VR.OB, ICC_PROFILE_BYTES);
        assertTrue(ICCProfile.isPresentIn(attrs));

        attrs = new Attributes();
        attrs.newSequence(Tag.OpticalPathSequence, 1).add(new Attributes());
        assertTrue(ICCProfile.isPresentIn(attrs));
    }

    @Test
    public void testColorSpaceFactoryOfTopLevel() {
        Attributes attrs = new Attributes();
        attrs.setBytes(Tag.ICCProfile, VR.OB, ICC_PROFILE_BYTES);

        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);
        Optional<ColorSpace> cs = factory.getColorSpace(0);
        assertTrue(cs.isPresent());
        assertTrue(cs.get() instanceof ICC_ColorSpace);
    }

    @Test
    public void testColorSpaceFactoryOfOpticalPathSequenceSingleItem() {
        Attributes attrs = new Attributes();
        Sequence seq = attrs.newSequence(Tag.OpticalPathSequence, 1);
        Attributes item = new Attributes();
        item.setBytes(Tag.ICCProfile, VR.OB, ICC_PROFILE_BYTES);
        seq.add(item);

        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);
        Optional<ColorSpace> cs = factory.getColorSpace(0);
        assertTrue(cs.isPresent());
    }

    @Test
    public void testColorSpaceFactoryOfOpticalPathSequenceMultipleItems() {
        Attributes attrs = new Attributes();
        Sequence seq = attrs.newSequence(Tag.OpticalPathSequence, 2);
        
        Attributes item1 = new Attributes();
        item1.setString(Tag.OpticalPathIdentifier, VR.SH, "1");
        // No ICC Profile in item1
        seq.add(item1);

        Attributes item2 = new Attributes();
        item2.setString(Tag.OpticalPathIdentifier, VR.SH, "2");
        item2.setBytes(Tag.ICCProfile, VR.OB, ICC_PROFILE_BYTES);
        seq.add(item2);

        // Frame 0 refers to path "2"
        attrs.newSequence(Tag.SharedFunctionalGroupsSequence, 1).add(new Attributes());
        Attributes perFrame0 = new Attributes();
        attrs.newSequence(Tag.PerFrameFunctionalGroupsSequence, 1).add(perFrame0);
        Attributes opId0 = new Attributes();
        perFrame0.newSequence(Tag.OpticalPathIdentificationSequence, 1).add(opId0);
        opId0.setString(Tag.OpticalPathIdentifier, VR.SH, "2");

        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);
        Optional<ColorSpace> cs = factory.getColorSpace(0);
        assertTrue("ColorSpace should be present for frame 0", cs.isPresent());

        // Frame 1 refers to path "1"
        Attributes perFrame1 = new Attributes();
        attrs.getSequence(Tag.PerFrameFunctionalGroupsSequence).add(perFrame1);
        Attributes opId1 = new Attributes();
        perFrame1.newSequence(Tag.OpticalPathIdentificationSequence, 1).add(opId1);
        opId1.setString(Tag.OpticalPathIdentifier, VR.SH, "1");
        
        cs = factory.getColorSpace(1);
        assertFalse("ColorSpace should be empty for frame 1 (no ICC Profile in path 1)", cs.isPresent());
    }

    @Test
    public void testOptionNone() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        // Default is sRGB, so Option.none should return the same image if it's already sRGB
        BufferedImage result = ICCProfile.Option.none.adjust(bi);
        assertSame(bi, result);
    }

    @Test
    public void testOptionSrgb() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage result = ICCProfile.Option.srgb.adjust(bi);
        // result should be a new image since Option.srgb converts/replaces color model
        assertNotSame(bi, result);
        assertTrue(result.getColorModel().getColorSpace() instanceof ICC_ColorSpace);
    }

    @Test
    public void testOptionYesNo() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        
        BufferedImage resultNo = ICCProfile.Option.no.adjust(bi);
        assertSame(bi, resultNo); // Already sRGB

        BufferedImage resultYes = ICCProfile.Option.yes.adjust(bi);
        // Yes should return new image even if already sRGB, because it uses srgb.colorModel (ICC based)
        assertNotSame(bi, resultYes);
    }
}
