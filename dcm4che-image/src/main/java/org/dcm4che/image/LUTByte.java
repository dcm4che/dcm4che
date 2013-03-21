package org.dcm4che.image;

public class LUTByte extends LUT {

    private final StoredValue pv;
    private final byte[] lut;
    private final int offset;

    LUTByte(StoredValue pv, int offset, byte[] lut) {
        this.pv = pv;
        this.lut = lut;
        this.offset = offset;
    }

    LUTByte(StoredValue pv, int offset, int size, int outBits, boolean invers) {
        this(pv, offset, new byte[size]);
        int maxOut = (1<<outBits)-1;
        int maxIndex = size - 1;
        int midIndex = size / 2;
        if (invers)
            for (int i = 0; i < size; i++)
                lut[maxIndex-i] = (byte) ((i * maxOut + midIndex) / maxIndex);
        else
            for (int i = 0; i < size; i++)
                lut[i] = (byte) ((i * maxOut + midIndex) / maxIndex);
    }

    @Override
    public void lookup(byte[] src, byte[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = lut[index(src[i])];
    }

    private int index(int pixel) {
        int index = pv.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length-1);
    }

    @Override
    public void lookup(short[] src, byte[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = lut[index(src[i])];
    }

    @Override
    public void lookup(byte[] src, short[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = (short) (lut[index(src[i])] & 0xff);
    }

    @Override
    public void lookup(short[] src, short[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = (short) (lut[index(src[i])] & 0xff);
    }

}
