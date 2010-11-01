package org.dcm4che.data;

import java.util.Arrays;

import org.dcm4che.util.TagUtils;

public class Attributes {

    private static final int TO_STRING_LIMIT = 20;

    private Attributes parent;
    private Group[] groups;
    private int groupsSize;
    private final int initialGroupCapacity;
    private SpecificCharacterSet cs;
    private long position = -1L;

    private boolean bigEndian;

    public Attributes() {
        this(10, 10);
    }

    public Attributes(int initialCapacity) {
        this(initialCapacity, 10);
    }

    public Attributes(int initialCapacity, int initialGroupCapacity) {
        this.groups = new Group[initialCapacity];
        this.initialGroupCapacity = initialGroupCapacity;
    }

    public Attributes(Attributes other) {
        this(other.groupsSize);
        putAll(other);
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

    public final void bigEndian(boolean bigEndian) {
        if (this.bigEndian != bigEndian) {
            for (int i = 0; i < groupsSize; i++)
                groups[i].toggleEndian();

            this.bigEndian = bigEndian;
        }
    }

    public final Attributes getParent() {
        return parent;
    }

    Attributes setParent(Attributes parent) {
        if (parent != null && this.parent != null)
            throw new IllegalStateException(
                    "Item already contained by Sequence");
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

    public int size() {
        int size = 0;
        for (int i = 0; i < groupsSize; i++)
            size += groups[i].size();

        return size;
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

    public boolean contains(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        return index >= 0 && groups[index].contains(tag, privateCreator);
    }

    public boolean containsValue(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        return index >= 0 && groups[index].containsValue(tag, privateCreator);
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

    public byte[] getBytes(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getBytes(tag, privateCreator);
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

    public String[] getStrings(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getStrings(tag, privateCreator);
    }

    public int getInt(int tag, String privateCreator, int valueIndex,
            int defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getInt(tag, privateCreator, valueIndex, defVal);
    }

    public int[] getInts(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getInts(tag, privateCreator);
    }

    public float getFloat(int tag, String privateCreator, int valueIndex,
            float defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getFloat(tag, privateCreator, valueIndex, defVal);
    }

    public float[] getFloats(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getFloats(tag, privateCreator);
    }

    public double getDouble(int tag, String privateCreator, int valueIndex,
            double defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getDouble(tag, privateCreator, valueIndex, defVal);
    }

    public double[] getDoubles(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getDoubles(tag, privateCreator);
    }

    public Sequence getSequence(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getSequence(tag, privateCreator);
    }

    public Fragments getFragments(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getFragments(tag, privateCreator);
    }

    public SpecificCharacterSet getSpecificCharacterSet() {
        return cs != null ? cs 
                : parent != null ? parent.getSpecificCharacterSet()
                        : SpecificCharacterSet.DEFAULT;
    }

    public boolean remove(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return false;
        boolean b = groups[index].remove(tag, privateCreator);
        if (tag == Tag.SpecificCharacterSet)
            cs = null;
        return b;
    }

    public void putNull(int tag, String privateCreator, VR vr) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putNull(tag, privateCreator, vr);
        if (tag == Tag.SpecificCharacterSet)
            cs = null;
    }

    public void putBytes(int tag, String privateCreator, VR vr, byte[] value,
            boolean bigEndian) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber, initialGroupCapacity)
                .putBytes(tag, privateCreator, vr, value, bigEndian);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
    }

    public void putString(int tag, String privateCreator, VR vr, String val) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putString(tag, privateCreator, vr, val);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
    }

    public void putString(int tag, String privateCreator, VR vr,
            String... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putString(tag, privateCreator, vr,
                value);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
    }

    public void putInt(int tag, String privateCreator, VR vr, int... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putInt(tag, privateCreator, vr, value);
    }

    public void putFloat(int tag, String privateCreator, VR vr,
            float... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putFloat(tag, privateCreator, vr,
                value);
    }

    public Sequence putSequence(int tag, String privateCreator,
            int initialCapacity) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber).putSequence(tag, privateCreator,
                initialCapacity);
    }

    public Fragments putFragments(int tag, String privateCreator, VR vr,
            boolean bigEndian, int initialCapacity) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber).putFragments(tag, privateCreator,
                vr, bigEndian, initialCapacity);
    }

    public void putAll(Attributes other) {
        other.bigEndian(bigEndian);
        Group[] srcGroups = other.groups;
        int srcGroupSize = other.groupsSize;
        for (int i = 0; i < srcGroupSize; i++) {
            Group srcGroup = srcGroups[i];
            int groupNumber = srcGroup.getGroupNumber();
            getOrCreateGroup(groupNumber, srcGroupSize).putAll(srcGroup);
        }
    }

    void initSpecificCharacterSet() {
        cs = null;
        String[] codes = getStrings(Tag.SpecificCharacterSet, null);
        if (codes != null)
            cs = SpecificCharacterSet.valueOf(codes);
    }

    private Group getOrCreateGroup(int groupNumber) {
        return getOrCreateGroup(groupNumber, initialGroupCapacity);
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
        groups[index] = Group.create(this, groupNumber, capacity);
        groupsSize++;
        return groups[index];
    }

    @Override
    public String toString() {
        return toString(TO_STRING_LIMIT, Group.TO_STRING_WIDTH);
    }
    
    public String toString(int limit, int maxWidth) {
        return promptAttributes(limit, maxWidth,
                new StringBuilder(limit * (maxWidth + 1) + 4))
                .toString();
    }

    public StringBuilder promptAttributes(int limit, int maxWidth,
            StringBuilder sb) {
        int remaining = limit;
        for (int i = 0; i < groupsSize; i++) {
            groups[i].promptAttributes(remaining, maxWidth, sb);
            if ((remaining -= groups[i].size()) < 0) {
                sb.append("...\n");
                break;
            }
        }
        return sb;
    }

 }
