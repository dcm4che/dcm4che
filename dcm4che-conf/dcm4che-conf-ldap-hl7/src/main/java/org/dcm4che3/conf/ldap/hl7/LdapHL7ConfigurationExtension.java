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
package org.dcm4che3.conf.ldap.hl7;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che3.conf.ldap.LdapDicomConfiguration;
import org.dcm4che3.net.hl7.HL7Application;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 *
 */
public class LdapHL7ConfigurationExtension {

    protected LdapHL7Configuration config;

    public LdapHL7Configuration getHL7Configuration() {
        return config;
    }

    public void setHL7Configuration(LdapHL7Configuration config) {
        if (config != null && this.config != null)
            throw new IllegalStateException("already owned by other HL7 Configuration");
        this.config = config;
    }

    public LdapDicomConfiguration getDicomConfiguration() {
        return config != null ? config.getDicomConfiguration() : null;
    }

    public void storeTo(HL7Application hl7App, String deviceDN, Attributes attrs) {}

    public void storeChilds(String appDN, HL7Application hl7App) throws NamingException {}

    public void loadFrom(HL7Application hl7App, Attributes attrs) throws NamingException {}

    public void loadChilds(HL7Application hl7App, String appDN) throws NamingException {}

    public void storeDiffs(HL7Application a, HL7Application b, List<ModificationItem> mods) {}

    public void mergeChilds(HL7Application prev, HL7Application hl7App, String appDN) throws NamingException {}
}
