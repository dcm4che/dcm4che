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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.dcm4che3.conf.ConfigurationSettingsLoader;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.core.util.PathPattern;
import org.dcm4che3.conf.dicom.CommonDicomConfiguration;
import org.dcm4che3.conf.dicom.DicomPath;


public class LdapConfigurationStorage implements Configuration {

    private String baseDN;
    private InitialDirContext ldapCtx;
    private List<Class> allExtensionClasses;

    public static Hashtable<String, String> collectLDAPProps(Hashtable<?, ?> props) {
        Hashtable<String, String> ldapStringProps = new Hashtable<String, String>();
        ldapStringProps.put("java.naming.provider.url",
                ConfigurationSettingsLoader.getPropertyWithNotice(props,
                        "org.dcm4che.conf.ldap.url",
                        "ldap://localhost:389/dc=example,dc=com"));
        ldapStringProps.put("java.naming.security.principal",
                ConfigurationSettingsLoader.getPropertyWithNotice(props,
                        "org.dcm4che.conf.ldap.principal",
                        "cn=Directory Manager"));
        ldapStringProps.put("java.naming.security.credentials",
                ConfigurationSettingsLoader.getPasswordWithNotice(props,
                        "org.dcm4che.conf.ldap.credentials", "1"));
        ldapStringProps.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
        ldapStringProps.put("java.naming.ldap.attributes.binary", "dicomVendorData");
        return ldapStringProps;
    }

    public List<Class> getAllExtensionClasses() {
        return allExtensionClasses;
    }

    public LdapConfigurationStorage() {
        //NOOP
    }
    
    public LdapConfigurationStorage(Hashtable<?, ?> env, List<Class> allExtensionClasses)
            throws ConfigurationException {
        setEnvironment(env);
        setExtensions(allExtensionClasses);
    }
    
    public void setExtensions(List<Class> allExtensionClasses) {
        this.allExtensionClasses = allExtensionClasses;
    }
    
    public void setEnvironment(Hashtable<?, ?> env) throws ConfigurationException {
        try {
            Hashtable<Object, Object> env_ = (Hashtable<Object, Object>) env.clone();
            String e = (String) env.get("java.naming.provider.url");
            int end = e.lastIndexOf('/');
            env_.put("java.naming.provider.url", e.substring(0, end));
            this.baseDN = e.substring(end + 1);
            // TODO: what happens when LDAP goes down and up again while app is
            // running?
            this.ldapCtx = new InitialDirContext(env_);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public synchronized void destroySubcontextWithChilds(String name) throws NamingException {
        NamingEnumeration list = getLdapCtx().list(new LdapName(name));

        while (list.hasMore()) {
            this.destroySubcontextWithChilds(((NameClassPair) list.next()).getNameInNamespace());
        }

        getLdapCtx().destroySubcontext(new LdapName(name));
    }


    private void merge(LdapNode ldapNode) {
        try {

            mergeIn(ldapNode);

        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> getConfigurationRoot() throws ConfigurationException {
        Object root = getConfigurationNode("/dicomConfigurationRoot", null);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("dicomConfigurationRoot", root);
        return map;
    }

    @Override
    public Object getConfigurationNode(String path, Class configurableClass) throws ConfigurationException {
        // TODO: byte[],x509 to base64
        // special booleanBased EnumSet

        if (path.equals("/dicomConfigurationRoot"))
            configurableClass = CommonDicomConfiguration.DicomConfigurationRootNode.class;

        String dn = LdapConfigUtils.refToLdapDN(path, this);

        try {
            return LdapConfigNodeReader.readNode(this, dn, configurableClass);
        } catch (NamingException e) {
            throw new ConfigurationException("Cannot read node from ldap :" + path, e);
        }
    }

    public void fillExtension(String dn, Map<String, Object> map, String extensionLabel) throws NamingException, ConfigurationException {
        HashMap<String, Object> exts = new HashMap<String, Object>();
        map.put(extensionLabel, exts);

        for (Class<?> aClass : getAllExtensionClasses()) {

            LDAP ldapAnno = aClass.getAnnotation(LDAP.class);

            String subDn;
            if (ldapAnno == null || !ldapAnno.noContainerNode())
                subDn = LdapConfigUtils.dnOf(dn, "cn", aClass.getSimpleName());
            else
                subDn = dn;

            Map ext = (Map) LdapConfigNodeReader.readNode(this, subDn, aClass);
            if (ext == null || ext.isEmpty()) continue;

            exts.put(aClass.getSimpleName(), ext);

        }
    }

    @Override
    public boolean nodeExists(String path) throws ConfigurationException {

        String dn = LdapConfigUtils.refToLdapDN(path, this);

        try {
            Object o = ldapCtx.lookup(new LdapName(dn));
            if (o == null) return false;
        } catch (NameNotFoundException nnfe) {
            return false;
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }

        return true;

    }

    @Override
    public void persistNode(String path, Map<String, Object> configNode, Class configurableClass) throws ConfigurationException {
        // TODO: byte[], x509 from base64
        // dynamic dn generation for lists... maybe allow to use an extension

        String dn = LdapConfigUtils.refToLdapDN(path, this);

        LdapNode ldapNode = new LdapNode(this);
        ldapNode.setDn(dn);
        ldapNode.populate(configNode, configurableClass);


        merge(ldapNode);
    }


    private void mergeIn(LdapNode ldapNode) throws NamingException {

        // merge attributes of this node
        if (!ldapNode.getObjectClasses().isEmpty()) {

            BasicAttribute objectClass = new BasicAttribute("objectClass");
            for (String c : ldapNode.getObjectClasses()) objectClass.add(c);
            ldapNode.getAttributes().put(objectClass);


            Attributes attributes = null;
            try {
                attributes = ldapCtx.getAttributes(new LdapName(ldapNode.getDn()));
            } catch (NameNotFoundException e) {
                // attributes stay null
            }

            if (attributes == null)
                storeAttributes(ldapNode);
            else {
                // TODO: PERFORMANCE: filter out the attributes that did not change
                // Append objectClass
                ldapNode.getAttributes().remove("objectClass");
                Attribute existingObjectClasses = getLdapCtx().getAttributes(ldapNode.getDn(), new String[]{"objectClass"}).get("objectClass");
                for (String c : ldapNode.getObjectClasses())
                    if (!existingObjectClasses.contains(c))
                        existingObjectClasses.add(c);

                ldapNode.getAttributes().put(existingObjectClasses);

                replaceAttributes(ldapNode);
            }
        }

        // remove children that do not exist in the new config
        // see which objectclasses are children of the node and remove them all
        for (String childObjClass : ldapNode.getChildrenObjectClasses()) {

            NamingEnumeration<SearchResult> ne = LdapConfigUtils.searchSubcontextWithClass(this, childObjClass, ldapNode.getDn());

            while (ne.hasMore()) {
                SearchResult sr = (SearchResult) ne.next();
                // TODO: filter out those who dont need to be killed
                try {
                    destroySubcontextWithChilds(sr.getName());
                } catch (NameNotFoundException exception) {
                    //noop, proceed
                }
            }

        }

        // descent recursively
        for (LdapNode child : ldapNode.getChildren()) mergeIn(child);
    }

    private void storeAttributes(LdapNode ldapNode) throws NamingException {
        getLdapCtx().createSubcontext(new LdapName(ldapNode.getDn()), ldapNode.getAttributes());
    }

    private void replaceAttributes(LdapNode ldapNode) throws NamingException {
        getLdapCtx().modifyAttributes(new LdapName(ldapNode.getDn()), DirContext.REPLACE_ATTRIBUTE, ldapNode.getAttributes());
    }

    @Override
    public void refreshNode(String path) throws ConfigurationException {
        // noop, there is no cache
    }

    @Override
    public void removeNode(String path) throws ConfigurationException {
        LdapConfigUtils.BooleanContainer dnIsKillableWrapper = new LdapConfigUtils.BooleanContainer();
        String dn = LdapConfigUtils.refToLdapDN(path, this, dnIsKillableWrapper);
        if (dnIsKillableWrapper.isKillable()) {
            try {
                destroySubcontextWithChilds(dn);
            } catch (NameNotFoundException nnfe) {
                //noop
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }
        }

    }

    @Override
    public Iterator search(String liteXPathExpression) throws IllegalArgumentException, ConfigurationException {

        DicomPath matchingPathType = null;
        PathPattern.PathParser parser = null;
        for (DicomPath pathType : DicomPath.values()) {
            try {
                parser = pathType.parse(liteXPathExpression);
                // if we get here, then the corresponding path has been found
                matchingPathType = pathType;
                break;
            } catch (IllegalArgumentException e) {
                // not this path, try others
            }
        }

        if (parser == null)
            throw new RuntimeException("Ldap config storage does not support this type of query (" + liteXPathExpression + ")");


        String devicesDn = LdapConfigUtils.refToLdapDN("/dicomConfigurationRoot/dicomDevicesRoot", this);

        try {
            SearchControls ctls;
            NamingEnumeration<SearchResult> search;

            switch (matchingPathType) {

                case DeviceNameByAEName:

                    String aeName = parser.getParam("aeName");

                    ctls = new SearchControls();
                    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    ctls.setReturningObjFlag(false);
                    ctls.setCountLimit(1);
                    search = getLdapCtx().search(devicesDn, "(&(objectclass=dicomNetworkAE)(dicomAETitle=" + Rdn.escapeValue(aeName) + "))", ctls);

                    return createSearchIteratorFromNamingEnumeration(search, 2);

                case AllDeviceNames:

                    ctls = new SearchControls();
                    ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
                    ctls.setReturningObjFlag(false);

                    search = getLdapCtx().search(devicesDn, "objectclass=dicomDevice", ctls);

                    return createSearchIteratorFromNamingEnumeration(search, 2);

                case AllAETitles:

                    ctls = new SearchControls();
                    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    ctls.setReturningObjFlag(false);

                    search = getLdapCtx().search(devicesDn, "objectclass=dcmNetworkAE", ctls);

                    return createSearchIteratorFromNamingEnumeration(search, 3);


                case AllHL7AppNames:
                    ctls = new SearchControls();
                    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    ctls.setReturningObjFlag(false);

                    search = getLdapCtx().search(devicesDn, "objectclass=hl7Application", ctls);

                    return createSearchIteratorFromNamingEnumeration(search, 3);


                case DeviceNameByHL7AppName:

                    String hl7AppName = parser.getParam("hl7AppName");

                    ctls = new SearchControls();
                    ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    ctls.setReturningObjFlag(false);

                    search = getLdapCtx().search(devicesDn, "(&(objectclass=hl7Application)(hl7ApplicationName=" + Rdn.escapeValue(hl7AppName) + "))", ctls);

                    return createSearchIteratorFromNamingEnumeration(search, 2);

                default:
                    return null;
            }
        } catch (Exception e) {
            throw new ConfigurationException("Failed to perform LDAP search for query "+liteXPathExpression,e);
        }

    }

    @Override
    public void lock() {
        // no locking
    }

    private Iterator createSearchIteratorFromNamingEnumeration(NamingEnumeration<SearchResult> search, int valIndex) throws NamingException {
        List<String> searchRes = new ArrayList<String>();
        while (search.hasMore()) {
            String nameInNamespace = search.next().getNameInNamespace();
            List<Rdn> rdns = LdapConfigUtils.getNonBaseRdns(nameInNamespace, baseDN);
            searchRes.add((String) rdns.get(valIndex).getValue());
        }
        return searchRes.iterator();
    }

    public String getBaseDN() {
        return baseDN;
    }

    public InitialDirContext getLdapCtx() {
        return ldapCtx;
    }

    @Override
    public void runBatch(ConfigBatch batch) {
        batch.run();
    }
    
}
