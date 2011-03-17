package org.dcm4che.util;

import org.dcm4che.data.Tag;

public class TagUtils {

    private static char[] HEX_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public static String shortToHexString(int n) {
        char[] s = {
                HEX_DIGITS[(n >>> 12) & 0xF],
                HEX_DIGITS[(n >>> 8) & 0xF],
                HEX_DIGITS[(n >>> 4) & 0xF],
                HEX_DIGITS[(n >>> 0) & 0xF] };
        return new String(s);
    }

    public static String toHexString(int tag) {
        char[] s = {
                HEX_DIGITS[(tag >>> 28)],
                HEX_DIGITS[(tag >>> 24) & 0xF],
                HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF],
                HEX_DIGITS[(tag >>> 12) & 0xF],
                HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF],
                HEX_DIGITS[(tag >>> 0) & 0xF] };
        return new String(s);
    }

    public static String toString(int tag) {
        char[] s = {
                '(',
                HEX_DIGITS[(tag >>> 28)],
                HEX_DIGITS[(tag >>> 24) & 0xF],
                HEX_DIGITS[(tag >>> 20) & 0xF],
                HEX_DIGITS[(tag >>> 16) & 0xF],
                ',',
                HEX_DIGITS[(tag >>> 12) & 0xF],
                HEX_DIGITS[(tag >>> 8) & 0xF],
                HEX_DIGITS[(tag >>> 4) & 0xF],
                HEX_DIGITS[(tag >>> 0) & 0xF],
                ')'};
        return new String(s);
    }

    public static int groupNumber(int tag) {
        return tag >>> 16;
    }

    public static int elementNumber(int tag) {
        return tag & 0xFFFF;
    }

    public static boolean isGroupLength(int tag) {
        return elementNumber(tag) == 0;
    }

    public static boolean isPrivateCreator(int tag) {
        return (tag & 0x00010000) != 0
            && (tag & 0x0000FF00) == 0
            && (tag & 0x000000FF) != 0;
    }

    public static boolean isPrivateGroup(int tag) {
        return (tag & 0x00010000) != 0;
    }

    public static boolean isPrivateTag(int tag) {
        return (tag & 0x00010000) != 0
            && (tag & 0x0000FF00) != 0;
    }

    public static int toTag(int groupNumber, int elementNumber) {
        return groupNumber << 16 | elementNumber;
    }

    public static int toPrivateTag(int creatorTag, int elementNumber) {
        return (creatorTag & 0xffff0000) 
             | ((creatorTag & 0xff) << 8
             | (elementNumber & 0xff));
    }

    public static int creatorTagOf(int tag) {
        return (tag & 0xffff0000) | ((tag >>> 8) & 0xff);
    }

    public static int groupLengthTagOf(int tag) {
        return tag & 0xffff0000;
    }

    public static boolean isItem(int tag) {
        return tag == Tag.Item
            || tag == Tag.ItemDelimitationItem
            || tag == Tag.SequenceDelimitationItem;
    }

    public static boolean isFileMetaInformation(int tag) {
        return (tag & 0xffff0000) == 0x00020000;
    }

}

