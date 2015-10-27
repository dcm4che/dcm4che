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
package org.dcm4che3.conf.dicom.ldap;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.AnnotatedConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.core.api.internal.ConfigIterators;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Roman K
 */
public class LdapConfigNodeReader {
    static Object readNode(LdapConfigurationStorage ldapConfigurationStorage, String dn, Class configurableClass) throws ConfigurationException, NamingException {
        ArrayList<String> objectClasses = LdapConfigUtils.extractObjectClasses(configurableClass);

        Attributes attributes;
        try {
            attributes = ldapConfigurationStorage.getLdapCtx().getAttributes(dn);
        } catch (NameNotFoundException noname) {
            // node is not there at all
            return null;
        }

        boolean isAnyContents = false;

        // if corresponding LDAP node has any of the configClass'es object classes - then make sure we don't return the node as null
        // this, e.g., safeguards a case when an object is there but has all values set to defaults which were filtered out
        NamingEnumeration<?> nodeObjectClasses = attributes.get("objectClass").getAll();
        while (nodeObjectClasses.hasMore()) {
            String oClass = (String) nodeObjectClasses.next();
            if (objectClasses.contains(oClass)) isAnyContents = true;
        }


        Map<String, Object> configNode = new HashMap<String, Object>();
        for (AnnotatedConfigurableProperty property : ConfigIterators.getAllConfigurableFieldsAndSetterParameters(configurableClass)) {
            // map
            if (Map.class.isAssignableFrom(property.getRawClass())) {
                String subDn = LdapConfigUtils.getSubDn(dn, property);

                Map<String, Object> map = new HashMap<String, Object>();
                try {
                    NamingEnumeration<SearchResult> enumeration = LdapConfigUtils.searchForCollectionElements(ldapConfigurationStorage, subDn, property);
                    while (enumeration.hasMore()) {
                        isAnyContents = true;

                        SearchResult res = enumeration.next();
                        String distField = LdapConfigUtils.getDistinguishingFieldForCollectionElement(property);
                        Attributes resAttributes = res.getAttributes();
                        String key = (String) resAttributes.get(distField).get();

                        // check if it is a primitive or a custom representation, e.g a ref
                        if (!property.isMapOfConfObjects()) {

                            Object value = resAttributes.get(property.getAnnotation(LDAP.class).mapValueAttribute()).get();

                            if (property.isCollectionOfReferences())
                                value = LdapConfigUtils.ldapDnToRef((String) value, property.getPseudoPropertyForCollectionElement(), ldapConfigurationStorage);

                            map.put(key, value);
                        } else {
                            Object value = readNode(ldapConfigurationStorage, res.getName() + "," + subDn, property.getPseudoPropertyForConfigClassCollectionElement().getRawClass());
                            map.put(key, value);
                        }
                    }
                } catch (NameNotFoundException e) {
                    //noop
                }

                configNode.put(property.getAnnotatedName(), map);
                continue;
            }

            // nested
            if (property.isConfObject()) {

                if (property.isReference()) {
                    if (attributes != null) {
                        isAnyContents = true;
                        Object value = attributes.get(LdapConfigUtils.getLDAPPropertyName(property)).get();
                        value = LdapConfigUtils.ldapDnToRef((String) value, property, ldapConfigurationStorage);
                        configNode.put(property.getAnnotatedName(), value);
                    }
                } else {
                    String subDn = LdapConfigUtils.getSubDn(dn, property);
                    Object value = readNode(ldapConfigurationStorage, subDn, property.getRawClass());
                    if (value != null) isAnyContents = true;
                    configNode.put(property.getAnnotatedName(), value);
                }

                continue;
            }

            // collection with confObjects (not refs)
            if (property.isArrayOfConfObjects()
                    || property.isCollectionOfConfObjects()
                    && !property.isCollectionOfReferences()) {

                Class elemClass = property.getPseudoPropertyForConfigClassCollectionElement().getRawClass();
                String subDn = LdapConfigUtils.getSubDn(dn, property);

                try {
                    NamingEnumeration<SearchResult> enumeration = LdapConfigUtils.searchForCollectionElements(ldapConfigurationStorage, subDn, property);
                    ArrayList<Object> list = new ArrayList<Object>();
                    while (enumeration.hasMore()) {
                        isAnyContents = true;
                        SearchResult next = enumeration.next();
                        list.add(readNode(ldapConfigurationStorage, next.getName() + "," + dn, elemClass));
                    }
                    configNode.put(property.getAnnotatedName(), list);
                } catch (NameNotFoundException e) {
                    //noop
                }
                continue;
            }

            // primitive or custom representation or reference collection
            if ((Collection.class.isAssignableFrom(property.getRawClass()) || property.getRawClass().isArray()) && attributes != null) {
                ArrayList<Object> list = new ArrayList<Object>();

                // special case - EnumSet with boolean repr
                String[] options = property.getAnnotation(LDAP.class).booleanBasedEnumStorageOptions();
                if (options.length>0) {
                    int i = 0;
                    for (String option : options) {
                        Attribute attribute = attributes.get(option);
                        if (attribute != null)
                            if (attribute.get().equals("TRUE")) {
                                isAnyContents = true;
                                list.add(i);
                            }
                        i++;
                    }
                    configNode.put(property.getAnnotatedName(), list);
                    continue;
                }

                Attribute attribute = attributes.get(LdapConfigUtils.getLDAPPropertyName(property));
                if (attribute == null) continue;

                for (int i = 0; i < attribute.size(); i++) {
                    isAnyContents = true;
                    Object val = attribute.get(i);

                    // special case with references
                    if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
                        val = LdapConfigUtils.ldapDnToRef((String) val, property.getPseudoPropertyForCollectionElement(), ldapConfigurationStorage);

                    list.add(val);
                }
                configNode.put(property.getAnnotatedName(), list);
                continue;
            }

            if (attributes != null) {
                Attribute attribute = attributes.get(LdapConfigUtils.getLDAPPropertyName(property));
                if (attribute != null) {
                    isAnyContents = true;
                    configNode.put(property.getAnnotatedName(), attribute.get().toString());
                }
                continue;
            }

        }

        if (configurableClass.equals(Device.class)) {
            ldapConfigurationStorage.fillExtension(dn, configNode, "deviceExtensions");
        } else if (configurableClass.equals(ApplicationEntity.class)) {
            ldapConfigurationStorage.fillExtension(dn, configNode, "aeExtensions");
        } else if (configurableClass.equals(HL7Application.class)) {
            ldapConfigurationStorage.fillExtension(dn, configNode, "hl7AppExtensions");
        }

        if (!isAnyContents) return null;

        return configNode;
    }
}
