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
import org.dcm4che3.audit.ObjectFactory;
import org.dcm4che3.audit.RoleIDCode;
import org.dcm4che3.conf.api.*;
import org.dcm4che3.conf.api.internal.DicomConfigurationManager;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.DefaultTypeSafeConfiguration;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.adapters.NullToNullDecorator;
import org.dcm4che3.conf.core.api.BatchRunner.Batch;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.TypeSafeConfiguration;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.dicom.adapters.*;
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

import java.util.*;

/**
 * @author Roman K
 */
@SuppressWarnings("unchecked")
public class CommonDicomConfiguration implements DicomConfigurationManager, TransferCapabilityConfigExtension {

    private static final Logger log = LoggerFactory.getLogger(CommonDicomConfiguration.class);


    /**
     * see preventDeviceModifications(org.dcm4che3.net.Device)
     */
    private final Map<Device, Object> readOnlyDevices = Collections.synchronizedMap(new WeakHashMap<Device, Object>());

    protected final Configuration lowLevelConfig;
    private final BeanVitalizer vitalizer;

    private final Map<Class, List<Class>> extensionsByClass;
    private final TypeSafeConfiguration<DicomConfigurationRoot> config;
    private AlternativeTCLoader alternativeTCLoader;

    public CommonDicomConfiguration(Configuration configurationStorage, Map<Class, List<Class>> extensionsByClass, boolean doCacheTCGroups) {
        this(configurationStorage, extensionsByClass);
        alternativeTCLoader = new AlternativeTCLoader(this, doCacheTCGroups);
    }

    public CommonDicomConfiguration(Configuration configStorage, Map<Class, List<Class>> extensionsByClass) {

        config = new DefaultTypeSafeConfiguration<DicomConfigurationRoot>(
                configStorage,
                DicomConfigurationRoot.class,
                extensionsByClass
        );

        this.lowLevelConfig = configStorage;
        this.extensionsByClass = extensionsByClass;

        vitalizer = config.getVitalizer();

        addCustomAdapters(vitalizer);

        // quick init
        try {
            if (!configurationExists()) {
                lowLevelConfig.persistNode(DicomPath.CONFIG_ROOT_PATH, createInitialConfigRootNode(), null);

            }
        } catch (ConfigurationException e) {
            throw new RuntimeException("Dicom configuration cannot be initialized", e);
        }

        alternativeTCLoader = new AlternativeTCLoader(this, false);
    }

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

    public static void addCustomAdapters(BeanVitalizer defaultBeanVitalizer) {

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
    }

    protected HashMap<String, Object> createInitialConfigRootNode() {
        HashMap<String, Object> rootNode = new HashMap<String, Object>();
        rootNode.put("dicomDevicesRoot", new HashMap<String, Object>());

        List<Object> pathItems = new ArrayList<Object>(METADATA_ROOT_PATH.getPathItems());
        pathItems.remove(0);

        Nodes.replaceNode(rootNode, new HashMap(), pathItems);
        return rootNode;
    }

    @Override
    public TypeSafeConfiguration<DicomConfigurationRoot> getTypeSafeConfiguration() {
        return config;
    }

    @Override
    public boolean configurationExists() throws ConfigurationException {
        return lowLevelConfig.nodeExists(DicomPath.CONFIG_ROOT_PATH);
    }

    @Override
    public boolean purgeConfiguration() throws ConfigurationException {
        if (!configurationExists()) return false;
        lowLevelConfig.persistNode(DicomPath.CONFIG_ROOT_PATH, new HashMap<String, Object>(), null);
        return true;
    }

    @Override
    public void preventDeviceModifications(Device d) {
        readOnlyDevices.put(d, true);
    }

    @Override
    public void refreshTCGroups() {
        alternativeTCLoader.refreshTCGroups();
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

        Iterator<?> search = lowLevelConfig.search(DicomPath.DeviceNameByAEName.set("aeName", aet).path());

        if (!search.hasNext()) {
            search = lowLevelConfig.search(DicomPath.DeviceNameByAENameAlias.set("aeNameAlias", aet).path());

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

        Iterator search = lowLevelConfig.search(DicomPath.DeviceNameByAEUUID.set("aeUUID", uuid).path());

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

        Iterator search = lowLevelConfig.search(DicomPath.DeviceNameByUUID.set("deviceUUID", uuid).path());

        try {
            String deviceNameNode = (String) search.next();
            return findDevice(deviceNameNode);
        } catch (NoSuchElementException e) {
            throw new ConfigurationNotFoundException("Device with UUID '" + uuid + "' not found", e);
        }
    }

    @Override
    public Device findDevice(String name, DicomConfigOptions options) throws ConfigurationException {

        options = options == null ? new DicomConfigOptions() : options;

        if (name == null) throw new IllegalArgumentException("Requested device name cannot be null");

        try {
            Object deviceConfigurationNode = lowLevelConfig.getConfigurationNode(DicomPath.devicePath(name), Device.class);
            if (deviceConfigurationNode == null)
                throw new ConfigurationNotFoundException("Device " + name + " not found");

            LoadingContext ctx = config.getContextFactory().newLoadingContext();
            if (options.getIgnoreUnresolvedReferences() == Boolean.TRUE) {
                ctx.setIgnoreUnresolvedReferences(true);
            }

            Device device = vitalizer.newConfiguredInstance((Map<String, Object>) deviceConfigurationNode, Device.class, ctx);

            // perform alternative TC init in case an extension is present
            alternativeTCLoader.initGroupBasedTCs(device);

            return device;
        } catch (ConfigurationNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ConfigurationException("Configuration for device " + name + " cannot be loaded", e);
        }

    }

    @Override
    public Device findDevice(String name) throws ConfigurationException {
        return findDevice(name, null);
    }

    @Override
    public DeviceInfo[] listDeviceInfos(DeviceInfo keys) throws ConfigurationException {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public String[] listDeviceNames() throws ConfigurationException {
        Iterator search = lowLevelConfig.search(DicomPath.AllDeviceNames.path());
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
    public List<String> listAllAETitles() throws ConfigurationException {
        List<String> aeNames = new ArrayList<String>();
        try {
            Iterator search = lowLevelConfig.search(DicomPath.AllAETitles.path());
            while (search.hasNext())
                aeNames.add((String) search.next());
        } catch (Exception e) {
            throw new ConfigurationException("Error while getting the list of registered AE titles", e);
        }
        return aeNames;
    }

    @Override
    public void persist(Device device) throws ConfigurationException {

        if (readOnlyDevices.containsKey(device)) handleReadOnlyDeviceModification();

        if (device.getDeviceName() == null) throw new ConfigurationException("The name of the device must not be null");
        if (lowLevelConfig.nodeExists(DicomPath.devicePath(device.getDeviceName())))
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

        log.warn(message, exception);
    }

    @Override
    public void merge(Device device) throws ConfigurationException {

        if (readOnlyDevices.containsKey(device)) handleReadOnlyDeviceModification();

        if (device.getDeviceName() == null) throw new ConfigurationException("The name of the device must not be null");
        Map<String, Object> configNode = createDeviceConfigNode(device);
        lowLevelConfig.persistNode(DicomPath.devicePath(device.getDeviceName()), configNode, Device.class);
    }

    protected Map<String, Object> createDeviceConfigNode(Device device) throws ConfigurationException {

        final Map<String, Object> deviceConfigNode = vitalizer.createConfigNodeFromInstance(device, Device.class);

        // wipe out TCs in case an extension is present
        alternativeTCLoader.cleanUpTransferCapabilitiesInDeviceNode(device, deviceConfigNode);


        return deviceConfigNode;
    }

    @Override
    public void removeDevice(String name) throws ConfigurationException {
        lowLevelConfig.removeNode(DicomPath.devicePath(name));
    }


    @Override
    public void close() {

    }

    @Override
    public void sync() throws ConfigurationException {
        lowLevelConfig.refreshNode(DicomPath.CONFIG_ROOT_PATH);
    }

    @Override
    public <T> T getDicomConfigurationExtension(Class<T> clazz) {

        // trick CDI
        if (TransferCapabilityConfigExtension.class.equals(clazz)) {
            return (T) new TransferCapabilityConfigExtension() {
                @Override
                public void persistTransferCapabilityConfig(TCConfiguration tcConfig) throws ConfigurationException {
                    CommonDicomConfiguration.this.persistTransferCapabilityConfig(tcConfig);
                }

                @Override
                public TCConfiguration getTransferCapabilityConfig() throws ConfigurationException {
                    return CommonDicomConfiguration.this.getTransferCapabilityConfig();
                }
            };
        }

        if (!clazz.isAssignableFrom(this.getClass())) {
            throw new IllegalArgumentException("Cannot find a configuration extension for class " + clazz.getName());
        }

        return (T) this;
    }

    @Override
    public BeanVitalizer getVitalizer() {
        return vitalizer;
    }


    @Override
    public Configuration getConfigurationStorage() {
        return lowLevelConfig;
    }

    @Override
    public void persistTransferCapabilityConfig(TCConfiguration tcConfig) throws ConfigurationException {
        Map<String, Object> configNode = vitalizer.createConfigNodeFromInstance(tcConfig);
        lowLevelConfig.persistNode(DicomPath.TC_GROUPS_PATH, configNode, TCConfiguration.class);
    }

    @Override
    public TCConfiguration getTransferCapabilityConfig() throws ConfigurationException {
        Map<String, Object> configurationNode = (Map<String, Object>) lowLevelConfig.getConfigurationNode(DicomPath.TC_GROUPS_PATH, TCConfiguration.class);

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
        lowLevelConfig.runBatch(new Batch() {

            @Override
            public void run() {
                dicomConfigBatch.run();
            }

        });
    }

    public static BeanVitalizer createDefaultDicomVitalizer() {
        DefaultBeanVitalizer defaultBeanVitalizer = new DefaultBeanVitalizer();
        addCustomAdapters(defaultBeanVitalizer);
        return defaultBeanVitalizer;
    }
}

