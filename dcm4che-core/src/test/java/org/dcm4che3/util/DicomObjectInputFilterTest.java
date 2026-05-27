/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Regression coverage for {@link DicomObjectInputFilter}: the allowlist that
 * guards Java deserialization sinks in {@code dcm4che-core} (issue #1581).
 *
 * <p>Tests fall into the groups laid out in PLAN.md §5:
 * <ol>
 *   <li>Benign round-trip — every shape the legitimate read path can produce
 *       must still survive serialize → deserialize.</li>
 *   <li>Rejection-before-callback — a synthetic {@code SideEffectClass}
 *       whose {@code readObject} flips a static flag must be rejected
 *       <em>before</em> that flag flips, when smuggled through each guarded
 *       sink ({@code Attributes}, {@code IntHashMap}, top-level OIS).</li>
 *   <li>Filter chaining — a pre-existing application-level filter must not
 *       be silently weakened by ours.</li>
 *   <li>Resource limits — pathological array sizes are rejected; legitimate
 *       sizes pass.</li>
 *   <li>Wire-format stability — {@code serialVersionUID} on {@code Attributes}
 *       and {@code BulkData} is unchanged so older serialized blobs still
 *       round-trip.</li>
 * </ol>
 *
 * <p>Rejection tests are guarded with {@link org.junit.Assume} so they skip
 * (rather than fail) on pre-8u121 JVMs where the filter API is documented to
 * be a no-op (PLAN.md §4 item 1).
 */
public class DicomObjectInputFilterTest {

    // ----------------------------------------------------------------------
    // Hostile probe + bookkeeping
    // ----------------------------------------------------------------------

    /**
     * Records whether its {@code readObject} ran. The filter's contract is
     * to reject this class <em>before</em> its {@code readObject} executes.
     */
    public static final class SideEffectClass implements Serializable {
        private static final long serialVersionUID = 1L;
        static volatile boolean readObjectFired = false;
        @SuppressWarnings("unused")
        private String marker = "fired-marker";

        private void readObject(ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            readObjectFired = true;
        }
    }

    @Before
    public void resetSideEffectFlag() {
        SideEffectClass.readObjectFired = false;
    }

    @After
    public void clearSideEffectFlag() {
        SideEffectClass.readObjectFired = false;
    }

    // ----------------------------------------------------------------------
    // §5.1 Benign round-trip — must keep passing with the filter active
    // ----------------------------------------------------------------------

    @Test
    public void roundTrip_emptyAttributes() throws Exception {
        Attributes a = new Attributes();
        Attributes b = (Attributes) roundTrip(a);

        assertNotNull(b);
        assertEquals("size() must round-trip", a.size(), b.size());
        assertEquals("bigEndian() must round-trip", a.bigEndian(), b.bigEndian());
    }

    @Test
    public void roundTrip_emptyAttributesBigEndian() throws Exception {
        Attributes a = new Attributes(true);
        Attributes b = (Attributes) roundTrip(a);

        assertNotNull(b);
        assertEquals(a.size(), b.size());
        assertTrue("bigEndian flag must round-trip", b.bigEndian());
    }

    @Test
    public void roundTrip_attributesWithBulkData() throws Exception {
        // Exercises the magic-length deserializeBulkData() path
        // (sinks #1/#2) reached via Attributes.readObject (sink #3).
        Attributes a = new Attributes();
        BulkData bd = new BulkData("file:///tmp/x.dcm", 0L, 1024L, false);
        a.setValue(Tag.PixelData, VR.OB, bd);

        Attributes b = (Attributes) roundTrip(a);
        Object value = b.getValue(Tag.PixelData);
        assertNotNull("BulkData value must be present", value);
        assertTrue("expected BulkData but got "
                        + (value == null ? "null" : value.getClass().getName()),
                value instanceof BulkData);

        BulkData bd2 = (BulkData) value;
        assertEquals(bd.getURI(), bd2.getURI());
        assertEquals(bd.offset(), bd2.offset());
        assertEquals(bd.longLength(), bd2.longLength());
        assertEquals(bd.bigEndian(), bd2.bigEndian());
    }

    @Test
    public void roundTrip_bulkData() throws Exception {
        // Exercises BulkData.readObject — sink #4.
        BulkData bd = new BulkData("file:///tmp/y.dcm", 42L, 8L, false);
        BulkData bd2 = (BulkData) roundTrip(bd);

        assertEquals(bd.getURI(), bd2.getURI());
        assertEquals(bd.offset(), bd2.offset());
        assertEquals(bd.longLength(), bd2.longLength());
        assertEquals(bd.bigEndian(), bd2.bigEndian());
        assertEquals(bd, bd2);
        assertEquals(bd.hashCode(), bd2.hashCode());
    }

    @Test
    public void roundTrip_bulkDataBigEndian() throws Exception {
        // Cover the bigEndian = true path through BulkData's three primitive
        // reads (defaultReadObject + 2x readUTF + readBoolean).
        BulkData bd = new BulkData("file:///be.dcm", 0L, 4096L, true);
        BulkData bd2 = (BulkData) roundTrip(bd);

        assertEquals(bd, bd2);
        assertTrue(bd2.bigEndian());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void roundTrip_intHashMapOfAttributes() throws Exception {
        // Exercises IntHashMap.readObject — sink #5 — and proves that an
        // allowlisted V (Attributes) is not collateral-damaged.
        IntHashMap<Attributes> map = new IntHashMap<Attributes>();
        for (int i = 1; i <= 5; i++) {
            Attributes a = new Attributes();
            a.setString(Tag.PatientName, VR.PN, "Patient^" + i);
            map.put(i, a);
        }

        IntHashMap<Attributes> map2 = (IntHashMap<Attributes>) roundTrip(map);
        assertNotNull(map2);
        assertEquals(map.size(), map2.size());
        for (int i = 1; i <= 5; i++) {
            assertTrue("key " + i + " must be present", map2.containsKey(i));
            Attributes a = map2.get(i);
            assertNotNull(a);
            assertEquals("Patient^" + i, a.getString(Tag.PatientName));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void roundTrip_intHashMapOfBoxedIntegers() throws Exception {
        // Mirrors the pre-existing IntHashMapTest#testSerialize shape so we
        // catch regressions there too — boxed Integer is on the allowlist.
        IntHashMap<Integer> map = new IntHashMap<Integer>();
        for (int i = 1; i < 45; i += 3) {
            map.put(i, Integer.valueOf(i));
        }

        IntHashMap<Integer> map2 = (IntHashMap<Integer>) roundTrip(map);
        assertEquals(map.size(), map2.size());
        for (int i = 1; i < 45; i += 3) {
            assertEquals(Integer.valueOf(i), map2.get(i));
        }
    }

    @Test
    public void roundTrip_attributesWithProperties() throws Exception {
        // PLAN.md §5.1 — properties map populated with allowlisted value
        // types (String, Integer) must round-trip cleanly.
        Attributes a = new Attributes();
        a.setProperty("k", "v");
        a.setProperty("n", Integer.valueOf(42));

        Attributes b = (Attributes) roundTrip(a);
        assertEquals("v", b.getProperty("k", null));
        assertEquals(Integer.valueOf(42), b.getProperty("n", null));
    }

    @Test
    public void roundTrip_attributesPropertyTypes_allBoxes() throws Exception {
        // PLAN.md §3.1 — primitive boxes are allowlisted.
        Attributes a = new Attributes();
        a.setProperty("bool",   Boolean.TRUE);
        a.setProperty("byte",   Byte.valueOf((byte) 1));
        a.setProperty("char",   Character.valueOf('z'));
        a.setProperty("short",  Short.valueOf((short) 2));
        a.setProperty("int",    Integer.valueOf(3));
        a.setProperty("long",   Long.valueOf(4L));
        a.setProperty("float",  Float.valueOf(5.0f));
        a.setProperty("double", Double.valueOf(6.0d));
        a.setProperty("str",    "string-value");

        Attributes b = (Attributes) roundTrip(a);
        assertEquals(Boolean.TRUE,                  b.getProperty("bool",   null));
        assertEquals(Byte.valueOf((byte) 1),        b.getProperty("byte",   null));
        assertEquals(Character.valueOf('z'),        b.getProperty("char",   null));
        assertEquals(Short.valueOf((short) 2),      b.getProperty("short",  null));
        assertEquals(Integer.valueOf(3),            b.getProperty("int",    null));
        assertEquals(Long.valueOf(4L),              b.getProperty("long",   null));
        assertEquals(Float.valueOf(5.0f),           b.getProperty("float",  null));
        assertEquals(Double.valueOf(6.0d),          b.getProperty("double", null));
        assertEquals("string-value",                b.getProperty("str",    null));
    }

    @Test
    public void roundTrip_attributesWithDefaultTimeZoneUTC() throws Exception {
        // PLAN.md §3.1 — TimeZone and concrete impls (SimpleTimeZone,
        // sun.util.calendar.ZoneInfo) are allowlisted.
        Attributes a = new Attributes();
        a.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));

        Attributes b = (Attributes) roundTrip(a);
        TimeZone tz = b.getDefaultTimeZone();
        assertNotNull("defaultTimeZone must survive round-trip", tz);
        assertEquals("UTC", tz.getID());
    }

    @Test
    public void roundTrip_attributesWithDefaultTimeZoneLosAngeles() throws Exception {
        // Non-UTC zone — likely a sun.util.calendar.ZoneInfo at runtime.
        Attributes a = new Attributes();
        TimeZone src = TimeZone.getTimeZone("America/Los_Angeles");
        a.setDefaultTimeZone(src);

        Attributes b = (Attributes) roundTrip(a);
        TimeZone tz = b.getDefaultTimeZone();
        assertNotNull(tz);
        assertEquals(src.getID(), tz.getID());
    }

    // ----------------------------------------------------------------------
    // §5.2 Rejection-before-callback — the actual security regression
    // ----------------------------------------------------------------------

    @Test
    public void install_rejectsHostileClass_beforeCallback_topLevel() throws Exception {
        // Direct test of install() on a vanilla OIS: a top-level
        // SideEffectClass must be rejected, its readObject must NOT fire.
        assumeFilterApiAvailable();
        byte[] payload = serializeRaw(new SideEffectClass());

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            try {
                ois.readObject();
                fail("expected the filter to reject SideEffectClass");
            } catch (InvalidClassException expected) {
                // ok
            } catch (IOException expected) {
                // some JVMs wrap as plain IOException
            } catch (RuntimeException expected) {
                // SecurityException-like wrappers
            }
        } finally {
            ois.close();
        }
        assertFalse("SideEffectClass.readObject must NOT fire before rejection",
                SideEffectClass.readObjectFired);
    }

    @Test
    public void intHashMapReadObject_rejectsHostileValue() throws Exception {
        // PLAN.md §5.2 — IntHashMap.readObject installs the filter, so when
        // a hostile V is encountered during the per-entry s.readObject()
        // loop, it must be rejected before the callback fires.
        assumeFilterApiAvailable();
        IntHashMap<SideEffectClass> hostile = new IntHashMap<SideEffectClass>();
        hostile.put(1, new SideEffectClass());
        hostile.put(7, new SideEffectClass());
        byte[] payload = serializeRaw(hostile);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            ois.readObject();
            fail("expected rejection of hostile IntHashMap value");
        } catch (InvalidClassException expected) {
            // ok
        } catch (IOException expected) {
            // ok
        } catch (RuntimeException expected) {
            // ok
        } finally {
            ois.close();
        }
        assertFalse("SideEffectClass.readObject must NOT fire when smuggled via IntHashMap",
                SideEffectClass.readObjectFired);
    }

    @Test
    public void attributesReadObject_rejectsHostilePropertyValue() throws Exception {
        // PLAN.md §5.2 — Attributes.readObject installs the filter before
        // defaultReadObject; a SideEffectClass smuggled as a property value
        // must be rejected during properties-map deserialization.
        assumeFilterApiAvailable();
        Attributes a = new Attributes();
        Map<String, Object> props = exposeProperties(a);
        props.put("evil", new SideEffectClass());

        byte[] payload = serializeRaw(a);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            ois.readObject();
            fail("expected rejection of hostile Attributes property value");
        } catch (InvalidClassException expected) {
            // ok
        } catch (IOException expected) {
            // ok
        } catch (RuntimeException expected) {
            // ok
        } finally {
            ois.close();
        }
        assertFalse("SideEffectClass.readObject must NOT fire when smuggled via Attributes",
                SideEffectClass.readObjectFired);
    }

    @Test
    public void install_rejectsHostileNestedInHashMap() throws Exception {
        // The filter must inspect every class in the stream, not only the
        // top-level one — a SideEffectClass nested inside an allowlisted
        // HashMap must still be rejected before its callback fires.
        assumeFilterApiAvailable();
        HashMap<String, Object> wrapper = new HashMap<String, Object>();
        wrapper.put("payload", new SideEffectClass());
        byte[] payload = serializeRaw(wrapper);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            try {
                ois.readObject();
                fail("expected nested SideEffectClass to be rejected");
            } catch (InvalidClassException expected) {
                // ok
            } catch (IOException expected) {
                // ok
            } catch (RuntimeException expected) {
                // ok
            }
        } finally {
            ois.close();
        }
        assertFalse("nested SideEffectClass.readObject must NOT fire",
                SideEffectClass.readObjectFired);
    }

    // ----------------------------------------------------------------------
    // Direct install() behavior on a vanilla OIS — proves the filter loads
    // ----------------------------------------------------------------------

    @Test
    public void install_allowsAttributesAtTopLevel() throws Exception {
        byte[] payload = serializeRaw(new Attributes());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            Object restored = ois.readObject();
            assertTrue("Attributes must pass the filter",
                    restored instanceof Attributes);
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_allowsBulkDataAtTopLevel() throws Exception {
        byte[] payload = serializeRaw(
                new BulkData("file:///ok.dcm", 0L, 16L, false));
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            Object restored = ois.readObject();
            assertTrue(restored instanceof BulkData);
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_allowsIntHashMapAtTopLevel() throws Exception {
        IntHashMap<Integer> m = new IntHashMap<Integer>();
        m.put(1, 1);
        byte[] payload = serializeRaw(m);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            Object restored = ois.readObject();
            assertTrue(restored instanceof IntHashMap);
        } finally {
            ois.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void install_allowsCommonCollections() throws Exception {
        // PLAN.md §3.1 — HashMap, LinkedHashMap, ArrayList are allowlisted.
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "b");
        map.put("c", Integer.valueOf(1));
        Map<String, Object> lhm = new LinkedHashMap<String, Object>();
        lhm.put("first", "1");
        lhm.put("second", "2");
        List<Object> list = new ArrayList<Object>();
        list.add("x");
        list.add(Long.valueOf(7L));
        list.add(map);
        list.add(lhm);

        byte[] payload = serializeRaw(list);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            List<Object> restored = (List<Object>) ois.readObject();
            assertEquals(4, restored.size());
            assertEquals("x", restored.get(0));
            assertEquals(Long.valueOf(7L), restored.get(1));
            assertEquals(map, restored.get(2));
            assertEquals(lhm, restored.get(3));
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_isIdempotentOnSameStream() throws Exception {
        // PLAN.md §4 item 6 — recursive install() calls on the same OIS
        // must not break a subsequent legitimate readObject.
        byte[] payload = serializeRaw(new Attributes());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            DicomObjectInputFilter.install(ois);
            DicomObjectInputFilter.install(ois);
            Object restored = ois.readObject();
            assertTrue(restored instanceof Attributes);
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_neverThrows_onFreshStream() throws Exception {
        // Javadoc contract per PLAN.md §3.1: "best-effort, idempotent, never throws".
        byte[] payload = serializeRaw("ok");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
        } catch (Throwable t) {
            fail("install() must never throw on a fresh OIS; propagated " + t);
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_neverThrows_onExhaustedStream() throws Exception {
        // Defense-in-depth: install() on an already-consumed OIS must still
        // not propagate exceptions.
        byte[] payload = serializeRaw("ok");
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        ois.readObject();
        try {
            DicomObjectInputFilter.install(ois);
        } catch (Throwable t) {
            fail("install() must never throw on an exhausted OIS; propagated " + t);
        } finally {
            ois.close();
        }
    }

    // ----------------------------------------------------------------------
    // §5.3 Filter chaining — prior filter must keep precedence
    // ----------------------------------------------------------------------

    @Test
    public void installRespectsPreExistingStricterFilter() throws Exception {
        // Outer filter rejects Attributes; install() must merge behind it
        // (PLAN.md §3.1 — "incoming filter wins on REJECTED/ALLOWED, ours
        // runs on UNDECIDED"). Attributes must still be rejected.
        byte[] payload = serializeRaw(new Attributes());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));

        boolean installedOuter = installOuterFilter(ois, REJECT_ATTRIBUTES);
        assumeTrue("No ObjectInputFilter API on this JVM; chaining test cannot run.",
                installedOuter);

        DicomObjectInputFilter.install(ois);
        try {
            ois.readObject();
            fail("outer filter's REJECTED verdict on Attributes must win");
        } catch (InvalidClassException expected) {
            // ok
        } catch (IOException expected) {
            // ok
        } catch (RuntimeException expected) {
            // ok
        } finally {
            ois.close();
        }
    }

    @Test
    public void installRespectsPreExistingPermissiveFilter() throws Exception {
        // Outer filter explicitly ALLOWS Attributes; install() merging
        // behind it must not block the allowlisted shape.
        byte[] payload = serializeRaw(new Attributes());
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));

        boolean installedOuter = installOuterFilter(ois, ALLOW_ALL);
        assumeTrue("No ObjectInputFilter API on this JVM; chaining test cannot run.",
                installedOuter);

        DicomObjectInputFilter.install(ois);
        try {
            Object restored = ois.readObject();
            assertTrue(restored instanceof Attributes);
        } finally {
            ois.close();
        }
    }

    // ----------------------------------------------------------------------
    // Resource limits — PLAN.md §3.1 / §4 item 9
    // ----------------------------------------------------------------------

    @Test
    public void install_rejectsPathologicalArrayLength() throws Exception {
        // PLAN.md §3.1: maxArrayLength = 100_000. A 500k int[] must be
        // rejected before the JVM allocates it.
        assumeFilterApiAvailable();
        byte[] payload = serializeRaw(new int[500_000]);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            try {
                ois.readObject();
                fail("filter must reject int[500_000] (> 100_000 cap)");
            } catch (InvalidClassException expected) {
                // ok
            } catch (IOException expected) {
                // ok
            } catch (RuntimeException expected) {
                // ok
            }
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_acceptsLegitimateArraySize() throws Exception {
        // Boundary check: a small int[] is far under any cap and must pass.
        int[] src = new int[64];
        for (int i = 0; i < src.length; i++) src[i] = i;

        byte[] payload = serializeRaw(src);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            Object restored = ois.readObject();
            assertTrue(restored instanceof int[]);
            int[] back = (int[]) restored;
            assertEquals(src.length, back.length);
            for (int i = 0; i < src.length; i++) {
                assertEquals(src[i], back[i]);
            }
        } finally {
            ois.close();
        }
    }

    @Test
    public void install_acceptsLargeButLegalArray() throws Exception {
        // Right at the edge: well under 100_000, must pass.
        byte[] big = new byte[90_000];
        byte[] payload = serializeRaw(big);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(payload));
        try {
            DicomObjectInputFilter.install(ois);
            byte[] back = (byte[]) ois.readObject();
            assertEquals(big.length, back.length);
        } finally {
            ois.close();
        }
    }

    // ----------------------------------------------------------------------
    // Wire-format stability — PLAN.md §3.4, §4 item 10, §6
    // ----------------------------------------------------------------------

    @Test
    public void serialVersionUID_attributes_unchanged() {
        ObjectStreamClass desc = ObjectStreamClass.lookup(Attributes.class);
        assertNotNull(desc);
        assertEquals("Attributes serialVersionUID must not change — wire format is "
                        + "part of public API per PLAN.md §3.3",
                7868714416968825241L, desc.getSerialVersionUID());
    }

    @Test
    public void serialVersionUID_bulkData_unchanged() {
        ObjectStreamClass desc = ObjectStreamClass.lookup(BulkData.class);
        assertNotNull(desc);
        assertEquals("BulkData serialVersionUID must not change",
                -6563845357491618094L, desc.getSerialVersionUID());
    }

    // ----------------------------------------------------------------------
    // Public surface — PLAN.md §3.1
    // ----------------------------------------------------------------------

    @Test
    public void installMethod_isPublicStaticVoid() throws Exception {
        Method m = DicomObjectInputFilter.class.getDeclaredMethod(
                "install", ObjectInputStream.class);
        assertTrue("install() must be public", Modifier.isPublic(m.getModifiers()));
        assertTrue("install() must be static", Modifier.isStatic(m.getModifiers()));
        assertSame("install() must return void", void.class, m.getReturnType());
    }

    @Test
    public void class_isFinalAndUtility() {
        // PLAN.md §3.1: `public final class DicomObjectInputFilter` with
        // a private constructor.
        assertTrue("DicomObjectInputFilter must be final",
                Modifier.isFinal(DicomObjectInputFilter.class.getModifiers()));
        java.lang.reflect.Constructor<?>[] ctors =
                DicomObjectInputFilter.class.getDeclaredConstructors();
        assertEquals("expected single declared constructor", 1, ctors.length);
        assertTrue("constructor must be private",
                Modifier.isPrivate(ctors[0].getModifiers()));
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    private static Object roundTrip(Object o) throws IOException, ClassNotFoundException {
        return deserializeRaw(serializeRaw(o));
    }

    private static byte[] serializeRaw(Object o) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        try {
            oout.writeObject(o);
        } finally {
            oout.close();
        }
        return bout.toByteArray();
    }

    private static Object deserializeRaw(byte[] bytes)
            throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }

    /**
     * Reflectively expose {@code Attributes#properties} so we can smuggle a
     * hostile value into the map before the serialize step. This bypasses
     * any type checking that {@code setProperty} might do, modeling a
     * worst-case attacker who controls the serialized byte stream directly.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> exposeProperties(Attributes a) throws Exception {
        java.lang.reflect.Field f = Attributes.class.getDeclaredField("properties");
        f.setAccessible(true);
        Map<String, Object> existing = (Map<String, Object>) f.get(a);
        if (existing == null) {
            existing = new HashMap<String, Object>();
            f.set(a, existing);
        }
        return existing;
    }

    /**
     * Skip the test if the running JVM exposes neither
     * {@code java.io.ObjectInputFilter} nor {@code sun.misc.ObjectInputFilter}.
     * On such a JVM the filter is a documented no-op (PLAN.md §4 item 1) so
     * rejection-based assertions cannot hold.
     */
    private static void assumeFilterApiAvailable() {
        boolean available;
        try {
            Class.forName("java.io.ObjectInputFilter");
            available = true;
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("sun.misc.ObjectInputFilter");
                available = true;
            } catch (ClassNotFoundException e2) {
                available = false;
            }
        }
        assumeTrue("ObjectInputFilter API unavailable on this JVM; "
                + "rejection tests cannot run (PLAN.md §4 item 1).", available);
    }

    // --- chaining-test helpers (JDK 9+ first, JDK 8u121 fallback) -----------

    /** Decide on a {@code Status} for a given {@code serialClass}. */
    private interface FilterMode {
        /** @return one of "ALLOWED", "REJECTED", "UNDECIDED". */
        String decide(Class<?> serialClass);
    }

    private static final FilterMode REJECT_ATTRIBUTES = new FilterMode() {
        public String decide(Class<?> serialClass) {
            if (serialClass != null && Attributes.class.isAssignableFrom(serialClass)) {
                return "REJECTED";
            }
            return "UNDECIDED";
        }
    };

    private static final FilterMode ALLOW_ALL = new FilterMode() {
        public String decide(Class<?> serialClass) {
            return "ALLOWED";
        }
    };

    /**
     * Install an outer ObjectInputFilter on the given stream using whichever
     * filter API is available at runtime. Returns false if neither JDK 9+
     * nor JDK 8u121 filter API is present.
     */
    private static boolean installOuterFilter(ObjectInputStream ois, FilterMode mode) {
        if (installViaJdk9Plus(ois, mode)) return true;
        return installViaJdk8(ois, mode);
    }

    private static boolean installViaJdk9Plus(ObjectInputStream ois, final FilterMode mode) {
        try {
            final Class<?> filterCls = Class.forName("java.io.ObjectInputFilter");
            final Class<?> infoCls = Class.forName("java.io.ObjectInputFilter$FilterInfo");
            final Class<?> statusCls = Class.forName("java.io.ObjectInputFilter$Status");
            final Method serialClassM = infoCls.getMethod("serialClass");
            Object proxy = Proxy.newProxyInstance(
                    filterCls.getClassLoader(),
                    new Class<?>[] { filterCls },
                    new InvocationHandler() {
                        public Object invoke(Object self, Method method, Object[] args)
                                throws Throwable {
                            if (!"checkInput".equals(method.getName())
                                    || args == null || args.length != 1) {
                                return enumConstant(statusCls, "UNDECIDED");
                            }
                            Class<?> sc = (Class<?>) serialClassM.invoke(args[0]);
                            return enumConstant(statusCls, mode.decide(sc));
                        }
                    });
            ObjectInputStream.class.getMethod("setObjectInputFilter", filterCls)
                    .invoke(ois, proxy);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean installViaJdk8(ObjectInputStream ois, final FilterMode mode) {
        try {
            final Class<?> filterCls = Class.forName("sun.misc.ObjectInputFilter");
            final Class<?> infoCls = Class.forName("sun.misc.ObjectInputFilter$FilterInfo");
            final Class<?> statusCls = Class.forName("sun.misc.ObjectInputFilter$Status");
            final Class<?> configCls = Class.forName("sun.misc.ObjectInputFilter$Config");
            final Method serialClassM = infoCls.getMethod("serialClass");
            Object proxy = Proxy.newProxyInstance(
                    filterCls.getClassLoader(),
                    new Class<?>[] { filterCls },
                    new InvocationHandler() {
                        public Object invoke(Object self, Method method, Object[] args)
                                throws Throwable {
                            if (!"checkInput".equals(method.getName())
                                    || args == null || args.length != 1) {
                                return enumConstant(statusCls, "UNDECIDED");
                            }
                            Class<?> sc = (Class<?>) serialClassM.invoke(args[0]);
                            return enumConstant(statusCls, mode.decide(sc));
                        }
                    });
            configCls.getMethod("setObjectInputFilter", ObjectInputStream.class, filterCls)
                    .invoke(null, ois, proxy);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object enumConstant(Class<?> enumClass, String name) {
        for (Object c : enumClass.getEnumConstants()) {
            if (name.equals(((Enum<?>) c).name())) {
                return c;
            }
        }
        throw new IllegalStateException(
                "No constant " + name + " in " + enumClass.getName());
    }
}
