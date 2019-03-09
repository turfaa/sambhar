package com.google.zxing.aztec;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.aztec.encoder.AztecCode;
import com.google.zxing.aztec.encoder.Encoder;
import com.google.zxing.common.BitMatrix;
import java.nio.charset.Charset;
import java.util.Map;

public final class AztecWriter implements Writer {
    private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2) {
        return encode(str, barcodeFormat, i, i2, null);
    }

    public BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Map<EncodeHintType, ?> map) {
        Charset charset;
        int i3;
        int parseInt;
        Charset charset2 = DEFAULT_CHARSET;
        int i4 = 33;
        if (map != null) {
            if (map.containsKey(EncodeHintType.CHARACTER_SET)) {
                charset2 = Charset.forName(map.get(EncodeHintType.CHARACTER_SET).toString());
            }
            if (map.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                i4 = Integer.parseInt(map.get(EncodeHintType.ERROR_CORRECTION).toString());
            }
            if (map.containsKey(EncodeHintType.AZTEC_LAYERS)) {
                charset = charset2;
                i3 = i4;
                parseInt = Integer.parseInt(map.get(EncodeHintType.AZTEC_LAYERS).toString());
                return encode(str, barcodeFormat, i, i2, charset, i3, parseInt);
            }
            charset = charset2;
            i3 = i4;
        } else {
            charset = charset2;
            i3 = 33;
        }
        parseInt = 0;
        return encode(str, barcodeFormat, i, i2, charset, i3, parseInt);
    }

    private static BitMatrix encode(String str, BarcodeFormat barcodeFormat, int i, int i2, Charset charset, int i3, int i4) {
        if (barcodeFormat == BarcodeFormat.AZTEC) {
            return renderResult(Encoder.encode(str.getBytes(charset), i3, i4), i, i2);
        }
        StringBuilder stringBuilder = new StringBuilder("Can only encode AZTEC, but got ");
        stringBuilder.append(barcodeFormat);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    private static BitMatrix renderResult(AztecCode aztecCode, int i, int i2) {
        BitMatrix matrix = aztecCode.getMatrix();
        if (matrix != null) {
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            i = Math.max(i, width);
            i2 = Math.max(i2, height);
            int min = Math.min(i / width, i2 / height);
            int i3 = (i - (width * min)) / 2;
            int i4 = (i2 - (height * min)) / 2;
            BitMatrix bitMatrix = new BitMatrix(i, i2);
            i2 = 0;
            while (i2 < height) {
                int i5 = i3;
                int i6 = 0;
                while (i6 < width) {
                    if (matrix.get(i6, i2)) {
                        bitMatrix.setRegion(i5, i4, min, min);
                    }
                    i6++;
                    i5 += min;
                }
                i2++;
                i4 += min;
            }
            return bitMatrix;
        }
        throw new IllegalStateException();
    }
}
