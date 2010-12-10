package org.dcm4che.data;

import java.io.IOException;
import java.util.Arrays;

import org.dcm4che.io.DicomOutputStream;
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
    private int length;

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

    public final int getLength() {
        return length;
    }

    public int calcLength(boolean explicitVR, EncodeOptions encOpts) {
        if (isEmpty())
            return length = 0;

        int len = encOpts.isGroupLength() ? 12 : 0;
        VR vr;
        Object val;
        for (int i = 0; i < size; i++) {
            vr = vrs[i];
            val = values[i];
            len += explicitVR ? vr.headerLength() : 8;
            if (val == null) {
                if (vr == VR.SQ && encOpts.isUndefEmptySequenceLength())
                    len += 8;
            } else if (val instanceof Sequence) {
                Sequence sq = (Sequence) val;
                len += sq.calcLength(explicitVR, encOpts);
                if (sq.isEmpty() ? encOpts.isUndefEmptySequenceLength()
                                 : encOpts.isUndefSequenceLength())
                    len += 8;
            } else if (val instanceof Fragments) {
                    len += ((Fragments) val).calcLength() + 8;
            } else {
                if (!(val instanceof byte[]))
                    values[i] = val = vr.toBytes(val, bigEndian(), cs());
                len += (((byte[]) val).length + 1) & ~1;
            }
        }
        return length = len;
    }

    public void writeTo(DicomOutputStream dos, EncodeOptions encOpts)
            throws IOException {
        if (isEmpty())
            return;

        if (encOpts.isGroupLength())
            dos.writeGroupLength(TagUtils.toTag(grTag, 0), length - 12);
        for (int i = 0; i < size; i++) {
            int tag = TagUtils.toTag(grTag, elTags[i]);
            VR vr = vrs[i];
            Object val = values[i];
            if (vr == VR.SQ)
                dos.writeSequence(tag, (Sequence) val, encOpts);
            else if (val == null) 
                dos.writeHeader(tag, vr, 0);
            else if (val instanceof Fragments)
                dos.writeFragments(tag, (Fragments) val);
            else if (val instanceof BulkDataLocator)
                dos.writeAttribute(tag, vr, (BulkDataLocator) val);
            else
                dos.writeAttribute(tag, vr, 
                        (val instanceof byte[]) ? (byte[]) val 
                                : vr.toBytes(val, bigEndian(), cs()));
        }
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

    public void internalizeStringValues(boolean decode) {
        Object value;
        VR vr;
        for (int i = 0; i < values.length; i++) {
            if ((value = values[i]) == null)
                continue;
            if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value)
                    item.internalizeStringValues(decode);
            } else if ((vr = vrs[i]).isStringType()) {
                if (value instanceof byte[]) {
                    if (!decode)
                        continue;
                    value = vr.toStrings((byte[]) value, bigEndian(), cs());
                }
                if (value instanceof String)
                    values[i] = ((String) value).intern();
                else {
                    String[] ss = (String[]) value;
                    for (int j = 0; j < ss.length; j++)
                        ss[j] = ss[j].intern();
                }
            }
        }
    }

    public void decodeStringValues() {
        Object value;
        VR vr;
        for (int i = 0; i < values.length; i++) {
            if ((value = values[i]) == null)
                continue;
            if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value)
                    item.decodeStringValues();
            } else if ((vr = vrs[i]).isStringType())
                if (value instanceof byte[])
                    values[i] =
                        vr.toStrings((byte[]) value, bigEndian(), cs());
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
            return null;

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
            return defVal;

        Object value = values[index];
        if (value == null)
            return defVal;

        VR vr = vrs[index];
        if (vr.isStringType())
            value = decodeStringValue(index);

        return vr.toString(value, bigEndian(), cs(), valueIndex, defVal);
    }

    public String[] getStrings(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null)
            return StringType.EMPTY_STRINGS;

        VR vr = vrs[index];
        return toStrings(vr.isStringType() ? decodeStringValue(index) 
                                 : vr.toStrings(value, bigEndian(), cs()));
    }

    private static String[] toStrings(Object val) {
        return (val instanceof String) 
                ? new String[] { (String) val } 
                : (String[]) val;
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == null)
            return defVal;
        
        VR vr = vrs[index];
        if (vr == VR.IS)
            value = decodeStringValue(index);

        return vr.toInt(value, bigEndian(), valueIndex, defVal);
    }

    private Object decodeStringValue(int index) {
        Object value = values[index];
        if (value instanceof byte[])
            values[index] = value =
                vrs[index].toStrings((byte[]) value, bigEndian(), cs());
        return value;
    }

    public int[] getInts(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_INTS;

        VR vr = vrs[index];
        if (vr == VR.IS)
            value = decodeStringValue(index);

        return vr.toInts(value, bigEndian());
     }

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == null)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toFloat(value, bigEndian(), valueIndex, defVal);
    }

    public float[] getFloats(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_FLOATS;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toFloats(value, bigEndian());
    }

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == null)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toDouble(value, bigEndian(), valueIndex, defVal);
    }

    public double[] getDoubles(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_DOUBLES;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toDoubles(value, bigEndian());
    }

    public Sequence getSequence(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

        return (Sequence) values[index];
    }

    public Fragments getFragments(int tag, String privateCreator) {
        int index = indexOf(tag, privateCreator);
        if (index < 0)
            return null;

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
        return set(tag, privateCreator, vr, null);
    }

    public Object setBytes(int tag, String privateCreator, VR vr, byte[] b) {
        return set(tag, privateCreator, vr, vr.toValue(b));
    }

    public Object setString(int tag, String privateCreator, VR vr, String s) {
        return set(tag, privateCreator, vr, vr.toValue(s, bigEndian()));
    }

    public Object setString(int tag, String privateCreator, VR vr,
            String... ss) {
        return set(tag, privateCreator, vr, vr.toValue(ss, bigEndian()));
    }

    public Object setInt(int tag, String privateCreator, VR vr, int... ints) {
        return set(tag, privateCreator, vr, vr.toValue(ints, bigEndian()));
    }

    public Object setFloat(int tag, String privateCreator, VR vr,
            float... floats) {
        return set(tag, privateCreator, vr, vr.toValue(floats, bigEndian()));
    }

    public Object setDouble(int tag, String privateCreator, VR vr,
            double[] doubles) {
        return set(tag, privateCreator, vr, vr.toValue(doubles, bigEndian()));
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

    public Object set(int tag, String privateCreator, VR vr, Object value) {
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
            if (privateCreator.equals(VR.LO.toString(
                            decodeStringValue(index), false, null, 0, null)))
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
        
        return VR.LO.toString(decodeStringValue(index), false, null, 0, null);
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
        for (Object o : src)
            dst.add((toogleEndian && o instanceof byte[])
                    ? vr.toggleEndian((byte[]) o, true)
                    : o);
        return dst ;
    }
}
