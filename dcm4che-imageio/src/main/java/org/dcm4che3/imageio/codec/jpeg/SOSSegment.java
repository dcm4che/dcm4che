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
public class SOSSegment {

    private final byte[] data;
    private final int offset;
    private final int numComponents;

    public SOSSegment(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
        this.numComponents = data[offset+3] & 255;
        getAl();
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

    public int getNumComponents() {
        return numComponents;
    }

    public int getComponentID(int index) {
        return data[offset+4+index*2] & 255;
    }

    public int getTa(int index) {
        return (data[offset+5+index*2]>>4) & 15;
    }

    public int getTd(int index) {
        return (data[offset+5+index*2]) & 15;
    }

    public int getSs() {
        return data[offset+4+numComponents*2] & 255;
    }

    public int getSe() {
        return data[offset+5+numComponents*2] & 255;
    }

    public int getAh() {
        return (data[offset+6+numComponents*2]>>4) & 15;
    }

    public int getAl() {
        return (data[offset+6+numComponents*2]) & 15;
    }

    public int getNear() {
        return getSs();
    }

    public int getILV() {
        return getSe();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SOS=[Ls=").append(getHeaderLength())
          .append(", Ns=").append(numComponents);
        for (int i = 0; i < numComponents; i++) {
            sb.append(", C").append(i+1).append('=').append(getComponentID(i))
              .append(", Td").append(i+1).append('=').append(getTd(i))
              .append(", Ta").append(i+1).append('=').append(getTa(i));
        }
        sb.append(", Ss=").append(getSs())
          .append(", Se=").append(getSe())
          .append(", Ah=").append(getAh())
          .append(", Al=").append(getAl())
          .append(']');
        return sb.toString();
    }
}
