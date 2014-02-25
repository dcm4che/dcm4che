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
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.Attributes;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ColorModelFactory {

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

}
