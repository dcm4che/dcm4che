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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dcm4che.net.pdu.AAssociateAC;
import org.dcm4che.net.pdu.AAssociateRJ;
import org.dcm4che.net.pdu.AAssociateRQ;
import org.dcm4che.net.pdu.AAssociateRQAC;
import org.dcm4che.net.pdu.ExtNegotiation;
import org.dcm4che.net.pdu.PresentationContext;
import org.dcm4che.net.pdu.RoleSelection;

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
    private String aeTitle;
    private String description;
    private Object vendorData;
    private LinkedHashSet<String> applicationCluster =
            new LinkedHashSet<String>();
    private LinkedHashSet<String> preferredCalledAETitle =
            new LinkedHashSet<String>();
    private LinkedHashSet<String> preferredCallingAETitle =
            new LinkedHashSet<String>();
    private String[] supportedCharacterSet = {};
    private boolean acceptOnlyPreferredCallingAETitles;
    private boolean associationAcceptor;
    private boolean associationInitiator;
    private Boolean installed;
    private final List<Connection> conns = new ArrayList<Connection>(1);
    private final HashMap<String, TransferCapability> scuTransferCapabilities =
            new HashMap<String, TransferCapability>();
    private final HashMap<String, TransferCapability> scpTransferCapabilities =
            new HashMap<String, TransferCapability>();
    private int maxPDULengthSend = AAssociateRQAC.DEF_MAX_PDU_LENGTH;
    private int maxPDULengthReceive = AAssociateRQAC.DEF_MAX_PDU_LENGTH;
    private int maxOpsPerformed = 1;
    private int maxOpsInvoked = 1;

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
        return aeTitle;
    }

    /**
     * Set the AE title for this Network AE.
     * 
     * @param aetitle
     *                A String containing the AE title.
     */
    public final void setAETitle(String aetitle) {
        this.aeTitle = aetitle;
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
        return preferredCalledAETitle;
    }

    public boolean isPreferredCalledAETitle(String aet) {
        return preferredCalledAETitle.contains(aet);
    }

    public boolean addPreferredCalledAETitle(String aet) {
        return preferredCalledAETitle.add(aet);
    }

    public boolean removePreferredCalledAETitle(String aet) {
        return preferredCalledAETitle.remove(aet);
    }

    public void clearPreferredCalledAETitles() {
        preferredCalledAETitle.clear();
    }

    /**
     * Get the AE title(s) that are preferred for accepting associations by
     * this network AE.
     * 
     * @return A Set<String> containing the preferred calling AE titles.
     */
    public Set<String> getPreferredCallingAETitle() {
        return preferredCallingAETitle;
    }

    public boolean isPreferredCallingAETitle(String aet) {
        return preferredCallingAETitle.contains(aet);
    }

    public void addPreferredCallingAETitle(String aet) {
        preferredCallingAETitle.add(aet);
    }

    public boolean removePreferredCallingAETitle(String aet) {
        return preferredCallingAETitle.remove(aet);
    }

    public void clearPreferredCallingAETitles() {
        preferredCallingAETitle.clear();
    }

    public void setAcceptOnlyPreferredCallingAETitles(boolean acceptOnly) {
        this.acceptOnlyPreferredCallingAETitles = acceptOnly;
    }

    public boolean isAcceptOnlyPreferredCallingAETitles() {
        return acceptOnlyPreferredCallingAETitles;
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
        return associationAcceptor;
    }

    /**
     * Set whether or not this network AE can accept associations.
     * 
     * @param acceptor
     *                A boolean value. True if the Network AE can accept
     *                associations, false otherwise.
     */
    public final void setAssociationAcceptor(boolean acceptor) {
        this.associationAcceptor = acceptor;
    }

    /**
     * Determine whether or not this network AE can initiate associations.
     * 
     * @return A boolean value. True if the Network AE can accept associations,
     *         false otherwise.
     */
    public final boolean isAssociationInitiator() {
        return associationInitiator;
    }

    /**
     * Set whether or not this network AE can initiate associations.
     * 
     * @param initiator
     *                A boolean value. True if the Network AE can accept
     *                associations, false otherwise.
     */
    public final void setAssociationInitiator(boolean initiator) {
        this.associationInitiator = initiator;
    }

    /**
     * Determine whether or not this network AE is installed on a network.
     * 
     * @return A Boolean value. True if the AE is installed on a network. If not
     *         present, information about the installed status of the AE is
     *         inherited from the device
     */
    public boolean isInstalled() {
        return installed != null ? installed.booleanValue() : device == null
                || device.isInstalled();
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

    public void add(TransferCapability tc) {
        String sopClass = tc.getSopClass();
        TransferCapability.Role role = tc.getRole();
        if (role == null || sopClass == null)
            throw new IllegalArgumentException(tc.toString());
        switch(role) {
        case SCU:
            scuTransferCapabilities.put(sopClass, tc);
            break;
        case SCP:
            scpTransferCapabilities.put(sopClass, tc);
            break;
        }
    }

    AAssociateAC negotiate(Association as, AAssociateRQ rq)
            throws AAssociateRJ {
        AAssociateAC ac = new AAssociateAC();
        ac.setCalledAET(rq.getCalledAET());
        ac.setCallingAET(rq.getCallingAET());
        ac.setMaxPDULength(maxPDULengthReceive);
        ac.setMaxOpsInvoked(minZeroAsMax(rq.getMaxOpsInvoked(),
                maxOpsPerformed));
        ac.setMaxOpsPerformed(minZeroAsMax(rq.getMaxOpsPerformed(),
                maxOpsInvoked));
        Collection<PresentationContext> pcs = rq.getPresentationContexts();
        for (PresentationContext rqpc : pcs)
            ac.addPresentationContext(negotiate(rq, ac, rqpc));
        return ac ;
    }

   private static int minZeroAsMax(int i1, int i2) {
        return i1 == 0 ? i2 : i2 == 0 ? i1 : Math.min(i1, i2);
    }

   private PresentationContext negotiate(AAssociateRQ rq, AAssociateAC ac,
           PresentationContext rqpc) {
       String asuid = rqpc.getAbstractSyntax();
       TransferCapability tcscu = scuTransferCapabilities.get(asuid);
       TransferCapability tcscp = scpTransferCapabilities.get(asuid);
       RoleSelection rqrs = rq.getRoleSelectionFor(asuid);
       RoleSelection acrs = ac.getRoleSelectionFor(asuid);
       if (rqrs != null && acrs == null) {
           boolean scu = rqrs.isSCU() && tcscp != null;
           boolean scp = rqrs.isSCP() && tcscu != null;
           acrs = new RoleSelection(asuid, scu, scp);
           ac.addRoleSelection(acrs);
       }
       TransferCapability tc = rqrs == null || acrs.isSCU() ? tcscp : tcscu;

       PresentationContext acpc = new PresentationContext();
       acpc.setPCID(rqpc.getPCID());
       if (tc != null) {
           Set<String> rqts = rqpc.getTransferSyntaxes();
           String[] acts = tc.getTransferSyntax();
           for (int i = 0; i < acts.length; i++) {
               if (rqts.contains(acts[i])) {
                   acpc.addTransferSyntax(acts[i]);
                   extNegotiate(rq, ac, asuid);
                   return acpc;
               }
           }
           acpc.setResult(PresentationContext.TRANSFER_SYNTAX_NOT_SUPPORTED);
       }
       else {
           acpc.setResult(PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED);
       }
       acpc.addTransferSyntax(rqpc.getTransferSyntax());
       return acpc;
    }

    private void extNegotiate(AAssociateRQ rq, AAssociateAC ac, String asuid) {
        ExtNegotiation rqexneg = rq.getExtNegotiationFor(asuid);
        ExtNegotiation acexneg = ac.getExtNegotiationFor(asuid);
        if (acexneg == null && rqexneg != null) {
            
        }
    }

}
