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

import org.dcm4che3.data.Implementation;

import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Wrap an existing image input stream and allow it to be used without closing the underlying input stream.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class DelegateInputStreamSpi extends ImageInputStreamSpi {
    private static final String VENDOR = "org.dcm4che";
    private static final String VERSION = Implementation.getVersionName();

    public DelegateInputStreamSpi() {
        super(VENDOR, VERSION, ImageInputStream.class);
    }

    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) throws IOException {
        return new DelegateImageInputStream((ImageInputStream)input);
    }

    @Override
    public String getDescription(Locale locale) {
        return "ImageInputStream which delegates to an underlying ImageInputStreamn";
    }


    @Override
    public Class<?> getInputClass() {
        return ImageInputStream.class;
    }
}
