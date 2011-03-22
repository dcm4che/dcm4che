package org.dcm4che.util;

import java.util.Arrays;

public class IntHashMap<V> implements Cloneable, java.io.Serializable {

    private static final int DEFAULT_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static final byte FREE = 0;
    private static final byte FULL = 1;
    private static final byte REMOVED = -1;

    private transient int[] keys;
    private transient Object[] values;
    private transient byte[] states;
    private transient int free;
    private transient int size;

    public IntHashMap() {
        init(DEFAULT_CAPACITY);
    }

    public IntHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException(
                    "expectedMaxSize is negative: " + expectedMaxSize);

        init(capacity(expectedMaxSize));
    }

    private int capacity(int expectedMaxSize) {
        int minCapacity = expectedMaxSize << 1;
        if (minCapacity > MAXIMUM_CAPACITY)
            return MAXIMUM_CAPACITY;

        int capacity = MINIMUM_CAPACITY;
        while (capacity < minCapacity)
            capacity <<= 1;

        return capacity;
    }

    private void init(int initCapacity) {
        keys = new int[initCapacity];
        values = new Object[initCapacity];
        states = new byte[initCapacity];
        free = initCapacity >>> 1;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    
    @SuppressWarnings("unchecked")
    public V get(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (true) {
            if (keys[i] == key)
                return (V) values[i];
            if (states[i] == FREE)
                return null;
            i = (i + 1) & mask;
        }
    }

    public boolean containsKey(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (true) {
            if (keys[i] == key)
                return true;
            if (states[i] == FREE)
                return false;
            i = (i + 1) & mask;
        }
    }

    @SuppressWarnings("unchecked")
    public V put(int key, V value) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;

        while (states[i] != FREE) {
            if (keys[i] == key) {
                V oldValue = (V) values[i];
                values[i] = value;
                if (states[i] < FREE) { // states[i] == REMOVED
                    states[i] = FULL; 
                    ++size;
                }
                return oldValue;
            }
            i = (i + 1) & mask;
        }

        states[i] = FULL;
        keys[i] = key;
        values[i] = value;
        ++size;
        if (--free <= 0)
            resize(keys.length << 1);
        return null;
    }

    public void rehash(boolean trimToSize) {
        resize(trimToSize ? capacity(size) : keys.length);
    }

    private void resize(int newLength) {
        if (newLength > MAXIMUM_CAPACITY)
            throw new IllegalStateException("Capacity exhausted.");

        int[] oldKeys = keys;
        Object[] oldValues = values;
        byte[] oldStates = states;
        int[] newKeys = new int[newLength];
        Object[] newValues = new Object[newLength];
        byte[] newStates = new byte[newLength];
        int mask = newLength - 1;

        for (int j = 0; j < oldKeys.length; j++) {
            if (oldStates[j] > 0) { // states[i] == FULL
                int key = oldKeys[j];
                int i = key & mask;
                while (newStates[i] != FREE)
                    i = (i + 1) & mask;
                newStates[i] = FULL;
                newKeys[i] = key;
                newValues[i] = oldValues[j];
                oldValues[j] = null;
            }
        }
        keys = newKeys;
        values = newValues;
        states = newStates;
        free = (newLength >>> 1) - size;
    }

    @SuppressWarnings("unchecked")
    public V remove(int key) {
        byte[] states = this.states;
        int[] keys = this.keys;
        int mask = keys.length - 1;
        int i = key & mask;
        while (states[i] != FREE) {
            if (keys[i] == key) {
                if (states[i] < FREE) // states[i] == REMOVED
                    return null;

                states[i] = REMOVED;
                V oldValue = (V) values[i];
                values[i] = null;
                size--;
                return oldValue;
            }
            i = (i + 1) & mask;
        }
        return null;
    }

    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(states, FREE);
        size = 0;
        free = keys.length >>> 1;
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            IntHashMap<V> m = (IntHashMap<V>) super.clone();
            m.states = states.clone();
            m.keys = keys.clone();
            m.values = values.clone();
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private static final long serialVersionUID = 9153226350279204066L;

    private void writeObject(java.io.ObjectOutputStream s)
            throws java.io.IOException {
        s.defaultWriteObject();

        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        s.writeInt(size);
        for (int i = 0; i < states.length; i++) {
            if (states[i] > FREE) { // states[i] == FULL
                s.writeInt(keys[i]);
                s.writeObject(values[i]);
            }
        }
    }

    private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException  {
        s.defaultReadObject();

        int count = s.readInt();
        init(capacity(count));
        size = count;
        free -= count;

        byte[] states = this.states;
        int[] keys = this.keys;
        Object[] values = this.values;
        int mask = keys.length - 1;

        while (count-- > 0) {
            int key = s.readInt();
            int i = key & mask;
            while (states[i] != FREE)
                i = (i + 1) & mask;
            states[i] = FULL;
            keys[i] = key;
            values[i] = s.readObject();
        }
    }
}
