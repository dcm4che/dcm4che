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
 * *** END LICENSE BLOCK *****
 */

package org.dcm4che3.imageio.codec;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2015
 */
final class ExtMemoryCacheImageOutputStream extends MemoryCacheImageOutputStream
        implements BytesWithImageImageDescriptor {

    private final ExtFilterOutputStream stream;
    private final ImageDescriptor imageDescriptor;

    public ExtMemoryCacheImageOutputStream(ImageDescriptor imageDescriptor) {
        this(new ExtFilterOutputStream(), imageDescriptor);
    }

    private ExtMemoryCacheImageOutputStream(ExtFilterOutputStream stream, ImageDescriptor imageDescriptor) {
        super(stream);
        this.stream = stream;
        this.imageDescriptor = imageDescriptor;
    }

    public void setOutputStream(OutputStream stream) {
        this.stream.setOutputStream(stream);
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @Override
    public ByteBuffer getBytes() throws IOException {
        byte[] array = new byte[8192];
        int length = 0;
        int read;
        while ((read = this.read(array, length, array.length - length)) > 0) {
            if ((length += read) == array.length)
                array = Arrays.copyOf(array, array.length << 1);
        }
        return ByteBuffer.wrap(array, 0, length);
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        if (stream.getOutputStream() != null)
            super.flushBefore(pos);
    }

    private static final class ExtFilterOutputStream extends FilterOutputStream {

        public ExtFilterOutputStream() {
            super(null);
        }

        public OutputStream getOutputStream() {
            return super.out;
        }

        public void setOutputStream(OutputStream out) {
            super.out = out;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
    }

}
