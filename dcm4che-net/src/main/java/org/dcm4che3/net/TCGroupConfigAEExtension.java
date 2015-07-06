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
        QUERY_RETRIEVE,
        MWL,
        STORAGE_COMMITMENT
    }

    @ConfigurableProperty
    Map<String, TCGroupDetails> scuTCs = new TreeMap<String, TCGroupDetails>();


    @ConfigurableProperty
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

        @ConfigurableProperty
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

            case QUERY_RETRIEVE:
                transferCapabilities = new ArrayList<TransferCapability>();
                allTCToList(transferCapabilities, DefaultTransferCapabilities.QUERY_CUIDS, EnumSet.allOf(QueryOption.class), UID.ImplicitVRLittleEndian);
                allTCToList(transferCapabilities, DefaultTransferCapabilities.RETRIEVE_CUIDS, EnumSet.of(QueryOption.RELATIONAL), UID.ImplicitVRLittleEndian);
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
