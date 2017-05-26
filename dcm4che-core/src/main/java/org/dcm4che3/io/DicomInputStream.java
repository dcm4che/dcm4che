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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.DatasetWithFMI;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.ItemPointer;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomInputStream extends FilterInputStream
    implements DicomInputHandler {

    public enum IncludeBulkData { NO, YES, URI }

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
    private static final String IMPLICIT_VR_BIG_ENDIAN =
        "Implicit VR Big Endian encoded DICOM Stream";
    private static final String DEFLATED_WITH_ZLIB_HEADER =
        "Deflated DICOM Stream with ZLIB Header";

    private static final int ZLIB_HEADER = 0x789c;
    private static final int DEF_ALLOCATE_LIMIT = 0x4000000; // 64MiB

    private int allocateLimit = DEF_ALLOCATE_LIMIT;
    private String uri;
    private String tsuid;
    private byte[] preamble;
    private Attributes fileMetaInformation;
    private boolean hasfmi;
    private boolean bigEndian;
    private boolean explicitVR;
    private IncludeBulkData includeBulkData = IncludeBulkData.YES;
    private IncludeBulkData includeFragmentBulkData;
    private long pos;
    private long fmiEndPos = -1L;
    private long tagPos;
    private long markPos;
    private int tag;
    private VR vr;
    private int length;
    private DicomInputHandler handler = this;
    private BulkDataDescriptor bulkDataDescriptor = BulkDataDescriptor.DEFAULT;
    private final byte[] buffer = new byte[12];
    private ItemPointer[] itemPointers = {};
    private boolean decodeUNWithIVRLE = true;
    private boolean addBulkDataReferences;

    private boolean catBlkFiles = true;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private ArrayList<File> blkFiles;
    private String blkURI;
    private FileOutputStream blkOut;
    private long blkOutPos;

    public DicomInputStream(InputStream in, String tsuid) throws IOException {
        super(in);
        switchTransferSyntax(tsuid);
    }

    public DicomInputStream(InputStream in) throws IOException {
        super(in.markSupported() ? in : new BufferedInputStream(in));
        guessTransferSyntax();
    }

    public DicomInputStream(File file) throws IOException {
        this(new FileInputStream(file));
        uri = file.toURI().toString();
    }

    public final String getTransferSyntax() {
        return tsuid;
    }

    /** 
     * Returns the limit of initial allocated memory for element values.
     * 
     * By default, the limit is set to 67108864 (64 MiB).
     *
     * @return Limit of initial allocated memory for value or -1 for no limit
     * @see #setAllocateLimit(int)
     */
    public final int getAllocateLimit() {
        return allocateLimit;
    }

    /**
     * Sets the limit of initial allocated memory for element values. If the
     * value length exceeds the limit, a byte array with the specified size is
     * allocated. If the array can filled with bytes read from this
     * <code>DicomInputStream</code>, the byte array is reallocated with
     * twice the previous length and filled again. That continues until
     * the twice of the previous length exceeds the actual value length. Then
     * the byte array is reallocated with actual value length and filled with
     * the remaining bytes for the value from this <code>DicomInputStream</code>.
     * 
     * The rational of the incrementing allocation of byte arrays is to avoid
     * OutOfMemoryErrors on parsing corrupted DICOM streams.
     * 
     * By default, the limit is set to 67108864 (64 MiB).
     * 
     * @param allocateLimit limit of initial allocated memory or -1 for no limit
     * 
     */
    public final void setAllocateLimit(int allocateLimit) {
        this.allocateLimit = allocateLimit;
    }

    public final String getURI() {
        return uri;
    }

    public final void setURI(String uri) {
        this.uri = uri;
    }

    public final IncludeBulkData getIncludeBulkData() {
        return includeBulkData;
    }

    public final void setIncludeBulkData(IncludeBulkData includeBulkData) {
        if (includeBulkData == null)
            throw new NullPointerException();
        this.includeBulkData = includeBulkData;
    }

    public final IncludeBulkData getIncludeFragmentBulkData() {
        return includeFragmentBulkData;
    }

    public final BulkDataDescriptor getBulkDataDescriptor() {
        return bulkDataDescriptor;
    }

    public final void setBulkDataDescriptor(BulkDataDescriptor bulkDataDescriptor) {
        this.bulkDataDescriptor = bulkDataDescriptor;
    }

    public final String getBulkDataFilePrefix() {
        return blkFilePrefix;
    }

    public final void setBulkDataFilePrefix(String blkFilePrefix) {
        this.blkFilePrefix = blkFilePrefix;
    }

    public final String getBulkDataFileSuffix() {
        return blkFileSuffix;
    }

    public final void setBulkDataFileSuffix(String blkFileSuffix) {
        this.blkFileSuffix = blkFileSuffix;
    }

    public final File getBulkDataDirectory() {
        return blkDirectory;
    }

    public final void setBulkDataDirectory(File blkDirectory) {
        this.blkDirectory = blkDirectory;
    }

    public final boolean isConcatenateBulkDataFiles() {
        return catBlkFiles;
    }

    public final void setConcatenateBulkDataFiles(boolean catBlkFiles) {
        this.catBlkFiles = catBlkFiles;
    }

    public final List<File> getBulkDataFiles() {
        if (blkFiles != null)
            return blkFiles;
        else
            return Collections.emptyList();
    }

    public final void setDicomInputHandler(DicomInputHandler handler) {
        if (handler == null)
            throw new NullPointerException("handler");
        this.handler = handler;
    }

    public boolean isDecodeUNWithIVRLE() {
        return decodeUNWithIVRLE;
    }

    public void setDecodeUNWithIVRLE(boolean decodeUNWithIVRLE) {
        this.decodeUNWithIVRLE = decodeUNWithIVRLE;
    }

    public boolean isAddBulkDataReferences() {
        return addBulkDataReferences;
    }

    public void setAddBulkDataReferences(boolean addBulkDataReferences) {
        this.addBulkDataReferences = addBulkDataReferences;
    }

    public final void setFileMetaInformationGroupLength(byte[] val) {
        fmiEndPos = pos + ByteUtils.bytesToInt(val, 0, bigEndian);
    }

    public final byte[] getPreamble() {
        return preamble;
    }

    public Attributes getFileMetaInformation() throws IOException {
        readFileMetaInformation();
        return fileMetaInformation;
    }

    public final int level() {
        return itemPointers.length;
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

    public void setPosition(long pos) {
        this.pos = pos;
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
    public void close() throws IOException {
        SafeClose.close(blkOut);
        super.close();
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

    public void skipFully(long n) throws IOException {
        StreamUtils.skipFully(this, n);
    }

    public void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte b[], int off, int len) throws IOException {
        StreamUtils.readFully(this, b, off, len);
    }

    public int readHeader() throws IOException {
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
                    return tag;
                }
                readFully(buf, 4, 4);
            } else {
                vr = VR.UN;
            }
        }
        length = ByteUtils.bytesToInt(buf, 4, bigEndian);
        return tag;
    }

    public Attributes readCommand() throws IOException {
        if (bigEndian || explicitVR)
            throw new IllegalStateException(
                    "bigEndian=" + bigEndian + ", explicitVR=" + explicitVR );
        Attributes attrs = new Attributes(9);
        readAttributes(attrs, -1, -1);
        return attrs;
    }

    /**
     * @return file meta information and complete dataset
     */
    public DatasetWithFMI readDatasetWithFMI() throws IOException {
        return readDatasetWithFMI(-1, -1);
    }

    /**
     * @param len     maximum length to read in bytes, use -1 for no limit
     * @param stopTag stop reading at the given Tag, use -1 for no stop tag
     *
     * @return file meta information and dataset
     */
    public DatasetWithFMI readDatasetWithFMI(int len, int stopTag) throws IOException {
        Attributes dataset = readDataset(len, stopTag);
        return new DatasetWithFMI(getFileMetaInformation(), dataset);
    }

    public Attributes readDataset(int len, int stopTag) throws IOException {
        handler.startDataset(this);
        readFileMetaInformation();
        Attributes attrs = new Attributes(bigEndian, 64);
        readAttributes(attrs, len, stopTag);
        attrs.trimToSize();
        handler.endDataset(this);
        return attrs;
    }

    public Attributes readFileMetaInformation() throws IOException {
        if (!hasfmi)
            return null;  // No File Meta Information
        if (fileMetaInformation != null)
            return fileMetaInformation;  // already read

        Attributes attrs = new Attributes(bigEndian, 9);
        while (pos != fmiEndPos) {
            mark(12);
            readHeader();
            if (TagUtils.groupNumber(tag) != 2) {
                LOG.warn(MISSING_FMI_LENGTH);
                reset();
                break;
            }
             if (vr != null) {
                if (vr == VR.UN)
                    vr = ElementDictionary.getStandardElementDictionary()
                            .vrOf(tag);
                handler.readValue(this, attrs);
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
        fileMetaInformation = attrs;

        String tsuid = attrs.getString(Tag.TransferSyntaxUID, null);
        if (tsuid == null) {
            LOG.warn(MISSING_TRANSFER_SYNTAX);
            tsuid = UID.ExplicitVRLittleEndian;
        }
        switchTransferSyntax(tsuid);
        return attrs;
    }

    public void readAttributes(Attributes attrs, int len, int stopTag)
            throws IOException {
        ItemPointer[] prevItemPointers = itemPointers;
        itemPointers = attrs.itemPointers();
        boolean undeflen = len == -1;
        boolean hasStopTag = stopTag != -1;
        long endPos =  pos + (len & 0xffffffffL);
        while (undeflen || this.pos < endPos) {
            try {
                readHeader();
            } catch (EOFException e) {
                if (undeflen && pos == tagPos)
                    break;
                throw e;
            }
            if (hasStopTag && tag == stopTag)
                break;
            if (vr != null) {
                boolean prevBigEndian = bigEndian;
                boolean prevExplicitVR = explicitVR;
                try {
                    if (vr == VR.UN) {
                        if (decodeUNWithIVRLE) {
                            bigEndian = false;
                            explicitVR = false;
                        }
                        vr = ElementDictionary.vrOf(tag,
                                attrs.getPrivateCreator(tag));
                        if (vr == VR.UN && length == -1)
                            vr = VR.SQ; // assumes UN with undefined length are SQ,
                                        // will fail on UN fragments!
                    }
                    handler.readValue(this, attrs);
                } finally {
                    bigEndian = prevBigEndian;
                    explicitVR = prevExplicitVR;
                }
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
        itemPointers = prevItemPointers;
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs)
            throws IOException {
        checkIsThis(dis);
        if (includeBulkData == IncludeBulkData.NO && length != -1 && isBulkData(attrs)) {
            skipFully(length);
        } else if (length == 0) {
            attrs.setNull(tag, vr);
        } else if (vr == VR.SQ) {
            readSequence(length, attrs, tag);
        } else if (length == -1) {
            readFragments(attrs, tag, vr);
        } else if (length == BulkData.MAGIC_LEN
                && super.in instanceof ObjectInputStream) {
            attrs.setValue(tag, vr, BulkData.deserializeFrom(
                    (ObjectInputStream) super.in));
        } else if (includeBulkData == IncludeBulkData.URI && isBulkData(attrs)) {
            BulkData bulkData = createBulkData();
            attrs.setValue(tag, vr, bulkData);
            if (addBulkDataReferences) {
                attrs.getRoot().addBulkDataReference(
                        attrs.privateCreatorOf(tag),
                        tag,
                        vr,
                        bulkData,
                        attrs.itemPointers());
            }
        } else {
            byte[] b = readValue();
            if (!TagUtils.isGroupLength(tag)) {
                if (bigEndian != attrs.bigEndian())
                    vr.toggleEndian(b, false);
                attrs.setBytes(tag, vr, b);
            } else if (tag == Tag.FileMetaInformationGroupLength)
                setFileMetaInformationGroupLength(b);
        }
    }

    public BulkData createBulkData() throws IOException {
            BulkData bulkData;
        if (uri != null && !(super.in instanceof InflaterInputStream)) {
            bulkData = new BulkData(uri, pos, length, bigEndian);
            skipFully(length);
        } else {
            if (blkOut == null) {
                File blkfile = File.createTempFile(blkFilePrefix,
                        blkFileSuffix, blkDirectory);
                if (blkFiles == null)
                    blkFiles = new ArrayList<File>();
                blkFiles.add(blkfile);
                blkURI = blkfile.toURI().toString();
                blkOut = new FileOutputStream(blkfile);
                blkOutPos = 0L;
            }
            try {
                StreamUtils.copy(this, blkOut, length);
            } finally {
                if (!catBlkFiles) {
                    SafeClose.close(blkOut);
                    blkOut = null;
                }
            }
            bulkData = new BulkData(blkURI, blkOutPos, length, bigEndian);
            blkOutPos += length;
        }
        return bulkData;
    }

    public boolean isBulkData(Attributes attrs) {
        return bulkDataDescriptor.isBulkData(
                attrs.getPrivateCreator(tag), tag, vr, length, itemPointers);
    }

    @Override
    public void readValue(DicomInputStream dis, Sequence seq)
            throws IOException {
        checkIsThis(dis);
        if (length == 0) {
            seq.add(new Attributes(seq.getParent().bigEndian(), 0));
            return;
        }
        Attributes attrs = new Attributes(seq.getParent().bigEndian());
        seq.add(attrs);
        readAttributes(attrs, length, Tag.ItemDelimitationItem);
        attrs.trimToSize();
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        checkIsThis(dis);
        if (includeFragmentBulkData == IncludeBulkData.NO) {
            skipFully(length);
        } else if (length == 0) {
            frags.add(ByteUtils.EMPTY_BYTES);
        } else if (length == BulkData.MAGIC_LEN
                && super.in instanceof ObjectInputStream) {
            frags.add(BulkData.deserializeFrom((ObjectInputStream) super.in));
        } else if (includeFragmentBulkData == IncludeBulkData.URI) {
            frags.add(createBulkData());
        } else {
            byte[] b = readValue();
            if (bigEndian != frags.bigEndian())
                frags.vr().toggleEndian(b, false);
            frags.add(b);
        }
    }

    @Override
    public void startDataset(DicomInputStream dis) {
    }

    @Override
    public void endDataset(DicomInputStream dis) {
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

    private void readSequence(int len, Attributes attrs, int sqtag)
            throws IOException {
        if (len == 0) {
            attrs.setNull(sqtag, VR.SQ);
            return;
        }
        Sequence seq = attrs.newSequence(sqtag, 10);
        String privateCreator = attrs.getPrivateCreator(sqtag);
        boolean undefLen = len == -1;
        long endPos = pos + (len & 0xffffffffL);
        for (int i = 0; undefLen || pos < endPos; ++i) {
            readHeader();
            if (tag == Tag.Item) {
                handler.readValue(this, seq);
            } else if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                break;
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
        if (seq.isEmpty())
            attrs.setNull(sqtag, VR.SQ);
        else
            seq.trimToSize();
    }

    public Attributes readItem() throws IOException {
        readHeader();
        if (tag != Tag.Item)
            throw new IOException("Unexpected attribute "
                    + TagUtils.toString(tag) + " #" + length + " @ " + pos);
        Attributes attrs = new Attributes(bigEndian);
        attrs.setItemPosition(tagPos);
        readAttributes(attrs, length, Tag.ItemDelimitationItem);
        attrs.trimToSize();
        return attrs;
    }

    private void readFragments(Attributes attrs, int fragsTag, VR vr)
            throws IOException {
        includeFragmentBulkData =
                includeBulkData == IncludeBulkData.YES || isBulkData(attrs)
                        ? includeBulkData 
                        : IncludeBulkData.YES;

        String privateCreator = attrs.getPrivateCreator(fragsTag);
        Fragments frags = new Fragments(privateCreator, fragsTag, vr, attrs.bigEndian(), 10);
        for (int i = 0; true; ++i) {
            readHeader();
            if (tag == Tag.Item) {
                handler.readValue(this, frags);
            } else if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH);
                break;
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE);
        }
        if (frags.isEmpty())
            attrs.setNull(fragsTag, vr);
        else {
            frags.trimToSize();
            attrs.setValue(fragsTag, vr, frags);
        }
    }

    public byte[] readValue() throws IOException {
        int valLen = length;
        try {
            if (valLen < 0)
                throw new EOFException(); // assume InputStream length < 2 GiB
            int allocLen = allocateLimit >= 0
                    ? Math.min(valLen, allocateLimit)
                    : valLen;
            byte[] value = new byte[allocLen];
            readFully(value, 0, allocLen);
            while (allocLen < valLen) {
                int newLength = Math.min(valLen, allocLen << 1);
                value = Arrays.copyOf(value, newLength);
                readFully(value, allocLen, newLength - allocLen);
                allocLen = newLength;
            }
            return value;
        } catch (IOException e) {
            LOG.warn("IOException during read of {} #{} @ {}",
                    TagUtils.toString(tag), length, tagPos, e);
            throw e;
        }
    }

    private void switchTransferSyntax(String tsuid) throws IOException {
        this.tsuid = tsuid;
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndianRetired);
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
        byte[] b132 = new byte[132];
        mark(132);
        int rlen = StreamUtils.readAvailable(this, b132, 0, 132);
        if (rlen == 132) {
            if (b132[128] == 'D' && b132[129] == 'I' && b132[130] == 'C' && b132[131] == 'M') {
                preamble = new byte[128];
                System.arraycopy(b132, 0, preamble, 0, 128);
                if (!markSupported()) {
                    hasfmi = true;
                    tsuid = UID.ExplicitVRLittleEndian;
                    bigEndian = false;
                    explicitVR = true;
                    return;
                }
                mark(132);
                rlen = StreamUtils.readAvailable(this, b132, 0, 132);
            }
        }
        if (rlen < 8
                || !guessTransferSyntax(b132, rlen, false)
                && !guessTransferSyntax(b132, rlen, true))
            throw new DicomStreamException(NOT_A_DICOM_STREAM);
        reset();
        hasfmi = TagUtils.isFileMetaInformation(
                ByteUtils.bytesToTag(b132, 0, bigEndian));
    }

    private boolean guessTransferSyntax(byte[] b128, int rlen, boolean bigEndian)
            throws DicomStreamException {
        int tag1 = ByteUtils.bytesToTag(b128, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN)
            return false;
        if (ByteUtils.bytesToVR(b128, 4) == vr.code()) {
            this.tsuid = bigEndian ? UID.ExplicitVRBigEndianRetired 
                                   : UID.ExplicitVRLittleEndian;
            this.bigEndian = bigEndian;
            this.explicitVR = true;
            return true;
        }
        int len = ByteUtils.bytesToInt(b128, 4, bigEndian);
        if (len < 0 || 8 + len > rlen)
            return false;

        if (bigEndian)
            throw new DicomStreamException(IMPLICIT_VR_BIG_ENDIAN);

        this.tsuid = UID.ImplicitVRLittleEndian;
        this.bigEndian = false;
        this.explicitVR = false;
        return true;
    }
}
