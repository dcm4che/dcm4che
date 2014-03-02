package org.dcm4che3.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dcm4che3.util.IntHashMap;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class IntHashMapTest {

    private IntHashMap<Integer> map;

    @Before
    public void setUp() throws Exception {
        map = new IntHashMap<Integer>();
        for (int i = 1; i < 45; i += 3)
            map.put(i, Integer.valueOf(i));
    }

    private void removeOdd() {
        for (int i = 1; i < 45; i += 2)
            map.remove(i);
    }

    private static void testGet(IntHashMap<Integer> map) {
        for (int i = 1; i < 45; i++)
            if ((i & 1) == 0 && (i % 3) == 1)
                assertEquals(Integer.valueOf(i), map.get(i));
            else
                assertNull(map.get(i));
    }

    @Test
    public void testSize() {
        assertEquals(15, map.size());
        removeOdd();
        assertEquals(7, map.size());
    }

    @Test
    public void testGet() {
        removeOdd();
        testGet(map);
    }

    @Test
    public void testPut() {
        removeOdd();
        for (int i = 0; i < 45; i++)
            map.put(i, Integer.valueOf(i));
        assertEquals(45, map.size());
        for (int i = 0; i < 45; i++)
            assertEquals(Integer.valueOf(i), map.get(i));
    }

    @Test
    public void testContainsKey() {
        removeOdd();
        for (int i = 1; i < 45; i++)
            assertEquals((i & 1) == 0 && (i % 3) == 1, map.containsKey(i));
    }

    @Test
    public void testRehash() {
        removeOdd();
        map.trimToSize();
        testGet(map);
   }

    @Test
    public void testRemove() {
        for (int i = 1; i < 45; i += 2)
            if ((i % 3) == 1)
                assertEquals(Integer.valueOf(i), map.remove(i));
            else
                assertNull(map.remove(i));
    }

    @Test
    public void testClear() {
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testClone() {
        removeOdd();
        IntHashMap<Integer> clone = (IntHashMap<Integer>) map.clone();
        map.clear();
        testGet(clone);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() throws Exception {
        removeOdd();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        try {
            oout.writeObject(map);
            oout.writeUTF("EOF");
        } finally {
            oout.close();
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(
                bout.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(bin);
        try {
            testGet((IntHashMap<Integer>) oin.readObject());
            assertEquals("EOF", oin.readUTF());
        } finally {
            oin.close();
        }
    }
}
