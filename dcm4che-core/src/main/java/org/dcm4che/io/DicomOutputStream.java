package org.dcm4che.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.EncodeOptions;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.util.ByteUtils;
import org.dcm4che.util.TagUtils;

public class DicomOutputStream extends FilterOutputStream {

    private static final byte[] DICM = { 'D', 'I', 'C', 'M' };

    private byte[] preamble = new byte[128];

    private EncodeOptions encOpts = EncodeOptions.DEFAULTS;

    private boolean explicitVR;

    private boolean bigEndian;

    private boolean includeBulkDataLocator;

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

    public final void setEncodeOptions(EncodeOptions encOpts) {
        if (encOpts == null)
            throw new NullPointerException();
        this.encOpts = encOpts;
    }

    public final EncodeOptions getEncodeOptions() {
        return encOpts;
    }

    public final boolean isExplicitVR() {
        return explicitVR;
    }

    public final boolean isBigEndian() {
        return bigEndian;
    }

    public final boolean isIncludeBulkDataLocator() {
        return includeBulkDataLocator;
    }

    public final void setIncludeBulkDataLocator(boolean includeBulkDataLocator) {
        this.includeBulkDataLocator = includeBulkDataLocator;
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
        String tsuid = fmi.getString(Tag.TransferSyntaxUID, null, 0, null);
        write(preamble);
        write(DICM);
        fmi.writeGroupTo(this, Tag.FileMetaInformationGroupLength);
        switchTransferSyntax(tsuid);
    }

    public void writeDataset(Attributes fmi, Attributes dataset)
            throws IOException {
        if (fmi != null)
            writeFileMetaInformation(fmi);
        boolean needCalcLength = encOpts.needCalcLength();
        if (needCalcLength || dataset.bigEndian() != bigEndian)
            dataset = new Attributes(bigEndian, dataset);
        if (needCalcLength)
            dataset.calcLength(explicitVR, encOpts);
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

    public void writeAttribute(int tag, VR vr, byte[] val) throws IOException {
        if (val == null) {
            writeHeader(tag, vr, 0);
            return;
        }
        int padlen = val.length & 1;
        writeHeader(tag, vr, val.length + padlen);
        out.write(val);
        if (padlen > 0)
            out.write(vr.paddingByte());
    }

    public void writeBulkDataLocator(BulkDataLocator val)
            throws IOException {
        byte[] b = buf;
        ByteUtils.intToBytesBE(val.length, b, 0);
        ByteUtils.longToBytesBE(val.offset, b, 4);
        write(b, 0, 12);
        writeASCII(val.uri);
        writeASCII(val.transferSyntax);
    }

    @SuppressWarnings("deprecation")
    private void writeASCII(String s) throws IOException {
        final int len = s.length();
        byte[] b = new byte[len];
        s.getBytes(0, len, b, 0);
        write(len << 8);
        write(len);
        write(b, 0, len);
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
}
