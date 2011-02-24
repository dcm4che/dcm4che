package org.dcm4che.util;

public class StringUtils {

    public static String[] EMPTY_STRING = {};

    public static String join(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0)
            return null;
        
        if (n == 1) {
            String s = ss[0];
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss)
            len += s != null ? s.length() : 0;

        char[] cs = new char[len];
        for (int i = 0, off = 0; i < n; ++i) {
            if (i != 0)
                cs[off++] = delim;
            String s = ss[i];
            if (s != null) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static Object splitAndTrim(String s, char delim) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos+1)) >= 0)
            count++;

        if (count == 1)
            return substring(s, 0, s.length());

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2-1);
            ss[count] = substring(s, delimPos+1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    private static String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) <= ' ')
            beginIndex++;
        while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return beginIndex < endIndex ? s.substring(beginIndex, endIndex) : null;
    }

    public static String trimTrailing(String s) {
        int endIndex = s.length();
        while (endIndex > 0 && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return s.substring(0, endIndex);
    }

    public static int parseIS(String s) {
        return Integer.parseInt(s.charAt(0) == '+' ? s.substring(1) : s);
    }

    public static String formatDS(float f) {
        String s = Float.toString(f);
        int l = s.length();
        if (s.startsWith(".0", l-2))
            return s.substring(0, l-2);
        int e = s.indexOf('E', l-5);
        return e > 0 && s.startsWith(".0", e-2) ? cut(s, e-2, e)  : s;
    }

    public static String formatDS(double d) {
        String s = Double.toString(d);
        int l = s.length();
        if (s.startsWith(".0", l-2))
            return s.substring(0, l-2);
        int skip = l - 16;
        int e = s.indexOf('E', l-5);
        return e < 0 ? (skip > 0 ? s.substring(0, 16) : s)
                : s.startsWith(".0", e-2) ? cut(s, e-2, e)
                : skip > 0 ? cut(s, e-skip, e) : s;
    }

    private static String cut(String s, int begin, int end) {
        int l = s.length();
        char[] ch = new char[l-(end-begin)];
        s.getChars(0, begin, ch, 0);
        s.getChars(end, l, ch, begin);
        return new String(ch);
    }

}
