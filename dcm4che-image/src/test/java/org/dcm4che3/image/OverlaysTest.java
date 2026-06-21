package org.dcm4che3.image;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


public class OverlaysTest {

    @Test
    public void testGetOverlayGroupOffsets() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.OverlayRows, VR.US, 100);
        attrs.setInt(Tag.OverlayRows | 0x00020000, VR.US, 100);
        attrs.setInt(Tag.OverlayRows | 0x00040000, VR.US, 100);

        int[] offsets = Overlays.getOverlayGroupOffsets(attrs, Tag.OverlayRows, 0xFFFF);
        assertArrayEquals(new int[]{0x00000000, 0x00020000, 0x00040000}, offsets);

        offsets = Overlays.getOverlayGroupOffsets(attrs, Tag.OverlayRows, 0x0005); // bit 0 and 2
        assertArrayEquals(new int[]{0x00000000, 0x00040000}, offsets);
    }

    @Test
    public void testGetEmbeddedOverlayGroupOffsets() {
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.BitsAllocated, VR.US, 16);
        attrs.setInt(Tag.BitsStored, VR.US, 12);

        // Overlay #1 at bit 12 (embedded)
        attrs.setInt(Tag.OverlayBitsAllocated, VR.US, 1);
        attrs.setInt(Tag.OverlayBitPosition, VR.US, 12);

        attrs.setInt(Tag.OverlayBitsAllocated, VR.US, 0); // Not 1
        attrs.setInt(Tag.OverlayBitPosition, VR.US, 12);

        attrs.setInt(Tag.OverlayBitsAllocated | 0x00020000, VR.US, 0); // Not 1
        attrs.setInt(Tag.OverlayBitPosition | 0x00020000, VR.US, 13);

        int[] offsets = Overlays.getEmbeddedOverlayGroupOffsets(attrs);
        assertArrayEquals(new int[]{0x00000000, 0x00020000}, offsets);
    }

    @Test
    public void testExtractFromPixelData() {
        int width = 8;
        int height = 1;
        short[] pixelData = new short[]{0x1000, 0x0000, 0x1000, 0x1000, 0x0000, 0x0000, 0x1000, 0x1000};
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT, width, height, width, 1, new int[]{0}, null);
        raster.setDataElements(0, 0, width, height, pixelData);

        byte[] ovlyData = new byte[1];
        Overlays.extractFromPixeldata(raster, 0x1000, ovlyData, 0, 8);
        
        // i=0: 1<<0 = 1
        // i=1: 0
        // i=2: 1<<2 = 4
        // i=3: 1<<3 = 8
        // i=4: 0
        // i=5: 0
        // i=6: 1<<6 = 64
        // i=7: 1<<7 = 128
        // Total: 1+4+8+64+128 = 205 (0xCD)
        assertEquals((byte)0xCD, ovlyData[0]);
    }

    @Test
    public void testApplyOverlay() {
        int width = 8;
        int height = 8;
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, 1, null);
        
        Attributes attrs = new Attributes();
        attrs.setInt(Tag.OverlayRows, VR.US, 2);
        attrs.setInt(Tag.OverlayColumns, VR.US, 2);
        attrs.setInt(Tag.OverlayOrigin, VR.SS, 1, 1);
        attrs.setBytes(Tag.OverlayData, VR.OB, new byte[]{(byte) 0x07}); // 0111 in binary -> pixels (0,0), (1,0), (0,1) set
        // index 0 -> (0,0)
        // index 1 -> (1,0)
        // index 2 -> (0,1)
        // index 3 -> (1,1)

        Overlays.applyOverlay(0, raster, attrs, 0, 255, null);
        
        assertEquals(255, raster.getSample(0, 0, 0));
        assertEquals(255, raster.getSample(1, 0, 0));
        assertEquals(255, raster.getSample(0, 1, 0));
        assertEquals(0, raster.getSample(1, 1, 0));
    }
}
