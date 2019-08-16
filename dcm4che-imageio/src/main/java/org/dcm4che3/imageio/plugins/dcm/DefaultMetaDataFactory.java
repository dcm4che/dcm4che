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
package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.data.*;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.stream.DelegateImageInputStreamLoader;
import org.dcm4che3.io.stream.ImageInputStreamLoader;
import org.dcm4che3.io.stream.ServiceImageInputStreamLoader;
import org.dcm4che3.util.ByteUtils;
import org.dcm4che3.util.StreamUtils;
import org.dcm4che3.util.TagUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Default DICOM metadata factory which will generate BulkData based MetaData objects.  The builder is used to
 * to modify the default values for the factory.
 * @author Andrew Cowan (awcowan@gmail.com)
 */
public class DefaultMetaDataFactory implements DicomMetaDataFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMetaDataFactory.class);

    private static final ImageInputStreamLoader SERVICE_LOADER = new ServiceImageInputStreamLoader();

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


    public static int READ_ENTIRE_FILE = -1;
    public static int DEFAULT_STOP_TAG = READ_ENTIRE_FILE;
    public static int DEFAULT_BUFFER_SIZE = 16 * 1024;
    public static BulkDataDescriptor DEFAULT_DESCRIPTOR = BulkDataDescriptor.DEFAULT;

    private int stopTag = DEFAULT_STOP_TAG;
    private int bufferSize = DEFAULT_BUFFER_SIZE;
    private BulkDataDescriptor descriptor = DEFAULT_DESCRIPTOR;

    public DefaultMetaDataFactory() {
        // Just use defaults.
    }

    public DefaultMetaDataFactory(boolean readEntireFile) {
        this.stopTag = readEntireFile?READ_ENTIRE_FILE:Tag.PixelData;
    }

    @Override
    public DicomMetaData readMetaData(Object input) throws IOException {
        URI inputURI = createURI(input);
        ImageInputStreamLoader loader = createURILoader(inputURI, input);

        ImageInputStream iis = loader.openStream(inputURI);
        try (DicomInputStream dis = new DicomInputStream( StreamUtils.toInputStream(iis,bufferSize))) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            dis.setBulkDataDescriptor(descriptor);
            dis.setURI(inputURI.toString());

            DatasetWithFMI datasetWithFMI = dis.readDatasetWithFMI(-1, stopTag);

            Object pixelData;
            VR pixelDataVR;

            // Maybe we just fill in the fake pixel data info and pass FMI / Attributes directly...
            // That would mean we could maybe write a different implementation which creates a DIS to load data again.
            if(stopTag == Tag.PixelData) {
                pixelDataVR = dis.vr();
                int numberOfFrames = datasetWithFMI.getDataset().getInt(Tag.NumberOfFrames,1);
                pixelData = createPixelDataValue(dis, numberOfFrames);
            }
            else {
                pixelDataVR = datasetWithFMI.getDataset().getVR(Tag.PixelData);
                pixelData = datasetWithFMI.getDataset().remove(Tag.PixelData);
            }

            return new BulkDataMetaData(inputURI, loader, datasetWithFMI, pixelData, pixelDataVR);
        }
    }

    @Override
    public String toString() {
        return "DefaultMetaDataFactory(stopTag="+(stopTag==READ_ENTIRE_FILE?"entireFile": TagUtils.toHexString(stopTag))+", bufferSize="+bufferSize+", descriptor="+descriptor+")";
    }

    public int getStopTag() {
        return stopTag;
    }

    public void setStopTag(int stopTag) {
        this.stopTag = stopTag;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public BulkDataDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(BulkDataDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * Define how the URI is defined in the code.
     */
    protected URI createURI(Object input) {
        // Default to java:originalImageInputStream to avoid writing temp files
        String inputURI = "java:iis";

        if(input instanceof URI) {
            inputURI = input.toString();
        }
        else if(input instanceof URL) {
            inputURI = input.toString();
        }
        else if(input instanceof File) {
            // Do we need special handling for UNC here?!?
            inputURI = ((File)input).toURI().toString();
        }
        else if(input instanceof Path){
            inputURI = ((Path)input).toUri().toString();
        }

        return URI.create(inputURI);
    }

    /**
     * Create an ImageInputStreamLoader for the input value.
     */
    protected ImageInputStreamLoader createURILoader(URI inputURI, Object input) throws IOException {
        ImageInputStreamLoader loader = SERVICE_LOADER;

        if(inputURI.getScheme().equals("java")) {
            loader = new DelegateImageInputStreamLoader(SERVICE_LOADER.openStream(input));
        }

        return loader;
    }

    /** Initializes the pixel data reading from an image input stream */
    private Object createPixelDataValue(DicomInputStream dis, int numberOfFrames) throws IOException {
         int pixelDataLength = dis.length();

        Object pixelData;
        if( pixelDataLength==0 ) {
            pixelData = null;
        }
        else if( pixelDataLength>0 ) {
            pixelData = new BulkData(dis.getURI(), dis.getPosition(), dis.length(),dis.bigEndian());
        }
        else {
            dis.readItemHeader();
            byte[] offsetTable = new byte[dis.length()];
            dis.readFully(offsetTable);

            long start = dis.getPosition();

            String tsUID = dis.getFileMetaInformation().getString(Tag.TransferSyntaxUID);
            Fragments pixelDataFragments = new Fragments(null, Tag.PixelData, dis.vr(), dis.bigEndian(), isVideo(tsUID) ? 16 : numberOfFrames);
            pixelDataFragments.add(offsetTable);

            generateOffsetLengths(pixelDataFragments, dis.getURI(), isVideo(tsUID) ? 1 : numberOfFrames, offsetTable, start);
            pixelData = pixelDataFragments;
        }

        return pixelData;
    }

    /** Indicate if the given transfer syntax is video */
    public boolean isVideo(String tsUID) {
        return VIDEO_TSUID.contains(tsUID);
    }


    /** Creates an offset/length table based on the frame positions */
    public void generateOffsetLengths(Fragments pixelData, String inputURI, int frames, byte[] basicOffsetTable, long start) {
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

            // Make sure the ends of the frames are marked.
            BulkData frag = new BulkData(inputURI, position,-1, false);
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
}
