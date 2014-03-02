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
package org.dcm4che3.conf.prefs.imageio;

import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dcm4che3.conf.prefs.PreferencesDicomConfigurationExtension;
import org.dcm4che3.conf.prefs.PreferencesUtils;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageReaderFactory.ImageReaderParam;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.imageio.ImageReaderExtension;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class PreferencesImageReaderConfiguration
        extends PreferencesDicomConfigurationExtension {

    @Override
    protected void storeChilds(Device device, Preferences deviceNode) {
        ImageReaderExtension ext =
                device.getDeviceExtension(ImageReaderExtension.class);
        if (ext != null)
            storeTo(ext.getImageReaderFactory(),
                    deviceNode.node("dcmImageReaderFactory"));
    }

    private void storeTo(ImageReaderFactory factory, Preferences prefs) {
        for (Entry<String, ImageReaderParam> entry : factory.getEntries())
            storeTo(entry.getValue(), prefs.node(entry.getKey()));
    }

    private void storeTo(ImageReaderParam param, Preferences prefs) {
        prefs.put("dcmIIOFormatName", param.formatName);
        PreferencesUtils.storeNotNull(prefs, "dcmJavaClassName", param.className);
        PreferencesUtils.storeNotNull(prefs, "dcmPatchJPEGLS", param.patchJPEGLS);
    }

    @Override
    protected void loadChilds(Device device, Preferences deviceNode)
            throws BackingStoreException {
        if (!deviceNode.nodeExists("dcmImageReaderFactory"))
            return;
        
        Preferences prefs = deviceNode.node("dcmImageReaderFactory");
        ImageReaderFactory factory = new ImageReaderFactory();
        for (String tsuid : prefs.childrenNames())
            factory.put(tsuid, load(prefs.node(tsuid)));
    }

    private ImageReaderParam load(Preferences prefs) {
        return new ImageReaderParam(
                prefs.get("dcmIIOFormatName", null),
                prefs.get("dcmJavaClassName", null),
                prefs.get("dcmPatchJPEGLS", null));
    }

    @Override
    protected void mergeChilds(Device prev, Device device, Preferences deviceNode)
            throws BackingStoreException {
        ImageReaderExtension prevExt =
                prev.getDeviceExtension(ImageReaderExtension.class);
        ImageReaderExtension ext =
                device.getDeviceExtension(ImageReaderExtension.class);
        if (ext == null && prevExt == null)
            return;
        
        Preferences factoryNode = deviceNode.node("dcmImageReaderFactory");
        if (ext == null)
            factoryNode.removeNode();
        else if (prevExt == null)
            storeTo(ext.getImageReaderFactory(), factoryNode);
        else
            storeDiffs(factoryNode, prevExt.getImageReaderFactory(),
                    ext.getImageReaderFactory());
    }

    private void storeDiffs(Preferences prefs, ImageReaderFactory prevFactory,
            ImageReaderFactory factory) throws BackingStoreException {
        for (Entry<String, ImageReaderParam> entry : prevFactory.getEntries()) {
            String tsuid = entry.getKey();
            if (factory.get(tsuid) == null) {
                Preferences node = prefs.node(tsuid);
                node.removeNode();
                node.flush();
            }
        }
        for (Entry<String, ImageReaderParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            storeDiffs(prefs.node(tsuid), prevFactory.get(tsuid), entry.getValue());
        }
    }

    private void storeDiffs(Preferences prefs,
            ImageReaderParam prev, ImageReaderParam param) {
        if (prev != null) {
            PreferencesUtils.storeDiff(prefs, "dcmIIOFormatName",
                    prev.formatName, param.formatName);
            PreferencesUtils.storeDiff(prefs, "dcmJavaClassName",
                    prev.className, param.className);
            PreferencesUtils.storeDiff(prefs, "dcmPatchJPEGLS",
                    prev.patchJPEGLS, param.patchJPEGLS);
        } else
            storeTo(param, prefs);
    }
}
