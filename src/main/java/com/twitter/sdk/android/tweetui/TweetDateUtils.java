package com.twitter.sdk.android.tweetui;

import android.content.res.Resources;
import android.support.v4.util.SparseArrayCompat;
import io.sentry.DefaultSentryClientFactory;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

final class TweetDateUtils {
    static final SimpleDateFormat DATE_TIME_RFC822 = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
    static final long INVALID_DATE = -1;
    static final DateFormatter RELATIVE_DATE_FORMAT = new DateFormatter();

    static class DateFormatter {
        private Locale currentLocale;
        private final SparseArrayCompat<SimpleDateFormat> dateFormatArray = new SparseArrayCompat();

        DateFormatter() {
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized String formatLongDateString(Resources resources, Date date) {
            return getDateFormat(resources, R.string.tw__relative_date_format_long).format(date);
        }

        /* Access modifiers changed, original: declared_synchronized */
        public synchronized String formatShortDateString(Resources resources, Date date) {
            return getDateFormat(resources, R.string.tw__relative_date_format_short).format(date);
        }

        private synchronized DateFormat getDateFormat(Resources resources, int i) {
            DateFormat dateFormat;
            if (this.currentLocale == null || this.currentLocale != resources.getConfiguration().locale) {
                this.currentLocale = resources.getConfiguration().locale;
                this.dateFormatArray.clear();
            }
            dateFormat = (SimpleDateFormat) this.dateFormatArray.get(i);
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(resources.getString(i), Locale.getDefault());
                this.dateFormatArray.put(i, dateFormat);
            }
            return dateFormat;
        }
    }

    private TweetDateUtils() {
    }

    static long apiTimeToLong(String str) {
        if (str == null) {
            return -1;
        }
        try {
            return DATE_TIME_RFC822.parse(str).getTime();
        } catch (ParseException unused) {
            return -1;
        }
    }

    static boolean isValidTimestamp(String str) {
        return apiTimeToLong(str) != -1;
    }

    static String dotPrefix(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("• ");
        stringBuilder.append(str);
        return stringBuilder.toString();
    }

    static String getRelativeTimeString(Resources resources, long j, long j2) {
        long j3 = j - j2;
        if (j3 < 0) {
            return RELATIVE_DATE_FORMAT.formatLongDateString(resources, new Date(j2));
        }
        int i;
        if (j3 < DefaultSentryClientFactory.BUFFER_FLUSHTIME_DEFAULT) {
            i = (int) (j3 / 1000);
            return resources.getQuantityString(R.plurals.tw__time_secs, i, new Object[]{Integer.valueOf(i)});
        } else if (j3 < 3600000) {
            i = (int) (j3 / DefaultSentryClientFactory.BUFFER_FLUSHTIME_DEFAULT);
            return resources.getQuantityString(R.plurals.tw__time_mins, i, new Object[]{Integer.valueOf(i)});
        } else if (j3 < 86400000) {
            i = (int) (j3 / 3600000);
            return resources.getQuantityString(R.plurals.tw__time_hours, i, new Object[]{Integer.valueOf(i)});
        } else {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(j);
            Calendar instance2 = Calendar.getInstance();
            instance2.setTimeInMillis(j2);
            Date date = new Date(j2);
            if (instance.get(1) == instance2.get(1)) {
                return RELATIVE_DATE_FORMAT.formatShortDateString(resources, date);
            }
            return RELATIVE_DATE_FORMAT.formatLongDateString(resources, date);
        }
    }
}
