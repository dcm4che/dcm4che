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
import org.dcm4che3.conf.api.ConfigurationChanges;
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
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device)
            throws NamingException {
        AuditRecordRepository arr =
                device.getDeviceExtension(AuditRecordRepository.class);
        if (arr != null)
            store(diffs, deviceDN, arr);
    }

    private void store(ConfigurationChanges diffs, String deviceDN, AuditRecordRepository arr)
            throws NamingException {
        String dn = CN_AUDIT_RECORD_REPOSITORY + deviceDN;
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
        config.createSubcontext(dn,
                storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                        arr, deviceDN, new BasicAttributes(true)));
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, AuditRecordRepository arr, String deviceDN,
            Attributes attrs) {
        attrs.put(new BasicAttribute("objectclass", "dcmAuditRecordRepository"));
        LdapUtils.storeConnRefs(ldapObj, attrs, arr.getConnections(),
                deviceDN);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dicomInstalled",
                arr.getInstalled(), null);
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
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN)
            throws NamingException {
        AuditRecordRepository prevARR = prev.getDeviceExtension(AuditRecordRepository.class);
        AuditRecordRepository arr = device.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null && prevARR == null)
            return;

        String dn = CN_AUDIT_RECORD_REPOSITORY + deviceDN;
        if (arr == null) {
            config.destroySubcontextWithChilds(dn);
            ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
        } else if (prevARR == null) {
            store(diffs, deviceDN, arr);
        } else {
            ConfigurationChanges.ModifiedObject ldapObj =
                    ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
            config.modifyAttributes(dn,
                    storeDiffs(ldapObj, prevARR, arr, deviceDN, new ArrayList<ModificationItem>()));
            ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, AuditRecordRepository a,
                                              AuditRecordRepository b, String deviceDN,
                                              ArrayList<ModificationItem> mods) {
        LdapUtils.storeDiff(ldapObj, mods, "dicomNetworkConnectionReference",
                a.getConnections(),
                b.getConnections(),
                deviceDN);
        LdapUtils.storeDiffObject(ldapObj, mods, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled(), null);
        return mods;
    }

}
