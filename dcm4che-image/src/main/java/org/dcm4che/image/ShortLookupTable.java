package org.dcm4che.image;

public class ShortLookupTable extends LookupTable {

    private final short[] lut;

    ShortLookupTable(StoredValue inBits, int outBits, int offset, short[] lut) {
        super(inBits, outBits, offset);
        this.lut = lut;
    }

    ShortLookupTable(StoredValue inBits, int outBits, int offset, int size, boolean flip) {
       this(inBits, outBits, offset, new short[size]);
       int maxOut = (1<<outBits)-1;
       int maxIndex = size - 1;
       int midIndex = size / 2;
       if (flip)
           for (int i = 0; i < size; i++)
               lut[maxIndex-i] = (short) ((i * maxOut + midIndex) / maxIndex);
       else
           for (int i = 0; i < size; i++)
               lut[i] = (short) ((i * maxOut + midIndex) / maxIndex);
    }

    @Override
    public int length() {
        return lut.length;
    }

    @Override
    public void lookup(byte[] src, byte[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = (byte) lut[index(src[i] & 0xff)];
    }

    private int index(int pixel) {
        int index = inBits.valueOf(pixel) - offset;
        return Math.min(Math.max(0, index), lut.length-1);
    }

    @Override
    public void lookup(short[] src, byte[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = (byte) lut[index(src[i] & 0xffff)];
    }

    @Override
    public void lookup(byte[] src, short[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = lut[index(src[i] & 0xff)];
    }

    @Override
    public void lookup(short[] src, short[] dest) {
        for (int i = 0; i < src.length; i++)
            dest[i] = lut[index(src[i] & 0xffff)];
    }

    @Override
    public LookupTable adjustOutBits(int outBits) {
        int diff = outBits - this.outBits;
        if (diff != 0) {
            short[] lut = this.lut;
            if (diff < 0) {
                diff = -diff;
                for (int i = 0; i < lut.length; i++)
                    lut[i] = (short) ((lut[i] & 0xffff) >> diff);
            } else
                for (int i = 0; i < lut.length; i++)
                    lut[i] <<= diff;
            this.outBits = outBits;
        }
        return this;
    }

    @Override
    public void inverse() {
        short[] lut = this.lut;
        int maxOut = (1<<outBits)-1;
        for (int i = 0; i < lut.length; i++)
            lut[i] = (short) (maxOut - lut[i]); 
     }

    @Override
    public LookupTable combine(LookupTable other) {
        short[] lut = this.lut;
        other.lookup(lut, lut);
        this.outBits = other.outBits;
        return this;
    }
}
