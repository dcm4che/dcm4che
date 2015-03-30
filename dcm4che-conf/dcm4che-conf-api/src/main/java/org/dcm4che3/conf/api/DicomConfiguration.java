package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;

/**
 * This interface should be used by vendors/integrators to access and manipulate the DICOM configuration in a type-safe way.
 * Always give the preference to this interface when possible.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Roman K
 */
public interface DicomConfiguration {

    /**
     * Looks up an application entity by name
     * @param aet application entity name
     * @return
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    ApplicationEntity findApplicationEntity(String aet) throws ConfigurationException;

    /**
     * Looks up a device by name
     * @param name device name
     * @return
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    Device findDevice(String name) throws ConfigurationException;

    /**
     * Stores the full configuration of a device in the configuration backend.
     * @param device Device to store
     * @throws ConfigurationAlreadyExistsException When a device with such name already exists.
     * @throws ConfigurationException When an error occured during the operation
     */
    void persist(Device device) throws ConfigurationException;

    /**
     * Replaces the full configuration of a device in the configuration backend with the configuration of
     * the provided device. The behavior is similar to JPA's merge.
     * @param device Device to merge
     * @throws ConfigurationException When an error occured during the operation
     */
    void merge(Device device) throws ConfigurationException;

    /**
     * Removes the device and its configuration from the configuration storage fully.
     * @param name
     * @throws ConfigurationException
     */
    void removeDevice(String name) throws ConfigurationException;

    /**
     * Returns all device names from the configuration backend
     * @return Device names
     * @throws ConfigurationException
     */
    String[] listDeviceNames() throws ConfigurationException;

    /**
     * Invalidates any present cached state for the configuration storage view of the client.
     * There is no guarantee whether the devices accessed afterwards will be re-loaded lazily or eagerly.
     *
     * Has no effect for non-cached or consistently-cached configuration storage backends.
     */
    void sync() throws ConfigurationException;

}
