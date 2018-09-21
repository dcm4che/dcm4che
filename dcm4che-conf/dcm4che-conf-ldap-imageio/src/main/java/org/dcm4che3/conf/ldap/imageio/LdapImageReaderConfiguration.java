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
package org.dcm4che3.conf.ldap.imageio;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.conf.ldap.LdapDicomConfigurationExtension;
import org.dcm4che3.conf.api.ConfigurationChanges;
import org.dcm4che3.conf.ldap.LdapUtils;
import org.dcm4che3.imageio.codec.ImageReaderFactory;
import org.dcm4che3.imageio.codec.ImageReaderFactory.ImageReaderParam;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.imageio.ImageReaderExtension;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapImageReaderConfiguration extends LdapDicomConfigurationExtension {

    private static final String CN_IMAGE_READER_FACTORY = "cn=Image Reader Factory,";

    @Override
    protected void storeChilds(ConfigurationChanges diffs, String deviceDN, Device device) throws NamingException {
        ImageReaderExtension ext =
                device.getDeviceExtension(ImageReaderExtension.class);
        if (ext != null)
            store(diffs, deviceDN, ext.getImageReaderFactory());
    }

    private String dnOf(String tsuid, String imageReadersDN) {
        return LdapUtils.dnOf("dicomTransferSyntax" ,tsuid, imageReadersDN);
    }

    private void store(ConfigurationChanges diffs, String deviceDN, ImageReaderFactory factory) throws NamingException {
        String imageReadersDN = CN_IMAGE_READER_FACTORY + deviceDN;
        ConfigurationChanges.ModifiedObject ldapObj =
                ConfigurationChanges.addModifiedObject(diffs, imageReadersDN, ConfigurationChanges.ChangeType.C);
        config.createSubcontext(imageReadersDN,
                LdapUtils.attrs("dcmImageReaderFactory", "cn", "Image Reader Factory"));
        for (Entry<String, ImageReaderParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            String dn = dnOf(tsuid, imageReadersDN);
            ConfigurationChanges.ModifiedObject ldapObj1 =
                    ConfigurationChanges.addModifiedObjectIfVerbose(diffs, dn, ConfigurationChanges.ChangeType.C);
            config.createSubcontext(dn, storeTo(ldapObj1, tsuid, entry.getValue(), new BasicAttributes(true)));
        }
    }

    private Attributes storeTo(ConfigurationChanges.ModifiedObject ldapObj, String tsuid, ImageReaderParam param, Attributes attrs) {
        attrs.put("objectclass", "dcmImageReader");
        attrs.put("dicomTransferSyntax", tsuid);
        attrs.put("dcmIIOFormatName", param.formatName);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmJavaClassName", param.className, null);
        LdapUtils.storeNotNullOrDef(ldapObj, attrs, "dcmPatchJPEGLS", param.patchJPEGLS, null);
        LdapUtils.storeNotEmpty(ldapObj, attrs, "dcmImageReadParam", param.getImageReadParams());
        return attrs;
    }

    @Override
    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException {
        String imageReadersDN = CN_IMAGE_READER_FACTORY + deviceDN;
        try {
            config.getAttributes(imageReadersDN);
        } catch (NameNotFoundException e) {
            return;
        }

        ImageReaderFactory factory = new ImageReaderFactory();
        NamingEnumeration<SearchResult> ne =
                config.search(imageReadersDN, "(objectclass=dcmImageReader)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                factory.put(
                        LdapUtils.stringValue(attrs.get("dicomTransferSyntax"), null),
                        new ImageReaderParam(
                                LdapUtils.stringValue(
                                        attrs.get("dcmIIOFormatName"), null),
                                LdapUtils.stringValue(
                                        attrs.get("dcmJavaClassName"), null),
                                LdapUtils.stringValue(
                                        attrs.get("dcmPatchJPEGLS"), null),
                                LdapUtils.stringArray(attrs.get("dcmImageReadParam"))));
            }
        } finally {
           LdapUtils.safeClose(ne);
        }
        device.addDeviceExtension(new ImageReaderExtension(factory));
    }

    @Override
    protected void mergeChilds(ConfigurationChanges diffs, Device prev, Device device, String deviceDN) throws NamingException {
        ImageReaderExtension prevExt = prev.getDeviceExtension(ImageReaderExtension.class);
        ImageReaderExtension ext = device.getDeviceExtension(ImageReaderExtension.class);
        if (ext == null && prevExt == null)
            return;

        String dn = CN_IMAGE_READER_FACTORY + deviceDN;
        if (ext == null) {
            config.destroySubcontextWithChilds(dn);
            ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
        } else if (prevExt == null) {
            store(diffs, deviceDN, ext.getImageReaderFactory());
        }
        else {
            merge(diffs, prevExt.getImageReaderFactory(), ext.getImageReaderFactory(), dn);
        }
    }

    private void merge(ConfigurationChanges diffs, ImageReaderFactory prev, ImageReaderFactory factory, String imageReadersDN)
            throws NamingException {
        for (Entry<String, ImageReaderParam> entry : prev.getEntries()) {
            String tsuid = entry.getKey();
            if (factory.get(tsuid) == null) {
                String dn = dnOf(tsuid, imageReadersDN);
                config.destroySubcontext(dn);
                ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.D);
            }
        }
        for (Entry<String, ImageReaderParam> entry : factory.getEntries()) {
            String tsuid = entry.getKey();
            String dn = dnOf(tsuid, imageReadersDN);
            ImageReaderParam prevParam = prev.get(tsuid);
            if (prevParam == null) {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.C);
                config.createSubcontext(dn,
                        storeTo(ConfigurationChanges.nullifyIfNotVerbose(diffs, ldapObj),
                                tsuid, entry.getValue(), new BasicAttributes(true)));
            } else {
                ConfigurationChanges.ModifiedObject ldapObj =
                        ConfigurationChanges.addModifiedObject(diffs, dn, ConfigurationChanges.ChangeType.U);
                config.modifyAttributes(dn,
                        storeDiffs(ldapObj, prevParam, entry.getValue(), new ArrayList<ModificationItem>()));
                ConfigurationChanges.removeLastIfEmpty(diffs, ldapObj);
            }
        }
    }

    private List<ModificationItem> storeDiffs(ConfigurationChanges.ModifiedObject ldapObj, ImageReaderParam prevParam,
                                              ImageReaderParam param, List<ModificationItem> mods) {
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmIIOFormatName",
                prevParam.formatName, param.formatName, null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmJavaClassName",
                prevParam.className, param.className, null);
        LdapUtils.storeDiffObject(ldapObj, mods, "dcmPatchJPEGLS",
                prevParam.patchJPEGLS, param.patchJPEGLS, null);
        LdapUtils.storeDiff(ldapObj, mods, "dcmImageReadParam",
                prevParam.getImageReadParams(), param.getImageReadParams());
       return mods;
    }

}
