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
 * Portions created by the Initial Developer are Copyright (C) 2015
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
package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;

import java.util.List;

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
     * Looks up an application entity by UUID
     * @param uuid UUID
     * @return
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    ApplicationEntity findApplicationEntityByUUID(String uuid) throws ConfigurationException;

    /**
     * Looks up a device by UUID
     * @param uuid UUID
     * @return
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    Device findDeviceByUUID(String uuid) throws ConfigurationException;

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
     * Returns all AE names from all devices from the configuration backend
     * @return AE names
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    List<String> listAllAETitles() throws ConfigurationException;

    /**
     * Invalidates any present cached state for the configuration storage view of the client.
     * There is no guarantee whether the devices accessed afterwards will be re-loaded lazily or eagerly.
     *
     * Has no effect for non-cached or consistently-cached configuration storage backends.
     */
    void sync() throws ConfigurationException;


    /**
     * Get an extension of configuration
     * @param clazz Extension class
     * @param <T>
     * @throws IllegalArgumentException - in case an extension of specified class is nto found
     * @return
     */
    <T> T getDicomConfigurationExtension(Class<T> clazz);
    
    /**
     * Provides support for batching configuration changes.
     * </p>
     * It is guaranteed that
     * <ul>
     * <li>All configuration reads and writes in a batch are performed within a single transaction, i.e. the changes are atomic and performed in READ COMMITTED isolation</li>
     * <li>When this method is called, an ongoing transaction (in case there is one) will be SUSPENDED. The batch will ALWAYS be executed in a SEPARATE transaction.</li>
     * <li>At most ONE batch is executed at the same time in the whole cluster (synchronized with a database lock)</li>
     * </ul>
     * 
     * @param dicomConfigBatch Configuration batch to execute
     */
    void runBatch(DicomConfigBatch dicomConfigBatch);
    
    /**
     * Defines a configuration batch that allows to execute configuration changes in a bulk-type manner.
     * 
     * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
     */
    interface DicomConfigBatch {
        /**
         * Executes configuration batch changes on the specified DICOM configuration.
         */
        void run();
    }
    
}
