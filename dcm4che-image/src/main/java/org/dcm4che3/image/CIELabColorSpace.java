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
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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

package org.dcm4che3.image;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Mar 2020
 */
import java.awt.color.ColorSpace;

public class CIELabColorSpace extends ColorSpace {

    private static class LazyHolder {
        static final CIELabColorSpace INSTANCE = new CIELabColorSpace(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ));
    }

    private static final double D = 4.0 / 29.0;
    private static final double DELTA = 6.0 / 29.0;
    private static final double DELTA_3 = 216.0 / 24389.0;
    private static final double THREE_DELTA_2 = 108.0 / 841.0;

    private final ColorSpace CIEXYZ;

    CIELabColorSpace(ColorSpace ciexyz) {
        super(ColorSpace.TYPE_Lab, 3);
        CIEXYZ = ciexyz;
    }

    public static CIELabColorSpace getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public float getMaxValue(int component) {
        if ((component < 0) || (component > 2)) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
        return component == 0 ? 100.0f : 127.0f;
    }

    @Override
    public float getMinValue(int component) {
        if ((component < 0) || (component > 2)) {
            throw new IllegalArgumentException("Component index out of range: " + component);
        }
        return component == 0 ? 0.0f : -128.0f;
    }

    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        double l = f(colorvalue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (f(colorvalue[0]) - l);
        double b = 200.0 * (l - f(colorvalue[2]));
        return new float[] {(float) L, (float) a, (float) b};
    }

    @Override
    public float[] fromRGB(float[] rgbvalue) {
        float[] xyz = CIEXYZ.fromRGB(rgbvalue);
        return fromCIEXYZ(xyz);
    }


    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        double l = (colorvalue[0] + 16.0) / 116.0;
        double X = fInv(l + colorvalue[1] / 500.0);
        double Y = fInv(l);
        double Z = fInv(l - colorvalue[2] / 200.0);
        return new float[] {(float) X, (float) Y, (float) Z};
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        float[] xyz = toCIEXYZ(colorvalue);
        return CIEXYZ.toRGB(xyz);
    }

    private static double f(double t) {
        return t > DELTA_3 ? Math.cbrt(t) : t / THREE_DELTA_2 + D;
    }

    private static double fInv(double t) {
        return t > DELTA ? t * t * t : THREE_DELTA_2 * (t - D);
    }

    private Object readResolve() {
        return getInstance();
    }
}