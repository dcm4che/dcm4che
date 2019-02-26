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

package org.dcm4che3.imageio.codec;

import java.util.HashMap;

import org.dcm4che3.data.UID;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public enum TransferSyntaxType {
    NATIVE(false, false, true, 16, 0),
    JPEG_BASELINE(true, true, false, 8, 0),
    JPEG_EXTENDED(true, true, false, 12, 0),
    JPEG_SPECTRAL(true, true, false, 12, 0),
    JPEG_PROGRESSIVE(true, true, false, 12, 0),
    JPEG_LOSSLESS(true, true, false, 16, 0),
    JPEG_2000(true, true, true, 16, 0),
    RLE(true, false, false, 16, 1),
    JPIP(false, false, false, 16, 0),
    MPEG(true, false, false, 8, 0);

    private final boolean pixeldataEncapsulated;
    private final boolean frameSpanMultipleFragments;
    private final boolean encodeSigned;
    private final int maxBitsStored;
    private final int planarConfiguration;

    TransferSyntaxType(boolean pixeldataEncapsulated, boolean frameSpanMultipleFragments, boolean encodeSigned,
            int maxBitsStored, int planarConfiguration) {
        this.pixeldataEncapsulated = pixeldataEncapsulated;
        this.frameSpanMultipleFragments = frameSpanMultipleFragments;
        this.encodeSigned = encodeSigned;
        this.maxBitsStored = maxBitsStored;
        this.planarConfiguration = planarConfiguration;
    }

    public boolean isPixeldataEncapsulated() {
        return pixeldataEncapsulated;
    }

    public boolean canEncodeSigned() {
        return encodeSigned;
    }

    public boolean mayFrameSpanMultipleFragments() {
        return frameSpanMultipleFragments;
    }

    public int getPlanarConfiguration() {
        return planarConfiguration;
    }

    public int getMaxBitsStored() {
        return maxBitsStored;
    }

    private static final HashMap<String, TransferSyntaxType> map =
            new HashMap<String, TransferSyntaxType>();
    static {
        map.put(UID.ImplicitVRLittleEndian, NATIVE);
        map.put(UID.ExplicitVRLittleEndian, NATIVE);
        map.put(UID.DeflatedExplicitVRLittleEndian, NATIVE);
        map.put(UID.ExplicitVRBigEndianRetired, NATIVE);
        map.put(UID.JPEGBaseline1, JPEG_BASELINE);
        map.put(UID.JPEGExtended24, JPEG_EXTENDED);
        map.put(UID.JPEGSpectralSelectionNonHierarchical68Retired, JPEG_SPECTRAL);
        map.put(UID.JPEGFullProgressionNonHierarchical1012Retired, JPEG_PROGRESSIVE);
        map.put(UID.JPEGLosslessNonHierarchical14, JPEG_LOSSLESS);
        map.put(UID.JPEGLossless, JPEG_LOSSLESS);
        map.put(UID.JPEGLSLossless, JPEG_LOSSLESS);
        map.put(UID.JPEGLSLossyNearLossless, JPEG_LOSSLESS);
        map.put(UID.JPEG2000LosslessOnly, JPEG_2000);
        map.put(UID.JPEG2000, JPEG_2000);
        map.put(UID.JPEG2000Part2MultiComponentLosslessOnly, JPEG_2000);
        map.put(UID.JPEG2000Part2MultiComponent, JPEG_2000);
        map.put(UID.JPIPReferenced, JPIP);
        map.put(UID.JPIPReferencedDeflate, JPIP);
        map.put(UID.MPEG2, MPEG);
        map.put(UID.MPEG2MainProfileHighLevel, MPEG);
        map.put(UID.MPEG4AVCH264HighProfileLevel41, MPEG);
        map.put(UID.MPEG4AVCH264BDCompatibleHighProfileLevel41, MPEG);
        map.put(UID.RLELossless, RLE);
    }

    public static TransferSyntaxType forUID(String uid) {
        return map.get(uid);
    }
}
