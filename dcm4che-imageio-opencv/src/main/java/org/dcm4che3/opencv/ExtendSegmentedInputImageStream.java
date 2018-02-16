package org.dcm4che3.opencv;

import java.io.File;

public class ExtendSegmentedInputImageStream {
    private final File file;
    private final long[] segmentPositions;
    private final long[] segmentLengths;

    public ExtendSegmentedInputImageStream(File file, long[] segmentPositions, int[] segmentLengths) {
        this.file = file;
        this.segmentPositions = segmentPositions;
        this.segmentLengths = segmentLengths == null ? null : getDoubleArray(segmentLengths);
    }

    public long[] getSegmentPositions() {
        return segmentPositions;
    }

    public long[] getSegmentLengths() {
        return segmentLengths;
    }

    public File getFile() {
        return file;
    }
    
    public static double[] getDoubleArray(long[] array) {
        double[] a = new double[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
    
    public static long[] getDoubleArray(int[] array) {
        long[] a = new long[array.length];
        for (int i = 0; i < a.length; i++) {
            a[i] = array[i];
        }
        return a;
    }
}
