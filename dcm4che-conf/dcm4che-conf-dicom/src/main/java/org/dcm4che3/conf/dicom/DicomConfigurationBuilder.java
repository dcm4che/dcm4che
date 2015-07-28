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

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.normalization.DefaultsAndNullFilterDecorator;
import org.dcm4che3.conf.core.storage.CachingConfigurationDecorator;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.dcm4che3.conf.dicom.ldap.LdapConfigurationStorage;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.dcm4che3.util.StringUtils;
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

    private Boolean cache;
    private Boolean persistDefaults;
    private Hashtable<?, ?> ldapProps = null;
    private Configuration configurationStorage = null;
    private Map<Class, List<Class>> extensionClassesMap = new HashMap<Class, List<Class>>();

    private void setLdapProps(Hashtable<?, ?> ldapProps) {
        this.ldapProps = ldapProps;
    }

    public DicomConfigurationBuilder() {
        this.props = new Hashtable<Object, Object>();
    }

    public DicomConfigurationBuilder(Hashtable<?, ?> props) {
        this.props = props;
    }

    private enum ConfigType {
        JSON_FILE,
        LDAP,
        CUSTOM;
    }

    public DicomConfigurationBuilder registerCustomConfigurationStorage(Configuration storage) {
        configurationStorage = storage;
        return this;
    }


    public void addExtensionForBaseExtension(Class extensionClass, Class baseExtensionClass) {

        List<Class> extensionClasses = extensionClassesMap.get(baseExtensionClass);

        if (extensionClasses == null) {
            extensionClasses = new ArrayList<Class>();
            extensionClassesMap.put(baseExtensionClass, extensionClasses);
        }

        // don't put duplicates
        if (!extensionClasses.contains(extensionClass))
            extensionClasses.add(extensionClass);

    }

    public <T extends DeviceExtension> DicomConfigurationBuilder registerDeviceExtension(
            Class<T> clazz) {
        addExtensionForBaseExtension(clazz, DeviceExtension.class);
        return this;
    }

    public <T extends AEExtension> DicomConfigurationBuilder registerAEExtension(
            Class<T> clazz) {
        addExtensionForBaseExtension(clazz, AEExtension.class);
        return this;
    }

    public <T extends HL7ApplicationExtension> DicomConfigurationBuilder registerHL7ApplicationExtension(
            Class<T> clazz) {
        addExtensionForBaseExtension(clazz, HL7ApplicationExtension.class);
        return this;
    }

    public DicomConfigurationBuilder cache(boolean cache) {
        this.cache = cache;
        return this;
    }

    public DicomConfigurationBuilder persistDefaults(boolean persistDefaults) {
        this.persistDefaults = persistDefaults;
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

    private Configuration createConfigurationStorage(List<Class> allExtensions) throws ConfigurationException {

        // if configurationStorage is already set - skip the storage init
        if (configurationStorage == null) {
            String configType = getPropertyWithNotice(props,
                    "org.dcm4che.conf.storage", "json_file",
                    " Possible values: 'json_file', 'ldap'.");

            switch (ConfigType.valueOf(configType.toUpperCase().trim())) {
                case JSON_FILE:
                    SingleJsonFileConfigurationStorage jsonConfigurationStorage = createJsonFileConfigurationStorage();
                    jsonConfigurationStorage.setFileName(
                            StringUtils.replaceSystemProperties(
                                    getPropertyWithNotice(
                                            props,
                                            "org.dcm4che.conf.filename",
                                            "${jboss.server.config.dir}/dcm4chee-arc/sample-config.json")));
                    configurationStorage = jsonConfigurationStorage;
                    break;
                case LDAP:
                    // init LDAP props if were not yet inited by the builder
                    if (ldapProps == null) {
                        Hashtable<String, String> ldapStringProps = new Hashtable<String, String>();
                        ldapStringProps.put("java.naming.provider.url",
                                getPropertyWithNotice(props,
                                        "org.dcm4che.conf.ldap.url",
                                        "ldap://localhost:389/dc=example,dc=com"));
                        ldapStringProps.put("java.naming.security.principal",
                                getPropertyWithNotice(props,
                                        "org.dcm4che.conf.ldap.principal",
                                        "cn=Directory Manager"));
                        ldapStringProps.put("java.naming.security.credentials",
                                getPasswordWithNotice(props,
                                        "org.dcm4che.conf.ldap.credentials", "1"));
                        ldapStringProps.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
                        ldapStringProps.put("java.naming.ldap.attributes.binary", "dicomVendorData");

                        ldapProps = ldapStringProps;
                    }


                    LdapConfigurationStorage ldapConfigurationStorage = createLdapConfigurationStorage();
                    ldapConfigurationStorage.setEnvironment(ldapProps);
                    ldapConfigurationStorage.setExtensions(allExtensions);

                    configurationStorage = ldapConfigurationStorage;

                    break;

                default:
                    throw new RuntimeException("Not implemented");
            }
        }

        if (cache != null ? cache
                : Boolean.valueOf(getPropertyWithNotice(props, "org.dcm4che.conf.cached", "false")))
            configurationStorage = new CachingConfigurationDecorator(configurationStorage, props);

        configurationStorage = new DefaultsAndNullFilterDecorator(
                configurationStorage,
                persistDefaults != null
                        ? persistDefaults
                        : Boolean.valueOf(getPropertyWithNotice(props, "org.dcm4che.conf.persistDefaults", "false")),
                allExtensions);

        return configurationStorage;
    }

    protected LdapConfigurationStorage createLdapConfigurationStorage() {
        return new LdapConfigurationStorage();
    }

    protected SingleJsonFileConfigurationStorage createJsonFileConfigurationStorage() {
        return new SingleJsonFileConfigurationStorage();
    }

    public static String getPropertyWithNotice(Hashtable<?, ?> props,
                                               String key, String defval) {
        return getPropertyWithNotice(props, key, defval, "", false);
    }

    private static String getPropertyWithNotice(Hashtable<?, ?> props,
                                                String key, String defval, String options) {
        return getPropertyWithNotice(props, key, defval, options, false);
    }

    private static String getPasswordWithNotice(Hashtable<?, ?> props,
                                                String key, String defval) {
        return getPropertyWithNotice(props, key, defval, "", true);
    }

    private static String getPropertyWithNotice(Hashtable<?, ?> props,
                                                String key, String defval, String options, boolean hide) {
        String val = (String) props.get(key);
        if (val == null) {
            val = defval;
            LOG.warn("Configuration storage init: system property '{}' not found. "
                    + "Using default value '{}'.{}", key, defval, options);
        } else {
            LOG.info("Initializing dcm4che configuration storage " + "({} = {})",
                    key, hide ? "***" : val);
        }
        return val;
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

    public static DicomConfigurationBuilder newLdapConfigurationBuilder(Hashtable<?, ?> ldapProps)
            throws ConfigurationException {
        Hashtable<Object, Object> props = new Hashtable<Object, Object>();
        props.put("org.dcm4che.conf.storage", "ldap");
        DicomConfigurationBuilder dicomConfigurationBuilder = new DicomConfigurationBuilder(props);
        dicomConfigurationBuilder.setLdapProps(ldapProps);
        return dicomConfigurationBuilder;
    }

}
