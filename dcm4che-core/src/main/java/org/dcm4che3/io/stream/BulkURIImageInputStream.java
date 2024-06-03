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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
package org.dcm4che3.io.stream;

import org.dcm4che3.error.TruncatedPixelDataException;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;

/**
 * Return a subset of an image input stream.  The local variables track the location within the offset/length
 * bounds which are mapped onto the underlying full image input stream.
 * @author Andrew Cowan (awcowan@gmail.com)
 */
public class BulkURIImageInputStream extends ImageInputStreamImpl {

    private final ImageInputStream iis;
    private final long offset;
    private final long length;



    public BulkURIImageInputStream(ImageInputStream iis, long offset, long length) throws IOException {
        this.iis = iis;
        this.offset = offset;
        this.length = length;

        // Set the pointer to the start of the file
        seek(0);
    }


    @Override
    public int read() throws IOException {
        int bytesRead;

        if(this.getStreamPosition() >= this.length) {
            bytesRead = -1;
        }
        else {
            bytesRead = iis.read();
            if(bytesRead > 0) this.streamPos++;
        }
        return bytesRead;
    }

    @Override
    public int read(byte[] b, int offset, int len) throws IOException {
        len = (int)Math.min(len, length() - getStreamPosition());

        int bytesRead;
        if(len <= 0) {
            bytesRead = -1;
        }
        else {
            bytesRead = this.iis.read(b, offset, len);
            if(bytesRead > 0) {
                this.streamPos += bytesRead;
            }
            else if(bytesRead == -1) {
                // EOF, but did we read everything?
                if(this.streamPos < length()) {
                    throw new TruncatedPixelDataException();
                }
            }
        }

        return bytesRead;
    }

    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        iis.seek(getStreamPosition() + this.offset);
    }

    @Override
    public long length() {
        return this.length;
    }


    @Override
    public void close() throws IOException {
        super.close();
        this.iis.close();
    }
}
