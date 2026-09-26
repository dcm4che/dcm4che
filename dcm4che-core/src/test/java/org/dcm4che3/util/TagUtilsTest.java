package org.dcm4che3.util;

import static org.junit.Assert.*;
import org.dcm4che3.data.Tag;
import org.junit.Test;

public class TagUtilsTest {

    @Test
    public void testTypeOf() {
        assertEquals(TagUtils.Type.STANDARD, TagUtils.Type.typeOf(Tag.PatientName));
        assertEquals(TagUtils.Type.PRIVATE_CREATOR, TagUtils.Type.typeOf(0x00090010));
        assertEquals(TagUtils.Type.PRIVATE, TagUtils.Type.typeOf(0x00091010));
    }

    @Test
    public void testShortToHexString() {
        assertEquals("0010", TagUtils.shortToHexString(0x0010));
        assertEquals("ABCD", TagUtils.shortToHexString(0xABCD));
    }

    @Test
    public void testToHexString() {
        assertEquals("00100010", TagUtils.toHexString(Tag.PatientName));
        assertEquals("7FE00010", TagUtils.toHexString(Tag.PixelData));
    }

    @Test
    public void testToHexStrings() {
        int[] tags = {Tag.PatientName, Tag.PixelData};
        String[] expected = {"00100010", "7FE00010"};
        assertArrayEquals(expected, TagUtils.toHexStrings(tags));
    }

    @Test
    public void testToHexStringByteArray() {
        byte[] b = {0x12, 0x34, (byte)0xAB, (byte)0xCD};
        assertEquals("1234ABCD", TagUtils.toHexString(b));
    }

    @Test
    public void testFromHexString() {
        byte[] expected = {0x12, 0x34, (byte)0xAB, (byte)0xCD};
        assertArrayEquals(expected, TagUtils.fromHexString("1234ABCD"));
        assertArrayEquals(expected, TagUtils.fromHexString("1234abcd"));
    }

    @Test
    public void testIntFromHexString() {
        assertEquals(0x1234ABCD, TagUtils.intFromHexString("1234ABCD"));
    }

    @Test
    public void testFromHexStrings() {
        String[] ss = {"00100010", "7FE00010"};
        int[] expected = {Tag.PatientName, Tag.PixelData};
        assertArrayEquals(expected, TagUtils.fromHexStrings(ss));
    }

    @Test
    public void testToString() {
        assertEquals("(0010,0010)", TagUtils.toString(Tag.PatientName));
    }

    @Test
    public void testGroupNumber() {
        assertEquals(0x0010, TagUtils.groupNumber(Tag.PatientName));
    }

    @Test
    public void testElementNumber() {
        assertEquals(0x0010, TagUtils.elementNumber(Tag.PatientName));
    }

    @Test
    public void testIsGroupLength() {
        assertTrue(TagUtils.isGroupLength(0x00080000));
        assertFalse(TagUtils.isGroupLength(Tag.PatientName));
    }

    @Test
    public void testIsPrivateCreator() {
        assertTrue(TagUtils.isPrivateCreator(0x00090010));
        assertFalse(TagUtils.isPrivateCreator(Tag.PatientName));
        assertFalse(TagUtils.isPrivateCreator(0x00091010));
    }

    @Test
    public void testIsPrivateGroup() {
        assertTrue(TagUtils.isPrivateGroup(0x00090010));
        assertFalse(TagUtils.isPrivateGroup(Tag.PatientName));
    }

    @Test
    public void testIsPrivateTag() {
        assertTrue(TagUtils.isPrivateTag(0x00091010));
        assertFalse(TagUtils.isPrivateTag(Tag.PatientName));
    }

    @Test
    public void testToTag() {
        assertEquals(Tag.PatientName, TagUtils.toTag(0x0010, 0x0010));
    }

    @Test
    public void testToPrivateTag() {
        assertEquals(0x00091010, TagUtils.toPrivateTag(0x00090010, 0x0010));
    }

    @Test
    public void testCreatorTagOf() {
        assertEquals(0x00090010, TagUtils.creatorTagOf(0x00091010));
    }

    @Test
    public void testGroupLengthTagOf() {
        assertEquals(0x00100000, TagUtils.groupLengthTagOf(Tag.PatientName));
    }

    @Test
    public void testIsItem() {
        assertTrue(TagUtils.isItem(Tag.Item));
        assertTrue(TagUtils.isItem(Tag.ItemDelimitationItem));
        assertTrue(TagUtils.isItem(Tag.SequenceDelimitationItem));
        assertFalse(TagUtils.isItem(Tag.PatientName));
    }

    @Test
    public void testIsFileMetaInformation() {
        assertTrue(TagUtils.isFileMetaInformation(Tag.FileMetaInformationVersion));
        assertFalse(TagUtils.isFileMetaInformation(Tag.PatientName));
    }

    @Test
    public void testNormalizeRepeatingGroup() {
        assertEquals(0x60000010, TagUtils.normalizeRepeatingGroup(0x60020010));
        assertEquals(0x50000010, TagUtils.normalizeRepeatingGroup(0x50020010));
        assertEquals(Tag.PatientName, TagUtils.normalizeRepeatingGroup(Tag.PatientName));
    }

    @Test
    public void testForName() {
        assertEquals(Tag.PatientName, TagUtils.forName("PatientName"));
    }
}
