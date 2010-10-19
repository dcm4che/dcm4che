package org.dcm4che.data;

import org.dcm4che.util.ByteUtils;

enum BinaryType {
    BYTE(1) {
        @Override
        public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
            return bytes[off];
        }

        @Override
        public byte[] intToBytes(int i, byte[] bytes, int off,
                boolean bigEndian) {
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
        public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
            return ByteUtils.bytesToUShort(bytes, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.shortToBytes(i, bytes, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapShorts(bytes) ;
        }

    },
    SHORT(2) {
        @Override
        public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
            return ByteUtils.bytesToShort(bytes, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.shortToBytes(i, bytes, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapShorts(bytes) ;
        }

    },
    INT(4) {
        @Override
        public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
            return ByteUtils.bytesToInt(bytes, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.intToBytes(i, bytes, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapInts(bytes) ;
        }
    },
    TAG(4) {
        @Override
        public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
            return ByteUtils.bytesToTag(bytes, off, bigEndian);
        }

        @Override
        public byte[] intToBytes(int i, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.tagToBytes(i, bytes, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapShorts(bytes) ;
        }
    },
    FLOAT(4) {
        @Override
        public float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
            return ByteUtils.bytesToFloat(bytes, off, bigEndian);
        }

        @Override
        public double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
            return bytesToFloat(bytes, off, bigEndian);
        }

        @Override
        public String bytesToString(byte[] bytes, int off, boolean bigEndian) {
           return Float.toString(
                   ByteUtils.bytesToFloat(bytes, off, bigEndian));
        }

        @Override
        public byte[] floatToBytes(float f, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes(f, bytes, off, bigEndian);
        }

        @Override
        public byte[] doubleToBytes(double d, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes((float) d, bytes, off, bigEndian);
        }

        @Override
        public byte[] stringToBytes(String s, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.floatToBytes(
                    Float.parseFloat(s), bytes, off, bigEndian);
        }

        @Override
        public byte[] toggleEndian(byte[] bytes) {
            return ByteUtils.swapInts(bytes);
        }
    },
    DOUBLE(8) {
        @Override
        public float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
            return (float) ByteUtils.bytesToDouble(bytes, off, bigEndian);
        }

        @Override
        public double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
            return ByteUtils.bytesToDouble(bytes, off, bigEndian);
        }

        @Override
        public String bytesToString(byte[] bytes, int off, boolean bigEndian) {
           return Double.toString(
                   ByteUtils.bytesToDouble(bytes, off, bigEndian));
        }

        @Override
        public byte[] floatToBytes(float f, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(f, bytes, off, bigEndian);
        }

        @Override
        public byte[] doubleToBytes(double d, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(d, bytes, off, bigEndian);
        }

        @Override
        public byte[] stringToBytes(String s, byte[] bytes, int off,
                boolean bigEndian) {
            return ByteUtils.doubleToBytes(Double.parseDouble(s), bytes, off, 
                    bigEndian);
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

    public abstract byte[] toggleEndian(byte[] bytes);

    public int bytesToInt(byte[] bytes, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public float bytesToFloat(byte[] bytes, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public double bytesToDouble(byte[] bytes, int off, boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public String bytesToString(byte[] bytes, int off, boolean bigEndian) {
        return Integer.toString(bytesToInt(bytes, off, bigEndian));
    }

    public byte[] intToBytes(int val, byte[] bytes, int off,
            boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public byte[] floatToBytes(float val, byte[] bytes, int off,
            boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public byte[] doubleToBytes(double val, byte[] bytes, int off,
            boolean bigEndian) {
        throw new UnsupportedOperationException();
    }

    public byte[] stringToBytes(String s, byte[] bytes, int off,
            boolean bigEndian) {
        return intToBytes(VR.IS.toInt(s), bytes, off, bigEndian);
    }

    private void checkLength(int len) {
        checkLength(len, numBytes);
    }

    private static void checkLength(int len, int numBytes) {
        if (len < 0 || (len % numBytes) != 0)
            throw new IllegalArgumentException("length: " + len);
    }

    public int[] bytesToInts(byte[] bytes, boolean bigEndian) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_INTS;

        int[] ints = new int[len / numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            ints[i] = bytesToInt(bytes, off, bigEndian);
        return ints ;
    }

    public float[] bytesToFloats(byte[] bytes, boolean bigEndian) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_FLOATS;

        float[] floats = new float[len / numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floats[i] = bytesToFloat(bytes, off, bigEndian);
        return floats;
    }

    public double[] bytesToDoubles(byte[] bytes, boolean bigEndian) {
        int len = bytes.length;
        checkLength(len);
        if (len == 0)
            return EMPTY_DOUBLES;

        double[] doubles = new double[len / numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubles[i] = bytesToDouble(bytes, off, bigEndian);
        return doubles;
    }

    public String[] bytesToStrings(byte[] bytes, boolean bigEndian) {
        int len = bytes.length;
        if (len == 0)
            return EMPTY_STRING;

        checkLength(len);
        String[] strings = new String[len / numBytes];
        for (int i = 0, off = 0; i < strings.length; i++, off += numBytes)
            strings[i] = bytesToString(bytes, off, bigEndian);
        return strings;
    }

    public byte[] intsToBytes(int[] ints, boolean bigEndian) {
        if (ints.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[ints.length * numBytes];
        for (int i = 0, off = 0; i < ints.length; i++, off += numBytes)
            intToBytes(ints[i], bytes, off, bigEndian);
        return bytes ;
    }

    public byte[] floatsToBytes(float[] floats, boolean bigEndian) {
        if (floats.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[floats.length * numBytes];
        for (int i = 0, off = 0; i < floats.length; i++, off += numBytes)
            floatToBytes(floats[i], bytes, off, bigEndian);
        return bytes ;
    }

    public byte[] doublesToBytes(double[] doubles, boolean bigEndian) {
        if (doubles.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[doubles.length * numBytes];
        for (int i = 0, off = 0; i < doubles.length; i++, off += numBytes)
            doubleToBytes(doubles[i], bytes, off, bigEndian);
        return bytes ;
    }

    public byte[] stringsToBytes(String[] ss, boolean bigEndian) {
        if (ss.length == 0)
            return EMPTY_BYTES;

        byte[] bytes = new byte[ss.length * numBytes];
        for (int i = 0, off = 0; i < ss.length; i++, off += numBytes)
            stringToBytes(ss[i], bytes, off, bigEndian);
        return bytes;
    }

}
