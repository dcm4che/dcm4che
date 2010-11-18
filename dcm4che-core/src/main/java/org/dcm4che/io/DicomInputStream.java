package org.dcm4che.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.ElementDictionary;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.util.ByteUtils;
import org.dcm4che.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DicomInputStream extends FilterInputStream
    implements DicomInputHandler {

    private static final Logger LOG = 
        LoggerFactory.getLogger(DicomInputStream.class);

    private static final String UNEXPECTED_NON_ZERO_ITEM_LENGTH =
        "Unexpected item value of {} #{} @ {}";
    private static final String UNEXPECTED_ATTRIBUTE =
        "Unexpected attribute {} #{} @ {}";
    private static final String MISSING_TRANSFER_SYNTAX =
        "Missing Transfer Syntax (0002,0010) - assume Explicit VR Little Endian";
    private static final String MISSING_FMI_LENGTH =
        "Missing or wrong File Meta Information Group Length (0002,0000)";
    private static final String NOT_A_DICOM_STREAM = 
        "Not a DICOM Stream";
    private static final String DEFLATED_WITH_ZLIB_HEADER =
        "Deflated DICOM Stream with ZLIB Header";

    private static final int ZLIB_HEADER = 0x789c;
    private static byte[] EMPTY_BYTES = {};

    private byte[] preamble;
    private Attributes fileMetaInformation;
    private boolean bigEndian;
    private boolean explicitVR;
    private long pos;
    private long fmiEndPos = -1L;
    private long tagPos;
    private long markPos;
    private int level;
    private int tag;
    private VR vr;
    private int length;
    private DicomInputHandler handler = this;
    private final byte[] buffer = new byte[8];

    public DicomInputStream(InputStream in, String tsuid) throws IOException {
        super(in);
        switchTransferSyntax(tsuid);
    }

    public DicomInputStream(InputStream in) throws IOException {
        super(buffer(in));
        guessTransferSyntax();
    }

    private static BufferedInputStream buffer(InputStream in) {
        return (in instanceof BufferedInputStream) 
                ? (BufferedInputStream) in
                : new BufferedInputStream(in);
    }

    public final void setDicomInputHandler(DicomInputHandler handler) {
        if (handler == null)
            throw new NullPointerException("handler");
        this.handler = handler;
    }

    public final void setFileMetaInformationGroupLength(byte[] val) {
        fmiEndPos = pos + ByteUtils.bytesToInt(val, 0, bigEndian);
    }

    public final byte[] getPreamble() {
        return preamble;
    }

    public final Attributes getFileMetaInformation() {
        return fileMetaInformation;
    }

    public final int level() {
        return level;
    }

    public final int tag() {
        return tag;
    }

    public final VR vr() {
        return vr;
    }

    public final int length() {
        return length;
    }

    public final long getPosition() {
        return pos;
    }

    public long getTagPosition() {
        return tagPos;
    }

    public final boolean bigEndian() {
        return bigEndian;
    }

    public final boolean explicitVR() {
        return explicitVR;
    }

   @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        markPos = pos;
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        pos = markPos;
    }

    @Override
    public final int read() throws IOException {
        int read = super.read();
        if (read >= 0)
            pos++;
        return read;
    }

    @Override
    public final int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        if (read > 0)
            pos += read;
        return read;
    }

    @Override
    public final int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public final long skip(long n) throws IOException {
        long skip = super.skip(n);
        pos += skip;
        return skip;
    }

    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte b[], int off, int len)
            throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0)
                throw new EOFException();
            n += count;
        }
    }

    public final void readHeader() throws IOException {
        byte[] buf = buffer;
        tagPos = pos; 
        readFully(buf, 0, 8);
        switch(tag = ByteUtils.bytesToTag(buf, 0, bigEndian)) {
        case Tag.Item:
        case Tag.ItemDelimitationItem:
        case Tag.SequenceDelimitationItem:
           vr = null;
           break;
        default:
            if (explicitVR) {
                vr = VR.valueOf(ByteUtils.bytesToVR(buf, 4));
                if (vr.headerLength() == 8) {
                    length = ByteUtils.bytesToUShort(buf, 6, bigEndian);
                    return;
                }
                readFully(buf, 4, 4);
            } else {
                vr = VR.UN;
            }
        }
        length = ByteUtils.bytesToInt(buf, 4, bigEndian);
    }

    public void readAttributes(Attributes attrs, int len) throws IOException {
        readAttributes(attrs, len, false);
    }

    public Attributes readCommand(int len) throws IOException {
        if (bigEndian || explicitVR)
            throw new IllegalStateException(
                    "bigEndian=" + bigEndian + ", explicitVR=" + explicitVR );
        Attributes attrs = new Attributes(1);
        readAttributes(attrs, len, false);
        return attrs;
    }

    public Attributes readDataset(int len) throws IOException {
        Attributes attrs = new Attributes(bigEndian);
        readAttributes(attrs, len, false);
        return attrs;
    }

    private void readFileMetaInformation() throws IOException {
        Attributes attrs = new Attributes(bigEndian, 1);
        readAttributes(attrs, -1, true);
        String tsuid = attrs.getString(
                Tag.TransferSyntaxUID, null, 0, null);
        if (tsuid == null) {
            LOG.warn(MISSING_TRANSFER_SYNTAX);
            tsuid = UID.ExplicitVRLittleEndian;
        }
        fileMetaInformation = attrs;
        switchTransferSyntax(tsuid);
    }

    private void readAttributes(Attributes attrs, int len,
            boolean fmi) throws IOException {
        long endPos =  pos + (len & 0xffffffffL);
        boolean undeflen = len == -1;
        while (fmi ? this.pos != fmiEndPos
                : (undeflen || this.pos < endPos)) {
            mark(12);
            try {
                readHeader();
            } catch (EOFException e) {
                if (undeflen && pos == tagPos)
                    break;
                throw e;
            }
            if (fmi && TagUtils.groupNumber(tag) != 2) {
                LOG.warn(MISSING_FMI_LENGTH);
                reset();
                break;
            }
            boolean prevBigEndian = bigEndian;
            boolean prevExplicitVR = explicitVR;
            try {
                if (vr == VR.UN) {
                    bigEndian = false;
                    explicitVR = false;
                    vr = ElementDictionary.vrOf(tag,
                            attrs.getPrivateCreator(tag));
                    if (vr == VR.UN && length == -1)
                        vr = VR.SQ; // assumes UN with undefined length are SQ,
                                    // will fail on UN fragments!
                }
                if (!handler.readValue(this, attrs))
                    break;
            } finally {
                bigEndian = prevBigEndian;
                explicitVR = prevExplicitVR;
            }
            if (fmi && pos == fmiEndPos)
                break;
        }
        attrs.trimToSize();
    }

    @Override
    public boolean readValue(DicomInputStream dis, Attributes attrs)
        throws IOException {

        checkIsThis(dis);
        if (vr == null) {
            if (tag == Tag.ItemDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                return false;
            }
            skipAttribute(UNEXPECTED_ATTRIBUTE);
        } else if (vr == VR.SQ) {
            readSequence(length, attrs, tag);
        } else if (length == -1) {
            readFragments(attrs, tag, vr);
        } else {
            byte[] b = readValue(length);
            if (!TagUtils.isGroupLength(tag)) {
                if (bigEndian != attrs.bigEndian())
                    vr.toggleEndian(b, false);
                attrs.setBytes(tag, null, vr, b);
            } else if (tag == Tag.FileMetaInformationGroupLength)
                setFileMetaInformationGroupLength(b);
        }
        return true;
    }

    @Override
    public boolean readValue(DicomInputStream dis, Sequence seq)
        throws IOException {

        checkIsThis(dis);
        if (vr == null) {
            if (tag == Tag.Item) {
                Attributes attrs = new Attributes(seq.getParent().bigEndian());
                seq.add(attrs);
                readAttributes(attrs, length);
                return true;
            }
            if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                return false;
            }
        }
        skipAttribute(UNEXPECTED_ATTRIBUTE);
        return true;
    }

    @Override
    public boolean readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        checkIsThis(dis);
        if (this.vr == null) {
            if (tag == Tag.Item) {
                byte[] b = readValue(length);
                if (bigEndian != frags.bigEndian())
                    vr.toggleEndian(b, false);
                frags.add(b);
                return true;
            }
            if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                return false;
            }
        }
        skipAttribute(UNEXPECTED_ATTRIBUTE);
        return true;
    }

    private void checkIsThis(DicomInputStream dis) {
        if (dis != this)
            throw new IllegalArgumentException("dis != this");
    }

    private void skipAttribute(String message) throws IOException {
        LOG.warn(message,
                 new Object[] { TagUtils.toString(tag), length, tagPos });
        skip(length);
    }

    private void readSequence(int len, Attributes attrs, int tag)
        throws IOException {

        if (len == 0)
            attrs.setNull(tag, null, VR.SQ);
        else {
            Sequence seq = attrs.newSequence(tag, null, 10);
            readSequence(len, seq);
            if (seq.isEmpty())
                attrs.setNull(tag, null, VR.SQ);
            else
                seq.trimToSize();
        }
    }

    private void readSequence(int len, Sequence seq) throws IOException {
        if (len == 0)
            return;

        boolean undefLen = len == -1;
        long endPos = pos + (len & 0xffffffffL);
        level++;
        do {
            readHeader();
        } while (handler.readValue(this, seq) && (undefLen || pos < endPos));
        level--;
    }

    private void readFragments(Attributes attrs, int tag, VR vr)
            throws IOException {
        Fragments frags =
                attrs.newFragments(tag, null, vr, attrs.bigEndian(), 10);
        level++;
        do {
            readHeader();
        } while (handler.readValue(this, frags));
        level--;
        if (frags.isEmpty())
            attrs.setNull(tag, null, vr);
        else
            frags.trimToSize();
    }

    private byte[] readValue(int len) throws IOException {
        if (len == 0)
            return EMPTY_BYTES;
        byte[] value = new byte[len];
        readFully(value);
        return value;
    }

    private void switchTransferSyntax(String tsuid) throws IOException {
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                        || tsuid.equals(UID.JPIPReferencedDeflate)) {
            if (hasZLIBHeader()) {
                LOG.warn(DEFLATED_WITH_ZLIB_HEADER);
                super.in = new InflaterInputStream(super.in);
            } else
                super.in = new InflaterInputStream(super.in,
                        new Inflater(true));
        }
    }

    private boolean hasZLIBHeader() throws IOException {
        if (!markSupported())
            return false;
        byte[] buf = buffer;
        mark(2);
        read(buf, 0, 2);
        reset();
        return ByteUtils.bytesToUShortBE(buf, 0) == ZLIB_HEADER;
    }

    private void guessTransferSyntax() throws IOException {
        byte[] b128 = new byte[128];
        byte[] buf = buffer;
        mark(132);
        read(b128);
        read(buf, 0, 4);
        if (buf[0] == 'D' && buf[1] == 'I'
                && buf[2] == 'C' && buf[3] == 'M') {
            preamble = b128.clone();
            if (!markSupported()) {
                bigEndian = false;
                explicitVR = true;
                readFileMetaInformation();
                return;
            }
            mark(128);
            read(b128);
        }
        if (!guessTransferSyntax(b128, false)
                && !guessTransferSyntax(b128, true))
            throw new DicomStreamException(NOT_A_DICOM_STREAM);
        reset();
        if (TagUtils.groupNumber(ByteUtils.bytesToTag(b128, 0, bigEndian))
                == 2)
            readFileMetaInformation();
    }

    private boolean guessTransferSyntax(byte[] b128, boolean bigEndian) {
        int tag1 = ByteUtils.bytesToTag(b128, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN)
            return false;
        if (ByteUtils.bytesToVR(b128, 4) == vr.code()) {
            this.bigEndian = bigEndian;
            this.explicitVR = true;
            return true;
        }
        int len = ByteUtils.bytesToInt(b128, 4, bigEndian);
        if (len < 0 || len > 116)
            return false;
        int tag2 = ByteUtils.bytesToTag(b128, len + 8, bigEndian);
        if (TagUtils.groupNumber(tag1) == TagUtils.groupNumber(tag2) &&
            tag1 < tag2) {

            this.bigEndian = bigEndian;
            this.explicitVR = false;
            return true;
        }
        return false;
    }

}
