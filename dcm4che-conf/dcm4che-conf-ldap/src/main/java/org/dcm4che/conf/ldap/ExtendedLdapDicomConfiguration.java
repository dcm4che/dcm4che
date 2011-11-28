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

package org.dcm4che.conf.ldap;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.net.pdu.QueryOption;
import org.dcm4che.net.pdu.StorageOptions;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ExtendedLdapDicomConfiguration extends LdapDicomConfiguration {

    public ExtendedLdapDicomConfiguration(Hashtable<String, Object> env, String baseDN)
            throws NamingException {
        super(env, baseDN);
    }

    @Override
    protected String objectclassOf(Connection conn) {
        return "dcm4cheNetworkConnection";
    }

    @Override
    protected String objectclassOf(ApplicationEntity ae) {
        return "dcm4cheNetworkAE";
    }

    @Override
    protected String objectclassOf(TransferCapability tc) {
        return "dcm4cheTransferCapability";
    }

    @Override
    protected Attributes storeTo(Connection conn, Attributes attrs) {
        super.storeTo(conn, attrs);
        storeNotEmpty(attrs, "dcm4cheBlacklistedHostname", conn.getBlacklist());
        storeNotDef(attrs, "dcm4cheTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        storeNotDef(attrs, "dcm4cheTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheAssociationRequestTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheAssociationAcknowledgeTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheAssociationReleaseTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheDIMSEResponseTimeout",
                conn.getDimseRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheCGetResponseTimeout",
                conn.getCGetRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheCMoveResponseTimeout",
                conn.getCMoveRSPTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheAssociationIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcm4cheTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(attrs, "dcm4cheTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(attrs, "dcm4cheTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeBoolean(attrs, "dcm4cheTCPNoDelay", conn.isTcpNoDelay());
        storeNotEmpty(attrs, "dcm4cheTLSProtocol", conn.getTlsProtocols());
        storeBoolean(attrs, "dcm4cheTLSNeedClientAuth", conn.isTlsNeedClientAuth());
        return attrs;
    }

    @Override
    protected Attributes storeTo(ApplicationEntity ae, String deviceDN, Attributes attrs) {
        super.storeTo(ae, deviceDN, attrs);
        storeNotDef(attrs, "dcm4cheSendPDULength",
                ae.getSendPDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(attrs, "dcm4cheReceivePDULength",
                ae.getReceivePDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeNotDef(attrs, "dcm4cheMaxOpsPerformed",
                ae.getMaxOpsPerformed(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeNotDef(attrs, "dcm4cheMaxOpsInvoked",
                ae.getMaxOpsInvoked(), ApplicationEntity.SYNCHRONOUS_MODE);
        storeBoolean(attrs, "dcm4chePackPDV", ae.isPackPDV());
        storeBoolean(attrs, "dcm4cheAcceptOnlyPreferredCallingAETitle",
                ae.isAcceptOnlyPreferredCallingAETitles());
        return attrs;
    }

    @Override
    protected Attributes storeTo(TransferCapability tc, Attributes attrs) {
        super.storeTo(tc, attrs);
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            storeBoolean(attrs, "dcm4cheRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL));
            storeBoolean(attrs, "dcm4cheCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME));
            storeBoolean(attrs, "dcm4cheFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY));
            storeBoolean(attrs, "dcm4cheTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE));
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            storeInt(attrs, "dcm4cheLevelOfStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            storeInt(attrs, "dcm4cheLevelOfDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            storeInt(attrs, "dcm4cheDataElementCoercion",
                    storageOpts.getElementCoercion().ordinal());
        }
        return attrs;
    }

    @Override
    protected void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        super.loadFrom(conn, attrs);
        conn.setBlacklist(toStrings(attrs.get("dcm4cheBlacklistedHostname")));
        conn.setBacklog(toInt(attrs.get("dcm4cheTCPBacklog"), Connection.DEF_BACKLOG));
        conn.setConnectTimeout(toInt(attrs.get("dcm4cheTCPConnectTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRequestTimeout(toInt(attrs.get("dcm4cheAssociationRequestTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(toInt(attrs.get("dcm4cheAssociationAcknowledgeTimeout"),
                Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(toInt(attrs.get("dcm4cheAssociationReleaseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setDimseRSPTimeout(toInt(attrs.get("dcm4cheDIMSEResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setCGetRSPTimeout(toInt(attrs.get("dcm4cheCGetResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setCMoveRSPTimeout(toInt(attrs.get("dcm4cheCMoveResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setIdleTimeout(toInt(attrs.get("dcm4cheAssociationIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(toInt(attrs.get("dcm4cheTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(toInt(attrs.get("dcm4cheTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(toInt(attrs.get("dcm4cheTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(toBoolean(attrs.get("dcm4cheTCPNoDelay"), Boolean.TRUE));
        conn.setTlsNeedClientAuth(toBoolean(attrs.get("dcm4cheTLSNeedClientAuth"), Boolean.TRUE));
        conn.setTlsProtocols(toStrings(attrs.get("dcm4cheTLSProtocol")));
    }

    @Override
    protected void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        super.loadFrom(ae, attrs);
        ae.setSendPDULength(toInt(attrs.get("dcm4cheSendPDULength"),
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setReceivePDULength(toInt(attrs.get("dcm4cheReceivePDULength"),
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setMaxOpsPerformed(toInt(attrs.get("dcm4cheMaxOpsPerformed"),
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setMaxOpsInvoked(toInt(attrs.get("dcm4cheMaxOpsInvoked"),
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setPackPDV(toBoolean(attrs.get("dcm4chePackPDV"), Boolean.TRUE));
        ae.setAcceptOnlyPreferredCallingAETitles(
                toBoolean(attrs.get("dcm4cheAcceptOnlyPreferredCallingAETitle"), Boolean.FALSE));
    }

    @Override
    protected void loadFrom(TransferCapability tc, Attributes attrs)
            throws NamingException {
        super.loadFrom(tc, attrs);
        tc.setQueryOptions(toQueryOptions(attrs));
        tc.setStorageOptions(toStorageOptions(attrs));
    }

    private static EnumSet<QueryOption> toQueryOptions(Attributes attrs)
            throws NamingException {
        Attribute relational = attrs.get("dcm4cheRelationalQueries");
        Attribute datetime = attrs.get("dcm4cheCombinedDateTimeMatching");
        Attribute fuzzy = attrs.get("dcm4cheFuzzySemanticMatching");
        Attribute timezone = attrs.get("dcm4cheTimezoneQueryAdjustment");
        if (relational == null && datetime == null && fuzzy == null && timezone == null)
            return null;
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (toBoolean(relational, Boolean.FALSE))
            opts.add(QueryOption.RELATIONAL);
        if (toBoolean(datetime, Boolean.FALSE))
            opts.add(QueryOption.DATETIME);
        if (toBoolean(fuzzy, Boolean.FALSE))
            opts.add(QueryOption.FUZZY);
        if (toBoolean(timezone, Boolean.FALSE))
            opts.add(QueryOption.TIMEZONE);
        return opts ;
     }

    private static StorageOptions toStorageOptions(Attributes attrs) throws NamingException {
        Attribute levelOfSupport = attrs.get("dcm4cheLevelOfStorageConformance");
        Attribute signatureSupport = attrs.get("dcm4cheLevelOfDigitalSignatureSupport");
        Attribute coercion = attrs.get("dcm4cheDataElementCoercion");
        if (levelOfSupport == null && signatureSupport == null && coercion == null)
            return null;
        StorageOptions opts = new StorageOptions();
        opts.setLevelOfSupport(
                StorageOptions.LevelOfSupport.valueOf(toInt(levelOfSupport, 3)));
        opts.setDigitalSignatureSupport(
                StorageOptions.DigitalSignatureSupport.valueOf(toInt(signatureSupport, 0)));
        opts.setElementCoercion(
                StorageOptions.ElementCoercion.valueOf(toInt(coercion, 2)));
        return opts;
    }

    @Override
    protected void storeDiffs(Collection<ModificationItem> mods, Connection a, Connection b) {
        super.storeDiffs(mods, a, b);
        storeDiff(mods, "dcm4cheBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        storeDiff(mods, "dcm4cheTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        storeDiff(mods, "dcm4cheTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheAssociationRequestTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheAssociationAcknowledgeTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheAssociationReleaseTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheDIMSEResponseTimeout",
                a.getDimseRSPTimeout(),
                b.getDimseRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheCGetResponseTimeout",
                a.getCGetRSPTimeout(),
                b.getCGetRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheCMoveResponseTimeout",
                a.getCMoveRSPTimeout(),
                b.getCMoveRSPTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheAssociationIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcm4cheTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        storeDiff(mods, "dcm4cheTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(mods, "dcm4cheTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(mods, "dcm4cheTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay());
        storeDiff(mods, "dcm4cheTLSProtocol",
                a.getTlsProtocols(),
                b.getTlsProtocols());
        storeDiff(mods, "dcm4cheTLSNeedClientAuth",
                a.isTlsNeedClientAuth(),
                b.isTlsNeedClientAuth());
    }

    @Override
    protected void storeDiffs(Collection<ModificationItem> mods,
            ApplicationEntity a, ApplicationEntity b, String deviceDN) {
        super.storeDiffs(mods, a, b, deviceDN);
        storeDiff(mods, "dcm4cheSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeDiff(mods, "dcm4cheReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        storeDiff(mods, "dcm4cheMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        storeDiff(mods, "dcm4cheMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        storeDiff(mods, "dcm4chePackPDV",
                a.isPackPDV(),
                b.isPackPDV());
        storeDiff(mods, "dcm4cheAcceptOnlyPreferredCallingAETitle",
                a.isAcceptOnlyPreferredCallingAETitles(),
                b.isAcceptOnlyPreferredCallingAETitles());
    }

    @Override
    protected void storeDiffs(Collection<ModificationItem> mods,
            TransferCapability a, TransferCapability b) {
        super.storeDiffs(mods, a, b);
        storeDiffs(mods, a.getQueryOptions(), b.getQueryOptions());
        storeDiffs(mods, a.getStorageOptions(), b.getStorageOptions());
    }

    private void storeDiffs(Collection<ModificationItem> mods,
            EnumSet<QueryOption> prev, EnumSet<QueryOption> val) {
        storeDiff(mods, "dcm4cheRelationalQueries",
                prev != null ? prev.contains(QueryOption.RELATIONAL) : null,
                val != null ? val.contains(QueryOption.RELATIONAL) : null);
        storeDiff(mods, "dcm4cheCombinedDateTimeMatching",
                prev != null ? prev.contains(QueryOption.DATETIME) : null,
                val != null ? val.contains(QueryOption.DATETIME) : null);
        storeDiff(mods, "dcm4cheFuzzySemanticMatching",
                prev != null ? prev.contains(QueryOption.FUZZY) : null,
                val != null ? val.contains(QueryOption.FUZZY) : null);
        storeDiff(mods, "dcm4cheTimezoneQueryAdjustment",
                prev != null ? prev.contains(QueryOption.TIMEZONE) : null,
                val != null ? val.contains(QueryOption.TIMEZONE) : null);
    }

    private void storeDiffs(Collection<ModificationItem> mods,
            StorageOptions prev, StorageOptions val) {
        storeDiff(mods, "dcm4cheLevelOfStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        storeDiff(mods, "dcm4cheLevelOfDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        storeDiff(mods, "dcm4cheDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

}
