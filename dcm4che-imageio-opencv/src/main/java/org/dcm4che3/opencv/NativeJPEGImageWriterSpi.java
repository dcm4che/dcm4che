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
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 *
 */

package org.dcm4che3.opencv;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 * @author Nicolas Roduit
 * @since Aug 2018
 */
public class NativeJPEGImageWriterSpi extends ImageWriterSpi {

    public NativeJPEGImageWriterSpi() {
        this(NativeJPEGImageWriter.class);
    }

    public NativeJPEGImageWriterSpi(Class<? extends NativeJPEGImageWriter> writer) {
        super("Weasis Team", "1.0", NativeJPEGImageReaderSpi.NAMES, NativeJPEGImageReaderSpi.SUFFIXES,
            NativeJPEGImageReaderSpi.MIMES, writer.getName(), new Class[] { ImageOutputStream.class },
            new String[] { NativeJPEGImageReaderSpi.class.getName() }, false, null, null, null, null, false, null, null,
            null, null);
    }
    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        return checkCommonJpgRequirement(type);
    }
    
    public static boolean checkCommonJpgRequirement(ImageTypeSpecifier type) {
        ColorModel colorModel = type.getColorModel();

        if (colorModel instanceof IndexColorModel) {
            // No need to check further: writer converts to 8-8-8 RGB.
            return true;
        }

        SampleModel sampleModel = type.getSampleModel();

        // Ensure all channels have the same bit depth
        int bitDepth;
        if (colorModel != null) {
            int[] componentSize = colorModel.getComponentSize();
            bitDepth = componentSize[0];
            for (int i = 1; i < componentSize.length; i++) {
                if (componentSize[i] != bitDepth) {
                    return false;
                }
            }
        } else {
            int[] sampleSize = sampleModel.getSampleSize();
            bitDepth = sampleSize[0];
            for (int i = 1; i < sampleSize.length; i++) {
                if (sampleSize[i] != bitDepth) {
                    return false;
                }
            }
        }

        // Ensure bitDepth is no more than 16
        if (bitDepth > 16) {
            return false;
        }

        // Check number of bands.
        int numBands = sampleModel.getNumBands();
        return numBands == 1 || numBands == 3 || numBands == 4;
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated JPEG Image Writer (8/12/16 bits, IJG 6b based)";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new NativeJPEGImageWriter(this);
    }
}
