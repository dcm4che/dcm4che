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

package org.dcm4che3.net.pdu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.dcm4che3.data.UID;
import org.dcm4che3.data.Implementation;
import org.dcm4che3.net.Connection;
import org.dcm4che3.util.IntHashMap;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class AAssociateRQAC {

    protected byte[] reservedBytes = new byte[32];
    protected int protocolVersion = 1;
    protected int maxPDULength = Connection.DEF_MAX_PDU_LENGTH;
    protected int maxOpsInvoked = Connection.SYNCHRONOUS_MODE;
    protected int maxOpsPerformed = Connection.SYNCHRONOUS_MODE;
    protected String calledAET;
    protected String callingAET;
    protected String applicationContext = UID.DICOMApplicationContextName;
    protected String implClassUID = Implementation.getClassUID();
    protected String implVersionName = Implementation.getVersionName();
    protected UserIdentityRQ userIdentityRQ;
    protected UserIdentityAC userIdentityAC;
    protected final ArrayList<PresentationContext>
            pcs = new ArrayList<PresentationContext>();
    protected final IntHashMap<PresentationContext>
            pcidMap = new IntHashMap<PresentationContext>();
    protected final LinkedHashMap<String, RoleSelection>
            roleSelMap = new LinkedHashMap<String, RoleSelection>();
    protected final LinkedHashMap<String, ExtendedNegotiation>
            extNegMap = new LinkedHashMap<String, ExtendedNegotiation>();
    protected final LinkedHashMap<String, CommonExtendedNegotiation>
            commonExtNegMap = new LinkedHashMap<String, CommonExtendedNegotiation>();

    public void checkCallingAET() {
        if (callingAET == null)
            throw new IllegalStateException("Calling AET not initalized");
    }

    public void checkCalledAET() {
        if (calledAET == null)
            throw new IllegalStateException("Called AET not initalized");
    }

    public final int getProtocolVersion() {
        return protocolVersion;
    }

    public final void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public final byte[] getReservedBytes() {
        return reservedBytes.clone();
    }

    public final void setReservedBytes(byte[] reservedBytes) {
        if (reservedBytes.length != 32)
            throw new IllegalArgumentException("reservedBytes.length: "
                    + reservedBytes.length);
        System.arraycopy(reservedBytes, 0, this.reservedBytes, 0, 32);
    }

    public final String getCalledAET() {
        return calledAET;
    }

    public final void setCalledAET(String calledAET) {
        if (calledAET.length() > 16)
            throw new IllegalArgumentException("calledAET: " + calledAET);
        this.calledAET = calledAET;
    }

    public final String getCallingAET() {
        return callingAET;
    }

    public final void setCallingAET(String callingAET) {
        if (callingAET.length() > 16)
            throw new IllegalArgumentException("callingAET: " + callingAET);
        this.callingAET = callingAET;
    }

    public final String getApplicationContext() {
        return applicationContext;
    }

    public final void setApplicationContext(String applicationContext) {
        if (applicationContext == null)
            throw new NullPointerException();

        this.applicationContext = applicationContext;
    }

    public final int getMaxPDULength() {
        return maxPDULength;
    }

    public final void setMaxPDULength(int maxPDULength) {
        this.maxPDULength = maxPDULength;
    }

    public final int getMaxOpsInvoked() {
        return maxOpsInvoked;
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        this.maxOpsInvoked = maxOpsInvoked;
    }

    public final int getMaxOpsPerformed() {
        return maxOpsPerformed;
    }

    public final void setMaxOpsPerformed(int maxOpsPerformed) {
        this.maxOpsPerformed = maxOpsPerformed;
    }

    public final boolean isAsyncOps() {
        return maxOpsInvoked != 1 || maxOpsPerformed != 1;
    }

    public final String getImplClassUID() {
        return implClassUID;
    }

    public final void setImplClassUID(String implClassUID) {
        if (implClassUID == null)
            throw new NullPointerException();

        this.implClassUID = implClassUID;
    }

    public final String getImplVersionName() {
        return implVersionName;
    }

    public final void setImplVersionName(String implVersionName) {
        this.implVersionName = implVersionName;
    }

    public final UserIdentityRQ getUserIdentityRQ() {
        return userIdentityRQ;
    }

    public void setUserIdentityRQ(UserIdentityRQ userIdentityRQ) {
        this.userIdentityRQ = userIdentityRQ;
    }

    public final UserIdentityAC getUserIdentityAC() {
        return userIdentityAC;
    }

    public void setUserIdentityAC(UserIdentityAC userIdentityAC) {
        this.userIdentityAC = userIdentityAC;
    }

    public List<PresentationContext> getPresentationContexts() {
        return Collections.unmodifiableList(pcs);
    }

    public int getNumberOfPresentationContexts() {
        return pcs.size();
    }

    public PresentationContext getPresentationContext(int pcid) {
        return pcidMap.get(pcid);
    }

    public void addPresentationContext(PresentationContext pc) {
        int pcid = pc.getPCID();
        if (pcidMap.containsKey(pcid))
            throw new IllegalStateException(
                    "Already contains Presentation Context with pid: "
                    + pcid);
        pcidMap.put(pcid, pc);
        pcs.add(pc);
    }

    public boolean removePresentationContext(PresentationContext pc) {
        if (!pcs.remove(pc))
            return false;

        pcidMap.remove(pc.getPCID());
        return true;
    }

    public Collection<RoleSelection> getRoleSelections() {
        return Collections.unmodifiableCollection(roleSelMap.values());
    }

    public RoleSelection getRoleSelectionFor(String cuid) {
        return roleSelMap.get(cuid);
    }

    public RoleSelection addRoleSelection(RoleSelection rs) {
        return roleSelMap.put(rs.getSOPClassUID(), rs);
    }

    public RoleSelection removeRoleSelectionFor(String cuid) {
        return roleSelMap.remove(cuid);
    }

    public Collection<ExtendedNegotiation> getExtendedNegotiations() {
        return Collections.unmodifiableCollection(extNegMap.values());
    }

    public ExtendedNegotiation getExtNegotiationFor(String cuid) {
        return extNegMap.get(cuid);
    }

    public ExtendedNegotiation addExtendedNegotiation(ExtendedNegotiation extNeg) {
        return extNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public ExtendedNegotiation removeExtendedNegotiationFor(String cuid) {
        return extNegMap.remove(cuid);
    }

    public Collection<CommonExtendedNegotiation> getCommonExtendedNegotiations() {
        return Collections.unmodifiableCollection(commonExtNegMap.values());
    }

    public CommonExtendedNegotiation getCommonExtendedNegotiationFor(String cuid) {
        return commonExtNegMap.get(cuid);
    }

    public CommonExtendedNegotiation addCommonExtendedNegotiation(
            CommonExtendedNegotiation extNeg) {
        return commonExtNegMap.put(extNeg.getSOPClassUID(), extNeg);
    }

    public CommonExtendedNegotiation removeCommonExtendedNegotiationFor(
            String cuid) {
        return commonExtNegMap.remove(cuid);
    }

    public int length() {
        int len = 68; // Fix AA-RQ/AC PDU fields
        len += 4 + applicationContext.length();
        for (PresentationContext pc : pcs) {
            len += 4 + pc.length();
        }
        len += 4 + userInfoLength();
        return len;
    }

    public int userInfoLength() {
        int len = 8; // Max Length Sub-Item
        len += 4 + implClassUID.length();
        if (isAsyncOps())
            len += 8; // Asynchronous Operations Window Sub-Item
        for (RoleSelection rs : roleSelMap.values()) {
            len += 4 + rs.length();
        }
        if (implVersionName != null)
            len += 4 + implVersionName.length();
        for (ExtendedNegotiation en : extNegMap.values()) {
            len += 4 + en.length();
        }
        for (CommonExtendedNegotiation cen : commonExtNegMap.values()) {
            len += 4 + cen.length();
        }
        if (userIdentityRQ != null)
            len += 4 + userIdentityRQ.length();
        if (userIdentityAC != null)
            len += 4 + userIdentityAC.length();
        return len;
    }

    protected StringBuilder promptTo(String header, StringBuilder sb) {
        sb.append(header)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  calledAET: ").append(calledAET)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  callingAET: ").append(callingAET)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  applicationContext: ");
        UIDUtils.promptTo(applicationContext, sb)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  implClassUID: ").append(implClassUID)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  implVersionName: ").append(implVersionName)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  maxPDULength: ").append(maxPDULength)
          .append(StringUtils.LINE_SEPARATOR)
          .append("  maxOpsInvoked/maxOpsPerformed: ")
          .append(maxOpsInvoked).append("/").append(maxOpsPerformed)
          .append(StringUtils.LINE_SEPARATOR);
        if (userIdentityRQ != null)
            userIdentityRQ.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        if (userIdentityAC != null)
            userIdentityAC.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        for (PresentationContext pc : pcs)
            pc.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        for (RoleSelection rs : roleSelMap.values())
            rs.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        for (ExtendedNegotiation extNeg : extNegMap.values())
            extNeg.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        for (CommonExtendedNegotiation extNeg : commonExtNegMap.values())
            extNeg.promptTo(sb).append(StringUtils.LINE_SEPARATOR);
        return sb.append("]");
    }
}
