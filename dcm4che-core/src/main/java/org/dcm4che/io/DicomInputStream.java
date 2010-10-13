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
            "Unexpected item value of {0} #{1} @ {2}";
    private static final String UNEXPECTED_ATTRIBUTE =
            "Unexpected attribute {0} #{1} @ {2}";
    private static final String MISSING_TRANSFER_SYNTAX =
            "Missing Transfer Syntax (0002,0010) - assume Explicit VR Little Endian";
    private static final String MISSING_FMI_LENGTH =
            "Missing or wrong File Meta Information Group Length (0002,0000)";

    private byte[] preamble;
    private boolean bigEndian;
    private boolean explicitVR;
    private long pos;
    private long tagPos;
    private long markPos;
    private int tag;
    private VR vr;
    private int length;
    private Attributes attrs;
    private DicomInputHandler handler = this;
    private final byte[] buffer = new byte[8];

    public DicomInputStream(InputStream in, String tsuid) {
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

    private void switchTransferSyntax(String tsuid) {
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                        || tsuid.equals(UID.JPIPReferencedDeflate))
            super.in = new InflaterInputStream(super.in, new Inflater(true));
        if (attrs != null)
            attrs.setBigEndian(bigEndian);
    }

    private void switchTransferSyntaxUID() {
        String tsuid = attrs.getString(
                Tag.TransferSyntaxUID, null, null);
        if (tsuid == null) {
            LOG.warn(MISSING_TRANSFER_SYNTAX);
            tsuid = UID.ExplicitVRLittleEndian;
        }
        switchTransferSyntax(tsuid);
    }

    private void guessTransferSyntax() throws IOException {
        byte[] b128 = new byte[128];
        byte[] buf = buffer;
        mark(132);
        read(b128);
        read(buf, 0, 4);
        if (buf[0] == 'D' && buf[1] == 'I'
                && buf[2] == 'C' && buf[3] == 'M') {
            preamble = b128;
            bigEndian = false;
            explicitVR = true;
            return;
        }
        if (!guessTransferSyntax(b128, false)
                && !guessTransferSyntax(b128, true))
            throw new DicomStreamException("Not a DICOM Stream");
        reset();
    }

    private boolean guessTransferSyntax(byte[] b128, boolean bigEndian) {
        int tag1 = ByteUtils.bytesToTag(b128, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN)
            return false;
        if (ByteUtils.bytesToVR(b128, 4) == vr.code()) {
            this.bigEndian = bigEndian;
            explicitVR = true;
            return true;
        }
        int len = ByteUtils.bytesToInt(b128, 4, bigEndian);
        if (len < 0 || len > 116)
            return false;
        int tag2 = ByteUtils.bytesToTag(b128, len + 8, bigEndian);
        int diff = tag2 - tag1;
        if (diff > 0 && diff < 0x00010000) {
            this.bigEndian = bigEndian;
            explicitVR = false;
            return true;
        }
        return false;
    }

    public byte[] getPreamble() {
        return preamble != null ? preamble.clone() : null;
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
        if (attrs == null)
            throw new IllegalStateException("No Attributes set");
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
                vr = attrs.vrOf(tag);
            }
        }
        length = ByteUtils.bytesToInt(buf, 4, bigEndian);
    }

    public Attributes readAttributes(int len) throws IOException {
        return readAttributes(len, false);
    }

    public Attributes readFileMetaInformation() throws IOException {
        return readAttributes(-1, true);
    }

    private Attributes readAttributes(int len, boolean stopAfterFmi)
            throws IOException {
        Attributes parentAttrs = this.attrs;
        this.attrs = new Attributes().setBigEndian(
                parentAttrs == null ? bigEndian : parentAttrs.isBigEndian());
        try {
            long endPos =  pos + (len & 0xffffffffL);
            long fmiEndPos = -1L;
            boolean undeflen = len == -1;
            boolean first = true;
            boolean expect0002xxxx = false;
            while (undeflen || this.pos < endPos) {
                mark(12);
                try {
                    readHeader();
                } catch (EOFException e) {
                    if (undeflen && pos == tagPos)
                        break;
                    throw e;
                }
                if (expect0002xxxx && (tag >>> 16) != 2) {
                    LOG.warn(MISSING_FMI_LENGTH);
                    reset();
                    if (stopAfterFmi)
                        return attrs;
                    switchTransferSyntaxUID();
                    expect0002xxxx = false;
                    continue;
                }
                if (vr == null) {
                    if (undeflen && tag == Tag.ItemDelimitationItem) {
                        if (length != 0)
                            skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                        break;
                    }
                    skipAttribute(UNEXPECTED_ATTRIBUTE);
                } else if (vr == VR.UN) {
                    boolean prevBigEndian = bigEndian;
                    boolean prevExplicitVR = explicitVR;
                    try {
                        if (prevExplicitVR)
                            vr = attrs.vrOf(tag);
                        if (vr == VR.UN && length == -1)
                            vr = VR.SQ;
                        if (!handler.readValue(this))
                            break;
                    } finally {
                        bigEndian = prevBigEndian;
                        explicitVR = prevExplicitVR;
                    }
                } else {
                    if (!handler.readValue(this))
                        break;
                }
                if (first && parentAttrs == null && (tag >>> 16) == 2) {
                    expect0002xxxx = true;
                    if (tag == Tag.FileMetaInformationGroupLength)
                        fmiEndPos = pos + attrs.getInt(
                                Tag.FileMetaInformationGroupLength, null, 0);
                } else if (expect0002xxxx && pos == fmiEndPos)  {
                    if (stopAfterFmi)
                        return attrs;
                    switchTransferSyntaxUID();
                    expect0002xxxx = false;
                }
                first = false;
            }
            return attrs;
        } finally {
            this.attrs = parentAttrs;
        }
    }

    private void readSequence(int len) throws IOException {
        Sequence seq = null;
        int seqtag = tag;
        long endPos =  pos + (len & 0xffffffffL);
        boolean undeflen = len == -1;
        while (undeflen || pos < endPos) {
            readHeader();
            if (tag == Tag.Item) {
                if (seq == null)
                    seq = attrs.putSequence(seqtag, null, 1);
                seq.add(readAttributes(length, false));
            } else if (undeflen && tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                break;
            } else {
                skipAttribute(UNEXPECTED_ATTRIBUTE);
            }
        }
        if (seq == null)
            attrs.putNull(seqtag, null, VR.SQ);
    }

    private void readFragments() throws IOException {
        Fragments frags = null;
        int fragtag = tag;
        VR fragvr = vr;
        while (true) {
            readHeader();
            if (tag == Tag.Item) {
                if (frags == null)
                    frags = attrs.putFragments(fragtag, null, fragvr, 2);
                byte[] value = new byte[length];
                readFully(value);
                if (attrs.isBigEndian() != bigEndian)
                    vr.toggleEndian(value);
                frags.add(value);
            } else if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                break;
            } else {
                skipAttribute(UNEXPECTED_ATTRIBUTE);
            }
        }
        if (frags == null)
            attrs.putNull(fragtag, null, fragvr);
    }

    private void skipAttribute(String message) throws IOException {
        LOG.warn(message, new Object[] {
                    TagUtils.toPrompt(tag), length, tagPos });
        skip(length);
    }

    @Override
    public boolean readValue(DicomInputStream din) throws IOException {
        if (din != this)
            throw new IllegalArgumentException("din != this");

        if (vr == VR.SQ)
            readSequence(length);
        else if (length == -1)
            readFragments();
        else
            readSimpleValue();
        return true;
    }

    private void readSimpleValue() throws IOException {
        if (length == 0) {
            attrs.putNull(tag, null, vr);
        } else {
            byte[] value = new byte[length];
            readFully(value);
            if (attrs.isBigEndian() != bigEndian)
                vr.toggleEndian(value);
            attrs.putBytes(tag, null, vr, value);
        }
    }

}
