package org.dcm4che.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.SpecificCharacterSet;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.data.Value;
import org.dcm4che.util.ByteUtils;
import org.dcm4che.util.TagUtils;

public class DicomOutputStream extends FilterOutputStream {

    private static final byte[] DICM = { 'D', 'I', 'C', 'M' };

    private static final int BULK_DATA_LOCATOR = 0xffff;

    private byte[] preamble = new byte[128];

    private boolean explicitVR;
    private boolean bigEndian;

    private boolean groupLength = false;
    private boolean undefSeqLength = true;
    private boolean undefEmptySeqLength = false;
    private boolean undefItemLength = true;
    private boolean undefEmptyItemLength = false;

    private final byte[] buf = new byte[12];

    public DicomOutputStream(OutputStream out, String tsuid)
            throws IOException {
        super(out);
        switchTransferSyntax(tsuid);
    }

    public DicomOutputStream(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)),
                UID.ExplicitVRLittleEndian);
    }

    public final void setPreamble(byte[] preamble) {
        if (preamble.length != 128)
            throw new IllegalArgumentException(
                    "preamble.length=" + preamble.length);
        this.preamble = preamble.clone();
    }

    public final boolean isExplicitVR() {
        return explicitVR;
    }

    public final boolean isBigEndian() {
        return bigEndian;
    }

    public final boolean isEncodeGroupLength() {
        return groupLength;
    }

    public final void setEncodeGroupLength(boolean groupLength) {
        this.groupLength = groupLength;
    }

    public final boolean isUndefSequenceLength() {
        return undefSeqLength;
    }

    public final void setUndefSequenceLength(boolean undefLength) {
        this.undefSeqLength = undefLength;
        if (!undefLength)
            undefEmptySeqLength = false;
    }

    public final boolean isUndefEmptySequenceLength() {
        return undefEmptySeqLength;
    }

    public final void setUndefEmptySequenceLength(boolean undefLength) {
        this.undefEmptySeqLength = undefLength;
        if (undefLength)
            undefSeqLength = true;
    }

    public final boolean isUndefItemLength() {
        return undefItemLength;
    }

    public final void setUndefItemLength(boolean undefLength) {
        this.undefItemLength = undefLength;
        if (!undefLength)
            undefEmptyItemLength = false;
    }

    public final boolean isUndefEmptyItemLength() {
        return undefEmptyItemLength;
    }

    public final void setUndefEmptyItemLength(boolean undefLength) {
        this.undefEmptyItemLength = undefLength;
        if (undefLength)
            undefItemLength = true;
    }

    public final boolean needCalcLength() {
        return groupLength || !undefSeqLength || !undefItemLength;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void writeCommand(Attributes cmd) throws IOException {
        if (explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian);
        cmd.writeGroupTo(this, Tag.CommandGroupLength);
    }

    public void writeFileMetaInformation(Attributes fmi) throws IOException {
        if (!explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian);
        String tsuid = fmi.getString(Tag.TransferSyntaxUID, null);
        write(preamble);
        write(DICM);
        fmi.writeGroupTo(this, Tag.FileMetaInformationGroupLength);
        switchTransferSyntax(tsuid);
    }

    public void writeDataset(Attributes fmi, Attributes dataset)
            throws IOException {
        if (fmi != null)
            writeFileMetaInformation(fmi);
        boolean needCalcLength = needCalcLength();
        if (needCalcLength || dataset.bigEndian() != bigEndian)
            dataset = new Attributes(bigEndian, dataset);
        if (needCalcLength)
            dataset.calcLength(this);
        dataset.writeTo(this);
    }

    private void switchTransferSyntax(String tsuid) throws IOException {
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                        || tsuid.equals(UID.JPIPReferencedDeflate)) {
                super.out = new DeflaterOutputStream(super.out,
                        new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        }
    }

    public void writeHeader(int tag, VR vr, int len) throws IOException {
        byte[] b = buf;
        ByteUtils.tagToBytes(tag, b, 0, bigEndian);
        int headerLen;
        if (!TagUtils.isItem(tag) && explicitVR) {
            ByteUtils.shortToBytesBE(vr.code(), b, 4);
            if ((headerLen = vr.headerLength()) == 8) {
                ByteUtils.shortToBytes(len, b, 6, bigEndian);
            } else {
                b[6] = b[7] = 0;
                ByteUtils.intToBytes(len, b, 8, bigEndian);
            }
        } else {
            ByteUtils.intToBytes(len, b, 4, bigEndian);
            headerLen = 8;
        }
        out.write(b, 0, headerLen);
    }


    public void writeAttribute(int tag, VR vr, Object value,
            SpecificCharacterSet cs)  throws IOException {
        if (value instanceof Value)
            writeAttribute(tag, vr, (Value) value);
        else
            writeAttribute(tag, vr,
                    (value instanceof byte[])
                            ? (byte[]) value
                            : vr.toBytes(value, cs));
    }

    public void writeAttribute(int tag, VR vr, byte[] val) throws IOException {
        int padlen = val.length & 1;
        writeHeader(tag, vr, val.length + padlen);
        out.write(val);
        if (padlen > 0)
            out.write(vr.paddingByte());
    }

    public void writeAttribute(int tag, VR vr, Value val) throws IOException {
        if (val instanceof BulkDataLocator
                && super.out instanceof ObjectOutputStream) {
            writeHeader(tag, vr, BULK_DATA_LOCATOR);
            ((BulkDataLocator) val).serializeTo((ObjectOutputStream) super.out);
        } else {
            int length = val.getEncodedLength(this, vr);
            writeHeader(tag, vr, length);
            val.writeTo(this, vr);
            if (length == -1)
                writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }
    }

    public void writeGroupLength(int tag, int len) throws IOException {
        byte[] b = buf;
        ByteUtils.tagToBytes(tag, b, 0, bigEndian);
        if (explicitVR) {
            ByteUtils.shortToBytesBE(VR.UL.code(), b, 4);
            ByteUtils.shortToBytes(4, b, 6, bigEndian);
        } else {
            ByteUtils.intToBytes(4, b, 4, bigEndian);
        }
        ByteUtils.intToBytes(len, b, 8, bigEndian);
        out.write(b, 0, 12);
    }

    public void finish() throws IOException {
        if( out instanceof DeflaterOutputStream ) {
            ((DeflaterOutputStream) out).finish();
        }
    }

    public void close() throws IOException {
        try {
            finish();
        } catch (IOException ignored) {
        }
        super.close();
    }
}
