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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;

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
    JPEG_LOSSLESS(true, true, true, 16, 0),
    JPEG_LS(true, true, true, 16, 0),
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

    public boolean adjustBitsStoredTo12(Attributes attrs) {
        if (maxBitsStored == 12) {
            int bitsStored = attrs.getInt(Tag.BitsStored, 8);
            if (bitsStored > 8 && bitsStored < 12) {
                attrs.setInt(Tag.BitsStored, VR.US, bitsStored = 12);
                attrs.setInt(Tag.HighBit, VR.US, 11);
                return true;
            }
        }
        return false;
    }

    public static TransferSyntaxType forUID(String uid) {
        switch(uid) {
            case UID.JPEGBaseline1:
                return JPEG_BASELINE;
            case UID.JPEGExtended24:
                return JPEG_EXTENDED;
            case UID.JPEGSpectralSelectionNonHierarchical68Retired:
                return JPEG_SPECTRAL;
            case UID.JPEGFullProgressionNonHierarchical1012Retired:
                return JPEG_PROGRESSIVE;
            case UID.JPEGLosslessNonHierarchical14:
            case UID.JPEGLossless:
                return JPEG_LOSSLESS;
            case UID.JPEGLSLossless:
            case UID.JPEGLSLossyNearLossless:
                return JPEG_LS;
            case UID.JPEG2000LosslessOnly:
            case UID.JPEG2000:
            case UID.JPEG2000Part2MultiComponentLosslessOnly:
            case UID.JPEG2000Part2MultiComponent:
                return JPEG_2000;
            case UID.JPIPReferenced:
            case UID.JPIPReferencedDeflate:
                return JPIP;
            case UID.MPEG2:
            case UID.MPEG2MainProfileHighLevel:
            case UID.MPEG4AVCH264HighProfileLevel41:
            case UID.MPEG4AVCH264BDCompatibleHighProfileLevel41:
            case UID.MPEG4AVCH264HighProfileLevel42For2DVideo:
            case UID.MPEG4AVCH264HighProfileLevel42For3DVideo:
            case UID.MPEG4AVCH264StereoHighProfileLevel42:
            case UID.HEVCH265MainProfileLevel51:
            case UID.HEVCH265Main10ProfileLevel51:
                return MPEG;
            case UID.RLELossless:
                return RLE;
        }
        return NATIVE;
    }

    public static boolean isLossyCompression(String uid) {
        switch(uid) {
            case UID.JPEGBaseline1:
            case UID.JPEGExtended24:
            case UID.JPEGSpectralSelectionNonHierarchical68Retired:
            case UID.JPEGFullProgressionNonHierarchical1012Retired:
            case UID.JPEGLSLossyNearLossless:
            case UID.JPEG2000:
            case UID.JPEG2000Part2MultiComponent:
            case UID.MPEG2:
            case UID.MPEG2MainProfileHighLevel:
            case UID.MPEG4AVCH264HighProfileLevel41:
            case UID.MPEG4AVCH264BDCompatibleHighProfileLevel41:
            case UID.MPEG4AVCH264HighProfileLevel42For2DVideo:
            case UID.MPEG4AVCH264HighProfileLevel42For3DVideo:
            case UID.MPEG4AVCH264StereoHighProfileLevel42:
            case UID.HEVCH265MainProfileLevel51:
            case UID.HEVCH265Main10ProfileLevel51:
                return true;
        }
        return false;
    }

    public static boolean isYBRCompression(String uid) {
        switch(uid) {
            case UID.JPEGBaseline1:
            case UID.JPEGExtended24:
            case UID.JPEGSpectralSelectionNonHierarchical68Retired:
            case UID.JPEGFullProgressionNonHierarchical1012Retired:
            case UID.JPEG2000LosslessOnly:
            case UID.JPEG2000:
                return true;
        }
        return false;
    }
}
