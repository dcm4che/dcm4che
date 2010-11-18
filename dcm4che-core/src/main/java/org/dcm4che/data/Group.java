package org.dcm4che.data;

import java.util.Arrays;

import org.dcm4che.util.TagUtils;

class Group {

    public static final int TO_STRING_WIDTH = 78;
    public static final int INIT_CAPACITY = 10;
    
    private final Attributes parent;
    private final int grTag;
    private int[] elTags;
    private VR[] vrs;
    private Object[] values;
    private int size;

    public Group(Attributes parent, int grTag, int capacity) {
        this.parent = parent;
        this.grTag = grTag;
        this.elTags = new int[capacity];
        this.vrs = new VR[capacity];
        this.values = new Object[capacity];
    }

    protected SpecificCharacterSet cs() {
        return parent.getSpecificCharacterSet();
    }

    protected boolean bigEndian() {
        return parent.bigEndian();
    }

    public final int getGroupNumber() {
        return grTag;
    }

    @Override
    public String toString() {
        return toStringBuilder(size, TO_STRING_WIDTH,
                new StringBuilder(size * (TO_STRING_WIDTH + 1))).toString();
    }

    public StringBuilder toStringBuilder(int lines, int maxWidth,
            StringBuilder sb) {
        for (int i = 0, n = Math.min(size, lines); i < n; i++)
            appendAttribute(i, maxWidth, sb).append('\n');
        return sb;
    }

    private StringBuilder appendAttribute(int index, int maxChars,
            StringBuilder sb) {
        int maxLength = sb.length() + maxChars;
        int elTag = elTags[index];
        int tag = TagUtils.toTag(grTag, elTag);
        VR vr = vrs[index];
        Object value = values[index];
        sb.append(TagUtils.toString(tag)).append(' ').append(vr).append(" [");
        if (vr.toStringBuilder(value, bigEndian(), cs(),
                maxLength - sb.length() - 1, sb)) {
            sb.append("] ").append(ElementDictionary.keywordOf(
                        tag, getPrivateCreator(elTag)));
            if (sb.length() > maxLength)
                sb.setLength(maxLength);
        }
        return sb;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final int size() {
        return size;
    }

    public void trimToSize(boolean recursive) {
        int oldCapacity = elTags.length;
        if (size < oldCapacity) {
            elTags = Arrays.copyOf(elTags, size);
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
        int oldCapacity = elTags.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = Math.max(minCapacity, (oldCapacity * 3)/2 + 1);
            elTags = Arrays.copyOf(elTags, newCapacity);
            vrs = Arrays.copyOf(vrs, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    public boolean contains(int tag, String privateCreator) {
        return indexOf(tag, privateCreator) >= 0;
    }

    public boolean containsValue(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        return index >= 0 && !isEmpty(values[index]);
    }

    private boolean isEmpty(Object value) {
        if (value == null)
            return true;

        if (value instanceof Sequence)
            return ((Sequence) value).isEmpty();

        return false;
    }

    public byte[] getBytes(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return  parent.getBytesFromDefaults(tag, privateCreator);

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_BYTES;

        if (value instanceof byte[])
            return (byte[]) value;

        return vrs[index].toBytes(values[index], bigEndian(), cs());
    }

    public String getString(int tag, String privateCreator, int valueIndex, 
            String defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getStringFromDefaults(tag, privateCreator,
                    valueIndex, defVal);

        Object value = values[index];
        if (value == null)
            return defVal;

        return getString(index, valueIndex, defVal);
    }

    private String getString(int index, int valueIndex, String defVal) {
        Object value = values[index];
        if (!(value instanceof String || value instanceof String[])) {
            value = vrs[index].toStrings(value, bigEndian(), cs());
            values[index] = value;
        }

        if (value instanceof String)
            return valueIndex == 0 ? (String) value : defVal;

        String[] ss = (String[]) value;
        return valueIndex < ss.length ? ss[valueIndex] : defVal;
    }

    public String[] getStrings(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getStringsFromDefaults(tag, privateCreator);

        Object value = values[index];
        if (value == null)
            return StringType.EMPTY_STRINGS;

        if (!(value instanceof String || value instanceof String[])) {
            value = vrs[index].toStrings(value, bigEndian(), cs());
            values[index] = value;
        }

        return (value instanceof String)
                ? new String[] {(String) value}
                : (String[]) value;
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getIntFromDefaults(tag, privateCreator, valueIndex,
                    defVal);

        Object value = values[index];
        if (value == null)
            return defVal;
        
        int[] ints;
        if (value instanceof int[])
            ints = (int[]) value;
        else {
            ints = vrs[index].toInts(value, bigEndian());
            values[index] = ints;
        }

        return valueIndex < ints.length ? ints[valueIndex] : defVal;
    }

    public int[] getInts(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getIntsFromDefaults(tag, privateCreator);

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_INTS;

        if (value instanceof int[])
            return (int[]) value;

        int[] ints = vrs[index].toInts(value, bigEndian());
        values[index] = ints;

        return ints;
    }

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getFloatFromDefaults(tag, privateCreator, valueIndex,
                    defVal);

        Object value = values[index];
        if (value == null)
            return defVal;

        float[] floats;
        if (value instanceof float[])
            floats = (float[]) value;
        else {
            floats = vrs[index].toFloats(value, bigEndian());
            values[index] = floats;
        }

        return valueIndex < floats.length ? floats[valueIndex] : defVal;
    }

    public float[] getFloats(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getFloatsFromDefaults(tag, privateCreator);

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_FLOATS;

        if (value instanceof float[])
            return (float[]) value;

        float[] floats = vrs[index].toFloats(value, bigEndian());
        values[index] = floats;

        return floats;
    }

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getDoubleFromDefaults(tag, privateCreator,
                    valueIndex, defVal);

        Object value = values[index];
        if (value == null)
            return defVal;

        double[] doubles;
        if (value instanceof double[])
            doubles = (double[]) value;
        else {
            doubles = vrs[index].toDoubles(value, bigEndian());
            values[index] = doubles;
        }

        return valueIndex < doubles.length ? doubles[valueIndex] : defVal;
    }

    public double[] getDoubles(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getDoublesFromDefaults(tag, privateCreator);

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_DOUBLES;

        if (value instanceof double[])
            return (double[]) value;

        double[] doubles = vrs[index].toDoubles(value, bigEndian());
        values[index] = doubles;

        return doubles;
    }

    public Sequence getSequence(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getSequenceFromDefaults(tag, privateCreator);

        return (Sequence) values[index];
    }

    public Fragments getFragments(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return parent.getFragmentsFromDefaults(tag, privateCreator);

        Object value = values[index];
        if (value == null || value instanceof Fragments)
            return (Fragments) value;

        return (Fragments) value;
    }

    public Object remove(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value instanceof Sequence)
            ((Sequence) value).clear();

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elTags, index+1, elTags, index, numMoved);
            System.arraycopy(vrs, index+1, vrs, index, numMoved);
            System.arraycopy(values, index+1, values, index, numMoved);
        }
        values[--size] = null;
        return value;
    }

    public Object setNull(int tag, String privateCreator, VR vr) {
        return set(tag, privateCreator, vr,null);
    }

    public Object setBytes(int tag, String privateCreator, VR vr, byte[] b) {
        vr.checkSupportBytes();
        return set(tag, privateCreator, vr, nullifyEmptyBytes(b));
    }

    public Object setString(int tag, String privateCreator, VR vr, String s) {
        vr.checkSupportString();
        return set(tag, privateCreator, vr, nullifyEmptyString(s));
    }

    public Object setString(int tag, String privateCreator, VR vr,
            String... ss) {
        vr.checkSupportStrings();
        return set(tag, privateCreator, vr, nullifyEmptyStrings(ss));
    }

    public Object setInt(int tag, String privateCreator, VR vr, int... ints) {
        vr.checkSupportInts();
        return set(tag, privateCreator, vr, nullifyEmptyInts(ints));
    }

    public Object setFloat(int tag, String privateCreator, VR vr,
            float... floats) {
        vr.checkSupportFloats();
        return set(tag, privateCreator, vr, nullifyEmptyFloats(floats));
    }

    public Object setDouble(int tag, String privateCreator, VR vr,
            double[] doubles) {
        vr.checkSupportFloats();
        return set(tag, privateCreator, vr, nullifyEmptyDoubles(doubles));
    }

    private byte[] nullifyEmptyBytes(byte[] b) {
        return b == null || b.length == 0 ? null : b;
    }

    private String nullifyEmptyString(String s) {
        return s == null || s.length() == 0 ? null : s;
    }

    private Object nullifyEmptyStrings(String[] ss) {
        if (ss == null || ss.length == 0)
            return null;
        for (int i = 0; i < ss.length; i++)
            ss[i] = nullifyEmptyString(ss[i]);
        if (ss.length == 1)
            return ss[0];
        return ss;
    }

    private Object nullifyEmptyInts(int[] ints) {
        return ints == null || ints.length == 0 ? null : ints;
    }

    private Object nullifyEmptyFloats(float[] floats) {
        return floats == null || floats.length == 0 ? null : floats;
    }

    private Object nullifyEmptyDoubles(double[] doubles) {
        return doubles == null || doubles.length == 0 ? null : doubles;
    }

    public Sequence newSequence(int tag, String privateCreator,
            int initialCapacity) {
        Sequence seq = new Sequence(parent, initialCapacity);
        set(tag, privateCreator, VR.SQ, seq);
        return seq;
    }

    public Fragments newFragments(int tag, String privateCreator, VR vr,
            boolean bigEndian, int initialCapacity) {
        Fragments fragments = new Fragments(vr, bigEndian, initialCapacity);
        set(tag, privateCreator, vr, fragments);
        return fragments;
    }

    private Object set(int tag, String privateCreator, VR vr, Object value) {
        return set(elTag(tag, privateCreator, true), vr, value);
    }

    private Object set(int elTag, VR vr, Object value) {
        int index = indexForInsertOf(elTag);
        if (index >= 0) {
            Object oldValue = values[index];
            vrs[index] = vr;
            values[index] = value;
            return oldValue;
        }
        index = -index-1;
        ensureCapacity(size+1);
        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(elTags, index, elTags, index+1, numMoved);
            System.arraycopy(vrs, index, vrs, index+1, numMoved);
            System.arraycopy(values, index, values, index+1, numMoved);
        }
        elTags[index] = elTag;
        vrs[index] = vr;
        values[index] = value;
        size++;
        return null;
    }

    private int indexOf(int tag, String privateCreator) {
        int elTag = elTag(tag, privateCreator, false);
        return elTag < 0 ? Integer.MIN_VALUE : indexOf(elTag);
    }

    private int indexForInsertOf(int elTag) {
        int size = this.size;
        return size == 0 ? -1
                : elTags[size-1] < elTag ? -(size+1)
                        : Arrays.binarySearch(elTags, 0, size, elTag);
    }

    private int indexOf(int elTag) {
        return Arrays.binarySearch(elTags, 0, size, elTag);
    }

    private int elTag(int tag, String privateCreator,
            boolean reservePrivateBlock) {
        int elTag = tag & 0xffff;
        if (privateCreator == null)
            return elTag;

        if ((grTag & 1) == 0)
            throw new IllegalArgumentException(String.format(
                    "Cannot specify privateCreator != null with Standard Attribute (%04X,%04X)!",
                    grTag, elTag));

        for (int creatorTag = 0x10; creatorTag <= 0xff; creatorTag++) {
            int index = indexOf(creatorTag);
            if (index < 0) {
                if (!reservePrivateBlock)
                    return -1;
                setString(creatorTag, null, VR.LO, privateCreator);
                return (creatorTag << 8) | (elTag & 0xff);
            }
            if (privateCreator.equals(getString(index, 0, null)))
                return (creatorTag << 8) | (elTag & 0xff);
        }
        throw new IllegalStateException(String.format(
                "No unreserved block in group (%04X,eeee) left.", grTag));
    }

    public String getPrivateCreator(int tag) {
        if ((grTag & 1) == 0)
            return null;

        int creatorTag = (tag >>> 8) & 0xff;
        int index = indexOf(creatorTag);
        if (index < 0)
            return null;
        
        return getString(index, 0, null);
    }

    public void addAll(Group srcGroup, boolean toogleEndian) {
        int[] elTags = srcGroup.elTags;
        VR[] srcVRs = srcGroup.vrs;
        Object[] srcValues = srcGroup.values;
        int otherGroupSize = srcGroup.size;
        if ((grTag & 1) == 0) {
            for (int i = 0; i < otherGroupSize; i++) {
                int elTag = elTags[i];
                VR vr = srcVRs[i];
                set(elTag, vr, copyValue(vr, toogleEndian, srcValues[i]));
                if (TagUtils.toTag(grTag, elTag)
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
                VR vr = srcVRs[i];
                String privateCreator = srcGroup.getPrivateCreator(elTag);
                set(elTag, privateCreator, vr,
                        copyValue(vr, toogleEndian, srcValues[i]));
            }
        }
    }

    private Object copyValue(VR vr, boolean toogleEndian, Object value) {
        if (value instanceof Sequence)
            return clone((Sequence) value);
        if (value instanceof Fragments)
            return clone((Fragments) value, toogleEndian);
        if (toogleEndian && value instanceof byte[])
            return vr.toggleEndian((byte[]) value, true);
        return value;
    }

    private Sequence clone(Sequence srcSeq) {
        Sequence dstSeq = new Sequence(parent, srcSeq.size());
        for (Attributes src : srcSeq)
            dstSeq.add(new Attributes(parent.bigEndian(), src));
        return dstSeq ;
    }

    private Fragments clone(Fragments src, boolean toogleEndian) {
        VR vr = src.vr();
        Fragments dst = new Fragments(vr, parent.bigEndian(), src.size());
        for (byte[] b : src)
            dst.add(toogleEndian ? vr.toggleEndian(b, true) : b);
        return dst ;
    }
    
}
