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
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Handle URIs in an extensible way.

 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public class FileURIImageInputStreamSpi extends ImageInputStreamSpi {
    private static final String VENDOR = "org.dcm4che";
    private static final String VERSION = Implementation.getVersionName();

    public FileURIImageInputStreamSpi() {
        super(VENDOR, VERSION, URI.class);
    }

    @Override
    public ImageInputStream createInputStreamInstance(Object input, boolean useCache, File cacheDir) throws IOException {
        URI dataURI = (URI)input;
        File file = toFile(dataURI);
        return new FileImageInputStream(file);
    }

    protected File toFile(URI fileURI) {

        if(fileURI.getQuery() != null) {
            String uriStr = fileURI.toString();
            int queryIdx = uriStr.indexOf('?');
            uriStr = uriStr.substring(0,queryIdx);
            fileURI = URI.create(uriStr);
        }

        return Paths.get(fileURI).toFile();
    }

    @Override
    public String getDescription(Locale locale) {
        return "ImageInputStream SPI for file:// URIs";
    }

}
