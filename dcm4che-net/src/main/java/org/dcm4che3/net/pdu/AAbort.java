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

import java.io.IOException;

import org.dcm4che3.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class AAbort extends IOException {

    private static final long serialVersionUID = -1825815767517748111L;

    public static final int UL_SERIVE_USER = 0;
    public static final int UL_SERIVE_PROVIDER = 2;

    public static final int REASON_NOT_SPECIFIED = 0;
    public static final int UNRECOGNIZED_PDU = 1;
    public static final int UNEXPECTED_PDU = 2;
    public static final int UNRECOGNIZED_PDU_PARAMETER = 4;
    public static final int UNEXPECTED_PDU_PARAMETER = 5;
    public static final int INVALID_PDU_PARAMETER_VALUE = 6;

    private static final String[] SOURCES = {
        "0 - service-user",
        "1",
        "2 - service-provider",
    };

    private static final String[] SERVICE_USER_REASONS = {
        "0",
    };

    private static final String[] SERVICE_PROVIDER_REASONS = {
        "0 - reason-not-specified",
        "1 - unrecognized-PDU",
        "2 - unexpected-PDU",
        "3",
        "4 - unrecognized-PDU-parameter",
        "5 - unexpected-PDU-parameter",
        "6 - invalid-PDU-parameter-value"
    };

    private static final String[][] REASONS = {
        SERVICE_USER_REASONS,
        StringUtils.EMPTY_STRING,
        SERVICE_PROVIDER_REASONS
    };

    private final int source;
    private final int reason;

    public AAbort(int source, int reason) {
        super("A-ABORT[source: " + toString(SOURCES, source)
                  + ", reason: " + toReason(source, reason)
                  + ']');
        this.source = source;
        this.reason = reason;
    }

    public AAbort() {
        this(UL_SERIVE_USER, 0);
    }

    private static String toString(String[] ss, int i) {
        try {
            return ss[i];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(i);
        }
    }

    private static String toReason(int source, int reason) {
        try {
            return toString(REASONS[source], reason);
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(reason);
        }
    }

    public final int getReason() {
        return reason;
    }

    public final int getSource() {
        return source;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
