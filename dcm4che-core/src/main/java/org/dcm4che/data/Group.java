package org.dcm4che.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.dcm4che.util.TagUtils;

class Group {

    private final int groupNumber;
    private boolean bigEndian;
    private int[] elementNumbers;
    private VR[] vrs;
    private Object[] values;
    private int size;

    private static final class DefaultCharacterSet extends Group {

        DefaultCharacterSet(int groupNumber, boolean bigEndian, int capacity) {
            super(groupNumber, bigEndian, capacity);
        }

        @Override
        protected SpecificCharacterSet cs(Attributes attrs) {
            return SpecificCharacterSet.DEFAULT;
        }
    }

    static Group create(int groupNumber, boolean bigEndian, int capacity) {
        return groupNumber < 8 
                ? new DefaultCharacterSet(groupNumber, bigEndian, capacity)
                : new Group(groupNumber, bigEndian, capacity);
    }

    private Group(int groupNumber, boolean bigEndian, int capacity) {
        this.groupNumber = groupNumber;
        this.bigEndian = bigEndian;
        elementNumbers = new int[capacity];
        vrs = new VR[capacity];
        values = new Object[capacity];
    }

    protected SpecificCharacterSet cs(Attributes attrs) {
        return attrs.getSpecificCharacterSet();
    }

    public final int getGroupNumber() {
        return groupNumber;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    public void bigEndian(boolean bigEndian) {
        if (this.bigEndian != bigEndian) {
            for (int i = 0; i < size; i++) {
                Object value = values[i];
                if (value instanceof byte[])
                    vrs[i].toggleEndian((byte[]) value);
            }
            this.bigEndian = bigEndian;
        }
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

    public void trimToSize(boolean recursive) {
        int oldCapacity = elementNumbers.length;
        if (size < oldCapacity) {
            elementNumbers = Arrays.copyOf(elementNumbers, size);
            vrs = Arrays.copyOf(vrs, size);
            values = Arrays.copyOf(values, size);
        }
        if (recursive)
            for (Object value : values) {
                if (value instanceof Sequence) {
                    ((Sequence) value).trimToSize(recursive);
                } else if (value instanceof Fragments)
                    ((Fragments) value).trimToSize();
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

    public boolean contains(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
            return false;

        return indexOf(elTag) >= 0;
    }

    public boolean containsValue(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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

    public ByteBuffer getByteBuffer(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr == VR.SQ)
            throw new UnsupportedOperationException("VR: "+vr);

        Object value = values[index];
        byte[] array;
        if (value == null)
            array = BinaryType.EMPTY_BYTES;
        else if (value instanceof byte[])
            array = (byte[]) value;
        else if (value instanceof String)
            array = vr.toBytes((String) value, cs(parent));
        else
            throw new UnsupportedOperationException(
                    "Cannot convert " + value + " to byte[]");
        return ByteBuffer.wrap(array)
                .order(bigEndian ? ByteOrder.BIG_ENDIAN
                                 : ByteOrder.LITTLE_ENDIAN);
    }

    public String getString(Attributes parent, int tag,
            String privateCreator, String defVal) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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
                 : vr.firstStringValue(toString(value, index, vr, cs(parent)));
    }

    private String toString(Object value, int index, VR vr,
            SpecificCharacterSet cs) {
        if (value instanceof String)
            return (String) value;
        String s = vr.toString((byte[]) value, cs);
        values[index] = s;
        return s;
    }

    public String[] getStrings(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag == -1)
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
                : vr.splitStringValue(toString(value, index, vr, cs(parent)));
     }

    public int getInt(Attributes parent, int tag, String privateCreator,
            int defVal) {
        int elTag = elTag(parent, privateCreator, tag, false);
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

        return vr == VR.IS ? vr.toInt(toString(value, index, vr, cs(parent)))
                           : vr.toInt((byte[]) value, bigEndian);
    }

    public int[] getInts(Attributes parent, int tag, String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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

        return vr == VR.IS ? vr.toInts(toString(value, index, vr, cs(parent)))
                           : vr.toInts((byte[]) value, bigEndian);
    }

    public float getFloat(Attributes parent, int tag, String privateCreator,
            float defVal) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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

        return vr == VR.DS ? vr.toFloat(toString(value, index, vr, cs(parent)))
                           : vr.toFloat((byte[]) value, bigEndian);
    }

    public float[] getFloats(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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
                values[index] = s = vr.toString((byte[]) value, cs(parent));
            String[] ss = vr.splitStringValue(s);
            float[] floatarr = new float[ss.length];
            for (int i = 0; i < ss.length; i++)
                floatarr[i] = Float.parseFloat(ss[i]);
            return floatarr;
        }
        return vr == VR.DS ? vr.toFloats(toString(value, index, vr, cs(parent)))
                           : vr.toFloats((byte[]) value, bigEndian);
    }


    public double getDouble(Attributes parent, int tag, String privateCreator,
            double defVal) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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

        return vr == VR.DS ? vr.toDouble(toString(value, index, vr, cs(parent)))
                           : vr.toDouble((byte[]) value, bigEndian);
    }

    public double[] getDoubles(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
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

        return vr == VR.DS ? vr.toDoubles(toString(value, index, vr, cs(parent)))
                           : vr.toDoubles((byte[]) value, bigEndian);
    }

    public Sequence getSequence(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        VR vr = vrs[index];
        if (vr != VR.SQ)
            throw new UnsupportedOperationException("VR: "+vr);

        return (Sequence) values[index];
    }

    public Fragments getFragments(Attributes parent, int tag,
            String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
            return null;

        int index = indexOf(elTag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null || value instanceof Fragments)
            return (Fragments) value;

        return (Fragments) value;
    }

    public boolean remove(Attributes parent, int tag, String privateCreator) {
        int elTag = elTag(parent, privateCreator, tag, false);
        if (elTag < 0)
            return false;

        int index = indexOf(elTag);
        if (index < 0)
            return false;

        Object value = values[index];
        if (value instanceof Sequence)
            ((Sequence) value).clear();

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementNumbers, index+1, elementNumbers, index, numMoved);
            System.arraycopy(vrs, index+1, vrs, index, numMoved);
            System.arraycopy(values, index+1, values, index, numMoved);
        }
        values[--size] = null;
        return true;
    }

    public void putNull(Attributes parent, int tag, String privateCreator,
            VR vr) {
        put(parent, tag, privateCreator, vr,null);
    }

    public void putBytes(Attributes parent, int tag, String privateCreator,
            VR vr, byte[] value, boolean bigEndian) {
        if (this.bigEndian != bigEndian)
            vr.toggleEndian(value);
        put(parent, tag, privateCreator, vr, vr.toValue(value));
    }

    public void putString(Attributes cs, int tag, String privateCreator,
            VR vr, String s) {
        put(cs, tag, privateCreator, vr, vr.toValue(s, bigEndian));
    }

    public void putStrings(Attributes parent, int tag, String privateCreator,
            VR vr, String... ss) {
        put(parent, tag, privateCreator, vr, vr.toValue(ss, bigEndian));
    }

    public void putInt(Attributes parent, int tag, String privateCreator,
            VR vr, int i) {
        put(parent, tag, privateCreator, vr, vr.toValue(i, bigEndian));
    }

    public void putInts(Attributes parent, int tag, String privateCreator,
            VR vr, int... intattr) {
        put(parent, tag, privateCreator, vr, vr.toValue(intattr, bigEndian));
    }

    public void putFloat(Attributes parent, int tag, String privateCreator,
            VR vr, float f) {
        put(parent, tag, privateCreator, vr, vr.toValue(f, bigEndian));
    }

    public void putFloats(Attributes parent, int tag, String privateCreator,
            VR vr, float[] floatarr) {
        put(parent, tag, privateCreator, vr, vr.toValue(floatarr, bigEndian));
    }

    public void putDouble(Attributes parent, int tag, String privateCreator,
            VR vr, double d) {
        put(parent, tag, privateCreator, vr, vr.toValue(d, bigEndian));
    }

    public void putDoubles(Attributes parent, int tag, String privateCreator,
            VR vr, double[] doublearr) {
        put(parent, tag, privateCreator, vr, vr.toValue(doublearr, bigEndian));
    }

    public Sequence putSequence(Attributes parent, int tag,
            String privateCreator, int initialCapacity) {
        Sequence seq = new Sequence(parent, initialCapacity);
        put(parent, tag, privateCreator, VR.SQ, seq);
        return seq;
    }

    public Fragments putFragments(Attributes parent, int tag,
            String privateCreator, VR vr, boolean bigEndian,
            int initialCapacity) {
        Fragments fragments = new Fragments(vr, bigEndian, initialCapacity);
        put(parent, tag, privateCreator, vr, fragments);
        return fragments;
    }

    private void put(Attributes parent, int tag, String privateCreator, VR vr,
            Object value) {
        put(elTag(parent, privateCreator, tag, true), vr, value);
    }

    private void put(int elTag, VR vr, Object value) {
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

    private int elTag(Attributes parent, String privateCreator, int tag,
            boolean reservePrivateBlock) {
        int elTag = tag & 0xffff;
        if (privateCreator == null)
            return elTag;

        if ((groupNumber & 1) == 0)
            throw new IllegalArgumentException(String.format(
                    "Cannot specify privateCreator != null with Standard Attribute (%04X,%04X)!",
                    groupNumber, elTag));

        SpecificCharacterSet cs = cs(parent);
        for (int creatorTag = 0x10; creatorTag <= 0xff; creatorTag++) {
            int index = indexOf(creatorTag);
            if (index < 0) {
                if (!reservePrivateBlock)
                    return -1;
                putString(parent, creatorTag, null, VR.LO, privateCreator);
                return (creatorTag << 8) | (elTag & 0xff);
            }
            if (privateCreator.equals(toString(values[index], index, VR.LO, cs)))
                return (creatorTag << 8) | (elTag & 0xff);
        }
        throw new IllegalStateException(String.format(
                "No unreserved block in group (%04X,eeee) left.", groupNumber));
    }

    public String getPrivateCreator(Attributes parent, int tag) {
        int creatorTag = (tag >>> 8) & 0xff;
        int index = indexOf(creatorTag);
        return (index < 0) ? null : toString(values[index], index, VR.LO,
                cs(parent));
    }

    public void putAll(Attributes parent, Group srcGroup) {
        bigEndian(srcGroup.bigEndian);
        int[] elTags = srcGroup.elementNumbers;
        VR[] srcVRs = srcGroup.vrs;
        Object[] srcValues = srcGroup.values;
        int otherGroupSize = srcGroup.size;
        if ((groupNumber & 1) == 0) {
            for (int i = 0; i < otherGroupSize; i++) {
                int elTag = elTags[i];
                put(elTag, srcVRs[i], cloneItems(srcValues[i], parent));
                if (TagUtils.toTag(groupNumber, elTag)
                        == Tag.SpecificCharacterSet)
                    parent.initSpecificCharacterSet();
            }
        } else {
            int i = 0;
            // skip private creators
            while (i < otherGroupSize && elTags[i] < 0xff)
                i++;
            for (; i < otherGroupSize; i++) {
                int elTag = elTags[i];
                String privateCreator = srcGroup.getPrivateCreator(
                        parent, elTag);
                put(parent, elTag, privateCreator, srcVRs[i],
                        cloneItems(srcValues[i], parent));
            }
        }
    }

    private Object cloneItems(Object value, Attributes parent) {
        if (value instanceof Sequence)
            return clone((Sequence) value, parent);
        if (value instanceof Fragments)
            return ((Fragments) value).clone();
        return value;
    }

    private Sequence clone(Sequence srcSeq, Attributes parent) {
        Sequence dstSeq = new Sequence(parent, srcSeq.size());
        for (Attributes src : srcSeq)
            dstSeq.add(new Attributes(src));
        return dstSeq ;
    }
}
