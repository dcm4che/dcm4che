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

package org.dcm4che3.cdi.conf;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.core.Configuration;
import org.dcm4che3.conf.core.normalization.DefaultsFilterDecorator;
import org.dcm4che3.conf.core.storage.CachedRootNodeConfiguration;
import org.dcm4che3.conf.core.storage.SingleJsonFileConfigurationStorage;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.ldap.LdapConfigurationStorage;
import org.dcm4che3.net.AEExtension;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.net.hl7.HL7ApplicationExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.*;

public class CommonDicomConfigurationFactory {

    private static Logger LOG = LoggerFactory
            .getLogger(CommonDicomConfigurationFactory.class);

    public enum ConfigType {
        JSON_FILE,
        PREFERENCES,
        LDAP
    }


    @Inject
    Instance<DeviceExtension> deviceExtensionInstance;

    @Inject
    Instance<HL7ApplicationExtension> hl7ExtensionInstance;

    @Inject
    Instance<AEExtension> aeExtensionsInstance;


    @Produces @ApplicationScoped
    public DicomConfiguration getDicomConfiguration() throws ConfigurationException {
        String configTypeStr = getPropertyWithNotice("org.dcm4che.conf.storage", "json_file", false, " Possible values: 'json_file', 'ldap'");
        ConfigType configType = ConfigType.valueOf(configTypeStr.toUpperCase().trim());

        switch (configType) {

            case JSON_FILE:
                String fileName = getPropertyWithNotice("org.dcm4che.conf.filename", "configuration.json");;
                return createDicomConfiguration(new SingleJsonFileConfigurationStorage(fileName));

            case LDAP:
                Hashtable<String,String> ldapProps = new Hashtable<String, String>();
                ldapProps.put("java.naming.provider.url", getPropertyWithNotice("org.dcm4che.conf.ldap.url", "ldap://localhost:389/dc=example,dc=com"));
                ldapProps.put("java.naming.security.principal", getPropertyWithNotice("org.dcm4che.conf.ldap.principal", "cn=Directory Manager"));
                ldapProps.put("java.naming.security.credentials", getPropertyWithNotice("org.dcm4che.conf.ldap.credentials", "1", true, null));
                ldapProps.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
                ldapProps.put("java.naming.ldap.attributes.binary", "dicomVendorData");
                return createDicomConfiguration(new LdapConfigurationStorage(ldapProps, getAllExtensions()));

            default:
            case PREFERENCES:
                throw new RuntimeException("Not implemented");
        }
    }

    String getPropertyWithNotice(String propertyName, String defaultValue) {
        return getPropertyWithNotice(propertyName, defaultValue, false, null);
    }



    String getPropertyWithNotice(String propertyName, String defaultValue, boolean hideValue, String options) {

        if (System.getProperty(propertyName) == null) {

            LOG.warn("Configuration storage init: system property '{}' not found. Using default value '{}'. "+(options!=null?options:""), propertyName, defaultValue);
        } else {
            LOG.info("Initializing dcm4che configuration storage " + "({} = {})", propertyName, hideValue ? "***" : defaultValue);
        }
        return System.getProperty(propertyName, defaultValue);
    }

    private List<Class<?>> getAllExtensions() {

        List<Class<?>> list = new ArrayList<Class<?>>();

        list.addAll(getExtensionClasses(deviceExtensionInstance));
        list.addAll(getExtensionClasses(aeExtensionsInstance));
        list.addAll(getExtensionClasses(hl7ExtensionInstance));

        return list;
    }

    private CommonDicomConfigurationWithHL7 createDicomConfiguration(Configuration configurationStorage) {

        ArrayList<Class<? extends DeviceExtension>> deviceExtensionClasses = getExtensionClasses(deviceExtensionInstance);
        ArrayList<Class<? extends AEExtension>> aeExtensionClasses = getExtensionClasses(aeExtensionsInstance);
        ArrayList<Class<? extends HL7ApplicationExtension>> hl7ApplicationExtensionClasses = getExtensionClasses(hl7ExtensionInstance);

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



    private Configuration decorate(Configuration configurationStorage) {
        // caching
        boolean cached = Boolean.valueOf(getPropertyWithNotice("org.dcm4che.configuration.cached", "true"));
        if (cached) configurationStorage = new CachedRootNodeConfiguration(configurationStorage);

        // defaults
        configurationStorage = new DefaultsFilterDecorator(configurationStorage);

        return configurationStorage;
    }

    private <T> ArrayList<Class<? extends T>> getExtensionClasses(Instance<T> instance) {
        ArrayList<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();

        Iterator<T> iterator = instance.iterator();
        while (iterator.hasNext()) {
            Class<? extends T> aClass = (Class<? extends T>) iterator.next().getClass();
            classes.add(aClass);
        }

        return classes;
    }

    public void dispose(@Disposes DicomConfiguration conf) {
        conf.close();
    }
}
