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

package org.dcm4che3.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class IDWithIssuer {

    public static final IDWithIssuer[] EMPTY = {};

    private final String id;
    private String typeOfPatientID;
    private String identifierTypeCode;
    private Issuer issuer;

    public IDWithIssuer(String id, Issuer issuer) {
        if (id.isEmpty())
            throw new IllegalArgumentException("empty id");
        this.id = id;
        this.setIssuer(issuer);
    }

    public IDWithIssuer(String id, String issuer) {
        this.id = id;
        this.setIssuer(issuer != null ? new Issuer(issuer, '&') : null);
    }

    public IDWithIssuer(String cx) {
        String[] ss = StringUtils.split(cx, '^');
        this.id = ss[0];
        if (ss.length > 3) {
            if (!ss[3].isEmpty())
                this.setIssuer(new Issuer(ss[3], '&'));
            if (ss.length > 4 && !ss[4].isEmpty())
                this.setIdentifierTypeCode(ss[4]);
        }
    }

    public IDWithIssuer withoutIssuer() {
        return issuer == null ? this : new IDWithIssuer(id, (Issuer) null);
    }

    public final String getID() {
        return id;
    }

    public String getTypeOfPatientID() {
        return typeOfPatientID;
    }

    public void setTypeOfPatientID(String typeOfPatientID) {
        this.typeOfPatientID = typeOfPatientID;
    }

    public final String getIdentifierTypeCode() {
        return identifierTypeCode;
    }

    public final void setIdentifierTypeCode(String identifierTypeCode) {
        this.identifierTypeCode = identifierTypeCode;
    }

    public final Issuer getIssuer() {
        return issuer;
    }

    public final void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    @Override
    public String toString() {
        if (issuer == null && identifierTypeCode == null)
            return id;
        
        StringBuilder sb = new StringBuilder(id);
        sb.append("^^^");
        if (issuer != null)
            sb.append(issuer.toString('&'));
        if (identifierTypeCode != null)
            sb.append('^').append(identifierTypeCode);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        if (typeOfPatientID != null)
            result += typeOfPatientID.hashCode() * 31;
        if (identifierTypeCode != null)
            result += identifierTypeCode.hashCode() * 31;
        if (issuer != null)
            result += issuer.hashCode() * 31;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IDWithIssuer))
            return false;
        IDWithIssuer other = (IDWithIssuer) obj;
        return id.equals(other.id) &&
                (typeOfPatientID == null
                    ? other.typeOfPatientID == null
                    : typeOfPatientID.equals(typeOfPatientID)) &&
                (identifierTypeCode == null
                    ? other.identifierTypeCode == null
                    : identifierTypeCode.equals(identifierTypeCode)) &&
                (issuer == null
                    ? other.issuer == null
                    : issuer.equals(other.issuer));
    }

    public boolean matches(IDWithIssuer other) {
        return id.equals(other.id) &&
                (issuer == null 
                    ? other.issuer == null
                    : issuer.matches(other.issuer));
    }

    public Attributes exportPatientIDWithIssuer(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(3);

        attrs.setString(Tag.PatientID, VR.LO, id);
        if (typeOfPatientID != null) {
            attrs.setString(Tag.TypeOfPatientID, VR.CS, typeOfPatientID);
        }
        if (issuer == null && identifierTypeCode == null) {
            return attrs;
        }

        if (issuer != null)
            issuer.toIssuerOfPatientID(attrs);

        if (identifierTypeCode != null) {
            Attributes item = attrs.getNestedDataset(
                    Tag.IssuerOfPatientIDQualifiersSequence);
            if (item == null) {
                item = new Attributes(1);
                attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1)
                    .add(item);
            }
            item.setString(Tag.IdentifierTypeCode, VR.CS, identifierTypeCode);
        }
        return attrs;
    }

    public static IDWithIssuer valueOf(Attributes attrs, int idTag,
            int issuerSeqTag) {
        String id = attrs.getString(idTag);
        if (id == null)
            return null;

        return new IDWithIssuer(id,
                Issuer.valueOf(attrs.getNestedDataset(issuerSeqTag)));
    }

    public static IDWithIssuer pidOf(Attributes attrs) {
        String id = attrs.getString(Tag.PatientID);
        if (id == null)
            return null;

        IDWithIssuer result = 
                new IDWithIssuer(id, Issuer.fromIssuerOfPatientID(attrs));
        result.setTypeOfPatientID(attrs.getString(Tag.TypeOfPatientID));
        result.setIdentifierTypeCode(identifierTypeCodeOf(attrs));
        return result;
    }

    private static String identifierTypeCodeOf(Attributes attrs) {
        Attributes qualifiers = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        return qualifiers != null
                ? qualifiers.getString(Tag.IdentifierTypeCode)
                : null;
    }

    public static Set<IDWithIssuer> pidsOf(Attributes attrs) {
        IDWithIssuer pid = IDWithIssuer.pidOf(attrs);
        Sequence opidseq = attrs.getSequence(Tag.OtherPatientIDsSequence);
        if (opidseq == null)
            if (pid == null)
                return Collections.emptySet();
            else
                return Collections.singleton(pid);
        
        Set<IDWithIssuer> pids =
                new HashSet<IDWithIssuer>((1 + opidseq.size()) << 1);
        if (pid != null)
            pids.add(pid);
        for (Attributes item : opidseq)
            addTo(IDWithIssuer.pidOf(item), pids);
        return pids;
    }

    private static void addTo(IDWithIssuer pid, Set<IDWithIssuer> pids) {
        if (pid == null)
            return;

        for (Iterator<IDWithIssuer> itr = pids.iterator(); itr.hasNext();) {
            IDWithIssuer next = itr.next();
            if (next.matches(pid)) {
                // replace existing matching pid if it is lesser qualified
                if (pid.issuer != null && (next.issuer == null
                        || next.issuer.isLesserQualifiedThan(pid.issuer)))
                    itr.remove();
                else
                    return;
            }
        }
        pids.add(pid);
    }
}
