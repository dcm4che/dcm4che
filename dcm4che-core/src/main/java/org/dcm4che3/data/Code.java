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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

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
    private transient final Key key = new Key();

    public Code(String codeValue, String codingSchemeDesignator,
            String codingSchemeVersion, String codeMeaning) {
        if (codeValue == null)
            throw new NullPointerException("Missing Code Value");
        if (isURN(codeValue)) {
            if (codingSchemeDesignator != null || codingSchemeVersion != null)
                throw new IllegalArgumentException("URN Code Value with Coding Scheme Designator");
        } else {
            if (codingSchemeDesignator == null)
                throw new NullPointerException("Missing Coding Scheme Designator");
        }
        if (codeMeaning == null)
            throw new NullPointerException("Missing Code Meaning");
        this.codeValue = codeValue;
        this.codingSchemeDesignator = codingSchemeDesignator;
        this.codingSchemeVersion = nullifyDCM01(codingSchemeDesignator, codingSchemeVersion);
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
        this.codeValue = trimsubstring(s, 1, endVal, false);
        if (isURN(codeValue)) {
            trimsubstring(s, endVal+1, endScheme, true);
        } else {
            this.codingSchemeDesignator = trimsubstring(s, endVal+1, endScheme, false);
            if (codingSchemeDesignator.endsWith("]")) {
                int endVersion = s.lastIndexOf(']', endScheme - 1);
                endScheme = s.lastIndexOf('[', endVersion - 1);
                this.codingSchemeDesignator = trimsubstring(s, endVal+1, endScheme, false);
                this.codingSchemeVersion = nullifyDCM01(codingSchemeDesignator,
                        trimsubstring(s, endScheme+1, endVersion, false));
            }
        }
        this.codeMeaning = trimsubstring(s, startMeaning, len-2, false);
    }

    private static String nullifyDCM01(String codingSchemeDesignator, String codingSchemeVersion) {
        return "01".equals(codingSchemeVersion) && "DCM".equals(codingSchemeDesignator)
                ? null
                : codingSchemeVersion;
    }

    private static String trimsubstring(String s, int start, int end, boolean empty) {
        try {
            String trim = s.substring(start, end).trim();
            if (trim.isEmpty() == empty)
                return trim;
        } catch (StringIndexOutOfBoundsException e) {}
        throw new IllegalArgumentException(s);
    }

    public Code(Attributes item) {
        this(codeValueOf(item),
             item.getString(Tag.CodingSchemeDesignator, null),
             item.getString(Tag.CodingSchemeVersion, null),
             item.getString(Tag.CodeMeaning, NO_CODE_MEANING));
    }

    protected Code() {} // needed for JPA

    public final String getCodeValue() {
        return codeValue;
    }

    public final String getCodingSchemeDesignator() {
        return codingSchemeDesignator;
    }

    public final String getCodingSchemeVersion() {
        return codingSchemeVersion;
    }

    public final String getCodeMeaning() {
        return codeMeaning;
    }

    @Override
    public int hashCode() {
        return codeValue.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Code))
            return false;
        Code other = (Code) o;
        return equalsIgnoreMeaning(other)
                && codeMeaning.equals(other.codeMeaning);
    }

    public boolean equalsIgnoreMeaning(Code other) {
        if (other == this)
            return true;
        return codeValue.equals(other.codeValue)
                && Objects.equals(codingSchemeDesignator, other.codingSchemeDesignator)
                && Objects.equals(codingSchemeVersion, other.codingSchemeVersion);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(codeValue).append(", ");
        if (codingSchemeDesignator != null) {
            sb.append(codingSchemeDesignator);
            if (codingSchemeVersion != null)
                sb.append(" [").append(codingSchemeVersion).append(']');
        }
        sb.append(", \"").append(codeMeaning).append("\")");
        return sb.toString();
    }

    public Attributes toItem() {
        Attributes codeItem = new Attributes(codingSchemeVersion != null ? 4 : 3);
        if (codingSchemeDesignator == null) {
            codeItem.setString(Tag.URNCodeValue, VR.UR, codeValue);
        } else {
            if (codeValue.length() > 16) {
                codeItem.setString(Tag.LongCodeValue, VR.UC, codeValue);
            } else {
                codeItem.setString(Tag.CodeValue, VR.SH, codeValue);
            }
            codeItem.setString(Tag.CodingSchemeDesignator, VR.SH, codingSchemeDesignator);
            if (codingSchemeVersion != null) {
                codeItem.setString(Tag.CodingSchemeVersion, VR.SH, codingSchemeVersion);
            }
        }
        codeItem.setString(Tag.CodeMeaning, VR.LO, codeMeaning);
        return codeItem ;
    }

    private static String codeValueOf(Attributes item) {
        String codeValue;
        return (codeValue = item.getString(Tag.CodeValue)) != null ? codeValue
                : (codeValue = item.getString(Tag.LongCodeValue)) != null ? codeValue
                : item.getString(Tag.URNCodeValue);
    }

    private static boolean isURN(String codeValue) {
        if (codeValue.indexOf(':') > 0)
            try {
                if (!codeValue.startsWith("urn:"))
                    new URL(codeValue);
                return true;
            } catch (MalformedURLException e) {}
        return false;
    }

    public final Key key() {
        return key;
    }

    public final class Key {
        private Key() {}

        @Override
        public int hashCode() {
            return codeValue.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Key))
                return false;

            Key other = (Key) o;
            return equalsIgnoreMeaning(other.outer());
        }

        private Code outer() {
            return Code.this;
        }
    }
}
