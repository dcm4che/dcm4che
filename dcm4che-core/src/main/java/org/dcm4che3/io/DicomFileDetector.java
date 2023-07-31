/*
 * *** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2013-2021
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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.io;

import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Sep 2021
 */
public class DicomFileDetector extends FileTypeDetector {
    public static final String APPLICATION_DICOM = "application/dicom";

    @Override
    public String probeContentType(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            byte[] b134 = new byte[134];
            int rlen = StreamUtils.readAvailable(in, b134, 0, 134);
            return rlen >= 8 && (isPart10(b134, rlen) || isIVR_LE(b134, rlen) || isEVR(b134, rlen))
                    ? APPLICATION_DICOM
                    : null;
        }
    }

    private static boolean isPart10(byte[] b134, int rlen) {
        return rlen == 134
                && b134[128] == 'D'
                && b134[129] == 'I'
                && b134[130] == 'C'
                && b134[131] == 'M'
                && b134[132] == 2
                && b134[133] == 0;
    }

    private static boolean isIVR_LE(byte[] b134, int rlen) {
        int tag = ByteUtils.bytesToTagLE(b134, 0);
        int vlen = ByteUtils.bytesToIntLE(b134, 4);
        return TagUtils.isGroupLength(tag) ? vlen == 4
                : (ElementDictionary.getStandardElementDictionary().vrOf(tag) != VR.UN && (16 + vlen) <= rlen);
    }

    private static boolean isEVR(byte[] b134, int rlen) {
        int tagLE = ByteUtils.bytesToTagLE(b134, 0);
        int tagBE = ByteUtils.bytesToTagBE(b134, 0);
        VR vr = VR.valueOf(ByteUtils.bytesToVR(b134, 4));
        return vr != null && vr == ElementDictionary.getStandardElementDictionary().vrOf(
                tagLE >= 0 && tagLE < tagBE ? tagLE : tagBE);
    }
}