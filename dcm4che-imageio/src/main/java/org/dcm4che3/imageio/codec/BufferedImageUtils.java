/*
 * **** BEGIN LICENSE BLOCK *****
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
 * The Initial Developer of the Original Code is Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011-2015
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
 * **** END LICENSE BLOCK *****
 */

package org.dcm4che3.imageio.codec;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Feb 2015.
 */
class BufferedImageUtils {

    public static BufferedImage createBufferedImage(ImageParams imageParams, TransferSyntaxType tsType) {
        int dataType = imageParams.getBitsAllocated() > 8
                ? (imageParams.isSigned() && (tsType == null || tsType.canEncodeSigned())
                    ? DataBuffer.TYPE_SHORT
                    : DataBuffer.TYPE_USHORT)
                : DataBuffer.TYPE_BYTE;
        int samples = imageParams.getSamples();
        int bitsStored = tsType == null
                ? imageParams.getBitsStored()
                : Math.min(imageParams.getBitsStored(), tsType.getMaxBitsStored());
        ComponentColorModel cm = samples == 1
                    ? new ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_GRAY),
                        new int[] { bitsStored },
                        false, // hasAlpha
                        false, // isAlphaPremultiplied,
                        Transparency.OPAQUE,
                        dataType)
                    :  new ComponentColorModel(
                        ColorSpace.getInstance(ColorSpace.CS_sRGB),
                        new int[] { bitsStored, bitsStored, bitsStored },
                        false, // hasAlpha
                        false, // isAlphaPremultiplied,
                        Transparency.OPAQUE,
                        dataType);

        int rows = imageParams.getRows();
        int columns = imageParams.getColumns();
        SampleModel sm = imageParams.isBanded()
                ? new BandedSampleModel(dataType, columns, rows, samples)
                : new PixelInterleavedSampleModel(dataType, columns, rows,
                        samples, columns * samples, bandOffsets(samples));
        WritableRaster raster = Raster.createWritableRaster(sm, null);
        return new BufferedImage(cm, raster, false, null);
    }

    private static int[] bandOffsets(int samples) {
        int[] offsets = new int[samples];
        for (int i = 0; i < samples; i++)
            offsets[i] = i;
        return offsets;
    }

    public static int sizeOf(BufferedImage bi) {
        WritableRaster raster = bi.getRaster();
        DataBuffer db = raster.getDataBuffer();
        return db.getSize() * db.getNumBanks()
                * (DataBuffer.getDataTypeSize(db.getDataType()) >>> 3);
    }

    public static void writeTo(BufferedImage bi, OutputStream out) throws IOException {
        WritableRaster raster = bi.getRaster();
        SampleModel sm = raster.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                writeTo(sm, ((DataBufferByte) db).getBankData(), out);
                break;
            case DataBuffer.TYPE_USHORT:
                writeTo(sm, ((DataBufferUShort) db).getData(), out);
                break;
            case DataBuffer.TYPE_SHORT:
                writeTo(sm, ((DataBufferShort) db).getData(), out);
                break;
            case DataBuffer.TYPE_INT:
                writeTo(sm, ((DataBufferInt) db).getData(), out);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unsupported Datatype: " + db.getDataType());
        }
    }

    private static void writeTo(SampleModel sm, byte[][] bankData, OutputStream out)
            throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        ComponentSampleModel csm = (ComponentSampleModel) sm;
        int len = w * csm.getPixelStride();
        int stride = csm.getScanlineStride();
        if (csm.getBandOffsets()[0] != 0)
            bgr2rgb(bankData[0]);
        for (byte[] b : bankData)
            for (int y = 0, off = 0; y < h; ++y, off += stride)
                out.write(b, off, len);
    }

    private static void bgr2rgb(byte[] bs) {
        for (int i = 0, j = 2; j < bs.length; i += 3, j += 3) {
            byte b = bs[i];
            bs[i] = bs[j];
            bs[j] = b;
        }
    }

    private static void writeTo(SampleModel sm, short[] data, OutputStream out)
            throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((ComponentSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 2];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length;) {
                short s = data[j++];
                b[i++] = (byte) s;
                b[i++] = (byte) (s >> 8);
            }
            out.write(b);
        }
    }

    private static void writeTo(SampleModel sm, int[] data, OutputStream out)
            throws IOException {
        int h = sm.getHeight();
        int w = sm.getWidth();
        int stride = ((SinglePixelPackedSampleModel) sm).getScanlineStride();
        byte[] b = new byte[w * 3];
        for (int y = 0; y < h; ++y) {
            for (int i = 0, j = y * stride; i < b.length;) {
                int s = data[j++];
                b[i++] = (byte) (s >> 16);
                b[i++] = (byte) (s >> 8);
                b[i++] = (byte) s;
            }
            out.write(b);
        }
    }

    public static void nullifyUnusedBits(int bitsStored, DataBuffer db) {
        if (bitsStored >= 16)
            return;

        short[] data;
        switch (db.getDataType()) {
        case DataBuffer.TYPE_USHORT:
            data = ((DataBufferUShort) db).getData();
            break;
        case DataBuffer.TYPE_SHORT:
            data = ((DataBufferShort) db).getData();
            break;
        default:
            throw new IllegalArgumentException("Unsupported Datatype: " + db.getDataType());
        }
        int mask = (1 << bitsStored) - 1;
        for (int i = 0; i < data.length; i++)
            data[i] &= mask;
    }

    public static int maxDiff(WritableRaster raster, WritableRaster raster2) {
        ComponentSampleModel csm =
                (ComponentSampleModel) raster.getSampleModel();
        ComponentSampleModel csm2 =
                (ComponentSampleModel) raster2.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        DataBuffer db2 = raster2.getDataBuffer();
        switch (db.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                return maxDiff(csm, ((DataBufferByte) db).getBankData(),
                        csm2, ((DataBufferByte) db2).getBankData());
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_SHORT:
                return maxDiff(csm, getShortData(db),csm2, getShortData(db2));
            default:
                throw new UnsupportedOperationException(
                        "Unsupported Datatype: " + db.getDataType());
        }
    }

    private static short[] getShortData (DataBuffer db) {
        if (db instanceof DataBufferShort)
            return ((DataBufferShort)db).getData();
        if (db instanceof DataBufferUShort)
            return ((DataBufferUShort)db).getData();
        throw new UnsupportedOperationException(
                "Unsupported Datatype: " + db.getDataType());
    }

    public static int maxDiff(WritableRaster raster, WritableRaster raster2, int blockSize) {
        if (blockSize <= 1)
            return maxDiff(raster, raster2);

        ComponentSampleModel csm =
                (ComponentSampleModel) raster.getSampleModel();
        ComponentSampleModel csm2 =
                (ComponentSampleModel) raster2.getSampleModel();
        DataBuffer db = raster.getDataBuffer();
        DataBuffer db2 = raster2.getDataBuffer();
        int w = csm.getWidth();
        int h = csm.getHeight();
        int maxY = (h / blockSize - 1) * blockSize;
        int maxX = (w / blockSize - 1) * blockSize;
        int[] samples = new int[blockSize * blockSize];
        int diff, maxDiff = 0;
        for (int b = 0; b < csm.getNumBands(); b++)
            for (int y = 0; y < maxY; y += blockSize) {
                for (int x = 0; x < maxX; x += blockSize) {
                    if (maxDiff < (diff = Math.abs(
                            sum(csm.getSamples(
                                    x, y, blockSize, blockSize, b, samples, db))
                                    - sum(csm2.getSamples(
                                    x, y, blockSize, blockSize, b, samples, db2)))))
                        maxDiff = diff;
                }
            }
        return maxDiff / samples.length;
    }

    private static int sum(int[] samples) {
        int sum = 0;
        for (int sample : samples)
            sum += sample;
        return sum;
    }

    private static int maxDiff(ComponentSampleModel csm, short[] data,
                        ComponentSampleModel csm2, short[] data2) {
        int w = csm.getWidth() * csm.getPixelStride();
        int h = csm.getHeight();
        int stride = csm.getScanlineStride();
        int stride2 = csm2.getScanlineStride();
        int diff, maxDiff = 0;
        for (int y = 0; y < h; y++) {
            for (int j = w, i = y * stride, i2 = y * stride2; j-- > 0; i++, i2++) {
                if (maxDiff < (diff = Math.abs(data[i] - data2[i2])))
                    maxDiff = diff;
            }
        }
        return maxDiff;
    }

    private static int maxDiff(ComponentSampleModel csm, byte[][] banks,
                        ComponentSampleModel csm2, byte[][] banks2) {
        int w = csm.getWidth();
        int h = csm.getHeight();
        int bands = csm.getNumBands();
        int stride = csm.getScanlineStride();
        int pixelStride = csm.getPixelStride();
        int[] bankIndices = csm.getBankIndices();
        int[] bandOffsets = csm.getBandOffsets();
        int stride2 = csm2.getScanlineStride();
        int pixelStride2 = csm2.getPixelStride();
        int[] bankIndices2 = csm2.getBankIndices();
        int[] bandOffsets2 = csm2.getBandOffsets();
        int diff, maxDiff = 0;
        for (int b = 0; b < bands; b++) {
            byte[] bank = banks[bankIndices[b]];
            byte[] bank2 = banks2[bankIndices2[b]];
            int off = bandOffsets[b];
            int off2 = bandOffsets2[b];
            for (int y = 0; y < h; y++) {
                for (int x = w, i = y * stride + off, i2 = y * stride2 + off2;
                     x-- > 0; i += pixelStride, i2 += pixelStride2) {
                    if (maxDiff < (diff = Math.abs(bank[i] - bank2[i2])))
                        maxDiff = diff;
                }
            }
        }
        return maxDiff;
    }
}
