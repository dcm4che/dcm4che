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

package org.dcm4che3.util;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class StringUtils {

    public static String LINE_SEPARATOR = AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty("line.separator");
                }
            }
         );

    public static String[] EMPTY_STRING = {};

    public static StringBuilder appendLine(StringBuilder sb, Object... ss) {
        for (Object s : ss)
            sb.append(s);
        return sb.append(LINE_SEPARATOR);
    }

    public static String concat(String[] ss, char delim) {
        int n = ss.length;
        if (n == 0)
            return "";
        
        if (n == 1) {
            String s = ss[0];
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss)
            if (s != null)
                len += s.length();

        char[] cs = new char[len];
        int off = 0;
        for (String s : ss) {
            if (off != 0)
                cs[off++] = delim;
            if (s != null) {
                int l = s.length();
                s.getChars(0, l, cs, off);
                off += l;
            }
        }
        return new String(cs);
    }

    public static String concat(Collection<String> ss, char delim) {
        int n = ss.size();
        if (n == 0)
            return "";

        if (n == 1) {
            String s = ss.iterator().next();
            return s != null ? s : "";
        }
        int len = n - 1;
        for (String s : ss)
            if (s != null)
                len += s.length();

        char[] cs = new char[len];
        int off = 0;
        for (String s : ss) {
            if (off != 0)
                cs[off++] = delim;
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

    public static String[] split(String s, char delim) {
        if (s == null || s.isEmpty())
            return EMPTY_STRING;

        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos+1)) >= 0)
            count++;

        if (count == 1)
            return new String[] { s };

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2-1);
            ss[count] = s.substring(delimPos+1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }

    public static String cut(String s, int index, char delim) {
        int i = 0;
        int begin = 0;
        int end;
        while ((end = s.indexOf(delim, begin)) >= 0) {
            if (i++ == index)
                return s.substring(begin, end);
            begin = end + 1;
        }
        return i == index ? s.substring(begin) : "";
    }

    private static String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) <= ' ')
            beginIndex++;
        while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return beginIndex < endIndex ? s.substring(beginIndex, endIndex) : "";
    }

    public static String trimTrailing(String s) {
        int endIndex = s.length();
        while (endIndex > 0 && s.charAt(endIndex - 1) <= ' ')
            endIndex--;
        return s.substring(0, endIndex);
    }

    public static int parseIS(String s) {
        return s != null && s.length() != 0
                ? Integer.parseInt(s.charAt(0) == '+' ? s.substring(1) : s)
                : 0;
    }

    public static double parseDS(String s) {
        return s != null && s.length() != 0
                ? Double.parseDouble(s.replace(',', '.'))
                : 0;
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

    public static boolean matches(String s, String key,
            boolean matchNullOrEmpty, boolean ignoreCase) {
        if (key == null || key.isEmpty())
            return true;

        if (s == null || s.isEmpty())
            return matchNullOrEmpty;

        return containsWildCard(key) 
                ? compilePattern(key, ignoreCase).matcher(s).matches()
                : ignoreCase ? key.equalsIgnoreCase(s) : key.equals(s);
    }

    public static Pattern compilePattern(String key, boolean ignoreCase) {
        StringTokenizer stk = new StringTokenizer(key, "*?", true);
        StringBuilder regex = new StringBuilder();
        while (stk.hasMoreTokens()) {
            String tk = stk.nextToken();
            char ch1 = tk.charAt(0);
            if (ch1 == '*') {
                regex.append(".*");
            } else if (ch1 == '?') {
                regex.append(".");
            } else {
                regex.append("\\Q").append(tk).append("\\E");
            }
        }
        return Pattern.compile(regex.toString(),
                ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
    }

    public static boolean containsWildCard(String s) {
        return (s.indexOf('*') >= 0 || s.indexOf('?') >= 0);
    }

    public static String[] maskNull(String[] ss) {
        return maskNull(ss, EMPTY_STRING);
    }

    public static <T> T maskNull(T o, T mask) {
        return o == null ? mask : o;
    }

    public static <T> T nullify(T o, T val) {
        return val.equals(o) ? null : o;
    }

    public static String maskEmpty(String s, String mask) {
        return s == null || s.isEmpty() ? mask : s;
    }

    public static String truncate(String s, int maxlen) {
        return s.length() > maxlen ? s.substring(0, maxlen) : s;
    }

    public static <T> boolean equals(T o1, T o2) {
        return o1 == o2 || o1 != null && o1.equals(o2);
    }

    public static String replaceSystemProperties(String s) {
        int i = s.indexOf("${");
        if (i == -1)
            return s;

        StringBuilder sb = new StringBuilder(s.length());
        int j = -1;
        do {
            sb.append(s.substring(j+1, i));
            if ((j = s.indexOf('}', i+2)) == -1) {
                j = i-1;
                break;
            }
            String val = System.getProperty(s.substring(i+2, j));
            sb.append(val != null ? val : s.substring(i, j+1));
            i = s.indexOf("${", j+1);
        } while (i != -1);
        sb.append(s.substring(j+1));
        return sb.toString();
    }


    /**
     * @deprecated use {@link ResourceLocator.resourceURL(String)} instead. 
     */
    @Deprecated
    public static String resourceURL(String name) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        URL url = tcl.getResource(name);
        return url != null ? url.toString() : null;
    }

    public static boolean isUpperCase(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (Character.toUpperCase(ch) != ch)
                return false;
        }
        return len != 0;
    }
}
