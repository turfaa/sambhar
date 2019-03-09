package com.google.zxing.client.result;

import io.sentry.DefaultSentryClientFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CalendarParsedResult extends ParsedResult {
    private static final Pattern DATE_TIME = Pattern.compile("[0-9]{8}(T[0-9]{6}Z?)?");
    private static final Pattern RFC2445_DURATION = Pattern.compile("P(?:(\\d+)W)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?");
    private static final long[] RFC2445_DURATION_FIELD_UNITS = new long[]{604800000, 86400000, 3600000, DefaultSentryClientFactory.BUFFER_FLUSHTIME_DEFAULT, 1000};
    private final String[] attendees;
    private final String description;
    private final Date end;
    private final boolean endAllDay;
    private final double latitude;
    private final String location;
    private final double longitude;
    private final String organizer;
    private final Date start;
    private final boolean startAllDay;
    private final String summary;

    public CalendarParsedResult(String str, String str2, String str3, String str4, String str5, String str6, String[] strArr, String str7, double d, double d2) {
        super(ParsedResultType.CALENDAR);
        this.summary = str;
        try {
            this.start = parseDate(str2);
            if (str3 == null) {
                Date date;
                long parseDurationMS = parseDurationMS(str4);
                if (parseDurationMS < 0) {
                    date = null;
                } else {
                    date = new Date(this.start.getTime() + parseDurationMS);
                }
                this.end = date;
            } else {
                try {
                    this.end = parseDate(str3);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e.toString());
                }
            }
            boolean z = false;
            this.startAllDay = str2.length() == 8;
            if (str3 != null && str3.length() == 8) {
                z = true;
            }
            this.endAllDay = z;
            this.location = str5;
            this.organizer = str6;
            this.attendees = strArr;
            this.description = str7;
            this.latitude = d;
            this.longitude = d2;
        } catch (ParseException e2) {
            throw new IllegalArgumentException(e2.toString());
        }
    }

    public String getSummary() {
        return this.summary;
    }

    public Date getStart() {
        return this.start;
    }

    public boolean isStartAllDay() {
        return this.startAllDay;
    }

    public Date getEnd() {
        return this.end;
    }

    public boolean isEndAllDay() {
        return this.endAllDay;
    }

    public String getLocation() {
        return this.location;
    }

    public String getOrganizer() {
        return this.organizer;
    }

    public String[] getAttendees() {
        return this.attendees;
    }

    public String getDescription() {
        return this.description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getDisplayResult() {
        StringBuilder stringBuilder = new StringBuilder(100);
        ParsedResult.maybeAppend(this.summary, stringBuilder);
        ParsedResult.maybeAppend(format(this.startAllDay, this.start), stringBuilder);
        ParsedResult.maybeAppend(format(this.endAllDay, this.end), stringBuilder);
        ParsedResult.maybeAppend(this.location, stringBuilder);
        ParsedResult.maybeAppend(this.organizer, stringBuilder);
        ParsedResult.maybeAppend(this.attendees, stringBuilder);
        ParsedResult.maybeAppend(this.description, stringBuilder);
        return stringBuilder.toString();
    }

    private static Date parseDate(String str) throws ParseException {
        if (!DATE_TIME.matcher(str).matches()) {
            throw new ParseException(str, 0);
        } else if (str.length() == 8) {
            return buildDateFormat().parse(str);
        } else {
            Date parse;
            if (str.length() == 16 && str.charAt(15) == 'Z') {
                parse = buildDateTimeFormat().parse(str.substring(0, 15));
                GregorianCalendar gregorianCalendar = new GregorianCalendar();
                long time = parse.getTime() + ((long) gregorianCalendar.get(15));
                gregorianCalendar.setTime(new Date(time));
                parse = new Date(time + ((long) gregorianCalendar.get(16)));
            } else {
                parse = buildDateTimeFormat().parse(str);
            }
            return parse;
        }
    }

    private static String format(boolean z, Date date) {
        if (date == null) {
            return null;
        }
        DateFormat dateInstance;
        if (z) {
            dateInstance = DateFormat.getDateInstance(2);
        } else {
            dateInstance = DateFormat.getDateTimeInstance(2, 2);
        }
        return dateInstance.format(date);
    }

    private static long parseDurationMS(CharSequence charSequence) {
        if (charSequence == null) {
            return -1;
        }
        Matcher matcher = RFC2445_DURATION.matcher(charSequence);
        if (!matcher.matches()) {
            return -1;
        }
        long j = 0;
        int i = 0;
        while (i < RFC2445_DURATION_FIELD_UNITS.length) {
            int i2 = i + 1;
            String group = matcher.group(i2);
            if (group != null) {
                j += RFC2445_DURATION_FIELD_UNITS[i] * ((long) Integer.parseInt(group));
            }
            i = i2;
        }
        return j;
    }

    private static DateFormat buildDateFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat;
    }

    private static DateFormat buildDateTimeFormat() {
        return new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.ENGLISH);
    }
}
