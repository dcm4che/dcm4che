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

import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;

/**
 * ImageInputStream which wraps an in-memory byte[]
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class ByteArrayImageInputStream extends ImageInputStreamImpl {
    private final byte[] data;

    public ByteArrayImageInputStream(byte[] data) {
        this.data = data;
    }

    public int read() throws IOException {
        this.bitOffset = 0;
        return this.streamPos >= (long)this.data.length ? -1 : this.data[(int)(this.streamPos++)] & 255;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        len = (int)Math.min((long)this.data.length - this.streamPos, (long)len);
        if (len <= 0) {
            return -1;
        } else {
            System.arraycopy(this.data, (int)this.streamPos, b, off, len);
            this.streamPos += (long)len;
            return len;
        }
    }

    @Override
    public long length() {
        return this.data.length;
    }
}
