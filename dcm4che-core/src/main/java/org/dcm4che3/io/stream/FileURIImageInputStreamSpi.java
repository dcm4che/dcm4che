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
 * Portions created by the Initial Developer are Copyright (C) 2011
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
