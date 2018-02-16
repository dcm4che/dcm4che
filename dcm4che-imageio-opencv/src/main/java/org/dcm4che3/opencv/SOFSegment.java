package org.dcm4che3.opencv;

class SOFSegment {
    private final int marker;
    private final int samplePrecision;
    private final int lines; // height
    private final int samplesPerLine; // width
    private final int components;

    SOFSegment(int marker, int samplePrecision, int lines, int samplesPerLine, int components) {
        this.marker = marker;
        this.samplePrecision = samplePrecision;
        this.lines = lines;
        this.samplesPerLine = samplesPerLine;
        this.components = components;
    }

    public int getMarker() {
        return marker;
    }

    public int getSamplePrecision() {
        return samplePrecision;
    }

    public int getLines() {
        return lines;
    }

    public int getSamplesPerLine() {
        return samplesPerLine;
    }

    public int getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return String.format("SOF%d[%04x, precision: %d, lines: %d, samples/line: %d]", marker & 0xff - 0xc0, marker,
            samplePrecision, lines, samplesPerLine);
    }
}
