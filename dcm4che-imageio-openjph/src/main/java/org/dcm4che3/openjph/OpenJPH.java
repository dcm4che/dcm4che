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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2025
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

package org.dcm4che3.openjph;

/**
 * JNI bridge to the OpenJPH HTJ2K encoding library.
 *
 * <p>The native method {@link #encode} wraps OpenJPH's {@code ojph::codestream} API.
 * Raw pixel data (pixel-interleaved for multi-component images) is passed in,
 * and a fully-formed HTJ2K codestream is returned.</p>
 *
 * @since Feb 2025
 */
public final class OpenJPH {

    /** Progression order: Layer-Resolution-Component-Position */
    public static final int PROGRESSION_LRCP = 0;
    /** Progression order: Resolution-Layer-Component-Position */
    public static final int PROGRESSION_RLCP = 1;
    /** Progression order: Resolution-Position-Component-Layer */
    public static final int PROGRESSION_RPCL = 2;
    /** Progression order: Position-Component-Resolution-Layer */
    public static final int PROGRESSION_PCRL = 3;
    /** Progression order: Component-Position-Resolution-Layer */
    public static final int PROGRESSION_CPRL = 4;

    static {
        OpenJPHNativeLoader.load();
    }

    private OpenJPH() {}

    /**
     * Encode raw pixel data to an HTJ2K codestream.
     *
     * @param rawPixelData pixel data in pixel-interleaved layout (R0G0B0 R1G1B1...)
     *                     For 16-bit data, stored as little-endian byte pairs.
     * @param width image width in pixels
     * @param height image height in pixels
     * @param components number of color components (1 for mono, 3 for RGB)
     * @param bitsPerSample bits per sample (8, 12, 16, etc.)
     * @param isSigned true if pixel values are signed
     * @param reversible true for lossless (reversible 5/3 wavelet), false for lossy (irreversible 9/7)
     * @param compressionRatio lossy compression ratio (e.g. 10 for 10:1); ignored if reversible is true
     * @param progressionOrder progression order (use PROGRESSION_* constants)
     * @param decompositions number of DWT decomposition levels (default 5)
     * @return the encoded HTJ2K codestream bytes
     * @throws OpenJPHException if encoding fails
     */
    public static native byte[] encode(
            byte[] rawPixelData,
            int width,
            int height,
            int components,
            int bitsPerSample,
            boolean isSigned,
            boolean reversible,
            float compressionRatio,
            int progressionOrder,
            int decompositions) throws OpenJPHException;
}
