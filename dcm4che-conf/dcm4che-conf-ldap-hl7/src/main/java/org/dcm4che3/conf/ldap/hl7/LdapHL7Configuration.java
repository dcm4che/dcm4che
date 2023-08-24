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
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
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

package org.dcm4che3.conf.ldap.hl7;

import org.dcm4che3.conf.api.ConfigurationChanges;
import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.hl7.HL7ApplicationAlreadyExistsException;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7ApplicationInfo;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.util.StringUtils;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapHL7Configuration extends LdapDicomConfigurationExtension
        implements HL7Configuration {

    private static final String CN_UNIQUE_HL7_APPLICATION_NAMES_REGISTRY =
            "cn=Unique HL7 Application Names Registry,";

    private String appNamesRegistryDN;

    private final List<LdapHL7ConfigurationExtension> extensions =
            new ArrayList<LdapHL7ConfigurationExtension>();

    static final String[] HL7_ATTRS = {
            "dicomDeviceName",
            "hl7ApplicationName",
            "hl7OtherApplicationName",
            "dicomDescription",
            "dicomApplicationCluster",
            "dicomInstalled",
            "dicomNetworkConnectionReference"
    };

    public void addHL7ConfigurationExtension(LdapHL7ConfigurationExtension ext) {
        ext.setHL7Configuration(this);
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(
            LdapHL7ConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        ext.setHL7Configuration(null);
        return true;
    }

    @Override
    public boolean registerHL7Application(String name)
            throws ConfigurationException {
        try {
            registerHL7App(name);
            return true;
        } catch (HL7ApplicationAlreadyExistsException e) {
            return false;
        }
    }

    private String registerHL7App(String name) throws ConfigurationException {
        ensureAppNamesRegistryExists();
        try {
            String dn = hl7appDN(name, appNamesRegistryDN);
            config.createSubcontext(dn,
                    LdapUtils.attrs("hl7UniqueApplicationName", "hl7ApplicationName", name));
            return dn;
        } catch (NameAlreadyBoundException e) {
            throw new HL7ApplicationAlreadyExistsException("HL7 Application '" + name + "' already exists");
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void unregisterHL7Application(String name)
            throws ConfigurationException {
        if (appNamesRegistryExists())
            try {
                config.destroySubcontext(hl7appDN(name, appNamesRegistryDN));
            } catch (NameNotFoundException e) {
            } catch (NamingException e) {
                throw new ConfigurationException(e);
            }
    }

    private void ensureAppNamesRegistryExists() throws ConfigurationException {
        if (appNamesRegistryDN != null)
            return;
        
        config.ensureConfigurationExists();
        String dn = CN_UNIQUE_HL7_APPLICATION_NAMES_REGISTRY
                + config.getConfigurationDN();
        try {
            if (!config.exists(dn))
                config.createSubcontext(dn,
                        LdapUtils.attrs("hl7UniqueApplicationNamesRegistryRoot", 
                                "cn", "Unique HL7 Application Names Registry"));
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
        appNamesRegistryDN = dn;
    }

    private boolean appNamesRegistryExists() throws ConfigurationException {
        if (appNamesRegistryDN != null)
            return true;
        
        if (!config.configurationExists())
            return false;
        
        String dn = CN_UNIQUE_HL7_APPLICATION_NAMES_REGISTRY
                + config.getConfigurationDN();
        try {
            if (!config.exists(dn))
                return false;
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        }
        
        appNamesRegistryDN = dn;
        return true;
    }

    @Override
    public String[] listRegisteredHL7ApplicationNames()
            throws ConfigurationException {
        if (!appNamesRegistryExists())
            return StringUtils.EMPTY_STRING;

        return config.list(appNamesRegistryDN, 
                "(objectclass=hl7UniqueApplicationName)", "hl7ApplicationName");
    }

    @Override
    public HL7Application findHL7Application(String name)
            throws ConfigurationException {
        Device device = config.findDevice(
            "(&(objectclass=hl7Application)(hl7ApplicationName=" + name + "))",
            name);
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext.getHL7Application(name);
    }

    @Override
    public synchronized HL7ApplicationInfo[] listHL7AppInfos(HL7ApplicationInfo keys) throws ConfigurationException {
        if (!config.configurationExists())
            return new HL7ApplicationInfo[0];

        ArrayList<HL7ApplicationInfo> results = new ArrayList<HL7ApplicationInfo>();
        NamingEnumeration<SearchResult> ne = null;
        try {
            String deviceName = keys.getDeviceName();
            ne = config.search(deviceName, HL7_ATTRS, toFilter(keys));
            Map<String, Connection> connCache = new HashMap<>();
            while (ne.hasMore()) {
                HL7ApplicationInfo hl7AppInfo = new HL7ApplicationInfo();
                SearchResult ne1 = ne.next();
                loadFrom(hl7AppInfo, ne1.getAttributes(),
                        deviceName != null ? deviceName : LdapUtils.cutDeviceName(ne1.getName()), connCache);
                results.add(hl7AppInfo);
            }
        } catch (NameNotFoundException e) {
            return new HL7ApplicationInfo[0];
        } catch (NamingException e) {
            throw new ConfigurationException(e);
        } finally {
            LdapUtils.safeClose(ne);
        }
        return results.toArray(new HL7ApplicationInfo[results.size()]);
    }

    private void loadFrom(HL7ApplicationInfo hl7AppInfo, Attributes attrs, String deviceName,
            Map<String, Connection> connCache)
        throws NamingException, ConfigurationException {
        hl7AppInfo.setDeviceName(deviceName);
        hl7AppInfo.setHl7ApplicationName(
                LdapUtils.stringValue(attrs.get("hl7ApplicationName"), null));
        hl7AppInfo.setHl7OtherApplicationName(
                LdapUtils.stringArray(attrs.get("hl7OtherApplicationName")));
        hl7AppInfo.setDescription(
                LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        hl7AppInfo.setApplicationClusters(LdapUtils.stringArray(attrs.get("dicomApplicationCluster")));
        hl7AppInfo.setInstalled(
                LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            hl7AppInfo.getConnections().add(config.findConnection(connDN, connCache));
    }


    private String toFilter(HL7ApplicationInfo keys) {
        if (keys == null)
            return "(objectclass=hl7Application)";

        StringBuilder sb = new StringBuilder();
        sb.append("(&(objectclass=hl7Application)");
        appendFilter("hl7ApplicationName", keys.getHl7ApplicationName(), sb);
        appendFilter("hl7OtherApplicationName", keys.getHl7ApplicationName(), sb);
        appendFilter("dicomApplicationCluster", keys.getApplicationClusters(), sb);
        sb.append(")");
        return sb.toString();
    }

    private void appendFilter(String attrid, String value, StringBuilder sb) {
        if (value == null)
            return;

        sb.append('(').append(attrid).append('=').append(value).append(')');
    }

    private void appendFilter(String attrid, String[] values, StringBuilder sb) {
        if (values.length == 0)
            return;

        if (values.length == 1) {
            appendFilter(attrid, values[0], sb);
            return;
        }

        sb.append("(|");
        for (String value : values)
            appendFilter(attrid, value, sb);
        sb.append(")");
    }

    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (HL7Application hl7App : hl7Ext.getHL7Applications())
            store(diffs, hl7App, deviceDN);
    }

    private void store(ConfigurationChanges diffs, HL7Application hl7App, String deviceDN) throws NamingException {
        String appDN = hl7appDN(hl7App.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, appDN, ConfigurationChanges.ChangeType.C);
        config.createSubcontext(appDN,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        hl7App, deviceDN, new BasicAttributes(true)));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeChilds(ConfigurationChanges.nullifyIfNotVerbose(diffs, diffs), appDN, hl7App);
    }

    private String hl7appDN(String name, String deviceDN) {
        return LdapUtils.dnOf("hl7ApplicationName" , name, deviceDN);
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, HL7Application hl7App, String deviceDN, Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "hl7Application"));
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "hl7ApplicationName",
                hl7App.getApplicationName(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "hl7AcceptedSendingApplication",
                hl7App.getAcceptedSendingApplications());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "hl7OtherApplicationName", hl7App.getOtherApplicationNames());
        LdapUtils.storeNotEmpty(ldapObj, attrs, "hl7AcceptedMessageType",
                hl7App.getAcceptedMessageTypes());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "hl7DefaultCharacterSet",
                hl7App.getHL7DefaultCharacterSet(), "ASCII");
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "hl7SendingCharacterSet",
                hl7App.getHL7SendingCharacterSet(), "ASCII");
        LdapUtils.storeNotEmpty(ldapObj, attrs, "hl7OptionalMSHField", hl7App.getOptionalMSHFields());
        LdapUtils.storeConnRefs(ldapObj, attrs, hl7App.getConnections(), deviceDN);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomDescription", hl7App.getDescription(), null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dicomApplicationCluster", hl7App.getApplicationClusters());
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled", hl7App.getInstalled(), null);
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeTo(ldapObj, hl7App, deviceDN, attrs);
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        NamingEnumeration<SearchResult> ne =
                config.search(deviceDN, "(objectclass=hl7Application)");
        try {
            if (!ne.hasMore())
                return;

            HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
            device.addDeviceExtension(hl7Ext);
            do {
                hl7Ext.addHL7Application(
                        loadHL7Application(ne.next(), deviceDN, device));
            } while (ne.hasMore());
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

    private HL7Application loadHL7Application(SearchResult sr, String deviceDN,
            Device device) throws NamingException, ConfigurationException {
        Attributes attrs = sr.getAttributes();
        HL7Application hl7app = new HL7Application(LdapUtils.stringValue(attrs.get("hl7ApplicationName"), null));
        loadFrom(hl7app, attrs);
        for (String connDN : LdapUtils.stringArray(attrs.get("dicomNetworkConnectionReference")))
            hl7app.addConnection(LdapUtils.findConnection(connDN, deviceDN, device));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.loadChilds(hl7app, sr.getNameInNamespace());
        return hl7app;
    }

    private void loadFrom(HL7Application hl7app, Attributes attrs) throws NamingException {
        hl7app.setAcceptedSendingApplications(LdapUtils.stringArray(attrs.get("hl7AcceptedSendingApplication")));
        hl7app.setOtherApplicationNames(LdapUtils.stringArray(attrs.get("hl7OtherApplicationName")));
        hl7app.setAcceptedMessageTypes(LdapUtils.stringArray(attrs.get("hl7AcceptedMessageType")));
        hl7app.setHL7DefaultCharacterSet(LdapUtils.stringValue(attrs.get("hl7DefaultCharacterSet"), "ASCII"));
        hl7app.setHL7SendingCharacterSet(LdapUtils.stringValue(attrs.get("hl7SendingCharacterSet"), "ASCII"));
        hl7app.setOptionalMSHFields(LdapUtils.intArray(attrs.get("hl7OptionalMSHField")));
        hl7app.setDescription(LdapUtils.stringValue(attrs.get("dicomDescription"), null));
        hl7app.setApplicationClusters(LdapUtils.stringArray(attrs.get("dicomApplicationCluster")));
        hl7app.setInstalled(LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.loadFrom(hl7app, attrs);
    }

    @Override
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException {
        HL7DeviceExtension prevHL7Ext =
                prev.getDeviceExtension(HL7DeviceExtension.class);
        HL7DeviceExtension hl7Ext = 
                device.getDeviceExtension(HL7DeviceExtension.class);

        if (prevHL7Ext != null)
            for (String appName : prevHL7Ext.getHL7ApplicationNames()) {
                if (hl7Ext == null || !hl7Ext.containsHL7Application(appName)) {
                    config.destroySubcontextWithChilds(hl7appDN(appName, deviceDN));
                    ConfigurationChanges.addModifiedObject(diffs, hl7appDN(appName, deviceDN), ConfigurationChanges.ChangeType.D);
                }
            }

        if (hl7Ext == null)
            return;

        for (HL7Application hl7app : hl7Ext.getHL7Applications()) {
            String appName = hl7app.getApplicationName();
            if (prevHL7Ext == null || !prevHL7Ext.containsHL7Application(appName)) {
                store(diffs, hl7app, deviceDN);
            }
            else
                merge(diffs, prevHL7Ext.getHL7Application(appName), hl7app, deviceDN);
        }
    }

    private void merge(ConfigurationChanges diffs, HL7Application prev, HL7Application app, String deviceDN)
            throws NamingException {
        String appDN = hl7appDN(app.getApplicationName(), deviceDN);
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, appDN, ConfigurationChanges.ChangeType.U);
        config.modifyAttributes(appDN, storeDiffs(ldapObj, prev, app, deviceDN,
                new ArrayList<ModificationItem>()));
        ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.mergeChilds(diffs, prev, app, appDN);
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, HL7Application a, HL7Application b,
                                              String deviceDN, List<ModificationItem> mods) {
        LdapUtils.storeDiff(ldapObj, mods, "hl7AcceptedSendingApplication",
                a.getAcceptedSendingApplications(),
                b.getAcceptedSendingApplications());
        LdapUtils.storeDiff(ldapObj, mods, "hl7OtherApplicationName",
                a.getOtherApplicationNames(),
                b.getOtherApplicationNames());
        LdapUtils.storeDiff(ldapObj, mods, "hl7AcceptedMessageType",
                a.getAcceptedMessageTypes(),
                b.getAcceptedMessageTypes());
        LdapUtils.storeDiffObject(ldapObj, mods, "hl7DefaultCharacterSet",
                a.getHL7DefaultCharacterSet(),
                b.getHL7DefaultCharacterSet(), "ASCII");
        LdapUtils.storeDiffObject(ldapObj, mods, "hl7SendingCharacterSet",
                a.getHL7SendingCharacterSet(),
                b.getHL7SendingCharacterSet(), "ASCII");
        LdapUtils.storeDiff(ldapObj, mods, "hl7OptionalMSHField",
                a.getOptionalMSHFields(),
                b.getOptionalMSHFields());
        LdapUtils.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomDescription",
                a.getDescription(),
                b.getDescription(), null);
        LdapUtils.storeDiff(ldapObj, mods, "dicomApplicationCluster",
                a.getApplicationClusters(),
                b.getApplicationClusters());
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeDiffs(ldapObj, a, b, mods);
        return mods;
    }



    @Override
    protected void register(Device device, List<String> dns) throws ConfigurationException {
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (String name : hl7Ext.getHL7ApplicationNames()) {
            if (!name.equals("*"))
                dns.add(registerHL7App(name));
        }
    }

    @Override
    protected void registerDiff(Device prev, Device device, List<String> dns) throws ConfigurationException {
        HL7DeviceExtension prevHL7Ext = prev.getDeviceExtension(HL7DeviceExtension.class);
        if (prevHL7Ext == null) {
            register(device, dns);
            return;
        }

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (String name : hl7Ext.getHL7ApplicationNames()) {
            if (!name.equals("*") && prevHL7Ext.getHL7Application(name) == null)
                dns.add(registerHL7App(name));
        }
    }

    @Override
    protected void markForUnregister(Device prev, Device device, List<String> dns) {
        HL7DeviceExtension prevHL7Ext = prev.getDeviceExtension(HL7DeviceExtension.class);
        if (prevHL7Ext == null)
            return;

        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        for (String name : prevHL7Ext.getHL7ApplicationNames()) {
            if (!name.equals("*") && (hl7Ext == null || hl7Ext.getHL7Application(name) == null))
                dns.add(hl7appDN(name, appNamesRegistryDN));
        }
    }

    @Override
    protected void markForUnregister(String deviceDN, List<String> dns)
            throws NamingException, ConfigurationException {
        if (!appNamesRegistryExists())
            return;

        NamingEnumeration<SearchResult> ne =
                config.search(deviceDN, "(objectclass=hl7Application)", StringUtils.EMPTY_STRING);
        try {
            while (ne.hasMore()) {
                String rdn = ne.next().getName();
                if (!rdn.equals("hl7ApplicationName=*"))
                    dns.add(rdn + ',' + appNamesRegistryDN);
            }
        } finally {
            LdapUtils.safeClose(ne);
        }
    }

}
