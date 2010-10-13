package org.dcm4che.data;

import java.util.Arrays;

public class Fragments {

    private final VR vr;
    private byte fragments[][];
    private int size;

    Fragments(VR vr, int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException(
                    "Illegal Capacity: "+initialCapacity);
        this.vr = vr;
        fragments = new byte[initialCapacity][];
    }

    public void trimToSize() {
        int oldCapacity = fragments.length;
        if (size < oldCapacity)
            fragments = Arrays.copyOf(fragments, size);
    }

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = fragments.length;
        if (minCapacity > oldCapacity)
            fragments = Arrays.copyOf(fragments, Math.max(minCapacity,
                    oldCapacity <= 2 ? 10 : (oldCapacity * 3)/2 + 1));
    }

    public final VR vr() {
        return vr;
    }

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public byte[] getFragment(int index) {
        rangeCheck(index);
        return fragments[index];
    }

    public byte[] setFragment(int index, byte[] fragment) {
        rangeCheck(index);
        byte[] oldBytes = fragments[index];
        fragments[index] = fragment;
        return oldBytes;
    }

    public void addFragment(byte[] bytes) {
        ensureCapacity(size+1);
        fragments[size++] = bytes;
     }

    public void addFragment(int index, byte[] fragment) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(
                    "Index: "+index+", Size: "+size);
        ensureCapacity(size+1);
        System.arraycopy(fragments, index, fragments, index + 1, size - index);
        fragments[index] = fragment;
        size++;
     }

    public byte[] removeFragment(int index) {
        rangeCheck(index);
        byte[] oldFragment = fragments[index];
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(fragments, index+1, fragments, index, numMoved);

        fragments[--size] = null;
        return oldFragment;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+ size);
    }
}
