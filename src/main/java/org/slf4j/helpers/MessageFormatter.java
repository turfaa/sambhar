package org.slf4j.helpers;

import java.util.HashMap;
import java.util.Map;

public final class MessageFormatter {
    static final char DELIM_START = '{';
    static final char DELIM_STOP = '}';
    static final String DELIM_STR = "{}";
    private static final char ESCAPE_CHAR = '\\';

    public static final FormattingTuple format(String str, Object obj) {
        return arrayFormat(str, new Object[]{obj});
    }

    public static final FormattingTuple format(String str, Object obj, Object obj2) {
        return arrayFormat(str, new Object[]{obj, obj2});
    }

    static final Throwable getThrowableCandidate(Object[] objArr) {
        if (objArr == null || objArr.length == 0) {
            return null;
        }
        Object obj = objArr[objArr.length - 1];
        if (obj instanceof Throwable) {
            return (Throwable) obj;
        }
        return null;
    }

    public static final FormattingTuple arrayFormat(String str, Object[] objArr) {
        Throwable throwableCandidate = getThrowableCandidate(objArr);
        if (throwableCandidate != null) {
            objArr = trimmedCopy(objArr);
        }
        return arrayFormat(str, objArr, throwableCandidate);
    }

    private static Object[] trimmedCopy(Object[] objArr) {
        if (objArr == null || objArr.length == 0) {
            throw new IllegalStateException("non-sensical empty or null argument array");
        }
        int length = objArr.length - 1;
        Object[] objArr2 = new Object[length];
        System.arraycopy(objArr, 0, objArr2, 0, length);
        return objArr2;
    }

    public static final FormattingTuple arrayFormat(String str, Object[] objArr, Throwable th) {
        if (str == null) {
            return new FormattingTuple(null, objArr, th);
        }
        if (objArr == null) {
            return new FormattingTuple(str);
        }
        StringBuilder stringBuilder = new StringBuilder(str.length() + 50);
        int i = 0;
        int i2 = 0;
        while (i < objArr.length) {
            int indexOf = str.indexOf(DELIM_STR, i2);
            if (indexOf != -1) {
                if (!isEscapedDelimeter(str, indexOf)) {
                    stringBuilder.append(str, i2, indexOf);
                    deeplyAppendParameter(stringBuilder, objArr[i], new HashMap());
                    indexOf += 2;
                } else if (isDoubleEscaped(str, indexOf)) {
                    stringBuilder.append(str, i2, indexOf - 1);
                    deeplyAppendParameter(stringBuilder, objArr[i], new HashMap());
                    indexOf += 2;
                } else {
                    i--;
                    stringBuilder.append(str, i2, indexOf - 1);
                    stringBuilder.append(DELIM_START);
                    indexOf++;
                }
                i2 = indexOf;
                i++;
            } else if (i2 == 0) {
                return new FormattingTuple(str, objArr, th);
            } else {
                stringBuilder.append(str, i2, str.length());
                return new FormattingTuple(stringBuilder.toString(), objArr, th);
            }
        }
        stringBuilder.append(str, i2, str.length());
        return new FormattingTuple(stringBuilder.toString(), objArr, th);
    }

    static final boolean isEscapedDelimeter(String str, int i) {
        return i != 0 && str.charAt(i - 1) == ESCAPE_CHAR;
    }

    static final boolean isDoubleEscaped(String str, int i) {
        return i >= 2 && str.charAt(i - 2) == ESCAPE_CHAR;
    }

    private static void deeplyAppendParameter(StringBuilder stringBuilder, Object obj, Map<Object[], Object> map) {
        if (obj == null) {
            stringBuilder.append("null");
            return;
        }
        if (!obj.getClass().isArray()) {
            safeObjectAppend(stringBuilder, obj);
        } else if (obj instanceof boolean[]) {
            booleanArrayAppend(stringBuilder, (boolean[]) obj);
        } else if (obj instanceof byte[]) {
            byteArrayAppend(stringBuilder, (byte[]) obj);
        } else if (obj instanceof char[]) {
            charArrayAppend(stringBuilder, (char[]) obj);
        } else if (obj instanceof short[]) {
            shortArrayAppend(stringBuilder, (short[]) obj);
        } else if (obj instanceof int[]) {
            intArrayAppend(stringBuilder, (int[]) obj);
        } else if (obj instanceof long[]) {
            longArrayAppend(stringBuilder, (long[]) obj);
        } else if (obj instanceof float[]) {
            floatArrayAppend(stringBuilder, (float[]) obj);
        } else if (obj instanceof double[]) {
            doubleArrayAppend(stringBuilder, (double[]) obj);
        } else {
            objectArrayAppend(stringBuilder, (Object[]) obj, map);
        }
    }

    private static void safeObjectAppend(StringBuilder stringBuilder, Object obj) {
        try {
            stringBuilder.append(obj.toString());
        } catch (Throwable th) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("SLF4J: Failed toString() invocation on an object of type [");
            stringBuilder2.append(obj.getClass().getName());
            stringBuilder2.append("]");
            Util.report(stringBuilder2.toString(), th);
            stringBuilder.append("[FAILED toString()]");
        }
    }

    private static void objectArrayAppend(StringBuilder stringBuilder, Object[] objArr, Map<Object[], Object> map) {
        stringBuilder.append('[');
        if (map.containsKey(objArr)) {
            stringBuilder.append("...");
        } else {
            map.put(objArr, null);
            int length = objArr.length;
            for (int i = 0; i < length; i++) {
                deeplyAppendParameter(stringBuilder, objArr[i], map);
                if (i != length - 1) {
                    stringBuilder.append(", ");
                }
            }
            map.remove(objArr);
        }
        stringBuilder.append(']');
    }

    private static void booleanArrayAppend(StringBuilder stringBuilder, boolean[] zArr) {
        stringBuilder.append('[');
        int length = zArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(zArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void byteArrayAppend(StringBuilder stringBuilder, byte[] bArr) {
        stringBuilder.append('[');
        int length = bArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(bArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void charArrayAppend(StringBuilder stringBuilder, char[] cArr) {
        stringBuilder.append('[');
        int length = cArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(cArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void shortArrayAppend(StringBuilder stringBuilder, short[] sArr) {
        stringBuilder.append('[');
        int length = sArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(sArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void intArrayAppend(StringBuilder stringBuilder, int[] iArr) {
        stringBuilder.append('[');
        int length = iArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(iArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void longArrayAppend(StringBuilder stringBuilder, long[] jArr) {
        stringBuilder.append('[');
        int length = jArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(jArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void floatArrayAppend(StringBuilder stringBuilder, float[] fArr) {
        stringBuilder.append('[');
        int length = fArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(fArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }

    private static void doubleArrayAppend(StringBuilder stringBuilder, double[] dArr) {
        stringBuilder.append('[');
        int length = dArr.length;
        for (int i = 0; i < length; i++) {
            stringBuilder.append(dArr[i]);
            if (i != length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(']');
    }
}
