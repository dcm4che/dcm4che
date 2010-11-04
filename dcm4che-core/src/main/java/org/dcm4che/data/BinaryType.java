package org.dcm4che.data;

import org.dcm4che.util.ByteUtils;

enum BinaryType {
    BYTE(1) {
        @Override
        public int bytesToInt(byte[] b, int off, boolean bigEndian) {
            return b[off];
        }

        @Override
        public byte[] intToBytes(int i, byte[] b, int off, boolean bigEndian) {
            b[off] = (byte) i;
            return b;
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return b ;
        }

    },
    USHORT(2) {
        @Override
        public int bytesToInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToUShort(b, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.shortToBytes(i, b, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return ByteUtils.swapShorts(b) ;
        }

    },
    SHORT(2) {
        @Override
        public int bytesToInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToShort(b, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.shortToBytes(i, b, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return ByteUtils.swapShorts(b) ;
        }

    },
    INT(4) {
        @Override
        public int bytesToInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToInt(b, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.intToBytes(i, b, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return ByteUtils.swapInts(b) ;
        }
    },
    TAG(4) {
        @Override
        public int bytesToInt(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToTag(b, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] b, int off, boolean bigEndian) {
            return ByteUtils.tagToBytes(i, b, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return ByteUtils.swapShorts(b) ;
        }
    },
    FLOAT(4) {
        @Override
        public float bytesToFloat(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToFloat(b, off, bigEndian);
        }

        @Override
        public double bytesToDouble(byte[] b, int off, boolean bigEndian) {
            return bytesToFloat(b, off, bigEndian);
        }

        @Override
        public String bytesToString(byte[] b, int off, boolean bigEndian) {
           return VR.formatDS(ByteUtils.bytesToFloat(b, off, bigEndian));
        }

        @Override
        public byte[] floatToBytes(float f, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes(f, b, off, bigEndian);
        }

        @Override
        public byte[] doubleToBytes(double d, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes((float) d, b, off, bigEndian);
        }

        @Override
        public byte[] stringToBytes(String s, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes(
                    Float.parseFloat(s), b, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return ByteUtils.swapInts(b);
        }
    },
    DOUBLE(8) {
        @Override
        public float bytesToFloat(byte[] b, int off, boolean bigEndian) {
            return (float) ByteUtils.bytesToDouble(b, off, bigEndian);
        }

        @Override
        public double bytesToDouble(byte[] b, int off, boolean bigEndian) {
            return ByteUtils.bytesToDouble(b, off, bigEndian);
        }

        @Override
        public String bytesToString(byte[] b, int off, boolean bigEndian) {
           return VR.formatDS(ByteUtils.bytesToDouble(b, off, bigEndian));
        }

        @Override
        public byte[] floatToBytes(float f, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(f, b, off, bigEndian);
        }

        @Override
        public byte[] doubleToBytes(double d, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(d, b, off, bigEndian);
        }

        @Override
        public byte[] stringToBytes(String s, byte[] b, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(Double.parseDouble(s), b, off,
                    bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] b) {
            return ByteUtils.swapLongs(b);
        }
    };
    
    public static byte[] EMPTY_BYTES = {};
    public static int[] EMPTY_INTS = {};
    public static float[] EMPTY_FLOATS = {};
    public static double[] EMPTY_DOUBLES = {};
    public static String[] EMPTY_STRING = {};

    private final int numBytes;

    private BinaryType(int numBytes) {
        this.numBytes = numBytes;
    }

    public byte[] intToBytes(int val, boolean bigEndian) {
        return intToBytes(val, new byte[numBytes], 0, bigEndian);
    }

    public byte[] floatToBytes(float val, boolean bigEndian) {
        return floatToBytes(val, new byte[numBytes], 0, bigEndian);
    }

    public byte[] doubleToBytes(double d, boolean bigEndian) {
        return doubleToBytes(d, new byte[numBytes], 0, bigEndian);
    }

    public byte[] stringToBytes(String s, boolean bigEndian) {
        return stringToBytes(s, new byte[numBytes], 0, bigEndian);
    }

    public abstract byte[] toggleEndian(byte[] b);

    public int bytesToInt(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public float bytesToFloat(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public double bytesToDouble(byte[] b, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public String bytesToString(byte[] b, int off, boolean bigEndian) {
        return Integer.toString(bytesToInt(b, off, bigEndian));
    }

    public byte[] intToBytes(int val, byte[] b, int off,
            boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public byte[] floatToBytes(float val, byte[] b, int off,
            boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public byte[] doubleToBytes(double val, byte[] b, int off,
            boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public byte[] stringToBytes(String s, byte[] b, int off,
            boolean bigEndian) {
        return intToBytes(VR.parseIS(s), b, off, bigEndian);
    }

    private void checkLength(int len) {
        checkLength(len, numBytes);
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    public int[] bytesToInts(byte[] b, boolean bigEndian) {
        int len = b.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_INTS;

        int[] ints = new int[len / numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            ints[i] = bytesToInt(b, off, bigEndian);
        return ints ;
    }

    public float[] bytesToFloats(byte[] b, boolean bigEndian) {
        int len = b.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_FLOATS;

        float[] floats = new float[len / numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floats[i] = bytesToFloat(b, off, bigEndian);
        return floats;
    }

    public double[] bytesToDoubles(byte[] b, boolean bigEndian) {
        int len = b.length;
        checkLength(len);
        double[] doubles = new double[len / numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubles[i] = bytesToDouble(b, off, bigEndian);
        return doubles;
    }

    public Object bytesToStrings(byte[] b, boolean bigEndian) {
        int len = b.length;
        checkLength(len);
        if (len == numBytes)
            return bytesToString(b, 0, bigEndian);

        String[] ss = new String[len / numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            ss[i] = bytesToString(b, off, bigEndian);
        return ss;
    }

    public boolean prompt(byte[] b, boolean bigEndian, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        for (int i = b.length / numBytes, off = 0; i-- > 0; 
                off += numBytes) {
            String s = bytesToString(b, off, bigEndian);
            if (sb.length() + s.length() > maxLength)
                return false;
            sb.append(s);
            if (i > 0)
                sb.append('\\');
        }
        return true;
    }

    public byte[] intsToBytes(int[] ints, boolean bigEndian) {
        if (ints.length == 0)
            return EMPTY_BYTES;

        byte[] b = new byte[ints.length * numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            intToBytes(ints[i], b, off, bigEndian);
        return b ;
    }

    public byte[] floatsToBytes(float[] floats, boolean bigEndian) {
        if (floats.length == 0)
            return EMPTY_BYTES;

        byte[] b = new byte[floats.length * numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floatToBytes(floats[i], b, off, bigEndian);
        return b ;
    }

    public byte[] doublesToBytes(double[] doubles, boolean bigEndian) {
        if (doubles.length == 0)
            return EMPTY_BYTES;

        byte[] b = new byte[doubles.length * numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubleToBytes(doubles[i], b, off, bigEndian);
        return b ;
    }

    public byte[] stringsToBytes(String[] ss, boolean bigEndian) {
        if (ss.length == 0)
            return EMPTY_BYTES;

        byte[] b = new byte[ss.length * numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            stringToBytes(ss[i], b, off, bigEndian);
        return b;
    }

}
