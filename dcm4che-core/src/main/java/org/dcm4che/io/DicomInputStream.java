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
    private boolean fmi;
    private long fmiEndPos = -1L;
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
        fmi = false;
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                        || tsuid.equals(UID.JPIPReferencedDeflate))
            super.in = new InflaterInputStream(super.in, new Inflater(true));
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
            fmi = true;
            bigEndian = false;
            explicitVR = true;
            return;
        }
        boolean be = false;
        int tag = ByteUtils.bytesToTagLE(b128, 0);
        VR vr = ElementDictionary.vrOf(ByteUtils.bytesToTagLE(b128, 0), null);
        if (vr == VR.UN) {
            be = true;
            tag = ByteUtils.bytesToTagBE(b128, 0);
            vr = ElementDictionary.vrOf(tag, null);
            if (vr == VR.UN)
                throw new DicomStreamException("Not a DICOM stream");
        }
        fmi = (tag >>> 16) == 2;
        bigEndian = be;
        explicitVR = ByteUtils.bytesToVR(b128, 8) == vr.code();
        reset();
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
        Attributes prevAttrs = this.attrs;
        this.attrs = new Attributes().setBigEndian(bigEndian);
        try {
            long endPos =  pos + (len & 0xffffffffL);
            boolean undeflen = len == -1;
            while (undeflen || this.pos < endPos) {
                mark(12);
                try {
                    readHeader();
                } catch (EOFException e) {
                    if (undeflen && pos == tagPos)
                        break;
                    throw e;
                }
                if (fmi && (tag >>> 16) != 2) {
                    LOG.warn(MISSING_FMI_LENGTH);
                    reset();
                    if (stopAfterFmi)
                        return attrs;
                    switchTransferSyntaxUID();
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
                if (fmi && pos == fmiEndPos) {
                    if (stopAfterFmi)
                        return attrs;
                    switchTransferSyntaxUID();
                }
            }
            return attrs;
        } finally {
            this.attrs = prevAttrs;
        }
    }

    private void readSequence(int len) throws IOException {
        Sequence seq = null;
        long endPos =  pos + (len & 0xffffffffL);
        boolean undeflen = len == -1;
        while (undeflen || pos < endPos) {
            readHeader();
            if (tag == Tag.Item) {
                if (seq == null)
                    seq = attrs.putSequence(tag, null, 1);
                seq.addItem(readAttributes(length, false));
            } else if (undeflen && tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                break;
            } else {
                skipAttribute(UNEXPECTED_ATTRIBUTE);
            }
        }
        if (seq == null)
            attrs.putNull(tag, null, VR.SQ);
    }

    private void readFragments() throws IOException {
        Fragments frags = null;
        while (true) {
            readHeader();
            if (tag == Tag.Item) {
                if (frags == null)
                    frags = attrs.putFragments(tag, null, vr, 2);
                byte[] value = new byte[length];
                readFully(value);
                frags.addFragment(value);
            } else if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                break;
            } else {
                skipAttribute(UNEXPECTED_ATTRIBUTE);
            }
        }
        if (frags == null)
            attrs.putNull(tag, null, vr);
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
            if ((tag & 0x0000FFFF) != 0)
                attrs.putBytes(tag, null, vr, value);
            else if (fmi) {
                fmiEndPos = pos + ByteUtils.bytesToInt(value, 0, bigEndian);
            }
        }
    }

}
