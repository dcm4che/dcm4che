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
 * Portions created by the Initial Developer are Copyright (C) 2011
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

package org.dcm4che.conf.ldap;

import java.util.Collection;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.dcm4che.conf.api.ConfigurationAlreadyExistsException;
import org.dcm4che.conf.api.ConfigurationManagementException;
import org.dcm4che.conf.api.DeviceManager;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.Device;
import org.dcm4che.net.TransferCapability;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapDeviceManager implements DeviceManager {

    private static final String CN_UNIQUE_AE_TITLES_REGISTRY =
            "cn=Unique AE Titles Registry,cn=DICOM Configuration";
    private static final String CN_DEVICES =
            "cn=Devices,cn=DICOM Configuration";
    private static final String CN_DICOM_CONFIGURATION =
            "cn=DICOM Configuration";

    private final Hashtable<String, Object> env = new Hashtable<String, Object>();
    private DirContext ctx;

    public LdapDeviceManager() {
        setLdapContextFactory("com.sun.jndi.ldap.LdapCtxFactory");
        setLdapURI("ldap://localhost:389");
        env.put("java.naming.ldap.attributes.binary", "dicomVendorData");
    }

    public final void setLdapContextFactory(String className) {
        env.put(Context.INITIAL_CONTEXT_FACTORY, className);
    }

    public final void setLdapURI(String uri) {
        env.put(Context.PROVIDER_URL, uri);
    }

    public final void setSimpleAuthentication(String userDN, String password) {
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDN);
        env.put(Context.SECURITY_CREDENTIALS, password);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        DirContext tmp = ctx;
        if (tmp != null)
            tmp.addToEnvironment(propName, propVal);
        return env.put(propName, propVal);
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        DirContext tmp = ctx;
        if (tmp != null)
            tmp.removeFromEnvironment(propName);
        return env.remove(propName);
    }

    public void connect() throws NamingException {
        if (ctx != null)
            throw new IllegalStateException("Already connected");
        this.ctx = new InitialDirContext(env);
    }

    public void close() {
        safeClose(ctx);
        ctx = null;
    }

    public boolean initConfiguration() throws ConfigurationManagementException {
        try {
            createSubcontext(ctx, CN_DICOM_CONFIGURATION,
                    newAttrs("dicomConfigurationRoot", "cn", "DICOM Configuration"));
            createSubcontext(ctx, CN_DEVICES,
                    newAttrs("dicomDevicesRoot", "cn", "Devices"));
            createSubcontext(ctx, CN_UNIQUE_AE_TITLES_REGISTRY,
                    newAttrs("dicomUniqueAETitlesRegistryRoot", "cn", "Unique AE Titles Registry"));
            return true;
        } catch (NameAlreadyBoundException e) {
            return false;
        } catch (NamingException e) {
            throw new ConfigurationManagementException(e);
        }
    }

    public boolean purgeConfiguration() throws ConfigurationManagementException {
        try {
            destroySubcontext(ctx, CN_DICOM_CONFIGURATION);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        } catch (NamingException e) {
            throw new ConfigurationManagementException(e);
        }
    }

    public boolean registerAETitle(String aet) throws ConfigurationManagementException {
        try {
            createSubcontext(ctx, aetDN(aet, CN_UNIQUE_AE_TITLES_REGISTRY),
                    newAttrs("dicomUniqueAETitle", "dicomAETitle", aet));
            return true;
        } catch (NameAlreadyBoundException e) {
            return false;
        } catch (NamingException e) {
            throw new ConfigurationManagementException(e);
       }
    }

    public boolean unregisterAETitle(String aet) throws ConfigurationManagementException {
        try {
            ctx.destroySubcontext(aetDN(aet, CN_UNIQUE_AE_TITLES_REGISTRY));
            return true;
        } catch (NameNotFoundException e) {
            return false;
        } catch (NamingException e) {
            throw new ConfigurationManagementException(e);
        }
    }

    @Override
    public ApplicationEntity findApplicationEntity(String aet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Device findDevice(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void merge(Device preUpdate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void persist(Device device) throws ConfigurationManagementException {
        String deviceDN = deviceDN(device.getDeviceName());
        boolean rollback = false;
        try {
            createSubcontext(ctx, deviceDN, newAttrs(device));
            rollback = true;
            for (Connection conn : device.listConnections())
                createSubcontext(ctx, dnOf(conn, deviceDN), newAttrs(conn));
            for (ApplicationEntity ae : device.getApplicationEntities()) {
                String aeDN = aetDN(ae.getAETitle(), deviceDN);
                createSubcontext(ctx, aeDN, newAttrs(ae, deviceDN));
                for (TransferCapability tc : ae.getTransferCapabilities())
                    createSubcontext(ctx, dnOf(tc, aeDN), newAttrs(tc));
            }
            rollback = false;
        } catch (NameAlreadyBoundException e) {
            throw new ConfigurationAlreadyExistsException("" + device);
        } catch (NamingException e) {
            throw new ConfigurationManagementException(e);
        } finally {
            if (rollback)
                try {
                    destroySubcontext(ctx, deviceDN);
                } catch (NamingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    private static String dnOf(String attrID, String attrValue, String baseDN) {
        return attrID + '=' + attrValue + ',' + baseDN;
    }

    private static String dnOf(String attrID1, String attrValue1,
            String attrID2, String attrValue2, String baseDN) {
        return attrID1 + '=' + attrValue1 + '+' + attrID2 + '=' + attrValue2 + ','  + baseDN;
    }

    private static String aetDN(String aet, String baseDN) {
        return dnOf("dicomAETitle" ,aet, baseDN);
    }

    private static String deviceDN(String name) {
        return dnOf("dicomDeviceName" ,name, CN_DEVICES);
    }

    private static String dnOf(Connection conn, String deviceDN) {
        String cn = conn.getCommonName();
        return (cn != null)
            ? dnOf("cn", cn , deviceDN)
            : conn.isServer()
                 ? dnOf("dicomHostname", conn.getHostname(), "dicomPort",
                         Integer.toString(conn.getPort()) , deviceDN)
                 : dnOf("dicomHostname", conn.getHostname(), deviceDN);
    }

    private String dnOf(TransferCapability tc, String aeDN) {
        String cn = tc.getCommonName();
        return (cn != null)
            ? dnOf("cn", cn , aeDN)
            : dnOf("dicomSOPClass", tc.getSopClass(),
                   "dicomTransferRole", tc.getRole().toString(), aeDN);
    }

    private static Attributes newAttrs(String objectclass, String attrID, String attrVal) {
        Attributes attrs = new BasicAttributes(true); // case-ignore
        attrs.put("objectclass", objectclass);
        put(attrs, attrID, attrVal);
        return attrs;
    }

    private static void put(Attributes attrs, String attrID, Boolean val) {
        if (val != null)
            attrs.put(attrID, val.booleanValue());
    }

    private static void put(Attributes attrs, String attrID, boolean val) {
        attrs.put(attrID, val ? "TRUE" : "FALSE");
    }

    private static void put(Attributes attrs, String attrID, Object val) {
        if (val != null)
            attrs.put(attrID, val);
    }

    private static <T> void put(Attributes attrs, String attrID, Collection<T> vals) {
        if (!vals.isEmpty()) {
            Attribute attr = new BasicAttribute(attrID);
            for (T val : vals)
                attr.add(val);
            attrs.put(attr);
        }
    }

    private static void putDNs(Attributes attrs, String attrID, Collection<Connection> conns,
            String deviceDN) {
        if (!conns.isEmpty()) {
            Attribute attr = new BasicAttribute(attrID);
            for (Connection conn : conns)
                attr.add(dnOf(conn, deviceDN));
            attrs.put(attr);
        }
    }


    private Attributes newAttrs(Device device) {
        Attributes attrs = newAttrs("dicomDevice", "dicomDeviceName", device.getDeviceName());
        put(attrs, "dicomDescription", device.getDescription());
        put(attrs, "dicomManufacturer", device.getManufacturer());
        put(attrs, "dicomManufacturerModelName", device.getManufacturerModelName());
        put(attrs, "dicomSoftwareVersion", device.getSoftwareVersion());
        put(attrs, "dicomStationName", device.getStationName());
        put(attrs, "dicomDeviceSerialNumber", device.getDeviceSerialNumber());
        put(attrs, "dicomIssuerOfPatientID", device.getIssuerOfPatientID());
        put(attrs, "dicomInstitutionName",device.getInstitutionNames());
        put(attrs, "dicomInstitutionAddress",device.getInstitutionAddresses());
        put(attrs, "dicomInstitutionalDepartmentName",device.getInstitutionalDepartmentNames());
        put(attrs, "dicomPrimaryDeviceType", device.getPrimaryDeviceTypes());
        put(attrs, "dicomRelatedDeviceReference", device.getRelatedDeviceRefs());
        put(attrs, "dicomAuthorizedNodeCertificateReference",
                device.getAuthorizedNodeCertificateRefs());
        put(attrs, "dicomThisNodeCertificateReference", device.getThisNodeCertificateRefs());
        put(attrs, "dicomVendorData", device.getVendorData());
        put(attrs, "dicomInstalled", device.isInstalled());
        // TODO
        return attrs;
    }

    private Attributes newAttrs(Connection conn) {
        Attributes attrs = newAttrs("dicomNetworkConnection", "cn", conn.getCommonName());
        put(attrs, "dicomHostname", conn.getHostname());
        if (conn.isServer())
            attrs.put("dicomPort", Integer.toString(conn.getPort()));
        put(attrs, "dicomTLSCipherSuite", conn.getTlsCipherSuite());
        put(attrs, "dicomInstalled", conn.getInstalled());
        // TODO
        return attrs;
    }

    private Attributes newAttrs(ApplicationEntity ae, String deviceDN) {
        Attributes attrs = newAttrs("dicomNetworkAE", "dicomAETitle", ae.getAETitle());
        put(attrs, "dicomDescription", ae.getDescription());
        put(attrs, "dicomVendorData", ae.getVendorData());
        put(attrs, "dicomApplicationCluster", ae.getApplicationClusters());
        put(attrs, "dicomPreferredCallingAETitle", ae.getPreferredCallingAETitles());
        put(attrs, "dicomPreferredCalledAETitle", ae.getPreferredCalledAETitles());
        put(attrs, "dicomAssociationInitiator", ae.isAssociationInitiator());
        put(attrs, "dicomAssociationAcceptor",  ae.isAssociationAcceptor());
        putDNs(attrs, "dicomNetworkConnectionReference", ae.getConnections(), deviceDN);
        put(attrs, "dicomSupportedCharacterSet", ae.getSupportedCharacterSets());
        put(attrs, "dicomInstalled", ae.getInstalled());
        return attrs;
    }


    private Attributes newAttrs(TransferCapability tc) {
        Attributes attrs = newAttrs("dicomTransferCapability", "cn", tc.getCommonName());
        put(attrs, "dicomSOPClass", tc.getSopClass());
        put(attrs, "dicomTransferRole", tc.getRole().toString());
        put(attrs, "dicomTransferSyntax", tc.getTransferSyntaxes());
        return attrs;
    }

    private static void safeClose(Context ctx) {
        if (ctx != null)
            try {
                ctx.close();
            } catch (NamingException e) {
            }
    }

    private static <T> void safeClose(NamingEnumeration<T> enumeration) {
        if (enumeration != null)
            try {
                enumeration.close();
            } catch (NamingException e) {
            }
    }


    @Override
    public void refresh(Device postLoad) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeDevice(String name) {
        // TODO Auto-generated method stub

    }

    private static void createSubcontext(DirContext ctx, String name, Attributes attrs)
            throws NamingException {
        safeClose(ctx.createSubcontext(name, attrs));
    }

    private static void destroySubcontext(DirContext ctx, String name)
            throws NamingException {
        NamingEnumeration<NameClassPair> list = ctx.list(name);
        try {
            if (list.hasMore()) {
                DirContext subContext = (DirContext) ctx.lookup(name);
                try {
                    do {
                        destroySubcontext(subContext,
                                list.next().getName());
                    } while (list.hasMore());
                } finally {
                    safeClose(subContext);
                }
            }
        } finally {
            safeClose(list);
        }
        ctx.destroySubcontext(name);
   }
}
