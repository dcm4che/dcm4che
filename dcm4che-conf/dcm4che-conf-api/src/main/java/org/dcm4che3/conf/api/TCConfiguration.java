package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.TransferCapability;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Roman K
 */
@ConfigurableClass
public class TCConfiguration {

    @ConfigurableProperty
    public Map<String, TCGroup> transferCapabilityGroups = new TreeMap<String, TCGroup>();

    /**
     * Persists default group config
     * @param config
     * @throws ConfigurationException
     */
    public static void persistDefaultTCGroups(DicomConfiguration config) throws ConfigurationException {

        TransferCapabilityConfigExtension tcExt = config.getDicomConfigurationExtension(TransferCapabilityConfigExtension.class);
        TCConfiguration tcConfig = new TCConfiguration();

        for (TCGroupConfigAEExtension.DefaultGroup group : TCGroupConfigAEExtension.DefaultGroup.values()) {
            TCGroup tcGroup = new TCGroup();
            tcGroup.setTransferCapabilities(TCGroupConfigAEExtension.getTCsForDefaultGroup(group));
            tcConfig.getTransferCapabilityGroups().put(group.name(), tcGroup);
        }

        tcExt.persistTransferCapabilityConfig(tcConfig);
    }

    public Map<String, TCGroup> getTransferCapabilityGroups() {
        return transferCapabilityGroups;
    }

    public void setTransferCapabilityGroups(Map<String, TCGroup> transferCapabilityGroups) {
        this.transferCapabilityGroups = transferCapabilityGroups;
    }

    @ConfigurableClass
    public static class TCGroup {

        @ConfigurableProperty
        Collection<TransferCapability> transferCapabilities = new ArrayList<TransferCapability>();

        public Collection<TransferCapability> getTransferCapabilities() {
            return transferCapabilities;
        }

        public void setTransferCapabilities(Collection<TransferCapability> transferCapabilities) {
            this.transferCapabilities = transferCapabilities;
        }
    }
}
