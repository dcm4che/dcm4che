package org.dcm4che.data;

import java.util.Arrays;

public class Sequence {

    private final Attributes parent;
    private Attributes[] items;
    private int size;

    Sequence(Attributes parent, int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException(
                    "Illegal Capacity: "+initialCapacity);
        this.parent = parent;
        items = new Attributes[initialCapacity];
    }

    public void trimToSize() {
        int oldCapacity = items.length;
        if (size < oldCapacity)
            items = Arrays.copyOf(items, size);
        for (Attributes item : items)
            item.trimToSize();
    }

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = items.length;
        if (minCapacity > oldCapacity)
            items = Arrays.copyOf(items,
                    Math.max(minCapacity, (oldCapacity * 3)/2 + 1));
    }

    public final int size() {
        return size;
    }

    public final boolean isEmpty() {
        return size == 0;
    }

    public Attributes getItem(int index) {
        rangeCheck(index);
        return items[index];
    }

    public Attributes setItem(int index, Attributes item) {
        rangeCheck(index);
        item.setParent(parent);
        Attributes oldItem = items[index];
        oldItem.setParent(null);
        items[index] = item;
        return oldItem;
    }

    public void addItem(Attributes item) {
        item.setParent(parent);
        ensureCapacity(size+1);
        items[size++] = item;
     }

    public void addItem(int index, Attributes item) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(
                    "Index: "+index+", Size: "+size);
        item.setParent(parent);
        ensureCapacity(size+1);
        System.arraycopy(items, index, items, index + 1, size - index);
        items[index] = item;
        size++;
     }

    public Attributes removeItem(int index) {
        rangeCheck(index);
        Attributes oldItem = items[index];
        oldItem.setParent(null);
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(items, index+1, items, index, numMoved);

        items[--size] = null;
        return oldItem;
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+ size);
    }
}
