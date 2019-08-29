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

package org.dcm4che3.data;

import java.io.Serializable;

import org.dcm4che3.data.Tag;
import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Issuer implements Serializable {

    private static final long serialVersionUID = 5350502680059507981L;

    private String localNamespaceEntityID;
    private String universalEntityID;
    private String universalEntityIDType;

    public Issuer(String localNamespaceEntityID, String universalEntityID,
            String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
        validate();
    }

    public Issuer(String s) {
        this(s, '&');
    }

    public Issuer(String s, char delim) {
        String[] ss = StringUtils.split(s, delim);
        if (ss.length > 3)
            throw new IllegalArgumentException(s);
        this.localNamespaceEntityID = emptyToNull(ss[0]);
        this.universalEntityID = ss.length > 1 ? emptyToNull(ss[1]) : null;
        this.universalEntityIDType = ss.length > 2 ? emptyToNull(ss[2]) : null;
        validate();
    }

    public Issuer(String issuerOfPatientID, Attributes qualifiers) {
        this(issuerOfPatientID,
             qualifiers != null ? qualifiers.getString(Tag.UniversalEntityID) : null,
             qualifiers != null ? qualifiers.getString(Tag.UniversalEntityIDType) : null);
    }

    public Issuer(Attributes issuerItem) {
        this(issuerItem.getString(Tag.LocalNamespaceEntityID),
             issuerItem.getString(Tag.UniversalEntityID),
             issuerItem.getString(Tag.UniversalEntityIDType));
    }

    public Issuer(Issuer other) {
        this(other.getLocalNamespaceEntityID(),
             other.getUniversalEntityID(),
             other.getUniversalEntityIDType());
    }

    protected Issuer() {} // needed for JPA

    public static Issuer fromIssuerOfPatientID(Attributes attrs) {
        String issuerOfPatientID = attrs.getString(Tag.IssuerOfPatientID);
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        if (qualifiers != null) {
            String universalEntityID = qualifiers.getString(Tag.UniversalEntityID);
            String universalEntityIDType = qualifiers.getString(Tag.UniversalEntityIDType);
            if (universalEntityID != null && universalEntityIDType != null)
                return new Issuer(issuerOfPatientID, universalEntityID, universalEntityIDType);
        }
        return (issuerOfPatientID != null)
                ? new Issuer(issuerOfPatientID, null, null)
                : null;
    }

    public static Issuer valueOf(Attributes issuerItem) {
        if (issuerItem == null)
            return null;

        String localNamespaceEntityID = issuerItem.getString(Tag.LocalNamespaceEntityID);
        String universalEntityID = issuerItem.getString(Tag.UniversalEntityID);
        String universalEntityIDType = issuerItem.getString(Tag.UniversalEntityIDType);

        return (universalEntityID != null && universalEntityIDType != null)
                ? new Issuer(localNamespaceEntityID, universalEntityID, universalEntityIDType)
                : localNamespaceEntityID != null
                ? new Issuer(localNamespaceEntityID, null, null)
                : null;
    }

    private void validate() {
        if (localNamespaceEntityID == null && universalEntityID == null)
            throw new IllegalArgumentException(
                    "Missing Local Namespace Entity ID or Universal Entity ID");
        if (universalEntityID != null) {
            if (universalEntityIDType == null)
                throw new IllegalArgumentException("Missing Universal Entity ID Type");
        }
    }

    private String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
    }

    public final String getLocalNamespaceEntityID() {
        return localNamespaceEntityID;
    }

    public final String getUniversalEntityID() {
        return universalEntityID;
    }

    public final String getUniversalEntityIDType() {
        return universalEntityIDType;
    }

    public boolean merge(Issuer other) {
        if (!matches(other))
            throw new IllegalArgumentException("other=" + other);

        boolean mergeLocalNamespace;
        boolean mergeUniversal;
        if (mergeLocalNamespace = this.localNamespaceEntityID == null
                && other.localNamespaceEntityID != null) {
            this.localNamespaceEntityID = other.localNamespaceEntityID;
         }
        if (mergeUniversal = this.universalEntityID == null
                && other.universalEntityID != null) {
            this.universalEntityID = other.universalEntityID;
            this.universalEntityIDType = other.universalEntityIDType;
        }
        return mergeLocalNamespace || mergeUniversal;
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

    public boolean matches(Issuer other) {
        if (this == other || other == null)
            return true;

        boolean matchLocal = localNamespaceEntityID != null
                && other.localNamespaceEntityID != null;
        boolean matchUniversal = universalEntityID != null
                && other.universalEntityID != null;

        return (matchLocal || matchUniversal)
            && (!matchLocal
                || localNamespaceEntityID.equals(other.localNamespaceEntityID))
            && (!matchUniversal
                || universalEntityID.equals(other.universalEntityID)
                && universalEntityIDType.equals(other.universalEntityIDType));
    }

    @Override
    public String toString() {
        return toString('&');
    }

    public String toString(char delim) {
        if (universalEntityID == null)
            return localNamespaceEntityID;
        StringBuilder sb = new StringBuilder();
        if (localNamespaceEntityID != null)
            sb.append(localNamespaceEntityID);
        sb.append(delim);
        sb.append(universalEntityID);
        sb.append(delim);
        sb.append(universalEntityIDType);
        return sb.toString();
    }

    public Attributes toItem() {
        int size = 0;
        if (localNamespaceEntityID != null)
            size++;
        if (universalEntityID != null)
            size++;
        if (universalEntityIDType != null)
            size++;

        Attributes item = new Attributes(size);
        if (localNamespaceEntityID != null)
            item.setString(Tag.LocalNamespaceEntityID, VR.UT, localNamespaceEntityID);
        if (universalEntityID != null)
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
        if (universalEntityIDType != null)
            item.setString(Tag.UniversalEntityIDType, VR.CS, universalEntityIDType);
        return item ;
    }

    public Attributes toIssuerOfPatientID(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(2);
        if (localNamespaceEntityID != null)
            attrs.setString(Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
        if (universalEntityID != null) {
            Attributes item = new Attributes(2);
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.CS, universalEntityIDType);
            attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
        }
        return attrs;
    }

    public boolean isLesserQualifiedThan(Issuer other) {
        return other.universalEntityID != null && (universalEntityID == null
                || other.localNamespaceEntityID != null && localNamespaceEntityID == null);
    }
}
