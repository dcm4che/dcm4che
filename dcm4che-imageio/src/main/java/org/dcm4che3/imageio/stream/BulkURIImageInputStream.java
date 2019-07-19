//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2019                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//
package org.dcm4che3.imageio.stream;

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
    private final int length;



    public BulkURIImageInputStream(ImageInputStream iis, long offset, int length) throws IOException {
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
            if(bytesRead > 0) this.streamPos += bytesRead;
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
}
