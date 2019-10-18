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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.ColorModelFactory;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.TransferSyntaxType;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.net.URI;

/**
 * Interface that provides basic methods for accessing DICOM attribute data.
 * @author Andrew Cowan (andrew.cowan@agfa.com)
 */
public interface DicomAccessor {

    /**
     * Get the base URI of the underlying DICOM data.
     */
    URI getURI();

    /**
     * Get the File Meta Information (FMI) header attributes.
     * @return FMI attributes from the file.
     */
    Attributes getFileMetaInformation();

    /**
     * Get the header attributes for the instance.  This will not contain the PixelData tag,
     * the {@link #openPixelStream(int)} method should be used to access pixel data.
     * @return Header attributes for this instance.
     */
    Attributes getAttributes();

    /**
     * Open an ImageInputStream which is partitioned to view only the pixels for the indicated frameIndex.
     * @param frameIndex 0 based index of the frame to view.
     * @throws IOException if there was a failure in the underlying data source when streaming the data
     * @throws IndexOutOfBoundsException if the frameIndex is less than 0 or greater than NumberOfFrames-1
     * @throws org.dcm4che3.error.TruncatedPixelDataException if there were fewer bytes streamed than expected
     * @throws org.dcm4che3.error.UnavailableFrameException if the frame requested is valid but does not exist in the instance.
     * @throws org.dcm4che3.error.NoPixelDataException  if this instance does not have a pixel data tag.
     * @return An open ImageInputStream.
     */
    ImageInputStream openPixelStream(int frameIndex) throws IOException;

    /**
     * Indicate whether pixel data is avilable for this instance.
     */
    boolean containsPixelData();

    /**
     * Get the VR of the pixel data tag.
     */
    VR getPixelDataVR();

    /**
     * Count the number of frames in the underlying instance.  This method may need to read
     * through to the underlying data source to determine the actual length of the data.
     * Results may be cached after the first call.
     */
    int countFrames() throws IOException;

    default String getSOPInstanceUID() {return getAttributes().getString(Tag.SOPInstanceUID);}
    default String getSOPClassUID() {return getAttributes().getString(Tag.SOPClassUID);}
    default String getTransferSyntax() {return getFileMetaInformation().getString(Tag.TransferSyntaxUID);}
    default TransferSyntaxType getTransferSyntaxType() {
        return TransferSyntaxType.forUID(getTransferSyntax());
    }
    default int getColumns() {
        return getAttributes().getInt(Tag.Columns, 0);
    }
    default int getRows() {
        return getAttributes().getInt(Tag.Rows, 0);
    }
    default int getSamplesPerPixel()  {
        return getAttributes().getInt(Tag.SamplesPerPixel, 1);
    }
    default int getBitsAllocated() {
        return getAttributes().getInt(Tag.BitsAllocated, 8);
    }
    default int getBitsStored() {
        return getAttributes().getInt(Tag.BitsStored, getBitsAllocated());
    }
    default boolean isMonochrome() {
        return ColorModelFactory.isMonochrome(getAttributes());
    }
    default PhotometricInterpretation getPhotometricInterpretation() { return PhotometricInterpretation.fromString(getAttributes().getString(Tag.PhotometricInterpretation, "MONOCHROME2")); }
    default int getNumberOfFrames() { return getAttributes().getInt(Tag.NumberOfFrames, 1); }
    default boolean isSingleFrame() { return getNumberOfFrames() == 1; }
    default int getPlanarConfiguration() { return getAttributes().getInt(Tag.PlanarConfiguration, 0); }
    default boolean isBanded() {return getSamplesPerPixel() > 1 && getPlanarConfiguration() != 0;}
    default int getDataType() { return getBitsAllocated() <= 8 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;}
}
