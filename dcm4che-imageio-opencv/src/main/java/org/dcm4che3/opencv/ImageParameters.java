package org.dcm4che3.opencv;

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
