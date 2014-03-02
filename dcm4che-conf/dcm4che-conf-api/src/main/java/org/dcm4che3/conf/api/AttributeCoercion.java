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

package org.dcm4che3.conf.api;

import java.io.Serializable;
import java.util.Arrays;

import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.TransferCapability.Role;
import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class AttributeCoercion
    implements Serializable, Comparable<AttributeCoercion> {


    private static final long serialVersionUID = 7799241531490684097L;

    private final String commonName;
    private final Condition condition;
    private final String uri;

    public AttributeCoercion(String commonName, String[] sopClasses,
            Dimse dimse, Role role, String[] aeTitles, String uri) {
        if (commonName == null)
            throw new NullPointerException("commonName");
        if (commonName.isEmpty())
            throw new IllegalArgumentException("commonName cannot be empty");

        this.commonName = commonName;
        this.condition = new Condition(
                StringUtils.maskNull(sopClasses),
                dimse,
                role,
                StringUtils.maskNull(aeTitles));
        this.uri = uri;
    }

    public final String getCommonName() {
        return commonName;
    }

    public final String[] getSOPClasses() {
        return condition.sopClasses;
    }

    public final Dimse getDIMSE() {
        return condition.dimse;
    }

    public final Role getRole() {
        return condition.role;
    }

    public final String[] getAETitles() {
        return condition.aeTitles;
    }

    public final String getURI() {
        return uri;
    }

    public boolean matchesCondition(String sopClass, Dimse dimse, Role role,
            String aeTitle) {
        return condition.matches(sopClass, dimse, role, aeTitle);
    }

    @Override
    public int compareTo(AttributeCoercion o) {
        return condition.compareTo(o.condition);
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder(64), "").toString();
    }

    public StringBuilder promptTo(StringBuilder sb, String indent) {
        String indent2 = indent + "  ";
        StringUtils.appendLine(sb, indent, 
                "AttributeCoercion[cn: ", commonName);
        StringUtils.appendLine(sb, indent2, "dimse: ", condition.dimse);
        StringUtils.appendLine(sb, indent2, "role: ", condition.role);
        promptCUIDsTo(sb, indent2, condition.sopClasses);
        promptAETsTo(sb, indent2, condition.aeTitles);
        StringUtils.appendLine(sb, indent2, "cuids: ",
                Arrays.toString(condition.sopClasses));
        StringUtils.appendLine(sb, indent2, "aets: ",
                Arrays.toString(condition.aeTitles));
        StringUtils.appendLine(sb, indent2, "uri: ", uri);
        return sb.append(indent).append(']');
    }

    private static void promptCUIDsTo(StringBuilder sb, String indent,
            String[] cuids) {
        if (cuids.length == 0)
            return;
        sb.append(indent).append("cuids: ");
        for (String cuid : cuids)
            UIDUtils.promptTo(cuid, sb).append(',');
        sb.setLength(sb.length()-1);
        sb.append(StringUtils.LINE_SEPARATOR);
    }

    private static void promptAETsTo(StringBuilder sb, String indent,
            String[] aets) {
        if (aets.length == 0)
            return;
        sb.append(indent).append("aets: ");
        for (String aet : aets)
            sb.append(aet).append(',');
        sb.setLength(sb.length()-1);
        sb.append(StringUtils.LINE_SEPARATOR);
    }

    private static class Condition
            implements Serializable, Comparable<Condition> {

        private static final long serialVersionUID = -8993828886666689060L;

        final String[] sopClasses;
        final Dimse dimse;
        final Role role;
        final String[] aeTitles;
        final int weight;

        public Condition(String[] sopClasses, Dimse dimse, Role role,
                String[] aeTitles) {
            if (dimse == null)
                throw new NullPointerException("dimse");
            if (role == null)
                throw new NullPointerException("role");

            this.sopClasses = sopClasses;
            this.dimse = dimse;
            this.role = role;
            this.aeTitles = aeTitles;
            this.weight = (aeTitles.length != 0 ? 2 : 0)
                      + (sopClasses.length != 0 ? 1 : 0);
        }

        @Override
        public int compareTo(Condition o) {
            return o.weight - weight;
        }

        public boolean matches(String sopClass, Dimse dimse, Role role,
                String aeTitle) {
            return this.dimse == dimse
                    && this.role == role
                    && isEmptyOrContains(this.aeTitles, aeTitle)
                    && isEmptyOrContains(this.sopClasses, sopClass);
        }

        private static boolean isEmptyOrContains(Object[] a, Object o) {
            if (o == null || a.length == 0)
                return true;

            for (int i = 0; i < a.length; i++)
                if (o.equals(a[i]))
                    return true;

            return false;
        }

    }

}
