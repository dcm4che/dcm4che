package org.dcm4che.data;

import java.util.Arrays;

class Group {

    private final int groupNumber;
    private int[] elementNumbers;
    private VR[] vrs;
    private Object[] values;
    private int size;

    Group(int groupNumber, int capacity) {
        this.groupNumber = groupNumber;
        elementNumbers = new int[capacity];
        vrs = new VR[capacity];
        values = new Object[capacity];
    }

    public final int getGroupNumber() {
        return groupNumber;
    }

    public String toString() {
        return String.format("(%04X,eeee)[%d]", groupNumber, size);
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final int size() {
        return size;
    }

    public void trimToSize() {
        int oldCapacity = elementNumbers.length;
        if (size < oldCapacity) {
            elementNumbers = Arrays.copyOf(elementNumbers, size);
            vrs = Arrays.copyOf(vrs, size);
            values = Arrays.copyOf(values, size);
        }
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = elementNumbers.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = Math.max(minCapacity, (oldCapacity * 3)/2 + 1);
            elementNumbers = Arrays.copyOf(elementNumbers, newCapacity);
            vrs = Arrays.copyOf(vrs, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    private int indexOf(int elTag) {
        return Arrays.binarySearch(elementNumbers, 0, size, elTag);
    }

    public boolean contains(SpecificCharacterSet cs, int tag,
            String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return false;

        return indexOf(elTag) >= 0;
    }

    public boolean containsValue(SpecificCharacterSet cs,
            int tag, String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return false;

        int index = indexOf(elTag);
        return index >= 0 && !isEmpty(values[index]);
    }

    private boolean isEmpty(Object value) {
        if (value == null)
            return true;

        if (value instanceof Sequence)
            return ((Sequence) value).isEmpty();

        return false;
    }

    public byte[] getBytes(SpecificCharacterSet cs, int tag,
            String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr == VR.SQ)
            throw new UnsupportedOperationException("VR: "+vr);

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_BYTES;

        if (value instanceof byte[])
            return (byte[]) value;

        if (value instanceof String)
            return vr.toBytes((String) value, cs);

        throw new UnsupportedOperationException(
                "Cannot convert " + value + " to byte[]");
    }

    public String getString(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, String defVal) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return defVal;

        int index = indexOf(elTag);
        if (index < 0)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.SQ)
            throw new UnsupportedOperationException("VR: "+vr);

        Object value = values[index];
        if (value == null)
            return defVal;

         return vr.isBinaryType() 
                 ? vr.firstBinaryValueAsString((byte[]) value, bigEndian)
                 : vr.firstStringValue(toString(value, index, vr, cs));
    }

    private String toString(Object value, int index, VR vr,
            SpecificCharacterSet cs) {
        if (value instanceof String)
            return (String) value;
        String s = vr.toString((byte[]) value, cs);
        values[index] = s;
        return s;
    }

    public String[] getStrings(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr == VR.SQ)
            throw new UnsupportedOperationException("VR: "+vr);

        Object value = values[index];
        if (value == null)
            return StringType.EMPTY_STRINGS;

        return vr.isBinaryType() 
                ? vr.binaryValueAsStrings((byte[]) value, bigEndian)
                : vr.splitStringValue(toString(value, index, vr, cs));
     }

    public int getInt(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, int defVal) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag < 0)
            return defVal;

        int index = indexOf(elTag);
        if (index < 0)
            return defVal;

        VR vr = vrs[index];
        if (vr != VR.IS)
            vr.checkBinaryType();

        Object value = values[index];
        if (value == null)
            return defVal;

        return vr == VR.IS ? vr.toInt(toString(value, index, vr, cs))
                           : vr.toInt((byte[]) value, bigEndian);
    }

    public int[] getInts(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr != VR.IS)
            vr.checkBinaryType();

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_INTS;

        return vr == VR.IS ? vr.toInts(toString(value, index, vr, cs))
                           : vr.toInts((byte[]) value, bigEndian);
    }

    public float getFloat(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, float defVal) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return defVal;

        int index = indexOf(elTag);
        if (index < 0)
            return defVal;

        VR vr = vrs[index];
        if (vr != VR.DS)
            vr.checkBinaryType();

        Object value = values[index];
        if (value == null)
            return defVal;

        return vr == VR.DS ? vr.toFloat(toString(value, index, vr, cs))
                           : vr.toFloat((byte[]) value, bigEndian);
    }

    public float[] getFloats(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr != VR.DS)
            vr.checkBinaryType();

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_FLOATS;

        if (vr == VR.DS) {
            String s;
            if (value instanceof String)
                s = (String) value;
            else
                values[index] = s = vr.toString((byte[]) value, cs);
            String[] ss = vr.splitStringValue(s);
            float[] floatarr = new float[ss.length];
            for (int i = 0; i < ss.length; i++)
                floatarr[i] = Float.parseFloat(ss[i]);
            return floatarr;
        }
        return vr == VR.DS ? vr.toFloats(toString(value, index, vr, cs))
                           : vr.toFloats((byte[]) value, bigEndian);
    }


    public double getDouble(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, double defVal) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return defVal;

        int index = indexOf(elTag);
        if (index < 0)
            return defVal;

        VR vr = vrs[index];
        if (vr != VR.DS)
            vr.checkBinaryType();

        Object value = values[index];
        if (value == null)
            return defVal;

        return vr == VR.DS ? vr.toDouble(toString(value, index, vr, cs))
                           : vr.toDouble((byte[]) value, bigEndian);
    }

    public double[] getDoubles(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr != VR.DS)
            vr.checkBinaryType();

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_DOUBLES;

        return vr == VR.DS ? vr.toDoubles(toString(value, index, vr, cs))
                           : vr.toDoubles((byte[]) value, bigEndian);
    }

    public Sequence getSequence(SpecificCharacterSet cs, int tag,
            String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr != VR.SQ)
            throw new UnsupportedOperationException("VR: "+vr);

        return (Sequence) values[index];
    }

    public Fragments getFragments(SpecificCharacterSet cs, int tag,
            String privateCreator) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null || value instanceof Fragments)
            return (Fragments) value;

        return (Fragments) value;
    }

    public boolean remove(int tag, String privateCreator,
            SpecificCharacterSet cs) {
        int elTag = elTag(cs, privateCreator, tag, false);
        if (elTag == 0)
            return false;

        int index = indexOf(elTag);
        if (index < 0)
            return false;

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementNumbers, index+1, elementNumbers, index, numMoved);
            System.arraycopy(vrs, index+1, vrs, index, numMoved);
            System.arraycopy(values, index+1, values, index, numMoved);
        }
        values[--size] = null;
        return true;
    }

    public void putNull(SpecificCharacterSet cs, int tag,
            String privateCreator, VR vr) {
        put(cs, tag, privateCreator, vr,null);
    }

    public void putBytes(SpecificCharacterSet cs, int tag,
            String privateCreator, VR vr, byte[] value) {
        put(cs, tag, privateCreator, vr, vr.toValue(value));
    }

    public void putString(SpecificCharacterSet cs, boolean bigEndian, int tag,
            String privateCreator, VR vr, String s) {
        put(cs, tag, privateCreator, vr, vr.toValue(s, bigEndian));
    }

    public void putStrings(SpecificCharacterSet cs, boolean bigEndian, int tag,
            String privateCreator, VR vr, String... ss) {
        put(cs, tag, privateCreator, vr, vr.toValue(ss, bigEndian));
    }

    public void putInt(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, VR vr, int i) {
        put(cs, tag, privateCreator, vr, vr.toValue(i, bigEndian));
    }

    public void putInts(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, VR vr, int... intattr) {
        put(cs, tag, privateCreator, vr, vr.toValue(intattr, bigEndian));
    }

    public void putFloat(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, VR vr, float f) {
        put(cs, tag, privateCreator, vr, vr.toValue(f, bigEndian));
    }

    public void putFloats(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, VR vr, float[] floatarr) {
        put(cs, tag, privateCreator, vr, vr.toValue(floatarr, bigEndian));
    }

    public void putDouble(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, VR vr, double d) {
        put(cs, tag, privateCreator, vr, vr.toValue(d, bigEndian));
    }

    public void putDoubles(SpecificCharacterSet cs, boolean bigEndian,
            int tag, String privateCreator, VR vr, double[] doublearr) {
        put(cs, tag, privateCreator, vr, vr.toValue(doublearr, bigEndian));
    }

    public Sequence putSequence(SpecificCharacterSet cs, int tag,
            String privateCreator, Attributes parent, int initialCapacity) {
        Sequence seq = new Sequence(parent, initialCapacity);
        put(cs, tag, privateCreator, VR.SQ, seq);
        return seq;
    }

    public Fragments putFragments(SpecificCharacterSet cs, int tag,
            String privateCreator, VR vr, int initialCapacity) {
        Fragments fragments = new Fragments(vr, initialCapacity);
        put(cs, tag, privateCreator, fragments.vr(), fragments);
        return fragments;
    }

    private void put(SpecificCharacterSet cs, int tag,
            String privateCreator, VR vr, Object value) {
        int elTag = elTag(cs, privateCreator, tag, true);
        int index = size;
        if (size != 0 && elTag <= elementNumbers[size-1]) {
            index = indexOf(elTag);
            if (index >= 0) {
                vrs[index] = vr;
                values[index] = value;
                return;
            }
            index = -index-1;
        }
        ensureCapacity(size+1);
        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(elementNumbers, index, elementNumbers, index+1, numMoved);
            System.arraycopy(vrs, index, vrs, index+1, numMoved);
            System.arraycopy(values, index, values, index+1, numMoved);
        }
        elementNumbers[index] = elTag;
        vrs[index] = vr;
        values[index] = value;
        size++;
    }

    private int elTag(SpecificCharacterSet cs, String privateCreator, int tag,
            boolean reservePrivateBlock) {
        int elTag = tag & 0xffff;
        if (privateCreator == null)
            return elTag;

        if ((groupNumber & 1) == 0)
            throw new IllegalArgumentException(String.format(
                    "Cannot specify privateCreator != null with Standard Attribute (%04X,%04X)!",
                    groupNumber, elTag));

        if ((elTag & 0xff00) != 0)
            throw new IllegalArgumentException(String.format(
                    "Element number of private Attribute (%04X,%04X) exeeds 255!",
                    groupNumber, elTag));

        for (int creatorTag = 0x10; creatorTag <= 0xff; creatorTag++) {
            String s = getString(cs, false, creatorTag, null, null);
            if (s == null) {
                if (!reservePrivateBlock)
                    return -1;
                putString(cs, false, creatorTag, null, VR.LO, privateCreator);
                return (creatorTag << 8) | elTag;
            }
            if (privateCreator.equals(s))
                return (creatorTag << 8) | elTag;
        }
        throw new IllegalStateException(String.format(
                "No unreserved block in group (%04X,eeee) left.", groupNumber));
    }

    public String getPrivateCreator(SpecificCharacterSet cs, int tag) {
        int creatorTag = (tag >>> 16) & 0xff;
        return getString(cs, false, creatorTag, null, null);
    }
}
