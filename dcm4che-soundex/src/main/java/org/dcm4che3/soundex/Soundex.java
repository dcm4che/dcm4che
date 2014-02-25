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
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Soundex implements FuzzyStr {

    protected static final java.lang.String MAP_6 =
        // A BCD  E FG  H   I JKLMN  O PQRST  U V  W X  Y Z
        "\000123\00012\001\00022455\00012623\0001\0012\0002";
    protected static final java.lang.String MAP_9 = 
        // A BCD  E FG  H   I JKLMN  O PQRST  U V  W X  Y Z
        "\000136\00024\001\00043788\00015936\0002\0015\0005";

    private final boolean encodeFirst;
    private final int codeLength;
    private final int padLength;
    private final char[] map;

    public Soundex() {
        this(false, 4, 4, MAP_6);
    }

    public Soundex(boolean encodeFirst, int codeLength, int padLength, String map) {
        this.encodeFirst = encodeFirst;
        this.codeLength = codeLength;
        this.padLength = padLength;
        this.map = map.toCharArray();
    }

    @Override
    public String toFuzzy(String s) {
        if (s == null || s.length() == 0)
            return "";

        char[] in = s.toCharArray();
        char[] out = in.length < padLength ? new char[padLength] : in;
        int i = 0;
        int j = 0;
        char prevout = 0;
        if (!encodeFirst) {
            while (!Character.isLetter(in[i]))
                if (++i >= in.length)
                    return "";
            prevout = map(out[j++] = Character.toUpperCase(in[i++]));
        }

        char curout = 0;
        for (; i < in.length && j < codeLength; i++) {
            curout = map(in[i]);
            switch (curout) {
            case '\0':
                prevout = curout;
            case '\1':
                break;
            default:
                if (curout != prevout)
                    out[j++] = prevout = curout;
            }
        }
        while (j < padLength)
            out[j++] = '0';
        return new String(out, 0, j);
    }

    private char map(char c) {
        try {
            return map[c >= 'a' ? c - 'a' : c - 'A'];
        } catch (IndexOutOfBoundsException e) {
            return (c == 'ß' || c == 'Ç' || c == 'ç') ? map['c' - 'a'] : '\u0000';
        }
    }

    public static void main(String[] args) {
        Soundex inst = new Soundex();
        for (String arg : args)
            System.out.println(inst.toFuzzy(arg));
    }

}
