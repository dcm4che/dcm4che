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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import org.dcm4che.conf.api.AttributeCoercion;
import org.dcm4che.conf.api.AttributeCoercions;
import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.QueryOption;
import org.dcm4che.net.StorageOptions;
import org.dcm4che.net.TransferCapability;
import org.dcm4che.util.StringUtils;

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
    protected Attribute objectClassesOf(Connection conn, Attribute attr) {
        super.objectClassesOf(conn, attr);
        attr.add("dcmNetworkConnection");
        return attr;
    }

    @Override
    protected Attribute objectClassesOf(ApplicationEntity ae, Attribute attr) {
        super.objectClassesOf(ae, attr);
        attr.add("dcmNetworkAE");
        return attr;
    }

    @Override
    protected Attribute objectClassesOf(TransferCapability tc, Attribute attr) {
        super.objectClassesOf(tc, attr);
        attr.add("dcmTransferCapability");
        return attr;
    }

    @Override
    protected Attributes storeTo(Connection conn, Attributes attrs) {
        super.storeTo(conn, attrs);
        storeNotEmpty(attrs, "dcmBlacklistedHostname", conn.getBlacklist());
        storeNotDef(attrs, "dcmTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        storeNotDef(attrs, "dcmTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmAARQTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmAAACTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmARRPTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmResponseTimeout",
                conn.getResponseTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmRetrieveTimeout",
                conn.getRetrieveTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        storeNotDef(attrs, "dcmTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        storeNotDef(attrs, "dcmTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(attrs, "dcmTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        storeNotDef(attrs, "dcmTCPNoDelay", conn.isTcpNoDelay(), true);
        storeNotDef(attrs, "dcmSendPDULength",
                conn.getSendPDULength(), Connection.DEF_MAX_PDU_LENGTH);
        storeNotDef(attrs, "dcmReceivePDULength",
                conn.getReceivePDULength(), Connection.DEF_MAX_PDU_LENGTH);
        storeNotDef(attrs, "dcmMaxOpsPerformed",
                conn.getMaxOpsPerformed(), Connection.SYNCHRONOUS_MODE);
        storeNotDef(attrs, "dcmMaxOpsInvoked",
                conn.getMaxOpsInvoked(), Connection.SYNCHRONOUS_MODE);
        storeNotDef(attrs, "dcmPackPDV", conn.isPackPDV(), true);
        if (conn.isTls()) {
            storeNotEmpty(attrs, "dcmTLSProtocol", conn.getTlsProtocols());
            storeNotDef(attrs, "dcmTLSNeedClientAuth", conn.isTlsNeedClientAuth(), true);
        }
        return attrs;
    }

    @Override
    protected Attributes storeTo(ApplicationEntity ae, String deviceDN, Attributes attrs) {
        super.storeTo(ae, deviceDN, attrs);
        storeNotEmpty(attrs, "dcmAcceptedCallingAETitle", ae.getAcceptedCallingAETitles());
        return attrs;
    }

    @Override
    protected Attributes storeTo(TransferCapability tc, Attributes attrs) {
        super.storeTo(tc, attrs);
        
        EnumSet<QueryOption> queryOpts = tc.getQueryOptions();
        if (queryOpts != null) {
            storeNotDef(attrs, "dcmRelationalQueries",
                    queryOpts.contains(QueryOption.RELATIONAL), false);
            storeNotDef(attrs, "dcmCombinedDateTimeMatching",
                    queryOpts.contains(QueryOption.DATETIME), false);
            storeNotDef(attrs, "dcmFuzzySemanticMatching",
                    queryOpts.contains(QueryOption.FUZZY), false);
            storeNotDef(attrs, "dcmTimezoneQueryAdjustment",
                    queryOpts.contains(QueryOption.TIMEZONE), false);
        }
        StorageOptions storageOpts = tc.getStorageOptions();
        if (storageOpts != null) {
            storeInt(attrs, "dcmStorageConformance",
                    storageOpts.getLevelOfSupport().ordinal());
            storeInt(attrs, "dcmDigitalSignatureSupport",
                    storageOpts.getDigitalSignatureSupport().ordinal());
            storeInt(attrs, "dcmDataElementCoercion",
                    storageOpts.getElementCoercion().ordinal());
        }
        return attrs;
    }

    protected void store(AttributeCoercions coercions, String parentDN)
        throws NamingException {
        for (AttributeCoercion ac : coercions.getAll())
            createSubcontext(dnOf(ac, parentDN), storeTo(ac, new BasicAttributes(true)));
    }

    private static String dnOf(AttributeCoercion ac, String parentDN) {
        StringBuilder sb = new StringBuilder();
        sb.append("dcmDIMSE=").append(ac.getDimse());
        sb.append("+dicomTransferRole=").append(ac.getRole());
        if (ac.getAETitle() != null)
            sb.append("+dicomAETitle=").append(ac.getAETitle());
        if (ac.getSopClass() != null)
            sb.append("+dicomSOPClass=").append(ac.getSopClass());
        sb.append(',').append(parentDN);
        return sb.toString();
    }

    private static Attributes storeTo(AttributeCoercion ac, BasicAttributes attrs) {
        attrs.put("objectclass", "dcmAttributeCoercion");
        storeNotNull(attrs, "dcmDIMSE", ac.getDimse());
        storeNotNull(attrs, "dicomTransferRole", ac.getRole());
        storeNotNull(attrs, "dicomAETitle", ac.getAETitle());
        storeNotNull(attrs, "dicomSOPClass", ac.getSopClass());
        storeNotNull(attrs, "labeledURI", ac.getURI());
        return attrs;
    }

    @Override
    protected void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        super.loadFrom(conn, attrs);
        if (!hasObjectClass(attrs, "dcmNetworkConnection"))
            return;
        conn.setBlacklist(stringArray(attrs.get("dcmBlacklistedHostname")));
        conn.setBacklog(intValue(attrs.get("dcmTCPBacklog"), Connection.DEF_BACKLOG));
        conn.setConnectTimeout(intValue(attrs.get("dcmTCPConnectTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRequestTimeout(intValue(attrs.get("dcmAARQTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(intValue(attrs.get("dcmAAACTimeout"),
                Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(intValue(attrs.get("dcmARRPTimeout"),
                Connection.NO_TIMEOUT));
        conn.setResponseTimeout(intValue(attrs.get("dcmResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRetrieveTimeout(intValue(attrs.get("dcmRetrieveTimeout"),
                Connection.NO_TIMEOUT));
        conn.setIdleTimeout(intValue(attrs.get("dcmIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(intValue(attrs.get("dcmTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(intValue(attrs.get("dcmTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(intValue(attrs.get("dcmTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(booleanValue(attrs.get("dcmTCPNoDelay"), true));
        conn.setTlsNeedClientAuth(booleanValue(attrs.get("dcmTLSNeedClientAuth"), true));
        String[] tlsProtocols = stringArray(attrs.get("dcmTLSProtocol"));
        if (tlsProtocols.length > 0)
            conn.setTlsProtocols(tlsProtocols);
        conn.setSendPDULength(intValue(attrs.get("dcmSendPDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setReceivePDULength(intValue(attrs.get("dcmReceivePDULength"),
                Connection.DEF_MAX_PDU_LENGTH));
        conn.setMaxOpsPerformed(intValue(attrs.get("dcmMaxOpsPerformed"),
                Connection.SYNCHRONOUS_MODE));
        conn.setMaxOpsInvoked(intValue(attrs.get("dcmMaxOpsInvoked"),
                Connection.SYNCHRONOUS_MODE));
        conn.setPackPDV(booleanValue(attrs.get("dcmPackPDV"), true));
    }

    @Override
    protected void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        super.loadFrom(ae, attrs);
        if (!hasObjectClass(attrs, "dcmNetworkAE"))
            return;
        ae.setAcceptedCallingAETitles(stringArray(attrs.get("dcmAcceptedCallingAETitle")));
    }

    @Override
    protected void loadFrom(TransferCapability tc, Attributes attrs)
            throws NamingException {
        super.loadFrom(tc, attrs);
        if (!hasObjectClass(attrs, "dcmTransferCapability"))
            return;
        tc.setQueryOptions(toQueryOptions(attrs));
        tc.setStorageOptions(toStorageOptions(attrs));
    }

    private static EnumSet<QueryOption> toQueryOptions(Attributes attrs)
            throws NamingException {
        Attribute relational = attrs.get("dcmRelationalQueries");
        Attribute datetime = attrs.get("dcmCombinedDateTimeMatching");
        Attribute fuzzy = attrs.get("dcmFuzzySemanticMatching");
        Attribute timezone = attrs.get("dcmTimezoneQueryAdjustment");
        if (relational == null && datetime == null && fuzzy == null && timezone == null)
            return null;
        EnumSet<QueryOption> opts = EnumSet.noneOf(QueryOption.class);
        if (booleanValue(relational, false))
            opts.add(QueryOption.RELATIONAL);
        if (booleanValue(datetime, false))
            opts.add(QueryOption.DATETIME);
        if (booleanValue(fuzzy, false))
            opts.add(QueryOption.FUZZY);
        if (booleanValue(timezone, false))
            opts.add(QueryOption.TIMEZONE);
        return opts ;
     }

    private static StorageOptions toStorageOptions(Attributes attrs) throws NamingException {
        Attribute levelOfSupport = attrs.get("dcmStorageConformance");
        Attribute signatureSupport = attrs.get("dcmDigitalSignatureSupport");
        Attribute coercion = attrs.get("dcmDataElementCoercion");
        if (levelOfSupport == null && signatureSupport == null && coercion == null)
            return null;
        StorageOptions opts = new StorageOptions();
        opts.setLevelOfSupport(
                StorageOptions.LevelOfSupport.valueOf(intValue(levelOfSupport, 3)));
        opts.setDigitalSignatureSupport(
                StorageOptions.DigitalSignatureSupport.valueOf(intValue(signatureSupport, 0)));
        opts.setElementCoercion(
                StorageOptions.ElementCoercion.valueOf(intValue(coercion, 2)));
        return opts;
    }

    protected void load(AttributeCoercions acs, String dn) throws NamingException {
        NamingEnumeration<SearchResult> ne = search(dn, "(objectclass=dcmAttributeCoercion)");
        try {
            while (ne.hasMore()) {
                SearchResult sr = ne.next();
                Attributes attrs = sr.getAttributes();
                acs.add(new AttributeCoercion(
                        stringValue(attrs.get("dicomSOPClass")),
                        AttributeCoercion.DIMSE.valueOf(
                                stringValue(attrs.get("dcmDIMSE"))),
                        TransferCapability.Role.valueOf(
                                stringValue(attrs.get("dicomTransferRole"))),
                        stringValue(attrs.get("dicomAETitle")),
                        stringValue(attrs.get("labeledURI"))));
            }
        } finally {
           safeClose(ne);
        }
    }


    @Override
    protected List<ModificationItem> storeDiffs(Connection a, Connection b,
            List<ModificationItem> mods) {
        super.storeDiffs(a, b, mods);
        storeDiff(mods, "dcmBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        storeDiff(mods, "dcmTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                Connection.DEF_BACKLOG);
        storeDiff(mods, "dcmTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmAARQTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmAAACTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmARRPTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmResponseTimeout",
                a.getResponseTimeout(),
                b.getResponseTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmRetrieveTimeout",
                a.getRetrieveTimeout(),
                b.getRetrieveTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        storeDiff(mods, "dcmTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        storeDiff(mods, "dcmTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(mods, "dcmTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
        storeDiff(mods, "dcmTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay(),
                true);
        storeDiff(mods, "dcmTLSProtocol",
                a.isTls() ? a.getTlsProtocols() : StringUtils.EMPTY_STRING,
                b.isTls() ? b.getTlsProtocols() : StringUtils.EMPTY_STRING);
        storeDiff(mods, "dcmTLSNeedClientAuth",
                !a.isTls() || a.isTlsNeedClientAuth(),
                !a.isTls() || a.isTlsNeedClientAuth(),
                true);
        storeDiff(mods, "dcmSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        storeDiff(mods, "dcmReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                Connection.DEF_MAX_PDU_LENGTH);
        storeDiff(mods, "dcmMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                Connection.SYNCHRONOUS_MODE);
        storeDiff(mods, "dcmMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                Connection.SYNCHRONOUS_MODE);
        storeDiff(mods, "dcmPackPDV",
                a.isPackPDV(),
                b.isPackPDV(),
                true);
        return mods;
    }

    @Override
    protected List<ModificationItem> storeDiffs(ApplicationEntity a,
            ApplicationEntity b, String deviceDN, List<ModificationItem> mods) {
        super.storeDiffs(a, b, deviceDN, mods);
        storeDiff(mods, "dcmAcceptedCallingAETitle",
                a.getAcceptedCallingAETitles(),
                b.getAcceptedCallingAETitles());
        return mods;
    }

    @Override
    protected List<ModificationItem> storeDiffs(TransferCapability a,
            TransferCapability b, List<ModificationItem> mods) {
        super.storeDiffs(a, b, mods);
        storeDiffs(a.getQueryOptions(), b.getQueryOptions(), mods);
        storeDiffs(a.getStorageOptions(), b.getStorageOptions(), mods);
        return mods;
    }

    private void storeDiffs(EnumSet<QueryOption> prev,
            EnumSet<QueryOption> val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        storeDiff(mods, "dcmRelationalQueries",
                prev != null && prev.contains(QueryOption.RELATIONAL),
                val != null && val.contains(QueryOption.RELATIONAL),
                false);
        storeDiff(mods, "dcmCombinedDateTimeMatching",
                prev != null && prev.contains(QueryOption.DATETIME),
                val != null && val.contains(QueryOption.DATETIME),
                false);
        storeDiff(mods, "dcmFuzzySemanticMatching",
                prev != null && prev.contains(QueryOption.FUZZY),
                val != null && val.contains(QueryOption.FUZZY),
                false);
        storeDiff(mods, "dcmTimezoneQueryAdjustment",
                prev != null && prev.contains(QueryOption.TIMEZONE),
                val != null && val.contains(QueryOption.TIMEZONE),
                false);
    }

    private void storeDiffs(StorageOptions prev,
            StorageOptions val, List<ModificationItem> mods) {
        if (prev != null ? prev.equals(val) : val == null)
            return;

        storeDiff(mods, "dcmStorageConformance",
                prev != null ? prev.getLevelOfSupport().ordinal() : -1,
                val != null ? val.getLevelOfSupport().ordinal() : -1,
                -1);
        storeDiff(mods, "dcmDigitalSignatureSupport",
                prev != null ? prev.getDigitalSignatureSupport().ordinal() : -1,
                val != null ? val.getDigitalSignatureSupport().ordinal() : -1,
                -1);
        storeDiff(mods, "dcmDataElementCoercion",
                prev != null ? prev.getElementCoercion().ordinal() : -1,
                val != null ? val.getElementCoercion().ordinal() : -1,
                -1);
    }

    protected void merge(AttributeCoercions prevs, AttributeCoercions acs, String parentDN)
            throws NamingException {
        for (AttributeCoercion prev : prevs.getAll())
            if (acs.getEquals(prev) == null)
                destroySubcontext(dnOf(prev, parentDN));
        for (AttributeCoercion ac : acs.getAll()) {
            String dn = dnOf(ac, parentDN);
            AttributeCoercion prev = prevs.getEquals(ac);
            if (prev == null)
                createSubcontext(dn, storeTo(ac, new BasicAttributes(true)));
            else
                modifyAttributes(dn, storeDiffs(prev, ac, new ArrayList<ModificationItem>()));
        }
    }

    private List<ModificationItem> storeDiffs(AttributeCoercion prev,
            AttributeCoercion ac, ArrayList<ModificationItem> mods) {
        storeDiff(mods, "labeledURI", prev.getURI(), ac.getURI());
        return mods;
    }

}
