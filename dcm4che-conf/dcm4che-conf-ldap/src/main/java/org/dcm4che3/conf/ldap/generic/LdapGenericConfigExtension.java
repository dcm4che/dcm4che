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
 * Portions created by the Initial Developer are Copyright (C) 2014
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
package org.dcm4che3.conf.ldap.generic;

import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DeviceExtension;
import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ReflectiveConfig;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic Ldap ConfigurationExtension implementation that works for an arbitrary config class annotated with @ConfigClass
 * and @ConfigField annotations. Config class must be provided both as a generic arg and as a constructor arg.
 * 
 * @author Roman K
 * 
 * @param <T>
 *            Config class
 */
public class LdapGenericConfigExtension<T extends DeviceExtension> extends LdapDicomConfigurationExtension {

    public static final Logger log = LoggerFactory.getLogger(LdapGenericConfigExtension.class);

    /**
     * LDAP cn
     */
    private String commonName;

    private Class<T> confClass;

    private ReflectiveConfig reflectiveConfig = new ReflectiveConfig(null, null);

    private String getCnStr() {
        return "cn=" + commonName + ",";
    }

    @Override
    public void setDicomConfiguration(LdapDicomConfiguration config) {
        super.setDicomConfiguration(config);
        reflectiveConfig.setDicomConfiguration(config);
    }

    public LdapGenericConfigExtension(Class<T> confClass) throws ConfigurationException {
        super();

        this.confClass = confClass;

        ConfigClass ccAnno = (ConfigClass) confClass.getAnnotation(ConfigClass.class);

        // no annotation - no configuration
        if (ccAnno == null)
            throw new ConfigurationException("The configuration class must be annotated with @ConfigClass");

        // get common name
        if (ccAnno.commonName().equals("") || ccAnno.objectClass().equals(""))
            throw new ConfigurationException("To use LDAP config, specify common name and objectClass for the config class in @ConfigClass annotation");

        commonName = ccAnno.commonName();
    }

    @Override
    protected void storeChilds(String deviceDN, Device device) throws NamingException {

        T confObj = device.getDeviceExtension(confClass);

        if (confObj != null)
            store(deviceDN, confObj);

    }

    private void store(String deviceDN, T confObj) throws NamingException {

        Attributes attrs = new BasicAttributes(true);
        attrs.put(new BasicAttribute("cn", commonName));

        String objectClassName = ((ConfigClass) confClass.getAnnotation(ConfigClass.class)).objectClass();

        // add or create objectclass attribute
        Attribute objectClassAttr = attrs.get("objectclass");
        if (objectClassAttr != null)
            objectClassAttr.add(objectClassName);
        else
            attrs.put(new BasicAttribute("objectclass", objectClassName));

        ConfigWriter ldapWriter = new LdapConfigIO(attrs, getCnStr() + deviceDN, config);

        try {

            reflectiveConfig.storeConfig(confObj, ldapWriter);

        } catch (Exception e) {
            log.error("Unable to store configuration for class " + confClass.getSimpleName(), e);
        }

    }

    @Override
    protected void loadChilds(Device device, String deviceDN) throws NamingException, ConfigurationException {
        Attributes attrs;
        try {
            attrs = config.getAttributes(getCnStr() + deviceDN);
        } catch (NameNotFoundException e) {
            return;
        }

        // TODO: creation can be done already by the reflective adapter
        try {

            T confObj = confClass.newInstance();

            ConfigReader ldapReader = new LdapConfigIO(attrs, getCnStr() + deviceDN, config);

            reflectiveConfig.readConfig(confObj, ldapReader);

            device.addDeviceExtension(confObj);

        } catch (Exception e) {
            log.error("Unable to read configuration for class " + confClass.getSimpleName(), e);
        }

    }

    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN) throws NamingException {
        T prevConfObj = prev.getDeviceExtension(confClass);
        T confObj = device.getDeviceExtension(confClass);

        if (confObj == null) {
            if (prevConfObj != null)
                config.destroySubcontextWithChilds(getCnStr() + deviceDN);
            return;
        }
        if (prevConfObj == null) {
            store(deviceDN, confObj);
            return;
        }

        ConfigWriter ldapDiffWriter = new LdapConfigIO(new ArrayList<ModificationItem>(), getCnStr() + deviceDN, config);

        try {

            reflectiveConfig.storeConfigDiffs(prevConfObj, confObj, ldapDiffWriter);

        } catch (Exception e) {
            log.error("Unable to merge configuration for class " + confClass.getSimpleName(), e);
        }
    }
}
