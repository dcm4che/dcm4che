/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.dicom.decorators;

import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.DelegatingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.dicom.DicomPath;
import org.dcm4che3.net.Device;

import java.util.Map;

/**
 * Performs validation of modifications and ensures referential integrity of configuration
 *
 * Current impl is a quick solution - persist/remove the node and try to reload all devices, if fails, revert the persisted node back
 * @author Roman K
 */
public class DicomValidatingConfigurationDecorator extends DelegatingConfiguration {

    private DicomConfigurationManager configurationManager;

    public DicomValidatingConfigurationDecorator(Configuration delegate, DicomConfigurationManager configurationManager) {
        super(delegate);
        this.configurationManager = configurationManager;
    }

    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        Object oldConfig = delegate.getConfigurationNode(path, configurableClass);

        delegate.persistNode(path, configNode, configurableClass);

        try {
            for (String deviceName : configurationManager.listDeviceNames())
                configurationManager.findDevice(deviceName);
        } catch (ConfigurationException e) {
            // validation failed, replace the node back
            delegate.persistNode(path, (Map<String, Object>) oldConfig, Device.class);
            throw e;
        }
    }

    @Override
    public void removeNode(String path) throws ConfigurationException {
        Object oldConfig = delegate.getConfigurationNode(path, Device.class);

        delegate.removeNode(path);

        try {
            for (String deviceName : configurationManager.listDeviceNames())
                configurationManager.findDevice(deviceName);
        } catch (ConfigurationException e) {
            // validation failed, replace the node back
            delegate.persistNode(path, (Map<String, Object>) oldConfig, Device.class);
            throw e;
        }
    }
}
