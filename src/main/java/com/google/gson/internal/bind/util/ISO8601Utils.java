package com.google.gson.internal.bind.util;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class ISO8601Utils {
    private static final TimeZone TIMEZONE_UTC = TimeZone.getTimeZone(UTC_ID);
    private static final String UTC_ID = "UTC";

    public static String format(Date date) {
        return format(date, false, TIMEZONE_UTC);
    }

    public static String format(Date date, boolean z) {
        return format(date, z, TIMEZONE_UTC);
    }

    public static String format(Date date, boolean z, TimeZone timeZone) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone, Locale.US);
        gregorianCalendar.setTime(date);
        StringBuilder stringBuilder = new StringBuilder(("yyyy-MM-ddThh:mm:ss".length() + (z ? ".sss".length() : 0)) + (timeZone.getRawOffset() == 0 ? "Z" : "+hh:mm").length());
        padInt(stringBuilder, gregorianCalendar.get(1), "yyyy".length());
        char c = '-';
        stringBuilder.append('-');
        padInt(stringBuilder, gregorianCalendar.get(2) + 1, "MM".length());
        stringBuilder.append('-');
        padInt(stringBuilder, gregorianCalendar.get(5), "dd".length());
        stringBuilder.append('T');
        padInt(stringBuilder, gregorianCalendar.get(11), "hh".length());
        stringBuilder.append(':');
        padInt(stringBuilder, gregorianCalendar.get(12), "mm".length());
        stringBuilder.append(':');
        padInt(stringBuilder, gregorianCalendar.get(13), "ss".length());
        if (z) {
            stringBuilder.append('.');
            padInt(stringBuilder, gregorianCalendar.get(14), "sss".length());
        }
        int offset = timeZone.getOffset(gregorianCalendar.getTimeInMillis());
        if (offset != 0) {
            int i = offset / 60000;
            int abs = Math.abs(i / 60);
            i = Math.abs(i % 60);
            if (offset >= 0) {
                c = '+';
            }
            stringBuilder.append(c);
            padInt(stringBuilder, abs, "hh".length());
            stringBuilder.append(':');
            padInt(stringBuilder, i, "mm".length());
        } else {
            stringBuilder.append('Z');
        }
        return stringBuilder.toString();
    }

    /* JADX WARNING: Removed duplicated region for block: B:75:0x01a9 A:{Catch:{ IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c4 A:{Catch:{ IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }} */
    public static java.util.Date parse(java.lang.String r17, java.text.ParsePosition r18) throws java.text.ParseException {
        /*
        r1 = r17;
        r2 = r18;
        r0 = r18.getIndex();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = r0 + 4;
        r0 = parseInt(r1, r0, r3);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = 45;
        r5 = checkOffset(r1, r3, r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r5 == 0) goto L_0x0018;
    L_0x0016:
        r3 = r3 + 1;
    L_0x0018:
        r5 = r3 + 2;
        r3 = parseInt(r1, r3, r5);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r6 = checkOffset(r1, r5, r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r6 == 0) goto L_0x0026;
    L_0x0024:
        r5 = r5 + 1;
    L_0x0026:
        r6 = r5 + 2;
        r5 = parseInt(r1, r5, r6);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r7 = 84;
        r7 = checkOffset(r1, r6, r7);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r8 = 1;
        if (r7 != 0) goto L_0x0049;
    L_0x0035:
        r9 = r17.length();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r9 > r6) goto L_0x0049;
    L_0x003b:
        r4 = new java.util.GregorianCalendar;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = r3 - r8;
        r4.<init>(r0, r3, r5);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r2.setIndex(r6);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = r4.getTime();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        return r0;
    L_0x0049:
        r9 = 43;
        r10 = 90;
        if (r7 == 0) goto L_0x00b9;
    L_0x004f:
        r6 = r6 + 1;
        r7 = r6 + 2;
        r6 = parseInt(r1, r6, r7);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r12 = 58;
        r13 = checkOffset(r1, r7, r12);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r13 == 0) goto L_0x0061;
    L_0x005f:
        r7 = r7 + 1;
    L_0x0061:
        r13 = r7 + 2;
        r7 = parseInt(r1, r7, r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r12 = checkOffset(r1, r13, r12);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r12 == 0) goto L_0x006f;
    L_0x006d:
        r13 = r13 + 1;
    L_0x006f:
        r12 = r17.length();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r12 <= r13) goto L_0x00bc;
    L_0x0075:
        r12 = r1.charAt(r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r12 == r10) goto L_0x00bc;
    L_0x007b:
        if (r12 == r9) goto L_0x00bc;
    L_0x007d:
        if (r12 == r4) goto L_0x00bc;
    L_0x007f:
        r12 = r13 + 2;
        r13 = parseInt(r1, r13, r12);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r14 = 59;
        if (r13 <= r14) goto L_0x008e;
    L_0x0089:
        r15 = 63;
        if (r13 >= r15) goto L_0x008e;
    L_0x008d:
        goto L_0x008f;
    L_0x008e:
        r14 = r13;
    L_0x008f:
        r13 = 46;
        r13 = checkOffset(r1, r12, r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r13 == 0) goto L_0x00b6;
    L_0x0097:
        r12 = r12 + 1;
        r13 = r12 + 1;
        r13 = indexOfNonDigit(r1, r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r15 = r12 + 3;
        r15 = java.lang.Math.min(r13, r15);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r16 = parseInt(r1, r12, r15);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r15 = r15 - r12;
        switch(r15) {
            case 1: goto L_0x00b1;
            case 2: goto L_0x00ae;
            default: goto L_0x00ad;
        };	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
    L_0x00ad:
        goto L_0x00b3;
    L_0x00ae:
        r16 = r16 * 10;
        goto L_0x00b3;
    L_0x00b1:
        r16 = r16 * 100;
    L_0x00b3:
        r12 = r16;
        goto L_0x00be;
    L_0x00b6:
        r13 = r12;
        r12 = 0;
        goto L_0x00be;
    L_0x00b9:
        r13 = r6;
        r6 = 0;
        r7 = 0;
    L_0x00bc:
        r12 = 0;
        r14 = 0;
    L_0x00be:
        r15 = r17.length();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r15 <= r13) goto L_0x01a9;
    L_0x00c4:
        r15 = r1.charAt(r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r11 = 5;
        if (r15 != r10) goto L_0x00d0;
    L_0x00cb:
        r4 = TIMEZONE_UTC;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r13 = r13 + r8;
        goto L_0x0178;
    L_0x00d0:
        if (r15 == r9) goto L_0x00f1;
    L_0x00d2:
        if (r15 != r4) goto L_0x00d5;
    L_0x00d4:
        goto L_0x00f1;
    L_0x00d5:
        r0 = new java.lang.IndexOutOfBoundsException;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = new java.lang.StringBuilder;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3.<init>();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = "Invalid time zone indicator '";
        r3.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3.append(r15);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = "'";
        r3.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = r3.toString();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0.<init>(r3);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        throw r0;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
    L_0x00f1:
        r4 = r1.substring(r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9 = r4.length();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r9 < r11) goto L_0x00fc;
    L_0x00fb:
        goto L_0x010d;
    L_0x00fc:
        r9 = new java.lang.StringBuilder;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9.<init>();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = "00";
        r9.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = r9.toString();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
    L_0x010d:
        r9 = r4.length();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r13 = r13 + r9;
        r9 = "+0000";
        r9 = r9.equals(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r9 != 0) goto L_0x0176;
    L_0x011a:
        r9 = "+00:00";
        r9 = r9.equals(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r9 == 0) goto L_0x0123;
    L_0x0122:
        goto L_0x0176;
    L_0x0123:
        r9 = new java.lang.StringBuilder;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9.<init>();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r10 = "GMT";
        r9.append(r10);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = r9.toString();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9 = java.util.TimeZone.getTimeZone(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r10 = r9.getID();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r15 = r10.equals(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r15 != 0) goto L_0x0174;
    L_0x0142:
        r15 = ":";
        r11 = "";
        r10 = r10.replace(r15, r11);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r10 = r10.equals(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        if (r10 == 0) goto L_0x0151;
    L_0x0150:
        goto L_0x0174;
    L_0x0151:
        r0 = new java.lang.IndexOutOfBoundsException;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = new java.lang.StringBuilder;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3.<init>();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r5 = "Mismatching time zone indicator: ";
        r3.append(r5);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = " given, resolves to ";
        r3.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = r9.getID();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3.append(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = r3.toString();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0.<init>(r3);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        throw r0;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
    L_0x0174:
        r4 = r9;
        goto L_0x0178;
    L_0x0176:
        r4 = TIMEZONE_UTC;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
    L_0x0178:
        r9 = new java.util.GregorianCalendar;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9.<init>(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r4 = 0;
        r9.setLenient(r4);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r9.set(r8, r0);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = r3 - r8;
        r0 = 2;
        r9.set(r0, r3);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = 5;
        r9.set(r0, r5);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = 11;
        r9.set(r0, r6);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = 12;
        r9.set(r0, r7);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = 13;
        r9.set(r0, r14);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = 14;
        r9.set(r0, r12);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r2.setIndex(r13);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r0 = r9.getTime();	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        return r0;
    L_0x01a9:
        r0 = new java.lang.IllegalArgumentException;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        r3 = "No time zone indicator";
        r0.<init>(r3);	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
        throw r0;	 Catch:{ IndexOutOfBoundsException -> 0x01b5, NumberFormatException -> 0x01b3, IllegalArgumentException | IndexOutOfBoundsException | NumberFormatException -> 0x01b1 }
    L_0x01b1:
        r0 = move-exception;
        goto L_0x01b6;
    L_0x01b3:
        r0 = move-exception;
        goto L_0x01b6;
    L_0x01b5:
        r0 = move-exception;
    L_0x01b6:
        if (r1 != 0) goto L_0x01ba;
    L_0x01b8:
        r1 = 0;
        goto L_0x01d0;
    L_0x01ba:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = 34;
        r3.append(r4);
        r3.append(r1);
        r1 = "'";
        r3.append(r1);
        r1 = r3.toString();
    L_0x01d0:
        r3 = r0.getMessage();
        if (r3 == 0) goto L_0x01dc;
    L_0x01d6:
        r4 = r3.isEmpty();
        if (r4 == 0) goto L_0x01fa;
    L_0x01dc:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "(";
        r3.append(r4);
        r4 = r0.getClass();
        r4 = r4.getName();
        r3.append(r4);
        r4 = ")";
        r3.append(r4);
        r3 = r3.toString();
    L_0x01fa:
        r4 = new java.text.ParseException;
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Failed to parse date [";
        r5.append(r6);
        r5.append(r1);
        r1 = "]: ";
        r5.append(r1);
        r5.append(r3);
        r1 = r5.toString();
        r2 = r18.getIndex();
        r4.<init>(r1, r2);
        r4.initCause(r0);
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.gson.internal.bind.util.ISO8601Utils.parse(java.lang.String, java.text.ParsePosition):java.util.Date");
    }

    private static boolean checkOffset(String str, int i, char c) {
        return i < str.length() && str.charAt(i) == c;
    }

    private static int parseInt(String str, int i, int i2) throws NumberFormatException {
        if (i < 0 || i2 > str.length() || i > i2) {
            throw new NumberFormatException(str);
        }
        int i3;
        int digit;
        StringBuilder stringBuilder;
        if (i < i2) {
            i3 = i + 1;
            digit = Character.digit(str.charAt(i), 10);
            if (digit >= 0) {
                digit = -digit;
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid number: ");
                stringBuilder.append(str.substring(i, i2));
                throw new NumberFormatException(stringBuilder.toString());
            }
        }
        i3 = i;
        digit = 0;
        while (i3 < i2) {
            int i4 = i3 + 1;
            i3 = Character.digit(str.charAt(i3), 10);
            if (i3 >= 0) {
                digit = (digit * 10) - i3;
                i3 = i4;
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Invalid number: ");
                stringBuilder.append(str.substring(i, i2));
                throw new NumberFormatException(stringBuilder.toString());
            }
        }
        return -digit;
    }

    private static void padInt(StringBuilder stringBuilder, int i, int i2) {
        String num = Integer.toString(i);
        for (i2 -= num.length(); i2 > 0; i2--) {
            stringBuilder.append('0');
        }
        stringBuilder.append(num);
    }

    private static int indexOfNonDigit(String str, int i) {
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt < '0' || charAt > '9') {
                return i;
            }
            i++;
        }
        return str.length();
    }
}
