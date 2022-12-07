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

package org.dcm4che3.imageio.stream;

import org.dcm4che3.imageio.codec.BytesWithImageImageDescriptor;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.io.DicomInputStream;

import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Aug 2015
 */
public class EncapsulatedPixelDataImageInputStream extends MemoryCacheImageInputStream
        implements BytesWithImageImageDescriptor {

    private final DicomInputStream dis;
    private final ImageDescriptor imageDescriptor;
    private final TransferSyntaxType tsType;
    private final byte[] basicOffsetTable;
    private final int frameStartWord;
    private int fragmStartWord;
    private long fragmEndPos;
    private long frameStartPos;
    private long frameEndPos = -1L;
    private boolean endOfStream;

    public EncapsulatedPixelDataImageInputStream(DicomInputStream dis, ImageDescriptor imageDescriptor)
            throws IOException {
        this(dis, imageDescriptor, TransferSyntaxType.forUID(dis.getTransferSyntax()));
    }

    public EncapsulatedPixelDataImageInputStream(DicomInputStream dis, ImageDescriptor imageDescriptor,
            TransferSyntaxType tsType) throws IOException {
        super(dis);
        this.dis = dis;
        this.imageDescriptor = imageDescriptor;
        this.tsType = tsType;
        dis.readItemHeader();
        byte[] b = new byte[dis.length()];
        dis.readFully(b);
        basicOffsetTable = b;
        readItemHeader();
        frameStartWord = fragmStartWord;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @Override
    public int read() throws IOException {
        if (endOfFrame())
            return -1;

        return super.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (endOfFrame())
            return -1;

        return super.read(b, off,
                Math.min(len, (int)((frameEndPos < 0 ? fragmEndPos : frameEndPos) - streamPos)));
    }

    public void seekCurrentFrame() throws IOException {
        seek(frameStartPos);
    }

    public boolean seekNextFrame() throws IOException {
        if (endOfStream)
            return false;

        if (frameEndPos >= 0) {
            seek(frameEndPos);
            flush();
        } else {
            while (!endOfFrame()) {
                seek(fragmEndPos-1);
                super.read(); // ensure to read wh ole Data Fragment from DicomInputStream
                flush();
            }
        }
        frameStartPos = streamPos;
        frameEndPos = -1L;
        return !endOfStream;
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

    private boolean readItemHeader() throws IOException {
        if (!dis.readItemHeader()) {
            endOfStream = true;
            return false;
        }
        fragmEndPos = streamPos + dis.unsignedLength();
        mark();
        fragmStartWord = (super.read() << 8) | super.read();
        reset();
        return true;
    }

    private boolean endOfFrame() throws IOException {
        if (frameEndPos >= 0)
            return streamPos >= frameEndPos;

        if (streamPos < fragmEndPos)
            return false;

        if (readItemHeader() && (!imageDescriptor.isMultiframe()
                || (tsType.mayFrameSpanMultipleFragments() && fragmStartWord != frameStartWord)))
            return false;

        frameEndPos = streamPos;
        return true;
    }

    public boolean isEndOfStream() {
        return endOfStream;
    }
}
