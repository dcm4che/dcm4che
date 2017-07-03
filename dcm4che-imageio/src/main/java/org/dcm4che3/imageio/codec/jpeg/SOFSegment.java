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

import org.dcm4che3.util.ByteUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class SOFSegment {

    private final byte[] data;
    private final int offset;
    private final int numComponents;

    public SOFSegment(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.numComponents = data[offset+8] & 255;
        getQTableSelector(numComponents-1);
    }

    public int offset() {
        return offset;
    }

    public int getMarker() {
        return data[offset] & 255;
    }

    public int getHeaderLength() {
        return ByteUtils.bytesToUShortBE(data, offset+1);
    }

    public int getPrecision() {
        return data[offset+3] & 255;
    }

    public int getY() {
        return ByteUtils.bytesToUShortBE(data, offset+4);
    }

    public int getX() {
        return ByteUtils.bytesToUShortBE(data, offset+6);
    }

    public int getNumComponents() {
        return numComponents;
    }

    public int getComponentID(int index) {
        return data[offset+9+index*3] & 255;
    }

    public int getXSubsampling(int index) {
        return (data[offset+10+index*3]>>4) & 15;
    }

    public int getYSubsampling(int index) {
        return (data[offset+10+index*3]) & 15;
    }

    public int getQTableSelector(int index) {
        return data[offset+11+index*3] & 255;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SOF").append(getMarker()-0xC0)
          .append("[Lf=").append(getHeaderLength())
          .append(", P=").append(getPrecision())
          .append(", Y=").append(getY())
          .append(", X=").append(getX())
          .append(", Nf=").append(numComponents);
        for (int i = 0; i < numComponents; i++) {
            sb.append(", C").append(i+1).append('=').append(getComponentID(i))
              .append(", H").append(i+1).append('=').append(getXSubsampling(i))
              .append(", V").append(i+1).append('=').append(getYSubsampling(i))
              .append(", Tq").append(i+1).append('=').append(getQTableSelector(i));
        }
        sb.append(']');
        return sb.toString();
    }
}
