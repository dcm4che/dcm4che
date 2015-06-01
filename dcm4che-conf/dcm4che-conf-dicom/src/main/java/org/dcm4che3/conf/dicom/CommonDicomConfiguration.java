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

import org.dcm4che3.audit.EventID;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.api.*;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.adapters.NullToNullDecorator;
import org.dcm4che3.conf.core.api.*;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.util.ConfigNodeUtil;
import org.dcm4che3.conf.dicom.adapters.*;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.*;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.*;

/**
 * @author Roman K
 */
public class CommonDicomConfiguration implements DicomConfigurationManager, TransferCapabilityConfigExtension {

    private static final Logger LOG =
            LoggerFactory.getLogger(CommonDicomConfiguration.class);

    Configuration config;
    BeanVitalizer vitalizer;
    private final Collection<Class<? extends DeviceExtension>> deviceExtensionClasses;
    private final Collection<Class<? extends AEExtension>> aeExtensionClasses;

    /**
     * Needed for avoiding infinite loops when dealing with extensions containing circular references
     * e.g., one device extension references another device which has an extension that references the former device.
     * Devices that have been created but not fully loaded are added to this threadlocal. See findDevice.
     */
    private ThreadLocal<Map<String, Device>> currentlyLoadedDevicesLocal = new ThreadLocal<Map<String, Device>>();

    public CommonDicomConfiguration(Configuration config) {
        this.config = config;

        deviceExtensionClasses = new ArrayList<Class<? extends DeviceExtension>>();
        aeExtensionClasses = new ArrayList<Class<? extends AEExtension>>();
    }


    public CommonDicomConfiguration(Configuration configurationStorage, Collection<Class<? extends DeviceExtension>> deviceExtensionClasses, Collection<Class<? extends AEExtension>> aeExtensionClasses) {
        this.config = configurationStorage;
        this.vitalizer = new DefaultBeanVitalizer();
        this.deviceExtensionClasses = deviceExtensionClasses;
        this.aeExtensionClasses = aeExtensionClasses;

        // register reference handler
        this.vitalizer.setReferenceTypeAdapter(new NullToNullDecorator(new DicomReferenceHandlerAdapter(this.vitalizer, configurationStorage)));

        // register DICOM type adapters
        this.vitalizer.registerCustomConfigTypeAdapter(AttributesFormat.class, new NullToNullDecorator(new AttributeFormatTypeAdapter()));
        this.vitalizer.registerCustomConfigTypeAdapter(Code.class, new NullToNullDecorator(new CodeTypeAdapter()));
        this.vitalizer.registerCustomConfigTypeAdapter(Issuer.class, new NullToNullDecorator(new IssuerTypeAdapter()));
        this.vitalizer.registerCustomConfigTypeAdapter(ValueSelector.class, new NullToNullDecorator(new ValueSelectorTypeAdapter()));
        this.vitalizer.registerCustomConfigTypeAdapter(Property.class, new NullToNullDecorator(new PropertyTypeAdapter()));

        // register audit log type adapters
        this.vitalizer.registerCustomConfigTypeAdapter(EventTypeCode.class, new NullToNullDecorator(new AuditSimpleTypeAdapters.EventTypeCodeAdapter()));
        this.vitalizer.registerCustomConfigTypeAdapter(EventID.class, new NullToNullDecorator(new AuditSimpleTypeAdapters.EventIDTypeAdapter()));
        this.vitalizer.registerCustomConfigTypeAdapter(RoleIDCode.class, new NullToNullDecorator(new AuditSimpleTypeAdapters.RoleIDCodeTypeAdapter()));

        // register DicomConfiguration context
        this.vitalizer.registerContext(DicomConfiguration.class, this);


        // quick init
        try {
            if (!configurationExists()) {
                config.persistNode(DicomPath.ConfigRoot.path(), createInitialConfigRootNode(), DicomConfigurationRootNode.class);

            }
        } catch (ConfigurationException e) {
            throw new RuntimeException("Dicom configuration cannot be initialized", e);
        }
    }

    protected HashMap<String, Object> createInitialConfigRootNode() {
        HashMap<String, Object> rootNode = new HashMap<String, Object>();
        rootNode.put("dicomDevicesRoot", new HashMap<String, Object>());
        rootNode.put("dicomUniqueAETitlesRegistryRoot", new HashMap<String, Object>());
        return rootNode;
    }

    @Override
    public boolean configurationExists() throws ConfigurationException {
        return config.nodeExists(DicomPath.ConfigRoot.path());
    }

    public boolean purgeConfiguration() throws ConfigurationException {
        if (!configurationExists()) return false;
        config.persistNode(DicomPath.ConfigRoot.path(), new HashMap<String, Object>(), DicomConfigurationRootNode.class);
        return true;
    }

    @LDAP(objectClasses = "hl7UniqueApplicationName", distinguishingField = "hl7ApplicationName")
    @ConfigurableClass
    static class HL7UniqueAppRegistryItem {

        @ConfigurableProperty(name = "hl7ApplicationName")
        String name;


        public HL7UniqueAppRegistryItem() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @LDAP(objectClasses = "dicomUniqueAETitle", distinguishingField = "dicomAETitle")
    @ConfigurableClass
    public static class AETitleItem {
        public AETitleItem(String aeTitle) {
            this.aeTitle = aeTitle;
        }

        @ConfigurableProperty(name = "dicomAETitle")
        String aeTitle;

        public String getAeTitle() {
            return aeTitle;
        }


        public void setAeTitle(String aeTitle) {
            this.aeTitle = aeTitle;
        }

    }

    @LDAP(objectClasses = "dicomConfigurationRoot")
    @ConfigurableClass
    public static class DicomConfigurationRootNode {

        @LDAP(
                overriddenName = "Devices",
                objectClasses = "dicomDevicesRoot"
        )
        @ConfigurableProperty(name = "dicomDevicesRoot")
        Map<String, Device> devices;

        @LDAP(
                overriddenName = "Unique AE Titles Registry",
                objectClasses = "dicomUniqueAETitlesRegistryRoot"
        )
        @ConfigurableProperty(name = "dicomUniqueAETitlesRegistryRoot")
        Map<String, AETitleItem> uniqueAETitleRegistry;

        @LDAP(
                overriddenName = "Unique HL7 Application Names Registry",
                objectClasses = "hl7UniqueApplicationNamesRegistryRoot"
        )
        @ConfigurableProperty(name = "hl7UniqueApplicationNamesRegistryRoot")
        Map<String, HL7UniqueAppRegistryItem> hl7UniqueApplicationNamesRegistry;

        public Map<String, Device> getDevices() {
            return devices;
        }

        public void setDevices(Map<String, Device> devices) {
            this.devices = devices;
        }

        public Map<String, AETitleItem> getUniqueAETitleRegistry() {
            return uniqueAETitleRegistry;
        }

        public void setUniqueAETitleRegistry(Map<String, AETitleItem> uniqueAETitleRegistry) {
            this.uniqueAETitleRegistry = uniqueAETitleRegistry;
        }

        public Map<String, HL7UniqueAppRegistryItem> getHl7UniqueApplicationNamesRegistry() {
            return hl7UniqueApplicationNamesRegistry;
        }

        public void setHl7UniqueApplicationNamesRegistry(Map<String, HL7UniqueAppRegistryItem> hl7UniqueApplicationNamesRegistry) {
            this.hl7UniqueApplicationNamesRegistry = hl7UniqueApplicationNamesRegistry;
        }
    }

    @Override
    public boolean registerAETitle(String aet) throws ConfigurationException {

        final String path = DicomPath.UniqueAETByName.set("aeName", aet).path();
        if (config.nodeExists(path)) return false;

        final HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("dicomAETitle", aet);

        config.persistNode(path, map, AETitleItem.class);
        return true;

    }


    @Override
    public void unregisterAETitle(String aet) throws ConfigurationException {
        config.removeNode(DicomPath.UniqueAETByName.set("aeName", aet).path());
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet) throws ConfigurationException {

        if (aet == null) throw new IllegalArgumentException("Requested AE's title cannot be null");

        Iterator search = config.search(DicomPath.DeviceNameByAEName.set("aeName", aet).path());

        try {
            String deviceNameNode = (String) search.next();
            if (search.hasNext())
                LOG.warn("Application entity title '{}' is not unique. Check the configuration!", aet);
            Device device = findDevice(deviceNameNode);

            ApplicationEntity ae = device.getApplicationEntitiesMap().get(aet);
            if (ae == null) throw new NoSuchElementException("Unexpected error");
            return ae;

        } catch (NoSuchElementException e) {
            throw new ConfigurationNotFoundException("AE '" + aet + "' not found", e);
        }
    }

    @Override
    public Device findDevice(String name) throws ConfigurationException {
        if (name == null) throw new IllegalArgumentException("Requested device name cannot be null");

        // get the device cache for this loading phase
        Map<String, Device> deviceCache = currentlyLoadedDevicesLocal.get();

        // if there is none, create one for the current thread and remember that it should be cleaned up when the device is loaded
        boolean doCleanUpCache = false;
        if (deviceCache == null) {
            doCleanUpCache = true;
            deviceCache = new HashMap<String, Device>();
            currentlyLoadedDevicesLocal.set(deviceCache);
        }

        // if a requested device is already being (was) loaded, do not load it again, just return existing Device object
        if (deviceCache.containsKey(name))
            return deviceCache.get(name);

        try {

            Device device;
            try {
                Object deviceConfigurationNode = config.getConfigurationNode(deviceRef(name), Device.class);
                device = vitalizeDevice(name, deviceCache, deviceConfigurationNode);
            } catch (Exception e) {
                throw new ConfigurationException("Configuration for device " + name + " cannot be loaded", e);
            }

            if (device == null) throw new ConfigurationNotFoundException("Device " + name + " not found");
            return device;

        } finally {
            // if this loadDevice call initialized the cache, then clean it up
            if (doCleanUpCache) currentlyLoadedDevicesLocal.remove();
        }
    }

    protected Device vitalizeDevice(String name, Map<String, Device> deviceCache, Object deviceConfigurationNode) throws ConfigurationException {
        if (deviceConfigurationNode == null) return null;

        Device device = new Device();
        deviceCache.put(name, device);

        vitalizer.configureInstance(device, (Map<String, Object>) deviceConfigurationNode, Device.class);

        // add device extensions
        for (Class<? extends DeviceExtension> deviceExtensionClass : deviceExtensionClasses) {

            String deviceExtensionPath = DicomPath.DeviceExtension.
                    set("extensionName", deviceExtensionClass.getSimpleName()).
                    path();

            Map<String, Object> deviceExtensionNode = (Map<String, Object>) ConfigNodeUtil.getNode(deviceConfigurationNode, deviceExtensionPath);
            if (deviceExtensionNode != null) {
                DeviceExtension ext = vitalizer.newInstance(deviceExtensionClass);
                // add extension before vitalizing it, so the device field is accessible for use in setters
                device.addDeviceExtension(ext);
                vitalizer.configureInstance(ext, deviceExtensionNode, deviceExtensionClass);
            }
        }

        // add ae extensions
        for (Map.Entry<String, ApplicationEntity> entry : device.getApplicationEntitiesMap().entrySet()) {
            String aeTitle = entry.getKey();
            ApplicationEntity ae = entry.getValue();
            for (Class<? extends AEExtension> aeExtensionClass : aeExtensionClasses) {

                String aeExtPath = DicomPath.AEExtension.
                        set("aeName", aeTitle).
                        set("extensionName", aeExtensionClass.getSimpleName()).
                        path();

                Object aeExtNode = (Map<String, Object>) ConfigNodeUtil.getNode(deviceConfigurationNode, aeExtPath);
                if (aeExtNode != null) {
                    AEExtension ext = vitalizer.newInstance(aeExtensionClass);
                    // add extension before vitalizing it, so the device field is accessible for use in setters
                    ae.addAEExtension(ext);
                    vitalizer.configureInstance(ext, (Map<String, Object>) aeExtNode, aeExtensionClass);
                }
            }
        }

        // perform alternative TC init in case an extension is present
        new AlternativeTCLoader(this).initGroupBasedTCs(device);

        return device;
    }

    @Override
    public DeviceInfo[] listDeviceInfos(DeviceInfo keys) throws ConfigurationException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String[] listDeviceNames() throws ConfigurationException {
        Iterator search = config.search(DicomPath.AllDeviceNames.path());
        List<String> deviceNames = null;
        try {
            deviceNames = new ArrayList<String>();
            while (search.hasNext())
                deviceNames.add((String) search.next());
        } catch (Exception e) {
            throw new ConfigurationException("Error while getting list of device names", e);
        }
        return deviceNames.toArray(new String[deviceNames.size()]);
    }

    @Override
    public String[] listRegisteredAETitles() throws ConfigurationException {
        List<String> aeNames = new ArrayList<String>();
        try {
            Iterator search = config.search(DicomPath.AllAETitles.path());
            while (search.hasNext())
                aeNames.add((String) search.next());
        } catch (Exception e) {
            throw new ConfigurationException("Error while getting the list of registered AE titles", e);
        }
        return aeNames.toArray(new String[aeNames.size()]);
    }

    @Override
    public void persist(Device device) throws ConfigurationException {
        if (config.nodeExists(deviceRef(device.getDeviceName())))
            throw new ConfigurationAlreadyExistsException("Device " + device.getDeviceName() + " already exists");
        // otherwise it is the same as merge
        merge(device);
    }

    @Override
    public void merge(Device device) throws ConfigurationException {
        Map<String, Object> configNode = createDeviceConfigNode(device);
        config.persistNode(deviceRef(device.getDeviceName()), configNode, Device.class);
    }

    protected Map<String, Object> createDeviceConfigNode(Device device) throws ConfigurationException {

        final Map<String, Object> deviceConfigNode = vitalizer.createConfigNodeFromInstance(device, Device.class);


        // populate AEExtensions
        for (Map.Entry<String, ApplicationEntity> entry : device.getApplicationEntitiesMap().entrySet()) {

            ApplicationEntity ae = entry.getValue();

            for (Class<? extends AEExtension> aeExtensionClass : aeExtensionClasses) {
                AEExtension aeExtension = ae.getAEExtension(aeExtensionClass);
                if (aeExtension == null) continue;
                Map<String, Object> aeExtNode = vitalizer.createConfigNodeFromInstance(aeExtension, aeExtensionClass);

                String aeExtensionPath = DicomPath.AEExtension.
                        set("aeName", ae.getAETitle()).
                        set("extensionName", aeExtensionClass.getSimpleName()).
                        path();

                ConfigNodeUtil.replaceNode(deviceConfigNode, aeExtensionPath, aeExtNode);
            }
        }

        // populate DeviceExtensions
        for (Class<? extends DeviceExtension> deviceExtensionClass : deviceExtensionClasses) {
            final DeviceExtension deviceExtension = device.getDeviceExtension(deviceExtensionClass);
            final String extensionPath =
                    DicomPath.DeviceExtension.
                            set("extensionName", deviceExtensionClass.getSimpleName()).
                            path();

            if (deviceExtension != null)
                ConfigNodeUtil.replaceNode(deviceConfigNode, extensionPath, vitalizer.createConfigNodeFromInstance(deviceExtension, deviceExtensionClass));
        }

        // wipe out TCs in case an extension is present
        new AlternativeTCLoader(this).cleanUpTransferCapabilitiesInDeviceNode(device, deviceConfigNode);


        return deviceConfigNode;
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        config.removeNode(deviceRef(name));
    }


    @Override
    public String deviceRef(String name) {
        return DicomPath.DeviceByName.set("deviceName", name).path();
    }

    @Override
    public void persistCertificates(String ref, X509Certificate... certs) throws ConfigurationException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void removeCertificates(String ref) throws ConfigurationException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public X509Certificate[] findCertificates(String dn) throws ConfigurationException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void close() {

    }

    @Override
    public void sync() throws ConfigurationException {
        config.refreshNode(DicomPath.ConfigRoot.path());
    }

    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {
        try {
            return (T) this;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Cannot find a configuration extension for class " + clazz.getName());
        }
    }

    @Override
    public Collection<Class<? extends DeviceExtension>> getRegisteredDeviceExtensions() {
        return Collections.unmodifiableCollection(deviceExtensionClasses);
    }

    @Override
    public Collection<Class<? extends AEExtension>> getRegisteredAEExtensions() {
        return Collections.unmodifiableCollection(aeExtensionClasses);
    }

    @Override
    public BeanVitalizer getVitalizer() {
        return vitalizer;
    }


    @Override
    public Configuration getConfigurationStorage() {
        return config;
    }

    @Override
    public List<Class> getExtensionClasses() {

        List<Class> list = new ArrayList<Class>();

        list.addAll(deviceExtensionClasses);
        list.addAll(aeExtensionClasses);

        return list;
    }

    @Override
    public void persistTransferCapabilityConfig(TCConfiguration tcConfig) throws ConfigurationException {
        Map<String, Object> configNode = vitalizer.createConfigNodeFromInstance(tcConfig);
        config.persistNode(DicomPath.TCGroups.path(), configNode, TCConfiguration.class);
    }

    @Override
    public TCConfiguration getTransferCapabilityConfig() throws ConfigurationException {
        Map<String, Object> configurationNode = (Map<String, Object>) config.getConfigurationNode(DicomPath.TCGroups.path(), TCConfiguration.class);

        if (configurationNode == null)
            return new TCConfiguration();

        return vitalizer.newConfiguredInstance(
                configurationNode,
                TCConfiguration.class);
    }
}

