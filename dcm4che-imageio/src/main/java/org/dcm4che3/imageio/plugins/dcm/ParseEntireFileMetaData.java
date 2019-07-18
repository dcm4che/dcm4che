//
///////////////////////////////////////////////////////////////
//                C O P Y R I G H T  (c) 2019                //
//        Agfa HealthCare N.V. and/or its affiliates         //
//                    All Rights Reserved                    //
///////////////////////////////////////////////////////////////
//                                                           //
//       THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF      //
//        Agfa HealthCare N.V. and/or its affiliates.        //
//      The copyright notice above does not evidence any     //
//     actual or intended publication of such source code.   //
//                                                           //
///////////////////////////////////////////////////////////////
//
package org.dcm4che3.imageio.plugins.dcm;

import org.dcm4che3.data.*;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.stream.BulkURIImageInputStream;
import org.dcm4che3.imageio.stream.ByteArrayImageInputStream;
import org.dcm4che3.io.stream.ImageInputStreamLoader;
import org.dcm4che3.io.stream.ServiceImageInputStreamLoader;
import org.dcm4che3.io.stream.DelegateImageInputStreamLoader;
import org.dcm4che3.imageio.stream.ImageInputStreamAdapter;
import org.dcm4che3.imageio.stream.SegmentedImageInputStream;
import org.dcm4che3.io.BulkDataDescriptor;
import org.dcm4che3.io.DicomInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.Path;

/**
 * Load the entire file into memory as BulkData.  Pixels are streamed from the BulkData URI to clients.  This class
 * sacrifices initial parse time for having a stateless class which is able to access the same file from different threads
 * without synchronization.
 *
 */
public class ParseEntireFileMetaData extends DicomMetaData {
    private static final Logger LOG = LoggerFactory.getLogger(ParseEntireFileMetaData.class);

    private static final ImageInputStreamLoader<Object> SERVICE_LOADER = new ServiceImageInputStreamLoader();

    private final Attributes fmi;
    private final Attributes attributes;
    private final URI uri;
    private final ImageInputStreamLoader<URI> uriLoader;

    private final Object pixelData;
    private final VR pixelDataVR;

    // IIS that was used to load this file.


    public ParseEntireFileMetaData(Object input) throws IOException {
        this.uri = toURI(input);


        String uriStr = this.uri.toString();
        ImageInputStream iis = SERVICE_LOADER.openStream(input);
        try (DicomInputStream dis = new DicomInputStream(new ImageInputStreamAdapter(iis))) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.URI);
            dis.setBulkDataDescriptor(BulkDataDescriptor.PIXELDATA);
            dis.setURI(uriStr);

            this.fmi = dis.getFileMetaInformation();
            this.attributes = dis.readDataset(-1,-1);
            this.pixelDataVR = attributes.getVR(Tag.PixelData);
            this.pixelData = attributes.remove(Tag.PixelData);
        }
        finally {
            if(uriStr.startsWith("java:iis")) {
                LOG.debug("BulkData URIs are relative to the original IIS for {}",input);
                uriLoader = new DelegateImageInputStreamLoader(iis);
            }
            else {
                LOG.debug("BulkData URIs are created relative to {}",uriStr);
                iis.close();
                uriLoader = new ServiceImageInputStreamLoader<URI>();
            }
        }
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public Attributes getFileMetaInformation() {
        return this.fmi;
    }

    @Override
    public Attributes getAttributes() {
        return this.attributes;
    }

    @Override
    public ImageInputStream openPixelStream(int frameIndex) throws IOException {
        ImageInputStream iisOfFrame;
        boolean singleFrame = getAttributes().getInt(Tag.NumberOfFrames,1) == 1;

        // This can be a compressed or uncompressed frame
        // Use the BulkData to determine where it is, and then return the segmented value.
        // Can be byte[] or Fragment or BulkData
        if(pixelData instanceof Fragments) {
            // Encapsulated (compressed) data
            ImageInputStream iisOfFile = uriLoader.openStream(this.uri);
            iisOfFrame = new SegmentedImageInputStream(iisOfFile,(Fragments)pixelData, frameIndex);
        }
        else {
            // Uncompressed data stored directly into the PixelData tag.
            PhotometricInterpretation pmi = getPhotometricInterpretation();
            int frameLength = pmi.frameLength(getColumns(), getRows(), getSamplesPerPixel(), getBitsAllocated());
            long offset  = frameLength * frameIndex;

            ImageInputStream iisOfPixelData;
            if(pixelData instanceof BulkData) {
                BulkData bulkData = (BulkData) pixelData;
                iisOfPixelData = this.uriLoader.openStream(URI.create(bulkData.getURI()));
                offset += bulkData.offset();
            }
            else if (pixelData instanceof byte[]){
                // replace with a byte[] reader
                iisOfPixelData = new ByteArrayImageInputStream((byte[])pixelData);
            }
            else {
                throw new IllegalArgumentException("Unexpected pixel data type "+pixelData.getClass());
            }

            iisOfFrame = new BulkURIImageInputStream(iisOfPixelData, offset, frameLength);
        }

        iisOfFrame.setByteOrder(getAttributes().bigEndian()
                ? ByteOrder.BIG_ENDIAN
                : ByteOrder.LITTLE_ENDIAN);

        return iisOfFrame;
    }

    @Override
    public boolean containsPixelData() {
        return pixelData != null;
    }

    @Override
    public VR getPixelDataVR() {
        return pixelDataVR;
    }


    /**
     * Define how the URI is defined in the code.
     */
    protected static URI toURI(Object input) {
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
}
