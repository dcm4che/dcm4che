package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.DefaultDelegatingConfigTypeAdapter;
import org.dcm4che3.conf.api.TCConfiguration;
import org.dcm4che3.conf.api.TCGroupsProvider;
import org.dcm4che3.conf.core.Nodes;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.ConfigProperty;
import org.dcm4che3.conf.core.context.LoadingContext;
import org.dcm4che3.conf.core.context.SavingContext;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.TransferCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 *  Relies on org.dcm4che3.net.TCGroupConfigAEExtension and TransferCapabilityConfigExtension to populate Transfer Capabilities on load
 *  Removes AE's TCs on save
 */
public class AppEntityTCGroupHandlingTypeAdapter extends DefaultDelegatingConfigTypeAdapter<ApplicationEntity, Map> {

    private static final Logger log = LoggerFactory.getLogger(AppEntityTCGroupHandlingTypeAdapter.class);


    private TCGroupsProvider tcGroupsProvider;

    public AppEntityTCGroupHandlingTypeAdapter(TCGroupsProvider tcGroupsProvider) {
        this.tcGroupsProvider = tcGroupsProvider;
    }

    @Override
    public ApplicationEntity fromConfigNode(Map configNode, ConfigProperty property, LoadingContext ctx, Object parent) throws ConfigurationException {
        ApplicationEntity applicationEntity = super.fromConfigNode(configNode, property, ctx, parent);


        TCGroupConfigAEExtension tcGroupConfigAEExtension = applicationEntity.getAEExtension(TCGroupConfigAEExtension.class);
        if (tcGroupConfigAEExtension != null) {

            // override any entries that might have been added before
            applicationEntity.setTransferCapabilities(new ArrayList<TransferCapability>());

            // add processed TCs from pre-configured groups to this ae
            for (Map.Entry<String, TCGroupConfigAEExtension.TCGroupDetails> tcGroupRefEntry : tcGroupConfigAEExtension.getScpTCs().entrySet())
                addTC(applicationEntity, tcGroupsProvider.getTCGroups(), tcGroupRefEntry, TransferCapability.Role.SCP);
            for (Map.Entry<String, TCGroupConfigAEExtension.TCGroupDetails> tcGroupRefEntry : tcGroupConfigAEExtension.getScuTCs().entrySet())
                addTC(applicationEntity, tcGroupsProvider.getTCGroups(), tcGroupRefEntry, TransferCapability.Role.SCU);

        }

        return applicationEntity;
    }

    @Override
    public Map toConfigNode(ApplicationEntity object, ConfigProperty property, SavingContext ctx) throws ConfigurationException {

        Map aeNode = super.toConfigNode(object, property, ctx);

        // strip the TCs out in case the TCGroupConfigAEExtension is present
        if (object.getAEExtension(TCGroupConfigAEExtension.class) != null) {

            // we cannot do this since we don't want to change the state of the ae object
            // object.setTransferCapabilities(Collections.<TransferCapability>emptyList());

            // so we modify the serialized node instead
            Nodes.removeNodes(aeNode, DicomPath.AETransferCapabilities.path());
        }

        return aeNode;
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
}
