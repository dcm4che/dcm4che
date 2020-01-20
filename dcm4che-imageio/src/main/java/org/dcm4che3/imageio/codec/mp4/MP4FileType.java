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
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
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

package org.dcm4che3.imageio.codec.mp4;

import java.nio.ByteBuffer;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Jan 2020
 */
public class MP4FileType {
    public static final int qt = 0x71742020;
    public static final int isom = 0x69736f6d;
    public static final MP4FileType ISOM_QT = new MP4FileType(isom, 0, isom, qt);

    private final int[] brands;

    public MP4FileType(int majorBrand, int minorVersion, int... compatibleBrands) {
        this.brands = new int[2 + compatibleBrands.length];
        brands[0] = majorBrand;
        brands[1] = minorVersion;
        System.arraycopy(compatibleBrands, 0, brands, 2, compatibleBrands.length);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append4CC(sb.append("ftyp["), brands[0]);
        sb.append('.').append(brands[1]);
        for (int i = 2; i < brands.length; i++) {
            append4CC(sb.append(", "), brands[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    public byte[] toBytes() {
        ByteBuffer bb = ByteBuffer.allocate(size());
        bb.putInt(bb.remaining());
        bb.putInt(0x66747970);
        for (int brand : brands) {
            bb.putInt(brand);
        }
        return bb.array();
    }

    private static void append4CC(StringBuilder sb, int brand) {
        sb.append((char)((brand >>> 24) & 0xFF));
        sb.append((char)((brand >>> 16) & 0xFF));
        sb.append((char)((brand >>> 8) & 0xFF));
        sb.append((char)((brand >>> 0) & 0xFF));
    }

    public int size() {
        return (2 + brands.length) * 4;
    }

    public int majorBrand() {
        return brands[0];
    }

    public int minorVersion() {
        return brands[1];
    }

    public int[] compatibleBrands() {
        int[] compatibleBrands = new int[brands.length - 2];
        System.arraycopy(brands, 2, brands, 0, compatibleBrands.length);
        return compatibleBrands;
    }
}
