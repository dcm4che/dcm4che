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

package org.dcm4che.hl7;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class HL7Utils {

    private static final AtomicInteger nextMessageControlID =
            new AtomicInteger(new Random().nextInt());

    public static int nextMessageControlID() {
        return nextMessageControlID.getAndIncrement() & 0x7FFFFFFF;
    }

    public static String dateTime(Date date) {
         return new SimpleDateFormat("yyyyMMddHHmmss.SSS").format(date);
    }

    public static String[] msh(byte[] msg) {
        int delim = msg[3];
        int i = 1;
        int len = 0;
        while (len < msg.length && msg[len] != '\r') {
            if (msg[len] == delim)
                i++;
            len++;
        }
        String s = new String(msg, 0, len);
        String[] ss = new String[i];
        int begin, end = s.length();
        while (--i >= 0) {
            begin = s.lastIndexOf(delim, end-1);
            ss[i] = s.substring(begin+1, end);
            end = begin;
        }
        return ss;
    }

    public static String makeACK(String[] msh, Ack ackCode, String text,
            char fieldDelim) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(msh[0]);
        sb.append(fieldDelim);
        sb.append(msh[1]);
        sb.append(fieldDelim);
        sb.append(msh[4]);
        sb.append(fieldDelim);
        sb.append(msh[5]);
        sb.append(fieldDelim);
        sb.append(msh[2]);
        sb.append(fieldDelim);
        sb.append(msh[3]);
        sb.append(fieldDelim);
        sb.append(dateTime(new Date()));
        sb.append(fieldDelim);
        sb.append(fieldDelim);
        sb.append("ACK");
        sb.append(fieldDelim);
        sb.append(nextMessageControlID());
        for (int i = 10; i < msh.length; i++) {
            sb.append(fieldDelim);
            sb.append(msh[i]);
        }
        sb.append('\r');
        sb.append("MSA");
        sb.append(fieldDelim);
        sb.append(ackCode.name());
        sb.append(fieldDelim);
        sb.append(msh[9]);
        sb.append(fieldDelim);
        if (text != null)
            sb.append(text.length() > 80 ? text.substring(0,80) : text);
        sb.append('\r');
        return sb.toString();
    }

    public static String charsetName(String[] msh, String defCode) {
        String code = msh.length > 17 ? msh[17] : null;
        return charsetName(code != null && !code.isEmpty() ? code : defCode);
    }

    public static String charsetName(String code) {
        if (code != null && !code.isEmpty())
            switch (code.charAt(code.length()-1)) {
            case '0':
                if (code.equals("GB 18030-2000"))
                    return "GB18030";
                break;
            case '1':
                if (code.equals("8859/1"))
                    return "ISO-8859-1";
                if (code.equals("KS X 1001"))
                    return "EUC-KR";
                break;
            case '2':
                if (code.equals("8859/2"))
                    return "ISO-8859-2";
                break;
            case '3':
                if (code.equals("8859/3"))
                    return "ISO-8859-3";
                break;
            case '4':
                if (code.equals("8859/4"))
                    return "ISO-8859-4";
                if (code.equals("ISO IR14"))
                    return "JIS_X0201";
                break;
            case '5':
                if (code.equals("8859/5"))
                    return "ISO-8859-5";
                break;
            case '6':
                if (code.equals("8859/6"))
                    return "ISO-8859-6";
                break;
            case '7':
                if (code.equals("8859/7"))
                    return "ISO-8859-7";
                if (code.equals("ISO IR87"))
                    return "x-JIS0208";
                break;
            case '8':
                if (code.equals("8859/8"))
                    return "ISO-8859-8";
                if (code.equals("UNICODE UTF-8"))
                    return "UTF-8";
                break;
            case '9':
                if (code.equals("8859/9"))
                    return "ISO-8859-9";
                if (code.equals("ISO IR159"))
                    return "JIS_X0212-1990";
                break;
            case 'E':
                if (code.equals("UNICODE"))
                    return "UTF-8";
                break;
            }
        return "US-ASCII";
    }
}
