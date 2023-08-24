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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.dcm4che3.data.DatePrecision;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class DateUtils {

    public static final Date[] EMPTY_DATES = {};

    private static TimeZone cachedTimeZone;

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
        return formatTM(tz, date, new DatePrecision());
    }

    public static String formatTM(TimeZone tz, Date date, DatePrecision precision) {
        return formatTM(cal(tz, date), new StringBuilder(10),
                precision.lastField).toString();
    }

    private static StringBuilder formatTM(Calendar cal, 
            StringBuilder toAppendTo, int lastField) {
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
        return formatDT(tz, date, new DatePrecision());
    }

    public static String formatDT(TimeZone tz, Date date, DatePrecision precision) {
        return formatDT(tz, date, new StringBuilder(23), precision).toString();
    }

    public static StringBuilder formatDT(TimeZone tz, Date date,
            StringBuilder toAppendTo, DatePrecision precision) {
        Calendar cal = cal(tz, date);
        formatDT(cal, toAppendTo, precision.lastField);
        if (precision.includeTimezone) {
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


    /**
     * Returns Timezone Offset From UTC in format {@code (+|i)HHMM} of specified
     * Timezone without concerning Daylight saving time (DST).
     *
     * @param tz Timezone
     * @return Timezone Offset From UTC in format {@code (+|i)HHMM}
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz) {
        return appendZZZZZ(tz.getRawOffset(), new StringBuilder(5)).toString();
    }

    /**
     * Returns Timezone Offset From UTC in format {@code (+|i)HHMM} of specified
     * Timezone on specified date. If no date is specified, DST is considered
     * for the current date.
     *
     * @param tz Timezone
     * @param date Date or {@code null}
     * @return Timezone Offset From UTC in format {@code (+|i)HHMM}
     */
    public static String formatTimezoneOffsetFromUTC(TimeZone tz, Date date) {
        return appendZZZZZ(tz.getOffset(date == null 
                ? System.currentTimeMillis() : date.getTime()), 
                new StringBuilder(5)).toString();
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
        if (!(length == 8 || length == 10 && !Character.isDigit(s.charAt(4)) && s.charAt(7) == s.charAt(4)))
            throw new IllegalArgumentException(s);
        int pos = 0;
        cal.set(Calendar.YEAR,
                parseDigit(s, pos++) * 1000 +
                parseDigit(s, pos++) * 100 +
                parseDigit(s, pos++) * 10 +
                parseDigit(s, pos++));
        if (length == 10)
            pos++;
        cal.set(Calendar.MONTH,
                parseDigit(s, pos++) * 10 + parseDigit(s, pos++) - 1);
        if (length == 10)
            pos++;
        cal.set(Calendar.DAY_OF_MONTH,
                parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
        if (ceil)
            ceil(cal, Calendar.DAY_OF_MONTH);
        return cal.getTime();
    }

    public static Date parseTM(TimeZone tz, String s, DatePrecision precision) {
        return parseTM(tz, s, false, precision);
    }

    public static Date parseTM(TimeZone tz, String s, boolean ceil,
            DatePrecision precision) {
        return parseTM(cal(tz), truncateTimeZone(s), ceil, precision);
    }

    private static String truncateTimeZone(String s) {
        int length = s.length();
        if (length > 4) {
            char sign = s.charAt(length - 5);
            if (sign == '+' || sign == '-') {
                return s.substring(0, length - 5);
            }
        }
        return s;
    }

    private static Date parseTM(Calendar cal, String s, boolean ceil,
            DatePrecision precision) {
        int length = s.length();
        int pos = 0;
        if (pos + 2 > length)
            throw new IllegalArgumentException(s);

        cal.set(precision.lastField = Calendar.HOUR_OF_DAY,
            parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            if (pos + 2 > length)
                throw new IllegalArgumentException(s);

            cal.set(precision.lastField = Calendar.MINUTE,
                parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(precision.lastField = Calendar.SECOND,
                        parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
                int n = length - pos;
                if (n > 0) {
                    if (s.charAt(pos++) != '.')
                        throw new IllegalArgumentException(s);

                    int d, millis = 0;
                    for (int i = 1; i < n; ++i) {
                        d = parseDigit(s, pos++);
                        if (i < 4)
                            millis += d;
                        else if (i == 4 & d > 4) // round up
                            millis++;
                        if (i < 3) millis *= 10;
                    }
                    for (int i = n; i < 3; ++i) millis *= 10;
                    cal.set(precision.lastField = Calendar.MILLISECOND, millis);
                    return cal.getTime();
                }
            }
        }
        if (ceil)
            ceil(cal, precision.lastField);
        return cal.getTime();
    }

    private static int parseDigit(String s, int index) {
        int d = s.charAt(index) - '0';
        if (d < 0 || d > 9) throw new IllegalArgumentException(s);
        return d;
    }

    public static Date parseDT(TimeZone tz, String s, DatePrecision precision) {
        return parseDT(tz, s, false, precision);
    }

    public static TimeZone timeZone(String s) {
        TimeZone tz;
        if (s.length() != 5 || (tz = safeTimeZone(s)) == null)
            throw new IllegalArgumentException("Illegal Timezone Offset: " + s);
        return tz;
    }

    private static TimeZone safeTimeZone(String s) {
        String tzid = tzid(s);
        if (tzid == null)
            return null;

        TimeZone tz = cachedTimeZone;
        if (tz == null || !tz.getID().equals(tzid))
            cachedTimeZone = tz = TimeZone.getTimeZone(tzid);

        return tz;
    }

    private static String tzid(String s) {
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
                return new String(tzid);
            }
        }
        return null;
    }

    public static Date parseDT(TimeZone tz, String s, boolean ceil,
            DatePrecision precision) {
        int length = s.length();
        TimeZone tz1 = safeTimeZone(s);
        if (precision.includeTimezone = tz1 != null) {
            length -= 5;
            tz = tz1;
        }
        Calendar cal = cal(tz);
        int pos = 0;
        if (pos + 4 > length)
            throw new IllegalArgumentException(s);
        cal.set(precision.lastField = Calendar.YEAR,
                parseDigit(s, pos++) * 1000 +
                parseDigit(s, pos++) * 100 +
                parseDigit(s, pos++) * 10 +
                parseDigit(s, pos++));
        if (pos < length) {
            if (!Character.isDigit(s.charAt(pos)))
                pos++;
            if (pos + 2 > length)
                throw new IllegalArgumentException(s);
            cal.set(precision.lastField = Calendar.MONTH,
                    parseDigit(s, pos++) * 10 + parseDigit(s, pos++) - 1);
            if (pos < length) {
                if (!Character.isDigit(s.charAt(pos)))
                    pos++;
                if (pos + 2 > length)
                    throw new IllegalArgumentException(s);
                cal.set(precision.lastField = Calendar.DAY_OF_MONTH,
                        parseDigit(s, pos++) * 10 + parseDigit(s, pos++));
                if (pos < length)
                    return parseTM(cal, s.substring(pos, length), ceil,
                            precision);
            }
        }
        if (ceil)
            ceil(cal, precision.lastField);
        return cal.getTime();
    }

}
