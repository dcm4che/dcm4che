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

package org.dcm4che3.imageio.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
  * Treats a specified portion of an image input stream as it's own input stream.  Can handle open-ended
  * specified sub-regions by specifying a -1 frame, which will treat all fragments as though they are part
  * of the segment, and will look for new segments in the DICOM original data once the first part
  * is read past. 
  * @author Gunter Zeilinger <gunterze@gmail.com>
  * @author Bill Wallace <wayfarer3130@gmail.com>
  */
public class SegmentedInputImageStream extends ImageInputStreamImpl {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentedInputImageStream.class);
    
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private final ImageInputStream stream;
    private int curSegment=0;
    private int firstSegment=1, lastSegment=Integer.MAX_VALUE;
    // The end of the current segment, in streamPos units, not in underlying stream units
    private long curSegmentEnd=-1;
    private final List<Object> fragments;
    private byte[] byteFrag;
    private ImageDescriptor imageDescriptor;

    /**
     * Create a segmented input stream, that updates the bulk data entries as required, frameIndex
     * of -1 means the entire object/value.
     */
    public SegmentedInputImageStream(ImageInputStream stream,
            Fragments pixeldataFragments, int frameIndex) throws IOException {
        if( frameIndex==-1 ) {
            frameIndex = 0;
        } else {
            firstSegment = frameIndex+1;
            lastSegment = frameIndex+2;
        }
        this.fragments = pixeldataFragments;
        this.stream = stream;
        this.curSegment = frameIndex;
        seek(0);
    }

    public SegmentedInputImageStream(ImageInputStream iis, long streamPosition, int length, boolean singleFrame) throws IOException {
        fragments = new Fragments(VR.OB, false, 16);
        if( !singleFrame ) {
            lastSegment = 2;
        }
        fragments.add(new byte[0]);
        fragments.add(new BulkData("pixelData://",  streamPosition, length, false));
        stream = iis;
        seek(0);
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    public void setImageDescriptor(ImageDescriptor imageDescriptor) {
        this.imageDescriptor = imageDescriptor;
    }

    /** Just read from the raw data segment - this gets converted to an in-memory fragments object,
     * which is then handled as a single fragment, with no basic offset table. Basically just an easy
     * way to get an image input stream on a byte array.
     */
    public SegmentedInputImageStream(byte[] data) throws IOException {
        stream = null;
        fragments = new Fragments(VR.OB,false,2);
        fragments.add(new byte[0]);
        fragments.add(data);
        lastSegment = 2;
        seek(0);
    }

    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        long beforePos = 0;
        for (int i = firstSegment; i<lastSegment; i++) {
            BulkData bulk = null;
            long bulkOffset = -1;
            int bulkLength = -1;
            synchronized(fragments) {
                if( i < fragments.size() ) {
                    Object fragment = fragments.get(i);
                    if(fragment instanceof BulkData) {
                        bulk = (BulkData) fragments.get(i);
                        bulkOffset = bulk.offset();
                        bulkLength = bulk.length();
                    } else {
                        byteFrag = (byte[]) fragment;
                        bulkLength = byteFrag.length;
                        bulkOffset = beforePos;
                    }
                    
                }
            }
            if( bulkOffset==-1 || bulkLength==-1 ) {
                bulk = updateBulkData(i);
                if( bulk==null ) {
                    lastSegment = i;
                    curSegment = -1;
                    super.seek(beforePos);
                    return;
                } else {
                    bulkOffset = bulk.offset();
                    bulkLength = bulk.length();
                }
            }
            // We are past end of input, but we didn't know soon enough
            if( i>=lastSegment ) {
                curSegment = -1;
                super.seek(beforePos);
                return;
            }
            long deltaInEnd = pos-beforePos;
            beforePos += bulkLength & 0xFFFFFFFFl;            
            if (pos < beforePos) {
                curSegment = i;
                curSegmentEnd = beforePos;
                if( bulk!=null ) {
                    stream.seek(bulk.offset() + deltaInEnd);
                }
                return;
            }
        }
        curSegment = -1;
    }

    BulkData updateBulkData(int endBulk) throws IOException {
        BulkData last = null;
        for(int i=1; i<=endBulk; i++) {
            BulkData bulk = null;
            long bulkOffset = -1;
            int bulkLength = -1;
            synchronized(fragments) {
                if( i < fragments.size() ) {
                    bulk = (BulkData) fragments.get(i);
                    bulkOffset = bulk.offset();
                    bulkLength = bulk.length();
                }
            }
            if( bulkOffset==-1  ) {
                long testOffset = last.offset() + (0xFFFFFFFFl & last.length());
                bulk = readBulkAt(testOffset, i);
            } else if( bulkLength==-1 ) {
                bulk = readBulkAt(bulkOffset-8, i);
            }
            if( bulk==null ) {
                return null;
            }
            last = bulk;
        }
        return last;
    }

    BulkData readBulkAt(long testOffset, int at) throws IOException {
        byte[] data = new byte[8];
        stream.seek(testOffset);        
        int size = stream.read(data);
        if( size<8 ) return null;
        int tag =ByteUtils.bytesToTagLE(data, 0);
        if( tag==Tag.SequenceDelimitationItem ) {
            // Safe to read un-protected now as we know there are no more items to update.
            lastSegment = fragments.size();
            return null;
        }
        if( tag!=Tag.Item ) {
            throw new IOException("At "+testOffset+" isn't an Item("+Integer.toHexString(Tag.Item)+"), but is "+Integer.toHexString(tag));
        }
        int itemLen = ByteUtils.bytesToIntLE(data, 4);
        BulkData bulk;
        synchronized(fragments) {
            if( at < fragments.size() ) {
                bulk = (BulkData) fragments.get(at);
                bulk.setOffset(testOffset+8);
                bulk.setLength(itemLen);
            } else {
                bulk = new BulkData("compressedPixelData://", testOffset+8,itemLen,false);
                fragments.add(bulk);
            }
        }
        return bulk;
    }

    @Override
    public int read() throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int val = stream.read();
        if (val != -1) {
            ++streamPos;
        }
        return val;
    }

    private boolean prepareRead() throws IOException {
        if (curSegment < 0)
            return false;

        if (streamPos < curSegmentEnd)
            return true;
        
        seek(streamPos);

        if (curSegment < 0 || curSegment >= lastSegment)
            return false;
        
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int bytesToRead = Math.min(len, (int) (curSegmentEnd-streamPos));
        int nbytes;
        if( byteFrag!=null ) {
            System.arraycopy(byteFrag, (int) (streamPos-curSegmentEnd+byteFrag.length), b, off, bytesToRead);
            nbytes = bytesToRead;
        } else {
            nbytes = stream.read(b, off, bytesToRead );
        }
        if (nbytes != -1) {
            streamPos += nbytes;
        }
        return nbytes;
    }

    public long getLastSegmentEnd() {
        synchronized(fragments) {
            BulkData bulk = (BulkData) fragments.get(fragments.size()-1);
            return bulk.getSegmentEnd();
        }
    }

    public long getOffsetPostPixelData() throws IOException {
        long ret = getLastSegmentEnd();
        if( ret!=-1 ) {
            return ret+8;
        }
        // Support up to 1024 additional fragments of a single image.
        updateBulkData(fragments.size()+1025);
        ret = getLastSegmentEnd();
        return ret+8;
    }

    /**
     * Reads all bytes from this input stream and writes the bytes to the given output stream. This method
     * does not close either stream.
     *
     * @param out the output stream, non-null
     * @return the number of bytes transferred
     * @throws IOException if an I/O error occurs when reading or writing
     * @throws NullPointerException if out is {@code null}
     */
    public long transferTo(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = this.read(buffer, 0, DEFAULT_BUFFER_SIZE)) > 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }
    
    @Override
    public long length() {
        try {
            long wasPos = this.getStreamPosition();
            seek(Long.MAX_VALUE);
            long ret = this.getStreamPosition();
            seek(wasPos);
            LOG.debug("wasPos {} end {}", wasPos,ret);
            return ret;
        }
        catch(IOException e) {
            LOG.warn("Caught error determining length:{}", e);
            LOG.debug("Stack trace",e);
            return -1;
        }
    }

    public ImageInputStream getStream() {
        return stream;
    }

    public int getCurSegment() {
        return curSegment;
    }

    public List<Object> getFragments() {
        return fragments;
    }

    public Integer getLastSegment() throws IOException {
        seek(Long.MAX_VALUE);
        return lastSegment;
    }
}
