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

package org.dcm4che3.imageio.plugins.rle;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che3.data.Implementation;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class RLEImageReaderSpi extends ImageReaderSpi {

    private static final String vendorName = "org.dcm4che";
    private static final String version = Implementation.getVersionName();
    private static final String[] formatNames = { "rle", "RLE" };
    private static String[] entensions = { "" };
    private static String[] mimeType = { "" };
    private static final Class<?>[] inputTypes = { ImageInputStream.class };

    public RLEImageReaderSpi() {
        super(vendorName, version, formatNames, 
            entensions,  // suffixes
            mimeType,  // MIMETypes
                RLEImageReader.class.getName(), inputTypes,
                null,  // writerSpiNames
                false, // supportsStandardStreamMetadataFormat
                null,  // nativeStreamMetadataFormatName
                null,  // nativeStreamMetadataFormatClassName
                null,  // extraStreamMetadataFormatNames
                null,  // extraStreamMetadataFormatClassNames
                false, // supportsStandardImageMetadataFormat
                null,  // nativeImageMetadataFormatName
                null,  // nativeImageMetadataFormatClassName
                null,  // extraImageMetadataFormatNames
                null); // extraImageMetadataFormatClassNames
    }

    @Override
    public String getDescription(Locale locale) {
        return "RLE Image Reader";
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object extension)
            throws IOException {
        return new RLEImageReader(this);
    }
}
