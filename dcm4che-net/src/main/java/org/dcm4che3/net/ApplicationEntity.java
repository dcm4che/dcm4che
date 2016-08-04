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

package org.dcm4che3.net;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.pdu.AAbort;
import org.dcm4che3.net.pdu.AAssociateAC;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.CommonExtendedNegotiation;
import org.dcm4che3.net.pdu.ExtendedNegotiation;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.pdu.RoleSelection;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ApplicationEntity implements Serializable {

    private static final long serialVersionUID = 3883790997057469573L;

    protected static final Logger LOG = 
            LoggerFactory.getLogger(ApplicationEntity.class);

    private Device device;
    private String aet;
    private String description;
    private byte[][] vendorData = {};
    private String[] applicationClusters = {};
    private String[] prefCalledAETs = {};
    private String[] prefCallingAETs = {};
    private String[] supportedCharacterSets = {};
    private boolean acceptor = true;
    private boolean initiator = true;
    private Boolean installed;
    private final LinkedHashSet<String> acceptedCallingAETs = new LinkedHashSet<>();
    private final LinkedHashSet<String> otherAETs = new LinkedHashSet<>();
    private final LinkedHashMap<String, String> masqueradeCallingAETs = new LinkedHashMap<>();
    private final List<Connection> conns = new ArrayList<>(1);
    private final LinkedHashMap<String, TransferCapability> scuTCs = new LinkedHashMap<>();
    private final LinkedHashMap<String, TransferCapability> scpTCs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<? extends AEExtension>,AEExtension> extensions = new LinkedHashMap<>();

    private transient DimseRQHandler dimseRQHandler;

    public ApplicationEntity() {
    }

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
        if (device != null) {
            if (this.device != null)
                throw new IllegalStateException("already owned by " + 
                        this.device.getDeviceName());
            for (Connection conn : conns)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " + 
                            device.getDeviceName());
        }
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
    public final byte[][] getVendorData() {
        return vendorData;
    }

    /**
     * Set any vendor information or configuration specific to this network AE
     * 
     * @param vendorData
     *                An Object of the vendor data.
     */
    public final void setVendorData(byte[]... vendorData) {
        this.vendorData = vendorData;
    }

    /**
     * Get the locally defined names for a subset of related applications. E.g.
     * neuroradiology.
     * 
     * @return A String[] containing the names.
     */
    public String[] getApplicationClusters() {
        return applicationClusters;
    }

    public void setApplicationClusters(String... clusters) {
        applicationClusters = clusters;
    }

    /**
     * Get the AE Title(s) that are preferred for initiating associations
     * from this network AE.
     * 
     * @return A String[] of the preferred called AE titles.
     */
    public String[] getPreferredCalledAETitles() {
        return prefCalledAETs;
    }

    public void setPreferredCalledAETitles(String... aets) {
        prefCalledAETs = aets;
    }

    /**
     * Get the AE title(s) that are preferred for accepting associations by
     * this network AE.
     * 
     * @return A String[] containing the preferred calling AE titles.
     */
    public String[] getPreferredCallingAETitles() {
        return prefCallingAETs;
    }

    public void setPreferredCallingAETitles(String... aets) {
        prefCallingAETs = aets;
    }

    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETs.toArray(
                new String[acceptedCallingAETs.size()]);
    }

    public void setAcceptedCallingAETitles(String... aets) {
        acceptedCallingAETs.clear();
        for (String name : aets)
            acceptedCallingAETs.add(name);
    }

    public boolean isAcceptedCallingAETitle(String aet) {
        return acceptedCallingAETs.isEmpty()
                || acceptedCallingAETs.contains(aet);
    }

    public String[] getOtherAETitles() {
        return otherAETs.toArray(new String[otherAETs.size()]);
    }

    public void setOtherAETitles(String... aets) {
        otherAETs.clear();
        for (String name : aets)
            otherAETs.add(name);
    }

    public boolean isOtherAETitle(String aet) {
        return otherAETs.contains(aet);
    }

    public String[] getMasqueradeCallingAETitles() {
        String[] aets = new String[masqueradeCallingAETs.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : masqueradeCallingAETs.entrySet()) {
            aets[i] = entry.getKey().equals("*")
                    ? entry.getValue()
                    : '[' + entry.getKey() + ']' + entry.getValue();
        }
        return aets;
    }

    public void setMasqueradeCallingAETitles(String... aets) {
        masqueradeCallingAETs.clear();
        for (String aet : aets) {
            if (aet.charAt(0) == '[') {
                int end = aet.indexOf(']');
                if (end > 0)
                    masqueradeCallingAETs.put(aet.substring(1,end), aet.substring(end+1));
            } else {
                masqueradeCallingAETs.put("*", aet);
            }
        }
    }

    public String getCallingAETitle(String calledAET) {
        String callingAET = masqueradeCallingAETs.get(calledAET);
        if (callingAET == null) {
            callingAET = masqueradeCallingAETs.get("*");
            if (callingAET == null)
                callingAET = aet;
        }
        return callingAET;
    }

    public boolean isMasqueradeCallingAETitle(String calledAET) {
        return masqueradeCallingAETs.containsKey(calledAET) || masqueradeCallingAETs.containsKey("*");
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
    public String[] getSupportedCharacterSets() {
        return supportedCharacterSets;
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
    public void setSupportedCharacterSets(String... characterSets) {
        supportedCharacterSets = characterSets;
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

    public final Boolean getInstalled() {
        return installed;
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
        this.installed = installed;
    }

    public DimseRQHandler getDimseRQHandler() {
        DimseRQHandler handler = dimseRQHandler;
        if (handler != null)
            return handler;

        Device device = this.device;
        return device != null
                ? device.getDimseRQHandler()
                : null;
    }

    public final void setDimseRQHandler(DimseRQHandler dimseRQHandler) {
        this.dimseRQHandler = dimseRQHandler;
    }

    private void checkInstalled() {
        if (!isInstalled())
            throw new IllegalStateException("Not installed");
    }

    private void checkDevice() {
        if (device == null)
            throw new IllegalStateException("Not attached to Device");
    }

    void onDimseRQ(Association as, PresentationContext pc, Dimse cmd,
            Attributes cmdAttrs, PDVInputStream data) throws IOException {
        DimseRQHandler tmp = getDimseRQHandler();
        if (tmp == null) {
            LOG.error("DimseRQHandler not initalized");
            throw new AAbort();
        }
        tmp.onDimseRQ(as, pc, cmd, cmdAttrs, data);
    }

    public void addConnection(Connection conn) {
        if (conn.getProtocol() != Connection.Protocol.DICOM)
            throw new IllegalArgumentException(
                    "protocol != DICOM - " + conn.getProtocol());


        if (device != null && device != conn.getDevice())
            throw new IllegalStateException(conn + " not contained by Device: " + 
                    device.getDeviceName());
        conns.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return conns.remove(conn);
    }

    public List<Connection> getConnections() {
        return conns;
    }

    public TransferCapability addTransferCapability(TransferCapability tc) {
        tc.setApplicationEntity(this);
        TransferCapability prev = (tc.getRole() == TransferCapability.Role.SCU
                ? scuTCs : scpTCs).put(tc.getSopClass(), tc);
        if (prev != null && prev != tc)
            prev.setApplicationEntity(null);
        return prev;
    }

    public TransferCapability removeTransferCapabilityFor(String sopClass,
            TransferCapability.Role role) {
        TransferCapability tc = (role == TransferCapability.Role.SCU ? scuTCs : scpTCs)
                        .remove(sopClass);
        if (tc != null)
            tc.setApplicationEntity(null);
        return tc;
    }

    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs =
                new ArrayList<TransferCapability>(scuTCs.size() + scpTCs.size());
        tcs.addAll(scpTCs.values());
        tcs.addAll(scuTCs.values());
        return tcs;
    }

    public Collection<TransferCapability> getTransferCapabilitiesWithRole(
            TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).values();
    }

    public TransferCapability getTransferCapabilityFor(
            String sopClass, TransferCapability.Role role) {
        return (role == TransferCapability.Role.SCU ? scuTCs : scpTCs).get(sopClass);
    }

    protected PresentationContext negotiate(AAssociateRQ rq, AAssociateAC ac,
           PresentationContext rqpc) {
       String as = rqpc.getAbstractSyntax();
       TransferCapability tc = roleSelection(rq, ac, as);
       int pcid = rqpc.getPCID();
       if (tc == null)
           return new PresentationContext(pcid,
                   PresentationContext.ABSTRACT_SYNTAX_NOT_SUPPORTED,
                   rqpc.getTransferSyntax());

       for (String ts : rqpc.getTransferSyntaxes())
           if (tc.containsTransferSyntax(ts)) {
               byte[] info = negotiate(rq.getExtNegotiationFor(as), tc);
               if (info != null)
                   ac.addExtendedNegotiation(new ExtendedNegotiation(as, info));
               return new PresentationContext(pcid,
                       PresentationContext.ACCEPTANCE, ts);
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

    private byte[] negotiate(ExtendedNegotiation exneg, TransferCapability tc) {
        if (exneg == null)
            return null;

        StorageOptions storageOptions = tc.getStorageOptions();
        if (storageOptions != null)
            return storageOptions.toExtendedNegotiationInformation();

        EnumSet<QueryOption> queryOptions = tc.getQueryOptions();
        if (queryOptions != null) {
            EnumSet<QueryOption> commonOpts = QueryOption.toOptions(exneg);
            commonOpts.retainAll(queryOptions);
            return QueryOption.toExtendedNegotiationInformation(commonOpts);
        }
        return null;
    }

    public Association connect(Connection local, Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        checkDevice();
        checkInstalled();
        if (rq.getCallingAET() == null)
            rq.setCallingAET(getCallingAETitle(rq.getCalledAET()));
        rq.setMaxOpsInvoked(local.getMaxOpsInvoked());
        rq.setMaxOpsPerformed(local.getMaxOpsPerformed());
        rq.setMaxPDULength(local.getReceivePDULength());
        Socket sock = local.connect(remote);
        Association as = new Association(this, local, sock);
        as.write(rq);
        as.waitForLeaving(State.Sta5);
        return as;
    }

    public Association connect(Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        return connect(findCompatibelConnection(remote), remote, rq);
    }

    public Connection findCompatibelConnection(Connection remoteConn)
            throws IncompatibleConnectionException {
        for (Connection conn : conns)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new IncompatibleConnectionException(
                "No compatible connection to " + remoteConn + " available on " + aet);
    }

    public CompatibleConnection findCompatibelConnection(ApplicationEntity remote)
            throws IncompatibleConnectionException {
        for (Connection remoteConn : remote.conns)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : conns)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn))
                        return new CompatibleConnection(conn, remoteConn);
        throw new IncompatibleConnectionException(
                "No compatible connection to " + remote.getAETitle() + " available on " + aet);
    }

    public Association connect(ApplicationEntity remote, AAssociateRQ rq)
        throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        CompatibleConnection cc = findCompatibelConnection(remote);
        if (rq.getCalledAET() == null)
            rq.setCalledAET(remote.getAETitle());
        return connect(cc.getLocalConnection(), cc.getRemoteConnection(), rq);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "ApplicationEntity[title: ", aet);
        StringUtils.appendLine(sb, indent2,"desc: ", description);
        StringUtils.appendLine(sb, indent2,"acceptor: ", acceptor);
        StringUtils.appendLine(sb, indent2,"initiator: ", initiator);
        StringUtils.appendLine(sb, indent2,"installed: ", getInstalled());
        for (Connection conn : conns)
            conn.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        for (TransferCapability tc : getTransferCapabilities())
            tc.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        return sb.append(indent).append(']');
    }

    void reconfigure(ApplicationEntity src) {
        setApplicationEntityAttributes(src);
        device.reconfigureConnections(conns, src.conns);
        reconfigureTransferCapabilities(src);
        reconfigureAEExtensions(src);
    }

    private void reconfigureTransferCapabilities(ApplicationEntity src) {
        scuTCs.clear();
        scuTCs.putAll(src.scuTCs);
        scpTCs.clear();
        scpTCs.putAll(src.scpTCs);
    }

    private void reconfigureAEExtensions(ApplicationEntity from) {
        for (Iterator<Class<? extends AEExtension>> it =
                extensions.keySet().iterator(); it.hasNext();) {
            if (!from.extensions.containsKey(it.next()))
                it.remove();
        }
        for (AEExtension src : from.extensions.values()) {
            Class<? extends AEExtension> clazz = src.getClass();
            AEExtension ext = extensions.get(clazz);
            if (ext == null)
                try {
                    addAEExtension(ext = clazz.newInstance());
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to instantiate " + clazz.getName(), e);
                }
            ext.reconfigure(src);
        }
    }

    protected void setApplicationEntityAttributes(ApplicationEntity from) {
        description = from.description;
        vendorData = from.vendorData;
        applicationClusters = from.applicationClusters;
        prefCalledAETs = from.prefCalledAETs;
        prefCallingAETs = from.prefCallingAETs;
        acceptedCallingAETs.clear();
        acceptedCallingAETs.addAll(from.acceptedCallingAETs);
        otherAETs.clear();
        otherAETs.addAll(from.otherAETs);
        masqueradeCallingAETs.clear();
        masqueradeCallingAETs.putAll(from.masqueradeCallingAETs);
        supportedCharacterSets = from.supportedCharacterSets;
        acceptor = from.acceptor;
        initiator = from.initiator;
        installed = from.installed;
    }

    public void addAEExtension(AEExtension ext) {
        Class<? extends AEExtension> clazz = ext.getClass();
        if (extensions.containsKey(clazz))
            throw new IllegalStateException(
                    "already contains AE Extension:" + clazz);

        ext.setApplicationEntity(this);
        extensions.put(clazz, ext);
    }

    public boolean removeAEExtension(AEExtension ext) {
        if (extensions.remove(ext.getClass()) == null)
            return false;

        ext.setApplicationEntity(null);
        return true;
    }

    public Collection<AEExtension> listAEExtensions() {
        return extensions.values();
    }

    @SuppressWarnings("unchecked")
    public <T extends AEExtension> T getAEExtension(Class<T> clazz) {
        return (T) extensions.get(clazz);
    }

    public <T extends AEExtension> T getAEExtensionNotNull(Class<T> clazz) {
        T aeExt = getAEExtension(clazz);
        if (aeExt == null)
            throw new IllegalStateException("No " + clazz.getName()
                    + " configured for AE: " + aet);
        return aeExt;
    }
}
