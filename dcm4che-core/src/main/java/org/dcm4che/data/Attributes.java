package org.dcm4che.data;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.dcm4che.util.TagUtils;

public class Attributes {

    private Attributes parent;
    private Group[] groups;
    private int groupsSize;
    private final int initialElementsPerGroupCapacity;
    private SpecificCharacterSet cs;
    private long position = -1L;

    public Attributes() {
        this(10, 10);
    }

    public Attributes(int initalGroupsCapacity) {
        this(initalGroupsCapacity, 10);
    }

    public Attributes(int initalGroupsCapacity,
            int initialElementsPerGroupCapacity) {
        this.groups = new Group[initalGroupsCapacity];
        this.initialElementsPerGroupCapacity = initialElementsPerGroupCapacity;
    }

    public final boolean isRoot() {
        return parent == null;
    }

    public final int getLevel() {
        return isRoot() ? 0 : 1 + parent.getLevel();
    }

    public final Attributes getParrent() {
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
        return index >= 0 && groups[index].contains(cs(groupNumber), tag,
                privateCreator);
    }

    public boolean containsValue(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        return index >= 0 && groups[index].containsValue(cs(groupNumber), tag,
                privateCreator);
    }

    public String getPrivateCreator(int tag) {
        if (!TagUtils.isPrivateTag(tag))
            return null;

        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getPrivateCreator(cs(groupNumber), tag);
    }

    public ByteBuffer getByteBuffer(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getByteBuffer(cs(groupNumber), tag, privateCreator);
    }

    public String getString(int tag, String privateCreator, String defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getString(cs(groupNumber),
                tag, privateCreator, defVal);
    }

    public String[] getStrings(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getStrings(cs(groupNumber),
                tag, privateCreator);
    }

    public int getInt(int tag, String privateCreator, int defVal) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return defVal;

        return groups[index].getInt(cs(groupNumber), tag,
                privateCreator, defVal);
    }

    public int[] getInts(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getInts(cs(groupNumber), tag,
                privateCreator);
    }

    public Sequence getSequence(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getSequence(cs(groupNumber), tag, privateCreator);
    }

    public Fragments getFragments(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return null;

        return groups[index].getFragments(cs(groupNumber), tag, privateCreator);
    }

    private SpecificCharacterSet cs(int groupNumber) {
        return groupNumber < 8 ? SpecificCharacterSet.DEFAULT : dscs();
    }

    private SpecificCharacterSet dscs() {
        return cs != null ? cs : parent != null ? parent.dscs()
                : SpecificCharacterSet.DEFAULT;
    }

    public boolean remove(int tag, String privateCreator) {
        int groupNumber = TagUtils.groupNumber(tag);
        int index = indexOf(groupNumber);
        if (index < 0)
            return false;
        boolean b = groups[index].remove(tag, privateCreator, cs(groupNumber));
        if (tag == Tag.SpecificCharacterSet)
            cs = null;
        return b;
    }

    public void putNull(int tag, String privateCreator, VR vr) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putNull(cs(groupNumber), tag,
                privateCreator, vr);
        if (tag == Tag.SpecificCharacterSet)
            cs = null;
    }

    public void putBytes(int tag, String privateCreator, VR vr, byte[] value,
            boolean bigEndian) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber, bigEndian)
                .putBytes(cs(groupNumber), tag, privateCreator, vr, value,
                        bigEndian);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
    }

    public void putString(int tag, String privateCreator, VR vr, String val) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putString(cs(groupNumber), tag,
                privateCreator, vr, val);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
    }

    public void putStrings(int tag, String privateCreator, VR vr,
            String... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putStrings(cs(groupNumber), tag,
                privateCreator, vr, value);
        if (tag == Tag.SpecificCharacterSet)
            initSpecificCharacterSet();
    }

    public void putInt(int tag, String privateCreator, VR vr, int value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putInt(cs(groupNumber), tag,
                privateCreator, vr, value);
    }

    public void putInts(int tag, String privateCreator, VR vr, int... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putInts(cs(groupNumber), tag,
                privateCreator, vr, value);
    }

    public void putFloat(int tag, String privateCreator, VR vr, float value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putFloat(cs(groupNumber), tag,
                privateCreator, vr, value);
    }

    public void putFloats(int tag, String privateCreator, VR vr,
            float... value) {
        int groupNumber = TagUtils.groupNumber(tag);
        getOrCreateGroup(groupNumber).putFloats(cs(groupNumber), tag,
                privateCreator, vr, value);
    }

    public Sequence putSequence(int tag, String privateCreator,
            int initialCapacity) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber).putSequence(cs(groupNumber), tag,
                privateCreator, this, initialCapacity);
    }

    public Fragments putFragments(int tag, String privateCreator, VR vr,
            boolean bigEndian, int initialCapacity) {
        int groupNumber = TagUtils.groupNumber(tag);
        return getOrCreateGroup(groupNumber).putFragments(cs(groupNumber), tag,
                privateCreator, vr, bigEndian, initialCapacity);
    }

    private void initSpecificCharacterSet() {
        cs = null;
        String[] codes = getStrings(Tag.SpecificCharacterSet, null);
        if (codes != null)
            cs = SpecificCharacterSet.valueOf(codes);
    }

    private Group getOrCreateGroup(int groupNumber) {
        return getOrCreateGroup(groupNumber, false);
    }

    private Group getOrCreateGroup(int groupNumber, boolean bigEndian) {
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
        groups[index] = new Group(groupNumber, bigEndian,
                initialElementsPerGroupCapacity);
        groupsSize++;
        return groups[index];
    }
}
