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

import org.dcm4che3.util.StringUtils;
import org.dcm4che3.util.UIDUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * 
 */
public class PresentationContext {

    public static final int ACCEPTANCE = 0;
    public static final int USER_REJECTION = 1;
    public static final int PROVIDER_REJECTION = 2;
    public static final int ABSTRACT_SYNTAX_NOT_SUPPORTED = 3;
    public static final int TRANSFER_SYNTAX_NOT_SUPPORTED = 4;

    private static final String[] RESULTS = {
        "0 - acceptance",
        "1 - user-rejection",
        "2 - no-reason (provider rejection)",
        "3 - abstract-syntax-not-supported (provider rejection)",
        "4 - transfer-syntaxes-not-supported (provider rejection)"
    };

    private final int pcid;
    private final int result;
    private final String as;
    private final String[] tss;

    public PresentationContext(int pcid, int result, String as, String... tss) {
        this.pcid = pcid;
        this.result = result;
        this.as = as;
        this.tss = tss;
    }

    public PresentationContext(int pcid, String as, String... tss) {
        this(pcid, 0, as, tss);
    }

    public PresentationContext(int pcid, int result, String ts) {
        this(pcid, result, null, ts);
    }

    private static String resultAsString(int result) {
        try {
            return RESULTS[result];
        } catch (IndexOutOfBoundsException e) {
            return Integer.toString(result);
        }
    }

    public final int getPCID() {
        return pcid;
    }

    public final int getResult() {
        return result;
    }

    public boolean isAccepted() {
        return result == ACCEPTANCE;
    }

   public final String getAbstractSyntax() {
        return as;
    }

    public final String[] getTransferSyntaxes() {
        return tss;
    }

    public boolean containsTransferSyntax(String ts) {
        for (String ts0 : tss)
            if (ts.equals(ts0))
                return true;
        return false;
    }

    public String getTransferSyntax() {
        return tss[0];
    }

    public int length() {
        int len = 4;
        if (as != null)
            len += 4 + as.length();
        for (String ts : tss)
            len += 4 + ts.length();
        return len;
    }

    @Override
    public String toString() {
        return promptTo(new StringBuilder()).toString();
    }

    StringBuilder promptTo(StringBuilder sb) {
        sb.append("  PresentationContext[id: ").append(pcid)
          .append(StringUtils.LINE_SEPARATOR);
        if (as != null)
            UIDUtils.promptTo(as, sb.append("    as: "));
        else
            sb.append("    result: ").append(resultAsString(result));
        sb.append(StringUtils.LINE_SEPARATOR);
        for (String ts : tss)
            UIDUtils.promptTo(ts, sb.append("    ts: "))
                .append(StringUtils.LINE_SEPARATOR);
        return sb.append("  ]");
    }

}
