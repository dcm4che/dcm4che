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
import java.io.IOException;
import java.net.URI;

/**
 * URIs which match "java:iis" will return a shared image input stream.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DelegateImageInputStreamLoader implements ImageInputStreamLoader<URI> {

    private final ImageInputStream iis;

    public DelegateImageInputStreamLoader(ImageInputStream iis) {
        this.iis = iis;
    }

    @Override
    public ImageInputStream openStream(URI input) throws IOException {
        return new DelegateImageInputStream(iis);
    }
}
