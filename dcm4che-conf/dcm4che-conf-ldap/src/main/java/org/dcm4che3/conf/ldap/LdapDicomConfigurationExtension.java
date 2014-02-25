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

package org.dcm4che3.conf.ldap;

import java.security.cert.CertificateException;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class LdapDicomConfigurationExtension {

    protected LdapDicomConfiguration config;

    public LdapDicomConfiguration getDicomConfiguration() {
        return config;
    }

    public void setDicomConfiguration(LdapDicomConfiguration config) {
        if (config != null && this.config != null)
            throw new IllegalStateException("already owned by other Dicom Configuration");
        this.config = config;
    }

    protected void storeTo(Device device, Attributes attrs) { }

    protected void storeChilds(String deviceDN, Device device)
            throws NamingException { }

    protected void loadFrom(Device device, Attributes attrs)
            throws NamingException, CertificateException { }

    protected void loadChilds(Device device, String deviceDN)
            throws NamingException, ConfigurationException { }

    protected void storeDiffs(Device prev, Device device, List<ModificationItem> mods) {}

    protected void mergeChilds(Device prev, Device device, String deviceDN)
            throws NamingException { }

    protected void storeTo(ApplicationEntity ae, Attributes attrs) {}

    protected void storeChilds(String aeDN, ApplicationEntity ae)
            throws NamingException { }

    protected void loadFrom(ApplicationEntity ae, Attributes attrs)
            throws NamingException {
    }

    protected void loadChilds(ApplicationEntity ae, String aeDN)
            throws NamingException {
    }

    protected void storeDiffs(ApplicationEntity a, ApplicationEntity b,
            List<ModificationItem> mods) {
    }

    protected void mergeChilds(ApplicationEntity prev, ApplicationEntity ae,
            String aeDN) throws NamingException {
    }

}
