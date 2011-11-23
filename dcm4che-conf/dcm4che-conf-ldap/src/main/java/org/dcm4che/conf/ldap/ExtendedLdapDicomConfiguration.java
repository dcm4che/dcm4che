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
    protected Attributes attrsOf(Connection conn) {
        Attributes attrs = super.attrsOf(conn);
        addNotEmpty(attrs, "dicomBlacklistedHostname", conn.getBlacklist());
        addNotDef(attrs, "dicomTCPBacklog",
                conn.getBacklog(), Connection.DEF_BACKLOG);
        addNotDef(attrs, "dicomTCPConnectTimeout",
                conn.getConnectTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomAssociationRequestTimeout",
                conn.getRequestTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomAssociationAcknowledgeTimeout",
                conn.getAcceptTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomAssociationReleaseTimeout",
                conn.getReleaseTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomDIMSEResponseTimeout",
                conn.getDimseRSPTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomCGetResponseTimeout",
                conn.getCGetRSPTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomCMoveResponseTimeout",
                conn.getCMoveRSPTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomAssociationIdleTimeout",
                conn.getIdleTimeout(), Connection.NO_TIMEOUT);
        addNotDef(attrs, "dicomTCPCloseDelay",
                conn.getSocketCloseDelay(), Connection.DEF_SOCKETDELAY);
        addNotDef(attrs, "dicomTCPSendBufferSize",
                conn.getSendBufferSize(), Connection.DEF_BUFFERSIZE);
        addNotDef(attrs, "dicomTCPReceiveBufferSize",
                conn.getReceiveBufferSize(), Connection.DEF_BUFFERSIZE);
        addBoolean(attrs, "dicomTCPNoDelay", conn.isTcpNoDelay());
        addNotEmpty(attrs, "dicomTLSProtocol", conn.getTlsProtocols());
        addBoolean(attrs, "dicomTLSNeedClientAuth", conn.isTlsNeedClientAuth());
        return attrs;
    }

    @Override
    protected Attributes attrsOf(ApplicationEntity ae, String deviceDN) {
        Attributes attrs = super.attrsOf(ae, deviceDN);
        addNotDef(attrs, "dicomSendPDULength",
                ae.getSendPDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        addNotDef(attrs, "dicomReceivePDULength",
                ae.getReceivePDULength(), ApplicationEntity.DEF_MAX_PDU_LENGTH);
        addNotDef(attrs, "dicomMaxOpsPerformed",
                ae.getMaxOpsPerformed(), ApplicationEntity.SYNCHRONOUS_MODE);
        addNotDef(attrs, "dicomMaxOpsInvoked",
                ae.getMaxOpsInvoked(), ApplicationEntity.SYNCHRONOUS_MODE);
        addBoolean(attrs, "dicomPackPDV", ae.isPackPDV());
        return attrs;
    }


    @Override
    protected void loadFrom(Connection conn, Attributes attrs) throws NamingException {
        super.loadFrom(conn, attrs);
        conn.setBlacklist(toStrings(attrs.get("dicomBlacklistedHostname")));
        conn.setBacklog(toInt(attrs.get("dicomTCPBacklog"), Connection.DEF_BACKLOG));
        conn.setConnectTimeout(toInt(attrs.get("dicomTCPConnectTimeout"),
                Connection.NO_TIMEOUT));
        conn.setRequestTimeout(toInt(attrs.get("dicomAssociationRequestTimeout"),
                Connection.NO_TIMEOUT));
        conn.setAcceptTimeout(toInt(attrs.get("dicomAssociationAcknowledgeTimeout"),
                Connection.NO_TIMEOUT));
        conn.setReleaseTimeout(toInt(attrs.get("dicomAssociationReleaseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setDimseRSPTimeout(toInt(attrs.get("dicomDIMSEResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setCGetRSPTimeout(toInt(attrs.get("dicomCGetResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setCMoveRSPTimeout(toInt(attrs.get("dicomCMoveResponseTimeout"),
                Connection.NO_TIMEOUT));
        conn.setIdleTimeout(toInt(attrs.get("dicomAssociationIdleTimeout"),
                Connection.NO_TIMEOUT));
        conn.setSocketCloseDelay(toInt(attrs.get("dicomTCPCloseDelay"),
                Connection.DEF_SOCKETDELAY));
        conn.setSendBufferSize(toInt(attrs.get("dicomTCPSendBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setReceiveBufferSize(toInt(attrs.get("dicomTCPReceiveBufferSize"),
                Connection.DEF_BUFFERSIZE));
        conn.setTcpNoDelay(toBoolean(attrs.get("dicomTCPNoDelay"), Boolean.TRUE));
        conn.setTlsNeedClientAuth(toBoolean(attrs.get("dicomTLSNeedClientAuth"), Boolean.TRUE));
        conn.setTlsProtocols(toStrings(attrs.get("dicomTLSProtocol")));
    }

    @Override
    protected void loadFrom(ApplicationEntity ae, Attributes attrs) throws NamingException {
        super.loadFrom(ae, attrs);
        ae.setSendPDULength(toInt(attrs.get("dicomSendPDULength"),
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setReceivePDULength(toInt(attrs.get("dicomReceivePDULength"),
                ApplicationEntity.DEF_MAX_PDU_LENGTH));
        ae.setMaxOpsPerformed(toInt(attrs.get("dicomMaxOpsPerformed"),
                ApplicationEntity.SYNCHRONOUS_MODE));
        ae.setMaxOpsInvoked(toInt(attrs.get("dicomMaxOpsInvoked"),
                ApplicationEntity.SYNCHRONOUS_MODE));
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
                Connection.DEF_BACKLOG);
        diffOf(mods, "dicomTCPConnectTimeout",
                a.getConnectTimeout(),
                b.getConnectTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomAssociationRequestTimeout",
                a.getRequestTimeout(),
                b.getRequestTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomAssociationAcknowledgeTimeout",
                a.getAcceptTimeout(),
                b.getAcceptTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomAssociationReleaseTimeout",
                a.getReleaseTimeout(),
                b.getReleaseTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomDIMSEResponseTimeout",
                a.getDimseRSPTimeout(),
                b.getDimseRSPTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomCGetResponseTimeout",
                a.getCGetRSPTimeout(),
                b.getCGetRSPTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomCMoveResponseTimeout",
                a.getCMoveRSPTimeout(),
                b.getCMoveRSPTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomAssociationIdleTimeout",
                a.getIdleTimeout(),
                b.getIdleTimeout(),
                Connection.NO_TIMEOUT);
        diffOf(mods, "dicomTCPCloseDelay",
                a.getSocketCloseDelay(),
                b.getSocketCloseDelay(),
                Connection.DEF_SOCKETDELAY);
        diffOf(mods, "dicomTCPSendBufferSize",
                a.getSendBufferSize(),
                b.getSendBufferSize(),
                Connection.DEF_BUFFERSIZE);
        diffOf(mods, "dicomTCPReceiveBufferSize",
                a.getReceiveBufferSize(),
                b.getReceiveBufferSize(),
                Connection.DEF_BUFFERSIZE);
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
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        diffOf(mods, "dicomReceivePDULength",
                a.getReceivePDULength(),
                b.getReceivePDULength(),
                ApplicationEntity.DEF_MAX_PDU_LENGTH);
        diffOf(mods, "dicomMaxOpsPerformed",
                a.getMaxOpsPerformed(),
                b.getMaxOpsPerformed(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        diffOf(mods, "dicomMaxOpsInvoked",
                a.getMaxOpsInvoked(),
                b.getMaxOpsInvoked(),
                ApplicationEntity.SYNCHRONOUS_MODE);
        diffOf(mods, "dicomPackPDV",
                a.isPackPDV(),
                b.isPackPDV());
    }

}
