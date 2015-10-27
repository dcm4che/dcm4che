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

import org.dcm4che3.conf.core.util.PathPattern;

import java.util.HashMap;
import java.util.Map;

/**
 * Single source for all the DicomConfiguration-related xpaths
 *
 * @author Roman K
 */
public enum DicomPath {

    DeviceNameByAEName,
    DeviceNameByAENameAlias,
    AllDeviceNames,
    AllAETitles,
    DeviceNameByHL7AppName,
    AllHL7AppNames,
    UniqueAETByName,
    ConfigRoot,
    DeviceByName,
    AEExtension,
    DeviceExtension,
    HL7AppExtension,
    UniqueHL7AppByName,
    DeviceByNameRef,
    ConnectionByCnRef,
    ConnectionByHostRef,
    ConnectionByHostPortRef,
    TCGroups,
    AllTCsOfAllAEsWithTCGroupExt,
    AEByTitleRef,
    DeviceNameByAEUUID,
    DeviceNameByUUID;

    public static final Map<DicomPath, String> PATHS = new HashMap<DicomPath, String>();
    public static final Map<DicomPath, PathPattern> PATH_PATTERNS = new HashMap<DicomPath, PathPattern>();

    static {
        // search
        PATHS.put(/**************/AllAETitles, "/dicomConfigurationRoot/dicomDevicesRoot/*/dicomNetworkAE/*/dicomAETitle | /dicomConfigurationRoot/dicomDevicesRoot/*/dicomNetworkAE/*/dcmAETitleAliases");

        // Note: I tried combining DeviceNameByAEName and DeviceNameByAENameAlias into one XPath like this:
        // /dicomConfigurationRoot/dicomDevicesRoot/storescp[dicomNetworkAE[@name='{aeName}'] | dicomNetworkAE[*/dcmAETitleAliases='{aeName}']]/dicomDeviceName
        // but it didn't work for AEs that did not have aliases defined at all.
        // Strange thing, e.g. this seems to work: /dicomConfigurationRoot/dicomDevicesRoot/storescp[dicomNetworkAE[@name='STORESCP'] | dicomNetworkAE[*/dcmAETitleAliases='STORESCP']]/dicomDeviceName
        // but this doesn't: /dicomConfigurationRoot/dicomDevicesRoot/*[dicomNetworkAE[@name='STORESCP'] | dicomNetworkAE[*/dcmAETitleAliases='STORESCP']]/dicomDeviceName
        // (in the case that there is no alias defined at all for the AE STORESCP)

        PATHS.put(/*******/DeviceNameByAEName, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomNetworkAE[@name='{aeName}']]/dicomDeviceName");
        PATHS.put(/**/DeviceNameByAENameAlias, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomNetworkAE[*/dcmAETitleAliases='{aeNameAlias}']]/dicomDeviceName");

        PATHS.put(/***********/AllDeviceNames, "/dicomConfigurationRoot/dicomDevicesRoot/*/dicomDeviceName");
        PATHS.put(/***********/AllHL7AppNames, "/dicomConfigurationRoot/dicomDevicesRoot/*/deviceExtensions/HL7DeviceExtension/hl7Apps/*/hl7ApplicationName");
        PATHS.put(/***/DeviceNameByHL7AppName, "/dicomConfigurationRoot/dicomDevicesRoot/*[deviceExtensions/HL7DeviceExtension/hl7Apps/*[hl7ApplicationName='{hl7AppName}']]/dicomDeviceName");
        PATHS.put(/*******/DeviceNameByAEUUID, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomNetworkAE/*[_.uuid='{aeUUID}']]/dicomDeviceName");
        PATHS.put(/*********/DeviceNameByUUID, "/dicomConfigurationRoot/dicomDevicesRoot/*[_.uuid='{deviceUUID}']/dicomDeviceName");

        // single-result getNode (also can be used to store nodes)
        PATHS.put(/***************/ConfigRoot, "/dicomConfigurationRoot");
        PATHS.put(/*************/DeviceByName, "/dicomConfigurationRoot/dicomDevicesRoot[@name='{deviceName}']");
        PATHS.put(/**************/AEExtension, "/dicomNetworkAE[@name='{aeName}']/aeExtensions/{extensionName}");
        PATHS.put(/**********/DeviceExtension, "/deviceExtensions/{extensionName}");
        PATHS.put(/**********/HL7AppExtension, "/deviceExtensions/HL7DeviceExtension/hl7Apps[@name='{hl7AppName}']/hl7AppExtensions/{extensionName}");
        PATHS.put(/**********/UniqueAETByName, "/dicomConfigurationRoot/dicomUniqueAETitlesRegistryRoot[@name='{aeName}']");
        PATHS.put(/*******/UniqueHL7AppByName, "/dicomConfigurationRoot/hl7UniqueApplicationNamesRegistryRoot[@name='{hl7AppName}']");

        // references (parsable, will be stored this way)
        PATHS.put(/*************/AEByTitleRef, "/dicomConfigurationRoot/dicomDevicesRoot/*/dicomNetworkAE[dicomAETitle='{aeName}']");
        PATHS.put(/**********/DeviceByNameRef, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomDeviceName='{deviceName}']");
        PATHS.put(/********/ConnectionByCnRef, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomDeviceName='{deviceName}']/dicomConnection[cn='{cn}']");
        PATHS.put(/******/ConnectionByHostRef, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomDeviceName='{deviceName}']/dicomConnection[dicomHostname='{hostName}']");
        PATHS.put(/**/ConnectionByHostPortRef, "/dicomConfigurationRoot/dicomDevicesRoot/*[dicomDeviceName='{deviceName}']/dicomConnection[dicomHostname='{hostName}' and dicomPort='{port}']");


        // Transfer capabilities
        PATHS.put(/*****************/TCGroups, "/dicomConfigurationRoot/globalConfiguration/dcmTransferCapabilities");
        PATHS.put(AllTCsOfAllAEsWithTCGroupExt, "dicomNetworkAE/*[aeExtensions/TCGroupConfigAEExtension]/dcmTransferCapability");


        for (Map.Entry<DicomPath, String> entry : PATHS.entrySet()) {
            PATH_PATTERNS.put(entry.getKey(), new PathPattern(entry.getValue()));
        }
    }

    /**
     * Starts chaining
     *
     * @param paramName
     * @param value
     * @return
     */
    public PathPattern.PathCreator set(String paramName, String value) {
        return PATH_PATTERNS.get(this).set(paramName, value);
    }


    public PathPattern.PathParser parse(String path) {
        return PATH_PATTERNS.get(this).parse(path);
    }

    /**
     * Gets path as is
     *
     * @return
     */
    public String path() {
        return PATHS.get(this);
    }

}
