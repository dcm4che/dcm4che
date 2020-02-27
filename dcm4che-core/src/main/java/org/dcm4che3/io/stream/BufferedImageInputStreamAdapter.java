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
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Buffered image input stream adapter which is able to intelligently handle skip / mark / reset.
 * @author Andrew Cowan (awcowan@gmail.com)
 */
public class BufferedImageInputStreamAdapter extends BufferedInputStream {
    public BufferedImageInputStreamAdapter(ImageInputStream iis) {
        super(new CloseableImageInputStreamAdapter(iis));
    }

    public BufferedImageInputStreamAdapter(ImageInputStream iis, int bufferSize) {
        super(new CloseableImageInputStreamAdapter(iis),bufferSize);
    }


    @Override
    public synchronized long skip(long n) throws IOException {
        // Reset the cached position if we are skipping forward in IIS
        this.markpos = -1;
        return super.skip(n);
    }
}
