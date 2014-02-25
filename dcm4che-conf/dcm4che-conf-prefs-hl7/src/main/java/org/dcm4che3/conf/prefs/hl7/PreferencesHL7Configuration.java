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

package org.dcm4che3.conf.prefs.hl7;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.api.hl7.HL7Configuration;
import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.HL7DeviceExtension;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesHL7Configuration 
        extends PreferencesDicomConfigurationExtension
        implements HL7Configuration {

    private static final String HL7_UNIQUE_APPLICATION_NAMES_REGISTRY_ROOT =
            "dicomConfigurationRoot/hl7UniqueApplicationNamesRegistryRoot";

    private final List<PreferencesHL7ConfigurationExtension> extensions =
            new ArrayList<PreferencesHL7ConfigurationExtension>();

    public void addHL7ConfigurationExtension(PreferencesHL7ConfigurationExtension ext) {
        extensions.add(ext);
    }

    public boolean removeHL7ConfigurationExtension(
            PreferencesHL7ConfigurationExtension ext) {
        if (!extensions.remove(ext))
            return false;

        return true;
    }

    @Override
    public boolean registerHL7Application(String name)
            throws ConfigurationException {
        String pathName = applicationNameRegistryPathNameOf(name);
        Preferences rootPrefs = config.getRootPrefs();
        if (PreferencesUtils.nodeExists(rootPrefs , pathName))
            return false;
        try {
            rootPrefs.node(pathName).flush();
            return true;
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public void unregisterHL7Application(String name)
            throws ConfigurationException {
        PreferencesUtils.removeNode(config.getRootPrefs(),
                applicationNameRegistryPathNameOf(name));
    }

    private String applicationNameRegistryPathNameOf(String name) {
        return HL7_UNIQUE_APPLICATION_NAMES_REGISTRY_ROOT + '/' + name;
    }

    @Override
    public String[] listRegisteredHL7ApplicationNames()
            throws ConfigurationException {
        Preferences rootPrefs = config.getRootPrefs();
        if (!PreferencesUtils.nodeExists(
                rootPrefs, HL7_UNIQUE_APPLICATION_NAMES_REGISTRY_ROOT))
            return StringUtils.EMPTY_STRING;

        try {
            return rootPrefs.node(HL7_UNIQUE_APPLICATION_NAMES_REGISTRY_ROOT)
                    .childrenNames();
        } catch (BackingStoreException e) {
            throw new ConfigurationException(e);
        }
    }

    @Override
    public HL7Application findHL7Application(String name)
            throws ConfigurationException {
        Device device = config.findDevice("hl7Application", name);
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        return hl7Ext.getHL7Application(name);
    }

    protected HL7Application newHL7Application(Preferences appNode) {
        return new HL7Application(appNode.name());
    }

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);
        if (hl7Ext == null)
            return;

        List<Connection> devConns = device.listConnections();
        Preferences parent = deviceNode.node("hl7Application");
        for (HL7Application hl7App : hl7Ext.getHL7Applications()) {
            Preferences appNode = parent.node(hl7App.getApplicationName());
            storeTo(hl7App, appNode, devConns);
        }
    }

    private void storeTo(HL7Application hl7App, Preferences prefs,
            List<Connection> devConns) {
        PreferencesUtils.storeNotEmpty(prefs, "hl7AcceptedSendingApplication",
                hl7App.getAcceptedSendingApplications());
        PreferencesUtils.storeNotEmpty(prefs, "hl7AcceptedMessageType",
                hl7App.getAcceptedMessageTypes());
        PreferencesUtils.storeNotNull(prefs, "hl7DefaultCharacterSet",
                hl7App.getHL7DefaultCharacterSet());
        PreferencesUtils.storeNotNull(prefs, "dicomInstalled", hl7App.getInstalled());
        PreferencesUtils.storeConnRefs(prefs, hl7App.getConnections(), devConns);

        for (PreferencesHL7ConfigurationExtension ext : extensions)
            ext.storeTo(hl7App, prefs);
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException, ConfigurationException {
        if (!deviceNode.nodeExists("hl7Application"))
            return;

        HL7DeviceExtension hl7Ext = new HL7DeviceExtension();
        device.addDeviceExtension(hl7Ext);
        List<Connection> devConns = device.listConnections();
        Preferences appsNode = deviceNode.node("hl7Application");
        for (String appName : appsNode.childrenNames()) {
            Preferences appNode = appsNode.node(appName);
            HL7Application hl7app = newHL7Application(appNode);
            loadFrom(hl7app, appNode);
            int n = appNode.getInt("dicomNetworkConnectionReference.#", 0);
            for (int i = 0; i < n; i++) {
                hl7app.addConnection(devConns.get(
                        appNode.getInt("dicomNetworkConnectionReference." + (i+1), 0) - 1));
            }
            hl7Ext.addHL7Application(hl7app);
        }
    }

    private void loadFrom(HL7Application hl7app, Preferences prefs) {
        hl7app.setAcceptedSendingApplications(
                PreferencesUtils.stringArray(prefs, "hl7AcceptedSendingApplication"));
        hl7app.setAcceptedMessageTypes(PreferencesUtils.stringArray(prefs, "hl7AcceptedMessageType"));
        hl7app.setHL7DefaultCharacterSet(prefs.get("hl7DefaultCharacterSet", null));
        hl7app.setInstalled(PreferencesUtils.booleanValue(prefs.get("dicomInstalled", null)));

        for (PreferencesHL7ConfigurationExtension ext : extensions)
            ext.loadFrom(hl7app, prefs);
    }

    @Override
    protected void mergeChilds(Device prev, Device device,
            Preferences deviceNode) throws BackingStoreException {
        HL7DeviceExtension prevHL7Ext = prev.getDeviceExtension(HL7DeviceExtension.class);
        HL7DeviceExtension hl7Ext = device.getDeviceExtension(HL7DeviceExtension.class);

        if (hl7Ext == null) {
            if (prevHL7Ext != null)
                deviceNode.node("hl7Application").removeNode();
            return;
        }

        Preferences appsNode = deviceNode.node("hl7Application");
        if (prevHL7Ext != null)
            for (String appName : prevHL7Ext.getHL7ApplicationNames()) {
                if (!hl7Ext.containsHL7Application(appName))
                    appsNode.node(appName).removeNode();
            }

        List<Connection> devConns = device.listConnections();
        for (HL7Application app : hl7Ext.getHL7Applications()) {
            String appName = app.getApplicationName();
            Preferences appNode = appsNode.node(appName);
            if (prevHL7Ext == null || !prevHL7Ext.containsHL7Application(appName)) {
                storeTo(app, appNode, devConns);
            } else {
                storeDiffs(appNode, prevHL7Ext.getHL7Application(appName), app);
            }
        }
    }

    private void storeDiffs(Preferences prefs, HL7Application a, HL7Application b) {
        PreferencesUtils.storeDiffConnRefs(prefs, 
                a.getConnections(), a.getDevice().listConnections(), 
                b.getConnections(), b.getDevice().listConnections());
        PreferencesUtils.storeDiff(prefs, "hl7AcceptedSendingApplication",
                a.getAcceptedSendingApplications(),
                b.getAcceptedSendingApplications());
        PreferencesUtils.storeDiff(prefs, "hl7AcceptedMessageType",
                a.getAcceptedMessageTypes(),
                b.getAcceptedMessageTypes());
        PreferencesUtils.storeDiff(prefs, "hl7DefaultCharacterSet",
                a.getHL7DefaultCharacterSet(),
                b.getHL7DefaultCharacterSet());
        PreferencesUtils.storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());

        for (PreferencesHL7ConfigurationExtension ext : extensions)
            ext.storeDiffs(a, b, prefs);
    }

}
