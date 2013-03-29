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
import java.awt.image.WritableRaster;
import java.util.Arrays;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class Overlays {

    public static int[] getGroupOffsets(Attributes attrs, int tag,
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

    public static byte[] extractFromPixeldata(int rows, int columns,
            int frames, int bitPosition, DataBuffer dataBuffer) {
        int frameLength = dataBuffer.getSize() / frames;
        int ovlyLength = rows * columns;
        int length = (((ovlyLength * frames + 7)>>>3)+1)&(~1);
        byte[] overlayData = new byte[length];
        int mask = 1 << bitPosition;
        if (dataBuffer instanceof DataBufferByte) {
            byte[] pixeldata = ((DataBufferByte) dataBuffer).getData();
            for (int f = 0, i = 0; f < frames; ++f)
                for (int j = f * frameLength, end = (f+1) * frameLength;
                        j < end; ++j, ++i)
                    if ((pixeldata[j] & mask) != 0)
                        overlayData[i>>>3] |= 1<<(i&7);
        } else {
            short[] pixeldata = ((DataBufferUShort) dataBuffer).getData();
            for (int f = 0, i = 0; f < frames; ++f)
                for (int j = f * frameLength, end = (f+1) * frameLength;
                        j < end; ++j, ++i)
                    if ((pixeldata[j] & mask) != 0)
                        overlayData[i>>>3] |= 1<<(i&7);
        }
        return overlayData ;
    }

    public static boolean extractFromPixeldata(Attributes attrs, int gg0000,
            DataBuffer dataBuffer) {
        if (!attrs.containsValue(Tag.OverlayRows | gg0000)
                || !isEmbedded(attrs, gg0000))
            return false;

        attrs.setBytes(Tag.OverlayData | gg0000, VR.OB,  extractFromPixeldata(
                attrs.getInt(Tag.OverlayRows | gg0000, 0),
                attrs.getInt(Tag.OverlayColumns | gg0000, 0),
                attrs.getInt(Tag.NumberOfFrames, 1),
                attrs.getInt(Tag.OverlayBitPosition | gg0000, 0),
                dataBuffer));
        attrs.setInt(Tag.OverlayBitsAllocated | gg0000, VR.US, 1);
        attrs.setInt(Tag.OverlayBitPosition | gg0000, VR.US, 0);
        return true;
    }

    public static boolean isEmbedded(Attributes attrs, int gg0000) {
        if (attrs.containsValue(Tag.OverlayData | gg0000))
            return false;

        int tagOverlayBitsAllocated = Tag.OverlayBitsAllocated | gg0000;
        int tagNumberOfFramesInOverlay = Tag.NumberOfFramesInOverlay | gg0000;
        int tagImageFrameOrigin = Tag.ImageFrameOrigin | gg0000;

        int frames = attrs.getInt(Tag.NumberOfFrames, 1);
        int bitsAllocated = attrs.getInt(Tag.BitsAllocated, 8);
        int bitsAllocatedInOverlay = attrs.getInt(tagOverlayBitsAllocated, -1);
        int framesInOverlay = attrs.getInt(tagNumberOfFramesInOverlay, 1);
        int imageFrameOrigin = attrs.getInt(tagImageFrameOrigin, 1);

        if (bitsAllocatedInOverlay != bitsAllocated)
            throw new IllegalArgumentException(
                    TagUtils.toString(tagOverlayBitsAllocated)
                    + " Overlay Bits Allocated [" + bitsAllocatedInOverlay 
                    + "] != (0028,0100) Bits Allocated [" + bitsAllocated
                    + "]");

        if (framesInOverlay != frames)
            throw new IllegalArgumentException(
                    TagUtils.toString(tagNumberOfFramesInOverlay)
                    + " Number of Frames in Overlay ["
                    + framesInOverlay + "] != (0028,0008) Number of Frames ["
                    + frames + "]");

        if (imageFrameOrigin != 1)
            throw new IllegalArgumentException(
                    TagUtils.toString(tagImageFrameOrigin)
                    + " Image Frame Origin [ "
                    + imageFrameOrigin + "] != 1");

        return true;
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
            Attributes attrs, int gg0000, int pixelValue) {

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
        byte[] overlayData = attrs.getSafeBytes(tagOverlayData);

        if (overlayData == null)
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
            int overlayBits = overlayData[i] & 0xff;
            for (int j = 0; (overlayBits>>>j) != 0; j++) {
                if ((overlayBits & (1<<j)) == 0)
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
