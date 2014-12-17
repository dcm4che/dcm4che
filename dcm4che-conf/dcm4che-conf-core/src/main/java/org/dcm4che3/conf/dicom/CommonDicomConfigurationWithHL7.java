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

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.dcm4che3.net.hl7.HL7DeviceExtension;

import java.util.*;

public class CommonDicomConfigurationWithHL7 extends CommonDicomConfiguration implements HL7Configuration {
    public CommonDicomConfigurationWithHL7(Configuration configurationStorage, Collection<Class<? extends DeviceExtension>> deviceExtensionClasses, Collection<Class<? extends AEExtension>> aeExtensionClasses, Collection<Class<? extends HL7ApplicationExtension>> hl7ApplicationExtensionClasses) {
        super(configurationStorage, deviceExtensionClasses, aeExtensionClasses);
        this.hl7ApplicationExtensionClasses = hl7ApplicationExtensionClasses;
    }

    Collection<Class<? extends HL7ApplicationExtension>> hl7ApplicationExtensionClasses;


    @Override
    public boolean registerHL7Application(String name) throws ConfigurationException {

        String path = getHL7UniqueAppItemPath(name);

        if (config.nodeExists(path)) return false;

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("hl7ApplicationName", name);

        config.persistNode(path, map, HL7UniqueAppRegistryItem.class);

        return true;
    }

    @Override
    protected HashMap<String, Object> createInitialConfigRootNode() {
        HashMap<String, Object> rootNode = super.createInitialConfigRootNode();
        rootNode.put("hl7UniqueApplicationNamesRegistryRoot", new HashMap<String, Object>());
        return rootNode;
    }

    private String getHL7UniqueAppItemPath(String name) {
        return DicomPath.UniqueHL7AppByName.set("hl7AppName",name).path();
    }

    @Override
    public void unregisterHL7Application(String name) throws ConfigurationException {
        config.removeNode(getHL7UniqueAppItemPath(name));
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

    @Override
    protected Device loadDevice(String name, Map<String, Device> deviceCache) throws ConfigurationException {

        Device device = super.loadDevice(name, deviceCache);
        if (device == null) return null;

        // add exts
        HL7DeviceExtension hl7DeviceExtension = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7DeviceExtension == null) return device;

        Map<String, HL7Application> hl7apps = hl7DeviceExtension.getHl7apps();

        for (Map.Entry<String, HL7Application> hl7ApplicationEntry : hl7apps.entrySet()) {
            for (Class<? extends HL7ApplicationExtension> hl7ApplicationExtensionClass : hl7ApplicationExtensionClasses) {
                try {
                    String path = getPathForHL7AppExtension(device, hl7ApplicationEntry.getKey(), hl7ApplicationExtensionClass);
                    Object configurationNode = config.getConfigurationNode(path, hl7ApplicationExtensionClass);
                    if (configurationNode == null) continue;
                    HL7ApplicationExtension hl7ApplicationExtension = vitalizer.newInstance(hl7ApplicationExtensionClass);
                    // add extension before vitalizing, so the hl7app field is accessible for use in setters
                    hl7ApplicationEntry.getValue().addHL7ApplicationExtension(hl7ApplicationExtension);
                    vitalizer.configureInstance(hl7ApplicationExtension, (Map<String, Object>) configurationNode, hl7ApplicationExtensionClass);
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to load HL7 app extension '"+hl7ApplicationExtensionClass.getSimpleName()+"' for hl7app '"+hl7ApplicationEntry.getKey()+"'",e);
                }
            }
        }

        return device;
    }

    @Override
    public void merge(Device device) throws ConfigurationException {
        super.merge(device);

        // add exts
        HL7DeviceExtension hl7DeviceExtension = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7DeviceExtension == null) return;

        Map<String, HL7Application> hl7apps = hl7DeviceExtension.getHl7apps();

        for (Map.Entry<String, HL7Application> hl7ApplicationEntry : hl7apps.entrySet()) {
            for (Class<? extends HL7ApplicationExtension> hl7ApplicationExtensionClass : hl7ApplicationExtensionClasses) {
                try {
                    HL7ApplicationExtension hl7ApplicationExtension = hl7ApplicationEntry.getValue().getHL7ApplicationExtension(hl7ApplicationExtensionClass);
                    if (hl7ApplicationExtension == null) continue;

                    Map<String, Object> configNode = vitalizer.createConfigNodeFromInstance(hl7ApplicationExtension, hl7ApplicationExtensionClass);
                    String path = getPathForHL7AppExtension(device, hl7ApplicationEntry.getKey(), hl7ApplicationExtensionClass);
                    config.persistNode(path,configNode,hl7ApplicationExtensionClass);
                } catch (Exception e) {
                    throw new ConfigurationException("Failed to save HL7 app extension '"+hl7ApplicationExtensionClass.getSimpleName()+"' for hl7app '"+hl7ApplicationEntry.getKey()+"'",e);
                }
            }
        }


    }

    private String getPathForHL7AppExtension(Device device, String hl7AppName, Class<? extends HL7ApplicationExtension> hl7ApplicationExtensionClass) {
       return  DicomPath.HL7AppExtension.
               set("deviceName",device.getDeviceName()).
               set("hl7AppName", hl7AppName).
               set("extensionName", hl7ApplicationExtensionClass.getSimpleName())
               .path();
    }
}
