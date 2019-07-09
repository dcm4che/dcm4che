package org.dcm4che3.data;

import org.junit.Before;
import org.junit.Test;

import java.sql.Date;

import static org.junit.Assert.*;

public class AttributesRemoveRepeatingGroupTest {

    private Attributes attributes;

    @Before
    public void setUp() {
        this.attributes = new Attributes();

        // 0x00 group
        this.attributes.setString(Tag.SpecificCharacterSet, VR.CS, "ISO_IR 100");
        this.attributes.setString(Tag.ImageType, VR.CS, "ORIGINAL");
        this.attributes.setDate(Tag.StudyDate, VR.DA, Date.valueOf("1990-01-01"));

        // 0x20 group
        this.attributes.setString(Tag.PresentationLUTShape, VR.CS, "IDENTITY");

        // 0x60 group
        this.attributes.setInt(Tag.OverlayRows, VR.US, 2012);
        this.attributes.setInt(Tag.OverlayColumns, VR.US, 2012);

        // 0x7F group
        this.attributes.setValue(Tag.PixelData, VR.OB, null);
    }

    @Test
    public void testRemoveNotExistingGroup() {
        int removed = this.attributes.removeRepeatingGroup(0x10000000);
        assertEquals(7, this.attributes.size());
        assertEquals(0, removed);
        assertTrue(this.attributes.contains(Tag.SpecificCharacterSet));
        assertTrue(this.attributes.contains(Tag.ImageType));
        assertTrue(this.attributes.contains(Tag.StudyDate));
        assertTrue(this.attributes.contains(Tag.PresentationLUTShape));
        assertTrue(this.attributes.contains(Tag.OverlayRows));
        assertTrue(this.attributes.contains(Tag.OverlayColumns));
        assertTrue(this.attributes.contains(Tag.PixelData));
    }

    @Test
    public void testRemoveGroupHaving1Tag() {
        int removed0x20 = this.attributes.removeRepeatingGroup(0x20000000);
        int removed0x7F = this.attributes.removeRepeatingGroup(0x7F000000);
        assertEquals(1, removed0x20);
        assertEquals(1, removed0x7F);
        assertEquals(5, this.attributes.size());
        assertTrue(this.attributes.contains(Tag.SpecificCharacterSet));
        assertTrue(this.attributes.contains(Tag.ImageType));
        assertTrue(this.attributes.contains(Tag.StudyDate));
        assertFalse(this.attributes.contains(Tag.PresentationLUTShape));
        assertTrue(this.attributes.contains(Tag.OverlayRows));
        assertTrue(this.attributes.contains(Tag.OverlayColumns));
        assertFalse(this.attributes.contains(Tag.PixelData));
    }

    @Test
    public void testRemoveGroupHavingMultipleTags() {
        int removed0x00 = this.attributes.removeRepeatingGroup(0x00000000);
        int removed0x60 = this.attributes.removeRepeatingGroup(0x60000000);
        assertEquals(3, removed0x00);
        assertEquals(2, removed0x60);
        assertEquals(2, this.attributes.size());
        assertFalse(this.attributes.contains(Tag.SpecificCharacterSet));
        assertFalse(this.attributes.contains(Tag.ImageType));
        assertFalse(this.attributes.contains(Tag.StudyDate));
        assertTrue(this.attributes.contains(Tag.PresentationLUTShape));
        assertFalse(this.attributes.contains(Tag.OverlayRows));
        assertFalse(this.attributes.contains(Tag.OverlayColumns));
        assertTrue(this.attributes.contains(Tag.PixelData));
    }
}