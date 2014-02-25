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
 * Medical Insight.
 * Portions created by the Initial Developer are Copyright (C) 2011
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

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Bojesen <jbb@medical-insight.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class PatchJPEGLSImageOutputStream extends ImageOutputStreamImpl {

    private static final Logger LOG =
            LoggerFactory.getLogger(PatchJPEGLSImageOutputStream.class);

    private final ImageOutputStream ios;
    private final PatchJPEGLS patchJpegLS;
    private byte[] jpegheader;
    private int jpegheaderIndex;

    public PatchJPEGLSImageOutputStream(ImageOutputStream ios,
            PatchJPEGLS patchJpegLS) throws IOException {
        if (ios == null)
            throw new NullPointerException("ios");
        super.streamPos = ios.getStreamPosition();
        super.flushedPos = ios.getFlushedPosition();
        this.ios = ios;
        this.patchJpegLS = patchJpegLS;
        this.jpegheader = patchJpegLS != null ? new byte[256] : null;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (jpegheader == null) {
            ios.write(b, off, len);
        } else {
            int len0 = Math.min(jpegheader.length - jpegheaderIndex, len);
            System.arraycopy(b, off, jpegheader, jpegheaderIndex, len0);
            jpegheaderIndex += len0;
            if (jpegheaderIndex >= jpegheader.length) {
                JPEGLSCodingParam param =
                        patchJpegLS.createJPEGLSCodingParam(jpegheader);
                if (param == null)
                    ios.write(jpegheader);
                else {
                    LOG.debug("Patch JPEG-LS with {}", param);
                    int offset = param.getOffset();
                    ios.write(jpegheader, 0, offset);
                    ios.write(param.getBytes());
                    ios.write(jpegheader, offset, jpegheader.length - offset);
                }
                ios.write(b, off + len0, len - len0);
                jpegheader = null;
            }
        }
        streamPos += len;
    }

    public void write(byte[] b) throws IOException {
	write(b, 0, b.length);
    }

    public void write(int b) throws IOException {
        if (jpegheader == null) {
            ios.write(b);
            streamPos++;
        } else
            write(new byte[]{(byte) b},0,1);
    }

    public int read() throws IOException {
	return ios.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
	return ios.read (b, off, len);
    }
}
