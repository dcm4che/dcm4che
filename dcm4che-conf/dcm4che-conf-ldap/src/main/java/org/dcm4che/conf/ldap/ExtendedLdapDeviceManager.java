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
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import org.dcm4che.net.ApplicationEntity;
import org.dcm4che.net.Connection;
import org.dcm4che.net.pdu.AAssociateRQAC;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class ExtendedLdapDeviceManager extends LdapDeviceManager {

    public ExtendedLdapDeviceManager(Hashtable<String, Object> env, String baseDN)
            throws NamingException {
        super(env, baseDN);
    }

    @Override
    protected Attributes attrsOf(Connection conn) {
        Attributes attrs = super.attrsOf(conn);
        addNotEmpty(attrs, "dicomBlacklistedHostname", conn.getBlacklist());
        addNotDef(attrs, "dicomTCPBacklog", conn.getBacklog(), 50);
        addNotDef(attrs, "dicomTCPConnectTimeout", conn.getConnectTimeout(), 0);
        addNotDef(attrs, "dicomAssociationRequestTimeout", conn.getRequestTimeout(), 0);
        addNotDef(attrs, "dicomAssociationAcknowledgeTimeout", conn.getAcceptTimeout(), 0);
        addNotDef(attrs, "dicomAssociationReleaseTimeout", conn.getReleaseTimeout(), 0);
        addNotDef(attrs, "dicomDIMSEResponseTimeout", conn.getDimseRSPTimeout(), 0);
        addNotDef(attrs, "dicomCGetResponseTimeout", conn.getCGetRSPTimeout(), 0);
        addNotDef(attrs, "dicomCMoveResponseTimeout", conn.getCMoveRSPTimeout(), 0);
        addNotDef(attrs, "dicomAssociationIdleTimeout", conn.getIdleTimeout(), 0);
        addNotDef(attrs, "dicomTCPCloseDelay", conn.getSocketCloseDelay(), 50);
        addNotDef(attrs, "dicomTCPSendBufferSize", conn.getSendBufferSize(), 0);
        addNotDef(attrs, "dicomTCPReceiveBufferSize", conn.getReceiveBufferSize(), 0);
        addBoolean(attrs, "dicomTCPNoDelay", conn.isTcpNoDelay());
        addNotEmpty(attrs, "dicomTLSProtocol", conn.getTlsProtocols());
        addBoolean(attrs, "dicomTLSNeedClientAuth", conn.isTlsNeedClientAuth());
        return attrs;
    }

    @Override
    protected Attributes attrsOf(ApplicationEntity ae, String deviceDN) {
        Attributes attrs = super.attrsOf(ae, deviceDN);
        addNotDef(attrs, "dicomSendPDULength", ae.getSendPDULength(),
                AAssociateRQAC.DEF_MAX_PDU_LENGTH);
        addNotDef(attrs, "dicomReceivePDULength", ae.getReceivePDULength(),
                AAssociateRQAC.DEF_MAX_PDU_LENGTH);
        addNotDef(attrs, "dicomMaxOpsPerformed", ae.getMaxOpsPerformed(), 1);
        addNotDef(attrs, "dicomMaxOpsInvoked", ae.getMaxOpsInvoked(), 1);
        addBoolean(attrs, "dicomPackPDV", ae.isPackPDV());
        return attrs;
    }


    @Override
    protected void load(Connection conn, Attributes attrs) throws NamingException {
        super.load(conn, attrs);
        conn.setBlacklist(toStrings(attrs.get("dicomBlacklistedHostname")));
        conn.setBacklog(toInt(attrs.get("dicomTCPBacklog"), 50));
        conn.setConnectTimeout(toInt(attrs.get("dicomTCPConnectTimeout"), 0));
        conn.setRequestTimeout(toInt(attrs.get("dicomAssociationRequestTimeout"), 0));
        conn.setAcceptTimeout(toInt(attrs.get("dicomAssociationAcknowledgeTimeout"), 0));
        conn.setReleaseTimeout(toInt(attrs.get("dicomAssociationReleaseTimeout"), 0));
        conn.setDimseRSPTimeout(toInt(attrs.get("dicomDIMSEResponseTimeout"), 0));
        conn.setCGetRSPTimeout(toInt(attrs.get("dicomCGetResponseTimeout"), 0));
        conn.setCMoveRSPTimeout(toInt(attrs.get("dicomCMoveResponseTimeout"), 0));
        conn.setIdleTimeout(toInt(attrs.get("dicomAssociationIdleTimeout"), 0));
        conn.setSocketCloseDelay(toInt(attrs.get("dicomTCPCloseDelay"), 50));
        conn.setSendBufferSize(toInt(attrs.get("dicomTCPSendBufferSize"), 0));
        conn.setReceiveBufferSize(toInt(attrs.get("dicomTCPReceiveBufferSize"), 0));
        conn.setTcpNoDelay(toBoolean(attrs.get("dicomTCPNoDelay"), Boolean.TRUE));
        conn.setTlsNeedClientAuth(toBoolean(attrs.get("dicomTLSNeedClientAuth"), Boolean.TRUE));
        conn.setTlsProtocols(toStrings(attrs.get("dicomTLSProtocol")));
    }

    @Override
    protected void load(ApplicationEntity ae, Attributes attrs) throws NamingException {
        super.load(ae, attrs);
        ae.setSendPDULength(toInt(attrs.get("dicomSendPDULength"),
                AAssociateRQAC.DEF_MAX_PDU_LENGTH));
        ae.setReceivePDULength(toInt(attrs.get("dicomReceivePDULength"),
                AAssociateRQAC.DEF_MAX_PDU_LENGTH));
        ae.setMaxOpsPerformed(toInt(attrs.get("dicomMaxOpsPerformed"), 1));
        ae.setMaxOpsInvoked(toInt(attrs.get("dicomMaxOpsInvoked"), 1));
        ae.setPackPDV(toBoolean(attrs.get("dicomPackPDV"), Boolean.TRUE));
    }

    @Override
    protected void diffsOf(Collection<ModificationItem> mods, Connection a, Connection b) {
        super.diffsOf(mods, a, b);
        diffOf(mods, "dicomBlacklistedHostname",
                a.getBlacklist(),
                b.getBlacklist());
        diffOf(mods, "dicomTCPBacklog",
                a.getBacklog(),
                b.getBacklog(),
                50);
        diffOf(mods, "dicomTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                0);
        diffOf(mods, "dicomAssociationRequestTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                0);
        diffOf(mods, "dicomAssociationAcknowledgeTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                0);
        diffOf(mods, "dicomAssociationReleaseTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                0);
        diffOf(mods, "dicomDIMSEResponseTimeout",
                a.getDimseRSPTimeout(),
                b.getDimseRSPTimeout(),
                0);
        diffOf(mods, "dicomCGetResponseTimeout",
                a.getCGetRSPTimeout(),
                b.getCGetRSPTimeout(),
                0);
        diffOf(mods, "dicomCMoveResponseTimeout",
                a.getCMoveRSPTimeout(),
                b.getCMoveRSPTimeout(),
                0);
        diffOf(mods, "dicomAssociationIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                0);
        diffOf(mods, "dicomTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                50);
        diffOf(mods, "dicomTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                0);
        diffOf(mods, "dicomTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                0);
        diffOf(mods, "dicomTCPNoDelay",
                a.isTcpNoDelay(),
                b.isTcpNoDelay());
        diffOf(mods, "dicomTLSProtocol",
                a.getTlsProtocols(),
                b.getTlsProtocols());
        diffOf(mods, "dicomTLSNeedClientAuth",
                a.isTlsNeedClientAuth(),
                b.isTlsNeedClientAuth());
    }

    @Override
    protected void diffsOf(Collection<ModificationItem> mods,
            ApplicationEntity a, ApplicationEntity b, String deviceDN) {
        super.diffsOf(mods, a, b, deviceDN);
        diffOf(mods, "dicomSendPDULength",
                a.getSendPDULength(),
                b.getSendPDULength(),
                AAssociateRQAC.DEF_MAX_PDU_LENGTH);
        diffOf(mods, "dicomReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                AAssociateRQAC.DEF_MAX_PDU_LENGTH);
        diffOf(mods, "dicomMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                1);
        diffOf(mods, "dicomMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                1);
        diffOf(mods, "dicomPackPDV",
                a.isPackPDV(),
                b.isPackPDV());
    }

}
