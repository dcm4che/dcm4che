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

package org.dcm4che.net.pdu;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class UserIdentityRQ {

    public static final int USERNAME = 1;
    public static final int USERNAME_PASSCODE = 2;
    public static final int KERBEROS = 3;
    public static final int SAML = 4;

    private static final String[] TYPES = {
        "0",
        "1 - Username",
        "2 - Username and passcode",
        "3 - Kerberos Service ticket",
        "4 - SAML Assertion"
    };

    private int userIdentityType;
    private boolean positiveResponseRequested;
    private byte[] primaryField = {};
    private byte[] secondaryField = {};

    private static String typeAsString(int type) {
        try {
            return TYPES[type];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(type);
        }
    }

    public final int getUserIdentityType() {
        return userIdentityType;
    }

    public final void setUserIdentityType(int userIdentityType) {
        this.userIdentityType = userIdentityType;
    }

    public final boolean isPositiveResponseRequested() {
        return positiveResponseRequested;
    }

    public final void setPositiveResponseRequested(
            boolean positiveResponseRequested) {
        this.positiveResponseRequested = positiveResponseRequested;
    }

    public final byte[] getPrimaryField() {
        return primaryField.clone();
    }

    public final void setPrimaryField(byte[] primaryField) {
        this.primaryField = primaryField.clone();
    }

    public final byte[] getSecondaryField() {
        return secondaryField.clone();
    }

    public final void setSecondaryField(byte[] secondaryField) {
        this.secondaryField = secondaryField.clone();
    }

    public String getUsername() {
        return toString(primaryField);
    }

    public void setUsername(String username) {
        primaryField = toBytes(username);
    }

    public char[] getPasscode() {
        return toChars(secondaryField);
    }

    public void setPasscode(char[] passcode) {
        secondaryField = toBytes(passcode);
    }

    private static byte[] toBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    private static String toString(byte[] b) {
        try {
            return new String(b, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    private static Charset utf8() {
        return Charset.forName("UTF-8");
    }
    
    private static byte[] toBytes(char[] ca) {
        return utf8().encode(CharBuffer.wrap(ca)).array();
    }

    private static char[] toChars(byte[] b) {
        return utf8().decode(ByteBuffer.wrap(b)).array();
    }

    public int length() {
        return 6 + primaryField.length + secondaryField.length;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  UserIdentity[")
            .append(StringUtils.LINE_SEPARATOR)
            .append("    type: ")
            .append(typeAsString(userIdentityType))
            .append(StringUtils.LINE_SEPARATOR);
        if (userIdentityType == USERNAME
                || userIdentityType == USERNAME_PASSCODE)
            sb.append("    username: ")
              .append(getUsername());
        else
            sb.append("    primaryField: byte[")
              .append(primaryField.length)
              .append(']');
        if (userIdentityType == USERNAME_PASSCODE) {
            sb.append(StringUtils.LINE_SEPARATOR)
              .append("    passcode: ");
            for (int i = secondaryField.length; --i >= 0;)
                sb.append('*');
        } else if (secondaryField.length > 0) {
            sb.append(StringUtils.LINE_SEPARATOR)
              .append("    secondaryField: byte[")
              .append(secondaryField.length)
              .append(']');
        }
        return sb.append(StringUtils.LINE_SEPARATOR).append("  ]");
    }
}
