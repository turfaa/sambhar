package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.util.Map;

public class Code93Writer extends OneDimensionalCodeWriter {
    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Map<EncodeHintType, ?> map) throws WriterException {
        if (barcodeFormat == BarcodeFormat.CODE_93) {
            return super.encode(str, barcodeFormat, i, i2, map);
        }
        StringBuilder stringBuilder = new StringBuilder("Can only encode CODE_93, but got ");
        stringBuilder.append(barcodeFormat);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public boolean[] encode(String str) {
        int length = str.length();
        if (length <= 80) {
            int[] iArr = new int[9];
            boolean[] zArr = new boolean[((((str.length() + 2) + 2) * 9) + 1)];
            toIntArray(Code93Reader.CHARACTER_ENCODINGS[47], iArr);
            int i = 0;
            int appendPattern = appendPattern(zArr, 0, iArr, true);
            while (i < length) {
                toIntArray(Code93Reader.CHARACTER_ENCODINGS["0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*".indexOf(str.charAt(i))], iArr);
                appendPattern += appendPattern(zArr, appendPattern, iArr, true);
                i++;
            }
            length = computeChecksumIndex(str, 20);
            toIntArray(Code93Reader.CHARACTER_ENCODINGS[length], iArr);
            appendPattern += appendPattern(zArr, appendPattern, iArr, true);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*".charAt(length));
            toIntArray(Code93Reader.CHARACTER_ENCODINGS[computeChecksumIndex(stringBuilder.toString(), 15)], iArr);
            appendPattern += appendPattern(zArr, appendPattern, iArr, true);
            toIntArray(Code93Reader.CHARACTER_ENCODINGS[47], iArr);
            zArr[appendPattern + appendPattern(zArr, appendPattern, iArr, true)] = true;
            return zArr;
        }
        StringBuilder stringBuilder2 = new StringBuilder("Requested contents should be less than 80 digits long, but got ");
        stringBuilder2.append(length);
        throw new IllegalArgumentException(stringBuilder2.toString());
    }

    private static void toIntArray(int i, int[] iArr) {
        for (int i2 = 0; i2 < 9; i2++) {
            int i3 = 1;
            if (((1 << (8 - i2)) & i) == 0) {
                i3 = 0;
            }
            iArr[i2] = i3;
        }
    }

    protected static int appendPattern(boolean[] zArr, int i, int[] iArr, boolean z) {
        int length = iArr.length;
        int i2 = i;
        i = 0;
        while (i < length) {
            int i3 = i2 + 1;
            zArr[i2] = iArr[i] != 0;
            i++;
            i2 = i3;
        }
        return 9;
    }

    private static int computeChecksumIndex(String str, int i) {
        int i2 = 0;
        int i3 = 1;
        for (int length = str.length() - 1; length >= 0; length--) {
            i2 += "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%abcd*".indexOf(str.charAt(length)) * i3;
            i3++;
            if (i3 > i) {
                i3 = 1;
            }
        }
        return i2 % 47;
    }
}
