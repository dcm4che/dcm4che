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

package org.dcm4che.data;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class PersonName {

    private static final Logger LOG = LoggerFactory.getLogger(PersonName.class);

    public static enum Component {
        FamilyName, GivenName, MiddleName, NamePrefix, NameSuffix
    };

    public static enum Group {
        Alphabetic, Ideographic, Phonetic
    };

    private final String[] fields = new String[15];
    
    public PersonName() {}

    public PersonName(String s) {
        this(s, false);
    }

    public PersonName(String s, boolean lenient) {
        if (s != null)
            parse(s, lenient);
    }

    private void parse(String s, boolean lenient) {
        int gindex = 0;
        int cindex = 0;
        StringTokenizer stk = new StringTokenizer(s, "^=", true);
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            switch (tk.charAt(0)) {
            case '=':
                if (++gindex > 2)
                    if (lenient) {
                        LOG.info(
                            "illegal PN: {} - truncate illegal component group(s)", s);
                        return;
                    } else
                        throw new IllegalArgumentException(s);
                cindex = 0;
                break;
            case '^':
                if (++cindex > 4)
                    if (lenient) {
                        if (cindex == 5)
                            LOG.info(
                                "illegal PN: {} - ignore illegal component(s)", s);
                        break;
                    } else
                        throw new IllegalArgumentException(s);
                break;
            default:
                if (cindex <= 4)
                    set(gindex, cindex, tk);
            }
        }
    }

    public String toString() {
        int totLen = 0;
        Group lastGroup = Group.Alphabetic;
        for (Group g : Group.values()) {
            Component lastCompOfGroup = Component.FamilyName;
            for (Component c : Component.values()) {
                String s = get(g, c);
                if (s != null) {
                    totLen += s.length();
                    lastGroup = g;
                    lastCompOfGroup = c;
                }
            }
            totLen += lastCompOfGroup.ordinal();
        }
        totLen += lastGroup.ordinal();
        char[] ch = new char[totLen];
        int wpos = 0;
        for (Group g : Group.values()) {
            Component lastCompOfGroup = Component.FamilyName;
            for (Component c : Component.values()) {
                String s = get(g, c);
                if (s != null) {
                    int d = c.ordinal() - lastCompOfGroup.ordinal();
                    while (d-- > 0)
                        ch[wpos++] = '^';
                    d = s.length();
                    s.getChars(0, d, ch, wpos);
                    wpos += d;
                    lastCompOfGroup = c;
                }
            }
            if (g == lastGroup)
                break;
            ch[wpos++] = '=';
        }
        return new String(ch); 
    }


    public String getNormalizedString(Group g, String defVal) {
        int totLen = 0;
        for (Component c : Component.values()) {
            String s = get(g, c);
            if (s != null)
                totLen += s.length();
        }
        if (totLen == 0)
            return defVal;

        char[] ch = new char[totLen+4];
        int wpos = 0;
        for (Component c : Component.values()) {
            String s = get(g, c);
            if (s != null) {
                int d = s.length();
                s.getChars(0, d, ch, wpos);
                wpos += d;
            }
            if (wpos < ch.length)
                ch[wpos++] = '^';
        }
        return new String(ch); 
    }

    public String getNormalizedQueryString(Group g) {
        if (isEmpty(g))
            return "*";

        int totLen = 0;
        for (Component c : Component.values()) {
            String s = get(g, c);
            totLen += s != null ? s.length() : 1;
        }

        char[] ch = new char[totLen+4];
        int wpos = 0;
        for (Component c : Component.values()) {
            String s = get(g, c);
            if (s != null) {
                int d = s.length();
                s.getChars(0, d, ch, wpos);
                wpos += d;
            } else {
                ch[wpos++] = '*';
            }
            if (wpos < ch.length)
                ch[wpos++] = '^';
        }
        return (totLen == 5
                && ch[0] == '*'
                && ch[2] == '*'
                && ch[4] == '*'
                && ch[6] == '*'
                && ch[8] == '*')
            ? "*"
            : new String(ch); 
    }

    public String get(Component c) {
        return get(Group.Alphabetic, c);
    }

    public String get(Group g, Component c) {
        return fields[g.ordinal() * 5 + c.ordinal()];
    }


    public void set(Component c, String s) {
        set(Group.Alphabetic, c, s);
    }

    public void set(Group g, Component c, String s) {
        set(g.ordinal(), c.ordinal(), s);
    }

    private void set(int gindex, int cindex, String s) {
        fields[gindex * 5 + cindex] = trim(s);
    }

    public boolean isEmpty() {
        for (Group g : Group.values())
            if (!isEmpty(g))
                return false;
        return true;
    }

    public boolean isEmpty(Group g) {
        for (Component c : Component.values())
            if (!isEmpty(g, c))
                return false;
        return true;
    }

    public boolean isEmpty(Group g, Component c) {
        return get(g, c) == null;
    }

    private static String trim(String s) {
        return s == null || (s = s.trim()).isEmpty() ? null : s;
    }
}
