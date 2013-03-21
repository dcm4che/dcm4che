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

package org.dcm4che.image;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LUTParam {

    private final StoredValue storedValue;
    private float rescaleSlope = 1;
    private float rescaleIntercept = 0;
    private Attributes modalityLUT;
    private float windowCenter;
    private float windowWidth;
    private String voiLUTFunction;
    private Attributes voiLUT;
    private Attributes presentationLUT;
    private boolean inverse;
    private int smallestImagePixelValue = -1;
    private int largestImagePixelValue = -1;

    public int getSmallestImagePixelValue() {
        return smallestImagePixelValue;
    }

    public int getLargestImagePixelValue() {
        return largestImagePixelValue;
    }

    public boolean needCalculateSmallestAndLargestPixelValue() {
        return largestImagePixelValue == 0;
    }

    public void calculateSmallestAndLargestPixelValue(DataBuffer buffer) {
        if (buffer instanceof DataBufferByte)
            calculateSmallestAndLargestPixelValue(
                    ((DataBufferByte) buffer).getData());
        else
            calculateSmallestAndLargestPixelValue(
                    ((DataBufferUShort) buffer).getData());
    }

    public void calculateSmallestAndLargestPixelValue(byte[] pixelData) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (byte pixel : pixelData) {
            int val = storedValue.valueOf(pixel);
            if (val < min)
                min = val;
            if (val > max)
                max = val;
        }
        smallestImagePixelValue = min;
        largestImagePixelValue = max;
    }

    public void calculateSmallestAndLargestPixelValue(short[] pixelData) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (short pixel : pixelData) {
            int val = storedValue.valueOf(pixel);
            if (val < min)
                min = val;
            if (val > max)
                max = val;
        }
        smallestImagePixelValue = min;
        largestImagePixelValue = max;
    }


    public LUTParam(StoredValue storedValue) {
        this.storedValue = storedValue;
    }

    public final StoredValue getStoredValue() {
        return storedValue;
    }

    public final float getRescaleSlope() {
        return rescaleSlope;
    }

    public final void setRescaleSlope(float rescaleSlope) {
        this.rescaleSlope = rescaleSlope;
    }

    public final float getRescaleIntercept() {
        return rescaleIntercept;
    }

    public final void setRescaleIntercept(float rescaleIntercept) {
        this.rescaleIntercept = rescaleIntercept;
    }

    public final Attributes getModalityLUT() {
        return modalityLUT;
    }

    public final void setModalityLUT(Attributes modalityLUT) {
        this.modalityLUT = modalityLUT;
    }

    public final float getWindowCenter() {
        return windowCenter;
    }

    public final void setWindowCenter(float windowCenter) {
        this.windowCenter = windowCenter;
    }

    public final float getWindowWidth() {
        return windowWidth;
    }

    public final void setWindowWidth(float windowWidth) {
        this.windowWidth = windowWidth;
    }

    public final boolean hasVOIWindow() {
        return windowWidth != 0;
    }

    public final boolean hasVOI() {
        return windowWidth != 0 || voiLUT != null;
    }

    public final String getVOILUTFunction() {
        return voiLUTFunction;
    }

    public final void setVOILUTFunction(String voiLUTFunction) {
        this.voiLUTFunction = voiLUTFunction;
    }

    public final Attributes getVOILUT() {
        return voiLUT;
    }

    public final void setVOILUT(Attributes voiLUT) {
        this.voiLUT = voiLUT;
    }

    public final boolean isInverse() {
        return inverse;
    }

    public final void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public final Attributes getPresentationLUT() {
        return presentationLUT;
    }

    public final void setPresentationLUT(Attributes presentationLUT) {
        this.presentationLUT = presentationLUT;
    }

    public void init(Attributes attrs) {
        rescaleIntercept = attrs.getFloat(Tag.RescaleIntercept, 0);
        rescaleSlope = attrs.getFloat(Tag.RescaleSlope, 1);
        modalityLUT = attrs.getNestedDataset(Tag.ModalityLUTSequence);
        String pShape = attrs.getString(Tag.PresentationLUTShape);
        inverse = (pShape != null 
                ? "INVERSE".equals(pShape)
                : "MONOCHROME1".equals(
                        attrs.getString(Tag.PhotometricInterpretation)));
        if (rescaleSlope < 0)
            inverse = !inverse;
    }

    public void setVOI(Attributes img, int windowIndex, int voiLUTIndex,
            boolean preferWindow, boolean autoWindowing) {
        Attributes voiLUT = img.getNestedDataset(Tag.VOILUTSequence, voiLUTIndex);
        if (preferWindow || voiLUT == null) {
            float[] wcs = img.getFloats(Tag.WindowCenter);
            float[] wws = img.getFloats(Tag.WindowWidth);
            if (wcs != null && windowIndex < wcs.length
                    && wws != null && windowIndex < wws.length) {
                windowCenter = wcs[windowIndex];
                windowWidth = wcs[windowIndex];
            } else
                this.voiLUT = voiLUT;
        } else
            this.voiLUT = voiLUT;
        if (!hasVOI()) {
            if (autoWindowing) {
                smallestImagePixelValue = storedValue.valueOf(
                        img.getInt(Tag.SmallestImagePixelValue, 0));
                largestImagePixelValue = storedValue.valueOf(
                        img.getInt(Tag.LargestImagePixelValue, 0));
            } else {
                smallestImagePixelValue = storedValue.minValue();
                largestImagePixelValue = storedValue.maxValue();
            }
        }
    }
}
