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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015
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

package org.dcm4che3.imageio.codec;

import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.util.StreamUtils;

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2015
 */
public class EncapsulatedPixelData extends ImageInputStreamImpl {

    private final DicomInputStream dis;
    private final byte[] basicOffsetTable;
    private final int frameStartWord;
    private int fragmStartWord;
    private int fragmPos;
    private int fragmLen;
    private boolean endOfFrame;
    private boolean nextFragment;

    public EncapsulatedPixelData(DicomInputStream dis) throws IOException {
        this.dis = dis;
        dis.readHeader();
        byte[] b = new byte[dis.length()];
        dis.readFully(b);
        basicOffsetTable = b;
        nextFragment = nextFragment();
        frameStartWord = fragmStartWord;
        endOfFrame = true;
    }

    private boolean nextFragment() throws IOException {
        if (!dis.readItemHeader())
            return false;
        fragmPos = 0;
        fragmLen = dis.length();
        fragmStartWord = (dis.read() << 8) | dis.read();
        return true;
    }

    @Override
    public int read() throws IOException {
        if (endOfFrame())
            return -1;

        switch (fragmPos++) {
            case 0:
                streamPos++;
                return (fragmStartWord >> 8) & 0xff;
            case 1:
                streamPos++;
                return fragmStartWord & 0xff;
        }
        int read = dis.read();
        streamPos++;
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = 0;
        while (fragmPos < 2 && len > 0) {
            int b0 = read();
            if (b0 == -1)
                return read > 0 ? read : -1;
            b[off++] = (byte) b0;
            len--;
            read++;
        }

        if (endOfFrame())
            return read > 0 ? read : -1;

        int read2 = dis.read(b, off, Math.min(len, fragmLen - fragmPos));
        if (read2 == -1)
            return read;

        streamPos += read2;
        return read + read2;
    }

    private boolean endOfFrame() throws IOException {
        if (endOfFrame)
            return true;

        if (fragmLen > fragmPos)
            return false;

        if (nextFragment = nextFragment()) {
            if (fragmStartWord != frameStartWord)
                return false;
        }
        endOfFrame = true;
        return true;
    }

    public boolean nextFrame() throws IOException {
        if (!endOfFrame)
            StreamUtils.skipFully(dis, fragmLen - fragmPos);
        if (nextFragment) {
            endOfFrame = false;
        }
        streamPos = 0;
        bitOffset = 0;
        return endOfFrame;
    }
}
