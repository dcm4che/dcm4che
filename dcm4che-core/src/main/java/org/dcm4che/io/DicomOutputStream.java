package org.dcm4che.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.EncodeOptions;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.util.ByteUtils;
import org.dcm4che.util.StreamUtils;
import org.dcm4che.util.TagUtils;

public class DicomOutputStream extends FilterOutputStream {

    private static final byte[] DICM = { 'D', 'I', 'C', 'M' };

    private byte[] preamble = new byte[128];

    private EncodeOptions encOpts = EncodeOptions.DEFAULTS;

    private boolean explicitVR;

    private boolean bigEndian;

    private final byte[] buf = new byte[12];

    public DicomOutputStream(OutputStream out, boolean explicitVR,
            boolean bigEndian) throws IOException {
        super(out);
        this.explicitVR = explicitVR;
        this.bigEndian = bigEndian;
    }

    public DicomOutputStream(OutputStream out, String tsuid)
            throws IOException {
        super(out);
        switchTransferSyntax(tsuid);
    }

    public DicomOutputStream(File file) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(file)), true, false);
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

    public void writeCommand(Attributes cmd) throws IOException {
        if (explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian);
        writeGroupWithGroupLength(Tag.CommandField, cmd);
    }

    public void writeFileMetaInformation(Attributes fmi) throws IOException {
        if (!explicitVR || bigEndian)
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian);
        String tsuid = fmi.getString(Tag.TransferSyntaxUID, null, 0, null);
        write(preamble);
        write(DICM);
        writeGroupWithGroupLength(Tag.FileMetaInformationGroupLength, fmi);
        switchTransferSyntax(tsuid);
    }

    private void writeGroupWithGroupLength(int grlentag, Attributes attrs)
            throws IOException {
        int grlen = attrs.calcLength(explicitVR, EncodeOptions.DEFAULTS);
        writeGroupLength(grlentag, grlen);
        attrs.writeTo(this, EncodeOptions.DEFAULTS);
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
        dataset.writeTo(this, encOpts);
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

    public void writeAttribute(int tag, VR vr, BulkDataLocator val)
            throws IOException {
        if (val == null) {
            writeHeader(tag, vr, 0);
            return;
        }
        int swapBytes = vr.numEndianBytes();
        int padlen = val.length & 1;
        writeHeader(tag, vr, val.length + padlen);
        InputStream in = val.openStream();
        try {
            StreamUtils.skipFully(in, val.position);
            if (swapBytes != 1 
                    && (val.transferSyntax.equals(UID.ExplicitVRBigEndian)
                            ? !bigEndian : bigEndian))
                StreamUtils.copy(in, this, val.length, swapBytes);
            else
                StreamUtils.copy(in, this, val.length);
        } finally {
            in.close();
        }
        if (padlen > 0)
            out.write(vr.paddingByte());
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

    public void writeSequence(int tag, Sequence sq, EncodeOptions encOpts)
            throws IOException {
        if (sq == null || sq.isEmpty()) {
            if (encOpts.isUndefEmptySequenceLength()) {
                writeHeader(tag, VR.SQ, -1);
                writeHeader(Tag.SequenceDelimitationItem, null, 0);
            } else {
                writeHeader(tag, VR.SQ, 0);
            }
        } else if (encOpts.isUndefSequenceLength()) {
            writeHeader(tag, VR.SQ, -1);
            for (Attributes item : sq)
                writeItem(item, encOpts);
            writeHeader(Tag.SequenceDelimitationItem, null, 0);
        }  else {
            writeHeader(tag, VR.SQ, sq.getLength());
            for (Attributes item : sq)
                writeItem(item, encOpts);
        }
    }

    private void writeItem(Attributes item, EncodeOptions encOpts)
            throws IOException {
        if (item.isEmpty()) {
            if (encOpts.isUndefEmptyItemLength()) {
                writeHeader(Tag.Item, null, -1);
                writeHeader(Tag.ItemDelimitationItem, null, 0);
            } else {
                writeHeader(Tag.Item, null, 0);
            }
        } else if (encOpts.isUndefItemLength()) {
            writeHeader(Tag.Item, null, -1);
            item.writeTo(this, encOpts);
            writeHeader(Tag.ItemDelimitationItem, null, 0);
        }  else {
            writeHeader(Tag.Item, null, item.getLength());
            item.writeTo(this, encOpts);
        }
    }

    public void writeFragments(int tag, Fragments frags)
            throws IOException {
        writeHeader(tag, frags.vr(), -1);
        for (Object o : frags)
            if (o instanceof BulkDataLocator)
                writeAttribute(Tag.Item, frags.vr(), (BulkDataLocator) o);
            else
                writeAttribute(Tag.Item, frags.vr(), (byte[]) o);
        writeHeader(Tag.SequenceDelimitationItem, null, 0);
    }
}
