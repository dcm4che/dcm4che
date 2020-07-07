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

package org.dcm4che3.hl7;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class HL7Charset {

    private static final Map<String, String> CHARSET_NAMES_MAP = new HashMap<>();

    private HL7Charset() {}

    /**
     * Extend/override mapping of field MSH-18-character to named charset specified by
     * <a href="http://www.hl7.eu/HL7v2x/v251/hl7v251tab0211.htm">
     * "HL7 table 0211 - Alternate character sets</a>..
     *
     * For example, {@code HL7Charset.setCharsetNameMapping("Windows-1252", "windows-1252")} associate
     * proprietary field MSH-18-character value {@code Windows-1252} with Windows-1252 (CP-1252) charset,
     * containing characters Š/š and Ž/ž not included in ISO-8859-1 (Latin-1), but used in Estonian and
     * Finnish for transcribing foreign names.
     *
     * @param  code
     *         value field MSH-18-character
     * @param  charsetName
     *         The name of the mapped charset
     *
     * @throws IllegalCharsetNameException
     *          If the given {@code charsetName} is illegal
     *
     * @throws  IllegalArgumentException
     *          If the given {@code charsetName} is null
     *
     * @throws  UnsupportedCharsetException
     *          If no support for the named charset is available
     *          in this instance of the Java virtual machine
     */
    public static void setCharsetNameMapping(String code, String charsetName) {
        if (!Charset.isSupported(charsetName))
            throw new UnsupportedCharsetException(charsetName);
        CHARSET_NAMES_MAP.put(code, charsetName);
    }

    /**
     * Reset mapping of field MSH-18-character to named charsets as specified by
     * <a href="http://www.hl7.eu/HL7v2x/v251/hl7v251tab0211.htm">
     * "HL7 table 0211 - Alternate character sets</a>.
     *
     * <table>
     * <tr>
     * <th>MSH-18 Value</th>
     * <th>Charset Name</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr><td>{@code ASCII}</td>
     *     <td>{@code US-ASCII}</td></tr>
     * <tr><td>{@code 8859/1}</td>
     *     <td>{@code ISO-8859-1}</td></tr>
     * <tr><td>{@code 8859/2}</td>
     *     <td>{@code ISO-8859-2}</td></tr>
     * <tr><td>{@code 8859/3}</td>
     *     <td>{@code ISO-8859-3}</td></tr>
     * <tr><td>{@code 8859/4}</td>
     *     <td>{@code ISO-8859-4}</td></tr>
     * <tr><td>{@code 8859/5}</td>
     *     <td>{@code ISO-8859-5}</td></tr>
     * <tr><td>{@code 8859/6}</td>
     *     <td>{@code ISO-8859-6}</td></tr>
     * <tr><td>{@code 8859/7}</td>
     *     <td>{@code ISO-8859-7}</td></tr>
     * <tr><td>{@code 8859/8}</td>
     *     <td>{@code ISO-8859-8}</td></tr>
     * <tr><td>{@code I8859/9}</td>
     *     <td>{@code ISO-8859-9}</td></tr>
     * <tr><td>{@code ISO IR14}</td>
     *     <td>{@code JIS_X0201}</td></tr>
     * <tr><td>{@code ISO IR87}</td>
     *     <td>{@code x-JIS0208}</td></tr>
     * <tr><td>{@code ISO IR159}</td>
     *     <td>{@code JIS_X0212-1990}</td></tr>
     * <tr><td>{@code GB18030-2000}</td>
     *     <td>{@code GB18030}</td></tr>
     * <tr><td>{@code KS X 1001}</td>
     *     <td>{@code EUC-KR}</td></tr>
     * <tr><td>{@code CNS 11643-1992}</td>
     *     <td>{@code TIS-620}</td></tr>
     * <tr><td>{@code UNICODE}</td>
     *     <td>{@code UTF-8}</td></tr>
     * <tr><td>{@code UNICODE UTF-8"}</td>
     *     <td>{@code UTF-8}</td></tr>
     * </tbody>
     * </table>
     */
    public static void resetCharsetNameMappings() {
        CHARSET_NAMES_MAP.clear();
    }

    public static String toCharsetName(String code) {
        if (code == null) code = "";
        String value = CHARSET_NAMES_MAP.get(code);
        if (value != null) return value;
        switch (code) {
            case "8859/1":
                return "ISO-8859-1";
            case "8859/2":
                return "ISO-8859-2";
            case "8859/3":
                return "ISO-8859-3";
            case "8859/4":
                return "ISO-8859-4";
            case "8859/5":
                return "ISO-8859-5";
            case "8859/6":
                return "ISO-8859-6";
            case "8859/7":
                return "ISO-8859-7";
            case "8859/8":
                return "ISO-8859-8";
            case "8859/9":
                return "ISO-8859-9";
            case "ISO IR14":
                return "JIS_X0201";
            case "ISO IR87":
                return "x-JIS0208";
            case "ISO IR159":
                return "JIS_X0212-1990";
            case "GB 18030-2000":
                return "GB18030";
            case "KS X 1001":
                return "EUC-KR";
            case "CNS 11643-1992":
                return "TIS-620";
            case "UNICODE":
            case "UNICODE UTF-8":
                return "UTF-8";
        }
        return "US-ASCII";
    }

    public static String toDicomCharacterSetCode(String code) {
        if (code != null && !code.isEmpty())
            switch (code) {
                case "8859/1":
                    return "ISO_IR 100";
                case "8859/2":
                    return "ISO_IR 101";
                case "8859/3":
                    return "ISO_IR 109";
                case "8859/4":
                    return "ISO_IR 110";
                case "8859/5":
                    return "ISO_IR 144";
                case "8859/6":
                    return "ISO_IR 127";
                case "8859/7":
                    return "ISO_IR 126";
                case "8859/8":
                    return "ISO_IR 138";
                case "8859/9":
                    return "ISO_IR 148";
                case "ISO IR14":
                    return "ISO_IR 13";
                case "ISO IR87":
                    return "ISO 2022 IR 87";
                case "ISO IR159":
                    return "ISO 2022 IR 159";
                case "GB 18030-2000":
                    return "GB18030";
                case "KS X 1001":
                    return "ISO 2022 IR 149";
                case "CNS 11643-1992":
                    return "ISO_IR 166";
                case "UNICODE":
                case "UNICODE UTF-8":
                    return "ISO_IR 192";
            }
        return null;
    }

}
