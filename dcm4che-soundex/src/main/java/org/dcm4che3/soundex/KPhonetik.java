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
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class KPhonetik implements FuzzyStr {

    @Override
    public String toFuzzy(String s) {
        if (s == null || s.length() == 0)
            return "";

        char[] in = s.toUpperCase().toCharArray();
        int countX = 0;
        for (char c : in)
            if (c == 'X')
                countX++;
        char[] out = countX > 0 ? new char[in.length + countX] : in;
        int i = 0;
        int j = 0;
        char prevout = 0;
        char curout = 0;
        char prev = 0;
        char cur = 0;
        char next = in[0];
        
        for (; i < in.length; i++) {
            prev = cur;
            cur = next;
            next = i+1 < in.length ? in[i+1] : 0;
            switch (cur) {
            case 'A':
            case 'E':
            case 'I':
            case 'J':
            case 'O':
            case 'U':
            case 'Y':
            case 'Ä':
            case 'Ö':
            case 'Ü':
                if (j > 0) {
                    prevout = '0';
                    continue;
                }
                curout = '0';
                break;
            case 'B':
                curout = '1';
                break;
            case 'P':
                curout = next == 'H' ? '3' : '1';
                break;
            case 'D':
            case 'T':
                curout = (next == 'C' || next == 'S' || next == 'Z') 
                        ? '8' : '2';
                break;
            case 'F':
            case 'V':
            case 'W':
                curout = '3';
                break;
            case 'G':
            case 'K':
            case 'Q':
                curout = '4';
                break;
            case 'C':
                switch (next) {
                case 'A':
                case 'H':
                case 'K':
                case 'O':
                case 'Q':
                case 'U':
                case 'X':
                    curout = i == 0 || (prev != 'S' && prev != 'Z')
                            ? '4' : '8';
                    break;
                case 'L':
                case 'R':
                    curout = i == 0 ? '4' : '8';
                    break;
                }
                break;
            case 'X':
                if (prev != 'C' && prev != 'K' && prev != 'Q'
                        && prevout != '4')
                    out[j++] = prevout = '4';
                curout = '8';
                break;
            case 'L':
                curout = '5';
                break;
            case 'M':
            case 'N':
                curout = '6';
                break;
            case 'R':
                curout = '7';
                break;
            case 'S':
            case 'Z':
            case 'ß':
                curout = '8';
                break;
            default:
                prevout = 0;
                continue;
            }
            if (prevout != curout)
                out[j++] = prevout = curout;
        }
        return new String(out, 0, j);
    }

    public static void main(String[] args) {
        KPhonetik inst = new KPhonetik();
        for (String arg : args)
            System.out.println(inst.toFuzzy(arg));
    }

}
