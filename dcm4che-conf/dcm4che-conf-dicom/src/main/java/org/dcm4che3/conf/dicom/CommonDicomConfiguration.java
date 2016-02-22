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

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import org.dcm4che3.audit.EventID;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.api.TransferCapabilityConfigExtension;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.adapters.NullToNullDecorator;
import org.dcm4che3.conf.core.api.BatchRunner.Batch;
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.internal.ConfigurationManager;
import org.dcm4che3.conf.dicom.adapters.AttributeFormatTypeAdapter;
import org.dcm4che3.conf.dicom.adapters.AuditSimpleTypeAdapters;
import org.dcm4che3.conf.dicom.adapters.CodeTypeAdapter;
import org.dcm4che3.conf.dicom.adapters.DicomReferenceHandlerAdapter;
import org.dcm4che3.conf.dicom.adapters.IssuerTypeAdapter;
import org.dcm4che3.conf.dicom.adapters.PropertyTypeAdapter;
import org.dcm4che3.conf.dicom.adapters.ValueSelectorTypeAdapter;
import org.dcm4che3.data.Code;
import org.dcm4che3.data.Issuer;
import org.dcm4che3.data.ValueSelector;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceInfo;
import org.dcm4che3.util.AttributesFormat;
import org.dcm4che3.util.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roman K
 */
@SuppressWarnings("unchecked")
public class CommonDicomConfiguration implements DicomConfigurationManager, TransferCapabilityConfigExtension {

    private static final Logger log = LoggerFactory.getLogger(CommonDicomConfiguration.class);


    /**
     * see preventDeviceModifications(org.dcm4che3.net.Device)
     */
    private Map<Device, Object> readOnlyDevices = Collections.synchronizedMap(new WeakHashMap<Device, Object>());

    Configuration config;
    private BeanVitalizer vitalizer;

    private final Map<Class, List<Class>> extensionsByClass;

    /**
     * Returns a list of registered extensions for a specified base extension class
     *
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> List<Class<? extends T>> getExtensionClassesByBaseClass(Class<T> clazz) {
        List<Class> classes = extensionsByClass.get(clazz);

        List<Class<? extends T>> list = new ArrayList<Class<? extends T>>();

        if (classes != null)
            for (Class<?> aClass : classes) list.add((Class<? extends T>) aClass);

        return list;
    }

    public CommonDicomConfiguration(Configuration configurationStorage, Map<Class, List<Class>> extensionsByClass) {
        this.config = configurationStorage;
        this.extensionsByClass = extensionsByClass;


        DefaultBeanVitalizer defaultBeanVitalizer = createDefaultDicomVitalizer();

        // register reference handler
        defaultBeanVitalizer.setReferenceTypeAdapter(new NullToNullDecorator(new DicomReferenceHandlerAdapter(defaultBeanVitalizer, configurationStorage)));

        // register DicomConfiguration context
        defaultBeanVitalizer.registerContext(DicomConfiguration.class, this);
        defaultBeanVitalizer.registerContext(ConfigurationManager.class, this);


        this.vitalizer = defaultBeanVitalizer;

        // quick init
        try {
            if (!configurationExists()) {
                config.persistNode(DicomPath.ConfigRoot.path(), createInitialConfigRootNode(), null);

            }
        } catch (ConfigurationException e) {
            throw new RuntimeException("Dicom configuration cannot be initialized", e);
        }
    }

    public static DefaultBeanVitalizer createDefaultDicomVitalizer() {
        DefaultBeanVitalizer defaultBeanVitalizer = new DefaultBeanVitalizer();


        // register DICOM type adapters
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(AttributesFormat.class, new NullToNullDecorator(new AttributeFormatTypeAdapter()));
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(Code.class, new NullToNullDecorator(new CodeTypeAdapter()));
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(Issuer.class, new NullToNullDecorator(new IssuerTypeAdapter()));
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(ValueSelector.class, new NullToNullDecorator(new ValueSelectorTypeAdapter()));
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(Property.class, new NullToNullDecorator(new PropertyTypeAdapter()));

        // register audit log type adapters
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(EventTypeCode.class, new NullToNullDecorator(new AuditSimpleTypeAdapters.EventTypeCodeAdapter()));
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(EventID.class, new NullToNullDecorator(new AuditSimpleTypeAdapters.EventIDTypeAdapter()));
        defaultBeanVitalizer.registerCustomConfigTypeAdapter(RoleIDCode.class, new NullToNullDecorator(new AuditSimpleTypeAdapters.RoleIDCodeTypeAdapter()));

        return defaultBeanVitalizer;
    }

    protected HashMap<String, Object> createInitialConfigRootNode() {
        HashMap<String, Object> rootNode = new HashMap<String, Object>();
        rootNode.put("dicomDevicesRoot", new HashMap<String, Object>());
        Nodes.replaceNode(rootNode, new HashMap(), Nodes.fromSimpleEscapedPath(METADATA_ROOT_PATH));
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

    @Override
    public void preventDeviceModifications(Device d) {
        readOnlyDevices.put(d, true);
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
        return true;
    }


    @Override
    public void unregisterAETitle(String aet) throws ConfigurationException {
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet) throws ConfigurationException {

        if (aet == null) throw new IllegalArgumentException("Requested AE's title cannot be null");

        Iterator<?> search = config.search(DicomPath.DeviceNameByAEName.set("aeName", aet).path());

        if (!search.hasNext()) {
            search = config.search(DicomPath.DeviceNameByAENameAlias.set("aeNameAlias", aet).path());

            if (!search.hasNext())
                throw new ConfigurationNotFoundException("AE '" + aet + "' not found");
        }

        String deviceNameNode = (String) search.next();
        if (search.hasNext())
            log.warn("Application entity title '{}' is not unique. Check the configuration!", aet);
        Device device = findDevice(deviceNameNode);

        ApplicationEntity ae = device.getApplicationEntity(aet);
        if (ae == null)
            throw new NoSuchElementException("Unexpected error");
        return ae;

    }

    @Override
    public ApplicationEntity findApplicationEntityByUUID(String uuid) throws ConfigurationException {

        if (uuid == null) throw new IllegalArgumentException("Requested AE's uuid cannot be null");

        Iterator search = config.search(DicomPath.DeviceNameByAEUUID.set("aeUUID", uuid).path());

        try {
            String deviceNameNode = (String) search.next();
            Device device = findDevice(deviceNameNode);

            for (ApplicationEntity applicationEntity : device.getApplicationEntities()) {
                if (uuid.equals(applicationEntity.getUuid())) {
                    return applicationEntity;
                }
            }

            throw new NoSuchElementException("Unexpected error");

        } catch (NoSuchElementException e) {
            throw new ConfigurationNotFoundException("AE with UUID '" + uuid + "' not found", e);
        }
    }

    @Override
    public Device findDeviceByUUID(String uuid) throws ConfigurationException {
        if (uuid == null) throw new IllegalArgumentException("Requested Device's uuid cannot be null");

        Iterator search = config.search(DicomPath.DeviceNameByUUID.set("deviceUUID", uuid).path());

        try {
            String deviceNameNode = (String) search.next();
            return findDevice(deviceNameNode);
        } catch (NoSuchElementException e) {
            throw new ConfigurationNotFoundException("Device with UUID '" + uuid + "' not found", e);
        }
    }

    @Override
    public Device findDevice(String name) throws ConfigurationException {
        if (name == null) throw new IllegalArgumentException("Requested device name cannot be null");

        try {
            Object deviceConfigurationNode = config.getConfigurationNode(deviceRef(name), Device.class);
            if (deviceConfigurationNode == null)
                throw new ConfigurationNotFoundException("Device " + name + " not found");

            Device device = vitalizer.newConfiguredInstance((Map<String, Object>) deviceConfigurationNode, Device.class);

            // perform alternative TC init in case an extension is present
            new AlternativeTCLoader(this).initGroupBasedTCs(device);

            return device;
        } catch (ConfigurationNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ConfigurationException("Configuration for device " + name + " cannot be loaded", e);
        }
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

        if (readOnlyDevices.containsKey(device)) handleReadOnlyDeviceModification();

        if (device.getDeviceName() == null) throw new ConfigurationException("The name of the device must not be null");
        if (config.nodeExists(deviceRef(device.getDeviceName())))
            throw new ConfigurationAlreadyExistsException("Device " + device.getDeviceName() + " already exists");
        // otherwise it is the same as merge
        merge(device);
    }

    private void handleReadOnlyDeviceModification() {

        String message = "Persisting the config for a Device object that is marked as read-only. " +
                "This warning is not affecting the behavior for now, but soon it will be replaced with throwing an exception!" +
                "If you want to make config modifications, use a separate instance of Device! See CSP configuration docs for details.";

        // create exception to log the stacktrace
        ConfigurationException exception = new ConfigurationException();

        log.warn(message,exception);
    }

    @Override
    public void merge(Device device) throws ConfigurationException {

        if (readOnlyDevices.containsKey(device)) handleReadOnlyDeviceModification();

        if (device.getDeviceName() == null) throw new ConfigurationException("The name of the device must not be null");
        Map<String, Object> configNode = createDeviceConfigNode(device);
        config.persistNode(deviceRef(device.getDeviceName()), configNode, Device.class);
    }

    protected Map<String, Object> createDeviceConfigNode(Device device) throws ConfigurationException {

        final Map<String, Object> deviceConfigNode = vitalizer.createConfigNodeFromInstance(device, Device.class);

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
        return DicomPath.DeviceByNameForWrite.set("deviceName", name).path();
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
    public BeanVitalizer getVitalizer() {
        return vitalizer;
    }


    @Override
    public Configuration getConfigurationStorage() {
        return config;
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

    @Override
    public void runBatch(final DicomConfigBatch dicomConfigBatch) {
        /*
         * Use the batch support of underlying configuration storage to execute batch
         */
        config.runBatch(new Batch() {

            @Override
            public void run() {
                dicomConfigBatch.run();
            }

        });
    }

}

