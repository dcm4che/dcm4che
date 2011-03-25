package org.dcm4che.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.util.StringUtils;
import org.dcm4che.util.TagUtils;

public class Attributes implements Serializable {

    private static final int INIT_CAPACITY = 16;
    private static final int TO_STRING_LIMIT = 50;
    private static final int TO_STRING_WIDTH = 78;

    private static final int[] EMPTY_INTS = {};
    private static final float[] EMPTY_FLOATS = {};
    private static final double[] EMPTY_DOUBLES = {};
    private static final String[] EMPTY_STRINGS = {};
 
    private transient Attributes parent;
    private transient int[] tags;
    private transient VR[] vrs;
    private transient Object[] values;
    private transient int size;
    private transient SpecificCharacterSet cs = SpecificCharacterSet.DEFAULT;
    private transient int length = -1;
    private transient int[] groupLengths;
    private transient int groupLengthIndex0;

    private boolean bigEndian;
    private long itemPosition = -1;

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
        this.bigEndian = bigEndian;
        init(initialCapacity);
    }

    private void init(int initialCapacity) {
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

    public final long getItemPosition() {
        return itemPosition;
    }

    public final void setItemPosition(long itemPosition) {
        this.itemPosition = itemPosition;
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
        SpecificCharacterSet cs = getSpecificCharacterSet();
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
                    value = vr.toStrings((byte[]) value, bigEndian, cs);
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
        SpecificCharacterSet cs = getSpecificCharacterSet();
        for (int i = 0; i < values.length; i++) {
            if ((value = values[i]) == null)
                continue;
            if (value instanceof Sequence) {
                for (Attributes item : (Sequence) value)
                    item.decodeStringValues();
            } else if ((vr = vrs[i]).isStringType())
                if (value instanceof byte[])
                    values[i] =
                        vr.toStrings((byte[]) value, bigEndian, cs);
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
            Sequence sq = (Sequence) item.getValue(ip.sequenceTag,
                    ip.privateCreator);
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
        if (!TagUtils.isPrivateGroup(tag))
            throw new IllegalArgumentException(TagUtils.toString(tag)
                    + " is not a private Data Element");

        for (int creatorTag = tag & 0xffff0000 | 0x10;
                (creatorTag & 0xff) != 0; creatorTag++) {
            int index = indexOf(creatorTag);
            if (index < 0) {
                if (!reserve)
                    return -1;
                setString(creatorTag, VR.LO, privateCreator);
                return creatorTag;
           }
           if (privateCreator.equals(VR.LO.toString(
                   decodeStringValue(index), false, 0, null)))
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
        return (value instanceof Value) && ((Value) value).isEmpty();
    }

    public boolean contains(int tag) {
        return contains(tag, null);
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

    public boolean contains(List<ItemPointer> itemPointers, int tag) {
        return contains(itemPointers, tag, null);
    }

    public boolean contains(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null && item.contains(tag, privateCreator);
    }

    public boolean containsValue(int tag) {
        return containsValue(tag, null);
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

    public boolean containsValue(List<ItemPointer> itemPointers, int tag) {
        return containsValue(itemPointers, tag, null);
    }

    public boolean containsValue(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
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
        
        return VR.LO.toString(decodeStringValue(index), false, 0, null);
    }

    public String privateCreatorOf(List<ItemPointer> itemPointers, int tag) {
        if (!TagUtils.isPrivateTag(tag))
            return null;

        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.privateCreatorOf(tag) : null;
    }

    public Object getValue(int tag) {
        return getValue(tag, null);
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

    public Object getValue(List<ItemPointer> itemPointers, int tag) {
        return getValue(itemPointers, tag, null);
    }

    public Object getValue(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getValue(tag, privateCreator) : null;
    }

    public byte[] getBytes(int tag) throws IOException {
        return getBytes(tag, null);
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
        VR vr = vrs[index];

        if (value instanceof Value)
            return ((Value) value).toBytes(vr, bigEndian);

        return vr.toBytes(value, getSpecificCharacterSet());
    }

    public byte[] getBytes(List<ItemPointer> itemPointers, int tag)
            throws IOException {
        return getBytes(itemPointers, tag, null);
    }

    public byte[] getBytes(List<ItemPointer> itemPointers, int tag,
            String privateCreator) throws IOException {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getBytes(tag, privateCreator) : null;
    }

    public String getString(int tag, String defVal) {
        return getString(tag, null, 0, defVal);
    }

    public String getString(int tag, int valueIndex, String defVal) {
        return getString(tag, null, valueIndex, defVal);
    }

    public String getString(int tag, String privateCreator, String defVal) {
        return getString(tag, privateCreator, 0, defVal);
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
        if (value == Value.NULL)
            return defVal;

        VR vr = vrs[index];
        if (vr.isStringType())
            value = decodeStringValue(index);

        return vr.toString(value, bigEndian, valueIndex, defVal);
    }

    public String getString(List<ItemPointer> itemPointers, int tag,
            String defVal) {
        return getString(itemPointers, tag, null, 0, defVal);
    }

    public String getString(List<ItemPointer> itemPointers, int tag,
            int valueIndex, String defVal) {
        return getString(itemPointers, tag, null, valueIndex, defVal);
    }

    public String getString(List<ItemPointer> itemPointers, int tag,
            String privateCreator, String defVal) {
        return getString(itemPointers, tag, privateCreator, 0, defVal);
    }

    public String getString(List<ItemPointer> itemPointers, int tag,
            String privateCreator, int valueIndex, String defVal) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getString(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public String[] getStrings(int tag) {
        return getStrings(tag, null);
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
        if (value == Value.NULL)
            return EMPTY_STRINGS;

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

    public String[] getStrings(List<ItemPointer> itemPointers, int tag) {
        return getStrings(itemPointers, tag, null);
    }

    public String[] getStrings(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getStrings(tag, privateCreator) : null;
    }

    public int getInt(int tag, int defVal) {
        return getInt(tag, null, 0, defVal);
    }

    public int getInt(int tag, int valueIndex, int defVal) {
        return getInt(tag, null, valueIndex, defVal);
    }

    public int getInt(int tag, String privateCreator, int defVal) {
        return getInt(tag, privateCreator, 0, defVal);
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
        if (value == Value.NULL)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.IS)
            value = decodeStringValue(index);

        return vr.toInt(value, bigEndian, valueIndex, defVal);
    }

    public int getInt(List<ItemPointer> itemPointers, int tag, int defVal) {
        return getInt(itemPointers, tag, null, 0, defVal);
    }

    public int getInt(List<ItemPointer> itemPointers, int tag, int valueIndex,
            int defVal) {
        return getInt(itemPointers, tag, null, valueIndex, defVal);
    }

    public int getInt(List<ItemPointer> itemPointers, int tag,
            String privateCreator, int defVal) {
        return getInt(itemPointers, tag, privateCreator, 0, defVal);
    }

    public int getInt(List<ItemPointer> itemPointers, int tag,
            String privateCreator, int valueIndex, int defVal) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getInt(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public int[] getInts(int tag) {
        return getInts(tag, null);
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
        if (value == Value.NULL)
            return EMPTY_INTS;

        VR vr = vrs[index];
        if (vr == VR.IS)
            value = decodeStringValue(index);

        return vr.toInts(value, bigEndian);
    }

    public int[] getInts(List<ItemPointer> itemPointers, int tag) {
        return getInts(itemPointers, tag, null);
    }

    public int[] getInts(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getInts(tag, privateCreator) : null;
    }

    public float getFloat(int tag, float defVal) {
        return getFloat(tag, null, 0, defVal);
    }

    public float getFloat(int tag, int valueIndex, float defVal) {
        return getFloat(tag, null, valueIndex, defVal);
    }

    public float getFloat(int tag, String privateCreator, float defVal) {
        return getFloat(tag, privateCreator, 0, defVal);
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
        if (value == Value.NULL)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toFloat(value, bigEndian, valueIndex, defVal);
    }

    public float getFloat(List<ItemPointer> itemPointers, int tag,
            float defVal) {
        return getFloat(itemPointers, tag, null, 0, defVal);
    }

    public float getFloat(List<ItemPointer> itemPointers, int tag,
            int valueIndex, float defVal) {
        return getFloat(itemPointers, tag, null, valueIndex, defVal);
    }

    public float getFloat(List<ItemPointer> itemPointers, int tag,
            String privateCreator, float defVal) {
        return getFloat(itemPointers, tag, privateCreator, 0, defVal);
    }

    public float getFloat(List<ItemPointer> itemPointers, int tag,
            String privateCreator, int valueIndex, float defVal) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getFloat(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public float[] getFloats(int tag) {
        return getFloats(tag, null);
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
        if (value == Value.NULL)
            return EMPTY_FLOATS;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toFloats(value, bigEndian);
    }

    public float[] getFloats(List<ItemPointer> itemPointers, int tag) {
        return getFloats(itemPointers, tag, null);
    }

    public float[] getFloats(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getFloats(tag, privateCreator) : null;
    }

    public double getDouble(int tag, double defVal) {
        return getDouble(tag, null, 0, defVal);
    }

    public double getDouble(int tag, int valueIndex, double defVal) {
        return getDouble(tag, null, valueIndex, defVal);
    }

    public double getDouble(int tag, String privateCreator, double defVal) {
        return getDouble(tag, privateCreator, 0, defVal);
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
        if (value == Value.NULL)
            return defVal;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toDouble(value, bigEndian, valueIndex, defVal);
    }

    public double getDouble(List<ItemPointer> itemPointers, int tag,
            double defVal) {
        return getDouble(itemPointers, tag, null, 0, defVal);
    }

    public double getDouble(List<ItemPointer> itemPointers, int tag,
            int valueIndex, double defVal) {
        return getDouble(itemPointers, tag, null, valueIndex, defVal);
    }

    public double getDouble(List<ItemPointer> itemPointers, int tag,
            String privateCreator, double defVal) {
        return getDouble(itemPointers, tag, privateCreator, 0, defVal);
    }

    public double getDouble(List<ItemPointer> itemPointers, int tag,
            String privateCreator, int valueIndex, double defVal) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getDouble(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public double[] getDoubles(int tag) {
        return getDoubles(tag, null);
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
        if (value == Value.NULL)
            return EMPTY_DOUBLES;

        VR vr = vrs[index];
        if (vr == VR.DS)
            value = decodeStringValue(index);

        return vr.toDoubles(value, bigEndian);
    }

    public double[] getDoubles(List<ItemPointer> itemPointers, int tag) {
        return getDoubles(itemPointers, tag, null);
    }

    public double[] getDoubles(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getDoubles(tag, privateCreator) : null;
    }


     public SpecificCharacterSet getSpecificCharacterSet() {
        return cs != null ? cs 
                : parent != null ? parent.getSpecificCharacterSet()
                        : SpecificCharacterSet.DEFAULT;
     }

     public String getPrivateCreator(int tag) {
         return TagUtils.isPrivateTag(tag)
                 ? getString(TagUtils.creatorTagOf(tag), null)
                 : null;
     }

     public Object remove(int tag) {
         return remove(tag, null);
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

     public Object remove(List<ItemPointer> itemPointers, int tag) {
         return remove(itemPointers, tag, null);
     }

    public Object remove(List<ItemPointer> itemPointers, int tag,
            String privateCreator) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.remove(tag, privateCreator) : null;
    }

    public Object setNull(int tag, VR vr) {
        return setNull(tag, null, vr);
    }

    public Object setNull(int tag, String privateCreator, VR vr) {
        return set(tag, privateCreator, vr, Value.NULL);
    }

    public Object setBytes(int tag, VR vr, byte[] b) {
        return setBytes(tag, null, vr, b);
    }

    public Object setBytes(int tag, String privateCreator, VR vr, byte[] b) {
        return set(tag, privateCreator, vr, vr.toValue(b));
    }

    public Object setString(int tag, VR vr, String s) {
        return setString(tag, null, vr, s);
    }

    public Object setString(int tag, String privateCreator, VR vr, String s) {
        return set(tag, privateCreator, vr, vr.toValue(s, bigEndian));
    }

    public Object setString(int tag, VR vr, String... ss) {
        return setString(tag, null, vr, ss);
    }

    public Object setString(int tag, String privateCreator, VR vr,
            String... ss) {
        return set(tag, privateCreator, vr, vr.toValue(ss, bigEndian));
    }

    public Object setInt(int tag, VR vr, int... is) {
        return setInt(tag, null, vr, is);
    }

    public Object setInt(int tag, String privateCreator, VR vr, int... is) {
        return set(tag, privateCreator, vr, vr.toValue(is, bigEndian));
    }

    public Object setFloat(int tag, VR vr, float... fs) {
        return setFloat(tag, null, vr, fs);
    }

    public Object setFloat(int tag, String privateCreator, VR vr,
            float... fs) {
        return set(tag, privateCreator, vr, vr.toValue(fs, bigEndian));
    }

    public Object setDouble(int tag, VR vr, double[] ds) {
        return setDouble(tag, null, vr, ds);
    }

    public Object setDouble(int tag, String privateCreator, VR vr,
            double[] ds) {
        return set(tag, privateCreator, vr, vr.toValue(ds, bigEndian));
    }

    public Object setValue(int tag, VR vr, Value value) {
        return setValue(tag, null, vr, value);
    }

    public Object setValue(int tag, String privateCreator, VR vr, Value value) {
        return set(tag, privateCreator, vr, value != null ? value : Value.NULL);
    }

    public Sequence newSequence(int tag, int initialCapacity) {
        return newSequence(tag, null, initialCapacity);
    }

    public Sequence newSequence(int tag, String privateCreator,
            int initialCapacity) {
        Sequence seq = new Sequence(this, initialCapacity);
        set(tag, privateCreator, VR.SQ, seq);
        return seq;
    }

    public Fragments newFragments(int tag, VR vr, int initialCapacity) {
        return newFragments(tag, null, vr, initialCapacity);
    }

    public Fragments newFragments(int tag, String privateCreator, VR vr,
            int initialCapacity) {
        Fragments frags = new Fragments(vr, bigEndian, initialCapacity);
        set(tag, privateCreator, vr, frags);
        return frags;
    }

    private Object set(int tag, String privateCreator, VR vr, Object value) {
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
            if (!TagUtils.isPrivateCreator(tag)) {
                VR vr = srcVRs[i];
                if (TagUtils.isPrivateGroup(tag)) {
                    int tmp = TagUtils.creatorTagOf(tag);
                    if (creatorTag != tmp) {
                        creatorTag = tmp;
                        privateCreator = other.getString(creatorTag, null);
                    }
                } else {
                    creatorTag = 0;
                    privateCreator = null;
                }
                Object value = srcValues[i];
                if (value instanceof Sequence) {
                    set(tag, privateCreator, (Sequence) value);
                } else if (value instanceof Fragments) {
                    set(tag, privateCreator, (Fragments) value);
                } else {
                    set(tag, privateCreator, vr,
                            (value instanceof byte[] && toggleEndian)
                                    ? vr.toggleEndian((byte[]) value, true)
                                    : value);
                }
            }
        }
    }

    private void set(int tag, String privateCreator, Sequence src) {
        Sequence dst = newSequence(tag, privateCreator, src.size());
        for (Attributes item : src)
            dst.add(new Attributes(bigEndian, item));
    }

    private void set(int tag, String privateCreator, Fragments src) {
        boolean toogleEndian = src.bigEndian() != bigEndian;
        VR vr = src.vr();
        Fragments dst = newFragments(tag, privateCreator, vr, src.size());
        for (Object frag : src)
            dst.add((frag instanceof byte[] && toogleEndian) 
                    ? vr.toggleEndian((byte[]) frag, true)
                    : frag);
    }

    void initSpecificCharacterSet() {
        cs = null;
        String[] codes = getStrings(Tag.SpecificCharacterSet);
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
                    privateCreator = getString(creatorTag, null);
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
        if (vr.prompt(value, bigEndian, getSpecificCharacterSet(),
                maxLength - sb.length() - 1, sb)) {
            sb.append("] ").append(ElementDictionary.keywordOf(
                        tag, privateCreator));
            if (sb.length() > maxLength)
                sb.setLength(maxLength);
        }
        return sb;
    }

    public int calcLength(DicomOutputStream out) {
        if (isEmpty())
            return 0;

        this.groupLengths = out.isEncodeGroupLength() 
                ? new int[countGroups()]
                : null;
        this.length = calcLength(out, getSpecificCharacterSet(), groupLengths);
        return this.length;
    }

    private int calcLength(DicomOutputStream out, SpecificCharacterSet cs, int[] groupLengths) {
        int len, totlen = 0;
        int groupLengthTag = -1;
        int groupLengthIndex = -1;
        boolean explicitVR = out.isExplicitVR();
        VR vr;
        Object val;
        for (int i = 0; i < size; i++) {
            vr = vrs[i];
            val = values[i];
            len = explicitVR ? vr.headerLength() : 8;
            if (val instanceof Value)
                len += ((Value) val).calcLength(out, vr);
            else {
                if (!(val instanceof byte[]))
                    values[i] = val = vr.toBytes(val, cs);
                len += (((byte[]) val).length + 1) & ~1;
            }
            totlen += len;
            if (groupLengths != null) {
                int tmp = TagUtils.groupLengthTagOf(tags[i]);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    groupLengthIndex++;
                    totlen += 12;
                }
                groupLengths[groupLengthIndex] += len;
            }
        }
        return totlen;
    }

    private int countGroups() {
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
        return count;
    }

    public void writeTo(DicomOutputStream dos)
            throws IOException {
        if (isEmpty())
            return;

        if (dos.isEncodeGroupLength() && groupLengths == null)
            throw new IllegalStateException(
                    "groupLengths not initialized by calcLength()");

        SpecificCharacterSet cs = getSpecificCharacterSet();
        if (tags[0] < 0) {
            int index0 = -(1 + indexOf(0));
            writeTo(dos, cs, index0, size, groupLengthIndex0);
            writeTo(dos, cs, 0, index0, 0);
        } else {
            writeTo(dos, cs, 0, size, 0);
        }
    }

     public void writeItemTo(DicomOutputStream out) throws IOException {
         int len = isEmpty() ? out.isUndefEmptyItemLength() ? -1 : 0
                 : out.isUndefItemLength() ? -1 : length;
         out.writeHeader(Tag.Item, null, len);
         writeTo(out);
         if (len == -1)
             out.writeHeader(Tag.ItemDelimitationItem, null, 0);
     }

    private void writeTo(DicomOutputStream out, SpecificCharacterSet cs,
            int start, int end, int groupLengthIndex) throws IOException {
        boolean groupLength = groupLengths != null;
        int groupLengthTag = -1;
        for (int i = start; i < end; i++) {
            int tag = tags[i];
            if (groupLength) {
                int tmp = TagUtils.groupLengthTagOf(tag);
                if (groupLengthTag != tmp) {
                    groupLengthTag = tmp;
                    out.writeGroupLength(groupLengthTag,
                            groupLengths[groupLengthIndex++]);
                }
            }
            out.writeAttribute(tag, vrs[i], values[i], cs);
        }
    }


    public void writeGroupTo(DicomOutputStream out, int groupLengthTag)
            throws IOException {
        if (isEmpty())
            throw new IllegalStateException("No attributes");
        
        checkInGroup(0, groupLengthTag);
        checkInGroup(size-1, groupLengthTag);
        SpecificCharacterSet cs = getSpecificCharacterSet();
        out.writeGroupLength(groupLengthTag, calcLength(out, cs, null));
        writeTo(out, cs, 0, size, 0);
    }


    private void checkInGroup(int i, int groupLengthTag) {
        int tag = tags[i];
        if (TagUtils.groupLengthTagOf(tag) != groupLengthTag)
            throw new IllegalStateException(TagUtils.toString(tag)
                    + " does not belong to group (" 
                    + TagUtils.shortToHexString(
                            TagUtils.groupNumber(groupLengthTag))
                    + ",eeee).");
        
    }

    public Attributes createFileMetaInformation(String tsuid) {
        return createFileMetaInformation(
                getString(Tag.SOPInstanceUID, null),
                getString(Tag.SOPClassUID, null),
                tsuid);
    }

    public static Attributes createFileMetaInformation(String iuid,
            String cuid, String tsuid) {
        if (iuid.isEmpty() || cuid.isEmpty() || tsuid.isEmpty())
            throw new IllegalArgumentException();

        Attributes fmi = new Attributes(6);
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB,
                new byte[]{ 0, 1 });
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, VR.UI,
                Implementation.getClassUID());
        fmi.setString(Tag.ImplementationVersionName, VR.SH,
                Implementation.getVersionName());
        return fmi;
    }

    public boolean matches(Attributes keys, boolean ignorePNCase) {
        int[] keyTags = keys.tags;
        VR[] keyVrs = keys.vrs;
        Object[] keyValues = keys.values;
        int keysSize = keys.size;
        String privateCreator = null;
        int creatorTag = 0;
        for (int i = 0; i < keysSize; i++) {
            int tag = keyTags[i];
            if (TagUtils.isPrivateCreator(tag))
                continue;

            if (TagUtils.isPrivateGroup(tag)) {
                int tmp = TagUtils.creatorTagOf(tag);
                if (creatorTag != tmp) {
                    creatorTag = tmp;
                    privateCreator = keys.getString(creatorTag, null);
                }
            } else {
                creatorTag = 0;
                privateCreator = null;
            }

            Object keyValue = keyValues[i];
            if (isEmpty(keyValue))
                continue;

            if (keyVrs[i].isStringType()) {
                if (!matches(tag, privateCreator, keyVrs[i], ignorePNCase,
                        keys.getStrings(tag, privateCreator)))
                    return false;
            } else if (keyValue instanceof Sequence) {
                if (!matches(tag, privateCreator, ignorePNCase,
                        (Sequence) keyValue))
                    return false;
            } else {
                throw new UnsupportedOperationException("Keys with VR: "
                        + keyVrs[i] + " not supported");
            }
        }
        return true;
    }

    private boolean matches(int tag, String privateCreator, VR vr,
            boolean ignorePNCase, String[] keyVals) {
        if (keyVals.length > 1)
            throw new IllegalArgumentException("Keys contain Attribute "
                    + TagUtils.toString(tag) + " with " + keyVals.length
                    + " values");

        String[] vals = getStrings(tag, privateCreator);
        if (vals == null || vals.length == 0)
            return true;

        boolean ignoreCase = ignorePNCase && vr == VR.PN;
        String keyVal = vr == VR.PN
                ? new PersonName(keyVals[0]).toString()
                : keyVals[0];

        if (StringUtils.containsWildCard(keyVal)) {
            Pattern pattern = StringUtils.compilePattern(keyVal, ignoreCase);
            for (String val : vals) {
                if (val == null)
                    return true;
                if (vr == VR.PN)
                    val = new PersonName(val).toString();
                if (pattern.matcher(val).matches())
                    return true;
            }
        } else {
            for (String val : vals) {
                if (val == null)
                    return true;
                if (vr == VR.PN)
                    val = new PersonName(val).toString();
                if (ignoreCase ? keyVal.equalsIgnoreCase(val)
                               : keyVal.equals(val))
                    return true;
            }
        }
        return false;
    }

    private boolean matches(int tag, String privateCreator,
            boolean ignorePNCase, Sequence keySeq) {
        int n = keySeq.size();
        if (n > 1)
            throw new IllegalArgumentException("Keys contain Sequence "
                    + TagUtils.toString(tag) + " with " + n + " Items");

        Attributes keys = keySeq.get(0);
        if (keys.isEmpty())
            return true;

        Object value = getValue(tag, privateCreator);
        if (value == null || isEmpty(value))
            return true;

        if (value instanceof Sequence) {
            Sequence sq = (Sequence) value;
            for (Attributes item : sq)
                if (item.matches(keys, ignorePNCase));
        }
        return false;
    }

    private static final long serialVersionUID = 7868714416968825241L;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size);
        DicomOutputStream dout = new DicomOutputStream(out,
                bigEndian ? UID.ExplicitVRBigEndian
                          : UID.ExplicitVRLittleEndian);
        dout.writeDataset(null, this);
        dout.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init(in.readInt());
        DicomInputStream din = new DicomInputStream(in, 
                bigEndian ? UID.ExplicitVRBigEndian
                          : UID.ExplicitVRLittleEndian);
        din.readAttributes(this, -1, Tag.ItemDelimitationItem);
    }
}
