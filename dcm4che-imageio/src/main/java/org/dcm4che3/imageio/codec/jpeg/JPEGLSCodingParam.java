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

package org.dcm4che3.imageio.codec.jpeg;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JPEGLSCodingParam {

    private int offset;
    private final int maxVal;
    private final int t1;
    private final int t2;
    private final int t3;
    private final int reset;

    public JPEGLSCodingParam(int maxVal, int t1, int t2, int t3, int reset) {
        super();
        this.maxVal = maxVal;
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.reset = reset;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public final int getMaxVal() {
        return maxVal;
    }

    public final int getT1() {
        return t1;
    }

    public final int getT2() {
        return t2;
    }

    public final int getT3() {
        return t3;
    }

    public final int getReset() {
        return reset;
    }

    public byte[] getBytes() {
        return new byte[] {
                -1, (byte) JPEG.LSE, 0, 13, 1,
                (byte) (maxVal >> 8), (byte) (maxVal),
                (byte) (t1 >> 8), (byte) (t1),
                (byte) (t2 >> 8), (byte) (t2),
                (byte) (t3 >> 8), (byte) (t3),
                (byte) (reset >> 8), (byte) (reset) };
    }

    private static JPEGLSCodingParam getDefaultJPEGLSEncodingParam(
            int maxVal, int clampedMaxVal, int near) {
        int factor = (clampedMaxVal + 128) >> 8;
        int t1 = factor + 2 + 3 * near;
        if (t1 > maxVal || t1 < near+1)
            t1 = near+1;
        int t2 = factor * 4 + 3 + 5 * near;
        if (t2 > maxVal || t2 < t1)
            t2 = t1;
        int t3 = factor * 17 + 4 + 7 * near;
        if (t3 > maxVal || t3 < t2)
            t3 = t2;
        return new JPEGLSCodingParam(maxVal, t1, t2, t3, 64);
    }

    public static JPEGLSCodingParam getDefaultJPEGLSCodingParam(int p, int near) {
        int maxVal = (1<<p)-1;
        return getDefaultJPEGLSEncodingParam(maxVal, Math.min(maxVal, 4095), near);
    }

    public static JPEGLSCodingParam getJAIJPEGLSCodingParam(int p) {
        int maxVal = (1<<p)-1;
        return getDefaultJPEGLSEncodingParam(maxVal, maxVal, 0);
    }

    @Override
    public String toString() {
        return "JPEGLSCodingParam[MAXVAL=" + maxVal
                + ", T1=" + t1
                + ", T2=" + t2
                + ", T3=" + t3
                + ", RESET=" + reset
                + "]";
    }

}
