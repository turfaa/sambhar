package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.util.Map;

public final class Code39Writer extends OneDimensionalCodeWriter {
    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Map<EncodeHintType, ?> map) throws WriterException {
        if (barcodeFormat == BarcodeFormat.CODE_39) {
            return super.encode(str, barcodeFormat, i, i2, map);
        }
        StringBuilder stringBuilder = new StringBuilder("Can only encode CODE_39, but got ");
        stringBuilder.append(barcodeFormat);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public boolean[] encode(String str) {
        int length = str.length();
        StringBuilder stringBuilder;
        if (length <= 80) {
            int[] iArr = new int[9];
            int i = length + 25;
            int i2 = 0;
            while (i2 < length) {
                int indexOf = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(str.charAt(i2));
                if (indexOf >= 0) {
                    toIntArray(Code39Reader.CHARACTER_ENCODINGS[indexOf], iArr);
                    indexOf = i;
                    for (i = 0; i < 9; i++) {
                        indexOf += iArr[i];
                    }
                    i2++;
                    i = indexOf;
                } else {
                    stringBuilder = new StringBuilder("Bad contents: ");
                    stringBuilder.append(str);
                    throw new IllegalArgumentException(stringBuilder.toString());
                }
            }
            boolean[] zArr = new boolean[i];
            toIntArray(Code39Reader.ASTERISK_ENCODING, iArr);
            i = OneDimensionalCodeWriter.appendPattern(zArr, 0, iArr, true);
            int[] iArr2 = new int[]{1};
            int appendPattern = i + OneDimensionalCodeWriter.appendPattern(zArr, i, iArr2, false);
            for (i = 0; i < length; i++) {
                toIntArray(Code39Reader.CHARACTER_ENCODINGS["0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. *$/+%".indexOf(str.charAt(i))], iArr);
                appendPattern += OneDimensionalCodeWriter.appendPattern(zArr, appendPattern, iArr, true);
                appendPattern += OneDimensionalCodeWriter.appendPattern(zArr, appendPattern, iArr2, false);
            }
            toIntArray(Code39Reader.ASTERISK_ENCODING, iArr);
            OneDimensionalCodeWriter.appendPattern(zArr, appendPattern, iArr, true);
            return zArr;
        }
        stringBuilder = new StringBuilder("Requested contents should be less than 80 digits long, but got ");
        stringBuilder.append(length);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    private static void toIntArray(int i, int[] iArr) {
        for (int i2 = 0; i2 < 9; i2++) {
            int i3 = 1;
            if (((1 << (8 - i2)) & i) != 0) {
                i3 = 2;
            }
            iArr[i2] = i3;
        }
    }
}
