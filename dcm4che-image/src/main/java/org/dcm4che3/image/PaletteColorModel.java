/* ***** BEGIN LICENSE BLOCK *****
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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2013
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
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PaletteColorModel extends ColorModel {

    private static final int[] opaqueBits = {8, 8, 8};

    private final LUT lut;

    public PaletteColorModel(int bits, int dataType, ColorSpace cs,
            Attributes ds) {
        super(bits, opaqueBits, cs, false, false, OPAQUE, dataType);
        int[] rDesc = lutDescriptor(ds,
                Tag.RedPaletteColorLookupTableDescriptor);
        int[] gDesc = lutDescriptor(ds,
                Tag.GreenPaletteColorLookupTableDescriptor);
        int[] bDesc = lutDescriptor(ds,
                Tag.BluePaletteColorLookupTableDescriptor);
        byte[] r = lutData(ds, rDesc,
                Tag.RedPaletteColorLookupTableData,
                Tag.SegmentedRedPaletteColorLookupTableData);
        byte[] g = lutData(ds, gDesc,
                Tag.GreenPaletteColorLookupTableData,
                Tag.SegmentedGreenPaletteColorLookupTableData);
        byte[] b = lutData(ds, bDesc, 
                Tag.BluePaletteColorLookupTableData,
                Tag.SegmentedBluePaletteColorLookupTableData);
        lut = LUT.create(bits, r, g, b, rDesc[1], gDesc[1], bDesc[1]);
    }

    private int[] lutDescriptor(Attributes ds, int descTag) {
        int[] desc = ds.getInts(descTag);
        if (desc == null) {
            throw new IllegalArgumentException("Missing LUT Descriptor!");
        }
        if (desc.length != 3) {
            throw new IllegalArgumentException(
                    "Illegal number of LUT Descriptor values: " + desc.length);
        }
        if (desc[0] < 0)
            throw new IllegalArgumentException(
                    "Illegal LUT Descriptor: len=" + desc[0]);
        int bits = desc[2];
        if (bits != 8 && bits != 16)
            throw new IllegalArgumentException(
                    "Illegal LUT Descriptor: bits=" + bits);
        return desc;
    }

    private byte[] lutData(Attributes ds, int[] desc, int dataTag, int segmTag) {
        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int bits = desc[2];
        byte[] data = ds.getSafeBytes(dataTag);
        if (data == null) {
            int[] segm = ds.getInts(segmTag);
            if (segm == null) {
                throw new IllegalArgumentException("Missing LUT Data!");
            }
            if (bits == 8) {
                throw new IllegalArgumentException(
                        "Segmented LUT Data with LUT Descriptor: bits=8");
            }
            data = new byte[len];
            inflateSegmentedLut(segm, data);
        } else if (bits == 16 || data.length != len) {
            if (data.length != len << 1)
                lutLengthMismatch(data.length, len);
            int hilo = ds.bigEndian() ? 0 : 1;
            if (bits == 8)
                hilo = 1 - hilo; // padded high bits -> use low bits
            data = LookupTableFactory.halfLength(data, hilo);
        }
        return data;
    }

    private void inflateSegmentedLut(int[] in, byte[] out) {
        int x = 0;
        try {
            for (int i = 0; i < in.length;) {
                int op = in[i++];
                int n = in[i++];
                switch (op) {
                case 0:
                    while (n-- > 0)
                        out[x++] = (byte) in[i++];
                    break;
                case 1:
                     x = linearSegment(in[i++], out, x, n);
                    break;
                case 2: {
                    int i2 = (in[i++] & 0xffff) | (in[i++] << 16);
                    while (n-- > 0) {
                        int op2 = in[i2++];
                        int n2 = in[i2++] & 0xffff;
                        switch (op2) {
                        case 0:
                            while (n2-- > 0)
                                out[x++] = (byte) in[i2++];
                            break;
                        case 1:
                            x = linearSegment(in[i2++], out, x, n);
                            break;
                        default:
                            illegalOpcode(op, i2-2);
                        }
                    }
                }
                default:
                    illegalOpcode(op, i-2);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            if (x > out.length)
                exceedsLutLength(out.length);
            else
                endOfSegmentedLut();
        }
        if (x < out.length)
            lutLengthMismatch(x, out.length);
    }

    private static void endOfSegmentedLut() {
        throw new IllegalArgumentException(
                "Running out of data inflating segmented LUT");
    }

    private static int linearSegment(int y1, byte[] out, int x, int n) {
        if (x == 0)
            throw new IllegalArgumentException(
                    "Linear segment cannot be the first segment");

        try {
            int y0 = out[x-1];
            int dy = y1-y0;
            for (int j = 1; j <= n; j++)
                out[x++] = (byte)((y0 + dy*j/n)>>8);
        } catch (IndexOutOfBoundsException e) {
            exceedsLutLength(out.length);
        }
        return x;
    }

    private static void exceedsLutLength(int descLen) {
        throw new IllegalArgumentException(
                "Number of entries in inflated segmented LUT exceeds specified value: "
                + descLen + " in LUT Descriptor");
    }

    private static void lutLengthMismatch(int lutLen, int descLen) {
        throw new IllegalArgumentException("Number of actual LUT entries: "
                + lutLen +  " mismatch specified value: " 
                + descLen + " in LUT Descriptor");
    }

    private static void illegalOpcode(int op, int i) {
        throw new IllegalArgumentException("illegal op code:" + op
                + ", index:" + i);
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm.getTransferType() == transferType
                && sm.getNumBands() == 1; 
    }

    @Override
    public int getRed(int pixel) {
        return lut.getRed(pixel);
    }

    @Override
    public int getGreen(int pixel) {
        return lut.getGreen(pixel);
    }

    @Override
    public int getBlue(int pixel) {
        return lut.getBlue(pixel);
    }

    @Override
    public int getAlpha(int pixel) {
        return lut.getAlpha(pixel);
    }

    @Override
    public int getRGB(int pixel) {
        return lut.getRGB(pixel);
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(int w, int h) {
        return Raster.createInterleavedRaster(
                pixel_bits <= 8
                    ? DataBuffer.TYPE_BYTE
                    : DataBuffer.TYPE_USHORT,
                    w, h, 1, null);
    }

    public BufferedImage convertToIntDiscrete(Raster raster) {
        if (!isCompatibleRaster(raster))
            throw new IllegalArgumentException(
                    "This raster is not compatible with this PaletteColorModel.");

        ColorModel cm = new DirectColorModel(getColorSpace(), 24,
                0xff0000, 0x00ff00, 0x0000ff, 0, false, DataBuffer.TYPE_INT);

        int w = raster.getWidth();
        int h = raster.getHeight();
        WritableRaster discreteRaster = cm.createCompatibleWritableRaster(w, h);
        int[] discretData = ((DataBufferInt) discreteRaster.getDataBuffer()).getData();
        DataBuffer data = raster.getDataBuffer();
        if (data instanceof DataBufferByte) {
            byte[] pixels = ((DataBufferByte) data).getData();
            for (int i = 0; i < pixels.length; i++)
                discretData[i] = getRGB(pixels[i]);
        } else {
            short[] pixels = ((DataBufferUShort) data).getData();
            for (int i = 0; i < pixels.length; i++)
                discretData[i] = getRGB(pixels[i]);
        }
        return new BufferedImage(cm, discreteRaster, false, null);
    }

    private static abstract class LUT {

        final int mask;

        LUT(int bits) {
            mask = (1 << bits) - 1;
        }

        public static LUT create(int bits, byte[] r, byte[] g, byte[] b,
                int rOffset, int gOffset, int bOffset) {
            
            return r.length == g.length && g.length == b.length
                  && rOffset == gOffset && gOffset == bOffset
                    ? new Packed(bits, r, g, b, rOffset)
                    : new PerColor(bits, r, g, b, rOffset, gOffset, bOffset);
        }

        int index(int pixel, int offset, int length) {
            return Math.min(Math.max(0, (pixel & mask) - offset), length-1);
        }

        abstract int getRed(int pixel);

        abstract int getGreen(int pixel);

        abstract int getBlue(int pixel);

        abstract int getAlpha(int pixel);

        abstract int getRGB(int pixel);

        static class Packed extends LUT {

            final int offset;
            final int[] rgb;
            
            Packed(int bits, byte[] r, byte[] g, byte[] b, int offset) {
                super(bits);
                int length = r.length;
                this.offset = offset;
                rgb = new int[length];
                for (int i = 0; i < r.length; i++)
                    rgb[i] = 0xff000000
                        | ((r[i] & 0xff) << 16)
                        | ((g[i] & 0xff) << 8)
                        | (b[i] & 0xff);
            }

            @Override
            public int getAlpha(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 24) & 0xff;
            }

            @Override
            public int getRed(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 16) & 0xff;
            }

            @Override
            public int getGreen(int pixel) {
                return (rgb[index(pixel, offset, rgb.length)] >> 8) & 0xff;
            }

            @Override
            public int getBlue(int pixel) {
                return rgb[index(pixel, offset, rgb.length)] & 0xff;
            }

            @Override
            public int getRGB(int pixel) {
                return rgb[index(pixel, offset, rgb.length)];
            }
        }

        static class PerColor extends LUT {
 
            final byte[] r;
            final byte[] g;
            final byte[] b;
            final int rOffset;
            final int gOffset;
            final int bOffset;

            PerColor(int bits, byte[] r, byte[] g, byte[] b, int rOffset,
                    int gbOffset, int bOffset) {
                super(bits);
                this.r = r;
                this.g = g;
                this.b = b;
                this.rOffset = rOffset;
                this.gOffset = gbOffset;
                this.bOffset = bOffset;
            }

            @Override
            public int getAlpha(int pixel) {
                return 0xff;
            }

            @Override
            public int getRed(int pixel) {
                return value(pixel, rOffset, r);
            }

            @Override
            public int getGreen(int pixel) {
                return value(pixel, gOffset, g);
            }

            @Override
            public int getBlue(int pixel) {
                return value(pixel, bOffset, b);
            }

            @Override
            public int getRGB(int pixel) {
                return 0xff000000
                            | (value(pixel, rOffset, r) << 16)
                            | (value(pixel, gOffset, g) << 8)
                            | (value(pixel, bOffset, b));
            }

            int value(int pixel, int offset, byte[] lut) {
                return lut[index(pixel, offset, lut.length)] & 0xff;
            }
        }

    }

}
