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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class SegmentedInputImageStream extends ImageInputStreamImpl {

    private final ImageInputStream stream;
    private final long[] segmentPositionsList;
    private final int[] segmentLengths;
    private int curSegment;
    private long curSegmentEnd;

    public SegmentedInputImageStream(ImageInputStream stream,
            Fragments pixeldataFragments, int frameIndex) throws IOException {
        long[] offsets = new long[pixeldataFragments.size()-(frameIndex+1)];
        int[] length = new int[offsets.length];
        for (int i = 0; i < length.length; i++) {
            BulkData bulkData = (BulkData) pixeldataFragments.get(i+frameIndex+1);
            offsets[i] = bulkData.offset;
            length[i] = bulkData.length;
        }
        this.stream = stream;
        this.segmentPositionsList = offsets;
        this.segmentLengths = length;
        seek(0);
    }

    public SegmentedInputImageStream(ImageInputStream stream,
            long[] segmentPositionsList, int[] segmentLengths)
                    throws IOException {
        this.stream = stream;
        this.segmentPositionsList = segmentPositionsList.clone();
        this.segmentLengths = segmentLengths.clone();
        seek(0);
    }

    private int offsetOf(int segment) {
        int pos = 0;
        for (int i = 0; i < segment; ++i)
            pos += segmentLengths[i];
        return pos;
    }

    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);
        for (int i = 0, off = 0; i < segmentLengths.length; i++) {
            int end = off + segmentLengths[i];
            if (pos < end) {
                stream.seek(segmentPositionsList[i] + pos - off);
                curSegment = i;
                curSegmentEnd = end;
                return;
            }
            off = end;
        }
        curSegment = -1;
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

        if (curSegment >= segmentPositionsList.length)
            return false;
        
        seek(offsetOf(curSegment+1));
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!prepareRead())
            return -1;

        bitOffset = 0;
        int nbytes = stream.read(b, off,
                Math.min(len, (int) (curSegmentEnd-streamPos)));
        if (nbytes != -1) {
            streamPos += nbytes;
        }
        return nbytes;
    }

}
