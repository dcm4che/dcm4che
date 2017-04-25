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

package org.dcm4che3.imageio.codec.jpeg;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class JPEGHeader {

    private final byte[] data;
    private final int[] offsets;

    public JPEGHeader(byte[] data, int lastMarker) {
        int n = 0;
        for (int offset = 0; (offset = nextMarker(data, offset)) != -1;) {
            n++;
            int marker = data[offset++] & 255;
            if (JPEG.isStandalone(marker))
                continue;
            if (offset+1 >= data.length)
                break;
            if (marker == lastMarker)
                break;
            offset += ByteUtils.bytesToUShortBE(data, offset);
        }
        this.data = data;
        this.offsets = new int[n];
        for (int i = 0, offset = 0; i < n; i++) {
            offsets[i] = (offset = nextMarker(data, offset)) ;
            if (!JPEG.isStandalone(offset++ & 255))
                offset += ByteUtils.bytesToUShortBE(data, offset);
        }
    }

    private static int nextMarker(byte[] data, int from) {
        for (int i = from+1; i < data.length; i++) {
            if (data[i-1] == -1 && data[i] != -1 && data[i] != 0) {
                return i;
            }
        }
        return -1;
    }

    public int offsetOf(int marker) {
        for (int i = 0; i < offsets.length; i++) {
            if (marker(i) == marker)
                return offsets[i];
        }
        return -1;
    }

    public int offsetSOF() {
        for (int i = 0; i < offsets.length; i++) {
            if (JPEG.isSOF(marker(i)))
                return offsets[i];
        }
        return -1;
    }

    public int offsetAfterAPP() {
        for (int i = 1; i < offsets.length; i++) {
            if (!JPEG.isAPP(marker(i)))
                return offsets[i];
        }
        return -1;
    }

    public int offset(int index) {
        return offsets[index];
    }

    public int marker(int index) {
        return data[offsets[index]] & 255;
    }

    public int numberOfMarkers() {
        return offsets.length;
    }

    /**
     * Return corresponding Image Pixel Description Macro Attributes
     * @param attrs target {@code Attributes} or {@code null}
     * @return Image Pixel Description Macro Attributes
     */
    public Attributes toAttributes(Attributes attrs) {
        int offsetSOF = offsetSOF();
        if (offsetSOF == -1)
            return null;

        if (attrs == null)
            attrs = new Attributes(10);

        int sof = data[offsetSOF] & 255;
        int p = data[offsetSOF+3] & 0xff;
        int y = ((data[offsetSOF+3 + 1] & 0xff) << 8)
                | (data[offsetSOF+3 + 2] & 0xff);
        int x = ((data[offsetSOF+3 + 3] & 0xff) << 8)
                | (data[offsetSOF+3 + 4] & 0xff);
        int nf = data[offsetSOF+3 + 5] & 0xff;
        attrs.setInt(Tag.SamplesPerPixel, VR.US, nf);
        if (nf == 3) {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, sof == JPEG.SOF0 ? "YBR_FULL_422" : "RGB");
            attrs.setInt(Tag.PlanarConfiguration, VR.US, 0);
        } else {
            attrs.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        }
        attrs.setInt(Tag.Rows, VR.US, y);
        attrs.setInt(Tag.Columns, VR.US, x);
        attrs.setInt(Tag.BitsAllocated, VR.US, p > 8 ? 16 : 8);
        attrs.setInt(Tag.BitsStored, VR.US, p);
        attrs.setInt(Tag.HighBit, VR.US, p - 1);
        attrs.setInt(Tag.PixelRepresentation, VR.US, 0);
        if (!(sof == JPEG.SOF3 || (sof == JPEG.SOF55 && ss() == 0)))
            attrs.setString(Tag.LossyImageCompression, VR.CS,  "01");
        return attrs;
    }

    public String getTransferSyntaxUID() {
        int sofOffset = offsetSOF();
        if (sofOffset == -1)
            return null;

        switch(data[sofOffset] & 255) {
            case JPEG.SOF0:
                return UID.JPEGBaseline1;
            case JPEG.SOF1:
                return UID.JPEGExtended24;
            case JPEG.SOF3:
                return ss() == 1 ? UID.JPEGLossless : UID.JPEGLosslessNonHierarchical14;
            case JPEG.SOF55:
                return ss() == 0 ? UID.JPEGLSLossless : UID.JPEGLSLossyNearLossless;
        }
        return null;
    }

    private int ss() {
        int offsetSOS = offsetOf(JPEG.SOS);
        return offsetSOS != -1 ? data[offsetSOS+6] & 255 : -1;
    }
}
