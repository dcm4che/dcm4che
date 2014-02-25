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
 * See listed authors below.
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

package org.dcm4che3.soundex;

/**
 * Implementation of the PHONEM substitutions as described in Georg Wilde and
 * Carsten Meyer, Doppelgaenger gesucht - Ein Programm fuer kontextsensitive
 * phonetische Textumwandlung ct Magazin fuer Computer & Technik 25/1998.
 * 
 * @see <a href="http://www.uni-koeln.de/phil-fak/phonetik/Lehre/MA-Arbeiten/magister_wilz.pdf"
 *      >Martin Wilz: Aspekte der Kodierung phonetischer Ähnlichkeiten in
 *      deutschen Eigennamen</a>
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Phonem implements FuzzyStr {

    @Override
    public String toFuzzy(String s) {
        if (s == null || s.length() == 0)
            return "";

        char[] in = s.toUpperCase().toCharArray();
        char next = in[0];
        int j = 0;
        for (int i = 1; i < in.length; i++) {
            char prev = next;
            switch ((prev << 8) + (next = in[i])) {
            case 0x5343:        // SC
            case 0x535a:        // SZ
            case 0x435a:        // CZ
            case 0x5453:        // TS
                next = 'C';
                break;
            case 0x4b53:        // KS
                next = 'X';
                break;
            case 0x5046:        // PF
            case 0x5048:        // PH
                next = 'V';
                break;
            case 0x5545:        // UE
                next = 'Y';
                break;
            case 0x4145:        // AE
                prev = 'E';
                break;
            case 0x4f45:        // OE
                next = 'Ö';
                break;
            case 0x4f55:        // OU
                next = '§';
                break;
            case 0x5155:        // QU
                in[j++] = 'K';
                next = 'W';
                break;
            case 0x4549:        // EI
            case 0x4559:        // EY
                in[j++] = 'A';
                next = 'Y';
                break;
            case 0x4555:        // EU
                in[j++] = 'O';
                next = 'Y';
                break;
            case 0x4155:        // AU
                in[j++] = 'A';
                next = '§';
                break;
            default:
                in[j++] = prev;
                break;
            }
        }
        in[j++] = next;
        int k = 0;
        char prev = 0;
        for (int i = 0; i < j; i++) {
            char ch = in[i];
            switch (ch) {
            case 'Z':
            case 'K':
            case 'G':
            case 'Q':
            case 'Ç':
                ch = 'C';
                break;
            case 'À':
            case 'Á':
            case 'Â':
            case 'Ã':
            case 'Å':
                ch = 'A';
                break;
            case 'Ä':
            case 'Æ':
            case 'È':
            case 'É':
            case 'Ê':
            case 'Ë':
                ch = 'E';
                break;
            case 'I':
            case 'J':
            case 'Ì':
            case 'Í':
            case 'Î':
            case 'Ï':
            case 'Ü':
            case 'Ý':
                ch = 'Y';
                break;
            case 'Ñ':
                ch = 'N';
                break;
            case 'Ò':
            case 'Ó':
            case 'Ô':
            case 'Õ':
                ch = 'O';
                break;
            case 'Ø':
                ch = 'Ö';
                break;
            case 'ß':
                ch = 'S';
                break;
            case 'F':
            case 'W':
                ch = 'V';
                break;
            case 'P':
                ch = 'B';
                break;
            case 'T':
                ch = 'D';
                break;
            case '§':
            case 'Ù':
            case 'Ú':
            case 'Û':
                ch = 'U';
                break;
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'R':
            case 'S':
            case 'U':
            case 'V':
            case 'X':
            case 'Y':
            case 'Ö':
                break;
            default:
                continue;
            }
            if (ch != prev)
                in[k++] = prev = ch;
        }
        return new String(in, 0, k);
    }

    public static void main(String[] args) {
        Phonem inst = new Phonem();
        for (String arg : args)
            System.out.println(inst.toFuzzy(arg));
    }
}
