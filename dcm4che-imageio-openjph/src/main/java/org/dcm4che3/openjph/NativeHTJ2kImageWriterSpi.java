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
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2025
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

package org.dcm4che3.openjph;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 * SPI registration for the OpenJPH-based HTJ2K image writer.
 *
 * @since Feb 2025
 */
public class NativeHTJ2kImageWriterSpi extends ImageWriterSpi {

    static final String[] NAMES = { "htj2k-openjph" };
    static final String[] SUFFIXES = { "jhc", "j2c", "jph" };
    static final String[] MIMES = { "image/jphc" };

    public NativeHTJ2kImageWriterSpi() {
        super("dcm4che.org", "1.0", NAMES, SUFFIXES, MIMES,
            NativeHTJ2kImageWriter.class.getName(),
            new Class[] { ImageOutputStream.class },
            null, false, null, null, null, null, false, null, null, null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        int bands = type.getNumBands();
        int dataType = type.getSampleModel().getDataType();
        return (bands == 1 || bands == 3)
                && (dataType == java.awt.image.DataBuffer.TYPE_BYTE
                    || dataType == java.awt.image.DataBuffer.TYPE_USHORT
                    || dataType == java.awt.image.DataBuffer.TYPE_SHORT);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Natively-accelerated HTJ2K Image Writer (OpenJPH based)";
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new NativeHTJ2kImageWriter(this);
    }
}
