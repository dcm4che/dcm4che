
/*
 * **** BEGIN LICENSE BLOCK *****
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
 * Portions created by the Initial Developer are Copyright (C) 2015-2018
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
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.opencv;

import java.io.IOException;
import java.util.Locale;

import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * @author Nicolas Roduit
 * @since Mar 2025
 */
public class NativeJXLImageReaderSpi extends ImageReaderSpi {

  static final String[] NAMES = { "jpeg-xl-cv", "jpeg-xl", "JPEG-XL" };
  static final String[] SUFFIXES = { "jxl" };
  static final String[] MIMES = { "image/jxl" };

  public NativeJXLImageReaderSpi() {
    super("Weasis Team", "1.0", NAMES, SUFFIXES, MIMES, NativeImageReader.class.getName(),
            new Class[] { ImageInputStream.class }, new String[] {NativeJXLImageWriterSpi.class.getName()}, false, // supportsStandardStreamMetadataFormat
        null, // nativeStreamMetadataFormatName
        null, // nativeStreamMetadataFormatClassName
        null, // extraStreamMetadataFormatNames
        null, // extraStreamMetadataFormatClassNames
        false, // supportsStandardImageMetadataFormat
        null, null, null, null);
  }

  @Override
  public String getDescription(Locale locale) {
    return "Natively-accelerated JPEG XL Image Reader";
  }

  @Override
  public boolean canDecodeInput(Object source) throws IOException {
    // NativeImageReader.read() eventually instantiates a StreamSegment,
    // which does not support all ImageInputStreams
    if (!StreamSegment.supportsInputStream(source)) {
      return false;
    }
    ImageInputStream iis = (ImageInputStream) source;

    iis.mark();
    // Check for JPEG XL signature
    int byte1 = iis.read();
    int byte2 = iis.read();

    if (byte1 == 0xFF && byte2 == 0x0A) {
      // Bare codestream format
      iis.reset();
      return true;
    }

    // Check for container format
    int byte3 = iis.read();
    int byte4 = iis.read();
    int byte5 = iis.read();
    int byte6 = iis.read();
    int byte7 = iis.read();
    int byte8 = iis.read();
    int byte9 = iis.read();
    int byte10 = iis.read();
    int byte11 = iis.read();
    int byte12 = iis.read();
    iis.reset();

    // Container format: 0x0000_000C_4A58_4C20_0D0A_870A
    return (byte1 == 0x00) && (byte2 == 0x00) && (byte3 == 0x00) && (byte4 == 0x0C) &&
        (byte5 == 0x4A) && (byte6 == 0x58) && (byte7 == 0x4C) && (byte8 == 0x20) &&
        (byte9 == 0x0D) && (byte10 == 0x0A) && (byte11 == 0x87) && (byte12 == 0x0A);
  }

  @Override
  public ImageReader createReaderInstance(Object extension) throws IIOException {
    return new NativeImageReader(this, false);
  }
}
