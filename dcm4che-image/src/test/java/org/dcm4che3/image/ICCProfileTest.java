package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Test;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.Assert.*;

public class ICCProfileTest {

    private byte[] sRGBProfile;

    @Before
    public void setup() throws IOException {
        try (InputStream is = ICCProfile.class.getResourceAsStream("sRGB.icc")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while (true) {
                assertNotNull(is);
                if ((read = is.read(buffer)) == -1) break;
                baos.write(buffer, 0, read);
            }
            sRGBProfile = baos.toByteArray();
        }
    }

    @Test
    public void testIsPresentIn() {
        Attributes attrs = new Attributes();
        assertFalse(ICCProfile.isPresentIn(attrs));

        attrs.setBytes(Tag.ICCProfile, VR.OB, sRGBProfile);
        assertTrue(ICCProfile.isPresentIn(attrs));

        attrs = new Attributes();
        attrs.newSequence(Tag.OpticalPathSequence, 1).add(new Attributes());
        assertTrue(ICCProfile.isPresentIn(attrs));
    }

    @Test
    public void testColorSpaceFactoryOfDirect() {
        Attributes attrs = new Attributes();
        attrs.setBytes(Tag.ICCProfile, VR.OB, sRGBProfile);

        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);
        Optional<ColorSpace> cs = factory.getColorSpace(0);
        assertTrue(cs.isPresent());
        assertEquals(ColorSpace.TYPE_RGB, cs.get().getType());
    }

    @Test
    public void testColorSpaceFactoryOfOpticalPathSingle() {
        Attributes attrs = new Attributes();
        Sequence seq = attrs.newSequence(Tag.OpticalPathSequence, 1);
        Attributes item = new Attributes();
        item.setBytes(Tag.ICCProfile, VR.OB, sRGBProfile);
        seq.add(item);

        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);
        Optional<ColorSpace> cs = factory.getColorSpace(0);
        assertTrue(cs.isPresent());
        assertEquals(ColorSpace.TYPE_RGB, cs.get().getType());
    }

    @Test
    public void testColorSpaceFactoryOfOpticalPathMultiple() {
        Attributes attrs = new Attributes();
        Sequence seq = attrs.newSequence(Tag.OpticalPathSequence, 2);
        
        Attributes item1 = new Attributes();
        item1.setString(Tag.OpticalPathIdentifier, VR.SH, "1");
        // No ICC profile in item1
        seq.add(item1);

        Attributes item2 = new Attributes();
        item2.setString(Tag.OpticalPathIdentifier, VR.SH, "2");
        item2.setBytes(Tag.ICCProfile, VR.OB, sRGBProfile);
        seq.add(item2);

        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);

        // Functional groups
        Sequence sharedSeq = attrs.newSequence(Tag.SharedFunctionalGroupsSequence, 1);
        Attributes shared = new Attributes();
        sharedSeq.add(shared);
        
        Sequence perFrameSeq = attrs.newSequence(Tag.PerFrameFunctionalGroupsSequence, 2);
        Attributes perFrame1 = new Attributes();
        perFrameSeq.add(perFrame1);
        Attributes perFrame2 = new Attributes();
        perFrameSeq.add(perFrame2);

        // Frame 0: Path 1
        Sequence idSeq1 = perFrame1.newSequence(Tag.OpticalPathIdentificationSequence, 1);
        Attributes idItem1 = new Attributes();
        idItem1.setString(Tag.OpticalPathIdentifier, VR.SH, "1");
        idSeq1.add(idItem1);

        // Frame 1: Path 2
        Sequence idSeq2 = perFrame2.newSequence(Tag.OpticalPathIdentificationSequence, 1);
        Attributes idItem2 = new Attributes();
        idItem2.setString(Tag.OpticalPathIdentifier, VR.SH, "2");
        idSeq2.add(idItem2);

        // Test frame 0 (OpticalPath "1", no ICC)
        assertFalse(factory.getColorSpace(0).isPresent());

        // Test frame 1 (OpticalPath "2", has ICC)
        Optional<ColorSpace> cs1 = factory.getColorSpace(1);
        assertTrue(cs1.isPresent());
        assertEquals(ColorSpace.TYPE_RGB, cs1.get().getType());
        
        // Test frame with no functional group
        assertFalse(factory.getColorSpace(2).isPresent());
    }

    @Test
    public void testColorSpaceFactoryOfNone() {
        Attributes attrs = new Attributes();
        ICCProfile.ColorSpaceFactory factory = ICCProfile.colorSpaceFactoryOf(attrs);
        assertFalse(factory.getColorSpace(0).isPresent());
    }

    @Test
    public void testOptionAdjustRGB() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        // Option.none adjust should convert to sRGB if not already. TYPE_INT_RGB is usually already sRGB.
        BufferedImage result = ICCProfile.Option.none.adjust(bi);
        assertNotNull(result);
        assertEquals(ColorSpace.TYPE_RGB, result.getColorModel().getColorSpace().getType());
    }

    @Test
    public void testOptionAdjustPalette() {
        Attributes ds = new Attributes();
        ds.setInt(Tag.RedPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.GreenPaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setInt(Tag.BluePaletteColorLookupTableDescriptor, VR.US, 256, 0, 8);
        ds.setBytes(Tag.RedPaletteColorLookupTableData, VR.OW, new byte[256]);
        ds.setBytes(Tag.GreenPaletteColorLookupTableData, VR.OW, new byte[256]);
        ds.setBytes(Tag.BluePaletteColorLookupTableData, VR.OW, new byte[256]);
        
        PaletteColorModel pcm = new PaletteColorModel(8, DataBuffer.TYPE_BYTE, ColorSpace.getInstance(ColorSpace.CS_sRGB), ds);
        BufferedImage bi = new BufferedImage(pcm, pcm.createCompatibleWritableRaster(10, 10), false, null);
        
        BufferedImage result = ICCProfile.Option.none.adjust(bi);
        assertNotNull(result);
        // Should be converted to RGB because it was PaletteColorModel
        assertFalse(result.getColorModel() instanceof PaletteColorModel);
        assertEquals(3, result.getColorModel().getNumColorComponents());
    }

    @Test
    public void testOptions() {
        BufferedImage bi = new BufferedImage(10, 10, BufferedImage.TYPE_3BYTE_BGR);
        
        assertNotNull(ICCProfile.Option.none.adjust(bi));
        assertNotNull(ICCProfile.Option.no.adjust(bi));
        assertNotNull(ICCProfile.Option.yes.adjust(bi));
        assertNotNull(ICCProfile.Option.srgb.adjust(bi));
        assertNotNull(ICCProfile.Option.adobergb.adjust(bi));
        assertNotNull(ICCProfile.Option.rommrgb.adjust(bi));
    }
}
