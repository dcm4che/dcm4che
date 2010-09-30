package org.dcm4che.data;

import static org.junit.Assert.*;


import org.junit.Test;

public class SpecificCharacterSetTest {

    private static final String JAPANESE_PERSON_NAME_ASCII =
            "Yamada^Tarou=山田^太郎=やまだ^たろう";

    private static final byte[] JAPANESE_PERSON_NAME_ASCII_BYTES = {
            (byte) 0x59, (byte) 0x61, (byte) 0x6d, (byte) 0x61, (byte) 0x64,
            (byte) 0x61, (byte) 0x5e, (byte) 0x54, (byte) 0x61, (byte) 0x72,
            (byte) 0x6f, (byte) 0x75, (byte) 0x3d, (byte) 0x1b, (byte) 0x24,
            (byte) 0x42, (byte) 0x3b, (byte) 0x33, (byte) 0x45, (byte) 0x44,
            (byte) 0x1b, (byte) 0x28, (byte) 0x42, (byte) 0x5e, (byte) 0x1b,
            (byte) 0x24, (byte) 0x42, (byte) 0x42, (byte) 0x40, (byte) 0x4f,
            (byte) 0x3a, (byte) 0x1b, (byte) 0x28, (byte) 0x42, (byte) 0x3d,
            (byte) 0x1b, (byte) 0x24, (byte) 0x42, (byte) 0x24, (byte) 0x64,
            (byte) 0x24, (byte) 0x5e, (byte) 0x24, (byte) 0x40, (byte) 0x1b,
            (byte) 0x28, (byte) 0x42, (byte) 0x5e, (byte) 0x1b, (byte) 0x24,
            (byte) 0x42, (byte) 0x24, (byte) 0x3f, (byte) 0x24, (byte) 0x6d,
            (byte) 0x24, (byte) 0x26, (byte) 0x1b, (byte) 0x28, (byte) 0x42 };

    private SpecificCharacterSet jisX0208() {
        return SpecificCharacterSet.valueOf(
                new String[] { null, "ISO 2022 IR 87" });
    }

    @Test
    public void testEncodeJapanesePersonNameASCII() {
        assertArrayEquals(JAPANESE_PERSON_NAME_ASCII_BYTES,
                jisX0208().encode(JAPANESE_PERSON_NAME_ASCII, "^=\\"));
    }

    @Test
    public void testDecodeJapanesePersonNameASCII() {
        assertEquals(JAPANESE_PERSON_NAME_ASCII,
                jisX0208().decode(JAPANESE_PERSON_NAME_ASCII_BYTES));
    }

    private static final String JAPANESE_PERSON_NAME_JISX0201 =
             "ﾔﾏﾀﾞ^ﾀﾛｳ=山田^太郎=やまだ^たろう";

    private static final byte[] JAPANESE_PERSON_NAME_JISX0201_BYTES = {
            (byte) 0xd4, (byte) 0xcf, (byte) 0xc0, (byte) 0xde, (byte) 0x5e,
            (byte) 0xc0, (byte) 0xdb, (byte) 0xb3, (byte) 0x3d, (byte) 0x1b,
            (byte) 0x24, (byte) 0x42, (byte) 0x3b, (byte) 0x33, (byte) 0x45,
            (byte) 0x44, (byte) 0x1b, (byte) 0x28, (byte) 0x4a, (byte) 0x5e,
            (byte) 0x1b, (byte) 0x24, (byte) 0x42, (byte) 0x42, (byte) 0x40,
            (byte) 0x4f, (byte) 0x3a, (byte) 0x1b, (byte) 0x28, (byte) 0x4a,
            (byte) 0x3d, (byte) 0x1b, (byte) 0x24, (byte) 0x42, (byte) 0x24,
            (byte) 0x64, (byte) 0x24, (byte) 0x5e, (byte) 0x24, (byte) 0x40,
            (byte) 0x1b, (byte) 0x28, (byte) 0x4a, (byte) 0x5e, (byte) 0x1b,
            (byte) 0x24, (byte) 0x42, (byte) 0x24, (byte) 0x3f, (byte) 0x24,
            (byte) 0x6d, (byte) 0x24, (byte) 0x26, (byte) 0x1b, (byte) 0x28,
            (byte) 0x4a };

    private SpecificCharacterSet jisX0201() {
        return SpecificCharacterSet.valueOf(
                new String[] { "ISO 2022 IR 13", "ISO 2022 IR 87" });
    }

     @Test
    public void testEncodeJapanesePersonNameJISX0201() {
        assertArrayEquals(JAPANESE_PERSON_NAME_JISX0201_BYTES,
                jisX0201().encode(JAPANESE_PERSON_NAME_JISX0201, "^=\\"));
    }

    @Test
    public void testDecodeJapanesePersonNameJISX0201() {
        assertEquals(JAPANESE_PERSON_NAME_JISX0201,
                jisX0201().decode(JAPANESE_PERSON_NAME_JISX0201_BYTES));
    }

    private SpecificCharacterSet ksx1001() {
        return SpecificCharacterSet.valueOf(
                new String[] { null, "ISO 2022 IR 149" });
    }

    private static final String KOREAN_PERSON_NAME =
            "Hong^Gildong=洪^吉洞=홍^길동";

    private static final byte[] KOREAN_PERSON_NAME_BYTES = {
            (byte) 0x48, (byte) 0x6f, (byte) 0x6e, (byte) 0x67, (byte) 0x5e,
            (byte) 0x47, (byte) 0x69, (byte) 0x6c, (byte) 0x64, (byte) 0x6f,
            (byte) 0x6e, (byte) 0x67, (byte) 0x3d, (byte) 0x1b, (byte) 0x24,
            (byte) 0x29, (byte) 0x43, (byte) 0xfb, (byte) 0xf3, (byte) 0x5e,
            (byte) 0x1b, (byte) 0x24, (byte) 0x29, (byte) 0x43, (byte) 0xd1,
            (byte) 0xce, (byte) 0xd4, (byte) 0xd7, (byte) 0x3d, (byte) 0x1b,
            (byte) 0x24, (byte) 0x29, (byte) 0x43, (byte) 0xc8, (byte) 0xab,
            (byte) 0x5e, (byte) 0x1b, (byte) 0x24, (byte) 0x29, (byte) 0x43,
            (byte) 0xb1, (byte) 0xe6, (byte) 0xb5, (byte) 0xbf };

    @Test
    public void testEncodeKoreanPersonName() {
        assertArrayEquals(KOREAN_PERSON_NAME_BYTES,
                ksx1001().encode(KOREAN_PERSON_NAME, "^=\\"));
    }

    @Test
    public void testDecodeKoreanPersonName() {
        assertEquals(KOREAN_PERSON_NAME,
                ksx1001().decode(KOREAN_PERSON_NAME_BYTES));
    }

    private static final String KOREAN_LONG_TEXT = 
            "The 1st line includes 길동.\r\n" +
            "The 2nd line includes 길동, too.\r\n" +
            "The 3rd line.";

    private static final byte[] KOREAN_LONG_TEXT_BYTES = {
            (byte) 0x1b, (byte) 0x24, (byte) 0x29, (byte) 0x43, (byte) 0x54,
            (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x31, (byte) 0x73,
            (byte) 0x74, (byte) 0x20, (byte) 0x6c, (byte) 0x69, (byte) 0x6e,
            (byte) 0x65, (byte) 0x20, (byte) 0x69, (byte) 0x6e, (byte) 0x63,
            (byte) 0x6c, (byte) 0x75, (byte) 0x64, (byte) 0x65, (byte) 0x73,
            (byte) 0x20, (byte) 0xb1, (byte) 0xe6, (byte) 0xb5, (byte) 0xbf,
            (byte) 0x2e, (byte) 0x0d, (byte) 0x0a, (byte) 0x1b, (byte) 0x24,
            (byte) 0x29, (byte) 0x43, (byte) 0x54, (byte) 0x68, (byte) 0x65,
            (byte) 0x20, (byte) 0x32, (byte) 0x6e, (byte) 0x64, (byte) 0x20,
            (byte) 0x6c, (byte) 0x69, (byte) 0x6e, (byte) 0x65, (byte) 0x20,
            (byte) 0x69, (byte) 0x6e, (byte) 0x63, (byte) 0x6c, (byte) 0x75,
            (byte) 0x64, (byte) 0x65, (byte) 0x73, (byte) 0x20, (byte) 0xb1,
            (byte) 0xe6, (byte) 0xb5, (byte) 0xbf, (byte) 0x2c, (byte) 0x20,
            (byte) 0x74, (byte) 0x6f, (byte) 0x6f, (byte) 0x2e, (byte) 0x0d,
            (byte) 0x0a, (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20,
            (byte) 0x33, (byte) 0x72, (byte) 0x64, (byte) 0x20, (byte) 0x6c,
            (byte) 0x69, (byte) 0x6e, (byte) 0x65, (byte) 0x2e };
    
    @Test
    public void testEncodeKoreanLongText() {
        assertArrayEquals(KOREAN_LONG_TEXT_BYTES,
                ksx1001().encode(KOREAN_LONG_TEXT, "\n\f\r"));
    }
    
    @Test
    public void testDecodeKoreanLongText() {
        assertEquals(KOREAN_LONG_TEXT,
                ksx1001().decode(KOREAN_LONG_TEXT_BYTES));
    }

    private static final String CHINESE_PERSON_NAME_UTF8 =
            "Wang^XiaoDong=王^小東=";

    private static final byte[] CHINESE_PERSON_NAME_UTF8_BYTES = {
            (byte) 0x57, (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x5e,
            (byte) 0x58, (byte) 0x69, (byte) 0x61, (byte) 0x6f, (byte) 0x44,
            (byte) 0x6f, (byte) 0x6e, (byte) 0x67, (byte) 0x3d, (byte) 0xe7,
            (byte) 0x8e, (byte) 0x8b, (byte) 0x5e, (byte) 0xe5, (byte) 0xb0,
            (byte) 0x8f, (byte) 0xe6, (byte) 0x9d, (byte) 0xb1, (byte) 0x3d };

    private SpecificCharacterSet utf8() {
        return SpecificCharacterSet.valueOf(new String[] { "ISO_IR 192" });
    }

    @Test
    public void testEncodeUTF8() {
        assertArrayEquals(CHINESE_PERSON_NAME_UTF8_BYTES,
                utf8().encode(CHINESE_PERSON_NAME_UTF8, "^=\\"));
    }

    @Test
    public void testDecodeUTF8() {
        assertEquals(CHINESE_PERSON_NAME_UTF8,
                utf8().decode(CHINESE_PERSON_NAME_UTF8_BYTES));
    }

    private static final String CHINESE_PERSON_NAME_GB18030 =
            "Wang^XiaoDong=王^小东=";

    private static final byte[] CHINESE_PERSON_NAME_GB18030_BYTES = {
           (byte) 0x57, (byte) 0x61, (byte) 0x6e, (byte) 0x67, (byte) 0x5e,
           (byte) 0x58, (byte) 0x69, (byte) 0x61, (byte) 0x6f, (byte) 0x44,
           (byte) 0x6f, (byte) 0x6e, (byte) 0x67, (byte) 0x3d, (byte) 0xcd,
           (byte) 0xf5, (byte) 0x5e, (byte) 0xd0, (byte) 0xa1, (byte) 0xb6,
           (byte) 0xab, (byte) 0x3d };

    private SpecificCharacterSet gb18030() {
        return SpecificCharacterSet.valueOf(new String[] { "GB18030" });
    }

    @Test
    public void testEncodeGB18030() {
        assertArrayEquals(CHINESE_PERSON_NAME_GB18030_BYTES,
                gb18030().encode(CHINESE_PERSON_NAME_GB18030, "^=\\"));
    }

    @Test
    public void testDecodeGB18030() {
        assertEquals(CHINESE_PERSON_NAME_GB18030,
                gb18030().decode(CHINESE_PERSON_NAME_GB18030_BYTES));
    }

}
