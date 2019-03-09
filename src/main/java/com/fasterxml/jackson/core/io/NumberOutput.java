package com.fasterxml.jackson.core.io;

import com.facebook.appevents.AppEventsConstants;
import io.sentry.connection.AbstractConnection;

public final class NumberOutput {
    private static int BILLION = 1000000000;
    private static long BILLION_L = 1000000000;
    private static long MAX_INT_AS_LONG = 2147483647L;
    private static int MILLION = 1000000;
    private static long MIN_INT_AS_LONG = -2147483648L;
    static final String SMALLEST_INT = String.valueOf(Integer.MIN_VALUE);
    static final String SMALLEST_LONG = String.valueOf(Long.MIN_VALUE);
    private static final int[] TRIPLET_TO_CHARS = new int[1000];
    private static final String[] sSmallIntStrs = new String[]{AppEventsConstants.EVENT_PARAM_VALUE_NO, AppEventsConstants.EVENT_PARAM_VALUE_YES, "2", "3", "4", "5", AbstractConnection.SENTRY_PROTOCOL_VERSION, "7", "8", "9", "10"};
    private static final String[] sSmallIntStrs2 = new String[]{"-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10"};

    static {
        int i = 0;
        int i2 = 0;
        while (i < 10) {
            int i3 = i2;
            i2 = 0;
            while (i2 < 10) {
                int i4 = i3;
                i3 = 0;
                while (i3 < 10) {
                    int i5 = i4 + 1;
                    TRIPLET_TO_CHARS[i4] = (((i + 48) << 16) | ((i2 + 48) << 8)) | (i3 + 48);
                    i3++;
                    i4 = i5;
                }
                i2++;
                i3 = i4;
            }
            i++;
            i2 = i3;
        }
    }

    public static int outputInt(int i, char[] cArr, int i2) {
        int i3;
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                return _outputSmallestI(cArr, i2);
            }
            i3 = i2 + 1;
            cArr[i2] = '-';
            i = -i;
            i2 = i3;
        }
        if (i < MILLION) {
            if (i >= 1000) {
                i3 = i / 1000;
                return _full3(i - (i3 * 1000), cArr, _leading3(i3, cArr, i2));
            } else if (i >= 10) {
                return _leading3(i, cArr, i2);
            } else {
                cArr[i2] = (char) (i + 48);
                return i2 + 1;
            }
        } else if (i >= BILLION) {
            i -= BILLION;
            if (i >= BILLION) {
                i -= BILLION;
                i3 = i2 + 1;
                cArr[i2] = '2';
            } else {
                i3 = i2 + 1;
                cArr[i2] = '1';
            }
            return _outputFullBillion(i, cArr, i3);
        } else {
            i3 = i / 1000;
            int i4 = i3 / 1000;
            return _full3(i - (i3 * 1000), cArr, _full3(i3 - (i4 * 1000), cArr, _leading3(i4, cArr, i2)));
        }
    }

    public static int outputInt(int i, byte[] bArr, int i2) {
        int i3;
        if (i < 0) {
            if (i == Integer.MIN_VALUE) {
                return _outputSmallestI(bArr, i2);
            }
            i3 = i2 + 1;
            bArr[i2] = (byte) 45;
            i = -i;
            i2 = i3;
        }
        if (i < MILLION) {
            if (i >= 1000) {
                i3 = i / 1000;
                i3 = _full3(i - (i3 * 1000), bArr, _leading3(i3, bArr, i2));
            } else if (i < 10) {
                i3 = i2 + 1;
                bArr[i2] = (byte) (i + 48);
            } else {
                i3 = _leading3(i, bArr, i2);
            }
            return i3;
        } else if (i >= BILLION) {
            i -= BILLION;
            if (i >= BILLION) {
                i -= BILLION;
                i3 = i2 + 1;
                bArr[i2] = (byte) 50;
            } else {
                i3 = i2 + 1;
                bArr[i2] = (byte) 49;
            }
            return _outputFullBillion(i, bArr, i3);
        } else {
            i3 = i / 1000;
            int i4 = i3 / 1000;
            return _full3(i - (i3 * 1000), bArr, _full3(i3 - (i4 * 1000), bArr, _leading3(i4, bArr, i2)));
        }
    }

    public static int outputLong(long j, char[] cArr, int i) {
        if (j < 0) {
            if (j > MIN_INT_AS_LONG) {
                return outputInt((int) j, cArr, i);
            }
            if (j == Long.MIN_VALUE) {
                return _outputSmallestL(cArr, i);
            }
            int i2 = i + 1;
            cArr[i] = '-';
            j = -j;
            i = i2;
        } else if (j <= MAX_INT_AS_LONG) {
            return outputInt((int) j, cArr, i);
        }
        long j2 = j / BILLION_L;
        j -= BILLION_L * j2;
        if (j2 < BILLION_L) {
            i = _outputUptoBillion((int) j2, cArr, i);
        } else {
            long j3 = j2 / BILLION_L;
            i = _outputFullBillion((int) (j2 - (BILLION_L * j3)), cArr, _leading3((int) j3, cArr, i));
        }
        return _outputFullBillion((int) j, cArr, i);
    }

    public static int outputLong(long j, byte[] bArr, int i) {
        if (j < 0) {
            if (j > MIN_INT_AS_LONG) {
                return outputInt((int) j, bArr, i);
            }
            if (j == Long.MIN_VALUE) {
                return _outputSmallestL(bArr, i);
            }
            int i2 = i + 1;
            bArr[i] = (byte) 45;
            j = -j;
            i = i2;
        } else if (j <= MAX_INT_AS_LONG) {
            return outputInt((int) j, bArr, i);
        }
        long j2 = j / BILLION_L;
        j -= BILLION_L * j2;
        if (j2 < BILLION_L) {
            i = _outputUptoBillion((int) j2, bArr, i);
        } else {
            long j3 = j2 / BILLION_L;
            i = _outputFullBillion((int) (j2 - (BILLION_L * j3)), bArr, _leading3((int) j3, bArr, i));
        }
        return _outputFullBillion((int) j, bArr, i);
    }

    public static String toString(int i) {
        if (i < sSmallIntStrs.length) {
            if (i >= 0) {
                return sSmallIntStrs[i];
            }
            int i2 = (-i) - 1;
            if (i2 < sSmallIntStrs2.length) {
                return sSmallIntStrs2[i2];
            }
        }
        return Integer.toString(i);
    }

    public static String toString(long j) {
        if (j > 2147483647L || j < -2147483648L) {
            return Long.toString(j);
        }
        return toString((int) j);
    }

    public static String toString(double d) {
        return Double.toString(d);
    }

    public static String toString(float f) {
        return Float.toString(f);
    }

    private static int _outputUptoBillion(int i, char[] cArr, int i2) {
        int i3;
        if (i >= MILLION) {
            i3 = i / 1000;
            i -= i3 * 1000;
            int i4 = i3 / 1000;
            i3 -= i4 * 1000;
            i2 = _leading3(i4, cArr, i2);
            i3 = TRIPLET_TO_CHARS[i3];
            i4 = i2 + 1;
            cArr[i2] = (char) (i3 >> 16);
            i2 = i4 + 1;
            cArr[i4] = (char) ((i3 >> 8) & 127);
            i4 = i2 + 1;
            cArr[i2] = (char) (i3 & 127);
            i = TRIPLET_TO_CHARS[i];
            i2 = i4 + 1;
            cArr[i4] = (char) (i >> 16);
            i3 = i2 + 1;
            cArr[i2] = (char) ((i >> 8) & 127);
            i2 = i3 + 1;
            cArr[i3] = (char) (i & 127);
            return i2;
        } else if (i < 1000) {
            return _leading3(i, cArr, i2);
        } else {
            i3 = i / 1000;
            return _outputUptoMillion(cArr, i2, i3, i - (i3 * 1000));
        }
    }

    private static int _outputFullBillion(int i, char[] cArr, int i2) {
        int i3 = i / 1000;
        i -= i3 * 1000;
        int i4 = i3 / 1000;
        int i5 = TRIPLET_TO_CHARS[i4];
        int i6 = i2 + 1;
        cArr[i2] = (char) (i5 >> 16);
        i2 = i6 + 1;
        cArr[i6] = (char) ((i5 >> 8) & 127);
        i6 = i2 + 1;
        cArr[i2] = (char) (i5 & 127);
        i2 = TRIPLET_TO_CHARS[i3 - (i4 * 1000)];
        i3 = i6 + 1;
        cArr[i6] = (char) (i2 >> 16);
        i4 = i3 + 1;
        cArr[i3] = (char) ((i2 >> 8) & 127);
        i3 = i4 + 1;
        cArr[i4] = (char) (i2 & 127);
        i = TRIPLET_TO_CHARS[i];
        i2 = i3 + 1;
        cArr[i3] = (char) (i >> 16);
        i3 = i2 + 1;
        cArr[i2] = (char) ((i >> 8) & 127);
        i2 = i3 + 1;
        cArr[i3] = (char) (i & 127);
        return i2;
    }

    private static int _outputUptoBillion(int i, byte[] bArr, int i2) {
        int i3;
        if (i >= MILLION) {
            i3 = i / 1000;
            i -= i3 * 1000;
            int i4 = i3 / 1000;
            i3 -= i4 * 1000;
            i2 = _leading3(i4, bArr, i2);
            i3 = TRIPLET_TO_CHARS[i3];
            i4 = i2 + 1;
            bArr[i2] = (byte) (i3 >> 16);
            i2 = i4 + 1;
            bArr[i4] = (byte) (i3 >> 8);
            i4 = i2 + 1;
            bArr[i2] = (byte) i3;
            i = TRIPLET_TO_CHARS[i];
            i2 = i4 + 1;
            bArr[i4] = (byte) (i >> 16);
            i3 = i2 + 1;
            bArr[i2] = (byte) (i >> 8);
            i2 = i3 + 1;
            bArr[i3] = (byte) i;
            return i2;
        } else if (i < 1000) {
            return _leading3(i, bArr, i2);
        } else {
            i3 = i / 1000;
            return _outputUptoMillion(bArr, i2, i3, i - (i3 * 1000));
        }
    }

    private static int _outputFullBillion(int i, byte[] bArr, int i2) {
        int i3 = i / 1000;
        i -= i3 * 1000;
        int i4 = i3 / 1000;
        i3 -= i4 * 1000;
        i4 = TRIPLET_TO_CHARS[i4];
        int i5 = i2 + 1;
        bArr[i2] = (byte) (i4 >> 16);
        i2 = i5 + 1;
        bArr[i5] = (byte) (i4 >> 8);
        i5 = i2 + 1;
        bArr[i2] = (byte) i4;
        i2 = TRIPLET_TO_CHARS[i3];
        i3 = i5 + 1;
        bArr[i5] = (byte) (i2 >> 16);
        i4 = i3 + 1;
        bArr[i3] = (byte) (i2 >> 8);
        i3 = i4 + 1;
        bArr[i4] = (byte) i2;
        i = TRIPLET_TO_CHARS[i];
        i2 = i3 + 1;
        bArr[i3] = (byte) (i >> 16);
        i3 = i2 + 1;
        bArr[i2] = (byte) (i >> 8);
        i2 = i3 + 1;
        bArr[i3] = (byte) i;
        return i2;
    }

    private static int _outputUptoMillion(char[] cArr, int i, int i2, int i3) {
        int i4 = TRIPLET_TO_CHARS[i2];
        if (i2 > 9) {
            if (i2 > 99) {
                i2 = i + 1;
                cArr[i] = (char) (i4 >> 16);
                i = i2;
            }
            i2 = i + 1;
            cArr[i] = (char) ((i4 >> 8) & 127);
            i = i2;
        }
        i2 = i + 1;
        cArr[i] = (char) (i4 & 127);
        i = TRIPLET_TO_CHARS[i3];
        i3 = i2 + 1;
        cArr[i2] = (char) (i >> 16);
        i2 = i3 + 1;
        cArr[i3] = (char) ((i >> 8) & 127);
        i3 = i2 + 1;
        cArr[i2] = (char) (i & 127);
        return i3;
    }

    private static int _outputUptoMillion(byte[] bArr, int i, int i2, int i3) {
        int i4 = TRIPLET_TO_CHARS[i2];
        if (i2 > 9) {
            if (i2 > 99) {
                i2 = i + 1;
                bArr[i] = (byte) (i4 >> 16);
                i = i2;
            }
            i2 = i + 1;
            bArr[i] = (byte) (i4 >> 8);
            i = i2;
        }
        i2 = i + 1;
        bArr[i] = (byte) i4;
        i = TRIPLET_TO_CHARS[i3];
        i3 = i2 + 1;
        bArr[i2] = (byte) (i >> 16);
        i2 = i3 + 1;
        bArr[i3] = (byte) (i >> 8);
        i3 = i2 + 1;
        bArr[i2] = (byte) i;
        return i3;
    }

    private static int _leading3(int i, char[] cArr, int i2) {
        int i3 = TRIPLET_TO_CHARS[i];
        if (i > 9) {
            if (i > 99) {
                i = i2 + 1;
                cArr[i2] = (char) (i3 >> 16);
            } else {
                i = i2;
            }
            i2 = i + 1;
            cArr[i] = (char) ((i3 >> 8) & 127);
        }
        i = i2 + 1;
        cArr[i2] = (char) (i3 & 127);
        return i;
    }

    private static int _leading3(int i, byte[] bArr, int i2) {
        int i3 = TRIPLET_TO_CHARS[i];
        if (i > 9) {
            if (i > 99) {
                i = i2 + 1;
                bArr[i2] = (byte) (i3 >> 16);
            } else {
                i = i2;
            }
            i2 = i + 1;
            bArr[i] = (byte) (i3 >> 8);
        }
        i = i2 + 1;
        bArr[i2] = (byte) i3;
        return i;
    }

    private static int _full3(int i, char[] cArr, int i2) {
        i = TRIPLET_TO_CHARS[i];
        int i3 = i2 + 1;
        cArr[i2] = (char) (i >> 16);
        i2 = i3 + 1;
        cArr[i3] = (char) ((i >> 8) & 127);
        i3 = i2 + 1;
        cArr[i2] = (char) (i & 127);
        return i3;
    }

    private static int _full3(int i, byte[] bArr, int i2) {
        i = TRIPLET_TO_CHARS[i];
        int i3 = i2 + 1;
        bArr[i2] = (byte) (i >> 16);
        i2 = i3 + 1;
        bArr[i3] = (byte) (i >> 8);
        i3 = i2 + 1;
        bArr[i2] = (byte) i;
        return i3;
    }

    private static int _outputSmallestL(char[] cArr, int i) {
        int length = SMALLEST_LONG.length();
        SMALLEST_LONG.getChars(0, length, cArr, i);
        return i + length;
    }

    private static int _outputSmallestL(byte[] bArr, int i) {
        int length = SMALLEST_LONG.length();
        int i2 = 0;
        while (i2 < length) {
            int i3 = i + 1;
            bArr[i] = (byte) SMALLEST_LONG.charAt(i2);
            i2++;
            i = i3;
        }
        return i;
    }

    private static int _outputSmallestI(char[] cArr, int i) {
        int length = SMALLEST_INT.length();
        SMALLEST_INT.getChars(0, length, cArr, i);
        return i + length;
    }

    private static int _outputSmallestI(byte[] bArr, int i) {
        int length = SMALLEST_INT.length();
        int i2 = 0;
        while (i2 < length) {
            int i3 = i + 1;
            bArr[i] = (byte) SMALLEST_INT.charAt(i2);
            i2++;
            i = i3;
        }
        return i;
    }
}
