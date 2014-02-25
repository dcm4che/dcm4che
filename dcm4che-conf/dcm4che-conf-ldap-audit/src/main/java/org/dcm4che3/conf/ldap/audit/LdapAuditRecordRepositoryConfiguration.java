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

package org.dcm4che3.conf.ldap.audit;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapAuditRecordRepositoryConfiguration extends LdapDicomConfigurationExtension {

    private static final String CN_AUDIT_RECORD_REPOSITORY =
            "cn=Audit Record Repository,";

    @Override
    protected void storeChilds(String deviceDN, Device device)
            throws NamingException {
        AuditRecordRepository arr =
                device.getDeviceExtension(AuditRecordRepository.class);
        if (arr != null)
            store(deviceDN, arr);
    }

    private void store(String deviceDN, AuditRecordRepository arr)
            throws NamingException {
        config.createSubcontext(CN_AUDIT_RECORD_REPOSITORY + deviceDN,
                storeTo(arr, deviceDN, new BasicAttributes(true)));
    }

    private Attributes storeTo(AuditRecordRepository arr, String deviceDN,
            Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditRecordRepository"));
        LdapUtils.storeConnRefs(attrs, arr.getConnections(),
                deviceDN);
        LdapUtils.storeNotNull(attrs, "dicomInstalled",
                arr.getInstalled());
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN)
            throws NamingException {
        Attributes attrs;
        try {
            attrs = config.getAttributes(CN_AUDIT_RECORD_REPOSITORY + deviceDN);
        } catch (NameNotFoundException e) {
            return;
        }
        AuditRecordRepository arr = new AuditRecordRepository();
        loadFrom(arr, attrs);
        for (String connDN : LdapUtils.stringArray(
                attrs.get("dicomNetworkConnectionReference")))
            arr.addConnection(
                    LdapUtils.findConnection(connDN, deviceDN, device));
        device.addDeviceExtension(arr);
    }

    private void loadFrom(AuditRecordRepository arr, Attributes attrs) throws NamingException {
        arr.setInstalled(
                LdapUtils.booleanValue(attrs.get("dicomInstalled"), null));
    }

    @Override
    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException {
        AuditRecordRepository prevARR = prev.getDeviceExtension(AuditRecordRepository.class);
        AuditRecordRepository arr = device.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null) {
            if (prevARR != null)
                config.destroySubcontextWithChilds(CN_AUDIT_RECORD_REPOSITORY + deviceDN);
            return;
        }
        if (prevARR == null) {
            store(deviceDN, arr);
            return;
        }
        config.modifyAttributes(CN_AUDIT_RECORD_REPOSITORY + deviceDN,
                storeDiffs(prevARR, arr, deviceDN, new ArrayList<ModificationItem>()));
    }

    private List<ModificationItem> storeDiffs(AuditRecordRepository a,
            AuditRecordRepository b, String deviceDN,
            ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiff(mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
        return mods;
    }

}
