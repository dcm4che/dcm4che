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
 * Java(TM), hosted at https://github.com/dcm4che.
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

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LookupTableFactory {

    private final StoredValue storedValue;
    private float rescaleSlope = 1;
    private float rescaleIntercept = 0;
    private LookupTable modalityLUT;
    private float windowCenter;
    private float windowWidth;
    private String voiLUTFunction; // not yet implemented
    private LookupTable voiLUT;
    private LookupTable presentationLUT;
    private boolean inverse;

    public LookupTableFactory(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    public void setModalityLUT(Attributes attrs) {
        rescaleIntercept = attrs.getFloat(Tag.RescaleIntercept, 0);
        rescaleSlope = attrs.getFloat(Tag.RescaleSlope, 1);
        modalityLUT = createLUT(storedValue,
                attrs.getNestedDataset(Tag.ModalityLUTSequence));
    }

    public void setPresentationLUT(Attributes attrs) {
        Attributes pLUT = attrs.getNestedDataset(Tag.PresentationLUTSequence);
        if (pLUT != null) {
            int[] desc = pLUT.getInts(Tag.LUTDescriptor);
            if (desc != null && desc.length == 3) {
                int len = desc[0] == 0 ? 0x10000 : desc[0];
                presentationLUT = createLUT(new StoredValue.Unsigned(log2(len)), 
                        resetOffset(desc), 
                        pLUT.getSafeBytes(Tag.LUTData), pLUT.bigEndian());
            }
        } else {
            String pShape = attrs.getString(Tag.PresentationLUTShape);
            inverse = (pShape != null 
                ? "INVERSE".equals(pShape)
                : "MONOCHROME1".equals(
                        attrs.getString(Tag.PhotometricInterpretation)));
        }
    }

    private int[] resetOffset(int[] desc) {
        if (desc[1] == 0)
            return desc;
        
        int[] copy = desc.clone();
        copy[1] = 0;
        return copy;
    }

    public void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void setVOI(Attributes img, int windowIndex, int voiLUTIndex,
            boolean preferWindow) {
        if (img == null)
            return;

        Attributes vLUT = img.getNestedDataset(Tag.VOILUTSequence, voiLUTIndex);
        if (preferWindow || vLUT == null) {
            float[] wcs = img.getFloats(Tag.WindowCenter);
            float[] wws = img.getFloats(Tag.WindowWidth);
            if (wcs != null && wcs.length != 0
                    && wws != null && wws.length != 0) {
                int index = windowIndex < Math.min(wcs.length, wws.length)
                        ? windowIndex
                        : 0;
                windowCenter = wcs[index];
                windowWidth = wws[index];
                return;
            }
        }
        if (vLUT != null) {
            adjustVOILUTDescriptor(vLUT);
            voiLUT = createLUT(modalityLUT != null
                          ? new StoredValue.Unsigned(modalityLUT.outBits)
                          : storedValue,
                      vLUT);
        }
    }

    private void adjustVOILUTDescriptor(Attributes vLUT) {
        int[] desc = vLUT.getInts(Tag.LUTDescriptor);
        byte[] data;
        if (desc != null && desc.length == 3 && desc[2] == 16
                && (data = vLUT.getSafeBytes(Tag.LUTData)) != null) {
            int hiByte = 0;
            for (int i = vLUT.bigEndian() ? 0 : 1; i < data.length; i++, i++)
                hiByte |= data[i];
            if ((hiByte & 0x80) == 0) {
                desc[2] = 40 - Integer.numberOfLeadingZeros(hiByte & 0xFF);
                vLUT.setInt(Tag.LUTDescriptor, VR.SS, desc);
            }
        }
    }

    private LookupTable createLUT(StoredValue inBits, Attributes attrs) {
        if (attrs == null)
            return null;

        return createLUT(inBits, attrs.getInts(Tag.LUTDescriptor),
                attrs.getSafeBytes(Tag.LUTData), attrs.bigEndian());
    }

    private LookupTable createLUT(StoredValue inBits, int[] desc, byte[] data,
            boolean bigEndian) {

        if (desc == null)
            return null;

        if (desc.length != 3)
            return null;

        int len = desc[0] == 0 ? 0x10000 : desc[0];
        int offset = (short) desc[1];
        int outBits = desc[2];
        if (data == null)
            return null;

        if (data.length == len << 1) {
            if (outBits > 8) {
                if (outBits > 16)
                    return null;

                short[] ss = new short[len];
                if (bigEndian)
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteUtils.bytesToShortBE(data, i << 1);
                else
                    for (int i = 0; i < ss.length; i++)
                        ss[i] = (short) ByteUtils.bytesToShortLE(data, i << 1);

                return new ShortLookupTable(inBits, outBits, offset, ss);
            }
            // padded high bits -> use low bits
            data = halfLength(data, bigEndian ? 1 : 0);
        }
        if (data.length != len)
            return null;
        
        if (outBits > 8)
            return null;

        return new ByteLookupTable(inBits, outBits, offset, data);
    }

    static byte[] halfLength(byte[] data, int hilo) {
        byte[] bs = new byte[data.length >> 1];
        for (int i = 0; i < bs.length; i++)
            bs[i] = data[(i<<1)|hilo];

        return bs;
    }

    public LookupTable createLUT(int outBits) {
        LookupTable lut = combineModalityVOILUT(presentationLUT != null
                ? log2(presentationLUT.length())
                : outBits);
        if (presentationLUT != null) {
            lut = lut.combine(presentationLUT.adjustOutBits(outBits));
        } else if (inverse)
            lut.inverse();
        return lut;
    }

    private static int log2(int value) {
        int i = 0;
        while ((value>>>i) != 0)
            ++i;
        return i-1;
    }

    private LookupTable combineModalityVOILUT(int outBits) {
        float m = rescaleSlope;
        float b = rescaleIntercept;
        LookupTable modalityLUT = this.modalityLUT;
        LookupTable lut = this.voiLUT;
        if (lut == null) {
            float c = windowCenter;
            float w = windowWidth;

            if (w == 0 && modalityLUT != null)
                return modalityLUT.adjustOutBits(outBits);

            int size, offset;
            StoredValue inBits = modalityLUT != null
                    ? new StoredValue.Unsigned(modalityLUT.outBits)
                    : storedValue;
            if (w != 0) {
                size = Math.max(2,Math.abs(Math.round(w/m)));
                offset = Math.round((c-b)/m) - size/2;
            } else {
                offset = inBits.minValue();
                size = inBits.maxValue() - inBits.minValue() + 1;
            }
            lut = outBits > 8
                    ? new ShortLookupTable(inBits, outBits, offset, size, m < 0)
                    : new ByteLookupTable(inBits, outBits, offset, size, m < 0);
        } else {
            //TODO consider m+b
            lut = lut.adjustOutBits(outBits);
        }
        return modalityLUT != null ? modalityLUT.combine(lut) : lut;
    }

    public boolean autoWindowing(Attributes img, Raster raster) {
        if (modalityLUT != null || voiLUT != null || windowWidth != 0)
            return false;

        int[] min_max = calcMinMax(raster);
        windowCenter = (min_max[0] + min_max[1] + 1) / 2 * rescaleSlope + rescaleIntercept;
        windowWidth = Math.abs((min_max[1] + 1 - min_max[0]) * rescaleSlope);
        return true;
    }

    private int[] calcMinMax(Raster raster) {
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        DataBuffer dataBuffer = raster.getDataBuffer();
        switch (dataBuffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                return calcMinMax(storedValue, sm,
                        ((DataBufferByte) dataBuffer).getData());
            case DataBuffer.TYPE_USHORT:
                return calcMinMax(storedValue, sm,
                        ((DataBufferUShort) dataBuffer).getData());
            case DataBuffer.TYPE_SHORT:
                return calcMinMax(storedValue, sm,
                        ((DataBufferShort) dataBuffer).getData());
            default:
                throw new UnsupportedOperationException(
                        "DataBuffer: "+ dataBuffer.getClass() + " not supported");
        }
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm,
            byte[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        return new int[] { min, max };
    }

    private int[] calcMinMax(StoredValue storedValue, ComponentSampleModel sm,
            short[] data) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        for (int y = 0; y < h; y++)
            for (int i = y * stride, end = i + w; i < end;) {
                int val = storedValue.valueOf(data[i++]);
                if (val < min) min = val;
                if (val > max) max = val;
            }
        return new int[] { min, max };
    }

}
