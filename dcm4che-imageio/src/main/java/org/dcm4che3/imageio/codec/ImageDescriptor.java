/*
 * *** BEGIN LICENSE BLOCK *****
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
 * J4Care.
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.imageio.codec;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.image.Overlays;
import org.dcm4che3.image.PhotometricInterpretation;

import java.awt.image.DataBuffer;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2015
 */
public final class ImageDescriptor {

    private final int rows;
    private final int columns;
    private final int samples;
    private final PhotometricInterpretation photometricInterpretation;
    private final int bitsAllocated;
    private final int bitsStored;
    private final int pixelRepresentation;
    private final String sopClassUID;
    private final String bodyPartExamined;
    private final int frames;
    private final int[] embeddedOverlays;
    private final int planarConfiguration;

    public ImageDescriptor(Attributes attrs) {
        this.rows = attrs.getInt(Tag.Rows, 0);
        this.columns = attrs.getInt(Tag.Columns, 0);
        this.samples = attrs.getInt(Tag.SamplesPerPixel, 0);
        this.photometricInterpretation = PhotometricInterpretation.fromString(
                attrs.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
        this.bitsAllocated = attrs.getInt(Tag.BitsAllocated, 8);
        this.bitsStored = attrs.getInt(Tag.BitsStored, bitsAllocated);
        this.pixelRepresentation = attrs.getInt(Tag.PixelRepresentation, 0);
        this.planarConfiguration = attrs.getInt(Tag.PlanarConfiguration, 0);
        this.sopClassUID = attrs.getString(Tag.SOPClassUID);
        this.bodyPartExamined = attrs.getString(Tag.BodyPartExamined);
        this.frames = attrs.getInt(Tag.NumberOfFrames, 1);
        this.embeddedOverlays = Overlays.getEmbeddedOverlayGroupOffsets(attrs);
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getSamples() {
        return samples;
    }

    public PhotometricInterpretation getPhotometricInterpretation() {
        return photometricInterpretation;
    }

    public int getBitsAllocated() {
        return bitsAllocated;
    }

    public int getBitsStored() {
        return bitsStored;
    }

    public int getPixelRepresentation() {
        return pixelRepresentation;
    }

    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public String getSopClassUID() {
        return sopClassUID;
    }

    public String getBodyPartExamined() {
        return bodyPartExamined;
    }

    public int getFrames() {
        return frames;
    }

    public boolean isMultiframe() {
        return frames > 1;
    }

    public int getFrameLength() {
        return rows * columns * samples * (bitsAllocated>>>3);
    }

    public int getLength() {
        return getFrameLength() * frames;
    }

    public boolean isSigned() {
        return pixelRepresentation != 0;
    }

    public boolean isBanded() {
        return planarConfiguration != 0;
    }

    public int[] getEmbeddedOverlays() {
        return embeddedOverlays;
    }

    public boolean isMultiframeWithEmbeddedOverlays() {
        return embeddedOverlays.length > 0 && frames > 1;
    }
}
