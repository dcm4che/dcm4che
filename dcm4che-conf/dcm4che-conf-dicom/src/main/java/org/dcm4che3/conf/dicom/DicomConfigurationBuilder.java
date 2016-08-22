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
 * Portions created by the Initial Developer are Copyright (C) 2011-2014
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

package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.ConfigurationSettingsLoader;
import org.dcm4che3.conf.core.DefaultBeanVitalizer;
import org.dcm4che3.conf.core.ExtensionMergingConfiguration;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Path;
import org.dcm4che3.conf.core.index.ReferenceIndexingDecorator;
import org.dcm4che3.conf.core.normalization.DefaultsAndNullFilterDecorator;
import org.dcm4che3.conf.core.olock.HashBasedOptimisticLockingConfiguration;
import org.dcm4che3.conf.core.storage.SimpleCachingConfigurationDecorator;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DicomConfigurationBuilder {

    private Hashtable<?, ?> props;

    private static Logger LOG = LoggerFactory
            .getLogger(DicomConfigurationBuilder.class);

    private boolean extensionMerge = false;
    private boolean cache = false;
    private Hashtable<?, ?> ldapProps = null;
    private Configuration configurationStorage = null;
    private Map<Class, List<Class>> extensionClassesMap = new HashMap<Class, List<Class>>();
    private boolean doOptimisticLocking = false;
    private boolean uuidIndexing = true;

    private void setLdapProps(Hashtable<?, ?> ldapProps) {
        this.ldapProps = ldapProps;
    }

    public DicomConfigurationBuilder() {
        this.props = new Hashtable<Object, Object>();
    }

    public DicomConfigurationBuilder(Hashtable<?, ?> props) {
        this.props = props;
    }

    public DicomConfigurationBuilder registerCustomConfigurationStorage(Configuration storage) {
        configurationStorage = storage;
        return this;
    }


    public DicomConfigurationBuilder registerExtensionForBaseExtension(Class extensionClass, Class baseExtensionClass) {

        List<Class> extensionClasses = extensionClassesMap.get(baseExtensionClass);

        if (extensionClasses == null) {
            extensionClasses = new ArrayList<Class>();
            extensionClassesMap.put(baseExtensionClass, extensionClasses);
        }

        // don't put duplicates
        if (!extensionClasses.contains(extensionClass))
            extensionClasses.add(extensionClass);

        return this;
    }

    public <T extends DeviceExtension> DicomConfigurationBuilder registerDeviceExtension(
            Class<T> clazz) {
        registerExtensionForBaseExtension(clazz, DeviceExtension.class);
        return this;
    }

    public <T extends AEExtension> DicomConfigurationBuilder registerAEExtension(
            Class<T> clazz) {
        registerExtensionForBaseExtension(clazz, AEExtension.class);
        return this;
    }

    public <T extends HL7ApplicationExtension> DicomConfigurationBuilder registerHL7ApplicationExtension(
            Class<T> clazz) {
        registerExtensionForBaseExtension(clazz, HL7ApplicationExtension.class);
        return this;
    }

    public DicomConfigurationBuilder extensionMerge(boolean extensionMerge) {
        this.extensionMerge = extensionMerge;
        return this;
    }

    public DicomConfigurationBuilder cache(boolean cache) {
        this.cache = cache;
        return this;
    }


    public DicomConfigurationBuilder uuidIndexing() {
        this.uuidIndexing = true;
        return this;
    }

    public DicomConfigurationBuilder disableUuidIndexing() {
        this.uuidIndexing = false;
        return this;
    }

    public CommonDicomConfigurationWithHL7 build() throws ConfigurationException {

        List<Class> allExtensions = new ArrayList<Class>();

        for (Map.Entry<Class, List<Class>> classListEntry : extensionClassesMap.entrySet())
            allExtensions.addAll(classListEntry.getValue());

        Configuration configurationStorage = createConfigurationStorage(allExtensions);
        if (configurationStorage == null) return null;


        for (Map.Entry<Class, List<Class>> classListEntry : extensionClassesMap.entrySet())
            LOG.info("Dcm4che configuration {} classes: {}", classListEntry.getKey().getSimpleName(), classListEntry.getValue());


        // check if we have any extensions that have equal simple name and fail if found
        HashSet<String> simpleNames = new HashSet<String>();
        for (Class extension : allExtensions) {
            if (!simpleNames.add(extension.getSimpleName()))

                throw new ConfigurationException(
                        "Duplicate configuration class extension name '"
                                + extension.getSimpleName()
                                + "'. Make sure that simple class names of extensions are unique!");
        }


        return new CommonDicomConfigurationWithHL7(configurationStorage, extensionClassesMap);
    }

    protected Configuration createConfigurationStorage(List<Class> allExtensions) throws ConfigurationException {

        // if configurationStorage is already set - skip the storage init
        if (configurationStorage == null) {
            String configType = ConfigurationSettingsLoader.getPropertyWithNotice(props,
                    Configuration.CONF_STORAGE_SYSTEM_PROP, "json_file",
                    " Possible values: 'json_file'.");

            switch (Configuration.ConfigStorageType.valueOf(configType.toUpperCase().trim())) {
                case JSON_FILE:
                    SingleJsonFileConfigurationStorage jsonConfigurationStorage = createJsonFileConfigurationStorage();
                    jsonConfigurationStorage.configure(props);
                    configurationStorage = jsonConfigurationStorage;
                    break;
                default:
                    throw new RuntimeException("Not implemented");
            }
        }

        if (cache)
            configurationStorage = new SimpleCachingConfigurationDecorator(configurationStorage, props);

        if(extensionMerge)
            configurationStorage = new ExtensionMergingConfiguration(configurationStorage, allExtensions);

        if (uuidIndexing)
            configurationStorage = new ReferenceIndexingDecorator(configurationStorage, new HashMap<String, Path>());

        if (doOptimisticLocking)
            configurationStorage = new HashBasedOptimisticLockingConfiguration(configurationStorage, allExtensions);

        configurationStorage = new DefaultsAndNullFilterDecorator(
                configurationStorage,
                allExtensions,
                CommonDicomConfiguration.createDefaultDicomVitalizer()
        );

        return configurationStorage;
    }

    protected SingleJsonFileConfigurationStorage createJsonFileConfigurationStorage() {
        return new SingleJsonFileConfigurationStorage();
    }

    public static DicomConfigurationBuilder newConfigurationBuilder(Hashtable<?, ?> props)
            throws ConfigurationException {
        return new DicomConfigurationBuilder(props);
    }

    public static DicomConfigurationBuilder newJsonConfigurationBuilder(String fileName) {
        Hashtable<Object, Object> props = new Hashtable<Object, Object>();
        props.put("org.dcm4che.conf.storage", "json_file");
        props.put("org.dcm4che.conf.filename", fileName);
        return new DicomConfigurationBuilder(props);
    }

    @Deprecated
    public static DicomConfigurationBuilder newLdapConfigurationBuilder(Hashtable<?, ?> ldapProps)
            throws ConfigurationException {
        Hashtable<Object, Object> props = new Hashtable<Object, Object>();
        props.put("org.dcm4che.conf.storage", "ldap");
        DicomConfigurationBuilder dicomConfigurationBuilder = new DicomConfigurationBuilder(props);
        dicomConfigurationBuilder.setLdapProps(ldapProps);
        return dicomConfigurationBuilder;
    }

    public DicomConfigurationBuilder optimisticLocking(boolean doOptimisticLocking) {
        this.doOptimisticLocking = doOptimisticLocking;
        return this;
    }
}
