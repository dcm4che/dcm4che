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

import java.util.Locale;

import javax.imageio.ImageWriteParam;

/**
 * @author Nicolas Roduit
 * @since Aug 2018
 */
public class JPEGImageWriteParam extends ImageWriteParam {

    private static final String[] COMPRESSION_TYPES = {
        "BASELINE", // JPEG Baseline: Imgcodecs.JPEG_baseline (0)
        "EXTENDED", // JPEG Extended sequential: Imgcodecs.JPEG_sequential (1)
        "SPECTRAL", // JPEG Spectral Selection: Imgcodecs.JPEG_spectralSelection (2) (Retired from DICOM)
        "PROGRESSIVE", // JPEG Full Progression: Imgcodecs.JPEG_progressive (3) (Retired from DICOM)
        "LOSSLESS-1", // JPEG Lossless, Selection Value 1: Imgcodecs.JPEG_lossless (4), prediction (1)
        "LOSSLESS-2", // JPEG Lossless, Selection Value 2: Imgcodecs.JPEG_lossless (4), prediction (2)
        "LOSSLESS-3", // JPEG Lossless, Selection Value 3: Imgcodecs.JPEG_lossless (4), prediction (3)
        "LOSSLESS-4", // JPEG Lossless, Selection Value 4: Imgcodecs.JPEG_lossless (4), prediction (4)
        "LOSSLESS-5", // JPEG Lossless, Selection Value 5: Imgcodecs.JPEG_lossless (4), prediction (5)
        "LOSSLESS-6", // JPEG Lossless, Selection Value 6: Imgcodecs.JPEG_lossless (4), prediction (6)
        "LOSSLESS-7", // JPEG Lossless, Selection Value 7: Imgcodecs.JPEG_lossless (4), prediction (7)
    };

    /** JPEG lossless point transform (0..15, default: 0)*/
    private int pointTransform;

    public JPEGImageWriteParam(Locale locale) {
        super(locale);
        super.canWriteCompressed = true;
        super.compressionMode = MODE_EXPLICIT;
        super.compressionType = "BASELINE";
        super.compressionTypes = COMPRESSION_TYPES;
        super.compressionQuality = 0.75F;
        this.pointTransform = 0;
    }

    public int getMode() {
        switch(compressionType.charAt(0)) {
            case 'B':
                return 0;
            case 'E':
                return 1;
            case 'S':
                return 2;
            case 'P':
                return 3;
        }
        return 4;
    }

    public int getPrediction() {
        return isCompressionLossless() ? (compressionType.charAt(9) - '0') : 0;
    }

    public int getPointTransform() {
        return pointTransform;
    }

    public void setPointTransform(int pointTransform) {
        this.pointTransform = pointTransform;
    }

    @Override
    public boolean isCompressionLossless() {
        return compressionType.charAt(0) == 'L';
    }
}
