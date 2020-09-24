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
 * Java(TM), hosted at https://github.com/dcm4che.
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

import java.io.Serializable;
import java.util.*;

import org.dcm4che3.data.UID;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

/**
 * DICOM Standard, Part 15, Annex H: Transfer Capability - The description of
 * the SOP classes and syntaxes supported by a Network AE.
 * <p>
 * An instance of the <code>TransferCapability</code> class describes the
 * DICOM transfer capabilities of an SCU or SCP in terms of a single
 * presentation syntax. This includes the role selection (SCU or SCP), the
 * acceptable transfer syntaxes for a given SOP Class, and any extra
 * information.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class TransferCapability implements Serializable {

    private static final long serialVersionUID = 6386251434418693778L;

    public enum Role { SCU, SCP }

    private ApplicationEntity ae;
    private String commonName;
    private String sopClass;
    private Role role;
    private String[] transferSyntaxes;
    private String[] prefTransferSyntaxes = {};
    private EnumSet<QueryOption> queryOptions;
    private StorageOptions storageOptions;

    public TransferCapability() {
        this(null, UID.Verification, Role.SCU, UID.ImplicitVRLittleEndian);
    }

    public TransferCapability(String commonName, String sopClass, Role role,
            String... transferSyntaxes) {
        setCommonName(commonName);
        setSopClass(sopClass);
        setRole(role);
        setTransferSyntaxes(transferSyntaxes);
    }

    public void setApplicationEntity(ApplicationEntity ae) {
        if (ae != null) {
            if (this.ae != null)
                throw new IllegalStateException("already owned by AE " + 
                        this.ae.getAETitle());
        }
        this.ae = ae;
    }

    /**
     * get the name of the Transfer Capability object. Can be a meaningful name
     * or any unique sequence of characters.
     * 
     * @return A String containing the common name.
     */
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Get the role for this <code>TransferCapability</code>instance.
     * 
     * @return Role (SCU or SCP) for this <code>TransferCapability</code>instance
     */
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        if (role == null)
            throw new NullPointerException();

        if (this.role == role)
            return;

        ApplicationEntity ae = this.ae;
        if (ae != null)
            ae.removeTransferCapabilityFor(sopClass, this.role);

        this.role = role;

        if (ae != null)
            ae.addTransferCapability(this);
    }

    /**
     * Get the SOP Class of this Transfer Capability object.
     * 
     * @return A String containing the SOP Class UID.
     */
    public String getSopClass() {
        return sopClass;
    }

    public void setSopClass(String sopClass) {
        if (sopClass.isEmpty())
            throw new IllegalArgumentException("empty sopClass");

        if (sopClass.equals(this.sopClass))
            return;

        ApplicationEntity ae = this.ae;
        if (ae != null)
            ae.removeTransferCapabilityFor(sopClass, this.role);

        this.sopClass = sopClass;

        if (ae != null)
            ae.addTransferCapability(this);
    }

    /**
     * Get the transfer syntax(es) that may be requested as an SCU or that are
     * offered as an SCP.
     * 
     * @return list of transfer syntaxes.
     */
    public String[] getTransferSyntaxes() {
        return transferSyntaxes;
    }

    public void setTransferSyntaxes(String... transferSyntaxes) {
        this.transferSyntaxes = StringUtils.requireContainsNoEmpty(
                StringUtils.requireNotEmpty(transferSyntaxes, "missing transferSyntax"),
                "empty transferSyntax");
    }

    public String[] getPreferredTransferSyntaxes() {
        return prefTransferSyntaxes;
    }

    public void setPreferredTransferSyntaxes(String... transferSyntaxes) {
        this.prefTransferSyntaxes =
                StringUtils.requireContainsNoEmpty(transferSyntaxes, "empty transferSyntax");
    }

    public boolean containsTransferSyntax(String ts) {
        return "*".equals(transferSyntaxes[0]) || StringUtils.contains(transferSyntaxes, ts);
    }

    public String selectTransferSyntax(String... transferSyntaxes) {
        if (transferSyntaxes.length == 1)
            return containsTransferSyntax(transferSyntaxes[0]) ? transferSyntaxes[0] : null;

        List<String> acceptable = retainAcceptable(transferSyntaxes);
        if (acceptable.isEmpty())
            return null;

        for (String prefTransferSyntax : prefTransferSyntaxes.length > 0
                ? prefTransferSyntaxes
                : ae.getPreferredTransferSyntaxes())
            if (acceptable.contains(prefTransferSyntax))
                return prefTransferSyntax;

        return acceptable.get(0);
    }

    private List<String> retainAcceptable(String[] transferSyntaxes) {
        List<String> acceptable = new ArrayList<>(transferSyntaxes.length);
        for (String transferSyntax : transferSyntaxes) {
            if (containsTransferSyntax(transferSyntax))
                acceptable.add(transferSyntax);
        }
        return acceptable;
    }

    public void setQueryOptions(EnumSet<QueryOption> queryOptions) {
        this.queryOptions = queryOptions;
    }

    public EnumSet<QueryOption> getQueryOptions() {
        return queryOptions;
    }

    public void setStorageOptions(StorageOptions storageOptions) {
        this.storageOptions = storageOptions;
    }

    public StorageOptions getStorageOptions() {
        return storageOptions;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(512), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, "TransferCapability[cn: ", commonName);
        StringUtils.appendLine(sb, indent2, "role: ", role);
        sb.append(indent2).append("as: ");
        UIDUtils.promptTo(sopClass, sb).append(StringUtils.LINE_SEPARATOR);
        for (String ts : transferSyntaxes) {
            sb.append(indent2).append("ts: ");
            UIDUtils.promptTo(ts, sb).append(StringUtils.LINE_SEPARATOR);
        }
        if (queryOptions != null)
            sb.append(indent2).append("QueryOptions").append(queryOptions)
                .append(StringUtils.LINE_SEPARATOR);
        if (storageOptions != null)
            sb.append(indent2).append(storageOptions)
                .append(StringUtils.LINE_SEPARATOR);
        return sb.append(indent).append(']');
    }

}
