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

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jan 2015.
 */
public class ImageParams {

    private final int rows;
    private final int cols;
    private final int samples;
    private final int pixelRepresentation;
    private final int bitsAllocated;
    private final int bitsStored;
    private final int frames;
    private final int frameLength;
    private final int length;

    private PhotometricInterpretation pmi;
    private int planarConfiguration;

    public ImageParams(Attributes attrs) {
        this.rows = attrs.getInt(Tag.Rows, 0);
        this.cols = attrs.getInt(Tag.Columns, 0);
        this.samples = attrs.getInt(Tag.SamplesPerPixel, 0);
        this.pmi = PhotometricInterpretation.fromString(
                attrs.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.bitsAllocated = attrs.getInt(Tag.BitsAllocated, 8);
        this.bitsStored = attrs.getInt(Tag.BitsStored, bitsAllocated);
        this.planarConfiguration = attrs.getInt(Tag.PlanarConfiguration, 0);
        this.pixelRepresentation = attrs.getInt(Tag.PixelRepresentation, 0);
        this.frames = attrs.getInt(Tag.NumberOfFrames, 1);
        this.frameLength = rows * cols * samples * (bitsAllocated >>> 3);
        this.length = frameLength * frames;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return cols;
    }

    public int getSamples() {
        return samples;
    }

    public int getBitsAllocated() {
        return bitsAllocated;
    }

    public int getBitsStored() {
        return bitsStored;
    }

    public int getFrameLength() {
        return frameLength;
    }

    public int getLength() {
        return length;
    }

    public PhotometricInterpretation getPhotometricInterpretation() {
        return pmi;
    }

    public int getEncodedLength() {
        return (length + 1) & ~1;
    }

    public boolean paddingNull() {
        return (length & 1) != 0;
    }

    public boolean isBanded() {
        return planarConfiguration != 0;
    }

    public boolean isSigned() {
        return pixelRepresentation != 0;
    }

    public int getFrames() {
        return frames;
    }

    public void decompress(Attributes attrs, TransferSyntaxType tstype) {
        if (samples > 1) {
            pmi = pmi.decompress();
            planarConfiguration = tstype.getPlanarConfiguration();
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());
            attrs.setInt(Tag.PlanarConfiguration, VR.US, planarConfiguration);
        }
    }

    public void compress(Attributes attrs, TransferSyntaxType tstype) {
        if (samples > 1) {
            pmi = tstype.compress(pmi);
            planarConfiguration = tstype.getPlanarConfiguration();
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, pmi.toString());
            attrs.setInt(Tag.PlanarConfiguration, VR.US, planarConfiguration);
        }
    }

}
