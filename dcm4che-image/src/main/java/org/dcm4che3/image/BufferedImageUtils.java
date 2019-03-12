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
 * Portions created by the Initial Developer are Copyright (C) 2015
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

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2015
 */
public class BufferedImageUtils {

    private BufferedImageUtils() {}

    public static BufferedImage convertToIntRGB(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        if (cm instanceof DirectColorModel)
            return bi;

        if (cm.getNumComponents() != 3)
            throw new IllegalArgumentException("ColorModel: " + cm);

        WritableRaster raster = bi.getRaster();
        if (cm instanceof PaletteColorModel)
            return ((PaletteColorModel) cm).convertToIntDiscrete(raster);

        BufferedImage intRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = intRGB.getGraphics();
        try {
            graphics.drawImage(bi, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return intRGB;
    }
    
    public static BufferedImage convertYBRtoRGB(BufferedImage src) {
        if (src.getColorModel().getTransferType() != DataBuffer.TYPE_BYTE) {
            throw new UnsupportedOperationException(
                "Cannot convert color model to RGB: unsupported transferType" + src.getColorModel().getTransferType());
        }
        if (src.getColorModel().getNumComponents() != 3) {
            throw new IllegalArgumentException("Unsupported colorModel: " + src.getColorModel());
        }

        ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8 },
            false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        int width = src.getWidth();
        int height = src.getHeight();
        SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
        DataBuffer dataBuffer = sampleModel.createDataBuffer();
        WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        WritableRaster raster = src.getRaster();
        ColorSpace cs = src.getColorModel().getColorSpace();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] ba = (byte[]) raster.getDataElements(j, i, null);
                float[] fba = new float[] { (ba[0] & 0xFF) / 255f, (ba[1] & 0xFF) / 255f, (ba[2] & 0xFF) / 255f };
                float[] rgb = cs.toRGB(fba);
                ba[0] = (byte) (rgb[0] * 255);
                ba[1] = (byte) (rgb[1] * 255);
                ba[2] = (byte) (rgb[2] * 255);
                rasterDst.setDataElements(j, i, ba);
            }
        }
        return new BufferedImage(cmodel, rasterDst, false, null);
    }

    public static BufferedImage convertPalettetoRGB(BufferedImage src, PaletteColorModel pcm) {
        int dataType = src.getColorModel().getTransferType();
        if (dataType != DataBuffer.TYPE_BYTE && dataType != DataBuffer.TYPE_USHORT) {
            throw new UnsupportedOperationException(
                "Cannot convert color model to RGB: unsupported transferType" + dataType);
        }
        
        ColorModel cmodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8 },
            false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        int width = src.getWidth();
        int height = src.getHeight();
        SampleModel sampleModel = cmodel.createCompatibleSampleModel(width, height);
        DataBuffer dataBuffer = sampleModel.createDataBuffer();
        WritableRaster rasterDst = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        WritableRaster raster = src.getRaster();
        ColorModel pm = pcm == null ? src.getColorModel(): pcm;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                byte[] b = convertTo3Bytes(pm , raster.getDataElements(j, i, null));
                rasterDst.setDataElements(j, i, b);
            }
        }
        return new BufferedImage(cmodel, rasterDst, false, null);
    }

    private static byte[] convertTo3Bytes(ColorModel pm, Object data) {
        byte[] b = new byte[3];
        int pix;
        if (data instanceof byte[]) {
            byte[] pixels = (byte[]) data;
            pix = pm.getRGB(pixels[0]);

        } else {
            short[] pixels = (short[]) data;
            pix = pm.getRGB(pixels[0]);
        }
        b[0] = (byte) ((pix >> 16) & 0xff);
        b[1] = (byte) ((pix >> 8) & 0xff);
        b[2] = (byte) (pix & 0xff);
        return b;
    }

}
