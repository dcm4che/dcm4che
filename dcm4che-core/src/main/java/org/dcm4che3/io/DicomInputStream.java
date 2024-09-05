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

import org.dcm4che3.data.*;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.LimitedInputStream;
import org.dcm4che3.util.SafeClose;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomInputStream extends FilterInputStream
    implements DicomInputHandler, BulkDataCreator {

    public enum IncludeBulkData { NO, YES, URI }

    private static final Logger LOG = 
        LoggerFactory.getLogger(DicomInputStream.class);

    private static final String UNEXPECTED_NON_ZERO_ITEM_LENGTH =
        "Unexpected item value of {} #{} @ {} during {}";
    private static final String UNEXPECTED_ATTRIBUTE =
        "Unexpected attribute {} #{} @ {} during {}";
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
    private static final String SEQUENCE_EXCEED_ENCODED_LENGTH =
        "Actual length of Sequence %s exceeds encoded length: %d";
    private static final String TREAT_SQ_AS_UN =
        "Actual length of Sequence {} exceeds encoded length: {} - treat as UN";
    private static final int TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH = 1024;

    /* VisibleForTesting */ static final String VALUE_TOO_LARGE =
      "tag value too large, must be less than 2Gib";

    private static final int ZLIB_HEADER = 0x789c;
    private static final int DEF_ALLOCATE_LIMIT = 0x4000000; // 64MiB

    private static final int DEFAULT_PREAMBLE_LENGTH = 128;
    private static final int UNDEFINED_LENGTH = -1;

    // Length of the buffer used for readFully(short[], int, int)
    private static final int BYTE_BUF_LENGTH = 8192;

    private byte[] byteBuf;
    private int allocateLimit = DEF_ALLOCATE_LIMIT;
    private String uri;
    private String tsuid;
    private byte[] preamble;
    private Attributes fileMetaInformation;
    private boolean hasfmi;
    private boolean bigEndian;
    private boolean explicitVR;
    private IncludeBulkData includeBulkData = IncludeBulkData.YES;
    private long pos;
    private long fmiEndPos = -1L;
    private long tagPos;
    private long markPos;
    private int tag;
    private VR vr;
    private int encodedVR;
    private long length;
    private DicomInputHandler handler = this;
    private BulkDataCreator bulkDataCreator = this;
    private BulkDataDescriptor bulkDataDescriptor = BulkDataDescriptor.DEFAULT;
    private final byte[] buffer = new byte[12];
    private List<ItemPointer> itemPointers = new ArrayList<ItemPointer>(4);
    private boolean excludeBulkData;
    private boolean includeBulkDataURI;

    private boolean catBlkFiles = true;
    private String blkFilePrefix = "blk";
    private String blkFileSuffix;
    private File blkDirectory;
    private ArrayList<File> blkFiles;
    private String blkURI;
    private FileOutputStream blkOut;
    private long blkOutPos;
    private Inflater inflater;

    public DicomInputStream(InputStream in, String tsuid) throws IOException {
        super(in);
        switchTransferSyntax(tsuid);
    }

    public DicomInputStream(InputStream in) throws IOException {
        this(in, DEFAULT_PREAMBLE_LENGTH);
    }

    public DicomInputStream(InputStream in, int preambleLength) throws IOException {
        super(ensureMarkSupported(in));
        guessTransferSyntax(preambleLength);
    }

    public DicomInputStream(File file) throws IOException {
        super(new BufferedInputStream(new FileInputStream(file)));
        try {
            guessTransferSyntax(DEFAULT_PREAMBLE_LENGTH);
        } catch (IOException e) {
            SafeClose.close(in);
            throw e;
        }
        uri = file.toURI().toString();
    }

    public static void parseUNSequence(byte[] buf, Attributes attrs, int sqtag) throws IOException {
        DicomInputStream dis = new DicomInputStream(new ByteArrayInputStream(buf),
                attrs.bigEndian() ? UID.ExplicitVRBigEndian : UID.ExplicitVRLittleEndian);
        dis.encodedVR = 0x554e;
        dis.readSequence(buf.length, attrs, sqtag);
    }

    /**
     * Create a new DicomInputStream for the given input stream, Transfer Syntax UID and read limit.
     * It ensures to never read more than the limit from the stream by wrapping it with a {@link LimitedInputStream}.
     *
     * The limit also helps to avoid OutOfMemory errors on parsing corrupt DICOM streams without the need to create
     * temporary arrays when allocating large tag values. (See also {@link #setAllocateLimit}.)
     *
     * @param in input stream to read data from
     * @param tsuid Transfer Syntax UID
     * @param limit limit in bytes
     * @return new DicomInputStream
     * @throws IOException if there is a problem reading from the given stream
     */
    public static DicomInputStream createWithLimit(InputStream in, String tsuid, long limit) throws IOException {
        return new DicomInputStream(limited(ensureMarkSupported(in), limit), tsuid);
    }

    /**
     * Create a new DicomInputStream for the given input stream and read limit.
     * It ensures to never read more than the limit from the stream by wrapping it with a {@link LimitedInputStream}.
     *
     * The limit also helps to avoid OutOfMemory errors on parsing corrupt DICOM streams without the need to create
     * temporary arrays when allocating large tag values. (See also {@link #setAllocateLimit}.)
     *
     * @param in input stream to read data from
     * @param limit limit in bytes
     * @return new DicomInputStream
     * @throws IOException if there is a problem reading from the given stream
     */
    public static DicomInputStream createWithLimit(InputStream in, long limit) throws IOException {
        return new DicomInputStream(limited(ensureMarkSupported(in), limit));
    }

    /**
     * Create a new DicomInputStream for the given file.
     *
     * A limit will be set by reading the length of the file (see also #createWithLimit).
     *
     * @param file file to read
     * @return new DicomInputStream
     * @throws IOException if there is a problem reading from the given file
     */
    public static DicomInputStream createWithLimitFromFileLength(File file) throws IOException {
        long fileLength = file.length();
        // Some operating systems may return 0 length for pathnames denoting system-dependent entities such as devices or pipes
        if(fileLength > 0) {
            InputStream in = limited(new BufferedInputStream(new FileInputStream(file)), fileLength);
            DicomInputStream dicomInputStream;
            try {
                dicomInputStream = new DicomInputStream(in);
            } catch (IOException e) {
                SafeClose.close(in);
                throw e;
            }
            dicomInputStream.setURI(file.toURI().toString());
            return dicomInputStream;
        } else {
            return new DicomInputStream(file);
        }
    }

    private static InputStream ensureMarkSupported(InputStream in) {
        return in.markSupported() ? in : new BufferedInputStream(in);
    }

    private static LimitedInputStream limited(InputStream in, long limit) {
        return new LimitedInputStream(in, limit, true);
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
     * Note: If a limit is given using {@link #createWithLimit} or
     * {@link #createWithLimitFromFileLength} or by supplying a {@link LimitedInputStream},
     * then this allocateLimit will be ignored (except for deflated data) and no
     * temporary arrays need to be created.
     *
     * @param allocateLimit limit of initial allocated memory or -1 for no limit
     */
    public final void setAllocateLimit(int allocateLimit) {
        if (!(allocateLimit > 0 || allocateLimit == -1))
            throw new IllegalArgumentException("allocateLimit must be a positive number or -1");

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

    /**
     * Set {@code DicomInputHandler} to parse Datasets without accumulating read attributes in {@code Attributes}.
     */
    public final void setSkipAllDicomInputHandler() {
        this.handler = new DicomInputHandler() {
            @Override
            public void readValue(DicomInputStream dis, Attributes attrs) throws IOException {
                if (dis.length() == -1) {
                    dis.skipSequence();
                } else {
                    long n = dis.unsignedLength();
                    StreamUtils.skipFully(dis, n);
                }
            }

            @Override
            public void readValue(DicomInputStream dis, Sequence seq) throws IOException {
                dis.readValue(dis, seq);
            }

            @Override
            public void readValue(DicomInputStream dis, Fragments frags) throws IOException {
                long n = dis.unsignedLength();
                StreamUtils.skipFully(dis, n);
            }

            @Override
            public void startDataset(DicomInputStream dis) throws IOException {
            }

            @Override
            public void endDataset(DicomInputStream dis) throws IOException {
            }
        };
    }

    public void setBulkDataCreator(BulkDataCreator bulkDataCreator) {
        if (bulkDataCreator == null)
            throw new NullPointerException("bulkDataCreator");
        this.bulkDataCreator = bulkDataCreator;
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
        return itemPointers.size();
    }

    public final int tag() {
        return tag;
    }

    public final VR vr() {
        return vr;
    }

    /**
     * Returns value length of last parsed data element header. May be negative for value length >= 2^31.
     * -1 indicates an Undefined Length.
     * @return value length of last parsed data element header.
     */
    public final int length() {
        return (int) length;
    }

    /**
     * Returns value length of last parsed data element header.
     * -1 indicates an Undefined Length.
     * @return value length of last parsed data element header.
     */
    public long unsignedLength() {
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

    public boolean isExcludeBulkData() {
        return excludeBulkData;
    }

    public boolean isIncludeBulkDataURI() {
        return includeBulkDataURI;
    }

    public static String toAttributePath(List<ItemPointer> itemPointers, int tag) {
        StringBuilder sb = new StringBuilder();
        for (ItemPointer itemPointer : itemPointers) {
            sb.append('/').append(TagUtils.toHexString(itemPointer.sequenceTag))
                    .append('/').append(itemPointer.itemIndex);
        }
        sb.append('/').append(TagUtils.toHexString(tag));
        return sb.toString();
    }

    public String getAttributePath() {
        return toAttributePath(itemPointers, tag);
    }

    @Override
    public void close() throws IOException {
        SafeClose.close(blkOut);
        if (inflater != null) {
            inflater.end();
        }
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

    public void readFully(short[] s, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > s.length || off + len < 0) {
            throw new IndexOutOfBoundsException
                    ("off < 0 || len < 0 || off + len > s.length!");
        }

        if (byteBuf == null)
            byteBuf = new byte[BYTE_BUF_LENGTH];

        while (len > 0) {
            int nelts = Math.min(len, byteBuf.length/2);
            readFully(byteBuf, 0, nelts*2);
            ByteUtils.bytesToShorts(byteBuf, s, off, nelts, bigEndian);
            off += nelts;
            len -= nelts;
        }
    }

    private DicomStreamException tagValueTooLargeException()  {
        return new DicomStreamException(
            String.format("0x%s %s", TagUtils.toHexString(tag), VALUE_TOO_LARGE));
      }

    public void readHeader() throws IOException {
        readHeader(dis -> false);
    }

    public void readHeader(Predicate<DicomInputStream> stopPredicate) throws IOException {
        byte[] buf = buffer;
        tagPos = pos; 
        readFully(buf, 0, 8);
        encodedVR = 0;
        switch(tag = ByteUtils.bytesToTag(buf, 0, bigEndian)) {
        case Tag.Item:
        case Tag.ItemDelimitationItem:
        case Tag.SequenceDelimitationItem:
           vr = null;
           break;
        default:
            if (explicitVR) {
                vr = VR.valueOf(encodedVR = ByteUtils.bytesToVR(buf, 4));
                if (vr == null) {
                    vr = ElementDictionary.getStandardElementDictionary().vrOf(tag);
                    if (!stopPredicate.test(this))
                        LOG.warn("Unrecognized VR code: {}H for {} - treat as {}",
                                TagUtils.shortToHexString(encodedVR), TagUtils.toString(tag), vr);
                }
                if (vr.headerLength() == 8) {
                    // This length can't overflow since length field is only 16 bits in this case.
                    length = ByteUtils.bytesToUShort(buf, 6, bigEndian);
                    return;
                }
                readFully(buf, 4, 4);
            } else {
                vr = VR.UN;
            }
        }
        length = toLongOrUndefined(ByteUtils.bytesToInt(buf, 4, bigEndian));
    }

    static long toLongOrUndefined(int length) {
        return length == UNDEFINED_LENGTH ? length : length & 0xffffffffL ;
    }

    public boolean readItemHeader() throws IOException {
        String methodName = "readItemHeader()";
        for(;;) {
            readHeader();
            if (tag == Tag.Item)
                return true;
            if (tag == Tag.SequenceDelimitationItem) {
                if (length != 0)
                    skipAttribute(UNEXPECTED_NON_ZERO_ITEM_LENGTH, methodName);
                return false;
            }
            skipAttribute(UNEXPECTED_ATTRIBUTE, methodName);
        }
    }

    public Attributes readCommand() throws IOException {
        if (bigEndian || explicitVR)
            throw new IllegalStateException(
                    "bigEndian=" + bigEndian + ", explicitVR=" + explicitVR );
        Attributes attrs = new Attributes(9);
        readAllAttributes(attrs);
        return attrs;
    }

    public void readAllAttributes(Attributes attrs) throws IOException {
        readAttributes(attrs, UNDEFINED_LENGTH, o -> false);
    }

    public Attributes readDataset() throws IOException {
        return readDataset(o -> false);
    }

    public Attributes readDatasetUntilPixelData() throws IOException {
        return readDataset(o -> o.tag == Tag.PixelData);
    }

    /**
     * @deprecated Use one of the other {@link #readDataset()} methods instead. If you want to
     * specify a length limit, you may supply a {@link LimitedInputStream} or use
     * {@link #createWithLimit} or {@link #createWithLimitFromFileLength}.
     */
    @Deprecated
    public Attributes readDataset(int len, int stopTag) throws IOException {
        return readDataset(len, tagEqualOrGreater(stopTag));
    }

    public Attributes readDataset(int stopTag) throws IOException {
        return readDataset(tagEqualOrGreater(stopTag));
    }

    public Attributes readDataset(Predicate<DicomInputStream> stopPredicate) throws IOException {
        return readDataset(UNDEFINED_LENGTH, stopPredicate);
    }

    /**
     * @deprecated Use one of the other {@link #readDataset()} methods instead. If you want to
     * specify a length limit, you may supply a {@link LimitedInputStream} or use
     * {@link #createWithLimit} or {@link #createWithLimitFromFileLength}.
     */
    @Deprecated
    public Attributes readDataset(long len, Predicate<DicomInputStream> stopPredicate) throws IOException {
        handler.startDataset(this);
        readFileMetaInformation();
        Attributes attrs = new Attributes(bigEndian, 64);
        readAttributes(attrs, len, stopPredicate);
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
                skipAttribute(UNEXPECTED_ATTRIBUTE, "readFileMetaInformation()");
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

    public void readAttributes(Attributes attrs, long len, int stopTag) throws IOException {
        readAttributes(attrs, len, tagEqualOrGreater(stopTag));
    }

    private static Predicate<DicomInputStream> tagEqualOrGreater(int stopTag) {
        return stopTag != -1 ? o -> Integer.compareUnsigned(o.tag, stopTag) >= 0 : o -> false;
    }

    public void readAttributes(Attributes attrs, long len, Predicate<DicomInputStream> stopPredicate)
            throws IOException {
        boolean undeflen = len == UNDEFINED_LENGTH;
        long endPos =  pos + (len & 0xffffffffL);
        while (undeflen || this.pos < endPos) {
            try {
                readHeader(stopPredicate);
            } catch (EOFException e) {
                if (undeflen && pos == tagPos)
                    break;
                throw e;
            }
            if (stopPredicate.test(this))
                break;
            if (vr != null) {
                if (vr == VR.UN) {
                    switch (tag) {
                        case Tag.SmallestValidPixelValue:
                        case Tag.LargestValidPixelValue:
                        case Tag.SmallestImagePixelValue:
                        case Tag.LargestImagePixelValue:
                        case Tag.SmallestPixelValueInSeries:
                        case Tag.LargestPixelValueInSeries:
                        case Tag.SmallestImagePixelValueInPlane:
                        case Tag.LargestImagePixelValueInPlane:
                        case Tag.PixelPaddingValue:
                        case Tag.PixelPaddingRangeLimit:
                        case Tag.GrayLookupTableDescriptor:
                        case Tag.RedPaletteColorLookupTableDescriptor:
                        case Tag.GreenPaletteColorLookupTableDescriptor:
                        case Tag.BluePaletteColorLookupTableDescriptor:
                        case Tag.LargeRedPaletteColorLookupTableDescriptor:
                        case Tag.LargeGreenPaletteColorLookupTableDescriptor:
                        case Tag.LargeBluePaletteColorLookupTableDescriptor:
                        case Tag.RealWorldValueLastValueMapped:
                        case Tag.RealWorldValueFirstValueMapped:
                        case Tag.HistogramFirstBinValue:
                        case Tag.HistogramLastBinValue:
                            vr = attrs.getRoot().getInt(Tag.PixelRepresentation, 0) == 0 ? VR.US : VR.SS;
                            break;
                        case Tag.PurposeOfReferenceCodeSequence:
                            vr = probeObservationClass() ? VR.CS : VR.SQ;
                            break;
                        default:
                            vr = ElementDictionary.vrOf(tag, attrs.getPrivateCreator(tag));
                            if (vr == VR.UN && length == UNDEFINED_LENGTH)
                                vr = VR.SQ; // assumes UN with undefined length are SQ,
                            // will fail on UN fragments!

                            // check if the SQ is valid otherwise keeps it as UN
                            if (vr == VR.SQ && length != 0 && markSupported()) {
                                mark(Integer.BYTES);
                                byte[] tagBytes = new byte[Integer.BYTES];
                                readFully(tagBytes);
                                //convert and check
                                switch (ByteUtils.bytesToTag(tagBytes, 0, bigEndian)){
                                    case Tag.Item:
                                    case Tag.ItemDelimitationItem:
                                    case Tag.SequenceDelimitationItem:
                                        break;
                                    default:
                                        vr = VR.UN;
                                }
                                reset();
                            }
                    }
                }

                excludeBulkData = includeBulkData == IncludeBulkData.NO && isBulkData(attrs);
                includeBulkDataURI = length != 0 && vr != VR.SQ
                        && includeBulkData == IncludeBulkData.URI && isBulkData(attrs);
                handler.readValue(this, attrs);
            } else
                skipAttribute(UNEXPECTED_ATTRIBUTE, "readAttributes()");
        }
    }

    private boolean probeObservationClass() {
        return !itemPointers.isEmpty() && itemPointers.get(0).sequenceTag == Tag.FindingsSequenceTrial;
    }

    @Override
    public void readValue(DicomInputStream dis, Attributes attrs)
            throws IOException {
        checkIsThis(dis);
        if (excludeBulkData) {
            if (length == UNDEFINED_LENGTH) {
                skipSequence();
            } else {
                skipFully(length);
            }
        } else if (length == 0) {
            attrs.setNull(tag, vr);
        } else if (vr == VR.SQ) {
            readSequence(length, attrs, tag);
        } else if (length == UNDEFINED_LENGTH) {
            readFragments(attrs, tag, vr);
        } else if (length == BulkData.MAGIC_LEN
                && super.in instanceof ObjectInputStream) {
            attrs.setValue(tag, vr, deserializeBulkData((ObjectInputStream) super.in));
        } else if (includeBulkDataURI) {
            attrs.setValue(tag, vr, bulkDataCreator.createBulkData(this));
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

    private Object deserializeBulkData(ObjectInputStream ois) throws IOException {
        try {
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    public void skipSequence() throws IOException {
        while (readItemHeader()) skipItem();
    }

    public void skipItem() throws IOException {
        if (length == UNDEFINED_LENGTH) {
            for (;;) {
                readHeader();
                if (length == UNDEFINED_LENGTH) {
                    skipSequence();
                } else {
                    skipFully(length);
                    if (tag == Tag.ItemDelimitationItem) break;
                }
            }
        } else {
            skipFully(length);
        }
    }

    @Override
    public BulkData createBulkData(DicomInputStream dis) throws IOException {
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

    private boolean isBulkData(Attributes attrs) {
        return bulkDataDescriptor.isBulkData(itemPointers,
                attrs.getPrivateCreator(tag), tag, vr, (int) length);
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
        readItemValue(attrs, length);
        attrs.trimToSize();
    }

    @Override
    public void readValue(DicomInputStream dis, Fragments frags)
            throws IOException {
        checkIsThis(dis);
        if (excludeBulkData) {
            skipFully(length);
        } else if (length == 0) {
            frags.add(ByteUtils.EMPTY_BYTES);
        } else if (length == BulkData.MAGIC_LEN
                && super.in instanceof ObjectInputStream) {
            frags.add(deserializeBulkData((ObjectInputStream) super.in));
        } else if (includeBulkDataURI) {
            frags.add(bulkDataCreator.createBulkData(this));
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

    /**
     * @param message the message to use in the warning log message
     * @param methodName the name of the method that is skipping the attribute
     * @throws IOException potentially thrown when performing the 'skip' operation
     */
    private void skipAttribute(String message, String methodName) throws IOException {
        String tagAsString = TagUtils.toString(this.tag);
        LOG.warn(message, tagAsString, length, tagPos, methodName);
        skipFully(length);
    }

    private void readSequence(long len, Attributes attrs, int sqtag)
            throws IOException {
        if (len == 0) {
            attrs.setNull(sqtag, VR.SQ);
            return;
        }
        Sequence seq = attrs.newSequence(sqtag, 10);
        String privateCreator = attrs.getPrivateCreator(sqtag);
        boolean undefLen = len == UNDEFINED_LENGTH;
        long endPos = pos + (len & 0xffffffffL);
        boolean explicitVR0 = explicitVR;
        boolean bigEndian0 = bigEndian;
        if (encodedVR == 0x554e // UN
                && !probeExplicitVR()) {
            explicitVR = false;
            bigEndian = false;
        }
        boolean recoverSequenceExceedsEncodedLength = !undefLen && markSupported() && len < allocateLimit;
        if (recoverSequenceExceedsEncodedLength)
            mark((int) len + TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH);
        for (int i = 0; (undefLen || pos < endPos) && readItemHeader(); ++i) {
            addItemPointer(sqtag, privateCreator, i);
            handler.readValue(this, seq);
            removeItemPointer();
        }
        explicitVR = explicitVR0;
        bigEndian = bigEndian0;
        if (seq.isEmpty())
            attrs.setNull(sqtag, VR.SQ);
        else if (!undefLen && pos != endPos) {
            if (!recoverSequenceExceedsEncodedLength || (pos - endPos) > TREAT_SQ_AS_UN_MAX_EXCEED_LENGTH)
                throw new DicomStreamException(String.format(SEQUENCE_EXCEED_ENCODED_LENGTH, TagUtils.toString(sqtag), len));
            LOG.info(TREAT_SQ_AS_UN, TagUtils.toString(sqtag), len);
            reset();
            tag = sqtag;
            vr = VR.UN;
            length = len;
            handler.readValue(this, attrs);
        } else
            seq.trimToSize();
    }

    private boolean probeExplicitVR() throws IOException {
        byte[] buf = new byte[14];
        if (in.markSupported()) {
            in.mark(14);
            in.read(buf);
            in.reset();
        } else {
            if (!(in instanceof PushbackInputStream))
                in = new PushbackInputStream(in, 14);
            int len = in.read(buf);
            ((PushbackInputStream) in).unread(buf, 0, len);
        }
        return VR.valueOf(ByteUtils.bytesToVR(buf, 12)) != null;
    }

    private void addItemPointer(int sqtag, String privateCreator, int itemIndex) {
        itemPointers.add(new ItemPointer(privateCreator, sqtag, itemIndex));
    }

    private void removeItemPointer() {
        itemPointers.remove(itemPointers.size() - 1);
    }

    public Attributes readItem() throws IOException {
        readHeader();
        if (tag != Tag.Item)
            throw new IOException("Unexpected attribute "
                    + TagUtils.toString(tag) + " #" + length + " @ " + pos);
        Attributes attrs = new Attributes(bigEndian);
        attrs.setItemPosition(tagPos);
        readItemValue(attrs, length);
        attrs.trimToSize();
        return attrs;
    }

    public void readItemValue(Attributes attrs, long length) throws IOException {
        readAttributes(attrs, length, dis -> dis.tag == Tag.ItemDelimitationItem);
    }

    private void readFragments(Attributes attrs, int fragsTag, VR vr)
            throws IOException {
        Fragments frags = new Fragments(vr, attrs.bigEndian(), 10);
        String privateCreator = attrs.getPrivateCreator(fragsTag);
        for (int i = 0; readItemHeader(); ++i) {
            addItemPointer(fragsTag, privateCreator, i);
            handler.readValue(this, frags);
            removeItemPointer();
        }
        if (frags.isEmpty())
            attrs.setNull(fragsTag, vr);
        else {
            frags.trimToSize();
            attrs.setValue(fragsTag, vr, frags);
        }
    }

    public byte[] readValue() throws IOException {
        int valLen = (int) length;
        if (valLen < 0) {
            throw tagValueTooLargeException();
        }
        try {
            boolean limitedStream = in instanceof LimitedInputStream;
            if(limitedStream && valLen > ((LimitedInputStream)in).getRemaining()) {
                throw new EOFException(
                        "Length " + valLen + " for tag " + TagUtils.toString(tag) + " @ " + tagPos  +
                                " exceeds remaining " + ((LimitedInputStream)in).getRemaining() +  " (pos: " + pos + ")");
            }
            int allocLen = allocateLimit != -1 && !limitedStream
                    ? Math.min(valLen, allocateLimit)
                    : valLen;
            byte[] value = new byte[allocLen];
            readFully(value, 0, allocLen);
            while (allocLen < valLen) {
                int newLength = allocLen << 1;
                if (newLength <= 0)
                    newLength = Integer.MAX_VALUE;
                if (newLength > valLen)
                    newLength = valLen;
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
        bigEndian = tsuid.equals(UID.ExplicitVRBigEndian);
        explicitVR = !tsuid.equals(UID.ImplicitVRLittleEndian);
        if (tsuid.equals(UID.DeflatedExplicitVRLittleEndian)
                        || tsuid.equals(UID.JPIPReferencedDeflate)
                        || tsuid.equals(UID.JPIPHTJ2KReferencedDeflate)) {
            if (hasZLIBHeader()) {
                LOG.warn(DEFLATED_WITH_ZLIB_HEADER);
                super.in = new InflaterInputStream(super.in);
            } else {
                super.in = new InflaterInputStream(super.in,
                        inflater = new Inflater(true));
            }
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

    private void guessTransferSyntax(int preambleLength) throws IOException {
        byte[] b134 = new byte[preambleLength + 6];
        mark(b134.length);
        int rlen = StreamUtils.readAvailable(this, b134, 0, b134.length);
        if (rlen == b134.length) {
            if (b134[preambleLength] == 'D'
                    && b134[preambleLength + 1] == 'I'
                    && b134[preambleLength + 2] == 'C'
                    && b134[preambleLength + 3] == 'M'
                    && b134[preambleLength + 4] == 2
                    && b134[preambleLength + 5] == 0
            ) {
                preamble = new byte[preambleLength];
                System.arraycopy(b134, 0, preamble, 0, preambleLength);
                reset();
                StreamUtils.skipFully(this, preambleLength + 4);
                mark(b134.length);
                rlen = StreamUtils.readAvailable(this, b134, 0, b134.length);
            }
        }
        if (rlen < 8
                || !guessTransferSyntax(b134, rlen, false)
                && !guessTransferSyntax(b134, rlen, true))
            throw new DicomStreamException(NOT_A_DICOM_STREAM);
        reset();
        hasfmi = TagUtils.isFileMetaInformation(
                ByteUtils.bytesToTag(b134, 0, bigEndian));
    }

    private boolean guessTransferSyntax(byte[] b132, int rlen, boolean bigEndian)
            throws DicomStreamException {
        int tag1 = ByteUtils.bytesToTag(b132, 0, bigEndian);
        VR vr = ElementDictionary.vrOf(tag1, null);
        if (vr == VR.UN)
            return false;
        if (ByteUtils.bytesToVR(b132, 4) == vr.code()) {
            this.tsuid = bigEndian ? UID.ExplicitVRBigEndian
                                   : UID.ExplicitVRLittleEndian;
            this.bigEndian = bigEndian;
            this.explicitVR = true;
            return true;
        }
        int len = ByteUtils.bytesToInt(b132, 4, bigEndian);

        // check if it is a reasonable length for ImplicitVRLittleEndian:
        // non-negative and not exceeding what we have read into the buffer so
        // far (under the assumption that the first tag value will not have more
        // than 64 bytes. That is reasonable to assume, as every Composite
        // Object will contain a SOP Class UID (0008,0016), and all tags that
        // could come before that do not have VRs that allow length > 64. In
        // fact we are reading a maximum value of 132-8=124 bytes initially, so
        // we would also accept a longer length of 124 bytes for the first tag
        // value.)
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
