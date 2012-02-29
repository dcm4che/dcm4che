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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.api.ConfigurationException;
import org.dcm4che.conf.api.hl7.HL7Configuration;
import org.dcm4che.conf.ldap.ExtendedLdapDicomConfiguration;
import org.dcm4che.net.Device;
import org.dcm4che.net.hl7.HL7Application;
import org.dcm4che.net.hl7.HL7Device;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapHL7Configuration extends ExtendedLdapDicomConfiguration
        implements HL7Configuration {

    public LdapHL7Configuration(Hashtable<String, Object> env, String baseDN)
            throws NamingException {
        super(env, baseDN);
    }

    @Override
    public HL7Application findHL7Application(String name)
            throws ConfigurationException {
        return ((HL7Device) findDevice(
                "(&(objectclass=hl7Application)(hl7ApplicationName=" + name + "))", name))
            .getHL7Application(name);
    }

    protected Attribute objectClassesOf(HL7Application hl7App, Attribute attr) {
        attr.add("hl7Application");
        return attr;
    }

    @Override
    protected Device newDevice(Attributes attrs) throws NamingException {
        return new HL7Device(stringValue(attrs.get("dicomDeviceName")));
    }

    protected HL7Application newHL7Application(Attributes attrs) throws NamingException {
        return new HL7Application(stringValue(attrs.get("hl7ApplicationName")));
    }

    @Override
    protected void storeChilds(String deviceDN, Device device) throws NamingException {
        super.storeChilds(deviceDN, device);
        if (!(device instanceof HL7Device))
            return;
        HL7Device hl7Dev = (HL7Device) device;
        for (HL7Application hl7App : hl7Dev.getHL7Applications()) {
            String appDN = hl7appDN(hl7App.getApplicationName(), deviceDN);
            createSubcontext(appDN, storeTo(hl7App, deviceDN, new BasicAttributes(true)));
            storeChilds(appDN, hl7App);
        }
    }

    protected void storeChilds(String appDN, HL7Application hl7App) {
    }

    private String hl7appDN(String name, String deviceDN) {
        return dnOf("hl7ApplicationName" , name, deviceDN);
    }

    protected Attributes storeTo(HL7Application hl7App, String deviceDN, Attributes attrs) {
        attrs.put(objectClassesOf(hl7App, new BasicAttribute("objectclass")));
        storeNotNull(attrs, "hl7ApplicationName", hl7App.getApplicationName());
        storeNotEmpty(attrs, "hl7AcceptedSendingApplication", hl7App.getAcceptedSendingApplications());
        storeNotEmpty(attrs, "hl7AcceptedMessageType", hl7App.getAcceptedMessageTypes());
        storeConnRefs(attrs, hl7App.getConnections(), deviceDN);
        storeNotNull(attrs, "dicomInstalled", hl7App.getInstalled());
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN) throws NamingException {
        super.loadChilds(device, deviceDN);
        if (!(device instanceof HL7Device))
            return;
        loadHL7Applications((HL7Device) device, deviceDN);
    }

    private void loadHL7Applications(HL7Device device, String deviceDN) throws NamingException {
        NamingEnumeration<SearchResult> ne = search(deviceDN, "(objectclass=hl7Application)");
        try {
            while (ne.hasMore()) {
                device.addHL7Application(
                        loadHL7Application(ne.next(), deviceDN, device));
            }
        } finally {
           safeClose(ne);
        }
    }

    protected HL7Application loadHL7Application(SearchResult sr, String deviceDN,
            HL7Device device) throws NamingException {
        Attributes attrs = sr.getAttributes();
        HL7Application hl7app = newHL7Application(attrs);
        loadFrom(hl7app, attrs);
        for (String connDN : stringArray(attrs.get("dicomNetworkConnectionReference")))
            hl7app.addConnection(findConnection(connDN, deviceDN, device));
        loadChilds(hl7app, sr.getNameInNamespace());
        return hl7app;
    }

    protected void loadChilds(HL7Application hl7app, String hl7appDN) throws NamingException {
    }

    protected void loadFrom(HL7Application hl7app, Attributes attrs) throws NamingException {
        hl7app.setAcceptedSendingApplications(stringArray(attrs.get("hl7AcceptedSendingApplication")));
        hl7app.setAcceptedMessageTypes(stringArray(attrs.get("hl7AcceptedMessageType")));
        hl7app.setInstalled(booleanValue(attrs.get("dicomInstalled"), null));
    }

    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        super.mergeChilds(prev, device, deviceDN);
        if (!(prev instanceof HL7Device && device instanceof HL7Device))
            return;

        mergeHL7Apps((HL7Device) prev, (HL7Device) device, deviceDN);
    }

    private void mergeHL7Apps(HL7Device prevDev, HL7Device dev, String deviceDN)
            throws NamingException {
        for (HL7Application ae : prevDev.getHL7Applications()) {
            String aet = ae.getApplicationName();
            if (dev.getHL7Application(aet) == null)
                destroySubcontextWithChilds(hl7appDN(aet, deviceDN));
        }
        for (HL7Application ae : dev.getHL7Applications()) {
            String aet = ae.getApplicationName();
            HL7Application prevAE = prevDev.getHL7Application(aet);
            if (prevAE == null) {
                String aeDN = hl7appDN(ae.getApplicationName(), deviceDN);
                createSubcontext(aeDN,
                        storeTo(ae, deviceDN, new BasicAttributes(true)));
                storeChilds(aeDN, ae);
            } else
                merge(prevAE, ae, deviceDN);
        }
    }

    private void merge(HL7Application prev, HL7Application app, String deviceDN)
            throws NamingException {
        String appDN = hl7appDN(app.getApplicationName(), deviceDN);
        modifyAttributes(appDN, storeDiffs(prev, app, deviceDN, 
                new ArrayList<ModificationItem>()));
        mergeChilds(prev, app, appDN);
    }

    protected void mergeChilds(HL7Application prev, HL7Application app, String appDN) {
    }

    protected List<ModificationItem> storeDiffs(HL7Application a, HL7Application b,
            String deviceDN, List<ModificationItem> mods) {
        storeDiff(mods, "hl7AcceptedSendingApplication",
                a.getAcceptedSendingApplications(),
                b.getAcceptedSendingApplications());
        storeDiff(mods, "hl7AcceptedMessageType",
                a.getAcceptedMessageTypes(),
                b.getAcceptedMessageTypes());
        storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        return mods;
    }
}
