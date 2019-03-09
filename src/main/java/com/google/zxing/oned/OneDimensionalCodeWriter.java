package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.util.Map;

public abstract class OneDimensionalCodeWriter implements Writer {
    public abstract boolean[] encode(String str);

    public int getDefaultMargin() {
        return 10;
    }

    public final BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2) throws WriterException {
        return encode(str, barcodeFormat, i, i2, null);
    }

    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Map<EncodeHintType, ?> map) throws WriterException {
        if (str.isEmpty()) {
            throw new IllegalArgumentException("Found empty contents");
        } else if (i < 0 || i2 < 0) {
            StringBuilder stringBuilder = new StringBuilder("Negative size is not allowed. Input: ");
            stringBuilder.append(i);
            stringBuilder.append('x');
            stringBuilder.append(i2);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else {
            int defaultMargin = getDefaultMargin();
            if (map != null && map.containsKey(EncodeHintType.MARGIN)) {
                defaultMargin = Integer.parseInt(map.get(EncodeHintType.MARGIN).toString());
            }
            return renderResult(encode(str), i, i2, defaultMargin);
        }
    }

    private static BitMatrix renderResult(boolean[] zArr, int i, int i2, int i3) {
        int length = zArr.length;
        i3 += length;
        i = Math.max(i, i3);
        i2 = Math.max(1, i2);
        i3 = i / i3;
        int i4 = (i - (length * i3)) / 2;
        BitMatrix bitMatrix = new BitMatrix(i, i2);
        int i5 = i4;
        i4 = 0;
        while (i4 < length) {
            if (zArr[i4]) {
                bitMatrix.setRegion(i5, 0, i3, i2);
            }
            i4++;
            i5 += i3;
        }
        return bitMatrix;
    }

    protected static int appendPattern(boolean[] zArr, int i, int[] iArr, boolean z) {
        int length = iArr.length;
        int i2 = i;
        boolean z2 = z;
        i = 0;
        int i3 = 0;
        while (i < length) {
            int i4 = iArr[i];
            int i5 = i2;
            i2 = 0;
            while (i2 < i4) {
                int i6 = i5 + 1;
                zArr[i5] = z2;
                i2++;
                i5 = i6;
            }
            i3 += i4;
            z2 = !z2;
            i++;
            i2 = i5;
        }
        return i3;
    }
}
