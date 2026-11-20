package org.dcm4che3.util;

import org.dcm4che3.data.Tag;
import org.junit.Test;
import static org.junit.Assert.*;


public class TagUtilsTest {

    @Test
    public void testTypeOf() {
        assertEquals(TagUtils.Type.STANDARD, TagUtils.Type.typeOf(0x00080010));
        assertEquals(TagUtils.Type.PRIVATE_CREATOR, TagUtils.Type.typeOf(0x00090010));
        assertEquals(TagUtils.Type.PRIVATE, TagUtils.Type.typeOf(0x00091010));
    }

    @Test
    public void testShortToHexString() {
        assertEquals("0008", TagUtils.shortToHexString(0x0008));
        assertEquals("0010", TagUtils.shortToHexString(0x0010));
        assertEquals("FFFF", TagUtils.shortToHexString(0xFFFF));
    }

    @Test
    public void testToHexString() {
        assertEquals("00080010", TagUtils.toHexString(0x00080010));
        assertEquals("FFFFFFFF", TagUtils.toHexString(0xFFFFFFFF));
    }

    @Test
    public void testToHexStrings() {
        assertArrayEquals(new String[]{"00080010", "00100010"}, TagUtils.toHexStrings(new int[]{0x00080010, 0x00100010}));
    }

    @Test
    public void testToHexStringFromBytes() {
        assertEquals("0102030A0B0F", TagUtils.toHexString(new byte[]{1, 2, 3, 10, 11, 15}));
    }

    @Test
    public void testFromHexString() {
        assertArrayEquals(new byte[]{1, 2, 3, 10, 11, 15}, TagUtils.fromHexString("0102030A0B0F"));
        assertArrayEquals(new byte[]{1, 2, 3, 10, 11, 15}, TagUtils.fromHexString("0102030a0b0f"));
    }

    @Test
    public void testIntFromHexString() {
        assertEquals(0x00080010, TagUtils.intFromHexString("00080010"));
        assertEquals(0xFFFFFFFF, TagUtils.intFromHexString("FFFFFFFF"));
    }

    @Test
    public void testFromHexStrings() {
        assertArrayEquals(new int[]{0x00080010, 0x00100010}, TagUtils.fromHexStrings(new String[]{"00080010", "00100010"}));
    }

    @Test
    public void testToString() {
        assertEquals("(0008,0010)", TagUtils.toString(0x00080010));
    }

    @Test
    public void testGroupNumber() {
        assertEquals(0x0008, TagUtils.groupNumber(0x00080010));
    }

    @Test
    public void testElementNumber() {
        assertEquals(0x0010, TagUtils.elementNumber(0x00080010));
    }

    @Test
    public void testIsGroupLength() {
        assertTrue(TagUtils.isGroupLength(0x00080000));
        assertFalse(TagUtils.isGroupLength(0x00080010));
    }

    @Test
    public void testIsPrivateCreator() {
        assertTrue(TagUtils.isPrivateCreator(0x00090010));
        assertTrue(TagUtils.isPrivateCreator(0x000900FF));
        assertFalse(TagUtils.isPrivateCreator(0x00080010));
        assertFalse(TagUtils.isPrivateCreator(0x00091010));
    }

    @Test
    public void testIsPrivateGroup() {
        assertTrue(TagUtils.isPrivateGroup(0x00090010));
        assertFalse(TagUtils.isPrivateGroup(0x00080010));
    }

    @Test
    public void testIsPrivateTag() {
        assertTrue(TagUtils.isPrivateTag(0x00091010));
        assertFalse(TagUtils.isPrivateTag(0x00090010));
        assertFalse(TagUtils.isPrivateTag(0x00080010));
    }

    @Test
    public void testToTag() {
        assertEquals(0x00080010, TagUtils.toTag(0x0008, 0x0010));
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
        assertEquals(0x00080000, TagUtils.groupLengthTagOf(0x00080010));
    }

    @Test
    public void testIsItem() {
        assertTrue(TagUtils.isItem(Tag.Item));
        assertTrue(TagUtils.isItem(Tag.ItemDelimitationItem));
        assertTrue(TagUtils.isItem(Tag.SequenceDelimitationItem));
        assertFalse(TagUtils.isItem(Tag.SOPInstanceUID));
    }

    @Test
    public void testIsFileMetaInformation() {
        assertTrue(TagUtils.isFileMetaInformation(0x00020010));
        assertFalse(TagUtils.isFileMetaInformation(0x00080010));
    }

    @Test
    public void testNormalizeRepeatingGroup() {
        assertEquals(0x60000010, TagUtils.normalizeRepeatingGroup(0x60020010));
        assertEquals(0x50000010, TagUtils.normalizeRepeatingGroup(0x50020010));
        assertEquals(0x00080010, TagUtils.normalizeRepeatingGroup(0x00080010));
    }

    @Test
    public void testForName() {
        assertEquals(Tag.SOPInstanceUID, TagUtils.forName("SOPInstanceUID"));
        assertEquals(-1, TagUtils.forName("NonExistentTag"));
    }

    @Test
    public void testParseTagPath() {
        assertArrayEquals(new int[]{Tag.ReferencedFrameNumber}, TagUtils.parseTagPath("ReferencedFrameNumber"));
        assertArrayEquals(new int[]{Tag.ReferencedImageSequence, Tag.ReferencedSOPInstanceUID}, 
                TagUtils.parseTagPath("ReferencedImageSequence.ReferencedSOPInstanceUID"));
    }
}
