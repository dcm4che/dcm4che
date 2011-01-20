package org.dcm4che.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.util.StreamUtils;
import org.dcm4che.util.TagUtils;

public class Attributes implements Serializable {

    private static final long serialVersionUID = 5458387005121933754L;

    private static final int INIT_CAPACITY = 16;
    private static final int TO_STRING_LIMIT = 50;
    private static final int TO_STRING_WIDTH = 78;
 
    private transient Attributes parent;
    private transient int[] tags;
    private transient VR[] vrs;
    private transient Object[] values;
    private transient int size;
    private transient SpecificCharacterSet cs = SpecificCharacterSet.DEFAULT;
    private transient long position = -1L;
    private transient int length = -1;
    private transient int[] groupLengths;
    private transient int groupLengthIndex0;
    private transient boolean bigEndian;

    public Attributes() {
        this(false, INIT_CAPACITY);
    }

    public Attributes(boolean bigEndian) {
        this(bigEndian, INIT_CAPACITY);
    }

    public Attributes(int initialCapacity) {
        this(false, initialCapacity);
    }

    public Attributes(boolean bigEndian, int initialCapacity) {
        init(bigEndian, initialCapacity);
    }

    private void init(boolean bigEndian, int initialCapacity) {
        this.bigEndian = bigEndian;
        this.tags = new int[initialCapacity];
        this.vrs = new VR[initialCapacity];
        this.values = new Object[initialCapacity];
    }

    public Attributes(boolean bigEndian, Attributes other) {
        this(bigEndian, other.size);
        addAll(other);
    }

    public final boolean isRoot() {
        return parent == null;
    }

    public final int getLevel() {
        return isRoot() ? 0 : 1 + parent.getLevel();
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    public final Attributes getParent() {
        return parent;
    }

    public final int getLength() {
        return length;
    }

    Attributes setParent(Attributes parent) {
        if (parent != null) {
            if (parent.bigEndian != bigEndian)
                throw new IllegalArgumentException(
                    "Endian of Item must match Endian of parent Data Set");
            if (this.parent != null)
                throw new IllegalArgumentException(
                    "Item already contained by Sequence");
        }
        this.parent = parent;
        return this;
    }

    public final long getPosition() {
        return position;
    }

    public final void setPosition(long position) {
        this.position = position;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public final int size() {
        return size;
    }

    public void trimToSize() {
        trimToSize(false);
    }

    public void trimToSize(boolean recursive) {
        int oldCapacity = tags.length;
        if (size < oldCapacity) {
            tags = Arrays.copyOf(tags, size);
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
                    value = vr.toStrings((byte[]) value, bigEndian,
                            getSpecificCharacterSet());
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
                        vr.toStrings((byte[]) value, bigEndian,
                                getSpecificCharacterSet());
        }
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = tags.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = Math.max(minCapacity, oldCapacity << 1);
            tags = Arrays.copyOf(tags, newCapacity);
            vrs = Arrays.copyOf(vrs, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }
    }

    public Attributes getNestedDataset(List<ItemPointer> itemPointers) {
        Attributes item = this;
        for (ItemPointer ip : itemPointers) {
            Sequence sq = item.getSequence(ip.sequenceTag, ip.privateCreator);
            if (sq == null || ip.itemIndex >= sq.size())
                return null;
            item = sq.get(ip.itemIndex);
        }
        return item;
    }

    private int indexForInsertOf(int tag) {
        return size == 0 ? -1
                : tags[size-1] < tag ? -(size+1)
                        : indexOf(tag);
    }

    private int indexOf(int tag) {
        return Arrays.binarySearch(tags, 0, size, tag);
    }

    private int creatorTagOf(int tag, String privateCreator, boolean reserve) {
        if (!TagUtils.isPrivateTag(tag))
            throw new IllegalArgumentException(TagUtils.toString(tag)
                    + " is not a private Data Element");

        for (int creatorTag = tag & 0xffff0000 | 0x10;
                (creatorTag & 0xff) != 0; creatorTag++) {
            int index = indexOf(creatorTag);
            if (index < 0) {
                if (!reserve)
                    return -1;
                setString(creatorTag, null, VR.LO, privateCreator);
                return creatorTag;
           }
           if (privateCreator.equals(VR.LO.toString(
                   decodeStringValue(index), false, null, 0, null)))
               return creatorTag;
        }
        
        throw new IllegalStateException("No free block for Private Element "
                + TagUtils.toString(tag));
    }

    private Object decodeStringValue(int index) {
        Object value = values[index];
        if (value instanceof byte[])
            values[index] = value =
                vrs[index].toStrings((byte[]) value, bigEndian,
                        getSpecificCharacterSet());
        return value;
    }

    private static boolean isEmpty(Object value) {
        if (value == null)
            return true;

        if (value instanceof Sequence)
            return ((Sequence) value).isEmpty();

        return false;
    }

    public boolean contains(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return false;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        return indexOf(tag) >= 0;
    }

    public boolean contains(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null && item.contains(tag, privateCreator);
    }

    public boolean containsValue(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return false;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        return index >= 0 && !isEmpty(values[index]);
    }

    public boolean containsValue(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null && item.containsValue(tag, privateCreator);
    }

    public String privateCreatorOf(int tag) {
        if (!TagUtils.isPrivateTag(tag))
            return null;

        int creatorTag = (tag & 0xffff0000) | ((tag >>> 8) & 0xff);
        int index = indexOf(creatorTag);
        if (index < 0)
            return null;
        
        return VR.LO.toString(decodeStringValue(index), false, null, 0, null);
    }

    public String privateCreatorOf(int tag, List<ItemPointer> itemPointers) {
        if (!TagUtils.isPrivateTag(tag))
            return null;

        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.privateCreatorOf(tag) : null;
    }

    public Object getValue(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return null;

        return values[index];
    }

    public Object getValue(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getValue(tag, privateCreator) : null;
    }

    public byte[] getBytes(int tag, String privateCreator) throws IOException {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null)
            return BinaryType.EMPTY_BYTES;

        if (value instanceof byte[])
            return (byte[]) value;

        if (value instanceof BulkDataLocator) {
            BulkDataLocator bdl = (BulkDataLocator) value;
            if (bdl.length == 0)
                return BinaryType.EMPTY_BYTES;

            InputStream in = bdl.openStream();
            try {
                StreamUtils.skipFully(in, bdl.offset);
                byte[] b = new byte[bdl.length];
                StreamUtils.readFully(in, b, 0, b.length);
                if (bdl.transferSyntax.equals(UID.ExplicitVRBigEndian)
                        ? !bigEndian : bigEndian)
                    vrs[index].toggleEndian(b, false);
                return b;
            } finally {
                in.close();
            }
        }

        return vrs[index].toBytes(values[index], bigEndian(),
                getSpecificCharacterSet());
    }

    public byte[] getBytes(int tag, String privateCreator,
            List<ItemPointer> itemPointers) throws IOException {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getBytes(tag, privateCreator) : null;
    }

    public String getString(int tag, String privateCreator, int valueIndex,
            String defVal) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return defVal;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == null)
            return defVal;

        VR vr = vrs[index];
        if (vr.isStringType())
            value = decodeStringValue(index);

        return vr.toString(value, bigEndian, getSpecificCharacterSet(),
                valueIndex, defVal);
    }

    public String getString(int tag, String privateCreator, int valueIndex,
            String defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getString(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public String[] getStrings(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value == null)
            return StringType.EMPTY_STRINGS;

        VR vr = vrs[index];
        return toStrings(vr.isStringType()
                ? decodeStringValue(index) 
                : vr.toStrings(value, bigEndian, getSpecificCharacterSet()));
    }

    private static String[] toStrings(Object val) {
        return (val instanceof String) 
                ? new String[] { (String) val } 
                : (String[]) val;
    }

    public String[] getStrings(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getStrings(tag, privateCreator) : null;
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return defVal;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return defVal;

        Object value = values[index];
        if (value == null)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.IS)
            value = decodeStringValue(index);

        return vr.toInt(value, bigEndian, valueIndex, defVal);
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getInt(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public int[] getInts(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
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

    public int[] getInts(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getInts(tag, privateCreator) : null;
    }

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return defVal;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
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

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getFloat(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public float[] getFloats(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
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

    public float[] getFloats(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getFloats(tag, privateCreator) : null;
    }

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return defVal;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
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

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getDouble(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public double[] getDoubles(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
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

    public double[] getDoubles(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getDoubles(tag, privateCreator) : null;
    }

    public Sequence getSequence(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return null;

        return (Sequence) values[index];
    }

    public Sequence getSequence(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getSequence(tag, privateCreator) : null;
    }

    public Fragments getFragments(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return null;

        return (Fragments) values[index];
    }

    public Fragments getFragments(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getFragments(tag, privateCreator) : null;
    }

     public SpecificCharacterSet getSpecificCharacterSet() {
        return cs != null ? cs 
                : parent != null ? parent.getSpecificCharacterSet()
                        : SpecificCharacterSet.DEFAULT;
     }

     public String getPrivateCreator(int tag) {
         return TagUtils.isPrivateTag(tag)
                 ? getString(TagUtils.creatorTagOf(tag), null, 0, null)
                 : null;
     }

     public Object remove(int tag, String privateCreator) {
        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, false);
            if (creatorTag == -1)
                return null;
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }
        int index = indexOf(tag);
        if (index < 0)
            return null;

        Object value = values[index];
        if (value instanceof Sequence)
            ((Sequence) value).clear();

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(tags, index+1, tags, index, numMoved);
            System.arraycopy(vrs, index+1, vrs, index, numMoved);
            System.arraycopy(values, index+1, values, index, numMoved);
        }
        values[--size] = null;

        if (tag == Tag.SpecificCharacterSet)
            cs = null;

        return value;
    }

    public Object remove(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.remove(tag, privateCreator) : null;
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

    public Object setBulkDataLocator(int tag, String privateCreator, VR vr,
            BulkDataLocator bulkDataLocator) {
        return set(tag, privateCreator, vr, bulkDataLocator);
    }

    public Sequence newSequence(int tag, String privateCreator,
            int initialCapacity) {
        Sequence seq = new Sequence(this, initialCapacity);
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
        if (TagUtils.isGroupLength(tag))
            return null;

        if (privateCreator != null) {
            int creatorTag = creatorTagOf(tag, privateCreator, true);
            tag = TagUtils.toPrivateTag(creatorTag, tag);
        }

        Object oldValue = set(tag, vr, value);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
        return oldValue;
    }

    private Object set(int tag, VR vr, Object value) {
        int index = indexForInsertOf(tag);
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
            System.arraycopy(tags, index, tags, index+1, numMoved);
            System.arraycopy(vrs, index, vrs, index+1, numMoved);
            System.arraycopy(values, index, values, index+1, numMoved);
        }
        tags[index] = tag;
        vrs[index] = vr;
        values[index] = value;
        size++;
        return null;
    }

    public void addAll(Attributes other) {
        boolean toggleEndian = bigEndian != other.bigEndian;
        int[] tags = other.tags;
        VR[] srcVRs = other.vrs;
        Object[] srcValues = other.values;
        int otherSize = other.size;
        String privateCreator = null;
        int creatorTag = 0;
        for (int i = 0; i < otherSize; i++) {
            int tag = tags[i];
            if (!TagUtils.isGroupLength(tag)
                    && !TagUtils.isPrivateCreator(tag)) {
                VR vr = srcVRs[i];
                if (TagUtils.isPrivateGroup(tag)) {
                    int tmp = TagUtils.creatorTagOf(tag);
                    if (creatorTag != tmp) {
                        creatorTag = tmp;
                        privateCreator = getString(creatorTag, null, 0, null);
                    }
                } else {
                    creatorTag = 0;
                    privateCreator = null;
                }
                set(tag, privateCreator, vr,
                        copyValue(vr, toggleEndian, srcValues[i]));
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
        Sequence dstSeq = new Sequence(this, srcSeq.size());
        for (Attributes src : srcSeq)
            dstSeq.add(new Attributes(bigEndian, src));
        return dstSeq;
    }

    private Fragments clone(Fragments src, boolean toogleEndian) {
        VR vr = src.vr();
        Fragments dst = new Fragments(vr, bigEndian, src.size());
        for (Object o : src)
            dst.add((toogleEndian && o instanceof byte[]) ? vr.toggleEndian(
                    (byte[]) o, true) : o);
        return dst;
    }

    void initSpecificCharacterSet() {
        cs = null;
        String[] codes = getStrings(Tag.SpecificCharacterSet, null);
        if (codes != null)
            cs = SpecificCharacterSet.valueOf(codes);
    }

    @Override
    public String toString() {
        return toString(TO_STRING_LIMIT, TO_STRING_WIDTH);
    }
    
    public String toString(int limit, int maxWidth) {
        return toStringBuilder(limit, maxWidth, new StringBuilder(1024))
                .toString();
    }

    public StringBuilder toStringBuilder(StringBuilder sb) {
        return toStringBuilder(TO_STRING_LIMIT, TO_STRING_WIDTH, sb);
    }

    public StringBuilder toStringBuilder(int lines, int maxWidth,
            StringBuilder sb) {
        int creatorTag = 0;
        String privateCreator = null;
        for (int i = 0, n = Math.min(size, lines); i < n; i++) {
            int tag = tags[i];
            if (TagUtils.isPrivateTag(tag)) {
                int tmp = TagUtils.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = getString(creatorTag, null, 0, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }
            appendAttribute(tag, privateCreator, vrs[i], values[i],
                    sb.length() + maxWidth, sb).append('\n');
        }
        if (size > lines)
            sb.append("...\n");
        return sb;
    }

    private StringBuilder appendAttribute(int tag, String privateCreator,
            VR vr, Object value, int maxLength, StringBuilder sb) {
        sb.append(TagUtils.toString(tag)).append(' ').append(vr).append(" [");
        if (vr.toStringBuilder(value, bigEndian, getSpecificCharacterSet(),
                maxLength - sb.length() - 1, sb)) {
            sb.append("] ").append(ElementDictionary.keywordOf(
                        tag, privateCreator));
            if (sb.length() > maxLength)
                sb.setLength(maxLength);
        }
        return sb;
    }

    public int calcLength(boolean explicitVR, EncodeOptions encOpts) {
        if (isEmpty())
            return 0;

        if (encOpts.isGroupLength()) { 
            int groupLengthTag = -1;
            int count = 0;
            for (int i = 0; i < size; i++) {
                int tmp = TagUtils.groupLengthTagOf(tags[i]);
                if (groupLengthTag != tmp) {
                    if (groupLengthTag < 0)
                        this.groupLengthIndex0 = count;
                    groupLengthTag = tmp;
                    count++;
                }
            }
            this.groupLengths = new int[count];
        } else {
            this.groupLengths = null;
        }

        int len, totlen = 0;
        int groupLengthTag = -1;
        int groupLengthIndex = - 1;
        VR vr;
        Object val;
        for (int i = 0; i < size; i++) {
            vr = vrs[i];
            val = values[i];
            len = explicitVR ? vr.headerLength() : 8;
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
            } else if (val instanceof BulkDataLocator){
                len += (((BulkDataLocator) val).length + 1) & ~1;
            } else {
                if (!(val instanceof byte[]))
                    values[i] = val = vr.toBytes(val, bigEndian(),
                            getSpecificCharacterSet());
                len += (((byte[]) val).length + 1) & ~1;
            }
            totlen += len;
            if (encOpts.isGroupLength()) {
                int tmp = TagUtils.groupLengthTagOf(tags[i]);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    groupLengthIndex++;
                    totlen += 12;
                }
                groupLengths[groupLengthIndex] += len;
            }
        }
        this.length = totlen;
        return totlen;
    }

     public void writeTo(DicomOutputStream dos, EncodeOptions encOpts)
            throws IOException {
        if (isEmpty())
            return;

        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            writeTo(dos, encOpts, index0, size, groupLengthIndex0);
            writeTo(dos, encOpts, 0, index0, 0);
        } else {
            writeTo(dos, encOpts, 0, size, 0);
        }
    }

    private void writeTo(DicomOutputStream dos, EncodeOptions encOpts,
            int start, int end, int groupLengthIndex0) throws IOException {
        int groupLengthTag = -1;
        int groupLengthIndex = groupLengthIndex0 - 1;
        for (int i = start; i < end; i++) {
            int tag = tags[i];
            int tmp = TagUtils.groupLengthTagOf(tag);
            if (groupLengthTag != tmp) {
                groupLengthTag = tmp;
                groupLengthIndex++;
                if (encOpts.isGroupLength())
                    dos.writeGroupLength(groupLengthTag,
                            groupLengths[groupLengthIndex]);
            }
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
                                : vr.toBytes(val, bigEndian, 
                                        getSpecificCharacterSet()));
        }
    }

    public Attributes createFileMetaInformation(String tsuid) {
        String cuid = getString(Tag.SOPClassUID, null, 0, null);
        String iuid = getString(Tag.SOPInstanceUID, null, 0, null);
        Attributes fmi = new Attributes(1);
        fmi.setBytes(Tag.FileMetaInformationVersion, null, VR.OB,
                new byte[]{ 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, null, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, null, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, null, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, null, VR.UI,
                Implementation.getClassUID());
        fmi.setString(Tag.ImplementationVersionName, null, VR.SH,
                Implementation.getVersionName());
        fmi.trimToSize(false);
        return fmi;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(bigEndian);
        out.writeInt(size);
        DicomOutputStream dout = new DicomOutputStream(out,
                bigEndian ? UID.ExplicitVRBigEndian
                          : UID.ExplicitVRLittleEndian);
        dout.setIncludeBulkDataLocator(true);
        dout.writeDataset(null, this);
        dout.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        init(in.readBoolean(), in.readInt());
        DicomInputStream din = new DicomInputStream(in, 
                bigEndian ? UID.ExplicitVRBigEndian
                          : UID.ExplicitVRLittleEndian);
        din.readAttributes(this, -1, Tag.ItemDelimitationItem);
    }

}
