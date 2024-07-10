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

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.EOFException;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.*;
import org.dcm4che3.image.LookupTable;
import org.dcm4che3.imageio.codec.ImageDescriptor;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageReaderFactory.ImageReaderParam;
import org.dcm4che3.imageio.codec.TransferSyntaxType;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che3.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che3.imageio.stream.EncapsulatedPixelDataImageInputStream;
import org.dcm4che3.imageio.stream.ImageInputStreamAdapter;
import org.dcm4che3.imageio.stream.RAFFileImageInputStream;
import org.dcm4che3.imageio.stream.SegmentedInputImageStream;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomInputStream.IncludeBulkData;
import org.dcm4che3.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads header and image data from a DICOM object.
 * 
 * Supports compressed and uncompressed images from a DicomMetaData object, an InputStream/DicomInputStream or an ImageInputStream.
 * For ImageInputStream, the access supports random/out of order reading from the input for everything except deflated streams.
 * For InputStream type data, only sequential access to images is supported, including deflated.
 * For DicomMetaData, random access is fully supported, and can have been read from a deflated stream.
 * Objects without pixel data are also supported, although only the metadata can be read from them (mostly for the use case that it is unknown whether or not there is
 * pixel data).
 * 
 * Tag values after the pixel data are not read up-front for performance reasons/ability to actually read them up front.  Call the relevant methods below to read that data.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Bill Wallace <wayfarer3130@gmail.com>
 * @since Feb 2013
 *
 */
public class DicomImageReader extends ImageReader implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(DicomImageReader.class);

    public static final String POST_PIXEL_DATA = "postPixelData";
    static final ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    private ImageInputStream iis;

    private DicomInputStream dis;

    private EncapsulatedPixelDataImageInputStream epdiis;

    private DicomMetaData metadata;

    private BulkData pixelData;

    private Fragments pixelDataFragments;

    private byte[] pixeldataBytes;

    private long pixelDataLength;

    private VR pixelDataVR;

    private File pixelDataFile;

    private int frames;

    private int flushedFrames;

    private int width;

    private int height;

    private ImageReader decompressor;

    private boolean rle;

    private PatchJPEGLS patchJpegLS;

    private int samples;

    private boolean banded;

    private int bitsStored;

    private int bitsAllocated;

    private int dataType;

    private int frameLength;

    private PhotometricInterpretation pmi;
    private PhotometricInterpretation pmiAfterDecompression;
    private ImageDescriptor imageDescriptor;
    private ICCProfile.ColorSpaceFactory colorSpaceFactory;

    public DicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();
        if (input instanceof InputStream) {
            try {
                dis = (input instanceof DicomInputStream)
                        ? (DicomInputStream) input
                        : new DicomInputStream((InputStream) input);
            } catch (IOException e) {
               throw new IllegalArgumentException(e.getMessage());
            }
        } else if (input instanceof DicomMetaData) {
            DicomMetaData metadata = (DicomMetaData) input;
            initPixelDataFromAttributes(metadata.getAttributes());
            initPixelDataFile();
            setMetadata(metadata);
        } else {
            iis = (ImageInputStream) input;
        }
    }

    private void initPixelDataFromAttributes(Attributes ds) {
        VR.Holder holder = new VR.Holder();
        Object value = ds.getValue(Tag.PixelData, holder);
        if (value != null) {
            imageDescriptor = new ImageDescriptor(ds);
            pixelDataVR = holder.vr;
            if (value instanceof BulkData) {
                pixelData = (BulkData) value;
                pixelDataLength = pixelData.longLength();
            } else if( value instanceof byte[] ) {
                pixeldataBytes = (byte[]) value;
                pixelDataLength = pixeldataBytes.length;
            } else { // value instanceof Fragments
                pixelDataFragments = (Fragments) value;
                pixelDataLength = -1;
            }
        }
    }

    private void initPixelDataFile() {
        if (pixelData != null)
            pixelDataFile = pixelData.getFile();
        else if (pixelDataFragments != null)
            pixelDataFile = pixelDataFragmentsFile(pixelDataFragments);
    }

    private File pixelDataFragmentsFile(Fragments pixelDataFragments) {
        File f = null;
        for (Object frag : pixelDataFragments) {
            if (frag instanceof BulkData)
                if (f == null)
                    f = ((BulkData) frag).getFile();
                else if (!f.equals(((BulkData) frag).getFile()))
                    throw new UnsupportedOperationException(
                            "data fragments in individual bulk data files not supported");
        }
        return f;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        readMetadata();
        return frames;
    }

    @Override
    public int getWidth(int frameIndex) throws IOException {
       readMetadata();
       checkIndex(frameIndex);
       return width;
    }

    @Override
    public int getHeight(int frameIndex) throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        return height;
    }


    @Override
    public ImageTypeSpecifier getRawImageType(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        ColorSpace cspace = colorSpaceOfFrame(frameIndex).orElse(sRGB);
        if (decompressor == null)
            return createImageType(bitsStored, dataType, banded, cspace);
        
        if (rle)
            return createImageType(bitsStored, dataType, true, cspace);
        
        openiis();
        try {
            decompressor.setInput(iisOfFrame(0));
            return decompressor.getRawImageType(0);
        } finally {
            closeiis();
        }
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        ColorSpace cspace = colorSpaceOfFrame(frameIndex).orElse(sRGB);
        ImageTypeSpecifier imageType;
        if (pmi.isMonochrome())
            imageType = createImageType(8, DataBuffer.TYPE_BYTE, false, cspace);
        else if (decompressor == null)
            imageType = createImageType(bitsStored, dataType, banded, cspace);
        else if (rle)
            imageType = createImageType(bitsStored, dataType, true, cspace);
        else {
            openiis();
            try {
                decompressor.setInput(iisOfFrame(0));
                return decompressor.getImageTypes(0);
            } finally {
                closeiis();
            }
        }

        return Collections.singletonList(imageType).iterator();
    }

    private void openiis() throws IOException {
        if (iis == null) {
            if (pixelDataFile != null) {
                iis = new RAFFileImageInputStream(pixelDataFile);
            } else if (pixeldataBytes != null) {
                iis = new SegmentedInputImageStream(pixeldataBytes);
            }
        }
    }

    private void closeiis() throws IOException {
        if ( (pixelDataFile != null || pixeldataBytes!=null) && iis != null) {
            iis.close();
            iis = null;
        }
    }

    @Override
    public ImageReadParam getDefaultReadParam() {
        return new DicomImageReadParam();
    }

    /** 
     * Gets the stream metadata.  May not contain post pixel data unless
     * there are no images or the getStreamMetadata has been called with the post pixel data 
     * node being specified.
     */
    @Override
    public DicomMetaData getStreamMetadata() throws IOException {
        readMetadata();
        return metadata;
    }
    
    /**
     * Gets the stream metadata.
     * If nodeNames contains POST_PIXEL_DATA constant "postPixelData" then
     * read the post pixel data as well.  In an InputStream instance that can
     * only safely be done after all pixel data is read.  On imageInputStream it
     * may be slow for large multiframes, but can safely be done at any time.
     */
    @Override
    public DicomMetaData getStreamMetadata(String formatName,
            Set<String> nodeNames)
                    throws IOException
    {
        DicomMetaData ret = getStreamMetadata();
        if( nodeNames!=null && nodeNames.contains(POST_PIXEL_DATA)) {
            readPostPixeldata();
            return getStreamMetadata();
        }
        return ret;
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

        openiis();
        try {
            if (decompressor != null) {
                decompressor.setInput(iisOfFrame(frameIndex));
    
                if (LOG.isDebugEnabled())
                    LOG.debug("Start decompressing frame #" + (frameIndex + 1));
                Raster wr = pmiAfterDecompression == pmi && decompressor.canReadRaster()
                        ? decompressor.readRaster(0, decompressParam(param))
                        : decompressor.read(0, decompressParam(param)).getRaster();
                if (LOG.isDebugEnabled())
                    LOG.debug("Finished decompressing frame #" + (frameIndex + 1));
                return wr;
            }
            WritableRaster wr = Raster.createWritableRaster(
                    createSampleModel(dataType, banded), null);
            DataBuffer buf = wr.getDataBuffer();
            if (dis != null) {
                dis.skipFully((frameIndex - flushedFrames) * frameLength);
                flushedFrames = frameIndex + 1;
            } else if (pixeldataBytes != null) {
                iis.setByteOrder(bigEndian()
                        ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN);
                iis.seek(frameIndex * frameLength);
            } else {
                iis.setByteOrder(bigEndian()
                        ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN);
                iis.seek(pixelData.offset() + frameIndex * frameLength);
            }
            if (buf instanceof DataBufferByte) {
                byte[][] data = ((DataBufferByte) buf).getBankData();
                for (byte[] bs : data)
                    if (dis != null)
                        dis.readFully(bs);
                    else
                        iis.readFully(bs);
                if (pixelDataVR == VR.OW && bigEndian())
                    ByteUtils.swapShorts(data);
            } else {
                short[] data = ((DataBufferUShort) buf).getData();
                if (dis != null)
                    dis.readFully(data, 0, data.length);
                else
                    iis.readFully(data, 0, data.length);
            }
            return wr;
        } finally {
            closeiis();
        }
    }

    private boolean bigEndian() {
        return metadata.bigEndian();
    }
    
    private String getTransferSyntaxUID() {
        return metadata.getTransferSyntaxUID();
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
            imageType = createImageType(bitsStored, dataType, true, sRGB);
        decompressParam.setDestinationType(imageType);
        decompressParam.setDestination(dest);
        return decompressParam;
    }

    @Override
    public BufferedImage read(int frameIndex, ImageReadParam param)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);

        BufferedImage bi = null;
        WritableRaster raster;
        if (decompressor != null) {
            openiis();
            try {
                ImageInputStream iisOfFrame = iisOfFrame(frameIndex);
                // Reading this up front sets the required values so that opencv succeeds - it is less than optimal performance wise
                iisOfFrame.length();
                decompressor.setInput(iisOfFrame);
                LOG.debug("Start decompressing frame #{}", (frameIndex + 1));
                bi = decompressor.read(0, decompressParam(param));
                LOG.debug("Finished decompressing frame #{}", (frameIndex + 1));
            } finally {
                closeiis();
            }
            raster = bi.getRaster();
        } else {
            raster = (WritableRaster) readRaster(frameIndex, param);
        }
        return pmi.isMonochrome()
                ? applyGrayscaleTransformations(frameIndex, param, raster)
                : applyColorTransformations(frameIndex, param, raster, bi);
    }

    private BufferedImage applyGrayscaleTransformations(int frameIndex, ImageReadParam param, WritableRaster raster) {
        int[] overlayGroupOffsets = getActiveOverlayGroupOffsets(param);
        byte[][] overlayData = new byte[overlayGroupOffsets.length][];
        for (int i = 0; i < overlayGroupOffsets.length; i++) {
            overlayData[i] = extractOverlay(overlayGroupOffsets[i], raster);
        }
        SampleModel sm = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_BYTE,
                width,
                height,
                1,
                width,
                new int[1]);
        raster = applyLUTs(raster, frameIndex, param, sm, 8);
        for (int i = 0; i < overlayGroupOffsets.length; i++) {
            try {
                applyOverlayMonochrome(overlayGroupOffsets[i], raster, frameIndex, param, overlayData[i]);
            } catch (IllegalArgumentException e) {
                LOG.info(ignoreInvalidOverlay(overlayGroupOffsets[i], e));
            }
        }
        ColorModel cm = ColorModelFactory.createMonochromeColorModel(8, DataBuffer.TYPE_BYTE);
        BufferedImage bi = new BufferedImage(cm, raster, false, null);
        return bi;
    }

    private BufferedImage applyColorTransformations(int frameIndex, ImageReadParam param, WritableRaster raster,
            BufferedImage bi) {
        int[] overlayGroupOffsets = getActiveOverlayGroupOffsets(param);
        Optional<ColorSpace> iccColorSpace = colorSpaceOfFrame(frameIndex);
        if (bi != null
                && pmi != PhotometricInterpretation.PALETTE_COLOR
                && bi.getColorModel().getColorSpace().getType()
                    == (pmiAfterDecompression.isYBR() ? ColorSpace.TYPE_YCbCr : ColorSpace.TYPE_RGB)
                && overlayGroupOffsets.length == 0
                && !iccColorSpace.isPresent()) {
            return bi;
        }
        ColorSpace colorSpace = iccColorSpace.orElse(sRGB);
        ColorModel cm = createColorModel(bitsStored, dataType, colorSpace);
        if (cm.isCompatibleRaster(raster)) {
            bi = new BufferedImage(cm, raster, false, null);
        } else {
            if (bi == null) {
                DirectColorModel directColorModel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
                LOG.info("Missing Color Model information, assume {}", directColorModel);
                bi = new BufferedImage(directColorModel, bi.getRaster(), false, null);
            }
            bi = BufferedImageUtils.convertColor(bi, cm);
        }
        if (overlayGroupOffsets.length == 0) {
            return bi;
        }
        if (cm instanceof PaletteColorModel) {
            bi = BufferedImageUtils.convertPalettetoRGB(bi, null);
        }
        for (int i = 0; i < overlayGroupOffsets.length; i++) {
            try {
                applyOverlayColor(overlayGroupOffsets[i], bi.getRaster(), frameIndex, param, bi.getColorModel().getColorSpace());
            } catch (IllegalArgumentException e) {
                LOG.info(ignoreInvalidOverlay(overlayGroupOffsets[i], e));
            }
        }
        return bi;
    }

    private static String ignoreInvalidOverlay(int overlayGroupOffset, IllegalArgumentException e) {
        return String.format("Ignore invalid Overlay (60%02X,eeee) with %s", overlayGroupOffset, e.getMessage());
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
        if (bitPosition < bitsStored)
            LOG.info("Ignore embedded overlay #{} from bit #{} < bits stored: {}",
                    (gg0000 >>> 17) + 1, bitPosition, bitsStored);
        else
            Overlays.extractFromPixeldata(raster, mask, ovlyData, 0, length);
        return ovlyData;
    }

    /** Generate an image input stream for the given frame, -1 for all frames (video, multi-component single frame)
     * Does not necessarily support the length operation without seeking/reading to the end of the input.
     * 
     * @param frameIndex
     * @return
     * @throws IOException
     */
    public ImageInputStream iisOfFrame(int frameIndex) throws IOException {
        ImageInputStream iisOfFrame;
        if (epdiis != null) {
            seekFrame(frameIndex);
            iisOfFrame = epdiis;
        } else if( pixelDataFragments==null ) {
            return null;
        } else {
            iisOfFrame = new SegmentedInputImageStream(
                    iis, pixelDataFragments, frames==1 ? -1 : frameIndex);
            ((SegmentedInputImageStream) iisOfFrame).setImageDescriptor(imageDescriptor);
        }
        return patchJpegLS != null
                ? new PatchJPEGLSImageInputStream(iisOfFrame, patchJpegLS)
                : iisOfFrame;
    }

    public Optional<ColorSpace> colorSpaceOfFrame(int frameIndex) {
        ICCProfile.ColorSpaceFactory colorSpaceFactory = this.colorSpaceFactory;
        if (colorSpaceFactory == null) {
            this.colorSpaceFactory = colorSpaceFactory = ICCProfile.colorSpaceFactoryOf(metadata.getAttributes());
        }
        return colorSpaceFactory.getColorSpace(frameIndex);
    }

    private void seekFrame(int frameIndex) throws IOException {
        assert frameIndex >= flushedFrames;
        if (frameIndex == flushedFrames)
            epdiis.seekCurrentFrame();
        else while (frameIndex > flushedFrames) {
            if (!epdiis.seekNextFrame()) {
                throw new IOException("Data Fragments only contains " + (flushedFrames + 1) + " frames");
            }
            flushedFrames++;
        }
    }

    private void applyOverlayMonochrome(int gg0000, WritableRaster raster,
            int frameIndex, ImageReadParam param, byte[] ovlyData) {
        Attributes ovlyAttrs = metadata.getAttributes();
        int[] pixelValue = new int[] { 0xff };
        if (param instanceof DicomImageReadParam) {
            DicomImageReadParam dParam = (DicomImageReadParam) param;
            pixelValue = new int[] { dParam.getOverlayGrayscaleValue() >> 8 };
            Attributes psAttrs = dParam.getPresentationState();
            if (psAttrs != null) {
                if (psAttrs.containsValue(Tag.OverlayData | gg0000))
                    ovlyAttrs = psAttrs;
                pixelValue = Overlays.getRecommendedGrayscalePixelValue(psAttrs, gg0000, 8);
            }
        }
        Overlays.applyOverlay(ovlyData != null ? 0 : frameIndex, raster, ovlyAttrs, gg0000, pixelValue, ovlyData);
    }

    private void applyOverlayColor(int gg0000, WritableRaster raster, int frameIndex, ImageReadParam param,
            ColorSpace cspace) {
        Attributes ovlyAttrs = metadata.getAttributes();
        int[] pixelValue = new int[] { 0xff, 0xff, 0xff };
        if (param instanceof DicomImageReadParam) {
            DicomImageReadParam dParam = (DicomImageReadParam) param;
            pixelValue = dParam.getOverlayRGBPixelValue();
            Attributes psAttrs = dParam.getPresentationState();
            if (psAttrs != null) {
                if (psAttrs.containsValue(Tag.OverlayData | gg0000))
                    ovlyAttrs = psAttrs;
                pixelValue = Overlays.getRecommendedRGBPixelValue(psAttrs, gg0000, cspace);
            }
        }
        Overlays.applyOverlay(frameIndex, raster, ovlyAttrs, gg0000, pixelValue, null);
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
            lutParam.setPresentationLUT(psAttrs, false);
        } else {
            Attributes sharedFctGroups = imgAttrs.getNestedDataset(
                    Tag.SharedFunctionalGroupsSequence);
            Attributes frameFctGroups = imgAttrs.getNestedDataset(
                    Tag.PerFrameFunctionalGroupsSequence, frameIndex);
            if (LookupTableFactory.applyModalityLUT(imgAttrs)) {
                lutParam.setModalityLUT(
                        selectFctGroup(imgAttrs, sharedFctGroups, frameFctGroups,
                                Tag.PixelValueTransformationSequence));
            }
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
                lutParam.autoWindowing(imgAttrs, raster, dParam.isAddAutoWindow());
            lutParam.setPresentationLUT(imgAttrs, dParam.isIgnorePresentationLUTShape());
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
                        if (refFrames == null  || refFrames.length == 0)
                            return voiLUT;
    
                        for (int refFrame : refFrames)
                            if (refFrame == frame)
                                return voiLUT;
                    }
                }
            }
        return null;
    }

    private void readMetadata() throws IOException {
        if (metadata != null)
            return;

        if (dis != null) {
            Attributes fmi = dis.readFileMetaInformation();
            Attributes ds = dis.readDatasetUntilPixelData();
            if (dis.tag() == Tag.PixelData) {
                imageDescriptor = new ImageDescriptor(ds);
                pixelDataVR = dis.vr();
                pixelDataLength = dis.unsignedLength();
                if (pixelDataLength == -1)
                    epdiis = new EncapsulatedPixelDataImageInputStream(dis, imageDescriptor);
            } else {
                try {
                    dis.readAllAttributes(ds);
                } catch (EOFException e) {};
            }
            setMetadata(new DicomMetaData(fmi, ds));
            return;
        }
        if (iis == null)
            throw new IllegalStateException("Input not set");

        DicomInputStream dis = new DicomInputStream(new ImageInputStreamAdapter(iis));
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
        dis.setURI("java:iis"); // avoid copy of pixeldata to temporary file
        Attributes fmi = dis.readFileMetaInformation();
        Attributes ds = dis.readDatasetUntilPixelData();
        if( dis.tag() == Tag.PixelData ) {
            imageDescriptor = new ImageDescriptor(ds);
            pixelDataVR = dis.vr();
            pixelDataLength = dis.unsignedLength();
        } else {
            try {
                dis.readAllAttributes(ds);
            } catch (EOFException e) {};
        }
        setMetadata(new DicomMetaData(fmi, ds));
        initPixelDataIIS(dis);
    }

    /** Initializes the pixel data reading from an image input stream */
    private void initPixelDataIIS(DicomInputStream dis) throws IOException {
        if( pixelDataLength==0 ) return;
        if( pixelDataLength>0 ) {
            pixelData = new BulkData("pixeldata://", dis.getPosition(), dis.length(),dis.bigEndian());
            metadata.getAttributes().setValue(Tag.PixelData, pixelDataVR, pixelData);
            return;
        }
        dis.readItemHeader();
        byte[] b = new byte[dis.length()];
        dis.readFully(b);        

        long start = dis.getPosition();
        pixelDataFragments = new Fragments(pixelDataVR, dis.bigEndian(), frames);
        pixelDataFragments.add(b);
        
        generateOffsetLengths(pixelDataFragments, frames,b, start);
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

    private void setMetadata(DicomMetaData metadata) {
        this.metadata = metadata;
        Attributes ds = metadata.getAttributes();
        if (pixelDataLength != 0) {
            frames = ds.getInt(Tag.NumberOfFrames, 1);
            width = ds.getInt(Tag.Columns, 0);
            height = ds.getInt(Tag.Rows, 0);
            samples = ds.getInt(Tag.SamplesPerPixel, 1);
            banded = samples > 1 && ds.getInt(Tag.PlanarConfiguration, 0) != 0;
            bitsAllocated = ds.getInt(Tag.BitsAllocated, 8);
            bitsStored = ds.getInt(Tag.BitsStored, bitsAllocated);
            dataType = bitsAllocated <= 8 ? DataBuffer.TYPE_BYTE 
                                          : DataBuffer.TYPE_USHORT;
            pmi = PhotometricInterpretation.fromString(
                    ds.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
            if (pixelDataLength != -1) {
                pmiAfterDecompression = pmi;
                this.frameLength = pmi.frameLength(width, height, samples, bitsAllocated);
            } else {
                Attributes fmi = metadata.getFileMetaInformation();
                if (fmi == null)
                    throw new IllegalArgumentException("Missing File Meta Information for Data Set with compressed Pixel Data");
                
                String tsuid = fmi.getString(Tag.TransferSyntaxUID);
                ImageReaderParam param =
                        ImageReaderFactory.getImageReaderParam(tsuid);
                if (param == null)
                    throw new UnsupportedOperationException("Unsupported Transfer Syntax: " + tsuid);
                TransferSyntaxType tsType = TransferSyntaxType.forUID(tsuid);
                if (tsType.adjustBitsStoredTo12(ds)) {
                    LOG.info("Adjust invalid Bits Stored: {} of {} to 12", bitsStored, tsType);
                    bitsStored = 12;
                }
                pmiAfterDecompression = pmi.isYBR() && TransferSyntaxType.isYBRCompression(tsuid)
                        ? PhotometricInterpretation.RGB
                        : pmi;
                this.rle = tsuid.equals(UID.RLELossless);
                this.decompressor = ImageReaderFactory.getImageReader(param);
                LOG.debug("Decompressor: {}", decompressor.getClass().getName());
                this.patchJpegLS = param.patchJPEGLS;
            }
        }
    }

    private SampleModel createSampleModel(int dataType, boolean banded) {
        return pmi.createSampleModel(dataType, width, height, samples, banded);
    }

    private ImageTypeSpecifier createImageType(int bits, int dataType, boolean banded, ColorSpace cspace) {
        return new ImageTypeSpecifier(
                createColorModel(bits, dataType, cspace),
                createSampleModel(dataType, banded));
    }

    private ColorModel createColorModel(int bits, int dataType, ColorSpace cspace) {
        return pmiAfterDecompression.createColorModel(bits, dataType, cspace, metadata.getAttributes());
    }

    private void resetInternalState() {
        dis = null;
        metadata = null;
        pixelData = null;
        pixelDataFragments = null;
        pixelDataVR = null;
        pixelDataLength = 0;
        pixeldataBytes = null;
        pixelDataFile = null;
        frames = 0;
        flushedFrames = 0;
        width = 0;
        height = 0;
        if (decompressor != null) {
            decompressor.dispose();
            decompressor = null;
        }
        patchJpegLS = null;
        pmi = null;
        colorSpaceFactory = null;
    }

    private void checkIndex(int frameIndex) {
        if (frames == 0)
            throw new IllegalStateException("Missing Pixel Data");
        
        if (frameIndex < 0 || frameIndex >= frames)
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);

        if (dis != null && frameIndex < flushedFrames)
            throw new IllegalStateException(
                    "input stream position already after requested frame #" + (frameIndex + 1));
    }
    
    /** Reads post-pixel data tags, will skip past any remaining images (which may be very slow), and
     * add any post-pixel data information to the attributes object.
     * NOTE: This read will read past image data, and may end up scanning/seeking through multiframe or video data in order to find the
     * post pixel data.  This may be slow.
     *
     * Replaces the attributes object with a new one, thus is thread safe for other uses of the object.
     */
    public Attributes readPostPixeldata() throws IOException {
        if( frames==0 ) return metadata.getAttributes();
        
        if( dis!=null ) {
            if( flushedFrames > frames ) {
                return metadata.getAttributes();
            }
            dis.skipFully((frames - flushedFrames) * frameLength);
            flushedFrames = frames+1;
            return readPostAttr(dis);
        }
        long offset;
        if( pixelData!=null ) {
            offset = pixelData.offset()+pixelData.longLength();
        } else {
            SegmentedInputImageStream siis = (SegmentedInputImageStream) iisOfFrame(-1);
            offset = siis.getOffsetPostPixelData();
        }
        iis.seek(offset);
        @SuppressWarnings("resource")
        DicomInputStream dis = new DicomInputStream(new ImageInputStreamAdapter(iis), getTransferSyntaxUID());
        return readPostAttr(dis);
    }
    
    private Attributes readPostAttr(DicomInputStream dis) throws IOException {
        Attributes postAttr = dis.readDataset();
        postAttr.addAll(metadata.getAttributes());
        metadata = new DicomMetaData(metadata.getFileMetaInformation(), postAttr);
        return postAttr;
   }

    @Override
    public void dispose() {
        resetInternalState();
    }

    @Override
    public void close() {
    	dispose();
    }
}
