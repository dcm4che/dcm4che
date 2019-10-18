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
import org.dcm4che3.error.NoPixelDataException;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.stream.ByteArrayImageInputStream;
import org.dcm4che3.imageio.stream.SegmentedImageInputStream;
import org.dcm4che3.io.stream.BulkURIImageInputStream;
import org.dcm4che3.io.stream.ImageInputStreamLoader;
import org.dcm4che3.io.stream.ServiceImageInputStreamLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;

/**
 * Abstract interface for handling a bulk data based DicomMetadata object.
 */
public class BulkDataMetaData extends DicomMetaData {
    private static Logger LOG = LoggerFactory.getLogger(BulkDataMetaData.class);

    protected final ImageInputStreamLoader uriLoader;
    protected final URI uri;

    private final Attributes fmi;
    private final Attributes attributes;

    private final Object pixelData;
    private final VR pixelDataVR;

    public BulkDataMetaData(URI uri, DatasetWithFMI datasetWithFMI) throws IOException {
        this(uri, datasetWithFMI, new ServiceImageInputStreamLoader());
    }

    public BulkDataMetaData(URI uri,  DatasetWithFMI datasetWithFMI, ImageInputStreamLoader loader) throws IOException {
        this.uri = uri;
        this.uriLoader = loader;

        this.fmi = datasetWithFMI.getFileMetaInformation();
        this.attributes = datasetWithFMI.getDataset();
        this.pixelDataVR = attributes.getVR(Tag.PixelData);
        this.pixelData = attributes.remove(Tag.PixelData);
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
    public boolean containsPixelData() {
        return pixelData != null;
    }

    @Override
    public VR getPixelDataVR() {
        return pixelDataVR;
    }

    @Override
    public int countFrames() throws IOException {
        int frames = 0;
        if(containsPixelData()) {
            if(pixelData instanceof Fragments) {
                Fragments fragments = (Fragments) pixelData;
                frames = fragments.size() - 1;
            }
            else {
                frames = 1;
            }
        }

        return frames;
    }

    @Override
    public ImageInputStream openPixelStream(int frameIndex) throws IOException {
        if(!containsPixelData()) {
            throw new NoPixelDataException("No PixelData defined for "+getSOPInstanceUID());
        }

        ImageInputStream iisOfFrame;

        // This can be a compressed or uncompressed frame
        // Use the BulkData to determine where it is, and then return the segmented value.
        // Can be byte[] or Fragment or BulkData
        if(pixelData instanceof Fragments) {
            // Encapsulated (compressed) data

            if(isSingleFrame()) {
                frameIndex = -1;
            }

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
                iisOfPixelData = this.uriLoader.openStream(bulkData.toFileURI());
                offset += bulkData.offset();
            }
            else if (pixelData instanceof byte[]){
                // replace with a byte[] reader
                iisOfPixelData = new ByteArrayImageInputStream((byte[])pixelData);
            }
            else if( pixelData == Value.NULL) {
                throw new NoPixelDataException("No pixel data tag found in "+getURI());
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



}
