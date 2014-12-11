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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.core.normalization.DefaultsFilterDecorator;
import org.dcm4che3.conf.core.storage.CachedRootNodeConfiguration;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.dcm4che3.conf.ldap.LdapConfigurationStorage;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class DicomConfigurationBuilder {

    private static class Ldap extends DicomConfigurationBuilder {

        private final LdapConfigurationStorage ldapConfigurationStorage;

        public Ldap(LdapConfigurationStorage ldapConfigurationStorage) {
            super(ldapConfigurationStorage);
            this.ldapConfigurationStorage = ldapConfigurationStorage;
        }

        @Override
        public <T extends DeviceExtension> DicomConfigurationBuilder registerDeviceExtension(
                Class<T> clazz) {
            ldapConfigurationStorage.addExtensionClass(clazz);
            return super.registerDeviceExtension(clazz);
        }

        @Override
        public <T extends AEExtension> DicomConfigurationBuilder registerAEExtension(
                Class<T> clazz) {
            ldapConfigurationStorage.addExtensionClass(clazz);
            return super.registerAEExtension(clazz);
        }

        @Override
        public <T extends HL7ApplicationExtension> DicomConfigurationBuilder registerHL7ApplicationExtension(
                Class<T> clazz) {
            ldapConfigurationStorage.addExtensionClass(clazz);
            return super.registerHL7ApplicationExtension(clazz);
        }

    }

    private static Logger LOG = LoggerFactory
            .getLogger(DicomConfigurationBuilder.class);

    private final Collection<Class<? extends DeviceExtension>> 
        deviceExtensionClasses = new ArrayList<Class<? extends DeviceExtension>>();
    private final Collection<Class<? extends AEExtension>>
        aeExtensionClasses = new ArrayList<Class<? extends AEExtension>>();
    private final Collection<Class<? extends HL7ApplicationExtension>>
        hl7ApplicationExtensionClasses = new ArrayList<Class<? extends HL7ApplicationExtension>>();
    private Configuration configurationStorage;

    private enum ConfigType {
        JSON_FILE,
        PREFERENCES,
        LDAP
    }

    private DicomConfigurationBuilder(Configuration configurationStorage) {
        this.configurationStorage = configurationStorage;
    }

    public static DicomConfigurationBuilder newConfigurationBuilder(Hashtable<?,?> props)
            throws ConfigurationException {
        DicomConfigurationBuilder builder;
        String configType = getPropertyWithNotice(props,
                "org.dcm4che.conf.storage", "json_file", 
                " Possible values: 'json_file', 'ldap'.");
        switch(ConfigType.valueOf(configType.toUpperCase().trim())) {
        case JSON_FILE:
            builder = newJsonConfigurationBuilder(getPropertyWithNotice(props,
                "org.dcm4che.conf.filename", "../standalone/configuration/sample-config.json"));
            break;
        case LDAP:
            Hashtable<String,String> ldapProps = new Hashtable<String, String>();
            ldapProps.put("java.naming.provider.url",
                    getPropertyWithNotice(props,
                            "org.dcm4che.conf.ldap.url", 
                            "ldap://localhost:389/dc=example,dc=com"));
            ldapProps.put("java.naming.security.principal",
                    getPropertyWithNotice(props,
                            "org.dcm4che.conf.ldap.principal",
                            "cn=Directory Manager"));
            ldapProps.put("java.naming.security.credentials",
                    getPasswordWithNotice(props, 
                            "org.dcm4che.conf.ldap.credentials", "1"));
            ldapProps.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
            ldapProps.put("java.naming.ldap.attributes.binary", "dicomVendorData");
            builder = newLdapConfigurationBuilder(ldapProps);
            break;
        case PREFERENCES:
        default:
            throw new RuntimeException("Not implemented");
        }
        boolean cached = Boolean.valueOf(getPropertyWithNotice(props, "org.dcm4che.configuration.cached", "true"));
        return cached ? builder.cache() : builder;

    }

    public static DicomConfigurationBuilder newJsonConfigurationBuilder(String fileName) {
        return new DicomConfigurationBuilder(
                new SingleJsonFileConfigurationStorage(fileName));
    }

    public static DicomConfigurationBuilder newLdapConfigurationBuilder(Hashtable<?,?> ldapProps)
            throws ConfigurationException {
        return new DicomConfigurationBuilder.Ldap(
                new LdapConfigurationStorage(ldapProps));
        
    }

    public <T extends DeviceExtension> DicomConfigurationBuilder registerDeviceExtension(
            Class<T> clazz) {
        deviceExtensionClasses.add(clazz);
        return this;
    }
 
    public <T extends AEExtension> DicomConfigurationBuilder registerAEExtension(
            Class<T> clazz) {
        aeExtensionClasses.add(clazz);
        return this;
    }
 
    public <T extends HL7ApplicationExtension> DicomConfigurationBuilder registerHL7ApplicationExtension(
            Class<T> clazz) {
        hl7ApplicationExtensionClasses.add(clazz);
        return this;
    }
 
    public DicomConfigurationBuilder cache() {
        if (!(configurationStorage instanceof CachedRootNodeConfiguration))
            configurationStorage = new CachedRootNodeConfiguration(configurationStorage);
        return this;
    }

    public CommonDicomConfigurationWithHL7 build() {
        LOG.info("Dcm4che configuration device extensions: {}", deviceExtensionClasses);
        LOG.info("Dcm4che configuration AE extensions: {}", aeExtensionClasses);
        LOG.info("Dcm4che configuration HL7 extensions: {}", hl7ApplicationExtensionClasses);

        return new CommonDicomConfigurationWithHL7(
                new DefaultsFilterDecorator(configurationStorage),
                deviceExtensionClasses,
                aeExtensionClasses,
                hl7ApplicationExtensionClasses
        );
    }

    private static String getPropertyWithNotice(Hashtable<?, ?> props,
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

}
