package org.dcm4che.util;

import java.util.Arrays;

public class PersonName {

    public static int FAMILY_NAME = 0;
    public static int GIVEN_NAME = 1;
    public static int MIDDLE_NAME = 2;
    public static int NAME_PREFIX = 3;
    public static int NAME_SUFFIX = 4;

    public static int ALPHABETIC = 0;
    public static int IDEOGRAPHIC = 1;
    public static int PHONETIC = 2;

    private final String[][] fields = new String[3][5]; 
    
    public PersonName() {
    }

    public PersonName(String s) {
        int reprIndex = 0;
        int reprEnd = -1;
        int reprBegin;
        while ((reprEnd = s.indexOf('=', reprBegin = reprEnd+1)) >= 0)
            initRepresentation(reprIndex++, s.substring(reprBegin, reprEnd));
        initRepresentation(reprIndex, s.substring(reprBegin));
    }

    public static PersonName valueOf(String s) {
        return s != null ? new PersonName(s) : null;
    }

    private void initRepresentation(int reprIndex, String s) {
        int compIndex = 0;
        int compEnd = -1;
        int compBegin;
        while ((compEnd = s.indexOf('^', compBegin = compEnd+1)) >= 0)
            set(reprIndex, compIndex++, s.substring(compBegin, compEnd));
        set(reprIndex, compIndex, s.substring(compBegin));
    }

    public void set(int compIndex, String s) {
        set(ALPHABETIC, compIndex, s);
    }

    public void set(int reprIndex, int compIndex, String s) {
        fields[reprIndex][compIndex] = (s != null && !s.isEmpty()) ? s : null;
    }

    public String get(int compIndex) {
        return get(ALPHABETIC, compIndex);
    }

    public String get(int reprIndex, int compIndex) {
        return fields[reprIndex][compIndex];
    }

    public String toString(int reprIndex) {
        int numComp = 0;
        int len = 0;
        for (int compIndex = 0; compIndex < 5; compIndex++) {
            String s = get(reprIndex, compIndex);
            if (s != null) {
                len += s.length();
                numComp = compIndex + 1;
            }
        }

        if (numComp == 0)
            return "";

        len += numComp-1;
        char[] str = new char[len];
        int pos = 0;
        for (int compIndex = 0; compIndex < numComp; compIndex++) {
            if (compIndex > 0)
                str[pos++] = '^';
            String s = get(reprIndex, compIndex);
            if (s != null) {
                int l = s.length();
                if (l > 0) {
                    s.getChars(0, l, str, pos);
                    pos += l;
                }
            }
        }
        return new String(str);
    }
    
    public String toString() {
        int numRepr = 0;
        int[] numComps = new int[3];
        int len = 0;
        for (int reprIndex = 0; reprIndex < 3; reprIndex++) {
            for (int compIndex = 0; compIndex < 5; compIndex++) {
                String s = get(reprIndex, compIndex);
                if (s != null) {
                    len += s.length();
                    numComps[reprIndex] = compIndex + 1;
                    numRepr = reprIndex + 1;
                }
            }
        }

        if (numRepr == 0)
            return "";

        for (int reprIndex = 0; reprIndex < numRepr; reprIndex++)
            len += Math.max(numComps[reprIndex]-1, 0);
        len += numRepr-1;
        char[] str = new char[len];
        int pos = 0;
        for (int reprIndex = 0; reprIndex < numRepr; reprIndex++) {
            if (reprIndex > 0)
                str[pos++] = '=';
            for (int compIndex = 0; compIndex < numComps[reprIndex]; compIndex++) {
                if (compIndex > 0)
                    str[pos++] = '^';
                String s = get(reprIndex, compIndex);
                if (s != null) {
                    int l = s.length();
                    if (l > 0) {
                        s.getChars(0, l, str, pos);
                        pos += l;
                    }
                }
            }
        }
        return new String(str);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fields);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PersonName other = (PersonName) obj;
        if (!Arrays.equals(fields, other.fields))
            return false;
        return true;
    }
}
