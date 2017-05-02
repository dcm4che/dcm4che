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

package org.dcm4che3.hl7;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public abstract class HL7Charset {

    public static String toCharsetName(String code) {
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
                if (code.equals("CNS 11643-1992"))
                    return "TIS-620";
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

    public static String toDicomCharacterSetCode(String code) {
        if (code != null && !code.isEmpty())
            switch (code.charAt(code.length()-1)) {
            case '0':
                if (code.equals("GB 18030-2000"))
                    return "GB18030";
                break;
            case '1':
                if (code.equals("8859/1"))
                    return "ISO_IR 100";
                if (code.equals("KS X 1001"))
                    return "ISO 2022 IR 149";
                break;
            case '2':
                if (code.equals("8859/2"))
                    return "ISO_IR 101";
                if (code.equals("CNS 11643-1992"))
                    return "ISO_IR 166";
                break;
            case '3':
                if (code.equals("8859/3"))
                    return "ISO_IR 109";
                break;
            case '4':
                if (code.equals("8859/4"))
                    return "ISO_IR 110";
                if (code.equals("ISO IR14"))
                    return "ISO_IR 13";
                break;
            case '5':
                if (code.equals("8859/5"))
                    return "ISO_IR 144";
                break;
            case '6':
                if (code.equals("8859/6"))
                    return "ISO_IR 127";
                break;
            case '7':
                if (code.equals("8859/7"))
                    return "ISO_IR 126";
                if (code.equals("ISO IR87"))
                    return "ISO 2022 IR 87";
                break;
            case '8':
                if (code.equals("8859/8"))
                    return "ISO_IR 138";
                if (code.equals("UNICODE UTF-8"))
                    return "ISO_IR 192";
                break;
            case '9':
                if (code.equals("8859/9"))
                    return "ISO_IR 148";
                if (code.equals("ISO IR159"))
                    return "ISO 2022 IR 159";
                break;
            case 'E':
                if (code.equals("UNICODE"))
                    return "ISO_IR 192";
                break;
            }
        return null;
    }

}
