package com.fasterxml.jackson.core.io;

import java.math.BigDecimal;

public final class NumberInput {
    static final long L_BILLION = 1000000000;
    static final String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);
    static final String MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE).substring(1);
    public static final String NASTY_SMALL_DOUBLE = "2.2250738585072012e-308";

    public static int parseInt(char[] cArr, int i, int i2) {
        int i3 = cArr[i] - 48;
        if (i2 > 4) {
            i++;
            i++;
            i++;
            i++;
            i3 = (((((((i3 * 10) + (cArr[i] - 48)) * 10) + (cArr[i] - 48)) * 10) + (cArr[i] - 48)) * 10) + (cArr[i] - 48);
            i2 -= 4;
            if (i2 > 4) {
                i++;
                i++;
                i++;
                return (((((((i3 * 10) + (cArr[i] - 48)) * 10) + (cArr[i] - 48)) * 10) + (cArr[i] - 48)) * 10) + (cArr[i + 1] - 48);
            }
        }
        if (i2 > 1) {
            i++;
            i3 = (i3 * 10) + (cArr[i] - 48);
            if (i2 > 2) {
                i++;
                i3 = (i3 * 10) + (cArr[i] - 48);
                if (i2 > 3) {
                    i3 = (i3 * 10) + (cArr[i + 1] - 48);
                }
            }
        }
        return i3;
    }

    /* JADX WARNING: Missing block: B:37:0x0075, code skipped:
            return java.lang.Integer.parseInt(r8);
     */
    public static int parseInt(java.lang.String r8) {
        /*
        r0 = 0;
        r1 = r8.charAt(r0);
        r2 = r8.length();
        r3 = 1;
        r4 = 45;
        if (r1 != r4) goto L_0x000f;
    L_0x000e:
        r0 = 1;
    L_0x000f:
        r4 = 10;
        if (r0 == 0) goto L_0x0025;
    L_0x0013:
        if (r2 == r3) goto L_0x0020;
    L_0x0015:
        if (r2 <= r4) goto L_0x0018;
    L_0x0017:
        goto L_0x0020;
    L_0x0018:
        r1 = 2;
        r3 = r8.charAt(r3);
        r1 = r3;
        r3 = 2;
        goto L_0x002e;
    L_0x0020:
        r8 = java.lang.Integer.parseInt(r8);
        return r8;
    L_0x0025:
        r5 = 9;
        if (r2 <= r5) goto L_0x002e;
    L_0x0029:
        r8 = java.lang.Integer.parseInt(r8);
        return r8;
    L_0x002e:
        r5 = 57;
        if (r1 > r5) goto L_0x0084;
    L_0x0032:
        r6 = 48;
        if (r1 >= r6) goto L_0x0037;
    L_0x0036:
        goto L_0x0084;
    L_0x0037:
        r1 = r1 - r6;
        if (r3 >= r2) goto L_0x0080;
    L_0x003a:
        r7 = r3 + 1;
        r3 = r8.charAt(r3);
        if (r3 > r5) goto L_0x007b;
    L_0x0042:
        if (r3 >= r6) goto L_0x0045;
    L_0x0044:
        goto L_0x007b;
    L_0x0045:
        r1 = r1 * 10;
        r3 = r3 - r6;
        r1 = r1 + r3;
        if (r7 >= r2) goto L_0x0080;
    L_0x004b:
        r3 = r7 + 1;
        r7 = r8.charAt(r7);
        if (r7 > r5) goto L_0x0076;
    L_0x0053:
        if (r7 >= r6) goto L_0x0056;
    L_0x0055:
        goto L_0x0076;
    L_0x0056:
        r1 = r1 * 10;
        r7 = r7 - r6;
        r1 = r1 + r7;
        if (r3 >= r2) goto L_0x0080;
    L_0x005c:
        r7 = r3 + 1;
        r3 = r8.charAt(r3);
        if (r3 > r5) goto L_0x0071;
    L_0x0064:
        if (r3 >= r6) goto L_0x0067;
    L_0x0066:
        goto L_0x0071;
    L_0x0067:
        r1 = r1 * 10;
        r3 = r3 + -48;
        r1 = r1 + r3;
        if (r7 < r2) goto L_0x006f;
    L_0x006e:
        goto L_0x0080;
    L_0x006f:
        r3 = r7;
        goto L_0x005c;
    L_0x0071:
        r8 = java.lang.Integer.parseInt(r8);
        return r8;
    L_0x0076:
        r8 = java.lang.Integer.parseInt(r8);
        return r8;
    L_0x007b:
        r8 = java.lang.Integer.parseInt(r8);
        return r8;
    L_0x0080:
        if (r0 == 0) goto L_0x0083;
    L_0x0082:
        r1 = -r1;
    L_0x0083:
        return r1;
    L_0x0084:
        r8 = java.lang.Integer.parseInt(r8);
        return r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fasterxml.jackson.core.io.NumberInput.parseInt(java.lang.String):int");
    }

    public static long parseLong(char[] cArr, int i, int i2) {
        i2 -= 9;
        return (((long) parseInt(cArr, i, i2)) * L_BILLION) + ((long) parseInt(cArr, i + i2, 9));
    }

    public static long parseLong(String str) {
        if (str.length() <= 9) {
            return (long) parseInt(str);
        }
        return Long.parseLong(str);
    }

    public static boolean inLongRange(char[] cArr, int i, int i2, boolean z) {
        String str = z ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int length = str.length();
        boolean z2 = true;
        if (i2 < length) {
            return true;
        }
        if (i2 > length) {
            return false;
        }
        for (i2 = 0; i2 < length; i2++) {
            int charAt = cArr[i + i2] - str.charAt(i2);
            if (charAt != 0) {
                if (charAt >= 0) {
                    z2 = false;
                }
                return z2;
            }
        }
        return true;
    }

    public static boolean inLongRange(String str, boolean z) {
        String str2 = z ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int length = str2.length();
        int length2 = str.length();
        boolean z2 = true;
        if (length2 < length) {
            return true;
        }
        if (length2 > length) {
            return false;
        }
        for (length2 = 0; length2 < length; length2++) {
            int charAt = str.charAt(length2) - str2.charAt(length2);
            if (charAt != 0) {
                if (charAt >= 0) {
                    z2 = false;
                }
                return z2;
            }
        }
        return true;
    }

    public static int parseAsInt(String str, int i) {
        if (str == null) {
            return i;
        }
        str = str.trim();
        int length = str.length();
        if (length == 0) {
            return i;
        }
        int i2 = 0;
        if (length > 0) {
            char charAt = str.charAt(0);
            if (charAt == '+') {
                str = str.substring(1);
                length = str.length();
            } else if (charAt == '-') {
                i2 = 1;
            }
        }
        while (i2 < length) {
            char charAt2 = str.charAt(i2);
            if (charAt2 > '9' || charAt2 < '0') {
                try {
                    return (int) parseDouble(str);
                } catch (NumberFormatException unused) {
                    return i;
                }
            }
            i2++;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException unused2) {
            return i;
        }
    }

    public static long parseAsLong(String str, long j) {
        if (str == null) {
            return j;
        }
        str = str.trim();
        int length = str.length();
        if (length == 0) {
            return j;
        }
        int i = 0;
        if (length > 0) {
            char charAt = str.charAt(0);
            if (charAt == '+') {
                str = str.substring(1);
                length = str.length();
            } else if (charAt == '-') {
                i = 1;
            }
        }
        while (i < length) {
            char charAt2 = str.charAt(i);
            if (charAt2 > '9' || charAt2 < '0') {
                try {
                    return (long) parseDouble(str);
                } catch (NumberFormatException unused) {
                    return j;
                }
            }
            i++;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException unused2) {
            return j;
        }
    }

    public static double parseAsDouble(String str, double d) {
        if (str == null) {
            return d;
        }
        str = str.trim();
        if (str.length() == 0) {
            return d;
        }
        try {
            return parseDouble(str);
        } catch (NumberFormatException unused) {
            return d;
        }
    }

    public static double parseDouble(String str) throws NumberFormatException {
        if (NASTY_SMALL_DOUBLE.equals(str)) {
            return Double.MIN_VALUE;
        }
        return Double.parseDouble(str);
    }

    public static BigDecimal parseBigDecimal(String str) throws NumberFormatException {
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException unused) {
            throw _badBD(str);
        }
    }

    public static BigDecimal parseBigDecimal(char[] cArr) throws NumberFormatException {
        return parseBigDecimal(cArr, 0, cArr.length);
    }

    public static BigDecimal parseBigDecimal(char[] cArr, int i, int i2) throws NumberFormatException {
        try {
            return new BigDecimal(cArr, i, i2);
        } catch (NumberFormatException unused) {
            throw _badBD(new String(cArr, i, i2));
        }
    }

    private static NumberFormatException _badBD(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Value \"");
        stringBuilder.append(str);
        stringBuilder.append("\" can not be represented as BigDecimal");
        return new NumberFormatException(stringBuilder.toString());
    }
}
