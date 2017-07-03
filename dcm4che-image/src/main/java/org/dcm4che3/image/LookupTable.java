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

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class LookupTable {

    protected StoredValue inBits;
    protected int outBits;
    protected int offset;

    public LookupTable(StoredValue inBits, int outBits, int offset) {
        this.inBits = inBits;
        this.outBits = outBits;
        this.offset = offset;
    }

    public abstract int length();

    public void lookup(Raster srcRaster, Raster destRaster) {
        ComponentSampleModel sm =
                (ComponentSampleModel) srcRaster.getSampleModel();
        ComponentSampleModel destsm =
                (ComponentSampleModel) destRaster.getSampleModel();
        DataBuffer src = srcRaster.getDataBuffer();
        DataBuffer dest = destRaster.getDataBuffer();
        switch (src.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferByte) src).getData(),
                        destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferByte) src).getData(),
                        destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        case DataBuffer.TYPE_USHORT:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferUShort) src).getData(),
                        destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferUShort) src).getData(),
                        destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        case DataBuffer.TYPE_SHORT:
            switch (dest.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookup(sm, ((DataBufferShort) src).getData(),
                        destsm, ((DataBufferByte) dest).getData());
                return;
            case DataBuffer.TYPE_USHORT:
                lookup(sm, ((DataBufferShort) src).getData(),
                        destsm, ((DataBufferUShort) dest).getData());
                return;
            }
            break;
        }
        throw new UnsupportedOperationException(
                "Lookup " + src.getClass()
                + " -> " + dest.getClass()
                + " not supported");
   }

    private void lookup(ComponentSampleModel sm, byte[] src,
            ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, short[] src,
            ComponentSampleModel destsm, byte[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, byte[] src,
            ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    private void lookup(ComponentSampleModel sm, short[] src,
            ComponentSampleModel destsm, short[] dest) {
        int w = sm.getWidth();
        int h = sm.getHeight();
        int stride = sm.getScanlineStride();
        int destStride = destsm.getScanlineStride();
        for (int y = 0; y < h; y++)
            lookup(src, y * stride, dest, y * destStride, w);
    }

    public abstract void lookup(byte[] src, int srcPost,
            byte[] dest, int destPos, int length);

    public abstract void lookup(short[] src, int srcPost,
            byte[] dest, int destPos, int length);

    public abstract void lookup(byte[] src, int srcPost,
            short[] dest, int destPos, int length);

    public abstract void lookup(short[] src, int srcPost,
            short[] dest, int destPos, int length);

    public abstract LookupTable adjustOutBits(int outBits);

    public abstract void inverse();

    public abstract LookupTable combine(LookupTable lut);

}
