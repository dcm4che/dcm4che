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
import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.core.api.internal.ConfigIterators;
import org.dcm4che3.conf.core.util.Extensions;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.*;

/**
 * @author: Roman K
 */
public class LdapNode {


    private LdapNode parent;
    private String dn;
    private Collection<String> objectClasses = new ArrayList<String>();
    private Set<String> childrenObjectClasses = new HashSet<String>();
    private Attributes attributes = new BasicAttributes();
    private Collection<LdapNode> children = new ArrayList<LdapNode>();

    private LdapConfigurationStorage ldapConfigurationStorage;

    public LdapNode() {
    }

    public LdapNode(LdapConfigurationStorage ldapConfigurationStorage) {

        this.ldapConfigurationStorage = ldapConfigurationStorage;
    }

    public LdapNode(Attributes attributes) {
        this.attributes = attributes;
    }

    public LdapConfigurationStorage getLdapConfigurationStorage() {
        return ldapConfigurationStorage == null && parent != null ? parent.getLdapConfigurationStorage() : ldapConfigurationStorage;
    }

    private String getBaseDn() {
        if (parent == null) return dn;
        else return parent.getBaseDn();
    }


    public LdapNode getParent() {
        return parent;
    }

    public void setParent(LdapNode parent) {
        this.parent = parent;
        parent.getChildren().add(this);
    }

    /**
     * Responsible for
     * objectClasses, attributes, children
     * <p/>
     * NOT responsible for
     * parent, dn
     *
     * @param configNode
     * @param configurableClass
     * @throws org.dcm4che3.conf.core.api.ConfigurationException
     */
    public void populate(Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {

        if (configurableClass == null ||
                configurableClass.getAnnotation(ConfigurableClass.class) == null)
            throw new ConfigurationException("Unexpected error - class '" + configurableClass + "' is not a configurable class");

        // fill in objectclasses
        ArrayList<String> objectClasses = LdapConfigUtils.extractObjectClasses(configurableClass);
        getObjectClasses().addAll(objectClasses);
        if (getParent()!= null)
            getParent().getChildrenObjectClasses().addAll(objectClasses);

        // iterate over configurable properties
        List<AnnotatedConfigurableProperty> properties = ConfigIterators.getAllConfigurableFieldsAndSetterParameters(configurableClass);
        for (AnnotatedConfigurableProperty property : properties) {

            Object propertyConfigNode = configNode.get(property.getAnnotatedName());

            if (propertyConfigNode == null) continue;

            // map of anything
            if (Map.class.isAssignableFrom(property.getRawClass())) {
                LdapNode thisParent = makeLdapCollectionNode(property);
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) propertyConfigNode).entrySet()) {
                    LdapNode elementNode = thisParent.makeLdapElementNode(property, String.valueOf(entry.getKey()));

                    // now if it is a conf obj, not primitive or custom representation, go deeper
                    if (property.isMapOfConfObjects() && entry.getValue() instanceof Map) {
                        elementNode.populate((Map<String, Object>) entry.getValue(), property.getPseudoPropertyForConfigClassCollectionElement().getRawClass());
                        continue;
                    }

                    //otherwise, make this mock with value
                    elementNode.setObjectClasses(getObjectClasses(property));
                    elementNode.getParent().getChildrenObjectClasses().addAll(elementNode.getObjectClasses());
                    elementNode.getAttributes().put(property.getAnnotation(LDAP.class).mapValueAttribute(), entry.getValue());

                }
                continue;
            }

            //collection/array of confobjects, (but not custom representations)
            if (property.isArrayOfConfObjects() || property.isCollectionOfConfObjects() && !property.getAnnotation(ConfigurableProperty.class).collectionOfReferences()) {
                Iterator iterator = ((Collection) propertyConfigNode).iterator();
                // collection not empty and first element is not a custom rep
                if (iterator.hasNext() && iterator.next() instanceof Map) {
                    LdapNode thisParent = makeLdapCollectionNode(property);
                    for (Map<String, Object> o : ((Collection<Map<String, Object>>) propertyConfigNode)) {
                        LdapNode elementNode = thisParent.makeLdapElementNode(property, (String) o.get(LdapConfigUtils.getDistinguishingFieldForCollectionElement(property)));
                        elementNode.populate(o, property.getPseudoPropertyForConfigClassCollectionElement().getRawClass());
                    }
                    continue;
                }
            }

            // nested conf object, not custom representation
            if (property.getRawClass().getAnnotation(ConfigurableClass.class) != null)
                if (propertyConfigNode instanceof Map) {
                    LdapNode nestedNode;
                    if (LdapConfigUtils.isNoContainerNode(property)) nestedNode = this;
                    else {
                        nestedNode = new LdapNode();
                        nestedNode.setParent(this);
                        nestedNode.setDn(LdapConfigUtils.dnOf(getDn(), LdapConfigUtils.getDistinguishingField(property), LdapConfigUtils.getLDAPPropertyName(property)));
                        nestedNode.getAttributes().put(LdapConfigUtils.getDistinguishingField(property), LdapConfigUtils.getLDAPPropertyName(property));
                    }
                    nestedNode.populate((Map<String, Object>) propertyConfigNode, property.getRawClass());
                    continue;
                }

            // any other array/collection
            if (propertyConfigNode instanceof Collection) {

                Collection<Object> collection = (Collection<Object>) propertyConfigNode;

                // special case - boolean based enumSet
                LDAP ldapAnno = property.getAnnotation(LDAP.class);
                if (ldapAnno != null && ldapAnno.booleanBasedEnumStorageOptions().length > 0) {
                    int i = 0;
                    for (String enumStorageOption : ldapAnno.booleanBasedEnumStorageOptions())
                        if (collection.contains(i++))
                            getAttributes().put(enumStorageOption, "TRUE");
                    continue;
                }

                // store only if not empty
                if (collection.isEmpty()) continue;

                BasicAttribute attribute = new BasicAttribute(LdapConfigUtils.getLDAPPropertyName(property));

                for (Object o : collection) {
                    String attrVal = o.toString();
                    // handle refs
                    if (property.getAnnotation(ConfigurableProperty.class).collectionOfReferences())
                        attrVal = LdapConfigUtils.refToLdapDN(attrVal, getLdapConfigurationStorage());

                    attribute.add(attrVal);
                }

                getAttributes().put(attribute);
                continue;
            }


            // reference
            if (property.getAnnotation(ConfigurableProperty.class).isReference()) {
                String ref = LdapConfigUtils.refToLdapDN(propertyConfigNode.toString(), getLdapConfigurationStorage());
                getAttributes().put(LdapConfigUtils.getLDAPPropertyName(property), ref);
                continue;
            }


            // regular attribute
            if (propertyConfigNode instanceof Boolean)
                getAttributes().put(LdapConfigUtils.getLDAPPropertyName(property), (Boolean) propertyConfigNode ? "TRUE" : "FALSE");
            else
                getAttributes().put(LdapConfigUtils.getLDAPPropertyName(property), propertyConfigNode.toString());

        }

        // hardcoded workarounds for extensions
        if (configurableClass.equals(Device.class)) {
            fillInExtensions(configNode, "deviceExtensions");
        } else if (configurableClass.equals(ApplicationEntity.class)) {
            fillInExtensions(configNode, "aeExtensions");
        } else if (configurableClass.equals(HL7Application.class)) {
            fillInExtensions(configNode, "hl7AppExtensions");
        }

    }

    private List<String> getObjectClasses(AnnotatedConfigurableProperty property) {

        AnnotatedConfigurableProperty elementAnno = property.getPseudoPropertyForConfigClassCollectionElement();
        if (elementAnno != null) {
            LDAP annotation = elementAnno.getAnnotation(LDAP.class);
            if (annotation != null) return new ArrayList<String>(Arrays.asList(annotation.objectClasses()));
        }

        LDAP propAnno = property.getAnnotation(LDAP.class);
        if (propAnno != null)
            return  Arrays.asList(propAnno.mapEntryObjectClass());

        if (property.isConfObject()) {
            LDAP confObjAnno = (LDAP) property.getRawClass().getAnnotation(LDAP.class);
            if (confObjAnno != null) confObjAnno.objectClasses();
        }

        return new ArrayList<String>();
    }

    private void fillInExtensions(Map<String, Object> configNode, String whichExtensions) throws ConfigurationException {
        Map<String, Map<String, Object>> extensions = (Map<String, Map<String, Object>>) configNode.get(whichExtensions);
        if (extensions != null) {
            for (Map.Entry<String, Map<String, Object>> ext : extensions.entrySet()) {
                Class<?> extClass = null;
                try {
                    extClass = Extensions.getExtensionClassBySimpleName(ext.getKey(), getLdapConfigurationStorage().getAllExtensionClasses());
                } catch (Exception e) {
                    throw new ConfigurationException("Cannot find extension class " + ext.getKey(), e);
                }

                LdapNode extNode = this;
                LDAP ldapAnno = (LDAP) extClass.getAnnotation(LDAP.class);
                if (ldapAnno == null || !ldapAnno.noContainerNode()) {
                    extNode = new LdapNode();
                    String attrID = ldapAnno == null ? LDAP.DEFAULT_DISTINGUISHING_FIELD : ldapAnno.distinguishingField();
                    extNode.setDn(LdapConfigUtils.dnOf(getDn(), attrID, ext.getKey()));
                    extNode.getAttributes().put(attrID, ext.getKey());
                    extNode.setParent(this);
                }

                extNode.populate(ext.getValue(), extClass);
            }
        }
    }

    private LdapNode makeLdapCollectionNode(AnnotatedConfigurableProperty property) throws ConfigurationException {

        LdapNode thisParent;

        if (LdapConfigUtils.isNoContainerNode(property)) {
            thisParent = this;
        } else {
            thisParent = new LdapNode();
            thisParent.setDn(LdapConfigUtils.dnOf(getDn(), "cn", LdapConfigUtils.getLDAPPropertyName(property)));

            LDAP annotation = property.getAnnotation(LDAP.class);
            if (annotation == null || annotation.objectClasses().length == 0)
                thisParent.getObjectClasses().add("dcmCollection");
            else
                thisParent.setObjectClasses(LdapConfigUtils.extractObjectClasses(property));

            thisParent.setParent(this);
        }
        return thisParent;
    }

    private LdapNode makeLdapElementNode(AnnotatedConfigurableProperty property, String key) {
        LdapNode elementNode = new LdapNode();
        String elementDistinguishingField = LdapConfigUtils.getDistinguishingFieldForCollectionElement(property);
        elementNode.setDn(LdapConfigUtils.dnOf(getDn(), elementDistinguishingField, key));
        elementNode.getAttributes().put(elementDistinguishingField, key);
        elementNode.setParent(this);
        return elementNode;
    }

    //<editor-fold desc="getters/setters">
    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public Collection<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Collection<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public Collection<LdapNode> getChildren() {
        return children;
    }

    public void setChildren(Collection<LdapNode> children) {
        this.children = children;
    }

    public Set<String> getChildrenObjectClasses() {
        return childrenObjectClasses;
    }
    //</editor-fold>

}
