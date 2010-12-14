package org.dcm4che.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomOutputStream;
import org.dcm4che.util.TagUtils;

public class Attributes implements Serializable {

    private static final long serialVersionUID = 5458387005121933754L;

    private static final int INIT_CAPACITY = 10;
    private static final int TO_STRING_LIMIT = 50;
    private static final byte[] FMI_VERS = { 0, 1 };

    private transient Attributes parent;
    private transient Group[] groups;
    private transient int groupsSize;
    private transient SpecificCharacterSet cs = SpecificCharacterSet.DEFAULT;
    private transient long position = -1L;
    private transient int length = -1;

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
        this.bigEndian = bigEndian;
        this.groups = new Group[initialCapacity];
    }

    public Attributes(boolean bigEndian, Attributes other) {
        this(bigEndian, other.groupsSize);
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

   public boolean isEmpty() {
        if (groupsSize == 0)
            return true;

        for (int i = 0; i < groupsSize; i++)
            if (!groups[i].isEmpty())
                return false;

        return true;
    }

    public void trimToSize() {
        trimToSize(false);
    }

    public void trimToSize(boolean recursive) {
        int oldCapacity = groups.length;
        int newCount = 0;
        for (int i = 0; i < groupsSize; i++) {
            Group group = groups[i];
            if (!group.isEmpty()) {
                group.trimToSize(recursive);
                newCount++;
            }
        }
        if (newCount < oldCapacity) {
            Group[] newGroups = new Group[newCount];
            for (int i = 0, j = 0; i < groupsSize; i++) {
                Group group = groups[i];
                if (!group.isEmpty())
                    newGroups[j++] = group;
            }
            groups = newGroups;
            groupsSize = newCount;
        }
    }

    public void internalizeStringValues(boolean decode) {
        for (int i = 0; i < groupsSize; i++)
            groups[i].internalizeStringValues(decode);
    }

    public void decodeStringValues() {
        for (int i = 0; i < groupsSize; i++)
            groups[i].decodeStringValues();
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = groups.length;
        if (minCapacity > oldCapacity)
            groups = Arrays.copyOf(groups, Math.max(minCapacity,
                    (oldCapacity * 3) / 2 + 1));
    }

    private int indexOf(int groupNumber) {
        int low = 0;
        int high = groupsSize - 1;
        Group[] g = groups;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midGroupNumber = g[mid].getGroupNumber();

            if (midGroupNumber < groupNumber)
                low = mid + 1;
            else if (midGroupNumber > groupNumber)
                high = mid - 1;
            else
                return mid;
        }
        return -(low + 1);
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

    public boolean contains(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        return index >= 0 && groups[index].contains(tag, privateCreator);
    }

    public boolean contains(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null && item.contains(tag, privateCreator);
    }

    public boolean containsValue(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        return index >= 0 && groups[index].containsValue(tag, privateCreator);
    }

    public boolean containsValue(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null && item.containsValue(tag, privateCreator);
    }

    public String getPrivateCreator(int tag) {
        if (!TagUtils.isPrivateTag(tag))
            return null;

        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getPrivateCreator(tag);
    }

    public String getPrivateCreator(int tag, List<ItemPointer> itemPointers) {
        if (!TagUtils.isPrivateTag(tag))
            return null;

        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getPrivateCreator(tag) : null;
    }

    public Object getValue(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getValue(tag, privateCreator);
    }

    public Object getValue(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getValue(tag, privateCreator) : null;
    }

    public byte[] getBytes(int tag, String privateCreator) throws IOException {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getBytes(tag, privateCreator);
    }

    public byte[] getBytes(int tag, String privateCreator,
            List<ItemPointer> itemPointers) throws IOException {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getBytes(tag, privateCreator) : null;
    }

    public String getString(int tag, String privateCreator, int valueIndex,
            String defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index]
                      .getString(tag, privateCreator, valueIndex, defVal);
    }

    public String getString(int tag, String privateCreator, int valueIndex,
            String defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getString(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public String[] getStrings(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getStrings(tag, privateCreator);
    }

    public String[] getStrings(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getStrings(tag, privateCreator) : null;
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getInt(tag, privateCreator, valueIndex, defVal);
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getInt(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public int[] getInts(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getInts(tag, privateCreator);
    }

    public int[] getInts(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getInts(tag, privateCreator) : null;
    }

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getFloat(tag, privateCreator, valueIndex, defVal);
    }

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getFloat(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public float[] getFloats(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getFloats(tag, privateCreator);
    }

    public float[] getFloats(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getFloats(tag, privateCreator) : null;
    }

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getDouble(tag, privateCreator, valueIndex,
                defVal);
    }

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal, List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null
                ? item.getDouble(tag, privateCreator, valueIndex, defVal)
                : defVal;
    }

    public double[] getDoubles(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getDoubles(tag, privateCreator);
    }

    public double[] getDoubles(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getDoubles(tag, privateCreator) : null;
    }

    public Sequence getSequence(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getSequence(tag, privateCreator);
    }

    public Sequence getSequence(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.getSequence(tag, privateCreator) : null;
    }

    public Fragments getFragments(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getFragments(tag, privateCreator);
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

    public Object remove(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return false;

        Object oldValue =  groups[index].remove(tag, privateCreator);
        if (tag == Tag.SpecificCharacterSet)
            cs = null;

        return oldValue;
    }

    public Object remove(int tag, String privateCreator,
            List<ItemPointer> itemPointers) {
        Attributes item = getNestedDataset(itemPointers);
        return item != null ? item.remove(tag, privateCreator) : null;
    }

    public Object setNull(int tag, String privateCreator, VR vr) {
        int groupNumber = TagUtils.groupNumber(tag);
        Object oldValue = getOrCreateGroup(groupNumber)
                .setNull(tag, privateCreator, vr);
        if (tag == Tag.SpecificCharacterSet)
            cs = null;
        return oldValue;
    }

    public Object setBytes(int tag, String privateCreator, VR vr,
            byte[] value) {
        int groupNumber = TagUtils.groupNumber(tag);
        Object oldValue = getOrCreateGroup(groupNumber)
                .setBytes(tag, privateCreator, vr, value);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
        return oldValue;
    }

    public Object setString(int tag, String privateCreator, VR vr,
            String val) {
        int groupNumber = TagUtils.groupNumber(tag);
        Object oldValue = getOrCreateGroup(groupNumber)
                .setString(tag, privateCreator, vr, val);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
        return oldValue;
    }

    public Object setString(int tag, String privateCreator, VR vr,
            String... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        Object oldValue = getOrCreateGroup(groupNumber)
                .setString(tag, privateCreator, vr,
                value);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
        return oldValue;
    }

    public Object setInt(int tag, String privateCreator, VR vr, int... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber)
                .setInt(tag, privateCreator, vr, value);
    }

    public Object setFloat(int tag, String privateCreator, VR vr,
            float... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber)
                .setFloat(tag, privateCreator, vr, value);
    }

    public Object setBulkDataLocator(int tag, String privateCreator, VR vr,
            BulkDataLocator bulkDataLocator) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber)
                .set(tag, privateCreator, vr, bulkDataLocator);
        
    }

    public Sequence newSequence(int tag, String privateCreator,
            int initialCapacity) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber).newSequence(tag, privateCreator,
                initialCapacity);
    }

    public Fragments newFragments(int tag, String privateCreator, VR vr,
            boolean bigEndian, int initialCapacity) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber).newFragments(tag, privateCreator,
                vr, bigEndian, initialCapacity);
    }

    public void addAll(Attributes other) {
        boolean toggleEndian = bigEndian != other.bigEndian;
        Group[] srcGroups = other.groups;
        int srcGroupSize = other.groupsSize;
        for (int i = 0; i < srcGroupSize; i++) {
            Group srcGroup = srcGroups[i];
            int groupNumber = srcGroup.getGroupNumber();
            getOrCreateGroup(groupNumber, srcGroupSize)
                    .addAll(srcGroup, toggleEndian);
        }
    }

    void initSpecificCharacterSet() {
        cs = null;
        String[] codes = getStrings(Tag.SpecificCharacterSet, null);
        if (codes != null)
            cs = SpecificCharacterSet.valueOf(codes);
    }

    private Group getOrCreateGroup(int groupNumber) {
        return getOrCreateGroup(groupNumber, Group.INIT_CAPACITY);
    }

    private Group getOrCreateGroup(int groupNumber, int capacity) {
        int index = groupsSize;
        if (index != 0) {
            Group lastGroup = groups[index - 1];
            int lastGroupNumber = lastGroup.getGroupNumber();
            if (groupNumber == lastGroupNumber)
                return lastGroup;

            if (groupNumber < lastGroupNumber) {
                index = indexOf(groupNumber);
                if (index >= 0)
                    return groups[index];

                index = -index - 1;
            }
        }
        ensureCapacity(groupsSize + 1);
        int numMoved = groupsSize - index;
        if (numMoved > 0)
            System.arraycopy(groups, index, groups, index + 1, numMoved);
        groups[index] = new Group(this, groupNumber, capacity);
        groupsSize++;
        return groups[index];
    }

    @Override
    public String toString() {
        return toString(TO_STRING_LIMIT, Group.TO_STRING_WIDTH);
    }
    
    public String toString(int limit, int maxWidth) {
        return toStringBuilder(limit, maxWidth, new StringBuilder(1024))
                .toString();
    }

    public StringBuilder toStringBuilder(StringBuilder sb) {
        return toStringBuilder(TO_STRING_LIMIT, Group.TO_STRING_WIDTH, sb);
    }

    public StringBuilder toStringBuilder(int limit, int maxWidth,
            StringBuilder sb) {
        int remaining = limit;
        for (int i = 0; i < groupsSize; i++) {
            groups[i].toStringBuilder(remaining, maxWidth, sb);
            if ((remaining -= groups[i].size()) < 0) {
                sb.append("...\n");
                break;
            }
        }
        return sb;
    }

    public int calcLength(boolean explicitVR, EncodeOptions encOpts) {
        int len = 0;
        for (int i = 0; i < groupsSize; i++)
            len += groups[i].calcLength(explicitVR, encOpts);
        length = len;
        return len;
    }

    public void writeTo(DicomOutputStream dos, EncodeOptions encOpts)
            throws IOException {
        for (int i = 0; i < groupsSize; i++)
            groups[i].writeTo(dos, encOpts);
    }

    public Attributes createFileMetaInformation(String tsuid) {
        String cuid = getString(Tag.SOPClassUID, null, 0, null);
        String iuid = getString(Tag.SOPInstanceUID, null, 0, null);
        Attributes fmi = new Attributes(1);
        fmi.setBytes(Tag.FileMetaInformationVersion, null, VR.OB, FMI_VERS);
        fmi.setString(Tag.MediaStorageSOPClassUID, null, VR.UI, cuid);
        fmi.setString(Tag.MediaStorageSOPInstanceUID, null, VR.UI, iuid);
        fmi.setString(Tag.TransferSyntaxUID, null, VR.UI, tsuid);
        fmi.setString(Tag.ImplementationClassUID, null, VR.UI,
                Implementation.getClassUID());
        fmi.setString(Tag.ImplementationVersionName, null, VR.SH,
                Implementation.getVersionName());
        return fmi;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(bigEndian);
        out.writeInt(groupsSize);
        DicomOutputStream dout = new DicomOutputStream(out, true, bigEndian);
        dout.setIncludeBulkDataLocator(true);
        dout.writeDataset(null, this);
        dout.writeHeader(Tag.ItemDelimitationItem, null, 0);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        this.position = -1L;
        this.length = -1;
        this.bigEndian = in.readBoolean();
        this.groups = new Group[in.readInt()];
        DicomInputStream din = new DicomInputStream(in, true, bigEndian);
        din.readAttributes(this, -1);
    }

}
