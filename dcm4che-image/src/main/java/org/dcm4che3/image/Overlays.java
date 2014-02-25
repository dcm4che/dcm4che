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

import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Overlays {

    public static int[] getActiveOverlayGroupOffsets(Attributes psattrs) {
        return getOverlayGroupOffsets(psattrs, Tag.OverlayActivationLayer, -1);
    }

    public static int[] getActiveOverlayGroupOffsets(Attributes attrs,
            int activationMask) {
        return getOverlayGroupOffsets(attrs, Tag.OverlayRows, activationMask);
    }

    public static int[] getOverlayGroupOffsets(Attributes attrs, int tag,
            int activationMask) {
        int len = 0;
        int[] result = new int[16];
        for (int i = 0; i < result.length; i++) {
            int gg0000 = i << 17;
            if ((activationMask & (1<<i)) != 0
                    &&  attrs.containsValue(tag | gg0000))
                result[len++] = gg0000;
        }
        return Arrays.copyOf(result, len);
    }

    public static int[] getEmbeddedOverlayGroupOffsets(Attributes attrs) {
        int len = 0;
        int[] result = new int[16];
        for (int i = 0; i < result.length; i++) {
            int gg0000 = i << 17;
            if (attrs.getInt(Tag.OverlayBitsAllocated | gg0000, 1) != 1)
                result[len++] = gg0000;
        }
        return Arrays.copyOf(result, len);
    }

   public static void extractFromPixeldata(Raster raster, int mask, 
            byte[] ovlyData, int off, int length) {
        ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        int rows = raster.getHeight();
        int columns = raster.getWidth();
        int stride = sm.getScanlineStride();
        DataBuffer db = raster.getDataBuffer();
        switch (db.getDataType()) {
        case DataBuffer.TYPE_BYTE:
            extractFromPixeldata(((DataBufferByte) db).getData(),
                    rows, columns, stride, mask,
                    ovlyData, off, length);
            break;
        case DataBuffer.TYPE_USHORT:
            extractFromPixeldata(((DataBufferUShort) db).getData(),
                    rows, columns, stride, mask,
                    ovlyData, off, length);
            break;
        case DataBuffer.TYPE_SHORT:
            extractFromPixeldata(((DataBufferShort) db).getData(),
                    rows, columns, stride, mask,
                    ovlyData, off, length);
            break;
        default:
            throw new UnsupportedOperationException(
                    "Unsupported DataBuffer type: " + db.getDataType());
        }
    }

    private static void extractFromPixeldata(byte[] pixeldata,
            int rows, int columns, int stride, int mask,
            byte[] ovlyData, int off, int length) {
        for (int y = 0, i = off, imax = off + length;
                y < columns && i < imax; y++) {
            for (int j = y * stride, jmax = j + rows; j < jmax && i < imax; j++, i++) {
                if ((pixeldata[j] & mask) != 0)
                    ovlyData[i >>> 3] |= 1 << (i & 7);
            }
        }
    }

    private static void extractFromPixeldata(short[] pixeldata,
            int rows, int columns, int stride, int mask,
            byte[] ovlyData, int off, int length) {
        for (int y = 0, i = off, imax = off + length;
                y < columns && i < imax; y++) {
            for (int j = y * stride, jmax = j + rows; j < jmax && i < imax; j++, i++) {
                if ((pixeldata[j] & mask) != 0) {
                    ovlyData[i >>> 3] |= 1 << (i & 7);
                }
            }
        }
    }

    public static int getRecommendedDisplayGrayscaleValue(Attributes psAttrs,
            int gg0000) {
        int tagOverlayActivationLayer = Tag.OverlayActivationLayer | gg0000;
        String layerName = psAttrs.getString(tagOverlayActivationLayer);
        if (layerName == null)
            throw new IllegalArgumentException("Missing "
                    + TagUtils.toString(tagOverlayActivationLayer)
                    + " Overlay Activation Layer");
        Sequence layers = psAttrs.getSequence(Tag.GraphicLayerSequence);
        if (layers == null)
            throw new IllegalArgumentException("Missing "
                    + TagUtils.toString(Tag.GraphicLayerSequence)
                    + " Graphic Layer Sequence");
        
        for (Attributes layer : layers)
            if (layerName.equals(layer.getString(Tag.GraphicLayer)))
                return layer.getInt(Tag.RecommendedDisplayGrayscaleValue, -1);

        throw new IllegalArgumentException("No Graphic Layer: " + layerName);
    }

    public static void applyOverlay(int frameIndex, WritableRaster raster,
            Attributes attrs, int gg0000, int pixelValue, byte[] ovlyData) {

        int imageFrameOrigin = attrs.getInt(Tag.ImageFrameOrigin | gg0000, 1);
        int framesInOverlay = attrs.getInt(Tag.NumberOfFramesInOverlay | gg0000, 1);
        int ovlyFrameIndex = frameIndex - imageFrameOrigin  + 1;
        if (ovlyFrameIndex < 0 || ovlyFrameIndex >= framesInOverlay)
            return;
        
        int tagOverlayRows = Tag.OverlayRows | gg0000;
        int tagOverlayColumns = Tag.OverlayColumns | gg0000;
        int tagOverlayData = Tag.OverlayData | gg0000;
        int tagOverlayOrigin = Tag.OverlayOrigin | gg0000;

        int ovlyRows = attrs.getInt(tagOverlayRows, -1);
        int ovlyColumns = attrs.getInt(tagOverlayColumns, -1);
        int[] ovlyOrigin = attrs.getInts(tagOverlayOrigin);
        if (ovlyData == null)
            ovlyData = attrs.getSafeBytes(tagOverlayData);

        if (ovlyData == null)
            throw new IllegalArgumentException("Missing "
                    + TagUtils.toString(tagOverlayData)
                    + " Overlay Data");
        if (ovlyRows <= 0)
            throw new IllegalArgumentException(
                    TagUtils.toString(tagOverlayRows)
                    + " Overlay Rows [" + ovlyRows + "]");
        if (ovlyColumns <= 0)
            throw new IllegalArgumentException(
                    TagUtils.toString(tagOverlayColumns)
                    + " Overlay Columns [" + ovlyColumns + "]");
        if (ovlyOrigin == null)
            throw new IllegalArgumentException("Missing "
                    + TagUtils.toString(tagOverlayOrigin)
                    + " Overlay Origin");
        if (ovlyOrigin.length != 2)
            throw new IllegalArgumentException(
                    TagUtils.toString(tagOverlayOrigin)
                    + " Overlay Origin " + Arrays.toString(ovlyOrigin));

        int x0 = ovlyOrigin[1] - 1;
        int y0 = ovlyOrigin[0] - 1;

        int ovlyLen = ovlyRows * ovlyColumns;
        int ovlyOff = ovlyLen * ovlyFrameIndex;
        for (int i = ovlyOff >>> 3,
               end = (ovlyOff + ovlyLen + 7) >>> 3; i < end; i++) {
            int ovlyBits = ovlyData[i] & 0xff;
            for (int j = 0; (ovlyBits>>>j) != 0; j++) {
                if ((ovlyBits & (1<<j)) == 0)
                    continue;

                int ovlyIndex = ((i<<3) + j) - ovlyOff;
                if (ovlyIndex >= ovlyLen)
                    continue;

                int y = y0 + ovlyIndex / ovlyColumns;
                int x = x0 + ovlyIndex % ovlyColumns;
                try {
                    raster.setSample(x, y, 0, pixelValue);
                } catch (ArrayIndexOutOfBoundsException ignore) {}
            }
        }
    }

}
