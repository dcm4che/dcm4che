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

package org.dcm4che3.net.pdu;

import java.nio.charset.StandardCharsets;

import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class UserIdentityRQ {

    public static final int USERNAME = 1;
    public static final int USERNAME_PASSCODE = 2;
    public static final int KERBEROS = 3;
    public static final int SAML = 4;
    public static final int JWT = 5;

    private static final String[] TYPES = {
        "0",
        "1 - Username",
        "2 - Username and passcode",
        "3 - Kerberos Service ticket",
        "4 - SAML Assertion",
        "5 - JSON Web Token (JWT)"
    };

    private final int type;
    private final boolean rspReq;
    private final byte[] primaryField;
    private final byte[] secondaryField;

    public UserIdentityRQ(int type, boolean rspReq, byte[] primaryField,
            byte[] secondaryField) {
        this.type = type;
        this.rspReq = rspReq;
        this.primaryField = primaryField.clone();
        this.secondaryField = secondaryField != null 
                ? secondaryField.clone()
                : new byte[0];
    }

    public UserIdentityRQ(int type, boolean rspReq, byte[] primaryField) {
        this(type, rspReq, primaryField, null);
    }

    /**
     * @deprecated use {@link #usernamePasscode}
     */
    @Deprecated
    public UserIdentityRQ(String username, char[] passcode) {
        this(USERNAME_PASSCODE, true, toBytes(username),
                toBytes(new String(passcode)));
    }

    /**
     * @deprecated use {@link #username}
     */
    @Deprecated
    public UserIdentityRQ(String username, boolean rspReq) {
        this(USERNAME, rspReq, toBytes(username));
    }

    public static UserIdentityRQ username(String username, boolean rspReq) {
        return new UserIdentityRQ(USERNAME, rspReq, toBytes(username));
    }

    public static UserIdentityRQ usernamePasscode(String username, char[] passcode, boolean rspReq) {
        return new UserIdentityRQ(USERNAME_PASSCODE, rspReq, toBytes(username), toBytes(new String(passcode)));
    }

    public static UserIdentityRQ kerberos(byte[] ticket, boolean rspReq) {
        return new UserIdentityRQ(KERBEROS, rspReq, ticket);
    }

    public static UserIdentityRQ saml(String assertion, boolean rspReq) {
        return new UserIdentityRQ(SAML, rspReq, toBytes(assertion));
    }

    public static UserIdentityRQ jwt(String token, boolean rspReq) {
        return new UserIdentityRQ(JWT, rspReq, toBytes(token));
    }

    private static String typeAsString(int type) {
        try {
            return TYPES[type];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(type);
        }
    }

    public final int getType() {
        return type;
    }

    public final boolean isPositiveResponseRequested() {
        return rspReq;
    }

    public final byte[] getPrimaryField() {
        return primaryField.clone();
    }

    public final byte[] getSecondaryField() {
        return secondaryField.clone();
    }

    public final String getUsername() {
        return toString(primaryField);
    }

    public final char[] getPasscode() {
        return toString(secondaryField).toCharArray();
    }

    private static byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String toString(byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
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
            .append(typeAsString(type))
            .append(StringUtils.LINE_SEPARATOR);
        if (type == USERNAME
                || type == USERNAME_PASSCODE)
            sb.append("    username: ")
              .append(getUsername());
        else
            sb.append("    primaryField: byte[")
              .append(primaryField.length)
              .append(']');
        if (type == USERNAME_PASSCODE) {
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
        return sb.append(StringUtils.LINE_SEPARATOR)
                 .append("  ]");
    }

}
