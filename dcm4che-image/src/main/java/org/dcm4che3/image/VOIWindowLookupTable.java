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

/**
 * Full-range VOI window LUT indexed by stored pixel value, with on-demand calculation
 * when an index falls outside the precomputed table.
 */
class VOIWindowLookupTable extends LookupTable {

    private final byte[] lut;
    private final int lutMin;
    private final LookupTable modalityLUT;
    private final float rescaleSlope;
    private final float rescaleIntercept;
    private final VOILUTFunction function;
    private final float windowCenter;
    private final float windowWidth;
    private final int ymin;
    private final int ymax;

    VOIWindowLookupTable(StoredValue inBits, int outBits, int lutMin, byte[] lut,
            LookupTable modalityLUT, float rescaleSlope, float rescaleIntercept,
            VOILUTFunction function, float windowCenter, float windowWidth) {
        super(inBits, outBits, lutMin);
        this.lut = lut;
        this.lutMin = lutMin;
        this.modalityLUT = modalityLUT;
        this.rescaleSlope = rescaleSlope;
        this.rescaleIntercept = rescaleIntercept;
        this.function = function;
        this.windowCenter = windowCenter;
        this.windowWidth = windowWidth;
        this.ymin = 0;
        this.ymax = (1 << outBits) - 1;
    }

    @Override
    public int length() {
        return lut.length;
    }

    private int output(int pixel) {
        int sv = inBits.valueOf(pixel);
        int index = sv - lutMin;
        if (index >= 0 && index < lut.length)
            return lut[index] & 0xff;
        return compute(sv);
    }

    private int compute(int storedValue) {
        double x = modalityValue(storedValue);
        return VOILUT.apply(function, x, windowCenter, windowWidth, ymin, ymax);
    }

    private double modalityValue(int storedValue) {
        if (modalityLUT == null)
            return VOILUT.modalityValue(storedValue, rescaleSlope, rescaleIntercept);
        byte[] bIn = { (byte) storedValue };
        short[] sIn = { (short) storedValue };
        if (modalityLUT.outBits > 8) {
            short[] sOut = { 0 };
            modalityLUT.lookup(sIn, 0, sOut, 0, 1);
            return sOut[0];
        }
        byte[] bOut = { 0 };
        modalityLUT.lookup(bIn, 0, bOut, 0, 1);
        return bOut[0] & 0xff;
    }

    @Override
    public void lookup(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (byte) output(src[i++]);
    }

    @Override
    public void lookup(short[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (byte) output(src[i++]);
    }

    @Override
    public void lookup(byte[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) output(src[i++]);
    }

    @Override
    public void lookup(short[] src, int srcPos, short[] dest, int destPos, int length) {
        for (int i = srcPos, endPos = srcPos + length, j = destPos; i < endPos;)
            dest[j++] = (short) output(src[i++]);
    }

    @Override
    public LookupTable adjustOutBits(int outBits) {
        if (outBits == this.outBits)
            return this;
        int diff = outBits - this.outBits;
        byte[] scaled = new byte[lut.length];
        if (diff > 0) {
            for (int i = 0; i < lut.length; i++)
                scaled[i] = (byte) (Math.min((lut[i] & 0xff) << diff, (1 << outBits) - 1));
        } else {
            diff = -diff;
            for (int i = 0; i < lut.length; i++)
                scaled[i] = (byte) ((lut[i] & 0xff) >> diff);
        }
        return new VOIWindowLookupTable(inBits, outBits, lutMin, scaled,
                modalityLUT, rescaleSlope, rescaleIntercept,
                function, windowCenter, windowWidth);
    }

    @Override
    public void inverse() {
        for (int i = 0; i < lut.length; i++)
            lut[i] = (byte) (ymax - (lut[i] & 0xff));
    }

    @Override
    public LookupTable combine(LookupTable other) {
        byte[] combined = new byte[lut.length];
        other.lookup(lut, 0, combined, 0, lut.length);
        return new ByteLookupTable(inBits, other.outBits, lutMin, combined);
    }
}
