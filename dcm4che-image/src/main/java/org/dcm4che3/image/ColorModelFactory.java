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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.util.Arrays;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dcm4che3.data.Attributes;

/**
 * Creates color models for various DICOM Attribues types, such as 
 * monochrome, palette colour, RGB etc.
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Bill Wallace <wayfarer3130@gmail.com>
 */
public class ColorModelFactory {
    private static final Logger verifyLog = LoggerFactory.getLogger("verify.dicom");
    private static final Logger log = LoggerFactory.getLogger(ColorModelFactory.class);
    
    public static final String MONOCHROME1 = "MONOCHROME1";
    public static final String MONOCHROME2 = "MONOCHROME2";
    public static final String MONOCHROME_INVALID = "MONOCHROME";
    public static final String PALETTE_COLOR = "PALETTE COLOR";
    public static final String RGB = "RGB";
    public static final String YBR_FULL = "YBR_FULL";
    public static final String YBR_FULL_422 = "YBR_FULL_422";
    public static final String YBR_PARTIAL_422 = "YBR_PARTIAL_422";
    public static final String YBR_PARTIAL_420 = "YBR_PARTIAL_420";

    public static final String YBR_RCT = "YBR_RCT";

    public static final String YBR_ICT = "YBR_ICT";

    
    public static ColorModel createMonochromeColorModel(int bits, int dataType) {
        return new ComponentColorModel(
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new int[] { bits },
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }

    public static ColorModel createPaletteColorModel(int bits, int dataType,
            Attributes ds) {
        return new PaletteColorModel(bits, dataType, createRGBColorSpace(ds), ds);
    }

    public static ColorModel createRGBColorModel(int bits, int dataType,
            Attributes ds) {
        return new ComponentColorModel(
                createRGBColorSpace(ds),
                new int[] { bits, bits, bits },
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }


    public static ColorModel createYBRFullColorModel(int bits, int dataType,
            Attributes ds) {
        return new ComponentColorModel(
                new YBRColorSpace(createRGBColorSpace(ds),  YBR.FULL),
                new int[] { bits, bits, bits },
                false, // hasAlpha
                false, // isAlphaPremultiplied
                Transparency.OPAQUE,
                dataType);
    }

    public static ColorModel createYBRColorModel(int bits, int dataType,
            Attributes ds, YBR ybr, ColorSubsampling subsampling) {
        return new SampledComponentColorModel(
                new YBRColorSpace(createRGBColorSpace(ds), ybr),
                subsampling);
    }

    private static ColorSpace createRGBColorSpace(Attributes ds) {
        return createRGBColorSpace(ds.getSafeBytes(Tag.ICCProfile));
    }

    private static ColorSpace createRGBColorSpace(byte[] iccProfile) {
        if (iccProfile != null)
            return new ICC_ColorSpace(ICC_Profile.getInstance(iccProfile));

        return ColorSpace.getInstance(ColorSpace.CS_sRGB);
    }

    /** Creates a color model appropriate for the given transfer syntax/
     * DICOM attributes, as a single one stop operation.  
     * Throws an exception for invalid DICOM states.
     */
    public static ColorModel createColorModel(String tsuid, Attributes attr) {
        int samples = attr.getInt(Tag.SamplesPerPixel, 1);
        if (samples != 1 && samples != 3)  {
            throw new IllegalArgumentException(
                    "Unsupported Samples per Pixel: " + samples);
        }
        int allocated = attr.getInt(Tag.BitsAllocated, 8);
        int stored = attr.getInt(Tag.BitsStored, allocated);
        int dataType = allocated <= 8 ? DataBuffer.TYPE_BYTE
                : DataBuffer.TYPE_USHORT;
        int[] bits = new int[samples];
        Arrays.fill(bits, stored);
        ColorSpace cs = null;
        String pmi = null;
        if (samples == 1) {
            pmi = getMonochromePhotometricInterpretation(attr);

            if (pmi.equals(MONOCHROME2) || pmi.equals(MONOCHROME1)) {
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            } else if (pmi.equals(PALETTE_COLOR)) {
                return createPaletteColorModel(allocated, dataType, attr);               
            }
        } else if (samples == 3) {
            pmi = attr.getString(Tag.PhotometricInterpretation, RGB);
            YBR ybr = pmi.indexOf("FULL")>=0 ? YBR.FULL : YBR.PARTIAL;
            if(isDecodingRGB(tsuid) ) {
                return new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
            }
            if (pmi.equals(RGB) || pmi.equals(YBR_ICT) || pmi.equals(YBR_RCT)  ) {
                log.debug("Color space is RGB.");
                return createRGBColorModel(8, dataType, attr);
            }
            if (pmi.equals(YBR_FULL) ) {
                log.debug("Color space is YBR full");
                return createYBRFullColorModel(allocated, dataType, attr);
            }
            if( pmi.endsWith("422") ) {
                return createYBRColorModel(allocated, dataType, attr, ybr, ColorSubsampling.YBR_XXX_422);
            }
            if( pmi.endsWith("420") ) {
                return createYBRColorModel(allocated, dataType, attr, ybr, ColorSubsampling.YBR_XXX_420);
            }
        } else {
            throw new IllegalArgumentException(
                    "Unsupported Samples per Pixel: " + samples);            
        }
        if (cs == null) {
            throw new IllegalArgumentException(
                    "Unsupported Photometric Interpretation: " + pmi +
                    " with Samples per Pixel: " + samples);            
            
        }
        return new ComponentColorModel(cs, bits, false, false,
                Transparency.OPAQUE, dataType);
    }

    /** Indicate if the given tsuid decodes directly to RGB when a colour model */
    public static boolean isDecodingRGB(String tsuid) {
        return UID.JPEG2000.equals(tsuid) || UID.JPEGBaseline1.equals(tsuid);
    }

    /**
     * Read the photometric interpetation from the DicomObject, and correct it if necessary.
     */
    private static String getMonochromePhotometricInterpretation(Attributes ds) {
        String pmi = ds.getString(Tag.PhotometricInterpretation, MONOCHROME2);

        if( pmi.equalsIgnoreCase(MONOCHROME_INVALID)) {
            verifyLog.debug("Instance {} has an invalid and ambiguous Photometric Interpretation {}.  Assuming MONOCHROME2",ds.getString(Tag.SOPInstanceUID),pmi);
            pmi = MONOCHROME2;
            ds.setString(Tag.PhotometricInterpretation, VR.CS,pmi);
        }

        return pmi;
    }

    /** Indicate if the DICOM specifies monochrome photometric interpretation */
    public static boolean isMonochrome(Attributes attr) {
        return attr.getInt(Tag.SamplesPerPixel, 1) == 1
                    && !isPaletteColor(attr);
    }

    /** Indicate if this is a palette colour image */
    public static boolean isPaletteColor(Attributes ds) {
        return PALETTE_COLOR.equals(
                ds.getString(Tag.PhotometricInterpretation));
    }

}
