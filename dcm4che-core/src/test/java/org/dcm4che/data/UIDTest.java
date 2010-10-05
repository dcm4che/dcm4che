package org.dcm4che.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class UIDTest {

    @Test
    public void testNameOf() {
        assertEquals("Implicit VR Little Endian",
                UID.nameOf(UID.ImplicitVRLittleEndian));
    }

    @Test
    public void testForName() {
        assertEquals(UID.ImplicitVRLittleEndian,
                UID.forName("ImplicitVRLittleEndian"));
    }

}
