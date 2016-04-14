package org.dcm4che3.net;

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.data.UID;

import java.io.Serializable;
import java.util.*;

import static org.dcm4che3.net.TransferCapability.Role.SCU;

/**
 * Alternative representation of Transfer Capabilities for an application entity
 *
 * @author Roman K
 */
@ConfigurableClass
public class TCGroupConfigAEExtension extends AEExtension {

    /**
     * Certain components' logic may be bound to these group names
     */
    public enum DefaultGroup {
        STORAGE,
        PPS,
        QUERY,
        RETRIEVE,
        MWL,
        STORAGE_COMMITMENT
    }

    @ConfigurableProperty(
            label = "Enabled transfer capability groups - SCU"
    )
    Map<String, TCGroupDetails> scuTCs = new TreeMap<String, TCGroupDetails>();


    @ConfigurableProperty(
            label = "Enabled transfer capability groups - SCP"
    )
    Map<String, TCGroupDetails> scpTCs = new TreeMap<String, TCGroupDetails>();

    public TCGroupConfigAEExtension() {
    }

    public TCGroupConfigAEExtension(EnumSet<DefaultGroup> scpGroups, EnumSet<DefaultGroup> scuGroups) {
        for (DefaultGroup defaultGroup : scpGroups) scpTCs.put(defaultGroup.name(), new TCGroupDetails());
        for (DefaultGroup defaultGroup : scuGroups) scuTCs.put(defaultGroup.name(), new TCGroupDetails());
    }

    /**
     * Shortcut to define which default SCP groups are supported, omitting exclusion details
     *
     * @return
     */
    public EnumSet<DefaultGroup> getSupportedDefaultScpGroups() {
        EnumSet<DefaultGroup> groups = EnumSet.noneOf(DefaultGroup.class);
        for (Map.Entry<String, TCGroupDetails> entry : scpTCs.entrySet()) {

            try {
                DefaultGroup group = DefaultGroup.valueOf(entry.getKey());
                groups.add(group);
            } catch (IllegalArgumentException e) {
                //noop
            }
        }
        return groups;
    }

    /**
     * Restricts transfer syntaxes for all SOP classes for this AE to Little Endian Implicit and Video-related ones
     */
    public void setLEIAndVideoOnly(boolean leiAndVideoOnly) {
        if (leiAndVideoOnly) {
            for (TCGroupDetails tcGroupDetails : scpTCs.values())
                whitelistLEIAndVideoTSs(tcGroupDetails);
            for (TCGroupDetails tcGroupDetails : scuTCs.values())
                whitelistLEIAndVideoTSs(tcGroupDetails);
        } else {

            for (TCGroupDetails tcGroupDetails : scpTCs.values())
                tcGroupDetails.getWhitelistedTransferSyntaxes().clear();
            for (TCGroupDetails tcGroupDetails : scuTCs.values())
                tcGroupDetails.getWhitelistedTransferSyntaxes().clear();
        }
    }

    private void whitelistLEIAndVideoTSs(TCGroupDetails tcGroupDetails) {
        tcGroupDetails.getWhitelistedTransferSyntaxes().clear();
        tcGroupDetails.getWhitelistedTransferSyntaxes().add(UID.ImplicitVRLittleEndian);

        for (String videoTsuid : DefaultTransferCapabilities.VIDEO_TSUIDS) {
            tcGroupDetails.getWhitelistedTransferSyntaxes().add(videoTsuid);
        }
    }

    public Map<String, TCGroupDetails> getScuTCs() {
        return scuTCs;
    }

    public void setScuTCs(Map<String, TCGroupDetails> scuTCs) {
        this.scuTCs = scuTCs;
    }

    public Map<String, TCGroupDetails> getScpTCs() {
        return scpTCs;
    }

    public void setScpTCs(Map<String, TCGroupDetails> scpTCs) {
        this.scpTCs = scpTCs;
    }

    @ConfigurableClass
    public static class TCGroupDetails implements Serializable {

        public TCGroupDetails() {
        }

        @ConfigurableProperty
        private List<String> excludedTransferSyntaxes = new ArrayList<String>();

        @ConfigurableProperty(
                description = "If not empty, all the syntaxes but those specified by this parameter" +
                "will be effectively removed from this AE's transfer capabilities")
        private List<String> whitelistedTransferSyntaxes = new ArrayList<String>();

        @ConfigurableProperty(
                label = "Excluded SOP classes",
                description = "This AE will include all transfer capabilities from the corresponding group, " +
                        "except those with SOP classes specified here"
        )
        private List<String> excludedTransferCapabilities = new ArrayList<String>();

        public List<String> getExcludedTransferSyntaxes() {
            return excludedTransferSyntaxes;
        }

        public void setExcludedTransferSyntaxes(List<String> excludedTransferSyntaxes) {
            this.excludedTransferSyntaxes = excludedTransferSyntaxes;
        }

        public List<String> getExcludedTransferCapabilities() {
            return excludedTransferCapabilities;
        }

        public void setExcludedTransferCapabilities(List<String> excludedTransferCapabilities) {
            this.excludedTransferCapabilities = excludedTransferCapabilities;
        }

        public List<String> getWhitelistedTransferSyntaxes() {
            return whitelistedTransferSyntaxes;
        }

        public void setWhitelistedTransferSyntaxes(List<String> whitelistedTransferSyntaxes) {
            this.whitelistedTransferSyntaxes = whitelistedTransferSyntaxes;
        }
    }

    @Override
    public void reconfigure(AEExtension from) {
        TCGroupConfigAEExtension tcGroupConfigAEExtension = (TCGroupConfigAEExtension) from;
        setScpTCs(tcGroupConfigAEExtension.getScpTCs());
        setScuTCs(tcGroupConfigAEExtension.getScuTCs());
    }


    public static List<TransferCapability> getTCsForDefaultGroup(DefaultGroup group) {

        ArrayList<TransferCapability> transferCapabilities;

        switch (group) {
            case STORAGE:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, DefaultTransferCapabilities.IMAGE_CUIDS, null, DefaultTransferCapabilities.IMAGE_TSUIDS);
                allTCToList(transferCapabilities, DefaultTransferCapabilities.VIDEO_CUIDS, null, DefaultTransferCapabilities.VIDEO_TSUIDS);
                allTCToList(transferCapabilities, DefaultTransferCapabilities.OTHER_CUIDS, null, DefaultTransferCapabilities.OTHER_TSUIDS);
                allTCToList(transferCapabilities, new String[]{UID.InstanceAvailabilityNotificationSOPClass, UID.VerificationSOPClass}, null, UID.ImplicitVRLittleEndian);
                return transferCapabilities;

            case QUERY:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, DefaultTransferCapabilities.QUERY_CUIDS, EnumSet.allOf(QueryOption.class), DefaultTransferCapabilities.OTHER_TSUIDS);
                allTCToList(transferCapabilities, new String[]{UID.VerificationSOPClass}, null, UID.ImplicitVRLittleEndian);
                return transferCapabilities;

            case RETRIEVE:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, DefaultTransferCapabilities.RETRIEVE_CUIDS, EnumSet.of(QueryOption.RELATIONAL), DefaultTransferCapabilities.OTHER_TSUIDS);
                allTCToList(transferCapabilities, new String[]{UID.CompositeInstanceRetrieveWithoutBulkDataGET, UID.VerificationSOPClass}, null, UID.ImplicitVRLittleEndian);
                return transferCapabilities;

            case MWL:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, new String[]{UID.ModalityWorklistInformationModelFIND, UID.VerificationSOPClass}, EnumSet.allOf(QueryOption.class), UID.ImplicitVRLittleEndian);
                return transferCapabilities;

            case PPS:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, new String[]{UID.ModalityPerformedProcedureStepSOPClass, UID.VerificationSOPClass}, null, UID.ImplicitVRLittleEndian);
                return transferCapabilities;

            case STORAGE_COMMITMENT:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, new String[]{UID.StorageCommitmentPushModelSOPClass, UID.VerificationSOPClass}, null, UID.ImplicitVRLittleEndian);
                return transferCapabilities;

            default:
                throw new RuntimeException("Group " + group + " is undefined");

        }
    }

    private static void allTCToList(ArrayList<TransferCapability> transferCapabilities, String[] imageCuids, EnumSet<QueryOption> queryOptions, String... transferSyntaxes) {
        for (String cuid : imageCuids) {
            String name = UID.nameOf(cuid).replace('/', ' ');
            TransferCapability tc = new TransferCapability(name, cuid, SCU, transferSyntaxes);
            tc.setQueryOptions(queryOptions);
            transferCapabilities.add(tc);
        }
    }

}
