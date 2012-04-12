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
 * Portions created by the Initial Developer are Copyright (C) 2012
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

import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Issuer {

    private String localNamespaceEntityID;
    private String universalEntityID;
    private String universalEntityIDType;

    public Issuer() {}

    public Issuer(String localNamespaceEntityID, String universalEntityID,
            String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
    }

    public Issuer(String s) {
        String[] ss = StringUtils.split(s, '^');
        switch (ss.length) {
        case 3:
            this.universalEntityIDType = emptyToNull(ss[2]);
        case 2:
            this.universalEntityID = emptyToNull(ss[1]);
        case 1:
            this.localNamespaceEntityID = emptyToNull(ss[0]);
            return;
        }
        throw new IllegalArgumentException(s);
    }

    public static Issuer valueOf(String s) {
        return (s == null) ? null : new Issuer(s);
    }

    private String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
    }

    public final String getLocalNamespaceEntityID() {
        return localNamespaceEntityID;
    }

    public final void setLocalNamespaceEntityID(String localNamespaceEntityID) {
        this.localNamespaceEntityID = notEmpty(localNamespaceEntityID);
    }

    public final String getUniversalEntityID() {
        return universalEntityID;
    }

    public final void setUniversalEntityID(String universalEntityID) {
        this.universalEntityID = notEmpty(universalEntityID);
    }

    public final String getUniversalEntityIDType() {
        return universalEntityIDType;
    }

    public final void setUniversalEntityIDType(String universalEntityIDType) {
        this.universalEntityIDType = notEmpty(universalEntityIDType);
    }

    private String notEmpty(String s) {
        if ("".equals(s))
            throw new IllegalArgumentException("cannot be empty");
        return s;
    }

    @Override
    public int hashCode() {
        return 37 * (
                37 * hashCode(localNamespaceEntityID)
                   + hashCode(universalEntityID))
                + hashCode(universalEntityIDType);
    }

    private int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Issuer))
            return false;
        Issuer other = (Issuer) o;
        return equals(localNamespaceEntityID, other.localNamespaceEntityID)
                && equals(universalEntityID, other.universalEntityID)
                && equals(universalEntityIDType, other.universalEntityIDType);
    }

    private boolean equals(String s1, String s2) {
        return s1 == s2 || s1 != null && s1.equals(s2);
    }

    @Override
    public String toString() {
        return (universalEntityID == null && universalEntityIDType == null)
            ? nullToEmpty(localNamespaceEntityID)
            : nullToEmpty(localNamespaceEntityID) + '^'
            + nullToEmpty(universalEntityID)  + '^'
            + nullToEmpty(universalEntityIDType);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
