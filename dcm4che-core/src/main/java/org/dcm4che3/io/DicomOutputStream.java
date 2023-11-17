/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che3.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.SpecificCharacterSet;
import org.dcm4che3.data.VR;
import org.dcm4che3.data.Value;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.CountingOutputStream;
import org.dcm4che3.util.TagUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomOutputStream extends FilterOutputStream {

    private static final byte[] DICM = { 'D', 'I', 'C', 'M' };

    private byte[] preamble = new byte[128];

    private boolean explicitVR;
    private boolean bigEndian;
    private CountingOutputStream countingOutputStream;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;

    private final byte[] buf = new byte[12];
    private Deflater deflater;

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

    public final DicomEncodingOptions getEncodingOptions() {
        return encOpts;
    }

    public final void setEncodingOptions(DicomEncodingOptions encOpts) {
        if (encOpts == null)
            throw new NullPointerException();
        this.encOpts = encOpts;
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
        if (!explicitVR || bigEndian || countingOutputStream != null)
            throw new IllegalStateException("explicitVR=" + explicitVR
                    + ", bigEndian=" + bigEndian
                    + ", deflated=" + (countingOutputStream != null));
        write(preamble);
        write(DICM);
        fmi.writeGroupTo(this, Tag.FileMetaInformationGroupLength);
    }

    public void writeDataset(Attributes fmi, Attributes dataset)
            throws IOException {
        if (fmi != null) {
            writeFileMetaInformation(fmi);
            switchTransferSyntax(fmi.getString(Tag.TransferSyntaxUID, null));
        }
        if (dataset.bigEndian() != bigEndian
                || encOpts.groupLength
                || !encOpts.undefSequenceLength
                || !encOpts.undefItemLength)
            dataset = new Attributes(dataset, bigEndian);
        if (encOpts.groupLength)
            dataset.calcLength(encOpts, explicitVR);
        dataset.writeTo(this);
    }

    public void switchTransferSyntax(String tsuid)  {
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                        || tsuid.equals(UID.JPIPReferencedDeflate)
                        || tsuid.equals(UID.JPIPHTJ2KReferencedDeflate)) {
                this.countingOutputStream = new CountingOutputStream(super.out);
                super.out = new DeflaterOutputStream(countingOutputStream,
                        deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true));
        }
    }

    public void writeHeader(int tag, VR vr, int len) throws IOException {
        byte[] b = buf;
        ByteUtils.tagToBytes(tag, b, 0, bigEndian);
        int headerLen;
        if (!TagUtils.isItem(tag) && explicitVR) {
            if ((len & 0xffff0000) != 0 && vr.headerLength() == 8)
                vr = VR.UN;
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
        if (val instanceof BulkData
                && super.out instanceof ObjectOutputStream) {
            writeHeader(tag, vr, BulkData.MAGIC_LEN);
            ((ObjectOutputStream) super.out).writeObject(val);
        } else {
            int length = val.getEncodedLength(encOpts, explicitVR, vr);
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
        if (countingOutputStream != null) {
            ((DeflaterOutputStream) out).finish();
            if ((countingOutputStream.getCount() & 1) != 0)
                countingOutputStream.write(0);
        }
    }

    public void close() throws IOException {
        try {
            finish();
        } catch (IOException ignored) {
        }
        if (deflater != null) {
            deflater.end();
        }
        super.close();
    }
}
