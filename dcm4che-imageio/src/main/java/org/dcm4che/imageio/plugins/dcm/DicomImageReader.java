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
import java.util.Collections;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.BulkDataLocator;
import org.dcm4che.data.Fragments;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.image.PhotometricInterpretation;
import org.dcm4che.imageio.stream.ImageInputStreamAdapter;
import org.dcm4che.io.DicomInputStream;
import org.dcm4che.io.DicomInputStream.IncludeBulkData;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Feb 2013
 *
 */
public class DicomImageReader extends ImageReader {

    private ImageInputStream iis;

    private DicomMetaData metadata;

    private int frames;

    private int width;

    private int height;

    private DicomInputStream dis;

    private BulkDataLocator pixeldata;

    private final VR.Holder pixeldataVR = new VR.Holder();

    private Fragments pixeldataFragments;

    private int samples;

    private boolean banded;

    private int bitsStored;

    private int bitsAllocated;

    private int dataType;

    private int frameLength;

    private PhotometricInterpretation pmi;

    private SampleModel sm;

    private ImageTypeSpecifier imageType;

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
    public Iterator<ImageTypeSpecifier> getImageTypes(int frameIndex)
            throws IOException {
        readMetadata();
        checkIndex(frameIndex);
        return Collections.singleton(imageType()).iterator();
    }

    private ImageTypeSpecifier imageType() throws IOException {
        ImageTypeSpecifier imageType = this.imageType;
        if (imageType == null)
            this.imageType = imageType = new ImageTypeSpecifier(
                    pmi.createColorModel(
                            bitsStored, dataType, metadata.getAttributes()),
                    sm);
        return imageType;
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

        iis.seek(pixeldata.offset + frameIndex * frameLength);
        WritableRaster wr = Raster.createWritableRaster(sm, null);
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

    @Override
    public BufferedImage read(int frameIndex, ImageReadParam param)
            throws IOException {
        WritableRaster raster = (WritableRaster) readRaster(frameIndex, param);
        ColorModel cm = imageType().getColorModel();
        BufferedImage bi = new BufferedImage(cm , raster, false, null);
        return bi;
    }

    private void readMetadata() throws IOException {
        if (metadata != null)
            return;

        if (iis == null)
            throw new IllegalStateException("Input not set");

        dis = new DicomInputStream(new ImageInputStreamAdapter(iis));
        dis.setIncludeBulkData(IncludeBulkData.LOCATOR);
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
            banded = ds.getInt(Tag.PlanarConfiguration, 0) != 0;
            bitsAllocated = ds.getInt(Tag.BitsAllocated, 8);
            bitsStored = ds.getInt(Tag.BitsStored, bitsAllocated);
            dataType = bitsAllocated <= 8 ? DataBuffer.TYPE_BYTE 
                                          : DataBuffer.TYPE_USHORT;
            pmi = PhotometricInterpretation.fromString(
                    ds.getString(Tag.PhotometricInterpretation, "MONOCHROME2"));
            if (pixeldata instanceof BulkDataLocator) {
                iis.setByteOrder(ds.bigEndian() 
                        ? ByteOrder.BIG_ENDIAN
                        : ByteOrder.LITTLE_ENDIAN);
                this.sm = pmi.createSampleModel(dataType, width, height, samples, banded);
                this.frameLength = pmi.frameLength(dataType, width, height, samples);
                this.pixeldata = (BulkDataLocator) pixeldata;
            } else {
                this.pixeldataFragments = (Fragments) pixeldata;
            }
        }
    }

    private void resetInternalState() {
        metadata = null;
        dis = null;
        frames = 0;
        width = 0;
        height = 0;
        pixeldata = null;
        pixeldataFragments = null;
        pmi = null;
        sm = null;
        imageType = null;
    }

    private void checkIndex(int frameIndex) {
        if (frameIndex < 0 || frameIndex >= frames)
            throw new IndexOutOfBoundsException();
    }

}
