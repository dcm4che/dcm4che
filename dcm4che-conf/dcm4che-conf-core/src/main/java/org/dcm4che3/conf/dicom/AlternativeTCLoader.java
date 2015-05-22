package org.dcm4che3.conf.dicom;

import org.dcm4che3.conf.api.DicomConfiguration;
import org.dcm4che3.conf.api.TransferCapabilityConfigExtension;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.util.ConfigNodeUtil;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.TransferCapability;

import java.util.*;

/**
 * Relies on org.dcm4che3.net.TCGroupConfigAEExtension and TransferCapabilityConfigExtension to load Transfer Capabilities
 *
 * @author Roman K
 */
public class AlternativeTCLoader {

    DicomConfiguration config;
    private TransferCapabilityConfigExtension.TCConfiguration tcConfig;

    public AlternativeTCLoader(DicomConfiguration config) {
        this.config = config;
    }


    private TransferCapabilityConfigExtension.TCConfiguration getTCConfig() throws ConfigurationException {
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


    private void addTC(ApplicationEntity applicationEntity, TransferCapabilityConfigExtension.TCConfiguration tcConfig, Map.Entry<String, TCGroupConfigAEExtension.TCGroupDetails> tcGroupRefEntry, TransferCapability.Role role) throws ConfigurationException {
        TransferCapabilityConfigExtension.TCGroup tcGroup = tcConfig.getTransferCapabilityGroups().get(tcGroupRefEntry.getKey());

        if (tcGroup == null)
            throw new ConfigurationException("Transfer capability group " + tcGroupRefEntry.getKey() + " not found");

        for (TransferCapability tc : tcGroup.getTransferCapabilities()) {

            // exclude TC if blacklisted
            if (tcGroupRefEntry.getValue().getExcludedTransferCapabilities().contains(tc.getSopClass()))
                continue;

            TransferCapability tcModified = tc.deepCopy();
            tcModified.setRole(role);
            tcModified.setCommonName(tcModified.getCommonName()+" "+role);


            // filter out excluded TSs
            ArrayList<String> tsList = new ArrayList<String>(Arrays.asList(tcModified.getTransferSyntaxes()));
            Iterator<String> iterator = tsList.iterator();
            while (iterator.hasNext())
                if (tcGroupRefEntry.getValue().getExcludedTransferSyntaxes().contains(iterator.next()))
                    iterator.remove();
            tcModified.setTransferSyntaxes((String[]) tsList.toArray());

            applicationEntity.addTransferCapability(tcModified);
        }
    }

    public void cleanUpTransferCapabilitiesInDeviceNode(Device device, Map<String, Object> deviceNode) {
        ConfigNodeUtil.removeNodes(deviceNode, DicomPath.AllTCsOfAllAEsWithTCGroupExt.path());
    }
}
