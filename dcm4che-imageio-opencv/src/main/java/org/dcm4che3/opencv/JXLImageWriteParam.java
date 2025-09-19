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
 * @since 2025
 */
public class JXLImageWriteParam extends ImageWriteParam {

    private static final String[] COMPRESSION_TYPES = {
        "LOSSLESS",
        "LOSSY"
    };

    /** JXL compression effort (1-9, default: 7) - Higher values are slower but produce smaller files */
    private int effort;
  /** JXL  decoding speed (0-4, default: 0 lowest to decode, best quality/density) */
    private int decodingSpeed = 0;


    public JXLImageWriteParam(Locale locale) {
        super(locale);
        super.canWriteCompressed = true;
        super.compressionMode = MODE_EXPLICIT;
        super.compressionType = "LOSSLESS";
        super.compressionTypes = COMPRESSION_TYPES;
        super.compressionQuality = 0.90F; // JXL default to high quality
        this.effort = 7;

    }

    /**
     * Returns the compression effort level (1-9).
     * Higher values produce smaller files but take longer to encode.
     * 
     * @return the effort level
     */
    public int getEffort() {
        return effort;
    }

    /**
     * Sets the compression effort level (1-9).
     * 
     * @param effort the effort level (1-9)
     * @throws IllegalArgumentException if effort is not in range [1,9]
     */
    public void setEffort(int effort) {
        if (effort < 1 || effort > 9) {
            throw new IllegalArgumentException("Effort must be between 1 and 9, got: " + effort);
        }
        this.effort = effort;
    }

    @Override
    public boolean isCompressionLossless() {
        return "LOSSLESS".equals(compressionType);
    }

  /**
     * Gets the effective quality value based on compression type.
     * For quality-based compression, returns compressionQuality.
     * For distance-based compression, converts distance to approximate quality.
     * For lossless compression, returns 1.0.
     *
     * @return the effective quality value (0.0-1.0)
     */
    public float getEffectiveQuality() {
      if (compressionType.equals("LOSSLESS")) {
        return 1.0f;
      }
      return compressionQuality;
    }

    /**
     * Sets compression parameters for lossless encoding.
     */
    public void setLossless() {
        compressionType = "LOSSLESS";
    }

    /**
     * Sets compression parameters for quality-based lossy encoding.
     *
     * @param quality the quality value (0.0-1.0)
     */
    public void setCompressionQuality(float quality) {
        quality = Math.max(0.0f, Math.min(1.0f, quality));
        super.setCompressionQuality(quality);
    }

    public int getDecodingSpeed() {
        return decodingSpeed;
      }

      /**
       * Sets the decoding speed (0-4).
       * Higher values produce faster decoding but lower quality/density.
       *
       * @param decodingSpeed the decoding speed (0-4)
       * @throws IllegalArgumentException if decodingSpeed is not in range [0,4]
       */
      public void setDecodingSpeed(int decodingSpeed) {
        if (decodingSpeed < 0 || decodingSpeed > 4) {
            throw new IllegalArgumentException("Decoding speed must be between 0 and 4, got: " + decodingSpeed);
        }
        this.decodingSpeed = decodingSpeed;
      }
}
