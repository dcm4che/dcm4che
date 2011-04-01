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

package org.dcm4che.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DateUtils {

    private static CachedTimeZone cachedTimeZone;
    
    private static final class CachedTimeZone {
        final String offset;
        final TimeZone zone;
        CachedTimeZone(String offset, TimeZone zone) {
            this.offset = offset;
            this.zone = zone;
        }
    }

    private static Calendar cal(TimeZone tz) {
        Calendar cal = (tz != null)
                ? new GregorianCalendar(tz)
                : new GregorianCalendar();
        cal.clear();
        return cal;
    }

    private static Calendar cal(TimeZone tz, Date date) {
        Calendar cal = (tz != null)
                ? new GregorianCalendar(tz)
                : new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    private static void ceil(Calendar cal, int field) {
        cal.add(field, 1);
        cal.add(Calendar.MILLISECOND, -1);
    }

    public static String formatDA(TimeZone tz, Date date) {
        return formatDA(tz, date, new StringBuilder(8)).toString();
    }

    public static StringBuilder formatDA(TimeZone tz, Date date,
            StringBuilder toAppendTo) {
        return formatDT(cal(tz, date), toAppendTo, Calendar.DAY_OF_MONTH);
    }

    public static String formatTM(TimeZone tz, Date date) {
        return formatTM(tz, date, Calendar.MILLISECOND);
    }

    public static String formatTM(TimeZone tz, Date date, int lastField) {
        return formatTM(cal(tz, date), new StringBuilder(10), lastField).toString();
    }

    private static StringBuilder formatTM(Calendar cal, StringBuilder toAppendTo,
            int lastField) {
        appendXX(cal.get(Calendar.HOUR_OF_DAY), toAppendTo);
        if (lastField > Calendar.HOUR_OF_DAY) {
            appendXX(cal.get(Calendar.MINUTE), toAppendTo);
            if (lastField > Calendar.MINUTE) {
                appendXX(cal.get(Calendar.SECOND), toAppendTo);
                if (lastField > Calendar.SECOND) {
                    toAppendTo.append('.');
                    appendXXX(cal.get(Calendar.MILLISECOND), toAppendTo);
                }
            }
        }
        return toAppendTo;
    }

    public static String formatDT(TimeZone tz, Date date) {
        return formatDT(tz, date, Calendar.MILLISECOND, false);
    }

    public static String formatDT(TimeZone tz, Date date, int lastField,
            boolean timeZone) {
        return formatDT(tz, date, new StringBuilder(23), lastField, timeZone)
                .toString();
    }

    public static StringBuilder formatDT(TimeZone tz, Date date,
            StringBuilder toAppendTo, int lastField, boolean timeZone) {
        Calendar cal = cal(tz, date);
        formatDT(cal, toAppendTo, lastField);
        if (timeZone) {
            int offset = cal.get(Calendar.ZONE_OFFSET)
                    + cal.get(Calendar.DST_OFFSET);
            appendZZZZZ(offset, toAppendTo);
        }
        return toAppendTo;
    }

    private static StringBuilder appendZZZZZ(int offset, StringBuilder sb) {
        if (offset < 0) {
            offset = -offset;
            sb.append('-');
        } else
            sb.append('+');
        int min = offset / 60000;
        appendXX(min / 60, sb);
        appendXX(min % 60, sb);
        return sb;
    }


    public static String format(TimeZone tz) {
        int offset = tz.getOffset(System.currentTimeMillis());
        return appendZZZZZ(offset, new StringBuilder(5)).toString();
    }

    private static StringBuilder formatDT(Calendar cal, StringBuilder toAppendTo,
            int lastField) {
        appendXXXX(cal.get(Calendar.YEAR), toAppendTo);
        if (lastField > Calendar.YEAR) {
            appendXX(cal.get(Calendar.MONTH) + 1, toAppendTo);
            if (lastField > Calendar.MONTH) {
                appendXX(cal.get(Calendar.DAY_OF_MONTH), toAppendTo);
                if (lastField > Calendar.DAY_OF_MONTH) {
                    formatTM(cal, toAppendTo, lastField);
                }
            }
        }
        return toAppendTo;
    }

    private static void appendXXXX(int i, StringBuilder toAppendTo) {
        if (i < 1000)
            toAppendTo.append('0');
        appendXXX(i, toAppendTo);
    }

    private static void appendXXX(int i, StringBuilder toAppendTo) {
        if (i < 100)
            toAppendTo.append('0');
        appendXX(i, toAppendTo);
    }

    private static void appendXX(int i, StringBuilder toAppendTo) {
        if (i < 10)
            toAppendTo.append('0');
        toAppendTo.append(i);
    }

    public static Date parseDA(TimeZone tz, String s) {
        return parseDA(tz, s, false);
    }

    public static Date parseDA(TimeZone tz, String s, boolean ceil) {
        Calendar cal = cal(tz);
        int length = s.length();
        if (!(length == 8 || length == 10 && !Character.isDigit(s.charAt(4))))
            throw new IllegalArgumentException(s);
        try {
            int pos = 0;
            cal.set(Calendar.YEAR,
                    Integer.parseInt(s.substring(pos, pos + 4)));
            pos += 4;
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            cal.set(Calendar.MONTH,
                    Integer.parseInt(s.substring(pos, pos + 2)) - 1);
            pos += 2;
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            cal.set(Calendar.DAY_OF_MONTH,
                    Integer.parseInt(s.substring(pos)));
            if (ceil)
                ceil(cal, Calendar.DAY_OF_MONTH);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        return cal.getTime();
    }

    public static Date parseTM(TimeZone tz, String s) {
        return parseTM(tz, s, false);
    }

    public static Date parseTM(TimeZone tz, String s, boolean ceil) {
        return parseTM(cal(tz), s, ceil);
    }

    private static Date parseTM(Calendar cal, String s, boolean ceil) {
        int length = s.length();
        int pos = 0;
        if (pos + 2 > length)
            throw new IllegalArgumentException(s);
        try {
            cal.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(s.substring(pos, pos + 2)));
            pos += 2;
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(Calendar.MINUTE,
                        Integer.parseInt(s.substring(pos, pos + 2)));
                pos += 2;
                if (pos < length) {
                    if (!Character.isDigit(s.charAt(pos)))
                        pos++;
                    if (pos + 2 > length)
                        throw new IllegalArgumentException(s);
                    cal.set(Calendar.SECOND,
                            Integer.parseInt(s.substring(pos, pos + 2)));
                    pos += 2;
                    if (pos < length) {
                        float f = Float.parseFloat(s.substring(pos));
                        if (f >= 1 || f < 0)
                            throw new IllegalArgumentException(s);
                        cal.set(Calendar.MILLISECOND, (int) (f * 1000));
                    } else if (ceil)
                        ceil(cal, Calendar.SECOND);
                } else if (ceil)
                    ceil(cal, Calendar.MINUTE);
            } else if (ceil)
                ceil(cal, Calendar.HOUR_OF_DAY);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        return cal.getTime();
    }

    public static Date parseDT(TimeZone tz, String s) {
        return parseDT(tz, s, false);
    }

    public static TimeZone timeZone(String s) {
        TimeZone tz;
        if (s.length() != 5 || (tz = safeTimeZone(s)) == null)
            throw new IllegalArgumentException("Illegal Timezone Offset: " + s);
        return tz;
    }

    private static TimeZone safeTimeZone(String s) {
        CachedTimeZone tmp = cachedTimeZone;
        if (tmp != null && s.endsWith(tmp.offset))
            return tmp.zone;

        int length = s.length();
        if (length > 4) {
            char[] tzid = { 'G', 'M', 'T', 0, 0, 0, ':', 0, 0 };
            s.getChars(length-5, length-2, tzid, 3);
            s.getChars(length-2, length, tzid, 7);
            if ((tzid[3] == '+' || tzid[3] == '-')
                    && Character.isDigit(tzid[4])
                    && Character.isDigit(tzid[5])
                    && Character.isDigit(tzid[7])
                    && Character.isDigit(tzid[8])) {
                TimeZone zone = TimeZone.getTimeZone(new String(tzid));
                cachedTimeZone = new CachedTimeZone(s.substring(length-5), zone);
                return zone;
            }
        }
        return null;
    }

    public static Date parseDT(TimeZone tz, String s, boolean ceil) {
        int length = s.length();
        TimeZone tz1 = safeTimeZone(s);
        if (tz1 != null) {
            length -= 5;
            tz = tz1;
        }
        Calendar cal = cal(tz);
        try {
            int pos = 0;
            if (pos + 4 > length)
                throw new IllegalArgumentException(s);
            cal.set(Calendar.YEAR, Integer.parseInt(s.substring(pos, pos + 4)));
            pos += 4;
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(Calendar.MONTH, Integer.parseInt(s.substring(pos,
                        pos + 2)) - 1);
                pos += 2;
                if (pos < length) {
                    if (!Character.isDigit(s.charAt(pos)))
                        pos++;
                    if (pos + 2 > length)
                        throw new IllegalArgumentException(s);
                    cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s
                            .substring(pos, pos + 2)));
                    pos += 2;
                    if (pos < length)
                        return parseTM(cal, s.substring(pos, length), ceil);
                    else if (ceil)
                        ceil(cal, Calendar.DAY_OF_MONTH);
                } else if (ceil)
                    ceil(cal, Calendar.MONTH);
            } else if (ceil)
                ceil(cal, Calendar.YEAR);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(s);
        }
        return cal.getTime();
    }

}
