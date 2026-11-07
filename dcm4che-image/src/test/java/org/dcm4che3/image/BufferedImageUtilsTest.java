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
 * Portions created by the Initial Developer are Copyright (C) 2026
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

package org.dcm4che3.image;

import org.junit.Test;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Eric Pope
 */
public class BufferedImageUtilsTest {

    private static final int W = 7;
    private static final int H = 5;

    @Test
    public void convertShortsToBytesGray() {
        BufferedImage src = createUShortImage(1, false);
        BufferedImage dst = BufferedImageUtils.convertShortsToBytes(src, null);
        assertEquals(BufferedImage.TYPE_BYTE_GRAY, dst.getType());
        assertSamples(src, dst);
    }

    @Test
    public void convertShortsToBytesPixelInterleavedRGB() {
        BufferedImage src = createUShortImage(3, false);
        BufferedImage dst = BufferedImageUtils.convertShortsToBytes(src, null);
        assertTrue(dst.getSampleModel() instanceof PixelInterleavedSampleModel);
        assertSamples(src, dst);
    }

    @Test
    public void convertShortsToBytesBandedRGB() {
        BufferedImage src = createUShortImage(3, true);
        BufferedImage dst = BufferedImageUtils.convertShortsToBytes(src, null);
        assertTrue(dst.getSampleModel() instanceof BandedSampleModel);
        assertSamples(src, dst);
    }

    @Test
    public void convertShortsToBytesReusesDestination() {
        BufferedImage src = createUShortImage(3, false);
        BufferedImage dst = BufferedImageUtils.convertShortsToBytes(src, null);
        src.getRaster().setSample(0, 0, 0, 0x2A);
        assertEquals(dst, BufferedImageUtils.convertShortsToBytes(src, dst));
        assertSamples(src, dst);
    }

    private static BufferedImage createUShortImage(int numBands, boolean banded) {
        ColorSpace cs = ColorSpace.getInstance(numBands == 1 ? ColorSpace.CS_GRAY : ColorSpace.CS_sRGB);
        int[] bits = new int[numBands];
        java.util.Arrays.fill(bits, 16);
        ColorModel cm = new ComponentColorModel(cs, bits, false, false, Transparency.OPAQUE,
                DataBuffer.TYPE_USHORT);
        WritableRaster raster = banded
                ? Raster.createBandedRaster(DataBuffer.TYPE_USHORT, W, H, numBands, null)
                : Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT, W, H, numBands, null);
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                for (int b = 0; b < numBands; b++)
                    raster.setSample(x, y, b, ((y * W + x) * numBands + b) * 7 & 0xFF);
        return new BufferedImage(cm, raster, false, null);
    }

    private static void assertSamples(BufferedImage src, BufferedImage dst) {
        Raster s = src.getRaster();
        Raster d = dst.getRaster();
        assertEquals(s.getNumBands(), d.getNumBands());
        assertEquals(DataBuffer.TYPE_BYTE, d.getTransferType());
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                for (int b = 0; b < s.getNumBands(); b++)
                    assertEquals("sample " + x + "," + y + "," + b, s.getSample(x, y, b), d.getSample(x, y, b));
    }
}
