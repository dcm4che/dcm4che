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

import java.lang.reflect.Field;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che3.conf.api.ConfigurationNotFoundException;
import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.generic.ConfigClass;
import org.dcm4che3.conf.api.generic.ConfigField;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigReader;
import org.dcm4che3.conf.api.generic.ReflectiveConfig.ConfigWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapConfigIO implements ConfigWriter, ConfigReader {

    public static final Logger log = LoggerFactory.getLogger(LdapConfigIO.class);

    public static final String FOLDER_OBJECT_CLASS = "dcmCollection";

    /**
     * DN with the whatever extension included
     */
    protected String dn;
    protected LdapDicomConfiguration config;

    private Attributes attrs;
    private List<ModificationItem> mods;

    private boolean writerFlushed = false;

    public LdapConfigIO(Attributes attrs) {
        this.attrs = attrs;
    }

    public LdapConfigIO(List<ModificationItem> mods) {
        this.mods = mods;
    }

    // for common methods
    public LdapConfigIO(Attributes attrs, List<ModificationItem> mods, String dn, LdapDicomConfiguration config) {
        super();
        this.dn = dn;
        this.config = config;
        this.attrs = attrs;
        this.mods = mods;
    }

    // for diff writer
    public LdapConfigIO(List<ModificationItem> mods, String dn, LdapDicomConfiguration config) {
        super();
        this.dn = dn;
        this.config = config;
        this.mods = mods;
        attrs = new BasicAttributes();
    }

    // for writer
    public LdapConfigIO(Attributes attrs, String dn, LdapDicomConfiguration config) {
        super();
        this.attrs = attrs;
        this.dn = dn;
        this.config = config;
    }

    String getFolderDn(String propName) {
        return LdapUtils.dnOf("cn", propName, dn);
    }

    String getCollectionElementDn(String keyName, String keyValue) {
        return LdapUtils.dnOf(keyName, keyValue, dn);
    }

    /**
     * Reader
     */

    @Override
    public String[] asStringArray(String propName) throws ConfigurationException {
        try {
            return LdapUtils.stringArray(attrs.get(propName));
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public int[] asIntArray(String propName) throws ConfigurationException {
        try {
            return LdapUtils.intArray(attrs.get(propName));
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public int asInt(String propName, String def) throws ConfigurationException {
        try {
            return LdapUtils.intValue(attrs.get(propName), Integer.parseInt(def));
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public String asString(String propName, String def) throws ConfigurationException {
        try {
            return LdapUtils.stringValue(attrs.get(propName), def);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public boolean asBoolean(String propName, String def) throws ConfigurationException {
        try {
            return LdapUtils.booleanValue(attrs.get(propName), Boolean.parseBoolean(def));
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public ConfigReader getChildReader(String propName) throws ConfigurationException {
        try {
            return new LdapConfigIO(config.getAttributes(getFolderDn(propName)), getFolderDn(propName), config);
        } catch (NamingException e) {
            throw new ConfigurationNotFoundException(e);
        }
    }

    @Override
    public Map<String, ConfigReader> readCollection(String keyName) throws ConfigurationException {

        try {

            if (dn == null)
                throw new NamingException("dn was not provided for readCollection");

            // do ldap search
            NamingEnumeration<SearchResult> ne =
            // get 'folder' (cn=propName), then lookup all its children with
            // keyname=*

            config.search(dn, String.format("(%s=*)", keyName));
            Map<String, ConfigReader> map = new HashMap<String, ConfigReader>();

            try {

                while (ne.hasMore()) {
                    SearchResult sr = ne.next();

                    Attributes attrs = sr.getAttributes();
                    String keyValue = LdapUtils.stringValue(attrs.get(keyName), null);

                    if (keyValue == null)
                        throw new NamingException("A key attribute for a map cannot be read");

                    String collectionElementDn = getCollectionElementDn(keyName, keyValue);

                    // generate a reader for nested node (i.e. map entry)
                    map.put(keyValue, new LdapConfigIO(attrs, collectionElementDn, config));
                }

                return map;

            } finally {
                LdapUtils.safeClose(ne);
            }
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Writer
     */

    @Override
    public void storeNotDef(String propName, Object value, String def) {

        if (value instanceof Boolean) {
            LdapUtils.storeNotDef(attrs, propName, (Boolean) value, Boolean.parseBoolean(def));
        } else if (value instanceof Integer) {
            LdapUtils.storeNotDef(attrs, propName, (Integer) value, Integer.parseInt(def));
        }
    }

    @Override
    public void storeNotEmpty(String propName, Object value) {

        // check needed since varargs would perceive as Object not as Object[]
        if (value != null && value.getClass().isArray())
            LdapUtils.storeNotEmpty(attrs, propName, (Object[]) value);
        else
            log.error("Cannot use storeNotEmpty for a non-array");

    }

    @Override
    public void storeNotNull(String propName, Object value) {

        // check needed since varargs would perceive as Object not as Object[]
        if (value != null && value.getClass().isArray())
            LdapUtils.storeNotNull(attrs, propName, (Object[]) value);
        LdapUtils.storeNotNull(attrs, propName, value);

    }

    @Override
    public void flushWriter() throws ConfigurationException {

        // if flush enabled, do replace attributes
        if (config != null && !writerFlushed) {
            try {
                config.createSubcontext(dn, attrs);
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }
        }

        writerFlushed = true;

    }

    @Override
    public ConfigWriter createCollectionChild(String propName, Field field) throws ConfigurationException {
        String folderDn = getFolderDn(propName);

        // create 'folder'
        try {
            Attributes attrs = new BasicAttributes(true);
            attrs.put("cn", propName);
            attrs.put("objectClass", FOLDER_OBJECT_CLASS);
            config.createSubcontext(folderDn, attrs);
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }

        return new LdapConfigIO(new BasicAttributes(true), folderDn, config);
    }

    @Override
    public ConfigWriter getCollectionElementWriter(String keyName, String keyValue, Field field) throws ConfigurationException {

        ParameterizedType pt = (ParameterizedType) field.getGenericType();

        Type[] ptypes = pt.getActualTypeArguments();

        // there must be only 2 parameterized types
        if (ptypes.length != 2)
            throw new MalformedParameterizedTypeException();

        // figure out the class of declared generic parameter
        Class clazz = (Class) ptypes[1];

        Attributes attrs = new BasicAttributes(true);
        ConfigField fieldAnno = field.getAnnotation(ConfigField.class);
        ConfigClass classAnno = (ConfigClass) clazz.getAnnotation(ConfigClass.class);

        if (classAnno != null) {
            attrs.put("objectClass", classAnno.objectClass());
        } else if (!fieldAnno.mapElementObjectClass().equals("")) {
            attrs.put("objectClass", fieldAnno.mapElementObjectClass());
        } else
            throw new ConfigurationException("objectClass for collection element cannot be resolved, key (" + keyName + " - " + keyValue
                    + ")");

        return new LdapConfigIO(attrs, getCollectionElementDn(keyName, keyValue), config);
    }

    @Override
    public void flushDiffs() throws ConfigurationException {

        // if flush enabled, merge
        if (config != null)
            try {
                config.modifyAttributes(dn, mods);
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }

    }

    @Override
    public void removeCollectionElement(String keyName, String keyValue) throws ConfigurationException {
        try {
            config.destroySubcontextWithChilds(getCollectionElementDn(keyName, keyValue));
        } catch (NamingException e) {
            throw new ConfigurationException();
        }
    };

    @Override
    public ConfigWriter getCollectionElementDiffWriter(String keyName, String keyValue) {
        return new LdapConfigIO(new ArrayList<ModificationItem>(), getCollectionElementDn(keyName, keyValue), config);
    }

    @Override
    public ConfigWriter getChildWriter(String propName, Field field) throws ConfigurationException {

        Attributes attrs = new BasicAttributes();

        ConfigClass classAnno = (ConfigClass) field.getType().getAnnotation(ConfigClass.class);
        if (classAnno != null) {
//            throw new ConfigurationException("Ldap object class for the child node is unknown");
            attrs.put("objectClass", classAnno.objectClass());
        }

        return new LdapConfigIO(attrs,new ArrayList<ModificationItem>(), getFolderDn(propName), config);
    }

    @Override
    public void storeDiff(String propName, Object prev, Object curr) {

        if (prev != null && curr != null && prev.getClass().isArray() && curr.getClass().isArray())
            LdapUtils.storeDiff(mods, propName, (Object[]) prev, (Object[]) curr);
        else
            LdapUtils.storeDiff(mods, propName, prev, curr);
    }

    @Override
    public void removeCurrentNode() throws ConfigurationException {
        try {
            config.destroySubcontextWithChilds(dn);
        } catch (NamingException e) {
            throw new ConfigurationException();
        }
    }

}