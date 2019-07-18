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

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Fragments;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.image.ColorModelFactory;
import org.dcm4che3.image.PhotometricInterpretation;
import org.dcm4che3.imageio.codec.TransferSyntaxType;

import javax.imageio.stream.ImageInputStream;
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
     * @throws IOException if the pixel data source could not be accessed
     * @throws IllegalArgumentException if there is no pixel data tag for this object.
     * @return An open ImageInputStream.
     */
    // Do we want to have a PixelDataMissingException that subclasses IOException?
    ImageInputStream openPixelStream(int frameIndex) throws IOException;

    /**
     * Indicate whether pixel data is avilable for this instance.
     */
    boolean containsPixelData();

    VR getPixelDataVR();


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
}
