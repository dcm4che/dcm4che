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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.PhotometricInterpretation;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2015.
 */
class ImageParams {

    private final int rows;
    private final int cols;
    private final int samples;
    private final PhotometricInterpretation pmi;
    private final int bitsAllocated;
    private final int bitsStored;
    private final boolean banded;
    private final boolean signed;
    private final int frames;
    private final int frameLength;
    private final int length;

    public ImageParams(Attributes attrs) {
        this.rows = attrs.getInt(Tag.Rows, 0);
        this.cols = attrs.getInt(Tag.Columns, 0);
        this.samples = attrs.getInt(Tag.SamplesPerPixel, 0);
        this.pmi = PhotometricInterpretation.fromString(
                attrs.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.bitsAllocated = attrs.getInt(Tag.BitsAllocated, 8);
        this.bitsStored = attrs.getInt(Tag.BitsStored, bitsAllocated);
        this.banded = attrs.getInt(Tag.PlanarConfiguration, 0) != 0;
        this.signed = attrs.getInt(Tag.PixelRepresentation, 0) != 0;
        this.frames = attrs.getInt(Tag.NumberOfFrames, 1);
        this.frameLength = rows * cols * samples * (bitsAllocated >>> 3);
        this.length = frameLength * frames;
    }

    public void decompress(Attributes attrs, TransferSyntaxType tstype) {
        if (samples > 1) {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS,
                    pmi.decompress().toString());
            attrs.setInt(Tag.PlanarConfiguration, VR.US,
                    tstype.getPlanarConfiguration());
        }
    }

    public int getLength() {
        return length;
    }

    public BufferedImage createBufferedImage() {
        int dataType = bitsAllocated > 8
                ? (signed ? DataBuffer.TYPE_SHORT : DataBuffer.TYPE_USHORT)
                : DataBuffer.TYPE_BYTE;
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

        SampleModel sm = banded
                ? new BandedSampleModel(dataType, cols, rows, samples)
                : new PixelInterleavedSampleModel(dataType, cols, rows,
                samples, cols * samples, bandOffsets());
        WritableRaster raster = Raster.createWritableRaster(sm, null);
        return new BufferedImage(cm, raster, false, null);
    }

    private int[] bandOffsets() {
        int[] offsets = new int[samples];
        for (int i = 0; i < samples; i++)
            offsets[i] = i;
        return offsets;
    }

    public int getFrames() {
        return frames;
    }
}
