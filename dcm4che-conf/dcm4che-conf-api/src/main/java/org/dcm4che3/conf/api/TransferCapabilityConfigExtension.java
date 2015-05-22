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
package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.TransferCapability;

import java.util.Collection;
import java.util.Map;

/**
 * An optional configuration extension that contains the information of which TCs are supported for each TC group.
 * Used in combination with TCGroupConfigAEExtension
 *
 * @author Roman K
 */
public interface TransferCapabilityConfigExtension {

    void persistTransferCapabilityConfig(TCConfiguration tcConfig) throws ConfigurationException;
    TCConfiguration getTransferCapabilityConfig() throws ConfigurationException;

    @ConfigurableClass
    class TCConfiguration {

        @ConfigurableProperty
        public Map<String, TCGroup> transferCapabilityGroups;

        public Map<String, TCGroup> getTransferCapabilityGroups() {
            return transferCapabilityGroups;
        }

        public void setTransferCapabilityGroups(Map<String, TCGroup> transferCapabilityGroups) {
            this.transferCapabilityGroups = transferCapabilityGroups;
        }
    }

    @ConfigurableClass
    class TCGroup {

        @ConfigurableProperty
        Collection<TransferCapability> transferCapabilities;

        public Collection<TransferCapability> getTransferCapabilities() {
            return transferCapabilities;
        }

        public void setTransferCapabilities(Collection<TransferCapability> transferCapabilities) {
            this.transferCapabilities = transferCapabilities;
        }
    }

}
