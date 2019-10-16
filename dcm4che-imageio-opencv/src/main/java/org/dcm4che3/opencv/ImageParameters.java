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

/**
 * @author Nicolas Roduit
 * @since Mar 2018
 */
public class ImageParameters {
    public static final int DEFAULT_TILE_SIZE = 512;

    // List of supported color model format
    public static final int CM_S_RGB = 1;
    public static final int CM_S_RGBA = 2;
    public static final int CM_GRAY = 3;
    public static final int CM_GRAY_ALPHA = 4;
    public static final int CM_S_YCC = 4;
    public static final int CM_E_YCC = 6;
    public static final int CM_YCCK = 7;
    public static final int CM_CMYK = 8;

    // Extend type of DataBuffer
    public static final int TYPE_BIT = 6;

    // Basic image parameters
    private int height;
    private int width;
    //
    private int bitsPerSample;
    // Bands
    private int bands;
    // Nb of components
    private int samplesPerPixel;
    private int bytesPerLine;
    private boolean bigEndian;
    // DataBuffer types + TYPE_BIT
    private int dataType;
    // Data offset of binary data
    private int bitOffset;
    private int dataOffset;
    private int format;
    private boolean signedData;
    private boolean initSignedData;
    private boolean jfif;
    private int jpegMarker;

    public ImageParameters() {
        this(0, 0, 0, 0, false);
    }

    public ImageParameters(int height, int width, int bitsPerSample, int samplesPerPixel, boolean bigEndian) {
        this.height = height;
        this.width = width;
        this.bitsPerSample = bitsPerSample;
        this.samplesPerPixel = samplesPerPixel;
        this.bigEndian = bigEndian;
        this.bands = 1;
        this.dataType = -1;
        this.bytesPerLine = 0;
        this.bitOffset = 0;
        this.dataOffset = 0;
        this.format = CM_GRAY;
        this.signedData = false;
        this.initSignedData = false;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getBitsPerSample() {
        return bitsPerSample;
    }

    public void setBitsPerSample(int bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
    }

    public int getSamplesPerPixel() {
        return samplesPerPixel;
    }

    public void setSamplesPerPixel(int samplesPerPixel) {
        this.samplesPerPixel = samplesPerPixel;
    }

    public int getBytesPerLine() {
        return bytesPerLine;
    }

    public void setBytesPerLine(int bytesPerLine) {
        this.bytesPerLine = bytesPerLine;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public void setBitOffset(int bitOffset) {
        this.bitOffset = bitOffset;
    }

    public int getBitOffset() {
        return bitOffset;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public boolean isSignedData() {
        return signedData;
    }

    public void setSignedData(boolean signedData) {
        this.signedData = signedData;
    }

    public boolean isInitSignedData() {
        return initSignedData;
    }

    public void setInitSignedData(boolean initSignedData) {
        this.initSignedData = initSignedData;
    }

    public int getBands() {
        return bands;
    }

    public void setBands(int bands) {
        this.bands = bands;
    }

    public void setJFIF(boolean jfif) {
        this.jfif = jfif;
    }

    public boolean isJFIF() {
        return jfif;
    }

    public int getJpegMarker() {
        return jpegMarker;
    }

    public void setJpegMarker(int jpegMarker) {
        this.jpegMarker = jpegMarker;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("Size:");
        buf.append(width);
        buf.append("x");
        buf.append(height);
        buf.append(" Bits/Sample:");
        buf.append(bitsPerSample);
        buf.append(" Samples/Pixel:");
        buf.append(samplesPerPixel);
        buf.append(" Bytes/Line:");
        buf.append(bytesPerLine);
        buf.append(" Signed:");
        buf.append(signedData);
        return buf.toString();
    }
}
