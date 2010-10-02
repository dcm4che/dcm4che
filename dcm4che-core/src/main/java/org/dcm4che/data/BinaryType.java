package org.dcm4che.data;

import org.dcm4che.util.ByteUtils;

enum BinaryType {
    BYTE(1) {
        @Override
        protected int bytesToIntBE(byte[] bytes, int off) {
            return bytes[off] & 255;
        }

        @Override
        protected int bytesToIntLE(byte[] bytes, int off) {
            return bytes[off] & 255;
        }

        @Override
        protected byte[] intToBytesBE(int i, byte[] bytes, int off) {
            bytes[off] = (byte) i;
            return bytes;
        }

        @Override
        protected byte[] intToBytesLE(int i, byte[] bytes, int off) {
            bytes[off] = (byte) i;
            return bytes;
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return bytes ;
        }
    },
    USHORT(2) {
        @Override
        protected int bytesToIntBE(byte[] bytes, int off) {
            return ByteUtils.bytesToUShortBE(bytes, off);
        }

        @Override
        protected int bytesToIntLE(byte[] bytes, int off) {
            return ByteUtils.bytesToUShortLE(bytes, off);
        }

        @Override
        protected byte[] intToBytesBE(int i, byte[] bytes, int off) {
            return ByteUtils.shortToBytesBE(i, bytes, off);
        }

        @Override
        protected byte[] intToBytesLE(int i, byte[] bytes, int off) {
            return ByteUtils.shortToBytesLE(i, bytes, off);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapShorts(bytes) ;
        }
    },
    SHORT(2) {
        @Override
        protected int bytesToIntBE(byte[] bytes, int off) {
            return ByteUtils.bytesToShortBE(bytes, off);
        }

        @Override
        protected int bytesToIntLE(byte[] bytes, int off) {
            return ByteUtils.bytesToShortLE(bytes, off);
        }

        @Override
        protected byte[] intToBytesBE(int i, byte[] bytes, int off) {
            return ByteUtils.shortToBytesBE(i, bytes, off);
        }

        @Override
        protected byte[] intToBytesLE(int i, byte[] bytes, int off) {
            return ByteUtils.shortToBytesLE(i, bytes, off);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapShorts(bytes) ;
        }
    },
    INT(4) {
        @Override
        protected int bytesToIntBE(byte[] bytes, int off) {
            return ByteUtils.bytesToIntBE(bytes, off);
        }

        @Override
        protected int bytesToIntLE(byte[] bytes, int off) {
            return ByteUtils.bytesToIntLE(bytes, off);
        }

        @Override
        protected byte[] intToBytesBE(int i, byte[] bytes, int off) {
            return ByteUtils.intToBytesBE(i, bytes, off);
        }

        @Override
        protected byte[] intToBytesLE(int i, byte[] bytes, int off) {
            return ByteUtils.intToBytesLE(i, bytes, off);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapInts(bytes) ;
        }
    },
    TAG(4) {
        @Override
        protected int bytesToIntBE(byte[] bytes, int off) {
            return ByteUtils.bytesToTagBE(bytes, off);
        }

        @Override
        protected int bytesToIntLE(byte[] bytes, int off) {
            return ByteUtils.bytesToTagLE(bytes, off);
        }

        @Override
        protected byte[] intToBytesBE(int i, byte[] bytes, int off) {
            return ByteUtils.tagToBytesBE(i, bytes, off);
        }

        @Override
        protected byte[] intToBytesLE(int i, byte[] bytes, int off) {
            return ByteUtils.tagToBytesLE(i, bytes, off);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapShorts(bytes) ;
        }
    },
    FLOAT(4) {
        @Override
        protected float bytesToFloatBE(byte[] bytes, int off) {
            return ByteUtils.bytesToFloatBE(bytes, off);
        }

        @Override
        protected float bytesToFloatLE(byte[] bytes, int off) {
            return ByteUtils.bytesToFloatLE(bytes, off);
        }

        @Override
        protected double bytesToDoubleBE(byte[] bytes, int off) {
            return bytesToFloatBE(bytes, off);
        }

        @Override
        protected double bytesToDoubleLE(byte[] bytes, int off) {
            return ByteUtils.bytesToFloatLE(bytes, off);
        }

        @Override
        protected byte[] floatToBytesBE(float f, byte[] bytes, int off) {
            return ByteUtils.floatToBytesBE(f, bytes, off);
        }

        @Override
        protected byte[] floatToBytesLE(float f, byte[] bytes, int off) {
            return ByteUtils.floatToBytesLE(f, bytes, off);
        }

        @Override
        protected byte[] doubleToBytesBE(double d, byte[] bytes, int off) {
            return ByteUtils.floatToBytesBE((float) d, bytes, off);
        }

        @Override
        protected byte[] doubleToBytesLE(double d, byte[] bytes, int off) {
            return ByteUtils.floatToBytesLE((float) d, bytes, off);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapInts(bytes);
        }
    },
    DOUBLE(8) {
        @Override
        protected float bytesToFloatBE(byte[] bytes, int off) {
            return (float) ByteUtils.bytesToDoubleBE(bytes, off);
        }

        @Override
        protected float bytesToFloatLE(byte[] bytes, int off) {
            return (float) ByteUtils.bytesToDoubleLE(bytes, off);
        }

        @Override
        protected double bytesToDoubleBE(byte[] bytes, int off) {
            return ByteUtils.bytesToDoubleBE(bytes, off);
        }

        @Override
        protected double bytesToDoubleLE(byte[] bytes, int off) {
            return ByteUtils.bytesToDoubleLE(bytes, off);
        }

        @Override
        protected byte[] floatToBytesBE(float f, byte[] bytes, int off) {
            return ByteUtils.doubleToBytesBE(f, bytes, off);
        }

        @Override
        protected byte[] floatToBytesLE(float f, byte[] bytes, int off) {
            return ByteUtils.doubleToBytesLE(f, bytes, off);
        }

        @Override
        protected byte[] doubleToBytesBE(double d, byte[] bytes, int off) {
            return ByteUtils.doubleToBytesBE(d, bytes, off);
        }

        @Override
        protected byte[] doubleToBytesLE(double d, byte[] bytes, int off) {
            return ByteUtils.doubleToBytesLE(d, bytes, off);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapLongs(bytes);
        }
    };
    
    public static byte[] EMPTY_BYTES = {};
    public static int[] EMPTY_INTS = {};
    public static float[] EMPTY_FLOATS = {};
    public static double[] EMPTY_DOUBLES = {};

    private final int numBytes;

    private BinaryType(int numBytes) {
        this.numBytes = numBytes;
    }

    public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToIntBE(bytes, off) : bytesToIntLE(bytes, off);
    }

    public int[] bytesToInts(byte[] bytes, boolean bigEndian) {
        return bigEndian ? bytesToIntsBE(bytes) : bytesToIntsLE(bytes);
    }

    public float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToFloatBE(bytes, off)
                         : bytesToFloatLE(bytes, off);
    }

    public float[] bytesToFloats(byte[] bytes, boolean bigEndian) {
        return bigEndian ? bytesToFloatsBE(bytes)
                         : bytesToFloatsLE(bytes);
    }

    public double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
        return bigEndian ? bytesToDoubleBE(bytes, off)
                         : bytesToDoubleLE(bytes, off);
    }

    public double[] bytesToDoubles(byte[] bytes, boolean bigEndian) {
        return bigEndian ? bytesToDoublesBE(bytes)
                         : bytesToDoublesLE(bytes);
    }

    public byte[] intToBytes(int val, boolean bigEndian) {
        return intToBytes(val, new byte[numBytes], 0, bigEndian);
    }

    public byte[] intToBytes(int val, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? intToBytesBE(val, bytes, off)
                         : intToBytesLE(val, bytes, off);
    }

    public byte[] intsToBytes(int[] ints, boolean bigEndian) {
        return bigEndian ? intsToBytesBE(ints)
                         : intsToBytesLE(ints);
    }

    public byte[] floatToBytes(float val, boolean bigEndian) {
        return floatToBytes(val, new byte[numBytes], 0, bigEndian);
    }

    public byte[] floatToBytes(float val, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? floatToBytesBE(val, bytes, off)
                         : floatToBytesLE(val, bytes, off);
    }

    public byte[] floatsToBytes(float[] floats, boolean bigEndian) {
        return bigEndian ? floatsToBytesBE(floats)
                         : floatsToBytesLE(floats);
    }

    public byte[] doubleToBytes(double d, boolean bigEndian) {
        return doubleToBytes(d, new byte[numBytes], 0, bigEndian);
    }

    public byte[] doubleToBytes(double d, byte[] bytes, int off,
            boolean bigEndian) {
        return bigEndian ? doubleToBytesBE(d, bytes, off)
                         : doubleToBytesLE(d, bytes, off);
    }

    public byte[] doublesToBytes(double[] doubles, boolean bigEndian) {
        return bigEndian ? doublesToBytesBE(doubles)
                         : doublesToBytesLE(doubles);
    }

    public abstract byte[] toggleEndian(byte[] bytes);

    protected int bytesToIntBE(byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected int bytesToIntLE(byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected float bytesToFloatBE(byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected float bytesToFloatLE(byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected double bytesToDoubleBE(byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected double bytesToDoubleLE(byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected byte[] intToBytesBE(int val, byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected byte[] intToBytesLE(int val, byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected byte[] floatToBytesBE(float val, byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected byte[] floatToBytesLE(float val, byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected byte[] doubleToBytesBE(double val, byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    protected byte[] doubleToBytesLE(double val, byte[] bytes, int off) {
        throw new UnsupportedOperationException();
    }

    private void checkLength(int len) {
        checkLength(len, numBytes);
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    private int[] bytesToIntsBE(byte[] bytes) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_INTS;

        int[] ints = new int[len / numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            ints[i] = bytesToIntBE(bytes, off);
        return ints ;
    }

    private int[] bytesToIntsLE(byte[] bytes) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_INTS;

        int[] ints = new int[len / numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            ints[i] = bytesToIntLE(bytes, off);
        return ints ;
    }

    private float[] bytesToFloatsBE(byte[] bytes) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_FLOATS;

        float[] floats = new float[len / numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floats[i] = bytesToFloatBE(bytes, off);
        return floats;
    }

    private float[] bytesToFloatsLE(byte[] bytes) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_FLOATS;

        float[] floats = new float[len / numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floats[i] = bytesToFloatLE(bytes, off);
        return floats ;
    }

    private double[] bytesToDoublesBE(byte[] bytes) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_DOUBLES;

        double[] doubles = new double[len / numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubles[i] = bytesToDoubleBE(bytes, off);
        return doubles;
    }

    private double[] bytesToDoublesLE(byte[] bytes) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_DOUBLES;

        double[] doubles = new double[len / numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubles[i] = bytesToDoubleLE(bytes, off);
        return doubles ;
    }

    private byte[] intsToBytesBE(int[] ints) {
        if (ints.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[ints.length * numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            intToBytesBE(ints[i], bytes, off);
        return bytes ;
    }

    private byte[] intsToBytesLE(int[] ints) {
        if (ints.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[ints.length * numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            intToBytesLE(ints[i], bytes, off);
        return bytes ;
    }

    private byte[] floatsToBytesBE(float[] floats) {
        if (floats.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[floats.length * numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floatToBytesBE(floats[i], bytes, off);
        return bytes ;
    }

    private byte[] floatsToBytesLE(float[] floats) {
        if (floats.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[floats.length * numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floatToBytesLE(floats[i], bytes, off);
        return bytes ;
    }

    private byte[] doublesToBytesBE(double[] doubles) {
        if (doubles.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[doubles.length * numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubleToBytesBE(doubles[i], bytes, off);
        return bytes ;
    }

    private byte[] doublesToBytesLE(double[] doubles) {
        if (doubles.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[doubles.length * numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubleToBytesLE(doubles[i], bytes, off);
        return bytes ;
    }
}
