/*
 * **** BEGIN LICENSE BLOCK *****
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
 *  Portions created by the Initial Developer are Copyright (C) 2014
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
package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.dcm4che3.net.hl7.HL7DeviceExtension;

import java.util.*;

public class CommonDicomConfigurationWithHL7 extends CommonDicomConfiguration implements HL7Configuration {


    public CommonDicomConfigurationWithHL7(Configuration configurationStorage, Map<Class, List<Class>> extensionsByClass) {
        super(configurationStorage, extensionsByClass);
    }


    @Override
    public boolean registerHL7Application(String name) throws ConfigurationException {
        return true;
    }

    @Override
    public void unregisterHL7Application(String name) throws ConfigurationException {
    }

    @Override
    public HL7Application findHL7Application(String name) throws ConfigurationException {
        String pathForDeviceName = DicomPath.DeviceNameByHL7AppName.set("hl7AppName", name).path();

        try {
            Iterator search = config.search(pathForDeviceName);
            String deviceName = (String) search.next();

            Device device = findDevice(deviceName);

            return device.getDeviceExtension(HL7DeviceExtension.class).getHL7Application(name);
        } catch (NoSuchElementException e) {
            throw new ConfigurationException("HL7 app with name '" + name + "' not found", e);
        } catch (Exception e) {
            throw new ConfigurationException("Error while searching for HL7 app with name '" + name + "'", e);
        }
    }

    @Override
    public String[] listRegisteredHL7ApplicationNames() throws ConfigurationException {
        String hl7NamesPath = DicomPath.AllHL7AppNames.path();
        List<String> list = new ArrayList<String>();
        try {
            Iterator search = config.search(hl7NamesPath);
            while (search.hasNext())
                list.add((String) search.next());
        } catch (Exception e) {
            throw new ConfigurationException("Error while getting a list of HL7 app names", e);
        }
        return list.toArray(new String[list.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {
        // workaround for Weld, if we just return 'this' - it will replace it with a proxy
        if (clazz.equals(HL7Configuration.class)) {
            return (T) new HL7Configuration() {
                @Override
                public boolean registerHL7Application(String name) throws ConfigurationException {
                    return CommonDicomConfigurationWithHL7.this.registerHL7Application(name);
                }

                @Override
                public void unregisterHL7Application(String name) throws ConfigurationException {
                    CommonDicomConfigurationWithHL7.this.unregisterHL7Application(name);
                }

                @Override
                public HL7Application findHL7Application(String name) throws ConfigurationException {
                    return CommonDicomConfigurationWithHL7.this.findHL7Application(name);
                }

                @Override
                public String[] listRegisteredHL7ApplicationNames() throws ConfigurationException {
                    return CommonDicomConfigurationWithHL7.this.listRegisteredHL7ApplicationNames();
                }
            };
        }

        return super.getDicomConfigurationExtension(clazz);
    }
}
