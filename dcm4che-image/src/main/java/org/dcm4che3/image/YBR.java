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

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public enum YBR {
    FULL {
        @Override
        public float[] toRGB(float[] ybr) {
            return convert(ybr, FROM_YBR_FULL);
        }

        @Override
        public float[] fromRGB(float[] rgb) {
            return convert(rgb, TO_YBR_FULL);
        }
    },
    PARTIAL {
        @Override
        public float[] toRGB(float[] ybr) {
            return convert(ybr, FROM_YBR_PARTIAL);
        }

        @Override
        public float[] fromRGB(float[] rgb) {
            return convert(rgb, TO_YBR_PARTIAL);
        }
    };

    private static double[] TO_YBR_FULL = {
        0.2990, 0.5870, 0.1140, 0.0,
        -0.1687, -0.3313, 0.5, 0.5,
        0.5, -0.4187, -0.0813, 0.5
    };
    
    private static double[] TO_YBR_PARTIAL = {
        0.2568, 0.5041, 0.0979, 0.0625,
        -0.1482, -0.2910, 0.4392, 0.5,
        0.4392, -0.3678, -0.0714, 0.5
    };
    
    private static final double[] FROM_YBR_FULL = {
        1.0, -3.681999032610751E-5, 1.4019875769352639, -0.7009753784724688, 
        1.0, -0.34411328131331737, -0.7141038211151132, 0.5291085512142153, 
        1.0, 1.7719781167370596, -1.345834129159976E-4, -0.8859217666620718, 
    };

    private static final double[] FROM_YBR_PARTIAL = {
        1.1644154634373545, -9.503599204778129E-5, 1.5960018776303868, -0.8707293872840042, 
        1.1644154634373545, -0.39172456367367336, -0.8130133682767554, 0.5295929995103797, 
        1.1644154634373545, 2.017290682233469, -1.3527300480981362E-4, -1.0813536710791642, 
    };

    public abstract float[] toRGB(float[] ybr);

    public abstract float[] fromRGB(float[] rgb);

    private static float[] convert(float[] in, double[] a) {
        return new float[] {
                (float) Math.max(0.0, Math.min(1.0,
                        a[0] * in[0] 
                      + a[1] * in[1]
                      + a[2] * in[2]
                      + a[3])),
                (float) Math.max(0.0, Math.min(1.0,
                        a[4] * in[0] 
                      + a[5] * in[1]
                      + a[6] * in[2]
                      + a[7])),
                (float) Math.max(0.0, Math.min(1.0,
                        a[8] * in[0] 
                      + a[9] * in[1]
                      + a[10] * in[2]
                      + a[11]))};
    }

//    public static void main(String[] args) {
//        out("FROM_YBR_FULL", invert(TO_YBR_FULL));
//        out("FROM_YBR_PARTIAL", invert(TO_YBR_PARTIAL));
//    }
//
//    private static void out(String label, double[] a) {
//        StringBuffer sb = new StringBuffer();
//        sb.append("\n    private static final double[] ");
//        sb.append(label);
//        sb.append(" = {");
//        for (int i = 0; i < a.length; i++) {
//            if (i % 4 == 0)
//                sb.append("\n        ");
//            sb.append(a[i]);
//            sb.append(", ");
//        }
//        sb.append("\n    };");
//        System.out.println(sb.toString());
//    }
//
//    private static double[] invert(double[] a) {
//        double[] b = new double[12];
//        double det = a[0]*a[5]*a[10] + a[1]*a[6]*a[8] + a[2]*a[4]*a[9]
//                   - a[2]*a[5]*a[8] - a[1]*a[4]*a[10] - a[0]*a[6]*a[9];
//        b[0] = (a[5]*a[10] - a[6]*a[9]) / det;
//        b[1] = (a[2]*a[9] - a[1]*a[10]) / det;
//        b[2] = (a[1]*a[6] - a[2]*a[5]) / det;
//        b[3] = (a[2]*a[5]*a[11] + a[1]*a[7]*a[10] + a[3]*a[6]*a[9]
//                  - a[3]*a[5]*a[10] - a[1]*a[6]*a[11] - a[2]*a[7]*a[9]) / det;
//        b[4] = (a[6]*a[8] - a[4]*a[10]) / det;
//        b[5] = (a[0]*a[10] - a[2]*a[8]) / det;
//        b[6] = (a[2]*a[4] - a[0]*a[6]) / det;
//        b[7] = (a[2]*a[7]*a[8] + a[3]*a[4]*a[10] + a[0]*a[6]*a[11]
//                  - a[0]*a[7]*a[10] - a[3]*a[6]*a[8] - a[2]*a[4]*a[11]) / det;
//        b[8] = (a[4]*a[9] - a[5]*a[8]) / det;
//        b[9] = (a[1]*a[8] - a[0]*a[9]) / det;
//        b[10] = (a[0]*a[5] - a[1]*a[4]) / det;
//        b[11] = (a[3]*a[5]*a[8] + a[1]*a[4]*a[11] + a[0]*a[7]*a[9]
//                   - a[0]*a[5]*a[11] - a[1]*a[7]*a[8] - a[3]*a[4]*a[9]) / det;
//        return b;
//    }

}
