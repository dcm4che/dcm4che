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
 * Portions created by the Initial Developer are Copyright (C) 2013
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

package org.dcm4che3.conf.prefs.audit;

import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.audit.AuditRecordRepository;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesAuditRecordRepositoryConfiguration extends
        PreferencesDicomConfigurationExtension {

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        AuditRecordRepository arr =
                device.getDeviceExtension(AuditRecordRepository.class);
        if (arr != null)
            storeTo(arr, deviceNode.node("dcmAuditRecordRepository"));
    }

    private void storeTo(AuditRecordRepository arr, Preferences prefs) {
        PreferencesUtils.storeConnRefs(prefs, arr.getConnections(), arr.getDevice().listConnections());
        PreferencesUtils.storeNotNull(prefs, "dicomInstalled", arr.getInstalled());
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException {
        if (!deviceNode.nodeExists("dcmAuditRecordRepository"))
            return;
        
        List<Connection> devConns = device.listConnections();
        Preferences arrNode = deviceNode.node("dcmAuditRecordRepository");
        AuditRecordRepository arr = new AuditRecordRepository();
        loadFrom(arr, arrNode);
        int n = arrNode.getInt("dicomNetworkConnectionReference.#", 0);
        for (int i = 0; i < n; i++) {
            arr.addConnection(devConns.get(
                    arrNode.getInt("dicomNetworkConnectionReference." + (i+1), 0) - 1));
        }
        device.addDeviceExtension(arr);
    }

    private void loadFrom(AuditRecordRepository arr, Preferences prefs) {
        arr.setInstalled(PreferencesUtils.booleanValue(prefs.get("dicomInstalled", null)));
    }

    @Override
    protected void mergeChilds(Device prev, Device device, Preferences deviceNode)
            throws BackingStoreException {
        AuditRecordRepository prevARR =
                prev.getDeviceExtension(AuditRecordRepository.class);
        AuditRecordRepository arr =
                device.getDeviceExtension(AuditRecordRepository.class);
        if (arr == null && prevARR == null)
            return;
        
        Preferences arrNode = deviceNode.node("dcmAuditRecordRepository");
        if (arr == null)
            arrNode.removeNode();
        else if (prevARR == null)
            storeTo(arr, arrNode);
        else
            storeDiffs(arrNode, prevARR, arr);
    }

    private void storeDiffs(Preferences prefs, AuditRecordRepository a,
            AuditRecordRepository b) {
        PreferencesUtils.storeDiffConnRefs(prefs, 
                a.getConnections(), a.getDevice().listConnections(), 
                b.getConnections(), b.getDevice().listConnections());
        PreferencesUtils.storeDiff(prefs, "dicomInstalled",
                a.getInstalled(),
                b.getInstalled());
    }
}
