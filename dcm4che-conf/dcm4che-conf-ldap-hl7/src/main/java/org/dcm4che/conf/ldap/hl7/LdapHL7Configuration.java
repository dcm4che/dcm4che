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

package org.dcm4che.conf.ldap.hl7;

import static org.dcm4che.conf.ldap.LdapDicomConfiguration.booleanValue;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.dnOf;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.findConnection;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.safeClose;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeConnRefs;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeDiff;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeNotEmpty;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.storeNotNull;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.stringArray;
import static org.dcm4che.conf.ldap.LdapDicomConfiguration.stringValue;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.hl7.HL7Configuration;
import org.dcm4che.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che.net.Device;
import org.dcm4che.net.hl7.HL7Application;
import org.dcm4che.net.hl7.HL7DeviceExtension;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapHL7Configuration extends LdapDicomConfigurationExtension
        implements HL7Configuration {

    private final List<LdapHL7ConfigurationExtension> extensions =
            new ArrayList<LdapHL7ConfigurationExtension>();

    public void addHL7ConfigurationExtension(LdapHL7ConfigurationExtension ext) {
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(
            LdapHL7ConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        return true;
    }

    @Override
    public synchronized HL7Application findHL7Application(String name)
            throws ConfigurationException {
        Device device = config.findDevice(
            "(&(objectclass=hl7Application)(hl7ApplicationName=" + name + "))",
            name);
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext.getHL7Application(name);
    }

    @Override
    protected void storeChilds(String deviceDN, Device device) throws NamingException {
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        for (HL7Application hl7App : hl7Ext.getHL7Applications()) {
            String appDN = hl7appDN(hl7App.getApplicationName(), deviceDN);
            config.createSubcontext(appDN,
                    storeTo(hl7App, deviceDN, new BasicAttributes(true)));
        }
    }

    private String hl7appDN(String name, String deviceDN) {
        return dnOf("hl7ApplicationName" , name, deviceDN);
    }

    private Attributes storeTo(HL7Application hl7App, String deviceDN, Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "hl7Application"));
        storeNotNull(attrs, "hl7ApplicationName",
                hl7App.getApplicationName());
        storeNotEmpty(attrs, "hl7AcceptedSendingApplication",
                hl7App.getAcceptedSendingApplications());
        storeNotEmpty(attrs, "hl7AcceptedMessageType",
                hl7App.getAcceptedMessageTypes());
        storeNotNull(attrs, "hl7DefaultCharacterSet",
                hl7App.getHL7DefaultCharacterSet());
        storeConnRefs(attrs, hl7App.getConnections(), deviceDN);
        storeNotNull(attrs, "dicomInstalled", hl7App.getInstalled());
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeTo(hl7App, deviceDN, attrs);
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
            safeClose(ne);
        }
    }

    private HL7Application loadHL7Application(SearchResult sr, String deviceDN,
            Device device) throws NamingException {
        Attributes attrs = sr.getAttributes();
        HL7Application hl7app = new HL7Application(stringValue(attrs.get("hl7ApplicationName"), null));
        loadFrom(hl7app, attrs);
        for (String connDN : stringArray(attrs.get("dicomNetworkConnectionReference")))
            hl7app.addConnection(findConnection(connDN, deviceDN, device));

        return hl7app;
    }

    protected void loadFrom(HL7Application hl7app, Attributes attrs) throws NamingException {
        hl7app.setAcceptedSendingApplications(stringArray(attrs.get("hl7AcceptedSendingApplication")));
        hl7app.setAcceptedMessageTypes(stringArray(attrs.get("hl7AcceptedMessageType")));
        hl7app.setHL7DefaultCharacterSet(stringValue(attrs.get("hl7DefaultCharacterSet"), null));
        hl7app.setInstalled(booleanValue(attrs.get("dicomInstalled"), null));
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.loadFrom(hl7app, attrs);
    }

    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        HL7DeviceExtension prevHL7Ext =
                prev.getDeviceExtension(HL7DeviceExtension.class);
        HL7DeviceExtension hl7Ext = 
                device.getDeviceExtension(HL7DeviceExtension.class);

        if (prevHL7Ext != null)
            for (String appName : prevHL7Ext.getHL7ApplicationNames()) {
                if (hl7Ext == null || !hl7Ext.containsHL7Application(appName))
                    config.destroySubcontextWithChilds(hl7appDN(appName, deviceDN));
            }

        if (hl7Ext == null)
            return;

        for (HL7Application hl7app : hl7Ext.getHL7Applications()) {
            String appName = hl7app.getApplicationName();
            HL7Application prevHL7App = prevHL7Ext != null
                    ? prevHL7Ext.getHL7Application(appName)
                    : null;
            if (prevHL7App == null) {
                String appDN = hl7appDN(hl7app.getApplicationName(), deviceDN);
                config.createSubcontext(appDN,
                        storeTo(hl7app, deviceDN, new BasicAttributes(true)));
            } else
                merge(prevHL7App, hl7app, deviceDN);
        }
    }

    private void merge(HL7Application prev, HL7Application app, String deviceDN)
            throws NamingException {
        String appDN = hl7appDN(app.getApplicationName(), deviceDN);
        config.modifyAttributes(appDN, storeDiffs(prev, app, deviceDN, 
                new ArrayList<ModificationItem>()));
    }

    private List<ModificationItem> storeDiffs(HL7Application a, HL7Application b,
            String deviceDN, List<ModificationItem> mods) {
        storeDiff(mods, "hl7AcceptedSendingApplication",
                a.getAcceptedSendingApplications(),
                b.getAcceptedSendingApplications());
        storeDiff(mods, "hl7AcceptedMessageType",
                a.getAcceptedMessageTypes(),
                b.getAcceptedMessageTypes());
        storeDiff(mods, "hl7DefaultCharacterSet",
                a.getHL7DefaultCharacterSet(),
                b.getHL7DefaultCharacterSet());
        storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        for (LdapHL7ConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, mods);
        return mods;
    }
}
