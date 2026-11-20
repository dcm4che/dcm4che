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

/**
 * VOI windowing per PS3.3 C.11.2.1.2 (LINEAR) and C.11.2.1.3.2 (LINEAR_EXACT).
 *
 * @author Gunter Zeilinger &lt;gunterze@gmail.com&gt;
 */
public final class VOILUT {

    private VOILUT() {}

    public static VOILUTFunction parseFunction(String function) {
        if (function == null || function.isEmpty())
            return VOILUTFunction.LINEAR;
        try {
            return VOILUTFunction.valueOf(function.trim());
        } catch (IllegalArgumentException e) {
            return VOILUTFunction.LINEAR;
        }
    }

  /**
   * Apply modality LUT or rescale: output = m * SV + b.
   */
    public static double modalityValue(int storedValue, float rescaleSlope, float rescaleIntercept) {
        return rescaleSlope * storedValue + rescaleIntercept;
    }

  /**
   * Map a modality value to a display output in {@code [ymin, ymax]}.
   *
   * @param x modality value (after Modality LUT or rescale)
   */
    public static int apply(VOILUTFunction function, double x,
            float windowCenter, float windowWidth, int ymin, int ymax) {
        if (windowWidth <= 0)
            windowWidth = 1;
        switch (function) {
            case LINEAR_EXACT:
                return linearExact(x, windowCenter, windowWidth, ymin, ymax);
            case SIGMOID:
                return sigmoid(x, windowCenter, windowWidth, ymin, ymax);
            case LINEAR:
            default:
                return linear(x, windowCenter, windowWidth, ymin, ymax);
        }
    }

  /** PS3.3 C.11.2.1.2.1 default LINEAR. */
    public static int linear(double x, float c, float w, int ymin, int ymax) {
        if (w < 1)
            w = 1;
        double low = c - 0.5 - (w - 1) / 2.;
        double high = c - 0.5 + (w - 1) / 2.;
        if (x <= low)
            return ymin;
        if (x > high)
            return ymax;
        double y = ((x - (c - 0.5)) / (w - 1) + 0.5) * (ymax - ymin) + ymin;
        return clamp(y, ymin, ymax);
    }

  /** PS3.3 C.11.2.1.3.2 LINEAR_EXACT. */
    public static int linearExact(double x, float c, float w, int ymin, int ymax) {
        if (w <= 0)
            w = 1;
        double low = c - w / 2.;
        double high = c + w / 2.;
        if (x <= low)
            return ymin;
        if (x > high)
            return ymax;
        double y = ((x - c) / w + 0.5) * (ymax - ymin) + ymin;
        return clamp(y, ymin, ymax);
    }

  /** PS3.3 C.11.2.1.3.1 SIGMOID (Equation C.11-1). */
    public static int sigmoid(double x, float c, float w, int ymin, int ymax) {
        if (w <= 0)
            w = 1;
        double y = ymax / (1 + Math.exp(-4 * (x - c) / w));
        return clamp(y, ymin, ymax);
    }

    private static int clamp(double y, int ymin, int ymax) {
        return (int) Math.min(Math.max(Math.round(y), ymin), ymax);
    }
}
