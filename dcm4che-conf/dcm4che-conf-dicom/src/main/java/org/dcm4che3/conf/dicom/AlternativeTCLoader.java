/*
 * *** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 *  Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 *  The Initial Developer of the Original Code is
 *  Agfa Healthcare.
 *  Portions created by the Initial Developer are Copyright (C) 2015
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *  See @authors listed below
 *
 *  Alternatively, the contents of this file may be used under the terms of
 *  either the GNU General Public License Version 2 or later (the "GPL"), or
 *  the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 *  in which case the provisions of the GPL or the LGPL are applicable instead
 *  of those above. If you wish to allow use of your version of this file only
 *  under the terms of either the GPL or the LGPL, and not to allow others to
 *  use your version of this file under the terms of the MPL, indicate your
 *  decision by deleting the provisions above and replace them with the notice
 *  and other provisions required by the GPL or the LGPL. If you do not delete
 *  the provisions above, a recipient may use your version of this file under
 *  the terms of any one of the MPL, the GPL or the LGPL.
 *
 *  ***** END LICENSE BLOCK *****
 */

package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.api.TransferCapabilityConfigExtension;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Relies on org.dcm4che3.net.TCGroupConfigAEExtension and TransferCapabilityConfigExtension to load Transfer Capabilities
 *
 * @author Roman K
 */
public class AlternativeTCLoader {

    private static final Logger log =
            LoggerFactory.getLogger(AlternativeTCLoader.class);


    DicomConfiguration config;
    private TCConfiguration tcConfig;

    public AlternativeTCLoader(DicomConfiguration config) {
        this.config = config;
    }


    private TCConfiguration getTCConfig() throws ConfigurationException {
        if (tcConfig == null)
            tcConfig = config.getDicomConfigurationExtension(TransferCapabilityConfigExtension.class).getTransferCapabilityConfig();
        return tcConfig;
    }


    void initGroupBasedTCs(Device d) throws ConfigurationException {
        for (ApplicationEntity applicationEntity : d.getApplicationEntities()) {
            TCGroupConfigAEExtension tcGroupConfigAEExtension = applicationEntity.getAEExtension(TCGroupConfigAEExtension.class);
            if (tcGroupConfigAEExtension != null) {

                // override any entries that might have been added before
                applicationEntity.setTransferCapabilities(new ArrayList<TransferCapability>());

                // add processed TCs from pre-configured groups to this ae
                for (Map.Entry<String, TCGroupConfigAEExtension.TCGroupDetails> tcGroupRefEntry : tcGroupConfigAEExtension.getScpTCs().entrySet())
                    addTC(applicationEntity, getTCConfig(), tcGroupRefEntry, TransferCapability.Role.SCP);
                for (Map.Entry<String, TCGroupConfigAEExtension.TCGroupDetails> tcGroupRefEntry : tcGroupConfigAEExtension.getScuTCs().entrySet())
                    addTC(applicationEntity, getTCConfig(), tcGroupRefEntry, TransferCapability.Role.SCU);

            }
        }
    }


    private void addTC(ApplicationEntity applicationEntity, TCConfiguration tcConfig, Map.Entry<String, TCGroupConfigAEExtension.TCGroupDetails> tcGroupRefEntry, TransferCapability.Role role) throws ConfigurationException {
        TCConfiguration.TCGroup tcGroup = tcConfig.getTransferCapabilityGroups().get(tcGroupRefEntry.getKey());

        if (tcGroup == null) {
            log.error("Transfer capability group " + tcGroupRefEntry.getKey() + " not found");
            return;
        }

        for (TransferCapability tc : tcGroup.getTransferCapabilities()) {

            TCGroupConfigAEExtension.TCGroupDetails tcGroupDetails = tcGroupRefEntry.getValue();

            // exclude TC if blacklisted
            if (tcGroupDetails.getExcludedTransferCapabilities().contains(tc.getSopClass()))
                continue;

            TransferCapability tcModified = tc.deepCopy();
            tcModified.setRole(role);
            tcModified.setCommonName(tcModified.getCommonName() + " " + role);

            // handle exclusions/whitelisting
            ArrayList<String> tsList = new ArrayList<String>(Arrays.asList(tcModified.getTransferSyntaxes()));
            Iterator<String> iterator = tsList.iterator();
            if (tcGroupDetails.getWhitelistedTransferSyntaxes() != null &&
                    !tcGroupDetails.getWhitelistedTransferSyntaxes().isEmpty()) {

                // use whitelisting logic if enabled - remove all but the specified TSs
                while (iterator.hasNext())
                    if (!tcGroupDetails.getWhitelistedTransferSyntaxes().contains(iterator.next()))
                        iterator.remove();
            } else {

                // otherwise just filter out excluded TSs
                while (iterator.hasNext())
                    if (tcGroupDetails.getExcludedTransferSyntaxes().contains(iterator.next()))
                        iterator.remove();
            }

            // add TC only if there is at least one TS left after filtering
            if (!tsList.isEmpty()) {
                tcModified.setTransferSyntaxes((String[]) tsList.toArray(new String[]{}));
                applicationEntity.addTransferCapability(tcModified);
            }
        }
    }

    public void cleanUpTransferCapabilitiesInDeviceNode(Device device, Map<String, Object> deviceNode) {
        Nodes.removeNodes(deviceNode, DicomPath.AllTCsOfAllAEsWithTCGroupExt.path());
    }
}
