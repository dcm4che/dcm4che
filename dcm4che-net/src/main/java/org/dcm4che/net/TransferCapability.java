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

/**
 * DICOM Standard, Part 15, Annex H: Transfer Capability � The description of
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
public class TransferCapability {

    public static enum Role { SCU, SCP }

    private String commonName;
    private String sopClass;
    private Role role;
    private String[] transferSyntax ;

    /**
     * Set the name of the Transfer Capability object. Can be a meaningful name
     * or any unique sequence of characters.
     * 
     * @return A String containing the common name.
     */
    public final String getCommonName() {
        return commonName;
    }

    /**
     * Get the name of the Transfer Capability object. Can be a meaningful name
     * or any unique sequence of characters.
     * 
     * @param commonName
     *                A String containing the common name.
     */
    public final void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * Get the role for this <code>TransferCapability</code>instance.
     * 
     * @return Role (SCU or SCP) for this <code>TransferCapability</code>instance
     */
    public final  Role getRole() {
        return role;
    }

    /**
     * Set the role for this <code>TransferCapability</code>instance.
     * 
     * @param role
     *                Role (SCU or SCP) for this <code>TransferCapability</code>instance
     */
    public final void setRole(Role role) {
        this.role = role;
    }

    /**
     * Get the SOP Class of this Transfer Capability object.
     * 
     * @return A String containing the SOP Class UID.
     */
    public final String getSopClass() {
        return sopClass;
    }

    /**
     * Set the SOP Class of this Transfer Capability object.
     * 
     * @param sopClass
     *                A String containing the SOP Class UID.
     */
    public final void setSopClass(String sopClass) {
        this.sopClass = sopClass;
    }

    /**
     * Get the transfer syntax(es) that may be requested as an SCU or that are
     * offered as an SCP.
     * 
     * @return String array containing the transfer syntaxes.
     */
    public final String[] getTransferSyntax() {
        return transferSyntax;
    }

    /**
     * Set the transfer syntax(es) that may be requested as an SCU or that are
     * offered as an SCP.
     * 
     * @param transferSyntax
     *                String array containing the transfer syntaxes.
     */
    public final void setTransferSyntax(String[] transferSyntax) {
       this.transferSyntax = transferSyntax;
    }
}
