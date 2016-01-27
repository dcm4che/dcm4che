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

package org.dcm4che3.data;

import java.io.Serializable;

import org.dcm4che3.data.Tag;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Code implements Serializable {

    private static final String NO_CODE_MEANING = "<none>";

    private static final long serialVersionUID = 8807594793107889446L;

    private String codeValue;
    private String codingSchemeDesignator;
    private String codingSchemeVersion;
    private String codeMeaning;

    public Code(String codeValue, String codingSchemeDesignator,
            String codingSchemeVersion, String codeMeaning) {
        if (codeValue == null)
            throw new NullPointerException("Missing Code Value");
        if (codingSchemeDesignator == null)
            throw new NullPointerException("Missing Coding Scheme Designator");
        if (codeMeaning == null)
            throw new NullPointerException("Missing Code Meaning");
        this.codeValue = codeValue;
        this.codingSchemeDesignator = codingSchemeDesignator;
        this.codingSchemeVersion = codingSchemeVersion;
        this.codeMeaning = codeMeaning;
    }

    public Code(String s) {
        int len = s.length();
        if (len < 9 
                || s.charAt(0) != '('
                || s.charAt(len-2) != '"'
                || s.charAt(len-1) != ')')
            throw new IllegalArgumentException(s);
        
        int endVal = s.indexOf(',');
        int endScheme = s.indexOf(',', endVal + 1);
        int startMeaning = s.indexOf('"', endScheme + 1) + 1;
        this.codeValue = trimsubstring(s, 1, endVal);
        this.codingSchemeDesignator = trimsubstring(s, endVal+1, endScheme);
        this.codeMeaning = trimsubstring(s, startMeaning, len-2);
        if (codingSchemeDesignator.endsWith("]")) {
            int endVersion = s.lastIndexOf(']', endScheme - 1);
            endScheme = s.lastIndexOf('[', endVersion - 1);
            this.codingSchemeDesignator = trimsubstring(s, endVal+1, endScheme);
            this.codingSchemeVersion = trimsubstring(s, endScheme+1, endVersion);
        }
    }

    private String trimsubstring(String s, int start, int end) {
        try {
            String trim = s.substring(start, end).trim();
            if (!trim.isEmpty())
                return trim;
        } catch (StringIndexOutOfBoundsException e) {}
        throw new IllegalArgumentException(s);
    }

    public Code(Attributes item) {
        this(item.getString(Tag.CodeValue, null),
             item.getString(Tag.CodingSchemeDesignator, null),
             item.getString(Tag.CodingSchemeVersion, null),
             item.getString(Tag.CodeMeaning, NO_CODE_MEANING));
    }

    protected Code() {} // needed for JPA

    public String getCodeValue() {
        return codeValue;
    }

    public String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }

    public String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }

    public final String getCodeMeaning() {
        return codeMeaning;
    }

    @Override
    public int hashCode() {
        return 37 * (37 * (37 * 
            codeValue.hashCode() +
            codeMeaning.hashCode()) +
            codingSchemeDesignator.hashCode()) + 
            hashCode(codingSchemeVersion);
    }

    private int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return equals(o, false);
    }

    public boolean equalsIgnoreMeaning(Code o) {
        return equals(o, true);
    }

    private boolean equals(Object o, boolean ignoreMeaning) {
        if (o == this)
            return true;
        if (!(o instanceof Code))
            return false;
        Code other = (Code) o;
        return codeValue.equals(other.codeValue)
                && codingSchemeDesignator.equals(other.codingSchemeDesignator)
                && equals(codingSchemeVersion, other.codingSchemeVersion)
                && (ignoreMeaning || codeMeaning.equals(other.codeMeaning));
    }

    private boolean equals(String s1, String s2) {
        return s1 == s2 || s1 != null && s1.equals(s2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(codeValue).append(", ").append(codingSchemeDesignator);
        if (codingSchemeVersion != null)
            sb.append(" [").append(codingSchemeVersion).append(']');
        sb.append(", \"").append(codeMeaning).append("\")");
        return sb.toString();
    }

    public Attributes toItem() {
        Attributes codeItem = new Attributes(codingSchemeVersion != null ? 4 : 3);
        codeItem.setString(Tag.CodeValue, VR.SH, codeValue);
        codeItem.setString(Tag.CodingSchemeDesignator, VR.SH, codingSchemeDesignator);
        if (codingSchemeVersion != null)
            codeItem.setString(Tag.CodingSchemeVersion, VR.SH, codingSchemeVersion);
        codeItem.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        return codeItem ;
    }

}
