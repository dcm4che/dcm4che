package org.dcm4che.image;

public class LUTByte extends LUT {

    private final byte[] lut;

    LUTByte(StoredValue inBits, int outBits, int offset, byte[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    LUTByte(StoredValue inBits, int outBits, int offset, int size, boolean flip) {
        this(inBits, outBits, offset, new byte[size]);
        int maxOut = (1<<outBits)-1;
        int maxIndex = size - 1;
        int midIndex = maxIndex / 2;
        if (flip)
            for (int i = 0; i < size; i++)
                lut[maxIndex-i] = (byte) ((i * maxOut + midIndex) / maxIndex);
        else
            for (int i = 0; i < size; i++)
                lut[i] = (byte) ((i * maxOut + midIndex) / maxIndex);
    }

    @Override
    public int length() {
        return lut.length;
    }

    @Override
    public void lookup(byte[] src, byte[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = lut[index(src[i])];
    }

    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
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

    @Override
    public LUT adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            byte[] lut = this.lut;
            if (outBits > 8) {
                short[] ss = new short[lut.length];
                for (int i = 0; i < lut.length; i++)
                    ss[i] = (short) ((lut[i] & 0xff) << diff);
                return new LUTShort(inBits, outBits, offset, ss);
            }
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; i++)
                    lut[i] = (byte) ((lut[i] & 0xff) >> diff);
            } else
                for (int i = 0; i < lut.length; i++)
                    lut[i] <<= diff;
            this.outBits = outBits;
        }
        return this;
    }

    @Override
    public void inverse() {
        byte[] lut = this.lut;
        int maxOut = (1<<outBits)-1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (byte) (maxOut - lut[i]); 
     }


    @Override
    public LUT combine(LUT other) {
        byte[] lut = this.lut;
        if (other.outBits > 8) {
            short[] ss = new short[lut.length];
            other.lookup(lut, ss);
            return new LUTShort(inBits, other.outBits, offset, ss);
        }
        other.lookup(lut, lut);
        this.outBits = other.outBits;
        return this;
    }

}
