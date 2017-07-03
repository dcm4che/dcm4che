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

import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.SampleModel;

import org.dcm4che3.data.Attributes;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public enum PhotometricInterpretation {
    MONOCHROME1 {
        @Override
        public boolean isMonochrome() {
            return true;
        }

        @Override
        public boolean isInvers() {
            return true;
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    },
    MONOCHROME2 {
        @Override
        public boolean isMonochrome() {
            return true;
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createMonochromeColorModel(bits, dataType);
        }
    },
    PALETTE_COLOR {
        @Override
        public String toString() {
            return "PALETTE COLOR";
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createPaletteColorModel(bits, dataType, ds);
        }
    },
    RGB {
        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createRGBColorModel(bits, dataType, ds);
        }
        @Override
        public PhotometricInterpretation compress(String tsuid) {
            if (tsuid.length() == 22 && tsuid.startsWith("1.2.840.10008.1.2.4."))
                if (tsuid.endsWith("50") || tsuid.endsWith("51"))
                    return YBR_FULL_422;
                else if (tsuid.endsWith("90") || tsuid.endsWith("92"))
                    return YBR_RCT;
                else if (tsuid.endsWith("91") || tsuid.endsWith("93"))
                    return YBR_ICT;

            return this;
        }
    },
    YBR_FULL {
        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createYBRFullColorModel(bits, dataType, ds);
        }
    },
    YBR_FULL_422 {
        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, ds,
                    YBR.FULL, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h,
                int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

        @Override
        public boolean isSubSambled() {
            return true;
        }
    },
    YBR_PARTIAL_422 {
        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_422.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, ds,
                    YBR.PARTIAL, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h, 
                int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_422);
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

        @Override
        public boolean isSubSambled() {
            return true;
        }
    },
    YBR_PARTIAL_420 {
        @Override
        public int frameLength(int w, int h, int samples, int bitsAllocated) {
            return ColorSubsampling.YBR_XXX_420.frameLength(w, h);
        }

        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            return ColorModelFactory.createYBRColorModel(bits, dataType, ds,
                    YBR.PARTIAL, ColorSubsampling.YBR_XXX_420);
        }

        @Override
        public SampleModel createSampleModel(int dataType, int w, int h,
                int samples, boolean banded) {
            return new SampledComponentSampleModel(w, h, ColorSubsampling.YBR_XXX_420);
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }

        @Override
        public boolean isSubSambled() {
            return true;
        }
    },
    YBR_ICT {
        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }
    },
    YBR_RCT {
        @Override
        public ColorModel createColorModel(int bits, int dataType, Attributes ds) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PhotometricInterpretation decompress() {
            return RGB;
        }
    };

    public static PhotometricInterpretation fromString(String s) {
        return s.equals("PALETTE COLOR") ? PALETTE_COLOR : valueOf(s);
    }

    public int frameLength(int w, int h, int samples, int bitsAllocated) {
        return w * h * samples * (bitsAllocated >> 3);
    }

    public boolean isMonochrome() {
        return false;
    }

    public PhotometricInterpretation decompress() {
        return this;
    }

    public PhotometricInterpretation compress(String tsuid) {
        return this;
    }

    public boolean isInvers() {
        return false;
    }

    public boolean isSubSambled() {
        return false;
    }

    public abstract ColorModel createColorModel(int bits, int dataType,
            Attributes ds);

    public SampleModel createSampleModel(int dataType, int w, int h,
            int samples, boolean banded) {
        int[] indicies = new int[samples];
        for (int i = 1; i < samples; i++)
            indicies[i] = i;
        return banded
                ? new BandedSampleModel(dataType, w, h,
                        w, indicies, new int[samples])
                : new PixelInterleavedSampleModel(dataType, w, h,
                        samples, w * samples, indicies);
    }
}
