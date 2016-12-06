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

import org.dcm4che3.conf.core.api.ConfigurableClass;
import org.dcm4che3.conf.core.api.ConfigurableProperty;
import org.dcm4che3.conf.core.api.ConfigurableProperty.ConfigurablePropertyType;
import org.dcm4che3.conf.core.api.ConfigurableProperty.Tag;
import org.dcm4che3.conf.core.api.LDAP;
import org.dcm4che3.conf.core.api.Parent;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.pdu.*;
import org.dcm4che3.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * DICOM Part 15, Annex H compliant description of a DICOM network service.
 * <p/>
 * A Network AE is an application entity that provides services on a network. A
 * Network AE will have the 16 same functional capability regardless of the
 * particular network connection used. If there are functional differences based
 * on selected network connection, then these are separate Network AEs. If there
 * are 18 functional differences based on other internal structures, then these
 * are separate Network AEs.
 *
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
@LDAP(objectClasses = {"dcmNetworkAE", "dicomNetworkAE"}, distinguishingField = "dicomAETitle")
@ConfigurableClass(referable = true)
public class ApplicationEntity implements Serializable {

    private static final long serialVersionUID = 3883790997057469573L;

    protected static final Logger LOG = LoggerFactory.getLogger(ApplicationEntity.class);


    @ConfigurableProperty(name = "dicomAETitle", tags = Tag.PRIMARY)
    private String AETitle;

    @ConfigurableProperty(type = ConfigurablePropertyType.UUID, description = "An immutable unique identifier")
    private String uuid = UUID.randomUUID().toString();

    @ConfigurableProperty(name = "dicomDescription")
    private String description;

    @ConfigurableProperty(type = ConfigurablePropertyType.OptimisticLockingHash)
    private String olockHash;

    @ConfigurableProperty(name = "dicomVendorData")
    private byte[][] vendorData = {};

    @ConfigurableProperty(name = "dicomApplicationCluster")
    private String[] applicationClusters = {};

    @ConfigurableProperty(name = "dicomPreferredCalledAETitle")
    private String[] preferredCalledAETitles = {};

    @ConfigurableProperty(name = "dicomPreferredCallingAETitle")
    private String[] preferredCallingAETitles = {};

    @ConfigurableProperty(name = "dicomSupportedCharacterSet")
    private String[] supportedCharacterSets = {};

    @ConfigurableProperty(name = "dicomInstalled")
    private Boolean aeInstalled;

    @ConfigurableProperty(name = "dcmAcceptedCallingAETitle")
    private final Set<String> acceptedCallingAETitlesSet =
            new LinkedHashSet<String>();

    // Connections are dereferenced by DicomConfiguration
    @ConfigurableProperty(name = "dicomNetworkConnectionReference", collectionOfReferences = true, tags = Tag.PRIMARY)
    private final List<Connection> connections = new ArrayList<Connection>(1);

    /**
     * "Proxy" property, actually forwards everything to scuTCs and scpTCs in its setter/getter
     */
    @LDAP(noContainerNode = true)
    @ConfigurableProperty(name = "dcmTransferCapability",
            description = "DICOM Transfer Capabilities",
            tags = Tag.PRIMARY)
    private Collection<TransferCapability> transferCapabilities;

    // populated/collected by transferCapabilities' setter/getter
    private final Map<String, TransferCapability> scuTCs =
            new TreeMap<String, TransferCapability>();

    // populated/collected by transferCapabilities' setter/getter
    private final Map<String, TransferCapability> scpTCs =
            new TreeMap<String, TransferCapability>();

    @ConfigurableProperty(name = "aeExtensions", isExtensionsProperty = true)
    private Map<Class<? extends AEExtension>, AEExtension> extensions =
            new HashMap<Class<? extends AEExtension>, AEExtension>();

    @ConfigurableProperty(name = "dicomAssociationAcceptor")
    private boolean associationAcceptor = true;

    @ConfigurableProperty(name = "dicomAssociationInitiator")
    private boolean associationInitiator = true;

    @ConfigurableProperty(name = "dcmAETitleAliases",
            label = "Aliases (alternative AE titles)")
    private List<String> AETitleAliases = new ArrayList<String>();

    @Parent
    private Device device;

    private transient DimseRQHandler dimseRQHandler;

    public ApplicationEntity() {
    }

    public ApplicationEntity(String aeTitle) {
        setAETitle(aeTitle);
    }

    public List<String> getAETitleAliases() {
        return new ArrayList<String>(AETitleAliases);
    }

    public void setAETitleAliases(List<String> AETitleAliases) {
        this.AETitleAliases = AETitleAliases;
    }

    public Map<Class<? extends AEExtension>, AEExtension> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<Class<? extends AEExtension>, AEExtension> extensions) {
        this.extensions = extensions;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setTransferCapabilities(Collection<TransferCapability> transferCapabilities) {
        scpTCs.clear();
        scuTCs.clear();

        for (TransferCapability tc : transferCapabilities) {
            tc.setApplicationEntity(this);
            switch (tc.getRole()) {
                case SCP:
                    scpTCs.put(tc.getSopClass(), tc);
                    break;
                case SCU:
                    scuTCs.put(tc.getSopClass(), tc);
            }
        }
    }

    public Collection<TransferCapability> getTransferCapabilities() {
        ArrayList<TransferCapability> tcs =
                new ArrayList<TransferCapability>(scuTCs.size() + scpTCs.size());
        tcs.addAll(scpTCs.values());
        tcs.addAll(scuTCs.values());
        return tcs;
    }

    /**
     * Get the device that is identified by this application entity.
     *
     * @return The owning <code>Device</code>.
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Set the device that is identified by this application entity.
     *
     * @param device The owning <code>Device</code>.
     */
    public void setDevice(Device device) {
        if (device != null) {
            if (this.device != null && this.device != device)
                throw new IllegalStateException("already owned by " + this.device.getDeviceName());
            for (Connection conn : connections)
                if (conn.getDevice() != device)
                    throw new IllegalStateException(conn + " not owned by " +
                            device.getDeviceName());
        }
        this.device = device;
    }

    /**
     * Get the AE title for this Network AE.
     * <p/>
     * <p/>
     * Please note that there could also be alias AE titles for the same AE. You
     * can get them via {@link #getAETitleAliases()}.
     *
     * @return A String containing the AE title.
     */
    public final String getAETitle() {
        return AETitle;
    }

    /**
     * Set the AE title for this Network AE.
     *
     * @param aet A String containing the AE title.
     */
    public void setAETitle(String aet) {
        if (aet.isEmpty())
            throw new IllegalArgumentException("AE title cannot be empty");
        Device device = this.device;
        if (device != null && this.AETitle != null)
            device.removeApplicationEntity(this.AETitle);
        this.AETitle = aet;
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
     * @param description A String containing the description.
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
     * @param vendorData An Object of the vendor data.
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
        return preferredCalledAETitles;
    }

    public void setPreferredCalledAETitles(String... aets) {
        preferredCalledAETitles = aets;
    }

    /**
     * Get the AE title(s) that are preferred for accepting associations by
     * this network AE.
     *
     * @return A String[] containing the preferred calling AE titles.
     */
    public String[] getPreferredCallingAETitles() {
        return preferredCallingAETitles;
    }

    public void setPreferredCallingAETitles(String... aets) {
        preferredCallingAETitles = aets;
    }

    public String[] getAcceptedCallingAETitles() {
        return acceptedCallingAETitlesSet.toArray(
                new String[acceptedCallingAETitlesSet.size()]);
    }

    public void setAcceptedCallingAETitles(String... aets) {
        acceptedCallingAETitlesSet.clear();
        for (String name : aets)
            acceptedCallingAETitlesSet.add(name);
    }

    public boolean isAcceptedCallingAETitle(String aet) {
        return acceptedCallingAETitlesSet.isEmpty()
                || acceptedCallingAETitlesSet.contains(aet);
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
     * @param characterSets A String array of the supported character sets.
     */
    public void setSupportedCharacterSets(String... characterSets) {
        supportedCharacterSets = characterSets;
    }

    /**
     * Determine whether or not this network AE can accept associations.
     *
     * @return A boolean value. True if the Network AE can accept associations,
     * false otherwise.
     */
    public final boolean isAssociationAcceptor() {
        return associationAcceptor;
    }

    /**
     * Set whether or not this network AE can accept associations.
     *
     * @param acceptor A boolean value. True if the Network AE can accept
     *                 associations, false otherwise.
     */
    public final void setAssociationAcceptor(boolean acceptor) {
        this.associationAcceptor = acceptor;
    }

    /**
     * Determine whether or not this network AE can initiate associations.
     *
     * @return A boolean value. True if the Network AE can accept associations,
     * false otherwise.
     */
    public final boolean isAssociationInitiator() {
        return associationInitiator;
    }

    /**
     * Set whether or not this network AE can initiate associations.
     *
     * @param initiator A boolean value. True if the Network AE can accept
     *                  associations, false otherwise.
     */
    public final void setAssociationInitiator(boolean initiator) {
        this.associationInitiator = initiator;
    }


    /**
     * Determine whether or not this network AE is installed on a network.
     *
     * @return A Boolean value. True if the AE is installed on a network. If not
     * present, information about the installed status of the AE is
     * inherited from the device
     */
    public boolean isInstalled() {
        return device != null && device.isInstalled()
                && (aeInstalled == null || aeInstalled.booleanValue());
    }

    public Boolean getAeInstalled() {
        return aeInstalled;
    }


    /**
     * Set whether or not this network AE is installed on a network.
     *
     * @param aeInstalled A Boolean value. True if the AE is installed on a network.
     *                    If not present, information about the installed status of
     *                    the AE is inherited from the device
     */
    public void setAeInstalled(Boolean aeInstalled) {
        this.aeInstalled = aeInstalled;
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
        connections.add(conn);
    }

    public boolean removeConnection(Connection conn) {
        return connections.remove(conn);
    }

    public void setConnections(List<Connection> connections) {
        this.connections.clear();
        for (Connection connection : connections) addConnection(connection);
    }

    public List<Connection> getConnections() {
        return connections;
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

    private TransferCapability getTC(Map<String, TransferCapability> tcs,
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
            rq.setCallingAET(AETitle);
        rq.setMaxOpsInvoked(local.getMaxOpsInvoked());
        rq.setMaxOpsPerformed(local.getMaxOpsPerformed());
        rq.setMaxPDULength(local.getReceivePDULength());

        final Socket sock = local.connect(remote); // automatically closes the socket in case an exception is thrown

        Association as;
        try {
            as = new Association(this, local, sock);
        } catch (final IOException e) {
            LOG.warn("Failed to open new association, will close underlying socket");
            local.close(sock);
            throw e;
        }
        try {
            as.write(rq);
            as.waitForLeaving(State.Sta5);
        } catch (final IOException e) {
            LOG.warn("{}: Failed to write A-ASSOCIATE-RQ, will abort association", as.toString());
            as.abort();
            throw e;
        } catch (final InterruptedException e) {
            LOG.warn("{}: Interrupted while waiting to leave state Sta 5, will abort association", as.toString());
            as.abort();
            throw e;
        }
        return as;
    }

    public Association connect(Connection remote, AAssociateRQ rq)
            throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException {
        return connect(findCompatibelConnection(remote), remote, rq);
    }

    public Connection findCompatibelConnection(Connection remoteConn)
            throws IncompatibleConnectionException {
        for (Connection conn : connections)
            if (conn.isInstalled() && conn.isCompatible(remoteConn))
                return conn;
        throw new IncompatibleConnectionException(
                "No compatible connection to " + remoteConn + " available on " + this);
    }

    public CompatibleConnection findCompatibelConnection(ApplicationEntity remote)
            throws IncompatibleConnectionException {
        CompatibleConnection cc = null;
        for (Connection remoteConn : remote.connections)
            if (remoteConn.isInstalled() && remoteConn.isServer())
                for (Connection conn : connections)
                    if (conn.isInstalled() && conn.isCompatible(remoteConn)) {
                        if (cc == null
                                || conn.isTls()
                                || conn.getProtocol() == Connection.Protocol.SYSLOG_TLS)
                            cc = new CompatibleConnection(conn, remoteConn);
                    }
        if (cc == null)
            throw new IncompatibleConnectionException(
                    "No compatible connection to " + remote + " available on " + this);
        return cc;
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
        StringUtils.appendLine(sb, indent, "ApplicationEntity[title: ", AETitle);
        StringUtils.appendLine(sb, indent2, "alias titles: ", AETitleAliases);
        StringUtils.appendLine(sb, indent2, "desc: ", description);
        StringUtils.appendLine(sb, indent2, "acceptor: ", associationAcceptor);
        StringUtils.appendLine(sb, indent2, "initiator: ", associationInitiator);
        StringUtils.appendLine(sb, indent2, "installed: ", getAeInstalled());
        for (Connection conn : connections)
            conn.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        for (TransferCapability tc : getTransferCapabilities())
            tc.promptTo(sb, indent2).append(StringUtils.LINE_SEPARATOR);
        return sb.append(indent).append(']');
    }

    void reconfigure(ApplicationEntity src) {
        setApplicationEntityAttributes(src);
        device.reconfigureConnections(connections, src.connections);
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
             extensions.keySet().iterator(); it.hasNext(); ) {
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
        setOlockHash(from.olockHash);
        setDescription(from.description);
        setAETitleAliases(from.getAETitleAliases());
        setVendorData(from.vendorData);
        setApplicationClusters(from.applicationClusters);
        setPreferredCalledAETitles(from.preferredCalledAETitles);
        setPreferredCallingAETitles(from.preferredCallingAETitles);
        setAcceptedCallingAETitles(from.getAcceptedCallingAETitles());
        setSupportedCharacterSets(from.supportedCharacterSets);
        setAssociationAcceptor(from.associationAcceptor);
        setAssociationInitiator(from.associationInitiator);
        setAeInstalled(from.aeInstalled);
        setUuid(from.getUuid());
    }

    public Set<String> getAcceptedCallingAETitlesSet() {
        return acceptedCallingAETitlesSet;
    }

    public void setAcceptedCallingAETitlesSet(Set<String> acceptedCallingAETitlesSet) {
        this.acceptedCallingAETitlesSet.clear();
        if (acceptedCallingAETitlesSet != null)
            this.acceptedCallingAETitlesSet.addAll(acceptedCallingAETitlesSet);
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

    public String getOlockHash() {
        return olockHash;
    }

    public void setOlockHash(String olockHash) {
        this.olockHash = olockHash;
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
                    + " configured for AE: " + AETitle);
        return aeExt;
    }

    public boolean supportsTransferCapability(
            TransferCapability transferCapability, boolean onlyAbstractSyntax) {
        TransferCapability matchingTC = this.getTransferCapabilityFor(
                transferCapability.getSopClass(), transferCapability.getRole());
        if (matchingTC == null)
            return false;
        else
            for (String ts : transferCapability.getTransferSyntaxes())
                if (!matchingTC.containsTransferSyntax(ts)
                        && !onlyAbstractSyntax)
                    return false;

        return true;
    }

}