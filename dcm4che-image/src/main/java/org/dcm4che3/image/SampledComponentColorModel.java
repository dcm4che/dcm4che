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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

/**
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class SampledComponentColorModel extends ColorModel {

    private static final int[] BITS = { 8, 8, 8 };

    private final ColorSubsampling subsampling;

    public SampledComponentColorModel(ColorSpace cspace,
            ColorSubsampling subsampling) {
        super(24, BITS, cspace, false, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        this.subsampling = subsampling;
    }

    @Override
    public boolean isCompatibleRaster(Raster raster) {
        return isCompatibleSampleModel(raster.getSampleModel());
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        return sm instanceof SampledComponentSampleModel;
    }

    @Override
    public SampleModel createCompatibleSampleModel(int w, int h) {
        return new SampledComponentSampleModel(w, h, subsampling);
    }

    @Override
    public int getAlpha(int pixel) {
        return 255;
    }

    @Override
    public int getBlue(int pixel) {
        return pixel & 0xFF;
    }

    @Override
    public int getGreen(int pixel) {
        return pixel & 0xFF00;
    }

    @Override
    public int getRed(int pixel) {
        return pixel & 0xFF0000;
    }

    @Override
    public int getAlpha(Object inData) {
        return 255;
    }

    @Override
    public int getBlue(Object inData) {
        return getRGB(inData) & 0xFF;
    }

    @Override
    public int getGreen(Object inData) {
        return (getRGB(inData) >> 8) & 0xFF;
    }

    @Override
    public int getRed(Object inData) {
        return getRGB(inData) >> 16;
    }

    @Override
    public int getRGB(Object inData) {
        byte[] ba = (byte[]) inData;
        ColorSpace cs = getColorSpace();
        float[] fba = new float[] { (ba[0] & 0xFF) / 255f,
                (ba[1] & 0xFF) / 255f, (ba[2] & 0xFF) / 255f };
        float[] rgb = cs.toRGB(fba);
        int ret = (((int) (rgb[0] * 255)) << 16)
                | (((int) (rgb[1] * 255)) << 8) | (((int) (rgb[2] * 255)));
        return ret;
    }

}
