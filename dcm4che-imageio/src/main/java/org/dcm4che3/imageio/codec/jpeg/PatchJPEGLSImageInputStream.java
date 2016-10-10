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
 * Java(TM), available at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa HealthCare.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below.
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

import java.io.IOException;
import java.util.Arrays;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class PatchJPEGLSImageInputStream extends ImageInputStreamImpl {

    private static final Logger LOG =
            LoggerFactory.getLogger(PatchJPEGLSImageInputStream.class);

    private final ImageInputStream iis;
    private long patchPos;
    private byte[] patch;

    public PatchJPEGLSImageInputStream(ImageInputStream iis,
            PatchJPEGLS patchJPEGLS) throws IOException {
        if (iis == null)
            throw new NullPointerException("iis");

        super.streamPos = iis.getStreamPosition();
        super.flushedPos = iis.getFlushedPosition();
        this.iis = iis;
        if (patchJPEGLS == null)
            return;

        JPEGLSCodingParam param = patchJPEGLS.createJPEGLSCodingParam(firstBytesOf(iis));
        if (param != null) {
            LOG.debug("Patch JPEG-LS with {}", param);
            this.patchPos = streamPos + param.getOffset();
            this.patch = param.getBytes();
        }
    }

    private byte[] firstBytesOf(ImageInputStream iis) throws IOException {
        byte[] b = new byte[256];
        int n, off = 0, len = b.length;
        iis.mark();
        while (len > 0 && (n = iis.read(b, off, len)) > 0) {
            off += n;
            len -= n;
        }
        iis.reset();
        return len > 0 ? Arrays.copyOf(b, b.length - len) : b;
    }

    private int readAvailable(byte[] b) throws IOException {
        int nbytes;
        int off = 0;
        int len = b.length;
        while (len > 0 && (nbytes = iis.read(b, off, len)) > 0) {
            off += nbytes;
            len -= nbytes;
        }
        return off;
    }

    public void close() throws IOException {
        super.close();
        iis.close();
    }

    public void flushBefore(long pos) throws IOException {
        super.flushBefore(pos);
        iis.flushBefore(adjustStreamPosition(pos));
    }

    private long adjustStreamPosition(long pos) {
        if (patch == null)
            return pos;
        long index = pos - patchPos;
        return index < 0 ? pos 
                : index < patch.length ? patchPos 
                        : pos - patch.length;
    }

    public boolean isCached() {
        return iis.isCached();
    }

    public boolean isCachedFile() {
        return iis.isCachedFile();
    }

    public boolean isCachedMemory() {
        return iis.isCachedMemory();
    }

    public long length() {
        try {
            long len = iis.length();
            return patch == null || len < 0 ? len : len + patch.length;
        } catch (IOException e) {
            return -1;
        }
    }

    public int read() throws IOException {
        int ch;
        long index;
        if (patch != null
                && (index = streamPos - patchPos) >= 0 
                && index < patch.length)
            ch = patch[(int) index];
        else
            ch = iis.read();
        if (ch >= 0)
            streamPos++;
        return ch;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int r = 0;
        if (patch != null && streamPos < patchPos + patch.length) {
            if (streamPos < patchPos) {
                r = iis.read(b, off, (int) Math.min(patchPos - streamPos, len));
                if (r < 0)
                    return r;
                streamPos += r;
                if (streamPos < patchPos)
                    return r;
                off += r;
                len -= r;
            }
            int index = (int) (patchPos - streamPos);
            int r2 = (int) Math.min(patch.length - index, len);
            System.arraycopy(patch, index, b, off, r2);
            streamPos += r2;
            r += r2;
            off += r2;
            len -= r2;
        }
        if (len > 0) {
            int r3 = iis.read(b, off, len);
            if (r3 < 0)
                return r3;
            streamPos += r3;
            r += r3;
        }
        return r;
    }

    public void mark() {
        super.mark();
        iis.mark();
    }

    public void reset() throws IOException {
        super.reset();
        iis.reset();
    }

    public void seek(long pos) throws IOException {
        super.seek(pos);
        iis.seek(adjustStreamPosition(pos));
    }

}
