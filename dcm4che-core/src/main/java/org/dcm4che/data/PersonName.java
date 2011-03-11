package org.dcm4che.data;

import java.util.StringTokenizer;

public class PersonName {

    public static enum Component {
        FamilyName, GivenName, MiddleName, NamePrefix, NameSuffix
    };

    public static enum Group {
        Alphabetic, Ideographic, Phonetic
    };

    private final String[] fields = new String[15];
    
    public PersonName() {}

    public PersonName(String s) {
        if (s != null)
            parse(s);
    }

    private void parse(String s) {
        int gindex = 0;
        int cindex = 0;
        StringTokenizer stk = new StringTokenizer(s, "^=", true);
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            switch (tk.charAt(0)) {
            case '=':
                if (++gindex > 2)
                    throw new IllegalArgumentException(s);
                cindex = 0;
                break;
            case '^':
                if (++cindex > 4)
                    throw new IllegalArgumentException(s);
                break;
            default:
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
