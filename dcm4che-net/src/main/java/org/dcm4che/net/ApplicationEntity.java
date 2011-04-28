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

package org.dcm4che.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dcm4che.data.Attributes;
import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.AAssociateRQAC;
import org.dcm4che.net.pdu.CommonExtendedNegotiation;
import org.dcm4che.net.pdu.ExtendedNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.pdu.RoleSelection;
import org.dcm4che.net.pdu.UserIdentityAC;
import org.dcm4che.net.service.DicomService;

/**
 * DICOM Part 15, Annex H compliant description of a DICOM network service.
 * <p>
 * A Network AE is an application entity that provides services on a network. A
 * Network AE will have the 16 same functional capability regardless of the
 * particular network connection used. If there are functional differences based
 * on selected network connection, then these are separate Network AEs. If there
 * are 18 functional differences based on other internal structures, then these
 * are separate Network AEs.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ApplicationEntity {

    private Device device;
    private String aet;
    private String description;
    private Object vendorData;
    private LinkedHashSet<String> applicationCluster =
            new LinkedHashSet<String>();
    private LinkedHashSet<String> prefCalledAETs =
            new LinkedHashSet<String>();
    private LinkedHashSet<String> prefCallingAETs =
            new LinkedHashSet<String>();
    private String[] supportedCharacterSet = {};
    private boolean checkCallingAET;
    private boolean acceptor;
    private boolean initiator;
    private Boolean installed;
    private final List<Connection> conns = new ArrayList<Connection>(1);
    private final HashMap<String, TransferCapability> scuTCs =
            new HashMap<String, TransferCapability>();
    private final HashMap<String, TransferCapability> scpTCs =
            new HashMap<String, TransferCapability>();
    private int maxPDULengthSend = AAssociateRQAC.DEF_MAX_PDU_LENGTH;
    private int maxPDULengthReceive = AAssociateRQAC.DEF_MAX_PDU_LENGTH;
    private int maxOpsPerformed = 1;
    private int maxOpsInvoked = 1;
    private boolean packPDV = true;
    private UserIdentityNegotiator userIdNegotiator;
    private final HashMap<String, ExtendedNegotiator> extNegotiators =
            new HashMap<String, ExtendedNegotiator>();
    private final DicomServiceRegistry serviceRegistry =
            new DicomServiceRegistry();

    public ApplicationEntity(String aeTitle) {
        setAETitle(aeTitle);
    }

    /**
     * Get the device that is identified by this application entity.
     * 
     * @return The owning <code>Device</code>.
     */
    public final Device getDevice() {
        return device;
    }

    /**
     * Set the device that is identified by this application entity.
     * 
     * @param device
     *                The owning <code>Device</code>.
     */
    void setDevice(Device device) {
        if (device != null  && this.device != null)
            throw new IllegalStateException("already owned by " + this.device);
        this.device = device;
    }

    /**
     * Get the AE title for this Network AE.
     * 
     * @return A String containing the AE title.
     */
    public final String getAETitle() {
        return aet;
    }

    /**
     * Set the AE title for this Network AE.
     * 
     * @param aet
     *            A String containing the AE title.
     */
    public void setAETitle(String aet) {
        if (aet.isEmpty())
            throw new IllegalArgumentException("AE title cannot be empty");
        Device device = this.device;
        if (device != null)
            device.removeApplicationEntity(this.aet);
        this.aet = aet;
        if (device != null)
            device.addApplicationEntity(this);
    }

    /**
     * Get the description of this network AE
     * 
     * @return A String containing the description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Set a description of this network AE.
     * 
     * @param description
     *                A String containing the description.
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get any vendor information or configuration specific to this network AE.
     * 
     * @return An Object of the vendor data.
     */
    public final Object getVendorData() {
        return vendorData;
    }

    /**
     * Set any vendor information or configuration specific to this network AE
     * 
     * @param vendorData
     *                An Object of the vendor data.
     */
    public final void setVendorData(Object vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * Get the locally defined names for a subset of related applications. E.g.
     * neuroradiology.
     * 
     * @return A Set<String> containing the names.
     */
    public Set<String> getApplicationClusters() {
        return applicationCluster;
    }

    public boolean isApplicationCluster(String cluster) {
        return applicationCluster.contains(cluster);
    }

    public boolean addApplicationCluster(String cluster) {
        return applicationCluster.add(cluster);
    }

    public boolean removeApplicationCluster(String cluster) {
        return applicationCluster.remove(cluster);
    }

    public void clearApplicationClusters() {
        applicationCluster.clear();
    }

    /**
     * Get the AE Title(s) that are preferred for initiating associations
     * from this network AE.
     * 
     * @return A Set<String> of the preferred called AE titles.
     */
    public Set<String> getPreferredCalledAETitles() {
        return prefCalledAETs;
    }

    public boolean isPreferredCalledAETitle(String aet) {
        return prefCalledAETs.contains(aet);
    }

    public boolean addPreferredCalledAETitle(String aet) {
        return prefCalledAETs.add(aet);
    }

    public boolean removePreferredCalledAETitle(String aet) {
        return prefCalledAETs.remove(aet);
    }

    public void clearPreferredCalledAETitles() {
        prefCalledAETs.clear();
    }

    /**
     * Get the AE title(s) that are preferred for accepting associations by
     * this network AE.
     * 
     * @return A Set<String> containing the preferred calling AE titles.
     */
    public Set<String> getPreferredCallingAETitle() {
        return prefCallingAETs;
    }

    public boolean isPreferredCallingAETitle(String aet) {
        return prefCallingAETs.contains(aet);
    }

    public void addPreferredCallingAETitle(String aet) {
        prefCallingAETs.add(aet);
    }

    public boolean removePreferredCallingAETitle(String aet) {
        return prefCallingAETs.remove(aet);
    }

    public void clearPreferredCallingAETitles() {
        prefCallingAETs.clear();
    }

    public void setAcceptOnlyPreferredCallingAETitles(boolean checkCallingAET) {
        this.checkCallingAET = checkCallingAET;
    }

    public boolean isAcceptOnlyPreferredCallingAETitles() {
        return checkCallingAET;
    }

    /**
     * Get the Character Set(s) supported by the Network AE for data sets it
     * receives. The value shall be selected from the Defined Terms for Specific
     * Character Set (0008,0005) in PS3.3. If no values are present, this
     * implies that the Network AE supports only the default character
     * repertoire (ISO IR 6).
     * 
     * @return A String array of the supported character sets.
     */
    public String[] getSupportedCharacterSet() {
        return supportedCharacterSet;
    }

    /**
     * Set the Character Set(s) supported by the Network AE for data sets it
     * receives. The value shall be selected from the Defined Terms for Specific
     * Character Set (0008,0005) in PS3.3. If no values are present, this
     * implies that the Network AE supports only the default character
     * repertoire (ISO IR 6).
     * 
     * @param characterSets
     *                A String array of the supported character sets.
     */
    public void setSupportedCharacterSet(String... characterSets) {
        this.supportedCharacterSet = characterSets;
    }

    /**
     * Determine whether or not this network AE can accept associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public final boolean isAssociationAcceptor() {
        return acceptor;
    }

    /**
     * Set whether or not this network AE can accept associations.
     * 
     * @param acceptor
     *                A boolean value. True if the Network AE can accept
     *                associations, false otherwise.
     */
    public final void setAssociationAcceptor(boolean acceptor) {
        this.acceptor = acceptor;
    }

    /**
     * Determine whether or not this network AE can initiate associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public final boolean isAssociationInitiator() {
        return initiator;
    }

    /**
     * Set whether or not this network AE can initiate associations.
     * 
     * @param initiator
     *                A boolean value. True if the Network AE can accept
     *                associations, false otherwise.
     */
    public final void setAssociationInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    /**
     * Determine whether or not this network AE is installed on a network.
     * 
     * @return A Boolean value. True if the AE is installed on a network. If not
     *         present, information about the installed status of the AE is
     *         inherited from the device
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled() 
                && (installed == null || installed.booleanValue());
    }

    /**
     * Set whether or not this network AE is installed on a network.
     * 
     * @param installed
     *                A Boolean value. True if the AE is installed on a network.
     *                If not present, information about the installed status of
     *                the AE is inherited from the device
     */
    public void setInstalled(Boolean installed) {
        if (installed != null && installed.booleanValue()
                && device != null && !device.isInstalled())
            throw new IllegalStateException("owning device not installed");
        this.installed = installed;
    }

    public final int getMaxPDULengthSend() {
        return maxPDULengthSend;
    }

    public final void setMaxPDULengthSend(int maxPDULengthSend) {
        this.maxPDULengthSend = maxPDULengthSend;
    }

    public final int getMaxPDULengthReceive() {
        return maxPDULengthReceive;
    }

    public final void setMaxPDULengthReceive(int maxPDULengthReceive) {
        this.maxPDULengthReceive = maxPDULengthReceive;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final boolean isPackPDV() {
        return packPDV;
    }

    public final void setPackPDV(boolean packPDV) {
        this.packPDV = packPDV;
    }

    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Device not initalized");
    }

    public void addConnection(Connection conn) {
        checkDevice();
        if (device != conn.getDevice())
            throw new IllegalArgumentException(
                    "" + conn + " is not a connection of " + device);
        conns.add(conn);
    }

    public void addTransferCapability(TransferCapability tc) {
        String sopClass = tc.getSopClass();
        TransferCapability.Role role = tc.getRole();
        if (role == null || sopClass == null)
            throw new IllegalArgumentException(tc.toString());
        switch(role) {
        case SCU:
            scuTCs.put(sopClass, tc);
            break;
        case SCP:
            scpTCs.put(sopClass, tc);
            break;
        }
    }

    public void addDicomService(DicomService service) {
        serviceRegistry.addDicomService(service);
    }

    public void removeDicomService(DicomService service) {
        serviceRegistry.removeDicomService(service);
    }

    AAssociateAC negotiate(Association as, AAssociateRQ rq)
            throws AAssociateRJ {
        if (!(isInstalled() && acceptor))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLED_AET_NOT_RECOGNIZED);
        if (checkCallingAET && !isPreferredCallingAETitle(rq.getCallingAET()))
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_PERMANENT,
                    AAssociateRJ.SOURCE_SERVICE_USER,
                    AAssociateRJ.REASON_CALLING_AET_NOT_RECOGNIZED);
        UserIdentityAC userIdentity = userIdNegotiator != null
                ? userIdNegotiator.negotiate(as, rq.getUserIdentity())
                : null;
        if (device.isLimitOfOpenConnectionsExceeded())
            throw new AAssociateRJ(AAssociateRJ.RESULT_REJECTED_TRANSIENT,
                    AAssociateRJ.SOURCE_SERVICE_PROVIDER_ACSE,
                    AAssociateRJ.REASON_LOCAL_LIMIT_EXCEEDED);
        AAssociateAC ac = new AAssociateAC();
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(maxPDULengthReceive);
        ac.setMaxOpsInvoked(minZeroAsMax(rq.getMaxOpsInvoked(),
                maxOpsPerformed));
        ac.setMaxOpsPerformed(minZeroAsMax(rq.getMaxOpsPerformed(),
                maxOpsInvoked));
        ac.setUserIdentity(userIdentity);
        Collection<PresentationContext> pcs = rq.getPresentationContexts();
        for (PresentationContext rqpc : pcs)
            ac.addPresentationContext(negotiate(rq, ac, rqpc));
        return ac ;
    }

   static int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
   }

   private PresentationContext negotiate(AAssociateRQ rq, AAssociateAC ac,
           PresentationContext rqpc) {
       String as = rqpc.getAbstractSyntax();
       TransferCapability tc = roleSelection(rq, ac, as);
       int pcid = rqpc.getPCID();
       if (tc == null)
           return new PresentationContext(pcid,
                   PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED,
                   rqpc.getTransferSyntax());

       for (String rqts : rqpc.getTransferSyntaxes())
            for (String ts : tc.getTransferSyntax())
                if (ts.hashCode() == rqts.hashCode() && ts.equals(rqts)
                        || ts.equals("*")) {
                    extNegotiate(rq, ac, as);
                    return new PresentationContext(pcid,
                            PresentationContext.ACCEPTANCE, rqts);
                }

       return new PresentationContext(pcid,
                PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED,
                rqpc.getTransferSyntax());
    }

    private TransferCapability roleSelection(AAssociateRQ rq,
            AAssociateAC ac, String asuid) {
        RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
        if (rqrs == null)
            return getTC(scpTCs, asuid, rq);

        RoleSelection acrs = ac.getRoleSelectionFor(asuid);
        if (acrs != null)
            return getTC(acrs.isSCU() ? scpTCs : scuTCs, asuid, rq);

        TransferCapability tcscu = null;
        TransferCapability tcscp = null;
        boolean scu = rqrs.isSCU()
                && (tcscp = getTC(scpTCs, asuid, rq)) != null;
        boolean scp = rqrs.isSCP()
                && (tcscu = getTC(scuTCs, asuid, rq)) != null;
        ac.addRoleSelection(new RoleSelection(asuid, scu, scp));
        return scu ? tcscp : tcscu;
    }

    private TransferCapability getTC(HashMap<String, TransferCapability> tcs,
            String asuid, AAssociateRQ rq) {
        TransferCapability tc = tcs.get(asuid);
        if (tc != null)
            return tc;

        CommonExtendedNegotiation commonExtNeg =
                rq.getCommonExtendedNegotiationFor(asuid);
        if (commonExtNeg != null) {
            for (String cuid : commonExtNeg.getRelatedGeneralSOPClassUIDs()) {
                tc = tcs.get(cuid);
                if (tc != null)
                    return tc;
            }
            tc = tcs.get(commonExtNeg.getServiceClassUID());
            if (tc != null)
                return tc;
        }

        return tcs.get("*");
    }

    private void extNegotiate(AAssociateRQ rq, AAssociateAC ac, String asuid) {
        ExtendedNegotiation rqexneg = rq.getExtNegotiationFor(asuid);
        if (rqexneg == null)
            return;

        ExtendedNegotiation acexneg = ac.getExtNegotiationFor(asuid);
        if (acexneg != null)
            return;

        ExtendedNegotiator exneg = extNegotiators .get(asuid);
        if (exneg == null)
            return;

        ac.addExtendedNegotiation(exneg.negotiate(rqexneg));
    }

    public Association connect(Connection local, String host, int port,
            AAssociateRQ rq) throws IOException, InterruptedException {
        if (!aet.equals("*"))
            rq.setCallingAET(aet);
        rq.setMaxOpsInvoked(maxOpsInvoked);
        rq.setMaxOpsPerformed(maxOpsPerformed);
        rq.setMaxPDULength(maxPDULengthReceive);
        Association as = new Association(local, local.connect(host, port), true);
        as.setApplicationEntity(this);
        as.write(rq);
        as.startARTIM(local.getAcceptTimeout());
        as.activate();
        as.waitForLeaving(State.Sta5);
        return as;
    }

    void perform(Association as, PresentationContext pc, Attributes cmd,
            PDVInputStream data) throws IOException {
        serviceRegistry.process(as, pc, cmd, data);
    }
}
