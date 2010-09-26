package org.dcm4che.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    private static Calendar cal() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        return cal;
    }

    private static Calendar cal(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    private static void ceil(Calendar cal, int field) {
        cal.add(field, 1);
        cal.add(Calendar.MILLISECOND, -1);
    }

    public static String formatDA(Date date) {
        return formatDA(date, new StringBuilder(8)).toString();
    }

    public static StringBuilder formatDA(Date date, StringBuilder toAppendTo) {
        return formatDT(cal(date), toAppendTo, Calendar.DAY_OF_MONTH);
    }

    public static String formatTM(Date date) {
        return formatTM(date, Calendar.MILLISECOND);
    }

    public static String formatTM(Date date, int lastField) {
        return formatTM(cal(date), new StringBuilder(10), lastField).toString();
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

    public static String formatDT(Date date) {
        return formatDT(date, Calendar.MILLISECOND, false);
    }

    public static String formatDT(Date date, int lastField, boolean timeZone) {
        return formatDT(date, new StringBuilder(23), lastField, timeZone)
                .toString();
    }

    public static StringBuilder formatDT(Date date, StringBuilder toAppendTo,
            int lastField, boolean timeZone) {
        Calendar cal = cal(date);
        formatDT(cal, toAppendTo, lastField);
        if (timeZone) {
            int value = cal.get(Calendar.ZONE_OFFSET)
                    + cal.get(Calendar.DST_OFFSET);
            if (value < 0) {
                value = -value;
                toAppendTo.append('-');
            } else
                toAppendTo.append('+');
            int min = value / 60000;
            appendXX(min / 60, toAppendTo);
            appendXX(min % 60, toAppendTo);
        }
        return toAppendTo;
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

    public static Date parseDA(String s) {
        return parseDA(s, false);
    }

    public static Date parseDA(String s, boolean ceil) {
        Calendar cal = cal();
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

    public static Date parseTM(String s) {
        return parseTM(s, false);
    }

    public static Date parseTM(String s, boolean ceil) {
        return parseTM(cal(), s, ceil);
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

    public static Date parseDT(String s) {
        return parseDT(s, false);
    }

    public static Date parseDT(String s, boolean ceil) {
        Calendar cal = cal();
        try {
            int length = s.length();
            if (length > 4) {
                char tzsign = s.charAt(length - 5);
                boolean tzneg = tzsign == '-';
                if (tzneg || tzsign == '+') {
                    int hh = Integer.parseInt(s.substring(length - 4,
                            length - 2));
                    int min = Integer.parseInt(s.substring(length - 2));
                    int offset = (hh * 60 + min) * 60000;
                    cal.set(Calendar.ZONE_OFFSET, tzneg ? -offset : offset);
                    cal.set(Calendar.DST_OFFSET, 0);
                    length -= 5;
                }
            }
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
