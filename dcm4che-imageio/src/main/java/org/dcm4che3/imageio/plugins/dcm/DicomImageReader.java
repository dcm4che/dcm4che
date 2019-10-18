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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.data.*;
import org.dcm4che3.image.LookupTable;
import org.dcm4che3.image.*;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageReaderFactory.ImageReaderParam;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.io.CloneIt;
import org.dcm4che3.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.*;
import java.io.Closeable;
import java.io.IOException;
import java.util.*;

/**
 * Image Reader implementation that reads from DICOM files.  Supports several
 * types of inputs to support various types of conditions on the reading side.
 * Provide a DicomInputStream to support single-pass streaming reading,
 * where images must be read in increasing frame number, and binary/byte
 * reading similarly.
 * Pass an ImageInputStream to support random-access reading, with initial
 * reading only up to the pixel data basic offset table, and all large objects
 * being included as binary BulkData objects.
 *
 * Support for video format, and/or raw read/streaming by using the iisOfFrame method,
 * noting that calling the length method on that input will cause a scan-forward
 * for video and last image.
 * 
 * Support for asynchronous usage by cloning the image reader.  Usage is:
 * try(DicomImageReader reader2 = reader1.cloneIt()) {
 *   // Use reader2
 * }
 * This requires implementation of CloneIt on the input object, with the new
 * input being closed on closing the reader2 instance.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @since Feb 2013
 *
 */
public class DicomImageReader extends ImageReader implements CloneIt<DicomImageReader, IOException>, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(DicomImageReader.class);

    private static final Set<String> VIDEO_TSUID = new HashSet<>();
    static {
        VIDEO_TSUID.add(UID.MPEG2);
        VIDEO_TSUID.add(UID.MPEG2MainProfileHighLevel);
        VIDEO_TSUID.add(UID.MPEG4AVCH264BDCompatibleHighProfileLevel41);
        VIDEO_TSUID.add(UID.MPEG4AVCH264HighProfileLevel41);
        VIDEO_TSUID.add(UID.MPEG4AVCH264HighProfileLevel42For2DVideo);
        VIDEO_TSUID.add(UID.MPEG4AVCH264HighProfileLevel42For3DVideo);
        VIDEO_TSUID.add(UID.MPEG4AVCH264StereoHighProfileLevel42);
        VIDEO_TSUID.add(UID.HEVCH265Main10ProfileLevel51);
        VIDEO_TSUID.add(UID.HEVCH265MainProfileLevel51);
    }

    /** Default to reading the entire file for image rendering */
    public static final DicomMetaDataFactory DEFAULT_METADATA_FACTORY = new DefaultMetaDataFactory(true);


    private final DicomMetaDataFactory metadataFactory;

    private DicomMetaData metadata;


    private ImageReader decompressor;

    private boolean rle;

    private PatchJPEGLS patchJpegLS;

    /** Number of frames that have been read from the underlying instance. */
    private Integer actualNumberOfFrames;


    public DicomImageReader(ImageReaderSpi originatingProvider) {
        this(originatingProvider, DEFAULT_METADATA_FACTORY);
    }

    public DicomImageReader(ImageReaderSpi originatingProvider, DicomMetaDataFactory metadataFactory) {
        super(originatingProvider);
        this.metadataFactory = metadataFactory;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();

        this.input = input;

        if (input instanceof DicomMetaData) {
            setMetadata((DicomMetaData) input);
        }
    }





    @Override
    public synchronized int getNumImages(boolean allowSearch) throws IOException {
        readMetadata();

        if(allowSearch && this.actualNumberOfFrames == null) {
            this.actualNumberOfFrames = this.metadata.countFrames();
        }

        return this.actualNumberOfFrames == null
                ? this.metadata.getNumberOfFrames()
                : this.actualNumberOfFrames;
    }

    @Override
    public int getWidth(int frameIndex) throws IOException {
       readMetadata();
       checkIndex(frameIndex);
       return this.metadata.getColumns();
    }

    @Override
    public int getHeight(int frameIndex) throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        return this.metadata.getRows();
    }


    @Override
    public ImageTypeSpecifier getRawImageType(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        if (decompressor == null)
            return createImageType(this.metadata.getBitsStored(), this.metadata.getDataType(), this.metadata.isBanded());

        if (rle)
            return createImageType(this.metadata.getBitsStored(), this.metadata.getDataType(), true);

        try (ImageInputStream iis = iisOfFrame( 0)) {
            decompressor.setInput(iis);
            return decompressor.getRawImageType(0);
        }
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        ImageTypeSpecifier imageType;
        if (this.metadata.getPhotometricInterpretation().isMonochrome())
            imageType = createImageType(8, DataBuffer.TYPE_BYTE, false);
        else if (decompressor == null)
            imageType = createImageType(this.metadata.getBitsStored(), this.metadata.getDataType(), this.metadata.isBanded());
        else if (rle)
            imageType = createImageType(this.metadata.getBitsStored(), this.metadata.getDataType(), true);
        else {
            try (ImageInputStream iis = iisOfFrame( 0)) {
                decompressor.setInput(iis);
                return decompressor.getImageTypes(0);
            }
        }

        return Collections.singletonList(imageType).iterator();
    }


    @Override
    public ImageReadParam getDefaultReadParam() {
        return new DicomImageReadParam();
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        readMetadata();
        return metadata;
    }

    @Override
    public IIOMetadata getImageMetadata(int frameIndex) throws IOException {
        return null;
    }

    @Override
    public boolean canReadRaster() {
        return true;
    }


    @Override
    public Raster readRaster(int frameIndex, ImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        // Need to map from Fragment to IIS.
        Raster raster;
        try (ImageInputStream iisOfFrame = iisOfFrame( frameIndex)) {
            if (decompressor != null) {
                decompressor.setInput(iisOfFrame);

                LOG.debug("Start decompressing frame # {}", (frameIndex + 1));

                PhotometricInterpretation pmi = this.metadata.getPhotometricInterpretation();
                raster = pmi.decompress() == pmi && decompressor.canReadRaster()
                        ? decompressor.readRaster(0, decompressParam(param))
                        : decompressor.read(0, decompressParam(param)).getRaster();
                if (LOG.isDebugEnabled())
                    LOG.debug("Finished decompressing frame #" + (frameIndex + 1));

            }
            else {
                WritableRaster wr = Raster.createWritableRaster(createSampleModel(this.metadata.getDataType(), this.metadata.isBanded()), null);
                DataBuffer buf = wr.getDataBuffer();

                if (buf instanceof DataBufferByte) {
                    byte[][] data = ((DataBufferByte) buf).getBankData();
                    for (byte[] bs : data) {
                        LOG.debug("Filling buffer of size {} from {} with offset {} and remaining {}",
                                bs.length,
                                iisOfFrame.toString(),
                                iisOfFrame.getStreamPosition(),
                                iisOfFrame.length() - iisOfFrame.getStreamPosition());
                        iisOfFrame.readFully(bs);
                    }

                    if (this.metadata.getPixelDataVR() == VR.OW && bigEndian()) {
                        ByteUtils.swapShorts(data);
                    }
                } else {
                    short[] data = ((DataBufferUShort) buf).getData();
                    iisOfFrame.readFully(data, 0, data.length);
                }

                raster = wr;
            }
        }


        return raster;
    }

    private ImageInputStream iisOfFrame(int frameIndex) throws IOException {
        ImageInputStream iisOfFrame = this.metadata.openPixelStream(frameIndex);
        return patchJpegLS != null
                ? new PatchJPEGLSImageInputStream(iisOfFrame, patchJpegLS)
                : iisOfFrame;
    }

    public boolean bigEndian() {
        return metadata.getAttributes().bigEndian();
    }

    private ImageReadParam decompressParam(ImageReadParam param) {
        ImageReadParam decompressParam = decompressor.getDefaultReadParam();
        ImageTypeSpecifier imageType = null;
        BufferedImage dest = null;
        if (param != null) {
            imageType = param.getDestinationType();
            dest = param.getDestination();
        }
        if (rle && imageType == null && dest == null)
            imageType = createImageType(this.metadata.getBitsStored(), this.metadata.getDataType(), true);
        decompressParam.setDestinationType(imageType);
        decompressParam.setDestination(dest);
        return decompressParam;
    }

    @Override
    public BufferedImage read(int frameIndex, ImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        WritableRaster raster;
        if (decompressor != null) {
            try (ImageInputStream iis = iisOfFrame(frameIndex)) {
                decompressor.setInput(iis);
                if (LOG.isDebugEnabled())
                    LOG.debug("Start decompressing frame #" + (frameIndex + 1));
                BufferedImage bi = decompressor.read(0, decompressParam(param));
                if (LOG.isDebugEnabled())
                    LOG.debug("Finished decompressing frame #" + (frameIndex + 1));
                if (this.metadata.getSamplesPerPixel() > 1)
                    return bi;

                raster = bi.getRaster();
            }
        } else {
            raster = (WritableRaster) readRaster(frameIndex, param);
        }

        ColorModel cm;
        if (this.metadata.getPhotometricInterpretation().isMonochrome()) {
            int[] overlayGroupOffsets = getActiveOverlayGroupOffsets(param);
            byte[][] overlayData = new byte[overlayGroupOffsets.length][];
            for (int i = 0; i < overlayGroupOffsets.length; i++) {
                overlayData[i] = extractOverlay(overlayGroupOffsets[i], raster);
            }
            cm = createColorModel(8, DataBuffer.TYPE_BYTE);
            SampleModel sm = createSampleModel(DataBuffer.TYPE_BYTE, false);
            raster = applyLUTs(raster, frameIndex, param, sm, 8);
            for (int i = 0; i < overlayGroupOffsets.length; i++) {
                applyOverlay(overlayGroupOffsets[i],
                        raster, frameIndex, param, 8, overlayData[i]);
            }
        } else {
            cm = createColorModel(this.metadata.getBitsStored(), this.metadata.getDataType());
        }
        return new BufferedImage(cm, raster , false, null);
    }

    /**
     * Read the raw bytes of a frame into a byte[].  This is a convenience method, which will result in a significant
     * amount of memory being used --- especially for uncompressed images.  Developers are encouraged to stream bytes
     * with ImageInputStreamS when possible.
     */
    public byte[] readBytes(int frameIndex) throws IOException {
        byte[] bytes;
        try(ImageInputStream iis = this.metadata.openPixelStream(frameIndex)) {
            bytes = new byte[(int)iis.length()];
            iis.readFully(bytes);
        }
        return bytes;
    }

    private byte[] extractOverlay(int gg0000, WritableRaster raster) {
        Attributes attrs = metadata.getAttributes();

        if (attrs.getInt(Tag.OverlayBitsAllocated | gg0000, 1) == 1)
            return null;

        int ovlyRows = attrs.getInt(Tag.OverlayRows | gg0000, 0);
        int ovlyColumns = attrs.getInt(Tag.OverlayColumns | gg0000, 0);
        int bitPosition = attrs.getInt(Tag.OverlayBitPosition | gg0000, 0);

        int mask = 1<<bitPosition;
        int length = ovlyRows * ovlyColumns;

        byte[] ovlyData = new byte[(((length+7)>>>3)+1)&(~1)] ;
        if (bitPosition < this.metadata.getBitsStored())
            LOG.info("Ignore embedded overlay #{} from bit #{} < bits stored: {}",
                    (gg0000 >>> 17) + 1, bitPosition, this.metadata.getBitsStored());
        else
            Overlays.extractFromPixeldata(raster, mask, ovlyData, 0, length);
        return ovlyData;
    }


    @SuppressWarnings("resource")


    private void applyOverlay(int gg0000, WritableRaster raster,
            int frameIndex, ImageReadParam param, int outBits, byte[] ovlyData) {
        Attributes ovlyAttrs = metadata.getAttributes();
        int grayscaleValue = 0xffff;
        if (param instanceof DicomImageReadParam) {
            DicomImageReadParam dParam = (DicomImageReadParam) param;
            Attributes psAttrs = dParam.getPresentationState();
            if (psAttrs != null) {
                if (psAttrs.containsValue(Tag.OverlayData | gg0000))
                    ovlyAttrs = psAttrs;
                grayscaleValue = Overlays.getRecommendedDisplayGrayscaleValue(
                        psAttrs, gg0000);
            } else
                grayscaleValue = dParam.getOverlayGrayscaleValue();
        }
        Overlays.applyOverlay(ovlyData != null ? 0 : frameIndex, raster,
                ovlyAttrs, gg0000, grayscaleValue >>> (16-outBits), ovlyData);
    }

    private int[] getActiveOverlayGroupOffsets(ImageReadParam param) {
        if (param instanceof DicomImageReadParam) {
            DicomImageReadParam dParam = (DicomImageReadParam) param;
            Attributes psAttrs = dParam.getPresentationState();
            if (psAttrs != null)
                return Overlays.getActiveOverlayGroupOffsets(psAttrs);
            else
                return Overlays.getActiveOverlayGroupOffsets(
                        metadata.getAttributes(),
                        dParam.getOverlayActivationMask());
        }
        return Overlays.getActiveOverlayGroupOffsets(
                metadata.getAttributes(),
                0xffff);
    }

    private WritableRaster applyLUTs(WritableRaster raster,
            int frameIndex, ImageReadParam param, SampleModel sm, int outBits) {
         WritableRaster destRaster =
                sm.getDataType() == raster.getSampleModel().getDataType()
                        ? raster
                        : Raster.createWritableRaster(sm, null);
        Attributes imgAttrs = metadata.getAttributes();
        StoredValue sv = StoredValue.valueOf(imgAttrs);
        LookupTableFactory lutParam = new LookupTableFactory(sv);
        DicomImageReadParam dParam = param instanceof DicomImageReadParam
                ? (DicomImageReadParam) param
                : new DicomImageReadParam();
        Attributes psAttrs = dParam.getPresentationState();
        if (psAttrs != null) {
            lutParam.setModalityLUT(psAttrs);
            lutParam.setVOI(
                    selectVOILUT(psAttrs,
                            imgAttrs.getString(Tag.SOPInstanceUID),
                            frameIndex+1),
                    0, 0, false);
            lutParam.setPresentationLUT(psAttrs);
        } else {
            Attributes sharedFctGroups = imgAttrs.getNestedDataset(
                    Tag.SharedFunctionalGroupsSequence);
            Attributes frameFctGroups = imgAttrs.getNestedDataset(
                    Tag.PerFrameFunctionalGroupsSequence, frameIndex);
            lutParam.setModalityLUT(
                    selectFctGroup(imgAttrs, sharedFctGroups, frameFctGroups,
                            Tag.PixelValueTransformationSequence));
            if (dParam.getWindowWidth() != 0) {
                lutParam.setWindowCenter(dParam.getWindowCenter());
                lutParam.setWindowWidth(dParam.getWindowWidth());
            } else
                lutParam.setVOI(
                    selectFctGroup(imgAttrs, sharedFctGroups, frameFctGroups,
                            Tag.FrameVOILUTSequence),
                    dParam.getWindowIndex(),
                    dParam.getVOILUTIndex(),
                    dParam.isPreferWindow());
            if (dParam.isAutoWindowing())
                lutParam.autoWindowing(imgAttrs, raster);
            lutParam.setPresentationLUT(imgAttrs);
        }
        LookupTable lut = lutParam.createLUT(outBits);
        lut.lookup(raster, destRaster);
        return destRaster;
    }

    private Attributes selectFctGroup(Attributes imgAttrs,
            Attributes sharedFctGroups,
            Attributes frameFctGroups,
            int tag) {
        if (frameFctGroups == null) {
            return imgAttrs;
        }
        Attributes group = frameFctGroups.getNestedDataset(tag);
        if (group == null && sharedFctGroups != null) {
            group = sharedFctGroups.getNestedDataset(tag);
        }
        return group != null ? group : imgAttrs;
    }

    private Attributes selectVOILUT(Attributes psAttrs, String iuid, int frame) {
        Sequence voiLUTs = psAttrs.getSequence(Tag.SoftcopyVOILUTSequence);
        if (voiLUTs != null)
            for (Attributes voiLUT : voiLUTs) {
                Sequence refImgs = voiLUT.getSequence(Tag.ReferencedImageSequence);
                if (refImgs == null || refImgs.isEmpty())
                    return voiLUT;
                for (Attributes refImg : refImgs) {
                    if (iuid.equals(refImg.getString(Tag.ReferencedSOPInstanceUID))) {
                        int[] refFrames = refImg.getInts(Tag.ReferencedFrameNumber);
                        if (refFrames == null)
                            return voiLUT;

                        for (int refFrame : refFrames)
                            if (refFrame == frame)
                                return voiLUT;
                    }
                }
            }
        return null;
    }

    /**
     * Read all of the attributes into memory including the pixel data as BulkData.  The code will skip the pixel data
     * when generating the bulkdata so it *should* be efficient.
     */
    private void readMetadata() throws IOException {
        if (metadata != null)
            return;

        setMetadata(metadataFactory.readMetaData(input));
    }

    /** Indicate if the given transfer syntax is video */
    public static boolean isVideo(String tsuid) {
        return VIDEO_TSUID.contains(tsuid);
    }

    /** Creates an offset/length table based on the frame positions */
    public static void generateOffsetLengths(Fragments pixelData, int frames, byte[] basicOffsetTable, long start) {
        long lastOffset = 0;
        BulkData lastFrag = null;
        for(int frame=0; frame<frames; frame++) {
            long offset = frame>0 ? 1 : 0;
            int offsetStart = frame*4;
            if( basicOffsetTable.length>=offsetStart+4 ) {
                offset = ByteUtils.bytesToIntLE(basicOffsetTable, offsetStart);
                if( offset!=1 ) {
                    // Handle > 4 gb total image size by assuming incrementing modulo 4gb
                    offset = offset | (lastOffset & 0xFFFFFF00000000l);
                    if( offset < lastOffset ) offset += 0x100000000l;
                    lastOffset = offset;
                    LOG.trace("Found offset {} for frame {}", offset, frame);
                }
            }
            long position = -1;
            if( offset!=1 ) {
                position = start+offset+8;
            }
            BulkData frag = new BulkData("compressedPixelData://", position,-1, false);
            if( lastFrag!=null && position!=-1 ) {
                lastFrag.setLength(position-8-lastFrag.offset());
            }
            lastFrag = frag;
            pixelData.add(frag);
            if( offset==0 && frame>0) {
                start = -1;
            }
        }
    }

    /**
     * Get the DicomAccessor instance which is being used to read the underlying DICOM instance.
     * @return the current DicomAccessor, or NULL if the DicomImageReader is uninitialized.
     */
    public DicomMetaData getDicomAccessor() {
        return this.metadata;
    }

    private void setMetadata(DicomMetaData metadata) {
        this.metadata = metadata;

        if (metadata.containsPixelData()) {
             if (metadata.getTransferSyntaxType().isPixeldataEncapsulated()) {
                 String tsuid = metadata.getTransferSyntax();
                if (tsuid == null)
                    throw new IllegalArgumentException("Missing Transfer Syntax for Data Set with compressed Pixel Data");

                ImageReaderParam param = ImageReaderFactory.getImageReaderParam(tsuid);
                if (param == null)
                    throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + tsuid);
                this.rle = tsuid.equals(UID.RLELossless);
                this.decompressor = ImageReaderFactory.getImageReader(param);
                this.patchJpegLS = param.patchJPEGLS;

                LOG.debug("Transfer Syntax {} is assigned Image Reader {}",tsuid, param.getClassName());
            }
        }
    }

    private SampleModel createSampleModel(int dataType, boolean banded) {
        return this.metadata.getPhotometricInterpretation().createSampleModel(dataType, this.metadata.getColumns(), this.metadata.getRows(), this.metadata.getSamplesPerPixel(), banded);
    }

    private ImageTypeSpecifier createImageType(int bits, int dataType, boolean banded) {
        return new ImageTypeSpecifier(
                createColorModel(bits, dataType),
                createSampleModel(dataType, banded));
    }

    private ColorModel createColorModel(int bits, int dataType) {
        return this.metadata.getPhotometricInterpretation().createColorModel(bits, dataType, metadata.getAttributes());
    }

    private void resetInternalState() {
        this.metadata = null;

        if (decompressor != null) {
            decompressor.dispose();
            decompressor = null;
        }

        this.patchJpegLS = null;
        this.actualNumberOfFrames = null;
    }

    private void checkIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= this.metadata.getNumberOfFrames())
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);
    }

    @Override
    public void dispose() {
        resetInternalState();
    }

    /** Clones this object, requires that the input stream objects are also all cloneable */
    @Override
    public DicomImageReader cloneIt() throws IOException {
        DicomImageReader clone = new DicomImageReader(this.getOriginatingProvider());
        clone.setInput(getStreamMetadata());
        return clone;
    }

    @Override
    public void close() throws IOException {
        dispose();
    }
}
