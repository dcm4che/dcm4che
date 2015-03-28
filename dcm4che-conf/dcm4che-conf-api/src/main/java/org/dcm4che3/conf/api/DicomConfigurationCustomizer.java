package org.dcm4che3.conf.api;

import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;

/**
 * Vendors may implement this interface to perform necessary modification to the configuration bootstrap process, e.g., set custom storage, add custom extensions.
 */
public interface DicomConfigurationCustomizer {

    /**
     * Should perferm necessary modification to the configuration bootstrap, e.g., set custom storage, add custom extensions.
     * @param builder
     */
    public void customize(DicomConfigurationBuilder builder);

}
