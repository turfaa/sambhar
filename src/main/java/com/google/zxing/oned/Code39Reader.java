package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;
import java.util.Arrays;
import java.util.Map;

public final class Code39Reader extends OneDReader {
    static final String ALPHABET_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%";
    static final int ASTERISK_ENCODING;
    static final int[] CHARACTER_ENCODINGS;
    private static final String CHECK_DIGIT_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";
    private final int[] counters;
    private final StringBuilder decodeRowResult;
    private final boolean extendedMode;
    private final boolean usingCheckDigit;

    static {
        int[] iArr = new int[]{52, 289, 97, 352, 49, 304, 112, 37, 292, 100, 265, 73, 328, 25, 280, 88, 13, 268, 76, 28, 259, 67, 322, 19, 274, 82, 7, 262, 70, 22, 385, 193, 448, 145, 400, 208, 133, 388, 196, 148, 168, 162, 138, 42};
        CHARACTER_ENCODINGS = iArr;
        ASTERISK_ENCODING = iArr[39];
    }

    public Code39Reader() {
        this(false);
    }

    public Code39Reader(boolean z) {
        this(z, false);
    }

    public Code39Reader(boolean z, boolean z2) {
        this.usingCheckDigit = z;
        this.extendedMode = z2;
        this.decodeRowResult = new StringBuilder(20);
        this.counters = new int[9];
    }

    public Result decodeRow(int i, BitArray bitArray, Map<DecodeHintType, ?> map) throws NotFoundException, ChecksumException, FormatException {
        int[] iArr = this.counters;
        Arrays.fill(iArr, 0);
        StringBuilder stringBuilder = this.decodeRowResult;
        stringBuilder.setLength(0);
        int[] findAsteriskPattern = findAsteriskPattern(bitArray, iArr);
        int nextSet = bitArray.getNextSet(findAsteriskPattern[1]);
        int size = bitArray.getSize();
        while (true) {
            OneDReader.recordPattern(bitArray, nextSet, iArr);
            int toNarrowWidePattern = toNarrowWidePattern(iArr);
            if (toNarrowWidePattern >= 0) {
                int i2;
                char patternToChar = patternToChar(toNarrowWidePattern);
                stringBuilder.append(patternToChar);
                int i3 = nextSet;
                for (int i4 : iArr) {
                    i3 += i4;
                }
                int nextSet2 = bitArray.getNextSet(i3);
                if (patternToChar == '*') {
                    stringBuilder.setLength(stringBuilder.length() - 1);
                    i2 = 0;
                    for (int i32 : iArr) {
                        i2 += i32;
                    }
                    int i5 = (nextSet2 - nextSet) - i2;
                    if (nextSet2 == size || (i5 << 1) >= i2) {
                        if (this.usingCheckDigit) {
                            i5 = stringBuilder.length() - 1;
                            size = 0;
                            for (int i6 = 0; i6 < i5; i6++) {
                                size += CHECK_DIGIT_STRING.indexOf(this.decodeRowResult.charAt(i6));
                            }
                            if (stringBuilder.charAt(i5) == CHECK_DIGIT_STRING.charAt(size % 43)) {
                                stringBuilder.setLength(i5);
                            } else {
                                throw ChecksumException.getChecksumInstance();
                            }
                        }
                        if (stringBuilder.length() != 0) {
                            String decodeExtended;
                            if (this.extendedMode) {
                                decodeExtended = decodeExtended(stringBuilder);
                            } else {
                                decodeExtended = stringBuilder.toString();
                            }
                            float f = ((float) nextSet) + (((float) i2) / 2.0f);
                            r5 = new ResultPoint[2];
                            float f2 = (float) i;
                            r5[0] = new ResultPoint(((float) (findAsteriskPattern[1] + findAsteriskPattern[0])) / 2.0f, f2);
                            r5[1] = new ResultPoint(f, f2);
                            return new Result(decodeExtended, null, r5, BarcodeFormat.CODE_39);
                        }
                        throw NotFoundException.getNotFoundInstance();
                    }
                    throw NotFoundException.getNotFoundInstance();
                }
                nextSet = nextSet2;
            } else {
                throw NotFoundException.getNotFoundInstance();
            }
        }
    }

    private static int[] findAsteriskPattern(BitArray bitArray, int[] iArr) throws NotFoundException {
        int size = bitArray.getSize();
        int nextSet = bitArray.getNextSet(0);
        int length = iArr.length;
        int i = nextSet;
        int i2 = 0;
        int i3 = 0;
        while (nextSet < size) {
            if ((bitArray.get(nextSet) ^ i2) != 0) {
                iArr[i3] = iArr[i3] + 1;
            } else {
                int i4 = length - 1;
                if (i3 != i4) {
                    i3++;
                } else if (toNarrowWidePattern(iArr) == ASTERISK_ENCODING && bitArray.isRange(Math.max(0, i - ((nextSet - i) / 2)), i, false)) {
                    return new int[]{i, nextSet};
                } else {
                    i += iArr[0] + iArr[1];
                    int i5 = length - 2;
                    System.arraycopy(iArr, 2, iArr, 0, i5);
                    iArr[i5] = 0;
                    iArr[i4] = 0;
                    i3--;
                }
                iArr[i3] = 1;
                i2 ^= 1;
            }
            nextSet++;
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static int toNarrowWidePattern(int[] iArr) {
        int length = iArr.length;
        int i = 0;
        while (true) {
            int i2 = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            for (int i3 : iArr) {
                if (i3 < i2 && i3 > i) {
                    i2 = i3;
                }
            }
            int i4 = 0;
            int i5 = 0;
            int i32 = 0;
            for (i = 0; i < length; i++) {
                int i6 = iArr[i];
                if (i6 > i2) {
                    i5 |= 1 << ((length - 1) - i);
                    i4++;
                    i32 += i6;
                }
            }
            if (i4 == 3) {
                for (int i7 = 0; i7 < length && i4 > 0; i7++) {
                    i = iArr[i7];
                    if (i > i2) {
                        i4--;
                        if ((i << 1) >= i32) {
                            return -1;
                        }
                    }
                }
                return i5;
            } else if (i4 <= 3) {
                return -1;
            } else {
                i = i2;
            }
        }
    }

    private static char patternToChar(int i) throws NotFoundException {
        for (int i2 = 0; i2 < CHARACTER_ENCODINGS.length; i2++) {
            if (CHARACTER_ENCODINGS[i2] == i) {
                return ALPHABET_STRING.charAt(i2);
            }
        }
        throw NotFoundException.getNotFoundInstance();
    }

    private static String decodeExtended(CharSequence charSequence) throws FormatException {
        int length = charSequence.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            char charAt = charSequence.charAt(i);
            if (charAt == '+' || charAt == '$' || charAt == '%' || charAt == '/') {
                i++;
                char charAt2 = charSequence.charAt(i);
                if (charAt != '+') {
                    if (charAt != '/') {
                        switch (charAt) {
                            case '$':
                                if (charAt2 >= 'A' && charAt2 <= 'Z') {
                                    charAt = (char) (charAt2 - 64);
                                    break;
                                }
                                throw FormatException.getFormatInstance();
                                break;
                            case '%':
                                if (charAt2 < 'A' || charAt2 > 'E') {
                                    if (charAt2 >= 'F' && charAt2 <= 'W') {
                                        charAt = (char) (charAt2 - 11);
                                        break;
                                    }
                                    throw FormatException.getFormatInstance();
                                }
                                charAt = (char) (charAt2 - 38);
                                break;
                                break;
                            default:
                                charAt = 0;
                                break;
                        }
                    } else if (charAt2 >= 'A' && charAt2 <= 'O') {
                        charAt = (char) (charAt2 - 32);
                    } else if (charAt2 == 'Z') {
                        charAt = ':';
                    } else {
                        throw FormatException.getFormatInstance();
                    }
                } else if (charAt2 < 'A' || charAt2 > 'Z') {
                    throw FormatException.getFormatInstance();
                } else {
                    charAt = (char) (charAt2 + 32);
                }
                stringBuilder.append(charAt);
            } else {
                stringBuilder.append(charAt);
            }
            i++;
        }
        return stringBuilder.toString();
    }
}
