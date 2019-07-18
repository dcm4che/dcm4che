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
package org.dcm4che3.io.stream;

import javax.imageio.stream.ImageInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class CloseableImageInputStreamAdapter extends InputStream {

    private final ImageInputStream iis;

    private long markedPos;

    private IOException markException;

    public CloseableImageInputStreamAdapter(ImageInputStream iis) {
        this.iis = iis;
    }

    @Override
    public int read() throws IOException {
        return iis.read();
    }

    @Override
    public synchronized void mark(int readlimit) {
        try {
            this.markedPos = iis.getStreamPosition();
            this.markException = null;
        } catch (IOException e) {
            this.markException = e;
        }
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return iis.read(b, off, len);
    }

    @Override
    public synchronized void reset() throws IOException {
        if (markException != null)
            throw markException;
        iis.seek(markedPos);
    }

    @Override
    public long skip(long n) throws IOException {
        return iis.skipBytes((int) n);
    }

    @Override
    public void close() throws IOException {
        this.iis.close();
    }
}
