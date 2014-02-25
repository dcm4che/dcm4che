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
            if ((data[offsets[i]] & 255) == marker)
                return offsets[i];
        }
        return -1;
    }

    public int numberOfMarkers() {
        return offsets.length;
    }
}
