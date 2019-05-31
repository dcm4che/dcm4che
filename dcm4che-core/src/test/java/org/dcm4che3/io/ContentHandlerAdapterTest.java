package org.dcm4che3.io;

import static org.junit.Assert.assertArrayEquals;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.ContentHandlerAdapter;
import org.dcm4che3.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.helpers.AttributesImpl;

/**
 */
public class ContentHandlerAdapterTest {
    private Attributes attrs;
    private ContentHandlerAdapter contentHandler;

    private final byte[] TEST_DATA = {
        0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
        0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,
        0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66 };

    private final char[] BASE64_DATA = new char[32];

    private static final String DICOM_ATTRIBUTE = "DicomAttribute";
    private static final String TAG = "tag";
    private static final String VR = "vr";
    private static final String CDATA = "CDATA";
    private static final String INLINE_BINARY = "InlineBinary";

    @Before
    public void setup() throws Exception {
        this.attrs = new Attributes();
        this.contentHandler = new ContentHandlerAdapter(attrs);
        Base64.encode(TEST_DATA, 0, TEST_DATA.length, BASE64_DATA, 0);
    }

    @Test
    public void testInlineBinarySingleBlock() throws Exception {
        startAttributeTag("7FE00010");

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 32);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }
    
    @Test
    public void testInlineBinaryAlignedBlocks() throws Exception {
        startAttributeTag("7FE00010");

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 16);
        this.contentHandler.characters(BASE64_DATA, 16, 16);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }

    @Test
    public void testInlineBinaryAlignedBlocksWithOffset0() throws Exception {
        startAttributeTag("7FE00010");

        final char[] buffer2 = new char[16];
        System.arraycopy(BASE64_DATA, 16, buffer2, 0, 16);

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 16);
        this.contentHandler.characters(buffer2, 0, 16);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }
    
    @Test
    public void testInlineBinaryUnalignedBlocks() throws Exception {
        startAttributeTag("7FE00010");

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 17);
        this.contentHandler.characters(BASE64_DATA, 17, 15);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }

    @Test
    public void testInlineBinaryUnalignedBlocksWithOffset0() throws Exception {
        startAttributeTag("7FE00010");

        final char[] buffer2 = new char[15];
        System.arraycopy(BASE64_DATA, 17, buffer2, 0, 15);

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 17);
        this.contentHandler.characters(buffer2, 0, 15);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }

    @Test
    public void testInlineBinarySmallBlocks() throws Exception {
        startAttributeTag("7FE00010");

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 17);
        this.contentHandler.characters(BASE64_DATA, 17, 1);
        this.contentHandler.characters(BASE64_DATA, 18, 1);
        this.contentHandler.characters(BASE64_DATA, 19, 1);
        this.contentHandler.characters(BASE64_DATA, 20, 12);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }

    @Test
    public void testInlineBinarySmallBlocksWithOffset0() throws Exception {
        startAttributeTag("7FE00010");
        
        final char[] buffer2 = new char[15];
        System.arraycopy(BASE64_DATA, 17, buffer2, 0, 15);

        this.contentHandler.startElement("", INLINE_BINARY, INLINE_BINARY, new AttributesImpl());
        this.contentHandler.characters(BASE64_DATA, 0, 17);
        this.contentHandler.characters(buffer2, 0, 1);
        this.contentHandler.characters(buffer2, 1, 1);
        this.contentHandler.characters(buffer2, 2, 1);
        this.contentHandler.characters(buffer2, 3, 12);
        this.contentHandler.endElement("", INLINE_BINARY, INLINE_BINARY);

        endAttributeTag();

        assertArrayEquals(TEST_DATA, attrs.getBytes(0x7FE00010));
    }

    private void startAttributeTag(final String tag) throws Exception {
        final AttributesImpl dicomAttributes = new AttributesImpl();
        dicomAttributes.addAttribute("", TAG, TAG, CDATA, tag);
        dicomAttributes.addAttribute("", VR, VR, CDATA, "OB");
        this.contentHandler.startElement("", DICOM_ATTRIBUTE, DICOM_ATTRIBUTE, dicomAttributes);
    }

    private void endAttributeTag() throws Exception {
        this.contentHandler.endElement("", DICOM_ATTRIBUTE, DICOM_ATTRIBUTE);
    }
}
