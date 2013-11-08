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

package org.dcm4che.imageio.plugins.dcm;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;

import org.codecCentral.imageio.generic.SegmentedDataInfo;
import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkData;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Sequence;
import org.dcm4che.data.Tag;
import org.dcm4che.data.UID;
import org.dcm4che.data.VR;
import org.dcm4che.image.LookupTable;
import org.dcm4che.image.LookupTableFactory;
import org.dcm4che.image.Overlays;
import org.dcm4che.image.PhotometricInterpretation;
import org.dcm4che.image.StoredValue;
import org.dcm4che.imageio.codec.ImageReaderFactory;
import org.dcm4che.imageio.codec.ImageReaderFactory.ImageReaderParam;
import org.dcm4che.imageio.codec.jpeg.PatchJPEGLS;
import org.dcm4che.imageio.codec.jpeg.PatchJPEGLSImageInputStream;
import org.dcm4che.imageio.stream.ImageInputStreamAdapter;
import org.dcm4che.imageio.stream.SegmentedInputImageStream;
import org.dcm4che.io.BulkDataDescriptor;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomInputStream.IncludeBulkData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Feb 2013
 *
 */
public class DicomImageReader extends ImageReader {

    private static final Logger LOG = LoggerFactory.getLogger(DicomImageReader.class);

    private ImageInputStream iis;

    private DicomMetaData metadata;

    private int frames;

    private int width;

    private int height;

    private DicomInputStream dis;

    private BulkData pixeldata;

    private final VR.Holder pixeldataVR = new VR.Holder();

    private Fragments pixeldataFragments;

    private ImageReader decompressor;

    private PatchJPEGLS patchJpegLS;

    private int samples;

    private boolean banded;

    private int bitsStored;

    private int bitsAllocated;

    private int dataType;

    private int frameLength;

    private PhotometricInterpretation pmi;

    public DicomImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly,
            boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        resetInternalState();
        iis = (ImageInputStream) input;
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

        if (decompressor == null)
            createImageType(bitsStored, dataType, banded);
        
        if (isRLELossless())
            createImageType(bitsStored, dataType, true);
        
        decompressor.setInput(iisOfFrame(0));
        return decompressor.getRawImageType(0);
    }

    private boolean isRLELossless() {
        return dis.getTransferSyntax().equals(UID.RLELossless);
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        
        ImageTypeSpecifier imageType;
        if (pmi.isMonochrome())
            imageType = createImageType(8, DataBuffer.TYPE_BYTE, false);
        else if (decompressor == null)
            imageType = createImageType(bitsStored, dataType, banded);
        else if (isRLELossless())
            imageType = createImageType(bitsStored, dataType, true);
        else {
            decompressor.setInput(iisOfFrame(0));
            return decompressor.getImageTypes(0);
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

        if (decompressor != null) {
            decompressor.setInput(iisOfFrame(frameIndex));

            if (LOG.isDebugEnabled())
                LOG.debug("Start decompressing frame #" + (frameIndex + 1));
            Raster wr = pmi.decompress() == pmi && decompressor.canReadRaster()
                    ? decompressor.readRaster(0, decompressParam(param))
                    : decompressor.read(0, decompressParam(param)).getRaster();
            if (LOG.isDebugEnabled())
                LOG.debug("Finished decompressing frame #" + (frameIndex + 1));
            return wr;
        }
        iis.seek(pixeldata.offset + frameIndex * frameLength);
        WritableRaster wr = Raster.createWritableRaster(
                createSampleModel(dataType, banded), null);
        DataBuffer buf = wr.getDataBuffer();
        if (buf instanceof DataBufferByte) {
            byte[][] data = ((DataBufferByte) buf).getBankData();
            for (byte[] bs : data)
                iis.readFully(bs);
        } else {
            short[] data = ((DataBufferUShort) buf).getData();
            iis.readFully(data, 0, data.length);
        }
        return wr;
    }

    private ImageReadParam decompressParam(ImageReadParam param) {
        ImageReadParam decompressParam = decompressor.getDefaultReadParam();
        ImageTypeSpecifier imageType = param.getDestinationType();
        BufferedImage dest = param.getDestination();
        if (isRLELossless() && imageType == null && dest == null)
            imageType = createImageType(bitsStored, dataType, true);
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
        	if (patchJpegLS == null)
        	{
        		SegmentedInputImageStream segmentedStream = 
        				new SegmentedInputImageStream(null, pixeldataFragments, frameIndex);
        		DicomImageReadParam dicomParams = (DicomImageReadParam)param;
        		SegmentedDataInfo info = new SegmentedDataInfo(dicomParams.getSourceFile().getAbsolutePath(),
										        				segmentedStream.getSegmentPositionsList(),
										        				segmentedStream.getSegmentLengths());
            	//try to set an array of strings
            	try
            	{
            		decompressor.setInput(info);
            	}
            	catch(IllegalArgumentException ex)
            	{
                    decompressor.setInput(iisOfFrame(frameIndex));        		
            	}        		
        	}
        	else
        	{
        		 decompressor.setInput(iisOfFrame(frameIndex));      
        	}
            if (LOG.isDebugEnabled())
                LOG.debug("Start decompressing frame #" + (frameIndex + 1));
            BufferedImage bi = decompressor.read(0, decompressParam(param));
            if (LOG.isDebugEnabled())
                LOG.debug("Finished decompressing frame #" + (frameIndex + 1));
            if (samples > 1)
                return bi;
            
            raster = bi.getRaster();
        } else
            raster = (WritableRaster) readRaster(frameIndex, param);

        ColorModel cm;
        if (pmi.isMonochrome()) {
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
            cm = createColorModel(bitsStored, dataType);
        }
        return new BufferedImage(cm, raster , false, null);
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
        Overlays.extractFromPixeldata(raster, mask, ovlyData, 0, length);
        return ovlyData;
    }

    @SuppressWarnings("resource")
    private ImageInputStreamImpl iisOfFrame(int frameIndex)
            throws IOException {
        SegmentedInputImageStream siis = new SegmentedInputImageStream(
                iis, pixeldataFragments, frameIndex);
        return patchJpegLS != null
                ? new PatchJPEGLSImageInputStream(siis, patchJpegLS)
                : siis;
    }

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

    private void readMetadata() throws IOException {
        if (metadata != null)
            return;

        if (iis == null)
            throw new IllegalStateException("Input not set");

        dis = new DicomInputStream(new ImageInputStreamAdapter(iis));
        dis.setIncludeBulkData(IncludeBulkData.URI);
        dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
        dis.setURI("java:iis"); // avoid copy of pixeldata to temporary file
        Attributes fmi = dis.readFileMetaInformation();
        Attributes ds = dis.readDataset(-1, -1);
        metadata = new DicomMetaData(fmi, ds);
        Object pixeldata = ds.getValue(Tag.PixelData, pixeldataVR );
        if (pixeldata != null) {
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
            if (pixeldata instanceof BulkData) {
                iis.setByteOrder(ds.bigEndian() 
                        ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN);
                this.frameLength = pmi.frameLength(width, height, samples, bitsAllocated);
                this.pixeldata = (BulkData) pixeldata;
            } else {
                String tsuid = dis.getTransferSyntax();
                ImageReaderParam param =
                        ImageReaderFactory.getImageReaderParam(tsuid);
                if (param == null)
                    throw new IOException("Unsupported Transfer Syntax: " + tsuid);
                this.decompressor = ImageReaderFactory.getImageReader(param);
                this.patchJpegLS = param.patchJPEGLS;
                this.pixeldataFragments = (Fragments) pixeldata;
            }
        }
    }

    private SampleModel createSampleModel(int dataType, boolean banded) {
        return pmi.createSampleModel(dataType, width, height, samples, banded);
    }

    private ImageTypeSpecifier createImageType(int bits, int dataType, boolean banded) {
        return new ImageTypeSpecifier(
                createColorModel(bits, dataType),
                createSampleModel(dataType, banded));
    }

    private ColorModel createColorModel(int bits, int dataType) {
        return pmi.createColorModel(bits, dataType, metadata.getAttributes());
    }

    private void resetInternalState() {
        metadata = null;
        dis = null;
        frames = 0;
        width = 0;
        height = 0;
        pixeldata = null;
        pixeldataFragments = null;
        if (decompressor != null) {
            decompressor.dispose();
            decompressor = null;
        }
        patchJpegLS = null;
        pmi = null;
    }

    private void checkIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= frames)
            throw new IndexOutOfBoundsException("imageIndex: " + frameIndex);
    }

    @Override
    public void dispose() {
        resetInternalState();
    }

}
