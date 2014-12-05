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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
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

import java.util.*;

public class DicomConfigurationFactory {

    private static Logger LOG = LoggerFactory
            .getLogger(DicomConfigurationFactory.class);

    public enum ConfigType {
        JSON_FILE,
        PREFERENCES,
        LDAP
    }


    public static DicomConfiguration createDicomConfiguration(Properties properties, ArrayList<Class<? extends DeviceExtension>> deviceExtensionClasses, ArrayList<Class<? extends AEExtension>> aeExtensionClasses, ArrayList<Class<? extends HL7ApplicationExtension>> hl7ApplicationExtensionClasses) throws ConfigurationException {

        String configTypeStr = getPropertyWithNotice("org.dcm4che.conf.storage", "json_file", false, " Possible values: 'json_file', 'ldap'", properties);
        ConfigType configType = ConfigType.valueOf(configTypeStr.toUpperCase().trim());

        switch (configType) {

            case JSON_FILE:
                String fileName = getPropertyWithNotice("org.dcm4che.conf.filename", "configuration.json", properties);;
                return createDicomConfiguration(new SingleJsonFileConfigurationStorage(fileName), deviceExtensionClasses, aeExtensionClasses, hl7ApplicationExtensionClasses);

            case LDAP:
                Hashtable<String,String> ldapProps = new Hashtable<String, String>();
                ldapProps.put("java.naming.provider.url", getPropertyWithNotice("org.dcm4che.conf.ldap.url", "ldap://localhost:389/dc=example,dc=com", properties));
                ldapProps.put("java.naming.security.principal", getPropertyWithNotice("org.dcm4che.conf.ldap.principal", "cn=Directory Manager", properties));
                ldapProps.put("java.naming.security.credentials", getPropertyWithNotice("org.dcm4che.conf.ldap.credentials", "1", true, null, properties));
                ldapProps.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
                ldapProps.put("java.naming.ldap.attributes.binary", "dicomVendorData");
                return createDicomConfiguration(
                        new LdapConfigurationStorage(ldapProps, getAllExtensions(deviceExtensionClasses, aeExtensionClasses, hl7ApplicationExtensionClasses)),
                        deviceExtensionClasses,
                        aeExtensionClasses,
                        hl7ApplicationExtensionClasses);

            default:
            case PREFERENCES:
                throw new RuntimeException("Not implemented");
        }
    }

    static String getPropertyWithNotice(String propertyName, String defaultValue, Properties properties) {
        return getPropertyWithNotice(propertyName, defaultValue, false, null, properties);
    }



    static String getPropertyWithNotice(String propertyName, String defaultValue, boolean hideValue, String options, Properties properties) {

        String userValue = properties.getProperty(propertyName);
        if (userValue == null) {
            userValue = defaultValue;
            LOG.warn("Configuration storage init: system property '{}' not found. Using default value '{}'. "+(options!=null?options:""), propertyName, defaultValue);
        } else {
            LOG.info("Initializing dcm4che configuration storage " + "({} = {})", propertyName, hideValue ? "***" : userValue);
        }
        return userValue;
    }

    private static List<Class<?>> getAllExtensions(ArrayList<Class<? extends DeviceExtension>> deviceExtensionClasses, ArrayList<Class<? extends AEExtension>> aeExtensionClasses, ArrayList<Class<? extends HL7ApplicationExtension>> hl7AppExtensionClasses) {

        List<Class<?>> list = new ArrayList<Class<?>>();

        list.addAll(deviceExtensionClasses);
        list.addAll(aeExtensionClasses);
        list.addAll(hl7AppExtensionClasses);

        return list;
    }

    private static CommonDicomConfigurationWithHL7 createDicomConfiguration(Configuration configurationStorage, ArrayList<Class<? extends DeviceExtension>> deviceExtensionClasses, ArrayList<Class<? extends AEExtension>> aeExtensionClasses, ArrayList<Class<? extends HL7ApplicationExtension>> hl7ApplicationExtensionClasses) {


        LOG.info("Dcm4che configuration device extensions: {}", deviceExtensionClasses);
        LOG.info("Dcm4che configuration AE extensions: {}", aeExtensionClasses);
        LOG.info("Dcm4che configuration HL7 extensions: {}", hl7ApplicationExtensionClasses);

        return new CommonDicomConfigurationWithHL7(
                decorate(configurationStorage),
                deviceExtensionClasses,
                aeExtensionClasses,
                hl7ApplicationExtensionClasses
        );
    }

    private static Configuration decorate(Configuration configurationStorage) {
        // caching
        boolean cached = Boolean.valueOf(getPropertyWithNotice("org.dcm4che.configuration.cached", "true", System.getProperties()));
        if (cached) configurationStorage = new CachedRootNodeConfiguration(configurationStorage);

        // defaults
        configurationStorage = new DefaultsFilterDecorator(configurationStorage);

        return configurationStorage;
    }

}
