package org.dcm4che3.conf.api;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.net.TCGroupConfigAEExtension;
import org.dcm4che3.net.TransferCapability;

import java.util.*;

/**
 * @author Roman K
 */
@ConfigurableClass
public class TCConfiguration {

    @ConfigurableProperty
    private Map<String, TCGroup> transferCapabilityGroups = new TreeMap<String, TCGroup>();

    /**
     * Persists default group config
     * Deprecated. Do not use it in upgrade scripts.
     * @param config
     * @throws ConfigurationException
     */
    @Deprecated
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

        public TCGroup() {
        }

        public TCGroup(List<TransferCapability> transferCapabilities) {
            this.transferCapabilities = transferCapabilities;
        }

        @ConfigurableProperty
        List<TransferCapability> transferCapabilities = new ArrayList<TransferCapability>();

        public List<TransferCapability> getTransferCapabilities() {
            return transferCapabilities;
        }

        public void setTransferCapabilities(List<TransferCapability> transferCapabilities) {
            this.transferCapabilities = transferCapabilities;
        }
    }
}
